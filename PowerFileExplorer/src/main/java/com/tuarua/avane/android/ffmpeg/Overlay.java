package com.tuarua.avane.android.ffmpeg;

/**
 * Created by Eoin Landy on 05/11/2016.
 */

public class Overlay {
    private String _fileName;
    private int _x = 0;
    private int _y = 0;
    private Double _inTime = -1.;
    private Double _outTime = -1.;

    public void setFileName(String value) {
        _fileName = value;
    }
    public String getFileName() {
        return _fileName;
    }
    public int getX() {
        return _x;
    }
    public void setX(int value) {
        _x = value;
    }
    public int getY() {
        return _y;
    }
    public void setY(int value) {
        _y = value;
    }
    public Double getInTime() {
        return _inTime;
    }
    public void setInTime(Double value) {
        _inTime = value;
    }
    public Double getOutTime() {
        return _outTime;
    }
    public void setOutTime(Double value) {
        _outTime = value;
    }

}
