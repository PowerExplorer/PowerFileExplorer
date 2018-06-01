package com.tuarua.avane.android.ffmpeg;

/**
 * Created by Eoin Landy on 05/11/2016.
 */

public class OutputVideoStream {
    public String codec;
    public int inputIndex = 0;
    public int sourceIndex = 0;
    public int bitrate = -1;
    public int crf = -1; //0-51
    public int qp = -1; //0-69
    public int frames = -1;
    public String pixelFormat;
    public X264Options encoderOptions;
    public X264AdvancedOptions advancedEncOpts;
}
