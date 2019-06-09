package net.gnu.p7zip;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.Toast;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import net.gnu.androidutil.ForegroundService;
import net.gnu.explorer.ExplorerActivity;
import net.gnu.explorer.R;
import net.gnu.util.FileUtil;
import net.gnu.util.Util;
import net.gnu.zpaq.Zpaq;
import java.util.Random;
import android.widget.ArrayAdapter;
import java.util.LinkedList;

public class CompressTask extends AsyncTask<String, String, String> implements UpdateProgress {

	private Activity activity = null;
	private CompressFragment compressFrag;
	private String lf = null;
	private String archive;
	private String password;
	private String level = "";
	private String type = "";
	private String volume = "";
	private String excludes = "";

//	private String solidArchive;
//	private String workingDirectory;
//
//	private String deleteFilesAfterArchiving = "";
//	private String encryptFileNames = "";

	private List<String> otherArgs;

	private String[] levels = new String[]{"-mx0", "-mx1", "-mx3", "-mx5", "-mx7", "-mx9"};
	private String[] zpaqLevels = new String[]{"-m0", "-m1", "-m2", "-m3", "-m4", "-m5"};
	//String[] types = new String[]{"-t7z", "-tzip", "-tgz", "-ttar"};
	//String[] volumes = new String[]{"b", "k", "m", "g"};
	private long start;
	private static final String TAG = "CompressTask";

	private PowerManager.WakeLock wl;
	private Andro7za andro7za;
	private Zpaq zpaq;
	private boolean test = false;
	private boolean createSeparateArchives = false;
	private String archiveNameMask = "";
	private String cmd = "";

	private Notification.Builder mBuilder;
    private NotificationManager mNotifyMgr;
    private Notification notification;
	private final int mNotificationId = TAG.hashCode();

	public CompressTask(CompressFragment compressFrag) {

		this.compressFrag = compressFrag;
		compressFrag.compressTask = this;

		activity = compressFrag.getActivity();
		andro7za = new Andro7za(activity);
		zpaq = new Zpaq(activity);
		cmd = compressFrag.update ? "u" : "a";
		this.lf = compressFrag.fileET.getText().toString();

		String fName = compressFrag.saveToET.getText().toString().trim();
		if (fName.matches(FileUtil.compressibleExtension)) {
			this.archive = fName;
		} else {
			this.archive = fName + "." + ((RadioButton)compressFrag.getView().findViewById(compressFrag.typeRadioGroup.getCheckedRadioButtonId())).getTag();// + "/" + compressFrag.fName
		}

		type = compressFrag.getView().findViewById(compressFrag.typeRadioGroup.getCheckedRadioButtonId()).getTag() + "";

		this.password = compressFrag.passwordET.getText().toString();

		if (type.equals("zpaq")) {
			level = zpaqLevels[compressFrag.compressLevelSpinner.getSelectedItemPosition()];
		} else {
			level = levels[compressFrag.compressLevelSpinner.getSelectedItemPosition()];
		}

		if (compressFrag.volumeValET.length() > 0) {
			volume = "" + ((RadioButton)compressFrag.getView().findViewById(compressFrag.volUnitRadioGroup.getCheckedRadioButtonId())).getTag();//volumes[compressFrag.volumeUnit];
			volume = compressFrag.volumeValET.getText() + volume;
		}

		excludes = compressFrag.excludeET.getText().toString();

		//String workingDirectory = compressFrag.workingDirectory.length() > 0 ? " -w\"" + compressFrag.workingDirectory + "\" " : "";

		String otherParameters = compressFrag.otherParametersET.getText().toString();
		String[] otherArg = (type.equals("7z") ? otherParameters.replace("-mqs=on", "") : otherParameters).split("\\s+");
		otherArgs = new ArrayList<String>(Arrays.asList(otherArg));
		otherArgs.add(0, (type.equals("7z") ? (compressFrag.solidArchiveCB.isChecked() ? compressFrag.solidArchiveET.getText().toString() : "-ms=off") : ""));
		otherArgs.add(0, (type.equals("7z") ? (compressFrag.encryptFileNamesCB.isChecked() ? "-mhe=on" : "") : ""));
		otherArgs.add(0, compressFrag.deleteFilesAfterArchivingCB.isChecked() ? "-sdel" : "");
		//otherArgs += workingDirectory;
		this.test = compressFrag.test;
		this.createSeparateArchives = compressFrag.createSeparateArchives;
		this.archiveNameMask = compressFrag.archiveNameMask.trim();

	}

