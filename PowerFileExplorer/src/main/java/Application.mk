NDK_TOOLCHAIN_VERSION := 4.9
APP_STL := gnustl_static
#stlport_static
APP_PIE := $(APP_PIE_REQUIRED)
ifeq ($(HOST_ARCH),x86)
    APP_ABI := all
#all
#x86
#armeabi
else
    APP_ABI := armeabi-v7a
endif
#APP_OPTIM := debug
