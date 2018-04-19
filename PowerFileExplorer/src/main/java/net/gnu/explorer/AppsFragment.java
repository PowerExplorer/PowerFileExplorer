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

public class AppsFragment extends FileFrag implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

	//private UtilitiesProviderInterface utilsProvider;

	private static final String TAG = "AppsFragment";

	//private AppsFragment app = this;
	private AppsAdapter appAdapter;
	public static final String BACKUP_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/app_backup";
	private AppListSorter appListSorter;
	//SharedPreferences Sp;
	//public IconHolder ic;
	private ArrayList<AppInfo> appList = new ArrayList<AppInfo>();
	public int theme1;

	private boolean searchMode = false;
	private String searchVal = "";

	private Spinner appType;
	private SearchFileNameTask searchTask = new SearchFileNameTask();
	private transient List<AppInfo> prevInfo = new LinkedList<>();;
	//HashSet<AppInfo> selectedInList1 = new HashSet<AppInfo>();
	//private final ArrayList<AppInfo> tempSelectedInList1 = new ArrayList<>();
	private final String[] appTypeArr = new String[] {
		"All",
		"System App",
		"Updated System App",
		"User App",
		"Internal",
		"External Asec"};

//	private ListView listView = null;
	private LoadAppListTask appLoadTask = new LoadAppListTask();
//	private int index;
//	private int top;
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		//Log.d(TAG, "onCreateView " + savedInstanceState);
		return inflater.inflate(R.layout.pager_item_app, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState) {
		Log.d(TAG, "onViewCreated " + savedInstanceState);
		super.onViewCreated(v, savedInstanceState);

		
		appType = (Spinner) v.findViewById(R.id.appType);
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
			activity, android.R.layout.simple_spinner_item, appTypeArr);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		appType.setAdapter(spinnerAdapter);
		appType.setOnItemSelectedListener(new ItemSelectedListener());
		appType.setSelection(3);

		clearButton.setOnClickListener(this);
		searchButton.setOnClickListener(this);
		searchET.addTextChangedListener(textSearch);
		mSwipeRefreshLayout.setOnRefreshListener(this);

		//listView.setFastScrollEnabled(true);
		//listView2.setOnTouchListener(this);
		// listView2.setFastScrollAlwaysVisible(false);
		// listView2.setVerticalScrollbarPosition(0);
		listView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
				@Override
				public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
					//Log.d(TAG, "onScroll firstVisibleItem=" + firstVisibleItem + ", visibleItemCount=" + visibleItemCount + ", totalItemCount=" + totalItemCount);
					if (System.currentTimeMillis() - lastScroll > 50) {//!mScaling && 
						if (dy > activity.density << 4 && selectionStatus1.getVisibility() == View.VISIBLE) {
							selectionStatus1.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_bottom));
							selectionStatus1.setVisibility(View.GONE);
							horizontalDivider0.setVisibility(View.GONE);
							horizontalDivider12.setVisibility(View.GONE);
							status.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_bottom));
							status.setVisibility(View.GONE);
						} else if (dy < -activity.density << 4 && selectionStatus1.getVisibility() == View.GONE) {
							selectionStatus1.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
							selectionStatus1.setVisibility(View.VISIBLE);
							horizontalDivider0.setVisibility(View.VISIBLE);
							horizontalDivider12.setVisibility(View.VISIBLE);
							status.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
							status.setVisibility(View.VISIBLE);
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
			//dividerItemDecoration = new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST, true, true);
			listView.addItemDecoration(dividerItemDecoration);
		}

		Bundle args = getArguments();
		Log.d(TAG, "onViewCreated " + currentPathTitle + ", " + "args=" + args + " saved "
			  + savedInstanceState);

		if (args != null) {
			title = args.getString("title");
		}
		if (savedInstanceState == null) {
//			title = savedInstanceState.getString("title");
////			index = savedInstanceState.getInt("index");
////			top = savedInstanceState.getInt("top");
//		} else {
			loadlist();//false
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
		//Sp = PreferenceManager.getDefaultSharedPreferences(getContext());
		// getSortModes();
//		int theme = Sp.getInt("theme", 0);
//		theme1 = theme == 2 ? PreferenceUtils.hourOfDay() : theme;
		// listView2.setDivider(null);

//		if (savedInstanceState == null)
//			loadlist(false);
//		else {
//			// packageInfos = savedInstanceState.getParcelableArrayList("c");
//			layoutElems = savedInstanceState.getParcelableArrayList("list");
//			adapter = new AppsAdapter(getContext(), R.layout.rowlayout);
//			listView2.setAdapter(adapter);
//			listView2.setSelectionFromTop(savedInstanceState.getInt("index"),
//										  savedInstanceState.getInt("top"));
//		}

	}

