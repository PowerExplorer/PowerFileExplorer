package jp.sblo.pandora.jota;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.view.MotionEvent;

public class ApiWrapper {
    static public int getSourceOfEvent( MotionEvent event )
    {
        return event.getSource();
    }

    static public float getAxisValue ( MotionEvent event , int axis)
    {
        return event.getAxisValue(axis);
    }
    static void scanFile(Context context, String []paths , String []mimeTypes )
    {
        MediaScannerConnection.scanFile(context, paths, mimeTypes, null);
    }

}

