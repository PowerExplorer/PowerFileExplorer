//package net.gnu.explorer;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.TreeSet;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.res.Configuration;
//import android.graphics.Color;
//import android.graphics.drawable.Drawable;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentActivity;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.View.OnLongClickListener;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.CheckBox;
//import android.widget.EditText;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.PopupMenu;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import net.gnu.androidutil.AndroidPathUtils;
//import net.gnu.androidutil.AndroidUtils;
//import net.gnu.androidutil.ImageThreadLoader;
//import net.gnu.util.CommandUtils;
//import net.gnu.util.FileUtil;
//import net.gnu.util.Util;
//import com.amaze.filemanager.filesystem.*;
//import com.amaze.filemanager.utils.*;
//import android.preference.*;
//import android.content.*;
//import android.widget.*;
//import android.view.*;
//import net.gnu.androidutil.*;
//import android.support.v7.view.menu.*;
//import android.os.*;
//import android.text.*;
//import android.view.animation.*;
//import android.graphics.*;
//import android.util.*;
//import android.support.v7.widget.*;
//import android.support.v4.app.*;
//import android.content.res.*;
//import android.support.v7.widget.helper.*;
//import java.util.*;
//
//import net.dongliu.apk.parser.*;
//import com.amaze.filemanager.ui.icons.*;
//import android.support.v4.widget.SwipeRefreshLayout;
//import com.amaze.filemanager.ui.LayoutElements;
//import android.view.inputmethod.InputMethodManager;
//
//public class ContentFragment2 extends FileFrag implements View.OnClickListener, View.OnTouchListener, SwipeRefreshLayout.OnRefreshListener {
//	private static final String TAG = "ContentFragment2";
//	
////	final ArrayList<File> dataSourceL1 = new ArrayList<>();
////	final ArrayList<File> selectedInList1 = new ArrayList<>();
////	final ArrayList<File> tempSelectedInList1 = new ArrayList<>();
////	final ArrayList<File> tempOriDataSourceL1 = new ArrayList<>();
////	private File tempPreviewL2 = null;
//
////	ImageButton allCbx = null;
////	private ImageButton icons;
////	private TextView allName;
////	private TextView allDate;
////	private TextView allSize;
////	private TextView allType;
////	TextView selectionStatus1;
////	private RecyclerView listView1 = null;
////	ArrAdapter srcAdapter;
//
//	//private ImageThreadLoader imageLoader;
//
//	public int theme1;
//	
//	private SearchFileNameTask searchTask = null;
//	private LinearLayout commands;
////	private LinearLayout mDirectoryButtons;
////	private HorizontalScrollView scrolltext;
//	//private FileListSorter fileListSorter;
//	//private GridLayoutManager gridLayoutManager;
//	//private GridDividerItemDecoration dividerItemDecoration;
//	
//    private TextSearch textSearch = new TextSearch();
//	private ScaleGestureDetector mScaleGestureDetector;
//	//boolean mScaling;    // Whether the user is currently pinch zooming
//	private ImageButton removeBtn;
//	private ImageButton removeAllBtn;
//	private ImageButton addBtn;
//	private ImageButton addAllBtn;
//	
//	public ContentFragment2() {
//		super();
//		type = Frag.TYPE.SELECTION.ordinal();
//		title = "Selection";
//	}
//
//	@Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//							 Bundle savedInstanceState) {
//		Log.d(TAG, "onCreateView " + currentPathTitle + ", " + savedInstanceState);
//		super.onCreateView(inflater, container, savedInstanceState);
//		View v = inflater.inflate(R.layout.pager_item2, container, false);
//		//v.requestFocusFromTouch();
//		return v;
//    }
//
//	@Override
//    public void onViewCreated(View v, Bundle savedInstanceState) {
//        Log.d(TAG, "onViewCreated " + currentPathTitle + ", " + (savedInstanceState == null));
//		super.onViewCreated(v, savedInstanceState);
//		//setRetainInstance(true);
//		
//		spanCount = AndroidUtils.getSharedPreference(getContext(), "SPAN_COUNT.ContentFrag2", 1);
//		
//        final Bundle args = getArguments();
//		Log.d(TAG, "onViewCreated " + currentPathTitle + ", " + "args=" + args);
//
//		if (args != null) {
//			currentPathTitle = args.getString("title");
//			if (savedInstanceState == null && args.getStringArrayList("dataSourceL2") != null) {
//				savedInstanceState = args;
//			}
//		}
//		allCbx.setOnClickListener(this);
//		icons.setOnClickListener(this);
//		allName.setOnClickListener(this);
//		allDate.setOnClickListener(this);
//		allSize.setOnClickListener(this);
//		allType.setOnClickListener(this);
//		
//		
//		removeBtn = (ImageButton) v.findViewById(R.id.remove);
//		removeAllBtn = (ImageButton) v.findViewById(R.id.removeAll);
//		addBtn = (ImageButton) v.findViewById(R.id.add);
//		addAllBtn = (ImageButton) v.findViewById(R.id.addAll);
//		
//		listView.requestFocusFromTouch();
//		//listView2.setFastScrollEnabled(true);
//		//listView2.setFastScrollAlwaysVisible(false);
//		//listView2.setVerticalScrollbarPosition(0);
//		listView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
//		//dividerItemDecoration = new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST, true, true);
//        //mmSwipeRefreshLayout.setColorSchemeColors(Color.parseColor(fabSkin));
//        DefaultItemAnimator animator = new DefaultItemAnimator();
//        animator.setAddDuration(500);
//		animator.setRemoveDuration(500);
//        listView.setItemAnimator(animator);
//		listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//				@Override
//				public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//					//Log.d(TAG, "onScrolled dx=" + dx + ", dy=" + dy + ", density=" + activity.density);
//					if (System.currentTimeMillis() - lastScroll > 50) {//!mScaling && 
//						if (dy > activity.density << 4 && selectionStatus1.getVisibility() == View.VISIBLE) {
//							selectionStatus1.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_bottom));
//							selectionStatus1.setVisibility(View.GONE);
//							horizontalDivider0.setVisibility(View.GONE);
//							horizontalDivider12.setVisibility(View.GONE);
//							status.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_bottom));
//							status.setVisibility(View.GONE);
//						} else if (dy < -activity.density << 4 && selectionStatus1.getVisibility() == View.GONE) {
//							selectionStatus1.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
//							selectionStatus1.setVisibility(View.VISIBLE);
//							horizontalDivider0.setVisibility(View.VISIBLE);
//							horizontalDivider12.setVisibility(View.VISIBLE);
//							status.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
//							status.setVisibility(View.VISIBLE);
//						}
//						lastScroll = System.currentTimeMillis();
//					}
//					
//				}
//			});
////		scrolltext = (HorizontalScrollView) v.findViewById(R.id.scroll_text);
////		mDirectoryButtons = (LinearLayout) v.findViewById(R.id.directory_buttons);
//		commands = (LinearLayout) v.findViewById(R.id.commands);
//		drawableDelete = activity.getDrawable(R.drawable.ic_delete_white_36dp);
//		drawablePaste = activity.getDrawable(R.drawable.ic_content_paste_white_36dp);
//		clearButton.setOnClickListener(this);
//		searchButton.setOnClickListener(this);
//		searchET.addTextChangedListener(textSearch);
//		mSwipeRefreshLayout.setOnRefreshListener(this);
//		mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
////                public boolean onScaleBegin(ScaleGestureDetector detector) {
////					mScaling = true;
////					return true;
////				}
////				public void onScaleEnd(ScaleGestureDetector detector) {
////					mScaling = false;
////				}
//				@Override
//                public boolean onScale(ScaleGestureDetector detector) {
//                    Log.d(TAG, "onScale getCurrentSpan " + detector.getCurrentSpan() + ", getPreviousSpan " + detector.getPreviousSpan() + ", getTimeDelta " + detector.getTimeDelta());
//					//if (detector.getCurrentSpan() > 200 && detector.getTimeDelta() > 50) {
//					//Log.d(TAG, "onScale " + (detector.getCurrentSpan() - detector.getPreviousSpan()) + ", getTimeDelta " + detector.getTimeDelta());
//					if (detector.getCurrentSpan() - detector.getPreviousSpan() < -50) {
//						if (spanCount == 1) {
//							spanCount = 2;
//							setRecyclerViewLayoutManager();
//							return true;
//						} else if (spanCount == 2 && width.size >= 0) {
//							if (left.getVisibility() == View.GONE || right.getVisibility() == View.GONE) {
//								spanCount = 8;
//							} else {
//								spanCount = 4;
//							}
//							setRecyclerViewLayoutManager();
//							return true;
//						}
//					} else if (detector.getCurrentSpan() - detector.getPreviousSpan() > 50) {
//						if ((spanCount == 4 || spanCount == 8)) {
//							spanCount = 2;
//							setRecyclerViewLayoutManager();
//							return true;
//						} else if (spanCount == 2) {
//							spanCount = 1;
//							setRecyclerViewLayoutManager();
//							return true;
//						} 
//					}
//                    //}
//                    return false;
//                }
//            });
//			
////		srcAdapter = new ArrAdapter(this, dataSourceL1, rightCommands, horizontalDivider6);
////		listView1.setAdapter(srcAdapter);
//
//        // use this setting to improve performance if you know that changes
//        // in content do not change the layout size of the RecyclerView
//        listView.setHasFixedSize(true);
//		listView.setScrollbarFadingEnabled(true);
//		listView.setItemViewCacheSize(20);
//		listView.setDrawingCacheEnabled(true);
//		
//        listView.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                   	//Log.d(TAG, "onTouch " + event);
//					select(true);
//					mScaleGestureDetector.onTouchEvent(event);
//                    return false;
//                }
//            });
//
//		if (savedInstanceState != null) {
////			if (savedInstanceState.getString("title") != null) {
////				title = savedInstanceState.getString("title");
////			}
////			dataSourceL2.addAll(Util.collectionString2FileArrayList(savedInstanceState.getStringArrayList("dataSourceL2")));
////			Log.d(TAG, "dataSourceL2 " + dataSourceL2);
////			tempOriDataSourceL2.addAll(dataSourceL2);
////
////			selectedInList2.addAll(Util.collectionString2FileArrayList(savedInstanceState.getStringArrayList("selectedInList2")));
////			Log.d(TAG, "selectedInList2 " + selectedInList2);
////			
////			if (savedInstanceState.getString("tempPreviewL2") != null) {
////				tempPreviewL2 = new File(savedInstanceState.getString("tempPreviewL2"));
////			}
//			setRecyclerViewLayoutManager();
//			gridLayoutManager.scrollToPositionWithOffset(savedInstanceState.getInt("index"), savedInstanceState.getInt("top"));
//		} else {
//			setRecyclerViewLayoutManager();
//		}
//
////		ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
////			new ItemTouchHelper.SimpleCallback(0 , ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
////			@Override
////			public boolean onMove(RecyclerView recyclerView,
////								  RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
////				return false ;
////			}
////			@Override
////			public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
////				final int adapterPosition = viewHolder.getAdapterPosition();
////				final File f = destAdapter.remove(adapterPosition);
////				destAdapter.notifyDataSetChanged();
////				selectedInList2.remove(f);
////				selectionStatus2.setText(selectedInList2.size() + "/" + dataSourceL2.size());
////				activity.curContentFrag.srcAdapter.notifyDataSetChanged();
////			}
////		};
////		ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
////		itemTouchHelper.attachToRecyclerView(listView2);
//
////		SharedPreferences Sp = PreferenceManager.getDefaultSharedPreferences(activity);
////		int theme = Sp.getInt("theme", 0);
////		theme1 = theme == 2 ? PreferenceUtils.hourOfDay() : theme;
//		if (selectedInList1.size() == 0) {
//			if (rightCommands.getVisibility() == View.VISIBLE) {
//				horizontalDivider6.setVisibility(View.GONE);
//				rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
//				rightCommands.setVisibility(View.GONE);
//			}
//		} else {
//			rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
//			rightCommands.setVisibility(View.VISIBLE);
//			horizontalDivider6.setVisibility(View.VISIBLE);
//		}
//	}
//
//	@Override
//	public void onSaveInstanceState(android.os.Bundle outState) {
//		AndroidUtils.setSharedPreference(getContext(), "SPAN_COUNT.ContentFrag2", spanCount);
//		outState.putString("title", currentPathTitle);
//		outState.putParcelableArrayList("dataSourceL1", dataSourceL1);
//		//outState.putStringArrayList("dataSourceL2", Util.collectionFile2StringArrayList(dataSourceL1));
//		outState.putParcelableArrayList("selectedInList1", selectedInList1);
//		outState.putString("searchVal", searchET.getText().toString());
//		if (tempPreviewL2 != null) {
//			outState.putString("tempPreviewL2", tempPreviewL2.path);
//		}
//		Log.d(TAG, "onSaveInstanceState " + currentPathTitle + ", " + outState);
//		int index = gridLayoutManager.findFirstVisibleItemPosition();
//        final View vi = listView.getChildAt(0); 
//        final int top = (vi == null) ? 0 : vi.getTop();
//		outState.putInt("index", index);
//		outState.putInt("top", top);
//		
//		super.onSaveInstanceState(outState);
//	}
//
//	public Map<String, Object> onSaveInstanceState() {
//		Map<String, Object> outState = new TreeMap<>();
//		outState.put("title", currentPathTitle);
//		outState.put("dataSourceL2", dataSourceL1);
//		outState.put("selectedInList2", selectedInList1);
//		outState.put("searchVal", searchET.getText().toString());
//		Log.d(TAG, "onSaveInstanceState " + currentPathTitle + ", " + outState);
//		int index = gridLayoutManager.findFirstVisibleItemPosition();
//        final View vi = listView.getChildAt(0); 
//        final int top = (vi == null) ? 0 : vi.getTop();
//		outState.put("index", index);
//		outState.put("top", top);
//		
//        return outState;
//	}
//	
//	void reload(Map<String, Object> savedInstanceState) {
//		currentPathTitle = (String) savedInstanceState.get("title");
//		dataSourceL1.clear();
//		dataSourceL1.addAll((ArrayList<LayoutElements>) savedInstanceState.get("dataSourceL1"));
//		Log.d(TAG, "dataSourceL2 " + dataSourceL1);
//		tempOriDataSourceL1.addAll(dataSourceL1);
//
//		selectedInList1.addAll((ArrayList<LayoutElements>) savedInstanceState.get("selectedInList1"));
//		srcAdapter.notifyDataSetChanged();
//		
//		setRecyclerViewLayoutManager();
//		gridLayoutManager.scrollToPositionWithOffset(savedInstanceState.get("index"), savedInstanceState.get("top"));
//		
//	}
//	
//	@Override
//    public void onRefresh() {
//		final Editable s = searchET.getText();
//		if (s.length() > 0) {
//			textSearch.afterTextChanged(s);
//		} else {
//			LayoutElements f;
//			boolean changed = false;
//        	for (int i = dataSourceL1.size() - 1; i >= 0; i--) {
//				f = dataSourceL1.get(i);
//				if (!f.bf.f.exists()) {
//					changed = true;
//					dataSourceL1.remove(i);
//					selectedInList1.remove(f);
//					tempOriDataSourceL1.remove(f);
//					tempSelectedInList1.remove(f);
//					if (tempPreviewL2 != null && f.path.equals(tempPreviewL2.path)) {
//						tempPreviewL2 = null;
//					}
//				}
//			}
//			if (changed) {
//				srcAdapter.notifyDataSetChanged();
//				selectionStatus1.setText(selectedInList1.size()  + "/" + dataSourceL1.size());
//			}
//			if (mSwipeRefreshLayout.isRefreshing()) {
//				mSwipeRefreshLayout.setRefreshing(false);
//			}
//		}
//    }
//
//	@Override
//    public void onPause() {
//        Log.d(TAG, "onPause " + currentPathTitle);
//		super.onPause();
//		if (imageLoader != null) {
//			imageLoader.stopThread();
//		}
//    }
//
//	@Override
//    public void onResume() {
//        Log.d(TAG, "onResume " + currentPathTitle);
//		activity = (ExplorerActivity)getActivity();
//		
//		activity.curContentFrag.dataSourceL2 = dataSourceL1;
//		if (activity.curContentFrag.srcAdapter != null) {
//			activity.curContentFrag.srcAdapter.notifyDataSetChanged();
//		}
//		updateColor(null);
//		super.onResume();
//	}
//
//	@Override
//	public Frag clone() {
//		final ContentFragment2 frag = new ContentFragment2();
//		frag.currentPathTitle = currentPathTitle;
//		frag.width = width;
//		return frag;
//	}
//
//	@Override
//	public void clone(final Frag frag2) {
//		Log.d(TAG, "clone " + frag2);
//		final ContentFragment2 frag = (ContentFragment2) frag2;
//		currentPathTitle = frag.currentPathTitle;
//		if (!fake) {
//			suffix = frag.suffix;
//			multiFiles = frag.multiFiles;
//			width = frag2.width;
//			dataSourceL1 = frag.dataSourceL1;
//			selectedInList1 = frag.selectedInList1;
//			tempSelectedInList1 = frag.tempSelectedInList1;
//		}
//		fake = true;
//		spanCount = frag.spanCount;
//		dataSourceL2 = frag.dataSourceL2;
//		tempPreviewL2 = frag.tempPreviewL2;
//		searchMode = frag.searchMode;
//		searchVal = frag.searchVal;
//		dirTemp4Search = frag.dirTemp4Search;
//		srcAdapter = frag.srcAdapter;
//		if (listView != null && listView.getAdapter() != srcAdapter) {
//			listView.setAdapter(srcAdapter);
//		}
//		if (allCbx != null) {
//			setDirectoryButtons();
//			allName.setText(frag.allName.getText());
//			allType.setText(frag.allType.getText());
//			allDate.setText(frag.allDate.getText());
//			allSize.setText(frag.allSize.getText());
//			final int size = selectedInList1.size();
//			if (size == dataSourceL1.size()) {
//				allCbx.setImageResource(R.drawable.ic_accept);
//			} else if (size > 0) {
//				allCbx.setImageResource(R.drawable.ready);
//			} else {
//				allCbx.setImageResource(R.drawable.dot);
//			}
//			//pi.fakeFrag.listView1.getAdapter().notifyDataSetChanged();
//			//pi.fakeFrag.setRecyclerViewLayoutManager();
//			if (gridLayoutManager == null || gridLayoutManager.getSpanCount() != spanCount) {
//				listView.removeItemDecoration(dividerItemDecoration);
//				gridLayoutManager = new GridLayoutManager(getContext(), spanCount);
//				listView.setLayoutManager(gridLayoutManager);
//			}
//
//			final int index = frag.gridLayoutManager.findFirstVisibleItemPosition();
//			final View vi = frag.listView.getChildAt(0); 
//			final int top = (vi == null) ? 0 : vi.getTop();
//			gridLayoutManager.scrollToPositionWithOffset(index, top);
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
//
//	@Override
//	public void updateColor(View rootView) {
//		getView().setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
//		icons.setColorFilter(ExplorerActivity.TEXT_COLOR);
//		allName.setTextColor(ExplorerActivity.TEXT_COLOR);
//		allDate.setTextColor(ExplorerActivity.TEXT_COLOR);
//		allSize.setTextColor(ExplorerActivity.TEXT_COLOR);
//		allType.setTextColor(ExplorerActivity.TEXT_COLOR);
//		selectionStatus1.setTextColor(ExplorerActivity.TEXT_COLOR);
//		searchET.setTextColor(ExplorerActivity.TEXT_COLOR);
//		clearButton.setColorFilter(ExplorerActivity.TEXT_COLOR);
//		searchButton.setColorFilter(ExplorerActivity.TEXT_COLOR);
//		selectionStatus1.setText(selectedInList1.size()  + "/" + dataSourceL1.size());
//		noFileText.setTextColor(ExplorerActivity.TEXT_COLOR);
//		noFileImage.setColorFilter(ExplorerActivity.TEXT_COLOR);
//		horizontalDivider0.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
//		horizontalDivider12.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
//		horizontalDivider7.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
//		addBtn.setColorFilter(ExplorerActivity.TEXT_COLOR);
//		addAllBtn.setColorFilter(ExplorerActivity.TEXT_COLOR);
//		removeBtn.setColorFilter(ExplorerActivity.TEXT_COLOR);
//		removeAllBtn.setColorFilter(ExplorerActivity.TEXT_COLOR);
//	}
//
//	public void refreshRecyclerViewLayoutManager() {
//		setRecyclerViewLayoutManager();
//		horizontalDivider0.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
//		horizontalDivider12.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
//		horizontalDivider7.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
//	}
//
//	void setRecyclerViewLayoutManager() {
//        Log.e(TAG, "setRecyclerViewLayoutManager " + gridLayoutManager);
//		if (listView == null) {
//			return;
//		}
//        int scrollPosition = 0, top = 0;
//		// If a layout manager has already been set, get current scroll position.
//        if (gridLayoutManager != null) {
//			scrollPosition = gridLayoutManager.findFirstVisibleItemPosition();
//			final View vi = listView.getChildAt(0); 
//			top = (vi == null) ? 0 : vi.getTop();
//		}
//		final Context context = getContext();
//		gridLayoutManager = new GridLayoutManager(context, spanCount);
//		listView.removeItemDecoration(dividerItemDecoration);
//		if (spanCount <= 2) {
//			dividerItemDecoration = new GridDividerItemDecoration(context, true);
//			//dividerItemDecoration = new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST, true, true);
//			listView.addItemDecoration(dividerItemDecoration);
//		}
//		srcAdapter = new ArrAdapter(this, dataSourceL1, rightCommands, horizontalDivider6);
//		listView.setAdapter(srcAdapter);
//
//		listView.setLayoutManager(gridLayoutManager);
//		gridLayoutManager.scrollToPositionWithOffset(scrollPosition, top);
//	}
//
//	private LinkedList<Map<String, Object>> backStack = new LinkedList<>();
//
//	boolean back() {
//		Map<String, Object> softBundle;
//		Log.d(TAG, "back " + backStack.size());
//		if (backStack.size() > 1 && (softBundle = backStack.pop()) != null && softBundle.get("dataSourceL1") != null) {
//			Log.d(TAG, "back " + softBundle);
//			reload(softBundle);
//			return true;
//		} else {
//			return false;
//		}
//	}
//	
//	public void manageUi(boolean search) {
//		// HorizontalScrollView scrollButtons =
//		// (HorizontalScrollView)findViewById(R.id.scroll_buttons);
//		
//		if (search == true) {
//			searchET.setHint("Search");
//			searchButton.setImageResource(R.drawable.ic_arrow_back_grey600);
//			//topflipper.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_bottom));
//			topflipper.setDisplayedChild(topflipper.indexOfChild(quickLayout));
//			searchMode = true;
//			searchET.requestFocus();
//			imm.showSoftInput(searchET, InputMethodManager.SHOW_IMPLICIT);
//		} else {
//			imm.hideSoftInputFromWindow(searchET.getWindowToken(), 0);
//			searchET.setText("");
//			searchButton.setImageResource(R.drawable.ic_action_search);
//			//topflipper.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
//			topflipper.setDisplayedChild(topflipper.indexOfChild(commands));
//			searchMode = false;
//			dataSourceL1.clear();
//			dataSourceL1.addAll(tempOriDataSourceL1);
//		}
//	}
//
//	public void searchButton() {
//		searchMode = !searchMode;
//		manageUi(searchMode);
//	}
//
//	private class TextSearch implements TextWatcher {
//		public void beforeTextChanged(CharSequence s, int start, int end, int count) {
//		}
//
//		public void afterTextChanged(final Editable text) {
//			final String filesearch = text.toString();
//			Log.d("quicksearch", "filesearch " + filesearch);
//			if (filesearch.length() > 0) {
//				if (searchTask != null
//					&& searchTask.getStatus() == AsyncTask.Status.RUNNING) {
//					searchTask.cancel(true);
//				}
//				
//				searchTask = new SearchFileNameTask();
//				searchTask.execute(filesearch);
//			}
//		}
//
//		public void onTextChanged(CharSequence s, int start, int end, int count) {
//		}
//	}
//
//	private class SearchFileNameTask extends AsyncTask<String, Long, Long> {
//
//		protected void onPreExecute() {
//			searchMode = true;
//			searchVal = searchET.getText().toString();
//			if (!mSwipeRefreshLayout.isRefreshing()) {
//				mSwipeRefreshLayout.setRefreshing(true);
//			}
//		}
//
//		@Override
//		protected Long doInBackground(String... params) {
//			Log.d(TAG, "SearchFileNameTask " + params[0]);
//			dataSourceL1.clear();
//			getActivity().runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						showToast("Searching...");
//						srcAdapter.notifyDataSetChanged();
//					}
//				});
//			final Collection<LayoutElements> c = FileUtil.getFilesBy(tempOriDataSourceL1, params[0], true);
//			Log.d(TAG, "getFilesBy " + Util.collectionToString(c, true, "\n"));
//			dataSourceL1.addAll(c);
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Long result) {
//			srcAdapter.notifyDataSetChanged();
//			if (mSwipeRefreshLayout.isRefreshing()) {
//				mSwipeRefreshLayout.setRefreshing(false);
//			}
//			selectedInList1.clear();
//			if (dataSourceL1.size() == 0) {
//				nofilelayout.setVisibility(View.VISIBLE);
//				mSwipeRefreshLayout.setVisibility(View.GONE);
//			} else {
//				nofilelayout.setVisibility(View.GONE);
//				mSwipeRefreshLayout.setVisibility(View.VISIBLE);
//			}
//		}
//	}
//
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
//			if (width.size <= 0) {
//				mi.setTitle("Wider panel");
//			} else {
//				mi.setTitle("2 panels equal");
//			}
//		}
//		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//				public boolean onMenuItemClick(MenuItem item) {
//					Log.d(TAG, item.getTitle() + ".");
//					switch (item.getItemId()) {
//						case R.id.rangeSelection:
//							int min = Integer.MAX_VALUE, max = -1;
//							int cur = -3;
//							for (LayoutElements s : selectedInList1) {
//								cur = dataSourceL1.indexOf(s);
//								if (cur > max) {
//									max = cur;
//								}
//								if (cur < min && cur >= 0) {
//									min = cur;
//								}
//							}
//							selectedInList1.clear();
//							for (cur = min; cur <= max; cur++) {
//								selectedInList1.add(dataSourceL1.get(cur));
//							}
//							srcAdapter.notifyDataSetChanged();
//							break;
//						case R.id.inversion:
//							tempSelectedInList1.clear();
//							for (LayoutElements s : dataSourceL1) {
//								if (!selectedInList1.contains(s)) {
//									tempSelectedInList1.add(s);
//								}
//							}
//							selectedInList1.clear();
//							selectedInList1.addAll(tempSelectedInList1);
//							srcAdapter.notifyDataSetChanged();
//							break;
//						case R.id.clearSelection:
//							tempSelectedInList1.clear();
//							tempSelectedInList1.addAll(selectedInList1);
//							selectedInList1.clear();
//							srcAdapter.notifyDataSetChanged();
//							break;
//						case R.id.undoClearSelection:
//							selectedInList1.clear();
//							selectedInList1.addAll(tempSelectedInList1);
//							tempSelectedInList1.clear();
//							srcAdapter.notifyDataSetChanged();
//							break;
//						case R.id.swap:
////							if (spanCount == 8) {
////								spanCount = 4;
////							}
////							AndroidUtils.setSharedPreference(getContext(), "SPAN_COUNT", spanCount);
//							activity.swap(v);
//							break;
//						case R.id.hide: 
//							if (activity.left.getVisibility() == View.VISIBLE) {
////								if (spanCount == 4) {
////									spanCount = 8;
////								}
//								if (activity.swap) {
//									activity.left.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_left));
//									activity.right.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_left));
//								} else {
//									activity.left.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_in_right));
//									activity.right.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_out_right));
//								}
//								activity.right.setVisibility(View.GONE);
//							} else {
//								if (spanCount == 8) {
//									spanCount = 4;
//									setRecyclerViewLayoutManager();
//								}
//								if (activity.swap) {
//									activity.left.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_left));
//									activity.right.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_left));
//								} else {
//									activity.left.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_in_right));
//									activity.right.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_out_right));
//								}
//								activity.left.setVisibility(View.VISIBLE);
//							}
////							final FragmentManager supportFragManager = activity.getSupportFragmentManager();
////							final FragmentTransaction transaction = supportFragManager.beginTransaction();
////							transaction.setCustomAnimations(R.animator.fragment_slide_left_enter,
////															R.animator.fragment_slide_left_exit,
////															R.animator.fragment_slide_right_enter,
////															R.animator.fragment_slide_right_exit);
////							activity.leftCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
////							rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
////							if (!activity.slideFrag.isHidden()) {
////								if (spanCount == 4)
////									spanCount = 8;
////								
////								transaction.hide(activity.slideFrag2);
////								transaction.commit();
////								activity.slideFrag.updateLayout(false);
////								if (activity.swap) {
////									activity.left.setVisibility(View.GONE);
////								} else {
////									activity.right.setVisibility(View.GONE);
////								}
////								//rightCommands.setVisibility(View.GONE);
////								//horizontalDivider6.setVisibility(View.GONE);
////							} else {
////								if (spanCount == 8)
////									spanCount = 4;
////								
////								transaction.show(activity.slideFrag);
////								transaction.commit();
////								activity.slideFrag2.updateLayout(false);
////								if (activity.swap) {
////									activity.right.setVisibility(View.VISIBLE);
////								} else {
////									activity.left.setVisibility(View.VISIBLE);
////								}
////								//activity.leftScroll.setVisibility(View.VISIBLE);
////								//activity.horizontalDivider11.setVisibility(View.VISIBLE);
////							}
//							//AndroidUtils.setSharedPreference(getContext(), "SPAN_COUNT", spanCount);
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
//									width.size = -1;
//								} else {
//									width.size = 1;
//								}
//							} else {
//								LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)activity.left.getLayoutParams();
//								params.weight = 1.0f;
//								activity.left.setLayoutParams(params);
//								params = (LinearLayout.LayoutParams)activity.right.getLayoutParams();
//								params.weight = 1.0f;
//								activity.right.setLayoutParams(params);
//								activity.leftSize = 0;
//								width.size = 0;
//							}
//							AndroidUtils.setSharedPreference(activity, "biggerequalpanel", activity.leftSize);
//					}
//					return true;
//				}
//			});
//		popup.show();
//	}
//
////	public boolean copys(final View v) {
////		activity.mode = OpenMode.FILE;
////		activity.copyl.clear();
////		activity.cutl.clear();
////		activity.copyl.addAll(selectedInList1);
////		return true;
////	}
//
////	public boolean cuts(final View v) {
////		activity.mode = OpenMode.FILE;
////		activity.copyl.clear();
////		activity.cutl.clear();
////		activity.cutl.addAll(selectedInList1);
////		return true;
////	}
//
//	public boolean renames(final View v) {
//		return true;
//	}
//
//	public boolean compresss(final View v) {
//		return true;
//	}
//
//	public boolean encrypts(final View v) {
//		return true;
//	}
//
//	public boolean deletes(final View v) {
//		if (selectedInList1.size() > 0) {
//			//new Futils().deleteFileList(selectedInList2, activity);
//			dataSourceL1.removeAll(selectedInList1);
//			selectedInList1.clear();
//			srcAdapter.notifyDataSetChanged();
//		}
//		return true;
//	}
//
////	public boolean shares(final View v) {
////		if (selectedInList1.size() > 0) {
////			if (selectedInList1.size() > 100)
////				Toast.makeText(getContext(), "Can't share more than 100 files", Toast.LENGTH_SHORT).show();
////			else {
////				final ArrayList<File> lf = new ArrayList<>(selectedInList1.size());
////				for (LayoutElements le : selectedInList1) {
////					lf.add(le.bf.f);
////				}
////				//new Futils().shareFiles(lf, activity, theme1, Color.BLUE);
////				new Futils().shareFiles(lf, activity, activity.getAppTheme(), Color.parseColor(fabSkin));
////			}
////		} else {
////			showToast("No file selected");
////		}
////		return true;
////	}
//
////	public boolean sends(final View v) {
////		if (selectedInList2.size() > 0) {
////			ArrayList<Uri> uris = new ArrayList<Uri>(selectedInList2.size());
////			Intent send_intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
////			send_intent.setFlags(0x1b080001);
////
////			send_intent.setType("*/*");
////			for(File file : selectedInList2) {
////				uris.add(Uri.fromFile(file));
////			}
////			Log.d(TAG, Util.collectionToString(uris, true, "\n") + ".");
////			send_intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
////			Log.d("send_intent", send_intent + ".");
////			Log.d("send_intent.getExtras()", AndroidUtils.bundleToString(send_intent.getExtras()));
////			Intent createChooser = Intent.createChooser(send_intent, "Send via..");
////			Log.d("createChooser", createChooser + ".");
////			Log.d("createChooser.getExtras()", AndroidUtils.bundleToString(createChooser.getExtras()));
////			// Verify that the intent will resolve to an activity
////			if (createChooser.resolveActivity(getContext().getPackageManager()) != null) {
////				startActivity(createChooser);
////			}
////			startActivity(createChooser);
////		} else {
////			showToast("No file selected");
////		}
////		return true;
////	}
//
//	@Override
//	public void onClick(final View p1) {
//		//select(true);
//		switch (p1.getId()) {
//			case R.id.allCbx:
//				selectedInList1.clear();
//				if (!allCbx.isSelected()) {//}.isChecked()) {
//					allCbx.setSelected(true);
//					selectedInList1.addAll(dataSourceL1);
//					if (selectedInList1.size() > 0 && rightCommands.getVisibility() == View.GONE) {
//						rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
//						rightCommands.setVisibility(View.VISIBLE);
//						horizontalDivider6.setVisibility(View.VISIBLE);
//					}
//				} else {
//					allCbx.setSelected(false);
//					if (selectedInList1.size() == 0 && rightCommands.getVisibility() != View.GONE) {
//						horizontalDivider6.setVisibility(View.GONE);
//						rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
//						rightCommands.setVisibility(View.GONE);
//					}
//				}
//				selectionStatus1.setText(selectedInList1.size()  + "/" + dataSourceL1.size());
//				srcAdapter.notifyDataSetChanged();
//				break;
//			case R.id.allName:
//				if (allName.getText().toString().equals("Name ▲")) {
//					allName.setText("Name ▼");
//					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.NAME, FileListSorter.DESCENDING);
//					AndroidUtils.setSharedPreference(activity, "ContentFrag2SortType", "Name ▼");
//				} else {
//					allName.setText("Name ▲");
//					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.NAME, FileListSorter.ASCENDING);
//					AndroidUtils.setSharedPreference(activity, "ContentFrag2SortType", "Name ▲");
//				}
//				//Log.i("allName2", Util.collectionToString(dataSourceL2, true, "\n"));
//				updateTemp();
//				allDate.setText("Date");
//				allSize.setText("Size");
//				allType.setText("Type");
//				Collections.sort(dataSourceL1, fileListSorter);
//				srcAdapter.notifyDataSetChanged();
//				break;
//			case R.id.allType:
//				if (allType.getText().toString().equals("Type ▲")) {
//					allType.setText("Type ▼");
//					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.TYPE, FileListSorter.DESCENDING);
//					AndroidUtils.setSharedPreference(activity, "ContentFrag2SortType", "Type ▼");
//				} else {
//					allType.setText("Type ▲");
//					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.TYPE, FileListSorter.ASCENDING);
//					AndroidUtils.setSharedPreference(activity, "ContentFrag2SortType", "Type ▲");
//				}
//				updateTemp();
//				allName.setText("Name");
//				allDate.setText("Date");
//				allSize.setText("Size");
//				Collections.sort(dataSourceL1, fileListSorter);
//				srcAdapter.notifyDataSetChanged();
//				break;
//			case R.id.allDate:
//				if (allDate.getText().toString().equals("Date ▲")) {
//					allDate.setText("Date ▼");
//					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.DATE, FileListSorter.DESCENDING);
//					AndroidUtils.setSharedPreference(activity, "ContentFrag2SortType", "Date ▼");
//				} else {
//					allDate.setText("Date ▲");
//					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.DATE, FileListSorter.ASCENDING);
//					AndroidUtils.setSharedPreference(activity, "ContentFrag2SortType", "Date ▲");
//				}
//				//Log.i("date", Util.collectionToString(dataSourceL2, true, "\n"));
//				updateTemp();
//				allName.setText("Name");
//				allSize.setText("Size");
//				allType.setText("Type");
//				Collections.sort(dataSourceL1, fileListSorter);
//				srcAdapter.notifyDataSetChanged();
//				break;
//			case R.id.allSize:
//				if (allSize.getText().toString().equals("Size ▲")) {
//					allSize.setText("Size ▼");
//					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.SIZE, FileListSorter.DESCENDING);
//					AndroidUtils.setSharedPreference(activity, "ContentFrag2SortType", "Size ▼");
//				} else {
//					allSize.setText("Size ▲");
//					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.SIZE, FileListSorter.ASCENDING);
//					AndroidUtils.setSharedPreference(activity, "ContentFrag2SortType", "Size ▲");
//				}
//				updateTemp();
//				allName.setText("Name");
//				allDate.setText("Date");
//				allType.setText("Type");
//				Collections.sort(dataSourceL1, fileListSorter);
//				srcAdapter.notifyDataSetChanged();
//				break;
//			case R.id.icons:
//				mainmenu(p1);
//				break;
//			case R.id.search:
//				searchButton();
//				break;
//			case R.id.clear:
//				searchET.setText("");
//				break;
//		}
//	}
//
//	void updateL2() {
//		final String order = activity != null ? AndroidUtils.getSharedPreference(activity, "ContentFrag2SortType", "Name ▲"): "Name ▲";
//		allName.setText("Name");
//		allSize.setText("Size");
//		allDate.setText("Date");
//		allType.setText("Type");
//		switch (order) {
//			case "Name ▼":
//				fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.NAME, FileListSorter.DESCENDING);
//				allName.setText("Name ▼");
//				break;
//			case "Date ▲":
//				fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.DATE, FileListSorter.ASCENDING);
//				allDate.setText("Date ▲");
//				break;
//			case "Date ▼":
//				fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.DATE, FileListSorter.DESCENDING);
//				allDate.setText("Date ▼");
//				break;
//			case "Size ▲":
//				fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.SIZE, FileListSorter.ASCENDING);
//				allSize.setText("Size ▲");
//				break;
//			case "Size ▼":
//				fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.SIZE, FileListSorter.DESCENDING);
//				allSize.setText("Size ▼");
//				break;
//			case "Type ▲":
//				fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.TYPE, FileListSorter.ASCENDING);
//				allType.setText("Type ▲");
//				break;
//			case "Type ▼":
//				fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.TYPE, FileListSorter.DESCENDING);
//				allType.setText("Type ▼");
//				break;
//			default:
//				fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.NAME, FileListSorter.ASCENDING);
//				allName.setText("Name ▲");
//				break;
//		}
//		updateTemp();
//		Collections.sort(dataSourceL1, fileListSorter);
//	}
//
//	void updateTemp() {
//		tempOriDataSourceL1.clear();
//		tempOriDataSourceL1.addAll(dataSourceL1);
//	}
//
//	void removeAllDS2(final Collection<LayoutElements> c) {
//		dataSourceL1.removeAll(c);
//		tempOriDataSourceL1.removeAll(c);
//	}
//
//	void clearDS2() {
//		dataSourceL1.clear();
//		tempOriDataSourceL1.clear();
//	}
//
////	void notifyDataSetChanged() {
////		srcAdapter.notifyDataSetChanged();
////	}
//
////	private class ArrAdapter extends RecyclerAdapter<File, ArrAdapter.ViewHolder> implements OnLongClickListener, OnClickListener {
////
////		private final int backgroundResource;
////
////		private class ViewHolder extends RecyclerView.ViewHolder {
////
////			TextView name;
////			TextView size;
////			TextView attr;
////			TextView lastModified;
////			TextView type;
////			ImageButton cbx;
////			ImageView image;
////			ImageButton more;
////			View convertedView;
////			
////			public ViewHolder(View convertView) {
////				super(convertView);
////				name = (TextView) convertView.findViewById(R.id.name);
////				size = (TextView) convertView.findViewById(R.id.items);
////				attr = (TextView) convertView.findViewById(R.id.attr);
////				lastModified = (TextView) convertView.findViewById(R.id.lastModified);
////				type = (TextView) convertView.findViewById(R.id.type);
////				cbx = (ImageButton) convertView.findViewById(R.id.cbx);
////				image = (ImageView)convertView.findViewById(R.id.icon);
////				more = (ImageButton)convertView.findViewById(R.id.more);
////
////				convertView.setOnClickListener(ArrAdapter.this);
////				cbx.setOnClickListener(ArrAdapter.this);
////				image.setOnClickListener(ArrAdapter.this);
////				more.setOnClickListener(ArrAdapter.this);
////
////				convertView.setOnLongClickListener(ArrAdapter.this);
////				cbx.setOnLongClickListener(ArrAdapter.this);
////				image.setOnLongClickListener(ArrAdapter.this);
////				more.setOnLongClickListener(ArrAdapter.this);
////
////				more.setColorFilter(ExplorerActivity.TEXT_COLOR);
////
////				name.setTextColor(ExplorerActivity.DIR_COLOR);
////				size.setTextColor(ExplorerActivity.TEXT_COLOR);
////				attr.setTextColor(ExplorerActivity.TEXT_COLOR);
////				lastModified.setTextColor(ExplorerActivity.TEXT_COLOR);
////				type.setTextColor(ExplorerActivity.TEXT_COLOR);
////
////				image.setScaleType(ImageView.ScaleType.FIT_CENTER);
////				convertView.setTag(this);
////				this.convertedView = convertView;
////			}
////		}
////
////		// Provide a suitable constructor (depends on the kind of dataset)
////		public ArrAdapter(ArrayList<File> objects) {
////			super(objects);
////			Log.d(TAG, "ArrAdapter " + objects);
////			int[] attrs = new int[]{android.R.attr.selectableItemBackground};
////			TypedArray typedArray = getActivity().obtainStyledAttributes(attrs);
////			backgroundResource = typedArray.getResourceId(0, 0);
////			typedArray.recycle();
////		}
////
////		@Override
////		public int getItemViewType(int position) {
////			if (dataSourceL1.size() == 0) {
////				return 0;
////			} else if (spanCount == 1 || (spanCount == 2 && activity.left.getVisibility() == View.GONE)) {
////				return 1;
////			} else if (spanCount == 2) {
////				return 2;
////			} else {
////				return 3;
////			}
////		}
////
////		@Override
////		public ArrAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
////														int viewType) {
////			// create a new view
////			View v;
////			if (viewType <= 1) {
////				v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
////			} else {
////				if (viewType == 2) {
////					v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_small, parent, false);
////				} else {
////					v = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
////				}
////			}
////			ViewHolder vh = new ViewHolder(v);
////			return vh;
////		}
////
////		// Replace the contents of a view (invoked by the layout manager)
////		@Override
////		public void onBindViewHolder(ViewHolder holder, int position) {
////			// - get element from your dataset at this position
////			// - replace the contents of the view with that element
////			//final File name = mDataset.get(position);
////			File f = mDataset.get(position);
////
////			final String fPath = f.getAbsolutePath();
////			holder.name.setText(fPath);
////
////			holder.image.setContentDescription(fPath);
////			holder.name.setContentDescription(fPath);
////			holder.size.setContentDescription(fPath);
////			holder.attr.setContentDescription(fPath);
////			holder.lastModified.setContentDescription(fPath);
////			holder.type.setContentDescription(fPath);
////			holder.cbx.setContentDescription(fPath);
////			holder.more.setContentDescription(fPath);
////			holder.more.setTag(holder);
////			holder.convertedView.setContentDescription(fPath);
////
////			holder.name.setEllipsize(TextUtils.TruncateAt.START);
////
//////	        if (!f.exists()) {
//////				holder.convertedView.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
//////				dataSourceL2.remove(f);
//////				tempOriDataSourceL2.remove(f);
//////				selectedInList2.remove(f);
//////				notifyItemRemoved(position);
//////				return;// convertView;
//////			}
////
////			if (selectedInList1.contains(f)) {
////				holder.convertedView.setBackgroundColor(ExplorerActivity.SELECTED_IN_LIST);
////				holder.cbx.setImageResource(R.drawable.ic_accept);
////				holder.cbx.setSelected(true);
////				holder.cbx.setEnabled(true);
////				if (selectedInList1.size() == dataSourceL1.size()) {
////					allCbx.setSelected(true);
////					allCbx.setImageResource(R.drawable.ic_accept);
////				}
////	        } else {
////				holder.convertedView.setBackgroundResource(backgroundResource);
////				if (selectedInList1.size() > 0) {
////					holder.cbx.setImageResource(R.drawable.ready);
////					allCbx.setImageResource(R.drawable.ready);
////				} else {
////					holder.cbx.setImageResource(R.drawable.dot);
////					allCbx.setImageResource(R.drawable.dot);
////				}
////				holder.cbx.setSelected(false);
////				holder.cbx.setEnabled(true);
////				allCbx.setSelected(false);
////	        }
////			if (tempPreviewL2 != null && tempPreviewL2.equals(f)) {
////				holder.convertedView.setBackgroundColor(ExplorerActivity.LIGHT_GREY);
////			}
////
////			boolean canRead = f.canRead();
////			boolean canWrite = f.canWrite();
////			if (!f.isDirectory()) {
////				long length = f.length();
////				holder.size.setText(Util.nf.format(length) + " B");
////				String st;
////				if (canWrite) {
////					st = "-rw";
////				} else if (canRead) {
////					st = "-r-";
////				} else {
////					st = "---";
////					holder.cbx.setEnabled(false);
////				}
////				String namef = f.getName();
////				int lastIndexOf = namef.lastIndexOf(".");
////				holder.type.setText(lastIndexOf >= 0 && lastIndexOf < namef.length() - 1 ? namef.substring(lastIndexOf + 1) : "");
////				holder.attr.setText(st);
////				holder.lastModified.setText(Util.dtf.format(f.lastModified()));
////			} else {
////				String[] list = f.list();
////				int length = list == null ? 0 : list.length;
////				holder.size.setText(Util.nf.format(length) + " item");
////				String st;
////				if (canWrite) {
////					st = "drw";
////				} else if (canRead) {
////					st = "dr-";
////				} else {
////					st = "d--";
////					holder.cbx.setEnabled(false);
////				}
////				holder.type.setText("Folder");
////				holder.attr.setText(st);
////				holder.lastModified.setText(Util.dtf.format(f.lastModified()));
////			}
////			imageLoader.displayImage(f, getContext(), holder.image, spanCount);
////		}
////
////		public boolean rename(final View more) {
////			final File oldF = new File((String)more.getContentDescription());
////			final String oldPath = oldF.getAbsolutePath();
////			Log.d(TAG, "oldPath " + oldPath + ", " + more);
////			final EditText editText = new EditText(activity);
////			editText.setText(oldF.getName());
////			final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
////				LinearLayout.LayoutParams.MATCH_PARENT,
////				LinearLayout.LayoutParams.WRAP_CONTENT);
////			layoutParams.gravity = Gravity.CENTER;
////			editText.setLayoutParams(layoutParams);
////			editText.setSingleLine(true);
////			editText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
////			editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
////			editText.setMinEms(2);
////			//editText.setGravity(Gravity.CENTER);
////			final int density = 8 * (int)getResources().getDisplayMetrics().density;
////			editText.setPadding(density, density, density, density);
////
////			AlertDialog dialog = new AlertDialog.Builder(activity)
////				.setIconAttribute(android.R.attr.dialogIcon)
////				.setTitle("New Name")
////				.setView(editText)
////				.setPositiveButton(R.string.ok,
////				new DialogInterface.OnClickListener() {
////					public void onClick(DialogInterface dialog, int whichButton) {
////						String name = editText.getText().toString();
////						File newF = new File(oldF.getParent(), name);
////						String newPath = newF.getAbsolutePath();
////						Log.d(TAG, "newF " + newPath);
////						if (newF.exists()) {
////							showToast("\"" + newF + "\" is existing. Please choose another name");
////						} else {
////							boolean ok = AndroidPathUtils.renameFolder(oldF, newF, ContentFragment2.this.getContext());//oldF.renameTo(newF);
////							if (ok) {
////								ViewHolder holder = (ViewHolder)more.getTag();
////								TextView nameTV = holder.name;
////								Log.d(TAG, "nameTV " + nameTV);
////								int i = 0;
////								for (File fn : dataSourceL1) {
////									if (fn.getAbsolutePath().equals(oldPath)) {
////										dataSourceL1.set(i, newF);
////										tempOriDataSourceL1.set(i, newF);
////									}
////									i++;
////								}
////								srcAdapter.notifyDataSetChanged();
////
////								nameTV.setContentDescription(newPath);
////								holder.size.setContentDescription(newPath);
////								holder.attr.setContentDescription(newPath);
////								holder.lastModified.setContentDescription(newPath);
////								holder.type.setContentDescription(newPath);
////								holder.cbx.setContentDescription(newPath);
////								holder.image.setContentDescription(newPath);
////								more.setContentDescription(newPath);
////								holder.convertedView.setContentDescription(newPath);
////								showToast("Rename successfully");
////							} else {
////								showToast("Rename unsuccessfully");
////							}
////							dialog.dismiss();
////						}
////					}
////				})
////				.setNegativeButton(R.string.cancel,
////				new DialogInterface.OnClickListener() {
////					public void onClick(DialogInterface dialog, int whichButton) {
////						dialog.dismiss();
////					}
////				}).create();
////			dialog.show();
////			return true;
////		}
////
////		public boolean copy(final View item) {
////			activity.mode = OpenMode.FILE;
////			activity.copyl.clear();
////			activity.cutl.clear();
////			final ArrayList<File> al = new ArrayList<>(1);
////			al.add(new File((String)item.getContentDescription()));
////			activity.copyl.addAll(al);
////			return true;
////		}
////
////		public boolean cut(final View item) {
////			activity.mode = OpenMode.FILE;
////			activity.copyl.clear();
////			activity.cutl.clear();
////			final ArrayList<File> al = new ArrayList<>(1);
////			al.add(new File((String)item.getContentDescription()));
////			activity.cutl.addAll(al);
////			return true;
////		}
////
////		public boolean delete(final View item) {
////			final ArrayList<String> arr = new ArrayList<>();
////			final String contentDescription = (String)item.getContentDescription();
////			arr.add(contentDescription);
////			//new Futils().deleteFiles(arr, activity);
////			final File file = new File(contentDescription);
////			dataSourceL1.remove(file);
////			tempOriDataSourceL1.remove(file);
////			selectedInList1.remove(file);
////			return true;
////		}
////
////		public boolean share(View item) {
////			ArrayList<File> arrayList = new ArrayList<File>();
////			arrayList.add(new File((String)item.getContentDescription()));
////			new Futils().shareFiles(arrayList, activity, theme1, Color.BLUE);
////			return true;
////		}
////
////		public boolean send(View item) {
////			File f = new File((String)item.getContentDescription());
////			Uri uri = Uri.fromFile(f);
////			Intent i = new Intent(Intent.ACTION_SEND);
////			i.setFlags(0x1b080001);
////			i.setData(uri);
////			Log.d("i.setData(uri)", uri + "." + i);
////
////			String mimeType = MimeTypes.getMimeType(f);
////			i.setDataAndType(uri, mimeType);
////			Log.d(TAG, f + " = " + mimeType);
////
////			Log.d("send", i + ".");
////			Log.d("send.getExtras()", AndroidUtils.bundleToString(i.getExtras()));
////			Intent createChooser = Intent.createChooser(i, "Send via..");
////			Log.d("createChooser", createChooser + ".");
////			Log.d("createChooser.getExtras()", AndroidUtils.bundleToString(createChooser.getExtras()));
////			startActivity(createChooser);
////			return true;
////		}
////
////		public boolean copyName(final View item) {
////			final String data = new File((String)item.getContentDescription()).getName();
////			AndroidUtils.copyToClipboard(getContext(), data);
////			return true;
////		}
////
////		public boolean copyPath(final View item) {
////			final String data = new File((String)item.getContentDescription()).getParent();
////			AndroidUtils.copyToClipboard(getContext(), data);
////			return true;
////		}
////
////		public boolean copyFullName(final View item) {
////			final String data = (String)item.getContentDescription();
////			AndroidUtils.copyToClipboard(getContext(), data);
////			return true;
////		}
////
////		public void onClick(final View v) {
////			select(true);
////			if (dataSourceL1 == null || dataSourceL1.size() == 0) {
////				return;
////			}
////			if (v.getId() == R.id.more) {
////				final MenuBuilder menuBuilder = new MenuBuilder(activity);
////				final MenuInflater inflater = new MenuInflater(activity);
////				inflater.inflate(R.menu.file_commands, menuBuilder);
////				final MenuPopupHelper optionsMenu = new MenuPopupHelper(activity , menuBuilder, allSize);
////				optionsMenu.setForceShowIcon(true);
////
////				menuBuilder.setCallback(new MenuBuilder.Callback() {
////						@Override
////						public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
////							switch (item.getItemId()) {
////								case R.id.copy:
////									copy(v);
////									break;
////								case R.id.cut:
////									cut(v);
////									break;
////								case R.id.rename:
////									rename(v);
////									break;
////								case R.id.delete:
////									delete(v);
////									break;
////								case R.id.share:
////									share(v);
////									break;
////								case R.id.send:
////									send(v);
////									break;
////								case R.id.name:
////									copyName(v);
////									break;
////								case R.id.path:
////									copyPath(v);
////									break;
////								case R.id.fullname:
////									copyFullName(v);
////									break;
////							}
////							return false ;
////						}
////						@Override
////						public void onMenuModeChange(MenuBuilder menu) {}
////					});
////				optionsMenu.show();
////				return;
////			}
////
////			final String fPath = (String) v.getContentDescription();
////			Log.d(TAG, "onClick, " + fPath + ", " + v);
////			if (fPath == null) {
////				return;
////			}
////			final File f = new File(fPath);
////			Log.d(TAG, "currentSelectedList " + Util.collectionToString(selectedInList1, true, "\r\n"));
////			Log.d(TAG, "selectedInList.contains(f)" + selectedInList1.contains(f));
////			Log.d(TAG, "f.exists() " + f.exists());
////			if (f.exists()) {
////				if (!f.canRead()) {
////					showToast(f + " cannot be read");
////				} else {
////					if (v.getId() == R.id.icon) {
////						tempPreviewL2 = f;
////						if (f.isFile()) {
////							load(f, fPath);
////						} else {
////							activity.curContentFrag.changeDir(f, true);
////						}
////						if (selectedInList1.size() > 0) {
////							if (selectedInList1.contains(f)) {
////								selectedInList1.remove(f);
////								if (selectedInList1.size() == 0 && activity.copyl.size() == 0 && activity.cutl.size() == 0 && activity.leftCommands.getVisibility() == View.VISIBLE) {
////									activity.horizontalDivider11.setVisibility(View.GONE);
////									activity.leftCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
////									activity.leftCommands.setVisibility(View.GONE);
////								}
////							} else {
////								selectedInList1.add(f);
////							}
////						}
////					} else if ((v.getId() == R.id.cbx) || f.isDirectory()) {
////						if (selectedInList1.contains(f)) {
////							selectedInList1.remove(f);
////							if (selectedInList1.size() == 0 && rightCommands.getVisibility() == View.VISIBLE) {
////								horizontalDivider6.setVisibility(View.GONE);
////								rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
////								rightCommands.setVisibility(View.GONE);
////							} 
////						} else {
////							selectedInList1.add(f);
////							if (rightCommands.getVisibility() == View.GONE) {
////								rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
////								rightCommands.setVisibility(View.VISIBLE);
////								horizontalDivider6.setVisibility(View.VISIBLE);
////							}
////						}
////					} else if (f.isFile()) { //file
////						try {
////							Uri uri = Uri.fromFile(f);
////							Intent i = new Intent(Intent.ACTION_VIEW); 
////							i.addCategory(Intent.CATEGORY_DEFAULT);
////							i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
////							i.setData(uri);
////							Log.d("i.setData(uri)", uri + "." + i);
////
////							String mimeType = MimeTypes.getMimeType(f);
////							i.setDataAndType(uri, mimeType);
////							Log.d(TAG, f + " = " + mimeType);
////							Intent createChooser = Intent.createChooser(i, "View");
////							Log.d("createChooser.getExtras()", AndroidUtils.bundleToString(createChooser.getExtras()));
////							startActivity(createChooser);
////						} catch (Throwable e) {
////							Toast.makeText(activity, "unable to view !\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
////						}
////					}
////
////					selectionStatus1.setText(selectedInList1.size() + "/" + dataSourceL1.size());
////					notifyDataSetChanged();
////				}
////			}
////		}
////		
////		private void load(final File f, final String fPath) throws IllegalStateException {
////			final String mime = MimeTypes.getMimeType(f);
////			Log.d(TAG, fPath + "=" + mime);
////			int i = 0;
////			final SlidingTabsFragment2.PagerAdapter pagerAdapter = activity.slideFrag2.pagerAdapter;
////			if (mime.startsWith("text/html") || mime.startsWith("text/xhtml")) {
////				pagerAdapter.getItem(i = Frag.TYPE.TEXT.ordinal()).load(fPath);
////				pagerAdapter.getItem(i = Frag.TYPE.WEB.ordinal()).load(fPath);
////			} else if (mime.startsWith("application/vnd.android.package-archive")) {
////				final StringBuilder sb = new StringBuilder(ExplorerActivity.DOCTYPE);
////				try {
////					ApkParser apkParser = new ApkParser(f);
////					sb.append(AndroidUtils.getSignature(getContext(), fPath));
////					sb.append("\nVerify apk " + apkParser.verifyApk());
////					sb.append("\nMeta data " + apkParser.getApkMeta());
////
////					String sb1 = sb.toString();
////
////					String sb2 = "\nAndroidManifest.xml \n" + apkParser.getManifestXml().replaceAll("&", "&amp;")
////						.replaceAll("\"", "&quot;")
////						.replaceAll("'", "&#39;")
////						.replaceAll("<", "&lt;")
////						.replaceAll(">", "&gt;");
////					sb.append(sb2);
////					sb.append(ExplorerActivity.END_PRE);
////					final String name = ExplorerApplication.PRIVATE_PATH + "/" + f.getName() + ".html";
////					FileUtil.writeFileAsCharset(new File(name), sb.toString(), "utf-8");
////					pagerAdapter.getItem(i = Frag.TYPE.WEB.ordinal()).load(name);
////					byte[] bytes = FileUtil.readFileToMemory(f);
////					new FillClassesNamesThread(activity, bytes, f, sb1, sb2, ExplorerActivity.END_PRE).start();
////				} catch (Throwable e) {
////					e.printStackTrace();
////				}
////			} else if (mime.startsWith("application/pdf")) {
////				pagerAdapter.getItem(i = Frag.TYPE.PDF.ordinal()).load(fPath);
////			} else if (mime.startsWith("image/svg+xml")) {
////				pagerAdapter.getItem(i = Frag.TYPE.TEXT.ordinal()).load(fPath);
////				pagerAdapter.getItem(i = Frag.TYPE.PHOTO.ordinal()).load(fPath);
////			} else if (mime.startsWith("text")) {
////				pagerAdapter.getItem(i = Frag.TYPE.TEXT.ordinal()).load(fPath);
////			} else if (mime.startsWith("video")) {
////				pagerAdapter.getItem(i = Frag.TYPE.MEDIA.ordinal()).load(fPath);
////			} else if (mime.startsWith("image")) {
////				pagerAdapter.getItem(i = Frag.TYPE.PHOTO.ordinal()).load(fPath);
////			} else if (mime.startsWith("audio")) {
////				pagerAdapter.getItem(i = Frag.TYPE.MEDIA.ordinal()).load(fPath);
////			} else {
////				tempPreviewL2 = null;
////			}
////			activity.slideFrag2.mViewPager.setCurrentItem(i, true);
////		}
////
////		public boolean onLongClick(final View v) {
////			select(true);
////			final String fPath = (String) v.getContentDescription();
////			final File f = new File(fPath);
////
////			Log.d(TAG, "onLongClick, " + fPath);
////			Log.d(TAG, "currentSelectedList" + Util.collectionToString(selectedInList1, true, "\r\n"));
////			Log.d(TAG, "selectedInList.contains(f) " + selectedInList1.contains(f));
////
////			if (!f.exists()) {
////				selectedInList1.remove(f);
////			} else if (!f.canRead()) {
////				showToast(f + " cannot be read");
////				return true;
////			} else {
////				if (selectedInList1.contains(f)) {
////					selectedInList1.remove(f);
////					if (selectedInList1.size() == 0 && rightCommands.getVisibility() == View.VISIBLE) {
////						horizontalDivider6.setVisibility(View.GONE);
////						rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
////						rightCommands.setVisibility(View.GONE);
////					} 
////				} else {
////					selectedInList1.add(f);
////					if (rightCommands.getVisibility() == View.GONE) {
////						rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
////						rightCommands.setVisibility(View.VISIBLE);
////						horizontalDivider6.setVisibility(View.VISIBLE);
////					}
////				}
////			}
////			selectionStatus1.setText(selectedInList1.size() + "/" + dataSourceL1.size());
////			notifyDataSetChanged();
////			return true;
////		}
////	}
//}
//