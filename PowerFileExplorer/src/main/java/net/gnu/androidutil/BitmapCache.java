package net.gnu.androidutil;

import android.graphics.*;
import android.util.*;
import android.content.*;
import java.util.*;
import android.app.*;

public class BitmapCache {

	//private static final String TAG = "BitmapCache";

	private LruCache<String, Bitmap> sCache2;

	public BitmapCache(Context context, int percent) {

		ActivityManager manager = ( ActivityManager ) context.getSystemService( Activity.ACTIVITY_SERVICE);
		final int memoryClass = manager.getMemoryClass();
		final int memoryClassInKilobytes = memoryClass << 10;
		final int cacheSize = memoryClassInKilobytes * percent / 100;
		
//		final int maxMemory = (int) (Runtime.getRuntime().maxMemory()) >> 10;
//		final int cacheSize = maxMemory * percent / 100;
		//Log.d(TAG, cacheSize + "/" + memoryClassInKilobytes);

		sCache2 = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(final String key, final Bitmap bitmap) {
				return bitmap.getByteCount() >> 10;
			}

			protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
				if (evicted) {
					oldValue.recycle();
				}
			}
		};
	}

	public Bitmap getBitmapFromCache(String url) {
		return sCache2.get(url);
	}

	public void put(final String url, final Bitmap bitmap) {
		//try {
		if (url != null && bitmap != null) {
			//Log.d(TAG, url + " put " + bitmap.getByteCount());
			sCache2.put(url, bitmap);
		}
		//} catch (RuntimeException re) {
		//Log.d(TAG, url + " put ");
		//throw re;
		//}
	}
}
