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
import com.amaze.filemanager.ui.LayoutElement;
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
import net.gnu.p7zip.DecompressTask;

public class ContentFragment extends FileFrag implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "ContentFragment";
	
	private static final int REQUEST_CODE_STORAGE_PERMISSION = 101;
    	
	private ScaleGestureDetector mScaleGestureDetector;
	private ImageButton dirMore;
	private TextView mMessageView;
	
	private SearchFileNameTask searchTask = new SearchFileNameTask();
	private TextSearch textSearch = new TextSearch();
	List<LayoutElement> dataSourceL1 = new LinkedList<>();
	List<LayoutElement> dataSourceL2;
	LayoutElement tempPreviewL2 = null;
	Button deletePastesBtn;
	ArrAdapter srcAdapter;
	
	private HorizontalScrollView scrolltext;
	private LinearLayout mDirectoryButtons;
	private ImageButton removeBtn;
	private ImageButton removeAllBtn;
	private ImageButton addBtn;
	private ImageButton addAllBtn;
	private LinearLayout selectionCommandsLayout;
	private LoadFiles loadList = new LoadFiles();
	private int file_count, folder_count, columns;
	private int sortby, dsort, asc;
    private String smbPath;
	private boolean mRetainSearchTask = false;
	private FileListSorter fileListSorter;
	private LinkedList<Map<String, Object>> backStack = new LinkedList<>();
	private LinkedList<String> history = new LinkedList<>();
	private FileObserver mFileObserver;
	private Drawable drawableDelete;
	private Drawable drawablePaste;
	View moreLeft;
	View moreRight;
	public boolean selection, results = false, SHOW_HIDDEN, CIRCULAR_IMAGES, SHOW_PERMISSIONS, SHOW_SIZE, SHOW_LAST_MODIFIED;
	String suffix = "*"; // "*" : files + folders,  "" only folder, ".*" only file "; *" split pattern
	String mimes = "*/*";
	boolean multiFiles = true;
	
	boolean mWriteableOnly;
	
	String[] previousSelectedStr;
	
	//int totalCount, progress;
	private boolean noMedia = false;
	private boolean displayHidden = true;
	private Pattern suffixPattern;
	DataUtils dataUtils = DataUtils.getInstance();
	
	@Override
	public String toString() {
		return "type " + type + ", " + slidingTabsFragment.side + ", fake=" + fake + ", suffix=" + suffix + ", mimes=" + mimes + ", multi=" + multiFiles + ", currentPathTitle " + currentPathTitle + ", " + super.toString();
	}

	public static ContentFragment newInstance(final SlidingTabsFragment sliding, final String dir, final String suffix, final String mimes, final boolean multiFiles, Bundle bundle) {//, int se) {//FragmentActivity ctx, 
        //Log.d(TAG, "newInstance dir " + dir + ", suffix " + suffix + ", multiFiles " + multiFiles);

		if (bundle == null) {
			bundle = new Bundle();
		}
		bundle.putString(ExplorerActivity.EXTRA_ABSOLUTE_PATH, dir);//EXTRA_DIR_PATH
		bundle.putString(ExplorerActivity.EXTRA_FILTER_FILETYPE, suffix);
		bundle.putBoolean(ExplorerActivity.EXTRA_MULTI_SELECT, multiFiles);

		final ContentFragment fragment = new ContentFragment();
		fragment.setArguments(bundle);
		fragment.currentPathTitle = dir;
		fragment.suffix = suffix.trim().toLowerCase();
		fragment.suffix = fragment.suffix == null ? "*" : fragment.suffix.toLowerCase();
		String suffixSpliter = fragment.suffix.replaceAll("[;\\s\\*\\.\\\\b]+", "|");
		suffixSpliter = suffixSpliter.startsWith("|") ? suffixSpliter.substring(1) : suffixSpliter;
		suffixSpliter = ".*?(" + suffixSpliter + ")";
		fragment.suffixPattern = Pattern.compile(suffixSpliter);

		fragment.mimes = mimes;
		fragment.mimes = fragment.mimes == null ? "*/*" : fragment.mimes.toLowerCase();
		fragment.multiFiles = multiFiles;
		fragment.slidingTabsFragment = sliding;
        Log.d(TAG, "newInstance " + fragment);
		return fragment;
    }

	@Override
	public void load(String path) {
		changeDir(path, false);
	}

	@Override
	public Frag clone(boolean fake) {
		final ContentFragment frag = new ContentFragment();
		frag.clone(this, fake);
		return frag;
	}

	@Override
	public void clone(final Frag frag, final boolean fake) {
		final ContentFragment contentFrag = (ContentFragment) frag;
		//Log.i(TAG, "clone " + frag + ", " + contentFrag.currentPathTitle + ", " + contentFrag.currentPathTitle + ", listView " + listView + ", srcAdapter " + srcAdapter + ", gridLayoutManager " + gridLayoutManager);
		type = contentFrag.type;
		currentPathTitle = contentFrag.currentPathTitle;
		
		suffix = contentFrag.suffix;
		String suffixSpliter = suffix.replaceAll("[;\\s\\*\\.\\\\b]+", "|");
		suffixSpliter = suffixSpliter.startsWith("|") ? suffixSpliter.substring(1) : suffixSpliter;
		suffixSpliter = ".*?(" + suffixSpliter + ")";
		suffixPattern = Pattern.compile(suffixSpliter);
		
		mimes = contentFrag.mimes;
		multiFiles = contentFrag.multiFiles;
		slidingTabsFragment = contentFrag.slidingTabsFragment;
		this.fake = fake;
		if (fake) {
			dataSourceL1 = contentFrag.dataSourceL1;
			selectedInList1 = contentFrag.selectedInList1;
			tempSelectedInList1 = contentFrag.tempSelectedInList1;
			tempOriDataSourceL1 = contentFrag.tempOriDataSourceL1;
		} else if (!contentFrag.searchMode) {
			dataSourceL1.clear();
			dataSourceL1.addAll(contentFrag.dataSourceL1);
			selectedInList1.clear();
			selectedInList1.addAll(contentFrag.selectedInList1);
			tempSelectedInList1.clear();
			tempSelectedInList1.addAll(contentFrag.tempSelectedInList1);
			tempOriDataSourceL1.clear();
			tempOriDataSourceL1.addAll(contentFrag.tempOriDataSourceL1);
		}
		spanCount = contentFrag.spanCount;
		dataSourceL2 = contentFrag.dataSourceL2;
		tempPreviewL2 = contentFrag.tempPreviewL2;
//		searchMode = contentFrag.searchMode;
//		searchVal = contentFrag.searchVal;
		srcAdapter = contentFrag.srcAdapter;
		if (listView != null && listView.getAdapter() != srcAdapter) {
			listView.setAdapter(srcAdapter);
		}

		if (allCbx != null) {
			if (dataSourceL1.size() == 0) {
				nofilelayout.setVisibility(View.VISIBLE);
				mSwipeRefreshLayout.setVisibility(View.GONE);
			} else {
				nofilelayout.setVisibility(View.GONE);
				mSwipeRefreshLayout.setVisibility(View.VISIBLE);
			}
			if (type == Frag.TYPE.EXPLORER) {
				setDirectoryButtons();
			}
			allName.setText(contentFrag.allName.getText());
			allType.setText(contentFrag.allType.getText());
			allDate.setText(contentFrag.allDate.getText());
			allSize.setText(contentFrag.allSize.getText());
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
			final int index = contentFrag.gridLayoutManager.findFirstVisibleItemPosition();
			final View vi = contentFrag.listView.getChildAt(0); 
			final int top = (vi == null) ? 0 : vi.getTop();
			gridLayoutManager.scrollToPositionWithOffset(index, top);
			if (contentFrag.selStatusLayout != null) {
				final int visibility = contentFrag.selStatusLayout.getVisibility();
				if (selStatusLayout.getVisibility() != visibility) {
					selStatusLayout.setVisibility(visibility);
					horizontalDivider0.setVisibility(visibility);
					horizontalDivider12.setVisibility(visibility);
					sortBarLayout.setVisibility(visibility);
				}
				selectionStatusTV.setText(contentFrag.selectionStatusTV.getText());
				rightStatus.setText(contentFrag.rightStatus.getText());
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

		if (slidingTabsFragment != null) {
			final int fragIndex;
			final String order;

			if (type == Frag.TYPE.EXPLORER) {
				fragIndex = slidingTabsFragment.indexOfMTabs(this);
				if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
					order = AndroidUtils.getSharedPreference(activity, "ContentFragSortType" + fragIndex, "Name ▲");
					spanCount = AndroidUtils.getSharedPreference(activity, "ContentFrag.SPAN_COUNT" + fragIndex, 1);
				} else {
					order = AndroidUtils.getSharedPreference(activity, "ExplorerFragSortType" + fragIndex, "Name ▲");
					spanCount = AndroidUtils.getSharedPreference(activity, "ExplorerFrag.SPAN_COUNT" + fragIndex, 1);
				} 
			} else {
				fragIndex = -1;
				if (slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT) {
					order = AndroidUtils.getSharedPreference(activity, "ContentFrag2SortTypeR", "Name ▲");
					spanCount = AndroidUtils.getSharedPreference(activity, "ContentFrag2.SPAN_COUNTR", 1);
				} else {
					order = AndroidUtils.getSharedPreference(activity, "ContentFrag2SortTypeL", "Name ▲");
					spanCount = AndroidUtils.getSharedPreference(activity, "ContentFrag2.SPAN_COUNTL", 1);
				}
			}
			Log.d(TAG, "onViewCreated index " + fragIndex + ", " + toString() + ", " + "args=" + args);
			//Log.d(TAG, "sharedPreference " + fragIndex + ", " + order);

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
			moreLeft = view.findViewById(R.id.moreLeft);
			moreLeft.setOnClickListener(this);
			moreRight = view.findViewById(R.id.moreRight);
			moreRight.setOnClickListener(this);
			if (activity.balance != 0) {
				moreLeft.setVisibility(View.GONE);
				moreRight.setVisibility(View.GONE);

				View findViewById = view.findViewById(R.id.book);
				findViewById.setVisibility(View.VISIBLE);

				findViewById = view.findViewById(R.id.hiddenfiles);
				findViewById.setVisibility(View.VISIBLE);

//			findViewById = view.findViewById(R.id.encrypts);
//			findViewById.setVisibility(View.VISIBLE);

				findViewById = view.findViewById(R.id.shortcuts);
				findViewById.setVisibility(View.VISIBLE);
			} else {
				if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT && !activity.swap
					|| slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT && activity.swap) {
					moreRight.setVisibility(View.GONE);
					moreLeft.setVisibility(View.VISIBLE);
				} else {
					moreLeft.setVisibility(View.GONE);
					moreRight.setVisibility(View.VISIBLE);
				}
			}
			view.findViewById(R.id.book).setOnClickListener(this);
			view.findViewById(R.id.hiddenfiles).setOnClickListener(this);
			//view.findViewById(R.id.encrypts).setOnClickListener(this);
			view.findViewById(R.id.infos).setOnClickListener(this);
			view.findViewById(R.id.shortcuts).setOnClickListener(this);
			view.findViewById(R.id.compresss).setOnClickListener(this);
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

			if (type == Frag.TYPE.SELECTION) {
				removeBtn = (ImageButton) view.findViewById(R.id.remove);
				removeAllBtn = (ImageButton) view.findViewById(R.id.removeAll);
				addBtn = (ImageButton) view.findViewById(R.id.add);
				addAllBtn = (ImageButton) view.findViewById(R.id.addAll);
				selectionCommandsLayout = (LinearLayout) view.findViewById(R.id.selectionCommandsLayout);
				topflipper.setDisplayedChild(topflipper.indexOfChild(selectionCommandsLayout));
				dirMore.setVisibility(View.GONE);
				if (dataSourceL1.size() == 0) {
					searchButton.setEnabled(false);
					nofilelayout.setVisibility(View.VISIBLE);
					mSwipeRefreshLayout.setVisibility(View.GONE);
				} else {
					searchButton.setEnabled(true);
					nofilelayout.setVisibility(View.GONE);
					mSwipeRefreshLayout.setVisibility(View.VISIBLE);
				}
				removeBtn.setOnClickListener(this);
				removeAllBtn.setOnClickListener(this);
				addBtn.setOnClickListener(this);
				addAllBtn.setOnClickListener(this);
			} else {
				dirMore.setOnClickListener(this);
				topflipper.setDisplayedChild(topflipper.indexOfChild(scrolltext));
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
			mSwipeRefreshLayout.setOnRefreshListener(this);
			mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {

					@Override
					public boolean onScale(ScaleGestureDetector detector) {
						Log.d(TAG, "onScale getCurrentSpan " + detector.getCurrentSpan() + ", getPreviousSpan " + detector.getPreviousSpan() + ", getTimeDelta " + detector.getTimeDelta());
						//if (detector.getCurrentSpan() > 300 && detector.getTimeDelta() > 50) {
						//Log.d(TAG, "onScale " + (detector.getCurrentSpan() - detector.getPreviousSpan()) + ", getTimeDelta " + detector.getTimeDelta());
						//mScaling = true;
						mSwipeRefreshLayout.setEnabled(false);
						if (detector.getCurrentSpan() - detector.getPreviousSpan() < -80 * activity.density) {
							if (spanCount == 1) {
								spanCount = 2;
								setRecyclerViewLayoutManager();
								mSwipeRefreshLayout.setEnabled(true);
								return true;
							} else if (spanCount == 2 && slidingTabsFragment.width >= 0) {
								if (activity.right.getVisibility() == View.GONE || activity.left.getVisibility() == View.GONE) {
									spanCount = 8;
								} else {
									spanCount = 4;
								}
								setRecyclerViewLayoutManager();
								mSwipeRefreshLayout.setEnabled(true);
								return true;
							}
						} else if (detector.getCurrentSpan() - detector.getPreviousSpan() > 80 * activity.density) {
							if ((spanCount == 4 || spanCount == 8)) {
								spanCount = 2;
								setRecyclerViewLayoutManager();
								mSwipeRefreshLayout.setEnabled(true);
								return true;
							} else if (spanCount == 2) {
								spanCount = 1;
								setRecyclerViewLayoutManager();
								mSwipeRefreshLayout.setEnabled(true);
								return true;
							} 
						}
						//}
						//mScaling = false;
						mSwipeRefreshLayout.setEnabled(true);
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
				if (currentPathTitle.length() == 0) {//"".equals(currentPathTitle) || 
					currentPathTitle = args.getString(ExplorerActivity.EXTRA_ABSOLUTE_PATH);//EXTRA_DIR_PATH);
				} else {
					args.putString(ExplorerActivity.EXTRA_ABSOLUTE_PATH, currentPathTitle);//EXTRA_DIR_PATH
				}
				//Log.d(TAG, "onViewCreated.dir " + dir);
				suffix = args.getString(ExplorerActivity.EXTRA_FILTER_FILETYPE, "*").trim().toLowerCase();
				String suffixSpliter = suffix.replaceAll("[;\\s\\*\\.\\\\b]+", "|");
				suffixSpliter = suffixSpliter.startsWith("|") ? suffixSpliter.substring(1) : suffixSpliter;
				suffixSpliter = ".*?(" + suffixSpliter + ")";
				suffixPattern = Pattern.compile(suffixSpliter);

				mimes = args.getString(ExplorerActivity.EXTRA_FILTER_MIMETYPE);
				mimes = mimes == null ? "*/*" : mimes.toLowerCase();
				//Log.d(TAG, "onViewCreated.suffix " + suffix);
				multiFiles = args.getBoolean(ExplorerActivity.EXTRA_MULTI_SELECT);
				//Log.d(TAG, "onViewCreated.multiFiles " + multiFiles);
				if (savedInstanceState == null && args.getStringArrayList("dataSourceL1") != null) {
					savedInstanceState = args;
				}

				if (!multiFiles) {
					allCbx.setVisibility(View.GONE);
				}
			}

			allName.setText("Name");
			allSize.setText("Size");
			allDate.setText("Date");
			allType.setText("Type");
			switch (order) {
				case "Name ▼":
					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.NAME, FileListSorter.DESCENDING);
					allName.setText("Name ▼");
					break;
				case "Date ▲":
					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.DATE, FileListSorter.ASCENDING);
					allDate.setText("Date ▲");
					break;
				case "Date ▼":
					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.DATE, FileListSorter.DESCENDING);
					allDate.setText("Date ▼");
					break;
				case "Size ▲":
					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.SIZE, FileListSorter.ASCENDING);
					allSize.setText("Size ▲");
					break;
				case "Size ▼":
					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.SIZE, FileListSorter.DESCENDING);
					allSize.setText("Size ▼");
					break;
				case "Type ▲":
					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.TYPE, FileListSorter.ASCENDING);
					allType.setText("Type ▲");
					break;
				case "Type ▼":
					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.TYPE, FileListSorter.DESCENDING);
					allType.setText("Type ▼");
					break;
				default:
					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.NAME, FileListSorter.ASCENDING);
					allName.setText("Name ▲");
					break;
			}

			//Log.d(TAG, "onViewCreated " + this + ", ctx=" + getContext());
			if (savedInstanceState != null && savedInstanceState.getString(ExplorerActivity.EXTRA_ABSOLUTE_PATH) != null) {//EXTRA_DIR_PATH
				currentPathTitle = savedInstanceState.getString(ExplorerActivity.EXTRA_ABSOLUTE_PATH);//EXTRA_DIR_PATH
				suffix = savedInstanceState.getString(ExplorerActivity.EXTRA_FILTER_FILETYPE, "*");
				mimes = savedInstanceState.getString(ExplorerActivity.EXTRA_FILTER_MIMETYPE, "*/*");
				multiFiles = savedInstanceState.getBoolean(ExplorerActivity.EXTRA_MULTI_SELECT, true);
				currentPathTitle = (String) savedInstanceState.get("currentPathTitle");

				allCbx.setEnabled(savedInstanceState.getBoolean("allCbx.isEnabled"));
				setRecyclerViewLayoutManager();
				//Log.d(TAG, "configurationChanged " + activity.configurationChanged);
				if (type == Frag.TYPE.EXPLORER) {//} && !fake) {// && !activity.configurationChanged
					//updateDir(currentPathTitle, ContentFragment.this);
					setDirectoryButtons();
				}
				final int index  = savedInstanceState.getInt("index");
				final int top  = savedInstanceState.getInt("top");
				//Log.d(TAG, "index = " + index + ", " + top);
				gridLayoutManager.scrollToPositionWithOffset(index, top);
			} else {
				//srcAdapter = new ArrAdapter(dataSourceL1);
				//listView1.setAdapter(srcAdapter);
				setRecyclerViewLayoutManager();
				if (type == Frag.TYPE.EXPLORER && !fake) {
					if (currentPathTitle != null) {
						changeDir(currentPathTitle, false);
					} else if (searchMode) {
						searchMode = !searchMode;
						manageSearchUI(searchMode);
						changeDir(currentPathTitle, false);
					}
				}
			}
			updateColor(view);
		}
	}

	void notifyDataSetChanged() {
		srcAdapter.notifyDataSetChanged();
	}

	@Override
    public void onRefresh() {
        final Editable s = searchET.getText();
		if (s.length() > 0) {
			textSearch.afterTextChanged(s);
		} else if (type == Frag.TYPE.EXPLORER) {
        	changeDir(currentPathTitle, false);
		} else {
			LayoutElement f;
			boolean changed = false;
        	for (int i = dataSourceL1.size() - 1; i >= 0; i--) {
				f = dataSourceL1.get(i);
				if (!f.bf.f.exists()) {
					changed = true;
					dataSourceL1.remove(i);
					selectedInList1.remove(f);
					tempOriDataSourceL1.remove(f);
					tempSelectedInList1.remove(f);
					if (tempPreviewL2 != null && f.path.equals(tempPreviewL2.path)) {
						tempPreviewL2 = null;
					}
				}
			}
			if (changed) {
				srcAdapter.notifyDataSetChanged();
				updateStatus();
			}
			mSwipeRefreshLayout.setRefreshing(false);
		}
    }

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		//Log.d(TAG, "onSaveInstanceState " + indexOf + ", fake=" + fake + ", " + currentPathTitle + ", " + outState);
		if (fake) {
			return;
		}
		if (type == Frag.TYPE.EXPLORER) {
			final int fragIndex = slidingTabsFragment.indexOfMTabs(this);
			if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
				AndroidUtils.setSharedPreference(activity, "ContentFrag.SPAN_COUNT" + fragIndex, spanCount);
			} else {
				AndroidUtils.setSharedPreference(activity, "ExplorerFrag.SPAN_COUNT" + fragIndex, spanCount);
			} 
		} else {
			if (slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT) {
				AndroidUtils.setSharedPreference(activity, "ContentFrag2.SPAN_COUNTR", spanCount);
			} else {
				AndroidUtils.setSharedPreference(activity, "ContentFrag2.SPAN_COUNTL", spanCount);
			}
		}
		//Log.d(TAG, "SPAN_COUNT.ContentFrag" + activity.slideFrag.indexOf(this));
		