	@Override
	public void updateProgress(String...values) {
		publishProgress(values);
	}

	@Override
	protected void onPreExecute() {
		mNotifyMgr = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent contentIntent = new Intent(activity, ExplorerActivity.class);
		contentIntent.setAction(Intent.ACTION_MAIN);
		contentIntent.putExtra(ExplorerActivity.KEY_INTENT_COMPRESS, true);
        contentIntent.putExtra("from", TAG);
        //contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(activity, new Random().nextInt(),
																	  contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder = new Notification.Builder(activity)
			.setSmallIcon(R.drawable.notification_template_icon_bg)
			.setContentTitle("Compressing")
			.setContentText(archive)
			.setOngoing(true)
			//.setSmallIcon(R.drawable.ic_action_compress)
			.setLargeIcon(((BitmapDrawable)activity.getDrawable(R.drawable.ic_action_compress)).getBitmap())
			.setTicker("Compress");

		mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
		mBuilder.setContentIntent(resultPendingIntent);
		mNotifyMgr.notify(mNotificationId, mBuilder.build());//mBuilder.setStyle(big)

		if (compressFrag.adapter != null) {
			compressFrag.adapter.clear();
			compressFrag.adapter.notifyDataSetChanged();
		}
	}

	@Override
	protected String doInBackground(String... urls) {
		start = System.currentTimeMillis();
		PowerManager pm = (PowerManager)activity.getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

		try {
			wl.acquire();
			//JarUtil.createJAR("/sdcard/lib.jar", "/storage/emulated/0/AppProjects/Searcher/bin/classesdebug/");
			final File f = new File(archive).getParentFile();
			if (!f.exists()) {
				f.mkdirs();
			}
			//sb = new StringBuilder();
			//rowNum = 0;
			publishProgress("Compressing " + archive);
			final List<String> fList = Arrays.asList(lf.split("\\|+\\s*"));
			final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat(archiveNameMask);
			if (createSeparateArchives) {
				final List<String> sList = new ArrayList<>(1);
				if (archiveNameMask != null && archiveNameMask.length() > 0) {
					for (String st : fList) {
						sList.clear();
						sList.add(st);
						File file = new File(st);
						if (!type.equals("zpaq")) {
							int ret = andro7za.compress(
								cmd,
								file.getParent() + "/" + file.getName() + "_" + TIME_FORMAT.format(Calendar.getInstance().getTimeInMillis()) + "." + type,
								password, 
								level, 
								volume, 
								sList,
								excludes,
								new ArrayList<String>(otherArgs),
								this);
							if (ret == 1) {
								publishProgress("Warning");
								return "Warning";
							} else if (ret == 2) {
								publishProgress("Fatal error");
								return "Fatal error";
							} else if (ret == 7) {
								publishProgress("Command line error");
								return "Command line error";
							} else if (ret == 8) {
								publishProgress("Not enough memory for operation");
								return "Not enough memory for operation";
							} else if (ret == 255) {
								publishProgress("User stopped the process");
								return "User stopped the process";
							}
						} else {
							int ret = zpaq.compress(
								file.getParent() + "/" + file.getName() + "_" + TIME_FORMAT.format(Calendar.getInstance().getTimeInMillis()) + "." + type,
								password, 
								level, 
								st,
								excludes,
								new ArrayList<String>(otherArgs),
								this);
						}
					}
				} else {//!archiveNameMask
					for (String st : fList) {
						sList.clear();
						sList.add(st);
						File file = new File(st);
						if (!type.equals("zpaq")) {
							int ret = andro7za.compress(
								cmd,
								file.getParent() + "/" + file.getName() + "." + type,
								password, 
								level, 
								volume, 
								sList,
								excludes,
								new ArrayList<String>(otherArgs),
								this);
							if (ret == 1) {
								publishProgress("Warning");
								return "Warning";
							} else if (ret == 2) {
								publishProgress("Fatal error");
								return "Fatal error";
							} else if (ret == 7) {
								publishProgress("Command line error");
								return "Command line error";
							} else if (ret == 8) {
								publishProgress("Not enough memory for operation");
								return "Not enough memory for operation";
							} else if (ret == 255) {
								publishProgress("User stopped the process");
								return "User stopped the process";
							}
						} else {
							int ret = zpaq.compress(
								file.getParent() + "/" + file.getName() + "." + type,
								password, 
								level, 
								st,
								excludes,
								new ArrayList<String>(otherArgs),
								this);
						}
					}
				}
			} else { //!createSeparateArchives
				int lastIndexOf = archive.lastIndexOf(".");
				if (!type.equals("zpaq")) {
					int ret = andro7za.compress(
						cmd,
						archiveNameMask.length() == 0 ? archive : archive.substring(0, lastIndexOf) + "_" + TIME_FORMAT.format(Calendar.getInstance().getTimeInMillis()) + archive.substring(lastIndexOf),
						password, 
						level, 
						volume, 
						fList,
						excludes,
						otherArgs,
						this);
					if (ret == 1) {
						publishProgress("Warning");
						return "Warning";
					} else if (ret == 2) {
						publishProgress("Fatal error");
						return "Fatal error";
					} else if (ret == 7) {
						publishProgress("Command line error");
						return "Command line error";
					} else if (ret == 8) {
						publishProgress("Not enough memory for operation");
						return "Not enough memory for operation";
					} else if (ret == 255) {
						publishProgress("User stopped the process");
						return "User stopped the process";
					}
				} else {
					int ret = zpaq.compress(
						archiveNameMask.length() == 0 ? archive : archive.substring(0, lastIndexOf) + "_" + TIME_FORMAT.format(Calendar.getInstance().getTimeInMillis()) + archive.substring(lastIndexOf),
						password, 
						level, 
						lf,
						excludes,
						otherArgs,
						this);
				}
			}

			return "";
		} catch (Throwable e) {
			e.printStackTrace();
			String message = "Compression is not successful\n" + e.getMessage();
			return message;
		} finally {
			Log.i(TAG, "finally doBackground");
			if (wl != null && wl.isHeld()) {
				wl.release();
			}
			compressFrag.stopService();
		}
	}

