package com.tuarua.avane.android.libavaneexample;

import android.app.Activity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.lylc.widget.circularprogressbar.CircularProgressBar;
import com.tuarua.avane.android.LibAVANE;
import com.tuarua.avane.android.Progress;
import com.tuarua.avane.android.events.Event;
import com.tuarua.avane.android.events.IEventHandler;
import com.tuarua.avane.android.ffmpeg.InputOptions;
import com.tuarua.avane.android.ffmpeg.InputStream;
import com.tuarua.avane.android.ffmpeg.OutputAudioStream;
import com.tuarua.avane.android.ffmpeg.OutputOptions;
import com.tuarua.avane.android.ffmpeg.OutputVideoStream;
import com.tuarua.avane.android.ffmpeg.X264Options;
import com.tuarua.avane.android.ffmpeg.constants.LogLevel;
import com.tuarua.avane.android.ffmpeg.constants.X264Preset;
import com.tuarua.avane.android.ffmpeg.constants.X264Profile;
import com.tuarua.avane.android.ffmpeg.gets.SampleFormat;
import com.tuarua.avane.android.ffprobe.Probe;
import com.tuarua.avane.android.libavaneexample.utils.TimeUtils;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.gnu.explorer.R;

public class MainActivity extends Activity {


    private LibAVANE libAVANE;
    private String appDirectory;

    private CircularProgressBar progressCircle;
    private double duration;
    private Button btn;
    private TextView tv3;
    private DecimalFormat percentFormat1D;
    private DecimalFormat percentFormat2D;

    private boolean isWorking = false;
	private EditText inputFile;
	private TextView errorTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avane);


        percentFormat1D = new DecimalFormat("0.0");
        percentFormat1D.setRoundingMode(RoundingMode.UP);

        percentFormat2D = new DecimalFormat("0.00");
        percentFormat2D.setRoundingMode(RoundingMode.UP);

        libAVANE = LibAVANE.getInstance();
        libAVANE.setLogLevel(LogLevel.INFO);

        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setText(libAVANE.getVersion());

        inputFile = (EditText) findViewById(R.id.inputFile);
		inputFile.setText("-i /storage/emulated/0/videoplayback-3.3gpp /storage/emulated/0/videoplayback-3.avi");

        tv3 = (TextView) findViewById(R.id.textView3);
		errorTv = (TextView) findViewById(R.id.errorTv);

        Log.i("build config", libAVANE.getBuildConfiguration());
		getInfo();
		
        //PackageManager m = getPackageManager();
        appDirectory = "/sdcard/" + getPackageName();
        //PackageInfo p = null;
