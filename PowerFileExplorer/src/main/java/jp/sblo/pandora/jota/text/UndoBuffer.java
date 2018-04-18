package jp.sblo.pandora.jota.text;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class UndoBuffer implements Parcelable {

    public static final int MAX_SIZE=512*1024;

    static class TextChange {
        int start;
        CharSequence oldtext;
        CharSequence newtext;
    };
    private ArrayList<TextChange> mBuffer;
    private int mCurrentSize=0;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt( mBuffer.size() );
        for( TextChange item : mBuffer ){
            out.writeInt(item.start);
            out.writeString(item.oldtext.toString());
            out.writeString(item.newtext.toString());
        }
    }

    public static final Parcelable.Creator<UndoBuffer> CREATOR
            = new Parcelable.Creator<UndoBuffer>() {
        public UndoBuffer createFromParcel(Parcel in) {
            return new UndoBuffer(in);
        }

        public UndoBuffer[] newArray(int size) {
            return new UndoBuffer[size];
        }
    };

    private UndoBuffer(Parcel in) {
        this();
        int len = in.readInt();
        while( in.dataAvail() > 0 && len-- > 0){
            TextChange item = new TextChange();
            item.start = in.readInt();
            item.oldtext = in.readString();
            item.newtext = in.readString();
            if ( item.newtext == null ){
                item.newtext = "";
            }
            if ( item.oldtext == null ){
                item.oldtext = "";
            }
            mBuffer.add(item);
            mCurrentSize += item.newtext.length() + item.oldtext.length();
        }
    }

    public UndoBuffer() {
        mBuffer = new ArrayList<TextChange>();
        mCurrentSize = 0;
    }

    public TextChange pop()
    {
        int size = mBuffer.size();
        if ( size > 0 ){
            TextChange item = mBuffer.get(size-1);
            mBuffer.remove(size-1);
            int removed = item.newtext.length() + item.oldtext.length();
            mCurrentSize -= removed;
            return item;
        }else{
            return null;
        }
    }

    public boolean removeLast()
    {
        int size = mBuffer.size();
        if ( size > 0 ){
            TextChange item = mBuffer.get(0);
            mBuffer.remove(0);
            mCurrentSize -= item.newtext.length() + item.oldtext.length();
            return true;
        }else{
            return false;
        }
    }

    public void push( TextChange item )
    {
        if ( item.newtext == null ){
            item.newtext = "";
        }
        if ( item.oldtext == null ){
            item.oldtext = "";
        }
        int delta = item.newtext.length() + item.oldtext.length() ;
        if ( delta < MAX_SIZE ){
            mCurrentSize += delta;
            mBuffer.add(item);
            while( mCurrentSize > MAX_SIZE ){
                if ( !removeLast() ){
                    break;
                }
            }
        }else{
            removeAll();
        }
    }

    public void removeAll()
    {
        mBuffer.removeAll(mBuffer);
        mCurrentSize = 0;
    }
    public boolean canUndo()
    {
        return mBuffer.size()>0;
    }
}
