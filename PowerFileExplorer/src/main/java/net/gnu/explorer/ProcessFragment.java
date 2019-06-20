package net.gnu.explorer;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.amaze.filemanager.utils.files.Futils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import net.gnu.androidutil.AndroidUtils;
import net.gnu.util.Util;
import android.graphics.PorterDuff;
import net.gnu.common.*;

public class ProcessFragment extends FileFrag implements View.OnClickListener {

	private static final String TAG = "ProcessFragment";

	static final String[] Q = new String[]{"B", "KB", "MB", "GB", "T", "P", "E"};

	private PackageManager pk;
	private ArrayList<RunningAppProcessInfo> display_process = new ArrayList<RunningAppProcessInfo>();


	private ProcessAdapter adapter;
	//HashSet<String> selectedInList1 = new HashSet<>();
	private Drawable apkDrawable;
	private Spinner processType;
	private SearchFileNameTask searchTask = new SearchFileNameTask();
	private List<ProcessInfo> lpinfo = new LinkedList<>();
	//private List<ProcessInfo> tempOriDataSourceL1 = new LinkedList<>();
	private LinkedList<String> killList = new LinkedList<>();
	private LoadProcessTask proTask = new LoadProcessTask();//save, 0, 0

	private final String[] appTypeArr = new String[] {
		"All",
		"System App",
		"Updated System App",
		"User App",
		"Internal",
		"External Asec",
		"Foreground",
		"Background",
		"Visible",
		"Perceptible",
		"Service",
		"Sleep",
		"Gone",
		"Empty"};
	//private final ArrayList<ProcessInfo> tempSelectedInList1 = new ArrayList<>();

	private TextSearch textSearch = new TextSearch();
	private ProcessSorter processSorter;

	public ProcessFragment() {
		super();
		type = Frag.TYPE.PROCESS;
		title = "Process";
	}

//	@Override
//	void clone(final ProcessFragment frag) {
//		if (!fake) {
//			selectedInList1 = frag.selectedInList1;
//			lpinfo = frag.lpinfo;
//			prevInfo = frag.prevInfo;
//			searchMode = frag.searchMode;
//			searchVal = frag.searchVal;
//			adapter = frag.adapter;
//			if (listView != null && listView.getAdapter() != adapter) {
//				listView.setAdapter(adapter);
//			}
//			fake = true;
//
//			if (frag.selStatus != null) {
//				final int visibility = frag.selStatus.getVisibility();
//				if (selStatus.getVisibility() != visibility) {
//					selStatus.setVisibility(visibility);
//					horizontalDivider0.setVisibility(visibility);
//					horizontalDivider12.setVisibility(visibility);
//					status.setVisibility(visibility);
//				}
//				selectionStatus1.setText(frag.selectionStatus1.getText());
//				diskStatus.setText(frag.diskStatus.getText());
//			}
//		}
//	}

	@Override
	public void clone(final Frag frag, final boolean fake) {
		super.clone(frag, fake);
		if (frag instanceof ProcessFragment) {//} && ((ProcessFragment)frag).gridLayoutManager != null) {
			final ProcessFragment appsFragment = (ProcessFragment)frag;
			lpinfo = appsFragment.lpinfo;
			adapter = appsFragment.adapter;
			//processSorter = appsFragment.processSorter;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		return inflater.inflate(R.layout.pager_item_process, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState) {
		super.onViewCreated(v, savedInstanceState);
		Log.d(TAG, "onViewCreated");
//Toast.makeText(ProcessManager.this," allocated size  = " + getAsString(Debug.getNativeHeapAllocatedSize()), 1).show();      
		pk = activity.getPackageManager();
		apkDrawable = getResources().getDrawable(R.drawable.ic_doc_apk);

		processType = (Spinner) v.findViewById(R.id.processType);
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
			activity, android.R.layout.simple_spinner_item, appTypeArr);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		processType.setAdapter(spinnerAdapter);
		processType.setOnItemSelectedListener(new ItemSelectedListener());
		processType.setSelection(3);

		v.findViewById(R.id.backup).setOnClickListener(this);
		v.findViewById(R.id.unins).setOnClickListener(this);
		v.findViewById(R.id.share).setOnClickListener(this);
		v.findViewById(R.id.shortcuts).setOnClickListener(this);
		v.findViewById(R.id.kill).setOnClickListener(this);


		clearButton.setOnClickListener(this);
		searchButton.setOnClickListener(this);
		searchET.addTextChangedListener(textSearch);
		
		//listView.setFastScrollEnabled(true);
		//mylist.setOnTouchListener(this);
		listView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		adapter = new ProcessAdapter(lpinfo);
		listView.setAdapter(adapter);
		spanCount = AndroidUtils.getSharedPreference(getContext(), "SPAN_COUNT.ProcessFrag", 1);
		gridLayoutManager = new GridLayoutManager(fragActivity, spanCount);
		listView.setLayoutManager(gridLayoutManager);
		if (spanCount <= 2) {
			dividerItemDecoration = new GridDividerItemDecoration(fragActivity, true);
			//dividerItemDecoration = new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST, true, true);
			listView.addItemDecoration(dividerItemDecoration);
		}
		listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
				@Override
				public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
					//Log.d(TAG, "onScroll firstVisibleItem=" + firstVisibleItem + ", visibleItemCount=" + visibleItemCount + ", totalItemCount=" + totalItemCount);
					if (System.currentTimeMillis() - lastScroll > 50) {//!mScaling && 
						if (dy > activity.density << 4 && selStatusLayout.getVisibility() == View.VISIBLE) {
							selStatusLayout.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_bottom));
							selStatusLayout.setVisibility(View.GONE);
							horizontalDivider0.setVisibility(View.GONE);
							horizontalDivider12.setVisibility(View.GONE);
							sortBarLayout.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_bottom));
							sortBarLayout.setVisibility(View.GONE);
						} else if (dy < -activity.density << 4 && selStatusLayout.getVisibility() == View.GONE) {
							selStatusLayout.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
							selStatusLayout.setVisibility(View.VISIBLE);
							horizontalDivider0.setVisibility(View.VISIBLE);
							horizontalDivider12.setVisibility(View.VISIBLE);
							sortBarLayout.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
							sortBarLayout.setVisibility(View.VISIBLE);
						}
						lastScroll = System.currentTimeMillis();
					}
				}});
		final Bundle args = getArguments();

		if (args != null) {
			title = args.getString("title");
		}

		if (savedInstanceState == null) {
			//title = savedInstanceState.getString("title");
			//type = ContentFactory.TYPE.values()[savedInstanceState.getInt("type")];
			//} else {
			loadlist(false);
		}

