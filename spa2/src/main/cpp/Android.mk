LOCAL_PATH_SAFE := $(call my-dir)
LOCAL_PATH := $(LOCAL_PATH_SAFE)/pcre-mini

include $(CLEAR_VARS)

LOCAL_MODULE := libpcre

LOCAL_SRC_FILES :=  \
  pcre_compile.c \
  pcre_config.c \
  pcre_dfa_exec.c \
  pcre_exec.c \
  pcre_fullinfo.c \
  pcre_get.c \
  pcre_globals.c \
  pcre_maketables.c \
  pcre_newline.c \
  pcre_ord2utf8.c \
  pcre_refcount.c \
  pcre_study.c \
  pcre_tables.c \
  pcre_ucd.c \
  pcre_valid_utf8.c \
  pcre_version.c \
  pcre_xclass.c \
  pcre_chartables.c

LOCAL_CFLAGS += -O3 -I. -DHAVE_CONFIG_H

include $(BUILD_STATIC_LIBRARY)


LOCAL_PATH := $(LOCAL_PATH_SAFE)

include $(CLEAR_VARS)

LOCAL_MODULE := regrep
LOCAL_LDFLAGS := -L$(LOCAL_PATH)/../jniLibs/$(TARGET_ARCH_ABI) -fuse-ld=bfd
LOCAL_LDLIBS := -landroid
LOCAL_STATIC_LIBRARIES := libpcre
LOCAL_SDK_VERSION := 23

LOCAL_SRC_FILES := regrep.c

include $(BUILD_SHARED_LIBRARY)
