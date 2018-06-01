package com.tuarua.avane.android.ffmpeg;

import java.util.ArrayList;

/**
 * Created by Eoin Landy on 05/11/2016.
 */

public class InputStream {
    public static ArrayList<InputOptions> options = new ArrayList<>();
    public static void clear() {
        if(options != null)
            options.clear();
    }
    public static void addInput(InputOptions inputOptions) {
        if(options == null)
            options = new ArrayList<>();
        options.add(inputOptions);
    }
    public static ArrayList<InputOptions> getOptions() {
        return options;
    }
}
