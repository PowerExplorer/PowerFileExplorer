package net.gnu.explorer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.*;
import android.widget.*;
import android.os.*;
import android.graphics.drawable.*;
import android.content.*;
import android.view.View.*;
import android.util.*;
import java.io.*;
import android.text.*;
import android.graphics.*;
import android.app.*;
import android.view.*;
import android.net.*;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;
import net.gnu.util.*;
import net.gnu.androidutil.*;
import net.gnu.explorer.R;
import com.amaze.filemanager.ui.icons.*;
import com.amaze.filemanager.utils.*;
import com.amaze.filemanager.filesystem.BaseFile;
import com.amaze.filemanager.filesystem.HFile;
import com.amaze.filemanager.activities.*;
import com.amaze.filemanager.services.asynctasks.*;
import android.preference.*;
import com.tekinarslan.sample.*;
import net.gnu.texteditor.*;
import android.support.v4.view.*;

import android.view.animation.*;
import android.widget.LinearLayout.*;
import android.support.v7.view.menu.*;
import android.widget.ImageView.*;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.*;
import android.content.res.*;
import java.lang.ref.*;
import android.support.v4.widget.SwipeRefreshLayout;
import net.gnu.explorer.ContentFragment.*;
import net.dongliu.apk.parser.*;
import net.dongliu.apk.parser.bean.*;
import com.amaze.filemanager.ui.LayoutElement;
import android.view.inputmethod.InputMethodManager;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbException;
import jcifs.smb.SmbAuthException;
import com.cloudrail.si.interfaces.CloudStorage;
import com.amaze.filemanager.exceptions.CloudPluginException;
import android.database.Cursor;
import android.provider.MediaStore;
import com.amaze.filemanager.filesystem.RootHelper;
import com.amaze.filemanager.exceptions.RootNotPermittedException;
import com.amaze.filemanager.fragments.CloudSheetFragment;
import com.amaze.filemanager.database.models.EncryptedEntry;
import com.amaze.filemanager.database.CryptHandler;
import com.amaze.filemanager.fragments.preference_fragments.Preffrag;
import com.amaze.filemanager.services.EncryptService;
import com.amaze.filemanager.utils.provider.UtilitiesProviderInterface;
import com.amaze.filemanager.filesystem.MediaStoreHack;
import com.amaze.filemanager.utils.SmbStreamer.Streamer;
import android.media.RingtoneManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.net.MalformedURLException;
import com.amaze.filemanager.database.CloudHandler;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.support.v4.content.ContextCompat.checkSelfPermission;

public class ContentFragment extends FileFrag implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "ContentFragment";
	
	//private SharedPreferences Sp;
	private int theme1;
	private static final int REQUEST_CODE_STORAGE_PERMISSION = 101;
    
	
	private ScaleGestureDetector mScaleGestureDetector;
	private ImageButton dirMore;
	private TextView mMessageView;
	
	private SearchFileNameTask searchTask = new SearchFileNameTask();
	private TextSearch textSearch = new TextSearch();
	
	private HorizontalScrollView scrolltext;
	private LinearLayout mDirectoryButtons;
	private ImageButton removeBtn;
	private ImageButton removeAllBtn;
	private ImageButton addBtn;
	private ImageButton addAllBtn;
	private LinearLayout selectionCommands;
	private LoadFiles loadList = new LoadFiles();
	private OpenMode openmode;
	private ArrAdapter adapter;
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
	String dirTemp4Search = "";
	public boolean selection, results = false, SHOW_HIDDEN, CIRCULAR_IMAGES, SHOW_PERMISSIONS, SHOW_SIZE, SHOW_LAST_MODIFIED;
	String suffix = "*"; // "*" : files + folders,  "" only folder, ".*" only file "; *" split pattern
	String mimes = "*/*";
	boolean multiFiles = true;
	
	//private Handler handler;
	//String mFilterFiletype;
	//String mFilterMimetype;
	boolean mWriteableOnly;
	//boolean mDirectoriesOnly;
	String[] previousSelectedStr;
	
	//int totalCount, progress;
	private boolean noMedia = false;
	private boolean displayHidden = true;
	
	
	@Override
	public String toString() {
		return "type " + type + ", fake=" + fake + ", suffix=" + suffix + ", mimes=" + mimes + ", multi=" + multiFiles + ", " + currentPathTitle + ", " + super.toString();
	}

	public static ContentFragment newInstance(final SlidingTabsFragment s, final String dir, final String suffix, final String mimes, final boolean multiFiles, Bundle bundle) {//, int se) {//FragmentActivity ctx, 
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
		fragment.suffix = suffix;
		fragment.suffix = fragment.suffix == null ? "*" : fragment.suffix.toLowerCase();
		fragment.mimes = mimes;
		fragment.mimes = fragment.mimes == null ? "*/*" : fragment.mimes.toLowerCase();
		fragment.multiFiles = multiFiles;
		fragment.slidingTabsFragment = s;
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
	public void clone(final Frag frag2, final boolean fake) {
		Log.d(TAG, "clone " + frag2 + ", listView " + listView + ", srcAdapter " + srcAdapter + ", gridLayoutManager " + gridLayoutManager);
		final ContentFragment frag = (ContentFragment) frag2;
		type = frag.type;
		currentPathTitle = frag.currentPathTitle;
		suffix = frag.suffix;
		mimes = frag.mimes;
		multiFiles = frag.multiFiles;
		slidingTabsFragment = frag.slidingTabsFragment;
		this.fake = fake;
		if (fake) {
			dataSourceL1 = frag.dataSourceL1;
			selectedInList1 = frag.selectedInList1;
			tempSelectedInList1 = frag.tempSelectedInList1;
			tempOriDataSourceL1 = frag.tempOriDataSourceL1;
		} else {
			dataSourceL1.clear();
			dataSourceL1.addAll(frag.dataSourceL1);
			selectedInList1.clear();
			selectedInList1.addAll(frag.selectedInList1);
			tempSelectedInList1.clear();
			tempSelectedInList1.addAll(frag.tempSelectedInList1);
			tempOriDataSourceL1.clear();
			tempOriDataSourceL1.addAll(frag.tempOriDataSourceL1);
		}
		spanCount = frag.spanCount;
		dataSourceL2 = frag.dataSourceL2;
		tempPreviewL2 = frag.tempPreviewL2;
		searchMode = frag.searchMode;
		searchVal = frag.searchVal;
		dirTemp4Search = frag.dirTemp4Search;
		srcAdapter = frag.srcAdapter;
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
			allName.setText(frag.allName.getText());
			allType.setText(frag.allType.getText());
			allDate.setText(frag.allDate.getText());
			allSize.setText(frag.allSize.getText());
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
			final int index = frag.gridLayoutManager.findFirstVisibleItemPosition();
			final View vi = frag.listView.getChildAt(0); 
			final int top = (vi == null) ? 0 : vi.getTop();
			gridLayoutManager.scrollToPositionWithOffset(index, top);
			if (frag.selStatus != null) {
				final int visibility = frag.selStatus.getVisibility();
				if (selStatus.getVisibility() != visibility) {
					selStatus.setVisibility(visibility);
					horizontalDivider0.setVisibility(visibility);
					horizontalDivider12.setVisibility(visibility);
					status.setVisibility(visibility);
				}
				selectionStatus1.setText(frag.selectionStatus1.getText());
				diskStatus.setText(frag.diskStatus.getText());
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
    public void onViewCreated(View v, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated " + toString() + ", savedInstanceState=" + savedInstanceState);
		super.onViewCreated(v, savedInstanceState);
		
        final Bundle args = getArguments();

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

		scrolltext = (HorizontalScrollView) v.findViewById(R.id.scroll_text);
		mDirectoryButtons = (LinearLayout) v.findViewById(R.id.directory_buttons);
		dirMore = (ImageButton) v.findViewById(R.id.dirMore);
		drawableDelete = activity.getDrawable(R.drawable.ic_delete_white_36dp);
		drawablePaste = activity.getDrawable(R.drawable.ic_content_paste_white_36dp);

		if (type == Frag.TYPE.SELECTION) {
			removeBtn = (ImageButton) v.findViewById(R.id.remove);
			removeAllBtn = (ImageButton) v.findViewById(R.id.removeAll);
			addBtn = (ImageButton) v.findViewById(R.id.add);
			addAllBtn = (ImageButton) v.findViewById(R.id.addAll);
			selectionCommands = (LinearLayout) v.findViewById(R.id.selectionCommands);
			topflipper.setDisplayedChild(topflipper.indexOfChild(selectionCommands));
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
		} else {
			dirMore.setOnClickListener(this);
			topflipper.setDisplayedChild(topflipper.indexOfChild(scrolltext));
		}

		allCbx.setOnClickListener(this);
		icons.setOnClickListener(this);
		allName.setOnClickListener(this);
		allDate.setOnClickListener(this);
		allSize.setOnClickListener(this);
		allType.setOnClickListener(this);

		listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
				public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
					//Log.d(TAG, "onScrolled dx=" + dx + ", dy=" + dy + ", density=" + activity.density);
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
				}
			});
		listView.setHasFixedSize(true);
		listView.setItemViewCacheSize(20);
		listView.setDrawingCacheEnabled(true);
		listView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		
        DefaultItemAnimator animator = new DefaultItemAnimator();
		animator.setAddDuration(500);
		//animator.setRemoveDuration(500);
        listView.setItemAnimator(animator);

		//scrolltext = (HorizontalScrollView) v.findViewById(R.id.scroll_text);
		//mDirectoryButtons = (LinearLayout) v.findViewById(R.id.directory_buttons);
		//diskStatus = (TextView) v.findViewById(R.id.diskStatus);

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
							if (right.getVisibility() == View.GONE || left.getVisibility() == View.GONE) {
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
//					if (type == -1) {
//						activity.slideFrag2.getCurrentFragment().select(false);
//					} else {
//						activity.slideFrag2.getCurrentFragment().select(true);
//					}
					mScaleGestureDetector.onTouchEvent(event);
                    return false;
                }
            });

		if (args != null) {
			if (currentPathTitle == null) {//"".equals(currentPathTitle) || 
				currentPathTitle = args.getString(ExplorerActivity.EXTRA_ABSOLUTE_PATH);//EXTRA_DIR_PATH);
			} else {
				args.putString(ExplorerActivity.EXTRA_ABSOLUTE_PATH, currentPathTitle);//EXTRA_DIR_PATH
			}
			//Log.d(TAG, "onViewCreated.dir " + dir);
			suffix = args.getString(ExplorerActivity.EXTRA_FILTER_FILETYPE, "*");
			mimes = args.getString(ExplorerActivity.EXTRA_FILTER_MIMETYPE);
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

			allCbx.setEnabled(savedInstanceState.getBoolean("allCbx.isEnabled"));
			setRecyclerViewLayoutManager();
			Log.d(TAG, "configurationChanged " + activity.configurationChanged);
			if (type == Frag.TYPE.EXPLORER && !fake) {// && !activity.configurationChanged
				//updateDir(currentPathTitle, ContentFragment.this);
				setDirectoryButtons();
			}
			final int index  = savedInstanceState.getInt("index");
			final int top  = savedInstanceState.getInt("top");
			Log.d(TAG, "index = " + index + ", " + top);
			gridLayoutManager.scrollToPositionWithOffset(index, top);
		} else {
			//srcAdapter = new ArrAdapter(dataSourceL1);
			//listView1.setAdapter(srcAdapter);
			setRecyclerViewLayoutManager();
			if (type == Frag.TYPE.EXPLORER && !fake) {
				changeDir(currentPathTitle, false);
			}
		}
		updateColor(v);
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
				selectionStatus1.setText(selectedInList1.size()  + "/" + dataSourceL1.size());
			}
			if (mSwipeRefreshLayout.isRefreshing()) {
				mSwipeRefreshLayout.setRefreshing(false);
			}
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
//		outState.putString("dirTemp4Search", dirTemp4Search);
		outState.putBoolean("allCbx.isEnabled", allCbx.isEnabled());

		final int index = gridLayoutManager.findFirstVisibleItemPosition();
        final View vi = listView.getChildAt(0); 
        final int top = (vi == null) ? 0 : vi.getTop();
		outState.putInt("index", index);
		outState.putInt("top", top);
		//Log.d(TAG, "onSaveInstanceState index = " + index + ", " + top);
		super.onSaveInstanceState(outState);
	}

