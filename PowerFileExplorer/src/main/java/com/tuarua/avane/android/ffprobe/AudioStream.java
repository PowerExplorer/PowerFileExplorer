package com.tuarua.avane.android.ffprobe;

/**
 * Created by Eoin Landy on 08/10/2016.
 */

public class AudioStream extends Stream {
    public String sampleFormat;
    public int sampleRate = 0;
    public int channels = 0;
    public String channelLayout;
    public int bitsPerSample = 0;
}
