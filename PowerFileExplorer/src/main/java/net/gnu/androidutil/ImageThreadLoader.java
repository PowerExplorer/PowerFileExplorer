package net.gnu.androidutil;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import com.amaze.filemanager.ui.icons.MimeTypes;
import com.amaze.filemanager.utils.files.CryptUtil;
import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.bumptech.glide.samples.svg.SvgDecoder;
import com.bumptech.glide.samples.svg.SvgDrawableTranscoder;
import com.bumptech.glide.samples.svg.SvgSoftwareLayerSetter;
import com.bumptech.glide.signature.StringSignature;
import com.caverock.androidsvg.SVG;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.LinkedList;
import net.gnu.explorer.ExplorerActivity;
import net.gnu.explorer.ExplorerApplication;
import net.gnu.explorer.R;
import net.gnu.p7zip.ZipEntry;
import net.gnu.util.FileUtil;
//import com.bumptech.glide.*;

public class ImageThreadLoader {
	private static final String TAG = "ImageThreadLoader";

    private static Drawable sdcard_72;
	//private static Drawable system72;
	private static Drawable myfolder72;
	private static Drawable myzip;
	private static Drawable rar;
	private static Drawable pdf_icon;
	private static Drawable textpng;
	private static Drawable html;
	private static Drawable audio;
	private static Drawable videos_new;
	private static Drawable script_file64;
	private static Drawable build_file64;
	public static Drawable xml64;
	private static Drawable nsword64;
	private static Drawable ppt64;
	private static Drawable spreadsheet64;
	private static Drawable miscellaneous;

	public static Drawable apkIcon;
	public static Drawable imageIcon;
	public static Drawable compressIcon;

	private static Bitmap apkIconBitmap;
	private static Drawable fileLockDrawable;
	private static Drawable folderLockDrawable;

	private static final String PREF_CACHE= "cachefiles";
    private static boolean cachefiles;
	private final LoaderThread loaderThread=new LoaderThread();

	public static String SDCARD_PATH;
	public static String MICROSD_PATH;
	public static String USB1_PATH;

	private final LinkedList<UrlImageView> urlImageViews=new LinkedList<UrlImageView>();
	private Activity activity;

	private static final String CACHED_IMG = "/cache_img";
	private static final String CACHED_IMG_PART = ExplorerApplication.ROOT_CACHE + CACHED_IMG;
	private static final String cachePath = ExplorerApplication.PRIVATE_PATH + CACHED_IMG;
	private final BitmapCache bc;

	public ImageThreadLoader(final Activity activity) {
		this.activity = activity;
		bc = new BitmapCache(activity, 20);
		if (sdcard_72 == null) {
			final Resources res = activity.getResources();
			sdcard_72 = res.getDrawable(R.drawable.root);
			//system72 = res.getDrawable(R.drawable.ic_launcher_sdcard);
			myfolder72 = res.getDrawable(R.drawable.myfolder72);
			myzip = res.getDrawable(R.drawable.myzip);
			rar = res.getDrawable(R.drawable.rar);
			pdf_icon = res.getDrawable(R.drawable.pdf_icon);
			textpng = res.getDrawable(R.drawable.textpng);
			html = res.getDrawable(R.drawable.html);
			audio = res.getDrawable(R.drawable.audio);
			videos_new = res.getDrawable(R.drawable.ic_launcher_video);
			script_file64 = res.getDrawable(R.drawable.script_file64);
			build_file64 = res.getDrawable(R.drawable.build_file64);
			xml64 = res.getDrawable(R.drawable.xml64);
			nsword64 = res.getDrawable(R.drawable.nsword64);
			ppt64 = res.getDrawable(R.drawable.ppt64);
			spreadsheet64 = res.getDrawable(R.drawable.spreadsheet64);
			miscellaneous = res.getDrawable(R.drawable.miscellaneous);
			apkIcon = res.getDrawable(R.drawable.ic_doc_apk);
			imageIcon = res.getDrawable(R.drawable.ic_launcher_image);
			apkIconBitmap = BitmapUtil.drawableToBitmap(ImageThreadLoader.apkIcon);
			folderLockDrawable = res.getDrawable(R.drawable.ic_folder_lock_white_36dp);
			fileLockDrawable = res.getDrawable(R.drawable.ic_file_lock_white_36dp);
			compressIcon = res.getDrawable(R.drawable.ic_doc_compressed);
			compressIcon.setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);

			//Find the dir to save cached images
			final File cacheDir2 = new File(cachePath);
			if (!cacheDir2.exists())
				cacheDir2.mkdirs();

			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
			cachefiles = prefs.getBoolean(PREF_CACHE, true);
			//cachefiles=PreferenceActivity.CacheFiles(context);
		}

