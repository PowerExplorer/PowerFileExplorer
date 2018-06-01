package com.tuarua.avane.android;

import android.support.annotation.Keep;
import android.util.Log;

import com.tuarua.avane.android.events.Event;
import com.tuarua.avane.android.events.EventDispatcher;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by User on 02/10/2016.
 */
public class JniEventDispatcher extends EventDispatcher {
    private Progress progress = new Progress();
    private JSONObject jsonObject;
    private static JniEventDispatcher ourInstance = new JniEventDispatcher();

    public static JniEventDispatcher getInstance() {
        return ourInstance;
    }

    private JniEventDispatcher() {
    }

    @Keep
    public void dispatchStatusEventAsync(String msg,String type) {
        Event event = null;
        switch (type){
            case Event.TRACE:
                event = new Event(Event.TRACE,msg);
                break;
            case Event.INFO:
                event = new Event(Event.INFO,msg);
                break;
            case Event.INFO_HTML:
                event = new Event(Event.INFO_HTML,msg);
                break;
            case Event.ON_PROBE_INFO:
                event = new Event(Event.ON_PROBE_INFO,msg);
                break;
            case Event.ON_PROBE_INFO_AVAILABLE:
                event = new Event(Event.ON_PROBE_INFO_AVAILABLE,msg);
                break;
            case Event.NO_PROBE_INFO:
                event = new Event(Event.NO_PROBE_INFO,msg);
                break;
            case Event.ON_ENCODE_START:
                Log.i("Event.ON_ENCODE_START",msg);
                event = new Event(Event.ON_ENCODE_START,msg);
                break;
            case Event.ON_ENCODE_FINISH:
                event = new Event(Event.ON_ENCODE_FINISH,msg);
                break;
            case Event.ON_ENCODE_ERROR:
                event = new Event(Event.ON_ENCODE_ERROR,msg);
                break;
            case Event.ON_ERROR_MESSAGE:
                event = new Event(Event.ON_ERROR_MESSAGE,msg);
                break;
            case Event.ON_ENCODE_PROGRESS:

                try {
                    jsonObject = new JSONObject(msg);
                    progress.bitrate = jsonObject.getDouble("bitrate");
                    progress.fps = jsonObject.getDouble("fps");
                    progress.frame = jsonObject.getInt("frame");
                    progress.secs = jsonObject.getInt("secs");
                    progress.size = jsonObject.getDouble("size");
                    progress.speed = jsonObject.getDouble("speed");
                    progress.us = jsonObject.getInt("us");
                    event = new Event(Event.ON_ENCODE_PROGRESS,progress);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;

            default:
                break;
        }
        if(event != null)
            dispatchEvent(event);

        //Log.i("JniHandler message",msg);
        //Log.i("JniHandler type",type);

        //StatusObject obj = new StatusObject();
        //obj.type = type;
        //obj.message = msg;
        //Event event = new Event(Event.STATUS,obj);
        //dispatchEvent(event);

    }
}
