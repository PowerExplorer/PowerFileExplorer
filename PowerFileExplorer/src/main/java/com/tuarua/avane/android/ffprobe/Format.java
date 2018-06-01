package com.tuarua.avane.android.ffprobe;

import java.util.HashMap;

/**
 * Created by Eoin Landy on 08/10/2016.
 */

public class Format {
    public String filename;
    public int numStreams = 0;
    public int numPrograms = 0;
    public String formatName;
    public String formatLongName;
    public double startTime = 0.0;
    public double duration = 0.0;
    public int size = 0; //bytes
    public int bitRate = 0;
    public int probeScore = 0;
    public HashMap<String,String> tags = new HashMap<>();//hasmap ?
}
