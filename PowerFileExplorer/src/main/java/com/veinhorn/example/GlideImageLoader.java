package com.veinhorn.example;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.signature.StringSignature;
import java.io.File;
import com.bumptech.glide.GenericRequestBuilder;
import java.io.InputStream;
import net.gnu.explorer.R;
import android.view.View;
import com.amaze.filemanager.ui.icons.MimeTypes;
import android.util.Log;
import net.gnu.explorer.MediaPlayerActivity;
import android.net.Uri;
import android.content.Intent;
import net.gnu.androidutil.AndroidUtils;
import android.widget.Toast;

import net.gnu.util.Util;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.regex.Pattern;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.caverock.androidsvg.SVG;
import com.bumptech.glide.samples.svg.SvgDrawableTranscoder;
import android.graphics.drawable.PictureDrawable;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.samples.svg.SvgDecoder;
import com.bumptech.glide.samples.svg.SvgSoftwareLayerSetter;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import net.gnu.androidutil.ImageThreadLoader;

public class GlideImageLoader {
	//private static final String TAG = "GlideImageLoader";

	/**
	 * Author: Alexey Nevinsky
	 * Date: 06.12.15 1:38
	 */
	//private final File f;
//	private Simple sThumb;
//	private Simple sMain;
	//private final String mime;

//	public GlideImageLoader(File url) {//, String mime
//		this.f = url;
//		//this.mime = mime;
//	}
	public static final Pattern GIF_PATTERN = Pattern.compile("^[^/]*?\\.gif$", Pattern.CASE_INSENSITIVE);
	public static final Pattern SVG_PATTERN = Pattern.compile("^[^/]*?\\.svg$", Pattern.CASE_INSENSITIVE);