//		Sp = PreferenceManager.getDefaultSharedPreferences(getContext());
//		int theme = Sp.getInt("theme", 0);
//		theme1 = theme == 2 ? PreferenceUtils.hourOfDay() : theme;
		updateColor(null);
		final String order = AndroidUtils.getSharedPreference(activity, "ProcessSorter.order", "Name ▲");
		allName.setText("Name");
		allSize.setText("Size");
		allType.setText("Status");
		switch (order) {
			case "Name ▼":
				processSorter = new ProcessSorter(ProcessSorter.BY_LABEL, ProcessSorter.DESC);
				allName.setText("Name ▼");
				break;
			case "Size ▲":
				processSorter = new ProcessSorter(ProcessSorter.BY_SIZE, ProcessSorter.ASC);
				allSize.setText("Size ▲");
				break;
			case "Size ▼":
				processSorter = new ProcessSorter(ProcessSorter.BY_SIZE, ProcessSorter.DESC);
				allSize.setText("Size ▼");
				break;
			case "Status ▲":
				processSorter = new ProcessSorter(ProcessSorter.BY_STATUS, ProcessSorter.ASC);
				allType.setText("Status ▲");
				break;
			case "Status ▼":
				processSorter = new ProcessSorter(ProcessSorter.BY_STATUS, ProcessSorter.DESC);
				allType.setText("Status ▼");
				break;
			default:
				processSorter = new ProcessSorter(ProcessSorter.BY_LABEL, ProcessSorter.ASC);
				allName.setText("Name ▲");
				break;
		}
	}

