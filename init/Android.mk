LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_C_INCLUDES := \
    system/core/base/include \
    system/core/init
LOCAL_SRC_FILES := init_raphael.cpp
LOCAL_MODULE := libinit_raphael

include $(BUILD_STATIC_LIBRARY)
