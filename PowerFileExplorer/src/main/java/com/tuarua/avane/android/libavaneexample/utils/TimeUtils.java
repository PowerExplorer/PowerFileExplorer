package com.tuarua.avane.android.libavaneexample.utils;

/**
 * Created by Eoin Landy on 08/10/2016.
 */

public class TimeUtils {
    public static String secsToTimeCode(double n) {
        String finStr = "";
        int h = (int) Math.floor((n/3600));
        int m = (int) Math.floor((n-(h*3600))/60);
        int s = (int) Math.floor(n-((h*3600)+(m*60)));
        String hStr = String.valueOf(h);
        String mStr = String.valueOf(m);
        String sStr = String.valueOf(s);
        if(h < 10) hStr = hStr;
        if(m < 10) mStr = "0"+mStr;
        if(s < 10) sStr = "0"+sStr;
        if(h > 0) finStr = finStr+hStr+":";
        finStr = finStr+mStr+":"+sStr;
        return finStr;
    }
}
