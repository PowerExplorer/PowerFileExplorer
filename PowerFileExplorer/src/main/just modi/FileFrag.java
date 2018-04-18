package net.gnu.explorer;

import java.util.ArrayList;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbException;
import com.amaze.filemanager.ui.LayoutElement;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.format.Formatter;
import com.amaze.filemanager.filesystem.BaseFile;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.support.v7.widget.RecyclerView;

import com.amaze.filemanager.utils.OpenMode;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import com.amaze.filemanager.utils.DataUtils;
import com.amaze.filemanager.database.CloudHandler;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import android.support.v7.view.ActionMode;
import com.amaze.filemanager.utils.OTGUtil;
import android.os.Build;
import com.amaze.filemanager.utils.ServiceWatcherUtil;
import android.widget.Toast;
import com.amaze.filemanager.database.models.EncryptedEntry;
import android.content.Intent;
import com.amaze.filemanager.utils.provider.UtilitiesProviderInterface;
import com.amaze.filemanager.services.EncryptService;
import android.preference.PreferenceManager;
import android.content.Context;
import com.amaze.filemanager.database.CryptHandler;
import android.graphics.drawable.BitmapDrawable;
import android.content.res.Resources;
import android.graphics.Color;
import com.amaze.filemanager.utils.color.ColorUsage;
import android.content.ClipData;
import android.view.MenuItem;
import android.widget.TextView;
import android.view.MenuInflater;
import android.graphics.drawable.ColorDrawable;
import android.view.Menu;
import com.amaze.filemanager.fragments.preference_fragments.Preffrag;
import android.support.v4.app.FragmentManager;
import android.widget.ImageView;
import com.amaze.filemanager.utils.MainActivityHelper;
import com.amaze.filemanager.utils.CloudUtil;
import com.amaze.filemanager.fragments.SearchAsyncHelper;
import com.amaze.filemanager.utils.CryptUtil;
import java.net.MalformedURLException;
import com.amaze.filemanager.filesystem.RootHelper;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import com.amaze.filemanager.ui.icons.MimeTypes;
import java.util.List;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import com.amaze.filemanager.filesystem.HFile;
import android.os.AsyncTask;
import com.amaze.filemanager.activities.ThemedActivity;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amaze.filemanager.utils.SmbStreamer.Streamer;
import com.amaze.filemanager.utils.files.Futils;
import com.amaze.filemanager.filesystem.MediaStoreHack;
import android.util.Log;
import android.media.RingtoneManager;
import com.amaze.filemanager.ui.icons.Icons;
import android.widget.ImageButton;
import net.gnu.androidutil.ImageThreadLoader;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;
import java.util.LinkedList;
import java.util.Map;
import android.os.FileObserver;
import java.util.TreeMap;
import android.support.v7.widget.GridLayoutManager;
import net.gnu.util.Util;
import android.view.Gravity;
import net.gnu.androidutil.AndroidUtils;
import android.view.View.OnLongClickListener;
import android.graphics.Typeface;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.TypedValue;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import com.cloudrail.si.interfaces.CloudStorage;
import com.amaze.filemanager.exceptions.CloudPluginException;
import com.amaze.filemanager.filesystem.RootHelper;
import com.amaze.filemanager.exceptions.RootNotPermittedException;
import com.amaze.filemanager.fragments.CloudSheetFragment;
import android.provider.MediaStore;
import android.database.Cursor;
import java.util.Arrays;
import net.gnu.util.FileUtil;
import java.util.Collections;
import com.amaze.filemanager.utils.HistoryManager;
import java.util.Comparator;
import java.util.Calendar;
import java.util.Date;
import android.widget.Button;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import com.amaze.filemanager.utils.color.ColorPreference;
import android.view.inputmethod.InputMethodManager;

public abstract class FileFrag extends Frag {

	private static final String TAG = "FileFrag";

	public boolean GO_BACK_ITEM, SHOW_THUMBS, COLORISE_ICONS, SHOW_DIVIDERS;

	public SwipeRefreshLayout mSwipeRefreshLayout;
    private DisplayMetrics displayMetrics;
	//public ArrayList<LayoutElements> LIST_ELEMENTS;
//    public com.amaze.filemanager.adapters.RecyclerAdapter adapter;
	public ActionMode mActionMode;
    //public SharedPreferences sharedPref;
	//public RecyclerView listView;
	//public com.amaze.filemanager.adapters.RecyclerAdapter adapter;
	//LoadList loadList;
	private View nofilesview;
//	public OpenMode openMode = OpenMode.FILE;
	public BitmapDrawable folder, apk, DARK_IMAGE, DARK_VIDEO;
	public Resources res;
	public boolean IS_LIST = true;

	public String iconskin;
    public float[] color;
    public int skin_color;
    public int skinTwoColor;
    public int icon_skin_color;
	private boolean addheader = false;
	private boolean stopAnims = true;
	public String home;//, currentPathTitle = "";//

	//from ContentFragment
	int spanCount = 1;
	ImageButton allCbx;
	ImageButton icons;
	TextView allName;
	TextView allDate;
	TextView allSize;
	TextView allType;
	protected View selStatus;
	TextView selectionStatus1;
	protected View horizontalDivider0;
	protected View horizontalDivider12;
	protected View horizontalDivider7;

	ImageButton searchButton;
	ImageButton clearButton;
	EditText searchET;
	ViewFlipper topflipper;
	boolean searchMode = false;
	String searchVal = "";
	LinearLayout quickLayout;

	View nofilelayout;
	ImageView noFileImage;
	TextView noFileText;