//        try {
//            p = m.getPackageInfo(appDirectory, 0);
//            appDirectory = p.applicationInfo.dataDir; // /data/user/0/com.tuarua.avane.android.libavaneexample
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }

        progressCircle = (CircularProgressBar) findViewById(R.id.circularprogressbar);
        progressCircle.setTitle("0%");
        progressCircle.setSubTitle("");
        progressCircle.setMax(360);
        progressCircle.setProgress(0);

        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (isWorking) {
						isWorking = false;
						Log.d("onClick", "isWorking " + isWorking);
						libAVANE.cancelEncode();
					} else {
						isWorking = true;
						Log.d("onClick", "isWorking " + isWorking);
						//btn.setEnabled(false);
						triggerProbe();
					}
				}
			});


        libAVANE.eventDispatcher.addEventListener(Event.TRACE, new IEventHandler(){
				@Override
				public void callback(Event event) {
					String msg = (String) event.getParams();
					Log.i("MA trace", msg);
				}
			});
        libAVANE.eventDispatcher.addEventListener(Event.INFO, new IEventHandler(){
				@Override
				public void callback(Event event) {
					final String msg = (String) event.getParams();
					Log.i("MA info", msg);
					if (msg.contains("[ffmpeg][][error]") || msg.contains("[ffmpeg][][fatal]")) {
						isWorking = false;
						runOnUiThread(new Runnable() {
								@Override
								public void run() {
									MainActivity.this.errorTv.append(msg.substring(" [ffmpeg][]".length()) + "\n");
								}
							});
					}
				}
			});
        libAVANE.eventDispatcher.addEventListener(Event.ON_ENCODE_FATAL, new IEventHandler(){
				@Override
				public void callback(Event event) {
					final String msg = (String) event.getParams();
					Log.i("MA ON_ENCODE_FATAL", msg);
					runOnUiThread(new Runnable() {
							@Override
							public void run() {
								MainActivity.this.errorTv.append(msg + "\n");
							}
						});
				}
			});
        libAVANE.eventDispatcher.addEventListener(Event.ON_ENCODE_ERROR, new IEventHandler(){
				@Override
				public void callback(Event event) {
					final String msg = (String) event.getParams();
					Log.i("MA ON_ENCODE_ERROR", msg);
					runOnUiThread(new Runnable() {
							@Override
							public void run() {
								MainActivity.this.errorTv.append(msg + "\n");
							}
						});
				}
			});
        libAVANE.eventDispatcher.addEventListener(Event.ON_ENCODE_START, new IEventHandler(){
				@Override
				public void callback(Event event) {
					String msg = (String) event.getParams();
					Log.i("MA ON_ENCODE_START", msg);
					runOnUiThread(new Runnable() {
							@Override
							public void run() {
								MainActivity.this.progressCircle.setVisibility(View.VISIBLE);
								MainActivity.this.btn.setText("Cancel");
								MainActivity.this.btn.setEnabled(true);
							}
						});

					Log.i("MA", "encode start");
				}
			});
        libAVANE.eventDispatcher.addEventListener(Event.ON_ENCODE_FINISH, new IEventHandler(){
				@Override
				public void callback(Event event) {
					String msg = (String) event.getParams();
					Log.i("MA ON_ENCODE_FINISH", msg);
					runOnUiThread(new Runnable() {
							@Override
							public void run() {
								MainActivity.this.progressCircle.setProgress(360);
								MainActivity.this.progressCircle.setTitle("100%");
								MainActivity.this.btn.setText("Encode");
								MainActivity.this.btn.setEnabled(true);
							}
						});
					isWorking = false;
					Log.i("MA", "encode finish");
				}
			});

        libAVANE.eventDispatcher.addEventListener(Event.ON_ENCODE_PROGRESS, new IEventHandler() {
				@Override
				public void callback(Event event) {
					final Progress progress = (Progress) event.getParams();
					final double percent = (progress.secs + (progress.us / 100)) / duration;
					final int degrees = (int) Math.round(percent * 360);
					runOnUiThread(new Runnable() {
							@Override
							public void run() {
								MainActivity.this.progressCircle.setProgress(degrees);
								MainActivity.this.tv3.setText("");
								MainActivity.this.tv3.append(String.format("time: %s",
																		   TimeUtils.secsToTimeCode((progress.secs + progress.us / 100))  + " / " + TimeUtils.secsToTimeCode(MainActivity.this.duration)));
								MainActivity.this.tv3.append(String.format("\nspeed: %s", String.valueOf(percentFormat2D.format(progress.speed)) + "x"));
								MainActivity.this.tv3.append(String.format("\nfps: %s", String.valueOf(percentFormat2D.format(progress.fps))));
								MainActivity.this.tv3.append(String.format("\nbitrate: %s", String.valueOf(percentFormat2D.format(progress.bitrate)) + " Kbps"));
								MainActivity.this.tv3.append(String.format("\nframe: %s", String.valueOf(progress.frame)));
								MainActivity.this.tv3.append(String.format("\nsize: %s", Formatter.formatFileSize(MainActivity.this, (long)progress.size)));
								MainActivity.this.progressCircle.setTitle(percentFormat1D.format(percent * 100) + "%");
							}
						});
				}
			});

        libAVANE.eventDispatcher.addEventListener(Event.ON_PROBE_INFO, new IEventHandler() {
				@Override
				public void callback(Event event) {
					Log.i("ON_PROBE_INFO", event.getParams() + ".");
				}
			});

        libAVANE.eventDispatcher.addEventListener(Event.ON_PROBE_INFO_AVAILABLE, new IEventHandler() {
				@Override
				public void callback(Event event) {
					Log.i("ON_PROBE_INFO_AVAILABLE", event.getParams() + ".");
					Probe probe = libAVANE.getProbeInfo();

					duration = probe.format.duration;
					Log.i("ON_PROBE_INFO_AVAILABLE", "PROBE DONE");
					Log.i("ON_PROBE_INFO_AVAILABLE", "CALLING encode");
					//doEncode();
					doEncodeClassic();
				}
			});

        libAVANE.eventDispatcher.addEventListener(Event.NO_PROBE_INFO, new IEventHandler() {
				@Override
				public void callback(Event event) {
					Log.i("NO_PROBE_INFO", event.getParams() + ".");
				}
			});


    }

    private void triggerProbe() {
		Pattern p = Pattern.compile(".*?-i\\s*?([\"\\s])([^\"]+?)(\\1)\\s*?.*?");
		Matcher m = p.matcher(inputFile.getText().toString());
		if (m.matches()) {
			Log.i("triggerProbe", "inputFile " + m.group(2));
			errorTv.setText("");
			libAVANE.triggerProbeInfo(m.group(2));
		}
    }

    private void doEncodeClassic() {
		String params = inputFile.getText().toString();//"-c:v libx264 -crf 22 -c:a copy -preset ultrafast -y "
			//+ " " + appDirectory + text.substring(text.lastIndexOf("/"));//"/files/avane-encode-classic.mp4";
		Log.i("params", params);
        libAVANE.encode(params);
    }

    private void doEncode() {

        InputOptions inputOptions = new InputOptions();
        inputOptions.uri = "http://download.blender.org/durian/trailer/sintel_trailer-1080p.mp4";
        InputStream.clear();
        InputStream.addInput(inputOptions);

        //video
        OutputVideoStream videoStream = new OutputVideoStream();
        videoStream.codec = "libx264";
        videoStream.crf = 22;

        X264Options x264Options = new X264Options();
        x264Options.preset = X264Preset.ULTRA_FAST;
        x264Options.profile = X264Profile.MAIN;
        x264Options.level = "4.1";
        videoStream.encoderOptions = x264Options;

        OutputOptions.addVideoStream(videoStream);

        //audio
        OutputAudioStream audioStream = new OutputAudioStream();
        audioStream.codec = "aac";
        OutputOptions.addAudioStream(audioStream);

        OutputOptions.uri = appDirectory + "/files/avane-encode-classic.mp4";

        libAVANE.encode();
    }
    private void getInfo() {
        /*
		 ArrayList<Color> clrs = libAVANE.getColors();
		 Log.i("num colors",String.valueOf(clrs.size()));
		 */

        /*
		 ArrayList<PixelFormat> fltrs = libAVANE.getPixelFormats();
		 Log.i("num flters",String.valueOf(fltrs.size()));
		 */

        //Layouts layouts = libAVANE.getLayouts();
        //Protocols protocols = libAVANE.getProtocols();
        //Log.i("num inputs",String.valueOf(protocols.inputs.size()));
        //Log.i("num outputs",String.valueOf(protocols.outputs.size()));

        /*
		 ArrayList<BitStreamFilter> bitStreamFilters = libAVANE.getBitStreamFilters();
		 Log.i("num bsfs",String.valueOf(bitStreamFilters.size()));
		 */

        /*
		 ArrayList<Codec> codecs = libAVANE.getCodecs();
		 Log.i("num codecs",String.valueOf(codecs.size()));
		 */

        /*
		 ArrayList<Decoder> decoders = libAVANE.getDecoders();
		 Log.i("num decoders",String.valueOf(decoders.size()));
		 */

        /*
		 ArrayList<Encoder> encoders = libAVANE.getEncoders();
		 Log.i("num encoders",String.valueOf(encoders.size()));
		 */
        /*
		 ArrayList<HardwareAcceleration> hwAcc = libAVANE.getHardwareAccelerations();
		 Log.i("num hw accels",String.valueOf(hwAcc.size()));
		 */
        /*
		 ArrayList<Device> devices = libAVANE.getDevices();
		 Log.i("num devices",String.valueOf(devices.size()));
		 */

        /*
		 ArrayList<AvailableFormat> formats = libAVANE.getAvailableFormats();
		 for (AvailableFormat format : formats) {
		 Log.i("format: ",format.nameLong);
		 }
		 Log.i("num formats",String.valueOf(formats.size()));
		 */



        ArrayList<SampleFormat> formats = libAVANE.getSampleFormats();
        for (SampleFormat format : formats) {
            Log.i("format: ", format.name);
        }

        Log.i("num sample formats", String.valueOf(formats.size()));
    }

}
