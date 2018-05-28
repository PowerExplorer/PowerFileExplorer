package net.gnu.explorer;

import android.app.Application;
import android.annotation.*;
import java.io.*;
import android.content.pm.*;
import android.os.*;
import dalvik.system.*;
import java.lang.reflect.*;
import jp.sblo.pandora.jota.*;
import android.util.Log;
import net.gnu.androidutil.*;
import com.amaze.filemanager.utils.AppConfig;
import net.gnu.texteditor.TextEditorActivity;
//import net.gnu.mupdf.viewer.app.LibraryActivity;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import android.content.Context;
//import android.support.multidex.MultiDex;

public class ExplorerApplication extends JotaTextEditor {
	
	private static final String TAG = "ExplorerApplication";
	private static ExplorerApplication instance;
	
	protected String userAgent;

	public static String PRIVATE_PATH = "";
	public static File PRIVATE_DIR = null;
	public static final String ROOT_CACHE = "/.net.gnu.explorer";
	public static String DATA_DIR = "/data/data/" + ExplorerApplication.class.getName().substring(0, ExplorerApplication.class.getName().lastIndexOf(".")) + "/"; //net.gnu.explorer/";
	
    static {
		String sdCardPath = System.getenv("SECONDARY_STORAGE");
		Log.d(TAG, "SECONDARY_STORAGE " + sdCardPath);
		File tmp = null;
		if (sdCardPath == null) {
			init();
			Log.d(TAG, "SECONDARY_STORAGE = null, PRIVATE_PATH = " + PRIVATE_PATH);
		} else if (!sdCardPath.contains(":")) {
			PRIVATE_PATH = sdCardPath + ROOT_CACHE;
			PRIVATE_DIR = new File(PRIVATE_PATH);
			tmp = new File(PRIVATE_PATH + "/xxx-" + System.currentTimeMillis());
			try {
				if (PRIVATE_DIR.mkdirs() || tmp.createNewFile()) {
					if (tmp != null) {
						Log.d(TAG, "delete 1 " + tmp + ": " + tmp.delete());
					}
					Log.d(TAG, sdCardPath + " has " + PRIVATE_DIR.getTotalSpace() + " bytes");
				} else {
					init();
				}
			} catch (IOException e) {
				//e.printStackTrace();
				Log.e(TAG, "tmp " + tmp);
				Log.e(TAG, "PRIVATE_DIR " + PRIVATE_DIR);
				init();
				Log.d(TAG, "sdCardPath 1, PRIVATE_PATH = " + PRIVATE_PATH);
			}
		} else if (sdCardPath.contains(":")) {
			//Multiple Sdcards show root folder and remove the Internal storage from that.
			File storage = new File("/storage");
			File[] fs = storage.listFiles();
			init();
			if (fs != null) {
				File maxPrev = PRIVATE_DIR;
				long maxTotal = PRIVATE_DIR.getTotalSpace();
				for (File f : fs) {
					String absolutePath = f.getAbsolutePath();
					long totalSpace = f.getTotalSpace();
					Log.d(absolutePath, totalSpace + " bytes, can write " + f.canWrite());
					try {
						String comPath = absolutePath + ROOT_CACHE;
						if (totalSpace > maxTotal && f.canWrite() && (new File(comPath).mkdirs() 
							|| (tmp = new File(comPath + "/xxx" + System.currentTimeMillis())).createNewFile())) {
							PRIVATE_PATH = comPath;
							PRIVATE_DIR = new File(PRIVATE_PATH);
							Log.d(TAG, "sdCard ok" + PRIVATE_DIR + ", tmp = " + tmp);
							maxTotal = totalSpace;
							// max old
							Log.d(TAG, "delete " + maxPrev + ": " + maxPrev.delete());
							maxPrev = f;
							if (tmp != null) {
								Log.d(TAG, "delete 2 " + tmp + ": " + tmp.delete());
								tmp = null;
							}
						}
					} catch (IOException e) {
						Log.e(TAG, "tmp " + tmp);
						Log.e(TAG,"PRIVATE_DIR " + PRIVATE_DIR);
						Log.d(TAG,"sdCardPath 2, PRIVATE_PATH = " + PRIVATE_PATH);
					}
				}
			}
		}
	}
	
