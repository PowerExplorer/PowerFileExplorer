package net.gnu.androidutil;

import android.graphics.*;
import android.webkit.*;
import android.content.*;
import java.io.*;
import android.net.*;
import android.util.*;
import android.view.*;
import android.os.*;
import android.widget.*;
import android.graphics.drawable.*;
import android.provider.*;
import android.app.*;
import net.gnu.util.*;
import android.media.ThumbnailUtils;
import android.content.res.Resources;

public class BitmapUtil {

	private static final String TAG = "BitmapUtil";

	public static Bitmap drawableToBitmap(final Drawable drawable) {
		if (drawable instanceof BitmapDrawable ) {
			return (( BitmapDrawable ) drawable).getBitmap();
		}
		// We ask for the bounds if they have been set as they would be most
		// correct, then we check we are  > 0
		final int width = !drawable.getBounds().isEmpty() ?
            drawable.getBounds().width() : drawable.getIntrinsicWidth();
		final int height = !drawable.getBounds().isEmpty() ?
            drawable.getBounds().height() : drawable.getIntrinsicHeight();
		// Now we check we are > 0
		final Bitmap bitmap = Bitmap.createBitmap(
				width <= 0 ? 1 : width, 
				height <= 0 ? 1 : height, 
				drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888: Bitmap.Config.RGB_565);
		final Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}

    public static Bitmap loadBitmapFromView(View v, final int w, final int h) {
		final Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		final Canvas c = new  Canvas(b);
		v.layout(0, 0, w, h);
		v.draw(c);
		return b;
	}

	public static void saveDrawable2Bitmap(Context ctx, int resId, String fName, int color, PorterDuff.Mode mode) throws Resources.NotFoundException {
		BitmapDrawable d = (BitmapDrawable) ctx.getResources().getDrawable(resId);
		d.setColorFilter(color, mode);
		//d.setFilterBitmap(true);
		d.setXfermode(new PorterDuffXfermode(mode));
		d = (BitmapDrawable) d.mutate();
		BitmapUtil.saveBitmap(d.getBitmap(), "/sdcard/Movies/" + fName + "." + mode.toString() + ".png");
	}

