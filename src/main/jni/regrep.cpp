#include <string>
#include <vector>
#include <utility>
#include <regex>
#include <thread>
#include <mutex>

#include <jni.h>
#include <android/asset_manager_jni.h>

constexpr const char* RetClass = "cz/absolutno/sifry/regexp/RegExpNative$Report";

class AssetRef {
    JavaVM* jvm;
    jobject jamgr;
    AAsset* asset;
    bool valid;

public:
    AssetRef() : valid(false) { }

    AssetRef(JNIEnv* env, jobject jamgr_, const std::string& fname) : valid(true) {
        env->GetJavaVM(&jvm);
        jamgr = env->NewGlobalRef(jamgr_);
        asset = AAssetManager_open(AAssetManager_fromJava(env, jamgr), fname.c_str(), AASSET_MODE_STREAMING);
        if (asset == NULL) {
            env->DeleteGlobalRef(jamgr);
            throw std::runtime_error{"Can't open asset"};
        }
    }

    AssetRef(const AssetRef&) = delete;

    AssetRef(AssetRef&& other) : jvm(other.jvm), jamgr(other.jamgr), asset(other.asset), valid(other.valid) {
        other.valid = false;
    }

    ~AssetRef() {
        if(valid) {
            JNIEnv* env;
            AAsset_close(asset);
            jvm->AttachCurrentThread(&env, NULL);
            env->DeleteGlobalRef(jamgr);
        }
    }

    operator AAsset*() {
        return asset;
    }
};

class Context {
    std::thread worker{};
    volatile std::atomic_bool stopFlag{false};
    bool running{false};

    std::vector<std::string> matches{};
    unsigned maxListResults;
    unsigned matchCount{0};  // the full count: the vector will only store first maxSavedMatches
    std::string error{};
    float progress{0};

    std::mutex progressMutex{};

    struct Report {
        bool running;
        float progress;
        bool error;
        unsigned matchCount;
    };

public:
    Context(unsigned max) : maxListResults(max) { }

    ~Context() {
        if(worker.joinable())
            worker.detach();
    }

    void start(AssetRef&& asset, std::vector<std::string>&& patterns);
    void stop();
    void free();
    void fail(const std::string& err);

    Report report();

    const std::string& getError() {
        return error;
    }

    std::string getMatch(std::size_t index);

private:
    static void threadMain(Context* ctx, AssetRef&& asset, std::vector<std::string>&& patterns);
};


void Context::start(AssetRef&& asset, std::vector<std::string>&& patterns) {
    free();
    try {
        worker = std::thread{threadMain, this, std::move(asset), std::move(patterns)};
    }
    catch(const std::system_error&) {
        error = "Unable to start thread";
    }
}

void Context::stop() {
    if(!worker.joinable())
        return;
    stopFlag = true;
    worker.join();
    stopFlag = false;
}

void Context::free() {
    stop();
    matches.clear();
    matchCount = 0;
    progress = 0;
    error = "";
}

void Context::fail(const std::string &err) {
    free();
    error = err;
}

Context::Report Context::report() {
    std::lock_guard<std::mutex> lock{progressMutex};
    return {running, progress, !error.empty(), matchCount};
}

std::string Context::getMatch(std::size_t index) {
    std::lock_guard<std::mutex> lock{progressMutex};
    return index < matches.size() ? matches[index] : "";
}


std::string strFromJava(JNIEnv *env, jstring jfn) {
    const char *cfn = env->GetStringUTFChars(jfn, 0);
    std::string ret{cfn};
    env->ReleaseStringUTFChars(jfn, cfn);
    return ret;
}

Context *getContext(JNIEnv *env, jobject obj) {
    jclass cls = env->GetObjectClass(obj);
    jfieldID fld = env->GetFieldID(cls, "nativeContext", "J");
    void *addr = reinterpret_cast<void *>(env->GetLongField(obj, fld));
    return static_cast<Context *>(addr);
}

void storeContext(JNIEnv *env, jobject obj, Context *ctx) {
    jclass cls = env->GetObjectClass(obj);
    jfieldID fld = env->GetFieldID(cls, "nativeContext", "J");
    jlong addr = reinterpret_cast<jlong>(ctx);
    env->SetLongField(obj, fld, addr);
}


