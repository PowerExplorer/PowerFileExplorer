package net.gnu.explorer;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amaze.filemanager.activities.ThemedActivity;
import com.amaze.filemanager.database.UtilsHandler;
import com.amaze.filemanager.exceptions.CloudPluginException;
import com.amaze.filemanager.exceptions.RootNotPermittedException;
import com.amaze.filemanager.filesystem.BaseFile;
import com.amaze.filemanager.filesystem.HFile;
import com.amaze.filemanager.filesystem.RootHelper;
import com.amaze.filemanager.fragments.CloudSheetFragment;
import com.amaze.filemanager.services.DeleteTask;
import com.amaze.filemanager.services.asynctasks.CopyFileCheck;
import com.amaze.filemanager.ui.dialogs.GeneralDialogCreation;
import com.amaze.filemanager.ui.icons.MimeTypes;
import com.amaze.filemanager.utils.DataUtils;
import com.amaze.filemanager.utils.MainActivityHelper;
import com.amaze.filemanager.utils.OTGUtil;
import com.amaze.filemanager.utils.OpenMode;
import com.amaze.filemanager.utils.cloud.CloudUtil;
import com.amaze.filemanager.utils.files.EncryptDecryptUtils;
import com.amaze.filemanager.utils.files.Futils;
import com.cloudrail.si.interfaces.CloudStorage;
import java.io.File;
import java.util.regex.Pattern;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import net.gnu.androidutil.AndroidUtils;
import net.gnu.explorer.R;
import net.gnu.util.FileUtil;
import net.gnu.util.Util;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.support.v4.content.ContextCompat.checkSelfPermission;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Collections;
import java.util.Collection;
import java.util.TreeMap;
import java.util.List;
import android.text.format.Formatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Comparator;
import android.widget.ImageView;
import android.os.Parcelable;
import android.os.Parcel;
import net.gnu.util.ComparableEntry;
import java.util.Set;
import java.util.TreeSet;
import net.gnu.p7zip.Andro7za;
import net.gnu.p7zip.Zip;
import net.gnu.zpaq.Zpaq;
import net.gnu.p7zip.DecompressTask;
import net.gnu.p7zip.ZipEntry;
import net.gnu.p7zip.ZipEntrySorter;
import net.gnu.common.*;

public class ZipFragment extends FileFrag implements View.OnClickListener {

    private static final String TAG = "ZipFragment";
	private static final int REQUEST_CODE_STORAGE_PERMISSION = 101;

	private ScaleGestureDetector mScaleGestureDetector;
	private ImageButton dirMore;

	private SearchFileNameTask searchTask = new SearchFileNameTask();
	private TextSearch textSearch = new TextSearch();

	List<ZipEntry> dataSourceL1 = new LinkedList<>();

	ZipEntry tempPreviewL2 = null;
	private Button deletePastesBtn;
	private ZipAdapter srcAdapter;

	private HorizontalScrollView scrolltext;
	private LinearLayout mDirectoryButtons;

	private LoadFiles loadList = new LoadFiles();
	private int file_count, folder_count, columns;

	private ZipEntrySorter zipListSorter;
	private LinkedList<Map<String, Object>> backStack = new LinkedList<>();
	//private LinkedList<String> history = new LinkedList<>();
	private FileObserver mFileObserver;
	private Drawable drawableDelete;
	private Drawable drawablePaste;

	public boolean selection, results = false, SHOW_HIDDEN, CIRCULAR_IMAGES, SHOW_PERMISSIONS, SHOW_SIZE, SHOW_LAST_MODIFIED;

	boolean mWriteableOnly;

	//int totalCount, progress;
	private boolean noMedia = false;
	private boolean displayHidden = true;
	public long totalZipLength;
	public long totalUnzipLength;
	private Andro7za andro7za;
	private Zpaq zpaq;
	public Zip zip;
	private String curPath;

	public ZipFragment() {
		type = TYPE.ZIP;
	}

	@Override
	public String toString() {
		return "type " + type + ", " + slidingTabsFragment.side + ", fake=" + fake + ", currentPathTitle " + currentPathTitle + ", " + super.toString();
	}

	public static ZipFragment newInstance(final SlidingTabsFragment sliding, final String zipPath, Bundle bundle) {
        //Log.d(TAG, "newInstance dir " + dir + ", suffix " + suffix + ", multiFiles " + multiFiles);

		if (bundle == null) {
			bundle = new Bundle();
		}
		bundle.putString(Constants.EXTRA_ABSOLUTE_PATH, zipPath);

		final ZipFragment zipFragment = new ZipFragment();
		zipFragment.setArguments(bundle);
		zipFragment.currentPathTitle = zipPath;

		zipFragment.slidingTabsFragment = sliding;
        Log.d(TAG, "newInstance " + zipFragment);
		return zipFragment;
    }

	@Override
	public void load(final String path) {
		currentPathTitle = path;
		changeDir(currentPathTitle, true, false, null);
	}

	@Override
	public void load(final String path, final Runnable run) {
		currentPathTitle = path;
		changeDir(currentPathTitle, true, false, run);
	}

	@Override
	public Frag clone(boolean fake) {
		final ZipFragment frag = new ZipFragment();
		frag.clone(this, fake);
		return frag;
	}

