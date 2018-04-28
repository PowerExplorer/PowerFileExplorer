#!/system/bin/sh

#   --disable-everything \
#    --enable-libxvid  \
#  --enable-libx264 \
#    --enable-libxvid  \
#    --enable-libwavpack  \
#    --enable-libvo-amrwbenc \
#  --enable-libfribidi \
#  --enable-fontconfig \
#  --enable-libass \
# --enable-libfreetype \
#   --enable-libtesseract \
#  --enable-libmp3lame \
#  --enable-yasm \
#  --enable-encoders \
#  --enable-decoders \
#  --enable-muxers \
#  --enable-demuxers \
#  --enable-parsers \
#  --disable-htmlpages  \
#  --enable-pic \
#    --enable-small \
#    --enable-runtime-cpudetect  \
#  --enable-pthreads \
#  --enable-hardcoded-tables \
#  --enable-optimizations \
#  --enable-lto \
#  --enable-protocols \
#cd $FFMPEG_EXT_PATH/jni/ffmpeg
pwd
ls -l ${NDK_PATH}/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86/bin/
chmod 777 ./configure
ls -l .
./configure \
    --libdir=android-libs/armeabi-v7a \
    --arch=arm \
    --cpu=armv7-a \
    --cross-prefix="${NDK_PATH}/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86/bin/arm-linux-androideabi-" \
    --target-os=android \
    --sysroot="${NDK_PATH}/platforms/android-9/arch-arm/" \
    --extra-cflags="-march=armv7-a -mfloat-abi=softfp" \
    --extra-ldflags="-Wl,--fix-cortex-a8" \
    --extra-ldexeflags=-pie \
    --disable-static \
    --enable-shared \
  --enable-asm \
  --enable-optimizations \
  --enable-decoders \
  --enable-encoders \
  --enable-hwaccels \
  --enable-muxers \
  --enable-demuxers \
  --enable-parsers \
  --enable-bsfs \
  --enable-protocols \
  --enable-indevs \
  --enable-outdevs \
  --enable-filters \
    --disable-doc \
  --disable-debug \
  --enable-gpl \
  --enable-version3 \
    --disable-programs \
  --enable-pic \
    --enable-small \
    --enable-runtime-cpudetect  \
  --enable-swscale-alpha \
  --enable-pixelutils \
  --enable-pthreads \
  --enable-hardcoded-tables \
    --enable-symver \
    --enable-avdevice \
    --enable-avfilter \
    --enable-avformat \
    --enable-avcodec \
    --enable-avresample \
    --enable-postproc \
    --enable-swscale \
    --enable-avutil \
    --enable-decoder=vorbis \
    --enable-decoder=opus \
    --enable-decoder=flac \
    --enable-decoder=alac \
    && \
make -j4 && \
make install-libs


