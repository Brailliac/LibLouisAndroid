LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := louis

LOCAL_C_INCLUDES := $(LOCAL_PATH) liblouis/liblouis
LOCAL_CFLAGS := -Wall -DTABLESDIR=\"liblouis/tables\"
LOCAL_LDLIBS := -llog

LOCAL_SRC_FILES := \
   liblouis/liblouis/commonTranslationFunctions.c \
   liblouis/liblouis/compileTranslationTable.c \
   liblouis/liblouis/logging.c \
   liblouis/liblouis/lou_backTranslateString.c \
   liblouis/liblouis/lou_translateString.c \
   liblouis/liblouis/metadata.c \
   liblouis/liblouis/pattern.c \
   liblouis/liblouis/utils.c \
   log.c \
   meta.c \
   translation.c \
   louis.c

include $(BUILD_SHARED_LIBRARY)
