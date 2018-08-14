#!/system/bin/sh

# --disable-everything \
# --enable-libxvid  \
# --enable-libx264 \
# --enable-libxvid  \
# --enable-libwavpack  \
# --enable-libvo-amrwbenc \
# --enable-libfribidi \
# --enable-fontconfig \
# --enable-libass \
# --enable-libfreetype \
# --enable-libtesseract \
# --enable-libmp3lame \
# --enable-yasm \
# --enable-encoders \
# --enable-decoders \
# --enable-muxers \
# --enable-demuxers \
# --enable-parsers \
# --disable-htmlpages  \
# --enable-pic \
# --enable-small \
# --enable-runtime-cpudetect  \
# --enable-pthreads \
# --enable-hardcoded-tables \
# --enable-optimizations \
# --enable-lto \
# --enable-protocols \ 
# --pkg-config=/usr/local/bin/pkg-config
# -mfloat-abi=softfp
#cd $FFMPEG_EXT_PATH/jni/ffmpeg
#HOST_PLATFORM="linux-x86_64"
pwd
ls -l ${NDK_PATH}/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86/bin/
chmod 777 ./configure
#mkdir ./android-libs/armeabi-v7a
#obj/local/
#NDK_PATH=/data/data/com.pdaxrom.cctools/root/cctools/home/android-ndk-aide
ls -l .
LAMEDIR=/data/data/com.pdaxrom.cctools/root/cctools/home/ffmpeg-3.3.7/libmp3lame
ARMEABI=armeabi-v7a
./configure \
 --libdir=android-libs/$ARMEABI \
 --arch=arm \
 --cpu=armv7-a \
 --cross-prefix="${NDK_PATH}/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86/bin/arm-linux-androideabi-" \
 --target-os=android \
 --sysroot="${NDK_PATH}/platforms/android-9/arch-arm/" \
 --extra-ldflags="-Wl,--fix-cortex-a8 -L$LAMEDIR/libs/$ARMEABI" \
 --extra-cflags="-O2 -fpic  -I$LAMEDIR/jni/lame-3.100/libmp3lame -I$LAMEDIR/jni/lame-3.100/include -marm -march=armv7-a  -Wno-multichar -fno-exceptions" \
 --extra-libs="-lc -lm -ldl -llog -lgcc -lz" \
 --extra-ldexeflags=-pie \
 --enable-neon \
 --enable-cross-compile \
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
 --enable-doc \
 --enable-htmlpages \
 --disable-debug \
 --enable-stripping \
 --enable-gpl \
 --enable-version3 \
 --disable-programs \
 --enable-pic \
 --enable-small \
 --enable-runtime-cpudetect  \
 --enable-swscale-alpha \
 --enable-gray \
 --enable-pixelutils \
 --enable-pthreads \
 --enable-hardcoded-tables \
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
 --enable-jni \
 --enable-avisynth \
 --enable-mediacodec \
 --enable-network \
 --enable-libmp3lame \
 --enable-encoder=libmp3lame \
 --enable-decoder=mp3 \
 --enable-decoder=vorbis \
 --enable-decoder=opus \
 --enable-decoder=flac \
 --enable-decoder=alac \
   && \
make clean && \
make -j4 && \
make install-libs


