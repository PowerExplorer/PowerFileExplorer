package com.veinhorn.scrollgalleryview;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ortiz.touch.TouchImageView;
import net.gnu.explorer.R;
import android.support.v4.view.ViewPager;
import android.view.View.OnClickListener;
import java.io.File;
import com.veinhorn.example.GlideImageLoader;
import java.util.regex.Pattern;
import net.gnu.androidutil.AndroidUtils;
import net.gnu.androidutil.BitmapUtil;
import android.app.ProgressDialog;
import android.view.GestureDetector;
import android.view.View.OnTouchListener;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.util.Log;
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.content.Context;
import net.gnu.explorer.ExplorerActivity;
import android.widget.TextView;
import android.view.animation.AnimationUtils;
import android.support.v4.content.ContextCompat;
import android.support.annotation.DrawableRes;
import android.app.Activity;
import android.provider.Settings;
import com.jarvanmo.exoplayerview.util.Permissions;
import net.gnu.util.Util;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import com.bumptech.glide.load.engine.*;
import android.net.*;

/**
 * Created by veinhorn on 29.8.15.
 */
public class ImageFragment extends Fragment {
	private static final String TAG = "ImageFragment";

    private static final Pattern VIDEO_PATTERN = Pattern.compile("^[^\n]*?\\.(avi|mpg|mpeg|mp4|3gpp|3gp|3gpp2|vob|asf|wmv|flv|mkv|asx|qt|mov|webm|mpe|3g2|m4v|wm|wmx|mpa)$", Pattern.CASE_INSENSITIVE);
	
	private File mMediaInfo;

    private TouchImageView image;
    private ImageView videoPlayImage;
    private Runnable zoomCallback;

	private OnDoubleTapListener onDoubleTapListener;
	private GestureDetector mGestureDetector;

	//Touch Events
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_ZOOM = 1;
    private static final int TOUCH_BRIGHTNESS = 2;
    //private static final int TOUCH_SEEK = 3;
	private static final int TOUCH_DELAY = 4;
	private static final int TOUCH_TRANSFORM = 5;
	private static final int TOUCH_PAGE = 6;
	
	//touch
    private int mTouchAction = TOUCH_NONE;
    private int mSurfaceYDisplayRange;
    private float mInitTouchY;
    private float touchX = -1f;
    private float touchY = -1f;

	private TextView centerInfo;
	private boolean mIsFirstBrightnessGesture = true;
	private View rootView;
	private float minZoom;
	private float maxZoom;
	static float DEFAULT_ZOOM = 2f;
	static float curZoom = DEFAULT_ZOOM;
	static int curDelay = 1000;//ScrollGalleryView.DELAY;
	static int curTransform = 12;

	private ViewPager viewPager;
	private Context context;;
	
	private ScrollGalleryView scrollGalleryView;

	@Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
		
        rootView = inflater.inflate(R.layout.image_fragment, container, false);
		context = getContext();
		
        image = (TouchImageView) rootView.findViewById(R.id.image);
        videoPlayImage = (ImageView) rootView.findViewById(R.id.videoPlayImage);
		minZoom = TouchImageView.SUPER_MIN_MULTIPLIER * image.getMinZoom();
		maxZoom = TouchImageView.SUPER_MAX_MULTIPLIER * image.getMaxZoom();
		image.setZoom(curZoom);
        final FragmentActivity activity = getActivity();
		viewPager = (ViewPager) activity.findViewById(R.id.photoViewPager);
		scrollGalleryView = (ScrollGalleryView) activity.findViewById(R.id.scroll_gallery_view);

		mGestureDetector = new GestureDetector(getContext(), new SimpleOnGestureListener() {
				@Override
				public boolean onSingleTapConfirmed(MotionEvent e) {
					Log.d(TAG, "onSingleTapConfirmed " + e + onDoubleTapListener);
					if (onDoubleTapListener != null) {
						return onDoubleTapListener.onSingleTapConfirmed(e);
					}
					return false;//performClick();
				}
			});
        rootView.setOnTouchListener(onTouch);
		//videoPlayImage.setOnTouchListener(onTouch);
		image.setOnTouchListener(onTouch);
		//rootView.setOnDoubleTapListener(onDoubleTapListener);
		//image.setOnDoubleTapListener(onDoubleTapListener);
		centerInfo = (TextView) rootView.findViewById(R.id.centerInfo);
		//backgroundImage.setZoom(1.5f);
		//backgroundImage.setMinZoom(1.0f);
		//backgroundImage.setMaxZoom(3.0f);
		//backgroundImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		//backgroundImage.setZoom(1);

        loadImageToView();

