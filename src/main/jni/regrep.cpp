#include <string>
#include <vector>
#include <utility>
#include <regex>
#include <thread>

#include <jni.h>
#include <android/asset_manager_jni.h>

constexpr std::size_t maxSavedMatches = 10000;

enum class Error {
    NONE = 0,
    REGEX,
    THREAD,
    NUMPAT,
    ASSET,
    READ
};

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
public:
    std::vector<std::string> matches{};
    volatile std::atomic_bool stopFlag{false};
    bool running{false};
    Error err{Error::NONE};
    std::size_t size{0}, pos{0}, matchCount{0};
    std::thread worker{};

public:
    ~Context() {
        if(worker.joinable())
            worker.detach();
    }

    void start(AssetRef&& asset, std::vector<std::string>&& patterns);
    void stop();

    void free() {
        stop();
        matches.clear();
        matchCount = size = pos = 0;
    }
};

void threadMain(Context* ctx, AssetRef&& asset, std::vector<std::string>&& patterns);


void Context::start(AssetRef&& asset, std::vector<std::string>&& patterns) {
    stop();
    try {
        worker = std::thread{threadMain, this, std::move(asset), std::move(patterns)};
    }
    catch(const std::system_error&) {
        err = Error::THREAD;
    }
}

void Context::stop() {
    if(!worker.joinable())
        return;
    stopFlag = true;
    worker.join();
    stopFlag = false;
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
    storeContext(env, obj, new Context());
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
    ctx->start(AssetRef{env, jmgr, strFromJava(env, jfn)}, std::move(patterns));
}

JNIEXPORT void JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_stopThread(JNIEnv *env , jobject obj) {
    getContext(env, obj)->stop();
}

JNIEXPORT jboolean JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_isRunning(JNIEnv *env , jobject obj) {
    return static_cast<jboolean>(getContext(env, obj)->running ? JNI_TRUE : JNI_FALSE);
}

JNIEXPORT jintArray JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_getProgress(JNIEnv *env , jobject obj) {
    Context* ctx = getContext(env, obj);
    jint tmp[5] = {ctx->running, (int)(ctx->err), (int)ctx->matchCount, (int)ctx->pos, (int)ctx->size};
    jintArray ret;
    ret = env->NewIntArray(5);
    env->SetIntArrayRegion(ret, 0, 5, tmp);
    return ret;
}

JNIEXPORT jstring JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_getResult(JNIEnv *env, jobject obj, jint ix) {
    Context* ctx = getContext(env, obj);
    if(ix < 0 || ix >= ctx->matches.size()) return NULL;
    return env->NewStringUTF(ctx->matches[ix].c_str());
}

JNIEXPORT jstring JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_getError(JNIEnv *env, jobject obj) {
    Context* ctx = getContext(env, obj);
    switch(ctx->err) {
        //case Error::PCRE:    return env->NewStringUTF(pcreerror);
        case Error::REGEX:   return env->NewStringUTF("bad regex");
        case Error::THREAD:  return env->NewStringUTF("pthread_create failed");
        /*case Error::NUMPAT:  return env->NewStringUTF("Illegal number of patterns");*/
        case Error::ASSET:   return env->NewStringUTF("Cannot find asset");
        case Error::READ:    return env->NewStringUTF("Read error");
        default: return NULL;
    }
}

}


void threadMain(Context* ctx, AssetRef&& asset, std::vector<std::string>&& patterns) {
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

        ctx->size = size_t(AAsset_getLength(asset));
        ctx->pos = 0;
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
                    if(++ctx->matchCount <= maxSavedMatches)
                        ctx->matches.push_back(line.substr(pos + 1));
                }
            }

            ctx->pos = ctx->size - AAsset_getRemainingLength(asset);
        }
    } catch (const std::regex_error& e) {
        ctx->err = Error::REGEX;
    } catch (const std::runtime_error& e) {
        ctx->err = Error::ASSET;
    }
    ctx->running = false;
}