//	@Override
//	public void onViewStateRestored(Bundle savedInstanceState) {
//		//Log.d(TAG, "onViewStateRestored " + savedInstanceState);
//		if (imageLoader == null) {
//			activity = (ExplorerActivity)getActivity();
//			imageLoader = new ImageThreadLoader(activity);
//		}
//		super.onViewStateRestored(savedInstanceState);
//	}

	Map<String, Object> onSaveInstanceState() {
		Map<String, Object> outState = new TreeMap<>();
		//Log.d(TAG, "Map onSaveInstanceState " + dir + ", " + outState);
		outState.put(ExplorerActivity.EXTRA_ABSOLUTE_PATH, currentPathTitle);//EXTRA_DIR_PATH
		outState.put(ExplorerActivity.EXTRA_FILTER_FILETYPE, suffix);
		outState.put(ExplorerActivity.EXTRA_FILTER_MIMETYPE, mimes);
		outState.put(ExplorerActivity.EXTRA_MULTI_SELECT, multiFiles);
		outState.put("selectedInList1", selectedInList1);
		outState.put("dataSourceL1", dataSourceL1);
		outState.put("searchMode", searchMode);
		outState.put("searchVal", searchET.getText().toString());
		outState.put("dirTemp4Search", dirTemp4Search);
		outState.put("allCbx.isEnabled", allCbx.isEnabled());

		final int index = gridLayoutManager.findFirstVisibleItemPosition();
        final View vi = listView.getChildAt(0); 
        final int top = (vi == null) ? 0 : vi.getTop();
		outState.put("index", index);
		outState.put("top", top);

        return outState;
	}

	void reload(Map<String, Object> savedInstanceState) {
		Log.d(TAG, "reload " + savedInstanceState + currentPathTitle);
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
		dirTemp4Search = (String) savedInstanceState.get("dirTemp4Search");
		//listView1.setSelectionFromTop(savedInstanceState.getInt("index"),
		//savedInstanceState.getInt("top"));
		allCbx.setEnabled(savedInstanceState.get("allCbx.isEnabled"));
		srcAdapter.notifyDataSetChanged();

		setRecyclerViewLayoutManager();
		gridLayoutManager.scrollToPositionWithOffset(savedInstanceState.get("index"), savedInstanceState.get("top"));

		updateDir(currentPathTitle, ContentFragment.this);
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
//	@Override
//	public void onStop() {
//		//Log.d(TAG, "onStop " + activity.slideFrag.indexOf(this) + ", fake=" + fake + ", " + toString());
//		//searchTask.cancel(true);
//		//loadList.cancel(true);
//		super.onStop();
//	}

//	@Override
//	public void onStart() {
//		Log.d(TAG, "onStart " + toString());
//		super.onStart();
//	}

	public void refreshDirectory() {
		Log.d(TAG, "refreshDirectory " + currentPathTitle + ", " + this);
		if (currentPathTitle != null) {
			changeDir(currentPathTitle, false);
		} else {
			updateDir(dirTemp4Search, this);
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
							changeDir(dir2, true);

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

	public void updateDir(String d, FileFrag cf) {//ExploreFragment
		Log.d(TAG, "updateDir " + d + ", " + cf);
		setDirectoryButtons();
		activity.dir = d;
		if (cf == activity.slideFrag.getCurrentFragment()) {
			activity.curContentFrag = (ContentFragment) cf;
			activity.slideFrag.notifyTitleChange();
		} else if (activity.slideFrag2 != null && cf == activity.slideFrag2.getCurrentFragment()) {
			//title = new File(d).getName();
			activity.curExploreFrag = (ContentFragment) cf;
			activity.slideFrag2.notifyTitleChange();
		}
	}

    /**
     * Sets up a FileObserver to watch the current directory.
     */
	FileObserver createFileObserver(String path) {
        return new FileObserver(path, FileObserver.CREATE | FileObserver.DELETE
								| FileObserver.MOVED_FROM | FileObserver.MOVED_TO
								| FileObserver.DELETE_SELF | FileObserver.MOVE_SELF
								| FileObserver.CLOSE_WRITE) {
            @Override
            public void onEvent(int event, String path) {
                if (path != null) {
                    Util.debug(TAG, "FileObserver received event %d, CREATE = 256;DELETE = 512;DELETE_SELF = 1024;MODIFY = 2;MOVED_FROM = 64;MOVED_TO = 128; path %s", event, path);
					activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								refreshDirectory();
							}
						});
                }
            }
        };
    }

    /**
     * Assigns sort modes
     * A value from 0 to 3 defines sort mode as name/last modified/size/type in ascending order
     * Values from 4 to 7 defines sort mode as name/last modified/size/type in descending order
     *
     * Final value of {@link #sortby} varies from 0 to 3
     */
    public void getSortModes() {
        int t = Integer.parseInt(sharedPref.getString("sortby", "0"));
        if (t <= 3) {
            sortby = t;
            asc = 1;
        } else if (t > 3) {
            asc = -1;
            sortby = t - 4;
        }

        dsort = Integer.parseInt(sharedPref.getString("dirontop", "0"));
    }

	void openwith() {
		//ArrayList<Integer> plist = adapter.getCheckedItemPositions();
		activity.getFutils().openunknown(new File(((LayoutElement)selectedInList1.get(0)).path), getActivity(), true);//dataSourceL1.plist.get(
	}

	void all() {
		if (dataSourceL1.size() == selectedInList1.size()) {//}adapter.areAllChecked(path)) {
			adapter.toggleChecked(false);//, path);
		} else {
			adapter.toggleChecked(true);//, path);
		}
	}

	void ex() {
		//ArrayList<Integer> plist = adapter.getCheckedItemPositions();
		activity.mainActivityHelper.extractFile(new File(((LayoutElement)selectedInList1.get(0)).path));//plist.get(
	}


	public static void decryptFile(final FileFrag main, OpenMode openMode, BaseFile sourceFile,
                                   String decryptPath,
                                   UtilitiesProviderInterface activity) {

        Intent decryptIntent = new Intent(main.getContext(), EncryptService.class);
        decryptIntent.putExtra(EncryptService.TAG_OPEN_MODE, openMode.ordinal());
        decryptIntent.putExtra(EncryptService.TAG_CRYPT_MODE,
							   EncryptService.CryptEnum.DECRYPT.ordinal());
        decryptIntent.putExtra(EncryptService.TAG_SOURCE, sourceFile);
        decryptIntent.putExtra(EncryptService.TAG_DECRYPT_PATH, decryptPath);

        SharedPreferences preferences1 = PreferenceManager.getDefaultSharedPreferences(main.getContext());

        EncryptedEntry encryptedEntry;
        try {
            encryptedEntry = findEncryptedEntry(main.getContext(), sourceFile.getPath());
        } catch (Exception e) {
            e.printStackTrace();
            encryptedEntry = null;
        }

        if (encryptedEntry == null) {

            // we couldn't find any entry in database or lost the key to decipher
            Toast.makeText(main.getContext(),
						   main.getActivity().getResources().getString(R.string.crypt_decryption_fail),
						   Toast.LENGTH_LONG).show();
            return;
        }

        ArrAdapter.DecryptButtonCallbackInterface decryptButtonCallbackInterface =
			new ArrAdapter.DecryptButtonCallbackInterface() {
			@Override
			public void confirm(Intent intent) {

				ServiceWatcherUtil.runService(main.getContext(), intent);
			}

			@Override
			public void failed() {
				Toast.makeText(main.getContext(), main.getActivity().getResources().getString(R.string.crypt_decryption_fail_password),
							   Toast.LENGTH_LONG).show();
			}
		};

        switch (encryptedEntry.getPassword()) {
            case Preffrag.ENCRYPT_PASSWORD_FINGERPRINT:
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        activity.getFutils().showDecryptFingerprintDialog(decryptIntent,
																		  main, activity.getAppTheme(), decryptButtonCallbackInterface);
                    } else throw new Exception();
                } catch (Exception e) {
                    e.printStackTrace();

                    Toast.makeText(main.getContext(),
								   main.getResources().getString(R.string.crypt_decryption_fail),
								   Toast.LENGTH_LONG).show();
                }
                break;
            case Preffrag.ENCRYPT_PASSWORD_MASTER:
                activity.getFutils().showDecryptDialog(decryptIntent,
													   main, activity.getAppTheme(),
													   preferences1.getString(Preffrag.PREFERENCE_CRYPT_MASTER_PASSWORD,
																			  Preffrag.PREFERENCE_CRYPT_MASTER_PASSWORD_DEFAULT),
													   decryptButtonCallbackInterface);
                break;
            default:
                activity.getFutils().showDecryptDialog(decryptIntent,
													   main, activity.getAppTheme(),
													   encryptedEntry.getPassword(),
													   decryptButtonCallbackInterface);
                break;
        }
    }

    /**
     * Queries database to find entry for the specific path
     * @param path  the path to match with
     * @return the entry
     */
    private static EncryptedEntry findEncryptedEntry(Context context, String path) throws Exception {

        CryptHandler handler = new CryptHandler(context);

        EncryptedEntry matchedEntry = null;
        // find closest path which matches with database entry
        for (EncryptedEntry encryptedEntry : handler.getAllEntries()) {
            if (path.contains(encryptedEntry.getPath())) {

                if (matchedEntry == null || (matchedEntry != null &&
					matchedEntry.getPath().length() < encryptedEntry.getPath().length())) {
                    matchedEntry = encryptedEntry;
                }
            }
        }
        return matchedEntry;
    }
	

    @Override
    public void onResume() {
        Log.d(TAG, "onResume index " + activity.slideFrag.indexOfMTabs(this) + ", " + /*activity.slideFrag2.indexOfMTabs(this) + ", " + */ slidingTabsFragment.side + ", " + type + ", fake=" + fake + ", " + currentPathTitle + ", dirTemp4Search=" + dirTemp4Search);
		super.onResume();
		if (type == Frag.TYPE.EXPLORER) {
			getView().setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
			if (mFileObserver != null) {
				mFileObserver.stopWatching();
			}
			if (currentPathTitle == null) {
				mFileObserver = createFileObserver(dirTemp4Search);
			} else {
				mFileObserver = createFileObserver(currentPathTitle);
			}
			mFileObserver.startWatching();
			activity = (ExplorerActivity)getActivity();

			selectionStatus1.setText(selectedInList1.size() 
									 + "/" + dataSourceL1.size());

			final File curDir = new File(currentPathTitle == null ? dirTemp4Search : currentPathTitle);
			diskStatus.setText(
				"Free " + Util.nf.format(curDir.getFreeSpace() / (1 << 20))
				+ " MiB. Used " + Util.nf.format((curDir.getTotalSpace() - curDir.getFreeSpace()) / (1 << 20))
				+ " MiB. Total " + Util.nf.format(curDir.getTotalSpace() / (1 << 20)) + " MiB");
		} else {
			activity.curContentFrag.dataSourceL2 = dataSourceL1;
			if (activity.curContentFrag.srcAdapter != null) {
				activity.curContentFrag.srcAdapter.notifyDataSetChanged();
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
		selectionStatus1.setTextColor(ExplorerActivity.TEXT_COLOR);
		diskStatus.setTextColor(ExplorerActivity.TEXT_COLOR);
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
		Log.d(TAG, "getTitle() " + this);
		if (type == Frag.TYPE.EXPLORER) {
			if (currentPathTitle == null) {
				return "";
			} else if ("/".equals(currentPathTitle)) {
				return "/";
			} else {
				return new File(currentPathTitle).getName();
			}
		} else {
			return title;
		}
	}

	public ArrayList<LayoutElement> addToSmb(SmbFile[] mFile, String path) throws SmbException {
        ArrayList<LayoutElement> ret = new ArrayList<>();
        //if (searchHelper.size() > 500) searchHelper.clear();
        for (int i = 0; i < mFile.length; i++) {
            if (DataUtils.hiddenfiles.contains(mFile[i].getPath()))
                continue;
            String name = mFile[i].getName();
            name = (mFile[i].isDirectory() && name.endsWith("/")) ? name.substring(0, name.length() - 1) : name;
            if (path.equals(smbPath)) {
                if (name.endsWith("$")) continue;
            }
            if (mFile[i].isDirectory()) {
                folder_count++;
                LayoutElement layoutElements = new LayoutElement(name, mFile[i].getPath(),
																   "", "", /*"", */mFile[i].length(), /*false, */mFile[i].lastModified(), true);
                layoutElements.setMode(OpenMode.SMB);
                //searchHelper.add(layoutElements.generateBaseFile());
                ret.add(layoutElements);
            } else {
                file_count++;
                try {
                    LayoutElement layoutElements = new LayoutElement(
						//Icons.loadMimeIcon(mFile[i].getPath(), !IS_LIST, res), 
						name,
						mFile[i].getPath(), "", "", //Formatter.formatFileSize(getContext(), mFile[i].length()), 
						mFile[i].length(), //false,
						mFile[i].lastModified(), false);
                    layoutElements.setMode(OpenMode.SMB);
                    //searchHelper.add(layoutElements.generateBaseFile());
                    ret.add(layoutElements);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }

    public void reauthenticateSmb() {
        if (smbPath != null) {
            try {
                activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							int i=-1;
							if ((i = DataUtils.containsServer(smbPath)) != -1) {
								activity.showSMBDialog(DataUtils.getServers().get(i)[0], smbPath, true);
							}
						}
					});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

	public boolean checkforpath(String path) {
        boolean grid = false, both_contain = false;
        int index1 = -1, index2 = -1;
        for (String s : DataUtils.gridfiles) {
            index1++;
            if ((path).contains(s)) {
                grid = true;
                break;
            }
        }
        for (String s : DataUtils.listfiles) {
            index2++;
            if ((path).contains(s)) {
                if (grid) both_contain = true;
                grid = false;
                break;
            }
        }
        if (!both_contain) return grid;
        String path1 = DataUtils.gridfiles.get(index1), path2 = DataUtils.listfiles.get(index2);
        if (path1.contains(path2))
            return true;
        else if (path2.contains(path1))
            return false;
        else
            return grid;
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

	private void returnIntentResults(File file) {
        activity.mReturnIntent = false;

        Intent intent = new Intent();
        if (activity.mRingtonePickerIntent) {

            Uri mediaStoreUri = MediaStoreHack.getUriFromFile(file.getPath(), getActivity());
            System.out.println(mediaStoreUri.toString() + "\t" + MimeTypes.getMimeType(file));
            intent.setDataAndType(mediaStoreUri, MimeTypes.getMimeType(file));
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, mediaStoreUri);
            getActivity().setResult(getActivity().RESULT_OK, intent);
            getActivity().finish();
        } else {

            Log.d("pickup", "file");
            intent.setData(Uri.fromFile(file));
            getActivity().setResult(getActivity().RESULT_OK, intent);
            getActivity().finish();
        }
    }

	public static void launchSMB(final SmbFile smbFile, final long si, final Activity activity) {
        final Streamer s = Streamer.getInstance();
        new Thread() {
            public void run() {
                try {
                    /*List<SmbFile> subtitleFiles = new ArrayList<SmbFile>();

					 // finding subtitles
					 for (Layoutelements layoutelement : LIST_ELEMENTS) {
					 SmbFile smbFile = new SmbFile(layoutelement.getDesc());
					 if (smbFile.getName().contains(smbFile.getName())) subtitleFiles.add(smbFile);
					 }*/

                    s.setStreamSrc(smbFile, si);
                    activity.runOnUiThread(new Runnable() {
							public void run() {
								try {
									Uri uri = Uri.parse(Streamer.URL + Uri.fromFile(new File(Uri.parse(smbFile.getPath()).getPath())).getEncodedPath());
									Intent i = new Intent(Intent.ACTION_VIEW);
									i.setDataAndType(uri, MimeTypes.getMimeType(new File(smbFile.getPath())));
									PackageManager packageManager = activity.getPackageManager();
									List<ResolveInfo> resInfos = packageManager.queryIntentActivities(i, 0);
									if (resInfos != null && resInfos.size() > 0)
										activity.startActivity(i);
									else
										Toast.makeText(activity,
													   activity.getResources().getString(R.string.smb_launch_error),
													   Toast.LENGTH_SHORT).show();
								} catch (ActivityNotFoundException e) {
									e.printStackTrace();
								}
							}
						});

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void goBack() {
        if (openMode == OpenMode.CUSTOM) {
            loadlist(home, /*false, */OpenMode.FILE);
            return;
        }

        HFile currentFile = new HFile(openMode, currentPathTitle);
        if (!results && !mRetainSearchTask) {

            // normal case
            if (selection) {
                adapter.toggleChecked(false);
            } else {

                if (openMode == OpenMode.SMB) {
                    try {
                        if (!smbPath.equals(currentPathTitle)) {
                            String path = (new SmbFile(this.currentPathTitle).getParent());
                            loadlist(path, /*true, */openMode);
                        } else {
							loadlist(home, /*false, */OpenMode.FILE);
						}
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                } else if (currentPathTitle.equals("/") || currentPathTitle.equals(home) ||
						   currentPathTitle.equals(OTGUtil.PREFIX_OTG + "/")
						   || currentPathTitle.equals(CloudHandler.CLOUD_PREFIX_BOX + "/")
						   || currentPathTitle.equals(CloudHandler.CLOUD_PREFIX_DROPBOX + "/")
						   || currentPathTitle.equals(CloudHandler.CLOUD_PREFIX_GOOGLE_DRIVE + "/")
						   || currentPathTitle.equals(CloudHandler.CLOUD_PREFIX_ONE_DRIVE + "/")
						   )
                    activity.onBackPressed();
                else if (activity.getFutils().canGoBack(getContext(), currentFile)) {
                    loadlist(currentFile.getParent(getContext()), /*true, */openMode);
                } else {
					activity.onBackPressed();
				}
            }
//        } else if (!results && mRetainSearchTask) {
//
//            // case when we had pressed on an item from search results and wanna go back
//            // leads to resuming the search task
//
//            if (MainActivityHelper.SEARCH_TEXT != null) {
//
//                // starting the search query again :O
//                MAIN_ACTIVITY.mainFragment = (MainFragment) MAIN_ACTIVITY.getFragment().getTab();
//                FragmentManager fm = MAIN_ACTIVITY.getSupportFragmentManager();
//
//                // getting parent path to resume search from there
//                String parentPath = new HFile(openMode, currentPathTitle).getParent(getActivity());
//                // don't fuckin' remove this line, we need to change
//                // the path back to parent on back press
//                currentPathTitle = parentPath;
//
//                MainActivityHelper.addSearchFragment(fm, new SearchAsyncHelper(),
//													 parentPath, MainActivityHelper.SEARCH_TEXT, openMode, BaseActivity.rootMode,
//													 sharedPref.getBoolean(SearchAsyncHelper.KEY_REGEX, false),
//													 sharedPref.getBoolean(SearchAsyncHelper.KEY_REGEX_MATCHES, false));
//            } else loadlist(currentPathTitle, true, OpenMode.UNKNOWN);
//
//            mRetainSearchTask = false;
//        } else {
//            // to go back after search list have been popped
//            FragmentManager fm = getActivity().getSupportFragmentManager();
//            SearchAsyncHelper fragment = (SearchAsyncHelper) fm.findFragmentByTag(MainActivity.TAG_ASYNC_HELPER);
//            if (fragment != null) {
//                if (fragment.mSearchTask.getStatus() == AsyncTask.Status.RUNNING) {
//                    fragment.mSearchTask.cancel(true);
//                }
//            }
//            loadlist(new File(currentPathTitle).getPath(), true, OpenMode.UNKNOWN);
//            results = false;
        }
    }

	public void loadlist(String path, /*boolean back, */OpenMode openMode) {
        if (mActionMode != null) {
            mActionMode.finish();
        }
        /*if(openMode==-1 && android.util.Patterns.EMAIL_ADDRESS.matcher(path).matches())
		 bindDrive(path);
		 else */
//        if (loadList != null) loadList.cancel(true);
//        loadList = new LoadFiles();//LoadList(activity, activity, /*back, */this, openMode);
//        loadList.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (path));
		changeDir(path, true);
		this.openMode = openMode;
    }

	@Override
	public void onClick(final View p1) {
		//Log.d(TAG, "onClick " + this + ", " + type);
//		if (type == -1) {
//			activity.slideFrag2.getCurrentFragment2().select(false);
//		} else {
//			activity.slideFrag2.getCurrentFragment2().select(true);
//		}
		super.onClick(p1);
		switch (p1.getId()) {
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
							horizontalDivider.setVisibility(View.VISIBLE);
						}
					} else {
						allCbx.setSelected(false);
						if (activity.COPY_PATH == null && activity.MOVE_PATH == null && commands.getVisibility() == View.VISIBLE) {
							horizontalDivider.setVisibility(View.GONE);
							commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
							commands.setVisibility(View.GONE);
						}
					}
					selectionStatus1.setText(selectedInList1.size() 
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
				mainmenu(p1);
				break;
			case R.id.search:
				searchButton();
				break;
			case R.id.clear:
				searchET.setText("");
				break;
			case R.id.dirMore:
				final MenuBuilder menuBuilder = new MenuBuilder(activity);
				final MenuInflater inflater = new MenuInflater(activity);
				inflater.inflate(R.menu.storage, menuBuilder);
				final MenuPopupHelper optionsMenu = new MenuPopupHelper(activity , menuBuilder, dirMore);
				optionsMenu.setForceShowIcon(true);
				MenuItem mi = menuBuilder.findItem(R.id.otg);
				if (true) {
					mi.setEnabled(true);
				} else {
					mi.setEnabled(false);
				}
				mi.getIcon().setFilterBitmap(true);
				mi.getIcon().setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);

				mi = menuBuilder.findItem(R.id.addFolder);
				mi.getIcon().setFilterBitmap(true);
				mi.getIcon().setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);

				mi = menuBuilder.findItem(R.id.sdcard);
				mi.getIcon().setFilterBitmap(true);
				mi.getIcon().setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);

				mi = menuBuilder.findItem(R.id.microsd);
				if (new File("/storage/MicroSD").exists()) {
					mi.setEnabled(true);
				} else {
					mi.setEnabled(false);
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
								case R.id.addFolder:
									activity.addFolder(p1);
									break;
							}
							return true;
						}
						@Override
						public void onMenuModeChange(MenuBuilder menu) {}
					});
				optionsMenu.show();
				break;
		}
	}

	private class TextSearch implements TextWatcher {
		public void beforeTextChanged(CharSequence s, int start, int end, int count) {
		}

		public void afterTextChanged(final Editable text) {
			final String filesearch = text.toString();
			Log.d(TAG, "quicksearch " + filesearch);
			if (filesearch.length() > 0) {
				if (searchTask.getStatus() == AsyncTask.Status.RUNNING) {
					searchTask.cancel(true);
				}
				searchTask.execute(filesearch);
			}
		}

		public void onTextChanged(CharSequence s, int start, int end, int count) {
		}
	}

	public void refreshRecyclerViewLayoutManager() {
		setRecyclerViewLayoutManager();
		horizontalDivider0.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
		horizontalDivider12.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
		horizontalDivider7.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
	}

	void setRecyclerViewLayoutManager() {
        Log.d(TAG, "setRecyclerViewLayoutManager " + gridLayoutManager);
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
		gridLayoutManager = new GridLayoutManager(fragActivity, spanCount);
		listView.removeItemDecoration(dividerItemDecoration);
		listView.invalidateItemDecorations();
		if (spanCount <= 2) {
			dividerItemDecoration = new GridDividerItemDecoration(fragActivity, true);
			listView.addItemDecoration(dividerItemDecoration);
		}

		srcAdapter = new ArrAdapter(this, dataSourceL1, commands, horizontalDivider);
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
		Log.d(TAG, "back " + backStack.size());
		if (backStack.size() > 1 && (softBundle = backStack.pop()) != null && softBundle.get("dataSourceL1") != null) {
			Log.d(TAG, "back " + softBundle);
			reload(softBundle);
			return true;
		} else {
			return false;
		}
	}

	public void changeDir(final String curDir, final boolean doScroll) {
		Log.d(TAG, "changeDir " + curDir + ", doScroll " + doScroll + ", " + type + ", " + slidingTabsFragment.side);
		loadList.cancel(true);
		loadList = new LoadFiles();
		searchTask.cancel(true);
		loadList.execute(curDir, doScroll);
	}

	private void manageUi(boolean search) {
		if (search == true) {
			searchButton.setImageResource(R.drawable.ic_arrow_back_grey600);
			topflipper.setDisplayedChild(topflipper.indexOfChild(quickLayout));
			if (type == Frag.TYPE.SELECTION) {
				searchET.setHint("Search");
				searchMode = true;
			} else {
				searchET.setHint("Search " + ((currentPathTitle != null) ? new File(currentPathTitle).getName() : new File(dirTemp4Search).getName()));
				setSearchMode(true);
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
				topflipper.setDisplayedChild(topflipper.indexOfChild(selectionCommands));
				searchMode = false;
				dataSourceL1.clear();
				dataSourceL1.addAll(tempOriDataSourceL1);
			} else {
				topflipper.setDisplayedChild(topflipper.indexOfChild(scrolltext));
				setSearchMode(false);
				refreshDirectory();
			}
		}
	}

	public void searchButton() {
		searchMode = !searchMode;
		manageUi(searchMode);
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

//    public void updateClipboardInfo() {
//        CopyHelper copyHelper = ((FileManagerApplication) getActivity().getApplication()).getCopyHelper();
//        if (copyHelper.canPaste()) {
//            mClipboardInfo.setVisibility(View.VISIBLE);
//            int count = copyHelper.getItemsCount();
//            if (CopyHelper.Operation.COPY.equals(copyHelper.getOperationType())) {
//                mClipboardContent.setText(getResources().getQuantityString(R.plurals.clipboard_info_items_to_copy, count, count));
//                mClipboardAction.setText(getString(R.string.clipboard_dismiss));
//            } else if (CopyHelper.Operation.CUT.equals(copyHelper.getOperationType())) {
//                mClipboardContent.setText(getResources().getQuantityString(R.plurals.clipboard_info_items_to_move, count, count));
//                mClipboardAction.setText(getString(R.string.clipboard_undo));
//            }
//        } else {
//            mClipboardInfo.setVisibility(View.GONE);
//        }
//    }
	
    protected void updateNoAccessMessage(boolean showMessage) {
        mMessageView.setVisibility(showMessage ? View.VISIBLE : View.GONE);
    }
	
//    protected void selectInList(File selectFile) {
//        String filename = selectFile.getName();
//
//        int count = mAdapter.getCount();
//        for (int i = 0; i < count; i++) {
//            FileHolder it = (FileHolder) mAdapter.getItem(i);
//            if (it.getName().equals(filename)) {
//                getListView().setSelection(i);
//                break;
//            }
//        }
//    }
	
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
	
	class LoadFiles extends AsyncTask<Object, String, Void> {
		//private File curDir;
		private Boolean doScroll;
		private ArrayList<LayoutElement> dataSourceL1a = new ArrayList<>();

		static final private int PROGRESS_STEPS = 50;
		boolean cancelled;
		
		
		@Override
		protected void onPreExecute() {
			if (!mSwipeRefreshLayout.isRefreshing()) {
				mSwipeRefreshLayout.setRefreshing(true);
			}
		}

		@Override
		protected Void doInBackground(final Object... params) {
			currentPathTitle = (String) params[0];
			doScroll = (Boolean) params[1];
			noMedia = false;
			if (currentPathTitle == null) {
				return null;
			}
			Log.d(TAG, "LoadFiles.doInBackground " + currentPathTitle + ", " + ContentFragment.this);
			folder_count = 0;
			file_count = 0;
			if (openmode == OpenMode.UNKNOWN) {
				HFile hFile = new HFile(OpenMode.UNKNOWN, currentPathTitle);
				hFile.generateMode(activity);
				if (hFile.isLocal()) {
					openmode = OpenMode.FILE;
				} else if (hFile.isSmb()) {
					openmode = OpenMode.SMB;
					smbPath = currentPathTitle;
				} else if (hFile.isOtgFile()) {
					openmode = OpenMode.OTG;
				} else if (hFile.isBoxFile()) {
					openmode = OpenMode.BOX;
				} else if (hFile.isDropBoxFile()) {
					openmode = OpenMode.DROPBOX;
				} else if (hFile.isGoogleDriveFile()) {
					openmode = OpenMode.GDRIVE;
				} else if (hFile.isOneDriveFile()) {
					openmode = OpenMode.ONEDRIVE;
				} else if (hFile.isCustomPath())
					openmode = OpenMode.CUSTOM;
				else if (android.util.Patterns.EMAIL_ADDRESS.matcher(currentPathTitle).matches()) {
					openmode = OpenMode.ROOT;
				}
			}

			switch (openmode) {
				case SMB:
					HFile hFile = new HFile(OpenMode.SMB, currentPathTitle);
					try {
						SmbFile[] smbFile = hFile.getSmbFile(5000).listFiles();
						dataSourceL1a = addToSmb(smbFile, currentPathTitle);
						openmode = OpenMode.SMB;
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
					switch (Integer.parseInt(currentPathTitle)) {
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

					currentPathTitle = String.valueOf(Integer.parseInt(currentPathTitle));

					try {
						if (arrayList != null)
							dataSourceL1a = addTo(arrayList);
						else return null;// new ArrayList<LayoutElements>();
					} catch (Exception e) {
					}
					break;
				case OTG:
					dataSourceL1a = addTo(listOtg(currentPathTitle));
					openmode = OpenMode.OTG;
					break;
				case DROPBOX:

					CloudStorage cloudStorageDropbox = DataUtils.getAccount(OpenMode.DROPBOX);

					try {
						dataSourceL1a = addTo(listCloud(currentPathTitle, cloudStorageDropbox, OpenMode.DROPBOX));
					} catch (CloudPluginException e) {
						e.printStackTrace();
						return null;// new ArrayList<LayoutElements>();
					}
					break;
				case BOX:
					CloudStorage cloudStorageBox = DataUtils.getAccount(OpenMode.BOX);

					try {
						dataSourceL1a = addTo(listCloud(currentPathTitle, cloudStorageBox, OpenMode.BOX));
					} catch (CloudPluginException e) {
						e.printStackTrace();
						return null;// new ArrayList<LayoutElements>();
					}
					break;
				case GDRIVE:
					CloudStorage cloudStorageGDrive = DataUtils.getAccount(OpenMode.GDRIVE);

					try {
						dataSourceL1a = addTo(listCloud(currentPathTitle, cloudStorageGDrive, OpenMode.GDRIVE));
					} catch (CloudPluginException e) {
						e.printStackTrace();
						return null;// new ArrayList<LayoutElements>();
					}
					break;
				case ONEDRIVE:
					CloudStorage cloudStorageOneDrive = DataUtils.getAccount(OpenMode.ONEDRIVE);

					try {
						dataSourceL1a = addTo(listCloud(currentPathTitle, cloudStorageOneDrive, OpenMode.ONEDRIVE));
					} catch (CloudPluginException e) {
						e.printStackTrace();
						return null;// new ArrayList<LayoutElements>();
					}
					break;
				default:
					// we're neither in OTG not in SMB, load the list based on root/general filesystem
					try {

						File curDir = new File(currentPathTitle);
						while (curDir != null && !curDir.exists()) {
							publishProgress(curDir.getAbsolutePath() + " is not existed");
							curDir = curDir.getParentFile();
						}
						if (curDir == null) {
							publishProgress("Current directory is not existed. Change to root");
							curDir = new File("/");
						}

						final String curPath = curDir.getAbsolutePath();
						if (!dirTemp4Search.equals(curPath)) {
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
						dirTemp4Search = currentPathTitle;
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
									openmode = mode;
								}
							});
						//List<File> files = FileUtil.currentFileFolderListing(curDir);
						//Log.d("filesListing", Util.collectionToString(files, true, "\r\n"));
						String[] suffixes = {"*"};
						if (suffix != null) {	// always dir, already checked
							// tìm danh sách các file có ext thích hợp
							//Log.d("suffix", suffix);
							suffixes = suffix.toLowerCase().split(";\\s*\\**\\.");
						}
						int lastIndexOfDot;
						String fName;
						String ext;
						Arrays.sort(suffixes);
						//final int size = files.size();
						boolean isDirectory;
						for (BaseFile f : files) {
							fName = f.getName();
							isDirectory = f.isDirectory();
//							if (isDirectory) {
//								folder_count++;
//							} else {
//								file_count++;
//							}
							//updateProgress(folder_count + file_count, size);

							// It's the noMedia file. Raise the flag.
							if (!noMedia && fName.equalsIgnoreCase(".nomedia")) {
								noMedia = true;
							}
							
							//If the user doesn't want to display hidden files and the file is hidden, ignore this file.
							if (!displayHidden && f.f.isHidden()) {
								continue;
							}

							Log.d(TAG, "f.f=" + f.f + ", mimes="+mimes + ", suffixes=" + suffixes +", suffix=" + suffix + ", getMimeType=" + MimeTypes.getMimeType(f.f) + ", " + ((mimes+"").indexOf(MimeTypes.getMimeType(f.f)+"") >= 0));
							if (isDirectory) {
								if (!mWriteableOnly || f.f.canWrite()) {
									dataSourceL1a.add(new LayoutElement(f));
								}
							} else if (suffix.length() > 0) {//!mDirectoriesOnly
								if (".*".equals(suffix) ||
									"*".equals(suffix) ||
									mimes.contentEquals("*/*") || 
									(mimes+"").indexOf(MimeTypes.getMimeType(f.f)+"") >= 0) {
									dataSourceL1a.add(new LayoutElement(f));
								} else {//if (suffix != null) 
									lastIndexOfDot = fName.lastIndexOf(".");
									ext = lastIndexOfDot >= 0 ? fName.substring(lastIndexOfDot) : "";
									Log.d(TAG, "ext=" + ext + ", " + (Arrays.binarySearch(suffixes, ext.toLowerCase()) >= 0));
									if (Arrays.binarySearch(suffixes, ext.toLowerCase()) >= 0) {
										dataSourceL1a.add(new LayoutElement(f));
									}
								}
							}
							
							
							
//							if (f.exists()) {
//								fName = f.getName();
//								//Log.d("changeDir fName", fName + ", isDir " + f.isDirectory());
//								if (f.isDirectory()) {
//									dataSourceL1a.add(new LayoutElements(f));
//								} else {
//									if (suffix.length() > 0) {
//										if (".*".equals(suffix)) {
//											dataSourceL1a.add(new LayoutElements(f));
//										} else {
//											lastIndexOfDot = fName.lastIndexOf(".");
//											if (lastIndexOfDot >= 0) {
//												ext = fName.substring(lastIndexOfDot);
//												if ((Arrays.binarySearch(suffixes, ext.toLowerCase()) >= 0)) {
//													dataSourceL1a.add(new LayoutElements(f));
//												}
//											}
//										}
//									}
//								}
//							}
						}
						// điền danh sách vào allFiles

						//dataSourceL1a = addTo(files);
						dirTemp4Search = currentPathTitle;
					} catch (RootNotPermittedException e) {
						//AppConfig.toast(c, c.getString(R.string.rootfailure));
						return null;
					}
					break;
			}

			if (dataSourceL1a != null && !(openmode == OpenMode.CUSTOM && ((currentPathTitle).equals("5") || (currentPathTitle).equals("6"))))
				Collections.sort(dataSourceL1a, fileListSorter);//(dsort, sortby, asc));

			if (openMode != OpenMode.CUSTOM)
				DataUtils.addHistoryFile(currentPathTitle);

			//curDir = (File) params[0];
			//curDir = new File(path);
//			doScroll = (Boolean) params[1];
//			
//			while (curDir != null && !curDir.exists()) {
//				publishProgress(curDir.getAbsolutePath() + " is not existed");
//				curDir = curDir.getParentFile();
//			}
//			if (curDir == null) {
//				publishProgress("Current directory is not existed. Change to root");
//				curDir = new File("/");
//			}
//			
//			final String curPath = curDir.getAbsolutePath();
//			if (!dirTemp4Search.equals(curPath)) {
//				if (backStack.size() > ExplorerActivity.NUM_BACK) {
//					backStack.remove(0);
//				}
//				final Map<String, Object> bun = onSaveInstanceState();
//				backStack.push(bun);
//				
//				history.remove(curPath);
//				if (history.size() > ExplorerActivity.NUM_BACK) {
//					history.remove(0);
//				}
//				history.push(curPath);
//				
//				activity.historyList.remove(curPath);
//				if (activity.historyList.size() > ExplorerActivity.NUM_BACK) {
//					activity.historyList.remove(0);
//				}
//				activity.historyList.push(curPath);
//				tempPreviewL2 = null;
//			}
//			path = curPath;
//			dirTemp4Search = path;
//			//Log.d(TAG, Util.collectionToString(history, true, "\n"));
//			
//			if (mFileObserver != null) {
//				mFileObserver.stopWatching();
//			}
//			mFileObserver = createFileObserver(path);
//			mFileObserver.startWatching();
//			if (tempPreviewL2 != null && !tempPreviewL2.bf.f.exists()) {
//				tempPreviewL2 = null;
//			}
//			
//			List<File> files = FileUtil.currentFileFolderListing(curDir);
//			//Log.d("filesListing", Util.collectionToString(files, true, "\r\n"));
//			if (files != null) {	// always dir, already checked
//				// tìm danh sách các file có ext thích hợp
//				//Log.d("suffix", suffix);
//				String[] suffixes = suffix.toLowerCase().split("; *");
//				Arrays.sort(suffixes);
//				for (File f : files) {
//					if (f.exists()) {
//						String fName = f.getName();
//						//Log.d("changeDir fName", fName + ", isDir " + f.isDirectory());
//						if (f.isDirectory()) {
//							dataSourceL1a.add(new LayoutElements(f));
//						} else {
//							if (suffix.length() > 0) {
//								if (".*".equals(suffix)) {
//									dataSourceL1a.add(new LayoutElements(f));
//								} else {
//									int lastIndexOf = fName.lastIndexOf(".");
//									if (lastIndexOf >= 0) {
//										String ext = fName.substring(lastIndexOf);
//										boolean chosen = Arrays.binarySearch(suffixes, ext.toLowerCase()) >= 0;
//										if (chosen) {
//											dataSourceL1a.add(new LayoutElements(f));
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//				// điền danh sách vào allFiles
//			}
			//Log.d(TAG, "changeDir dataSourceL1a.size=" + dataSourceL1a.size() + ", fake=" + fake + ", " + path);
			//String dirSt = dir.getText().toString();

			return null;
		}

		private ArrayList<LayoutElement> addTo(ArrayList<BaseFile> baseFiles) {
			ArrayList<LayoutElement> a = new ArrayList<>();
			for (int i = 0; i < baseFiles.size(); i++) {
				BaseFile baseFile = baseFiles.get(i);
				//File f = new File(ele.getPath());
				//String size = "";
				if (!DataUtils.hiddenfiles.contains(baseFile.getPath())) {
					if (baseFile.isDirectory()) {
						//size = "";

//                    Bitmap lockBitmap = BitmapFactory.decodeResource(ma.getResources(),
//                            R.drawable.ic_folder_lock_white_36dp);
						//BitmapDrawable lockBitmapDrawable = new BitmapDrawable(ma.getResources(), lockBitmap);

						LayoutElement layoutElements = activity.getFutils()
                            .newElement(//baseFile.getName().endsWith(CryptUtil.CRYPT_EXTENSION) ? lockBitmapDrawable : ma.folder,
							baseFile.getPath(), baseFile.getPermission(), baseFile.getLink(), /*size, */baseFile.f.length(), true, //false,
                            baseFile.getDate());
						layoutElements.setMode(baseFile.getMode());
						a.add(layoutElements);
						folder_count++;
					} else {
						long longSize = 0;
						try {
							if (baseFile.getSize() != -1) {
								longSize = baseFile.getSize();
								//size = Formatter.formatFileSize(c, longSize);
							} else {
								//size = "";
								longSize = 0;
							}
						} catch (NumberFormatException e) {
							//e.printStackTrace();
						}
						try {
							LayoutElement layoutElements = activity.getFutils().newElement(//Icons.loadMimeIcon(baseFile.getPath(), !ma.IS_LIST, ma.res), 
								baseFile.getPath(), baseFile.getPermission(),
                                baseFile.getLink(), /*size, */longSize, false, /*false, */baseFile.getDate());
							layoutElements.setMode(baseFile.getMode());
							a.add(layoutElements);
							file_count++;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			return a;
		}

		protected void onProgressUpdate(String...values) {
			showToast(values[0]);
		}

		protected void onPostExecute(Object result) {
			if (currentPathTitle != null) {
				if (currentPathTitle.startsWith("/")) {
					final File curDir = new File(currentPathTitle);
					diskStatus.setText(
						"Free " + Util.nf.format(curDir.getFreeSpace() / (1 << 20))
						+ " MiB. Used " + Util.nf.format((curDir.getTotalSpace() - curDir.getFreeSpace()) / (1 << 20))
						+ " MiB. Total " + Util.nf.format(curDir.getTotalSpace() / (1 << 20)) + " MiB");
				}
				dataSourceL1.clear();
				Collections.sort(dataSourceL1a, fileListSorter);
				dataSourceL1.addAll(dataSourceL1a);
				dataSourceL1a.clear();
				selectedInList1.clear();
			}
			if (status.getVisibility() == View.GONE) {
				if (selStatus != null) {
					selStatus.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
					selStatus.setVisibility(View.VISIBLE);
				} else {
					selectionStatus1.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
					selectionStatus1.setVisibility(View.VISIBLE);
				}
				horizontalDivider0.setVisibility(View.VISIBLE);
				horizontalDivider12.setVisibility(View.VISIBLE);
				status.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
				status.setVisibility(View.VISIBLE);
			}

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

			if (activity.COPY_PATH == null && activity.MOVE_PATH == null && commands != null && commands.getVisibility() == View.VISIBLE) {
				horizontalDivider.setVisibility(View.GONE);
				commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
				commands.setVisibility(View.GONE);
			} else if ((activity.COPY_PATH != null || activity.MOVE_PATH != null) && commands != null && commands.getVisibility() == View.GONE) {
				horizontalDivider.setVisibility(View.VISIBLE);
				commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
				commands.setVisibility(View.VISIBLE);
				updateDelPaste();
			}

			//Log.d("changeDir dataSourceL1", Util.collectionToString(dataSourceL1, true, "\r\n"));
			listView.setActivated(true);
			srcAdapter.notifyDataSetChanged();
			if (doScroll) {
				gridLayoutManager.scrollToPosition(0);
			}

			if (allCbx.isSelected()) {//}.isChecked()) {
				selectionStatus1.setText(dataSourceL1.size() 
										 + "/" + dataSourceL1.size());
			} else {
				selectionStatus1.setText(selectedInList1.size() 
										 + "/" + dataSourceL1.size());
			}
			Log.d(TAG, "LoadFiles.onPostExecute " + currentPathTitle);

			updateDir(currentPathTitle, ContentFragment.this);
			if (mSwipeRefreshLayout.isRefreshing()) {
				mSwipeRefreshLayout.setRefreshing(false);
			}
			if (dataSourceL1.size() == 0) {
				nofilelayout.setVisibility(View.VISIBLE);
				mSwipeRefreshLayout.setVisibility(View.GONE);
			} else {
				nofilelayout.setVisibility(View.GONE);
				mSwipeRefreshLayout.setVisibility(View.VISIBLE);
			}

		}

		ArrayList<BaseFile> listaudio() {
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

		ArrayList<BaseFile> listImages() {
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

		ArrayList<BaseFile> listVideos() {
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

		ArrayList<BaseFile> listRecentFiles() {
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

		ArrayList<BaseFile> listApks() {
			ArrayList<BaseFile> songs = new ArrayList<>();
			final String[] projection = {MediaStore.Files.FileColumns.DATA};

			Cursor cursor = activity.getContentResolver().query(MediaStore.Files
																.getContentUri("external"), projection,
																null,
																null, null);
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

		ArrayList<BaseFile> listRecent() {
			final HistoryManager history = new HistoryManager(activity, "Table2");
			final ArrayList<String> paths = history.readTable(DataUtils.HISTORY);
			history.end();
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

		ArrayList<BaseFile> listDocs() {
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
					if (path != null && contains(types, path.toLowerCase())) {
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
		 * @param path the path to the directory tree, starts with prefix {@link com.amaze.filemanager.utils.OTGUtil#PREFIX_OTG}
		 *             Independent of URI (or mount point) for the OTG
		 * @return a list of files loaded
		 */
		ArrayList<BaseFile> listOtg(String path) {

			return OTGUtil.getDocumentFilesList(path, activity);
		}

		boolean contains(String[] types, String path) {
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
			if (type == Frag.TYPE.SELECTION) {
				searchMode = true;
			} else {
				setSearchMode(true);
			}
			searchVal = searchET.getText().toString();
			showToast("Searching...");
			dataSourceL1.clear();
			srcAdapter.notifyDataSetChanged();
		}

		@Override
		protected ArrayList<LayoutElement> doInBackground(String... params) {
			Log.d("SearchFileNameTask", "dirTemp4Search " + dirTemp4Search);
			final ArrayList<LayoutElement> tempAppList = new ArrayList<>();
			if (type == Frag.TYPE.SELECTION) {
				final Collection<LayoutElement> c = FileUtil.getFilesBy(tempOriDataSourceL1, params[0], true);
				Log.d(TAG, "getFilesBy " + Util.collectionToString(c, true, "\n"));
				tempAppList.addAll(c);
			} else {
				File file = new File(dirTemp4Search);

				if (file.exists()) {
					Collection<File> c = FileUtil.getFilesBy(file.listFiles(), params[0], true);
					Log.d(TAG, "getFilesBy " + Util.collectionToString(c, true, "\n"));
					for (File le : c) {
						tempAppList.add(new LayoutElement(le));
					}
					//addAllDS1(Util.collectionFile2CollectionString(c));// dataSourceL1.addAll(Util.collectionFile2CollectionString(c));curContentFrag.
					// Log.d("dataSourceL1 new task",
					// Util.collectionToString(dataSourceL1, true, "\n"));
				} else {
					showToast(dirTemp4Search + " is not existed");
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
			selectionStatus1.setText(selectedInList1.size() 
									 + "/" + dataSourceL1.size());
			File file = new File(dirTemp4Search);
			diskStatus.setText(
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

    private void setSearchMode(final boolean search) {
		Log.d(TAG, "setSearchMode " + searchMode + ", " + currentPathTitle + ", " + dirTemp4Search);
		if (search) {
			if (currentPathTitle != null) {
				dirTemp4Search = currentPathTitle;
			}
			currentPathTitle = null;
			searchMode = true;
		} else {
			currentPathTitle = dirTemp4Search;
			searchMode = false;
		}
		Log.d(TAG, "setSearchMode " + searchMode + ", " + currentPathTitle + ", " + dirTemp4Search);
	}

	void setAllCbxChecked(boolean en) {
		allCbx.setSelected(en);
		if (en) {
			allCbx.setImageResource(R.drawable.ic_accept);
		} else {
			allCbx.setImageResource(R.drawable.dot);
		}
	}

	public void mainmenu(final View v) {
		final PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.inflate(R.menu.panel_commands);
		final Menu menu = popup.getMenu();
		if (!activity.multiFiles) {
			menu.findItem(R.id.horizontalDivider5).setVisible(false);
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
        mi = menu.findItem(R.id.hide);
		if (right.getVisibility() == View.VISIBLE) {
			mi.setTitle("Hide");
		} else {
			mi.setTitle("2 panels");
		}
        mi = menu.findItem(R.id.biggerequalpanel);
		if (left.getVisibility() == View.GONE || right.getVisibility() == View.GONE) {
			mi.setEnabled(false);
		} else {
			mi.setEnabled(true);
			if (slidingTabsFragment.width <= 0) {
				mi.setTitle("Wider panel");
			} else {
				mi.setTitle("2 panels equal");
			}
		}
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					Log.d(TAG, item.getTitle() + ".");
					switch (item.getItemId())  {
						case R.id.rangeSelection:
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
							break;
						case R.id.inversion:
							tempSelectedInList1.clear();
							for (LayoutElement f : dataSourceL1) {
								if (!selectedInList1.contains(f)) {
									tempSelectedInList1.add(f);
								}
							}
							selectedInList1.clear();
							selectedInList1.addAll(tempSelectedInList1);
							srcAdapter.notifyDataSetChanged();
							break;
						case R.id.clearSelection:
							tempSelectedInList1.clear();
							tempSelectedInList1.addAll(selectedInList1);
							selectedInList1.clear();
							srcAdapter.notifyDataSetChanged();
							break;
						case R.id.undoClearSelection:
							selectedInList1.clear();
							selectedInList1.addAll(tempSelectedInList1);
							tempSelectedInList1.clear();
							srcAdapter.notifyDataSetChanged();
							break;
						case R.id.swap:
//							if (spanCount == 8) {
//								spanCount = 4;
//							}
//							AndroidUtils.setSharedPreference(getContext(), "SPAN_COUNT", spanCount);
							activity.swap(v);
							break;
						case R.id.hide: 
							if (right.getVisibility() == View.VISIBLE && left.getVisibility() == View.VISIBLE) {
								if (spanCount == 4) {
									spanCount = 8;
									setRecyclerViewLayoutManager();
								}
								if (activity.swap) {
									left.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_left));
									right.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_left));
								} else {
									left.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_in_right));
									right.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_out_right));
								}
								if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT)
									left.setVisibility(View.GONE);
								else
									right.setVisibility(View.GONE);
							} else {
								if (spanCount == 8) {
									spanCount = 4;
									setRecyclerViewLayoutManager();
								}
								if (activity.swap) {
									left.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_left));
									right.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_left));
								} else {
									left.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_in_right));
									right.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_out_right));
								}
								right.setVisibility(View.VISIBLE);
							}
							break;
						case R.id.biggerequalpanel:
							if (activity.leftSize <= 0) {
								if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
									LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)activity.left.getLayoutParams();
									params.weight = 1.0f;
									activity.left.setLayoutParams(params);
									params = (LinearLayout.LayoutParams)activity.right.getLayoutParams();
									params.weight = 2.0f;
									activity.right.setLayoutParams(params);
									activity.leftSize = 1;
									if (left == activity.left) {
										slidingTabsFragment.width = 1;
										//activity.leftSize = width.width;
										activity.slideFrag2.width = -slidingTabsFragment.width;
									} else {
										slidingTabsFragment.width = -1;
										//activity.leftSize = -width.width;
										activity.slideFrag2.width = -slidingTabsFragment.width;
									}
								} else {
									LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)activity.left.getLayoutParams();
									params.weight = 2.0f;
									activity.left.setLayoutParams(params);
									params = (LinearLayout.LayoutParams)activity.right.getLayoutParams();
									params.weight = 1.0f;
									activity.right.setLayoutParams(params);
									activity.leftSize = 1;
									if (left == activity.left) {
										slidingTabsFragment.width = -1;
										//activity.leftSize = -width.width;
										activity.slideFrag.width = -slidingTabsFragment.width;
									} else {
										slidingTabsFragment.width = 1;
										//activity.leftSize = width.width;
										activity.slideFrag.width = -slidingTabsFragment.width;
									}
								}
							} else {
								LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)left.getLayoutParams();
								params.weight = 1.0f;
								left.setLayoutParams(params);
								params = (LinearLayout.LayoutParams)right.getLayoutParams();
								params.weight = 1.0f;
								right.setLayoutParams(params);
								activity.leftSize = 0;
								//width.width = 0;
								activity.slideFrag.width = 0;
								activity.slideFrag2.width = 0;
							}
							activity.curSelectionFrag2.setRecyclerViewLayoutManager();
							activity.curExploreFrag.setRecyclerViewLayoutManager();
							AndroidUtils.setSharedPreference(activity, "biggerequalpanel", activity.leftSize);

					}
					return true;
				}
			});
		popup.show();
	}

