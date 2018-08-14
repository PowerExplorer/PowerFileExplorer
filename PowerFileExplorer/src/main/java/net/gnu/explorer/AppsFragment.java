package net.gnu.explorer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.amaze.filemanager.activities.ThemedActivity;
import com.amaze.filemanager.filesystem.BaseFile;
import com.amaze.filemanager.filesystem.RootHelper;
import com.amaze.filemanager.services.CopyService;
import com.amaze.filemanager.services.DeleteTask;
import com.amaze.filemanager.services.asynctasks.CopyFileCheck;
import com.amaze.filemanager.utils.OpenMode;
import com.amaze.filemanager.utils.ServiceWatcherUtil;
import com.amaze.filemanager.utils.files.Futils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import net.gnu.androidutil.AndroidUtils;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.view.MenuInflater;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask.Status;
import net.gnu.util.Util;
import java.util.HashSet;
import android.graphics.PorterDuff;

public class AppsFragment extends FileFrag implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

	private static final String TAG = "AppsFragment";

	private AppsAdapter appAdapter;
	public static final String BACKUP_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/app_backup";
	private AppListSorter appListSorter;

	private ArrayList<AppInfo> appList = new ArrayList<AppInfo>();
	public int theme1;

	private boolean searchMode = false;
	private String searchVal = "";

	private Spinner appType;
	private SearchFileNameTask searchTask = new SearchFileNameTask();
	//private transient List<AppInfo> tempOriDataSourceL1 = new LinkedList<>();;
	//HashSet<AppInfo> selectedInList1 = new HashSet<AppInfo>();
	//private final ArrayList<AppInfo> tempSelectedInList1 = new ArrayList<>();
	private final String[] appTypeArr = new String[] {
		"All",
		"System App",
		"Updated System App",
		"User App",
		"Internal",
		"External Asec"};

	private LoadAppListTask appLoadTask = new LoadAppListTask(false, 0, 0);
	private int index;
	private int top;
	private TextSearch textSearch = new TextSearch();

	public AppsFragment() {
		super();
		type = Frag.TYPE.APP;
		title = "Apps";
		final File backupPath = new File(BACKUP_PATH);
		if (!backupPath.exists())
			backupPath.mkdirs();
	}

	@Override
	public void clone(final Frag frag, final boolean fake) {
		super.clone(frag, fake);
		if (frag instanceof AppsFragment) {//} && ((AppsFragment)frag).gridLayoutManager != null) {
			final AppsFragment appsFragment = (AppsFragment)frag;
			appList = appsFragment.appList;
			appAdapter = appsFragment.appAdapter;
			//appListSorter = appsFragment.appListSorter;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		//Log.d(TAG, "onCreateView " + savedInstanceState);
		return inflater.inflate(R.layout.pager_item_app, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState) {
		Log.d(TAG, "onViewCreated " + savedInstanceState);
		super.onViewCreated(v, savedInstanceState);

		v.findViewById(R.id.backup).setOnClickListener(this);
		v.findViewById(R.id.unins).setOnClickListener(this);
		v.findViewById(R.id.share).setOnClickListener(this);
		v.findViewById(R.id.shortcuts).setOnClickListener(this);

		appType = (Spinner) v.findViewById(R.id.appType);
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
			activity, android.R.layout.simple_spinner_item, appTypeArr);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		appType.setAdapter(spinnerAdapter);
		appType.setOnItemSelectedListener(new ItemSelectedListener());
		if (savedInstanceState == null) {
			appType.setSelection(3);
		}

		clearButton.setOnClickListener(this);
		searchButton.setOnClickListener(this);
		searchET.addTextChangedListener(textSearch);
		mSwipeRefreshLayout.setOnRefreshListener(this);

		listView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
				@Override
				public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
					//Log.d(TAG, "onScroll firstVisibleItem=" + firstVisibleItem + ", visibleItemCount=" + visibleItemCount + ", totalItemCount=" + totalItemCount);
					if (System.currentTimeMillis() - lastScroll > 50) {//!mScaling && 
						if (dy > activity.density << 4 && selectionStatusTV.getVisibility() == View.VISIBLE) {
							selectionStatusTV.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_bottom));
							selectionStatusTV.setVisibility(View.GONE);
							horizontalDivider0.setVisibility(View.GONE);
							horizontalDivider12.setVisibility(View.GONE);
							sortBarLayout.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_bottom));
							sortBarLayout.setVisibility(View.GONE);
						} else if (dy < -activity.density << 4 && selectionStatusTV.getVisibility() == View.GONE) {
							selectionStatusTV.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
							selectionStatusTV.setVisibility(View.VISIBLE);
							horizontalDivider0.setVisibility(View.VISIBLE);
							horizontalDivider12.setVisibility(View.VISIBLE);
							sortBarLayout.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
							sortBarLayout.setVisibility(View.VISIBLE);
						}
						lastScroll = System.currentTimeMillis();
					}
				}});
		appAdapter = new AppsAdapter(appList);
		listView.setAdapter(appAdapter);
		spanCount = AndroidUtils.getSharedPreference(getContext(), "SPAN_COUNT.Apps", 1);
		gridLayoutManager = new GridLayoutManager(fragActivity, spanCount);
		listView.setLayoutManager(gridLayoutManager);
		if (spanCount <= 2) {
			dividerItemDecoration = new GridDividerItemDecoration(fragActivity, true);
			listView.addItemDecoration(dividerItemDecoration);
		}

		updateColor(null);
		final String order = AndroidUtils.getSharedPreference(activity, "AppsList.order", "Name ▲");
		allName.setText("Name");
		allSize.setText("Size");
		allDate.setText("Date");
		switch (order) {
			case "Name ▼":
				appListSorter = new AppListSorter(
					AppListSorter.BY_LABEL,
					AppListSorter.DESC, false);
				allName.setText("Name ▼");
				break;
			case "Date ▲":
				appListSorter = new AppListSorter(
					AppListSorter.BY_DATE,
					AppListSorter.ASC, false);
				allDate.setText("Date ▲");
				break;
			case "Date ▼":
				appListSorter = new AppListSorter(
					AppListSorter.BY_DATE,
					AppListSorter.DESC, false);
				allDate.setText("Date ▼");
				//allType.setText("Package");
				break;
			case "Size ▲":
				appListSorter = new AppListSorter(
					AppListSorter.BY_SIZE,
					AppListSorter.ASC, false);
				allSize.setText("Size ▲");
				break;
			case "Size ▼":
				appListSorter = new AppListSorter(
					AppListSorter.BY_SIZE,
					AppListSorter.DESC, false);
				allSize.setText("Size ▼");
				break;
			default:
				appListSorter = new AppListSorter(
					AppListSorter.BY_LABEL,
					AppListSorter.ASC, false);
				allName.setText("Name ▲");
				break;
		}

		if (savedInstanceState == null)
			loadlist(false);
		else {
			gridLayoutManager.scrollToPositionWithOffset(savedInstanceState.getInt("index"),
														 savedInstanceState.getInt("top"));
		}
		final int size = selectedInList1.size();
		Log.d(TAG, "selectedInList1.size " + size + ", " + Util.collectionToString(selectedInList1, true, "\n"));
		if (size == 0) {
			allCbx.setImageResource(R.drawable.dot);
			if (commands.getVisibility() == View.VISIBLE) {
				horizontalDivider6.setVisibility(View.GONE);
				commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
				commands.setVisibility(View.GONE);
			}
		} else {
			if (size < appList.size()) {
				allCbx.setImageResource(R.drawable.ready);
			} else {
				allCbx.setImageResource(R.drawable.ic_accept);
			}
			if (commands.getVisibility() == View.GONE) {
				commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
				commands.setVisibility(View.VISIBLE);
				horizontalDivider6.setVisibility(View.VISIBLE);
			} 
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState " + title + ", " + outState);
		super.onSaveInstanceState(outState);

		index = gridLayoutManager.findFirstVisibleItemPosition();
		final View vi = listView.getChildAt(0);
		top = (vi == null) ? 0 : vi.getTop();
		outState.putInt("index", index);
		outState.putInt("top", top);
	}

	@Override
    public void onRefresh() {
        final Editable s = searchET.getText();
		if (s.length() > 0) {
			textSearch.afterTextChanged(s);
		} else {
			loadlist(false);
		}
    }

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
		if (Build.VERSION.SDK_INT > 23) {
			final IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_PACKAGE_INSTALL);
			intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
			intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
			intentFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
			intentFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
			//intentFilter.addDataScheme("package");
			br = new PackageReceiver();
			activity.registerReceiver(br, intentFilter);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		if (Build.VERSION.SDK_INT <= 23) {
			final IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_PACKAGE_INSTALL);
			intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
			intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
			intentFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
			intentFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
			//intentFilter.addDataScheme("package");
			br = new PackageReceiver();
			activity.registerReceiver(br, intentFilter);
		}
	}

	private PackageReceiver br = new PackageReceiver();
	final class PackageReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "PackageReceiver " + intent + ", " + context);
			if (intent != null) {
				loadlist(true);
			}
		}
	};

	@Override
	public void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		if (Build.VERSION.SDK_INT > 23) {
			searchTask.cancel(true);
			appLoadTask.cancel(true);
			activity.unregisterReceiver(br);
			br = null;
		}
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
		if (Build.VERSION.SDK_INT <= 23) {
			searchTask.cancel(true);
			appLoadTask.cancel(true);
			activity.unregisterReceiver(br);
			br = null;
		}
	}

	public void updateColor(View rootView) {
		getView().setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
		icons.setColorFilter(ExplorerActivity.TEXT_COLOR);
		allName.setTextColor(ExplorerActivity.TEXT_COLOR);
		allDate.setTextColor(ExplorerActivity.TEXT_COLOR);
		allSize.setTextColor(ExplorerActivity.TEXT_COLOR);
		//allType.setTextColor(ExplorerActivity.TEXT_COLOR);
		searchET.setTextColor(ExplorerActivity.TEXT_COLOR);
		clearButton.setColorFilter(ExplorerActivity.TEXT_COLOR);
		searchButton.setColorFilter(ExplorerActivity.TEXT_COLOR);
		selectionStatusTV.setTextColor(ExplorerActivity.TEXT_COLOR);

		horizontalDivider0.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
		horizontalDivider12.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
		horizontalDivider7.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);

		if (ExplorerActivity.BASE_BACKGROUND < 0xff808080) {
			appType.setPopupBackgroundResource(R.drawable.textfield_black);
		} else {
			appType.setPopupBackgroundResource(R.drawable.textfield_default_old);
		}
		appAdapter.notifyDataSetChanged();
	}

	private class ItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(
			AdapterView<?> parent, View view, int position, long id) {
			Log.i(TAG, "onItemSelected view " + view + ", position " + position + ", id " + id);
			//showToast("Spinner1: position=" + position + " id=" + id);
			synchronized (searchTask) {
				searchTask.cancel(true);
				searchTask = new SearchFileNameTask();
				searchTask.execute(position);
			}
		}
		public void onNothingSelected(AdapterView<?> parent) {
		}
	}

	private class TextSearch implements TextWatcher {
		public void beforeTextChanged(CharSequence s, int start, int end, int count) {
		}

		public void afterTextChanged(final Editable text) {
			final String filesearch = text.toString();
			Log.d("quicksearch", "filesearch " + filesearch);
			if (filesearch.length() > 0) {
				synchronized (searchTask) {
					searchTask.cancel(true);
					searchTask = new SearchFileNameTask();
					searchTask.execute(filesearch);
				}
			}
		}
		public void onTextChanged(CharSequence s, int start, int end, int count) {
		}
	}

	private class SearchFileNameTask extends AsyncTask<Object, Void, ArrayList<AppInfo>> {

		protected void onPreExecute() {
			mSwipeRefreshLayout.setRefreshing(true);
		}

		@Override
		protected ArrayList<AppInfo> doInBackground(Object... params) {
			final ArrayList<AppInfo> tempAppList = new ArrayList<AppInfo>();
			if (params[0] instanceof String) {
				searchMode = true;
				searchVal = searchET.getText().toString();
				synchronized (tempOriDataSourceL1) {
					for (AppInfo pi : tempOriDataSourceL1) {
						if (pi.label.contains((String)params[0]) || pi.packageName.contains((String)params[0])) {
							tempAppList.add(pi);
						}
					}
				}
			} else {
				int sel = params[0];
				synchronized (tempOriDataSourceL1) {
					if (sel == 0) {
						tempAppList.addAll(tempOriDataSourceL1);
					} else {
						for (AppInfo pi : tempOriDataSourceL1) {
							if (sel == 1 && pi.isSystemApp) {
								tempAppList.add(pi);
							} else if (sel == 2 && pi.isUpdatedSystemApp) {
								tempAppList.add(pi);
							} else if (sel == 3 && !pi.isSystemApp) {
								tempAppList.add(pi);
							} else if (sel == 4 && pi.isInternal) {
								tempAppList.add(pi);
							} else if (sel == 5 && pi.isExternalAsec) {
								tempAppList.add(pi);
							}
						}
					}
				}
			}
			return tempAppList;
		}

		@Override
		protected void onPostExecute(ArrayList<AppInfo> tempAppList) {
			
			Collections.sort(tempAppList, appListSorter);
			appList.clear();
			appList.addAll(tempAppList);
			selectedInList1.clear();
			appAdapter.notifyDataSetChanged();
			selectionStatusTV.setText(selectedInList1.size() + "/" + appList.size() + "/" + tempOriDataSourceL1.size());
			mSwipeRefreshLayout.setRefreshing(false);
		}
	}

	public void manageUi(boolean search) {

		if (search == true) {
			searchET.setHint("Search ");
			searchButton.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_bottom));
			searchButton.setImageResource(R.drawable.ic_arrow_back_white_36dp);
			//topflipper.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_bottom));
			topflipper.setDisplayedChild(topflipper.indexOfChild(quickLayout));
			searchMode = true;
			searchET.requestFocus();
			imm.showSoftInput(searchET, InputMethodManager.SHOW_IMPLICIT);
		} else {
			imm.hideSoftInputFromWindow(searchET.getWindowToken(), 0);
			searchET.setText("");
			searchButton.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
			searchButton.setImageResource(R.drawable.ic_action_search);
			//topflipper.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
			topflipper.setDisplayedChild(topflipper.indexOfChild(appType));
			searchMode = false;
			loadlist(false);
		}
	}

	void updateStatus() {
		appAdapter.notifyDataSetChanged();
		selectionStatusTV.setText(selectedInList1.size() + "/" + appList.size() + "/" + tempOriDataSourceL1.size());
	}

	void rangeSelection() {
		int min = Integer.MAX_VALUE, max = -1;
		int cur = -3;
		for (AppInfo s : selectedInList1) {
			cur = appList.indexOf(s);

			if (cur > max) {
				max = cur;
			}
			if (cur < min && cur >= 0) {
				min = cur;
			}
		}
		selectedInList1.clear();
		for (cur = min; cur <= max; cur++) {
			selectedInList1.add(appList.get(cur));
		}
		updateStatus();
	}

	void inversion() {
		tempSelectedInList1.clear();
		for (AppInfo f : appList) {
			if (!selectedInList1.contains(f)) {
				tempSelectedInList1.add(f);
			}
		}
		selectedInList1.clear();
		selectedInList1.addAll(tempSelectedInList1);
		updateStatus();
	}

	void clearSelection() {
		tempSelectedInList1.clear();
		tempSelectedInList1.addAll(selectedInList1);
		selectedInList1.clear();
		updateStatus();
	}

	void undoClearSelection() {
		selectedInList1.clear();
		selectedInList1.addAll(tempSelectedInList1);
		tempSelectedInList1.clear();
		updateStatus();
	}

	@Override
	public void onClick(View p1) {
		switch (p1.getId()) {
			case R.id.allCbx:
				final boolean all = !allCbx.isSelected();
				allCbx.setSelected(all);
				if (all) {
					allCbx.setImageResource(R.drawable.ic_accept);
				} else {
					allCbx.setImageResource(R.drawable.dot);
				}
				appAdapter.toggleChecked(all);
				selectionStatusTV.setText(selectedInList1.size() + "/" + appList.size() + "/" + tempOriDataSourceL1.size());
				break;
			case R.id.allName:
				if (allName.getText().toString().equals("Name ▲")) {
					allName.setText("Name ▼");
					appListSorter = new AppListSorter(
						AppListSorter.BY_LABEL,
						AppListSorter.DESC, false);
					AndroidUtils.setSharedPreference(activity,
													 "AppsList.order", "Name ▼");
				} else {
					allName.setText("Name ▲");
					appListSorter = new AppListSorter(
						AppListSorter.BY_LABEL,
						AppListSorter.ASC, false);
					AndroidUtils.setSharedPreference(activity,
													 "AppsList.order", "Name ▲");
				}
				// Log.i("allName", Util.collectionToString(dataSourceL2, true, "\n"));
				allDate.setText("Date");
				allSize.setText("Size");
				Collections.sort(appList, appListSorter);
				appAdapter.notifyDataSetChanged();
				break;
			case R.id.allDate:
				if (allDate.getText().toString().equals("Date ▲")) {
					allDate.setText("Date ▼");
					appListSorter = new AppListSorter(
						AppListSorter.BY_DATE,
						AppListSorter.DESC, false);
					AndroidUtils.setSharedPreference(activity,
													 "AppsList.order", "Date ▼");
				} else {
					allDate.setText("Date ▲");
					appListSorter = new AppListSorter(
						AppListSorter.BY_DATE,
						AppListSorter.ASC, false);
					AndroidUtils.setSharedPreference(activity,
													 "AppsList.order", "Date ▲");
				}
				// Log.i("date", Util.collectionToString(dataSourceL2, true, "\n"));
				allName.setText("Name");
				allSize.setText("Size");
				Collections.sort(appList, appListSorter);
				appAdapter.notifyDataSetChanged();
				break;
			case R.id.allSize:
				if (allSize.getText().toString().equals("Size ▲")) {
					allSize.setText("Size ▼");
					appListSorter = new AppListSorter(
						AppListSorter.BY_SIZE,
						AppListSorter.DESC, false);
					AndroidUtils.setSharedPreference(activity,
													 "AppsList.order", "Size ▼");
				} else {
					allSize.setText("Size ▲");
					appListSorter = new AppListSorter(
						AppListSorter.BY_SIZE,
						AppListSorter.ASC, false);
					AndroidUtils.setSharedPreference(activity,
													 "AppsList.order", "Size ▲");
				}
				allName.setText("Name");
				allDate.setText("Date");
				Collections.sort(appList, appListSorter);
				appAdapter.notifyDataSetChanged();
				break;
			case R.id.icons:
				moreInPanel(p1);
				break;
			case R.id.search:
				searchMode = !searchMode;
				manageUi(searchMode);
				break;
			case R.id.clear:
				searchET.setText("");
				break;
			case R.id.backup:
				Toast.makeText(
					AppsFragment.this.getContext(),
					AppsFragment.this.getResources().getString(R.string.copyingapk)
					+ BACKUP_PATH, Toast.LENGTH_LONG).show();
				for (AppInfo pi : selectedInList1) {
					backup(pi.path, pi.label,
						   pi.version);//, AppsFragment.this);
				}
				break;
			case R.id.share:
				ArrayList<File> arrayList2 = new ArrayList<File>();
				for (AppInfo pi : selectedInList1) {
					arrayList2.add(new File(pi.path));
				}
				Futils.shareFiles(arrayList2, activity, activity.getAppTheme(), accentColor);
				break;
			case R.id.unins:
				for (AppInfo pi : selectedInList1) {
					appAdapter.uninstall(pi);
				}
				break;
		}
	}

	public void loadlist(final boolean save) {

		Log.d(TAG, "loadlist " + save + ", fake " + fake);
		if (!fake) {
			if (save) {
				index = gridLayoutManager.findFirstVisibleItemPosition();
				final View vi = listView.getChildAt(0);
				top = (vi == null) ? 0 : vi.getTop();
			}
//			if (appLoadTask == null) {
//				appLoadTask = new LoadAppListTask(save, top, index);
//			} else {
//				final AsyncTask.Status status = appLoadTask.getStatus();
//				if (status == AsyncTask.Status.RUNNING ||
//					status == AsyncTask.Status.PENDING) {
					appLoadTask.cancel(true);
				//}
				appLoadTask = new LoadAppListTask(save, top, index);
			//}
			appLoadTask.execute();
		}
	}

	class LoadAppListTask extends AsyncTask<Void, ArrayList<AppInfo>, Void> {

		private final int index, top;
		private final boolean save;

		public long prevUpdate = 0;
		public boolean busyNoti = false;
		
		public LoadAppListTask(final boolean save, final int top, final int index) {
			this.save = save;
			this.index = index;
			this.top = top;
		}

		@Override
		protected void onPreExecute() {
			appList.clear();
			selectedInList1.clear();
			appAdapter.notifyDataSetChanged();
			mSwipeRefreshLayout.setRefreshing(true);
		}

		protected Void doInBackground(Void[] p1) {
            ArrayList<AppInfo> tempAppList = new ArrayList<AppInfo>(64);
			try {
                final PackageManager packageManager = activity.getPackageManager();
				final List<PackageInfo> all_apps = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);
                Log.d(TAG, all_apps.size() + " apps");
				for (PackageInfo pinfo : all_apps) {
					//File f=new File(pinfo.applicationInfo.publicSourceDir);
					//Log.d("AppsList", pinfo + ".");

					tempAppList.add(new AppInfo(packageManager, pinfo));

					final long present = System.currentTimeMillis();
					if (present - prevUpdate > 1000 && !busyNoti) {
						prevUpdate = present;
						publishProgress(tempAppList);
						tempAppList = new ArrayList<>(64);
					}
					//packageInfos.add(pinfo);
					//System.out.println( new ObjectDumper(pinfo).dump());
				}
				publishProgress(tempAppList);
				//Collections.sort(layoutElems, new FileListSorter(0, sortby, asc, false));
            } catch (Throwable e) {
				e.printStackTrace();
                //Toast.makeText(getActivity(), "" + e, Toast.LENGTH_LONG).show();
            }
            return null;//tempAppList;
        }

		@Override
		protected void onProgressUpdate(ArrayList<AppInfo>... values) {
			super.onProgressUpdate(values);
			busyNoti = true;
			appList.addAll(values[0]);
			appAdapter.notifyDataSetChanged();
			busyNoti = false;
			selectionStatusTV.setText(selectedInList1.size() + "/" + appList.size());
		}

		@Override
		// Once the image is downloaded, associates it to the imageView
		protected void onPostExecute(Void v) {
			try {
				Collections.sort(appList, appListSorter);
				synchronized (tempOriDataSourceL1) {
					tempOriDataSourceL1.clear();
					tempOriDataSourceL1.addAll(appList);
				}

				appAdapter.notifyDataSetChanged();

				selectionStatusTV.setText(selectedInList1.size() + "/" + appList.size() + "/" + tempOriDataSourceL1.size());
				if (save) {
					gridLayoutManager.scrollToPositionWithOffset(index, top);
				}

				mSwipeRefreshLayout.setRefreshing(false);
				new ItemSelectedListener().onItemSelected(null, null, appType.getSelectedItemPosition(), 0);

			} catch (Throwable e) {
				e.printStackTrace();
			}

		}
	} // copy the .apk file to wherever

	private class AppsAdapter extends RecyclerAdapter<AppInfo, AppsAdapter.ViewHolder> implements OnClickListener, OnLongClickListener {
		private static final String TAG = "AppsAdapter";

		private final PackageManager packageManager = activity.getPackageManager();

		public AppsAdapter(ArrayList<AppInfo> appList) {
			super(appList);
		}

		public void toggleChecked(boolean checked, AppInfo packageInfo) {
			Log.d(TAG, "toggleChecked " + checked + packageInfo);
			if (checked) {
				selectedInList1.add(packageInfo);
			} else {
				selectedInList1.remove(packageInfo);
			}
			notifyDataSetChanged();
			selectionStatusTV.setText(selectedInList1.size() + "/" + appList.size() + "/" + tempOriDataSourceL1.size());
			boolean all = selectedInList1.size() == appList.size();
			allCbx.setSelected(all);
			if (all) {
				allCbx.setImageResource(R.drawable.ic_accept);
			} else if (selectedInList1.size() > 0) {
				allCbx.setImageResource(R.drawable.ready);
			} else {
				allCbx.setImageResource(R.drawable.dot);
			}
			if (selectedInList1.size() > 0) {
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
			if (b) {
				selectedInList1.clear();
				selectedInList1.addAll(appList);
				if (selectedInList1.size() > 0 && commands.getVisibility() == View.GONE) {
					commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
					commands.setVisibility(View.VISIBLE);
					horizontalDivider6.setVisibility(View.VISIBLE);
				}
			} else {
				selectedInList1.clear();
				if (commands.getVisibility() == View.VISIBLE) {
					horizontalDivider6.setVisibility(View.GONE);
					commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
					commands.setVisibility(View.GONE);
				}
			}
			notifyDataSetChanged();
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent,
											 int viewType) {
			View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_app, parent, false);
			// set the view's size, margins, paddings and layout parameters
			final ViewHolder vh = new ViewHolder(v);
			return vh;
		}

		private class ViewHolder extends RecyclerView.ViewHolder {
			final View ll;
			final TextView name;
			final TextView items;
			final TextView attr;
			final TextView lastModified;
			final TextView type;
			final ImageButton cbx;
			final ImageView image;
			final ImageButton more;

			private ViewHolder(View convertView) {
				super(convertView);
				name = (TextView) convertView.findViewById(R.id.name);
				items = (TextView) convertView.findViewById(R.id.items);
				attr = (TextView) convertView.findViewById(R.id.attr);
				lastModified = (TextView) convertView.findViewById(R.id.lastModified);
				type = (TextView) convertView.findViewById(R.id.type);
				cbx = (ImageButton) convertView.findViewById(R.id.cbx);
				image = (ImageView) convertView.findViewById(R.id.icon);
				more = (ImageButton) convertView.findViewById(R.id.more);
				ll = convertView;

				ll.setTag(this);
				ll.setOnClickListener(AppsAdapter.this);
				cbx.setOnClickListener(AppsAdapter.this);

				ll.setOnLongClickListener(AppsAdapter.this);
				cbx.setOnLongClickListener(AppsAdapter.this);
				more.setOnClickListener(AppsAdapter.this);

				more.setColorFilter(ExplorerActivity.TEXT_COLOR);
				name.setTextColor(ExplorerActivity.DIR_COLOR);
				items.setTextColor(ExplorerActivity.TEXT_COLOR);
				attr.setTextColor(ExplorerActivity.TEXT_COLOR);
				lastModified.setTextColor(ExplorerActivity.TEXT_COLOR);
				type.setTextColor(ExplorerActivity.TEXT_COLOR);
			}

			private void bind(final int position) {
				final AppInfo appInfo = appList.get(position);
				// Log.d(TAG, convertView + ".");

				try {
					image.setImageDrawable(packageManager.getApplicationIcon(appInfo.packageName));
				} catch (PackageManager.NameNotFoundException e) {
					image.setImageResource(R.drawable.ic_doc_apk);
				}

				name.setText(appInfo.label);
				items.setText(appInfo.size);
				attr.setText(appInfo.packageName);
				lastModified.setText(appInfo.date);
				type.setText(appInfo.version);

				more.setTag(appInfo);
				cbx.setTag(appInfo);	

				final boolean checked = selectedInList1.contains(appInfo);
				//Log.d(TAG, "selectedInList1.contains(appInfo) " + checked + ", " + appInfo);
				if (checked) {
					ll.setBackgroundColor(ExplorerActivity.IN_DATA_SOURCE_2);
					cbx.setSelected(true);
					cbx.setImageResource(R.drawable.ic_accept);
				} else if (selectedInList1.size() > 0) {
					ll.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
					cbx.setSelected(false);
					cbx.setImageResource(R.drawable.ready);
				} else {
					ll.setBackgroundResource(R.drawable.ripple);
					cbx.setSelected(false);
					cbx.setImageResource(R.drawable.dot);
				}
			}
		}

		@Override
		public void onBindViewHolder(final ViewHolder holder, final int position) {
			holder.bind(position);
		}

		@Override
		public void onClick(final View p1) {
			final int id = p1.getId();
			if (selectedInList1.size() > 0 && id != R.id.more) {
				onLongClick(p1);
				return;
			}
			Intent i1;
			final Object tag = p1.getTag();
			if (id == R.id.cbx) {
				p1.setSelected(!p1.isSelected());
				toggleChecked(p1.isSelected(), (AppInfo) tag);
				return;
			} else if (id == R.id.more) {
				showPopup(p1, (AppInfo) tag);
				return;
			} else {
				i1 = packageManager.getLaunchIntentForPackage(
					((AppInfo) ((ViewHolder) tag).cbx.getTag()).packageName);
			}
			if (i1 != null)
				AppsFragment.this.startActivity(i1);
			else
				Toast.makeText(AppsFragment.this.getContext(),
							   AppsFragment.this.getResources().getString(R.string.not_allowed),
							   Toast.LENGTH_LONG).show();
		}

		@Override
		public boolean onLongClick(final View p1) {
			final Object tag = p1.getTag();
			AppInfo tag2;
			if (tag instanceof AppInfo) {
				tag2 = (AppInfo) tag;
			} else {
				tag2 = (AppInfo) ((ViewHolder) tag).cbx.getTag();
			}
			toggleChecked(!selectedInList1.contains(tag2), tag2);
			return false;
		}

		private void showPopup(final View v, final AppInfo rowItem) {

			final MenuBuilder menuBuilder = new MenuBuilder(activity);
			final MenuInflater inflater = new MenuInflater(activity);
			inflater.inflate(R.menu.app_commands, menuBuilder);
			final MenuPopupHelper optionsMenu = new MenuPopupHelper(activity , menuBuilder, allSize);
			optionsMenu.setForceShowIcon(true);

			Drawable icon = menuBuilder.findItem(R.id.shortcut).getIcon();
			icon.setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);

			icon = menuBuilder.findItem(R.id.properties).getIcon();
			icon.setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
			
			menuBuilder.findItem(R.id.play).getIcon().setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);

			menuBuilder.setCallback(new MenuBuilder.Callback() {
					@Override
					public boolean onMenuItemSelected(MenuBuilder p1, MenuItem item) {
						switch (item.getItemId()) {
							case R.id.open:
								Intent i1 = packageManager.getLaunchIntentForPackage(
									rowItem.packageName);
								if (i1 != null)
									AppsFragment.this.startActivity(i1);
								else
									Toast.makeText(
										AppsFragment.this.getContext(),
										AppsFragment.this.getResources().getString(R.string.not_allowed),
										Toast.LENGTH_LONG).show();
								return true;
							case R.id.share:
								ArrayList<File> arrayList2 = new ArrayList<File>();
								arrayList2.add(new File(rowItem.path));
								Futils.shareFiles(arrayList2, activity, activity.getAppTheme(), accentColor);
								return true;
							case R.id.unins:
								uninstall(rowItem);
								return true;
							case R.id.play:
								Intent intent1 = new Intent(Intent.ACTION_VIEW);
								intent1.setData(Uri.parse("market://details?id=" + rowItem.packageName));
								AppsFragment.this.startActivity(intent1);
								return true;
							case R.id.properties:
								AppsFragment.this.startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
																		   Uri.parse("package:" + rowItem.packageName)));
								return true;
							case R.id.backup:
								Toast.makeText(
									AppsFragment.this.getContext(),
									AppsFragment.this.getResources().getString(R.string.copyingapk)
									+ BACKUP_PATH,
									Toast.LENGTH_LONG).show();
								backup(rowItem.path,
									   rowItem.label,
									   rowItem.version);//, AppsFragment.this);
								return true;
							case R.id.shortcut:
								//AndroidUtils.createShortCut(AppsFragment.this.getContext(), rowItem.packageName, rowItem.label, image.setImageDrawable(packageManager.getApplicationIcon(appInfo.packageName)));
								return true;
						}
						return true;
					}

					@Override
					public void onMenuModeChange(MenuBuilder p1) {
					}
				});
			optionsMenu.show();
		}

		public void uninstall(final AppInfo item) {
			final BaseFile f1 = new BaseFile(item.path);
			f1.setMode(OpenMode.ROOT);
			ApplicationInfo info1 = AndroidUtils.getAppInfo(AppsFragment.this.getContext(), item.packageName);

			if (info1 != null && (info1.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
				// system package
				if (AppsFragment.this.sharedPref.getBoolean("rootmode", false)) {
					MaterialDialog.Builder builder1 = new MaterialDialog.Builder(AppsFragment.this.getContext());
					if (AppsFragment.this.theme1 == 1)
						builder1.theme(Theme.DARK);
					builder1.content(AppsFragment.this.getResources().getString(R.string.unin_system_apk))
						.title(AppsFragment.this.getResources().getString(R.string.warning))
						.negativeColor(accentColor)
						.positiveColor(accentColor)
						.negativeText(AppsFragment.this.getResources().getString(R.string.no))
						.positiveText(AppsFragment.this.getResources().getString(R.string.yes))
						.callback(new MaterialDialog.ButtonCallback() {
							@Override
							public void onNegative(MaterialDialog materialDialog) {
								materialDialog.cancel();
							}

							@Override
							public void onPositive(MaterialDialog materialDialog) {

								ArrayList<BaseFile> files = new ArrayList<>();
								if (Build.VERSION.SDK_INT >= 21) {
									String parent = f1.getParent();
									if (!parent.equals("app") && !parent.equals("priv-app")) {
										BaseFile baseFile=new BaseFile(f1.getParent());
										baseFile.setMode(OpenMode.ROOT);
										files.add(baseFile);
									} else {
										files.add(f1);
									}
								} else {
									files.add(f1);
								}
								new DeleteTask(AppsFragment.this.getContext(), null).execute((files));
							}
						}).build().show();
				} else {
					Toast.makeText(AppsFragment.this.getContext(), AppsFragment.this.getResources().getString(R.string.enablerootmde), Toast.LENGTH_SHORT).show();
				}
			} else {
				uninstall(item.packageName);
			}
		}

		public boolean uninstall(String pkg) {
			try {
				Intent intent = new Intent(Intent.ACTION_DELETE);
				intent.setData(Uri.parse("package:" + pkg));
				AppsFragment.this.startActivity(intent);
			} catch (Exception e) {
				Toast.makeText(AppsFragment.this.getContext(), "" + e, Toast.LENGTH_SHORT).show();
				e.printStackTrace();
				return false;
			}
			return true;
		}
	}

	void backup(final String path, final String title, final String version) {
		//Toast.makeText(activity, activity.getResources().getString(R.string.copyingapk) + Environment.getExternalStorageDirectory().getPath() + "/app_backup", Toast.LENGTH_LONG).show();
		File f = new File(path);
		ArrayList<BaseFile> ab = new ArrayList<>();
		File dst = new File(Environment.getExternalStorageDirectory().getPath() + "/app_backup");
		if (!dst.exists() || !dst.isDirectory())dst.mkdirs();
		Intent intent = new Intent(activity, CopyService.class);
		BaseFile baseFile=RootHelper.generateBaseFile(f, true);
		baseFile.setName(title + "_" + version + ".apk");
		ab.add(baseFile);

		intent.putParcelableArrayListExtra(CopyService.TAG_COPY_SOURCES, ab);
		intent.putExtra(CopyService.TAG_COPY_TARGET, dst.getPath());
		intent.putExtra(CopyService.TAG_COPY_OPEN_MODE, 0);

		ServiceWatcherUtil.runService(activity, intent);
	}

	public static void backup(final String path, final String title, final String version, FileFrag fileFrag) {
		Log.d("AppsFragment", path + ", " + title + ", " + version);
		File f = new File(path);
		ArrayList<BaseFile> ab = new ArrayList<>();

		BaseFile baseFile=RootHelper.generateBaseFile(f, true);
		baseFile.setName(title + "_" + version + ".apk");
		Log.d("AppsFragment", baseFile.toString());

		//Log.d("AppsAdapter", new ObjectDumper(rowItem).dump() + "");
		ab.add(baseFile);
		new CopyFileCheck(fileFrag, BACKUP_PATH, false, fileFrag.activity, ThemedActivity.rootMode, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ab);

	}
}