//		if (tempPreviewL2 != null) {
//			outState.putString("tempPreviewL2", tempPreviewL2.path);
//		}
		outState.putString(ExplorerActivity.EXTRA_ABSOLUTE_PATH, currentPathTitle);//EXTRA_DIR_PATH
		outState.putString(ExplorerActivity.EXTRA_FILTER_FILETYPE, suffix);
		outState.putString(ExplorerActivity.EXTRA_FILTER_MIMETYPE, mimes);
		outState.putBoolean(ExplorerActivity.EXTRA_MULTI_SELECT, multiFiles);
//		outState.putStringArrayList("selectedInList1", Util.collectionFile2StringArrayList(selectedInList1));
//		outState.putStringArrayList("dataSourceL1", Util.collectionFile2StringArrayList(dataSourceL1));
//		outState.putBoolean("searchMode", searchMode);
//		outState.putString("searchVal", quicksearch.getText().toString());
//		outState.putString("currentPathTitle", currentPathTitle);
		outState.putBoolean("allCbx.isEnabled", allCbx.isEnabled());
		outState.putString("currentPathTitle", currentPathTitle);
		
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
		outState.put(ExplorerActivity.EXTRA_ABSOLUTE_PATH, currentPathTitle);//EXTRA_DIR_PATH
		outState.put(ExplorerActivity.EXTRA_FILTER_FILETYPE, suffix);
		outState.put(ExplorerActivity.EXTRA_FILTER_MIMETYPE, mimes);
		outState.put(ExplorerActivity.EXTRA_MULTI_SELECT, multiFiles);
		
		final ArrayList<LayoutElement> dataSource = new ArrayList<>(dataSourceL1.size());
		dataSource.addAll(dataSourceL1);
		outState.put("dataSourceL1", dataSource);
		
		final ArrayList<LayoutElement> selectedInList = new ArrayList<>(selectedInList1.size());
		selectedInList.addAll(selectedInList1);
		outState.put("selectedInList1", selectedInList);
		
		outState.put("searchMode", searchMode);
		outState.put("searchVal", searchET.getText().toString());
		outState.put("currentPathTitle", currentPathTitle);
		outState.put("allCbx.isEnabled", allCbx.isEnabled());

		final int index = gridLayoutManager.findFirstVisibleItemPosition();
        final View vi = listView.getChildAt(0); 
        final int top = (vi == null) ? 0 : vi.getTop();
		outState.put("index", index);
		outState.put("top", top);

        return outState;
	}

	void reload(Map<String, Object> savedInstanceState) {
		Log.d(TAG, "reload currentPathTitle " + currentPathTitle + ", "  + savedInstanceState);
		currentPathTitle = (String) savedInstanceState.get(ExplorerActivity.EXTRA_ABSOLUTE_PATH);//EXTRA_DIR_PATH
		suffix = (String) savedInstanceState.get(ExplorerActivity.EXTRA_FILTER_FILETYPE);
		mimes = (String) savedInstanceState.get(ExplorerActivity.EXTRA_FILTER_MIMETYPE);
		multiFiles = savedInstanceState.get(ExplorerActivity.EXTRA_MULTI_SELECT);
		selectedInList1.clear();
		selectedInList1.addAll((ArrayList<LayoutElement>) savedInstanceState.get("selectedInList1"));
		dataSourceL1.clear();
		dataSourceL1.addAll((ArrayList<LayoutElement>) savedInstanceState.get("dataSourceL1"));
		
		if (type == Frag.TYPE.SELECTION) {
			tempOriDataSourceL1.clear();
			tempOriDataSourceL1.addAll(dataSourceL1);
		}
		searchMode = savedInstanceState.get("searchMode");
		searchVal = (String) savedInstanceState.get("searchVal");
		currentPathTitle = (String) savedInstanceState.get("currentPathTitle");
		
		allCbx.setEnabled(savedInstanceState.get("allCbx.isEnabled"));
		srcAdapter.notifyDataSetChanged();
		
		setRecyclerViewLayoutManager();
		gridLayoutManager.scrollToPositionWithOffset(savedInstanceState.get("index"), savedInstanceState.get("top"));

		updateDir(currentPathTitle);
	}

	boolean isEncryptOpen = false;       // do we have to open a file when service is begin destroyed
    BaseFile encryptBaseFile;            // the cached base file which we're to open, delete it later

    private BroadcastReceiver decryptReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (isEncryptOpen && encryptBaseFile != null) {
                activity.getFutils().openFile(new File(encryptBaseFile.getPath()), activity);
                isEncryptOpen = false;
            }
        }
    };

	@Override
    public void onPause() {
        //Log.d(TAG, "onPause " + toString());
		super.onPause();
        fragActivity.unregisterReceiver(receiver2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            fragActivity.unregisterReceiver(decryptReceiver);
        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (!isEncryptOpen && encryptBaseFile != null) {
                // we've opened the file and are ready to delete it
                ArrayList<BaseFile> baseFiles = new ArrayList<>();
                baseFiles.add(encryptBaseFile);
                new DeleteTask(fragActivity, null).execute(baseFiles);
            }
        }
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
		if (type == Frag.TYPE.EXPLORER && !fake) {
			if (currentPathTitle != null) {
				changeDir(currentPathTitle, false);
			} else {
				updateDir(currentPathTitle);
			}
		}
    }

	void setDirectoryButtons() {
		Log.d(TAG, "setDirectoryButtons " + type + ", " + currentPathTitle);
		//topflipper.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));

		if (currentPathTitle != null) {
			mDirectoryButtons.removeAllViews();
			String[] parts = currentPathTitle.split("/");

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
			ib.setTextColor(ExplorerActivity.TEXT_COLOR);
			// ib.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
			ib.setGravity(Gravity.CENTER);
			ib.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						changeDir("/", true);

					}
				});
			mDirectoryButtons.addView(ib);

			String folder = "";
			View v;
			TextView b = null;
			for (int i = 1; i < parts.length; i++) {
				folder += "/" + parts[i];
				v = activity.getLayoutInflater().inflate(R.layout.dir, null);
				b = (TextView) v.findViewById(R.id.name);
				b.setText(parts[i]);
				b.setTag(folder);
				b.setTextColor(ExplorerActivity.TEXT_COLOR);
				b.setOnClickListener(new View.OnClickListener() {
						public void onClick(View view) {
							String dir2 = (String) view.getTag();
							if (dir2.equals(currentPathTitle)) {
								changeDir(dir2, false);
							} else {
								changeDir(dir2, true);
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
								editText.setText(currentPathTitle);
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
										File newF = new File(name);
										if (newF.exists()) {
											if (newF.isDirectory()) {
												currentPathTitle = name;
												changeDir(currentPathTitle, true);
											} else {
												currentPathTitle = newF.getParent();
												changeDir(newF.getParentFile().getAbsolutePath(), true);
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

	public void updateDir(String d) {
		Log.d(TAG, "updateDir " + d);
		if (openMode != OpenMode.CUSTOM) {
			setDirectoryButtons();
			activity.dir = d;
		}
		if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {//}cf == activity.slideFrag.getCurrentFragment()) {
			activity.slideFrag.notifyTitleChange();
		} else if (activity.slideFrag2 != null) {//} && cf == activity.slideFrag2.getCurrentFragment()) {
			activity.slideFrag2.notifyTitleChange();
		}
	}

    /**
     * Sets up a FileObserver to watch the current directory.
     */
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
								updateList();
							}
						});
                }
            }
        };
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume index " + activity.slideFrag.indexOfMTabs(this) + ", " + /*activity.slideFrag2.indexOfMTabs(this) + ", " + */ slidingTabsFragment.side + ", " + type + ", fake=" + fake + ", " + currentPathTitle + ", currentPathTitle=" + currentPathTitle);
		super.onResume();
		fragActivity.registerReceiver(receiver2, new IntentFilter("loadlist"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            fragActivity.registerReceiver(decryptReceiver, new IntentFilter(EncryptDecryptUtils.DECRYPT_BROADCAST));
        }
		if (type == Frag.TYPE.EXPLORER) {
			getView().setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
			if (mFileObserver != null) {
				mFileObserver.stopWatching();
			}
			mFileObserver = createFileObserver(currentPathTitle);
			mFileObserver.startWatching();
			
			activity = (ExplorerActivity)getActivity();

			selectionStatusTV.setText(selectedInList1.size() 
									 + "/" + dataSourceL1.size());

			final File curDir = new File(currentPathTitle);//currentPathTitle == null ? currentPathTitle : currentPathTitle);
			rightStatus.setText(
				"Free " + Util.nf.format(curDir.getFreeSpace() / (1 << 20))
				+ " MiB. Used " + Util.nf.format((curDir.getTotalSpace() - curDir.getFreeSpace()) / (1 << 20))
				+ " MiB. Total " + Util.nf.format(curDir.getTotalSpace() / (1 << 20)) + " MiB");
		} else {
			if (slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT) {
				activity.curContentFrag.dataSourceL2 = dataSourceL1;
				if (activity.curContentFrag.srcAdapter != null) {
					activity.curContentFrag.srcAdapter.notifyDataSetChanged();
				}
			} else if (activity.slideFrag2 != null) {
				activity.curExplorerFrag.dataSourceL2 = dataSourceL1;
				if (activity.curExplorerFrag.srcAdapter != null) {
					activity.curExplorerFrag.srcAdapter.notifyDataSetChanged();
				}
			}
			updateColor(null);
		}
	}

	public void updateColor(View rootView) {
		getView().setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
		icons.setColorFilter(ExplorerActivity.TEXT_COLOR);
		allName.setTextColor(ExplorerActivity.TEXT_COLOR);
		allDate.setTextColor(ExplorerActivity.TEXT_COLOR);
		allSize.setTextColor(ExplorerActivity.TEXT_COLOR);
		allType.setTextColor(ExplorerActivity.TEXT_COLOR);
		selectionStatusTV.setTextColor(ExplorerActivity.TEXT_COLOR);
		rightStatus.setTextColor(ExplorerActivity.TEXT_COLOR);
		searchET.setTextColor(ExplorerActivity.TEXT_COLOR);
		clearButton.setColorFilter(ExplorerActivity.TEXT_COLOR);
		searchButton.setColorFilter(ExplorerActivity.TEXT_COLOR);
		noFileImage.setColorFilter(ExplorerActivity.TEXT_COLOR);
		noFileText.setTextColor(ExplorerActivity.TEXT_COLOR);
		dirMore.setColorFilter(ExplorerActivity.TEXT_COLOR);
		horizontalDivider0.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
		horizontalDivider12.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
		horizontalDivider7.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
		if (type == Frag.TYPE.SELECTION) {
			addBtn.setColorFilter(ExplorerActivity.TEXT_COLOR);
			addAllBtn.setColorFilter(ExplorerActivity.TEXT_COLOR);
			removeBtn.setColorFilter(ExplorerActivity.TEXT_COLOR);
			removeAllBtn.setColorFilter(ExplorerActivity.TEXT_COLOR);
		}
	}

	void updateL2() {
		Collections.sort(dataSourceL1, fileListSorter);
		updateTemp();
	}

	void updateTemp() {
		tempOriDataSourceL1.clear();
		tempOriDataSourceL1.addAll(dataSourceL1);
	}

	void removeAllDS2(final Collection<LayoutElement> c) {
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
		if (type == Frag.TYPE.EXPLORER) {
			if (currentPathTitle == null) {
				return "";//new File("").getName();
			} else if ("/".equals(currentPathTitle)) {
				return "/";
			} else if (openMode.equals(openMode.CUSTOM)) {
				String path = null;
				switch (Integer.parseInt(currentPathTitle)) {
					case 0:
						path = "Images";
						break;
					case 1:
						path = "Videos";
						break;
					case 2:
						path = "Audio";
						break;
					case 3:
						path = "Docs";
						break;
					case 4:
						path = "Apk";
						break;
					case 5:
						path = "Recent";
						break;
					case 6:
						path = "Recent Files";
						break;
				}
				return path;
			} else {
				return new File(currentPathTitle).getName();
			}
		} else {
			return title;
		}
	}

	
	public ArrayList<LayoutElement> addToSmb(SmbFile[] mFile, String path) throws SmbException {
        ArrayList<LayoutElement> a = new ArrayList<>(mFile.length);
        //if (searchHelper.size() > 500) searchHelper.clear();
        for (SmbFile aMFile : mFile) {
            if (dataUtils.getHiddenfiles().contains(aMFile.getPath()))
                continue;
            String name = aMFile.getName();
            name = (aMFile.isDirectory() && name.endsWith("/")) ? name.substring(0, name.length() - 1) : name;
            if (path.equals(smbPath)) {
                if (name.endsWith("$")) continue;
            }
            if (aMFile.isDirectory()) {
                folder_count++;
//                LayoutElement layoutElement = new LayoutElement(folder, name, aMFile.getPath(),
//																"", "", "", 0, false, aMFile.lastModified() + "", true);
                LayoutElement layoutElement = new LayoutElement(name, aMFile.getPath(),
																 "", "", /*"", */aMFile.length(), /*false, */aMFile.lastModified(), true);
				layoutElement.setMode(OpenMode.SMB);
                //searchHelper.add(layoutElement.generateBaseFile());
                a.add(layoutElement);
            } else {
                file_count++;
                try {
//                    LayoutElement layoutElement = new LayoutElement(
//						Icons.loadMimeIcon(aMFile.getPath(), !IS_LIST, res), name,
//						aMFile.getPath(), "", "", Formatter.formatFileSize(getContext(),
//																		   aMFile.length()), aMFile.length(), false,
//						aMFile.lastModified() + "", false);
                    LayoutElement layoutElement = new LayoutElement(
						//Icons.loadMimeIcon(mFile[i].getPath(), !IS_LIST, res), 
						name,
						aMFile.getPath(), "", "", //Formatter.formatFileSize(getContext(), mFile[i].length()), 
						aMFile.length(), //false,
						aMFile.lastModified(), false);
						layoutElement.setMode(OpenMode.SMB);
                    //searchHelper.add(layoutElement.generateBaseFile());
                    a.add(layoutElement);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return a;
    }
	
    public void reauthenticateSmb() {
        if (smbPath != null) {
            try {
                activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							int i;
							if ((i = dataUtils.containsServer(smbPath)) != -1) {
								activity.showSMBDialog(dataUtils.getServers().get(i)[0], smbPath, true);
							}
						}
					});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
		deletePastesBtn.getCompoundDrawables()[1].setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
	}


	public void loadlist(String path, /*boolean back, */OpenMode openMode) {
//        if (mActionMode != null) {
//            mActionMode.finish();
//        }
        /*if(openMode==-1 && android.util.Patterns.EMAIL_ADDRESS.matcher(path).matches())
		 bindDrive(path);
		 else */
//        if (loadList != null) loadList.cancel(true);
//        loadList = new LoadFiles();//LoadList(activity, activity, /*back, */this, openMode);
//        loadList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (path));
		changeDir(path, true);
		this.openMode = openMode;
    }

    private BroadcastReceiver receiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // load the list on a load broadcast
            switch (openMode) {
                case ROOT:
                case FILE:
                    // local file system don't need an explicit load, we've set an observer to
                    // take actions on creation/moving/deletion/modification of file on current path
                    //break;
                default:
                    updateList();
                    break;
            }
        }
    };

	private void addFiles(final ContentFragment curContentFrag, final ContentFragment curSelectionFrag2) {
		curContentFrag.dataSourceL2 = curSelectionFrag2.dataSourceL1;
		if (curContentFrag.selectedInList1.size() > 0) {
			if (multiFiles) {
				String st2;
				//File file;
				int size;
				for (LayoutElement file : curContentFrag.selectedInList1) {
					//file = new File(st);
					if (file.isDirectory) {
						size = curSelectionFrag2.dataSourceL1.size();
						st2 = file.path + "/";
						for (int i = 0; i >= 0 && i < size; i++) {
							if (curSelectionFrag2.dataSourceL1.get(i).path.startsWith(st2)) {
								curSelectionFrag2.dataSourceL1.remove(i);
								curSelectionFrag2.tempOriDataSourceL1.remove(i);
								i--;
								size--;
							}
						}
					}
					if (!curSelectionFrag2.dataSourceL1.contains(file)
						&& file.bf.exists()) {
						curSelectionFrag2.dataSourceL1.add(file);
						curSelectionFrag2.tempOriDataSourceL1.add(file);
					}
				}
				boolean allInclude = true;

				//final String dirSt = dir.endsWith("/") ? dir : dir + "/";
				for (LayoutElement st : curContentFrag.dataSourceL1) {
					if (!curSelectionFrag2.dataSourceL1.contains(st)) {
						allInclude = false;
						break;
					}
				}
				if (allInclude) {
					curContentFrag.setAllCbxChecked(true);// allCbx.setChecked(true);
					curContentFrag.allCbx.setEnabled(false);// allCbx.setEnabled(false);
				}
			} else {
				curSelectionFrag2.dataSourceL1.clear();
				curSelectionFrag2.dataSourceL1.addAll(curContentFrag.selectedInList1);
				curSelectionFrag2.tempOriDataSourceL1.clear();
				curSelectionFrag2.tempOriDataSourceL1.addAll(curContentFrag.selectedInList1);
			}
			curSelectionFrag2.updateL2();
			curSelectionFrag2.allCbx.setSelected(false);
			if (curSelectionFrag2.selectedInList1.size() == 0) {
				curSelectionFrag2.allCbx.setImageResource(R.drawable.dot);
			} else {
				curSelectionFrag2.allCbx.setImageResource(R.drawable.ready);
			}
			curSelectionFrag2.allCbx.setEnabled(true);
			curContentFrag.selectedInList1.clear();
			if (curSelectionFrag2.dataSourceL1.size() == 0) {
				curSelectionFrag2.searchButton.setEnabled(false);
				curSelectionFrag2.nofilelayout.setVisibility(View.VISIBLE);
				curSelectionFrag2.mSwipeRefreshLayout.setVisibility(View.GONE);
			} else {
				if (curSelectionFrag2.gridLayoutManager == null || curSelectionFrag2.gridLayoutManager.getSpanCount() != curSelectionFrag2.spanCount) {
					curSelectionFrag2.gridLayoutManager = new GridLayoutManager(activity, curSelectionFrag2.spanCount);
					curSelectionFrag2.listView.setLayoutManager(curSelectionFrag2.gridLayoutManager);
				}
				curSelectionFrag2.listView.removeItemDecoration(curSelectionFrag2.dividerItemDecoration);
				curSelectionFrag2.listView.invalidateItemDecorations();
				if (curSelectionFrag2.spanCount <= 2) {
					curSelectionFrag2.dividerItemDecoration = new GridDividerItemDecoration(activity, true);
					curSelectionFrag2.listView.addItemDecoration(curSelectionFrag2.dividerItemDecoration);
				}
				curSelectionFrag2.searchButton.setEnabled(true);
				curSelectionFrag2.nofilelayout.setVisibility(View.GONE);
				curSelectionFrag2.mSwipeRefreshLayout.setVisibility(View.VISIBLE);
			}
			curContentFrag.notifyDataSetChanged();// srcAdapter.notifyDataSetChanged();
			curSelectionFrag2.notifyDataSetChanged();// destAdapter.notifyDataSetChanged();
			curContentFrag.selectionStatusTV
				.setText(curContentFrag.selectedInList1.size() + "/"
						 + curContentFrag.dataSourceL1.size());
			curSelectionFrag2.selectionStatusTV
				.setText(curSelectionFrag2.selectedInList1.size() + "/"
						 + curSelectionFrag2.dataSourceL1.size());
		}
	}

	private void addAllFiles(final ContentFragment curContentFrag, final ContentFragment curSelectionFrag2) {
		curContentFrag.dataSourceL2 = curSelectionFrag2.dataSourceL1;
		final String dirSt = activity.dir.endsWith("/") ? activity.dir : activity.dir + "/";
		Log.d(TAG, "addAllFiles " + dirSt);
		if (multiFiles) {
			String st3;
			//File file;
			int size;
			for (LayoutElement file : curContentFrag.dataSourceL1) {
				if (file.isDirectory) {
					st3 = file.path + "/"; 
					size = curSelectionFrag2.dataSourceL1.size();
					for (int i = 0; i >= 0 && i < size; i++) {
						if (curSelectionFrag2.dataSourceL1.get(i).path.startsWith(st3)) {
							curSelectionFrag2.dataSourceL1.remove(i);
							curSelectionFrag2.tempOriDataSourceL1.remove(i);
							i--;
							size--;
						}
					}
				}
				if (!curSelectionFrag2.dataSourceL1.contains(file)
					&& file.bf.exists() && file.bf.f.canRead()) {
					curSelectionFrag2.dataSourceL1.add(file);
					curSelectionFrag2.tempOriDataSourceL1.add(file);
				}
			}

			curContentFrag.setAllCbxChecked(true);// allCbx.setChecked(true);
			curContentFrag.selectedInList1.clear();
			curSelectionFrag2.updateL2();
			curContentFrag.notifyDataSetChanged();// srcAdapter.notifyDataSetChanged();
			curSelectionFrag2.notifyDataSetChanged();// destAdapter.notifyDataSetChanged();
			curContentFrag.allCbx.setEnabled(false);// allCbx.setEnabled(false);
			curSelectionFrag2.allCbx.setSelected(false);

			if (curSelectionFrag2.dataSourceL1.size() == 0) {
				curSelectionFrag2.searchButton.setEnabled(false);
				curSelectionFrag2.nofilelayout.setVisibility(View.VISIBLE);
				curSelectionFrag2.mSwipeRefreshLayout.setVisibility(View.GONE);
			} else {
				if (curSelectionFrag2.gridLayoutManager == null || curSelectionFrag2.gridLayoutManager.getSpanCount() != curSelectionFrag2.spanCount) {
					curSelectionFrag2.gridLayoutManager = new GridLayoutManager(activity, curSelectionFrag2.spanCount);
					curSelectionFrag2.listView.setLayoutManager(curSelectionFrag2.gridLayoutManager);
				}
				curSelectionFrag2.listView.removeItemDecoration(curSelectionFrag2.dividerItemDecoration);
				curSelectionFrag2.listView.invalidateItemDecorations();
				if (curSelectionFrag2.spanCount <= 2) {
					curSelectionFrag2.dividerItemDecoration = new GridDividerItemDecoration(activity, true);
					curSelectionFrag2.listView.addItemDecoration(curSelectionFrag2.dividerItemDecoration);
				}
				curSelectionFrag2.searchButton.setEnabled(true);
				curSelectionFrag2.nofilelayout.setVisibility(View.GONE);
				curSelectionFrag2.mSwipeRefreshLayout.setVisibility(View.VISIBLE);
			}
			curSelectionFrag2.allCbx.setEnabled(true);
			curContentFrag.selectionStatusTV
				.setText(curContentFrag.dataSourceL1.size() + "/"
						 + curContentFrag.dataSourceL1.size());
			curSelectionFrag2.selectionStatusTV
				.setText(curSelectionFrag2.selectedInList1.size() + "/"
						 + curSelectionFrag2.dataSourceL1.size());
		} else {
			LayoutElement file = curContentFrag.dataSourceL1.get(0);//new File(dirSt, curContentFrag.dataSourceL1.get(0));
			if (curContentFrag.dataSourceL1.size() == 1 && file.bf.exists()
				&& !file.isDirectory) {
				curSelectionFrag2.dataSourceL1.clear();
				curSelectionFrag2.dataSourceL1.add(curContentFrag.dataSourceL1.get(0));

				curSelectionFrag2.tempOriDataSourceL1.clear();
				curSelectionFrag2.tempOriDataSourceL1.add(curContentFrag.dataSourceL1.get(0));

				curContentFrag.selectedInList1.clear();
				curContentFrag.notifyDataSetChanged();// srcAdapter.notifyDataSetChanged();
				curSelectionFrag2.notifyDataSetChanged();// destAdapter.notifyDataSetChanged();
			}
		}
	}

	private void removeAllFiles(final ContentFragment curContentFrag, final ContentFragment curSelectionFrag2) {
		curContentFrag.dataSourceL2 = curSelectionFrag2.dataSourceL1;
		//curContentFrag.srcAdapter.dataSourceL2 = curContentFrag.dataSourceL2;
		curSelectionFrag2.dataSourceL1.clear();
		curSelectionFrag2.tempOriDataSourceL1.clear();
		curSelectionFrag2.selectedInList1.clear();
		curContentFrag.allCbx.setEnabled(true);// allCbx.setEnabled(true);
		curSelectionFrag2.allCbx.setSelected(false);//.setChecked(false);
		curSelectionFrag2.allCbx.setImageResource(R.drawable.dot);
		curSelectionFrag2.allCbx.setEnabled(false);
		curSelectionFrag2.nofilelayout.setVisibility(View.VISIBLE);
		curSelectionFrag2.mSwipeRefreshLayout.setVisibility(View.GONE);
		curSelectionFrag2.searchButton.setEnabled(false);
		curSelectionFrag2.listView.removeItemDecoration(curSelectionFrag2.dividerItemDecoration);
		curSelectionFrag2.listView.invalidateItemDecorations();
		curSelectionFrag2.notifyDataSetChanged();// destAdapter.notifyDataSetChanged();
		curContentFrag.notifyDataSetChanged();// srcAdapter.notifyDataSetChanged();
		curContentFrag.selectionStatusTV
			.setText(curContentFrag.selectedInList1.size() + "/"
					 + curContentFrag.dataSourceL1.size());
		curSelectionFrag2.selectionStatusTV
			.setText(curSelectionFrag2.selectedInList1.size() + "/"
					 + curSelectionFrag2.dataSourceL1.size());
	}

	private void removeFiles(final ContentFragment curContentFrag, final ContentFragment curSelectionFrag2) {
		curContentFrag.dataSourceL2 = curSelectionFrag2.dataSourceL1;
		if (curSelectionFrag2.selectedInList1.size() > 0) {
			curSelectionFrag2.allCbx.setImageResource(R.drawable.dot);
			if (curSelectionFrag2.selectedInList1.size() == curSelectionFrag2.dataSourceL1.size()) {
				curSelectionFrag2.allCbx.setSelected(false);//.setChecked(false);
				curSelectionFrag2.allCbx.setEnabled(false);
			}
			if (multiFiles) {
				curSelectionFrag2.removeAllDS2(curSelectionFrag2.selectedInList1);// dataSourceL2.removeAll(selectedInList2);
			} else {
				curSelectionFrag2.clearDS2();// dataSourceL2.clear();
			}
			curContentFrag.allCbx.setEnabled(true);// allCbx.setEnabled(true);
			curSelectionFrag2.selectedInList1.clear();
			if (curSelectionFrag2.dataSourceL1.size() == 0) {
				curSelectionFrag2.searchButton.setEnabled(false);
				curSelectionFrag2.nofilelayout.setVisibility(View.VISIBLE);
				curSelectionFrag2.mSwipeRefreshLayout.setVisibility(View.GONE);
			} 
			curSelectionFrag2.notifyDataSetChanged();// destAdapter.notifyDataSetChanged();
			curContentFrag.notifyDataSetChanged();// srcAdapter.notifyDataSetChanged();
			curContentFrag.selectionStatusTV
				.setText(curContentFrag.selectedInList1.size() + "/"
						 + curContentFrag.dataSourceL1.size());
			curSelectionFrag2.selectionStatusTV
				.setText(curSelectionFrag2.selectedInList1.size() + "/"
						 + curSelectionFrag2.dataSourceL1.size());
		}
	}

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
			case R.id.remove:
				if (!activity.swap) {
					if (slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT) {
						removeFiles(activity.curContentFrag, this);
					} else {
						addFiles(activity.curExplorerFrag, this);
					}
				} else {
					if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
						removeFiles(activity.curExplorerFrag, this);
					} else {
						addFiles(activity.curContentFrag, this);
					}
				}
				showStatus();
				break;
			case R.id.removeAll:
				if (!activity.swap) {
					if (slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT) {
						removeAllFiles(activity.curContentFrag, this);
					} else {
						addAllFiles(activity.curExplorerFrag, this);
					}
				} else {
					if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
						removeAllFiles(activity.curExplorerFrag, this);
					} else {
						addAllFiles(activity.curContentFrag, this);
					}
				}
				showStatus();
				break;
			case R.id.add:
				if (!activity.swap) {
					if (slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT) {
						addFiles(activity.curContentFrag, this);
					} else {
						removeFiles(activity.curExplorerFrag, this);
					}
				} else {
					if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
						addFiles(activity.curExplorerFrag, this);
					} else {
						removeFiles(activity.curContentFrag, this);
					}
				}
				showStatus();
				break;
			case R.id.addAll:
				if (!activity.swap) {
					if (slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT) {
						addAllFiles(activity.curContentFrag, this);
					} else {
						removeAllFiles(activity.curExplorerFrag, this);
					}
				} else {
					if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
						addAllFiles(activity.curExplorerFrag, this);
					} else {
						removeAllFiles(activity.curContentFrag, this);
					}
				}
				showStatus();
				break;
			case R.id.allCbx:
				if (multiFiles) {
					selectedInList1.clear();
					if (!allCbx.isSelected()) {//}.isChecked()) {
						allCbx.setSelected(true);
						//String st;// = dir.endsWith("/") ? dir : dir + "/";
						for (LayoutElement f : dataSourceL1) {
							//st = f.getAbsolutePath();
							if (f.bf.f.canRead() && (dataSourceL2 == null || !dataSourceL2.contains(f))) {
								selectedInList1.add(f);
							}
						}
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
				}
				break;
			case R.id.allName:
				if (allName.getText().toString().equals("Name ▲")) {
					allName.setText("Name ▼");
					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.NAME, FileListSorter.DESCENDING);
					AndroidUtils.setSharedPreference(activity, (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) ? ("ContentFragSortType" + activity.slideFrag.indexOfMTabs(ContentFragment.this)) : ("ExplorerFragSortType" + activity.slideFrag2.indexOfMTabs(ContentFragment.this)), "Name ▼");
				} else {
					allName.setText("Name ▲");
					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.NAME, FileListSorter.ASCENDING);
					AndroidUtils.setSharedPreference(activity, (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) ? ("ContentFragSortType" + activity.slideFrag.indexOfMTabs(ContentFragment.this)) : ("ExplorerFragSortType" + activity.slideFrag2.indexOfMTabs(ContentFragment.this)), "Name ▲");
				}
				//Log.d(TAG, "activity.slideFrag.indexOf " + activity.slideFrag.indexOf(ContentFragment.this));
				allDate.setText("Date");
				allSize.setText("Size");
				allType.setText("Type");
				Collections.sort(dataSourceL1, fileListSorter);
				srcAdapter.notifyDataSetChanged();
				break;
			case R.id.allType:
				if (allType.getText().toString().equals("Type ▲")) {
					allType.setText("Type ▼");
					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.TYPE, FileListSorter.DESCENDING);
					AndroidUtils.setSharedPreference(activity, (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) ? ("ContentFragSortType" + activity.slideFrag.indexOfMTabs(ContentFragment.this)) : ("ExplorerFragSortType" + activity.slideFrag2.indexOfMTabs(ContentFragment.this)), "Type ▼");
				} else {
					allType.setText("Type ▲");
					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.TYPE, FileListSorter.ASCENDING);
					AndroidUtils.setSharedPreference(activity, (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) ? ("ContentFragSortType" + activity.slideFrag.indexOfMTabs(ContentFragment.this)) : ("ExplorerFragSortType" + activity.slideFrag2.indexOfMTabs(ContentFragment.this)), "Type ▲");
				}
				allName.setText("Name");
				allDate.setText("Date");
				allSize.setText("Size");
				Collections.sort(dataSourceL1, fileListSorter);
				srcAdapter.notifyDataSetChanged();
				break;
			case R.id.allDate:
				if (allDate.getText().toString().equals("Date ▲")) {
					allDate.setText("Date ▼");
					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.DATE, FileListSorter.DESCENDING);
					AndroidUtils.setSharedPreference(activity, (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) ? ("ContentFragSortType" + activity.slideFrag.indexOfMTabs(ContentFragment.this)) : ("ExplorerFragSortType" + activity.slideFrag2.indexOfMTabs(ContentFragment.this)), "Date ▼");
				} else {
					allDate.setText("Date ▲");
					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.DATE, FileListSorter.ASCENDING);
					AndroidUtils.setSharedPreference(activity, (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) ? ("ContentFragSortType" + activity.slideFrag.indexOfMTabs(ContentFragment.this)) : ("ExplorerFragSortType" + activity.slideFrag2.indexOfMTabs(ContentFragment.this)), "Date ▲");
				}
				allName.setText("Name");
				allSize.setText("Size");
				allType.setText("Type");
				Collections.sort(dataSourceL1, fileListSorter);
				srcAdapter.notifyDataSetChanged();
				break;
			case R.id.allSize:
				if (allSize.getText().toString().equals("Size ▲")) {
					allSize.setText("Size ▼");
					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.SIZE, FileListSorter.DESCENDING);
					AndroidUtils.setSharedPreference(activity, (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) ? ("ContentFragSortType" + activity.slideFrag.indexOfMTabs(ContentFragment.this)) : ("ExplorerFragSortType" + activity.slideFrag2.indexOfMTabs(ContentFragment.this)), "Size ▼");
				} else {
					allSize.setText("Size ▲");
					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.SIZE, FileListSorter.ASCENDING);
					AndroidUtils.setSharedPreference(activity, (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) ? ("ContentFragSortType" + activity.slideFrag.indexOfMTabs(ContentFragment.this)) : ("ExplorerFragSortType" + activity.slideFrag2.indexOfMTabs(ContentFragment.this)), "Size ▲");
				}
				allName.setText("Name");
				allDate.setText("Date");
				allType.setText("Type");
				Collections.sort(dataSourceL1, fileListSorter);
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
				MenuInflater inflater = new MenuInflater(activity);
				inflater.inflate(R.menu.storage, menuBuilder);
				MenuPopupHelper optionsMenu = new MenuPopupHelper(activity , menuBuilder, dirMore);
				optionsMenu.setForceShowIcon(true);
				MenuItem mi = menuBuilder.findItem(R.id.otg);
				if (true) {
					mi.setEnabled(true);
				} else {
					mi.setEnabled(false);
				}
				
				mi = menuBuilder.findItem(R.id.microsd);
				if (new File("/storage/MicroSD").exists()) {
					mi.setEnabled(true);
				} else {
					mi.setEnabled(false);
				}

				final int size = menuBuilder.size();
				for (int i = 0; i < size;i++) {
					final Drawable icon = menuBuilder.getItem(i).getIcon();
					icon.setFilterBitmap(true);
					icon.setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
				}
				
				menuBuilder.setCallback(new MenuBuilder.Callback() {
						@Override
						public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
							Log.d(TAG, item.getTitle() + ".");
							switch (item.getItemId())  {
								case R.id.sdcard:
									changeDir("/sdcard", true);
									break;
								case R.id.microsd:
									changeDir("/storage/MicroSD", true);
									break;
								case R.id.newFolder:
									activity.mainActivityHelper.add(MainActivityHelper.NEW_FOLDER);
									break;
								case R.id.newFile:
									activity.mainActivityHelper.add(MainActivityHelper.NEW_FILE);
									break;
								}
							return true;
						}
						@Override
						public void onMenuModeChange(MenuBuilder menu) {}
					});
				optionsMenu.show();
				break;
			case R.id.copys:
				activity.MOVE_PATH = null;
				ArrayList<BaseFile> copies = new ArrayList<>();
				for (LayoutElement le : selectedInList1) {//int i2 = 0; i2 < plist.size(); i2++
					copies.add(le.generateBaseFile());//dataSourceL1.get(plist.get(i2))
				}
				activity.COPY_PATH = copies;
				activity.callback = null;
				if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT && activity.curExplorerFrag.commands.getVisibility() == View.GONE) {//type == -1
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
				ArrayList<BaseFile> copie = new ArrayList<>();
				for (LayoutElement le : selectedInList1) {//int i3 = 0; i3 < plist.size(); i3++
					copie.add(le.generateBaseFile());//dataSourceL1.get(plist.get(i3))
				}
				activity.MOVE_PATH = copie;
				activity.callback = new Runnable() {
					@Override
					public void run() {
						dataSourceL1.removeAll(selectedInList1);
						selectedInList1.clear();
						srcAdapter.notifyDataSetChanged();
					}
				};
				if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT && activity.curExplorerFrag.commands.getVisibility() == View.GONE) {
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
				if (selectedInList1.size() > 0) {
					activity.callback = new Runnable() {
						@Override
						public void run() {
							dataSourceL1.removeAll(selectedInList1);
							selectedInList1.clear();
							srcAdapter.notifyDataSetChanged();
						}
					};
					GeneralDialogCreation.deleteFilesDialog(activity, //getLayoutElements(),
															activity, selectedInList1, activity.getAppTheme(), activity.callback);
					activity.callback = null;
				} else {
					if (activity.MOVE_PATH != null || activity.COPY_PATH != null) {
						String path = currentPathTitle;
						ArrayList<BaseFile> arrayList = activity.COPY_PATH != null ? activity.COPY_PATH: activity.MOVE_PATH;
						boolean move = activity.MOVE_PATH != null;
						new CopyFileCheck(this, path, move, activity, ThemedActivity.rootMode, activity.callback)
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, arrayList);
						activity.MOVE_PATH = null;
						activity.callback = null;
					} else if (activity.EXTRACT_PATH != null || activity.EXTRACT_MOVE_PATH != null) {
						final List<String> stList = activity.EXTRACT_PATH != null ? activity.EXTRACT_PATH: activity.EXTRACT_MOVE_PATH;
						final Runnable r = new Runnable() {
							@Override
							public void run() {
								final ArrayList<BaseFile> arrayList = new ArrayList<>(stList.size());
								for (String s : stList) {
									arrayList.add(new BaseFile(ExplorerApplication.PRIVATE_PATH + (s.startsWith("/") ? "" : "/") + s));
									Log.d(TAG, "EXTRACT_PATH " + new File(ExplorerApplication.PRIVATE_PATH + (s.startsWith("/") ? "" : "/") + s).length());
								}
								final boolean move = activity.EXTRACT_MOVE_PATH != null;
								new CopyFileCheck(ContentFragment.this, currentPathTitle, move, activity, ThemedActivity.rootMode, null)
									.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, arrayList);
								activity.EXTRACT_MOVE_PATH = null;
							}
						};
						final StringBuilder sb = new StringBuilder();
						for (String le : stList) {
							sb.append(le).append("\n");
						}
						new DecompressTask(fragActivity,
										   activity.zip.file.getAbsolutePath(),
										   ExplorerApplication.PRIVATE_PATH,
										   sb.toString(),
										   "",
										   "",
										   "",
										   0,
										   "x",
										   r).execute();
					}
				}
				break;
