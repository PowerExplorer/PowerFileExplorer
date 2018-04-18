package net.gnu.explorer;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.TrafficStats;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.widget.TextView;
import android.os.Handler;
import android.os.SystemClock;
import java.util.concurrent.TimeUnit;
import android.text.format.Formatter;
import android.content.Context;
import android.app.Activity;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import java.util.HashSet;
import android.util.Log;
import android.support.v7.view.menu.MenuBuilder;
import android.view.MenuInflater;
import android.support.v7.view.menu.MenuPopupHelper;
import android.widget.Toast;
import android.app.AlertDialog;
import android.os.Build;
import java.io.File;
import android.net.Uri;
import android.view.ViewGroup;
import java.util.LinkedList;
import android.view.MenuItem;
import android.view.LayoutInflater;
import net.gnu.androidutil.AndroidUtils;
import android.graphics.Color;
import com.amaze.filemanager.utils.files.Futils;
import net.gnu.util.Util;
import com.amaze.filemanager.utils.PreferenceUtils;
import android.widget.AbsListView;
import android.view.animation.AnimationUtils;
import android.preference.PreferenceManager;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.content.pm.PackageManager.NameNotFoundException;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.os.Parcel;
import android.widget.Adapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ToggleButton;
import java.util.Comparator;
import android.util.TypedValue;
import android.view.Gravity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.GridLayoutManager;

public class DataTrackerFrag extends FileFrag implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

	private static final String TAG = "DataTrackerFrag";

	private final ArrayList<AppStats> appStatsList = new ArrayList<>();
	private AppsAdapter appStatAdapter;

	private final ArrayList<AppStats> prevAppStatsList = new ArrayList<>();
	private final HashSet<AppStats> myChecked = new HashSet<>();
//	private ListView listView;

	private final Handler handler = new Handler();
	private PackageManager pk;

	private long totalRxSincePrev = 0;
	private long totalTxSincePrev = 0;

	private long startTime = 0;//SystemClock.elapsedRealtimeNanos();
	private long prevElapsedTime;

	private TextView totalTransferTV;
	private TextView appStatus;
	private TextView interval;
	private TextView unit;
	private Drawable apkDrawable;
	private ToggleButton enabled;
	private ToggleButton totalBtn;

	private boolean totalOn = false;;

//	private int networkType = 1;
//	private Spinner networkSpinner;
//	private final String[] networkTypeArr = new String[] {
//		"All",
//		"Mobile",
//		"Wifi"
//	};

	private int statusType = 1;
	private Spinner statusSpinner;
	private final String[] statusTypeArr = new String[] {
		"All",
		"Active",
		"Idle"
	};

	private int intervalType = 5;
	private Spinner intervalSpinner;
	private final String[] intervalTypeArr = new String[] {
		"1",
		"2",
		"3",
		"4",
		"5",
		"6",
		"7",
		"8",
		"9",
		"10",
	};

	private int unitType = 1;
	private Spinner unitSpinner;
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

	private AppStatsComparator appStatsComparator;
	private String serviceCurrentStatus = "pause";

	private final Runnable runnable = new Runnable() {
        @Override
        public synchronized void run() {
            updateAppList();
			if (mSwipeRefreshLayout.isRefreshing()) {
				mSwipeRefreshLayout.setRefreshing(false);
			}
			if (appStatsList.size() == 0) {
				nofilelayout.setVisibility(View.VISIBLE);
				mSwipeRefreshLayout.setVisibility(View.GONE);
			} else {
				nofilelayout.setVisibility(View.GONE);
				mSwipeRefreshLayout.setVisibility(View.VISIBLE);
			}
			if (enabled.isChecked()) {
				handler.removeCallbacks(runnable);
				handler.postDelayed(runnable, intervalType * 1000);
			}
        }
    };

	public DataTrackerFrag() {
		super();
		type = Frag.TYPE.TRAFFIC_STATS;
		title = "Traffic";
	}

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.pager_item_net, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState) {
		super.onViewCreated(v, savedInstanceState);
		Log.d(TAG, "onViewCreated");

		pk = activity.getPackageManager();
		apkDrawable = getResources().getDrawable(R.drawable.ic_doc_apk);
		totalTransferTV = (TextView)v.findViewById(R.id.netStatus);

		allCbx = (ImageButton) v.findViewById(R.id.allCbx);
		icons = (ImageButton) v.findViewById(R.id.icons);
		allName = (TextView) v.findViewById(R.id.allName);
		allDate = (TextView) v.findViewById(R.id.allDate);
		allSize = (TextView) v.findViewById(R.id.allSize);
		allType = (TextView) v.findViewById(R.id.allType);
		enabled = (ToggleButton) v.findViewById(R.id.enabled);
		totalBtn = (ToggleButton) v.findViewById(R.id.totalBtn);

		//networkSpinner = (Spinner) v.findViewById(R.id.networkType);
		statusSpinner = (Spinner) v.findViewById(R.id.statusType);
		intervalSpinner = (Spinner) v.findViewById(R.id.intervalType);
		unitSpinner = (Spinner) v.findViewById(R.id.unitType);

		appStatus = (TextView) v.findViewById(R.id.appStatus);
		interval = (TextView) v.findViewById(R.id.interval);
		unit = (TextView) v.findViewById(R.id.unit);

		allCbx.setOnClickListener(this);
		icons.setOnClickListener(this);
		allName.setOnClickListener(this);
		allDate.setOnClickListener(this);
		allSize.setOnClickListener(this);
		allType.setOnClickListener(this);
		enabled.setOnClickListener(this);
		totalBtn.setOnClickListener(this);

		mSwipeRefreshLayout.setOnRefreshListener(this);
		totalOn = totalBtn.isChecked();

//		final ArrayAdapter<String> networkSpinnerAdapter = new ArrayAdapter<String>(
//			activity, android.R.layout.simple_spinner_item, statusTypeArr);
//		networkSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//		networkSpinner.setAdapter(networkSpinnerAdapter);
//		networkSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
//
//				@Override
//				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//					networkType = position;
//					handler.removeCallbacks(runnable);
//					handler.postDelayed(runnable, 0);
//				}
//
//				@Override
//				public void onNothingSelected(AdapterView<?> p1) {
//				}
//			
//		});
//		networkSpinner.setSelection(0);

		final ArrayAdapter<String> statusSpinnerAdapter = new SpinnerAdapter(
			activity, android.R.layout.simple_spinner_item, statusTypeArr);
		statusSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		statusSpinner.setAdapter(statusSpinnerAdapter);
		statusSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					statusType = position;
					if (enabled.isChecked()) {
						synchronized (runnable) {
							handler.removeCallbacks(runnable);
							handler.postDelayed(runnable, 50);
						}
					}
				}
				@Override
				public void onNothingSelected(AdapterView<?> p1) {
				}
			});
		statusSpinner.setSelection(0);

		final ArrayAdapter<String> intervalSpinnerAdapter = new SpinnerAdapter(
			activity, android.R.layout.simple_spinner_item, intervalTypeArr);
		intervalSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		intervalSpinner.setAdapter(intervalSpinnerAdapter);
		intervalSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					intervalType = Integer.valueOf(intervalTypeArr[position]);
					if (enabled.isChecked()) {
						final Intent intent = new Intent(activity, DataTrackerService.class); //getApp
						intent.putExtra("pollRate", intervalType);
						intent.putExtra("unitType", unitType);
						intent.putExtra("command", serviceCurrentStatus);
						synchronized (runnable) {
							//activity.stopService(intent);
							activity.startService(intent);
							handler.removeCallbacks(runnable);
							handler.postDelayed(runnable, 50);
						}
					}
				}
				@Override
				public void onNothingSelected(AdapterView<?> p1) {
				}
			});
		intervalSpinner.setSelection(4);

		final ArrayAdapter<String> unitSpinnerAdapter = new SpinnerAdapter(
			activity, android.R.layout.simple_spinner_item, unitTypeArr);
		unitSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		unitSpinner.setAdapter(unitSpinnerAdapter);
		unitSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					unitType = position;
					if (enabled.isChecked()) {
						final Intent intent = new Intent(activity, DataTrackerService.class); //getApp
						intent.putExtra("pollRate", intervalType);
						intent.putExtra("unitType", unitType);
						intent.putExtra("command", serviceCurrentStatus);
						synchronized (runnable) {
							//activity.stopService(intent);
							activity.startService(intent);
							handler.removeCallbacks(runnable);
							handler.postDelayed(runnable, 50);
						}
					}
				}
				@Override
				public void onNothingSelected(AdapterView<?> p1) {
				}
			});
		unitSpinner.setSelection(4);

		appStatAdapter = new AppsAdapter(new ArrayList<AppStats>());

