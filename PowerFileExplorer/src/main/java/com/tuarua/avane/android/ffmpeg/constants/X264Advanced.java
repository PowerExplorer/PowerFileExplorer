package com.tuarua.avane.android.ffmpeg.constants;

/**
 * Created by Eoin Landy on 05/11/2016.
 */

public class X264Advanced {
    public static final int TRELLIS_OFF = 0;
    public static final int TRELLIS_ENCODE = 1;
    public static final int TRELLIS_ALWAYS = 2;

    public static final int BADAPT_DISABLED = 0;
    public static final int BADAPT_FAST = 1;
    public static final int BADAPT_OPTIMAL = 2;

    public static final String BPYRAMID_DISABLED = "none";
    public static final String BPYRAMID_STRICT = "strict";
    public static final String BPYRAMID_NORMAL = "normal";

    public static final String DIRECT_NONE = "none";
    public static final String DIRECT_SPATIAL = "spatial";
    public static final String DIRECT_TEMPORAL = "temporal";
    public static final String DIRECT_AUTO = "auto";

    public static final String PARTITIONS_NONE = "none";
    public static final String PARTITIONS_ALL = "all";
    public static final String PARTITIONS_SOME = "i4x4,i8x8";
    public static final String PARTITIONS_MOST = null;

    public static final String ME_DIAMOND = "dia";
    public static final String ME_HEX = "hex";
    public static final String ME_UNEVEN_MULTIHEX = "umh";
    public static final String ME_EXHAUSTIVE = "esa";
    public static final String ME_HADAMARD = "tesa";

    public static final int WEIGHTP_DISABLED = 0;
    public static final int WEIGHTP_REFS = 1;
    public static final int WEIGHTP_DUPLICATES = 2;
}
