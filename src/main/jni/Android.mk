LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := regrep
LOCAL_LDFLAGS := -L$(LOCAL_PATH)/../jniLibs/$(TARGET_ARCH_ABI)
LOCAL_LDLIBS := -lz -lpcre

LOCAL_SRC_FILES := regrep.c
LOCAL_SHARED_LIBRARIES := libpcre

include $(BUILD_SHARED_LIBRARY)