        return rootView;
    }

	public TouchImageView getImage() {
		return image;
	}

    public void setMediaInfo(final File mediaInfo) {
        mMediaInfo = mediaInfo;
    }

    final OnTouchListener onTouch = new OnTouchListener() {
		@Override
		public boolean onTouch(final View p1, final MotionEvent event) {
			//Log.d(TAG, "onTouch " + event);
			mGestureDetector.onTouchEvent(event);
			dispatchCenterWrapperTouchEvent(p1, event);
			return false;
		}
	};

	private boolean dispatchCenterWrapperTouchEvent(final View p1, final MotionEvent event) {
		//Log.d(TAG, "dispatchCenterWrapperTouchEvent " + event);

		final int measuredWidth = rootView.getMeasuredWidth();
		if (mSurfaceYDisplayRange == 0) {
            mSurfaceYDisplayRange = Math.min(measuredWidth, rootView.getMeasuredHeight());
        }

        float x_changed, y_changed;
        if (touchX != -1f && touchY != -1f) {
            y_changed = event.getY() - touchY;
            x_changed = event.getX() - touchX;
        } else {
            x_changed = 0f;
            y_changed = 0f;
        }

        final float coef = Math.abs(y_changed / x_changed);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchAction = TOUCH_NONE;
                touchX = event.getX();
                curZoom = image.getCurrentZoom();
                touchY = mInitTouchY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if ((x_changed == 0 || coef > 2) && Math.abs(y_changed) >= 10) {//mTouchAction != TOUCH_SEEK && 
                    touchX = event.getX();
                    touchY = event.getY();
					//Log.d(TAG, "ACTION_MOVE " + y_changed + ", mTouchAction " + mTouchAction + ", touchX " + touchX + ", measuredWidth " + measuredWidth);
					if ((int) touchX > (4 * measuredWidth / 5)) {
						doDelayTouch(y_changed);
//					} else if ((int) touchX > (3 * measuredWidth / 5)) {
//						doZoomTouch(y_changed);
//						if (zoomCallback != null) {
//							zoomCallback.run();
//						}
					} else if ((int) touchX < (measuredWidth / 5)) {
						doBrightnessTouch(y_changed);
//					} if ((int) touchX < (2 * measuredWidth / 5)) {//Transform
//						doTransformTouch(y_changed);
//					} else {
//						doPageTouch(y_changed);
					}
				}
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchAction != TOUCH_NONE) {
                    hideCenterInfo();
                }
                touchX = -1f;
                touchY = -1f;
                break;
            default:
                break;
        }
        return mTouchAction != TOUCH_NONE;
    }

    private void hideCenterInfo() {
        centerInfo.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
        centerInfo.setVisibility(View.GONE);
    }

//    private void doZoomTouch(final float y_changed) {
//        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_ZOOM) {
//            return;
//        }
//        final float oldZoom = curZoom;
//        mTouchAction = TOUCH_ZOOM;
//        final float delta = -((y_changed / mSurfaceYDisplayRange));
//        curZoom += delta;
//        curZoom = Math.min(Math.max(curZoom, minZoom), maxZoom);
//        if (delta != 0f) {
//            setZoom(curZoom, curZoom > oldZoom);
//        }
//    }

//	private void doPageTouch(final float y_changed) {
//        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_PAGE) {
//            return;
//        }
//        final int oldZoom = ScreenSlidePagerAdapter.numOfPages;
//        mTouchAction = TOUCH_PAGE;
//        final float delta = -y_changed;
//        //Log.d(TAG, "doPageTouch " + y_changed + ", delta " + delta + ", mTouchAction " + mTouchAction + ", ScreenSlidePagerAdapter.numOfPages " + ScreenSlidePagerAdapter.numOfPages);
//        if (delta != 0) {
//            ScreenSlidePagerAdapter.numOfPages += (delta > 0 ? 1 : -1);
//			ScreenSlidePagerAdapter.numOfPages = Math.min(Math.max(ScreenSlidePagerAdapter.numOfPages, 1), 2);
//
//			final int drawableId;
//			if (ScreenSlidePagerAdapter.numOfPages == oldZoom) {
//				drawableId = R.drawable.ic_volume_mute_white_36dp;
//			} else if (ScreenSlidePagerAdapter.numOfPages > oldZoom) {
//				drawableId = R.drawable.ic_volume_up_white_36dp;
//			} else {
//				drawableId = R.drawable.ic_volume_down_white_36dp;
//			}
//			setInfo(ScreenSlidePagerAdapter.numOfPages + " pages", drawableId);
//        }
//    }

