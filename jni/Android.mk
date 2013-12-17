WHEATLEY_PATH := $(call my-dir)

include jni/external/libffi.mk
include jni/external/wayland.mk
include jni/external/pixman.mk
include jni/external/libwlb.mk

LOCAL_PATH := $(WHEATLEY_PATH)

include $(CLEAR_VARS)

LOCAL_MODULE := wheatley
LOCAL_CFLAGS :=
LOCAL_LDLIBS := -llog -landroid -lEGL -lGLESv2
LOCAL_SRC_FILES := compositor_activity.c
LOCAL_STATIC_LIBRARIES := android_native_app_glue wlb

wheatley: wlb

include $(BUILD_SHARED_LIBRARY)

include $(LOCAL_PATH)/test-clients/Android.mk

$(call import-module, android/native_app_glue)
