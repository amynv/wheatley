LOCAL_PATH := $(call my-dir)

LIBWAYLAND_PATH := $(LOCAL_PATH)/wayland
LIBWAYLAND_EXTRA_PATH := $(LOCAL_PATH)/wayland-extra

LIBWAYLAND_UTIL_SRC =		\
	src/connection.c	\
	src/wayland-util.c	\
	src/wayland-os.c	\

LIBWAYLAND_SERVER_SRC =		\
	src/wayland-protocol.c	\
	src/wayland-server.c	\
	src/wayland-shm.c	\
	src/event-loop.c

LIBWAYLAND_CLIENT_SRC =		\
	src/wayland-protocol.c	\
	src/wayland-client.c

LIBWAYLAND_CFLAGS := -O2

# Protocol stuff
WAYLAND_SCANNER_SRC := $(LIBWAYLAND_PATH)/protocol/wayland.xml
WAYLAND_SCANNER_DEST := $(LIBWAYLAND_PATH)/src
include $(LOCAL_PATH)/wayland-scanner.mk

LOCAL_PATH := $(LIBWAYLAND_PATH)

include $(CLEAR_VARS)

LOCAL_MODULE		:= libwayland-client
LOCAL_CFLAGS		:= $(LIBWAYLAND_CFLAGS)
LOCAL_SRC_FILES		:= $(LIBWAYLAND_UTIL_SRC) $(LIBWAYLAND_CLIENT_SRC)
LOCAL_GENERATED_SOURCES	:= src/wayland-protocol.c
LOCAL_C_INCLUDES	:= $(LIBWAYLAND_EXTRA_PATH)
LOCAL_EXPORT_C_INCLUDES	:= $(LOCAL_C_INCLUDES) $(LIBWAYLAND_PATH)/src
LOCAL_STATIC_LIBRARIES	:= libffi

wayland-client: $(LIBWAYLAND_PATH)/src/wayland-client-protocol.h

include $(BUILD_STATIC_LIBRARY)
$(LOCAL_OBJECTS): $(LOCAL_PATH)/src/wayland-client-protocol.h

include $(CLEAR_VARS)

LOCAL_MODULE		:= libwayland-server
LOCAL_CFLAGS		:= $(LIBWAYLAND_CFLAGS)
LOCAL_SRC_FILES		:= $(LIBWAYLAND_UTIL_SRC) $(LIBWAYLAND_SERVER_SRC)
LOCAL_GENERATED_SOURCES	:= src/wayland-protocol.c
LOCAL_C_INCLUDES	:= $(LIBWAYLAND_EXTRA_PATH)
LOCAL_EXPORT_C_INCLUDES	:= $(LOCAL_C_INCLUDES) $(LIBWAYLAND_PATH)/src
LOCAL_STATIC_LIBRARIES	:= libffi

wayland-server: $(LIBWAYLAND_PATH)/src/wayland-server-protocol.h

include $(BUILD_STATIC_LIBRARY)
$(LOCAL_OBJECTS): $(LOCAL_PATH)/src/wayland-server-protocol.h
