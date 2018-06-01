package com.tuarua.avane.android.ffmpeg;

/**
 * Created by Eoin Landy on 06/11/2016.
 */

public class GlobalOptions {
    public static Boolean overwriteOutputFiles = true;
    public static Boolean ignoreUnknown = false;
    //public static var maxAllocBytes:int = -1; //what is default ?
    //public static var volume:int = 256;
    public static Double maxErrorRate = 0.0;
    public static Boolean copyUnknown = false;
    public static int timeLimit = -1;
    public static String vsync = "-1"; //-1 auto, 0 = passthrough,1 = cfr,2 = vfr, drop
    public static Double fDropThreshold = -1.1; //doesn't like frameDropThreadhold
    public static Boolean copyTs = false;
    public static Boolean startAtZero = false;
    public static int copyTb = -1; // 0 is decoder, 1 is demuxer
}