//	@Override
//	public void onSaveInstanceState(Bundle outState) {
//		super.onSaveInstanceState(outState);
//		Log.d(TAG, "onSaveInstanceState " + title + ", " + outState);
//		outState.putString("title", title);
//		// b.putParcelableArrayList("c", packageInfos);
//		//outState.putParcelableArrayList("list", layoutElems);
////		index = appListView.getFirstVisiblePosition();
////		final View vi = appListView.getChildAt(0);
////		top = (vi == null) ? 0 : vi.getTop();
////		outState.putInt("index", index);
////		outState.putInt("top", top);
//	}

	@Override
    public void onRefresh() {
        final Editable s = searchET.getText();
		if (s.length() > 0) {
			textSearch.afterTextChanged(s);
		} else {
			loadlist();//false
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
			intentFilter.addDataScheme("package");
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
			intentFilter.addDataScheme("package");
			br = new PackageReceiver();
			activity.registerReceiver(br, intentFilter);
		}
	}

	private PackageReceiver br = new PackageReceiver();
	final class PackageReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				loadlist();//true
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
		selectionStatus1.setTextColor(ExplorerActivity.TEXT_COLOR);

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
			//Log.i("on appType", type[position]);
			//showToast("Spinner1: position=" + position + " id=" + id);
			AsyncTask.Status status;
			synchronized (searchTask) {
				if ((status = searchTask.getStatus()) == AsyncTask.Status.RUNNING
					|| status == AsyncTask.Status.PENDING) {
					searchTask.cancel(true);
				}
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
				AsyncTask.Status status;
				synchronized (searchTask) {
					if (((status = searchTask.getStatus()) == AsyncTask.Status.RUNNING
						|| status == AsyncTask.Status.PENDING)) {
						searchTask.cancel(true);
					}
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
			if (!mSwipeRefreshLayout.isRefreshing()) {
				mSwipeRefreshLayout.setRefreshing(true);
			}
		}

		@Override
		protected ArrayList<AppInfo> doInBackground(Object... params) {
			final ArrayList<AppInfo> tempAppList = new ArrayList<AppInfo>();
			if (params[0] instanceof String) {
				searchMode = true;
				searchVal = searchET.getText().toString();
				for (AppInfo pi : prevInfo) {
					if (pi.label.contains((String)params[0]) || pi.packageName.contains((String)params[0])) {
						tempAppList.add(pi);
					}
				}
			} else {
				int sel = params[0];
				if (sel == 0) {
					tempAppList.addAll(prevInfo);
				} else {
					for (AppInfo pi : prevInfo) {
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
			return tempAppList;
		}

		@Override
		protected void onPostExecute(ArrayList<AppInfo> tempAppList) {
			if (isCancelled()) {
				return;
			}
			Collections.sort(tempAppList, appListSorter);
			appList.clear();
			appList.addAll(tempAppList);
			appAdapter.notifyDataSetChanged();
			selectionStatus1.setText(selectedInList1.size() + "/" + appList.size() + "/" + prevInfo.size());
			if (mSwipeRefreshLayout.isRefreshing()) {
				mSwipeRefreshLayout.setRefreshing(false);
			}
		}
	}

	public void manageUi(boolean search) {
		// HorizontalScrollView scrollButtons =
		// (HorizontalScrollView)findViewById(R.id.scroll_buttons);

		if (search == true) {
			searchET.setHint("Search ");
			searchButton.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_bottom));
			searchButton.setImageResource(R.drawable.ic_arrow_back_grey600);
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
			loadlist();//false
		}
	}

//	public void mainmenu(final View v) {
//		final PopupMenu popup = new PopupMenu(v.getContext(), v);
//        popup.inflate(R.menu.panel_commands);
//		final Menu menu = popup.getMenu();
//		if (!activity.multiFiles) {
//			menu.findItem(R.id.horizontalDivider5).setVisible(false);
//		}
//		MenuItem mi = menu.findItem(R.id.clearSelection);
//		if (selectedInList1.size() == 0) {
//			mi.setEnabled(false);
//		} else {
//			mi.setEnabled(true);
//		}
//		mi = menu.findItem(R.id.rangeSelection);
//		if (selectedInList1.size() > 1) {
//			mi.setEnabled(true);
//		} else {
//			mi.setEnabled(false);
//		}
//		mi = menu.findItem(R.id.undoClearSelection);
//		if (tempSelectedInList1.size() > 0) {
//			mi.setEnabled(true);
//		} else {
//			mi.setEnabled(false);
//		}
//		mi = menu.findItem(R.id.hide);
//		if (activity.left.getVisibility() == View.VISIBLE) {
//			mi.setTitle("Hide");
//		} else {
//			mi.setTitle("2 panels");
//		}
//		mi = menu.findItem(R.id.biggerequalpanel);
//		if (activity.left.getVisibility() == View.GONE || activity.right.getVisibility() == View.GONE) {
//			mi.setEnabled(false);
//		} else {
//			mi.setEnabled(true);
//			if (slidingTabsFragment.width <= 0) {
//				mi.setTitle("Wider panel");
//			} else {
//				mi.setTitle("2 panels equal");
//			}
//		}
//        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//				public boolean onMenuItemClick(MenuItem item) {
//					switch (item.getItemId()) {
//						case R.id.rangeSelection:
//							rangeSelection();
//							break;
//						case R.id.inversion:
//							inversion();
//							break;
//						case R.id.clearSelection:
//							clearSelection();
//							break;
//						case R.id.undoClearSelection:
//							undoClearSelection();
//							break;
//						case R.id.swap:
//							swap(v);
//							break;
//						case R.id.hide: 
//							if (activity.right.getVisibility() == View.VISIBLE) {
//								if (activity.swap) {
//									activity.left.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_left));
//									activity.right.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_left));
//								} else {
//									activity.left.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_in_right));
//									activity.right.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_out_right));
//								}
//								activity.left.setVisibility(View.GONE);
//							} else {
//								if (activity.swap) {
//									activity.left.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_left));
//									activity.right.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_left));
//								} else {
//									activity.left.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_in_right));
//									activity.right.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_out_right));
//								}
//								activity.right.setVisibility(View.VISIBLE);
//							}
//							break;
//						case R.id.biggerequalpanel:
//							if (activity.leftSize <= 0) {
//								//mi.setTitle("Wider panel");
//								LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)activity.left.getLayoutParams();
//								params.weight = 2.0f;
//								activity.left.setLayoutParams(params);
//								params = (LinearLayout.LayoutParams)activity.right.getLayoutParams();
//								params.weight = 1.0f;
//								activity.right.setLayoutParams(params);
//								activity.leftSize = 1;
//								if (left == activity.left) {
//									slidingTabsFragment.width = -1;
//								} else {
//									slidingTabsFragment.width = 1;
//								}
//							} else {
//								LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)activity.left.getLayoutParams();
//								params.weight = 1.0f;
//								activity.left.setLayoutParams(params);
//								params = (LinearLayout.LayoutParams)activity.right.getLayoutParams();
//								params.weight = 1.0f;
//								activity.right.setLayoutParams(params);
//								activity.leftSize = 0;
//								slidingTabsFragment.width = 0;
//							}
//							AndroidUtils.setSharedPreference(activity, "biggerequalpanel", activity.leftSize);
//					}
//					appAdapter.notifyDataSetChanged();
//					selectionStatus1.setText(selectedInList1.size() + "/" + appList.size() + "/" + prevInfo.size());
//					return true;
//				}
//			});
//		popup.show();
//	}

	void updateStatus() {
		appAdapter.notifyDataSetChanged();
		selectionStatus1.setText(selectedInList1.size() + "/" + appList.size() + "/" + prevInfo.size());
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

	public void mainmenu2(final View v) {
		switch (v.getId()) {
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
				new Futils().shareFiles(arrayList2, activity, activity.getAppTheme(), accentColor);
				break;
			case R.id.unins:
				for (AppInfo pi : selectedInList1) {
					appAdapter.uninstall(pi);
				}
				break;
		}
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
				selectionStatus1.setText(selectedInList1.size() + "/" + appList.size() + "/" + prevInfo.size());
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
		}
	}

	public void loadlist() {//boolean save

		Log.d(TAG, "loadlist ");//+ save
//		if (save) {
//			index = appListView.getFirstVisiblePosition();
//			View vi = appListView.getChildAt(0);
//			top = (vi == null) ? 0 : vi.getTop();
//		}
//		if (appLoadTask == null) {
//			appLoadTask = new LoadListTask(save, top, index);
//		} else {
		if (appLoadTask.getStatus() == AsyncTask.Status.RUNNING ||
			appLoadTask.getStatus() == AsyncTask.Status.PENDING) {
			appLoadTask.cancel(true);
		}
		appLoadTask = new LoadAppListTask();
		//}
		appLoadTask.execute();
	}

	class LoadAppListTask extends AsyncTask<Void, Void, ArrayList<AppInfo>> {

//		private int index, top;
//		private boolean save;
//
//		public LoadListTask(boolean save, int top, int index) {
//			this.save = save;
//			this.index = index;
//			this.top = top;
//		}

		@Override
		protected void onPreExecute() {
			if (!mSwipeRefreshLayout.isRefreshing()) {
				mSwipeRefreshLayout.setRefreshing(true);
			}
		}

		protected ArrayList<AppInfo> doInBackground(Void[] p1) {
            final ArrayList<AppInfo> tempAppList = new ArrayList<AppInfo>();
			try {
                final PackageManager packageManager = activity.getPackageManager();
				final List<PackageInfo> all_apps = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);
                Log.d(TAG, all_apps.size() + " apps");
				for (PackageInfo pinfo : all_apps) {
					//File f=new File(pinfo.applicationInfo.publicSourceDir);
					//Log.d("AppsList", pinfo + ".");

					tempAppList.add(new AppInfo(packageManager, pinfo));

					//packageInfos.add(pinfo);
					//System.out.println( new ObjectDumper(pinfo).dump());
				}
				//Collections.sort(layoutElems, new FileListSorter(0, sortby, asc, false));
            } catch (Throwable e) {
				e.printStackTrace();
                //Toast.makeText(getActivity(), "" + e, Toast.LENGTH_LONG).show();
            }
            return tempAppList;
        }

		@Override
		// Once the image is downloaded, associates it to the imageView
		protected void onPostExecute(ArrayList<AppInfo> tempAppList) {
			if (isCancelled()) {
				return;
			}
			try {
				Collections.sort(tempAppList, appListSorter);
				appList.clear();
				prevInfo.clear();
				appList.addAll(tempAppList);
				prevInfo.addAll(tempAppList);
				appAdapter.notifyDataSetChanged();

				//if (save)
				selectionStatus1.setText(selectedInList1.size() + "/" + appList.size() + "/" + prevInfo.size());
				//appListView.setSelectionFromTop(index, top);
				//index = 0;
				//top = 0;

				new ItemSelectedListener().onItemSelected(null, null, appType.getSelectedItemPosition(), 0);
				if (mSwipeRefreshLayout.isRefreshing()) {
					mSwipeRefreshLayout.setRefreshing(false);
				}

//				File dst = new File(BACKUP_PATH);
//				if (!dst.exists())
//					dst.mkdirs();
//				if ((!dst.exists() || !dst.canWrite() || AndroidPathUtils
//						.isOnExtSdCard(dst, AppsFragment.this.getActivity()))) {
//					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//						// && AndroidPathUtils.isOnExtSdCard(dst)) {
//						// if (AndroidPathUtils.treeUri == null) {// &&
//						// !checkFolder(new File(st).getParentFile(),
//						// WRITE_REQUEST_CODE)) {
//						// AndroidPathUtils.treeUri =
//						// AndroidPathUtils.getSharedPreferenceUri(R.string.key_internal_uri_extsdcard,
//						// AppsFragment.this.getActivity());
//						if (AndroidPathUtils.getTreeUri(AppsFragment.this
//								.getActivity()) == null) {
//							AlertDialog.Builder alert = new AlertDialog.Builder(
//									AppsFragment.this.getActivity());
//							alert.setTitle("Grant Permission in extSdCard");
//							alert.setMessage("In the following Android dialog, "
//									+ "please select the external SD card and confirm at the bottom.");
//							alert.setCancelable(true);
//							alert.setPositiveButton("Yes",
//									new DialogInterface.OnClickListener() {
//										@Override
//										public void onClick(
//												DialogInterface dialog,
//												int which) {
//											triggerStorageAccessFramework(ExplorerActivity.INTENT_WRITE_REQUEST_CODE);
//										}
//									});
//							alert.setNegativeButton("No",
//									new DialogInterface.OnClickListener() {
//										@Override
//										public void onClick(
//												DialogInterface dialog,
//												int which) {
//											dialog.cancel();
//										}
//									});
//							AlertDialog alertDialog = alert.create();
//							alertDialog.show();
//						}
//						// }
//					} else {
//						Toast.makeText(AppsFragment.this.getActivity(),
//								BACKUP_PATH + " cannot be written",
//								Toast.LENGTH_SHORT).show();
//					}
//				}
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
			selectionStatus1.setText(selectedInList1.size() + "/" + appList.size() + "/" + prevInfo.size());
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
			View v;
			v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_app, parent, false);
			// set the view's size, margins, paddings and layout parameters
			final ViewHolder vh = new ViewHolder(v);
			return vh;
		}

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
				image = (ImageView) convertView.findViewById(R.id.icon);
				more = (ImageButton) convertView.findViewById(R.id.more);
				ll = convertView;

				ll.setTag(this);
				ll.setOnClickListener(AppsAdapter.this);
				cbx.setOnClickListener(AppsAdapter.this);

				ll.setOnLongClickListener(AppsAdapter.this);
				cbx.setOnLongClickListener(AppsAdapter.this);
			}
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {

			final AppInfo rowItem = appList.get(position);
			// Log.d(TAG, convertView + ".");

			try {
				holder.image.setImageDrawable(packageManager.getApplicationIcon(rowItem.packageName));
			} catch (PackageManager.NameNotFoundException e) {
				holder.image.setImageResource(R.drawable.ic_doc_apk);
			}

			holder.name.setText(rowItem.label);
			holder.items.setText(rowItem.size);
			holder.attr.setText(rowItem.packageName);
			holder.lastModified.setText(rowItem.date);
			holder.type.setText(rowItem.version);

			holder.more.setColorFilter(ExplorerActivity.TEXT_COLOR);
			holder.name.setTextColor(ExplorerActivity.DIR_COLOR);
			holder.items.setTextColor(ExplorerActivity.TEXT_COLOR);
			holder.attr.setTextColor(ExplorerActivity.TEXT_COLOR);
			holder.lastModified.setTextColor(ExplorerActivity.TEXT_COLOR);
			holder.type.setTextColor(ExplorerActivity.TEXT_COLOR);

			holder.ll.setTag(holder);
			holder.cbx.setTag(rowItem);
			showPopup(holder.more, rowItem);

			final boolean checked = selectedInList1.contains(rowItem);
			if (checked) {
				holder.ll.setBackgroundColor(ExplorerActivity.IN_DATA_SOURCE_2);
				holder.cbx.setSelected(true);
				holder.cbx.setImageResource(R.drawable.ic_accept);
			} else if (selectedInList1.size() > 0) {
				holder.ll.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
				holder.cbx.setSelected(false);
				holder.cbx.setImageResource(R.drawable.ready);
			} else {
				holder.ll.setBackgroundResource(R.drawable.ripple);
				holder.cbx.setSelected(false);
				holder.cbx.setImageResource(R.drawable.dot);
			}
		}

		@Override
		public void onClick(View p1) {
			if (selectedInList1.size() > 0) {
				onLongClick(p1);
				return;
			}
			Intent i1;
			Object tag = p1.getTag();
			if (p1.getId() == R.id.cbx) {
				p1.setSelected(!p1.isSelected());
				toggleChecked(p1.isSelected(), (AppInfo) p1.getTag());
				return;
			} else if (tag instanceof AppInfo) {
				i1 = packageManager.getLaunchIntentForPackage(
					((AppInfo) tag).packageName);
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
			if (tag instanceof AppInfo) {
				AppInfo tag2 = (AppInfo) tag;
				toggleChecked(!selectedInList1.contains(tag2), tag2);
			} else {
				AppInfo tag2 = (AppInfo) ((ViewHolder) tag).cbx.getTag();
				toggleChecked(!selectedInList1.contains(tag2), tag2);
			}
			return false;
		}

		private void showPopup(final View v, final AppInfo rowItem) {
			v.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						PopupMenu popupMenu = new PopupMenu(activity, view);
						popupMenu
							.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
								@Override
								public boolean onMenuItemClick(MenuItem item) {
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
											//int color1 = Color.parseColor(PreferenceUtils.getAccentString(AppsFragment.this.sharedPref));
											//activity.getFutils().shareFiles(arrayList2, activity, AppsFragment.this.theme1, color1);
											new Futils().shareFiles(arrayList2, activity, activity.getAppTheme(), accentColor);
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
									}
									return false;
								}
							});
						popupMenu.inflate(R.menu.app_commands);
						popupMenu.show();
					}
				});
		}

		public void uninstall(final AppInfo item) {
			final BaseFile f1 = new BaseFile(item.path);
			f1.setMode(OpenMode.ROOT);
			ApplicationInfo info1 = AndroidUtils.getAppInfo(AppsFragment.this.getContext(), item.packageName);
			//int color= Color.parseColor(PreferenceUtils.getAccentString(AppsFragment.this.sharedPref));
			//arrayList.add(utils.newElement(Icons.loadMimeIcon(getActivity(), f1.path, false), f1.path, null, null, utils.getSize(f1),"", false));
			//utils.deleteFiles(arrayList, null, arrayList1);
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
								new DeleteTask(AppsFragment.this.getContext().getContentResolver(), AppsFragment.this.getContext()).execute((files));
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
//			File dst = new File(BACKUP_PATH);
//			ExplorerActivity mainActivity = (ExplorerActivity)AppsFragment.this.getActivity();
//			int mode = new MainActivityHelper(mainActivity).checkFolder(dst, mainActivity);
//			if (mode == 1 || mode == 2) {
		BaseFile baseFile=RootHelper.generateBaseFile(f, true);
		baseFile.setName(title + "_" + version + ".apk");
		Log.d("AppsFragment", baseFile.toString());
//				Log.d(TAG, dst.path);
		//Log.d("AppsAdapter", new ObjectDumper(rowItem).dump() + "");
		ab.add(baseFile);
		new CopyFileCheck(fileFrag, BACKUP_PATH, false, fileFrag.activity, ThemedActivity.rootMode).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ab);

//				if (mode == 2) {
//					mainActivity.oparrayList = (ab);
//					mainActivity.operation = DataUtils.COPY;
//					mainActivity.oppathe = BACKUP_PATH;
//				} else if (mode == 1) {//mode == 1 || 
//					Intent intent = new Intent(mainActivity, CopyService.class);
//					intent.putExtra("FILE_PATHS", ab);
//					intent.putExtra("COPY_DIRECTORY", dst.path);
//					intent.putExtra("MODE", 0);
//					mainActivity.startService(intent);
//				}
//			} else {
//				Toast.makeText(mainActivity, R.string.grantfailed, Toast.LENGTH_SHORT);
//			}
	}
}



