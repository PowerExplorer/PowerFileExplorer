package com.tuarua.avane.android.ffmpeg.filters;

/**
 * Created by Eoin Landy on 06/11/2016.
 */
public class Video {
    public static String To16x9() {
        return "pad=ih*16/9:ih:(ow-iw)/2:(oh-ih)/2";
    }

    //new StudentBuilder()
    //.name("Spicoli")
    //.age(16)
    //.motto("Aloha, Mr Hand")
    //.buildStudent();
    public class Sharpen {
        private int _lumaMatrixX = 5;
        public Sharpen() { }
        public String build() {
            return "unsharp="+_lumaMatrixX;
        }
        public Sharpen lumaMatrixX(int _lumaMatrixX) {
            this._lumaMatrixX = _lumaMatrixX;
            return this;
        }

    }

    //public static String sharpen(Optional<Integer> lumaMatrixX,int lumaMatrixY, Double lumaAmount,int chromaMatrixX,int chromaMatrixY, Number chromaAmount) {
        //return "unsharp=";
   //}
    //public static String sharpen(int lumaMatrixX=5,lumaMatrixY:int=5,lumaAmount:Number=1.0,chromaMatrixX:int=5,chromaMatrixY:int=5,chromaAmount:Number=0.0) {
        //return "unsharp="+lumaMatrixX.toString()+":"+lumaMatrixY.toString()+":"+lumaAmount.toString()+":"+chromaMatrixX.toString()+":"+chromaMatrixY.toString()+":"+chromaAmount.toString();
    //}
}