	RecyclerView listView = null;
	ArrAdapter srcAdapter;
	ImageThreadLoader imageLoader;

//	SearchFileNameTask searchTask = null;
	GridLayoutManager gridLayoutManager;
	TextView diskStatus;
	GridDividerItemDecoration dividerItemDecoration;
	//	ViewGroup commands;
//	View horizontalDivider;
//	Button deletePastes;
	long lastScroll = System.currentTimeMillis();
	protected InputMethodManager imm;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		//Log.d(TAG, "onCreate " + savedInstanceState);
		super.onCreate(savedInstanceState);
		res = getResources();
		folder = new BitmapDrawable(res, BitmapFactory.decodeResource(res, R.drawable.ic_grid_folder_new));
		ColorPreference colorPreference = activity.getColorPreference();
		fabSkin = colorPreference.getColorAsString(ColorUsage.ACCENT);
        iconskin = colorPreference.getColorAsString(ColorUsage.ICON_SKIN);
        skin_color = colorPreference.getColor(ColorUsage.PRIMARY);
        skinTwoColor = colorPreference.getColor(ColorUsage.PRIMARY_TWO);
        icon_skin_color = Color.parseColor(iconskin);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//Log.d(TAG, "onCreateView " + savedInstanceState);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

    @Override
	public void onViewCreated(View v, Bundle savedInstanceState) {
		//Log.d(TAG, "onViewCreated " + savedInstanceState);
		super.onViewCreated(v, savedInstanceState);
		nofilelayout = v.findViewById(R.id.nofilelayout);
		noFileImage = (ImageView)v.findViewById(R.id.image);
		noFileText = (TextView)v.findViewById(R.id.nofiletext);

		allCbx = (ImageButton) v.findViewById(R.id.allCbx);
		icons = (ImageButton) v.findViewById(R.id.icons);
		allName = (TextView) v.findViewById(R.id.allName);
		allDate = (TextView) v.findViewById(R.id.allDate);
		allSize = (TextView) v.findViewById(R.id.allSize);
		allType = (TextView) v.findViewById(R.id.allType);
		selectionStatus1 = (TextView) v.findViewById(R.id.selectionStatus);
		mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);
		horizontalDivider0 = v.findViewById(R.id.horizontalDivider0);
		horizontalDivider12 = v.findViewById(R.id.horizontalDivider12);
		horizontalDivider7 = v.findViewById(R.id.horizontalDivider7);
		diskStatus = (TextView) v.findViewById(R.id.diskStatus);
		selStatus = v.findViewById(R.id.selStatus);
		listView = (RecyclerView) v.findViewById(R.id.listView1);
		imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);

		clearButton = (ImageButton) v.findViewById(R.id.clear);
		searchButton = (ImageButton) v.findViewById(R.id.search);
		searchET = (EditText) v.findViewById(R.id.search_box);
		topflipper = (ViewFlipper) v.findViewById(R.id.flipper_top);
		quickLayout = (LinearLayout) v.findViewById(R.id.quicksearch);
	}

	//public void updateList() {}
	public void changeDir(final String curDir, final boolean doScroll) {}
	@Override
    public void onResume() {
        //Log.d(TAG, "onResume " + currentPathTitle);
		imageLoader = new ImageThreadLoader(activity);
		super.onResume();
	}

	@Override
	public void onStart() {
		//Log.d(TAG, "onStart " + currentPathTitle);
		imageLoader = new ImageThreadLoader(activity);
		super.onStart();
	}
//	public Map<String, Object> onSaveInstanceState() {
//		Map<String, Object> outState = new TreeMap<>();
//		Log.d(TAG, "onSaveInstanceState " + path + ", " + outState);
//		outState.put("path", path);
//
//		outState.put("selectedInList1", selectedInList1);
//		outState.put("dataSourceL1", dataSourceL1);
//		outState.put("searchMode", searchMode);
//		outState.put("searchVal", quicksearch.getText().toString());
//		outState.put("dirTemp4Search", dirTemp4Search);
//
//		outState.put("allCbx.isEnabled", allCbx.isEnabled());
//		outState.put(ExplorerActivity.EXTRA_SUFFIX, suffix);
//		outState.put(ExplorerActivity.EXTRA_MULTI_SELECT, multiFiles);
//
//		int index = gridLayoutManager.findFirstVisibleItemPosition();
//        final View vi = listView1.getChildAt(0); 
//        final int top = (vi == null) ? 0 : vi.getTop();
//		outState.put("index", index);
//		outState.put("top", top);
//		return outState;
//	}

//	void reload(Map<String, Object> savedInstanceState) {
//		Log.d(TAG, "reload " + savedInstanceState + path);
//		path = (String) savedInstanceState.get(ExplorerActivity.EXTRA_DIR_PATH);
//		suffix = (String) savedInstanceState.get(ExplorerActivity.EXTRA_SUFFIX);
//		multiFiles = savedInstanceState.get(ExplorerActivity.EXTRA_MULTI_SELECT);
//		selectedInList1.clear();
//		selectedInList1.addAll((ArrayList<LayoutElements>) savedInstanceState.get("selectedInList1"));
//		dataSourceL1.clear();
//		dataSourceL1.addAll((ArrayList<LayoutElements>) savedInstanceState.get("dataSourceL1"));
//
//		searchMode = savedInstanceState.get("searchMode");
//		searchVal = (String) savedInstanceState.get("searchVal");
//		dirTemp4Search = (String) savedInstanceState.get("dirTemp4Search");
//		//listView1.setSelectionFromTop(savedInstanceState.getInt("index"),
//		//savedInstanceState.getInt("top"));
//		allCbx.setEnabled(savedInstanceState.get("allCbx.isEnabled"));
//		srcAdapter.notifyDataSetChanged();
//
//		setRecyclerViewLayoutManager();
//		gridLayoutManager.scrollToPositionWithOffset(savedInstanceState.get("index"), savedInstanceState.get("top"));
//
//		updateDir(path, this);
//	}