//			case R.id.extract:
//                activity.mainActivityHelper.extractFile(selectedInList1);
//                break;
            case R.id.renames:
				final BaseFile f = ((LayoutElement)selectedInList1.get(0)).generateBaseFile();
				rename(f);
				break;
			case R.id.compresss:
				StringBuilder sb = new StringBuilder();
				for (LayoutElement le : selectedInList1) {
					sb.append(le.path).append("| ");
				}
				activity.compress(sb.toString(), activity.dir + "/" + new File(activity.dir).getName());
				break;
			case R.id.shares:
				if (selectedInList1.size() > 0) {
					if (selectedInList1.size() > 100)
						Toast.makeText(activity, getResources().getString(R.string.share_limit),
									   Toast.LENGTH_SHORT).show();
					else {
						switch (dataSourceL1.get(0).getMode()) {
							case DROPBOX:
							case BOX:
							case GDRIVE:
							case ONEDRIVE:
								activity.getFutils().shareCloudFile(((LayoutElement)selectedInList1.get(0)).path,
																	dataSourceL1.get(0).getMode(), getContext());
								break;
							default:
								ArrayList<File> arrayList = new ArrayList<>(selectedInList1.size());
								for (LayoutElement i : selectedInList1) {
									arrayList.add(new File(i.path));
								}
								Futils.shareFiles(arrayList, activity, activity.getAppTheme(), accentColor);
								break;
						}
					}
				}
				break;
			case R.id.moreLeft:
			case R.id.moreRight:
				menuBuilder = new MenuBuilder(fragActivity);
				inflater = new MenuInflater(fragActivity);
				inflater.inflate(R.menu.more_commands, menuBuilder);
				optionsMenu = new MenuPopupHelper(fragActivity , menuBuilder, v);
				optionsMenu.setForceShowIcon(true);

				final int num= menuBuilder.size();
				Drawable icon;
				for (int i = 0; i < num; i++) {
					icon = menuBuilder.getItem(i).getIcon();
					if (icon != null) {
						icon.setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
					}
				}
				
				menuBuilder.setCallback(new MenuBuilder.Callback() {

						@Override
						public void onMenuModeChange(MenuBuilder p1) {
						}

						@Override
						public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
							Log.d(TAG, item.getTitle() + ".");
							switch (item.getItemId()) {
								case (R.id.shortcuts):
									for (LayoutElement le : selectedInList1) {
										AndroidUtils.addShortcut(activity, le.bf.f);
									}
									break;
								case R.id.book:
									DataUtils dataUtils = DataUtils.getInstance();
									for (LayoutElement le : selectedInList1) {
										dataUtils.addBook(new String[]{le.name, le.path}, true);
									}
									activity.refreshDrawer();
									Toast.makeText(activity, activity.getResources().getString(R.string.bookmarksadded), Toast.LENGTH_LONG).show();
									break;
//								case R.id.encrypts:
//									if (selectedInList1.size() > 0) {
//										final LayoutElement[] les = new LayoutElement[selectedInList1.size()];
//										selectedInList1.toArray(les);
//										Log.d(TAG, "encrypts " + les);
//									ArrAdapter.encrypt(activity, ContentFragment.this, les);
//									}
//									break;
								case (R.id.hiddenfiles):
									for (LayoutElement le : selectedInList1) {
										activity.dataUtils.addHiddenFile(le.path);
										if (new File(le.path).isDirectory()) {
											File f1 = new File(le.path + "/.nomedia");
											if (!f1.exists()) {
												try {
													com.amaze.filemanager.filesystem.FileUtil.mkfile(f1, activity);
													//activity.mainActivityHelper.mkFile(new HFile(OpenMode.FILE, le.path), Frag.this);
												} catch (Exception e) {
													e.printStackTrace();
												}
											}
											Futils.scanFile(le.path, activity);
										}
									}
									updateList();
									break;
							}
							return true;
						}
					});
				optionsMenu.show();
				break;
			case (R.id.shortcuts):
				for (LayoutElement le : selectedInList1) {
					AndroidUtils.addShortcut(activity, le.bf.f);
				}
				break;
			case R.id.book:
				DataUtils dataUtils = DataUtils.getInstance();
				for (LayoutElement le : selectedInList1) {
					dataUtils.addBook(new String[]{le.name, le.path}, true);
				}
				activity.refreshDrawer();
				Toast.makeText(activity, activity.getResources().getString(R.string.bookmarksadded), Toast.LENGTH_LONG).show();
				break;
