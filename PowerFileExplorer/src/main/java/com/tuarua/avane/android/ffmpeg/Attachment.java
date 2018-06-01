package com.tuarua.avane.android.ffmpeg;
import android.webkit.MimeTypeMap;
/**
 * Created by Eoin Landy on 05/11/2016.
 */

public class Attachment {
    public String fileName;
    public String getMimeType() {
       return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName.substring(fileName.lastIndexOf(".")+1, fileName.length()));
    }
}