	public static void loadMedia(final File f, final Context context, final ImageView imageView) {//, final SuccessCallback callback) {
		//final long start= System.currentTimeMillis();
        //Log.d(TAG, "loadMedia " + f.getAbsolutePath());
		try {
			final String name = f.getName();
			if (GIF_PATTERN.matcher(name).matches()) {
				Glide.with(context)
					.load(f)
					.asGif()
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.signature(new StringSignature(f.lastModified() + " " + f.length()))//file.getAbsolute()
					.placeholder(R.drawable.ic_doc_image_dark)
					.into(imageView);//(sMain = new Simple(imageView, callback)));
			} else if (SVG_PATTERN.matcher(name).matches()) {
				Glide.clear(imageView);
				GenericRequestBuilder<File, InputStream, SVG, PictureDrawable> requestBuilder = Glide.with(context)
					.using(Glide.buildStreamModelLoader(File.class, context), InputStream.class)
					.from(File.class)
					.as(SVG.class)
					.transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
					.sourceEncoder(new StreamEncoder())
					.cacheDecoder(new FileToStreamDecoder<SVG>(new SvgDecoder()))
					.decoder(new SvgDecoder())
					.placeholder(R.drawable.image_loading)
					.error(ImageThreadLoader.xml64)
					//.animate(android.R.anim.fade_in)
					.dontAnimate()
					.listener(new SvgSoftwareLayerSetter<File>());

				requestBuilder
					.diskCacheStrategy(DiskCacheStrategy.SOURCE)
					// SVG cannot be serialized so it's not worth to cache it
					.load(f)
					.into(imageView);
			//Log.d(TAG, requestBuilder + ", " + fPath + ", " + file.getAbsolutePath());
			} else {
				Glide.with(context)
					.load(f)
					//.apply(RequestOptions.bitmapTransform(mBitmapTransformation))
					//.bitmapTransform(new CenterCrop(context))
					.asBitmap()
					//.skipMemoryCache(true)
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					//.fitCenter()
					//.crossFade()
					.signature(new StringSignature(f.lastModified() + " " + f.length()))//file.getAbsolute()
					.placeholder(R.drawable.ic_doc_image_dark)
					//.error(R.drawable.image_error)
					//.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
					.into(imageView);//(sMain = new Simple(imageView, callback)));
//			.into(new SimpleTarget<Bitmap>() {
//				@Override
//				public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
//					imageView.setImageBitmap(bitmap);
//					callback.onSuccess();
//					//thumbView.setImageBitmap(bitmap);
//				}
//			});
//			.listener(new RequestListener<String, GlideDrawable>() {
//				@Override
//				public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
//					return false;
//				}
//
//				@Override
//				public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//					attacher.setZoomable(true);
//					return false;
//				}
//			})
//			.into(imageView);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		//Log.d(TAG, f.getAbsolutePath() + " loadMedia " + Util.nf.format(System.currentTimeMillis() - start));
	}

	public static void loadThumbnail(final File f, final Context context, final ImageView thumbnailView) {//}, final int dpToPx) {//, SuccessCallback callback
		//final int dpToPx = AndroidUtils.dpToPx(56, context);
		//final long start= System.currentTimeMillis();
        //Log.d(TAG, "loadThumbnail " + f.getAbsolutePath());
		try {
			final String name = f.getName();
			if (GIF_PATTERN.matcher(name).matches()) {
				Glide.with(context)
					.load(f)
					.asGif()
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.signature(new StringSignature(f.lastModified() + " " + f.length()))//file.getAbsolute()
					.placeholder(R.drawable.ic_doc_image_dark)
					.into(thumbnailView);//(sMain = new Simple(imageView, callback)));
			} else if (SVG_PATTERN.matcher(name).matches()) {
				Glide.clear(thumbnailView);
				GenericRequestBuilder<File, InputStream, SVG, PictureDrawable> requestBuilder = Glide.with(context)
					.using(Glide.buildStreamModelLoader(File.class, context), InputStream.class)
					.from(File.class)
					.as(SVG.class)
					.transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
					.sourceEncoder(new StreamEncoder())
					.cacheDecoder(new FileToStreamDecoder<SVG>(new SvgDecoder()))
					.decoder(new SvgDecoder())
					.placeholder(R.drawable.image_loading)
					.error(ImageThreadLoader.xml64)
					//.animate(android.R.anim.fade_in)
					.dontAnimate()
					.listener(new SvgSoftwareLayerSetter<File>());

				requestBuilder
					.diskCacheStrategy(DiskCacheStrategy.SOURCE)
					// SVG cannot be serialized so it's not worth to cache it
					.load(f)
					.into(thumbnailView);
//			Log.d(TAG, requestBuilder + ", " + fPath + ", " + file.getAbsolutePath());
			} else {
				Glide.with(context)
					.load(f)
					.asBitmap()//android:adjustViewBounds="true"
					//.resize(100, 100)
					.fitCenter()
					//.crossFade()
					.signature(new StringSignature(f.lastModified() + " " + f.length()))
					.placeholder(R.drawable.ic_doc_image_dark)
					//.error(R.drawable.image_error)
					//.override(dpToPx, dpToPx)
					.into(thumbnailView);//(sThumb = new Simple(thumbnailView, null)));// tranh setTag

			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		//Log.d(TAG, f.getAbsolutePath() + " loadThumbnail " + Util.nf.format(System.currentTimeMillis() - start));
	}
//	class Simple extends SimpleTarget<Bitmap> {
//		final ImageView iview;
//		final SuccessCallback callback;
//		public Simple(final ImageView iview, final SuccessCallback callback) {
//			super(iview.getMeasuredWidth(), iview.getMeasuredHeight());
//			Log.d(TAG, iview.getMeasuredWidth() + " " + iview.getMeasuredHeight());
//			this.iview = iview;
//			this.callback = callback;
//		}
//
//		@Override
//		public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
//			iview.setImageBitmap(bitmap);
//			if (callback != null) {
//				callback.onSuccess();
//			}
//		}
//	}

}



