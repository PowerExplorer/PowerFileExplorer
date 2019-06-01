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
#  --pkg-config=/usr/local/bin/pkg-config
#cd $FFMPEG_EXT_PATH/jni/ffmpeg
#HOST_PLATFORM="linux-x86_64"
pwd
ls -l ${NDK_PATH}/toolchains/x86-4.9/prebuilt/linux-x86/bin/
chmod 777 ./configure
ls -l .
./configure \
    --libdir=android-libs/x86 \
    --arch=x86 \
    --cpu=i686 \
    --cross-prefix="${NDK_PATH}/toolchains/x86-4.9/prebuilt/linux-x86/bin/i686-linux-android-" \
    --target-os=android \
    --sysroot="${NDK_PATH}/platforms/android-9/arch-x86/" \
    --extra-ldexeflags=-pie \
    --disable-static \
    --enable-shared \
  --disable-asm \
  --enable-optimizations \
  --enable-everything \
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
    --enable-htmlpages \
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
  --enable-jni \
    --disable-symver \
    --enable-avdevice \
    --enable-avfilter \
    --enable-avformat \
    --enable-avcodec \
    --enable-swresample \
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
make install-libs && \
make clean

