#include <jni.h>
#include <android/asset_manager_jni.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include "pcre.h"

#define PATSZ 256
#define FNSZ 256
#define LINESZ 100
#define MAXPAT 3
#define MAXRES 10000

#define ERR_PCRE    1
#define ERR_PTHREAD 2
#define ERR_NUMPAT  3
#define ERR_ASSET   4
#define ERR_READ    5

AAssetManager *mgr;

pthread_t t;
char fn[FNSZ];
const char *pcreerror;
int stop;
int refcount = 0;
int run = 0, err = 0;
int size = 0, pos = 0, matches = 0;

typedef struct {
	pcre *cmp;
	pcre_extra *extra;
	char pat[PATSZ];
	int act;
	int val;
} pattern;

pattern p[MAXPAT];

char *res[MAXRES] = {0};

void startThread(void*);
void stopThread();
void *thr(void*);


JNIEXPORT void JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_init(JNIEnv *env, jobject obj, jobject jmgr, jstring jfn) {
	const char *cfn = (*env)->GetStringUTFChars(env, jfn, 0);
	strcpy(fn, cfn);
	(*env)->ReleaseStringUTFChars(env, jfn, cfn);
	mgr = AAssetManager_fromJava(env, jmgr);
	refcount++;
}

JNIEXPORT void JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_free(JNIEnv *env, jobject obj) {
	int i;
	stopThread();
	size = 0;
	pos = 0;
	matches = 0;
	for(i = 0; i < MAXRES; i++) {
		if(res[i]) free(res[i]);
	}
	memset(res, 0, sizeof(res));
}

JNIEXPORT void JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_nativeFinalize(JNIEnv *env, jobject obj) {
	if(!--refcount)
		Java_cz_absolutno_sifry_regexp_RegExpNative_free(env, obj);
}

JNIEXPORT void JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_startThread(JNIEnv *env , jobject obj, jobjectArray joa) {
	int i, n;

	if(run) stopThread();
	n = (*env)->GetArrayLength(env, joa);
	if(n > MAXPAT) {
		err = ERR_NUMPAT;
		return;
	}
	for(i = 0; i < n; i++) {
		jstring js = (jstring)((*env)->GetObjectArrayElement(env, joa, i));
		const char *str = (*env)->GetStringUTFChars(env, js, 0);
		if(str[0] == '!') {
			strncpy(p[i].pat, str+1, PATSZ-1);
			p[i].val = 0;
		} else {
			strncpy(p[i].pat, str, PATSZ-1);
			p[i].val = 1;
		}
		if(p[i].pat[0]) p[i].act = 1;
		else p[i].act = 0;
		(*env)->ReleaseStringUTFChars(env, js, str);
	}
	for(; i < MAXPAT; i++) {
		p[i].act = 0;
	}
	startThread(p);
}

JNIEXPORT void JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_stopThread(JNIEnv *env , jobject obj) {
	stopThread();
}


JNIEXPORT jboolean JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_isRunning(JNIEnv *env , jobject obj) {
	return run?JNI_TRUE:JNI_FALSE;
}

JNIEXPORT jintArray JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_getProgress(JNIEnv *env , jobject obj) {
	jint tmp[5] = {run, err, matches, pos, size};
	jintArray ret;
	ret = (*env)->NewIntArray(env, 5);
	(*env)->SetIntArrayRegion(env, ret, 0, 5, tmp);
	return ret;
}

JNIEXPORT jstring JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_getResult(JNIEnv *env, jobject obj, jint ix) {
	if(ix < 0 || ix >= matches) return NULL;
	return (*env)->NewStringUTF(env, res[ix]);
}