	@Override
	public void clone(final Frag frag, final boolean fake) {
		final ZipFragment zipFrag = (ZipFragment) frag;
		//Log.i(TAG, "clone " + frag + ", " + contentFrag.currentPathTitle + ", " + contentFrag.currentPathTitle + ", listView " + listView + ", srcAdapter " + srcAdapter + ", gridLayoutManager " + gridLayoutManager);
		type = zipFrag.type;
		currentPathTitle = zipFrag.currentPathTitle;

		slidingTabsFragment = zipFrag.slidingTabsFragment;
		this.fake = fake;
		if (fake) {
			dataSourceL1 = zipFrag.dataSourceL1;
			selectedInList1 = zipFrag.selectedInList1;
			tempSelectedInList1 = zipFrag.tempSelectedInList1;
			tempOriDataSourceL1 = zipFrag.tempOriDataSourceL1;
		} else if (!zipFrag.searchMode) {
			dataSourceL1.clear();
			dataSourceL1.addAll(zipFrag.dataSourceL1);
			selectedInList1.clear();
			selectedInList1.addAll(zipFrag.selectedInList1);
			tempSelectedInList1.clear();
			tempSelectedInList1.addAll(zipFrag.tempSelectedInList1);
			tempOriDataSourceL1.clear();
			tempOriDataSourceL1.addAll(zipFrag.tempOriDataSourceL1);
		}
		spanCount = zipFrag.spanCount;

		tempPreviewL2 = zipFrag.tempPreviewL2;
//		searchMode = contentFrag.searchMode;
//		searchVal = contentFrag.searchVal;
		srcAdapter = zipFrag.srcAdapter;
		if (listView != null && listView.getAdapter() != srcAdapter) {
			listView.setAdapter(srcAdapter);
		}

		if (allCbx != null) {
			if (dataSourceL1.size() == 0) {
				nofilelayout.setVisibility(View.VISIBLE);
				//mSwipeRefreshLayout.setVisibility(View.GONE);
			} else {
				nofilelayout.setVisibility(View.GONE);
				//mSwipeRefreshLayout.setVisibility(View.VISIBLE);
			}
			setDirectoryButtons();

			allName.setText(zipFrag.allName.getText());
			allType.setText(zipFrag.allType.getText());
			allDate.setText(zipFrag.allDate.getText());
			allSize.setText(zipFrag.allSize.getText());
			final int size = selectedInList1.size();
			//Log.d(TAG, "clone " + type + ", size " + size + ", dataSourceL1.size() " + dataSourceL1.size());
			if (size > 0) {
				if (size == dataSourceL1.size()) {
					allCbx.setImageResource(R.drawable.ic_accept);
				} else {
					allCbx.setImageResource(R.drawable.ready);
				}
			} else {
				allCbx.setImageResource(R.drawable.dot);
			}

			if (gridLayoutManager == null || gridLayoutManager.getSpanCount() != spanCount) {
				listView.removeItemDecoration(dividerItemDecoration);
				listView.invalidateItemDecorations();
				gridLayoutManager = new GridLayoutManager(fragActivity, spanCount);
				listView.setLayoutManager(gridLayoutManager);
			}
			final int index = zipFrag.gridLayoutManager.findFirstVisibleItemPosition();
			final View vi = zipFrag.listView.getChildAt(0); 
			final int top = (vi == null) ? 0 : vi.getTop();
			gridLayoutManager.scrollToPositionWithOffset(index, top);
			if (zipFrag.selStatusLayout != null) {
				final int visibility = zipFrag.selStatusLayout.getVisibility();
				if (selStatusLayout.getVisibility() != visibility) {
					selStatusLayout.setVisibility(visibility);
					horizontalDivider0.setVisibility(visibility);
					horizontalDivider12.setVisibility(visibility);
					sortBarLayout.setVisibility(visibility);
				}
				selectionStatusTV.setText(zipFrag.selectionStatusTV.getText());
				rightStatus.setText(zipFrag.rightStatus.getText());
			}
		}
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
        //Log.d(TAG, "onCreateView fake=" + fake + ", dir=" + dir + ", " + savedInstanceState);
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.pager_item, container, false);
    }

	@Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final Bundle args = getArguments();
		Log.d(TAG, "onViewCreated " + toString() + ", savedInstanceState=" + savedInstanceState + ", args " + args);
		super.onViewCreated(view, savedInstanceState);
		andro7za = new Andro7za(fragActivity);
		zpaq = new Zpaq(fragActivity);

		//if (slidingTabsFragment != null) {
		final String order;

		if (slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT) {
			order = AndroidUtils.getSharedPreference(activity, "ZipFragSortTypeR", "Name ▲");
			spanCount = AndroidUtils.getSharedPreference(activity, "ZipFrag.SPAN_COUNTR", 1);
		} else {
			order = AndroidUtils.getSharedPreference(activity, "ZipFragSortTypeL", "Name ▲");
			spanCount = AndroidUtils.getSharedPreference(activity, "ZipFrag.SPAN_COUNTL", 1);
		}

		SHOW_HIDDEN = sharedPref.getBoolean("showHidden", true);

		scrolltext = (HorizontalScrollView) view.findViewById(R.id.scroll_text);
		mDirectoryButtons = (LinearLayout) view.findViewById(R.id.directory_buttons);
		dirMore = (ImageButton) view.findViewById(R.id.dirMore);

		drawableDelete = activity.getDrawable(R.drawable.ic_delete_white_36dp);
		drawablePaste = activity.getDrawable(R.drawable.ic_content_paste_white_36dp);
		deletePastesBtn = (Button) view.findViewById(R.id.deletes_pastes);

		view.findViewById(R.id.copys).setOnClickListener(this);
		view.findViewById(R.id.cuts).setOnClickListener(this);
		deletePastesBtn.setOnClickListener(this);
		view.findViewById(R.id.renames).setOnClickListener(this);
		view.findViewById(R.id.shares).setOnClickListener(this);
		dirMore.setOnClickListener(this);

		view.findViewById(R.id.moreLeft).setVisibility(View.GONE);
		view.findViewById(R.id.moreRight).setVisibility(View.GONE);
		view.findViewById(R.id.infos).setOnClickListener(this);

		final View compresssBtn = view.findViewById(R.id.compresss);
		((Button)compresssBtn).setText("Decompress");
		compresssBtn.setOnClickListener(this);

		if (selectedInList1.size() == 0 && activity.COPY_PATH == null && activity.MOVE_PATH == null) {
			if (commands.getVisibility() == View.VISIBLE) {
				horizontalDivider6.setVisibility(View.GONE);
				commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
				commands.setVisibility(View.GONE);
			}
		} else if (commands.getVisibility() == View.GONE) {
			commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
			commands.setVisibility(View.VISIBLE);
			horizontalDivider6.setVisibility(View.VISIBLE);
		}
		listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
				public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
					//Log.d(TAG, "onScrolled dx=" + dx + ", dy=" + dy + ", density=" + activity.density);
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
				}
			});

		DefaultItemAnimator animator = new DefaultItemAnimator();
		animator.setAddDuration(500);
		listView.setItemAnimator(animator);

		clearButton.setOnClickListener(this);
		searchButton.setOnClickListener(this);

		searchET.addTextChangedListener(textSearch);
		mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {

				@Override
				public boolean onScale(ScaleGestureDetector detector) {
					Log.d(TAG, "onScale getCurrentSpan " + detector.getCurrentSpan() + ", getPreviousSpan " + detector.getPreviousSpan() + ", getTimeDelta " + detector.getTimeDelta());
					//if (detector.getCurrentSpan() > 300 && detector.getTimeDelta() > 50) {
					//Log.d(TAG, "onScale " + (detector.getCurrentSpan() - detector.getPreviousSpan()) + ", getTimeDelta " + detector.getTimeDelta());
					if (detector.getCurrentSpan() - detector.getPreviousSpan() < -80 * activity.density) {
						if (spanCount == 1) {
							spanCount = 2;
							setRecyclerViewLayoutManager();
							return true;
						} else if (spanCount == 2 && slidingTabsFragment.width >= 0) {
							if (activity.right.getVisibility() == View.GONE || activity.left.getVisibility() == View.GONE) {
								spanCount = 8;
							} else {
								spanCount = 4;
							}
							setRecyclerViewLayoutManager();
							return true;
						}
					} else if (detector.getCurrentSpan() - detector.getPreviousSpan() > 80 * activity.density) {
						if ((spanCount == 4 || spanCount == 8)) {
							spanCount = 2;
							setRecyclerViewLayoutManager();
							return true;
						} else if (spanCount == 2) {
							spanCount = 1;
							setRecyclerViewLayoutManager();
							return true;
						} 
					}
					return false;
				}
			});

		listView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					//Log.d(TAG, "onTouch " + event);
					select(true);
					mScaleGestureDetector.onTouchEvent(event);
					return false;
				}
			});
		if (args != null) {
			if (currentPathTitle == null) {
				currentPathTitle = args.getString(Constants.EXTRA_ABSOLUTE_PATH);
			} else {
				args.putString(Constants.EXTRA_ABSOLUTE_PATH, currentPathTitle);
			}
			//Log.d(TAG, "onViewCreated.dir " + dir);
			if (savedInstanceState == null && args.getStringArrayList("dataSourceL1") != null) {
				savedInstanceState = args;
			}
		}
		allName.setText("Name");
		allSize.setText("Size");
		allDate.setText("Date");
		allType.setText("Type");
		switch (order) {
			case "Name ▼":
				zipListSorter = new ZipEntrySorter(zipListSorter.DIR_TOP, zipListSorter.NAME, zipListSorter.DESCENDING);
				allName.setText("Name ▼");
				break;
			case "Date ▲":
				zipListSorter = new ZipEntrySorter(zipListSorter.DIR_TOP, zipListSorter.DATE, zipListSorter.ASCENDING);
				allDate.setText("Date ▲");
				break;
			case "Date ▼":
				zipListSorter = new ZipEntrySorter(zipListSorter.DIR_TOP, zipListSorter.DATE, zipListSorter.DESCENDING);
				allDate.setText("Date ▼");
				break;
			case "Size ▲":
				zipListSorter = new ZipEntrySorter(zipListSorter.DIR_TOP, zipListSorter.SIZE, zipListSorter.ASCENDING);
				allSize.setText("Size ▲");
				break;
			case "Size ▼":
				zipListSorter = new ZipEntrySorter(zipListSorter.DIR_TOP, zipListSorter.SIZE, zipListSorter.DESCENDING);
				allSize.setText("Size ▼");
				break;
			case "Type ▲":
				zipListSorter = new ZipEntrySorter(zipListSorter.DIR_TOP, zipListSorter.TYPE, zipListSorter.ASCENDING);
				allType.setText("Type ▲");
				break;
			case "Type ▼":
				zipListSorter = new ZipEntrySorter(zipListSorter.DIR_TOP, zipListSorter.TYPE, zipListSorter.DESCENDING);
				allType.setText("Type ▼");
				break;
			default:
				zipListSorter = new ZipEntrySorter(zipListSorter.DIR_TOP, zipListSorter.NAME, zipListSorter.ASCENDING);
				allName.setText("Name ▲");
				break;
		}

		//Log.d(TAG, "onViewCreated " + this + ", ctx=" + getContext());
		if (savedInstanceState != null) {//EXTRA_DIR_PATH
			setDirectoryButtons();
			if (dataSourceL1.size() == 0) {//cannot use currentPathTitle for checking

				currentPathTitle = savedInstanceState.getString(Constants.EXTRA_ABSOLUTE_PATH);//EXTRA_DIR_PATH
				curPath = (String) savedInstanceState.get("curPath");

				fake = savedInstanceState.getBoolean("fake", false);

				searchMode = savedInstanceState.getBoolean("searchMode", false);

				allCbx.setEnabled(savedInstanceState.getBoolean("allCbx.isEnabled"));
				allCbx.setSelected(savedInstanceState.getBoolean("allCbx.isSelected"));
				allCbx.setImageResource(savedInstanceState.getInt("allCbx.imageResource", R.drawable.dot));

				tempPreviewL2 = savedInstanceState.getParcelable("tempPreviewL2");

				selectedInList1.clear();
				selectedInList1.addAll(savedInstanceState.getParcelableArrayList("selectedInList1"));
				tempSelectedInList1.clear();
				tempSelectedInList1.addAll(savedInstanceState.getParcelableArrayList("tempSelectedInList1"));
				tempOriDataSourceL1.clear();
				tempOriDataSourceL1.addAll(savedInstanceState.getParcelableArrayList("tempOriDataSourceL1"));
				if (searchMode) {
					searchET.removeTextChangedListener(textSearch);
					manageSearchUI(searchMode);
					searchET.setText(savedInstanceState.getString("searchVal", ""));
					searchET.addTextChangedListener(textSearch);
					dataSourceL1.clear();
					dataSourceL1.addAll((List<ZipEntry>)savedInstanceState.getParcelableArrayList("dataSourceL1"));
					setRecyclerViewLayoutManager();
				} else {
					setRecyclerViewLayoutManager();
					final Runnable r = new Runnable() {
						@Override
						public void run() {
							changeDir(curPath, false, true, null);
						}
					};
					load(currentPathTitle, r);
				}
				final int index  = savedInstanceState.getInt("index");
				final int top  = savedInstanceState.getInt("top");
				//Log.d(TAG, "index = " + index + ", " + top);
				gridLayoutManager.scrollToPositionWithOffset(index, top);
			} else {
				setRecyclerViewLayoutManager();
			}
		} else {
			setRecyclerViewLayoutManager();
			if (!fake) {
				if (currentPathTitle != null) {
					changeDir(currentPathTitle, true, false, null);
				} else if (searchMode) {
					searchMode = !searchMode;
					manageSearchUI(searchMode);
					changeDir(currentPathTitle, true, false, null);
				}
			}
		}
		updateColor(view);
		//}
	}

	@Override
    public void onRefresh() {
        final Editable s = searchET.getText();
		if (s.length() > 0) {
			textSearch.afterTextChanged(s);
//		} else {
//			loadlist(false);
		}
    }

	void notifyDataSetChanged() {
		srcAdapter.notifyDataSetChanged();
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		//Log.d(TAG, "onSaveInstanceState " + indexOf + ", fake=" + fake + ", " + currentPathTitle + ", " + outState);
		if (slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT) {
			AndroidUtils.setSharedPreference(activity, "ZipFrag.SPAN_COUNTR", spanCount);
		} else {
			AndroidUtils.setSharedPreference(activity, "ZipFrag.SPAN_COUNTL", spanCount);
		}
		outState.putParcelableArrayList("dataSourceL1", new ArrayList<ZipEntry>(dataSourceL1));

		outState.putParcelableArrayList("selectedInList1", new ArrayList<ZipEntry>(selectedInList1));
		outState.putParcelableArrayList("tempOriDataSourceL1", new ArrayList<ZipEntry>(tempOriDataSourceL1));
		outState.putParcelableArrayList("tempSelectedInList1", new ArrayList<ZipEntry>(tempSelectedInList1));

		outState.putParcelable("tempPreviewL2", tempPreviewL2);

		outState.putString(Constants.EXTRA_ABSOLUTE_PATH, currentPathTitle);//EXTRA_DIR_PATH

		outState.putBoolean("searchMode", searchMode);
		outState.putString("searchVal", searchET.getText().toString());
		outState.putBoolean("fake", fake);

		outState.putBoolean("allCbx.isEnabled", allCbx.isEnabled());
		outState.putBoolean("allCbx.isSelected", allCbx.isSelected());
		final int size = selectedInList1.size();
		if (size > 0) {
			if (size == dataSourceL1.size()) {
				outState.putInt("allCbx.imageResource", R.drawable.ic_accept);
			} else {
				outState.putInt("allCbx.imageResource", R.drawable.ready);
			}
		} else {
			outState.putInt("allCbx.imageResource", R.drawable.dot);
		}
		outState.putString("curPath", curPath);

		final int index = gridLayoutManager.findFirstVisibleItemPosition();
        final View vi = listView.getChildAt(0); 
        final int top = (vi == null) ? 0 : vi.getTop();
		outState.putInt("index", index);
		outState.putInt("top", top);

		super.onSaveInstanceState(outState);
	}

	Map<String, Object> onSaveInstanceState() {
		Map<String, Object> outState = new TreeMap<>();
		//Log.d(TAG, "Map onSaveInstanceState " + dir + ", " + outState);
		outState.put(Constants.EXTRA_ABSOLUTE_PATH, currentPathTitle);//EXTRA_DIR_PATH

		final ArrayList<ZipEntry> dataSource = new ArrayList<>(dataSourceL1.size());
		dataSource.addAll(dataSourceL1);
		outState.put("dataSourceL1", dataSource);

		final ArrayList<ZipEntry> selectedInList = new ArrayList<>(selectedInList1.size());
		selectedInList.addAll(selectedInList1);
		outState.put("selectedInList1", selectedInList);

		outState.put("searchMode", searchMode);
		outState.put("searchVal", searchET.getText().toString());
		//outState.put("currentPathTitle", currentPathTitle);
		outState.put("allCbx.isEnabled", allCbx.isEnabled());
		outState.put("allCbx.isSelected", allCbx.isSelected());
		outState.put("curPath", curPath);

		final int index = gridLayoutManager.findFirstVisibleItemPosition();
        final View vi = listView.getChildAt(0); 
        final int top = (vi == null) ? 0 : vi.getTop();
		outState.put("index", index);
		outState.put("top", top);
		outState.put("tempPreviewL2", tempPreviewL2);

        return outState;
	}

	void reload(Map<String, Object> savedInstanceState) {
		Log.d(TAG, "reload currentPathTitle " + currentPathTitle + ", "  + savedInstanceState);
		currentPathTitle = (String) savedInstanceState.get(Constants.EXTRA_ABSOLUTE_PATH);//EXTRA_DIR_PATH
		curPath = (String) savedInstanceState.get("curPath");
		selectedInList1.clear();
		selectedInList1.addAll((ArrayList<ZipEntry>) savedInstanceState.get("selectedInList1"));
		dataSourceL1.clear();
		dataSourceL1.addAll((ArrayList<ZipEntry>) savedInstanceState.get("dataSourceL1"));

		if (type == Frag.TYPE.SELECTION) {
			tempOriDataSourceL1.clear();
			tempOriDataSourceL1.addAll(dataSourceL1);
		}
		searchMode = savedInstanceState.get("searchMode");
		searchVal = (String) savedInstanceState.get("searchVal");
		//currentPathTitle = (String) savedInstanceState.get("currentPathTitle");
		tempPreviewL2 = (ZipEntry) savedInstanceState.get("tempPreviewL2");
		allCbx.setEnabled(savedInstanceState.get("allCbx.isEnabled"));
		allCbx.setSelected(savedInstanceState.get("allCbx.isSelected"));
		srcAdapter.notifyDataSetChanged();

		setRecyclerViewLayoutManager();
		gridLayoutManager.scrollToPositionWithOffset(savedInstanceState.get("index"), savedInstanceState.get("top"));

		if (allCbx.isSelected()) {
			selectionStatusTV.setText(dataSourceL1.size() 
									  + "/" + dataSourceL1.size());
		} else {
			selectionStatusTV.setText(selectedInList1.size() 
									  + "/" + dataSourceL1.size());
		}

		setDirectoryButtons();
	}

	@Override
    public void onPause() {
        //Log.d(TAG, "onPause " + toString());
		super.onPause();
        if (imageLoader != null) {
			imageLoader.stopThread();
		}
		//loadList.cancel(true);
        if (mFileObserver != null) {
            mFileObserver.stopWatching();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

	private boolean hasPermissions() {
        return checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        //setLoading(true);
        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
    }

    /**
     * Switch to permission request mode.
     */
    private void showPermissionDenied() {
        //setLoading(false);
        Toast.makeText(getActivity(), R.string.details_permissions, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_STORAGE_PERMISSION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    refresh();
                } else {
                    showPermissionDenied();
                }
                break;
        }
    }

	public void refresh() {
        if (hasPermissions()) {
            // Cancel and GC previous scanner so that it doesn't load on top of the
            // new list.
            // Race condition seen if a long list is requested, and a short list is
            // requested before the long one loads.
//            mScanner.cancel();
//            mScanner = null;

            // Indicate loading and start scanning.
//            setLoading(true);
//            renewScanner().start();
        } else {
            requestPermissions();
        }
    }

	public void updateList() {
		Log.d(TAG, "updateList " + this);
		if (!fake) {
			if (currentPathTitle != null) {
				changeDir(currentPathTitle, true, false, null);
			} else {
				updateDir();
			}
		}
    }

	void setDirectoryButtons() {
		Log.d(TAG, "setDirectoryButtons " + type + ", " + currentPathTitle);
		//topflipper.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));

		if (curPath != null) {
			mDirectoryButtons.removeAllViews();
			String[] parts = (curPath.startsWith("/") ? curPath : "/" + curPath).split("/");
			Log.d(TAG, "setDirectoryButtons " + parts);

			final TextView ib = new TextView(activity);
			final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.CENTER;
			ib.setLayoutParams(layoutParams);
			ib.setBackgroundResource(R.drawable.ripple);
			ib.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			ib.setText("/");
			ib.setTag("/");
			ib.setMinEms(2);
			ib.setPadding(0, 4, 0, 4);
			ib.setTextColor(Constants.TEXT_COLOR);
			// ib.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
			ib.setGravity(Gravity.CENTER);
			ib.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						changeDir("/", false, true, null);
					}
				});
			mDirectoryButtons.addView(ib);

			String folder = "";
			View v;
			TextView b = null;
			for (int i = 1; i < parts.length; i++) {
				if (i == 1) {
					folder += (curPath.startsWith("/") ? "/" : "") + parts[i];
				} else {
					folder += "/" + parts[i];
				}
				Log.d(TAG, "setDirectoryButtons " + folder);

				v = activity.getLayoutInflater().inflate(R.layout.dir, null);
				b = (TextView) v.findViewById(R.id.name);
				b.setText(parts[i]);
				b.setTag(folder);
				b.setTextColor(Constants.TEXT_COLOR);
				b.setOnClickListener(new View.OnClickListener() {
						public void onClick(View view) {
							String dir2 = (String) view.getTag();
							if (dir2.equals(curPath)) {
								changeDir(dir2, false, false, null);
							} else {
								changeDir(dir2, false, true, null);
							}
						}
					});
				mDirectoryButtons.addView(v);
				scrolltext.postDelayed(new Runnable() {
						public void run() {
							scrolltext.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
						}
					}, 100L);
			}
			AndroidUtils.setOnTouchListener(mDirectoryButtons, this);
			if (b != null) {
				b.setOnLongClickListener(new OnLongClickListener() {
						@Override
						public boolean onLongClick(View p1) {
							final EditText editText = new EditText(activity);
							final CharSequence clipboardData = AndroidUtils.getClipboardData(activity);
							if (clipboardData.length() > 0 && clipboardData.charAt(0) == '/') {
								editText.setText(clipboardData);
							} else {
								editText.setText(curPath);
							}
							final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.MATCH_PARENT,
								LinearLayout.LayoutParams.WRAP_CONTENT);
							layoutParams.gravity = Gravity.CENTER;
							editText.setLayoutParams(layoutParams);
							editText.setSingleLine(true);
							editText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
							editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
							editText.setMinEms(2);
							//editText.setGravity(Gravity.CENTER);
							final int density = 8 * (int)getResources().getDisplayMetrics().density;
							editText.setPadding(density, density, density, density);

							AlertDialog dialog = new AlertDialog.Builder(activity)
								.setIconAttribute(android.R.attr.dialogIcon)
								.setTitle("Go to...")
								.setView(editText)
								.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
										String name = editText.getText().toString();
										Log.d(TAG, "new " + name);
										//File newF = new File(name);
										ZipEntry newF = zip.entries.get(name);
										if (newF != null) {
											if (newF.isDirectory) {
												curPath = name;
												changeDir(curPath, false, true, null);
											} else {
												curPath = newF.parentPath;
												changeDir(curPath, false, true, null);
											}
											dialog.dismiss();
										} else {
											showToast("\"" + newF + "\" does not exist. Please choose another name");
										}
									}
								})
								.setNegativeButton(R.string.cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int whichButton) {
										dialog.dismiss();
									}
								}).create();
							dialog.show();
							return true;
						}
					});
			}
		}
	}

	public void updateDir() {
		Log.d(TAG, "updateDir " + curPath);
		setDirectoryButtons();

		if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
			activity.slideFrag.notifyTitleChange();
		} else if (activity.slideFrag2 != null) {
			activity.slideFrag2.notifyTitleChange();
		}
	}

	FileObserver createFileObserver(final String path) {
        return new FileObserver(path, FileObserver.CREATE | FileObserver.DELETE
								| FileObserver.MOVED_FROM | FileObserver.MOVED_TO
								| FileObserver.DELETE_SELF | FileObserver.MOVE_SELF
								//| FileObserver.CLOSE_WRITE
								) {
            @Override
            public void onEvent(final int event, final String path) {
                if (path != null) {
                    Util.debug(TAG, "FileObserver received event %d, CREATE = 256;DELETE = 512;DELETE_SELF = 1024;MODIFY = 2;MOVED_FROM = 64;MOVED_TO = 128; path %s", event, path);
					activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								currentPathTitle = zip.file.getAbsolutePath();
								zip = null;
								updateList();
							}
						});
                }
            }
        };
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume index " + activity.slideFrag.indexOfMTabs(this) + slidingTabsFragment.side + ", " + type + ", fake=" + fake + ", " + currentPathTitle + ", currentPathTitle=" + currentPathTitle);
		super.onResume();
		activity = (ExplorerActivity)getActivity();

		getView().setBackgroundColor(Constants.BASE_BACKGROUND);
		if (mFileObserver != null) {
			mFileObserver.stopWatching();
		}
		mFileObserver = createFileObserver(currentPathTitle);
		mFileObserver.startWatching();

		selectionStatusTV.setText(selectedInList1.size()  + "/" + dataSourceL1.size());

		if (zip != null) {
			rightStatus.setText(
				"Free " + Formatter.formatFileSize(activity, zip.file.length())
				+ " MiB. Used " + Formatter.formatFileSize(activity, zip.unZipSize));
		}
	}

	public void updateColor(View rootView) {
		getView().setBackgroundColor(Constants.BASE_BACKGROUND);
		icons.setColorFilter(Constants.TEXT_COLOR);
		allName.setTextColor(Constants.TEXT_COLOR);
		allDate.setTextColor(Constants.TEXT_COLOR);
		allSize.setTextColor(Constants.TEXT_COLOR);
		allType.setTextColor(Constants.TEXT_COLOR);
		selectionStatusTV.setTextColor(Constants.TEXT_COLOR);
		rightStatus.setTextColor(Constants.TEXT_COLOR);
		searchET.setTextColor(Constants.TEXT_COLOR);
		clearButton.setColorFilter(Constants.TEXT_COLOR);
		searchButton.setColorFilter(Constants.TEXT_COLOR);
		noFileImage.setColorFilter(Constants.TEXT_COLOR);
		noFileText.setTextColor(Constants.TEXT_COLOR);
		dirMore.setColorFilter(Constants.TEXT_COLOR);
		
		horizontalDivider0.setBackgroundColor(Constants.DIVIDER_COLOR);
		horizontalDivider12.setBackgroundColor(Constants.DIVIDER_COLOR);
		horizontalDivider7.setBackgroundColor(Constants.DIVIDER_COLOR);
	}

	void updateL2() {
		Collections.sort(dataSourceL1, zipListSorter);
		updateTemp();
	}

	void updateTemp() {
		tempOriDataSourceL1.clear();
		tempOriDataSourceL1.addAll(dataSourceL1);
	}

	void removeAllDS2(final Collection<ZipEntry> c) {
		dataSourceL1.removeAll(c);
		tempOriDataSourceL1.removeAll(c);
	}

	void clearDS2() {
		dataSourceL1.clear();
		tempOriDataSourceL1.clear();
	}

	@Override
    public String getTitle() {
		//Log.d(TAG, "getTitle() openMode " + openMode + ", " + currentPathTitle + ", CUSTOM " + openMode.equals(openMode.CUSTOM) + ", " + this);
		return new File(currentPathTitle).getName();
	}

	public void updateDelPaste() {
		if (selectedInList1.size() > 0 && deletePastesBtn.getCompoundDrawables()[1] != drawableDelete) {
			deletePastesBtn.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
			deletePastesBtn.setCompoundDrawablesWithIntrinsicBounds(null, drawableDelete, null, null);
			deletePastesBtn.setText("Delete");
		} else if (selectedInList1.size() == 0 && deletePastesBtn.getCompoundDrawables()[1] != drawablePaste) {
			deletePastesBtn.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
			deletePastesBtn.setCompoundDrawablesWithIntrinsicBounds(null, drawablePaste, null, null);
			deletePastesBtn.setText("Paste");
		}
		deletePastesBtn.getCompoundDrawables()[1].setColorFilter(Constants.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
	}


//	public void loadlist(String path, /*boolean back, */OpenMode openMode) {
//        /*if(openMode==-1 && android.util.Patterns.EMAIL_ADDRESS.matcher(path).matches())
//		 bindDrive(path);
//		 else */
////        if (loadList != null) loadList.cancel(true);
////        loadList = new LoadFiles();//LoadList(activity, activity, /*back, */this, openMode);
////        loadList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (path));
//		changeDir(path, true, true, null);
//		
//    }

	private void showStatus() {
		if (sortBarLayout.getVisibility() == View.GONE) {
			if (selStatusLayout != null) {
				selStatusLayout.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
				selStatusLayout.setVisibility(View.VISIBLE);
			} else {
				selectionStatusTV.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
				selectionStatusTV.setVisibility(View.VISIBLE);
			}
			horizontalDivider0.setVisibility(View.VISIBLE);
			horizontalDivider12.setVisibility(View.VISIBLE);
			sortBarLayout.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
			sortBarLayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(final View v) {
		//Log.d(TAG, "onClick " + this + ", " + type);
		//super.onClick(p1);
		switch (v.getId()) {
			case R.id.allCbx:
				selectedInList1.clear();
				if (!allCbx.isSelected()) {//}.isChecked()) {
					allCbx.setSelected(true);
					selectedInList1.addAll(dataSourceL1);
					if (selectedInList1.size() > 0 && commands.getVisibility() == View.GONE) {
						commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
						commands.setVisibility(View.VISIBLE);
						horizontalDivider6.setVisibility(View.VISIBLE);
					}
				} else {
					allCbx.setSelected(false);
					if (activity.COPY_PATH == null && activity.MOVE_PATH == null && commands.getVisibility() == View.VISIBLE) {
						horizontalDivider6.setVisibility(View.GONE);
						commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
						commands.setVisibility(View.GONE);
					}
				}
				selectionStatusTV.setText(selectedInList1.size() 
										  + "/" + dataSourceL1.size());
				srcAdapter.notifyDataSetChanged();
				updateDelPaste();
				break;
			case R.id.allName:
				if (allName.getText().toString().equals("Name ▲")) {
					allName.setText("Name ▼");
					zipListSorter = new ZipEntrySorter(zipListSorter.DIR_TOP, zipListSorter.NAME, zipListSorter.DESCENDING);
					AndroidUtils.setSharedPreference(activity, (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) ? ("ZipFragSortTypeL" + activity.slideFrag.indexOfMTabs(ZipFragment.this)) : ("ZipFragSortTypeR" + activity.slideFrag2.indexOfMTabs(ZipFragment.this)), "Name ▼");
				} else {
					allName.setText("Name ▲");
					zipListSorter = new ZipEntrySorter(zipListSorter.DIR_TOP, zipListSorter.NAME, zipListSorter.ASCENDING);
					AndroidUtils.setSharedPreference(activity, (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) ? ("ZipFragSortTypeL" + activity.slideFrag.indexOfMTabs(ZipFragment.this)) : ("ZipFragSortTypeR" + activity.slideFrag2.indexOfMTabs(ZipFragment.this)), "Name ▲");
				}
				//Log.d(TAG, "activity.slideFrag.indexOf " + activity.slideFrag.indexOf(ContentFragment.this));
				allDate.setText("Date");
				allSize.setText("Size");
				allType.setText("Type");
				Collections.sort(dataSourceL1, zipListSorter);
				srcAdapter.notifyDataSetChanged();
				break;
			case R.id.allType:
				if (allType.getText().toString().equals("Type ▲")) {
					allType.setText("Type ▼");
					zipListSorter = new ZipEntrySorter(zipListSorter.DIR_TOP, zipListSorter.TYPE, zipListSorter.DESCENDING);
					AndroidUtils.setSharedPreference(activity, (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) ? ("ZipFragSortTypeL" + activity.slideFrag.indexOfMTabs(ZipFragment.this)) : ("ZipFragSortTypeR" + activity.slideFrag2.indexOfMTabs(ZipFragment.this)), "Type ▼");
				} else {
					allType.setText("Type ▲");
					zipListSorter = new ZipEntrySorter(zipListSorter.DIR_TOP, zipListSorter.TYPE, zipListSorter.ASCENDING);
					AndroidUtils.setSharedPreference(activity, (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) ? ("ZipFragSortTypeL" + activity.slideFrag.indexOfMTabs(ZipFragment.this)) : ("ZipFragSortTypeR" + activity.slideFrag2.indexOfMTabs(ZipFragment.this)), "Type ▲");
				}
				allName.setText("Name");
				allDate.setText("Date");
				allSize.setText("Size");
				Collections.sort(dataSourceL1, zipListSorter);
				srcAdapter.notifyDataSetChanged();
				break;
			case R.id.allDate:
				if (allDate.getText().toString().equals("Date ▲")) {
					allDate.setText("Date ▼");
					zipListSorter = new ZipEntrySorter(zipListSorter.DIR_TOP, zipListSorter.DATE, zipListSorter.DESCENDING);
					AndroidUtils.setSharedPreference(activity, (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) ? ("ZipFragSortTypeL" + activity.slideFrag.indexOfMTabs(ZipFragment.this)) : ("ZipFragSortTypeR" + activity.slideFrag2.indexOfMTabs(ZipFragment.this)), "Date ▼");
				} else {
					allDate.setText("Date ▲");
					zipListSorter = new ZipEntrySorter(zipListSorter.DIR_TOP, zipListSorter.DATE, zipListSorter.ASCENDING);
					AndroidUtils.setSharedPreference(activity, (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) ? ("ZipFragSortTypeL" + activity.slideFrag.indexOfMTabs(ZipFragment.this)) : ("ZipFragSortTypeR" + activity.slideFrag2.indexOfMTabs(ZipFragment.this)), "Date ▲");
				}
				allName.setText("Name");
				allSize.setText("Size");
				allType.setText("Type");
				Collections.sort(dataSourceL1, zipListSorter);
				srcAdapter.notifyDataSetChanged();
				break;
			case R.id.allSize:
				if (allSize.getText().toString().equals("Size ▲")) {
					allSize.setText("Size ▼");
					zipListSorter = new ZipEntrySorter(zipListSorter.DIR_TOP, zipListSorter.SIZE, zipListSorter.DESCENDING);
					AndroidUtils.setSharedPreference(activity, (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) ? ("ZipFragSortTypeL" + activity.slideFrag.indexOfMTabs(ZipFragment.this)) : ("ZipFragSortTypeR" + activity.slideFrag2.indexOfMTabs(ZipFragment.this)), "Size ▼");
				} else {
					allSize.setText("Size ▲");
					zipListSorter = new ZipEntrySorter(zipListSorter.DIR_TOP, zipListSorter.SIZE, zipListSorter.ASCENDING);
					AndroidUtils.setSharedPreference(activity, (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) ? ("ZipFragSortTypeL" + activity.slideFrag.indexOfMTabs(ZipFragment.this)) : ("ZipFragSortTypeR" + activity.slideFrag2.indexOfMTabs(ZipFragment.this)), "Size ▲");
				}
				allName.setText("Name");
				allDate.setText("Date");
				allType.setText("Type");
				Collections.sort(dataSourceL1, zipListSorter);
				srcAdapter.notifyDataSetChanged();
				break;
			case R.id.icons:
				moreInPanel(v);
				break;
			case R.id.search:
				searchButton();
				break;
			case R.id.clear:
				searchET.setText("");
				break;
			case R.id.dirMore:
				MenuBuilder menuBuilder = new MenuBuilder(activity);

				if (activity.multiFiles) {

					if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT && activity.right.getVisibility() == View.VISIBLE
						|| slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT && activity.left.getVisibility() == View.VISIBLE) {
						menuBuilder.add("Hide");
					} else {
						menuBuilder.add("2 panels");
					}
					menuBuilder.add("Swap panels");

					if (activity.left.getVisibility() == View.VISIBLE && activity.right.getVisibility() == View.VISIBLE) {
						if (slidingTabsFragment.width <= 0) {
							menuBuilder.add("Wider panel");
						} else {
							menuBuilder.add("2 panels equal");
						}
					}

					if (activity.COPY_PATH != null || activity.MOVE_PATH != null || activity.EXTRACT_PATH != null || activity.EXTRACT_MOVE_PATH != null) {
						menuBuilder.add("Clear Clipboard");
					}
				}

				MenuPopupHelper optionsMenu = new MenuPopupHelper(activity , menuBuilder, dirMore);

				menuBuilder.setCallback(new MenuBuilder.Callback() {
						@Override
						public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
							Log.d(TAG, item.getTitle() + ".");
							if ("Hide".equals(item.getTitle()) || "2 panels".equals(item.getTitle())) {
								hide();
							} else if ("Wider panel".equals(item.getTitle()) || "2 panels equal".equals(item.getTitle())) {
								biggerequalpanel();
							} else if ("Swap panels".equals(item.getTitle())) {
								swap(v);
							} else if ("Clear Clipboard".equals(item.getTitle())) {
								activity.COPY_PATH = null;
								activity.MOVE_PATH = null;
								activity.EXTRACT_PATH = null;
								activity.EXTRACT_MOVE_PATH = null;
								if (activity.curExplorerFrag.selectedInList1.size() == 0 && activity.curExplorerFrag.commands.getVisibility() == View.VISIBLE) {
									activity.curExplorerFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
									activity.curExplorerFrag.commands.setVisibility(View.GONE);
									activity.curExplorerFrag.horizontalDivider6.setVisibility(View.GONE);
									activity.curExplorerFrag.updateDelPaste();
								} 
								if (activity.curContentFrag.selectedInList1.size() == 0 && activity.curContentFrag.commands.getVisibility() == View.VISIBLE) {
									activity.curContentFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
									activity.curContentFrag.commands.setVisibility(View.GONE);
									activity.curContentFrag.horizontalDivider6.setVisibility(View.GONE);
									activity.curContentFrag.updateDelPaste();
								}
							} 
							return true;
						}
						@Override
						public void onMenuModeChange(MenuBuilder menu) {}
					});
				optionsMenu.show();
				break;
			case R.id.copys:
				activity.COPY_PATH = null;
				activity.MOVE_PATH = null;
				activity.EXTRACT_MOVE_PATH = null;
				final ArrayList<String> copies = new ArrayList<>(selectedInList1.size());
				for (ZipEntry le : selectedInList1) {
					copies.add(le.path);
				}
				activity.zip = zip;
				activity.EXTRACT_PATH = copies;

				if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT && activity.multiFiles && activity.curExplorerFrag.commands.getVisibility() == View.GONE) {//type == -1
					activity.curExplorerFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
					activity.curExplorerFrag.commands.setVisibility(View.VISIBLE);
					activity.curExplorerFrag.horizontalDivider6.setVisibility(View.VISIBLE);
					activity.curExplorerFrag.updateDelPaste();
				} else if (slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT && activity.curContentFrag.commands.getVisibility() == View.GONE) {//type != -1
					activity.curContentFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
					activity.curContentFrag.commands.setVisibility(View.VISIBLE);
					activity.curContentFrag.horizontalDivider6.setVisibility(View.VISIBLE);
					activity.curContentFrag.updateDelPaste();
				}
				break;
			case R.id.cuts:
				activity.COPY_PATH = null;
				activity.MOVE_PATH = null;
				activity.EXTRACT_PATH = null;
				final ArrayList<String> copie = new ArrayList<>(selectedInList1.size());
				for (ZipEntry le : selectedInList1) {
					copie.add(le.path);
				}
				activity.zip = zip;
				activity.EXTRACT_MOVE_PATH = copie;
				activity.callback = new Runnable() {
					@Override
					public void run() {
						changeDir(currentPathTitle, true, false, new Runnable() {
								@Override
								public void run() {
									changeDir(curPath, false, true, null);
								}
							});
					}
				};
				if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT && activity.multiFiles && activity.curExplorerFrag.commands.getVisibility() == View.GONE) {
					activity.curExplorerFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
					activity.curExplorerFrag.commands.setVisibility(View.VISIBLE);
					activity.curExplorerFrag.horizontalDivider6.setVisibility(View.VISIBLE);
					activity.curExplorerFrag.updateDelPaste();
				} else if (slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT && activity.curContentFrag.commands.getVisibility() == View.GONE) {
					activity.curContentFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
					activity.curContentFrag.commands.setVisibility(View.VISIBLE);
					activity.curContentFrag.horizontalDivider6.setVisibility(View.VISIBLE);
					activity.curContentFrag.updateDelPaste();
				}
				break;
			case R.id.deletes_pastes:
				Log.d(TAG, "deletesPastes selectedInList1.size() " + selectedInList1.size());
				activity.callback = new Runnable() {
					@Override
					public void run() {
						changeDir(currentPathTitle, true, false, new Runnable() {
								@Override
								public void run() {
									changeDir(curPath, false, true, null);
								}
							});
					}
				};
				if (selectedInList1.size() > 0) {
					GeneralDialogCreation.deleteFilesDialog(activity, //getLayoutElements(),
															activity, zip, (List<ZipEntry>)selectedInList1, activity.getAppTheme(), activity.callback);
				} else {

				}
				break;
			case R.id.renames:
				rename((ZipEntry)selectedInList1.get(0));
				break;
			case R.id.compresss:
				if (selectedInList1.size() == 0) {
					File file = new File(currentPathTitle);
					String name = file.getName();
					activity.decompress(currentPathTitle, file.getParent() + "/" + name.substring(0, name.lastIndexOf(".")), "", true);
				} else {
					StringBuilder sb = new StringBuilder();
					for (ZipEntry le : selectedInList1) {
						sb.append(le.path).append("| ");
					}
					File file = new File(currentPathTitle);
					String name = file.getName();
					activity.decompress(currentPathTitle, file.getParent() + "/" + name.substring(0, name.lastIndexOf(".")), sb.toString(), true);
				}
				break;
			case R.id.shares:
				if (selectedInList1.size() > 0) {

					if (selectedInList1.size() > 100)
						Toast.makeText(activity, getResources().getString(R.string.share_limit),
									   Toast.LENGTH_SHORT).show();
					else {
						final ArrayList<File> arrayList = new ArrayList<>(selectedInList1.size());
						final StringBuilder sb = new StringBuilder();
						for (ZipEntry i : selectedInList1) {
							arrayList.add(new File(ExplorerApplication.PRIVATE_PATH + "/" + i.path));
							sb.append(i.path).append("\n");
						}
						new DecompressTask(fragActivity,
							currentPathTitle,
							ExplorerApplication.PRIVATE_PATH,
							sb.toString(),
							"",
							"",
							"",
							0,
							"x",
							new Runnable() {
								@Override
								public void run() {
									Futils.shareFiles(arrayList, activity, activity.getAppTheme(), accentColor);
								}		   
							}).execute();
					}
				}
				break;
			case R.id.infos:
				ZipEntry le = (ZipEntry) selectedInList1.get(0);
				GeneralDialogCreation.showPropertiesDialog(le,
														   activity, 
														   activity.getAppTheme(), totalZipLength, totalUnzipLength);
				break;
		}
	}

	public void rename(final ZipEntry f) {
		MaterialDialog.Builder builder = new MaterialDialog.Builder(activity);
		String name = f.name;
		builder.input("", name, false, new MaterialDialog.InputCallback() {
				@Override
				public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {

				}
			});
		builder.theme(activity.getAppTheme().getMaterialDialogTheme());
		builder.title(getResources().getString(R.string.rename));

		builder.onNegative(new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(MaterialDialog dialog, DialogAction which) {
					dialog.cancel();
				}
			});

		builder.onPositive(new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(MaterialDialog dialog, DialogAction which) {
					String name = dialog.getInputEditText().getText().toString();
					if (zip.entries.get(name) != null) {
						dialog.show();
						Toast.makeText(activity, getString(R.string.fileexist),
									   Toast.LENGTH_SHORT).show();
					} else {
						List<String> l = new ArrayList<String>(2);
						l.add(f.path);
						l.add(name);
						new DecompressTask(fragActivity,
							currentPathTitle,
							ExplorerApplication.PRIVATE_PATH,
							"",
							"",
							"",
							l,
							0,
							"rn",
							new Runnable() {
								@Override
								public void run() {
									changeDir(currentPathTitle, true, false, new Runnable() {
											@Override
											public void run() {
												changeDir(f.parentPath, false, true, null);
											}
										});
								}
							}).execute();
					}
				}
			});

		builder.positiveText(R.string.save);
		builder.negativeText(R.string.cancel);
		builder.positiveColor(accentColor).negativeColor(accentColor).widgetColor(accentColor);
		final MaterialDialog materialDialog = builder.build();
		materialDialog.show();
		Log.d(TAG, "rename " + name);//f.getNameString(getContext()));

		// place cursor at the starting of edit text by posting a runnable to edit text
		// this is done because in case android has not populated the edit text layouts yet, it'll
		// reset calls to selection if not posted in message queue
		materialDialog.getInputEditText().post(new Runnable() {
				@Override
				public void run() {
					if (!f.isDirectory) {
						int lastIndexOf = f.path.lastIndexOf(".");
						materialDialog.getInputEditText().setSelection(lastIndexOf>=0 ? lastIndexOf : lastIndexOf + 1);
					}
				}
			});
	}

	private class TextSearch implements TextWatcher {
		public void beforeTextChanged(CharSequence s, int start, int end, int count) {
		}

		public void afterTextChanged(final Editable text) {
			if (searchMode) {
				final String filesearch = text.toString();
				Log.d(TAG, "quicksearch " + filesearch);
				if (filesearch.length() > 0) {
					if (searchTask.getStatus() == AsyncTask.Status.RUNNING) {
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

	void setRecyclerViewLayoutManager() {
        //Log.d(TAG, "setRecyclerViewLayoutManager " + gridLayoutManager);
		if (listView == null) {
			return;
		}
		int scrollPosition = 0, top = 0;
        // If a layout manager has already been set, get current scroll position.
        if (gridLayoutManager != null) {
			scrollPosition = gridLayoutManager.findFirstVisibleItemPosition();
			final View vi = listView.getChildAt(0); 
			top = (vi == null) ? 0 : vi.getTop();
		}

		gridLayoutManager = new GridLayoutManager(fragActivity, spanCount);
		listView.setLayoutManager(gridLayoutManager);

		listView.removeItemDecoration(dividerItemDecoration);
		listView.invalidateItemDecorations();
		if (spanCount == 1 || spanCount == 2 && slidingTabsFragment.width >= 0) {
			dividerItemDecoration = new GridDividerItemDecoration(fragActivity, true);
			listView.addItemDecoration(dividerItemDecoration);
		} else {
			if (activity.right.getVisibility() == View.GONE || activity.left.getVisibility() == View.GONE) {
				spanCount = 8;
			} else if (slidingTabsFragment.width < 0) {
				spanCount = 2;
			} else if (slidingTabsFragment.width == 0) {
				spanCount = 4;
			} else {
				spanCount = 6;
			}
		}

		srcAdapter = new ZipAdapter(this, dataSourceL1);
		listView.setAdapter(srcAdapter);

		gridLayoutManager.scrollToPositionWithOffset(scrollPosition, top);
	}

	void trimBackStack() {
		final int size = backStack.size() / 2;
		Log.d(TAG, "trimBackStack " + size);
		for (int i = 0; i < size; i++) {
			backStack.remove(0);
		}
	}

	@Override
	public void onLowMemory() {
		Log.d(TAG, "onLowMemory " + Runtime.getRuntime().freeMemory());
		super.onLowMemory();
		trimBackStack();
	}

	public void searchButton() {
		searchMode = !searchMode;
		manageSearchUI(searchMode);
	}

	private void manageSearchUI(boolean search) {
		if (search == true) {
			searchButton.setImageResource(R.drawable.ic_arrow_back_white_36dp);
			topflipper.setDisplayedChild(topflipper.indexOfChild(quickLayout));
			searchET.setHint("Search " + new File(currentPathTitle).getName());
			searchET.requestFocus();
			//imm.showSoftInput(quicksearch, InputMethodManager.SHOW_FORCED);
			imm.showSoftInput(searchET, InputMethodManager.SHOW_IMPLICIT);
			//imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
			//activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		} else {
			imm.hideSoftInputFromWindow(searchET.getWindowToken(), 0);
			searchET.setText("");
			searchButton.setImageResource(R.drawable.ic_action_search);
			topflipper.setDisplayedChild(topflipper.indexOfChild(scrolltext));
			updateList();
		}
	}

	boolean back() {
		Map<String, Object> softBundle;
		//Log.d(TAG, "back " + backStack.size());
		if (backStack.size() >= 1 && (softBundle = backStack.pop()) != null && softBundle.get("dataSourceL1") != null) {
			//Log.d(TAG, "back " + softBundle);
			reload(softBundle);
			return true;
		} else {
			return false;
		}
	}

	public void changeDir(final String curDir, final boolean fromBeginZip, final boolean doScroll, final Runnable run) {
		Log.d(TAG, "changeDir " + curDir + ", doScroll " + doScroll + ", " + type + ", " + slidingTabsFragment.side);
		if (fake) {
			return;
		}
		loadList.cancel(true);
		searchTask.cancel(true);
		loadList = new LoadFiles();
		loadList.execute(curDir, fromBeginZip, doScroll, run);
	}

	private class LoadFiles extends AsyncTask<Object, String, List<ZipEntry>> {

		private boolean fromBeginZip;
		private Boolean doScroll;
		private Runnable run;

		protected void onPreExecute() {
			mSwipeRefreshLayout.setRefreshing(true);
		}

		protected void onCancelled(List<ZipEntry> result) {
			if (andro7za != null) {
				andro7za.cancel();
			}
			if (zpaq != null) {
				zpaq.cancel();
			}
			mSwipeRefreshLayout.setRefreshing(false);
		}

		@Override
		protected List<ZipEntry> doInBackground(Object... params) {
			String path = (String) params[0];
			fromBeginZip = (Boolean) params[1];
			doScroll = (Boolean) params[2];
			run = (Runnable) params[3];

			noMedia = false;
			List<ZipEntry> dataSourceL1a = new LinkedList<>();

			if (currentPathTitle == null) {
				return dataSourceL1a;
			}
			Log.d(TAG, "LoadFiles.doInBackground " + path + ", " + fromBeginZip + ", " + ZipFragment.this);
			folder_count = 0;
			file_count = 0;
			// we're neither in OTG not in SMB, load the list based on root/general filesystem
			//dataSourceL1a = new LinkedList<LayoutElement>();
			try {
				if (!fromBeginZip) {
					if (backStack.size() > ExplorerActivity.NUM_BACK) {
						backStack.remove(0);
					}
					backStack.push(onSaveInstanceState());
				} else {
					backStack.clear();
					tempPreviewL2 = null;
				}
				//Log.d(TAG, Util.collectionToString(history, true, "\n"));

				if (fromBeginZip || zip == null) {//} || !zip.file.getAbsolutePath().equals(currentPathTitle)) {
					if (mFileObserver != null) {
						mFileObserver.stopWatching();
					}
					mFileObserver = createFileObserver(currentPathTitle);
					mFileObserver.startWatching();
					if (!currentPathTitle.toLowerCase().endsWith(".zpaq")) {
						zip = andro7za.listCmd(currentPathTitle, "");
					} else {
						zip = zpaq.listing(currentPathTitle, "");
					}
					curPath = "/";
					activity.zip = zip;
					if (zip == null) {
						Toast.makeText(activity, "Archive error", Toast.LENGTH_LONG);
						return dataSourceL1a;
					}
				} else {
					curPath = path;
				}
				if (tempPreviewL2 != null && !zip.entries.containsKey(tempPreviewL2.path)) {
					tempPreviewL2 = null;
				}
				final ZipEntry get2 = zip.entries.get(curPath);
				Log.d(TAG, curPath + ", " + path + ", " + zip + ", " + get2);
				if (get2 != null) {
					dataSourceL1a.addAll(get2.list);
				} else {
					for (ZipEntry get : zip.entries.values()) {
						if (get.parentPath.equals(curPath)) {
							dataSourceL1a.add(get);
						}
					}
				}
			} catch (Throwable e) {
				publishProgress(e.getMessage());
				e.printStackTrace();
				return dataSourceL1a;
			}

			if (dataSourceL1a != null) //} && !(openMode == OpenMode.CUSTOM && ((currentPathTitle).equals("5") || (currentPathTitle).equals("6"))))
				Collections.sort(dataSourceL1a, zipListSorter);

			return dataSourceL1a;
		}

		@Override
		protected void onPostExecute(List<ZipEntry> dataSourceL1a) {
			//Log.d(TAG, "LoadFiles.onPostExecute.dataSourceL1a=" + Util.collectionToString(dataSourceL1a, false, "\n"));
			if (currentPathTitle != null && zip != null) {
				if (currentPathTitle.startsWith("/")) {
					rightStatus.setText(
						Formatter.formatFileSize(activity, zip.file.length())
						+ "/" + Formatter.formatFileSize(activity, zip.unZipSize)
					);
				}
				dataSourceL1.clear();
				dataSourceL1.addAll(dataSourceL1a);
				selectedInList1.clear();
			}
			showStatus();
			mSwipeRefreshLayout.setRefreshing(false);
			if (activity.COPY_PATH == null && activity.MOVE_PATH == null && 
				activity.EXTRACT_PATH == null && activity.EXTRACT_MOVE_PATH == null && commands.getVisibility() == View.VISIBLE) {//commands != null && 
				horizontalDivider6.setVisibility(View.GONE);
				commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
				commands.setVisibility(View.GONE);
			} else if (activity.COPY_PATH != null || activity.MOVE_PATH != null
					   || activity.EXTRACT_PATH != null || activity.EXTRACT_MOVE_PATH != null) {//commands != null && 
				if (commands.getVisibility() == View.GONE) {
					horizontalDivider6.setVisibility(View.VISIBLE);
					commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
					commands.setVisibility(View.VISIBLE);
				}
				updateDelPaste();
			}

			//Log.d("changeDir dataSourceL1", Util.collectionToString(dataSourceL1, true, "\r\n"));
			listView.setActivated(true);
			srcAdapter.notifyDataSetChanged();
			if (doScroll) {
				gridLayoutManager.scrollToPosition(0);
			}

			if (allCbx.isSelected()) {
				selectionStatusTV.setText(dataSourceL1.size() 
										  + "/" + dataSourceL1.size());
			} else {
				selectionStatusTV.setText(selectedInList1.size() 
										  + "/" + dataSourceL1.size());
			}
			Log.d(TAG, "LoadFiles.onPostExecute " + currentPathTitle + ", " + zip);

			updateDir();

			if (dataSourceL1.size() == 0) {
				nofilelayout.setVisibility(View.VISIBLE);
			} else {
				nofilelayout.setVisibility(View.GONE);
			}
			if (run != null) {
				run.run();
			}
		}

		@Override
		public void onProgressUpdate(String... message) {
			Log.d(TAG, "onProgressUpdate " + message[0]);
			showToast(message[0]);
		}
	}



	private class SearchFileNameTask extends AsyncTask<String, Long, List<ZipEntry>> {
		protected void onPreExecute() {
			searchVal = searchET.getText().toString();
			showToast("Searching...");
			dataSourceL1.clear();
			srcAdapter.notifyDataSetChanged();

			mSwipeRefreshLayout.setRefreshing(true);
		}

		@Override
		protected List<ZipEntry> doInBackground(String... params) {
			final List<ZipEntry> tempAppList = new LinkedList<>();
			final Collection<ZipEntry> keys = zip.entries.values();
			final int length = curPath.length() - 1;
			final String paramLowerCase = params[0].toLowerCase();
			Log.d("SearchFileNameTask", "currentPathTitle " + currentPathTitle + ", " + curPath + ", " + paramLowerCase);
			for (ZipEntry le : keys) {
				Log.d("SearchFileNameTask", le + ", " + le.path);
				if (le.parentPath.startsWith(curPath) && le.path.substring(length).toLowerCase().contains(paramLowerCase)) {
					tempAppList.add(le);
				}
			}
			Log.d("dataSourceL1 new task", Util.collectionToString(tempAppList, true, "\n"));
			Collections.sort(tempAppList, zipListSorter);
			return tempAppList;
		}

		@Override
		protected void onPostExecute(List<ZipEntry> result) {
			dataSourceL1.addAll(result);
			selectedInList1.clear();
			srcAdapter.notifyDataSetChanged();
			selectionStatusTV.setText(selectedInList1.size() 
									  + "/" + dataSourceL1.size());
			rightStatus.setText(
				Formatter.formatFileSize(activity, zip.file.length())
				+ "/" + Formatter.formatFileSize(activity, zip.unZipSize));
			if (dataSourceL1.size() == 0) {
				nofilelayout.setVisibility(View.VISIBLE);
			} else {
				nofilelayout.setVisibility(View.GONE);
			}
			mSwipeRefreshLayout.setRefreshing(false);
		}

		@Override
		protected void onCancelled(List<ZipEntry> result) {
			super.onCancelled(result);
			mSwipeRefreshLayout.setRefreshing(false);
		}
	}

	void setAllCbxChecked(boolean en) {
		allCbx.setSelected(en);
		if (en) {
			allCbx.setImageResource(R.drawable.ic_accept);
		} else {
			allCbx.setImageResource(R.drawable.dot);
		}
	}

	void moreInPanel(final View v) {
		final PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.inflate(R.menu.explorer_commands);
		final Menu menu = popup.getMenu();
		if (!activity.multiFiles) {
			menu.findItem(R.id.hide).setVisible(false);
			menu.findItem(R.id.swap).setVisible(false);
			menu.findItem(R.id.biggerequalpanel).setVisible(false);
		}
		MenuItem mi = menu.findItem(R.id.clearSelection);
		if (selectedInList1.size() == 0) {
			mi.setEnabled(false);
		} else {
			mi.setEnabled(true);
		}
		mi = menu.findItem(R.id.rangeSelection);
		if (selectedInList1.size() > 1) {
			mi.setEnabled(true);
		} else {
			mi.setEnabled(false);
		}
		mi = menu.findItem(R.id.undoClearSelection);
		if (tempSelectedInList1.size() > 0) {
			mi.setEnabled(true);
		} else {
			mi.setEnabled(false);
		}
//        mi = menu.findItem(R.id.hide);
//		if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT && activity.multiFiles && activity.right.getVisibility() == View.VISIBLE
//			|| slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT && activity.left.getVisibility() == View.VISIBLE) {
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
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					Log.d(TAG, item.getTitle() + ".");
					switch (item.getItemId())  {
						case R.id.rangeSelection:
							rangeSelection();
							break;
						case R.id.inversion:
							inversion();
							break;
						case R.id.clearSelection:
							clearSelection();
							break;
						case R.id.undoClearSelection:
							undoSelection();
							break;
						case R.id.swap:
							swap(v);
							break;
						case R.id.hide: 
							hide();
							break;
						case R.id.biggerequalpanel:
							biggerequalpanel();

					}
					return true;
				}
			});
		popup.show();
	}

	void updateStatus() {
		selectionStatusTV.setText(selectedInList1.size()  + "/" + dataSourceL1.size());
		srcAdapter.notifyDataSetChanged();
	}

	void rangeSelection() {
		int min = Integer.MAX_VALUE, max = -1;
		int cur = -3;
		tempSelectedInList1.clear();
		tempSelectedInList1.addAll(selectedInList1);
		for (ZipEntry s : selectedInList1) {
			cur = dataSourceL1.indexOf(s);
			if (cur > max) {
				max = cur;
			}
			if (cur < min && cur >= 0) {
				min = cur;
			}
		}
		selectedInList1.clear();
		for (cur = min; cur <= max; cur++) {
			selectedInList1.add(dataSourceL1.get(cur));
		}
		updateStatus();
	}

	void inversion() {
		tempSelectedInList1.clear();
		tempSelectedInList1.addAll(selectedInList1);
		final ArrayList<ZipEntry> listTemp = new ArrayList<>(4096);
		for (ZipEntry f : dataSourceL1) {
			if (!selectedInList1.contains(f)) {
				listTemp.add(f);
			}
		}
		selectedInList1.clear();
		selectedInList1.addAll(listTemp);
		updateStatus();
	}

//	void clearSelection() {
//		tempSelectedInList1.clear();
//		tempSelectedInList1.addAll(selectedInList1);
//		selectedInList1.clear();
//		updateStatus();
//	}
//
//	void undoSelection() {
//		selectedInList1.clear();
//		selectedInList1.addAll(tempSelectedInList1);
//		tempSelectedInList1.clear();
//		updateStatus();
//	}
}

	
	