	@Override
	protected void onCancelled(String result) {
		Log.d(TAG, "onCancelled zpaq.command " + zpaq.command + ", andro7za.command " + andro7za.command);
		mNotifyMgr.cancel(mNotificationId);
		if (zpaq.command != null) {
			zpaq.command.stopAll();
		}
		if (andro7za.command != null) {
			andro7za.command.stopAll();
		}
		if (wl != null && wl.isHeld()) {
			wl.release();
		}
		compressFrag.stopService();
	}

	@Override
	protected void onPostExecute(String result) {
		Log.i(TAG, "onPostExecute " + result);
		final String elapseTime = Util.nf.format(System.currentTimeMillis() - start);
		mNotifyMgr.cancel(mNotificationId);
		compressFrag.mBtnOK.setText("Compress");
		if (result.length() > 0) {
			compressFrag.adapter.add(result);
			compressFrag.adapter.add("Operation took " + elapseTime + " milliseconds");
			compressFrag.adapter.notifyDataSetChanged();
			//Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
		} 
		//if (activity != null) {
		Toast.makeText(activity, "Compression finished. Operation took " + elapseTime + " milliseconds", Toast.LENGTH_SHORT).show();
		//}

		if (wl != null && wl.isHeld()) {
			wl.release();
		}
		compressFrag.stopService();
		Log.d(TAG, result);
	}

	//private int rowNum = 0;
	//private StringBuilder sb;
	@Override
	protected void onProgressUpdate(String... progress) {
		if (progress != null && progress.length > 0 
			&& progress[0] != null) {//} && progress[0].trim().length() > 0) {
//			Toast.makeText(activity, progress[0], Toast.LENGTH_LONG).show();
//			if (++rowNum > 24) {
//				sb = new StringBuilder(sb.substring(sb.indexOf("\n") + 1));
//			} 
			//sb.append(progress[0]).append("\n");
			compressFrag.adapter.add(progress[0]);
			compressFrag.adapter.notifyDataSetChanged();
			Log.d(TAG, progress[0]);
		}
	}
}