//	public boolean copys(final View v) {
//		curDir = path;
//		activity.mode = OpenMode.FILE;
//		activity.copyl.clear();
//		activity.cutl.clear();
//		activity.copyl.addAll(selectedInList1);
//		return true;
//	}

//	public boolean cuts(final View v) {
//		curDir = path;
//		activity.mode = OpenMode.FILE;
//		activity.copyl.clear();
//		activity.cutl.clear();
//		activity.cutl.addAll(selectedInList1);
//		return true;
//	}

	private String curDir = "";
//	public boolean pastes(final View v) {
//		if (activity.copyl.size() > 0) {
//			ArrayList<BaseFile> copies = new ArrayList<>();
//			for (LayoutElements f : activity.copyl) {
//				//File f = new File(st);
//				BaseFile baseFile=new BaseFile(f.getPath(), "", f.lastModified(), f.length(), f.isDirectory());
//				baseFile.setMode(OpenMode.FILE);
//				baseFile.setName(f.getName());
//				copies.add(baseFile);
//			}
//			//MAIN_ACTIVITY.COPY_PATH = copies;
//			//String path = path;
//
//			ArrayList<BaseFile> arrayList;// = new ArrayList<>();
//			arrayList = copies;//COPY_PATH;
//			new CopyFileCheck(this, path, false, activity, BaseActivity.rootMode).executeOnExecutor(AsyncTask
//																									  .THREAD_POOL_EXECUTOR, arrayList);
//		} else if (activity.cutl.size() > 0) {//MOVE_PATH != null) {
//			ArrayList<BaseFile> moves = new ArrayList<>();
//			for (LayoutElements f : activity.cutl) {
//				//File f = new File(st);
//				BaseFile baseFile=new BaseFile(f.getPath(), "", f.lastModified(), f.length(), f.isDirectory());
//				baseFile.setMode(OpenMode.FILE);
//				baseFile.setName(f.getName());
//				moves.add(baseFile);
//			}
//			//MAIN_ACTIVITY.COPY_PATH = copies;
//			//String path = path;
//
//			ArrayList<BaseFile> arrayList;// = new ArrayList<>();
//			arrayList = moves;
//			new CopyFileCheck(this, path, true, activity, BaseActivity.rootMode).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, arrayList);
//			activity.curContentFrag2.dataSourceL1.removeAll(activity.cutl);
//			activity.curContentFrag2.tempOriDataSourceL1.removeAll(activity.cutl);
//			activity.curContentFrag2.selectedInList1.removeAll(activity.cutl);
//			activity.cutl.clear();
//		}
//		activity.curContentFrag2.srcAdapter.notifyDataSetChanged();
//
////		if (copyl.size() > 0) {
////			List<File> l = new LinkedList<>();
////			for (String st : copyl) {
////				l.addAll(FileUtil.getFiles(st, false));
////			}
////			for
////			Futils.moveCopy(getActivity(), false, dir, Util.collection2Array(copyl));
////		} else if (cutl.size() > 0) {
////			Futils.moveCopy(getActivity(), true, dir, Util.collection2Array(cutl));
////		} else {
////			showToast("No file selected");
////		}
//		return true;
//	}

	public boolean renames(final View v) {
		return true;
	}