        //Make the background thead low priority. This way it will not affect the UI performance
        loaderThread.setPriority(Thread.NORM_PRIORITY - 1);
		loaderThread.start();
	}

//	public Drawable getFolderIcon(final String fPath) {
//		if (fPath.equals("/sdcard")) {
//			return sdcard_72;
////		} else if (fPath.equals("/system")) {
////			return system72;
//		} else {
//			return myfolder72;
//		}
//	}

	public static int getResId(final File f) {
		final String mimeType = MimeTypes.getMimeType(f);
		final String ext = FileUtil.getExtension(f.getName());
		if (ext.equals("zip")) {
			return R.drawable.myzip;
		} else if (ext.equals("rar")) {
			return R.drawable.rar;
		} else if (ext.equals("pdf")) {
			return R.drawable.pdf_icon;
		} else if (ext.equals("html")) {
			return R.drawable.html;
		} else if (ext.equals("txt") || mimeType.startsWith("text")) {
			return R.drawable.textpng;
		} else if (ext.equals("sh") || ext.equals("rc")) {
			return R.drawable.script_file64;
		} else if (ext.equals("prop")) {
			return R.drawable.build_file64;
		} else if (ext.equals("xml")) {
			return R.drawable.xml64;
		} else if (ext.equals("doc")
				   || ext.equals("docx")) {
			return R.drawable.nsword64;
		} else if (ext.equals("ppt")
				   || ext.equals("pptx")) {
			return R.drawable.ppt64;
		} else if (ext.equals("xls")
				   || ext.equals("xlsx")) {
			return R.drawable.spreadsheet64;
		} else if (ext.equals(CryptUtil.CRYPT_EXTENSION)) {
			return R.drawable.ic_file_lock_white_36dp;
		} else if (mimeType.startsWith("image")) {
			return R.drawable.ic_launcher_image;
		} else if (FileUtil.extractibleExtensionPattern.matcher(ext).matches()) {
			return R.drawable.ic_doc_compressed;
		} else {
			return R.drawable.miscellaneous;
		}
	}

	public Drawable getFileIcon(final String f) {
		final String mimeType = MimeTypes.getMimeType(f);
		final String ext = FileUtil.getExtension(new File(f).getName());
		return getFileIcon(mimeType, ext);
	}

	public Drawable getFileIcon(final File f) {
		final String mimeType = MimeTypes.getMimeType(f);
		final String ext = FileUtil.getExtension(f.getName());
		return getFileIcon(mimeType, ext);
	}

	public Drawable getFileIcon(final String mimeType, final String ext) {
		if (ext.equals("zip")) {
			return myzip;
		} else if (ext.equals("rar")) {
			return rar;
		} else if (ext.equals("pdf")) {
			return pdf_icon;
		} else if (ext.equals("html")) {
			return html;
		} else if (ext.equals("txt") || mimeType.startsWith("text")) {
			return textpng;
		} else if (ext.equals("sh") || ext.equals("rc")) {
			return script_file64;
		} else if (ext.equals("prop")) {
			return build_file64;
		} else if (ext.equals("xml")) {
			return xml64;
		} else if (ext.equals("doc")
				   || ext.equals("docx")) {
			return nsword64;
		} else if (ext.equals("ppt")
				   || ext.equals("pptx")) {
			return ppt64;
		} else if (ext.equals("xls")
				   || ext.equals("xlsx")) {
			return spreadsheet64;
		} else if (ext.equals(CryptUtil.CRYPT_EXTENSION)) {
			return fileLockDrawable;
		} else if (mimeType.startsWith("video")) {
			return videos_new;
		} else if (mimeType.startsWith("audio")) {
			return audio;
		} else if (mimeType.startsWith("image")) {
			return imageIcon;
		} else if (FileUtil.extractibleExtensionPattern.matcher(ext).matches()) {
			return compressIcon;
		} else {
			return miscellaneous;
		}
	}

	public void displayImage(final ZipEntry ze, final Context ctx, final ImageView imageView, int cols) {
		if (cols <= 2) {
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		} else {
			imageView.setScaleType(ImageView.ScaleType.CENTER);
		}
		if (ze.isDirectory) {
			imageView.setImageDrawable(myfolder72);
		} else {
			imageView.setImageDrawable(getFileIcon(ze.path));
		}
	}

    /**
     * Display image.
     *
     * @param url the url
     * @param activity the activity
     * @param imageView the image view
     */
    public void displayImage(final File file, final Context ctx, final ImageView imageView, int cols) {
		final String fPath = file.getAbsolutePath();
		//imageView.setContentDescription(fPath);
		if (file.isDirectory()) {
			if (cols <= 2) {
				imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			} else {
				imageView.setScaleType(ImageView.ScaleType.CENTER);
			}
			imageView.setImageDrawable(myfolder72);//getFolderIcon(fPath));
		} else {
			final Bitmap b = bc.getBitmapFromCache(fPath + file.length() + file.lastModified());
			if (b != null) {
				imageView.setImageBitmap(b);
			} else {
				String type;
				final String ext = FileUtil.getExtension(file.getName());
				if (ext.equals("apk")) {
					imageView.setImageDrawable(apkIcon);
					queuePhoto(fPath, ctx, imageView);
				} else if ((type = MimeTypes.getMimeType(file)) != null) {
					//Log.d(TAG, fPath + "=" + type);
					if (type.startsWith("audio")) {
						if (cols <= 2) {
							imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
						} else {
							imageView.setScaleType(ImageView.ScaleType.CENTER);
						}
						imageView.setImageDrawable(audio);
//					} else if (type.startsWith("text/xml")) {
//						BitmapUtil.setImageDrawable(imageView, fPath);
					} else if (type.startsWith("image/svg+xml")) {
						//Glide.clear(imageView);
						GenericRequestBuilder<File, InputStream, SVG, PictureDrawable> requestBuilder = Glide.with(ctx)
							.using(Glide.buildStreamModelLoader(File.class, ctx), InputStream.class)
							.from(File.class)
							.as(SVG.class)
							.transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
							.sourceEncoder(new StreamEncoder())
							.cacheDecoder(new FileToStreamDecoder<SVG>(new SvgDecoder()))
							.decoder(new SvgDecoder())
							.placeholder(R.drawable.image_loading)
							.error(xml64)
							//.animate(android.R.anim.fade_in)
							.dontAnimate()
							.listener(new SvgSoftwareLayerSetter<File>());

						requestBuilder
							.diskCacheStrategy(DiskCacheStrategy.SOURCE)
							// SVG cannot be serialized so it's not worth to cache it
							.load(file)
							.into(imageView);
						Log.d(TAG, requestBuilder + ", " + fPath + ", " + file.getAbsolutePath());
					} else if (type.startsWith("image")) {
//						imageView.setImageDrawable(imageIcon);
//						queuePhoto(fPath, ctx, imageView);
						Glide
							.with(ctx)
							.load(file)
							.asBitmap()//android:adjustViewBounds="true"
							.skipMemoryCache(true)
							.diskCacheStrategy(DiskCacheStrategy.RESULT)
							.fitCenter()
							.placeholder(imageIcon)
							//.crossFade()
							.signature(new StringSignature(file.lastModified() + " " + file.length()))
							.into(imageView);
					} else if (type.startsWith("video")) {
//						imageView.setImageDrawable(videos_new);
//						queuePhoto(fPath, ctx, imageView);
						Glide
							.with(ctx)
							.load(file)
							.asBitmap()
							.skipMemoryCache(true)
							.diskCacheStrategy(DiskCacheStrategy.RESULT)
							.fitCenter()
							.placeholder(videos_new)
							//.crossFade()
							.signature(new StringSignature(file.lastModified() + " " + file.length()))
							.into(imageView);
					} else {
						final Drawable fileIcon = getFileIcon(file);
						if (cols <= 2) {
							imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
						} else {
							imageView.setScaleType(ImageView.ScaleType.CENTER);
						}
						if (type.startsWith("text") && fileIcon == miscellaneous) {
							imageView.setImageDrawable(textpng);
						} else {
							imageView.setImageDrawable(fileIcon);
						}
					}
				} else {
					if (cols <= 2) {
						imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
					} else {
						imageView.setScaleType(ImageView.ScaleType.CENTER);
					}
					imageView.setImageDrawable(getFileIcon(file));
				}
			}
		}
	} 

	/**
     * Queue photo.
     *
     * @param url the url
     * @param activity the activity
     * @param imageView the image view
     */
    private void queuePhoto(final String url, final Context ctx, final ImageView imageView) {

        final UrlImageView urlImageView = new UrlImageView(url, imageView);
		synchronized (urlImageViews) {
			urlImageViews.push(urlImageView);
            urlImageViews.notifyAll();
        } 
		//start thread if it's not started yet
//        if (loaderThread.getState()==Thread.State.NEW)
//            loaderThread.start();
    }

	/**
     * Cache image.
     *
     * @param bmp the Bitmap
     * @param url the filepath
     */
	public Bitmap createCacheFile(final Bitmap oriBitmap, final String url, final int width, final int height) {
		//Log.d(TAG, cachePath + " createCacheFile " + url);
        if (url.contains(CACHED_IMG_PART)) {
			return oriBitmap;
		} else {
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			try {
				final File img = new File(cachePath, FileUtil.getPathHash(new File(url)) + ".png");
				fos = new FileOutputStream(img);
				bos = new BufferedOutputStream(fos);
				final Bitmap bitmap = oriBitmap;//Bitmap.createScaledBitmap(oriBitmap, width, height, true);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
				//if (!bOut.equals(bmp)) {
				//oriBitmap.recycle();
				//}
				return bitmap;
			} catch (Exception e) {
				e.printStackTrace();
				return oriBitmap;
			} finally {
				FileUtil.flushClose(bos, fos);
			}
		}
	}

	/**
     * Gets the bitmap.
     *
     * @param url the url
     * @return the bitmap
     */
    private Bitmap getBitmap(final String url, final int width, final int height) {
		//Log.d(TAG, cachePath + " getBitmap " + url);
        final File file = new File(url);
		final String lastModified = url + file.length() + file.lastModified();
		final Bitmap b1 = bc.getBitmapFromCache(lastModified);
		File hashNameFile=new File(cachePath, (FileUtil.getPathHash(file) + ".png"));
		if (b1 != null  && hashNameFile.lastModified() > file.lastModified()) {
            return b1;
		}

		if (url.contains(CACHED_IMG_PART)) {
			hashNameFile = file;
		} 
		//I identify images by hashcode. Not a perfect solution, good for the demo.
		if (hashNameFile.exists() && hashNameFile.lastModified() >= file.lastModified()) {
			final Bitmap d = BitmapFactory.decodeFile(hashNameFile.getAbsolutePath());
			//from SD cache
			if (d != null) {
				bc.put(lastModified, d);  
				return d;
			}
		}
        //from origin file
		Bitmap drawableToBitmap;
		String extension = FileUtil.getExtension(file.getName());
		Log.d(TAG, "extension " + file.getName() + " " + extension);
		if ("apk".equals(extension)) {
			final Drawable apkIcon = AndroidUtils.getApkIcon(activity, url);
			if (apkIcon != null) {
				drawableToBitmap = BitmapUtil.drawableToBitmap(apkIcon);
			} else {
				drawableToBitmap = apkIconBitmap;
			}
		} else {
			try {
				final byte[] barr = FileUtil.readFileToMemory(file);
				final BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inJustDecodeBounds = true;
				opts.inSampleSize = 1;

				drawableToBitmap = BitmapFactory.decodeByteArray(barr, 0, barr.length, opts);
				//drawableToBitmap.recycle();

				int w = opts.outWidth;
				int h = opts.outHeight;
				//Log.d(TAG, url + " " + w + ", " + h);
				int t = 1;
				if (w * height >= h * width && w > width) {
					//opts.inSampleSize = opts.outWidth / size;
					while (w > width) {
						w >>= 1;
						t <<= 1;
					}
					opts.inSampleSize = t;
				} else if (w * height < h * width && h > height) {
					//opts.inSampleSize = opts.outHeight / size;
					while (h > height) {
						h >>= 1;
						t <<= 1;
					}
					opts.inSampleSize = t;
				} 
//				if (opts.inSampleSize < 1) {
//					opts.inSampleSize = 1;
//				} 
				opts.inJustDecodeBounds = false;
				//opts.inPreferredConfig = Bitmap.Config.RGB_565;
				drawableToBitmap = BitmapFactory.decodeByteArray(barr, 0, barr.length, opts);
			} catch (Throwable ex) {
				System.gc();
				ex.printStackTrace();
				return null;
			}
		}
		bc.put(lastModified, drawableToBitmap);  
		return drawableToBitmap;
	} 

    public void stopThread() {
        loaderThread.interrupt();
    } 

	//Task for the queue
	private class UrlImageView {

		private String url;
		private ImageView imageView;

		private UrlImageView(final String u, final ImageView i) {
			url = u; 
			imageView = i;
		} 
	}

    private class LoaderThread extends Thread {
        public void run() {
            try {
				while (true) {
                    //thread waits until there are any images to load in the queue
                    synchronized (urlImageViews) {
						if (urlImageViews.size() == 0)
							urlImageViews.wait();
					}
					if (urlImageViews.size() != 0) {
						final UrlImageView urlImageView = urlImageViews.pop();
						final String url = urlImageView.url;
						final ImageView imageView = urlImageView.imageView;
						if (url == imageView.getContentDescription()) {
							final Thread thread = new Thread(new BitmapDisplayer(url, imageView));
							thread.setPriority(Thread.NORM_PRIORITY - 3);
							thread.start();
						}
					}
					if (Thread.interrupted())
                        break;
                }
			} catch (InterruptedException e) {
                //e.printStackTrace();
            }
		}
	}

    //Used to display bitmap in the UI thread
    /**
     * The Class BitmapDisplayer.
     */
    private class BitmapDisplayer implements Runnable {

        private String url;
		private ImageView imageView;

        private BitmapDisplayer(final String u, ImageView i) {
			url = u;
			imageView = i;
		}

        public void run() {
			if (url == imageView.getContentDescription()) {
				final Bitmap bitmap = getBitmap(url, imageView.getWidth(), imageView.getHeight());
				if (bitmap != null) {
					if (url == imageView.getContentDescription()) {
						activity.runOnUiThread(new Runnable() {
								public void run() {
									if (url == imageView.getContentDescription()) {
										synchronized (imageView) {
											if (url == imageView.getContentDescription()) {
												imageView.setImageBitmap(bitmap);
											}
										}
									}
								}
							});
					}
					if (cachefiles) {
						createCacheFile(bitmap, url, bitmap.getWidth(), bitmap.getHeight());
					}
				}
			}
		}
	}	
}
