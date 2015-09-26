#include <jni.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <zlib.h>
#include "pcre.h"

#define BUFSZ 4096
#define PATSZ 256
#define FNSZ 256
#define LINESZ 100
#define N 3
#define OVECSZ 3
#define MAXRES 10000

pthread_t t;
char fn[FNSZ];
const char *pcreerror;
int stop;
int refcount = 0;
int run = 0, err = 0;
int ts = 0, ps = 0, mc = 0, prg = 0;

typedef struct {
	pcre *cmp;
	pcre_extra *extra;
	char pat[PATSZ];
	int act;
	int val;
} pattern;

pattern p[N];

char *res[MAXRES] = {0};

void startThread(void*);
void stopThread();
void *thr(void*);


JNIEXPORT void JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_init(JNIEnv *env, jobject obj, jstring jfn) {
	const char *cfn = (*env)->GetStringUTFChars(env, jfn, 0);
	strcpy(fn, cfn);
	(*env)->ReleaseStringUTFChars(env, jfn, cfn);
	refcount++;
}

JNIEXPORT void JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_free(JNIEnv *env, jobject obj) {
	int i;
	stopThread();
	ts = 0;
	ps = 0;
	mc = 0;
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
	if(n > N) {
		err = 3;
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
	for(; i < N; i++) {
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
	jint tmp[6] = {prg, mc, ts, ps, run, err};
	jintArray ret;
	ret = (*env)->NewIntArray(env, 6);
	(*env)->SetIntArrayRegion(env, ret, 0, 6, tmp);
	return ret;
}

JNIEXPORT jstring JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_getResult(JNIEnv *env, jobject obj, jint ix) {
	if(ix < 0 || ix >= mc) return NULL;
	return (*env)->NewStringUTF(env, res[ix]);
}

JNIEXPORT jstring JNICALL Java_cz_absolutno_sifry_regexp_RegExpNative_getError(JNIEnv *env, jobject obj) {
	switch(err) {
	case 1: return (*env)->NewStringUTF(env, pcreerror);
	case 2: return (*env)->NewStringUTF(env, "pthread_create failed");
	case 3: return (*env)->NewStringUTF(env, "Illegal number of patterns");
	case 4: return (*env)->NewStringUTF(env, fn);
	case 5: return (*env)->NewStringUTF(env, "Failed to open gzip stream");
	default: return NULL;
	}
}


void startThread(void *data) {
	if(run) return;
	stop = 0;
	if(!pthread_create(&t, NULL, thr, data)) { run = 1; err = 0; }
	else { run = 0; err = 2; }
}

void stopThread() {
	if(!run) return;
	stop = 1;
	pthread_join(t, NULL);
	run = 0;
}


void *thr(void *data) {
	pattern *p = (pattern*)data;
	int erroroffset;
	char buf[LINESZ];
	int ovec[OVECSZ];
	int i, r, ll;
	int f;
	gzFile g;

	mc = 0;
	prg = 0;
	for(i = 0; i < MAXRES; i++) {
		if(res[i]) free(res[i]);
	}
	memset(res, 0, sizeof(res));
	for(i = 0; i < N; i++) {
		if(p[i].act) {
			p[i].cmp = pcre_compile(p[i].pat, PCRE_UTF8 | PCRE_UCP, &pcreerror, &erroroffset, NULL);
			if(!p[i].cmp) {
				err = 1;
				run = 0;
				return NULL;
			}
			p[i].extra = pcre_study(p[i].cmp, 0, &pcreerror);
		}
	}

	f = open(fn, O_RDONLY);
	if(f < 0) {
		err = 4;
		strcpy(fn, strerror(errno));
		return NULL;
	}
	ts = (int)lseek(f, 0, SEEK_END);
	lseek(f, 0, SEEK_SET);
	g = gzdopen(f, "r");
	if(!g) {
		close(f);
		err = 5;
		return NULL;
	}
	while(!stop) {
		if(!gzgets(g, buf, LINESZ)) break;
		ll = strlen(buf);
		if(!ll) continue;
		prg = ((int)(buf[0]-'a')*26) + (buf[1]==':'?0:buf[1]-'a');
		for(i = 0; i < N; i++) {
			if(!p[i].act) continue;
			r = pcre_exec(p[i].cmp, p[i].extra, buf, ll, 0, 0, ovec, OVECSZ);
			if(r < 0 && p[i].val == 1) break;
			if(r >= 0 && p[i].val == 0) break;
		}
		if(i == N) {
			if(mc < MAXRES) {
				for(i = 0; i < ll; i++)
					if(buf[i] == ':') break;
				i++;
				res[mc] = malloc(ll - i);
				strncpy(res[mc], buf+i, ll-i-1);
				res[mc][ll-i-1] = 0;
			}
			mc++;
		}
		ps = (int)lseek(f, 0, SEEK_CUR);
	}
	for(i = 0; i < N; i++) {
		if(p[i].act) {
			if(p[i].extra) pcre_free_study(p[i].extra);
			if(p[i].cmp) pcre_free(p[i].cmp);
		}
	}
	gzclose(g);
	run = 0;
	return NULL;
}