//	private void doTransformTouch(final float y_changed) {
//        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_TRANSFORM) {
//            return;
//        }
//        final float oldTransform = curTransform;
//        mTouchAction = TOUCH_TRANSFORM;
//        final float delta = y_changed;
//        //Log.d(TAG, "doTransformTouch " + y_changed + ", delta " + delta + ", mTouchAction " + mTouchAction + ", curTransorm " + curTransorm);
//        if (delta != 0) {
//            curTransform += (delta > 0 ? 1 : -1);
//			curTransform = Math.min(Math.max(curTransform, 0), 13);
//
//			final int drawableId;
//			if (curTransform == oldTransform) {
//				drawableId = R.drawable.ic_volume_mute_white_36dp;
//			} else if (curTransform > oldTransform) {
//				drawableId = R.drawable.ic_volume_up_white_36dp;
//			} else {
//				drawableId = R.drawable.ic_volume_down_white_36dp;
//			}
//			viewPager.setPageTransformer(true, ScrollGalleryView.transforms[curTransform]);
//
//			final StringBuilder sb = new StringBuilder();
//			final int length = "Transformer".length();
//			final int length2 = ScrollGalleryView.transforms.length;
//			for (int i = 0; i < length2; i++) {
//				final String simpleName = ScrollGalleryView.transforms[i].getClass().getSimpleName();
//				Log.d(TAG, "getSimpleName " + simpleName);
//				if (i != curTransform) {
//					sb.append(simpleName.substring(0, simpleName.length() - length).replaceAll("([A-Z])", " $1")).append("\n");
//				} else {
//					sb.append("———").append(simpleName.substring(0, simpleName.length() - length).replaceAll("([A-Z])", " $1")).append(" ———\n");
//				}
//			}
//			setInfo(sb.toString().trim(), drawableId);
//        }
//    }

	private void doDelayTouch(final float y_changed) {
		//Log.d(TAG, "doDelayTouch " + y_changed + ", mTouchAction " + mTouchAction + ", curDelay " + curDelay);
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_DELAY) {
            return;
        }
        final int oldDelay = curDelay;
        mTouchAction = TOUCH_DELAY;
        final int delta = -((int) ((y_changed * 100) / mSurfaceYDisplayRange)) * 1000000 / 2000;
        //Log.d(TAG, "doDelayTouch " + y_changed + ", delta " + delta + ", mTouchAction " + mTouchAction + ", curDelay " + curDelay);
        if (delta != 0) {
            curDelay += delta;
			//Log.d(TAG, "curDelay " + curDelay);
			curDelay = Math.min(Math.max(curDelay, 500), 60000);
			//Log.d(TAG, "curDelay " + curDelay);
			final int drawableId;
			if (curDelay == oldDelay) {
				drawableId = R.drawable.ic_volume_mute_white_36dp;
			} else if (curDelay > oldDelay) {
				drawableId = R.drawable.ic_volume_up_white_36dp;
			} else {
				drawableId = R.drawable.ic_volume_down_white_36dp;
			}
			//ScrollGalleryView.DELAY = curDelay;
			scrollGalleryView.resetDelay();
			//Log.d(TAG, "ScrollGalleryView.DELAY " + ScrollGalleryView.DELAY);
			setInfo("Delay " + Util.nf.format(curDelay) + " ms", drawableId);
        }
    }