//		listView = (ListView) v.findViewById(R.id.listView1);
		//listView.setFastScrollEnabled(true);

		listView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		listView.setAdapter(appStatAdapter);
		spanCount = AndroidUtils.getSharedPreference(getContext(), "SPAN_COUNT.DataTrackerFrag", 1);
		gridLayoutManager = new GridLayoutManager(fragActivity, spanCount);
		listView.setLayoutManager(gridLayoutManager);
		if (spanCount <= 2) {
			dividerItemDecoration = new GridDividerItemDecoration(fragActivity, true);
			//dividerItemDecoration = new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST, true, true);
			listView.addItemDecoration(dividerItemDecoration);
		}
		//runnable.run();//loadlist(getContext());
		listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
				@Override
				public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
					//Log.d(TAG, "onScroll firstVisibleItem=" + firstVisibleItem + ", visibleItemCount=" + visibleItemCount + ", totalItemCount=" + totalItemCount);
					if (System.currentTimeMillis() - lastScroll > 50) {//!mScaling && 
						if (dy > activity.density << 4 && selStatus.getVisibility() == View.VISIBLE) {
							selStatus.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_bottom));
							selStatus.setVisibility(View.GONE);
							horizontalDivider0.setVisibility(View.GONE);
							horizontalDivider12.setVisibility(View.GONE);
							status.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_bottom));
							status.setVisibility(View.GONE);
						} else if (dy < -activity.density << 4 && selStatus.getVisibility() == View.GONE) {
							selStatus.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
							selStatus.setVisibility(View.VISIBLE);
							horizontalDivider0.setVisibility(View.VISIBLE);
							horizontalDivider12.setVisibility(View.VISIBLE);
							status.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
							status.setVisibility(View.VISIBLE);
						}
						lastScroll = System.currentTimeMillis();
					}
				}});
		final Bundle args = getArguments();
		
		if (args != null) {
			title = args.getString("title");
		}

		allName.setText("Name");
		allType.setText("Download");
		allDate.setText("Upload");
		allSize.setText("Total");
		final String order = AndroidUtils.getSharedPreference(activity, "AppStats.order", "Name ▲");

		switch (order) {
			case "Name ▼":
				appStatsComparator = new AppStatsComparator(AppStatsComparator.BY_NAME, AppStatsComparator.DESC, totalOn);
				allName.setText("Name ▼");
				break;
			case "Name ▲":
				appStatsComparator = new AppStatsComparator(AppStatsComparator.BY_NAME, AppStatsComparator.ASC, totalOn);
				allName.setText("Name ▲");
				break;
			case "Download ▼":
				appStatsComparator = new AppStatsComparator(AppStatsComparator.BY_DOWNLOAD, AppStatsComparator.DESC, totalOn);
				allType.setText("Download ▼");
				break;
			case "Download ▲":
				appStatsComparator = new AppStatsComparator(AppStatsComparator.BY_DOWNLOAD, AppStatsComparator.ASC, totalOn);
				allType.setText("Download ▲");
				break;
			case "Upload ▼":
				appStatsComparator = new AppStatsComparator(AppStatsComparator.BY_UPLOAD, AppStatsComparator.DESC, totalOn);
				allDate.setText("Upload ▼");
				break;
			case "Upload ▲":
				appStatsComparator = new AppStatsComparator(AppStatsComparator.BY_UPLOAD, AppStatsComparator.ASC, totalOn);
				allDate.setText("Upload ▲");
				break;
			case "Total ▼":
				appStatsComparator = new AppStatsComparator(AppStatsComparator.BY_TOTAL, AppStatsComparator.DESC, totalOn);
				allSize.setText("Total ▼");
				break;
			case "Total ▲":
				appStatsComparator = new AppStatsComparator(AppStatsComparator.BY_TOTAL, AppStatsComparator.ASC, totalOn);
				allSize.setText("Total ▲");
				break;
		}
		updateColor(null);
	}

	@Override
	public void onRefresh() {
		synchronized (runnable) {
			handler.removeCallbacks(runnable);
			handler.post(runnable);
		}
	}
	
	public void refreshRecyclerViewLayoutManager() {

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.allCbx:
				v.setSelected(!v.isSelected());
				appStatAdapter.toggleChecked(v.isSelected());
				break;
			case R.id.enabled:
				synchronized (runnable) {
					final Intent intent = new Intent(activity, DataTrackerService.class); //getApp
					intent.putExtra("pollRate", intervalType);
					intent.putExtra("unitType", unitType);
					if (enabled.isChecked()) {
						serviceCurrentStatus = "start";
						intent.putExtra("command", serviceCurrentStatus);
						activity.startService(intent);
						handler.removeCallbacks(runnable);
						handler.post(runnable);
					} else {
						serviceCurrentStatus = "pause";
						intent.putExtra("command", serviceCurrentStatus);
						activity.startService(intent);
						nofilelayout.setVisibility(View.VISIBLE);
						mSwipeRefreshLayout.setVisibility(View.GONE);
						handler.removeCallbacks(runnable);
					}
				}
				break;
			case R.id.totalBtn:
				totalOn = totalBtn.isChecked();
				appStatsComparator.totalOn = totalOn;
				if (enabled.isChecked()) {
					synchronized (runnable) {
						handler.removeCallbacks(runnable);
						handler.post(runnable);
					} 
				}
				break;
			case R.id.allName:
				if (allName.getText().toString().equals("Name ▲")) {
					allName.setText("Name ▼");
					appStatsComparator = new AppStatsComparator(AppStatsComparator.BY_NAME, AppStatsComparator.DESC, totalOn);
					AndroidUtils.setSharedPreference(getContext(), "AppStats.order", "Name ▼");
				} else {
					allName.setText("Name ▲");
					appStatsComparator = new AppStatsComparator(AppStatsComparator.BY_NAME, AppStatsComparator.ASC, totalOn);
					AndroidUtils.setSharedPreference(getContext(), "AppStats.order", "Name ▲");
				}
				Collections.sort(appStatsList, appStatsComparator);
				appStatAdapter.notifyDataSetChanged();
				allType.setText("Download");
				allDate.setText("Upload");
				allSize.setText("Total");
				break;
			case R.id.allType:
				if (allType.getText().toString().equals("Download ▲")) {
					allType.setText("Download ▼");
					appStatsComparator = new AppStatsComparator(AppStatsComparator.BY_DOWNLOAD, AppStatsComparator.DESC, totalOn);
					AndroidUtils.setSharedPreference(getContext(), "AppStats.order", "Download ▼");
				} else {
					allType.setText("Download ▲");
					appStatsComparator = new AppStatsComparator(AppStatsComparator.BY_DOWNLOAD, AppStatsComparator.ASC, totalOn);
					AndroidUtils.setSharedPreference(getContext(), "AppStats.order", "Download ▲");
				}
				Collections.sort(appStatsList, appStatsComparator);
				appStatAdapter.notifyDataSetChanged();
				allName.setText("Name");
				allDate.setText("Upload");
				allSize.setText("Total");
				break;
			case R.id.allDate:
				if (allDate.getText().toString().equals("Upload ▲")) {
					allDate.setText("Upload ▼");
					appStatsComparator = new AppStatsComparator(AppStatsComparator.BY_UPLOAD, AppStatsComparator.DESC, totalOn);
					AndroidUtils.setSharedPreference(getContext(), "AppStats.order", "Upload ▼");
				} else {
					allDate.setText("Upload ▲");
					appStatsComparator = new AppStatsComparator(AppStatsComparator.BY_UPLOAD, AppStatsComparator.ASC, totalOn);
					AndroidUtils.setSharedPreference(getContext(), "AppStats.order", "Upload ▲");
				}
				Collections.sort(appStatsList, appStatsComparator);
				appStatAdapter.notifyDataSetChanged();
				allName.setText("Name");
				allType.setText("Download");
				allSize.setText("Total");
				break;
			case R.id.allSize:
				if (allSize.getText().toString().equals("Total ▲")) {
					allSize.setText("Total ▼");
					appStatsComparator = new AppStatsComparator(AppStatsComparator.BY_TOTAL, AppStatsComparator.DESC, totalOn);
					AndroidUtils.setSharedPreference(getContext(), "AppStats.order", "Total ▼");
				} else {
					allSize.setText("Total ▲");
					appStatsComparator = new AppStatsComparator(AppStatsComparator.BY_TOTAL, AppStatsComparator.ASC, totalOn);
					AndroidUtils.setSharedPreference(getContext(), "AppStats.order", "Total ▲");
				}
				Collections.sort(appStatsList, appStatsComparator);
				appStatAdapter.notifyDataSetChanged();
				allName.setText("Name");
				allType.setText("Download");
				allDate.setText("Upload");
				break;

		}
	}

	@Override
	public void updateColor(View rootView) {
		getView().setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
		icons.setColorFilter(ExplorerActivity.TEXT_COLOR);
		allName.setTextColor(ExplorerActivity.TEXT_COLOR);
		allDate.setTextColor(ExplorerActivity.TEXT_COLOR);
		allSize.setTextColor(ExplorerActivity.TEXT_COLOR);
		allType.setTextColor(ExplorerActivity.TEXT_COLOR);
		selectionStatus1.setTextColor(ExplorerActivity.TEXT_COLOR);
		totalTransferTV.setTextColor(ExplorerActivity.TEXT_COLOR);
		appStatus.setTextColor(ExplorerActivity.TEXT_COLOR);
		interval.setTextColor(ExplorerActivity.TEXT_COLOR);
		unit.setTextColor(ExplorerActivity.TEXT_COLOR);

		if (ExplorerActivity.BASE_BACKGROUND < 0xff808080) {
//			networkSpinner.setPopupBackgroundResource(R.drawable.textfield_black);
			statusSpinner.setPopupBackgroundResource(R.drawable.textfield_black);
			intervalSpinner.setPopupBackgroundResource(R.drawable.textfield_black);
			unitSpinner.setPopupBackgroundResource(R.drawable.textfield_black);
		} else {
//			networkSpinner.setPopupBackgroundResource(R.drawable.textfield_default_old);
			statusSpinner.setPopupBackgroundResource(R.drawable.textfield_default_old);
			intervalSpinner.setPopupBackgroundResource(R.drawable.textfield_default_old);
			unitSpinner.setPopupBackgroundResource(R.drawable.textfield_default_old);
		}

		horizontalDivider0.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
		horizontalDivider12.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
		horizontalDivider7.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
		noFileText.setTextColor(ExplorerActivity.TEXT_COLOR);
		noFileImage.setColorFilter(ExplorerActivity.TEXT_COLOR);
	}

	private void update_labels() {
		if (totalOn) {
			long totalRxBytes = TrafficStats.getTotalRxBytes();
			long totalTxBytes = TrafficStats.getTotalTxBytes();
			totalTransferTV.setText(String.format("Total:↓%s, ↑%s, ⇅%s", //↑↓▲▼⬆⬇⬍
												  Formatter.formatFileSize(activity, totalRxBytes),// + "@" + rxRate + unitTypeArr[unitType],
												  Formatter.formatFileSize(activity, totalTxBytes),// + "@" + txRate + unitTypeArr[unitType],
												  Formatter.formatFileSize(activity, totalRxBytes + totalTxBytes)// + "@" + totalRate + unitTypeArr[unitType],
												  ));
		} else {
			long rxRate = totalRxSincePrev * 1000000 / (prevElapsedTime / 1000);
			long txRate = totalTxSincePrev * 1000000 / (prevElapsedTime / 1000);
			long totalRate = (totalRxSincePrev + totalTxSincePrev) * 1000000 / (prevElapsedTime / 1000);
			if (unitType > 3) {
				rxRate = (rxRate >> (10 * (unitType - 4)));
				txRate = (txRate >> (10 * (unitType - 4)));
				totalRate = (totalRate >> (10 * (unitType - 4)));
			} else {
				rxRate = ((rxRate >> (10 * unitType)) << 3);
				txRate = ((txRate >> (10 * unitType)) << 3);
				totalRate = ((totalRate >> (10 * unitType)) << 3);
			}

			totalTransferTV.setText(String.format("Current:↓%s, ↑%s, ⇅%s", //↑↓▲▼⬆⬇⬍
												  Formatter.formatFileSize(activity, totalRxSincePrev) + "@" + rxRate + unitTypeArr[unitType],
												  Formatter.formatFileSize(activity, totalTxSincePrev) + "@" + txRate + unitTypeArr[unitType],
												  Formatter.formatFileSize(activity, totalRxSincePrev + totalTxSincePrev) + "@" + totalRate + unitTypeArr[unitType]
												  ));
		}
		selectionStatus1.setText(myChecked.size() + "/" + appStatsList.size());
	}

	private void updateAppList() {

		final List<ApplicationInfo> applications = pk.getInstalledApplications(PackageManager.GET_META_DATA);
		AppStats appStats;

		int i = 0;

		long rx = 0;
		long tx = 0;
//		rxRate = 0;
//		txRate = 0;
		totalRxSincePrev = 0;
		totalTxSincePrev = 0;
		long elapsedRealtimeNanos = 0;
		final LinkedList<AppStats> tempAppStatsList = new LinkedList<>();
		for (ApplicationInfo app : applications) {
            try {
                //appName = app.loadLabel(pk);
                appStats = new AppStats(app.loadLabel(pk), app.packageName, app.uid);

//				if (networkType == 1) {
//					rx = TrafficStats.getUidRxBytes(app.uid);
//					tx = TrafficStats.getUidTxBytes(app.uid);
//				} else if (networkType == 2) {
//					rx = TrafficStats.getUidRxBytes(app.uid);
//					tx = TrafficStats.getUidTxBytes(app.uid);
//				} else if (networkType == 0) {
				rx = TrafficStats.getUidRxBytes(app.uid);
				tx = TrafficStats.getUidTxBytes(app.uid);
				elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos();
				//}
				//Log.d(TAG, SystemClock.elapsedRealtimeNanos() + ", " + rx + ", " + tx);
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
						if (statusType != 2) {
							tempAppStatsList.add(appStats);
						}
					} else if (statusType != 1 && (appStats.bytesRxSinceStart > 0 || appStats.bytesTxSinceStart > 0)) {
						tempAppStatsList.add(appStats);
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

						if (statusType != 2) {
							tempAppStatsList.add(appStats);
						}
					} 
					prevAppStatsList.add(appStats);

					appStats.elapsedTimeSincePrev = elapsedRealtimeNanos;
					appStats.elapsedTimeSinceStart = elapsedRealtimeNanos;
				}
            } catch (Resources.NotFoundException e) {
            }
        }

		prevElapsedTime = elapsedRealtimeNanos - startTime;
		startTime = elapsedRealtimeNanos;
        Collections.sort(tempAppStatsList, appStatsComparator);
		appStatAdapter.clear();
		appStatAdapter.addAll(tempAppStatsList);

		appStatAdapter.notifyDataSetChanged();
		update_labels();
	}

	class AppsAdapter extends RecyclerAdapter<AppStats, AppsAdapter.ViewHolder> implements OnClickListener, OnLongClickListener {
		private static final String TAG = "AppsAdapter";

		class ViewHolder extends RecyclerView.ViewHolder {
			View ll;
			TextView name;
			TextView items;
			TextView attr;
			TextView lastModified;
			TextView type;
			ImageButton cbx;
			ImageView image;
			ImageButton more;

			ViewHolder(View convertView) {
				super(convertView);
				name = (TextView) convertView.findViewById(R.id.name);
				items = (TextView) convertView.findViewById(R.id.items);
				attr = (TextView) convertView.findViewById(R.id.attr);
				lastModified = (TextView) convertView.findViewById(R.id.lastModified);
				type = (TextView) convertView.findViewById(R.id.type);
				cbx = (ImageButton) convertView.findViewById(R.id.cbx);
				image = (ImageView)convertView.findViewById(R.id.icon);
				more = (ImageButton)convertView.findViewById(R.id.more);
				convertView.setTag(this);
				ll = convertView;

				ll.setOnClickListener(AppsAdapter.this);
				more.setOnClickListener(AppsAdapter.this);
				cbx.setOnClickListener(AppsAdapter.this);

				ll.setOnLongClickListener(AppsAdapter.this);
				more.setOnLongClickListener(AppsAdapter.this);
				cbx.setOnLongClickListener(AppsAdapter.this);
			}
		}
		
		public AppsAdapter(final ArrayList<AppStats> objects) {
			super(objects);
		}

		public void toggleChecked(boolean checked, AppStats packageInfo) {
			if (checked) {
				myChecked.add(packageInfo);
			} else {
				myChecked.remove(packageInfo);
			}
			notifyDataSetChanged();
			update_labels();
			final boolean all = myChecked.size() == appStatsList.size();
			allCbx.setSelected(all);
			if (all) {
				allCbx.setImageResource(R.drawable.ic_accept);
			} else if (myChecked.size() > 0) {
				allCbx.setImageResource(R.drawable.ready);
			} else {
				allCbx.setImageResource(R.drawable.dot);
			}
			if (myChecked.size() > 0) {
				if (commands.getVisibility() == View.GONE) {
					commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
					commands.setVisibility(View.VISIBLE);
					horizontalDivider6.setVisibility(View.VISIBLE);
				}
			} else if (commands.getVisibility() == View.VISIBLE) {
				horizontalDivider6.setVisibility(View.GONE);
				commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
				commands.setVisibility(View.GONE);
			}
		}

		public void toggleChecked(boolean b) {
			allCbx.setSelected(b);
			if (b) {
				myChecked.clear();
				myChecked.addAll(appStatsList);
				allCbx.setImageResource(R.drawable.ic_accept);
				if (myChecked.size() > 0 && commands.getVisibility() == View.GONE) {
					commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
					commands.setVisibility(View.VISIBLE);
					horizontalDivider6.setVisibility(View.VISIBLE);
				}
			} else {
				myChecked.clear();
				allCbx.setImageResource(R.drawable.dot);
				if (commands.getVisibility() == View.VISIBLE) {
					horizontalDivider6.setVisibility(View.GONE);
					commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
					commands.setVisibility(View.GONE);
				}
			}
			notifyDataSetChanged();
		}

		@Override
		public boolean onLongClick(final View view) {
			final Object tag = view.getTag();
			view.setSelected(!view.isSelected());
			AppStats appStats;
			if (tag instanceof AppStats) {
				appStats = (AppStats) tag;
			} else {
				appStats = (AppStats) ((ViewHolder) tag).cbx.getTag();
			}
			toggleChecked(!myChecked.contains(appStats), appStats);
			return false;
		}

		@Override
		public void onClick(final View view) {
			Log.d(TAG, view.getTag() + ".");
			if (myChecked.size() > 0) {
				onLongClick(view);
				return;
			}
			if (view.getId() == R.id.cbx) {
				view.setSelected(!view.isSelected());
				toggleChecked(view.isSelected(), (AppStats) view.getTag());
			} else if (view.getId() == R.id.more) {
				final AppStats pinfo = (AppStats) view.getTag();
				final MenuBuilder menuBuilder = new MenuBuilder(activity);
				final MenuInflater inflater = new MenuInflater(activity);
				inflater.inflate(R.menu.process, menuBuilder);
				final MenuPopupHelper optionsMenu = new MenuPopupHelper(activity , menuBuilder, allSize);
				optionsMenu.setForceShowIcon(true);
				final MenuItem mi = menuBuilder.findItem(R.id.info);
				mi.setVisible(false);
				menuBuilder.setCallback(new MenuBuilder.Callback() {
						@Override
						public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
							switch (item.getItemId()) {
								case R.id.kill:
									try {
										List<ActivityManager.RunningAppProcessInfo> lp = ((ActivityManager)activity.getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
										for (RunningAppProcessInfo ra : lp) {
											if (ra.processName.equals(pinfo.packageName)) {
												AndroidUtils.killProcess(activity, ra.pid, pinfo.packageName);
												break;
											}
										}
										lp = ((ActivityManager)activity.getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
										boolean exist = false;
										for (RunningAppProcessInfo ra : lp) {
											if (ra.processName.equals(pinfo.packageName)) {
												AndroidUtils.killProcess(activity, ra.pid, pinfo.packageName);
												exist = true;
												break;
											}
										}
										if (exist) {
											showToast(pinfo.packageName + " cannot be killed");
										} else {
											showToast(pinfo.packageName + " was killed");
										}
									} catch (Exception e) {
										Toast.makeText(DataTrackerFrag.this.getContext(), "couldn't kill the process ", Toast.LENGTH_SHORT).show();
									}
									//Toast.makeText(ProcessFragment.this.getContext(), pinfo.label + " was killed !", Toast.LENGTH_SHORT).show();
									update_labels();
									//updateAppList();//loadlist(getContext());
									break;
								case R.id.open:
									Intent i = pk.getLaunchIntentForPackage(pinfo.packageName);
									if (i != null)
										startActivity(i);
									else
										Toast.makeText(DataTrackerFrag.this.getContext(), "Could not launch", Toast.LENGTH_SHORT).show();
									break;
								case R.id.backup:
									final PackageInfo info = AndroidUtils.getPackageInfo(activity, pinfo.packageName);
									if (info != null) {
										ApplicationInfo applicationInfo = info.applicationInfo;
										if (applicationInfo != null) {
											AppsFragment.backup(applicationInfo.publicSourceDir, applicationInfo.loadLabel(activity.getPackageManager()) + "",
																info.versionName, DataTrackerFrag.this);
										} else {
											Toast.makeText(activity, pinfo.packageName + " cannot be accessed", Toast.LENGTH_SHORT).show();
										}
									} else {
										Toast.makeText(activity, pinfo.packageName + " cannot be accessed", Toast.LENGTH_SHORT).show();
									}
									break;
								case R.id.unins:
									try {
										Intent uninstall_intent= new Intent(Intent.ACTION_DELETE);
										uninstall_intent.setData(Uri.parse("package:" + pinfo.packageName));
										startActivity(uninstall_intent);
									} catch (Exception e) {
										Toast.makeText(DataTrackerFrag.this.getContext(), "Can't Uninstall" , Toast.LENGTH_SHORT).show();
									}
									break;
//								case R.id.info:
//									//Toast.makeText(ProcessManager.this, "Process : "+display_process.get(position).processName +" lru : " +display_process.get(position).lru + " Pid :  " +display_process.get(position).pid, Toast.LENGTH_SHORT).show();	
//									final AlertDialog alert1 = new AlertDialog.Builder(DataTranferTrackerFrag.this.getContext()).create();
//									alert1.setTitle("Process Info");
//									alert1.setIcon(AndroidUtils.getProcessIcon(pk, pinfo.packageName, apkDrawable));
//									alert1.setMessage("Process : " + pinfo.packageName + " \nlru : " + pinfo.runningAppProcessInfo.lru + "\nPid : " + pinfo.pid);
//									alert1.show();
//									break;
								case R.id.detail:
									final int apiLevel = Build.VERSION.SDK_INT;
									Intent intent = new Intent();
									if (apiLevel >= 9) {
										startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
																 Uri.parse("package:" + pinfo.packageName)));
									} else {
										final String appPkgName = (apiLevel == 8 ? "pkg" : "com.android.settings.ApplicationPkgName");
										intent.setAction(Intent.ACTION_VIEW);
										intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
										intent.putExtra(appPkgName, pinfo.packageName);
										startActivity(intent);
									}
									break;
								case R.id.play:
									Intent intent1 = new Intent(Intent.ACTION_VIEW);
									intent1.setData(Uri.parse("market://details?id=" + pinfo.packageName));
									activity.startActivity(intent1);
									break;
								case R.id.share:
									ArrayList<File> arrayList2 = new ArrayList<File>();
									final List<PackageInfo> all_apps = pk.getInstalledPackages(PackageManager.GET_META_DATA);
									for (PackageInfo pi : all_apps) {
										if (pi.packageName.equals(pinfo.packageName)) {
											arrayList2.add(new File(pi.applicationInfo.publicSourceDir));
											break;
										}
									}
									//int color1 = Color.parseColor(PreferenceUtils.getAccentString(sharedPref));
									//new Futils().shareFiles(arrayList2, activity, theme1, color1);
									//ArrayList<File> arrayList = new ArrayList<>();
									//arrayList.add(new File(rowItem.getPath()));
									new Futils().shareFiles(arrayList2, activity, activity.getAppTheme(), Color.parseColor(fabSkin));
									break;
							}
							return true;
						}
						@Override
						public void onMenuModeChange(MenuBuilder menu) {}
					});
				optionsMenu.show();
			} else {
				final int apiLevel = Build.VERSION.SDK_INT;
				Intent intent = new Intent();
				final AppStats pinfo = (AppStats) ((ViewHolder) view.getTag()).cbx.getTag();
				if (apiLevel >= 9) {
					startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
											 Uri.parse("package:" + pinfo.packageName)));
				} else {
					final String appPkgName = (apiLevel == 8 ? "pkg" : "com.android.settings.ApplicationPkgName");
					intent.setAction(Intent.ACTION_VIEW);
					intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
					intent.putExtra(appPkgName, pinfo.packageName);
					startActivity(intent);
				}
			}
		}
		
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent,
														int viewType) {
			View v;
			v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_net, parent, false);
			// set the view's size, margins, paddings and layout parameters
			final ViewHolder vh = new ViewHolder(v);
			return vh;
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			final AppStats appStat = appStatsList.get(position);

			holder.cbx.setTag(appStat);
			holder.more.setTag(appStat);

			holder.more.setColorFilter(ExplorerActivity.TEXT_COLOR);
			holder.name.setTextColor(ExplorerActivity.DIR_COLOR);
			holder.items.setTextColor(ExplorerActivity.TEXT_COLOR);
			holder.attr.setTextColor(ExplorerActivity.TEXT_COLOR);
			holder.lastModified.setTextColor(ExplorerActivity.TEXT_COLOR);
			holder.type.setTextColor(ExplorerActivity.TEXT_COLOR);

			if (appStat.idle) {
				holder.name.setTextColor(ExplorerActivity.TEXT_COLOR);
				holder.attr.setTextColor(ExplorerActivity.TEXT_COLOR);
				holder.type.setTextColor(ExplorerActivity.TEXT_COLOR);
				holder.lastModified.setTextColor(ExplorerActivity.TEXT_COLOR);
				holder.items.setTextColor(ExplorerActivity.TEXT_COLOR);
			} else {
				holder.name.setTextColor(Color.RED);
				holder.attr.setTextColor(Color.RED);
				holder.type.setTextColor(Color.RED);
				holder.lastModified.setTextColor(Color.RED);
				holder.items.setTextColor(Color.RED);
			}

			holder.name.setText(appStat.name);
			holder.attr.setText(appStat.packageName);

			final long elapsedTimeSincePrev = appStat.elapsedTimeSincePrev;
			final long bytesRxSincePrev = appStat.bytesRxSincePrev;
			final long bytesTxSincePrev = appStat.bytesTxSincePrev;
			long rateRxSincePrev = 0;
			long rateTxSincePrev = 0;
			long totalRateSincePrev = 0;

			//Log.d(TAG, elapsedTimeSinceStart + ", " + appStat.prevtime + ", " + elapsedTimeSincePrev + ", ");
			rateRxSincePrev = bytesRxSincePrev * 1000000 / (elapsedTimeSincePrev / 1000);
			rateTxSincePrev = bytesTxSincePrev * 1000000 / (elapsedTimeSincePrev / 1000);
			totalRateSincePrev = (bytesRxSincePrev + bytesTxSincePrev) * 1000000 / (elapsedTimeSincePrev / 1000);
			if (unitType > 3) {
				rateRxSincePrev = (rateRxSincePrev >> (10 * (unitType - 4)));
				rateTxSincePrev = (rateTxSincePrev >> (10 * (unitType - 4)));
				totalRateSincePrev = (totalRateSincePrev >> (10 * (unitType - 4)));
			} else {
				rateRxSincePrev = ((rateRxSincePrev >> (10 * unitType)) << 3);
				rateTxSincePrev = ((rateTxSincePrev >> (10 * unitType)) << 3);
				totalRateSincePrev = ((totalRateSincePrev >> (10 * unitType)) << 3);
			}

			if (totalOn) {
				holder.type.setText(Formatter.formatFileSize(activity, appStat.bytesRxSinceStart));
				holder.lastModified.setText(Formatter.formatFileSize(activity, appStat.bytesTxSinceStart));
				holder.items.setText(Formatter.formatFileSize(activity, appStat.bytesRxSinceStart + appStat.bytesTxSinceStart));
			} else {
				holder.type.setText(Formatter.formatFileSize(activity, bytesRxSincePrev) + "@" + rateRxSincePrev + unitTypeArr[unitType]);
				holder.lastModified.setText(Formatter.formatFileSize(activity, bytesTxSincePrev) + "@" + rateTxSincePrev + unitTypeArr[unitType]);
				holder.items.setText(Formatter.formatFileSize(activity, bytesRxSincePrev + bytesTxSincePrev) + "@" + totalRateSincePrev + unitTypeArr[unitType]);
			}

			try {
				holder.image.setImageDrawable(pk.getApplicationIcon(appStat.packageName));
			} catch (NameNotFoundException e) {
				holder.image.setImageResource(R.drawable.ic_doc_apk);
			}

			final boolean checked = myChecked.contains(appStat);
			if (checked) {
				holder.ll.setBackgroundColor(ExplorerActivity.IN_DATA_SOURCE_2);
				holder.cbx.setSelected(true);
				holder.cbx.setImageResource(R.drawable.ic_accept);
			} else if (myChecked.size() > 0) {
				holder.ll.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
				holder.cbx.setSelected(false);
				holder.cbx.setImageResource(R.drawable.ready);
			} else {
				holder.ll.setBackgroundResource(R.drawable.ripple);
				holder.cbx.setSelected(false);
				holder.cbx.setImageResource(R.drawable.dot);
			}
		}
	}
}

