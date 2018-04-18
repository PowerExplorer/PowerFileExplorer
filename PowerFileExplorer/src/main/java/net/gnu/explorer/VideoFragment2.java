//package net.gnu.explorer;
//
//import android.app.Activity;
//import android.media.AudioManager;
//import android.media.MediaPlayer;
//import android.media.MediaPlayer.OnBufferingUpdateListener;
//import android.media.MediaPlayer.OnCompletionListener;
//import android.media.MediaPlayer.OnPreparedListener;
//import android.media.MediaPlayer.OnVideoSizeChangedListener;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.widget.Toast;
//import android.view.*;
//import android.content.*;
//import android.net.*;
//import java.io.*;
//import android.widget.*;
//import android.support.v4.app.*;
//import android.view.View.*;
//import android.os.*;
//import com.afollestad.easyvideoplayer.Util;
//
//interface EasyVideoProgressCallback {
//	void onVideoProgressUpdate(int position, int duration);
//}
//
//public class VideoFragment2 extends Frag implements
//OnBufferingUpdateListener, OnCompletionListener,
//OnPreparedListener, OnVideoSizeChangedListener, SurfaceHolder.Callback {
//
//	private static final int UPDATE_INTERVAL = 100;
//	
//	private final Runnable mUpdateCounters = new Runnable() {
//        @Override
//        public void run() {
//            if (mHandler == null || /*!mIsPrepared || */mSeeker == null || mPlayer == null)
//                return;
//            int pos = mPlayer.getCurrentPosition();
//            final int dur = mPlayer.getDuration();
//            if (pos > dur) pos = dur;
//            mLabelPosition.setText(Util.getDurationString(pos, false));
//            mLabelDuration.setText(Util.getDurationString(dur - pos, true));
//            mSeeker.setProgress(pos);
//            mSeeker.setMax(dur);
//
//            if (mProgressCallback != null)
//                mProgressCallback.onVideoProgressUpdate(pos, dur);
//            if (mHandler != null)
//                mHandler.postDelayed(this, UPDATE_INTERVAL);
//        }
//    };
//
//    private static final String TAG = "VideoFragment";
//    private int mVideoWidth;
//    private int mVideoHeight;
//    private MediaPlayer mPlayer;
//    private SurfaceView mPreview;
//    private SurfaceHolder holder;
//    //String path;
//    private Uri extras;
//    private static final int LOCAL_VIDEO = 4;
//    private static final int STREAM_VIDEO = 5;
//    private boolean mIsVideoSizeKnown = false;
//    private boolean mIsVideoReadyToBePlayed = false;
////	private LinearLayout mButtonLayout;
//	private int playbackPosition=0;
//	TextView mLabelPosition;
//	TextView mLabelDuration;
//	SeekBar mSeeker;
//	ImageButton startBtn;
//	ImageButton stopBtn;
//	private EasyVideoProgressCallback mProgressCallback;
//    private Handler mHandler;
//
//	public VideoFragment2() {
//		super();
//		type = Frag.TYPE.MEDIA;
//	}
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//							 Bundle savedInstanceState) {
//		//setRetainInstance(true);
//        Log.d(TAG, "onCreateView " + title + ", " + savedInstanceState);
//		View v = inflater.inflate(R.layout.mediaplayer_2, container, false);
//		return v;
//    }
//
//    @Override
//    public void onViewCreated(final View v, Bundle savedInstanceState) {
//        super.onViewCreated(v, savedInstanceState);
//		Bundle args = getArguments();
//		Log.d(TAG, "onViewCreated " + title + ", " + "args=" + args + ", " + savedInstanceState);
//
//		if (args != null) {
//			title = args.getString("title");
//			url = args.getString("url");
//		}
//		if (savedInstanceState != null) {
//			title = savedInstanceState.getString("title");
//			url = savedInstanceState.getString("url");
//		}
//        Intent intent = getActivity().getIntent();
//		if (intent != null) {
//			extras = intent.getData();
//			if (extras != null) {
//				url = extras.toString();
//			}
//		}
//		mPreview = (SurfaceView) v.findViewById(R.id.surface);
//        holder = mPreview.getHolder();
//        holder.addCallback(this);
//        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//
//		mLabelPosition = (TextView) v.findViewById(R.id.curPos);
//
//		mLabelDuration = (TextView) v.findViewById(R.id.duration);
//		mSeeker = (SeekBar) v.findViewById(R.id.curBar);
//		mSeeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//				public void onProgressChanged(SeekBar seekBar, int progress,
//											  boolean fromUser)
//				{
//					if (fromUser) {
//						mPlayer.seekTo(progress * 1000);
//					}
//				}
//
//				public void onStartTrackingTouch(SeekBar seekBar)
//				{
//
//				}
//				public void onStopTrackingTouch(SeekBar seekBar)
//				{
//
//				}
//			});
//		v.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
//
//
//
//        startBtn = (ImageButton) v.findViewById(R.id.start);//new Button(activity);
//		
//		startBtn.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View p1) {
//					if (mPlayer == null || !mPlayer.isPlaying()) {
//						if (playbackPosition == 0) {
//							try {
//								doCleanUp();
//								releaseMediaPlayer();
//								mPlayer = new MediaPlayer();
//								mPlayer.setDataSource(url);
//								mPlayer.setDisplay(holder);
//								mPlayer.setOnBufferingUpdateListener(VideoFragment2.this);
//								mPlayer.setOnCompletionListener(VideoFragment2.this);
//								mPlayer.setOnPreparedListener(VideoFragment2.this);
//								mPlayer.setOnVideoSizeChangedListener(VideoFragment2.this);
//								mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//								mPlayer.prepare();
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						} else {
//							mPlayer.seekTo(playbackPosition);
//						}
//						mPlayer.start();
//						startBtn.setImageResource(R.drawable.exo_controls_pause);
//						
//						//if (mCallback != null) mCallback.onStarted(this);
//						if (mHandler == null) mHandler = new Handler();
//						mHandler.post(mUpdateCounters);
//					} else {
//						playbackPosition = mPlayer.getCurrentPosition();
//						mPlayer.pause();
//						startBtn.setImageResource(R.drawable.exo_controls_play);
//						
//						
//						//mCallback.onPaused(this);
//						if (mHandler == null) return;
//						mHandler.removeCallbacks(mUpdateCounters);
//					}
//				}
//			});
//		stopBtn = (ImageButton) v.findViewById(R.id.stop);//new Button(activity);
//		
//		stopBtn.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View p1) {
//					if (mPlayer != null && (mPlayer.isPlaying() || mPlayer.getCurrentPosition() > 0)) {
//						mPlayer.stop();
//						playbackPosition = 0;
//						startBtn.setImageResource(R.drawable.exo_controls_play);
//						mSeeker.setProgress(0);
//						doCleanUp();
////						releaseMediaPlayer();
//						
//						if (mHandler == null) return;
//						mHandler.removeCallbacks(mUpdateCounters);
//					}
//				}
//			});
//
//		Log.d(TAG, "path " + url);
//	}
//
//
//	private String getTime(int duration)
//	{
//		int hour = duration / 3600000;
//		int min = (duration - (hour * 3600000)) / 60000;
//		int sec = (duration - (hour * 3600000) - min * 60000) / 1000;
//		return (hour + ":" + min + ":" + sec + "." + duration % 1000);
//	}
//
//
//	@Override
//	public void onSaveInstanceState(android.os.Bundle outState) {
//		outState.putString("url", url);
//		outState.putString("title", title);
//		Log.d(TAG, "onSaveInstanceState" + url + ", " + outState);
//		super.onSaveInstanceState(outState);
//	}
//
//    public void load(String path) {
//		Log.d(TAG, "path " + path);
//		if (path != null) {
//			doCleanUp();
//			releaseMediaPlayer();
//			this.url = path;
//			try {
//				int Media = new File(path).exists() ? LOCAL_VIDEO : STREAM_VIDEO;
//				switch (Media) {
//					case LOCAL_VIDEO:
//						if (path == "") {
//							// Tell the user to provide a media file URL.
//							Toast
//								.makeText(
//								getContext(),
//								"Please edit MediaPlayerDemo_Video Activity, "
//								+ "and set the path variable to your media file path."
//								+ " Your media file must be stored on sdcard.",
//								Toast.LENGTH_LONG).show();
//
//						}
//						break;
//					case STREAM_VIDEO:
//						/*
//						 * TODO: Set path variable to progressive streamable mp4 or
//						 * 3gpp format URL. Http protocol should be used.
//						 * Mediaplayer can only play "progressive streamable
//						 * contents" which basically means: 1. the movie atom has to
//						 * precede all the media data atoms. 2. The clip has to be
//						 * reasonably interleaved.
//						 * 
//						 */
//						if (path == "") {
//							// Tell the user to provide a media file URL.
//							Toast
//								.makeText(
//								getContext(),
//								"Please edit MediaPlayerDemo_Video Activity,"
//								+ " and set the path variable to your media file URL.",
//								Toast.LENGTH_LONG).show();
//
//						}
//						break;
//				}
//
//				// Create a new media player and set the listeners
////				mMediaPlayer = new MediaPlayer();
////				mMediaPlayer.setDataSource(path);
////				mMediaPlayer.setDisplay(holder);
////				mMediaPlayer.setOnBufferingUpdateListener(this);
////				mMediaPlayer.setOnCompletionListener(this);
////				mMediaPlayer.setOnPreparedListener(this);
////				mMediaPlayer.setOnVideoSizeChangedListener(this);
////				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//				//mMediaPlayer.prepare();
//				startBtn.performClick();
//			} catch (Exception e) {
//				Log.e(TAG, "error: " + e.getMessage(), e);
//			}
//		}
//    }
//
//    public void onBufferingUpdate(MediaPlayer arg0, int percent) {
//        Log.d(TAG, "onBufferingUpdate percent:" + percent);
//
//    }
//
//    public void onCompletion(MediaPlayer arg0) {
//        Log.d(TAG, "onCompletion called");
//    }
//
//    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
//        Log.v(TAG, "onVideoSizeChanged called");
//        if (width == 0 || height == 0) {
//            Log.e(TAG, "invalid video width(" + width + ") or height(" + height + ")");
//            return;
//        }
//        mIsVideoSizeKnown = true;
//        mVideoWidth = width;
//        mVideoHeight = height;
//        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
//            startVideoPlayback();
//        }
//    }
//
//    public void onPrepared(MediaPlayer mediaplayer) {
//        Log.d(TAG, "onPrepared called");
//        mIsVideoReadyToBePlayed = true;
//        if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
//            startVideoPlayback();
//        }
//    }
//
//    public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
//        Log.d(TAG, "surfaceChanged called");
//    }
//
//    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
//        Log.d(TAG, "surfaceDestroyed called");
//    }
//
//    public void surfaceCreated(SurfaceHolder holder) {
//        Log.d(TAG, "surfaceCreated called");
//        load(url);
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        releaseMediaPlayer();
//        doCleanUp();
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        releaseMediaPlayer();
//        doCleanUp();
//    }
//
//    private void releaseMediaPlayer() {
//        if (mPlayer != null) {
//            mPlayer.release();
//            mPlayer = null;
//        }
//		if (mHandler != null) {
//            mHandler.removeCallbacks(mUpdateCounters);
//            mHandler = null;
//        }
//
//        LOG("Released player and Handler");
//    }
//
//	private static void LOG(String message, Object... args) {
//        try {
//            if (args != null)
//                message = String.format(message, args);
//            Log.d("EasyVideoPlayer", message);
//        } catch (Exception ignored) {
//        }
//    }
//	
//    private void doCleanUp() {
//        mVideoWidth = 0;
//        mVideoHeight = 0;
//		playbackPosition = 0;
//        mIsVideoReadyToBePlayed = false;
//        mIsVideoSizeKnown = false;
//    }
//
//    private void startVideoPlayback() {
//        Log.v(TAG, "startVideoPlayback");
//        holder.setFixedSize(mVideoWidth, mVideoHeight);
//        mPlayer.start();
//    }
//}
//