//	void setRecyclerViewLayoutManager() {
//        Log.d(TAG, "setRecyclerViewLayoutManager " + gridLayoutManager);
//		if (listView1 == null) {
//			return;
//		}
//		int scrollPosition = 0, top = 0;
//        // If a layout manager has already been set, get current scroll position.
//        if (gridLayoutManager != null) {
//			scrollPosition = gridLayoutManager.findFirstVisibleItemPosition();
//			final View vi = listView1.getChildAt(0); 
//			top = (vi == null) ? 0 : vi.getTop();
//		}
//		final Context context = getContext();
//		gridLayoutManager = new GridLayoutManager(context, spanCount);
//		listView1.removeItemDecoration(dividerItemDecoration);
//		if (spanCount <= 2) {
//			dividerItemDecoration = new GridDividerItemDecoration(context, true);
//			//dividerItemDecoration = new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST, true, true);
//			listView1.addItemDecoration(dividerItemDecoration);
//		}
//
//		srcAdapter = new ArrAdapter(this, dataSourceL1, activity.leftCommands, activity.horizontalDivider11);
//		listView1.setAdapter(srcAdapter);
//
//		listView1.setLayoutManager(gridLayoutManager);
//		gridLayoutManager.scrollToPositionWithOffset(scrollPosition, top);
//	}

	public abstract void refreshRecyclerViewLayoutManager();

//	void setSearchMode(final boolean search) {
//		Log.d(TAG, "setSearchMode " + searchMode + ", " + path + ", " + dirTemp4Search);
//		if (search) {
//			if (path != null) {
//				dirTemp4Search = path;
//			}
//			path = null;
//			//pagerItem.searchMode = true;
//			searchMode = true;
//		} else {
//			path = dirTemp4Search;
//			//pagerItem.searchMode = false;
//			searchMode = false;
//		}
//		Log.d(TAG, "setSearchMode " + searchMode + ", " + path + ", " + dirTemp4Search);
//	}

	void notifyDataSetChanged() {
		srcAdapter.notifyDataSetChanged();
	}
    /**
     * Loading adapter after getting a list of elements
     * @param bitmap the list of objects for the adapter
     * @param back
     * @param path the path for the adapter
     * @param openMode the type of file being created
     * @param results is the list of elements a result from search
     * @param grid whether to set grid view or list view
     */
//    public void createViews(ArrayList<LayoutElements> bitmap, boolean back, String path, OpenMode
//							openMode, boolean results, boolean grid) {
//        try {
//            if (bitmap != null) {
//                if (GO_BACK_ITEM)
//                    if (!path.equals("/") && (openMode == OpenMode.FILE || openMode == OpenMode.ROOT)
//						&& !path.equals(OTGUtil.PREFIX_OTG + "/")
//						&& !path.equals(CloudHandler.CLOUD_PREFIX_GOOGLE_DRIVE + "/")
//						&& !path.equals(CloudHandler.CLOUD_PREFIX_ONE_DRIVE + "/")
//						&& !path.equals(CloudHandler.CLOUD_PREFIX_BOX + "/")
//						&& !path.equals(CloudHandler.CLOUD_PREFIX_DROPBOX + "/")) {
////                        if (bitmap.size() == 0 || !bitmap.get(0).getSize().equals(goback)) {
////
////                            Bitmap iconBitmap = BitmapFactory.decodeResource(res, R.drawable.ic_arrow_left_white_24dp);
////                            bitmap.add(0,
////									   activity.getFutils().newElement(new BitmapDrawable(res, iconBitmap),
////																	   "..", "", "", goback, 0, false, true, ""));
////                        }
//                    }
//
//                if (bitmap.size() == 0 && !results) {
//                    nofilesview.setVisibility(View.VISIBLE);
//                    listView.setVisibility(View.GONE);
//                    mSwipeRefreshLayout.setEnabled(false);
//                } else {
//                    mSwipeRefreshLayout.setEnabled(true);
//                    nofilesview.setVisibility(View.GONE);
//                    listView.setVisibility(View.VISIBLE);
//
//                }
//                dataSourceL1 = bitmap;
//                if (grid && IS_LIST)
//                    switchToGrid();
//                else if (!grid && !IS_LIST) 
//					switchToList();
//                if (adapter == null)
//					adapter = new ArrAdapter(this, activity, bitmap, activity);
//                else {
//                    adapter.generate(dataSourceL1);
//                }
//                stopAnims = true;
//                this.openMode = openMode;
//                if (openMode != OpenMode.CUSTOM)
//                    DataUtils.addHistoryFile(path);
//                //mSwipeRefreshLayout.setRefreshing(false);
//                try {
//                    listView.setAdapter(adapter);
////                    if (!addheader) {
////                        listView.removeItemDecoration(headersDecor);
////                        listView.removeItemDecoration(dividerItemDecoration);
////                        addheader = true;
////                    }
////                    if (addheader && IS_LIST) {
////                        dividerItemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST, true, SHOW_DIVIDERS);
////                        listView.addItemDecoration(dividerItemDecoration);
////                        headersDecor = new StickyRecyclerHeadersDecoration(adapter);
////                        listView.addItemDecoration(headersDecor);
////                        addheader = false;
////                    }
//                    if (!results) this.results = false;
//                    this.path = path;
////                    if (back) {
////                        if (scrolls.containsKey(currentPathTitle)) {
////                            Bundle b = scrolls.get(currentPathTitle);
////                            if (IS_LIST)
////                                mLayoutManager.scrollToPositionWithOffset(b.getInt("index"), b.getInt("top"));
////                            else
////                                mLayoutManagerGrid.scrollToPositionWithOffset(b.getInt("index"), b.getInt("top"));
////                        }
////                    }
//                    //floatingActionButton.show();
////                    activity.updatePaths(no);
////                    listView.stopScroll();
////                    fastScroller.setRecyclerView(listView, IS_LIST ? 1 : columns);
////                    mToolbarContainer.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
////							@Override
////							public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
////								fastScroller.updateHandlePosition(verticalOffset, 112);
////								//    fastScroller.setPadding(fastScroller.getPaddingLeft(),fastScroller.getTop(),fastScroller.getPaddingRight(),112+verticalOffset);
////								//      fastScroller.updateHandlePosition();
////							}
////						});
////                    fastScroller.registerOnTouchListener(new FastScroller.onTouchListener() {
////							@Override
////							public void onTouch() {
////								if (stopAnims && adapter != null) {
////									stopAnimation();
////									stopAnims = false;
////								}
////							}
////						});
////                    if (buttons.getVisibility() == View.VISIBLE) activity.bbar(this);
//                    //activity.invalidateFab(openMode);
//                } catch (Exception e) {
//                }
//            } else {
//                // list loading cancelled
//                // TODO: Add support for cancelling list loading
//                loadlist(home, true, OpenMode.FILE);
//            }
//        } catch (Exception e) {
//        }
//    }

