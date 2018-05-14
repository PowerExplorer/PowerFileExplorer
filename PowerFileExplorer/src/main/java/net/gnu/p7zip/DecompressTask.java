package net.gnu.p7zip;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import android.app.*;
import net.gnu.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import net.gnu.zpaq.Zpaq;
import net.gnu.androidutil.ForegroundService;
import net.gnu.androidutil.AndroidUtils;
import net.gnu.explorer.ExplorerActivity;
import android.graphics.drawable.BitmapDrawable;
import net.gnu.explorer.R;
import java.util.Random;

public class DecompressTask extends AsyncTask<String, String, String> implements UpdateProgress {

	private ExplorerActivity activity = null;
	DecompressFragment decompFrag;
	private String fList = null;

	private String command;
	private String saveTo;
	private String password;
	private String include;
	private String exclude;
	private String szmode = "";
	private String zpaqmode = "";
	List<String> otherArgs;
	private String[] modes = new String[]{"-aoa", "-aos", "-aou", "-aot"};
	private String[] zpaqmodes = new String[]{"-force", "", "", ""};
	private long start;
	PowerManager.WakeLock wl;
	private Andro7za andro7za;
	private Zpaq zpaq;
	Runnable run;
	private static final String TAG = "DecompressTask";
	private Notification.Builder mBuilder;
    private NotificationManager mNotifyMgr;
    private Notification notification;
	private final int mNotificationId = TAG.hashCode();

	public DecompressTask(final DecompressFragment decompFrag) {
		this.decompFrag = decompFrag;
		this.activity = (ExplorerActivity) decompFrag.getActivity();
		andro7za = new Andro7za(activity);
		zpaq = new Zpaq(activity);
		this.fList = decompFrag.files;//.getText().toString();
		this.saveTo = decompFrag.saveTo;//.getText().toString();
		this.include = decompFrag.include;//.getText().toString();
		this.exclude = decompFrag.exclude;//.getText().toString();
		this.password = decompFrag.password;//.getText().toString();
		String[] otherArg = decompFrag.otherArgs.split("\\s+");//.getText().toString().split("\\s+");
		this.otherArgs = new ArrayList<String>(Arrays.asList(otherArg));
		szmode = modes[decompFrag.overwriteModeSpinner.getSelectedItemPosition()];
		zpaqmode = zpaqmodes[decompFrag.overwriteModeSpinner.getSelectedItemPosition()];
		command = decompFrag.command;//extractWithFullPathsCB.isChecked() ? "x" : "e";
	}

	public DecompressTask(final Activity activity, 
						  final String files, 
						  final String saveTo, 
						  final String include, 
						  final String exclude, 
						  final String password, 
						  final String otherArgs, 
						  final int overwriteModeSpinner, 
						  final String command, 
						  final Runnable run) {
		this.activity = (ExplorerActivity) activity;
		andro7za = new Andro7za(activity);
		zpaq = new Zpaq(activity);
		this.fList = files;
		this.saveTo = saveTo;
		this.include = include;
		this.exclude = exclude;
		this.password = password;
		String[] otherArg = otherArgs.split("\\s+");
		this.otherArgs = new ArrayList<String>(Arrays.asList(otherArg));
		szmode = modes[overwriteModeSpinner];
		zpaqmode = zpaqmodes[overwriteModeSpinner];
		this.command = command;
		this.run = run;
	}

	public DecompressTask(final Activity activity, 
						  final String files, 
						  final String saveTo, 
						  final String include, 
						  final String exclude, 
						  final String password, 
						  final List<String> otherArgs, 
						  final int overwriteModeSpinner, 
						  final String command, 
						  final Runnable run) {
		this.activity = (ExplorerActivity) activity;
		andro7za = new Andro7za(activity);
		zpaq = new Zpaq(activity);
		this.fList = files;
		this.saveTo = saveTo;
		this.include = include;
		this.exclude = exclude;
		this.password = password;
		this.otherArgs = otherArgs;
		szmode = modes[overwriteModeSpinner];
		zpaqmode = zpaqmodes[overwriteModeSpinner];
		this.command = command;
		this.run = run;
	}

	@Override
	protected void onPreExecute() {
		mNotifyMgr = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent contentIntent = new Intent(activity, ExplorerActivity.class);
		contentIntent.setAction(Intent.ACTION_MAIN);
		contentIntent.putExtra(ExplorerActivity.KEY_INTENT_DECOMPRESS, true);
        //contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(activity, new Random().nextInt(),
																	  contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder = new Notification.Builder(activity)
			.setSmallIcon(R.drawable.notification_template_icon_bg)
			.setContentTitle("Decompressing")
			.setContentText(saveTo)
			.setOngoing(true)
			//.setSmallIcon(R.drawable.ic_action_compress)
			.setLargeIcon(((BitmapDrawable)activity.getDrawable(R.drawable.ic_action_compress)).getBitmap())
			.setTicker("Compress");

		mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
		mBuilder.setContentIntent(resultPendingIntent);
		mNotifyMgr.notify(mNotificationId, mBuilder.build());//mBuilder.setStyle(big)

		if (decompFrag != null && decompFrag.adapter != null) {
			decompFrag.adapter.clear();
			decompFrag.adapter.notifyDataSetChanged();
		}
		
		
	}

