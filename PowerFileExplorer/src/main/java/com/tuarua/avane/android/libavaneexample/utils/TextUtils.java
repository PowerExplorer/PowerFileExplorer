//package com.tuarua.avane.android.libavaneexample.utils;
//
//import java.lang.reflect.Array;
//import java.text.DecimalFormat;
//
///**
// * Created by Eoin Landy on 08/10/2016.
// */
//
//public class TextUtils {
//    private static DecimalFormat percentFormat2D = new DecimalFormat("0.00");
//    private static final String[] byteSizes = {"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
//    public static String bytesToString(Double bytes) {
//        int index = (int) Math.floor(Math.log(bytes)/Math.log(1024));
//        return percentFormat2D.format((bytes/Math.pow(1024, index))) + " " +byteSizes[index];
//    }
//}