//    void switchToGrid() {
////        IS_LIST = false;
////
////        ic = new IconHolder(getActivity(), SHOW_THUMBS, !IS_LIST);
////        folder = new BitmapDrawable(res, mFolderBitmap);
////        fixIcons(true);
////
////        if (activity.getAppTheme().equals(AppTheme.LIGHT)) {
////
////            // will always be grid, set alternate white background
////            listView.setBackgroundColor(getResources().getColor(R.color.grid_background_light));
////        }
////
////        if (mLayoutManagerGrid == null)
////            if (columns == -1 || columns == 0)
////                mLayoutManagerGrid = new GridLayoutManager(getActivity(), 3);
////            else
////                mLayoutManagerGrid = new GridLayoutManager(getActivity(), columns);
////        listView.setLayoutManager(mLayoutManagerGrid);
////        adapter = null;
//    }

//    void switchToList() {
////        IS_LIST = true;
////
////        if (activity.getAppTheme().equals(AppTheme.LIGHT)) {
////
////            listView.setBackgroundDrawable(null);
////        }
////
////        ic = new IconHolder(getActivity(), SHOW_THUMBS, !IS_LIST);
////        folder = new BitmapDrawable(res, mFolderBitmap);
////        fixIcons(true);
////        if (mLayoutManager == null)
////            mLayoutManager = new LinearLayoutManager(getActivity());
////        listView.setLayoutManager(mLayoutManager);
////        adapter = null;
//    }
//    public ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
//        private void hideOption(int id, Menu menu) {
//            MenuItem item = menu.findItem(id);
//            item.setVisible(false);
//        }
//
//        private void showOption(int id, Menu menu) {
//            MenuItem item = menu.findItem(id);
//            item.setVisible(true);
//        }
//
//        public void initMenu(Menu menu) {
//            /*menu.findItem(R.id.cpy).setIcon(icons.getCopyDrawable());
//			 menu.findItem(R.id.cut).setIcon(icons.getCutDrawable());
//			 menu.findItem(R.id.delete).setIcon(icons.getDeleteDrawable());
//			 menu.findItem(R.id.all).setIcon(icons.getAllDrawable());*/
//        }
//
//        // called when the action mode is created; startActionMode() was called
//        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//            // Inflate a menu resource providing context menu items
//            MenuInflater inflater = mode.getMenuInflater();
//            actionModeView = getActivity().getLayoutInflater().inflate(R.layout.actionmode, null);
//            mode.setCustomView(actionModeView);
//
//            activity.setPagingEnabled(false);
//            activity.floatingActionButton.hideMenuButton(true);
//
//            // translates the drawable content down
//            // if (activity.isDrawerLocked) activity.translateDrawerList(true);
//
//            // assumes that you have "contexual.xml" menu resources
//            inflater.inflate(R.menu.contextual, menu);
//            initMenu(menu);
//            hideOption(R.id.addshortcut, menu);
//            hideOption(R.id.share, menu);
//            hideOption(R.id.openwith, menu);
//            if (activity.mReturnIntent)
//                showOption(R.id.openmulti, menu);
//            //hideOption(R.id.setringtone,menu);
//            mode.setTitle(getResources().getString(R.string.select));
//
//            activity.updateViews(new ColorDrawable(res.getColor(R.color.holo_dark_action_mode)));
//
//            // do not allow drawer to open when item gets selected
//            if (!activity.isDrawerLocked) {
//
//                activity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNDEFINED,
//														 activity.mDrawerLinear);
//            }
//            return true;
//        }
//
//        // the following method is called each time
//        // the action mode is shown. Always called after
//        // onCreateActionMode, but
//        // may be called multiple times if the mode is invalidated.
//        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//            ArrayList<Integer> positions = adapter.getCheckedItemPositions();
//            TextView textView1 = (TextView) actionModeView.findViewById(R.id.item_count);
//            textView1.setText(positions.size() + "");
//            textView1.setOnClickListener(null);
//            mode.setTitle(positions.size() + "");
//            hideOption(R.id.openmulti, menu);
//            if (openMode == OpenMode.SMB) {
//                hideOption(R.id.addshortcut, menu);
//                hideOption(R.id.openwith, menu);
//                hideOption(R.id.share, menu);
//                hideOption(R.id.compress, menu);
//                return true;
//            }
//            if (activity.mReturnIntent)
//                if (Build.VERSION.SDK_INT >= 16)
//                    showOption(R.id.openmulti, menu);
//            //tv.setText(positions.size());
//            if (!results) {
//                hideOption(R.id.openparent, menu);
//                if (positions.size() == 1) {
//                    showOption(R.id.addshortcut, menu);
//                    showOption(R.id.openwith, menu);
//                    showOption(R.id.share, menu);
//
//                    File x = new File(LIST_ELEMENTS.get(adapter.getCheckedItemPositions().get(0))
//
//									  .getPath());
//
//                    if (x.isDirectory()) {
//                        hideOption(R.id.openwith, menu);
//                        hideOption(R.id.share, menu);
//                        hideOption(R.id.openmulti, menu);
//                    }
//
//                    if (activity.mReturnIntent)
//                        if (Build.VERSION.SDK_INT >= 16)
//                            showOption(R.id.openmulti, menu);
//
//                } else {
//                    try {
//                        showOption(R.id.share, menu);
//                        if (activity.mReturnIntent)
//                            if (Build.VERSION.SDK_INT >= 16) showOption(R.id.openmulti, menu);
//                        for (int c : adapter.getCheckedItemPositions()) {
//                            File x = new File(LIST_ELEMENTS.get(c).getPath());
//                            if (x.isDirectory()) {
//                                hideOption(R.id.share, menu);
//                                hideOption(R.id.openmulti, menu);
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    hideOption(R.id.openwith, menu);
//
//                }
//            } else {
//                if (positions.size() == 1) {
//                    showOption(R.id.addshortcut, menu);
//                    showOption(R.id.openparent, menu);
//                    showOption(R.id.openwith, menu);
//                    showOption(R.id.share, menu);
//
//                    File x = new File(LIST_ELEMENTS.get(adapter.getCheckedItemPositions().get(0))
//
//									  .getPath());
//
//                    if (x.isDirectory()) {
//                        hideOption(R.id.openwith, menu);
//                        hideOption(R.id.share, menu);
//                        hideOption(R.id.openmulti, menu);
//                    }
//                    if (activity.mReturnIntent)
//                        if (Build.VERSION.SDK_INT >= 16)
//                            showOption(R.id.openmulti, menu);
//
//                } else {
//                    hideOption(R.id.openparent, menu);
//
//                    if (activity.mReturnIntent)
//                        if (Build.VERSION.SDK_INT >= 16)
//                            showOption(R.id.openmulti, menu);
//                    try {
//                        for (int c : adapter.getCheckedItemPositions()) {
//                            File x = new File(LIST_ELEMENTS.get(c).getPath());
//                            if (x.isDirectory()) {
//                                hideOption(R.id.share, menu);
//                                hideOption(R.id.openmulti, menu);
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                    hideOption(R.id.openwith, menu);
//
//                }
//            }
//
//            return true; // Return false if nothing is done
//        }
//
//        // called when the user selects a contextual menu item
//        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//            computeScroll();
//            ArrayList<Integer> plist = adapter.getCheckedItemPositions();
//            switch (item.getItemId()) {
//                case R.id.openmulti:
//                    if (Build.VERSION.SDK_INT >= 16) {
//                        Intent intentresult = new Intent();
//                        ArrayList<Uri> resulturis = new ArrayList<>();
//                        for (int k : plist) {
//                            try {
//                                resulturis.add(Uri.fromFile(new File(LIST_ELEMENTS.get(k).getPath())));
//                            } catch (Exception e) {
//
//                            }
//                        }
//                        final ClipData clipData = new ClipData(
//							null, new String[]{"*/*"}, new ClipData.Item(resulturis.get(0)));
//                        for (int i = 1; i < resulturis.size(); i++) {
//                            clipData.addItem(new ClipData.Item(resulturis.get(i)));
//                        }
//                        intentresult.setClipData(clipData);
//                        mode.finish();
//                        getActivity().setResult(getActivity().RESULT_OK, intentresult);
//                        getActivity().finish();
//                    }
//                    return true;
//                case R.id.about:
//                    LayoutElements x;
//                    x = LIST_ELEMENTS.get((plist.get(0)));
//                    activity.getFutils().showProps((x).generateBaseFile(), x.getPermissions(), ma, BaseActivity.rootMode, activity.getAppTheme());
//                    /*PropertiesSheet propertiesSheet = new PropertiesSheet();
//					 Bundle arguments = new Bundle();
//					 arguments.putParcelable(PropertiesSheet.KEY_FILE, x.generateBaseFile());
//					 arguments.putString(PropertiesSheet.KEY_PERMISSION, x.getPermissions());
//					 arguments.putBoolean(PropertiesSheet.KEY_ROOT, BaseActivity.rootMode);
//					 propertiesSheet.setArguments(arguments);
//					 propertiesSheet.show(getFragmentManager(), PropertiesSheet.TAG_FRAGMENT);*/
//                    mode.finish();
//                    return true;
//					/*case R.id.setringtone:
//					 File fx;
//					 if(results)
//					 fx=new File(slist.get((plist.get(0))).getPath());
//					 else
//					 fx=new File(list.get((plist.get(0))).getPath());
//
//					 ContentValues values = new ContentValues();
//					 values.put(MediaStore.MediaColumns.DATA, fx.getAbsolutePath());
//					 values.put(MediaStore.MediaColumns.TITLE, "Amaze");
//					 values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
//					 //values.put(MediaStore.MediaColumns.SIZE, fx.);
//					 values.put(MediaStore.Audio.Media.ARTIST, R.string.app_name);
//					 values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
//					 values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
//					 values.put(MediaStore.Audio.Media.IS_ALARM, false);
//					 values.put(MediaStore.Audio.Media.IS_MUSIC, false);
//
//					 Uri uri = MediaStore.Audio.Media.getContentUriForPath(fx.getAbsolutePath());
//					 Uri newUri = getActivity().getContentResolver().insert(uri, values);
//					 try {
//					 RingtoneManager.setActualDefaultRingtoneUri(getActivity(), RingtoneManager.TYPE_RINGTONE, newUri);
//					 //Settings.System.putString(getActivity().getContentResolver(), Settings.System.RINGTONE, newUri.toString());
//					 Toast.makeText(getActivity(), "Successful" + fx.getAbsolutePath(), Toast.LENGTH_LONG).show();
//					 } catch (Throwable t) {
//
//					 Log.d("ringtone", "failed");
//					 }
//					 return true;*/
//                case R.id.delete:
//                    activity.getFutils().deleteFiles(LIST_ELEMENTS, ma, plist, activity.getAppTheme());
//                    return true;
//                case R.id.share:
//                    ArrayList<File> arrayList = new ArrayList<>();
//                    for (int i : plist) {
//                        arrayList.add(new File(LIST_ELEMENTS.get(i).getPath()));
//                    }
//                    if (arrayList.size() > 100)
//                        Toast.makeText(getActivity(), getResources().getString(R.string.share_limit),
//									   Toast.LENGTH_SHORT).show();
//                    else {
//
//                        switch (LIST_ELEMENTS.get(0).getMode()) {
//                            case DROPBOX:
//                            case BOX:
//                            case GDRIVE:
//                            case ONEDRIVE:
//                                activity.getFutils().shareCloudFile(LIST_ELEMENTS.get(0).getPath(),
//																	LIST_ELEMENTS.get(0).getMode(), getContext());
//                                break;
//                            default:
//                                activity.getFutils().shareFiles(arrayList, getActivity(), activity.getAppTheme(), Color.parseColor(fabSkin));
//                                break;
//                        }
//                    }
//                    return true;
//                case R.id.openparent:
//                    loadlist(new File(LIST_ELEMENTS.get(plist.get(0)).getPath()).getParent(), false, OpenMode.FILE);
//                    return true;
//                case R.id.all:
//                    if (adapter.areAllChecked(currentPathTitle)) {
//                        adapter.toggleChecked(false, currentPathTitle);
//                    } else {
//                        adapter.toggleChecked(true, currentPathTitle);
//                    }
//                    mode.invalidate();
//
//                    return true;
//                case R.id.rename:
//
//                    final ActionMode m = mode;
//                    final BaseFile f;
//                    f = (LIST_ELEMENTS.get(
//						(plist.get(0)))).generateBaseFile();
//                    rename(f);
//                    mode.finish();
//                    return true;
//                case R.id.hide:
//                    for (int i1 = 0; i1 < plist.size(); i1++) {
//                        hide(LIST_ELEMENTS.get(plist.get(i1)).getPath());
//                    }
//                    updateList();
//                    mode.finish();
//                    return true;
//                case R.id.ex:
//                    activity.mainActivityHelper.extractFile(new File(LIST_ELEMENTS.get(plist.get(0)).getPath()));
//                    mode.finish();
//                    return true;
//                case R.id.cpy:
//                    activity.MOVE_PATH = null;
//                    ArrayList<BaseFile> copies = new ArrayList<>();
//                    for (int i2 = 0; i2 < plist.size(); i2++) {
//                        copies.add(LIST_ELEMENTS.get(plist.get(i2)).generateBaseFile());
//                    }
//                    activity.COPY_PATH = copies;
//                    activity.supportInvalidateOptionsMenu();
//                    mode.finish();
//                    return true;
//                case R.id.cut:
//                    activity.COPY_PATH = null;
//                    ArrayList<BaseFile> copie = new ArrayList<>();
//                    for (int i3 = 0; i3 < plist.size(); i3++) {
//                        copie.add(LIST_ELEMENTS.get(plist.get(i3)).generateBaseFile());
//                    }
//                    activity.MOVE_PATH = copie;
//                    activity.supportInvalidateOptionsMenu();
//                    mode.finish();
//                    return true;
//                case R.id.compress:
//                    ArrayList<BaseFile> copies1 = new ArrayList<>();
//                    for (int i4 = 0; i4 < plist.size(); i4++) {
//                        copies1.add(LIST_ELEMENTS.get(plist.get(i4)).generateBaseFile());
//                    }
//                    activity.getFutils().showCompressDialog((MainActivity) getActivity(), copies1, currentPathTitle);
//                    mode.finish();
//                    return true;
//                case R.id.openwith:
//                    activity.getFutils().openunknown(new File(LIST_ELEMENTS.get((plist.get(0))).getPath()), getActivity(), true);
//                    return true;
//                case R.id.addshortcut:
//                    addShortcut(LIST_ELEMENTS.get(plist.get(0)));
//                    mode.finish();
//                    return true;
//                default:
//                    return false;
//            }
//        }
//
//        // called when the user exits the action mode
//        public void onDestroyActionMode(ActionMode mode) {
//            mActionMode = null;
//            selection = false;
//
//            // translates the drawer content up
//            //if (activity.isDrawerLocked) activity.translateDrawerList(false);
//
//            activity.floatingActionButton.showMenuButton(true);
//            if (!results) adapter.toggleChecked(false, currentPathTitle);
//            else adapter.toggleChecked(false);
//            activity.setPagingEnabled(true);
//
//            activity.updateViews(new ColorDrawable(MainActivity.currentTab == 1 ?
//												   skinTwoColor : skin_color));
//
//            if (!activity.isDrawerLocked) {
//                activity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED,
//														 activity.mDrawerLinear);
//            }
//        }
//    };

