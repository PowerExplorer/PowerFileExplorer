package net.gnu.explorer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.widget.RemoteViews;
import net.gnu.explorer.R;
import java.util.Collections;
import java.util.LinkedList;
import android.os.SystemClock;
import android.content.res.Resources;
import android.text.format.Formatter;
import android.net.ConnectivityManager;
import android.util.Log;
import net.gnu.util.Util;
import android.os.Binder;

public class DataTrackerService extends Service {

    private int pollRate = 5;
	private String command = "";

    private PowerManager pm;
    private ConnectivityManager cm;
    private Notification.Builder mBuilder;
    private NotificationManager mNotifyMgr;
    private Notification notification;

    private PackageManager pk;

    private final ArrayList<AppStats> appStatsList = new ArrayList<>();
	private final ArrayList<AppStats> prevAppStatsList = new ArrayList<>();

	private long totalRxSincePrev = 0;
	private long totalTxSincePrev = 0;

	private long startTime = 0;//SystemClock.elapsedRealtimeNanos();
	private long prevElapsedTime;

	private int unitType = 4;
	private final String[] unitTypeArr = new String[] {
		"bps",
		"kbps",
		"mbps",
		"gbps",
		"Bps",
		"KBps",
		"MBps",
		"GBps"
	};
	private final int mNotificationId = 1;

	private ScheduledFuture updateHandler;

    @Override
    public void onCreate() {
        Log.d("DataTrackerService", "onCreate " + this);
        super.onCreate();

	}

    private void createService(final Service service) {
		Log.d("DataTrackerService", "createService " + command);
        pk = getPackageManager();
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mNotifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

//		Intent resultIntent = new Intent(service, ExplorerActivity.class);
//		TaskStackBuilder stackBuilder = TaskStackBuilder.create(service);
//		stackBuilder.addParentStack(ExplorerActivity.class);
//		stackBuilder.addNextIntent(resultIntent);
//		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
//			0,
//			PendingIntent.FLAG_UPDATE_CURRENT
//		);
		Intent contentIntent = new Intent(this, ExplorerActivity.class);
		contentIntent.setAction(Intent.ACTION_MAIN);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
																	  contentIntent, 0);
		Intent startIntent = new Intent(this, ServiceActivity.class);
		startIntent.setAction("start");
		PendingIntent startPendingIntent = PendingIntent.getActivity(this, 0,
																	 startIntent, 0);
		Intent pauseIntent = new Intent(this, ServiceActivity.class);
		pauseIntent.setAction("pause");
		PendingIntent pausePendingIntent = PendingIntent.getActivity(this, 0,
																	 pauseIntent, 0);
		Intent exitIntent = new Intent(this, ServiceActivity.class);
		exitIntent.setAction("exit");
		PendingIntent exitPendingIntent = PendingIntent.getActivity(this, 0,
																	exitIntent, 0);
		mBuilder = new Notification.Builder(service)
			.setSmallIcon(R.drawable.notification_template_icon_bg)
			.setContentTitle("Data Tracker")
			.setContentText("")
			.setOngoing(true)
			.setSmallIcon(android.R.drawable.stat_sys_download)
			.setTicker("Data usage");

		mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
		mBuilder.setContentIntent(resultPendingIntent);

		//mNotifyMgr.notify(mNotificationId, notification);

