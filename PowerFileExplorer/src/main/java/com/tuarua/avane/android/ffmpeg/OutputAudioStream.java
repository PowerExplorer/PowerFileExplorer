package com.tuarua.avane.android.ffmpeg;

/**
 * Created by Eoin Landy on 05/11/2016.
 */

public class OutputAudioStream {
    public String codec;
    public int samplerate = -1;
    public int bitrate = -1;
    public int sourceIndex = 0;
    public int channels = 2;
    public int frames = -1;
}