//	void cpy() {
//		//ArrayList<Integer> plist = adapter.getCheckedItemPositions();
//		activity.MOVE_PATH = null;
//		ArrayList<BaseFile> copies = new ArrayList<>();
//		for (LayoutElements le : selectedInList1) {//int i2 = 0; i2 < plist.size(); i2++
//			copies.add(le.generateBaseFile());//dataSourceL1.get(plist.get(i2))
//		}
//		activity.COPY_PATH = copies;
//	}

//	void cut() {
//		//ArrayList<Integer> plist = adapter.getCheckedItemPositions();
//		activity.COPY_PATH = null;
//		ArrayList<BaseFile> copie = new ArrayList<>();
//		for (LayoutElements le : selectedInList1) {//int i3 = 0; i3 < plist.size(); i3++
//			copie.add(le.generateBaseFile());//dataSourceL1.get(plist.get(i3))
//		}
//		activity.MOVE_PATH = copie;
//		//activity.supportInvalidateOptionsMenu();
//	}

//	void compress() {
//		//ArrayList<Integer> plist = adapter.getCheckedItemPositions();
//		ArrayList<BaseFile> copies1 = new ArrayList<>();
//		for (LayoutElements le : selectedInList1) {//int i4 = 0; i4 < plist.size(); i4++
//			copies1.add(le.generateBaseFile());//dataSourceL1.get(plist.get(i4)
//		}
//		activity.getFutils().showCompressDialog(activity, copies1, currentPathTitle);
//	}
//	void share() {
//		ArrayList<File> arrayList = new ArrayList<>();
//		//ArrayList<Integer> plist = adapter.getCheckedItemPositions();
//		for (LayoutElements i : selectedInList1) {//plist
//			arrayList.add(new File(i.getPath()));//dataSourceL1.get(
//		}
//		if (selectedInList1.size() > 100)
//			Toast.makeText(getActivity(), getResources().getString(R.string.share_limit),
//						   Toast.LENGTH_SHORT).show();
//		else {
//
//			switch (dataSourceL1.get(0).getMode()) {
//				case DROPBOX:
//				case BOX:
//				case GDRIVE:
//				case ONEDRIVE:
//					activity.getFutils().shareCloudFile(dataSourceL1.get(0).getPath(),
//														dataSourceL1.get(0).getMode(), getContext());
//					break;
//				default:
//					activity.getFutils().shareFiles(arrayList, getActivity(), activity.getAppTheme(), Color.parseColor(fabSkin));
//					break;
//			}
//		}
//	}