class AppStats implements Parcelable {

	final String name;
	final String packageName;
	final int uid;

	long bytesRxSincePrev = 0;
	long bytesTxSincePrev = 0;

	long bytesRxSinceStart = 0;
	long bytesTxSinceStart = 0;

	long elapsedTimeSincePrev = 0;
	long elapsedTimeSinceStart = 0;
	boolean idle = true;

	public AppStats(final CharSequence name, final String packageName, final int uid) {
		this.name = String.valueOf(name);
		this.packageName = packageName;
		this.uid = uid;
	}

	public AppStats(final Parcel im) {
		name = im.readString();
		packageName = im.readString();
		uid = im.readInt();
		bytesRxSincePrev = im.readLong();
		bytesTxSincePrev = im.readLong();
		bytesRxSinceStart = im.readLong();
		bytesTxSinceStart = im.readLong();
		elapsedTimeSincePrev = im.readLong();
		elapsedTimeSinceStart = im.readLong();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel p1, final int p2) {
		p1.writeString(name);
		p1.writeString(packageName);
		p1.writeInt(uid);
		p1.writeLong(bytesRxSincePrev);
		p1.writeLong(bytesTxSincePrev);
		p1.writeLong(bytesRxSinceStart);
		p1.writeLong(bytesTxSinceStart);
		p1.writeLong(elapsedTimeSincePrev);
		p1.writeLong(elapsedTimeSinceStart);
	}

