package com.tuarua.avane.android.ffmpeg;

/**
 * Created by Eoin Landy on 05/11/2016.
 */

public class InputOptions {
    public String format;
    public String uri;
    public int streamLoop = 0;
    public Double duration = -1.0;
    public Double startTime = 0.0;
    public Double inputTimeOffset = 0.0;
    public Boolean realtime = false;
    //public var pixelFormat:String;//need
    public int frameRate = 0;
    public int playlist = -1;
    public String hardwareAcceleration;
    public int threads = 0;
}