//	void delete() {
//		//ArrayList<Integer> plist = adapter.getCheckedItemPositions();
//		activity.getFutils().deleteFiles(selectedInList1, this, /*plist, */activity.getAppTheme());
//	}
    /**
     * Show dialog to rename a file
     * @param f the file to rename
     */
//    public void rename(final BaseFile f) {
//        //ArrayList<Integer> plist = adapter.getCheckedItemPositions();
//		//final BaseFile f = (LIST_ELEMENTS.get((plist.get(0)))).generateBaseFile();
//		MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
//        String name = f.getName();
//        builder.input("", name, false, new MaterialDialog.InputCallback() {
//				@Override
//				public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
//
//				}
//			});
//        builder.theme(activity.getAppTheme().getMaterialDialogTheme());
//        builder.title(getResources().getString(R.string.rename));
//        builder.callback(new MaterialDialog.ButtonCallback() {
//				@Override
//				public void onPositive(MaterialDialog materialDialog) {
//					String name = materialDialog.getInputEditText().getText().toString();
//					if (f.isSmb())
//						if (f.isDirectory() && !name.endsWith("/"))
//							name = name + "/";
//
//					activity.mainActivityHelper.rename(openMode, f.getPath(),
//													   currentPathTitle + "/" + name, getActivity(), BaseActivity.rootMode);
//				}
//
//				@Override
//				public void onNegative(MaterialDialog materialDialog) {
//
//					materialDialog.cancel();
//				}
//			});
//        builder.positiveText(R.string.save);
//        builder.negativeText(R.string.cancel);
//        int color = Color.parseColor(fabSkin);
//        builder.positiveColor(color).negativeColor(color).widgetColor(color);
//        builder.build().show();
//    }

