package com.veinhorn.example;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.signature.StringSignature;
import com.bumptech.glide.request.RequestListener;
import java.io.File;
import com.bumptech.glide.GenericRequestBuilder;
import java.io.InputStream;
import net.gnu.explorer.R;
import android.view.View;
import com.amaze.filemanager.ui.icons.MimeTypes;
import android.util.Log;
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
import android.content.ContentResolver;
import com.bumptech.glide.GifRequestBuilder;
import com.bumptech.glide.BitmapRequestBuilder;
import android.graphics.*;
import net.gnu.androidutil.*;

public class GlideImageLoader {
	private static final String TAG = "GlideImageLoader";

	public static final Pattern GIF_PATTERN = Pattern.compile("^[^/]*?\\.gif$", Pattern.CASE_INSENSITIVE);
	public static final Pattern SVG_PATTERN = Pattern.compile("^[^/]*?\\.svg$", Pattern.CASE_INSENSITIVE);

	public static void loadMedia(final File f, final Context context, final ImageView imageView, final DiskCacheStrategy strategy) {//, final SuccessCallback callback) {
		//final long start= System.currentTimeMillis();
		try {
			//Log.d("GlideImageLoader", "SCHEME_FILE loadMedia " + f.getAbsolutePath());
			final String name = f.getName();
			if (GIF_PATTERN.matcher(name).matches()) {
				RequestListener<File, GifDrawable> listener = new RequestListener<File, GifDrawable>() {
					@Override
					public boolean onException(final Exception p1, final File p2, final Target<GifDrawable> p3, final boolean p4) {
						if (p1 != null) {
							p1.printStackTrace();
						}
						final BitmapRequestBuilder req = Glide.with(context)
							.load(f)
							//.apply(RequestOptions.bitmapTransform(mBitmapTransformation))
							//.bitmapTransform(new CenterCrop(context))
							.asBitmap()
							.dontAnimate()
							.skipMemoryCache(true)
							.diskCacheStrategy(strategy)
							//.fitCenter()
							//.crossFade()
							.placeholder(R.drawable.transparent_256)
							.error(R.drawable.image_error);
						//.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
						if (strategy != DiskCacheStrategy.NONE) {
							req.signature(new StringSignature(f.lastModified() + " " + f.length()));//file.getAbsolute()
						}
						req.into(imageView);//(sMain = new Simple(imageView, callback)));
						return true;
					}
					@Override
					public boolean onResourceReady(final GifDrawable p1, final File p2, final Target<GifDrawable> p3, final boolean p4, final boolean p5) {
						return false;
					}
				};
				final GifRequestBuilder req = Glide.with(context)
					.load(f)
					.asGif()
					.dontAnimate()
					.skipMemoryCache(true)
					.diskCacheStrategy(strategy)
					.placeholder(R.drawable.transparent_256)
					.listener(listener)
					.error(R.drawable.image_error);
				if (strategy != DiskCacheStrategy.NONE) {
					req.signature(new StringSignature(f.lastModified() + " " + f.length()));//file.getAbsolute()
				}
				req.into(imageView);//(sMain = new Simple(imageView, callback)));
			} else if (SVG_PATTERN.matcher(name).matches()) {
				Glide.clear(imageView);
				final GenericRequestBuilder<File, InputStream, SVG, PictureDrawable> requestBuilder = Glide.with(context)
					.using(Glide.buildStreamModelLoader(File.class, context), InputStream.class)
					.from(File.class)
					.as(SVG.class)
					.transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
					.sourceEncoder(new StreamEncoder())
					.cacheDecoder(new FileToStreamDecoder<SVG>(new SvgDecoder()))
					.decoder(new SvgDecoder())
					.placeholder(R.drawable.transparent_256)
					.error(ImageThreadLoader.xml64)
					//.animate(android.R.anim.fade_in)
					.dontAnimate()
					.listener(new SvgSoftwareLayerSetter<File>());

				requestBuilder
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					// SVG cannot be serialized so it's not worth to cache it
					.load(f)
					.into(imageView);
				//Log.d(TAG, requestBuilder + ", " + fPath + ", " + file.getAbsolutePath());
			} else {
				Glide.clear(imageView);
				//BitmapUtil.recycleBitmap(imageView);
				//BitmapUtil.setImageDrawable(imageView, uri.getPath());
				RequestListener <File, Bitmap> requestListener = new
					RequestListener<File, Bitmap>() {

					@Override
					public boolean onException (Exception e, File model, Target <Bitmap> target, boolean isFirstResource) {
// todo log exception
// important to return false so the error placeholder can be placed
						Log.e(TAG, e.getMessage(), e);
						return false;
					}
					@Override
					public boolean onResourceReady(
						Bitmap resource, File model, Target <Bitmap> target,
						boolean isFromMemoryCache, boolean
						isFirstResource ) {

						//Log.d(TAG, "onResourceReady " + imageView.getMeasuredWidth() + ", " + imageView.getMeasuredHeight() + ", " + model);
						return false;
					}
				};
				final BitmapRequestBuilder req = Glide.with(context)
					.load(f)
					//.apply(RequestOptions.bitmapTransform(mBitmapTransformation))
					//.bitmapTransform(new CenterCrop(context))
					.asBitmap()
					.listener(requestListener)
					.dontAnimate()
					.skipMemoryCache(true)
					.diskCacheStrategy(strategy)
					//.fitCenter()
					//.crossFade()
					.placeholder(R.drawable.transparent_256)
					.error(R.drawable.image_error);
				//.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
				if (strategy != DiskCacheStrategy.NONE) {
					req.signature(new StringSignature(f.lastModified() + " " + f.length()));//file.getAbsolute()
				}
				req.into(imageView);//(sMain = new Simple(imageView, callback)));
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
	
	public static void loadMedia(final Uri uri, final Context context, final ImageView imageView, final DiskCacheStrategy strategy) {//, final SuccessCallback callback) {
		//final long start= System.currentTimeMillis();
		try {
			if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
				final File f = new File(Uri.decode(uri.getPath()));
				//Log.d("GlideImageLoader", "SCHEME_FILE loadMedia " + f.getAbsolutePath());
				final String name = f.getName();
				if (GIF_PATTERN.matcher(name).matches()) {
					RequestListener<File, GifDrawable> listener = new RequestListener<File, GifDrawable>() {
						@Override
						public boolean onException(final Exception p1, final File p2, final Target<GifDrawable> p3, final boolean p4) {
							if (p1 != null) {
								p1.printStackTrace();
							}
							final BitmapRequestBuilder req = Glide.with(context)
								.load(uri)
								//.apply(RequestOptions.bitmapTransform(mBitmapTransformation))
								//.bitmapTransform(new CenterCrop(context))
								.asBitmap()
								.dontAnimate()
								.skipMemoryCache(true)
								.diskCacheStrategy(strategy)
								//.fitCenter()
								//.crossFade()
								.placeholder(R.drawable.transparent_256)
								.error(R.drawable.image_error);
							//.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
							if (strategy != DiskCacheStrategy.NONE) {
								req.signature(new StringSignature(f.lastModified() + " " + f.length()));//file.getAbsolute()
							}
							req.into(imageView);//(sMain = new Simple(imageView, callback)));
							return true;
						}
						@Override
						public boolean onResourceReady(final GifDrawable p1, final File p2, final Target<GifDrawable> p3, final boolean p4, final boolean p5) {
							return false;
						}
					};
					final GifRequestBuilder req = Glide.with(context)
						.load(f)
						.asGif()
						.dontAnimate()
						.skipMemoryCache(true)
						.diskCacheStrategy(strategy)
						.placeholder(R.drawable.transparent_256)
						.listener(listener)
						.error(R.drawable.image_error);
					if (strategy != DiskCacheStrategy.NONE) {
						req.signature(new StringSignature(f.lastModified() + " " + f.length()));//file.getAbsolute()
					}
					req.into(imageView);//(sMain = new Simple(imageView, callback)));
				} else if (SVG_PATTERN.matcher(name).matches()) {
					Glide.clear(imageView);
					final GenericRequestBuilder<File, InputStream, SVG, PictureDrawable> requestBuilder = Glide.with(context)
						.using(Glide.buildStreamModelLoader(File.class, context), InputStream.class)
						.from(File.class)
						.as(SVG.class)
						.transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
						.sourceEncoder(new StreamEncoder())
						.cacheDecoder(new FileToStreamDecoder<SVG>(new SvgDecoder()))
						.decoder(new SvgDecoder())
						.placeholder(R.drawable.transparent_256)
						.error(ImageThreadLoader.xml64)
						//.animate(android.R.anim.fade_in)
						.dontAnimate()
						.listener(new SvgSoftwareLayerSetter<File>());

					requestBuilder
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						// SVG cannot be serialized so it's not worth to cache it
						.load(f)
						.into(imageView);
					//Log.d(TAG, requestBuilder + ", " + fPath + ", " + file.getAbsolutePath());
				} else {
					Glide.clear(imageView);
					//BitmapUtil.recycleBitmap(imageView);
					//BitmapUtil.setImageDrawable(imageView, uri.getPath());
					RequestListener <File, Bitmap> requestListener = new
						RequestListener<File, Bitmap>() {

						@Override
						public boolean onException (Exception e, File model, Target <Bitmap> target, boolean isFirstResource) {
// todo log exception
// important to return false so the error placeholder can be placed
							Log.e(TAG, e.getMessage(), e);
							return false;
						}
						@Override
						public boolean onResourceReady(
							Bitmap resource, File model, Target <Bitmap> target,
							boolean isFromMemoryCache, boolean
							isFirstResource ) {

							//Log.d(TAG, "onResourceReady " + imageView.getMeasuredWidth() + ", " + imageView.getMeasuredHeight() + ", " + model);
							return false;
						}
					};
					final BitmapRequestBuilder req = Glide.with(context)
						.load(f)
						//.apply(RequestOptions.bitmapTransform(mBitmapTransformation))
						//.bitmapTransform(new CenterCrop(context))
						.asBitmap()
						.listener(requestListener)
						.dontAnimate()
						.skipMemoryCache(true)
						.diskCacheStrategy(strategy)
						//.fitCenter()
						//.crossFade()
						.placeholder(R.drawable.transparent_256)
						.error(R.drawable.image_error);
					//.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
					if (strategy != DiskCacheStrategy.NONE) {
						req.signature(new StringSignature(f.lastModified() + " " + f.length()));//file.getAbsolute()
					}
					req.into(imageView);//(sMain = new Simple(imageView, callback)));
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
			} else {
				Log.d("GlideImageLoader", "!SCHEME_FILE loadMedia " + uri);
				Glide.clear(imageView);
				Glide.with(context)
					.load(uri)
					.asBitmap()
					.dontAnimate()
					.skipMemoryCache(true)
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					//.fitCenter()
					//.crossFade()
					.placeholder(R.drawable.transparent_256)
					.error(R.drawable.image_error)
					//.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
					.into(imageView);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		//Log.d(TAG, f.getAbsolutePath() + " loadMedia " + Util.nf.format(System.currentTimeMillis() - start));
	}

//	public static void loadThumbnail(final File f, final Context context, final ImageView thumbnailView) {//}, final int dpToPx) {//, SuccessCallback callback
//		//final int dpToPx = AndroidUtils.dpToPx(56, context);
//		//final long start= System.currentTimeMillis();
//        //Log.d(TAG, "loadThumbnail " + f.getAbsolutePath());
//		try {
//			final String name = f.getName();
//			if (GIF_PATTERN.matcher(name).matches()) {
//				Glide.with(context)
//					.load(f)
//					.asGif()
//					.skipMemoryCache(true)
//					.diskCacheStrategy(DiskCacheStrategy.RESULT)
//					.signature(new StringSignature(f.lastModified() + " " + f.length()))//file.getAbsolute()
//					.placeholder(R.drawable.ic_doc_image)
//					.into(thumbnailView);//(sMain = new Simple(imageView, callback)));
//			} else if (SVG_PATTERN.matcher(name).matches()) {
//				Glide.clear(thumbnailView);
//				GenericRequestBuilder<File, InputStream, SVG, PictureDrawable> requestBuilder = Glide.with(context)
//					.using(Glide.buildStreamModelLoader(File.class, context), InputStream.class)
//					.from(File.class)
//					.as(SVG.class)
//					.transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
//					.sourceEncoder(new StreamEncoder())
//					.cacheDecoder(new FileToStreamDecoder<SVG>(new SvgDecoder()))
//					.decoder(new SvgDecoder())
//					.placeholder(R.drawable.image_loading)
//					.error(ImageThreadLoader.xml64)
//					//.animate(android.R.anim.fade_in)
//					.dontAnimate()
//					.listener(new SvgSoftwareLayerSetter<File>());
//
//				requestBuilder
//					.diskCacheStrategy(DiskCacheStrategy.SOURCE)
//					// SVG cannot be serialized so it's not worth to cache it
//					.load(f)
//					.into(thumbnailView);
////			Log.d(TAG, requestBuilder + ", " + fPath + ", " + file.getAbsolutePath());
//			} else {
//				Glide.with(context)
//					.load(f)
//					.asBitmap()//android:adjustViewBounds="true"
//					.skipMemoryCache(true)
//					.diskCacheStrategy(DiskCacheStrategy.RESULT)
//					//.resize(100, 100)
//					.fitCenter()
//					//.crossFade()
//					.signature(new StringSignature(f.lastModified() + " " + f.length()))
//					.placeholder(R.drawable.ic_doc_image)
//					//.error(R.drawable.image_error)
//					//.override(dpToPx, dpToPx)
//					.into(thumbnailView);//(sThumb = new Simple(thumbnailView, null)));// tranh setTag
//
//			}
//		} catch (Throwable t) {
//			t.printStackTrace();
//		}
//		//Log.d(TAG, f.getAbsolutePath() + " loadThumbnail " + Util.nf.format(System.currentTimeMillis() - start));
//	}
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




