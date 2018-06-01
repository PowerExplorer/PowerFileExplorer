package com.tuarua.avane.android.ffprobe;

import java.util.ArrayList;

/**
 * Created by Eoin Landy on 08/10/2016.
 */

public class Probe {
    public Format format = new Format();
    public ArrayList<VideoStream> videoStreams = new ArrayList<>();
    public ArrayList<AudioStream> audioStreams = new ArrayList<>();
    public ArrayList<SubtitleStream> subtitleStreams = new ArrayList<>();
}
