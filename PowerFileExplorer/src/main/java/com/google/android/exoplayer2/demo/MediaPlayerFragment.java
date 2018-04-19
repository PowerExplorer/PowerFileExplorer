/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.google.android.exoplayer2.demo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.UUID;
import net.gnu.explorer.Frag;
import net.gnu.explorer.R;
import java.io.File;
import net.gnu.explorer.ExplorerActivity;
import net.gnu.explorer.ExplorerApplication;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.support.annotation.DrawableRes;
import android.content.Context;
import android.app.Activity;
import android.media.AudioManager;
import java.util.Formatter;
import android.provider.Settings;
import com.jarvanmo.exoplayerview.util.Permissions;
import java.util.Locale;
import android.view.animation.AnimationUtils;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.util.TypedValue;
import android.text.style.ForegroundColorSpan;
import android.text.Spanned;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.content.IntentFilter;
import static android.content.Context.AUDIO_SERVICE;
import android.content.BroadcastReceiver;
import java.util.Calendar;
import com.amaze.filemanager.ui.LayoutElement;
import java.util.List;

/**
 * An activity that plays media using {@link SimpleExoPlayer}.
 */
public class MediaPlayerFragment extends Frag implements OnClickListener, ExoPlayer.EventListener,
PlaybackControlView.VisibilityListener {

	private final String TAG = "MediaPlayerFragment";
	
	public static final int DEFAULT_FAST_FORWARD_MS = 15000;
    public static final int DEFAULT_REWIND_MS = 5000;
    public static final int DEFAULT_SHOW_TIMEOUT_MS = 5000;

    private static final int PROGRESS_BAR_MAX = 1000;
    private static final long MAX_POSITION_FOR_SEEK_TO_PREVIOUS = 3000;

    //Touch Events
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_VOLUME = 1;
    private static final int TOUCH_BRIGHTNESS = 2;
    private static final int TOUCH_SEEK = 3;

    //touch
    private int mTouchAction = TOUCH_NONE;
    private int mSurfaceYDisplayRange;
    private float mInitTouchY;
    private float touchX = -1f;
    private float touchY = -1f;

    //Volume
    private AudioManager mAudioManager;
    private int mAudioMax;
    private float mVol;

    // Brightness
    private boolean mIsFirstBrightnessGesture = true;


//    private final Timeline.Window currentWindow;
//    private final ComponentListener componentListener;
//	private boolean isTimelineStatic;
//    private Timeline.Window window;
	

    private final StringBuilder formatBuilder;
    private final Formatter formatter;
	
	
	public static final String DRM_SCHEME_UUID_EXTRA = "drm_scheme_uuid";
	public static final String DRM_LICENSE_URL = "drm_license_url";
	public static final String DRM_KEY_REQUEST_PROPERTIES = "drm_key_request_properties";
	public static final String PREFER_EXTENSION_DECODERS = "prefer_extension_decoders";

	public static final String ACTION_VIEW = "com.google.android.exoplayer.demo.action.VIEW";
	public static final String EXTENSION_EXTRA = "extension";

	public static final String ACTION_VIEW_LIST =
	"com.google.android.exoplayer.demo.action.VIEW_LIST";
	public static final String URI_LIST_EXTRA = "uri_list";
	public static final String EXTENSION_LIST_EXTRA = "extension_list";

	private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
	private static final CookieManager DEFAULT_COOKIE_MANAGER;
	static {
		DEFAULT_COOKIE_MANAGER = new CookieManager();
		DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
	}

	private Handler mainHandler;
	private EventLogger eventLogger;
	private SimpleExoPlayerView simpleExoPlayerView;
	private LinearLayout debugRootView;
	private TextView debugTextView;
	private Button retryButton;

	private DataSource.Factory mediaDataSourceFactory;
	private SimpleExoPlayer player;
	private DefaultTrackSelector trackSelector;
	private TrackSelectionHelper trackSelectionHelper;
	private DebugTextViewHelper debugViewHelper;
	private boolean needRetrySource;

	private boolean shouldAutoPlay = true;
	private int resumeWindow;
	private long resumePosition;
	private TextView centerInfo;
	
//	private TextView localTime;
//    private BatteryLevelView battery;
    
//    private final BroadcastReceiver timeReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (action.equals(Intent.ACTION_TIME_TICK)) {
//                updateTime();
//            }
//        }
//    };
	
	public MediaPlayerFragment() {
		super();
		type = Frag.TYPE.MEDIA;
		title = "Media";
		formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
		
//		currentWindow = new Timeline.Window();
//        componentListener = new ComponentListener();
//        window = new Timeline.Window();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		super.onCreateView(inflater, container, savedInstanceState);
		setHasOptionsMenu(false);
		return inflater.inflate(R.layout.player_activity, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState) {
		//@Override
		//public void onCreate(Bundle savedInstanceState) {
		super.onViewCreated(v, savedInstanceState);
//		if (savedInstanceState != null) {
//			shouldAutoPlay = savedInstanceState.getBoolean("shouldAutoPlay");
//		}
		shouldAutoPlay = true;
		clearResumePosition();
		mediaDataSourceFactory = buildDataSourceFactory(true);
		mainHandler = new Handler();
		if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
			CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
		}

		//setContentView(R.layout.player_activity);
		View rootView = v.findViewById(R.id.root);
		rootView.setOnClickListener(this);
		debugRootView = (LinearLayout) v.findViewById(R.id.controls_root);
		debugTextView = (TextView) v.findViewById(R.id.debug_text_view);
		retryButton = (Button) v.findViewById(R.id.retry_button);
		retryButton.setOnClickListener(this);
		centerInfo = (TextView) v.findViewById(R.id.centerInfo);
//        localTime = (TextView) v.findViewById(R.id.localTime);
//        battery = (BatteryLevelView) v.findViewById(R.id.battery);
        
		simpleExoPlayerView = (SimpleExoPlayerView) v.findViewById(R.id.player_view);
		simpleExoPlayerView.setControllerVisibilityListener(this);
		simpleExoPlayerView.requestFocus();
		
		final Intent intent = getActivity().getIntent();
		if (intent != null) {
			Uri extras = intent.getData();
			if (extras != null) {
				currentPathTitle = extras.getPath();
				Log.d(TAG, "intent.getData() " + currentPathTitle);
			}
		}
		updateColor(rootView);
	}

	//@Override
	public void onNewIntent(Intent intent) {
		releasePlayer();
		shouldAutoPlay = true;
		clearResumePosition();
		fragActivity.setIntent(intent);
	}
	
	@Override
	public void clone(final Frag mediafrag, final boolean fake) {
		final MediaPlayerFragment player = (MediaPlayerFragment)mediafrag;
		currentPathTitle = player.currentPathTitle;
		needRetrySource =player.needRetrySource;
		shouldAutoPlay = player.shouldAutoPlay;
		resumeWindow = player.resumeWindow;
		resumePosition = player.resumePosition;
		mVol = player.mVol;
		mAudioMax = player.mAudioMax;
		slidingTabsFragment = player.slidingTabsFragment;
		if (debugTextView != null) {
			debugTextView.setText((player).debugTextView.getText());
		}
	}

	@Override
	public Frag clone(boolean fake) {
		final MediaPlayerFragment frag = new MediaPlayerFragment();
		frag.clone(this, fake);
		return frag;
	}

	@Override
	public void open(final int curPos, final List<LayoutElement> path) {
	}
	
	@Override
	public void load(String path) {
		Log.d(TAG, "path " + path);
		if (path != null) {
			//doCleanUp();
			//player.release();//.releaseMediaPlayer();
			this.currentPathTitle = path;
			File file = new File(path);
			MediaSource mediaSource = buildMediaSource(Uri.fromFile(file), null);//extensions[i]);
			boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
			if (haveResumePosition) {
				player.seekTo(resumeWindow, resumePosition);
			}
			player.prepare(mediaSource, !haveResumePosition, false);
			needRetrySource = false;
			updateButtonVisibilities();
		}
	}

	public void updateColor(View rootView) {
		if (activity != null) {
			debugTextView.setTextColor(ExplorerActivity.TEXT_COLOR);
			debugTextView.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
			debugRootView.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
			rootView.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
			simpleExoPlayerView.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
		} else {
			debugTextView.setTextColor(0xffffffff);
			debugTextView.setBackgroundColor(0xff808080);
			debugRootView.setBackgroundColor(0xff808080);
			rootView.setBackgroundColor(0xff808080);
			simpleExoPlayerView.setBackgroundColor(0xff808080);
		}
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//Log.d(TAG, "onTouch " + event);
		super.onTouch(v, event);
		dispatchCenterWrapperTouchEvent(event);
		return false;
	}
	
	private boolean dispatchCenterWrapperTouchEvent(MotionEvent event) {
		Log.d(TAG, "dispatchCenterWrapperTouchEvent " + event);

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics screen = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(screen);

        if (mSurfaceYDisplayRange == 0) {
            mSurfaceYDisplayRange = Math.min(screen.widthPixels, screen.heightPixels);
        }

        float x_changed, y_changed;
        if (touchX != -1f && touchY != -1f) {
            y_changed = event.getRawY() - touchY;
            x_changed = event.getRawX() - touchX;
        } else {
            x_changed = 0f;
            y_changed = 0f;
        }

//        Log.e("tag","x_c=" + x_changed + "screen_x =" + screen.xdpi +" screen_rawx" + event.getRawX());
        float coef = Math.abs(y_changed / x_changed);
        float xgesturesize = (((event.getRawX() - touchX) / screen.xdpi) * 2.54f);//2.54f
        float delta_y = Math.max(1f, (Math.abs(mInitTouchY - event.getRawY()) / screen.xdpi + 0.5f) * 2f);

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mTouchAction = TOUCH_NONE;
                touchX = event.getRawX();
                mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                touchY = mInitTouchY = event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:

                if (mTouchAction != TOUCH_SEEK && coef > 2) {
                    if (Math.abs(y_changed / mSurfaceYDisplayRange) < 0.05) {
                        return false;
                    }

                    touchX = event.getRawX();
                    touchY = event.getRawY();

					if (activity == null) {
						if ((int) touchX > (4 * screen.widthPixels / 7)) {
							doVolumeTouch(y_changed);
//                        hideCenterInfo();
//                            hideOverlay(true);
						}
						// Brightness (Up or Down - Left side)
						if ((int) touchX < (3 * screen.widthPixels / 7)) {
							doBrightnessTouch(y_changed);
						}
					} else {
						if (!activity.swap && activity.left.getVisibility() == View.VISIBLE && activity.right.getVisibility() == View.VISIBLE) {
							if ((int) touchX > (3 * screen.widthPixels / 4)) {
								doVolumeTouch(y_changed);
//                        hideCenterInfo();
//                            hideOverlay(true);
							}
							// Brightness (Up or Down - Left side)
							else if ((int) touchX >= (2 * screen.widthPixels / 4)) {
								doBrightnessTouch(y_changed);
							}
						} else {
							if ((int) touchX < (1 * screen.widthPixels / 4)) {
								doBrightnessTouch(y_changed);
//                        hideCenterInfo();
//                            hideOverlay(true);
							}
							// Brightness (Up or Down - Left side)
							else if ((int) touchX < (2 * screen.widthPixels / 4)) {
								doVolumeTouch(y_changed);
							}
						}
					}
                } else {
                    doSeekTouch(Math.round(delta_y), xgesturesize, false);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchAction == TOUCH_SEEK) {
                    doSeekTouch(Math.round(delta_y), xgesturesize, true);
                }
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

//    private void updateTime() {
//        final Calendar calendar = Calendar.getInstance();
//
//        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
//        int minute = calendar.get(Calendar.MINUTE);
////        int amOrPm = calendar.get(Calendar.AM_PM);
////        boolean is24HourFormat = DateFormat.is24HourFormat(getContext());
//
////        Resources res = getResources();
//        String timeResult = "";
////        hourOfDay = is24HourFormat ? hourOfDay : (hourOfDay > 12 ? hourOfDay - 12: hourOfDay);
//        if (hourOfDay >= 10) {
//            timeResult += Integer.toString(hourOfDay);
//        } else {
//            timeResult += "0" + hourOfDay;
//        }
//
//        timeResult += ":";
//
//        if (minute >= 10) {
//            timeResult += Integer.toString(minute);
//        } else {
//            timeResult += "0" + minute;
//        }
//
//
////
////        if (!is24HourFormat) {
////            String str = amOrPm == Calendar.AM ? res.getString(R.string.time_am) : res.getString(R.string.time_pm);
////            timeResult = timeResult + " " + str;
////        }
//        localTime.setText(timeResult);
//    }
	
//    @Override
//    public void onAttachedToWindow() {
//        super.onAttachedToWindow();
//
//        isAttachedToWindow = true;
//
//        registerBroadcast();
//
//        initVol();
//
//        if (hideAtMs != C.TIME_UNSET) {
//            long delayMs = hideAtMs - SystemClock.uptimeMillis();
//            if (delayMs <= 0) {
//                hide();
//            } else {
//                postDelayed(hideAction, delayMs);
//            }
//        }
//        updateAll();
//    }


//    @Override
//    public void onDetachedFromWindow() {
//        super.onDetachedFromWindow();
//        isAttachedToWindow = false;
//
//        mAudioManager = null;
//
//        screenOrientationEventListener.disable();
//
//        unregisterBroadcast();
//        removeCallbacks(updateProgressAction);
//        removeCallbacks(hideAction);
//    }
	
    private void initVol() {
		/* Services and miscellaneous */
        mAudioManager = (AudioManager) getContext().getApplicationContext().getSystemService(AUDIO_SERVICE);
        mAudioMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

//    private void registerBroadcast() {
//
//        IntentFilter timeFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
//        getContext().registerReceiver(timeReceiver, timeFilter);
//
//    }
//
//    private void unregisterBroadcast() {
//        getContext().unregisterReceiver(timeReceiver);
//    }
	
    private void hideCenterInfo() {
        centerInfo.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
        centerInfo.setVisibility(View.GONE);
    }
	
    private void doVolumeTouch(float y_changed) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_VOLUME) {
            return;
        }

        int oldVol = (int) mVol;
        mTouchAction = TOUCH_VOLUME;
        float delta = -((y_changed / mSurfaceYDisplayRange) * mAudioMax);
        mVol += delta;
        int vol = (int) Math.min(Math.max(mVol, 0), mAudioMax);
        if (delta != 0f) {
            setAudioVolume(vol, vol > oldVol);
        }
    }

    private void setAudioVolume(int vol, boolean up) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);

        /* Since android 4.3, the safe volume warning dialog is displayed only with the FLAG_SHOW_UI flag.
         * We don't want to always show the default UI volume, so show it only when volume is not set. */
        int newVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (vol != newVol) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_SHOW_UI);
        }

        mTouchAction = TOUCH_VOLUME;
        vol = vol * 100 / mAudioMax;
        int drawableId;
        if (newVol == 0) {
            drawableId = R.drawable.ic_volume_mute_white_36dp;
        } else if (up) {
            drawableId = R.drawable.ic_volume_up_white_36dp;
        } else {
            drawableId = R.drawable.ic_volume_down_white_36dp;
        }
        setVolumeOrBrightnessInfo(getContext().getString(R.string.volume_changing, vol), drawableId);
