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

import jp.sblo.pandora.jota.text.TextUtils;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;

public class RelativeSizeSpan extends MetricAffectingSpan implements ParcelableSpan {

	private final float mProportion;

	public RelativeSizeSpan(float proportion) {
		mProportion = proportion;
	}

    public RelativeSizeSpan(Parcel src) {
        mProportion = src.readFloat();
    }

    public int getSpanTypeId() {
        return getSpanTypeIdInternal() ;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
		writeToParcelInternal(dest, flags);
    }

	public float getSizeChange() {
		return mProportion;
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setTextSize(ds.getTextSize() * mProportion);
	}

	@Override
	public void updateMeasureState(TextPaint ds) {
		ds.setTextSize(ds.getTextSize() * mProportion);
	}

	public int getSpanTypeIdInternal() {
		return TextUtils.RELATIVE_SIZE_SPAN;
	}

	public void writeToParcelInternal(Parcel dest, int flags) {
		dest.writeFloat(mProportion);
	}
}
