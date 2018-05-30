#include <string>
#include <exception>

#include <android/asset_manager_jni.h>
#include "pcre.h"

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



class PCRE {
    pcre* regex;
    pcre_extra* study;
    bool expected;  // true: must match; false: mustn't
    bool valid;

public:
    PCRE(const std::string& re, bool exp) : expected(exp), valid(true) {
        const char* pcreError;
        int errorOffset;
        regex = pcre_compile(re.c_str(), PCRE_UTF8 | PCRE_UCP, &pcreError, &errorOffset, NULL);
        if(!regex) {
            using namespace std::string_literals;
            throw std::runtime_error("Bad regex: "s + pcreError);
        }
        study = pcre_study(regex, PCRE_STUDY_JIT_COMPILE, &pcreError);
    }

    PCRE(const PCRE&) = delete;

    PCRE(PCRE&& other) : regex(other.regex), study(other.study), expected(other.expected), valid(other.valid) {
        other.valid = false;
    }

    ~PCRE() {
        if(!valid)
            return;
        if(study) pcre_free_study(study);
        pcre_free(regex);
    }

    bool test(const std::string& target) {
        int r = pcre_exec(regex, study, target.c_str(), (int)target.length(), 0, 0, NULL, 0);
        return expected ? (r >= 0) : (r < 0);
    }
};