//	public boolean addScreens(final View v) {
//		activity = (ExplorerActivity) getActivity();
//		for (LayoutElements f : selectedInList1) {
//			AndroidUtils.addShortcut(activity, f.bf.f);
//		}
//		return true;
//	}

//	public boolean deletes(final View v) {
//		//new Futils().deleteFileList(selectedInList1, activity);
//		activity.curContentFrag2.dataSourceL1.removeAll(selectedInList1);
//		activity.curContentFrag2.tempOriDataSourceL1.removeAll(selectedInList1);
//		activity.curContentFrag2.selectedInList1.removeAll(selectedInList1);
//		return true;
//	}

//	public boolean compresss(final View v) {
//		return true;
//	}
//
//	public boolean encrypts(final View v) {
//		return true;
//	}

//	public boolean sends(final View v) {
//		if (selectedInList1.size() > 0) {
//			ArrayList<Uri> uris = new ArrayList<Uri>(selectedInList1.size());
//			Intent send_intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
//			send_intent.setFlags(0x1b080001);
//
//			send_intent.setType("*/*");
//			for(File file : selectedInList1) {
//				uris.add(Uri.fromFile(file));
//			}
//			Log.d(TAG, Util.collectionToString(uris, true, "\n") + ".");
//			send_intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
//			Log.d("send_intent", send_intent + ".");
//			Log.d("send_intent.getExtras()", AndroidUtils.bundleToString(send_intent.getExtras()));
//			Intent createChooser = Intent.createChooser(send_intent, "Send via..");
//			Log.d("createChooser", createChooser + ".");
//			Log.d("createChooser.getExtras()", AndroidUtils.bundleToString(createChooser.getExtras()));
//			// Verify that the intent will resolve to an activity
//			if (createChooser.resolveActivity(getContext().getPackageManager()) != null) {
//				startActivity(createChooser);
//			}
//			startActivity(createChooser);
//		} else {
//			showToast("No file selected");
//		}
//		return true;
//	}

