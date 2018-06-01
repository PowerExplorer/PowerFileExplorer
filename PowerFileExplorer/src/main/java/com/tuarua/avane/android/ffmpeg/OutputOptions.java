package com.tuarua.avane.android.ffmpeg;

import java.util.ArrayList;

/**
 * Created by Eoin Landy on 05/11/2016.
 */

public class OutputOptions {
    public static String format;
    public static String uri;
    public static ArrayList<OutputVideoStream> videoStreams = new ArrayList<>();
    public static ArrayList<OutputAudioStream> audioStreams = new ArrayList<>();
    public static ArrayList<Attachment> attachments = new ArrayList<>();
    public static MetaData metadata; //http://wiki.multimedia.cx/index.php?title=FFmpeg_Metadata

  //  public static var arbitraryOptions:*; //eg hls //TODO

    public static Boolean fastStart = false;
    public static ArrayList<String> videoFilters = new ArrayList<>();
    public static ArrayList<String> complexFilters = new ArrayList<>();
    public static ArrayList<BitStreamFilter> bitStreamFilters = new ArrayList<>();
    public static Boolean copyAllVideoStreams = false;
    public static Boolean copyAllAudioStreams = false;

    public static int bufferSize = -1;
    public static int maxRate = -1;
    public static Double duration = -1.0;
    public static Double to = -1.0;
    public static int fileSizeLimit = -1;

    public static String preset;
    public static String target;
    public static int frameRate = 0;
    public static Boolean realtime = false;

    public static void addOverlay(Overlay overlay) {
        InputOptions inputOptions = new InputOptions();
        inputOptions.uri = overlay.getFileName();
        InputStream.addInput(inputOptions);
        String str = "overlay="+overlay.getX()+":"+overlay.getY();
        if(overlay.getInTime() > -1 && overlay.getOutTime() > -1) {
            str += String.format(":enable='between(t,%s,%s)'", overlay.getInTime(), overlay.getOutTime());
        }
        complexFilters.add(str);
    }

    public static void addVideoStream(OutputVideoStream _videoStream) {
        if(videoStreams == null)
            videoStreams = new ArrayList<>();
        videoStreams.add(_videoStream);
    }
    public static void addAudioStream(OutputAudioStream _audioStream) {
        if(audioStreams == null)
            audioStreams = new ArrayList<>();
        audioStreams.add(_audioStream);
    }

    public static void addAttachment(Attachment _attachment) {
        if(attachments == null)
            attachments = new ArrayList<>();
        attachments.add(_attachment);
    }
    public static void clear() {
        format = null;
        uri = null;
        fastStart = false;
        copyAllVideoStreams = false;
        copyAllAudioStreams = false;
        metadata = null;
        duration = -1.;
        to = -1.;
        fileSizeLimit = -1;

        bufferSize = -1;
        maxRate = -1;
        preset = null;
        target = null;
        frameRate = 0;
        realtime = false;

        if(videoStreams == null)
            videoStreams.clear();
        videoStreams = null;

        if(audioStreams == null)
            audioStreams.clear();
        audioStreams = null;

        if(attachments == null)
            attachments.clear();
        attachments = null;

        if(videoFilters == null)
            videoFilters.clear();

        if(bitStreamFilters == null)
            bitStreamFilters.clear();

        if(complexFilters == null)
            complexFilters.clear();
    }

    public static void burnSubtitles(String path) {
        videoFilters.add("subtitles="+path);
    }
    public static void addVideoFilter(String value) {
        videoFilters.add(value);
    }
    public static void addBitStreamFilter(String value,String type) {
        BitStreamFilter bsf = new BitStreamFilter(type,value);
        bitStreamFilters.add(bsf);
    }


}
