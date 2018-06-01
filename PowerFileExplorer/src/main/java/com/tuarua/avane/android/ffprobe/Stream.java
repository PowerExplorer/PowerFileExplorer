package com.tuarua.avane.android.ffprobe;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Eoin Landy on 08/10/2016.
 */

public class Stream {
    //Common
    public int index = 0;
    public String id;
    public String codecName;
    public String codecLongName;
    public String profile;
    public String codecType;
    public String codecTimeBase;
    public String codecTagString;
    public int codecTag = 0;

    public Double duration = 0.0;
    public Double durationTimestamp = 0.0;

    public Double realFrameRate = 0.0;
    public Double averageFrameRate = 0.0;
    public String timeBase;

    public Double startPTS = 0.0;
    public Double startTime = 0.0;

    public Double bitRate = 0.0;
    public Double maxBitRate = 0.0;
    public Double bitsPerRawSample = 0.0;
    public Double numFrames = 0.0;
    public HashMap<String,String> tags = new HashMap<>();
    public ArrayList<HashMap<String, String>> disposition;//TODO create Disposition Object
}