//	public boolean shares(final View v) {
//		if (selectedInList1.size() > 0) {
//
//			ArrayList<LayoutElements> arrayList = new ArrayList<>();
//			for (LayoutElements s : selectedInList1) {
//				arrayList.add(s);
//			}
//			if (selectedInList1.size() > 100)
//				Toast.makeText(getContext(), "Can't share more than 100 files", Toast.LENGTH_SHORT).show();
//			else {
//				final ArrayList<File> lf = new ArrayList<>(selectedInList1.size());
//				for (LayoutElements le : selectedInList1) {
//					lf.add(le.bf.f);
//				}
//				//new Futils().shareFiles(lf, activity, theme1, Color.BLUE);
//				new Futils().shareFiles(lf, activity, activity.getAppTheme(), Color.parseColor(fabSkin));
//			}
//		} else {
//			showToast("No file selected");
//		}
//		return true;
//	}

//	public boolean infos(final View v) {
//		if (selectedInList1.size() > 0) {
//
//			ArrayList<LayoutElements> arrayList = new ArrayList<>();
//			for (LayoutElements s : selectedInList1) {
//				arrayList.add(s);
//			}
//			if (selectedInList1.size() > 100)
//				Toast.makeText(getContext(), "Can't share more than 100 files", Toast.LENGTH_SHORT).show();
//			else {
//				final ArrayList<File> lf = new ArrayList<>(selectedInList1.size());
//				for (LayoutElements le : selectedInList1) {
//					lf.add(le.bf.f);
//				}
//				//new Futils().shareFiles(lf, activity, theme1, Color.BLUE);
//				new Futils().shareFiles(lf, activity, activity.getAppTheme(), Color.parseColor(fabSkin));
//			}
//		} else {
//			showToast("No file selected");
//		}
//		return true;
//	}

