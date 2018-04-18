//package net.gnu.explorer;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.res.AssetFileDescriptor;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Rect;
//import android.media.AudioManager;
//import android.media.MediaPlayer;
//import android.media.audiofx.Equalizer;
//import android.media.audiofx.Visualizer;
//import android.net.Uri;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.LinearLayout;
//import android.widget.SeekBar;
//import android.widget.TextView;
//
//import java.io.IOException;
//import java.io.*;
//import android.view.*;
//import android.support.v4.app.*;
//import android.content.*;
//import android.widget.*;
//import android.view.View.*;
//import android.os.*;
//import com.afollestad.easyvideoplayer.*;
//import android.view.ViewGroup.*;
//
//public class AudioFragment extends Frag {
//    
//    private static final float VISUALIZER_HEIGHT_DIP = 50f;
//
//    private MediaPlayer mPlayer;
//    private Visualizer mVisualizer;
//    private Equalizer mEqualizer;
//
//    private LinearLayout mLinearLayout;
//    private VisualizerView mVisualizerView;
//    private TextView mStatusTextView;
//	private LinearLayout mButtonLayout;
//	private int playbackPosition=0;
//	
//	private EasyVideoProgressCallback mProgressCallback;
//    private Handler mHandler;
//	private TextView mLabelPosition;
//	private TextView mLabelDuration;
//	private SeekBar mSeeker;
//	private final ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
//		ViewGroup.LayoutParams.FILL_PARENT,
//		ViewGroup.LayoutParams.WRAP_CONTENT);
//	private final ViewGroup.LayoutParams layoutParamsW = new ViewGroup.LayoutParams(
//		ViewGroup.LayoutParams.WRAP_CONTENT,
//		ViewGroup.LayoutParams.WRAP_CONTENT);
//	public AudioFragment() {
//		super();
//		type = Frag.TYPE.MEDIA;
//		TAG = "AudioFragment";
//	}
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
//	@Override
//	public void onCreate(Bundle savedInstanceState)
//	{
//		//setRetainInstance(true);
//		super.onCreate(savedInstanceState);
//	}
//	
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//							 Bundle savedInstanceState) {
//		
//        Log.d(TAG, "onCreateView " + title + ", " + savedInstanceState);
//		
//		FragmentActivity activity = getActivity();
//		activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
//		
//        mStatusTextView = new TextView(activity);
//		mStatusTextView.setSingleLine(true);
//		
//        mLinearLayout = new LinearLayout(activity);
//		ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
//			ViewGroup.LayoutParams.MATCH_PARENT,
//			ViewGroup.LayoutParams.MATCH_PARENT);
//		mLinearLayout.setLayoutParams(layoutParams);
//        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
//        mLinearLayout.addView(mStatusTextView);
//		mLinearLayout.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
//		
//		ScrollView sv = new ScrollView(activity);
//		sv.setLayoutParams(layoutParams);
//		sv.addView(mLinearLayout);
//		return sv;//mLinearLayout;
//    }
//
//    @Override
//    public void onViewCreated(View v, Bundle savedInstanceState) {
//        super.onViewCreated(v, savedInstanceState);
//		Bundle args = getArguments();
//		Log.d(TAG, "onViewCreated " + title + ", " + "args=" + args + ", " + savedInstanceState);
//		v.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
//		if (args != null) {
//			url = args.getString("url");
//			title = args.getString("title");
//		}
//		if (savedInstanceState != null) {
//			title = savedInstanceState.getString("title");
//			url = savedInstanceState.getString("url");
//		}
//		Intent intent = getActivity().getIntent();
//		if (intent != null) {
//			Uri extras = intent.getData();
//			if (extras != null) {
//				url = extras.toString();
//			}
//		}
//		Log.d(TAG, "onViewCreated.path " + url);
//        
//		load(url);
//		
//    }
//
//	public void load(final String fPath) throws IllegalStateException {
//		this.url = fPath;
//		Log.d(TAG, "play.url " + url);
//        
//		if (fPath != null) {
//			release();
//			File file = new File(fPath);
//			mPlayer = MediaPlayer.create(getContext(), Uri.fromFile(file));//R.raw.test_cbr);
//			Log.d(TAG, "MediaPlayer audio session ID: " + mPlayer.getAudioSessionId());
//			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//			setupVisualizerFxAndUI();
//			setupEqualizerFxAndUI();
//			
//			// Make sure the visualizer is enabled only when you actually want to receive data, and
//			// when it makes sense to receive data.
//			mVisualizer.setEnabled(true);
//
//			// When the stream ends, we don't need to collect any more data. We don't do this in
//			// setupVisualizerFxAndUI because we likely want to have more, non-Visualizer related code
//			// in this callback.
//			mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//					public void onCompletion(MediaPlayer mediaPlayer)
//					{
//						mVisualizer.setEnabled(false);
//					}
//				});
//
//			//mPlayer.start();
//			startBtn.performClick();
//			mStatusTextView.setText("Playing " + file.getName() + "...");
//		} else {
//			mStatusTextView.setText("No file to play...");
//			//Toast.makeText(getActivity(), "File is null", Toast.LENGTH_SHORT).show();
//		}
//	}
//
//	@Override
//	public void onSaveInstanceState(android.os.Bundle outState) {
//		outState.putString("url", url);
//		outState.putString("title", title);
//		Log.d(TAG, "onSaveInstanceState" + url + ", " + outState);
//		super.onSaveInstanceState(outState);
//	}
//	
//    private void setupEqualizerFxAndUI() {
//        // Create the Equalizer object (an AudioEffect subclass) and attach it to our media player,
//        // with a default priority (0).
//        mEqualizer = new Equalizer(0, mPlayer.getAudioSessionId());
//        mEqualizer.setEnabled(true);
//
//        Context activity = getContext();
//		TextView eqTextView = new TextView(activity);
//        eqTextView.setText("Equalizer:");
//        mLinearLayout.addView(eqTextView);
//		
//        short bands = mEqualizer.getNumberOfBands();
//
//        final short minEQLevel = mEqualizer.getBandLevelRange()[0];
//        final short maxEQLevel = mEqualizer.getBandLevelRange()[1];
//
//        for (short i = 0; i < bands; i++) {
//            final short band = i;
//
//            TextView freqTextView = new TextView(activity);
//            freqTextView.setLayoutParams(layoutParams);
//            freqTextView.setGravity(Gravity.CENTER_HORIZONTAL);
//            freqTextView.setText((mEqualizer.getCenterFreq(band) / 1000) + " Hz");
//            mLinearLayout.addView(freqTextView);
//
//            LinearLayout row = new LinearLayout(activity);
//            row.setOrientation(LinearLayout.HORIZONTAL);
//
//            TextView minEQLabel = new TextView(activity);
//            
//			minEQLabel.setLayoutParams(layoutParamsW);
//            minEQLabel.setText((minEQLevel / 100) + " dB");
//
//            TextView maxEQLabel = new TextView(activity);
//            maxEQLabel.setLayoutParams(layoutParamsW);
//            maxEQLabel.setText((maxEQLevel / 100) + " dB");
//
//			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
//				ViewGroup.LayoutParams.FILL_PARENT,
//				ViewGroup.LayoutParams.WRAP_CONTENT);
//            layoutParams.weight = 1;
//            SeekBar mSeeker = new SeekBar(activity);
//            mSeeker.setLayoutParams(layoutParams);
//            mSeeker.setMax(maxEQLevel - minEQLevel);
//            mSeeker.setProgress(mEqualizer.getBandLevel(band));
//
//            mSeeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//					public void onProgressChanged(SeekBar seekBar, int progress,
//												  boolean fromUser) {
//						mEqualizer.setBandLevel(band, (short) (progress + minEQLevel));
//					}
//
//					public void onStartTrackingTouch(SeekBar seekBar) {}
//					public void onStopTrackingTouch(SeekBar seekBar) {}
//				});
//
//            row.addView(minEQLabel);
//            row.addView(mSeeker);
//            row.addView(maxEQLabel);
//
//            mLinearLayout.addView(row);
//        }
//    }
//
//	private ImageButton startBtn;
//	//private ImageButton stopBtn;
//    private void setupVisualizerFxAndUI() {
//        // Create a VisualizerView (defined below), which will render the simplified audio
//        // wave form to a Canvas.
//        mVisualizerView = new VisualizerView(getContext());
//        mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
//											ViewGroup.LayoutParams.FILL_PARENT,
//											(int)(VISUALIZER_HEIGHT_DIP * getResources().getDisplayMetrics().density)));
//        mLinearLayout.addView(mVisualizerView);
//
//        // Create the Visualizer object and attach it to our media player.
//        mVisualizer = new Visualizer(mPlayer.getAudioSessionId());
//        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
//        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
//				public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
//												  int samplingRate) {
//					mVisualizerView.updateVisualizer(bytes);
//				}
//
//				public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {}
//			}, Visualizer.getMaxCaptureRate() / 2, true, false);
//
//		Context activity = getContext();
//		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
//			ViewGroup.LayoutParams.FILL_PARENT,
//			ViewGroup.LayoutParams.WRAP_CONTENT);
//		layoutParams.weight = 1;
//
//		setupVolume(activity, layoutParams);
//
//		setupDuration(activity, layoutParams);
//
//		mButtonLayout = new LinearLayout(activity);
//		mButtonLayout.setOrientation(LinearLayout.HORIZONTAL);
//        mButtonLayout.setLayoutParams(layoutParams);
//		mLinearLayout.addView(mButtonLayout);
//
////		stopBtn = new ImageButton(activity);
////		stopBtn.setImageResource(R.drawable.exo_controls_previous);
////		stopBtn.setBackground(getContext().getDrawable(R.drawable.ripple));
////		stopBtn.setMaxWidth(72);
////		stopBtn.setMaxHeight(72);
////		mButtonLayout.addView(stopBtn);
////		
////		stopBtn.setOnClickListener(new OnClickListener() {
////				@Override
////				public void onClick(View p1) {
////					if (mPlayer != null && (mPlayer.isPlaying() || mPlayer.getCurrentPosition() > 0)) {
////						mSeeker.setProgress(0);
////						mPlayer.stop();
////						playbackPosition = 0;
////						startBtn.setImageResource(R.drawable.exo_controls_play);
////						mLabelPosition.setText(Util.getDurationString(0, false));
////						mLabelDuration.setText(Util.getDurationString(mPlayer.getDuration(), true));
////						
////						if (mHandler == null) return;
////						mHandler.removeCallbacks(mUpdateCounters);
////					}
////				}
////			});
//
////		stopBtn.setOnClickListener(new OnClickListener() {
////				@Override
////				public void onClick(View p1) {
////					mPlayer.stop();
////					playbackPosition = 0;
////					if (mHandler == null) return;
////					mHandler.removeCallbacks(mUpdateCounters);
////				}
////			});
//
//        startBtn = new ImageButton(activity);
//		startBtn.setImageResource(R.drawable.exo_controls_play);
//		startBtn.setBackground(getContext().getDrawable(R.drawable.ripple));
//		startBtn.setMaxWidth(72);
//		startBtn.setMaxHeight(72);
//		mButtonLayout.addView(startBtn);
//
//		startBtn.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View p1) {
//					if (mPlayer == null || !mPlayer.isPlaying()) {
//						if (playbackPosition == 0) {
//							try {
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
//						//mCallback.onPaused(this);
//						if (mHandler == null) return;
//						mHandler.removeCallbacks(mUpdateCounters);
//					}
//				}
//			});
//
//
////		startBtn.setOnClickListener(new OnClickListener() {
////				@Override
////				public void onClick(View p1) {
////					try {
////						mPlayer.prepare();
////					} catch (Throwable e) {
////						e.printStackTrace();
////					}
////					mPlayer.start();
////					if (mHandler == null) mHandler = new Handler();
////					mHandler.post(mUpdateCounters);
////				}
////		});
//
////		Button pauseBtn = new Button(activity);
////		pauseBtn.setText("Pause");
////		mButtonLayout.addView(pauseBtn);
////		pauseBtn.setOnClickListener(new OnClickListener() {
////				@Override
////				public void onClick(View p1) {
////					playbackPosition = mPlayer.getCurrentPosition();
////					mPlayer.pause();
////					if (mHandler == null) return;
////					mHandler.removeCallbacks(mUpdateCounters);
////				}
////			});
////		Button restartBtn = new Button(activity);
////		restartBtn.setText("Restart");
////		mButtonLayout.addView(restartBtn);
////		restartBtn.setOnClickListener(new OnClickListener() {
////				@Override
////				public void onClick(View p1) {
////					if (playbackPosition == 0) {
////						try {
////							mPlayer.prepare();
////						} catch (Exception e) {
////							e.printStackTrace();
////						}
////					} else {
////						mPlayer.seekTo(playbackPosition);
////					}
////					mPlayer.start();
////				}
////		});
//    }
//
//	private void setupDuration(Context activity, LinearLayout.LayoutParams layoutParams) {
//		LinearLayout row = new LinearLayout(activity);
//		row.setOrientation(LinearLayout.HORIZONTAL);
//
//		mLabelPosition = new TextView(activity);
//		mLabelPosition.setLayoutParams(layoutParamsW);
//
//		mLabelDuration = new TextView(activity);
//		mLabelDuration.setLayoutParams(layoutParamsW);
//
//		mSeeker = new SeekBar(activity);
//		mSeeker.setLayoutParams(layoutParams);
//
//		mSeeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//				public void onProgressChanged(SeekBar seekBar, int progress,
//											  boolean fromUser) {
//					if (fromUser) {
//						mPlayer.seekTo(progress);
//						playbackPosition = progress;
//						mLabelPosition.setText(Util.getDurationString(progress, false));
//						mLabelDuration.setText(Util.getDurationString(mPlayer.getDuration() - progress, true));
//					}
//				}
//				public void onStartTrackingTouch(SeekBar seekBar)
//				{}
//				public void onStopTrackingTouch(SeekBar seekBar)
//				{}
//			});
//
//		row.addView(mLabelPosition);
//		row.addView(mSeeker);
//		row.addView(mLabelDuration);
//
//		mLinearLayout.addView(row);
//	}
//
//	private void setupVolume(Context activity, LinearLayout.LayoutParams layoutParams) {
//		final AudioManager am = (AudioManager) activity.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
//		final int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//		int streamVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
//
//		final LinearLayout row2 = new LinearLayout(activity);
//		row2.setOrientation(LinearLayout.HORIZONTAL);
//
//		final TextView mLabelPosition2 = new TextView(activity);
//		mLabelPosition2.setLayoutParams(layoutParamsW);
//		mLabelPosition2.setGravity(Gravity.CENTER_HORIZONTAL);
//		mLabelPosition2.setText(streamVolume + "");
//
//		final TextView mLabelDuration2 = new TextView(activity);
//		mLabelDuration2.setLayoutParams(layoutParamsW);
//		mLabelDuration2.setGravity(Gravity.CENTER_HORIZONTAL);
//		mLabelDuration2.setText(max + "");
//
//		final SeekBar mSeeker2 = new SeekBar(activity);
//		mSeeker2.setLayoutParams(layoutParams);
//		mSeeker2.setMax(max);
//		mSeeker2.setProgress(streamVolume);
//		mSeeker2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//				public void onProgressChanged(SeekBar seekBar, int progress,
//											  boolean fromUser)
//				{
//					if (fromUser)
//					{
//						am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
//						mLabelPosition2.setText(progress + "");
//					}
//				}
//				public void onStartTrackingTouch(SeekBar seekBar)
//				{}
//				public void onStopTrackingTouch(SeekBar seekBar)
//				{}
//			});
//		row2.addView(mLabelPosition2);
//		row2.addView(mSeeker2);
//		row2.addView(mLabelDuration2);
//
//		mLinearLayout.addView(row2);
//	}
//
//	
//	public void doClick(View view) {
////        switch(view.getId()) {
////			case R.id.startPlayerBtn:
////				try {
////					playAudio(AUDIO_PATH);
////					playLocalAudio();
////					playLocalAudio_UsingDescriptor();
////				} catch (Exception e) {
////					e.printStackTrace();
////				}
////				break;
////			case R.id.pausePlayerBtn:
////				if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
////					playbackPosition = mMediaPlayer.getCurrentPosition();
////					mMediaPlayer.pause();
////				}
////				break;
////			case R.id.restartPlayerBtn:
////				if(mediaPlayer != null && !mediaPlayer.isPlaying()) {
////					mMediaPlayer.seekTo(playbackPosition);
////					mMediaPlayer.start();
////				}
////				break;
////			case R.id.stopPlayerBtn:
////				if(mMediaPlayer != null) {
////					mMediaPlayer.stop();
////					playbackPosition = 0;
////				}
////				break;
////        }
//    }
//
////    private void playAudio(String url) throws Exception
////    {
////        release();
////
////        mMediaPlayer = new MediaPlayer();
////        mMediaPlayer.setDataSource(url);
////        mMediaPlayer.prepare();
////        mMediaPlayer.start();
////    }
//
////    private void playLocalAudio() throws Exception
////    {
////        mMediaPlayer = MediaPlayer.create(this, R.raw.music_file);
////        mMediaPlayer.start();
////    }
//
////    private void playLocalAudio_UsingDescriptor() throws Exception {
////
////        AssetFileDescriptor fileDesc = getResources().openRawResourceFd(
////            R.raw.music_file);
////        if (fileDesc != null) {
////
////            mMediaPlayer = new MediaPlayer();
////            mMediaPlayer.setDataSource(fileDesc.getFileDescriptor(), fileDesc
////									  .getStartOffset(), fileDesc.getLength());
////
////            fileDesc.close();
////
////            mMediaPlayer.prepare();
////            mMediaPlayer.start();
////        }
////    }
//
////    @Override
////    public void onDestroy() {
////        super.onDestroy();
////        release();
////    }
//
////    private void killMediaPlayer() {
////        if(mediaPlayer!=null) {
////            try {
////                mediaPlayer.release();
////            }
////            catch(Exception e) {
////                e.printStackTrace();
////            }
////        }
////    }
//	
//	
//    @Override
//    public void onPause() {
//        super.onPause();
//		if (getActivity().isFinishing()) {
//			release();
//		}
//    }
//
//	private void release() {
//		if (mPlayer != null) {
//			mLinearLayout.removeAllViews();
//			mLinearLayout.addView(mStatusTextView);
//            mVisualizer.setEnabled(false);
//			mVisualizer.release();
//            mEqualizer.release();
//            mPlayer.release();
//            mPlayer = null;
//        }
//		playbackPosition=0;
//		if (mSeeker != null) {
//			mSeeker.setProgress(0);
//		}
//		if (mHandler != null) {
//            mHandler.removeCallbacks(mUpdateCounters);
//            mHandler = null;
//        }
//	}
//}
//
///**
// * A simple class that draws waveform data received from a
// * {@link Visualizer.OnDataCaptureListener#onWaveFormDataCapture }
// */
//class VisualizerView extends View {
//    private byte[] mBytes;
//    private float[] mPoints;
//    private Rect mRect = new Rect();
//
//    private Paint mForePaint = new Paint();
//
//    public VisualizerView(Context context) {
//        super(context);
//        init();
//    }
//
//    private void init() {
//        mBytes = null;
//
//        mForePaint.setStrokeWidth(1f);
//        mForePaint.setAntiAlias(true);
//        mForePaint.setColor(Color.rgb(0, 128, 255));
//    }
//
//    public void updateVisualizer(byte[] bytes) {
//        mBytes = bytes;
//        invalidate();
//    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//
//        if (mBytes == null) {
//            return;
//        }
//
//        if (mPoints == null || mPoints.length < mBytes.length * 4) {
//            mPoints = new float[mBytes.length * 4];
//        }
//
//        mRect.set(0, 0, getWidth(), getHeight());
//
//        for (int i = 0; i < mBytes.length - 1; i++) {
//            mPoints[i * 4] = mRect.width() * i / (mBytes.length - 1);
//            mPoints[i * 4 + 1] = mRect.height() / 2
//				+ ((byte) (mBytes[i] + 128)) * (mRect.height() / 2) / 128;
//            mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (mBytes.length - 1);
//            mPoints[i * 4 + 3] = mRect.height() / 2
//				+ ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2) / 128;
//        }
//
//        canvas.drawLines(mPoints, mForePaint);
//    }
//}
