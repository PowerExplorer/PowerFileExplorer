package com.tuarua.avane.android;

/**
 * Created by User on 04/10/2016.
 */

public class JniHandler {
    private JniEventDispatcher jniHandlerEventD;
    private JniHandler(){
        jniHandlerEventD = JniEventDispatcher.getInstance();
    }
    public void dispatchStatusEventAsync(String msg,String type) {
        jniHandlerEventD.dispatchStatusEventAsync(msg,type);
    }
}
