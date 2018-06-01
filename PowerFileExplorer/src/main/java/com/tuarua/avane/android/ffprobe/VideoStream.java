package com.tuarua.avane.android.ffprobe;

/**
 * Created by Eoin Landy on 08/10/2016.
 */
public class VideoStream extends Stream {
    public int width = 0;
    public int height = 0;
    public int codedWidth = 0;
    public int codedHeight = 0;
    public int hasBframes = 0;
    public String sampleAspectRatio;
    public String displayAspectRatio;
    public String pixelFormat;
    public int level = 0;
    public String colorRange;
    public String colorSpace;
    public String colorTransfer;
    public String colorPrimaries;
    public String chromaLocation;
    public String timecode;
    public int refs = 0;

}