//	@Override
//	public void onSaveInstanceState(android.os.Bundle outState) {
//        super.onSaveInstanceState(outState);
//		Log.d(TAG, "onSaveInstanceState" + title + ", " + outState);
//		outState.putString("title", title);
//    }

	@Override
    public void onRefresh() {
        final Editable s = searchET.getText();
		if (s.length() > 0) {
			textSearch.afterTextChanged(s);
		} else {
			loadlist(false);
		}
    }

	public void updateColor(View rootView) {
		getView().setBackgroundColor(Constants.BASE_BACKGROUND);
		icons.setColorFilter(Constants.TEXT_COLOR);
		allName.setTextColor(Constants.TEXT_COLOR);
		//allDate.setTextColor(Constants.TEXT_COLOR);
		allSize.setTextColor(Constants.TEXT_COLOR);
		allType.setTextColor(Constants.TEXT_COLOR);
		searchET.setTextColor(Constants.TEXT_COLOR);
		clearButton.setColorFilter(Constants.TEXT_COLOR);
		searchButton.setColorFilter(Constants.TEXT_COLOR);
		rightStatus.setTextColor(Constants.TEXT_COLOR);
		selectionStatusTV.setTextColor(Constants.TEXT_COLOR);

		if (Constants.BASE_BACKGROUND < 0xff808080) {
			processType.setPopupBackgroundResource(R.drawable.textfield_black);
		} else {
			processType.setPopupBackgroundResource(R.drawable.textfield_default_old);
		}

		horizontalDivider0.setBackgroundColor(Constants.DIVIDER_COLOR);
		horizontalDivider12.setBackgroundColor(Constants.DIVIDER_COLOR);
		horizontalDivider7.setBackgroundColor(Constants.DIVIDER_COLOR);

		adapter.notifyDataSetChanged();

	}

	private class LoadProcessTask extends AsyncTask<Void, Void, List<ProcessInfo>> {
//		private int index, top;
//		private boolean save;

//		public LoadProcessTask(boolean save, int top, int index) {
//			this.save = save;
//			this.index = index;
//			this.top = top;
//		}

		protected void onPreExecute() {
//			availMem_label.setText("calculating...");
//			selectionStatus1.setText("Listing Processes...");
			if (!mSwipeRefreshLayout.isRefreshing()) {
				mSwipeRefreshLayout.setRefreshing(true);
			}
		}

		@Override
		protected List<ProcessInfo> doInBackground(Void... params) {
			final List<ProcessInfo> tempInfo = new LinkedList<>();

			display_process.clear();
			display_process.addAll(((ActivityManager)getContext().getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses());

			ApplicationInfo appinfo = null;
			String label;
			for (RunningAppProcessInfo r : display_process) {
				try {
					appinfo = pk.getApplicationInfo(r.processName, 0);
					label = appinfo.loadLabel(pk) + "";
				} catch (NameNotFoundException e1) {
					//e1.printStackTrace();
					label = r.processName.substring(r.processName.lastIndexOf(".") + 1);
				}
				if (appinfo != null) {
					tempInfo.add(new ProcessInfo(r, label, r.processName, r.importance, r.pid, new File(appinfo.publicSourceDir).length(), appinfo));
				} else {
					tempInfo.add(new ProcessInfo(r, label, r.processName, r.importance, r.pid, 0, null));
				}
			}
			Collections.sort(tempInfo, processSorter);

			return tempInfo;
		}

		@Override
		protected void onPostExecute(List<ProcessInfo> tempInfo) {
			if (isCancelled()) {
				return;
			}
			lpinfo.clear();
			lpinfo.addAll(tempInfo);//tempOriDataSourceL1);
			synchronized (tempOriDataSourceL1) {
				tempOriDataSourceL1.clear();
				tempOriDataSourceL1.addAll(tempInfo);
			}

			updateStatus();
			//listView.setSelectionFromTop(index, top);
			for (String kSt : killList) {
				boolean exist = false;
				for (ProcessInfo pi : lpinfo) {
					if (kSt.equals(pi.packageName)) {
						exist = true;
						break;
					}
				}
				if (exist) {
					showToast(kSt + " cannot be killed");
				} else {
					showToast(kSt + " was killed");
				}
			}
			killList.clear();
			new ItemSelectedListener().onItemSelected(null, null, processType.getSelectedItemPosition(), 0);
			if (mSwipeRefreshLayout.isRefreshing()) {
				mSwipeRefreshLayout.setRefreshing(false);
			}
		}
	}

	public void loadlist(boolean save) {
		if (proTask.getStatus() == AsyncTask.Status.RUNNING 
			|| proTask.getStatus() == AsyncTask.Status.PENDING) {
			proTask.cancel(true);
		}
//		if (save) {
//			final int index = listView.getFirstVisiblePosition();
//			final View vi = listView.getChildAt(0);
//			final int top = (vi == null) ? 0 : vi.getTop();
//			proTask = new LoadProcessTask(save, top, index);
//		} else {
		proTask = new LoadProcessTask();//save, 0, 0
		//}
		proTask.execute();
	}

	public void manageUi(boolean search) {
		if (search == true) {
			searchET.setHint("Search ");
			searchButton.setImageResource(R.drawable.ic_arrow_back_white_36dp);
			//topflipper.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_bottom));
			topflipper.setDisplayedChild(topflipper.indexOfChild(quickLayout));
			searchMode = true;
			searchET.requestFocus();
			imm.showSoftInput(searchET, InputMethodManager.SHOW_IMPLICIT);
		} else {
			imm.hideSoftInputFromWindow(searchET.getWindowToken(), 0);
			searchET.setText("");
			searchButton.setImageResource(R.drawable.ic_action_search);
			//topflipper.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
			topflipper.setDisplayedChild(topflipper.indexOfChild(processType));
			searchMode = false;//curContentFrag.
			loadlist(false);//slideFrag.getCurrentFragment().
		}
	}

	private class ItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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
				if (searchTask != null
					&& searchTask.getStatus() == AsyncTask.Status.RUNNING) {
					searchTask.cancel(true);
				}
				searchTask = new SearchFileNameTask();
				searchTask.execute(filesearch);
			}
		}

		public void onTextChanged(CharSequence s, int start, int end, int count) {
		}
	}

	private class SearchFileNameTask extends AsyncTask<Object, Long, List<ProcessInfo>> {
		protected void onPreExecute() {
			if (!mSwipeRefreshLayout.isRefreshing()) {
				mSwipeRefreshLayout.setRefreshing(true);
			}
		}

		@Override
		protected List<ProcessInfo> doInBackground(Object... params) {
			final List<ProcessInfo> templpinfo = new LinkedList<>();
			if (params[0] instanceof String) {
				searchMode = true;
				searchVal = searchET.getText().toString();
				final String param = (String)params[0];
				synchronized (tempOriDataSourceL1) {
					for (ProcessInfo pi : tempOriDataSourceL1) {
						if (pi.label.contains(param) || pi.packageName.contains(param)) {
							templpinfo.add(pi);
						}
					}
				}
			} else {
				int sel = params[0];
				synchronized (tempOriDataSourceL1) {
					if (sel == 0) {
						templpinfo.addAll(tempOriDataSourceL1);
					} else {
						for (ProcessInfo pi : tempOriDataSourceL1) {
							if (sel == 1 && pi.isSystemApp) {
								templpinfo.add(pi);
							} else if (sel == 2 && pi.isUpdatedSystemApp) {
								templpinfo.add(pi);
							} else if (sel == 3 && !pi.isSystemApp) {
								templpinfo.add(pi);
							} else if (sel == 4 && pi.isInternal) {
								templpinfo.add(pi);
							} else if (sel == 5 && pi.isExternalAsec) {
								templpinfo.add(pi);
							} else if (sel == 6 && pi.status == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
								templpinfo.add(pi);
							} else if (sel == 7 && pi.status == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
								templpinfo.add(pi);
							} else if (sel == 8 && pi.status == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
								templpinfo.add(pi);
							} else if (sel == 9 && pi.status == ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE) {
								templpinfo.add(pi);
							} else if (sel == 10 && pi.status == ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
								templpinfo.add(pi);
							} else if (sel == 11 && pi.status == 150) {
								templpinfo.add(pi);
							} else if (sel == 12 && pi.status == ActivityManager.RunningAppProcessInfo.IMPORTANCE_GONE) {
								templpinfo.add(pi);
							} else if (sel == 13 && pi.status == ActivityManager.RunningAppProcessInfo.IMPORTANCE_EMPTY) {
								templpinfo.add(pi);
							} 
						}
					}
				}
			}
			return templpinfo;
		}

		@Override
		protected void onPostExecute(List<ProcessInfo> templpinfo) {
			if (isCancelled()) {
				return;
			}
			Collections.sort(templpinfo, processSorter);
			lpinfo.clear();
			lpinfo.addAll(templpinfo);
			updateStatus();
			if (mSwipeRefreshLayout.isRefreshing()) {
				mSwipeRefreshLayout.setRefreshing(false);
			}
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
//        mi = menu.findItem(R.id.hide);
//		if (activity.left.getVisibility() == View.VISIBLE) {
//			mi.setTitle("Hide");
//		} else {
//			mi.setTitle("2 panels");
//		}
//        mi = menu.findItem(R.id.biggerequalpanel);
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
////							ExplorerActivity.SPAN_COUNT = 3;
////							AndroidUtils.setSharedPreference(getContext(), "SPAN_COUNT", ExplorerActivity.SPAN_COUNT);
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
//					update_labels(getContext());
//					return true;
//				}
//			});
//		popup.show();
//	}

	void rangeSelection() {
		int min = Integer.MAX_VALUE, max = -1;
		int cur = -3;
		tempSelectedInList1.clear();
		for (String s : selectedInList1) {
			//cur = lpinfo.indexOf(s);
			int i = 0;
			for (ProcessInfo pi : lpinfo) {
				if (s.equals(pi.packageName)) {
					cur = i;
				} else {
					i++;
					tempSelectedInList1.add(pi);
				}
			}
			if (cur > max) {
				max = cur;
			}
			if (cur < min && cur >= 0) {
				min = cur;
			}
		}
		selectedInList1.clear();
		for (cur = min; cur <= max; cur++) {
			selectedInList1.add(lpinfo.get(cur).packageName);
		}
		updateStatus();
	}

	void inversion() {
		tempSelectedInList1.clear();
		//tempSelectedInList1.addAll(selectedInList1);
		final ArrayList<ProcessInfo> listTemp = new ArrayList<>(4096);
		for (ProcessInfo f : lpinfo) {
			if (!selectedInList1.contains(f.packageName)) {
				listTemp.add(f);
			} else {
				tempSelectedInList1.add(f);
			}
		}
		selectedInList1.clear();
		for (ProcessInfo f : listTemp) {
			selectedInList1.add(f.packageName);
		}
		updateStatus();
	}

	void clearSelection() {
		tempSelectedInList1.clear();
		String label;
		ApplicationInfo appinfo;
		for (RunningAppProcessInfo r : display_process) {
			appinfo = null;
			if (selectedInList1.contains(r.processName)) {
				try {
					appinfo = pk.getApplicationInfo(r.processName, 0);
					label = appinfo.loadLabel(pk) + "";
				} catch (NameNotFoundException e1) {	
					//e1.printStackTrace();
					label = r.processName.substring(r.processName.lastIndexOf(".") + 1);
				}
				if (appinfo != null) {
					tempSelectedInList1.add(new ProcessInfo(r, label, r.processName, r.importance, r.pid, new File(appinfo.publicSourceDir).length(), appinfo));
				} else {
					tempSelectedInList1.add(new ProcessInfo(r, label, r.processName, r.importance, r.pid, 0, null));
				}
			}
		}
		selectedInList1.clear();
		updateStatus();
	}

	void undoSelection() {
		final ArrayList listTemp = new ArrayList<>(selectedInList1);
		selectedInList1.clear();
		for (ProcessInfo f : tempSelectedInList1) {
			selectedInList1.add(f.packageName);
		}
		tempSelectedInList1.clear();
		tempSelectedInList1.addAll(listTemp);
		updateStatus();
	}

//	public void fromActivity(final View v) {
//		//final Futils utils = utilsProvider.getFutils();
//        PopupMenu popup = new PopupMenu(v.getContext(), v);
//        popup.inflate(R.menu.process);
//        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//				public boolean onMenuItemClick(MenuItem item) {
//					switch (item.getItemId()) {
//						case R.id.kill:
//							killList.clear();
//							for (String p : selectedInList1) {
//								try {
//									for (ProcessInfo pi : lpinfo) {
//										if (pi.packageName.equals(p)) {
//											AndroidUtils.killProcess(activity, pi.pid, p);
//											killList.add(p);
//										}
//									}
//								} catch (Exception e) {
//									e.printStackTrace();
//								}
//							}
//							loadlist(true);
//							Toast.makeText(activity, selectedInList1.size() + " processes were killed !", Toast.LENGTH_SHORT).show();
//							break;
//						case R.id.share:
//							ArrayList<File> arrayList2 = new ArrayList<File>();
//							ArrayList<String> apkPath = AndroidUtils.getApkPath(activity);
//							Collections.sort(apkPath);
//							for (String pi : selectedInList1) {
//								int binarySearch = Collections.binarySearch(apkPath, pi);
//								if (binarySearch >= 0) {
//									arrayList2.add(new File(apkPath.get(binarySearch)));
//								}
//							}
////							int color1 = Color.parseColor(PreferenceUtils
////														  .getAccentString(sharedPref));
////							new Futils().shareFiles(arrayList2, activity, theme1,
////													color1);
//							new Futils().shareFiles(arrayList2, activity, activity.getAppTheme(), accentColor);
//							break;
//						case R.id.backup:
//							Toast.makeText(
//								getContext(),
//								getResources().getString(R.string.copyingapk)
//								+ AppsFragment.BACKUP_PATH, Toast.LENGTH_LONG).show();
//							PackageManager pm = activity.getPackageManager();
//							for (String pi : selectedInList1) {
//								PackageInfo info = AndroidUtils.getPackageInfo(activity, pi);
//								if (info != null) {
//									ApplicationInfo applicationInfo = info.applicationInfo;
//									if (applicationInfo != null) {
//										AppsFragment.backup(applicationInfo.publicSourceDir, applicationInfo.loadLabel(pm) + "",
//															info.versionName, ProcessFragment.this);
//									} else {
//										Toast.makeText(activity, pi + " cannot be accessed", Toast.LENGTH_SHORT).show();
//									}
//								} else {
//									Toast.makeText(activity, pi + " cannot be accessed", Toast.LENGTH_SHORT).show();
//								}
//							}
//							break;
//						case R.id.unins:
//							for (String pi : selectedInList1) {
//								AndroidUtils.uninstall(activity, pi);
//							}
//					}
//					return true;
//				}
//			});
//		popup.show();
//	}

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
				adapter.toggleChecked(all);
				updateStatus();
				break;
			case R.id.allName:
				if (allName.getText().toString().equals("Name ▲")) {
					allName.setText("Name ▼");
					processSorter = new ProcessSorter(ProcessSorter.BY_LABEL, ProcessSorter.DESC);
					AndroidUtils.setSharedPreference(getContext(), "ProcessSorter.order", "Name ▼");
				} else {
					allName.setText("Name ▲");
					processSorter = new ProcessSorter(ProcessSorter.BY_LABEL, ProcessSorter.ASC);
					AndroidUtils.setSharedPreference(getContext(), "ProcessSorter.order", "Name ▲");
				}
				allSize.setText("Size");
				allType.setText("Status");
				Collections.sort(lpinfo, processSorter);
				adapter.notifyDataSetChanged();
				break;
			case R.id.allType:
				if (allType.getText().toString().equals("Status ▲")) {
					allType.setText("Status ▼");
					processSorter = new ProcessSorter(ProcessSorter.BY_STATUS, ProcessSorter.DESC);
					AndroidUtils.setSharedPreference(getContext(), "ProcessSorter.order", "Status ▼");
				} else {
					allType.setText("Status ▲");
					processSorter = new ProcessSorter(ProcessSorter.BY_STATUS, ProcessSorter.ASC);
					AndroidUtils.setSharedPreference(getContext(), "ProcessSorter.order", "Status ▲");
				}
				allName.setText("Name");
				allSize.setText("Size");
				Collections.sort(lpinfo, processSorter);
				adapter.notifyDataSetChanged();
				break;
			case R.id.allSize:
				if (allSize.getText().toString().equals("Size ▲")) {
					allSize.setText("Size ▼");
					processSorter = new ProcessSorter(ProcessSorter.BY_SIZE, ProcessSorter.DESC);
					AndroidUtils.setSharedPreference(getContext(), "ProcessSorter.order", "Size ▼");
				} else {
					allSize.setText("Size ▲");
					processSorter = new ProcessSorter(ProcessSorter.BY_SIZE, ProcessSorter.ASC);
					AndroidUtils.setSharedPreference(getContext(), "ProcessSorter.order", "Size ▲");
				}
				allName.setText("Name");
				allType.setText("Status");
				Collections.sort(lpinfo, processSorter);
				adapter.notifyDataSetChanged();
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
			case R.id.kill:
				killList.clear();
				for (String p : selectedInList1) {
					try {
						for (ProcessInfo pi : lpinfo) {
							if (pi.packageName.equals(p)) {
								AndroidUtils.killProcess(activity, pi.pid, p);
								killList.add(p);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				loadlist(true);
				Toast.makeText(activity, selectedInList1.size() + " processes were killed !", Toast.LENGTH_SHORT).show();
				break;
			case R.id.share:
				ArrayList<File> arrayList2 = new ArrayList<File>();
				ArrayList<String> apkPath = AndroidUtils.getApkPath(activity);
				Collections.sort(apkPath);
				for (String pi : selectedInList1) {
					int binarySearch = Collections.binarySearch(apkPath, pi);
					if (binarySearch >= 0) {
						arrayList2.add(new File(apkPath.get(binarySearch)));
					}
				}
				Futils.shareFiles(arrayList2, activity, activity.getAppTheme(), accentColor);
				break;
			case R.id.backup:
				Toast.makeText(
					getContext(),
					getResources().getString(R.string.copyingapk)
					+ AppsFragment.BACKUP_PATH, Toast.LENGTH_LONG).show();
				PackageManager pm = activity.getPackageManager();
				for (String pi : selectedInList1) {
					PackageInfo info = AndroidUtils.getPackageInfo(activity, pi);
					if (info != null) {
						ApplicationInfo applicationInfo = info.applicationInfo;
						if (applicationInfo != null) {
							AppsFragment.backup(applicationInfo.publicSourceDir, applicationInfo.loadLabel(pm) + "",
												info.versionName, ProcessFragment.this);
						} else {
							Toast.makeText(activity, pi + " cannot be accessed", Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(activity, pi + " cannot be accessed", Toast.LENGTH_SHORT).show();
					}
				}
				break;
			case R.id.unins:
				for (String pi : selectedInList1) {
					AndroidUtils.uninstall(activity, pi);
				}
		}
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected");
		return super.onOptionsItemSelected(item);
    }


	@Override
	public void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
		if (Build.VERSION.SDK_INT > 23) {
			final IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
			intentFilter.addDataScheme("package");
			activity.registerReceiver(br, intentFilter);
//			if (pk == null) {
//				pk = getContext().getPackageManager();
//			}
		}
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		if (Build.VERSION.SDK_INT <= 23) {
			final IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
			intentFilter.addDataScheme("package");
			activity.registerReceiver(br, intentFilter);
//			if (pk == null) {
//				pk = getContext().getPackageManager();
//			}
		}
	}

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
		super.onPause();
		if (Build.VERSION.SDK_INT <= 23) {
			searchTask.cancel(true);
			proTask.cancel(true);
			activity.unregisterReceiver(br);
		}
    }

	@Override
	public void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		if (Build.VERSION.SDK_INT > 23) {
			searchTask.cancel(true);
			proTask.cancel(true);
			activity.unregisterReceiver(br);
		}
	}

	private final BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                loadlist(true);
            }
		}
    };

	void updateStatus() {
		final MemoryInfo mem_info = new ActivityManager.MemoryInfo();
		final ActivityManager systemService = (ActivityManager)activity.getSystemService(Context.ACTIVITY_SERVICE);
		systemService.getMemoryInfo(mem_info);
		rightStatus.setText(String.format("Available memory: %s B", Util.nf.format((mem_info.availMem)) + "/" + Util.nf.format(mem_info.totalMem)));
		//numProc_label.setText("Number of processes: " + display_process.size());
		selectionStatusTV.setText(selectedInList1.size() + "/" + lpinfo.size() + "/" + display_process.size());
		adapter.notifyDataSetChanged();
	}

	private class ProcessAdapter extends RecyclerAdapter<ProcessInfo, ProcessAdapter.ViewHolder> implements OnClickListener, OnLongClickListener {

		public ProcessAdapter(final List<ProcessInfo> lpinfo) {
			//loadProcess();
			super(lpinfo);//, R.layout.list_item_process, lpinfo);
		}

		class ViewHolder extends RecyclerView.ViewHolder {
			private final View ll;
			private final TextView name;
			private final TextView items;
			private final TextView attr;

			private final TextView type;
			private final ImageButton cbx;
			private final ImageView image;
			private final ImageButton more;

			ViewHolder(final View convertView) {
				super(convertView);
				name = (TextView) convertView.findViewById(R.id.name);
				items = (TextView) convertView.findViewById(R.id.items);
				attr = (TextView) convertView.findViewById(R.id.attr);
				//lastModified = (TextView) convertView.findViewById(R.id.lastModified);
				type = (TextView) convertView.findViewById(R.id.type);
				cbx = (ImageButton) convertView.findViewById(R.id.cbx);
				image = (ImageView)convertView.findViewById(R.id.icon);
				more = (ImageButton)convertView.findViewById(R.id.more);
				convertView.setTag(this);
				ll = convertView;

				ll.setOnClickListener(ProcessAdapter.this);
				more.setOnClickListener(ProcessAdapter.this);
				cbx.setOnClickListener(ProcessAdapter.this);

				ll.setOnLongClickListener(ProcessAdapter.this);
				more.setOnLongClickListener(ProcessAdapter.this);
				cbx.setOnLongClickListener(ProcessAdapter.this);

				more.setColorFilter(Constants.TEXT_COLOR);
				name.setTextColor(Constants.DIR_COLOR);
				items.setTextColor(Constants.TEXT_COLOR);
				attr.setTextColor(Constants.TEXT_COLOR);
				//holder.lastModified.setTextColor(Constants.TEXT_COLOR);
				type.setTextColor(Constants.TEXT_COLOR);

			}

			private void bind(final int position) {
				final ProcessInfo pi = lpinfo.get(position);

				cbx.setTag(pi);
				more.setTag(pi);

				name.setText(pi.label);
				attr.setText(pi.packageName);
				items.setText(Util.nf.format(pi.size) + " B");

				int importance = pi.status;
				if (importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
					type.setText("Foreground");
				} else if (importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE) {
					type.setText("Foreground Service");
				} else if (importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
					type.setText("Background");
				} else if (importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
					type.setText("Visible");
				} else if (importance == RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE) {
					type.setText("Perceptible");
				} else if (importance == RunningAppProcessInfo.IMPORTANCE_SERVICE) {
					type.setText("Service");
				} else if (importance == 150) {//}RunningAppProcessInfo.IMPORTANCE_TOP_SLEEPING) {
					type.setText("Sleep");
				} else if (importance == RunningAppProcessInfo.IMPORTANCE_GONE) {
					type.setText("Gone");
				} else if (importance == RunningAppProcessInfo.IMPORTANCE_CANT_SAVE_STATE) {
					type.setText("Can't save state");
				} else if (importance == RunningAppProcessInfo.IMPORTANCE_EMPTY) {
					type.setText("Empty");
				}

				try {
					image.setImageDrawable(pk.getApplicationIcon(pi.packageName));
				} catch (NameNotFoundException e) {
					image.setImageResource(R.drawable.ic_doc_apk);
				}

				final boolean checked = selectedInList1.contains(pi.packageName);
				if (checked) {
					ll.setBackgroundColor(Constants.IN_DATA_SOURCE_2);
					cbx.setSelected(true);
					cbx.setImageResource(R.drawable.ic_accept);
				} else if (selectedInList1.size() > 0) {
					ll.setBackgroundColor(Constants.BASE_BACKGROUND);
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
		public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
			final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_process, parent, false);
			// set the view's size, margins, paddings and layout parameters
			final ViewHolder vh = new ViewHolder(v);
			return vh;
		}

		@Override
		public void onBindViewHolder(final ViewHolder holder, final int position) {
			holder.bind(position);
		}

		public void toggleChecked(final boolean checked, final ProcessInfo packageInfo) {
			if (checked) {
				selectedInList1.add(packageInfo.packageName);
			} else {
				selectedInList1.remove(packageInfo.packageName);
			}
			updateStatus();
			final boolean all = selectedInList1.size() == display_process.size();
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

		public void toggleChecked(final boolean b) {
			if (b) {
				selectedInList1.clear();
				for (RunningAppProcessInfo r : display_process) {
					selectedInList1.add(r.processName);
				}
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
		public boolean onLongClick(final View p1) {
			final Object tag = p1.getTag();
			final ProcessInfo tag2;
			if (tag instanceof ProcessInfo) {
				tag2 = (ProcessInfo) tag;
			} else {
				tag2 = (ProcessInfo) ((ViewHolder) tag).cbx.getTag();
			}
			toggleChecked(!selectedInList1.contains(tag2.packageName), tag2);
			return false;
		}

		@Override
		public void onClick(final View view) {
			Log.d(TAG, view.getTag() + ".");
//			if (selectedInList1.size() > 0) {
//				onLongClick(view);
//				return;
//			}
			final int id = view.getId();
			if (id == R.id.cbx) {
				view.setSelected(!view.isSelected());
				toggleChecked(view.isSelected(), (ProcessInfo) view.getTag());
			} else if (id == R.id.more) {
				final ProcessInfo pinfo = (ProcessInfo) view.getTag();
				final MenuBuilder menuBuilder = new MenuBuilder(activity);
				final MenuInflater inflater = new MenuInflater(activity);
				inflater.inflate(R.menu.process, menuBuilder);
				final MenuPopupHelper optionsMenu = new MenuPopupHelper(activity , menuBuilder, allSize);
				optionsMenu.setForceShowIcon(true);

				Drawable icon = menuBuilder.findItem(R.id.shortcut).getIcon();
				icon.setColorFilter(Constants.TEXT_COLOR, PorterDuff.Mode.SRC_IN);

				icon = menuBuilder.findItem(R.id.properties).getIcon();
				icon.setColorFilter(Constants.TEXT_COLOR, PorterDuff.Mode.SRC_IN);

				menuBuilder.findItem(R.id.play).getIcon().setColorFilter(Constants.TEXT_COLOR, PorterDuff.Mode.SRC_IN);

				menuBuilder.setCallback(new MenuBuilder.Callback() {
						@Override
						public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
							switch (item.getItemId()) {
								case R.id.kill:
									try {
										AndroidUtils.killProcess(ProcessFragment.this.getContext(), pinfo.pid, pinfo.packageName);
										killList.clear();
										killList.add(pinfo.packageName);
									} catch (Exception e) {
										Toast.makeText(ProcessFragment.this.getContext(), "couldn't kill the process ", Toast.LENGTH_SHORT).show();
									}
									//Toast.makeText(ProcessFragment.this.getContext(), pinfo.label + " was killed !", Toast.LENGTH_SHORT).show();
									loadlist(true);
									break;
								case R.id.open:
									Intent i = pk.getLaunchIntentForPackage(pinfo.packageName);
									if (i != null)
										startActivity(i);
									else
										Toast.makeText(ProcessFragment.this.getContext(), "Could not launch", Toast.LENGTH_SHORT).show();
									break;
								case R.id.backup:
									final PackageInfo info = AndroidUtils.getPackageInfo(activity, pinfo.packageName);
									if (info != null) {
										ApplicationInfo applicationInfo = info.applicationInfo;
										if (applicationInfo != null) {
											AppsFragment.backup(applicationInfo.publicSourceDir, applicationInfo.loadLabel(activity.getPackageManager()) + "",
																info.versionName, ProcessFragment.this);
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
										Toast.makeText(ProcessFragment.this.getContext(), "Can't Uninstall" , Toast.LENGTH_SHORT).show();
									}
									break;
								case R.id.properties:
									//Toast.makeText(ProcessManager.this, "Process : "+display_process.get(position).processName +" lru : " +display_process.get(position).lru + " Pid :  " +display_process.get(position).pid, Toast.LENGTH_SHORT).show();	
									final AlertDialog alert1 = new AlertDialog.Builder(ProcessFragment.this.getContext()).create();
									alert1.setTitle("Process Info");
									alert1.setIcon(AndroidUtils.getProcessIcon(pk, pinfo.packageName, apkDrawable));
									alert1.setMessage("Process : " + pinfo.packageName + " \nlru : " + pinfo.runningAppProcessInfo.lru + "\nPid : " + pinfo.pid);
									alert1.show();
									break;
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
									arrayList2.add(new File(pinfo.path));
									//int color1 = Color.parseColor(PreferenceUtils.getAccentString(sharedPref));
									//new Futils().shareFiles(arrayList2, activity, theme1, color1);
									Futils.shareFiles(arrayList2, activity, activity.getAppTheme(), accentColor);
									break;
								case R.id.shortcut:
									//AndroidUtils.createShortCut(AppsFragment.this.getContext(), rowItem.packageName, rowItem.label, image.setImageDrawable(packageManager.getApplicationIcon(appInfo.packageName)));
									return true;
							}
							return true;
						}
						@Override
						public void onMenuModeChange(MenuBuilder menu) {}
					});
				optionsMenu.show();
			} else {
				final int apiLevel = Build.VERSION.SDK_INT;
				final Intent intent = new Intent();
				final ProcessInfo pinfo = (ProcessInfo) ((ViewHolder) view.getTag()).cbx.getTag();
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

	}
}