//	private void setZoom(final float vol, final boolean up) {
//        int drawableId;
//        if (curZoom == 1) {
//            drawableId = R.drawable.ic_volume_mute_white_36dp;
//        } else if (up) {
//            drawableId = R.drawable.ic_volume_up_white_36dp;
//        } else {
//            drawableId = R.drawable.ic_volume_down_white_36dp;
//        }
//		image.setZoom(vol);
//        setInfo(getContext().getString(R.string.volume_changing, Math.round(vol * 100)), drawableId);
//    }

	private void doBrightnessTouch(float y_changed) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_BRIGHTNESS) {
            return;
        }
        mTouchAction = TOUCH_BRIGHTNESS;
        if (mIsFirstBrightnessGesture) {
            initBrightnessTouch();
        }
        
        // Set delta : 2f is arbitrary for now, it possibly will change in the future
        float delta = -y_changed / mSurfaceYDisplayRange;
        changeBrightness(delta);
    }

	private void initBrightnessTouch() {

        Activity activity = (Activity) getContext();
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        float brightnesstemp = lp.screenBrightness != -1f ? lp.screenBrightness : 0.6f;
        
		// Initialize the layoutParams screen brightness
        try {
            if (Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                if (!Permissions.canWriteSettings(activity)) {
                    return;
                }
                Settings.System.putInt(activity.getContentResolver(),
									   Settings.System.SCREEN_BRIGHTNESS_MODE,
									   Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
//                restoreAutoBrightness = android.provider.Settings.System.getInt(activity.getContentResolver(),
//                        android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
            } else if (brightnesstemp == 0.6f) {
                brightnesstemp = android.provider.Settings.System.getInt(activity.getContentResolver(),
																		 android.provider.Settings.System.SCREEN_BRIGHTNESS) / 255.0f;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        lp.screenBrightness = brightnesstemp;
        activity.getWindow().setAttributes(lp);
        mIsFirstBrightnessGesture = false;
    }

	private void changeBrightness(float delta) {
        // Estimate and adjust Brightness
        Activity activity = (Activity) getContext();
        Window window = activity.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
        float brightness = Math.min(Math.max(lp.screenBrightness + delta, 0.01f), 1f);
        lp.screenBrightness = brightness;
        // Set Brightness
        activity.getWindow().setAttributes(lp);
		//setWindowBrightness(brightness);
        brightness = Math.round(brightness * 100);

        int brightnessInt = (int) brightness;
        setInfo(getContext().getString(R.string.brightness_changing, brightnessInt), whichBrightnessImageToUse(brightnessInt));
    }

    private void setInfo(String txt, @DrawableRes int drawableId) {
        Log.d(TAG, "setInfo " + txt);
		centerInfo.setVisibility(View.VISIBLE);
        centerInfo.setText(txt);
        centerInfo.setTextColor(0xffffffff);
        centerInfo.setCompoundDrawablesWithIntrinsicBounds(0, drawableId, 0, 0);
    }

	@DrawableRes
    private int whichBrightnessImageToUse(int brightnessInt) {
        if (brightnessInt <= 15) {
            return R.drawable.ic_brightness_1_white_36dp;
        } else if (brightnessInt <= 30 && brightnessInt > 15) {
            return R.drawable.ic_brightness_2_white_36dp;
        } else if (brightnessInt <= 45 && brightnessInt > 30) {
            return R.drawable.ic_brightness_3_white_36dp;
        } else if (brightnessInt <= 60 && brightnessInt > 45) {
            return R.drawable.ic_brightness_4_white_36dp;
        } else if (brightnessInt <= 75 && brightnessInt > 60) {
            return R.drawable.ic_brightness_5_white_36dp;
        } else if (brightnessInt <= 90 && brightnessInt > 75) {
            return R.drawable.ic_brightness_6_white_36dp;
        } else {
            return R.drawable.ic_brightness_7_white_36dp;
        }
    }

	public void setOnDoubleTapListener(final OnDoubleTapListener onDoubleTapListener) {
		this.onDoubleTapListener = onDoubleTapListener;
	}

    public void setCallback(final Runnable callback) {
		this.zoomCallback = callback;
	}

	private void loadImageToView() {
        if (mMediaInfo != null) {
			if (VIDEO_PATTERN.matcher(mMediaInfo.getName()).matches()) {
				videoPlayImage.setVisibility(View.VISIBLE);
			} else {
				videoPlayImage.setVisibility(View.GONE);
			}
			//showWait();
            GlideImageLoader.loadMedia(Uri.fromFile(mMediaInfo), context, image, DiskCacheStrategy.NONE);//, callback);//, mimenew MediaLoader.SuccessCallback() {
//                @Override
//                public void onSuccess() {
//                    //createViewAttacher(getArguments());
//                }
//            });
			//hideWait();
        }
    }

//	final public void showWait() {
//        if (pd == null)
//            pd = ProgressDialog.show(getContext(), "", getString(R.string.loading), true, true);
//		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//    }
//
//    final public void hideWait() {
//        if (pd != null)
//            pd.cancel();
//        pd = null;
//    }

//    private void createViewAttacher(Bundle savedInstanceState) {
//        if (savedInstanceState.getBoolean(Constants.ZOOM)) {
//            photoViewAttacher = new PhotoViewAttacher(backgroundImage);
//        }
//    }

//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState) {
////        if (isViewPagerActive()) {
////            outState.putBoolean(Constants.IS_LOCKED, viewPager.isLocked());
////        }
////        if (isBackgroundImageActive()) {
////            outState.putParcelable(Constants.IMAGE, ((BitmapDrawable) backgroundImage.getDrawable()).getBitmap());
////        }
//        //outState.putBoolean(Constants.ZOOM, photoViewAttacher != null);
//        super.onSaveInstanceState(outState);
//    }

//    private boolean isViewPagerActive() {
//        return viewPager != null;
//    }
//
//    private boolean isBackgroundImageActive() {
//        return backgroundImage != null && backgroundImage.getDrawable() != null;
//    }
}