		if ("start".equals(command)) {
			mBuilder.addAction(android.R.drawable.ic_media_pause,
							   "Pause",
							   pausePendingIntent);
			startUpdateService(pollRate);
		} else {
			mBuilder.addAction(new Notification.Action(android.R.drawable.ic_media_play,
													   "Start",
													   startPendingIntent));
			if (updateHandler != null) {
				updateHandler.cancel(true);
			}
		}
        mBuilder.addAction(new Notification.Action(R.drawable.ic_action_cancel,
												   "Exit",
												   exitPendingIntent));
		notification = mBuilder.build();
		startForeground(mNotificationId, notification);

	}

    private final IBinder mBinder = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("DataTrackerService", "onBind");
		return mBinder;
    }

    public class LocalBinder extends Binder {
        public DataTrackerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return DataTrackerService.this;
        }
    }

    @Override
    public void onDestroy() {
        Log.d("DataTrackerService", "onDestroy " + this);
        try {
            updateHandler.cancel(true);
			//mNotifyMgr.cancel(mNotificationId);
        } catch (NullPointerException e) {
            //The only way there will be a null pointer, is if the disabled preference is checked.  
			//Because if it is, onDestory() is called right away, without creating the updateHandler
        }
        super.onDestroy();
    }

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("DataTrackerService", "onStartCommand " + intent + ", " + this);
        Bundle extras = null;
        if (intent != null) {
            extras = intent.getExtras();
        }
		command = "";
		if (extras != null) {
            unitType = extras.getInt("unitType", 4);
			pollRate = extras.getInt("pollRate", 5);
			command = extras.getString("command");
            //newAppUid = extras.getInt("EXTRA_UID");
        }
		createService(this);
		return START_STICKY;
    }

    public void startUpdateService(long pollRate) {
        final Runnable updater = new Runnable() {
            public void run() {
//                if (!cm.getActiveNetworkInfo().isConnected()) {
//					return;
//				}
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					if (!pm.isInteractive()) {
						return;
					}
				} else if (!pm.isScreenOn()) {
					return;
				}
				updateAppList();
				initiateUpdate();
            }
        };
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        updateHandler = scheduler.scheduleAtFixedRate(updater, 0, pollRate, TimeUnit.SECONDS);
    }

	private void updateAppList() {

		final List<ApplicationInfo> applications = pk.getInstalledApplications(PackageManager.GET_META_DATA);
		AppStats appStats;

		int i = 0;

		long rx = 0;
		long tx = 0;
		totalRxSincePrev = 0;
		totalTxSincePrev = 0;
		long elapsedRealtimeNanos = 0;
		appStatsList.clear();
		for (ApplicationInfo app : applications) {
            try {
                appStats = new AppStats(app.loadLabel(pk), app.packageName, app.uid);

				rx = TrafficStats.getUidRxBytes(app.uid);
				tx = TrafficStats.getUidTxBytes(app.uid);
				elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos();

				if ((i = prevAppStatsList.indexOf(appStats)) >= 0) {
					appStats = prevAppStatsList.get(i);

					appStats.elapsedTimeSincePrev = elapsedRealtimeNanos - appStats.elapsedTimeSinceStart;
					appStats.elapsedTimeSinceStart = elapsedRealtimeNanos;

					appStats.bytesRxSincePrev = rx - appStats.bytesRxSinceStart;
					appStats.bytesTxSincePrev = tx - appStats.bytesTxSinceStart;

					appStats.idle = appStats.bytesRxSincePrev == 0 && appStats.bytesTxSincePrev == 0;
					if (!appStats.idle) {
						totalRxSincePrev += appStats.bytesRxSincePrev;
						totalTxSincePrev += appStats.bytesTxSincePrev;
//						rxRate += appStats.bytesRxSincePrev * 1000000 / (appStats.elapsedTimeSincePrev / 1000);
//						txRate += appStats.bytesTxSincePrev * 1000000 / (appStats.elapsedTimeSincePrev / 1000);
						appStatsList.add(appStats);
					} 
					appStats.bytesRxSinceStart = rx;
					appStats.bytesTxSinceStart = tx;

				} else {
					if (rx > 0 || tx > 0) {
						appStats.idle = false;
						appStats.bytesRxSinceStart = rx;
						appStats.bytesTxSinceStart = tx;

						appStats.bytesRxSincePrev = rx;
						appStats.bytesTxSincePrev = tx;

						totalRxSincePrev += rx;
						totalTxSincePrev += tx;

						appStatsList.add(appStats);
					} 
					prevAppStatsList.add(appStats);

					appStats.elapsedTimeSincePrev = elapsedRealtimeNanos;
					appStats.elapsedTimeSinceStart = elapsedRealtimeNanos;
				}
            } catch (Resources.NotFoundException e) {
            }
        }

		prevElapsedTime = elapsedRealtimeNanos - startTime;
		rateTotalRxSincePrev = totalRxSincePrev * 1000000 / (prevElapsedTime / 1000);
		rateTotalTxSincePrev = totalTxSincePrev * 1000000 / (prevElapsedTime / 1000);
		if (unitType > 3) {
			rateTotalRxSincePrev = (rateTotalRxSincePrev >> (10 * (unitType - 4)));
			rateTotalTxSincePrev = (rateTotalTxSincePrev >> (10 * (unitType - 4)));
		} else {
			rateTotalRxSincePrev = ((rateTotalRxSincePrev >> (10 * unitType)) << 3);
			rateTotalTxSincePrev = ((rateTotalTxSincePrev >> (10 * unitType)) << 3);
		}
		startTime = elapsedRealtimeNanos;
	}
	
	private long rateTotalRxSincePrev;
	private long rateTotalTxSincePrev;
	
    private synchronized void initiateUpdate() {
		mBuilder.setContentTitle("Data traffic");
//		String.format("↓%s@%s%s, ↑%s@%s%s, ⇅%s@%s%s", 
//											   Formatter.formatFileSize(getApplicationContext(), totalRxSincePrev), 
//											   Util.nf.format(rateTotalRxSincePrev), 
//											   unitTypeArr[unitType], 
//											   Formatter.formatFileSize(getApplicationContext(), totalTxSincePrev), 
//											   Util.nf.format(rateTotalTxSincePrev), 
//											   unitTypeArr[unitType],
//											   Formatter.formatFileSize(getApplicationContext(), (totalRxSincePrev + totalTxSincePrev)),
//											   Util.nf.format(rateTotalRxSincePrev + rateTotalTxSincePrev),
//											   unitTypeArr[unitType]));
		if (rateTotalTxSincePrev == 0 && rateTotalRxSincePrev == 0) {
			mBuilder.setSmallIcon(android.R.drawable.ic_menu_more);
		} else if (rateTotalTxSincePrev == 0 && rateTotalRxSincePrev > 0) {
			mBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
		} else if (rateTotalTxSincePrev > 0 && rateTotalRxSincePrev == 0) {
			mBuilder.setSmallIcon(android.R.drawable.stat_sys_upload);
		} else if (rateTotalTxSincePrev > 0 && rateTotalRxSincePrev > 0) {//1307 bytes is equal to .1Mbit
			mBuilder.setSmallIcon(android.R.drawable.ic_notification_overlay);
		}
		
		final Notification.InboxStyle big = new Notification.InboxStyle();
		big.addLine(String.format("↓%s@%s%s", 
								  Formatter.formatFileSize(getApplicationContext(), totalRxSincePrev), 
								  Util.nf.format(rateTotalRxSincePrev), 
								  unitTypeArr[unitType]));
		big.addLine(String.format("↑%s@%s%s", 
								  Formatter.formatFileSize(getApplicationContext(), totalTxSincePrev), 
								  Util.nf.format(rateTotalTxSincePrev), 
								  unitTypeArr[unitType]));
		big.addLine(String.format("⇅%s@%s%s", 
								  Formatter.formatFileSize(getApplicationContext(), (totalRxSincePrev + totalTxSincePrev)),
								  Util.nf.format(rateTotalRxSincePrev + rateTotalTxSincePrev),
								  unitTypeArr[unitType]));
//		long rateRxSincePrev;
//		long rateTxSincePrev;
//		final Context applicationContext = getApplicationContext();
//		final int gap = pollRate * 1000 / appStatsList.size();
//		for (AppStats appStat : appStatsList) {
//			rateRxSincePrev = appStat.bytesRxSincePrev * 1000000 / (appStat.elapsedTimeSincePrev / 1000);
//			rateTxSincePrev = appStat.bytesTxSincePrev * 1000000 / (appStat.elapsedTimeSincePrev / 1000);
//			if (unitType > 3) {
//				rateRxSincePrev = (rateRxSincePrev >> (10 * (unitType - 4)));
//				rateTxSincePrev = (rateTxSincePrev >> (10 * (unitType - 4)));
//			} else {
//				rateRxSincePrev = ((rateRxSincePrev >> (10 * unitType)) << 3);
//				rateTxSincePrev = ((rateTxSincePrev >> (10 * unitType)) << 3);
//			}
//			big.addLine(String.format("%s\n↓%s@%s%s, ↑%s@%s%s, ⇅%s@%s%s", 
//									  appStat.name,
//									  Formatter.formatFileSize(applicationContext, appStat.bytesRxSincePrev), 
//									  Util.nf.format(rateRxSincePrev), 
//									  unitTypeArr[unitType], 
//									  Formatter.formatFileSize(applicationContext, appStat.bytesTxSincePrev), 
//									  Util.nf.format(rateTxSincePrev), 
//									  unitTypeArr[unitType],
//									  Formatter.formatFileSize(applicationContext, (appStat.bytesRxSincePrev + appStat.bytesTxSincePrev)),
//									  Util.nf.format(rateRxSincePrev + rateTxSincePrev),
//									  unitTypeArr[unitType]));
//			try {
//				Thread.sleep(gap);
//			} catch (InterruptedException e) {}
//		}
		mNotifyMgr.notify(mNotificationId, mBuilder.setStyle(big).build());
    }

}