//    public void hide(String path) {
//
//        DataUtils.addHiddenFile(path);
//        if (new File(path).isDirectory()) {
//			File f1 = new File(path + "/" + ".nomedia");
//            if (!f1.exists()) {
//                try {
//                    activity.mainActivityHelper.mkFile(new HFile(OpenMode.FILE, f1.getPath()), this);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            Futils.scanFile(path, getActivity());
//        }
//
//    }

//    void addShortcut() {
//		//ArrayList<Integer> plist = adapter.getCheckedItemPositions();
//		LayoutElements path = selectedInList1.get(0);//dataSourceL1
//        //Adding shortcut for MainActivity
//        //on Home screen
//        Intent shortcutIntent = new Intent(getActivity().getApplicationContext(),
//										   ExplorerActivity.class);
//        shortcutIntent.putExtra(ExplorerActivity.EXTRA_DIR_PATH, path.getPath());
//        shortcutIntent.setAction(Intent.ACTION_MAIN);
//        shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        Intent addIntent = new Intent();
//        addIntent
//			.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
//        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, new File(path.getPath()).getName());
//
//        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
//						   Intent.ShortcutIconResource.fromContext(getActivity(),
//																   R.mipmap.ic_launcher));
//
//        addIntent
//			.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
//        getActivity().sendBroadcast(addIntent);
//    }

    /**
     * method called when list item is clicked in the adapter
     * @param position the {@link int} position of the list item
     * @param imageView the check {@link RoundedImageView} that is to be animated
     */
    public void onListItemClicked(int position, ImageView imageView) {
        if (position >= dataSourceL1.size()) return;

//        if (results) {
//
//            // check to initialize search results
//            // if search task is been running, cancel it
//            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//            SearchAsyncHelper fragment = (SearchAsyncHelper) fragmentManager
//				.findFragmentByTag(MainActivity.TAG_ASYNC_HELPER);
//            if (fragment != null) {
//
//                if (fragment.mSearchTask.getStatus() == AsyncTask.Status.RUNNING) {
//
//                    fragment.mSearchTask.cancel(true);
//                }
//                getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
//            }
//
//            mRetainSearchTask = true;
//            results = false;
//        } else {
//            mRetainSearchTask = false;
//            MainActivityHelper.SEARCH_TEXT = null;
//        }
//        if (selection) {
//            if (!LIST_ELEMENTS.get(position).getSize().equals(goback)) {
//                // the first {goback} item if back navigation is enabled
//                adapter.toggleChecked(position, imageView);
//            } else {
//                selection = false;
//                if (mActionMode != null)
//                    mActionMode.finish();
//                mActionMode = null;
//            }
//
//        } else {
//            if (!LIST_ELEMENTS.get(position).getSize().equals(goback)) {
//
//                // hiding search view if visible
//                if (ExplorerActivity.isSearchViewEnabled)   activity.hideSearchView();
//
//                String path;
//                LayoutElements l = LIST_ELEMENTS.get(position);
//                if (!l.hasSymlink()) {
//
//                    path = l.getPath();
//                } else {
//
//                    path = l.getSymlink();
//                }
//
//                // check if we're trying to click on encrypted file
//                if (!LIST_ELEMENTS.get(position).isDirectory() &&
//					LIST_ELEMENTS.get(position).getPath().endsWith(CryptUtil.CRYPT_EXTENSION)) {
//                    // decrypt the file
//                    activity.isEncryptOpen = true;
//
//                    activity.encryptBaseFile = new BaseFile(getActivity().getExternalCacheDir().getPath()
//															+ "/"
//															+ LIST_ELEMENTS.get(position).generateBaseFile().getName().replace(CryptUtil.CRYPT_EXTENSION, ""));
//
//                    decryptFile(this, openMode, LIST_ELEMENTS.get(position).generateBaseFile(),
//								getActivity().getExternalCacheDir().getPath(),
//								activity);
//                    return;
//                }
//
//                if (LIST_ELEMENTS.get(position).isDirectory()) {
//                    computeScroll();
//                    loadlist(path, false, openMode);
//                } else {
//                    if (l.getMode() == OpenMode.SMB) {
//                        try {
//                            SmbFile smbFile = new SmbFile(l.getPath());
//                            launchSMB(smbFile, l.length(), activity);
//                        } catch (MalformedURLException e) {
//                            e.printStackTrace();
//                        }
//                    } else if (l.getMode() == OpenMode.OTG) {
//
//                        activity.getFutils().openFile(RootHelper.getDocumentFile(l.getPath(), getContext(), false),
//													  (ExplorerActivity) getActivity());
//                    } else if (l.getMode() == OpenMode.DROPBOX
//							   || l.getMode() == OpenMode.BOX
//							   || l.getMode() == OpenMode.GDRIVE
//							   || l.getMode() == OpenMode.ONEDRIVE) {
//
//                        Toast.makeText(getContext(), getResources().getString(R.string.please_wait), Toast.LENGTH_LONG).show();
//                        CloudUtil.launchCloud(LIST_ELEMENTS.get(position).generateBaseFile(), openMode, activity);
//                    } else if (activity.mReturnIntent) {
//                        returnIntentResults(new File(l.getPath()));
//                    } else {
//
//                        activity.getFutils().openFile(new File(l.getPath()), activity);
//                    }
//                    DataUtils.addHistoryFile(l.getPath());
//                }
//            } else {
//                goBackItemClick();
//            }
//        }
    }

