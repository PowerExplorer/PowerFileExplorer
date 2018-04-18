///*
// * Copyright (C) 2016 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package net.gnu.explorer;
//
//import android.app.*;
//import android.content.*;
//import android.content.pm.*;
//import android.net.*;
//import android.os.*;
//import android.text.*;
//import android.util.*;
//import android.view.*;
//import android.view.View.*;
//import android.widget.*;
//import com.google.android.exoplayer2.*;
//import com.google.android.exoplayer2.drm.*;
//import com.google.android.exoplayer2.extractor.*;
//import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.*;
//import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.*;
//import com.google.android.exoplayer2.source.*;
//import com.google.android.exoplayer2.source.dash.*;
//import com.google.android.exoplayer2.source.hls.*;
//import com.google.android.exoplayer2.source.smoothstreaming.*;
//import com.google.android.exoplayer2.trackselection.*;
//import com.google.android.exoplayer2.trackselection.MappingTrackSelector.*;
//import com.google.android.exoplayer2.ui.*;
//import com.google.android.exoplayer2.upstream.*;
//import com.google.android.exoplayer2.util.*;
//import java.net.*;
//import java.util.*;
//import net.gnu.explorer.R;
//import com.google.android.exoplayer2.demo.EventLogger;
//import com.google.android.exoplayer2.demo.TrackSelectionHelper;
//import java.io.*;
//import android.support.v4.app.*;
//import android.view.ViewGroup.*;
//
///**
// * An activity that plays media using {@link SimpleExoPlayer}.
// */
//public class MediaPlayerFragment extends Frag implements OnClickListener, ExoPlayer.EventListener,
//PlaybackControlView.VisibilityListener {
//	
//	private static final String TAG = "MediaPlayerFragment";
//	
//	public static final String DRM_SCHEME_UUID_EXTRA = "drm_scheme_uuid";
//	public static final String DRM_LICENSE_URL = "drm_license_url";
//	public static final String DRM_KEY_REQUEST_PROPERTIES = "drm_key_request_properties";
//	public static final String PREFER_EXTENSION_DECODERS = "prefer_extension_decoders";
//
//	public static final String ACTION_VIEW = "com.google.android.exoplayer.demo.action.VIEW";
//	public static final String EXTENSION_EXTRA = "extension";
//
//	public static final String ACTION_VIEW_LIST =
//	"com.google.android.exoplayer.demo.action.VIEW_LIST";
//	public static final String URI_LIST_EXTRA = "uri_list";
//	public static final String EXTENSION_LIST_EXTRA = "extension_list";
//
//	private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
//	private static final CookieManager DEFAULT_COOKIE_MANAGER;
//	static {
//		DEFAULT_COOKIE_MANAGER = new CookieManager();
//		DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
//	}
//
//	private Handler mainHandler;
//	private EventLogger eventLogger;
//	private SimpleExoPlayerView simpleExoPlayerView;
//	private LinearLayout debugRootView;
//	private TextView debugTextView;
//	private Button retryButton;
//	private View rootView;
//	
//	private DataSource.Factory mediaDataSourceFactory;
//	private SimpleExoPlayer player;
//	private DefaultTrackSelector trackSelector;
//	private TrackSelectionHelper trackSelectionHelper;
//	private DebugTextViewHelper debugViewHelper;
//	private boolean needRetrySource;
//
//	private boolean shouldAutoPlay;
//	private int resumeWindow;
//	private long resumePosition;
//
//	// Activity lifecycle
//	public MediaPlayerFragment() {
//		super();
//		type = Frag.TYPE.MEDIA.ordinal();
//	}
//
//	@Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//		Log.d(TAG, "onCreate");
//        setHasOptionsMenu(false);
//    }
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		Log.d(TAG, "onCreateView");
//		
////		final Context ctx = getContext();
////		
////		rootView = new FrameLayout(ctx);
////		ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
////			ViewGroup.LayoutParams.MATCH_PARENT,
////			ViewGroup.LayoutParams.MATCH_PARENT);
////		rootView.setLayoutParams(layoutParams);
////		
////		LinearLayout mLinearLayout = new LinearLayout(ctx);
////		mLinearLayout.setId(R.id.status);
////		layoutParams = new ViewGroup.LayoutParams(
////			ViewGroup.LayoutParams.MATCH_PARENT,
////			ViewGroup.LayoutParams.MATCH_PARENT);
////		mLinearLayout.setLayoutParams(layoutParams);
////        mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
////		rootView.addView(mLinearLayout);
////		
////		View v = new View(ctx);
////		LinearLayout.LayoutParams layoutParamsSub = new LinearLayout.LayoutParams(
////			0,
////			LinearLayout.LayoutParams.WRAP_CONTENT);
////		layoutParamsSub.weight = 1;
////		v.setLayoutParams(layoutParamsSub);
////		//v.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
////		mLinearLayout.addView(v);
////		simpleExoPlayerView = new SimpleExoPlayerView(ctx);
////		simpleExoPlayerView.setLayoutParams(layoutParams);
////		mLinearLayout.addView(simpleExoPlayerView);
////		v = new View(ctx);
////		v.setLayoutParams(layoutParamsSub);
////		//v.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
////		mLinearLayout.addView(v);
////		
////		mLinearLayout = new LinearLayout(ctx);
////		layoutParams = new ViewGroup.LayoutParams(
////			ViewGroup.LayoutParams.WRAP_CONTENT,
////			ViewGroup.LayoutParams.WRAP_CONTENT);
////		mLinearLayout.setLayoutParams(layoutParams);
////        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
////		//mLinearLayout.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
////		rootView.addView(mLinearLayout);
////		
////		debugTextView = new TextView(ctx);
////		//int density = (int)(4 * getResources().getDisplayMetrics().density);
////		debugTextView.setPadding(activity.density << 2, 0, activity.density << 2, 0);
////		debugTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
////		debugTextView.setLayoutParams(layoutParams);
////		//debugTextView.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
////		mLinearLayout.addView(debugTextView);
////		
////		debugRootView = new LinearLayout(ctx);
////		debugRootView.setLayoutParams(layoutParams);
////		debugRootView.setOrientation(LinearLayout.HORIZONTAL);
////		debugRootView.setVisibility(View.GONE);
////		mLinearLayout.addView(debugRootView);
////		
////		retryButton = new Button(ctx);
////		retryButton.setText(R.string.retry);
////		retryButton.setLayoutParams(new ViewGroup.LayoutParams(
////										ViewGroup.LayoutParams.WRAP_CONTENT,
////										ViewGroup.LayoutParams.WRAP_CONTENT));
////		retryButton.setVisibility(View.GONE);
////		debugRootView.addView(retryButton);
//		
//		return inflater.inflate(R.layout.player_activity, container, false);//rootView;//
//	}
//
//
//	@Override
//	public void onViewCreated(View v, Bundle savedInstanceState) {
//	//@Override
//	//public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		shouldAutoPlay = true;
//		clearResumePosition();
//		mediaDataSourceFactory = buildDataSourceFactory(true);
//		mainHandler = new Handler();
//		if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
//			CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
//		}
//
//		//setContentView(R.layout.player_activity);
//		rootView = v.findViewById(R.id.root);
//		rootView.setOnClickListener(this);
//		debugRootView = (LinearLayout) v.findViewById(R.id.controls_root);
//		debugTextView = (TextView) v.findViewById(R.id.debug_text_view);
//		retryButton = (Button) v.findViewById(R.id.retry_button);
//		retryButton.setOnClickListener(this);
//
//		simpleExoPlayerView = (SimpleExoPlayerView) v.findViewById(R.id.player_view);
//		simpleExoPlayerView.setControllerVisibilityListener(this);
//		simpleExoPlayerView.requestFocus();
//		
//		final Intent intent = getActivity().getIntent();
//		if (intent != null) {
//			Uri extras = intent.getData();
//			if (extras != null) {
//				path = extras.getPath();
//				Log.d(TAG, "intent.getData() " + path);
//			}
//		}
//		updateColor(null);
//	}
//	
//	//@Override
//	public void onNewIntent(Intent intent) {
//		releasePlayer();
//		shouldAutoPlay = true;
//		clearResumePosition();
//		getActivity().setIntent(intent);
//	}
//	
//	public void clone(final Frag fragO) {}
//
//	public void load(String path) {
//		Log.d(TAG, "path " + path);
//		if (path != null) {
//			//doCleanUp();
//			//player.release();//.releaseMediaPlayer();
//			this.path = path;
//			File file = new File(path);
//			MediaSource mediaSource = buildMediaSource(Uri.fromFile(file), null);//extensions[i]);
//			boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
//			if (haveResumePosition) {
//				player.seekTo(resumeWindow, resumePosition);
//			}
//			player.prepare(mediaSource, !haveResumePosition, false);
//			needRetrySource = false;
//			updateButtonVisibilities();
//		}
//	}
//	
//	public void updateColor(View root) {
//		getView().setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
//		debugTextView.setTextColor(ExplorerActivity.TEXT_COLOR);
////		debugRootView.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
//		rootView.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
//		simpleExoPlayerView.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
//	}
//
//	@Override
//	public void onStart() {
//		super.onStart();
//		if (Build.VERSION.SDK_INT > 23) {
//			initializePlayer();
//			Log.d(TAG, "path " + path);
//			load(path);
//		}
//	}
//
//	@Override
//	public void onResume() {
//		super.onResume();
//		if (Build.VERSION.SDK_INT <= 23 || player == null) {
//			initializePlayer();
//			Log.d(TAG, "path " + path);
//			load(path);
//		}
//	}
//
//	@Override
//	public void onPause() {
//		super.onPause();
//		if (Build.VERSION.SDK_INT <= 23) {
//			releasePlayer();
//		}
//	}
//
//	@Override
//	public void onStop() {
//		super.onStop();
//		if (Build.VERSION.SDK_INT > 23) {
//			releasePlayer();
//		}
//	}
//
//	@Override
//	public void onRequestPermissionsResult(int requestCode, String[] permissions,
//										   int[] grantResults) {
//		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//			initializePlayer();
//		} else {
//			showToast(R.string.storage_permission_denied);
//			getActivity().finish();
//		}
//	}
//
//	// Activity input
//
//	//@Override
//	public boolean dispatchKeyEvent(KeyEvent event) {
//		// Show the controls on any key event.
//		simpleExoPlayerView.showController();
//		// If the event was not handled then see if the player view can handle it as a media key event.
//		return /*super.dispatchKeyEvent(event) || */simpleExoPlayerView.dispatchMediaKeyEvent(event);
//	}
//
//	// OnClickListener methods
//
//	@Override
//	public void onClick(View view) {
//		if (view == retryButton) {
//			initializePlayer();
//		} else if (view.getParent() == debugRootView) {
//			MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
//			if (mappedTrackInfo != null) {
//				trackSelectionHelper.showSelectionDialog(this.getActivity(), ((Button) view).getText(),
//														 trackSelector.getCurrentMappedTrackInfo(), (int) view.getTag());
//			}
//		}
//	}
//
//	// PlaybackControlView.VisibilityListener implementation
//
//	@Override
//	public void onVisibilityChange(int visibility) {
//		debugRootView.setVisibility(visibility);
//		debugTextView.setVisibility(visibility);
//	}
//
//	// Internal methods
//
//	private void initializePlayer() {
//		Intent intent = getActivity().getIntent();
//		boolean needNewPlayer = player == null;
//		if (needNewPlayer) {
//			boolean preferExtensionDecoders = intent.getBooleanExtra(PREFER_EXTENSION_DECODERS, false);
//			UUID drmSchemeUuid = intent.hasExtra(DRM_SCHEME_UUID_EXTRA)
//				? UUID.fromString(intent.getStringExtra(DRM_SCHEME_UUID_EXTRA)) : null;
//			DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
//			if (drmSchemeUuid != null) {
//				String drmLicenseUrl = intent.getStringExtra(DRM_LICENSE_URL);
//				String[] keyRequestPropertiesArray = intent.getStringArrayExtra(DRM_KEY_REQUEST_PROPERTIES);
//				try {
//					drmSessionManager = buildDrmSessionManager(drmSchemeUuid, drmLicenseUrl,
//															   keyRequestPropertiesArray);
//				} catch (UnsupportedDrmException e) {
//					int errorStringId = Util.SDK_INT < 18 ? R.string.error_drm_not_supported
//						: (e.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
//						? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown);
//					showToast(errorStringId);
//					return;
//				}
//			}
//
//			@SimpleExoPlayer.ExtensionRendererMode int extensionRendererMode =
//				((ExplorerApplication) getActivity().getApplication()).useExtensionRenderers()
//				? (preferExtensionDecoders ? SimpleExoPlayer.EXTENSION_RENDERER_MODE_PREFER
//				: SimpleExoPlayer.EXTENSION_RENDERER_MODE_ON)
//				: SimpleExoPlayer.EXTENSION_RENDERER_MODE_OFF;
//			TrackSelection.Factory videoTrackSelectionFactory =
//				new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
//			trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
//			trackSelectionHelper = new TrackSelectionHelper(trackSelector, videoTrackSelectionFactory);
//			player = ExoPlayerFactory.newSimpleInstance(this.getActivity(), trackSelector, new DefaultLoadControl(),
//														drmSessionManager, extensionRendererMode);
//			player.addListener(this);
//
//			eventLogger = new EventLogger(trackSelector);
//			player.addListener(eventLogger);
//			player.setAudioDebugListener(eventLogger);
//			player.setVideoDebugListener(eventLogger);
//			player.setMetadataOutput(eventLogger);
//
//			simpleExoPlayerView.setPlayer(player);
//			player.setPlayWhenReady(shouldAutoPlay);
//			debugViewHelper = new DebugTextViewHelper(player, debugTextView);
//			debugViewHelper.start();
//		}
//		if (needNewPlayer || needRetrySource) {
//			String action = intent.getAction();
//			Log.d(TAG, "action " + action);
//			
//			Uri[] uris;
//			String[] extensions;
//			if (ACTION_VIEW.equals(action)) {
//				uris = new Uri[] {intent.getData()};
//				extensions = new String[] {intent.getStringExtra(EXTENSION_EXTRA)};
//			} else if (Intent.ACTION_VIEW.equals(action)) {
//				uris = new Uri[] {intent.getData()};
//				extensions = new String[] {intent.getStringExtra(EXTENSION_EXTRA)};
//			} else if (ACTION_VIEW_LIST.equals(action)) {
//				String[] uriStrings = intent.getStringArrayExtra(URI_LIST_EXTRA);
//				uris = new Uri[uriStrings.length];
//				for (int i = 0; i < uriStrings.length; i++) {
//					uris[i] = Uri.parse(uriStrings[i]);
//				}
//				extensions = intent.getStringArrayExtra(EXTENSION_LIST_EXTRA);
//				if (extensions == null) {
//					extensions = new String[uriStrings.length];
//				}
//			} else {
//				if (!Intent.ACTION_MAIN.equals(action)) {
//					showToast(getString(R.string.unexpected_intent_action, action));
//				}
//				return;
//			}
//			if (Util.maybeRequestReadExternalStoragePermission(this.getActivity(), uris)) {
//				// The player will be reinitialized if the permission is granted.
//				return;
//			}
//			MediaSource[] mediaSources = new MediaSource[uris.length];
//			for (int i = 0; i < uris.length; i++) {
//				mediaSources[i] = buildMediaSource(uris[i], extensions[i]);
//			}
//			MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
//				: new ConcatenatingMediaSource(mediaSources);
//			boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
//			if (haveResumePosition) {
//				player.seekTo(resumeWindow, resumePosition);
//			}
//			player.prepare(mediaSource, !haveResumePosition, false);
//			needRetrySource = false;
//			updateButtonVisibilities();
//		}
//	}
//
//	private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
//		int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
//			: Util.inferContentType("." + overrideExtension);
//		switch (type) {
//			case C.TYPE_SS:
//				return new SsMediaSource(uri, buildDataSourceFactory(false),
//										 new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
//			case C.TYPE_DASH:
//				return new DashMediaSource(uri, buildDataSourceFactory(false),
//										   new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);
//			case C.TYPE_HLS:
//				return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);
//			case C.TYPE_OTHER:
//				return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),
//												mainHandler, eventLogger);
//			default: {
//					throw new IllegalStateException("Unsupported type: " + type);
//				}
//		}
//	}
//
//	private DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManager(UUID uuid,
//																		   String licenseUrl, String[] keyRequestPropertiesArray) throws UnsupportedDrmException {
//		if (Util.SDK_INT < 18) {
//			return null;
//		}
//		HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl,
//																	buildHttpDataSourceFactory(false));
//		if (keyRequestPropertiesArray != null) {
//			for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
//				drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i],
//												  keyRequestPropertiesArray[i + 1]);
//			}
//		}
//		return new DefaultDrmSessionManager<FrameworkMediaCrypto>(uuid,
//											  FrameworkMediaDrm.newInstance(uuid), drmCallback, null, mainHandler, eventLogger);
//	}
//
//	private void releasePlayer() {
//		if (player != null) {
//			debugViewHelper.stop();
//			debugViewHelper = null;
//			shouldAutoPlay = player.getPlayWhenReady();
//			updateResumePosition();
//			player.release();
//			player = null;
//			trackSelector = null;
//			trackSelectionHelper = null;
//			eventLogger = null;
//		}
//	}
//
//	private void updateResumePosition() {
//		resumeWindow = player.getCurrentWindowIndex();
//		resumePosition = player.isCurrentWindowSeekable() ? Math.max(0, player.getCurrentPosition())
//			: C.TIME_UNSET;
//	}
//
//	private void clearResumePosition() {
//		resumeWindow = C.INDEX_UNSET;
//		resumePosition = C.TIME_UNSET;
//	}
//
//	/**
//	 * Returns a new DataSource factory.
//	 *
//	 * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
//	 *     DataSource factory.
//	 * @return A new DataSource factory.
//	 */
//	private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
//		return ((ExplorerApplication) getActivity().getApplication())
//			.buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
//	}
//
//	/**
//	 * Returns a new HttpDataSource factory.
//	 *
//	 * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
//	 *     DataSource factory.
//	 * @return A new HttpDataSource factory.
//	 */
//	private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
//		return ((ExplorerApplication) getActivity().getApplication())
//			.buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
//	}
//
//	// ExoPlayer.EventListener implementation
//
//	@Override
//	public void onLoadingChanged(boolean isLoading) {
//		// Do nothing.
//	}
//
//	@Override
//	public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
//		if (playbackState == ExoPlayer.STATE_ENDED) {
//			showControls();
//		}
//		updateButtonVisibilities();
//	}
//
//	@Override
//	public void onPositionDiscontinuity() {
//		if (needRetrySource) {
//			// This will only occur if the user has performed a seek whilst in the error state. Update the
//			// resume position so that if the user then retries, playback will resume from the position to
//			// which they seeked.
//			updateResumePosition();
//		}
//	}
//
//	@Override
//	public void onTimelineChanged(Timeline timeline, Object manifest) {
//		// Do nothing.
//	}
//
//	@Override
//	public void onPlayerError(ExoPlaybackException e) {
//		String errorString = null;
//		if (e.type == ExoPlaybackException.TYPE_RENDERER) {
//			Exception cause = e.getRendererException();
//			if (cause instanceof DecoderInitializationException) {
//				// Special case for decoder initialization failures.
//				DecoderInitializationException decoderInitializationException =
//					(DecoderInitializationException) cause;
//				if (decoderInitializationException.decoderName == null) {
//					if (decoderInitializationException.getCause() instanceof DecoderQueryException) {
//						errorString = getString(R.string.error_querying_decoders);
//					} else if (decoderInitializationException.secureDecoderRequired) {
//						errorString = getString(R.string.error_no_secure_decoder,
//												decoderInitializationException.mimeType);
//					} else {
//						errorString = getString(R.string.error_no_decoder,
//												decoderInitializationException.mimeType);
//					}
//				} else {
//					errorString = getString(R.string.error_instantiating_decoder,
//											decoderInitializationException.decoderName);
//				}
//			}
//		}
//		if (errorString != null) {
//			showToast(errorString);
//		}
//		needRetrySource = true;
//		if (isBehindLiveWindow(e)) {
//			clearResumePosition();
//			initializePlayer();
//		} else {
//			updateResumePosition();
//			updateButtonVisibilities();
//			showControls();
//		}
//	}
//
//	@Override
//	public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
//		updateButtonVisibilities();
//		MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
//		if (mappedTrackInfo != null) {
//			if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO)
//				== MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
//				showToast(R.string.error_unsupported_video);
//			}
//			if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)
//				== MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
//				showToast(R.string.error_unsupported_audio);
//			}
//		}
//	}
//
//	// User controls
//
//	private void updateButtonVisibilities() {
//		debugRootView.removeAllViews();
//
//		retryButton.setVisibility(needRetrySource ? View.VISIBLE : View.GONE);
//		debugRootView.addView(retryButton);
//
//		if (player == null) {
//			return;
//		}
//
//		MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
//		if (mappedTrackInfo == null) {
//			return;
//		}
//
//		for (int i = 0; i < mappedTrackInfo.length; i++) {
//			TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
//			if (trackGroups.length != 0) {
//				Button button = new Button(this.getActivity());
//				int label;
//				switch (player.getRendererType(i)) {
//					case C.TRACK_TYPE_AUDIO:
//						label = R.string.audio;
//						break;
//					case C.TRACK_TYPE_VIDEO:
//						label = R.string.video;
//						break;
//					case C.TRACK_TYPE_TEXT:
//						label = R.string.text;
//						break;
//					default:
//						continue;
//				}
//				button.setText(label);
//				button.setTag(i);
//				button.setOnClickListener(this);
//				debugRootView.addView(button, debugRootView.getChildCount() - 1);
//			}
//		}
//	}
//
//	private void showControls() {
//		debugRootView.setVisibility(View.VISIBLE);
//		debugTextView.setVisibility(View.VISIBLE);
//	}
//
//	private void showToast(int messageId) {
//		showToast(getString(messageId));
//	}
//
//	private void showToast(String message) {
//		Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG).show();
//	}
//
//	private static boolean isBehindLiveWindow(ExoPlaybackException e) {
//		if (e.type != ExoPlaybackException.TYPE_SOURCE) {
//			return false;
//		}
//		Throwable cause = e.getSourceException();
//		while (cause != null) {
//			if (cause instanceof BehindLiveWindowException) {
//				return true;
//			}
//			cause = cause.getCause();
//		}
//		return false;
//	}
//
//}
