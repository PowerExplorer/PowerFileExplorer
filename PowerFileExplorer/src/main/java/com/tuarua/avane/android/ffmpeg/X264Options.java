package com.tuarua.avane.android.ffmpeg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eoin Landy on 06/11/2016.
 */

public class X264Options {

    public String preset;
    public String profile;
    public String level;
    public String tune;

    public Map<String, String> getAsMap() {
        Map<String, String> ret = new HashMap<>();
        if(preset != null && !preset.isEmpty())
            ret.put("preset",preset);
        if(profile != null && !profile.isEmpty())
            ret.put("profile",profile);
        if(level != null && !level.isEmpty())
            ret.put("level",level);
        if(tune != null && !tune.isEmpty())
            ret.put("tune",tune);
        return ret;
    }
}
