//package net.gnu.androidutil;
//
//import android.graphics.*;
//import android.util.*;
//import android.content.*;
//import java.util.*;
//
//public class Cache<K, V> {
//
//	private static final String TAG = "Cache";
//	private LruCache<K, V> cache;
//
//	public Cache(int items) {
//		// Use 1/4th of the available memory for this memory cache.
////		final int maxMemory = (int) (Runtime.getRuntime().maxMemory()) >> 10;
////		final int cacheSize = maxMemory * percentMax / 100;
////		Log.d(TAG, cacheSize + "/" + maxMemory);
//
//		cache = new LruCache<K, V>(items);
////	{
////	@Override
////	protected int sizeOf(final K key, final V bitmap) {
////		return bitmap.getByteCount() >> 10;
////	}
////};
//	}
//
//	public V get(K url) {
//		return cache.get(url);
//	}
//
//	public void put(final K url, final V bitmap) {
//		//try {
//		if (url != null && bitmap != null) {
//			Log.d(TAG, url + " put " + bitmap);
//			cache.put(url, bitmap);
//		}
//		//} catch (RuntimeException re) {
//		//Log.d(TAG, url + " put ");
//		//throw re;
//		//}
//	}
//
//
//}