	public static Bitmap createVideoThumbnail(String path) {
        return ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);        
    }

	public static void setImageDrawable(ImageView imgView, Context ctx, int resId) {
		imgView.setImageBitmap(BitmapFactory.decodeResource(ctx.getResources(), resId));
	}

	public static void setImageDrawable(ImageView imgView, String path) {
		final Uri uri = Uri.parse(path);
		path = uri.getPath();
		Drawable createFromPath = Drawable.createFromPath(path);
		Log.d(TAG, "createFromPath " + createFromPath + ", path " + path);
		imgView.setImageDrawable(createFromPath);
	}

	public static Bitmap createResizedBitmap(byte[] img, int newHeight,
											 int newWidth) {

		// Calculate the correct sample size for the new resolution
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = calculateSampleSize(getBitmapDimesions(img),
												   newHeight, newWidth);

		// Create a bitmap from the byte array, and scale it to 80px x 80px
		return Bitmap.createScaledBitmap(
			BitmapFactory.decodeByteArray(img, 0, img.length, options), 80,
			80, false);
	}

	public static int calculateSampleSize(BitmapFactory.Options options,
										  int newHeight, int newWidth) {
		int sampleSize = 1;

		while (((options.outHeight / 2) / sampleSize) > newHeight
			   && ((options.outWidth / 2) / sampleSize) > newWidth) {

			// Make sure the sample size is a power of 2.
			sampleSize <<= 2;
		}
		return sampleSize;
	}

	public static BitmapFactory.Options getBitmapDimesions(byte[] img) {

		// Make sure the bitmap is not stored in memory!
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeByteArray(img, 0, img.length, options);

		return options;
	}

	public static BitmapFactory.Options getBitmapDimesions(final String img) {

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(img, options);

		return options;
	}

	public static Bitmap getBitmapForVisibleRegion(WebView webview) {
		Bitmap returnedBitmap = null;
		webview.setDrawingCacheEnabled(true);
		returnedBitmap = Bitmap.createBitmap(webview.getDrawingCache());
		webview.setDrawingCacheEnabled(false);
		return returnedBitmap;
	}

	public static Bitmap getThumbnail(Context context, Uri uri, int size)
	throws FileNotFoundException, IOException {
        InputStream input = context.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;// optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight
			: onlyBoundsOptions.outWidth;

        double ratio = (originalSize > size) ? (originalSize / size) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true;// optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0)
            return 1;
        else
            return k;
    }

	public Bitmap scaleRelative2View(View view, Bitmap bitmap, float inScaleX, float inScaleY) {
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();

        float outScaleX, outScaleY;
        if (inScaleY != 0f) {
            // take the given y scale
            outScaleY = (view.getHeight() * inScaleY) / bitmap.getHeight();
        } else {
            // take outScaleX
            outScaleY = (view.getWidth() * inScaleX) / bitmap.getWidth();
        }

        if (inScaleX != 0f) {
            // take the given x scale
            outScaleX = (view.getWidth() * inScaleX) / bitmap.getWidth();
        } else {
            // take the given y scale
            outScaleX = (view.getHeight() * inScaleY) / bitmap.getHeight();
        }

        matrix.postScale(outScaleX, outScaleY);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

	public static Bitmap resizeKeepScale(final Bitmap bitmap, final int max) {
		//Log.d(TAG, "resizeKeepScale " + max);
		int bmpWidth = bitmap.getWidth();
		int bmpHeight = bitmap.getHeight();
		if (bmpWidth < bmpHeight) {
			bmpWidth = Math.max(1, (bmpWidth * max / bmpHeight));
			bmpHeight = max;
		} else {
			bmpHeight = Math.max(1, (bmpHeight * max / bmpWidth));
			bmpWidth = max;
		}
		final Bitmap createScaledBitmap = Bitmap.createScaledBitmap(bitmap, bmpWidth, bmpHeight, true);
		final Rect rect = new Rect((max - bmpWidth)/2, (max - bmpHeight)/2, bmpWidth, bmpHeight);

		final Bitmap output = Bitmap.createBitmap(max, max, Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(output);
		canvas.drawARGB(0, 0, 0, 0);

		final Paint paint = new Paint();
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
		canvas.drawBitmap(createScaledBitmap, rect, rect, paint);
		createScaledBitmap.recycle();
		return output;
	}

	/**
	 * scale the bitmap to a long edge of max
	 * 
	 * @param Bitmap
	 * @param Integer
	 * @return Bitmap
	 */
	public static Bitmap scaleBmp(final Bitmap bmp, final int max) {
		int bmpWidth = bmp.getWidth();
		int bmpHeight = bmp.getHeight();
		if (bmpWidth < bmpHeight) {
			bmpWidth = Math.max(1, (bmpWidth * max / bmpHeight));
			bmpHeight = max;
		} else {
			bmpHeight = Math.max(1, (bmpHeight * max / bmpWidth));
			bmpWidth = max;
		}
		//if (bmpWidth > 0 && bmpWidth > 0) {
		return Bitmap.createScaledBitmap(bmp, bmpWidth,
										 bmpHeight, true);
//		}
//		return bmp;
	}

	public static File saveBitmap(final Bitmap bitmap,
								  final String filename) {
		OutputStream outStream = null;
		BufferedOutputStream bos = null;
		final File out = new File(filename);
		out.getParentFile().mkdirs();
		Log.i("GEITH", "Writing Bitmap to " + filename);
		try {
			outStream = new FileOutputStream(out);
			bos = new BufferedOutputStream(outStream);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			FileUtil.flushClose(bos, outStream);
		}
		return out;
	}

	public static void recycleBitmap(ImageView iv) {
        Drawable d = iv.getDrawable();
        if (d instanceof BitmapDrawable) {
            Bitmap b = ((BitmapDrawable)d).getBitmap();
            b.recycle();
        }
        d.setCallback(null);
    }

	public static Bitmap rotate(Bitmap bitmap, int rotation) {

        int targetWidth = bitmap.getWidth();
        int targetHeight = bitmap.getHeight();

        if (rotation == 90 || rotation == 270) {
            targetHeight = bitmap.getWidth();
            targetWidth = bitmap.getHeight();
        }

        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight,
												  Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(targetBitmap);
        Matrix matrix = new Matrix();
        matrix.setRotate(rotation, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        canvas.drawBitmap(bitmap, matrix, new Paint());

        bitmap.recycle();
        return targetBitmap;
    }

	public static Bitmap rotateImageView(int angle, Bitmap bitmap) {

		if (bitmap == null)
			return null;

		Matrix matrix = new Matrix();
		matrix.postRotate(angle);

		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
												   bitmap.getHeight(), matrix, true);
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap = null;
		}
		bitmap.recycle();
		return resizedBitmap;
	}

	public static Bitmap loadScaledBitmap(Context context, String bitmapFilePath, int widthDp, int heightDp) throws IOException {
		//create movie icon
		Bitmap bitmap;
		bitmap = BitmapFactory.decodeStream(context.openFileInput(bitmapFilePath));
		bitmap = Bitmap.createScaledBitmap(bitmap, widthDp, heightDp, true);
		return bitmap;
	}

	public static Bitmap loadBitmapAndScale(final String filePath,
											final int width, final int height) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		options.inDither = false;
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inTempStorage = new byte[32 * 1024];
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, width, height);
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		final Bitmap b = BitmapFactory.decodeFile(filePath, options);
		if (b != null) {
			final Bitmap bOut = Bitmap.createScaledBitmap(b, width, height,
														  true);
			if (!bOut.equals(b)) {
				b.recycle();
			}
			return bOut;
		}

		return null;
	}

	private static int calculateInSampleSize(
		final BitmapFactory.Options options, final int reqWidth,
		final int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
				   && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize <<= 2;
			}
		}
		return inSampleSize;
	}

	public static Bitmap circularBitmap(Bitmap bitmap) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
											bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		// canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
						  bitmap.getWidth() / 2, paint);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		//Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
		//return _bmp;
		return output;
	}

	public static Bitmap centerCrop(Bitmap srcBmp) {
        Bitmap dstBmp = null;
        if (srcBmp.getWidth() >= srcBmp.getHeight()) {
            dstBmp = Bitmap.createBitmap(
				srcBmp,
				srcBmp.getWidth() / 2 - srcBmp.getHeight() / 2,
				0,
				srcBmp.getHeight(),
				srcBmp.getHeight()
			);
        } else {
            dstBmp = Bitmap.createBitmap(
				srcBmp,
				0,
				srcBmp.getHeight() / 2 - srcBmp.getWidth() / 2,
				srcBmp.getWidth(),
				srcBmp.getWidth()
			);
        }
        return dstBmp;
    }

	public static Bitmap cropCenter(Bitmap bitmap) {

        int minSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        int diffSize = Math.abs(bitmap.getWidth() - bitmap.getHeight());

        Bitmap targetBitmap = Bitmap.createBitmap(minSize, minSize, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Matrix matrix = new Matrix();
        if (bitmap.getWidth() >= bitmap.getHeight())
            matrix.setTranslate(diffSize, 0);
        else
            matrix.setTranslate(0, diffSize);

        canvas.drawBitmap(targetBitmap, new Matrix(), new Paint());

        bitmap.recycle();
        return targetBitmap;
    }

	public static Bitmap drawableToBitmap(int res_id, Context context) {
		BitmapDrawable drawable = (BitmapDrawable) context.getResources().getDrawable(res_id);
		Bitmap bitmap = drawable.getBitmap();
		return bitmap;
	}

	public static Drawable bitmapToDrawable(Bitmap bitmap) {
		return new BitmapDrawable(bitmap);
    }

	public static Bitmap bytes2Bimap(byte[] b) {
		if (b.length == 0) {
			return null;
		}
		return BitmapFactory.decodeByteArray(b, 0, b.length);  
    }

	public static Bitmap getScaledScreenshot(final Activity activity, int scaleWidth, int scaleHeight, boolean relativeScaleIfTrue) {
        final View someView = activity.findViewById(android.R.id.content);
        final View rootView = someView.getRootView();
        final boolean originalCacheState = rootView.isDrawingCacheEnabled();
        rootView.setDrawingCacheEnabled(true);
        rootView.buildDrawingCache(true);

        final Bitmap original = rootView.getDrawingCache();
        Bitmap scaled = null;
        if (null != original && original.getWidth() > 0 && original.getHeight() > 0) {
            if (relativeScaleIfTrue) {
                scaleWidth = original.getWidth() / scaleWidth;
                scaleHeight = original.getHeight() / scaleHeight;
            }
            if (scaleWidth > 0 && scaleHeight > 0) {
                scaled = Bitmap.createScaledBitmap(original, scaleWidth, scaleHeight, false);
            }
        }
        if (!originalCacheState) {
            rootView.setDrawingCacheEnabled(false);
        }
        return scaled;
    }

//	public static Bitmap flipBitmap(Bitmap image, int flipType) {
//		Matrix matrix = new Matrix();
//
//		if (flipType == FLIP_HORIZONTAL) {
//			matrix.preScale(-1, 1);
//		} else if (flipType == FLIP_VERTICAL) {
//			matrix.preScale(1, -1);
//		} else {
//			return image;
//		}
//
//		return Bitmap.createBitmap(image, 0, 0, image.getWidth(),
//								   image.getHeight(), matrix, false);
//	}
}