	public static final Parcelable.Creator<AppStats> CREATOR = new Parcelable.Creator<AppStats>() {
		public AppStats createFromParcel(final Parcel in) {
			return new AppStats(in);
		}

		public AppStats[] newArray(final int size) {
			return new AppStats[size];
		}
	};

//	@Override
//	public int compareTo(final Object o) {
//		final AppStats a = (AppStats) o;
//		if (idle && a.idle) {
//			return name.toString().compareTo(a.name.toString());
//		} else if (idle) {
//			return 1;
//		} else if (a.idle) {
//			return -1;
//		} else {
//			return name.toString().compareTo(a.name.toString());
//		}
//	}

	@Override
	public boolean equals(final Object o) {
		final AppStats a = (AppStats) o;
		return uid == a.uid && packageName.equals(a.packageName);
	}

	@Override
	public int hashCode() {
		return packageName.hashCode();
	}
}

class AppStatsComparator implements Comparator<AppStats>  {

	public static final int BY_NAME = 0;
	public static final int BY_DOWNLOAD = 1;
	public static final int BY_UPLOAD = 2;
	public static final int BY_TOTAL = 3;

	public static final int ASC = 1;
	public static final int DESC = -1;

	final int asc;
	final int sort;
	boolean totalOn;

	public AppStatsComparator(final int sort, final int asc, final boolean totalOn) {
		this.asc = asc;
		this.sort = sort;
		this.totalOn = totalOn;
	}

