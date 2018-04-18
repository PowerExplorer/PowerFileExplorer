/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.sblo.pandora.jota.text.style;

import java.util.ArrayList;

import jp.sblo.pandora.jota.text.TextUtils;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.text.style.CharacterStyle;

public class ForegroundColorSpan extends CharacterStyle
        implements UpdateAppearance, ParcelableSpan {

    private int mColor;
    private int mStart;
    private int mEnd;

    static private ArrayList<ForegroundColorSpan> sPool = new ArrayList<ForegroundColorSpan>();

	public ForegroundColorSpan(int color) {
		mColor = color;
	}

    public ForegroundColorSpan(Parcel src) {
        mColor = src.readInt();
    }

    public int getSpanTypeId() {
        return getSpanTypeIdInternal() ;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeToParcelInternal( dest, flags);
    }

	public int getForegroundColor() {
		return mColor;
	}

    @Override
	public void updateDrawState(TextPaint ds) {
		ds.setColor(mColor);
	}

    public boolean isLapped(int s, int e)
    {
        return  ( mStart < e && s < mEnd );
    }

    static public ForegroundColorSpan obtain(int color,int start,int end)
    {
        if ( sPool.size() == 0 ){
            return new ForegroundColorSpan(color);
        }else{
            ForegroundColorSpan fcs = sPool.get(0);
            sPool.remove(fcs);
            fcs.mColor = color;
            fcs.mStart = start;
            fcs.mEnd = end;
            return fcs;
        }
    }

    public void recycle()
    {
        sPool.add(this);
    }

    public int getSpanTypeIdInternal() {
        return TextUtils.FOREGROUND_COLOR_SPAN;
    }
    public void writeToParcelInternal(Parcel dest, int flags) {
        dest.writeInt(mColor);
    }
}
