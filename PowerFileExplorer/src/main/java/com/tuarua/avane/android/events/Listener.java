package com.tuarua.avane.android.events;

/**
 * Created by User on 02/10/2016.
 */

public class Listener {
    private String type;
    private IEventHandler handler;
    public Listener(String type, IEventHandler handler){
        this.type = type;
        this.handler = handler;
    }
    public String getType(){
        return this.type;
    }
    public IEventHandler getHandler(){
        return this.handler;
    }
}