//    public void goBackItemClick() {
//        if (openMode == OpenMode.CUSTOM) {
//            loadlist(home, false, OpenMode.FILE);
//            return;
//        }
//        HFile currentFile = new HFile(openMode, currentPathTitle);
//        if (!results) {
//            if (selection) {
//                adapter.toggleChecked(false);
//            } else {
//                if (openMode == OpenMode.SMB) {
//
//                    try {
//                        if (!currentPathTitle.equals(smbPath)) {
//                            String path = (new SmbFile(currentPathTitle).getParent());
//                            loadlist((path), true, OpenMode.SMB);
//                        } else loadlist(home, false, OpenMode.FILE);
//                    } catch (MalformedURLException e) {
//                        e.printStackTrace();
//                    }
//                } else if (currentPathTitle.equals("/") || currentPathTitle.equals(home) ||
//                            currentPathTitle.equals(OTGUtil.PREFIX_OTG)
//                            || currentPathTitle.equals(CloudHandler.CLOUD_PREFIX_BOX + "/")
//                            || currentPathTitle.equals(CloudHandler.CLOUD_PREFIX_DROPBOX + "/")
//                            || currentPathTitle.equals(CloudHandler.CLOUD_PREFIX_GOOGLE_DRIVE + "/")
//                            || currentPathTitle.equals(CloudHandler.CLOUD_PREFIX_ONE_DRIVE + "/")
//                            )
//                        MAIN_ACTIVITY.exit();
//                    else if (utils.canGoBack(getContext(), currentFile)) {
//                        loadlist(currentFile.getParent(getContext()), true, openMode);
//                    } else MAIN_ACTIVITY.exit();
//            }
//        } else {
//            loadlist(currentFile.getPath(), true, openMode);
//        }
//    }

}