	@Override
	public int compare(final AppStats app1, final AppStats app2) {
		if (sort == BY_NAME) {
			return asc * app1.name.compareToIgnoreCase(app2.name);
		} else if (totalOn) {
			if (sort == BY_DOWNLOAD) {
				return asc * (app1.bytesRxSinceStart < app2.bytesRxSinceStart ? -1 : (app1.bytesRxSinceStart == app2.bytesRxSinceStart ? 0 : 1));
			} else if (sort == BY_UPLOAD) {
				return asc * (app1.bytesTxSinceStart < app2.bytesTxSinceStart ? -1 : (app1.bytesTxSinceStart == app2.bytesTxSinceStart ? 0 : 1));
			} else if (sort == BY_TOTAL) {
				return asc * (app1.bytesRxSinceStart + app1.bytesTxSinceStart < app2.bytesRxSinceStart + app2.bytesTxSinceStart ? -1 : (app1.bytesRxSinceStart + app1.bytesTxSinceStart == app2.bytesRxSinceStart + app2.bytesTxSinceStart ? 0 : 1));
			}
		} else {
			if (sort == BY_DOWNLOAD) {
				return asc * (app1.bytesRxSincePrev < app2.bytesRxSincePrev ? -1 : (app1.bytesRxSincePrev == app2.bytesRxSincePrev ? 0 : 1));
			} else if (sort == BY_UPLOAD) {
				return asc * (app1.bytesTxSincePrev < app2.bytesTxSincePrev ? -1 : (app1.bytesTxSincePrev == app2.bytesTxSincePrev ? 0 : 1));
			} else if (sort == BY_TOTAL) {
				return asc * (app1.bytesRxSincePrev + app1.bytesTxSincePrev < app2.bytesRxSincePrev + app2.bytesTxSincePrev ? -1 : (app1.bytesRxSincePrev + app1.bytesTxSincePrev == app2.bytesRxSincePrev + app2.bytesTxSincePrev ? 0 : 1));
			}
		}
		return 0;
	}
}
