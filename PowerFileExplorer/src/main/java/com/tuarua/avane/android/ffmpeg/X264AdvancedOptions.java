package com.tuarua.avane.android.ffmpeg;

import com.tuarua.avane.android.ffmpeg.constants.X264Advanced;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Eoin Landy on 06/11/2016.
 */

public class X264AdvancedOptions {
    public String type = "x264-params";
    public int bFrames = 3;
    public int bAdapt = X264Advanced.BADAPT_FAST;
    public String bPyramid = X264Advanced.BPYRAMID_NORMAL;
    public Boolean noCabac = false;
    public int ref = 3;
    public int[] deblock = {0,0};
    public int vbvMaxRate = 0;
    public int vbvBufSize = 0;
    public Double aqStrength = 1.0;
    public String partitions = X264Advanced.PARTITIONS_MOST;
    public String direct = X264Advanced.DIRECT_SPATIAL;
    public int weightP = X264Advanced.WEIGHTP_DUPLICATES;
    public String me = X264Advanced.ME_HEX;
    public int merange = 16;
    public int subme = 7;// - 0: fullpel only (not recommended) ,- 1: SAD mode decision, one qpel iteration, - 2: SATD mode decision, - 3-5: Progressively more qpel, - 6: RD mode decision for I/P-frames, - 7: RD mode decision for all frames, - 8: RD refinement for I/P-frames, - 9: RD refinement for all frames, - 10: QP-RD - requires trellis=2, aq-mode>0, - 11: Full RD: disable all early terminations
    public Double[] psyRd = {1.0,0.0};
    public Boolean no8x8dct = false;
    public int trellis = X264Advanced.TRELLIS_ENCODE;
    public Boolean noDctDecimate = false;

    public String getAsString() {
        ArrayList<String> arr = new ArrayList<>();

        if(trellis != X264Advanced.TRELLIS_ENCODE)
            arr.add("trellis="+trellis);
        if(merange != 16)
            arr.add("merange="+merange);
        if(ref != 3)
            arr.add("ref="+ref);
        if(bFrames != 3)
            arr.add("bframes="+bFrames);
        if(noCabac)
            arr.add("cabac=0");
        if(no8x8dct)
            arr.add("8x8dct=0");
        if(weightP != X264Advanced.WEIGHTP_DUPLICATES)
            arr.add("weightp="+weightP);
        if(bPyramid != X264Advanced.BPYRAMID_NORMAL)
            arr.add("b-pyramid="+bPyramid);
        if(bAdapt != X264Advanced.BADAPT_FAST)
            arr.add("b-adapt="+bAdapt);
        if(direct != "spatial")
            arr.add("direct="+direct);
        if(me != X264Advanced.ME_HEX)
            arr.add("me="+me);
        if(subme != 7)
            arr.add("subme="+subme);
        if(aqStrength != 1.0)
            arr.add("aq-strength="+aqStrength);
        if(noDctDecimate)
            arr.add("no-dct-decimate=1");

        if(deblock[0] != 0 || deblock[1] != 0){
            String deblockAsString = Arrays.toString(deblock);
            deblockAsString = deblockAsString.replace(", ",":");
            deblockAsString = deblockAsString.substring(1,deblockAsString.length()-1);
            arr.add("deblock="+deblockAsString);
        }

        if(partitions != X264Advanced.PARTITIONS_MOST)
            arr.add("analyse="+partitions);
        if(psyRd[0] != 1.0 || psyRd[1] != 0.0){
            String psyRdAsString = Arrays.toString(psyRd);
            psyRdAsString = psyRdAsString.replace(", ",":");
            psyRdAsString = psyRdAsString.substring(1,psyRdAsString.length()-1);
            arr.add("psy-rd="+psyRdAsString);
        }


        if(vbvMaxRate > 0)
            arr.add("vbv-maxrate="+vbvMaxRate);
        if(vbvBufSize > 0)
            arr.add("vbv-bufsize="+vbvBufSize);

        String argsAsString = null;
        if(arr.size() > 0){
            String[] argsAsArray;
            argsAsArray = arr.toArray(new String[arr.size()]);
            argsAsString = Arrays.toString(argsAsArray);
            argsAsString = argsAsString.replace(", ",":");
            argsAsString = argsAsString.substring(1,argsAsString.length()-1);
        }


        return argsAsString;
    }

}
