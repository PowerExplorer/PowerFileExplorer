package com.tuarua.avane.android.ffmpeg;

/**
 * Created by Eoin Landy on 05/11/2016.
 */

public class BitStreamFilter {
    public String type;//v or a
    public String value;//eg aac_adtstoasc

    public BitStreamFilter(String _type,String _value){
        type = _type;
        value = _value;
    }
}
