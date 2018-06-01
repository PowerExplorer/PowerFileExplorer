package com.tuarua.avane.android.events;
public class Event {
    public static final String TRACE = "TRACE";
    public static final String INFO = "INFO";
    public static final String INFO_HTML = "INFO_HTML";
    public static final String ON_PROBE_INFO = "ON_PROBE_INFO";
    public static final String NO_PROBE_INFO = "NO_PROBE_INFO";
    public static final String ON_PROBE_INFO_AVAILABLE = "ON_PROBE_INFO_AVAILABLE";
    public static final String ON_ENCODE_START = "ON_ENCODE_START";
    public static final String ON_ENCODE_FINISH = "ON_ENCODE_FINISH";
    public static final String ON_ENCODE_ERROR = "ON_ENCODE_ERROR";
    public static final String ON_ERROR_MESSAGE = "Encode.ERROR_MESSAGE";
    public static final String ON_ENCODE_PROGRESS = "ON_ENCODE_PROGRESS";
    public static final String ON_ENCODE_FATAL = "ON_ENCODE_FATAL";

    protected String strType = "";
    protected Object params;
    public Event(String type, Object params){
        initProperties(type, params);
    }
    protected void initProperties(String type, Object params){
        strType = type;
        this.params = params;
    }
    public String getStrType(){
        return strType;
    }
    public Object getParams(){
        return params;
    }

}
