NDK_TOOLCHAIN_VERSION := 4.9
APP_STL := gnustl_static
#stlport_static
#gnustl_static
APP_PIE := $(APP_PIE_REQUIRED)
#APP_OPTIM := release
ifeq ($(HOST_ARCH),x86)
    APP_ABI := x86 
#all
#x86
#armeabi
#armeabi-v7a
else
    APP_ABI := armeabi-v7a 
endif
#APP_OPTIM := debug
#APP_CPPFLAGS := -frtti
#APP_PLATFORM := android-9