//        showInfoWithVerticalBar(getString(R.string.volume) + "\n" + Integer.toString(vol) + '%', 1000, vol);
    }
	
    private void doSeekTouch(int coef, float gesturesize, boolean seek) {
        if (coef == 0) {
            coef = 1;
        }
        // No seek action if coef > 0.5 and gesturesize < 1cm
        if (Math.abs(gesturesize) < 1) {// || !canSeek()
            return;
        }
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_SEEK) {
            return;
        }
        mTouchAction = TOUCH_SEEK;

        long length = player.getDuration();
        long time = player.getCurrentPosition();

        // Size of the jump, 10 minutes max (600000), with a bi-cubic progression, for a 8cm gesture
        int jump = (int) ((Math.signum(gesturesize) * ((600000 * Math.pow((gesturesize / 8), 4)) + 3000)) / coef);

        // Adjust the jump
        if ((jump > 0) && ((time + jump) > length)) {
            jump = (int) (length - time);
        }
        if ((jump < 0) && ((time + jump) < 0)) {
            jump = (int) -time;
        }
        //Jump !
        if (seek && length > 0) {
            seek(time + jump);
        }

        if (length > 0) {
            //Show the jump's size
            setFastForwardOrRewind(time + jump, jump > 0 ? R.drawable.ic_fast_forward_white_36dp : R.drawable.ic_fast_rewind_white_36dp);
        }
    }

    private void seek(long position) {
        if (player != null) {
            player.seekTo(position);
        }
    }
	
    private void setFastForwardOrRewind(long changingTime, @DrawableRes int drawableId) {
        centerInfo.setVisibility(View.VISIBLE);
        centerInfo.setText(generateFastForwardOrRewindTxt(changingTime));
        centerInfo.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), drawableId), null, null);
    }

    private CharSequence generateFastForwardOrRewindTxt(long changingTime) {

        long duration = player == null ? 0 : player.getDuration();
        String result = stringForTime(changingTime) + " / " + stringForTime(duration);

        int index = result.indexOf("/");
        SpannableString spannableString = new SpannableString(result);

        TypedValue typedValue = new TypedValue();
        TypedArray a = getContext().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorAccent});
        int color = a.getColor(0, 0);
        a.recycle();
        spannableString.setSpan(new ForegroundColorSpan(color), 0, index, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        return spannableString;
    }

    private String stringForTime(long timeMs) {
        if (timeMs == C.TIME_UNSET) {
            timeMs = 0;
        }
        long totalSeconds = (timeMs + 500) / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        formatBuilder.setLength(0);
        return hours > 0 ? formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
			: formatter.format("%02d:%02d", minutes, seconds).toString();
    }
	