JNIEXPORT jstring JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_getError(JNIEnv *env, jobject obj) {
	switch(err) {
	case ERR_PCRE:    return (*env)->NewStringUTF(env, pcreerror);
	case ERR_PTHREAD: return (*env)->NewStringUTF(env, "pthread_create failed");
	case ERR_NUMPAT:  return (*env)->NewStringUTF(env, "Illegal number of patterns");
	case ERR_ASSET:   return (*env)->NewStringUTF(env, "Cannot find asset");
	case ERR_READ:    return (*env)->NewStringUTF(env, "Read error");
	default: return NULL;
	}
}


void startThread(void *data) {
	if(run) return;
	stop = 0;
	if(!pthread_create(&t, NULL, thr, data)) { run = 1; err = 0; }
	else { run = 0; err = ERR_PTHREAD; }
}

void stopThread() {
	if(!run) return;
	stop = 1;
	pthread_join(t, NULL);
	run = 0;
}


void *thr(void *data) {
	AAsset *a;
	pattern *p = (pattern*)data;
	int erroroffset;
	char buf[LINESZ];
	int o1, o2, o3; /* Buffer start, end, line break */
	int i, r;

	/* Clear matches */
	matches = 0;
	for(i = 0; i < MAXRES; i++) {
		if(res[i]) free(res[i]);
	}
	memset(res, 0, sizeof(res));

	/* Initialize asset and PCRE */
	a = AAssetManager_open(mgr, fn, AASSET_MODE_STREAMING);
	if(a == NULL) {
		err = ERR_ASSET;
		return NULL;
	}
	for(i = 0; i < MAXPAT; i++) {
		if(p[i].act) {
			p[i].cmp = pcre_compile(p[i].pat, PCRE_UTF8 | PCRE_UCP, &pcreerror, &erroroffset, NULL);
			if(!p[i].cmp) {
				err = ERR_PCRE;
				run = 0;
				AAsset_close(a);
				return NULL;
			}
			p[i].extra = pcre_study(p[i].cmp, 0, &pcreerror);
		}
	}

	size = AAsset_getLength(a);
	o1 = 0;
	o2 = 0;
	while(!stop) {
		/* Read one line between o1 and o2; o3 = current end of cached input */
		for(;;) {
			for(o3 = o1; o3 < o2; o3++)
				if(buf[o3] == '\n') break;
			if(o3 < o2) break;
			memmove(buf, buf+o1, o2-o1);
			o2 -= o1;
			o1 = 0;
			if(o2 == LINESZ) {
				err = ERR_READ; /* Line too long */
				break;
			}
			r = AAsset_read(a, buf+o2, LINESZ-o2);
			if(r == 0) {
				break; /* EOF */
			} else if(r < 0) {
				err = ERR_READ;
				break;
			} else {
				o2 += r;
			}
		}
		if(o1 == o2 && !AAsset_getRemainingLength(a)) break; /* EOF */
		if(err) break;
		if(o3 == o1) continue; /* Empty line */

		/* Pattern matching */
		for(i = 0; i < MAXPAT; i++) {
			if(!p[i].act) continue;
			r = pcre_exec(p[i].cmp, p[i].extra, buf+o1, o3-o1, 0, 0, NULL, 0);
			if(r < 0 && p[i].val == 1) break;
			if(r >= 0 && p[i].val == 0) break;
		}
		/* All tests passed = match found */
		if(i == MAXPAT) {
			if(matches < MAXRES) {
				while(buf[o1++] != ':') {};
				res[matches] = malloc(o3-o1+1);
				strncpy(res[matches], buf+o1, o3-o1);
				res[matches][o3-o1] = 0;
			}
			matches++;
		}

		/* Discard the line */
		o1 = o3+1;
		pos = size - AAsset_getRemainingLength(a);
	}

	/* Free PCRE data and asset */
	for(i = 0; i < MAXPAT; i++) {
		if(p[i].act) {
			if(p[i].extra) pcre_free_study(p[i].extra);
			if(p[i].cmp) pcre_free(p[i].cmp);
		}
	}
	AAsset_close(a);
	run = 0;
	/* Exit the thread */
	return NULL;
}