extern "C" {

JNIEXPORT void JNICALL
Java_cz_absolutno_sifry_regexp_RegExpNative_init(JNIEnv *env, jobject obj) {
    jclass cls = env->GetObjectClass(obj);
    jfieldID fld = env->GetStaticFieldID(cls, "MaxListResults", "I");
    unsigned maxListResults = (unsigned)env->GetStaticIntField(cls, fld);
    storeContext(env, obj, new Context(maxListResults));
}

JNIEXPORT void JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_free(JNIEnv *env, jobject obj) {
    getContext(env, obj)->free();
}

JNIEXPORT void JNICALL
Java_cz_absolutno_sifry_regexp_RegExpNative_nativeFinalize(JNIEnv *env, jobject obj) {
    delete getContext(env, obj);
}

JNIEXPORT void JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_startThread(JNIEnv *env, jobject obj, jobject jmgr, jstring jfn, jobjectArray joa) {
    Context* ctx = getContext(env, obj);
    ctx->stop();
    jsize n = env->GetArrayLength(joa);
    std::vector<std::string> patterns{};
    for(jsize i = 0; i < n; i++) {
        jobject sobj = env->GetObjectArrayElement(joa, i);
        jstring* js = reinterpret_cast<jstring*>(&sobj);
        patterns.push_back(strFromJava(env, *js));
    }
    try {
        ctx->start(AssetRef{env, jmgr, strFromJava(env, jfn)}, std::move(patterns));
    } catch(std::runtime_error& e) {
        ctx->fail(e.what());
    }
}

JNIEXPORT void JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_stopThread(JNIEnv *env , jobject obj) {
    getContext(env, obj)->stop();
}

JNIEXPORT jboolean JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_isRunning(JNIEnv *env , jobject obj) {
    return static_cast<jboolean>(getContext(env, obj)->report().running ? JNI_TRUE : JNI_FALSE);
}

JNIEXPORT jobject JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_getProgress(JNIEnv *env , jobject obj) {
    Context* ctx = getContext(env, obj);
    auto report = ctx->report();
    jclass cls = env->FindClass(RetClass);
    jmethodID ctor = env->GetMethodID(cls, "<init>", "()V");
    jobject ret = env->NewObject(cls, ctor);
    jfieldID fld = env->GetFieldID(cls, "running", "Z");
    env->SetBooleanField(ret, fld, (jboolean)report.running);
    fld = env->GetFieldID(cls, "error", "Z");
    env->SetBooleanField(ret, fld, (jboolean)report.error);
    fld = env->GetFieldID(cls, "matches", "I");
    env->SetIntField(ret, fld, (jint)report.matchCount);
    fld = env->GetFieldID(cls, "progress", "F");
    env->SetFloatField(ret, fld, report.progress);
    return ret;
}

JNIEXPORT jstring JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_getResult(JNIEnv *env, jobject obj, jint index) {
    Context* ctx = getContext(env, obj);
    return env->NewStringUTF(ctx->getMatch(std::size_t(index)).c_str());
}

JNIEXPORT jstring JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_getError(JNIEnv *env, jobject obj) {
    Context* ctx = getContext(env, obj);
    return env->NewStringUTF(ctx->getError().c_str());
}

}


void Context::threadMain(Context* ctx, AssetRef&& asset, std::vector<std::string>&& patterns) {
    constexpr unsigned bufAlloc = 1024;
    char buffer[bufAlloc];
    unsigned bufPos = 0, bufSize = 0;

    try {
        std::vector<std::pair<std::regex, bool>> REs;
        for (auto &pat : patterns) {
            bool inv = false;
            if (pat[0] == '!') {
                inv = true;
                pat = pat.substr(1);
            }
            if (pat.empty())
                continue;
            std::regex re{pat, std::regex_constants::optimize | std::regex_constants::collate};
            REs.push_back({std::move(re), !inv});
        }

        ctx->matches.clear();
        ctx->matchCount = 0;
        ctx->running = true;

        while (!ctx->stopFlag) {
            /* Read one line */
            std::string line{};
            for (;;) {
                unsigned x;
                for (x = bufPos; x < bufSize; x++)
                    if (buffer[x] == '\n')
                        break;
                if (x < bufSize) {
                    line += std::string{buffer + bufPos, x - bufPos};
                    bufPos = x + 1;
                    break;
                }
                /* else: buffer ended before finding \n */
                line += std::string{buffer + bufPos, bufSize - bufPos};
                int r = AAsset_read(asset, buffer, bufAlloc);
                if (r > 0) /* more data */ {
                    bufPos = 0;
                    bufSize = unsigned(r);
                } else if (r == 0) /* EOF*/ {
                    break;
                } else /* error */ {
                    throw std::runtime_error("Can't read asset");
                }
            }

            if (line.empty() && AAsset_getRemainingLength(asset) == 0) // EOF
                break;
            if (line.empty()) // empty line
                continue;

            auto it = REs.begin();
            for (; it != REs.end(); it++)
                if (std::regex_search(line, it->first) != it->second)
                    break;
            if (it == REs.end()) { // all tests passed
                auto pos = line.find(':');
                if(pos != std::string::npos) {
                    std::lock_guard<std::mutex> lock{ctx->progressMutex};
                    if(++ctx->matchCount <= ctx->maxListResults)
                        ctx->matches.push_back(line.substr(pos + 1));
                }
            }

            std::size_t assetSize = size_t(AAsset_getLength(asset));
            std::size_t assetPos = assetSize - AAsset_getRemainingLength(asset);
            ctx->progress = (float)(assetPos) / assetSize;
        }
    } catch (const std::regex_error& e) {
        using namespace std::string_literals;
        ctx->error = "Bad regex: "s + e.what();
    } catch (const std::runtime_error& e) {
        ctx->error = e.what();
    }
    ctx->running = false;
}