//    private boolean canSeek() {
//
//        Timeline currentTimeline = player != null ? player.getCurrentTimeline() : null;
//        boolean haveNonEmptyTimeline = currentTimeline != null && !currentTimeline.isEmpty();
//        boolean isSeekable = false;
//        if (haveNonEmptyTimeline) {
//            int currentWindowIndex = player.getCurrentWindowIndex();
//            currentTimeline.getWindow(currentWindowIndex, currentWindow);
//            isSeekable = currentWindow.isSeekable;
////            enablePrevious = currentWindowIndex > 0 || isSeekable || !currentWindow.isDynamic;
//        }
//
//        return isSeekable && isTimelineStatic;
//    }
	
	private void doBrightnessTouch(float y_changed) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_BRIGHTNESS) {
            return;
        }

        mTouchAction = TOUCH_BRIGHTNESS;
        if (mIsFirstBrightnessGesture) {
            initBrightnessTouch();
        }

        mTouchAction = TOUCH_BRIGHTNESS;
//
        // Set delta : 2f is arbitrary for now, it possibly will change in the future
        float delta = -y_changed / mSurfaceYDisplayRange;
        changeBrightness(delta);
    }


    private void initBrightnessTouch() {

//        if (!(getContext() instanceof Activity)) {
//            return;
//        }
        Activity activity = fragActivity;//(Activity) getContext();

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
//        if (!(getContext() instanceof Activity)) {
//            return;
//        }
        Activity activity = fragActivity;//(Activity) getContext();
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        float brightness = Math.min(Math.max(lp.screenBrightness + delta, 0.01f), 1f);
        setWindowBrightness(brightness);
        brightness = Math.round(brightness * 100);

        int brightnessInt = (int) brightness;

        setVolumeOrBrightnessInfo(getContext().getString(R.string.brightness_changing, brightnessInt), whichBrightnessImageToUse(brightnessInt));
    }

    private void setWindowBrightness(float brightness) {
//        if (!(getContext() instanceof Activity)) {
//            return;
//        }
        Activity activity = fragActivity;//(Activity) getContext();
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = brightness;
        // Set Brightness
        activity.getWindow().setAttributes(lp);
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
	
    private void setVolumeOrBrightnessInfo(String txt, @DrawableRes int drawableId) {
        centerInfo.setVisibility(View.VISIBLE);
        centerInfo.setText(txt);
        centerInfo.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
        centerInfo.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(getContext(), drawableId), null, null);
    }

	
	@Override
	public void onStart() {
		super.onStart();
		if (Util.SDK_INT > 23) {
			initializePlayer();
			Log.d(TAG, "path " + currentPathTitle);
			load(currentPathTitle);
			initVol();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if ((Util.SDK_INT <= 23 || player == null)) {
			initializePlayer();
			Log.d(TAG, "path " + currentPathTitle);
			load(currentPathTitle);
			initVol();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (Util.SDK_INT <= 23) {
			releasePlayer();
			mAudioManager = null;
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (Util.SDK_INT > 23) {
			releasePlayer();
			mAudioManager = null;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions,
										   int[] grantResults) {
		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			initializePlayer();
		} else {
			showToast(R.string.storage_permission_denied);
			fragActivity.finish();
		}
	}

	// Activity input

	//@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// Show the controls on any key event.
		//simpleExoPlayerView.showController();
		// If the event was not handled then see if the player view can handle it as a media key event.
		return /*super.dispatchKeyEvent(event) || */simpleExoPlayerView.dispatchKeyEvent(event);//dispatchMediaKeyEvent
	}

	// OnClickListener methods

	@Override
	public void onClick(View view) {
		if (view == retryButton) {
			initializePlayer();
		} else if (view.getParent() == debugRootView) {
			MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
			if (mappedTrackInfo != null) {
				trackSelectionHelper.showSelectionDialog(fragActivity, ((Button) view).getText(),
														 trackSelector.getCurrentMappedTrackInfo(), (int) view.getTag());
			}
		}
	}

	// PlaybackControlView.VisibilityListener implementation

	@Override
	public void onVisibilityChange(int visibility) {
		debugRootView.setVisibility(visibility);
		debugTextView.setVisibility(visibility);
	}

	// Internal methods

	private void initializePlayer() {
		Intent intent = fragActivity.getIntent();
		boolean needNewPlayer = player == null;
		if (needNewPlayer) {
			boolean preferExtensionDecoders = intent.getBooleanExtra(PREFER_EXTENSION_DECODERS, false);
			UUID drmSchemeUuid = intent.hasExtra(DRM_SCHEME_UUID_EXTRA)
				? UUID.fromString(intent.getStringExtra(DRM_SCHEME_UUID_EXTRA)) : null;
			DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
			if (drmSchemeUuid != null) {
				String drmLicenseUrl = intent.getStringExtra(DRM_LICENSE_URL);
				String[] keyRequestPropertiesArray = intent.getStringArrayExtra(DRM_KEY_REQUEST_PROPERTIES);
				try {
					drmSessionManager = buildDrmSessionManager(drmSchemeUuid, drmLicenseUrl,
															   keyRequestPropertiesArray);
				} catch (UnsupportedDrmException e) {
					int errorStringId = Util.SDK_INT < 18 ? R.string.error_drm_not_supported
						: (e.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
						? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown);
					showToast(errorStringId);
					return;
				}
			}

			@SimpleExoPlayer.ExtensionRendererMode int extensionRendererMode =
				((ExplorerApplication) fragActivity.getApplication()).useExtensionRenderers()
				? (preferExtensionDecoders ? SimpleExoPlayer.EXTENSION_RENDERER_MODE_PREFER
				: SimpleExoPlayer.EXTENSION_RENDERER_MODE_ON)
				: SimpleExoPlayer.EXTENSION_RENDERER_MODE_OFF;
			TrackSelection.Factory videoTrackSelectionFactory =
				new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
			trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
			trackSelectionHelper = new TrackSelectionHelper(trackSelector, videoTrackSelectionFactory);
			player = ExoPlayerFactory.newSimpleInstance(fragActivity, trackSelector, new DefaultLoadControl(),
														drmSessionManager, extensionRendererMode);
			player.addListener(this);

			eventLogger = new EventLogger(trackSelector);
			player.addListener(eventLogger);
			player.setAudioDebugListener(eventLogger);
			player.setVideoDebugListener(eventLogger);
			player.setMetadataOutput(eventLogger);

			simpleExoPlayerView.setPlayer(player);
			player.setPlayWhenReady(shouldAutoPlay);
			debugViewHelper = new DebugTextViewHelper(player, debugTextView);
			debugViewHelper.start();
		}
		if (needNewPlayer || needRetrySource) {
			String action = intent.getAction();
			Uri[] uris;
			String[] extensions;
			if (Intent.ACTION_VIEW.equals(action)) {
				uris = new Uri[] {intent.getData()};
				extensions = new String[] {intent.getStringExtra(EXTENSION_EXTRA)};
			} else if (ACTION_VIEW.equals(action)) {
				uris = new Uri[] {intent.getData()};
				extensions = new String[] {intent.getStringExtra(EXTENSION_EXTRA)};
			} else if (ACTION_VIEW_LIST.equals(action)) {
				String[] uriStrings = intent.getStringArrayExtra(URI_LIST_EXTRA);
				uris = new Uri[uriStrings.length];
				for (int i = 0; i < uriStrings.length; i++) {
					uris[i] = Uri.parse(uriStrings[i]);
				}
				extensions = intent.getStringArrayExtra(EXTENSION_LIST_EXTRA);
				if (extensions == null) {
					extensions = new String[uriStrings.length];
				}
			} else {
				if (!Intent.ACTION_MAIN.equals(action)) {
					showToast(getString(R.string.unexpected_intent_action, action));
				}
				return;
			}
			if (Util.maybeRequestReadExternalStoragePermission(fragActivity, uris)) {
				// The player will be reinitialized if the permission is granted.
				return;
			}
			MediaSource[] mediaSources = new MediaSource[uris.length];
			for (int i = 0; i < uris.length; i++) {
				mediaSources[i] = buildMediaSource(uris[i], extensions[i]);
			}
			MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
				: new ConcatenatingMediaSource(mediaSources);
			boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
			if (haveResumePosition) {
				player.seekTo(resumeWindow, resumePosition);
			}
			player.prepare(mediaSource, !haveResumePosition, false);
			needRetrySource = false;
			updateButtonVisibilities();
		}
	}

	private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
		int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
			: Util.inferContentType("." + overrideExtension);
		switch (type) {
			case C.TYPE_SS:
				return new SsMediaSource(uri, buildDataSourceFactory(false),
										 new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
			case C.TYPE_DASH:
				return new DashMediaSource(uri, buildDataSourceFactory(false),
										   new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
			case C.TYPE_HLS:
				return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
			case C.TYPE_OTHER:
				return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
												mainHandler, eventLogger);
			default: {
					throw new IllegalStateException("Unsupported type: " + type);
				}
		}
	}

	private DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManager(UUID uuid,
																		   String licenseUrl, String[] keyRequestPropertiesArray) throws UnsupportedDrmException {
		if (Util.SDK_INT < 18) {
			return null;
		}
		HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl,
																	buildHttpDataSourceFactory(false));
		if (keyRequestPropertiesArray != null) {
			for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
				drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i],
												  keyRequestPropertiesArray[i + 1]);
			}
		}
		return new DefaultDrmSessionManager<FrameworkMediaCrypto>(uuid,
											  FrameworkMediaDrm.newInstance(uuid), drmCallback, null, mainHandler, eventLogger);
	}

	private void releasePlayer() {
		if (player != null) {
			debugViewHelper.stop();
			debugViewHelper = null;
			shouldAutoPlay = player.getPlayWhenReady();
			updateResumePosition();
			player.release();
			player = null;
			trackSelector = null;
			trackSelectionHelper = null;
			eventLogger = null;
		}
	}

	private void updateResumePosition() {
		resumeWindow = player.getCurrentWindowIndex();
		resumePosition = player.isCurrentWindowSeekable() ? Math.max(0, player.getCurrentPosition())
			: C.TIME_UNSET;
	}

	private void clearResumePosition() {
		resumeWindow = C.INDEX_UNSET;
		resumePosition = C.TIME_UNSET;
	}

	/**
	 * Returns a new DataSource factory.
	 *
	 * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
	 *     DataSource factory.
	 * @return A new DataSource factory.
	 */
	private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
		return ((ExplorerApplication) fragActivity.getApplication())
			.buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
	}

	/**
	 * Returns a new HttpDataSource factory.
	 *
	 * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
	 *     DataSource factory.
	 * @return A new HttpDataSource factory.
	 */
	private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
		return ((ExplorerApplication)fragActivity.getApplication())
			.buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
	}

	// ExoPlayer.EventListener implementation

	@Override
	public void onLoadingChanged(boolean isLoading) {
		// Do nothing.
	}

	@Override
	public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
		if (playbackState == ExoPlayer.STATE_ENDED) {
			showControls();
		}
		updateButtonVisibilities();
	}

	@Override
	public void onPositionDiscontinuity() {
		if (needRetrySource) {
			// This will only occur if the user has performed a seek whilst in the error state. Update the
			// resume position so that if the user then retries, playback will resume from the position to
			// which they seeked.
			updateResumePosition();
		}
	}

	@Override
	public void onTimelineChanged(Timeline timeline, Object manifest) {
		// Do nothing.
	}

	@Override
	public void onPlayerError(ExoPlaybackException e) {
		String errorString = null;
		if (e.type == ExoPlaybackException.TYPE_RENDERER) {
			Exception cause = e.getRendererException();
			if (cause instanceof DecoderInitializationException) {
				// Special case for decoder initialization failures.
				DecoderInitializationException decoderInitializationException =
					(DecoderInitializationException) cause;
				if (decoderInitializationException.decoderName == null) {
					if (decoderInitializationException.getCause() instanceof DecoderQueryException) {
						errorString = getString(R.string.error_querying_decoders);
					} else if (decoderInitializationException.secureDecoderRequired) {
						errorString = getString(R.string.error_no_secure_decoder,
												decoderInitializationException.mimeType);
					} else {
						errorString = getString(R.string.error_no_decoder,
												decoderInitializationException.mimeType);
					}
				} else {
					errorString = getString(R.string.error_instantiating_decoder,
											decoderInitializationException.decoderName);
				}
			}
		}
		if (errorString != null) {
			showToast(errorString);
		}
		needRetrySource = true;
		if (isBehindLiveWindow(e)) {
			clearResumePosition();
			initializePlayer();
		} else {
			updateResumePosition();
			updateButtonVisibilities();
			showControls();
		}
	}

	@Override
	public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
		updateButtonVisibilities();
		MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
		if (mappedTrackInfo != null) {
			if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO)
				== MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
				showToast(R.string.error_unsupported_video);
			}
			if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)
				== MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
				showToast(R.string.error_unsupported_audio);
			}
		}
	}

	// User controls

	private void updateButtonVisibilities() {
		debugRootView.removeAllViews();

		retryButton.setVisibility(needRetrySource ? View.VISIBLE : View.GONE);
		debugRootView.addView(retryButton);

		if (player == null) {
			return;
		}

		MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
		if (mappedTrackInfo == null) {
			return;
		}

		for (int i = 0; i < mappedTrackInfo.length; i++) {
			TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
			if (trackGroups.length != 0) {
				Button button = new Button(fragActivity);
				int label;
				switch (player.getRendererType(i)) {
					case C.TRACK_TYPE_AUDIO:
						label = R.string.audio;
						break;
					case C.TRACK_TYPE_VIDEO:
						label = R.string.video;
						break;
					case C.TRACK_TYPE_TEXT:
						label = R.string.text;
						break;
					default:
						continue;
				}
				button.setText(label);
				button.setTag(i);
				button.setOnClickListener(this);
				debugRootView.addView(button, debugRootView.getChildCount() - 1);
			}
		}
	}

	private void showControls() {
		debugRootView.setVisibility(View.VISIBLE);
	}

	private void showToast(int messageId) {
		showToast(getString(messageId));
	}

	private static boolean isBehindLiveWindow(ExoPlaybackException e) {
		if (e.type != ExoPlaybackException.TYPE_SOURCE) {
			return false;
		}
		Throwable cause = e.getSourceException();
		while (cause != null) {
			if (cause instanceof BehindLiveWindowException) {
				return true;
			}
			cause = cause.getCause();
		}
		return false;
	}

}
