//package net.gnu.androidutil;
//
//import android.content.*;
//import android.app.*;
//import android.widget.*;
//import android.graphics.*;
//import android.os.*;
//import java.net.*;
//import java.io.*;
//import android.content.res.*;
//import android.support.v4.util.*;
//
//public class TCImageLoader implements ComponentCallbacks2 {
//	private TCLruCache cache;
//	public TCImageLoader (Context context) {
//		ActivityManager am = ( ActivityManager ) context.getSystemService(
//			Context.ACTIVITY_SERVICE);
//		int maxKb = am.getMemoryClass() * 1024 ;
//		int limitKb = maxKb / 8; // 1/8th of total ram
//        cache = new TCLruCache(limitKb);
//    }
//	public void display( String url, ImageView imageview, int defaultresource) {
//		imageview.setImageResource(defaultresource);
//		Bitmap image = cache. get (url);
//		if (image != null ) {
//			imageview.setImageBitmap(image);
//		}
//		else {
//			new SetImageTask (imageview).execute(url);
//		}
//    }
//	private class TCLruCache extends LruCache < String , Bitmap > {
//		public TCLruCache( int maxSize) {
//			super (maxSize);
//        }
//		@Override
//		protected int sizeOf( String key, Bitmap value) {
//			int kbOfBitmap = value.getByteCount() / 1024 ;
//			return kbOfBitmap;
//        }
//    }
//	private class SetImageTask extends AsyncTask<String , Void , Integer > {
//		private ImageView imageview;
//		private Bitmap bmp;
//		public SetImageTask (ImageView imageview) {
//			this.imageview = imageview;
//        }
//		@Override
//		protected Integer doInBackground( String... params ) {
//			String url = params [0 ];
//			try {
//                bmp = getBitmapFromURL(url);
//				if (bmp != null ) {
//                    cache.put(url, bmp);
//                }
//				else {
//					return 0 ;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//				return 0;
//            }
//			return 1;
//        }
//		@Override
//		protected void onPostExecute( Integer result) {
//			if (result == 1) {
//                imageview.setImageBitmap(bmp);
//            }
//			super.onPostExecute(result);
//        }
//		private Bitmap getBitmapFromURL( String src) {
//			try {
//                URL url = new URL(src);
//				HttpURLConnection connection
//                    = ( HttpURLConnection ) url.openConnection();
//                connection.setDoInput( true );
//                connection.connect();
//				InputStream input = connection.getInputStream();
//				Bitmap myBitmap = BitmapFactory.decodeStream(input);
//				return myBitmap;
//            } catch (IOException e) {
//                e.printStackTrace();
//				return null ;
//            }
//        }
//    }
//	@Override
//	public void onConfigurationChanged( Configuration newConfig) {
//    }
//	@Override
//	public void onLowMemory() {
//    }
//	@Override
//	public void onTrimMemory( int level) {
//		if (level >= TRIM_MEMORY_MODERATE) {
//            cache.evictAll();
//        }
//		else if (level >= TRIM_MEMORY_BACKGROUND) {
//            cache.trimToSize(cache.size() / 2 );
//        }
//    }
//}