//			case R.id.encrypts:
//				Log.d(TAG, "encrypts " + selectedInList1);
//				if (selectedInList1.size() > 0) {
//					final LayoutElement[] les = new LayoutElement[selectedInList1.size()];
//					selectedInList1.toArray(les);
//					ArrAdapter.encrypt(activity, ContentFragment.this, les);
//				}
//				break;
			case (R.id.hiddenfiles):
				for (LayoutElement le : selectedInList1) {
					activity.dataUtils.addHiddenFile(le.path);
					if (new File(le.path).isDirectory()) {
						File f1 = new File(le.path + "/.nomedia");
						if (!f1.exists()) {
							try {
								com.amaze.filemanager.filesystem.FileUtil.mkfile(f1, activity);
								//activity.mainActivityHelper.mkFile(new HFile(OpenMode.FILE, le.path), Frag.this);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						Futils.scanFile(le.path, activity);
					}
				}
				updateList();
				break;
			case R.id.infos:
				LayoutElement le = (LayoutElement) selectedInList1.get(0);
				GeneralDialogCreation.showPropertiesDialogWithPermissions(le.generateBaseFile(),
																		  le.permissions, activity, ThemedActivity.rootMode,
																		  activity.getAppTheme());
				break;
		}
	}

    /**
     * Show dialog to rename a file
     *
     * @param f the file to rename
     */
    public void rename(final BaseFile f) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        String name = f.getName();
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
					if (f.isSmb()) {
						if (f.isDirectory() && !name.endsWith("/"))
							name = name + "/";
					}
					if (new File(activity.dir, name).exists()) {
						dialog.show();
						Toast.makeText(activity, getString(R.string.fileexist),
									   Toast.LENGTH_SHORT).show();
					} else 
					activity.mainActivityHelper.rename(openMode, f.getPath(),
														   activity.dir + "/" + name, activity, ThemedActivity.rootMode);
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
					if (!f.isDirectory()) {
						materialDialog.getInputEditText().setSelection(f.getNameString(getContext()).length());
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
		//final Context context = getContext();
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
		
		gridLayoutManager = new GridLayoutManager(fragActivity, spanCount);
		
		srcAdapter = new ArrAdapter(this, dataSourceL1);
		listView.setAdapter(srcAdapter);

		listView.setLayoutManager(gridLayoutManager);
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

	public void changeDir(final String curDir, final boolean doScroll) {
		Log.d(TAG, "changeDir " + curDir + ", doScroll " + doScroll + ", " + type + ", " + slidingTabsFragment.side);
		if (fake) {
			return;
		}
		loadList.cancel(true);
		searchTask.cancel(true);
		loadList = new LoadFiles();
		loadList.execute(curDir, doScroll);
	}

	private void manageSearchUI(boolean search) {
		if (search == true) {
			searchButton.setImageResource(R.drawable.ic_arrow_back_white_36dp);
			topflipper.setDisplayedChild(topflipper.indexOfChild(quickLayout));
			if (type == Frag.TYPE.SELECTION) {
				searchET.setHint("Search");
			} else {
				searchET.setHint("Search " + new File(currentPathTitle).getName());
			}
			searchET.requestFocus();
			//imm.showSoftInput(quicksearch, InputMethodManager.SHOW_FORCED);
			imm.showSoftInput(searchET, InputMethodManager.SHOW_IMPLICIT);
			//imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
			//activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		} else {
			imm.hideSoftInputFromWindow(searchET.getWindowToken(), 0);
			searchET.setText("");
			searchButton.setImageResource(R.drawable.ic_action_search);
			if (type == Frag.TYPE.SELECTION) {
				topflipper.setDisplayedChild(topflipper.indexOfChild(selectionCommandsLayout));
				dataSourceL1.clear();
				dataSourceL1.addAll(tempOriDataSourceL1);
			} else {
				topflipper.setDisplayedChild(topflipper.indexOfChild(scrolltext));
				currentPathTitle = currentPathTitle;
				updateList();
			}
		}
	}

	public void searchButton() {
		searchMode = !searchMode;
		manageSearchUI(searchMode);
	}
	
//    private void updateProgress(int progress, int maxProgress) {
//        // Only update the progress bar every n steps...
//        if ((progress % 50) == 0) {
//            // Also don't update for the first second.
//            
//            // Okay, send an update.
//            Message msg = handler.obtainMessage(MESSAGE_SET_PROGRESS);
//            msg.arg1 = progress;
//            msg.arg2 = maxProgress;
//            msg.sendToTarget();
//        }
//    }


    protected void updateNoAccessMessage(boolean showMessage) {
        mMessageView.setVisibility(showMessage ? View.VISIBLE : View.GONE);
    }
	

//	private class FileListMessageHandler extends Handler {
//        @Override
//        public void handleMessage(Message msg) {
//
//            switch (msg.what) {
//                case DirectoryScanner.MESSAGE_SHOW_DIRECTORY_CONTENTS:
//                    if (getActivity() == null) {
//                        return;
//                    }
//
//                    DirectoryContents c = (DirectoryContents) msg.obj;
//                    mFiles.clear();
//                    mFiles.addAll(c.listSdCard);
//                    mFiles.addAll(c.listDir);
//                    mFiles.addAll(c.listFile);
//
//                    mAdapter.notifyDataSetChanged();
//                    updateNoAccessMessage(c.noAccess);
//
//
//                    if (mPreviousDirectory != null) {
//                        selectInList(mPreviousDirectory);
//                    } else {
//                        // Reset list position.
//                        if (!mFiles.isEmpty() && getView() != null) {
//                            getListView().setSelection(0);
//                        }
//                    }
//                    setLoading(false);
//                    updateClipboardInfo();
//                    if (resourceCallback != null) {
//                        resourceCallback.onTransitionToIdle();
//                    }
//                    break;
//                case DirectoryScanner.MESSAGE_SET_PROGRESS:
//                    // ignore
//                    break;
//            }
//        }
//    }
	
	
	private class LoadFiles extends AsyncTask<Object, String, List<LayoutElement>> {

		private Boolean doScroll;
		
		@Override
		protected void onPreExecute() {
			mSwipeRefreshLayout.setRefreshing(true);
		}

		@Override
		protected List<LayoutElement> doInBackground(Object... params) {
			String path = (String) params[0];
			doScroll = (Boolean) params[1];
			
			noMedia = false;
			List<LayoutElement> dataSourceL1a = new LinkedList<>();
			
			if (currentPathTitle == null) {
				return dataSourceL1a;
			}
			Log.d(TAG, "LoadFiles.doInBackground " + path + ", " + openMode + ", " + ContentFragment.this);
			folder_count = 0;
			file_count = 0;

			if (openMode == OpenMode.UNKNOWN) {
				HFile hFile = new HFile(OpenMode.UNKNOWN, path);
				hFile.generateMode(activity);

				if (hFile.isLocal()) {
					openMode = OpenMode.FILE;
				} else if (hFile.isSmb()) {
					openMode = OpenMode.SMB;
					smbPath = path;
				} else if (hFile.isOtgFile()) {
					openMode = OpenMode.OTG;
				} else if (hFile.isBoxFile()) {
					openMode = OpenMode.BOX;
				} else if (hFile.isDropBoxFile()) {
					openMode = OpenMode.DROPBOX;
				} else if (hFile.isGoogleDriveFile()) {
					openMode = OpenMode.GDRIVE;
				} else if (hFile.isOneDriveFile()) {
					openMode = OpenMode.ONEDRIVE;
				} else if (hFile.isCustomPath())
					openMode = OpenMode.CUSTOM;
				else if (android.util.Patterns.EMAIL_ADDRESS.matcher(path).matches()) {
					openMode = OpenMode.ROOT;
				}
			}

			switch (openMode) {
				case SMB:
					HFile hFile = new HFile(OpenMode.SMB, path);
					try {
						SmbFile[] smbFile = hFile.getSmbFile(5000).listFiles();
						dataSourceL1a = addToSmb(smbFile, path);
						openMode = OpenMode.SMB;
					} catch (SmbAuthException e) {
						if (!e.getMessage().toLowerCase().contains("denied"))
							reauthenticateSmb();
						publishProgress(e.getLocalizedMessage());
					} catch (SmbException | NullPointerException e) {
						publishProgress(e.getLocalizedMessage());
						e.printStackTrace();
					}
					break;
				case CUSTOM:
					ArrayList<BaseFile> arrayList = null;
					switch (Integer.parseInt(path)) {
						case 0:
							arrayList = listImages();
							break;
						case 1:
							arrayList = listVideos();
							break;
						case 2:
							arrayList = listaudio();
							break;
						case 3:
							arrayList = listDocs();
							break;
						case 4:
							arrayList = listApks();
							break;
						case 5:
							arrayList = listRecent();
							break;
						case 6:
							arrayList = listRecentFiles();
							break;
					}
					
					if (arrayList != null) {
						dataSourceL1a = addTo(arrayList);
					} else 
						return new ArrayList<LayoutElement>(0);
					break;
				case OTG:
					dataSourceL1a = addTo(listOtg(path));
					openMode = OpenMode.OTG;
					break;
				case DROPBOX:
					CloudStorage cloudStorageDropbox = dataUtils.getAccount(OpenMode.DROPBOX);
					try {
						dataSourceL1a = addTo(listCloud(path, cloudStorageDropbox, OpenMode.DROPBOX));
					} catch (CloudPluginException e) {
						e.printStackTrace();
						return new ArrayList<LayoutElement>(0);
					}
					break;
				case BOX:
					CloudStorage cloudStorageBox = dataUtils.getAccount(OpenMode.BOX);
					try {
						dataSourceL1a = addTo(listCloud(path, cloudStorageBox, OpenMode.BOX));
					} catch (CloudPluginException e) {
						e.printStackTrace();
						return new ArrayList<LayoutElement>(0);
					}
					break;
				case GDRIVE:
					CloudStorage cloudStorageGDrive = dataUtils.getAccount(OpenMode.GDRIVE);
					try {
						dataSourceL1a = addTo(listCloud(path, cloudStorageGDrive, OpenMode.GDRIVE));
					} catch (CloudPluginException e) {
						e.printStackTrace();
						return new ArrayList<LayoutElement>(0);
					}
					break;
				case ONEDRIVE:
					CloudStorage cloudStorageOneDrive = dataUtils.getAccount(OpenMode.ONEDRIVE);
					try {
						dataSourceL1a = addTo(listCloud(path, cloudStorageOneDrive, OpenMode.ONEDRIVE));
					} catch (CloudPluginException e) {
						e.printStackTrace();
						return new ArrayList<LayoutElement>(0);
					}
					break;
				default:
					// we're neither in OTG not in SMB, load the list based on root/general filesystem
					//dataSourceL1a = new LinkedList<LayoutElement>();
					try {
						File curDir = new File(path);
						while (curDir != null && !curDir.exists()) {
							publishProgress(curDir.getAbsolutePath() + " is not existed");
							curDir = curDir.getParentFile();
						}
						if (curDir == null) {
							publishProgress("Current directory is not existed. Change to root");
							curDir = new File("/");
						}

						final String curPath = curDir.getAbsolutePath();
						if (!currentPathTitle.equals(curPath)) {
							if (backStack.size() > ExplorerActivity.NUM_BACK) {
								backStack.remove(0);
							}
							final Map<String, Object> bun = onSaveInstanceState();
							backStack.push(bun);

							history.remove(curPath);
							if (history.size() > ExplorerActivity.NUM_BACK) {
								history.remove(0);
							}
							history.push(curPath);

							activity.historyList.remove(curPath);
							if (activity.historyList.size() > ExplorerActivity.NUM_BACK) {
								activity.historyList.remove(0);
							}
							activity.historyList.push(curPath);
							tempPreviewL2 = null;
						}
						currentPathTitle = curPath;
						currentPathTitle = currentPathTitle;
						//Log.d(TAG, Util.collectionToString(history, true, "\n"));
						
						if (mFileObserver != null) {
							mFileObserver.stopWatching();
						}
						mFileObserver = createFileObserver(currentPathTitle);
						mFileObserver.startWatching();
						if (tempPreviewL2 != null && !tempPreviewL2.bf.f.exists()) {
							tempPreviewL2 = null;
						}
						
						ArrayList<BaseFile> files = RootHelper.getFilesList(currentPathTitle, ThemedActivity.rootMode, SHOW_HIDDEN,
                            new RootHelper.GetModeCallBack() {
                                @Override
                                public void getMode(OpenMode mode) {
                                    openMode = mode;
                                }
                            });
						
						String fName;
						boolean isDirectory;
						final ArrayList<String> hiddenfiles = dataUtils.getHiddenfiles();
						for (BaseFile f : files) {
							fName = f.getName();
							isDirectory = f.isDirectory();

							// It's the noMedia file. Raise the flag.
							if (!noMedia && fName.equalsIgnoreCase(".nomedia")) {
								noMedia = true;
							}

							//If the user doesn't want to display hidden files and the file is hidden, ignore this file.
							if (!displayHidden && f.f.isHidden()) {
								continue;
							}
							if (!hiddenfiles.contains(f.getPath())) {

								//Log.d(TAG, "f.f=" + f.f + ", mimes=" + mimes + ", suffix=" + suffix + ", getMimeType=" + MimeTypes.getMimeType(f.f) + ", " + ((mimes + "").indexOf(MimeTypes.getMimeType(f.f) + "") >= 0));
								if (isDirectory) {
									folder_count++;
									if (!mWriteableOnly || f.f.canWrite()) {
										dataSourceL1a.add(new LayoutElement(f));
									}
								} else if (suffix.length() > 0) {//!mDirectoriesOnly
									if (".*".equals(suffix) ||
										"*".equals(suffix) ||
										mimes.indexOf("*/*") > 0 || 
										mimes.indexOf(MimeTypes.getMimeType(f.f) + "") >= 0) {
										dataSourceL1a.add(new LayoutElement(f));
										file_count++;
									} else {//if (suffix != null) 
										//lastIndexOfDot = fName.lastIndexOf(".");
										//ext = lastIndexOfDot >= 0 ? fName.substring(++lastIndexOfDot) : "";
										//Log.d(TAG, "ext=" + ext + ", " + (Arrays.binarySearch(suffixes, ext.toLowerCase()) >= 0));
										if (suffixPattern.matcher(fName).matches()) {//}suffix.matches(".*?\\b" + ext + "\\b.*?")) {
											dataSourceL1a.add(new LayoutElement(f));
											file_count++;
										}
									}
								}
							}
						}
						
					} catch (RootNotPermittedException e) {
						showToast(activity.getString(R.string.rootfailure));
						e.printStackTrace();
						return dataSourceL1a;
					}
					break;
			}
			if (dataSourceL1a != null) //} && !(openMode == OpenMode.CUSTOM && ((currentPathTitle).equals("5") || (currentPathTitle).equals("6"))))
				Collections.sort(dataSourceL1a, fileListSorter);
			
//			if (openMode != OpenMode.CUSTOM)
//				DataUtils.addHistoryFile(currentPathTitle);
			return dataSourceL1a;
		}

		@Override
		protected void onPostExecute(List<LayoutElement> dataSourceL1a) {
			//Log.d(TAG, "LoadFiles.onPostExecute.dataSourceL1a=" + Util.collectionToString(dataSourceL1a, false, "\n"));
			if (currentPathTitle != null) {
				if (currentPathTitle.startsWith("/")) {
					final File curDir = new File(currentPathTitle);
					rightStatus.setText(
						"Free " + Formatter.formatFileSize(activity, curDir.getFreeSpace())
						+ ". Used " + Formatter.formatFileSize(activity, curDir.getTotalSpace() - curDir.getFreeSpace())
						+ ". Total " + Formatter.formatFileSize(activity, curDir.getTotalSpace()));
				}
				dataSourceL1.clear();
				dataSourceL1.addAll(dataSourceL1a);
				selectedInList1.clear();
			}
			showStatus();

			if (multiFiles) {
				boolean allInclude = (dataSourceL2 != null && dataSourceL1a.size() > 0) ? true : false;
				if (allInclude) {
					for (LayoutElement st : dataSourceL1a) {
						if (!dataSourceL2.contains(st)) {
							allInclude = false;
							break;
						}
					}
				}

				if (allInclude) {
					allCbx.setSelected(true);
					allCbx.setImageResource(R.drawable.ic_accept);
					allCbx.setEnabled(false);
				} else {
					allCbx.setSelected(false);
					allCbx.setImageResource(R.drawable.dot);
					allCbx.setEnabled(true);
				}
			}

			if (activity.COPY_PATH == null && activity.MOVE_PATH == null && commands.getVisibility() == View.VISIBLE) {//commands != null && 
				horizontalDivider6.setVisibility(View.GONE);
				commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
				commands.setVisibility(View.GONE);
			} else if (activity.COPY_PATH != null || activity.MOVE_PATH != null) {//commands != null && 
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

			if (allCbx.isSelected()) {//}.isChecked()) {
				selectionStatusTV.setText(dataSourceL1.size() 
										 + "/" + dataSourceL1.size());
			} else {
				selectionStatusTV.setText(selectedInList1.size() 
										 + "/" + dataSourceL1.size());
			}
			Log.d(TAG, "LoadFiles.onPostExecute " + currentPathTitle);

			updateDir(currentPathTitle);
			mSwipeRefreshLayout.setRefreshing(false);
			
			if (dataSourceL1.size() == 0) {
				nofilelayout.setVisibility(View.VISIBLE);
				mSwipeRefreshLayout.setVisibility(View.GONE);
			} else {
				nofilelayout.setVisibility(View.GONE);
				mSwipeRefreshLayout.setVisibility(View.VISIBLE);
			}

		}

		@Override
		public void onProgressUpdate(String... message) {
			Log.d(TAG, "onProgressUpdate " + message[0]);
			showToast(message[0]);
		}

		private ArrayList<LayoutElement> addTo(ArrayList<BaseFile> baseFiles) {
			ArrayList<LayoutElement> items = new ArrayList<>();

			for (int i = 0; i < baseFiles.size(); i++) {
				BaseFile baseFile = baseFiles.get(i);
				//File f = new File(ele.getPath());

				if (!dataUtils.getHiddenfiles().contains(baseFile.getPath())) {
					final LayoutElement layoutElement = new LayoutElement(baseFile);
					layoutElement.setMode(baseFile.getMode());
					items.add(layoutElement);
					if (baseFile.isDirectory()) {
						folder_count++;
					} else {
						file_count++;
					}
				}
			}
			return items;
		}

		private ArrayList<BaseFile> listaudio() {
			String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
			String[] projection = {
                MediaStore.Audio.Media.DATA
			};

			Cursor cursor = activity.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);

			ArrayList<BaseFile> songs = new ArrayList<>();
			if (cursor.getCount() > 0 && cursor.moveToFirst()) {
				do {
					String path = cursor.getString(cursor.getColumnIndex
												   (MediaStore.Files.FileColumns.DATA));
					BaseFile strings = RootHelper.generateBaseFile(new File(path), SHOW_HIDDEN);
					if (strings != null) songs.add(strings);
				} while (cursor.moveToNext());
			}
			cursor.close();
			return songs;
		}

		private ArrayList<BaseFile> listImages() {
			ArrayList<BaseFile> songs = new ArrayList<>();
			final String[] projection = {MediaStore.Images.Media.DATA};
			final Cursor cursor = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
																	  projection, null, null, null);
			if (cursor.getCount() > 0 && cursor.moveToFirst()) {
				do {
					String path = cursor.getString(cursor.getColumnIndex
												   (MediaStore.Files.FileColumns.DATA));
					BaseFile strings = RootHelper.generateBaseFile(new File(path), SHOW_HIDDEN);
					if (strings != null) songs.add(strings);
				} while (cursor.moveToNext());
			}
			cursor.close();
			return songs;
		}

		private ArrayList<BaseFile> listVideos() {
			ArrayList<BaseFile> songs = new ArrayList<>();
			final String[] projection = {MediaStore.Images.Media.DATA};
			final Cursor cursor = activity.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
																	  projection, null, null, null);
			if (cursor.getCount() > 0 && cursor.moveToFirst()) {
				do {
					String path = cursor.getString(cursor.getColumnIndex
												   (MediaStore.Files.FileColumns.DATA));
					BaseFile strings = RootHelper.generateBaseFile(new File(path), SHOW_HIDDEN);
					if (strings != null) songs.add(strings);
				} while (cursor.moveToNext());
			}
			cursor.close();
			return songs;
		}

		private ArrayList<BaseFile> listRecentFiles() {
			ArrayList<BaseFile> songs = new ArrayList<>();
			final String[] projection = {MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.DATE_MODIFIED};
			Calendar c = Calendar.getInstance();
			c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) - 2);
			Date d = c.getTime();
			Cursor cursor = activity.getContentResolver().query(MediaStore.Files
															  .getContentUri("external"), projection,
															  null,
															  null, null);
			if (cursor == null) return songs;
			if (cursor.getCount() > 0 && cursor.moveToFirst()) {
				do {
					String path = cursor.getString(cursor.getColumnIndex
												   (MediaStore.Files.FileColumns.DATA));
					File f = new File(path);
					if (d.compareTo(new Date(f.lastModified())) != 1 && !f.isDirectory()) {
						BaseFile strings = RootHelper.generateBaseFile(new File(path), SHOW_HIDDEN);
						if (strings != null) songs.add(strings);
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
			Collections.sort(songs, new Comparator<BaseFile>() {
					@Override
					public int compare(BaseFile lhs, BaseFile rhs) {
						return -1 * Long.valueOf(lhs.getDate()).compareTo(rhs.getDate());

					}
				});
			if (songs.size() > 20)
				for (int i = songs.size() - 1; i > 20; i--) {
					songs.remove(i);
				}
			return songs;
		}

		private ArrayList<BaseFile> listApks() {
			ArrayList<BaseFile> songs = new ArrayList<>();
			final String[] projection = {MediaStore.Files.FileColumns.DATA};

			Cursor cursor = activity.getContentResolver()
                .query(MediaStore.Files.getContentUri("external"), projection, null, null, null);
			if (cursor.getCount() > 0 && cursor.moveToFirst()) {
				do {
					String path = cursor.getString(cursor.getColumnIndex
												   (MediaStore.Files.FileColumns.DATA));
					if (path != null && path.endsWith(".apk")) {
						BaseFile strings = RootHelper.generateBaseFile(new File(path), SHOW_HIDDEN);
						if (strings != null) songs.add(strings);
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
			return songs;
		}

		private ArrayList<BaseFile> listRecent() {
			UtilsHandler utilsHandler = new UtilsHandler(activity);
			final ArrayList<String> paths = utilsHandler.getHistoryList();
			ArrayList<BaseFile> songs = new ArrayList<>();
			for (String f : paths) {
				if (!f.equals("/")) {
					BaseFile a = RootHelper.generateBaseFile(new File(f), SHOW_HIDDEN);
					a.generateMode(activity);
					if (a != null && !a.isSmb() && !(a).isDirectory() && a.exists())
						songs.add(a);
				}
			}
			return songs;
		}

		private ArrayList<BaseFile> listDocs() {
			ArrayList<BaseFile> songs = new ArrayList<>();
			final String[] projection = {MediaStore.Files.FileColumns.DATA};
			Cursor cursor = activity.getContentResolver().query(MediaStore.Files.getContentUri("external"),
																projection, null, null, null);
			String[] types = new String[]{".pdf", ".xml", ".html", ".asm", ".text/x-asm", ".def", ".in", ".rc",
                ".list", ".log", ".pl", ".prop", ".properties", ".rc",
                ".doc", ".docx", ".msg", ".odt", ".pages", ".rtf", ".txt", ".wpd", ".wps"};
			if (cursor.getCount() > 0 && cursor.moveToFirst()) {
				do {
					String path = cursor.getString(cursor.getColumnIndex
												   (MediaStore.Files.FileColumns.DATA));
					if (path != null && contains(types, path)) {
						BaseFile strings = RootHelper.generateBaseFile(new File(path), SHOW_HIDDEN);
						if (strings != null) songs.add(strings);
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
			return songs;
		}

		/**
		 * Lists files from an OTG device
		 *
		 * @param path the path to the directory tree, starts with prefix {@link com.amaze.filemanager.utils.OTGUtil#PREFIX_OTG}
		 *             Independent of URI (or mount point) for the OTG
		 * @return a list of files loaded
		 */
		private ArrayList<BaseFile> listOtg(String path) {

			return OTGUtil.getDocumentFilesList(path, activity);
		}

		private boolean contains(String[] types, String path) {
			for (String string : types) {
				if (path.endsWith(string)) return true;
			}
			return false;
		}

		private ArrayList<BaseFile> listCloud(String path, CloudStorage cloudStorage, OpenMode openMode)
		throws CloudPluginException {
			if (!CloudSheetFragment.isCloudProviderAvailable(activity))
				throw new CloudPluginException();

			return CloudUtil.listFiles(path, cloudStorage, openMode);
		}
	}
	
	

	private class SearchFileNameTask extends AsyncTask<String, Long, ArrayList<LayoutElement>> {
		protected void onPreExecute() {
			if (!mSwipeRefreshLayout.isRefreshing()) {
				mSwipeRefreshLayout.setRefreshing(true);
			}
			searchVal = searchET.getText().toString();
			showToast("Searching...");
			dataSourceL1.clear();
			srcAdapter.notifyDataSetChanged();
		}

		@Override
		protected ArrayList<LayoutElement> doInBackground(String... params) {
			Log.d("SearchFileNameTask", "currentPathTitle " + currentPathTitle);
			final ArrayList<LayoutElement> tempAppList = new ArrayList<>();
			if (type == Frag.TYPE.SELECTION) {
				final Collection<LayoutElement> c = FileUtil.getFilesBy(tempOriDataSourceL1, params[0], true);
				//Log.d(TAG, "getFilesBy " + Util.collectionToString(c, true, "\n"));
				tempAppList.addAll(c);
			} else {
				File file = new File(currentPathTitle);

				if (file.exists()) {
					Collection<File> c = FileUtil.getFilesBy(file.listFiles(), params[0], true);
					//Log.d(TAG, "getFilesBy " + Util.collectionToString(c, true, "\n"));
					for (File le : c) {
						tempAppList.add(new LayoutElement(le));
					}
					//addAllDS1(Util.collectionFile2CollectionString(c));// dataSourceL1.addAll(Util.collectionFile2CollectionString(c));curContentFrag.
					// Log.d("dataSourceL1 new task",
					// Util.collectionToString(dataSourceL1, true, "\n"));
				} else {
					showToast(currentPathTitle + " is not existed");
				}
			}
			Collections.sort(tempAppList, fileListSorter);
			return tempAppList;
		}

		@Override
		protected void onPostExecute(ArrayList<LayoutElement> result) {

			if (mSwipeRefreshLayout.isRefreshing()) {
				mSwipeRefreshLayout.setRefreshing(false);
			}
			dataSourceL1.addAll(result);
			selectedInList1.clear();
			srcAdapter.notifyDataSetChanged();
			selectionStatusTV.setText(selectedInList1.size() 
									 + "/" + dataSourceL1.size());
			File file = new File(currentPathTitle);
			rightStatus.setText(
				"Free " + Util.nf.format(file.getFreeSpace() / (1 << 20))
				+ " MiB. Used " + Util.nf.format((file.getTotalSpace() - file.getFreeSpace()) / (1 << 20))
				+ " MiB. Total " + Util.nf.format(file.getTotalSpace() / (1 << 20)) + " MiB");
			if (dataSourceL1.size() == 0) {
				nofilelayout.setVisibility(View.VISIBLE);
				mSwipeRefreshLayout.setVisibility(View.GONE);
			} else {
				nofilelayout.setVisibility(View.GONE);
				mSwipeRefreshLayout.setVisibility(View.VISIBLE);
			}
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
			menu.findItem(R.id.sameFolder).setVisible(false);
			menu.findItem(R.id.hideToolbar).setVisible(false);
		} else {
			menu.findItem(R.id.sameFolder).setVisible(true);
		}
		MenuItem mi = menu.findItem(R.id.clearSelection);
		if (selectedInList1.size() == 0) {
			mi.setVisible(false);
		} else {
			mi.setVisible(true);
		}
		mi = menu.findItem(R.id.rangeSelection);
		if (selectedInList1.size() > 1) {
			mi.setVisible(true);
		} else {
			mi.setVisible(false);
		}
		mi = menu.findItem(R.id.undoClearSelection);
		if (tempSelectedInList1.size() > 0) {
			mi.setVisible(true);
		} else {
			mi.setVisible(false);
		}
        mi = menu.findItem(R.id.hide);
		if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT && activity.right.getVisibility() == View.VISIBLE
			|| slidingTabsFragment.side == SlidingTabsFragment.Side.RIGHT && activity.left.getVisibility() == View.VISIBLE) {
			mi.setTitle("Hide");
		} else {
			mi.setTitle("2 panels");
		}
        mi = menu.findItem(R.id.biggerequalpanel);
		if (activity.left.getVisibility() == View.GONE || activity.right.getVisibility() == View.GONE) {
			mi.setVisible(false);
		} else {
			mi.setVisible(true);
			if (slidingTabsFragment.width <= 0) {
				mi.setTitle("Wider panel");
			} else {
				mi.setTitle("2 panels equal");
			}
		}
        if (activity.COPY_PATH == null && activity.MOVE_PATH == null && activity.EXTRACT_PATH == null && activity.EXTRACT_MOVE_PATH == null) {
			menu.findItem(R.id.hideToolbar).setVisible(false);
		}
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					Log.d(TAG, item.getTitle() + ".");
					switch (item.getItemId())  {
						case R.id.history:
							//if (ma != null)
							GeneralDialogCreation.showHistoryDialog(activity.dataUtils, activity.getFutils(), ContentFragment.this, activity.getAppTheme());
							break;
						case (R.id.hiddenfiles):
							if (dataUtils.getHiddenfiles().size() == 0) {
								showToast("There is no hidden file/folder");
							} else {
								GeneralDialogCreation.showHiddenDialog(activity.dataUtils, activity.getFutils(), ContentFragment.this, activity.getAppTheme());
							}
							break;
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
							undoClearSelection();
							break;
						case R.id.swap:
							swap(v);
							break;
						case R.id.hide: 
							hide();
							break;
						case R.id.biggerequalpanel:
							biggerequalpanel();
							break;
						case R.id.sameFolder:
							if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
								changeDir(activity.curExplorerFrag.currentPathTitle, true);
							} else {
								changeDir(activity.curContentFrag.currentPathTitle, true);
							}
							break;
						case R.id.hideToolbar:
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
							break;
					}
					return true;
				}
			});
		popup.show();
	}

	void updateStatus() {
		selectionStatusTV.setText(selectedInList1.size()  + "/" + dataSourceL1.size());
	}

	void rangeSelection() {
		int min = Integer.MAX_VALUE, max = -1;
		int cur = -3;
		for (LayoutElement s : selectedInList1) {
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
		srcAdapter.notifyDataSetChanged();
		updateStatus();
	}

	void inversion() {
		tempSelectedInList1.clear();
		for (LayoutElement f : dataSourceL1) {
			if (!selectedInList1.contains(f)) {
				tempSelectedInList1.add(f);
			}
		}
		selectedInList1.clear();
		selectedInList1.addAll(tempSelectedInList1);
		srcAdapter.notifyDataSetChanged();
		updateStatus();
	}

	void clearSelection() {
		tempSelectedInList1.clear();
		tempSelectedInList1.addAll(selectedInList1);
		selectedInList1.clear();
		srcAdapter.notifyDataSetChanged();
		updateStatus();
	}

	void undoClearSelection() {
		selectedInList1.clear();
		selectedInList1.addAll(tempSelectedInList1);
		tempSelectedInList1.clear();
		srcAdapter.notifyDataSetChanged();
		updateStatus();
	}
}