//	private class ArrAdapter extends RecyclerAdapter<File, ArrAdapter.ViewHolder> implements OnLongClickListener, OnClickListener {
//
//		private final int backgroundResource;
//
//		private class ViewHolder extends RecyclerView.ViewHolder {
//			private TextView name;
//			private TextView size;
//			private TextView attr;
//			private TextView lastModified;
//			private TextView type;
//			private ImageButton cbx;
//			private ImageView image;
//			private ImageButton more;
//			private View convertedView;
//
//			public ViewHolder(View convertView) {
//				super(convertView);
//				name = (TextView) convertView.findViewById(R.id.name);
//				size = (TextView) convertView.findViewById(R.id.items);
//				attr = (TextView) convertView.findViewById(R.id.attr);
//				lastModified = (TextView) convertView.findViewById(R.id.lastModified);
//				type = (TextView) convertView.findViewById(R.id.type);
//				cbx = (ImageButton) convertView.findViewById(R.id.cbx);
//				image = (ImageView)convertView.findViewById(R.id.icon);
//				more = (ImageButton)convertView.findViewById(R.id.more);
//
//				convertView.setOnClickListener(ArrAdapter.this);
//				cbx.setOnClickListener(ArrAdapter.this);
//				image.setOnClickListener(ArrAdapter.this);
//				more.setOnClickListener(ArrAdapter.this);
//
//				convertView.setOnLongClickListener(ArrAdapter.this);
//				cbx.setOnLongClickListener(ArrAdapter.this);
//				image.setOnLongClickListener(ArrAdapter.this);
//				more.setOnLongClickListener(ArrAdapter.this);
//
//				more.setColorFilter(ExplorerActivity.TEXT_COLOR);
//
//				name.setTextColor(ExplorerActivity.DIR_COLOR);
//				size.setTextColor(ExplorerActivity.TEXT_COLOR);
//				attr.setTextColor(ExplorerActivity.TEXT_COLOR);
//				lastModified.setTextColor(ExplorerActivity.TEXT_COLOR);
//				type.setTextColor(ExplorerActivity.TEXT_COLOR);
//
//				image.setScaleType(ImageView.ScaleType.FIT_CENTER);
//				convertView.setTag(this);
//				this.convertedView = convertView;
//			}
//		}
//
//		public ArrAdapter(ArrayList<File> objects) {
//			super(objects);
//			Log.d(TAG, "ArrAdapter " + objects);
//			int[] attrs = new int[]{R.attr.selectableItemBackground};
//			TypedArray typedArray = getActivity().obtainStyledAttributes(attrs);
//			backgroundResource = typedArray.getResourceId(0, 0);
//			typedArray.recycle();
//		}
//
//		@Override
//		public int getItemViewType(int position) {
//			if (dataSourceL1.size() == 0) {
//				return 0;
//			} else if (spanCount == 1 || (spanCount == 2 && right.getVisibility() == View.GONE)) {
//				return 1;
//			} else if (spanCount == 2) {
//				return 2;
//			} else {
//				return 3;
//			}
//		}
//	
//		@Override
//		public ArrAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
//														int viewType) {
//			View v;
//			if (viewType <= 1) {
//				v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
//			} else {
//				if (viewType == 2) {
//					v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_small, parent, false);
//				} else {
//					v = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
//				}
//			}
//			// set the view's size, margins, paddings and layout parameters
//			final ViewHolder vh = new ViewHolder(v);
//			return vh;
//		}
//
//		// Replace the contents of a view (invoked by the layout manager)
//		@Override
//		public void onBindViewHolder(ViewHolder holder, int position) {
//			final File f = mDataset.get(position);
//			//Log.d(TAG, "getView " + fileName);
//
//			final String fPath = f.getAbsolutePath();
//			holder.name.setText(f.getName());
//			holder.image.setContentDescription(fPath);
//			holder.name.setContentDescription(fPath);
//			holder.size.setContentDescription(fPath);
//			holder.attr.setContentDescription(fPath);
//			holder.lastModified.setContentDescription(fPath);
//			holder.type.setContentDescription(fPath);
//			holder.cbx.setContentDescription(fPath);
//			holder.more.setContentDescription(fPath);
//			holder.more.setTag(holder);
//			holder.convertedView.setContentDescription(fPath);
//
//			if (dir == null || dir.length() > 0) {
//				holder.name.setEllipsize(TextUtils.TruncateAt.MIDDLE);
//			} else {
//				holder.name.setEllipsize(TextUtils.TruncateAt.START);
//			}
//
////	        if (!f.exists()) {
////				dataSourceL1.remove(f);
////				selectedInList1.remove(f);
////				notifyItemRemoved(position);
////				return;//convertView;
////			}
//
//			boolean inDataSource2 = false;
//			boolean isPartial = false;
//			//Log.d(TAG, "dataSource2" + Util.collectionToString(dataSourceL2, true, "\n"));
//			if (multiFiles && dataSourceL2 != null) {
//				final String fPathD = fPath + "/";
//				String f2Path;
//				for (File f2 : dataSourceL2) {
//					f2Path = f2.getAbsolutePath();
//					if (f2.equals(f) || fPath.startsWith(f2Path + "/")) {
//						inDataSource2 = true;
//						break;
//					} else if (f2Path.startsWith(fPathD)) {
//						isPartial = true;
//					}
//				}
//			}
////			if (activity.theme1 == 1) {
////				holder.convertedView.setBackgroundResource(R.drawable.safr_ripple_white);
////			} else {
////				holder.convertedView.setBackgroundResource(R.drawable.safr_ripple_black);
////			}
//			//Log.d(TAG, "inDataSource2 " + inDataSource2 + ", " + dir);
//			//Log.d("f.getAbsolutePath()", f.getAbsolutePath());
//			//Log.d("curSelectedFiles", curSelectedFiles.toString());
//			if (inDataSource2) {
//				holder.convertedView.setBackgroundColor(ExplorerActivity.IN_DATA_SOURCE_2);
//				holder.cbx.setImageResource(R.drawable.ic_accept);
//				holder.cbx.setSelected(true);
//				holder.cbx.setEnabled(false);
//				if ((dir == null || dir.length() > 0) && selectedInList1.size() == dataSourceL1.size()) {
//					allCbx.setSelected(true);//.setChecked(true);
//					allCbx.setImageResource(R.drawable.ic_accept);
//				}
//			} else if (selectedInList1.contains(f)) {
//				holder.convertedView.setBackgroundColor(ExplorerActivity.SELECTED_IN_LIST);
//				holder.cbx.setImageResource(R.drawable.ic_accept);
//				holder.cbx.setSelected(true);
//				holder.cbx.setEnabled(true);
//				if ((dir == null || dir.length() > 0) && selectedInList1.size() == dataSourceL1.size()) {
//					allCbx.setSelected(true);//.setChecked(true);
//					allCbx.setImageResource(R.drawable.ic_accept);
//				}
//			} else if (isPartial) {
//				holder.convertedView.setBackgroundColor(ExplorerActivity.IS_PARTIAL);
//				holder.cbx.setImageResource(R.drawable.ready);
//				holder.cbx.setSelected(false);
//				holder.cbx.setEnabled(true);
//				allCbx.setSelected(false);//.setChecked(false);
//				if (selectedInList1.size() == 0) {
//					allCbx.setImageResource(R.drawable.dot);
//				} else {
//					allCbx.setImageResource(R.drawable.ready);
//				}
//	        } else {
//				holder.convertedView.setBackgroundResource(backgroundResource);
//				if (selectedInList1.size() > 0) {
//					holder.cbx.setImageResource(R.drawable.ready);
//					allCbx.setImageResource(R.drawable.ready);
//				} else {
//					holder.cbx.setImageResource(R.drawable.dot);
//					allCbx.setImageResource(R.drawable.dot);
//				}
//				holder.cbx.setSelected(false);
//				holder.cbx.setEnabled(true);
//				allCbx.setSelected(false);
//				
//	        }
//			if (tempPreviewL2 != null && tempPreviewL2.equals(f)) {
//				holder.convertedView.setBackgroundColor(ExplorerActivity.LIGHT_GREY);
//			}
//
//			final boolean canRead = f.canRead();
//			final boolean canWrite = f.canWrite();
//			if (!f.isDirectory()) {
//				long length = f.length();
//				holder.size.setText(Util.nf.format(length) + " B");
//				String st;
//				if (canWrite) {
//					st = "-rw";
//				} else if (canRead) {
//					st = "-r-";
//				} else {
//					st = "---";
//					holder.cbx.setEnabled(false);
//				}
//				final String namef = f.getName();
//				final int lastIndexOf = namef.lastIndexOf(".");
//				holder.type.setText(lastIndexOf >= 0 && lastIndexOf < namef.length() - 1 ? namef.substring(lastIndexOf + 1) : "");
//				holder.attr.setText(st);
//				holder.lastModified.setText(Util.dtf.format(f.lastModified()));
//			} else {
//				final String[] list = f.list();
//				int length = list == null ? 0 : list.length;
//				holder.size.setText(Util.nf.format(length) + " item");
//				final String st;
//				if (canWrite) {
//					st = "drw";
//				} else if (canRead) {
//					st = "dr-";
//				} else {
//					st = "d--";
//					holder.cbx.setEnabled(false);
//				}
//				holder.type.setText("Folder");
//				holder.attr.setText(st);
//				holder.lastModified.setText(Util.dtf.format(f.lastModified()));
//			}
//			imageLoader.displayImage(f, getContext(), holder.image, spanCount);
//		}
//
//		public boolean copy(final View item) {
//			activity.mode = OpenMode.FILE;
//			activity.copyl.clear();
//			activity.cutl.clear();
//			final ArrayList<File> al = new ArrayList<>(1);
//			al.add(new File((String)item.getContentDescription()));
//			activity.copyl.addAll(al);
//			return true;
//		}
//
//		public boolean cut(final View item) {
//			activity.mode = OpenMode.FILE;
//			activity.copyl.clear();
//			activity.cutl.clear();
//			final ArrayList<File> al = new ArrayList<>(1);
//			al.add(new File((String)item.getContentDescription()));
//			activity.cutl.addAll(al);
//			return true;
//		}
//
//		public boolean rename(final View item) {
//			final File oldF = new File((String)item.getContentDescription());
//			final String oldPath = oldF.getAbsolutePath();
//			Log.d(TAG, "oldPath " + oldPath + ", " + item);
//			final EditText editText = new EditText(getContext());
//			editText.setText(oldF.getName());
//			final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
//				LinearLayout.LayoutParams.MATCH_PARENT,
//				LinearLayout.LayoutParams.WRAP_CONTENT);
//			layoutParams.gravity = Gravity.CENTER;
//			editText.setLayoutParams(layoutParams);
//			editText.setSingleLine(true);
//			editText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//			editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
//			editText.setMinEms(2);
//			//editText.setGravity(Gravity.CENTER);
//			final int density = 8 * (int)getResources().getDisplayMetrics().density;
//			editText.setPadding(density, density, density, density);
//
//			AlertDialog dialog = new AlertDialog.Builder(getContext())
//				.setIconAttribute(android.R.attr.dialogIcon)
//				.setTitle("New Name")
//				.setView(editText)
//				.setPositiveButton(R.string.ok,
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int whichButton) {
//						String name = editText.getText().toString();
//						File newF = new File(oldF.getParent(), name);
//						String newPath = newF.getAbsolutePath();
//						Log.d("newF", newPath);
//						if (newF.exists()) {
//							showToast("\"" + newF + "\" is existing. Please choose another name");
//						} else {
//							boolean ok = AndroidPathUtils.renameFolder(oldF, newF, ContentFragment.this.getContext());//oldF.renameTo(newF);
//							if (ok) {
//								ViewHolder holder = (ViewHolder)item.getTag();
//								TextView nameTV = holder.name;
//								Log.d(TAG, "nameTV " + nameTV);
//								int i = 0;
//								for (File fn : dataSourceL1) {
//									if (fn.equals(oldPath)) {
//										dataSourceL1.set(i, newF);
//									}
//									i++;
//								}
//								srcAdapter.notifyDataSetChanged();
//
//								nameTV.setContentDescription(newPath);
//								holder.size.setContentDescription(newPath);
//								holder.attr.setContentDescription(newPath);
//								holder.lastModified.setContentDescription(newPath);
//								holder.type.setContentDescription(newPath);
//								holder.cbx.setContentDescription(newPath);
//								holder.image.setContentDescription(newPath);
//								item.setContentDescription(newPath);
//								holder.convertedView.setContentDescription(newPath);
//								showToast("Rename successfully");
//							} else {
//								showToast("Rename unsuccessfully");
//							}
//							dialog.dismiss();
//						}
//					}
//				})
//				.setNegativeButton(R.string.cancel,
//				new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int whichButton) {
//						dialog.dismiss();
//					}
//				}).create();
//			dialog.show();
//			return true;
//		}
//
//		public boolean delete(final View item) {
//			ArrayList<String> arr = new ArrayList<>();
//			arr.add((String)item.getContentDescription());
//			//new Futils().deleteFiles(arr, activity);
//			return true;
//		}
//
//		public boolean share(View item) {
//			ArrayList<File> arrayList = new ArrayList<File>();
//			arrayList.add(new File((String)item.getContentDescription()));
//			new Futils().shareFiles(arrayList, activity, theme1, Color.BLUE);
//			return true;
//		}
//
//		public boolean send(View item) {
//			File f = new File((String)item.getContentDescription());
//			Uri uri = Uri.fromFile(f);
//			Intent i = new Intent(Intent.ACTION_SEND);
//			i.setFlags(0x1b080001);
//
//			i.setData(uri);
//			Log.d("i.setData(uri)", uri + "." + i);
//			String mimeType = MimeTypes.getMimeType(f);
//			i.setDataAndType(uri, mimeType);  //floor.getValue()
//			Log.d(TAG, f + " = " + mimeType);
//
//			Log.d("send", i + ".");
//			Log.d("send.getExtras()", AndroidUtils.bundleToString(i.getExtras()));
//			Intent createChooser = Intent.createChooser(i, "Send via..");
//			Log.d("createChooser", createChooser + ".");
//			Log.d("createChooser.getExtras()", AndroidUtils.bundleToString(createChooser.getExtras()));
//			startActivity(createChooser);
//			return true;
//		}
//
//		public boolean copyName(final View item) {
//			final String data = new File((String)item.getContentDescription()).getName();
//			AndroidUtils.copyToClipboard(getContext(), data);
//			return true;
//		}
//
//		public boolean copyPath(final View item) {
//			final String data = new File((String)item.getContentDescription()).getParent();
//			AndroidUtils.copyToClipboard(getContext(), data);
//			return true;
//		}
//
//		public boolean copyFullName(final View item) {
//			final String data = (String) item.getContentDescription();
//			AndroidUtils.copyToClipboard(getContext(), data);
//			return true;
//		}
//
//		public void onClick(final View v) {
//			activity.slideFrag2.getCurrentFragment2().select(false);
//			if (v.getId() == R.id.more) {
//
//				final MenuBuilder menuBuilder = new MenuBuilder(activity);
//				final MenuInflater inflater = new MenuInflater(activity);
//				inflater.inflate(R.menu.file_commands, menuBuilder);
//				final MenuPopupHelper optionsMenu = new MenuPopupHelper(activity , menuBuilder, allSize);
//				optionsMenu.setForceShowIcon(true);
//
//				menuBuilder.setCallback(new MenuBuilder.Callback() {
//						@Override
//						public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
//							switch (item.getItemId()) {
//								case R.id.copy:
//									copy(v);
//									break;
//								case R.id.cut:
//									cut(v);
//									break;
//								case R.id.rename:
//									rename(v);
//									break;
//								case R.id.delete:
//									delete(v);
//									break;
//								case R.id.share:
//									share(v);
//									break;
//								case R.id.send:
//									send(v);
//									break;
//								case R.id.name:
//									copyName(v);
//									break;
//								case R.id.path:
//									copyPath(v);
//									break;
//								case R.id.fullname:
//									copyFullName(v);
//									break;
//							}
//							return false ;
//						}
//						@Override
//						public void onMenuModeChange(MenuBuilder menu) {}
//					});
//				optionsMenu.show();
//				return;
//			}
//			final String fPath = (String) v.getContentDescription();
//			Log.d(TAG, "onClick, " + fPath + ", " + v);
//			if (fPath == null) {
//				return;
//			}
//			final File f = new File(fPath);
//			//Log.d(TAG, "currentSelectedList " + Util.collectionToString(selectedInList1, true, "\r\n"));
//			//Log.d(TAG, "selectedInList.contains(f) " + selectedInList1.contains(f));
//			//Log.d(TAG, "multiFiles " + multiFiles);
//			//Log.d(TAG, "f.exists() " + f.exists());
//			if (f.exists()) {
//				if (!f.canRead()) {
//					showToast(f + " cannot be read");
//				} else {
//					boolean inSelected = false;
//					if (dataSourceL2 != null)
//						for (File st : dataSourceL2) {
//							if (f.equals(st) || fPath.startsWith(st.getAbsolutePath() + "/")) {
//								inSelected = true;
//								break;
//							}
//						}
//					if (!inSelected) {
//						if (multiFiles || suffix.length() == 0) {
//							if (v.getId() == R.id.icon) {
//								tempPreviewL2 = f;
//								if (f.isFile()) {
//									load(f, fPath);
//								} else {
//									activity.slideFrag2.mViewPager.setCurrentItem(Frag.TYPE.EXPLORER.ordinal(), true);
//									activity.curExploreFrag.changeDir(f, true);
//								}
//								if (selectedInList1.size() > 0) {
//									if (selectedInList1.contains(f)) {
//										selectedInList1.remove(f);
//										if (selectedInList1.size() == 0 && activity.copyl.size() == 0 && activity.cutl.size() == 0 && commands.getVisibility() == View.VISIBLE) {
//											horizontalDivider.setVisibility(View.GONE);
//											commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
//											commands.setVisibility(View.GONE);
//										}
//									} else {
//										selectedInList1.add(f);
//									}
//								}
//							} else if (v.getId() == R.id.cbx) {//file and folder
//								if (selectedInList1.contains(f)) {
//									selectedInList1.remove(f);
//									if (selectedInList1.size() == 0 && activity.copyl.size() == 0 && activity.cutl.size() == 0 && commands.getVisibility() == View.VISIBLE) {
//										horizontalDivider.setVisibility(View.GONE);
//										commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
//										commands.setVisibility(View.GONE);
//									}
//								} else {
//									selectedInList1.add(f);
//									if (commands.getVisibility() == View.GONE) {
//										commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
//										commands.setVisibility(View.VISIBLE);
//										horizontalDivider.setVisibility(View.VISIBLE);
//									}
//								}
//							} else if (f.isDirectory()) { 
//								if (selectedInList1.size() == 0) { 
//									changeDir(f, true);
//								} else {
//									if (selectedInList1.contains(f)) {
//										selectedInList1.remove(f);
//										if (selectedInList1.size() == 0 && activity.copyl.size() == 0 && activity.cutl.size() == 0 && commands.getVisibility() == View.VISIBLE) {
//											horizontalDivider.setVisibility(View.GONE);
//											commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
//											commands.setVisibility(View.GONE);
//										} 
//									} else {
//										selectedInList1.add(f);
//										if (commands.getVisibility() == View.GONE) {
//											commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
//											commands.setVisibility(View.VISIBLE);
//											horizontalDivider.setVisibility(View.VISIBLE);
//										}
//									}
//								}
//							} else if (f.isFile()) { 
//								if (selectedInList1.size() == 0) { 
//									openFile(f);
//								} else {
//									if (selectedInList1.contains(f)) {
//										selectedInList1.remove(f);
//										if (selectedInList1.size() == 0 && activity.copyl.size() == 0 && activity.cutl.size() == 0 && commands.getVisibility() == View.VISIBLE) {
//											horizontalDivider.setVisibility(View.GONE);
//											commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
//											commands.setVisibility(View.GONE);
//										} 
//									} else {
//										selectedInList1.add(f);
//										if (commands.getVisibility() == View.GONE) {
//											commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
//											commands.setVisibility(View.VISIBLE);
//											horizontalDivider.setVisibility(View.VISIBLE);
//										}
//									}
//								}
//							}
//							if ((dir == null || dir.length() > 0)) {
//								selectionStatus1.setText(selectedInList1.size() 
//														 + "/" + dataSourceL1.size());
//							}
//						} else { //!multifile no preview
//							if (f.isFile()) {
//								// chọn mới đầu tiên
//								if (v.getId() == R.id.cbx) {
//									if (selectedInList1.size() == 0) {
//										selectedInList1.add(f);
//										if (commands.getVisibility() == View.GONE) {
//											commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
//											commands.setVisibility(View.VISIBLE);
//											horizontalDivider.setVisibility(View.VISIBLE);
//										}
//									} else if (selectedInList1.size() > 0) {
//										if (selectedInList1.contains(f)) { // đã chọn
//											selectedInList1.clear();
//											if (selectedInList1.size() == 0 && activity.copyl.size() == 0 && activity.cutl.size() == 0 && commands.getVisibility() == View.VISIBLE) {
//												horizontalDivider.setVisibility(View.GONE);
//												commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
//												commands.setVisibility(View.GONE);
//											}
//										} else { // chọn mới bỏ cũ
//											selectedInList1.clear();
//											selectedInList1.add(f);
//										}
//									}
//								} else {
//									openFile(f);
//								}
//							} else { //", "Directory
//								selectedInList1.clear();
//								if (dir == null || dir.length() > 0) {
//									changeDir(f, true);
//								}
//							}
//						}
//						notifyDataSetChanged();
//					} else { // inselected
//						if (f.isFile()) {
//							if (v.getId() == R.id.icon) {
//								load(f, fPath);
//							} else {
//								openFile(f);
//							}
//						} else if (v.getId() == R.id.icon) { //dir
//							tempPreviewL2 = f;
//							activity.slideFrag2.mViewPager.setCurrentItem(Frag.TYPE.EXPLORER.ordinal(), true);
//							activity.curExploreFrag.changeDir(f, true);
//						} 
//					}
//				}
//			} else {
//				changeDir(f.getParentFile(), true);
//			}
//			updateDelPaste();
//		}
//		
//		private void load(final File f, final String fPath) throws IllegalStateException {
//			final String mime = MimeTypes.getMimeType(f);
//			Log.d(TAG, fPath + "=" + mime);
//			int i = 0;
//			SlidingTabsFragment2.PagerAdapter pagerAdapter = activity.slideFrag2.pagerAdapter;
//			if (mime.startsWith("text/html") || mime.startsWith("text/xhtml")) {
//				pagerAdapter.getItem(i = Frag.TYPE.TEXT.ordinal()).load(fPath);
//				pagerAdapter.getItem(i = Frag.TYPE.WEB.ordinal()).load(fPath);
//			} else if (mime.startsWith("application/vnd.android.package-archive")) {
//				final StringBuilder sb = new StringBuilder(ExplorerActivity.DOCTYPE);
//				try {
//					ApkParser apkParser = new ApkParser(f);
//					sb.append(AndroidUtils.getSignature(getContext(), fPath));
//					sb.append("\nVerify apk " + apkParser.verifyApk());
//					sb.append("\nMeta data " + apkParser.getApkMeta());
//					
//					String sb1 = sb.toString();
//					
//					String sb2 = "\nAndroidManifest.xml \n" + apkParser.getManifestXml().replaceAll("&", "&amp;")
//						.replaceAll("\"", "&quot;")
//						.replaceAll("'", "&#39;")
//						.replaceAll("<", "&lt;")
//						.replaceAll(">", "&gt;");
//					sb.append(sb2);
//					sb.append(ExplorerActivity.END_PRE);
//					final String name = ExplorerApplication.PRIVATE_PATH + "/" + f.getName() + ".html";
//					FileUtil.writeFileAsCharset(new File(name), sb.toString(), "utf-8");
//					pagerAdapter.getItem(i = Frag.TYPE.WEB.ordinal()).load(name);
//					byte[] bytes = FileUtil.readFileToMemory(f);
//					new FillClassesNamesThread(activity, bytes, f, sb1, sb2, ExplorerActivity.END_PRE).start();
//				} catch (Throwable e) {
//					e.printStackTrace();
//				}
//			} else if (mime.startsWith("application/pdf")) {
//				pagerAdapter.getItem(i = Frag.TYPE.PDF.ordinal()).load(fPath);
//			} else if (mime.startsWith("image/svg+xml")) {
//				pagerAdapter.getItem(i = Frag.TYPE.TEXT.ordinal()).load(fPath);
//				pagerAdapter.getItem(i = Frag.TYPE.PHOTO.ordinal()).load(fPath);
//			} else if (mime.startsWith("text")) {
//				pagerAdapter.getItem(i = Frag.TYPE.TEXT.ordinal()).load(fPath);
//			} else if (mime.startsWith("video")) {
//				pagerAdapter.getItem(i = Frag.TYPE.MEDIA.ordinal()).load(fPath);
//			} else if (mime.startsWith("image")) {
//				pagerAdapter.getItem(i = Frag.TYPE.PHOTO.ordinal()).load(fPath);
//			} else if (mime.startsWith("audio")) {
//				pagerAdapter.getItem(i = Frag.TYPE.MEDIA.ordinal()).load(fPath);
//			} else {
//				tempPreviewL2 = null;
//			}
//			activity.slideFrag2.mViewPager.setCurrentItem(i, true);
//		}
//
//		private void openFile(final File f) {
//			try {
//				final Uri uri = Uri.fromFile(f);
//				final Intent i = new Intent(Intent.ACTION_VIEW); 
//				i.addCategory(Intent.CATEGORY_DEFAULT);
//				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
//				i.setData(uri);
//				Log.d("i.setData(uri)", uri + "." + i);
//				final String mimeType = MimeTypes.getMimeType(f);
//				i.setDataAndType(uri, mimeType);//floor.getValue()
//				Log.d(TAG, f + "=" + mimeType);
//				final Intent createChooser = Intent.createChooser(i, "View");
//				Log.d("createChooser.getExtras()", AndroidUtils.bundleToString(createChooser.getExtras()));
//				startActivity(createChooser);
//			} catch (Throwable e) {
//				Toast.makeText(getContext(), "unable to view !\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
//			}
//		}
//
//		public boolean onLongClick(final View v) {
//			activity.slideFrag2.getCurrentFragment2().select(false);
//			final String fPath = (String) v.getContentDescription();
//			final File f = new File(fPath);
//
//			if (!f.exists()) {
//				changeDir(f, true);
//				return true;
//			} else if (!f.canRead()) {
//				showToast(f + " cannot be read");
//				return true;
//			}
//			Log.d(TAG, "onLongClick, " + fPath);
//			Log.d(TAG, "currentSelectedList" + Util.collectionToString(selectedInList1, true, "\r\n"));
//			Log.d(TAG, "selectedInList.contains(f) " + selectedInList1.contains(f));
//			Log.d(TAG, "multiFiles " + multiFiles);
//
//			boolean inSelectedFiles = false;
//			if (dataSourceL2 != null)
//				for (File st : dataSourceL2) {
//					if (f.equals(st) || fPath.startsWith(st.getAbsolutePath() + "/")) {
//						inSelectedFiles = true;
//						break;
//					}
//				}
//			if (!inSelectedFiles) {
//				if (multiFiles || suffix.length() == 0) {
//					if (selectedInList1.contains(f)) {
//						selectedInList1.remove(f);
//						if (selectedInList1.size() == 0 && activity.copyl.size() == 0 && activity.cutl.size() == 0 && commands.getVisibility() == View.VISIBLE) {
//							horizontalDivider.setVisibility(View.GONE);
//							commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
//							commands.setVisibility(View.GONE);
//						} 
//					} else {
//						selectedInList1.add(f);
//						if (commands.getVisibility() == View.GONE) {
//							commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
//							commands.setVisibility(View.VISIBLE);
//							horizontalDivider.setVisibility(View.VISIBLE);
//						}
//					}
//					if ((dir == null || dir.length() > 0)) {
//						selectionStatus1.setText(selectedInList1.size() 
//												 + "/" + dataSourceL1.size());
//					}
//				} else { // single file
//					if (f.isFile()) {
//						// chọn mới đầu tiên
//						if (selectedInList1.size() == 0) {
//							selectedInList1.add(f);
//							if (commands.getVisibility() == View.GONE) {
//								commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
//								commands.setVisibility(View.VISIBLE);
//								horizontalDivider.setVisibility(View.VISIBLE);
//							}
//						} else if (selectedInList1.size() > 0) {
//							if (selectedInList1.contains(f)) {
//								// đã chọn
//								selectedInList1.clear();
//								if (selectedInList1.size() == 0 && activity.copyl.size() == 0 && activity.cutl.size() == 0 && commands.getVisibility() == View.VISIBLE) {
//									horizontalDivider.setVisibility(View.GONE);
//									commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
//									commands.setVisibility(View.GONE);
//								} 
//							} else {
//								// chọn mới bỏ cũ
//								selectedInList1.clear();
//								selectedInList1.add(f);
//							}
//						}
//					} else { //", "Directory
//						selectedInList1.clear();
//						if (dir == null || dir.length() > 0) {
//							changeDir(f, true);
//						}
//					}
//				}
//				notifyDataSetChanged();
//			} 
//			updateDelPaste();
//			return true;
//		}
//		
//
//		/**
//		 * Queries database to map path and password.
//		 * Starts the encryption process after database query
//		 * @param path the path of file to encrypt
//		 * @param password the password in plaintext
//		 */
//		private void startEncryption(final String path, final String password, Intent intent) throws Exception {
//
//			CryptHandler cryptHandler = new CryptHandler(activity);
//			EncryptedEntry encryptedEntry = new EncryptedEntry(path.concat(CryptUtil.CRYPT_EXTENSION),
//															   password);
//			cryptHandler.addEntry(encryptedEntry);
//
//			// start the encryption process
//			ServiceWatcherUtil.runService(activity, intent);
//		}
//
//		public interface EncryptButtonCallbackInterface {
//
//			/**
//			 * Callback fired when we've just gone through warning dialog before encryption
//			 * @param intent
//			 * @throws Exception
//			 */
//			void onButtonPressed(Intent intent) throws Exception;
//
//			/**
//			 * Callback fired when user has entered a password for encryption
//			 * Not called when we've a master password set or enable fingerprint authentication
//			 * @param intent
//			 * @param password the password entered by user
//			 * @throws Exception
//			 */
//			void onButtonPressed(Intent intent, String password) throws Exception;
//		}
//
//		public interface DecryptButtonCallbackInterface {
//			/**
//			 * Callback fired when we've confirmed the password matches the database
//			 * @param intent
//			 */
//			void confirm(Intent intent);
//
//			/**
//			 * Callback fired when password doesn't match the value entered by user
//			 */
//			void failed();
//		}
//
//		
//	}
}