	protected String doInBackground(String... urls) {
		start = System.currentTimeMillis();
		PowerManager pm = (PowerManager)activity.getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		try {
			wl.acquire();
			List<String> fiList = Arrays.asList(fList.split("\\|+\\s*"));
			include = include.replaceAll("\\|+\\s*", "\n");
			exclude = exclude.replaceAll("\\|+\\s*", "\n");
			Log.d(TAG, "doInBackground fiList " + fiList);
			if (saveTo != null && saveTo.length() > 0) {
				File f = new File(saveTo);
				if (!f.exists()) {
					f.mkdirs();
				}
			}

			int ret = 0;
			List<String> args = new ArrayList<>();
			for (String archiveName : fiList) {
				publishProgress("Extracting " + archiveName);
				if (new File(archiveName).exists()) {
					args.clear();
					args.addAll(otherArgs);
					//sb = new StringBuilder();
					//rowNum = 0;
					if (archiveName.toLowerCase().endsWith(".zpaq")) {
						ret = zpaq.decompress(archiveName, password, saveTo, zpaqmode, include, exclude, args, this);
					} else {
						ret = andro7za.extractInEx(command, archiveName, password, szmode, saveTo, include, exclude, args, this);
						if (ret == 1) {
							publishProgress("Warning " + archiveName);
							return "Warning";
						} else if (ret == 2) {
							publishProgress("Fatal error " + archiveName);
							return "Fatal error";
						} else if (ret == 7) {
							publishProgress("Command line error " + archiveName);
							return "Command line error";
						} else if (ret == 8) {
							publishProgress("Not enough memory for operation" + archiveName);
							return "Not enough memory for operation";
						} else if (ret == 255) {
							publishProgress("User stopped the process " + archiveName);
							return "User stopped the process";
						}
					}
				} else {
					publishProgress(archiveName + " is not existed");
				}
			}
			return "Decompression finished";
		} catch (Throwable e) {
			e.printStackTrace();
			String message = "Decompression is not successful" + e.getMessage();
			return message;
		} finally {
			if (wl != null && wl.isHeld()) {
				wl.release();
			}
			if (decompFrag != null) {
				decompFrag.stopService();
			}
		}
	}

	@Override
	protected void onCancelled(String result) {
		Log.i(TAG, "onCancelled");
		mNotifyMgr.cancel(mNotificationId);
		andro7za.command.stopAll();
		zpaq.command.stopAll();
		if (wl != null && wl.isHeld()) {
			wl.release();
		}
		if (decompFrag != null) {
			decompFrag.stopService();
		}
		
	}

	@Override
	public void updateProgress(String...values) {
		publishProgress(values);
	}

	@Override
	protected void onPostExecute(final String result) {
		mNotifyMgr.cancel(mNotificationId);
		//sb.append(result);
		if (wl != null && wl.isHeld()) {
			wl.release();
		}
		//if (decompFrag.activity != null) {
		final String elapseTime = Util.nf.format(System.currentTimeMillis() - start);
		Toast.makeText(activity, result + "Operation took " + elapseTime + " milliseconds", Toast.LENGTH_LONG).show();
		//}
		//Toast.makeText(activity, "Operation took " + Util.nf.format(System.currentTimeMillis() - start) + " milliseconds", Toast.LENGTH_LONG).show();
		if (decompFrag != null) {
			decompFrag.stopService();
			decompFrag.mBtnOK.setText("Decompress");
			decompFrag.adapter.add(result);
			decompFrag.adapter.add("Operation took " + elapseTime + " milliseconds");
			decompFrag.adapter.notifyDataSetChanged();
		}
		Log.d(TAG, result);
		if (run != null && activity != null && activity.isResumed()) {
			activity.act(run);
		}
	}

	//private int rowNum = 0;
	//private StringBuilder sb = new StringBuilder();
	@Override
	protected void onProgressUpdate(String... progress) {
		//Log.d(TAG, "decompFrag " + decompFrag + ", " + progress[0]);
		if (decompFrag != null && progress != null && progress.length > 0 
			&& progress[0] != null) {//} && progress[0].trim().length() > 0) {
			
//			if (++rowNum > 24) {
//				sb = new StringBuilder(sb.substring(sb.indexOf("\n") + 1));
//				//decompFrag.statusTV.setText("");
//			} 
			//sb.append(progress[0]).append("\n");
			decompFrag.adapter.add(progress[0]);
			decompFrag.adapter.notifyDataSetChanged();
			//Log.d(TAG, progress[0]);
		}
	}
}