    private static void init() {
		PRIVATE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + ROOT_CACHE;
		PRIVATE_DIR = new File(PRIVATE_PATH);
		PRIVATE_DIR.mkdirs();
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		//MultiDex.install(this);
	}
	
	@Override
    public void onCreate() {
        super.onCreate();
		userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
		//dexTool();
		instance = this;
		final boolean installed_shortcut = AndroidUtils.getSharedPreference(this, "install_shortcut", false);
		if (!installed_shortcut) {
			AndroidUtils.createShortCut(getApplicationContext(), TextEditorActivity.class, "Text Editor", R.drawable.textpng);
//			AndroidUtils.createShortCut(getApplicationContext(), MediaPlayerActivity.class, "Media Player", R.drawable.exo_banner);
//			AndroidUtils.createShortCut(getApplicationContext(), WebActivity.class, "WebView", R.drawable.html);
			//AndroidUtils.createShortCut(getApplicationContext(), LibraryActivity.class, "PDF Viewer", R.drawable.pdf_icon);
			AndroidUtils.setSharedPreference(this, "install_shortcut", true);
		}

    }
	
	public static ExplorerApplication getInstance() {
        return instance;
    }
	
	/**  
	 * Copy the following code and call dexTool() after super.onCreate() in  
	 * Application.onCreate()  
	 * <p>  * This method hacks the default PathClassLoader and load the secondary dex  
	 * file as it's parent.  
	 */ 
	@SuppressLint("NewApi") 
	private void dexTool() {
		File dexDir = new File(getFilesDir(), "dlibs"); 
		dexDir.mkdir();
		File dexFile = new File(dexDir, "libs.apk"); 
		File dexOpt = new File(dexDir, "opt"); 
		dexOpt.mkdir(); 
		try { 
			InputStream ins= getAssets().open("libs.apk"); 
			if (dexFile.length() != ins.available()) { 
				FileOutputStream fos = new FileOutputStream(dexFile); 
				byte[] buf = new byte[4096]; 
				int l; 
				while ((l = ins.read(buf)) != -1) {
					fos.write(buf, 0, l); 
				} 
				fos.close();
			} 
			ins.close(); 
		} catch (Exception e) { 
			throw new RuntimeException(e);
		}
		ClassLoader cl = getClassLoader();
		ApplicationInfo ai = getApplicationInfo();
		String nativeLibraryDir = null;
		if (Build.VERSION.SDK_INT > 8) {
			nativeLibraryDir = ai.nativeLibraryDir;
		} else {
			nativeLibraryDir = "/data/data/" + ai.packageName + "/lib/";
		} 
		DexClassLoader dcl = new DexClassLoader(dexFile.getAbsolutePath(), dexOpt.getAbsolutePath(), nativeLibraryDir, cl.getParent());
		try {
			Field f = ClassLoader.class.getDeclaredField("parent"); f.setAccessible(true); f.set(cl, dcl); }
		catch (Exception e) { 
			throw new RuntimeException(e);
		}
	}

//	//2.7.3
//	/** Returns a {@link DataSource.Factory}. */
//	public DataSource.Factory buildDataSourceFactory(TransferListener<? super DataSource> listener) {
//		return new DefaultDataSourceFactory(this, listener, buildHttpDataSourceFactory(listener));
//	}
//
//	/** Returns a {@link HttpDataSource.Factory}. */
//	public HttpDataSource.Factory buildHttpDataSourceFactory(
//		TransferListener<? super DataSource> listener) {
//		return new DefaultHttpDataSourceFactory(userAgent, listener);
//	}

	public boolean useExtensionRenderers() {
		return true;//BuildConfig.FLAVOR.equals("withExtensions");
	}
	
//	//2.3.1
	public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
		return new DefaultDataSourceFactory(this, bandwidthMeter,
											buildHttpDataSourceFactory(bandwidthMeter));
	}

	public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
		return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
	}

	
}

