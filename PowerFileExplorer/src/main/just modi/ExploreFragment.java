//package net.gnu.explorer;
//
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//import java.util.*;
//import android.widget.*;
//import android.os.*;
//import android.graphics.drawable.*;
//import android.content.*;
//import android.view.View.*;
//import android.util.*;
//import java.io.*;
//import android.text.*;
//import android.graphics.*;
//import android.app.*;
//import android.view.*;
//import android.net.*;
//import android.support.v4.app.FragmentTransaction;
//import android.support.v4.app.FragmentManager;
//import net.gnu.util.*;
//import net.gnu.androidutil.*;
//import net.gnu.explorer.R;
////import com.amaze.filemanager.ui.icons.*;
//import com.amaze.filemanager.utils.*;
//import com.amaze.filemanager.filesystem.BaseFile;
//import com.amaze.filemanager.filesystem.HFile;
//import com.amaze.filemanager.activities.*;
//import com.amaze.filemanager.services.asynctasks.*;
//import android.preference.*;
//import com.tekinarslan.sample.*;
//import net.gnu.texteditor.*;
//import android.support.v4.view.*;
//
//import android.view.animation.*;
//import android.widget.LinearLayout.*;
//import android.support.v7.view.menu.*;
//import android.support.v7.widget.PopupMenu;
//import android.support.v7.widget.*;
//import android.content.res.*;
//import android.support.v4.widget.SwipeRefreshLayout;
//import net.gnu.explorer.ExploreFragment.*;
//import android.view.inputmethod.*;
//import net.dongliu.apk.parser.*;
//import android.support.v4.content.*;
//import com.amaze.filemanager.ui.icons.*;
//import com.amaze.filemanager.ui.LayoutElements;
//import com.amaze.filemanager.utils.FileListSorter;
//
///**
// * Simple Fragment used to display some meaningful content for each page in the sample's
// * {@link android.support.v4.view.ViewPager}.
// */
//public class ExploreFragment extends FileFrag implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
//
//	private static final String TAG = "ExploreFragment";
//	
//    //String dir = "";
//	//String suffix = ".*"; // ".*" : all file types,  "" only folder, "; *" split pattern
//	//boolean multiFiles = false;
//	
////	ArrayList<File> dataSourceL1 = new ArrayList<>();
////	ArrayList<File> selectedInList1 = new ArrayList<>();
////	ArrayList<File> tempSelectedInList1 = new ArrayList<>();
////	private File tempPreviewL2 = null;
//
////	private ImageButton allCbx;
////	private ImageButton icons;
////	private TextView allName;
////	private TextView allDate;
////	private TextView allSize;
////	private TextView allType;
////	TextView selectionStatus1;
////	private TextView diskStatus;
//
//	//RecyclerView listView1 = null;
//	//ArrAdapter srcAdapter;
//	//private ImageThreadLoader imageLoader;
//
//	private int theme1;
//	
//	
//    private Drawable drawableDelete;
//	private Drawable drawablePaste;
//	GridDividerItemDecoration dividerItemDecoration;
//	
//    private LoadFiles lf = new LoadFiles(this, OpenMode.FILE);
//	private boolean fake = false;
//	
//	private TextSearch textSearch = new TextSearch(this);
//	private View selStatus;
//	private InputMethodManager imm;
//	private ScaleGestureDetector mScaleGestureDetector;
//	private ImageButton dirMore;
//	
//	public ExploreFragment() {
//		super();
//		type = Frag.TYPE.EXPLORER.ordinal();
//	}
//
//	public void clone(final Frag fragO) {
//		final ExploreFragment frag = (ExploreFragment)fragO;
//		path = frag.path;
//		if (!fake) {
//			dataSourceL1 = frag.dataSourceL1;
//			selectedInList1 = frag.selectedInList1;
//			tempSelectedInList1 = frag.tempSelectedInList1;
//			tempPreviewL2 = frag.tempPreviewL2;
//			searchMode = frag.searchMode;
//			searchVal = frag.searchVal;
//			
//			dirTemp4Search = frag.dirTemp4Search;
//			srcAdapter = frag.srcAdapter;
//			if (listView1 != null && listView1.getAdapter() != srcAdapter) {
//				listView1.setAdapter(srcAdapter);
//			}
//			fake = true;
//		}
//	}
//
//	@Override
//	public String toString() {
//		return path + ", " + super.toString();
//	}
//
//    @Override
//	public void onCreate(android.os.Bundle savedInstanceState) {
//		Log.d(TAG, "onCreate " + savedInstanceState);
//		super.onCreate(savedInstanceState);
//	}
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//							 Bundle savedInstanceState) {
//        Log.d(TAG, "onCreateView " + path + ", " + savedInstanceState);
//		super.onCreateView(inflater, container, savedInstanceState);
//		View v = inflater.inflate(R.layout.pager_item, container, false);
//		return v;
//    }
//
//	@Override
//    public void onViewCreated(View v, Bundle savedInstanceState) {
//        Log.d(TAG, "onViewCreated " + path + ", " + savedInstanceState);
//		super.onViewCreated(v, savedInstanceState);
//		setRetainInstance(true);
//		
//		spanCount = AndroidUtils.getSharedPreference(getContext(), "SPAN_COUNT.ExplorerFrag", 1);
//		
//        allCbx.setOnClickListener(this);
//		icons.setOnClickListener(this);
//		allName.setOnClickListener(this);
//		allDate.setOnClickListener(this);
//		allSize.setOnClickListener(this);
//		allType.setOnClickListener(this);
//		
//		Bundle args = getArguments();
//		Log.d(TAG, "onViewCreated " + path + ", " + "args=" + args);
//
//		selStatus = v.findViewById(R.id.selStatus);
//		dirMore = (ImageButton) v.findViewById(R.id.dirMore);
//		
//		drawableDelete = activity.getDrawable(R.drawable.ic_action_delete);
//		drawablePaste = activity.getDrawable(R.drawable.ic_action_paste);
//
//		listView1 = (RecyclerView) v.findViewById(R.id.files);
//		listView1.addOnScrollListener(new RecyclerView.OnScrollListener() {
//				public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//					//Log.d(TAG, "onScrolled dx=" + dx + ", dy=" + dy + ", density=" + activity.density);
//					if (dy > activity.density << 3 && selStatus.getVisibility() == View.VISIBLE) {
//						selStatus.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_bottom));
//						selStatus.setVisibility(View.GONE);
//						horizontalDivider0.setVisibility(View.GONE);
//						horizontalDivider12.setVisibility(View.GONE);
//						status.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_bottom));
//						status.setVisibility(View.GONE);
//					} else if (dy < -activity.density << 3 && selStatus.getVisibility() == View.GONE) {
//						selStatus.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
//						selStatus.setVisibility(View.VISIBLE);
//						horizontalDivider0.setVisibility(View.VISIBLE);
//						horizontalDivider12.setVisibility(View.VISIBLE);
//						status.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
//						status.setVisibility(View.VISIBLE);
//					}
//				}
//			});
//		
//		//listView1.setOnTouchListener(this);
//		listView1.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
//		listView1.setHasFixedSize(true);
//		//dividerItemDecoration = new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST, true, true);
//        
//        listView1.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                   	//Log.d(TAG, "onTouch " + event);
//					activity.slideFrag2.getCurrentFragment2().select(true);
//					mScaleGestureDetector.onTouchEvent(event);
//                    return false;
//                }
//            });
//
//		//mSwipeRefreshLayout.setColorSchemeColors(Color.parseColor(fabSkin));
//        DefaultItemAnimator animator = new DefaultItemAnimator();
//        animator.setAddDuration(500);
//		animator.setRemoveDuration(500);
//        listView1.setItemAnimator(animator);
//		
//		
//		dirMore.setOnClickListener(this);
//		
//		searchButton.setColorFilter(ExplorerActivity.TEXT_COLOR);
//		clearButton.setOnClickListener(this);
//		searchButton.setOnClickListener(this);
//		quicksearch.addTextChangedListener(textSearch);
//		mSwipeRefreshLayout.setOnRefreshListener(this);
//		imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
//		mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
//                @Override
//                public boolean onScale(ScaleGestureDetector detector) {
//                    Log.d(TAG, "onScale getCurrentSpan " + detector.getCurrentSpan() + ", getPreviousSpan " + detector.getPreviousSpan() + ", getTimeDelta " + detector.getTimeDelta());
//					if (detector.getCurrentSpan() > 200 && detector.getTimeDelta() > 50) {
//                        Log.d(TAG, "onScale " + (detector.getCurrentSpan() - detector.getPreviousSpan()) + ", getTimeDelta " + detector.getTimeDelta());
//						if (detector.getCurrentSpan() - detector.getPreviousSpan() < -1) {
//                            if (spanCount == 1) {
//								spanCount = 2;
//                                setRecyclerViewLayoutManager();
//								return true;
//                            } else if (spanCount == 2) {
//								if (activity.left.getVisibility() == View.GONE) {
//									spanCount = 8;
//								} else {
//									spanCount = 4;
//								}
//								setRecyclerViewLayoutManager();
//								return true;
//                            }
//                        } else if(detector.getCurrentSpan() - detector.getPreviousSpan() > 1) {
//                            if ((spanCount == 4 || spanCount == 8)) {
//								spanCount = 2;
//                                setRecyclerViewLayoutManager();
//								return true;
//                            } else if (spanCount == 2) {
//								spanCount = 1;
//								  setRecyclerViewLayoutManager();
//								return true;
//                            } 
//                        }
//                    }
//                    return false;
//                }
//            });
//		if (args != null) {
//			path = args.getString("path");//ExplorerActivity.EXTRA_DIR_PATH);
//			//Log.d(TAG, "onViewCreated.dir " + dir);
//			if (savedInstanceState == null && args.getStringArrayList("dataSourceL1") != null) {
//				savedInstanceState = args;
//			}
//        }
//		
//		allName.setText("Name");
//		allSize.setText("Size");
//		allDate.setText("Date");
//		allType.setText("Type");
//		final String order = activity != null ? AndroidUtils.getSharedPreference(activity, "explorerSortType", "Name ▲"): "Name ▲";
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
//		Log.d(TAG, "onViewCreated savedInstanceState" + savedInstanceState);
//		if (savedInstanceState != null && savedInstanceState.getString("path") != null) {
//			//title = savedInstanceState.getString("title");
//			//path = savedInstanceState.getString("path");
//			
////			selectedInList1.addAll(Util.collectionString2FileArrayList(savedInstanceState.getStringArrayList("selectedInList1")));
////			
////			dataSourceL1.addAll(Util.collectionString2FileArrayList(savedInstanceState.getStringArrayList("dataSourceL1")));
////			
////			searchMode = savedInstanceState.getBoolean("searchMode", false);
////			searchVal = savedInstanceState.getString("searchVal", "");
////			dirTemp4Search = savedInstanceState.getString("dirTemp4Search", "");
////			
////			if (savedInstanceState.getString("tempPreviewL2") != null) {
////				tempPreviewL2 = new File(savedInstanceState.getString("tempPreviewL2"));
////			}
////			
//			setRecyclerViewLayoutManager();
//			updateDir(path, ExploreFragment.this);
//			final int index  = savedInstanceState.getInt("index");
//			final int top  = savedInstanceState.getInt("top");
//			Log.d(TAG, "index = " + index + ", " + top);
//			gridLayoutManager.scrollToPositionWithOffset(index, top);
//			
//		} else {
//			setRecyclerViewLayoutManager();
//			changeDir(new File(path), false);
//		}
////		Sp = PreferenceManager.getDefaultSharedPreferences(getContext());
////		int theme = Sp.getInt("theme", 0);
////		theme1 = theme == 2 ? PreferenceUtils.hourOfDay() : theme;
//		updateColor(null);
//		if (selectedInList1.size() == 0 && activity.copyl.size() == 0 && activity.cutl.size() == 0) {
//			if (activity.rightCommands.getVisibility() == View.VISIBLE) {
//				activity.horizontalDivider6.setVisibility(View.GONE);
//				activity.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
//				activity.rightCommands.setVisibility(View.GONE);
//			}
//		} else {
//			activity.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
//			activity.rightCommands.setVisibility(View.VISIBLE);
//			activity.horizontalDivider6.setVisibility(View.VISIBLE);
//		}
//    }
//
//	@Override
//    public void onRefresh() {
//		Log.d(TAG, "onRefresh " + quicksearch.getText() + ", " + path);
//		final Editable s = quicksearch.getText();
//		if (s.length() > 0) {
//			textSearch.afterTextChanged(s);
//		} else {
//        	changeDir(new File(path), false);
//		}
//    }
//
//	@Override
//	public void onSaveInstanceState(android.os.Bundle outState) {
//		Log.d(TAG, "onSaveInstanceState " + path + ", " + outState);
//		if (!fake) {
//			AndroidUtils.setSharedPreference(getContext(), "SPAN_COUNT.ExplorerFrag", spanCount);
////			outState.putString("path", path);
////			outState.putString("title", title);
////			if (tempPreviewL2 != null) {
////				outState.putString("tempPreviewL2", tempPreviewL2.getAbsolutePath());
////			}
////			
////			outState.putStringArrayList("selectedInList1", Util.collectionFile2StringArrayList(selectedInList1));
////			outState.putStringArrayList("dataSourceL1", Util.collectionFile2StringArrayList(dataSourceL1));
////			outState.putBoolean("searchMode", searchMode);
////			outState.putString("searchVal", quicksearch.getText().toString());
////			outState.putString("dirTemp4Search", dirTemp4Search);
//			
//			int index = gridLayoutManager.findFirstVisibleItemPosition();
//			final View vi = listView1.getChildAt(0); 
//			final int top = (vi == null) ? 0 : vi.getTop();
//			outState.putInt("index", index);
//			outState.putInt("top", top);
//			//Log.d(TAG, "onSaveInstanceState index = " + index + ", " + top);
//			
//		}
//		super.onSaveInstanceState(outState);
//	}
//
////	private void reload(Map<String, Object> savedInstanceState) {
////		path = (String) savedInstanceState.get("path");
////		
////		ArrayList<LayoutElements> stringArrayList = (ArrayList<LayoutElements>) savedInstanceState.get("selectedInList1");
////		selectedInList1.clear();
////		selectedInList1.addAll(stringArrayList);
////		
////		stringArrayList = (ArrayList<LayoutElements>) savedInstanceState.get("dataSourceL1");
////		dataSourceL1.clear();
////		dataSourceL1.addAll(stringArrayList);
////		
////		searchMode = savedInstanceState.get("searchMode");
////		searchVal = (String) savedInstanceState.get("searchVal");
////		dirTemp4Search = (String) savedInstanceState.get("dirTemp4Search");
////		srcAdapter.notifyDataSetChanged();
////		
////		setRecyclerViewLayoutManager();
////		gridLayoutManager.scrollToPositionWithOffset(savedInstanceState.get("index"), savedInstanceState.get("top"));
////		
////		updateDir(path, ExploreFragment.this);
////	}
//	
//	@Override
//	public void onViewStateRestored(Bundle savedInstanceState) {
//		Log.d(TAG, "onViewStateRestored " + savedInstanceState);
//		if (imageLoader == null) {
//			activity = (ExplorerActivity)getActivity();
//			imageLoader = new ImageThreadLoader(activity);
//		}
//		super.onViewStateRestored(savedInstanceState);
//	}
////
////	public Map<String, Object> onSaveInstanceState() {
////		Map<String, Object> outState = new TreeMap<>();
////		Log.d(TAG, "onSaveInstanceState " + path + ", " + outState);
////		outState.put("path", path);
////		
////		outState.put("selectedInList1", selectedInList1);
////		outState.put("dataSourceL1", dataSourceL1);
////		outState.put("searchMode", searchMode);
////		outState.put("searchVal", quicksearch.getText().toString());
////		outState.put("dirTemp4Search", dirTemp4Search);
////		int index = gridLayoutManager.findFirstVisibleItemPosition();
////		
////        final View vi = listView1.getChildAt(0); 
////        final int top = (vi == null) ? 0 : vi.getTop();
////		outState.put("index", index);
////		outState.put("top", top);
////		return outState;
////	}
////	
////	@Override
////	public void onActivityCreated(Bundle savedInstanceState) {
////		Log.d(TAG, "onActivityCreated " + savedInstanceState);
////		super.onActivityCreated(savedInstanceState);
////	}
//
//	@Override
//    public void onPause() {
//        Log.d(TAG, "onPause " + path);
//		super.onPause();
//		if (imageLoader != null) {
//			imageLoader.stopThread();
//		}
//		lf.cancel(true);
//        if (mFileObserver != null) {
//            mFileObserver.stopWatching();
//        }
//    }
//
//	@Override
//	public void onStop() {
//		Log.d(TAG, "onStop");
//		lf.cancel(true);
//		super.onStop();
//	}
//
////	@Override
////	public void onDestroyView() {
////		Log.d(TAG, "onDestroyView");
////		super.onDestroyView();
////	}
//
////	@Override
////	public void onDestroy() {
////		Log.d(TAG, "onDestroy");
////		super.onDestroy();
////	}
//
//    public void updateColor(View rootView) {
//		getView().setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
//		icons.setColorFilter(ExplorerActivity.TEXT_COLOR);
//		allName.setTextColor(ExplorerActivity.TEXT_COLOR);
//		allDate.setTextColor(ExplorerActivity.TEXT_COLOR);
//		allSize.setTextColor(ExplorerActivity.TEXT_COLOR);
//		allType.setTextColor(ExplorerActivity.TEXT_COLOR);
//		selectionStatus1.setTextColor(ExplorerActivity.TEXT_COLOR);
//		diskStatus.setTextColor(ExplorerActivity.TEXT_COLOR);
//		clearButton.setColorFilter(ExplorerActivity.TEXT_COLOR);
//		searchButton.setColorFilter(ExplorerActivity.TEXT_COLOR);
//		dirMore.setColorFilter(ExplorerActivity.TEXT_COLOR);
//		selectionStatus1.setText(selectedInList1.size() 
//								 + "/"+ dataSourceL1.size());
//		noFileText.setTextColor(ExplorerActivity.TEXT_COLOR);
//		noFileImage.setColorFilter(ExplorerActivity.TEXT_COLOR);
//		horizontalDivider0.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
//		horizontalDivider12.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
//		horizontalDivider7.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
//		
//		final File curDir = new File(path == null ? dirTemp4Search : path);
//		diskStatus.setText(
//			"Free " + Util.nf.format(curDir.getFreeSpace() / (1 << 20))
//			+ " MiB. Usable " + Util.nf.format(curDir.getUsableSpace() / (1 << 20))
//			+ " MiB. Total " + Util.nf.format(curDir.getTotalSpace() / (1 << 20)) + " MiB");
//	}
//
//	@Override
//    public void onResume() {
//        Log.d(TAG, "onResume dir" + path + ", dirTemp4Search=" + dirTemp4Search);
//		super.onResume();
//		
//		if (mFileObserver != null) {
//            mFileObserver.stopWatching();
//        }
//		if (path == null) {
//			mFileObserver = createFileObserver(dirTemp4Search);
//		} else {
//			mFileObserver = createFileObserver(path);
//		}
//		mFileObserver.startWatching();
//		activity = (ExplorerActivity)getActivity();
//		imageLoader = new ImageThreadLoader(activity);
//		
//	}
//
//	public void load(String path) {
//	}
//
//    @Override
//	public void onClick(final View p1) {
//		Log.d(TAG, selectedInList1 + " onClick.");
//		select(true);
//		switch (p1.getId()) {
//			case R.id.allCbx:
//				Log.d("allCbx", selectedInList1 + ".");
//				selectedInList1.clear();
//				if (!allCbx.isSelected()) {
//					allCbx.setSelected(true);
//					for (LayoutElements f : dataSourceL1) {
//						if (f.bf.f.canRead()) {
//							selectedInList1.add(f);
//						}
//					}
//					if (selectedInList1.size() > 0 && activity.rightCommands.getVisibility() == View.GONE) {
//						activity.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
//						activity.rightCommands.setVisibility(View.VISIBLE);
//						activity.horizontalDivider6.setVisibility(View.VISIBLE);
//					}
//				} else {
//					allCbx.setSelected(false);
//					if (selectedInList1.size() == 0 && activity.copyl.size() == 0 && activity.cutl.size() == 0 && activity.rightCommands.getVisibility() != View.GONE) {
//						activity.horizontalDivider6.setVisibility(View.GONE);
//						activity.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
//						activity.rightCommands.setVisibility(View.GONE);
//					}
//				}
//				selectionStatus1.setText(selectedInList1.size() 
//										 + "/"+ dataSourceL1.size());
//				srcAdapter.notifyDataSetChanged();
//				updateDelPaste();
//				break;
//			case R.id.allName:
//				if (allName.getText().toString().equals("Name ▲")) {
//					allName.setText("Name ▼");
//					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.NAME, FileListSorter.DESCENDING);
//					AndroidUtils.setSharedPreference(activity, "explorerSortType", "Name ▼");
//				} else {
//					allName.setText("Name ▲");
//					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.NAME, FileListSorter.ASCENDING);
//					AndroidUtils.setSharedPreference(activity, "explorerSortType", "Name ▲");
//				}
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
//					AndroidUtils.setSharedPreference(activity, "explorerSortType", "Type ▼");
//				} else {
//					allType.setText("Type ▲");
//					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.TYPE, FileListSorter.ASCENDING);
//					AndroidUtils.setSharedPreference(activity, "explorerSortType", "Type ▲");
//				}
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
//					AndroidUtils.setSharedPreference(activity, "explorerSortType", "Date ▼");
//				} else {
//					allDate.setText("Date ▲");
//					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.DATE, FileListSorter.ASCENDING);
//					AndroidUtils.setSharedPreference(activity, "explorerSortType", "Date ▲");
//				}
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
//					AndroidUtils.setSharedPreference(activity, "explorerSortType", "Size ▼");
//				} else {
//					allSize.setText("Size ▲");
//					fileListSorter = new FileListSorter(FileListSorter.DIR_TOP, FileListSorter.SIZE, FileListSorter.ASCENDING);
//					AndroidUtils.setSharedPreference(activity, "explorerSortType", "Size ▲");
//				}
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
//				quicksearch.setText("");
//				break;
//			case R.id.dirMore:
//				final MenuBuilder menuBuilder = new MenuBuilder(activity);
//				final MenuInflater inflater = new MenuInflater(activity);
//				inflater.inflate(R.menu.storage, menuBuilder);
//				final MenuPopupHelper optionsMenu = new MenuPopupHelper(activity , menuBuilder, dirMore);
//				optionsMenu.setForceShowIcon(true);
//				MenuItem mi = menuBuilder.findItem(R.id.otg);
//				if (true) {
//					mi.setVisible(true);
//					mi.getIcon().setFilterBitmap(true);
//					mi.getIcon().setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
//				} else {
//					mi.setVisible(false);
//				}
//				
//				mi = menuBuilder.findItem(R.id.addFolder);
//				mi.getIcon().setFilterBitmap(true);
//				mi.getIcon().setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
//
//				mi = menuBuilder.findItem(R.id.sdcard);
//				mi.getIcon().setFilterBitmap(true);
//				mi.getIcon().setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
//				
//				mi = menuBuilder.findItem(R.id.microsd);
//				if (new File("/storage/MicroSD").exists()) {
//					mi.setEnabled(true);
//				} else {
//					mi.setEnabled(false);
//				}
//
//				menuBuilder.setCallback(new MenuBuilder.Callback() {
//						@Override
//						public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
//							Log.d(TAG, item.getTitle() + ".");
//							switch (item.getItemId())  {
//								case R.id.sdcard:
//									changeDir(new File("/sdcard"), true);
//									break;
//								case R.id.microsd:
//									changeDir(new File("/storage/MicroSD"), true);
//									break;
//								case R.id.addFolder:
//									activity.addFolder(p1);
//									break;
//							}
//							return true;
//						}
//						@Override
//						public void onMenuModeChange(MenuBuilder menu) {}
//					});
//				optionsMenu.show();
//				break;
//		}
//	}
//
////	private class TextSearch implements TextWatcher {
////		public void beforeTextChanged(CharSequence s, int start, int end, int count) {
////		}
////
////		public void afterTextChanged(final Editable text) {
////			final String filesearch = text.toString();
////			Log.d(TAG, "afterTextChanged " + filesearch);
////			if (filesearch.length() > 0) {
////				if (searchTask != null
////					&& searchTask.getStatus() == AsyncTask.Status.RUNNING) {
////					searchTask.cancel(true);
////				}
////				if (!mSwipeRefreshLayout.isRefreshing()) {
////					mSwipeRefreshLayout.post(new Runnable() {
////							@Override
////							public void run() {
////								mSwipeRefreshLayout.setRefreshing(true);
////							}
////						});
////				}
////				searchTask = new SearchFileNameTask();
////				searchTask.execute(filesearch);
////			}
////		}
////
////		public void onTextChanged(CharSequence s, int start, int end, int count) {
////		}
////	}
//	
//	void refreshRecyclerViewLayoutManager() {
//		setRecyclerViewLayoutManager();
//		horizontalDivider0.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
//		horizontalDivider12.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
//		horizontalDivider7.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
//	}
//
////	void setRecyclerViewLayoutManager() {
////        Log.e(TAG, "setRecyclerViewLayoutManager " + gridLayoutManager);
////		if (listView1 == null) {
////			return;
////		}
////		int scrollPosition = 0, top = 0;
////        // If a layout manager has already been set, get current scroll position.
////        if (gridLayoutManager != null) {
////			scrollPosition = gridLayoutManager.findFirstVisibleItemPosition();
////			final View vi = listView1.getChildAt(0); 
////			top = (vi == null) ? 0 : vi.getTop();
////		}
////		gridLayoutManager = new GridLayoutManager(activity, spanCount);
////		listView1.setLayoutManager(gridLayoutManager);
////		srcAdapter = new ArrAdapter(this, dataSourceL1, activity.rightCommands, activity.horizontalDivider6);
////		listView1.setAdapter(srcAdapter);
////		listView1.removeItemDecoration(dividerItemDecoration);
////		if (spanCount <= 2) {
////			dividerItemDecoration = new GridDividerItemDecoration(activity, true);
////			//dividerItemDecoration = new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST, true, true);
////			listView1.addItemDecoration(dividerItemDecoration);
////		}
////		
////		gridLayoutManager.scrollToPositionWithOffset(scrollPosition, top);
////	}
//	
//	void trimBackStack() {
//		int size = backStack.size() / 2;
//		for (int i = 0; i < size; i++) {
//			backStack.remove(0);
//		}
//	}
//	
//	@Override
//	public void onLowMemory() {
//		Log.d(TAG, "onLowMemory " + Runtime.getRuntime().freeMemory());
//		super.onLowMemory();
//		trimBackStack();
//	}
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
//	public void changeDir(final File curDir, final boolean doScroll) {
//		Log.d(TAG, "changeDir1 " + curDir.getAbsolutePath());
//		lf.cancel(true);
//		//lf = new LoadFiles();
//		if (searchTask != null
//			&& searchTask.getStatus() == AsyncTask.Status.RUNNING) {
//			searchTask.cancel(true);
//		}
//		lf.execute(curDir, doScroll);
//		
//	}
//
////	private class LoadFiles extends AsyncTask<Object, String, Void> {
////		File curDir;
////		Boolean doScroll;
////		final ArrayList<LayoutElements> dataSourceL1a = new ArrayList<>();
////
////		@Override
////		protected void onPreExecute() {
////			if (!mSwipeRefreshLayout.isRefreshing()) {
////				mSwipeRefreshLayout.setRefreshing(true);
////			}
////		}
////
////		@Override
////		protected Void doInBackground(final Object... objs) {
////			curDir = (File) objs[0];
////			doScroll = (Boolean) objs[1];
////			
////			while (curDir != null && !curDir.exists()) {
////				publishProgress(curDir.getAbsolutePath() + " is not existed");
////				curDir = curDir.getParentFile();
////			}
////			if (curDir == null) {
////				publishProgress("Current directory is not existed. Change to root");
////				curDir = new File("/");
////			}
////			
////			final String curPath = curDir.getAbsolutePath();
////			if (!dirTemp4Search.equals(curPath)) {
////				if (backStack.size() > ExplorerActivity.NUM_BACK) {
////					backStack.remove(0);
////				}
////				final Map<String, Object> bun = onSaveInstanceState();
////				backStack.push(bun);
////				
////				history.remove(curPath);
////				if (history.size() > ExplorerActivity.NUM_BACK) {
////					history.remove(0);
////				}
////				history.push(curPath);
////				
////				activity.historyList.remove(curPath);
////				if (activity.historyList.size() > ExplorerActivity.NUM_BACK) {
////					activity.historyList.remove(0);
////				}
////				activity.historyList.push(curPath);
////				tempPreviewL2 = null;
////			}
////
////			path = curPath;
////			dirTemp4Search = path;
////			if (tempPreviewL2 != null && !tempPreviewL2.bf.f.exists()) {
////				tempPreviewL2 = null;
////			}
////			if (mFileObserver != null) {
////				mFileObserver.stopWatching();
////			}
////			mFileObserver = createFileObserver(path);
////			mFileObserver.startWatching();
////			final List<File> currentFileFolderListing = FileUtil.currentFileFolderListing(curDir);
////			for (File f : currentFileFolderListing) {
////				dataSourceL1a.add(new LayoutElements(f));
////			}
////			//dataSourceL1a.addAll(currentFileFolderListing);
////			//Log.d("filesListing", Util.collectionToString(files, true, "\r\n"));
////
////			Log.d(TAG, "changeDir dataSourceL1a.size=" + dataSourceL1a.size());
////			//String dirSt = dir.getText().toString();
////			return null;
////		}
////
////		protected void onProgressUpdate(String...values) {
////			showToast(values[0]);
////		}
////
////		protected void onPostExecute(Object result) {
////			diskStatus.setText(
////				"Free " + Util.nf.format(curDir.getFreeSpace() / (1 << 20))
////				+ " MiB. Usable " + Util.nf.format(curDir.getUsableSpace() / (1 << 20))
////				+ " MiB. Total " + Util.nf.format(curDir.getTotalSpace() / (1 << 20)) + " MiB");
////			Collections.sort(dataSourceL1a, fileListSorter);
////
////			dataSourceL1.clear();
////			dataSourceL1.addAll(dataSourceL1a);
////			srcAdapter.notifyDataSetChanged();
////			dataSourceL1a.clear();
////			selectedInList1.clear();
////			if (activity.copyl.size() == 0 && activity.cutl.size() == 0 && activity.rightCommands.getVisibility() == View.VISIBLE) {
////				activity.horizontalDivider6.setVisibility(View.GONE);
////				activity.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
////				activity.rightCommands.setVisibility(View.GONE);
////			}
////			//Log.d("changeDir dataSourceL1", Util.collectionToString(dataSourceL1, true, "\r\n"));
////			listView1.setActivated(true);
////			if (doScroll) {
////				listView1.scrollToPosition(0);
////			}
////
////			if (allCbx.isSelected()) {//}.isChecked()) {
////				selectionStatus1.setText(dataSourceL1.size() 
////										 + "/"+ dataSourceL1.size());
////			} else {
////				selectionStatus1.setText(selectedInList1.size() 
////										 + "/"+ dataSourceL1.size());
////			}
////
////			Log.d(TAG, "changeDir " + path + ", " + this);
////			updateDir((path != null) ? path : dirTemp4Search, ExploreFragment.this);
////			if (mSwipeRefreshLayout.isRefreshing()) {
////				mSwipeRefreshLayout.setRefreshing(false);
////			}
////			if (dataSourceL1.size() == 0) {
////				nofilelayout.setVisibility(View.VISIBLE);
////				mSwipeRefreshLayout.setVisibility(View.GONE);
////			} else {
////				nofilelayout.setVisibility(View.GONE);
////				mSwipeRefreshLayout.setVisibility(View.VISIBLE);
////			}
////		}
////	}
//
////	public void updateDir(String d, ExploreFragment cf) {
////		Log.d(TAG, "updateDir " + d);
////		setDirectoryButtons();
////		if (cf == activity.slideFrag2.getCurrentFragment2()) {
////			title = new File(d).getName();
////			activity.curExploreFrag = cf;
////			activity.slideFrag2.notifyTitleChange();
////		}
////	}
//
////	void setDirectoryButtons() {
////		Log.d(TAG, "setDirectoryButtons " + path);
////		//topflipper.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
////
////		if (path != null) {
////			mDirectoryButtons.removeAllViews();
////			String[] parts = path.split("/");
////
////			activity = (ExplorerActivity) getActivity();
////			TextView ib = new TextView(activity);
////			final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
////				LinearLayout.LayoutParams.WRAP_CONTENT,
////				LinearLayout.LayoutParams.WRAP_CONTENT);
////			layoutParams.gravity = Gravity.CENTER;
////			ib.setLayoutParams(layoutParams);
////			ib.setBackgroundResource(R.drawable.ripple);
////			ib.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
////			ib.setText("/");
////			ib.setTag("/");
////			ib.setMinEms(2);
////			ib.setTextColor(ExplorerActivity.TEXT_COLOR);
////			// ib.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
////			ib.setGravity(Gravity.CENTER);
////			ib.setOnClickListener(new View.OnClickListener() {
////					public void onClick(View view) {
////						changeDir(new File("/"), true);
////						updateDelPaste();
////					}
////				});
////			mDirectoryButtons.addView(ib);
////			View v;
////			TextView b = null;
////			String folder = "";
////			for (int i = 1; i < parts.length; i++) {
////				folder += "/" + parts[i];
////				v = activity.getLayoutInflater().inflate(R.layout.dir, null);
////				b = (TextView) v.findViewById(R.id.name);
////				b.setText(parts[i]);
////				b.setTag(folder);
////				b.setTextColor(ExplorerActivity.TEXT_COLOR);
////				b.setOnClickListener(new View.OnClickListener() {
////						public void onClick(View view) {
////							String dir2 = (String) view.getTag();
////							changeDir(new File(dir2), true);
////							updateDelPaste();
////						}
////					});
////				mDirectoryButtons.addView(v);
////				scrolltext.postDelayed(new Runnable() {
////						public void run() {
////							scrolltext.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
////						}
////					}, 100L);
////			}
////			AndroidUtils.setTouch(mDirectoryButtons, this);
////			
////			if (b != null) {
////				b.setOnLongClickListener(new OnLongClickListener() {
////						@Override
////						public boolean onLongClick(View p1) {
////							final EditText editText = new EditText(activity);
////							final CharSequence clipboardData = AndroidUtils.getClipboardData(activity);
////							if (clipboardData.length() > 0 && clipboardData.charAt(0) == '/') {
////								editText.setText(clipboardData);
////							} else {
////								editText.setText(path);
////							}
////							final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
////								LinearLayout.LayoutParams.MATCH_PARENT,
////								LinearLayout.LayoutParams.WRAP_CONTENT);
////							layoutParams.gravity = Gravity.CENTER;
////							editText.setLayoutParams(layoutParams);
////							editText.setSingleLine(true);
////							editText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
////							editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
////							editText.setMinEms(2);
////							//editText.setGravity(Gravity.CENTER);
////							final int density = 8 * (int)getResources().getDisplayMetrics().density;
////							editText.setPadding(density, density, density, density);
////
////							AlertDialog dialog = new AlertDialog.Builder(activity)
////								.setIconAttribute(android.R.attr.dialogIcon)
////								.setTitle("Go to")
////								.setView(editText)
////								.setPositiveButton(R.string.ok,
////								new DialogInterface.OnClickListener() {
////									public void onClick(DialogInterface dialog, int whichButton) {
////										String name = editText.getText().toString();
////										Log.d(TAG, "new " + name);
////										File newF = new File(name);
////										if (newF.exists()) {
////											if (newF.isDirectory()) {
////												path = name;
////												changeDir(newF, true);
////											} else {
////												path = newF.getParent();
////												changeDir(newF.getParentFile(), true);
////											}
////											dialog.dismiss();
////										} else {
////											showToast("\"" + newF + "\" does not exist. Please choose another name");
////										}
////									}
////								})
////								.setNegativeButton(R.string.cancel,
////								new DialogInterface.OnClickListener() {
////									public void onClick(DialogInterface dialog, int whichButton) {
////										dialog.dismiss();
////									}
////								}).create();
////							dialog.show();
////							return true;
////						}
////					});
////			}
////		}
////	}
//
//
////	void changeDir(File curDir, boolean doScroll) {
////		Log.d(TAG, "changeDir " + curDir + ", " + doScroll);
////		slideFrag.getCurrentFragment().changeDir(curDir, doScroll);
////		// setDirectoryButtons();
////	}
//
//	private class SearchFileNameTask extends AsyncTask<String, Long, Long> {
//
//		protected void onPreExecute() {
//			setSearchMode(true);// srcAdapter.dirStr = null;curContentFrag.
//			searchVal = quicksearch.getText().toString();//curContentFrag.
//			
//		}
//
//		@Override
//		protected Long doInBackground(String... params) {
//			Log.d("SearchFileNameTask", "dirTemp4Search " + dirTemp4Search);
//			File file = new File(dirTemp4Search);
//			dataSourceL1.clear();
//			getActivity().runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						showToast("Searching...");
//						notifyDataSetChanged();// srcAdapter.notifyDataSetChanged();curContentFrag.
//					}
//				});
//
//			if (file.exists()) {
//				Collection<File> c = FileUtil.getFilesBy(file.listFiles(),
//														 params[0], true);
//				Log.d(TAG, "getFilesBy " + Util.collectionToString(c, true, "\n"));
//				for (File f : c) {
//					dataSourceL1.add(new LayoutElements(f));
//				}
//				//dataSourceL1.addAll(c);
//				//addAllDS1(Util.collectionFile2CollectionString(c));// dataSourceL1.addAll(Util.collectionFile2CollectionString(c));curContentFrag.
//				// Log.d("dataSourceL1 new task",
//				// Util.collectionToString(dataSourceL1, true, "\n"));
//			} else {
//				showToast(dirTemp4Search + " is not existed");
//			}
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Long result) {
//			notifyDataSetChanged();// srcAdapter.notifyDataSetChanged();curContentFrag.
//			if (mSwipeRefreshLayout.isRefreshing()) {
//				mSwipeRefreshLayout.setRefreshing(false);
//			}
//		}
//	}
//
//	void updateDelPaste() {
//		if (selectedInList1.size() > 0 && activity.deletePastesBtn2.getCompoundDrawables()[1] != drawableDelete) {
//			activity.deletePastesBtn2.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
//			activity.deletePastesBtn2.setCompoundDrawablesWithIntrinsicBounds(null, drawableDelete, null, null);
//			activity.deletePastesBtn2.setText("Delete");
//		} else if (selectedInList1.size() == 0 && activity.deletePastesBtn2.getCompoundDrawables()[1] != drawablePaste) {
//			activity.deletePastesBtn2.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
//			activity.deletePastesBtn2.setCompoundDrawablesWithIntrinsicBounds(null, drawablePaste, null, null);
//			activity.deletePastesBtn2.setText("Paste");
//		}
//		activity.deletePastesBtn2.getCompoundDrawables()[1].setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
//	}
//
//	public void manageUi(boolean search) {
//		// HorizontalScrollView scrollButtons =
//		// (HorizontalScrollView)findViewById(R.id.scroll_buttons);
//		
//		if (search == true) {
//			quicksearch.setHint("Search " + ((path != null) ? new File(path).getName() : new File(dirTemp4Search).getName()));
//			searchButton.setImageResource(R.drawable.ic_arrow_back_grey600);
//			//topflipper.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_bottom));
//			topflipper.setDisplayedChild(topflipper.indexOfChild(quickLayout));
//			setSearchMode(true);
//			quicksearch.requestFocus();
//			imm.showSoftInput(quicksearch, InputMethodManager.SHOW_IMPLICIT);
//		} else {
//			imm.hideSoftInputFromWindow(quicksearch.getWindowToken(), 0);
//			quicksearch.setText("");
//			searchButton.setImageResource(R.drawable.ic_action_search);
//			//topflipper.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_top));
//			topflipper.setDisplayedChild(topflipper.indexOfChild(scrolltext));
//			setSearchMode(false);//curContentFrag.
//			refreshDirectory();//slideFrag.getCurrentFragment().
//		}
//	}
//
//	public void searchButton() {
//		searchMode = !searchMode;
//		manageUi(searchMode);
//	}
//
//	public void up(View view) {
//
//		File curDir = new File(path);
//		Log.d("up curDir", curDir.getAbsolutePath());
//		File parentFile = curDir.getParentFile();
//		if (parentFile != null) {
//			Log.d("curDir.getParentFile()", parentFile.getAbsolutePath());
//			changeDir(parentFile, true);
//		}
//	}
//
//	
//    /**
//     * Sets up a FileObserver to watch the current directory.
//     */
////    private FileObserver createFileObserver(String path) {
////        return new FileObserver(path, FileObserver.CREATE | FileObserver.DELETE
////								| FileObserver.MOVED_FROM | FileObserver.MOVED_TO
////								| FileObserver.DELETE_SELF | FileObserver.MOVE_SELF
////								| FileObserver.CLOSE_WRITE) {
////            @Override
////            public void onEvent(int event, String path) {
////                if (path != null) {
////                    Util.debug(TAG, "FileObserver received event %d, CREATE = 256;DELETE = 512;DELETE_SELF = 1024;MODIFY = 2;MOVED_FROM = 64;MOVED_TO = 128; path %s", event, path);
////					activity.runOnUiThread(new Runnable() {
////							@Override
////							public void run() {
////								refreshDirectory();
////							}
////						});
////                }
////            }
////        };
////    }
//
////	void updateButtons() {
////		mListener.updateButtons();
////	}
//
////	public void search(String s) {
////		mListener.search(s);
////	}
//
//	/**
//     * Refresh the contents of the directory that", "currently shown.
//     */
////    public void refreshDirectory() {
////		Log.d(TAG, "refreshDirectory " + path + ", " + this);
////		if (path != null) {
////			changeDir(new File(path), false);
////		} else {
////			updateDir(path, this);
////		}
////    }
//
////	private void setSearchMode(final boolean search) {
////		Log.d(TAG, "setSearchMode " + searchMode + ", " + path + ", " + dirTemp4Search);
////		if (search) {
////			if (path != null) {
////				dirTemp4Search = path;
////			}
////			path = null;
////			searchMode = true;
////		} else {
////			path = dirTemp4Search;
////			searchMode = false;
////		}
////		Log.d(TAG, "setSearchMode " + searchMode + ", " + path + ", " + dirTemp4Search);
////	}
////
////	void notifyDataSetChanged() {
////		srcAdapter.notifyDataSetChanged();
////	}
//
//	private void addShortcut(final File f) {
//        //Adding shortcut for MainActivity
//        //on Home screen
//        Intent shortcutIntent = new Intent(activity.getApplicationContext(),
//										   ExplorerActivity.class);
//        shortcutIntent.putExtra("path", f.getAbsolutePath());
//        shortcutIntent.setAction(Intent.ACTION_MAIN);
//        shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        Intent addIntent = new Intent();
//        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
//        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, f.getName());
//
//        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
//						   Intent.ShortcutIconResource.fromContext(activity, R.mipmap.ic_launcher));
//
//        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
//        activity.sendBroadcast(addIntent);
//    }
////	private void addShortcut(final File f) {
////        
////		final Uri uri = Uri.fromFile(f);
////		final Intent shortcutIntent = new Intent(Intent.ACTION_VIEW); 
////		shortcutIntent.addCategory(Intent.CATEGORY_DEFAULT);
////		shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
////		shortcutIntent.setData(uri);
////		Log.d("i.setData(uri)", uri + "." + shortcutIntent);
//////							String suff = FileUtil.getExtension(f);
//////							ComparableEntry<String, String> comparableEntry = new ComparableEntry<String, String>(suff, "");
//////							ComparableEntry<String, String> floor = null;
//////							if ((floor = mimeMap.floor(comparableEntry)).equals(comparableEntry)) {
////		if (f.isFile()) {
////			final String mimeType = MimeTypes.getMimeType(f);
////			shortcutIntent.setDataAndType(uri, mimeType);  
////		}
////		
////        final Intent addIntent = new Intent();
////        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
////        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, f.getName());
////        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
////						   Intent.ShortcutIconResource.fromContext(getContext(),
////																   R.mipmap.ic_launcher));
////        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
////        getContext().sendBroadcast(addIntent);
////    }
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
//		if (activity.left.getVisibility() == View.GONE) {
//			mi.setEnabled(false);
//		} else {
//			mi.setEnabled(true);
//			if (activity.leftSize >= 0) {
//				mi.setTitle("Wider panel");
//			} else {
//				mi.setTitle("2 panels equal");
//			}
//		}
//        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
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
//							for (LayoutElements f : dataSourceL1) {
//								if (!selectedInList1.contains(f)) {
//									tempSelectedInList1.add(f);
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
//							break;
//						case R.id.biggerequalpanel:
//							if (activity.leftSize >= 0) {
//								//mi.setTitle("Wider panel");
//								LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)activity.left.getLayoutParams();
//								params.weight = 2.0f;
//								activity.left.setLayoutParams(params);
//								params = (LinearLayout.LayoutParams)activity.right.getLayoutParams();
//								params.weight = 1.0f;
//								activity.right.setLayoutParams(params);
//								activity.leftSize = -1;
//							} else {
//								LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)activity.left.getLayoutParams();
//								params.weight = 1.0f;
//								activity.left.setLayoutParams(params);
//								params = (LinearLayout.LayoutParams)activity.right.getLayoutParams();
//								params.weight = 1.0f;
//								activity.right.setLayoutParams(params);
//								activity.leftSize = 0;
//							}
//							AndroidUtils.setSharedPreference(activity, "biggerequalpanel", activity.leftSize);
//					}
//					return true;
//				}
//			});
//		popup.show();
//	}
//
//	public boolean copys(final View v) {
//		activity.mode = OpenMode.FILE;
//		activity.copyl.clear();
//		activity.cutl.clear();
//		activity.copyl.addAll(selectedInList1);
//		return true;
//	}
//
//	public boolean cuts(final View v) {
//		activity.mode = OpenMode.FILE;
//		activity.copyl.clear();
//		activity.cutl.clear();
//		activity.cutl.addAll(selectedInList1);
//		return true;
//	}
//
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
//
//	public boolean renames(final View v) {
//		return true;
//	}
//
//	public boolean addScreens(final View v) {
//		for (LayoutElements f : selectedInList1) {
//			addShortcut(f.bf.f);
//		}
//		return true;
//	}
//
//	public boolean deletes(final View v) {
//		if (selectedInList1.size() > 0) {
//			//new Futils().deleteFileList(selectedInList1, activity);
//			activity.curContentFrag2.dataSourceL1.removeAll(selectedInList1);
//			activity.curContentFrag2.tempOriDataSourceL1.removeAll(selectedInList1);
//			activity.curContentFrag2.selectedInList1.removeAll(selectedInList1);
//		}
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
////	public boolean deletes(final View v) {
////		if (selectedInList1.size() > 0) {
////			final ListView editText = new ListView(activity);
////			editText.setAdapter(new ArrayAdapter(activity, android.R.layout.simple_list_item_1, selectedInList1));//(Util.collectionToString(selectedInList2.subList(0, Math.min(3, selectedInList2.size())), true, "\n") + "...");
////			AlertDialog dialog = new AlertDialog.Builder(activity)
////				.setIconAttribute(android.R.attr.alertDialogIcon)
////				.setTitle("Delete " + selectedInList1.size() + " files?")
////				.setView(editText)
////				.setPositiveButton(R.string.ok,
////				new DialogInterface.OnClickListener() {
////					public void onClick(DialogInterface dialog, int whichButton) {
////						Collections.sort(selectedInList1, new StringSortingDecrease());
////						List<String> removedL = new LinkedList<>();
////						if (Build.VERSION.SDK_INT >= 21) {
////							//FragmentActivity activity = getActivity();
////							for (String file : selectedInList1) {
////								boolean ret = AndroidPathUtils.deleteFile(file, activity);
////								if (ret) {
////									removedL.add(file);
////									if (dataSourceL1 != null) {
////										dataSourceL1.remove(file);
////									}
////								}
////							}
////						} else {
////							for (String file : selectedInList1) {
////								boolean ret = new File(file).delete();
////								//StringBuilder ret = CommandUtils.delete(file.getAbsolutePath());
////								if (ret) {//}.length() > 0) {
////									removedL.add(file);
////									if (dataSourceL1 != null) {
////										dataSourceL1.remove(file);
////									}
////								} else {
////									showToast(file + " can not be deleted");
////								}
////							}
////						}
////						showToast("Deletion is finished");
////						selectedInList1.removeAll(removedL);
////						srcAdapter.notifyDataSetChanged();
////					}
////				})
////				.setNegativeButton(R.string.cancel,
////				new DialogInterface.OnClickListener() {
////					public void onClick(DialogInterface dialog, int whichButton) {
////						dialog.dismiss();
////					}
////				}).create();
////			dialog.show();
////		} else {
////			showToast("No file selected");
////		}
////		return true;
////	}
//
////	public boolean sends(final View v) {
////		if (selectedInList1.size() > 0) {
////			ArrayList<Uri> uris = new ArrayList<Uri>(selectedInList1.size());
////			Intent send_intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
////			send_intent.setFlags(0x1b080001);
////
////			send_intent.setType("*/*");
////			for(File file : selectedInList1) {
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
//	public boolean shares(final View v) {
//		if (selectedInList1.size() > 0) {
//			ArrayList<File> arrayList = new ArrayList<File>();
//			for (LayoutElements s : selectedInList1) {
//				arrayList.add(s.bf.f);
//			}
//			if (selectedInList1.size() > 100)
//				Toast.makeText(getContext(), "Can't share more than 100 files", Toast.LENGTH_SHORT).show();
//			else
//				new Futils().shareFiles(arrayList, activity, theme1, Color.BLUE);
//		} else {
//			showToast("No file selected");
//		}
//		return true;
//	}
//
////	private class ArrAdapter extends RecyclerAdapter<File, ArrAdapter.ViewHolder> implements OnLongClickListener, OnClickListener {
////
////		private final int backgroundResource;
////
////		private class ViewHolder extends RecyclerView.ViewHolder {
////			private TextView name;
////			private TextView size;
////			private TextView attr;
////			private TextView lastModified;
////			private TextView type;
////			private ImageButton cbx;
////			private ImageView image;
////			private ImageButton more;
////			private View convertedView;
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
////		public ArrAdapter(ArrayList<File> objects) {
////			super(objects);
////			Log.d(TAG, "ArrAdapter " + objects);
////			int[] attrs = new int[]{R.attr.selectableItemBackground};
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
////			// set the view's size, margins, paddings and layout parameters
////			ViewHolder vh = new ViewHolder(v);
////			return vh;
////		}
////
////		// Replace the contents of a view (invoked by the layout manager)
////		@Override
////		public void onBindViewHolder(ViewHolder holder, int position) {
////			final File f = mDataset.get(position);
////			//Log.d(TAG, "getView " + fileName);
////
////			final String fPath = f.getAbsolutePath();
////			holder.name.setText(f.getName());
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
////			if (path == null || path.length() > 0) {
////				holder.name.setEllipsize(TextUtils.TruncateAt.MIDDLE);
////			} else {
////				holder.name.setEllipsize(TextUtils.TruncateAt.START);
////			}
////
//////			if (!f.exists()) {
//////				dataSourceL1.remove(f);
//////				selectedInList1.remove(f);
//////				notifyItemRemoved(position);
//////				return;// convertView;
//////			}
////
////			//Log.d(TAG, "inDataSource2 " + inDataSource2 + ", " + dir);
////			//Log.d("f.getAbsolutePath()", f.getAbsolutePath());
////			//Log.d("curSelectedFiles", curSelectedFiles.toString());
////			if (selectedInList1.contains(f)) {
////				holder.convertedView.setBackgroundColor(ExplorerActivity.SELECTED_IN_LIST);
////				holder.cbx.setImageResource(R.drawable.ic_accept);
////				holder.cbx.setSelected(true);
////				holder.cbx.setEnabled(true);
////				if ((path == null || path.length() > 0) && selectedInList1.size() == dataSourceL1.size()) {
////					allCbx.setSelected(true);//.setChecked(true);
////					allCbx.setImageResource(R.drawable.ic_accept);
////				}
////			} else {
////				holder.convertedView.setBackgroundResource(backgroundResource);
////				if (selectedInList1.size() > 0) {
////					holder.cbx.setImageResource(R.drawable.ready);
////				} else {
////					holder.cbx.setImageResource(R.drawable.dot);
////				}
////				holder.cbx.setSelected(false);
////				holder.cbx.setEnabled(true);
////				allCbx.setSelected(false);
////				if (selectedInList1.size() == 0) {
////					allCbx.setImageResource(R.drawable.dot);
////				} else {
////					allCbx.setImageResource(R.drawable.ready);
////				}
////			}
////			if (tempPreviewL2 != null && tempPreviewL2.equals(f)) {
////				holder.convertedView.setBackgroundColor(ExplorerActivity.LIGHT_GREY);
////			}
////			final boolean canRead = f.canRead();
////			final boolean canWrite = f.canWrite();
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
////				final String namef = f.getName();
////				final int lastIndexOf = namef.lastIndexOf(".");
////				holder.type.setText(lastIndexOf >= 0 && lastIndexOf < namef.length() - 1 ? namef.substring(lastIndexOf + 1) : "");
////				holder.attr.setText(st);
////				holder.lastModified.setText(Util.dtf.format(f.lastModified()));
////			} else {
////				final String[] list = f.list();
////				int length = list == null ? 0 : list.length;
////				if (length > 1) {
////					holder.size.setText(Util.nf.format(length) + " items");
////				} else {
////					holder.size.setText(Util.nf.format(length) + " item");
////				}
////				final String st;
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
////
////			imageLoader.displayImage(f, getContext(), holder.image, spanCount);
////		}
////
////		public boolean rename(final View item) {
////			final File oldF = new File((String)item.getContentDescription());
////			final String oldPath = oldF.getAbsolutePath();
////			Log.d(TAG, "oldPath " + oldPath + ", " + item);
////			final EditText editText = new EditText(getContext());
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
////			AlertDialog dialog = new AlertDialog.Builder(getContext())
////				.setIconAttribute(android.R.attr.dialogIcon)
////				.setTitle("New Name")
////				.setView(editText)
////				.setPositiveButton(R.string.ok,
////				new DialogInterface.OnClickListener() {
////					public void onClick(DialogInterface dialog, int whichButton) {
////						String name = editText.getText().toString();
////						File newF = new File(oldF.getParent(), name);
////						String newPath = newF.getAbsolutePath();
////						Log.d("newF", newPath);
////						if (newF.exists()) {
////							showToast("\"" + newF + "\" is existing. Please choose another name");
////						} else {
////							boolean ok = AndroidPathUtils.renameFolder(oldF, newF, ExploreFragment.this.getContext());//oldF.renameTo(newF);
////							if (ok) {
////								ViewHolder holder = (ViewHolder)item.getTag();
////								TextView nameTV = holder.name;
////								Log.d(TAG, "nameTV " + nameTV);
////								int i = 0;
////								for (File fn : dataSourceL1) {
////									if (fn.equals(oldPath)) {
////										dataSourceL1.set(i, newF);
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
////								item.setContentDescription(newPath);
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
////		public boolean delete(final View item) {
////			ArrayList<String> arr = new ArrayList<>();
////			arr.add((String)item.getContentDescription());
////			//new Futils().deleteFiles(arr, activity);
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
////
////			i.setData(uri);
////			Log.d("i.setData(uri)", uri + "." + i);
////			//String suff = FileUtil.getExtension(f);
////			//ComparableEntry<String, String> comparableEntry = new ComparableEntry<String, String>(suff, "");
////			//ComparableEntry<String, String> floor = null;
////			//if ((floor = mimeMap.floor(comparableEntry)).equals(comparableEntry)) {
////			String mimeType = MimeTypes.getMimeType(f);
////			i.setDataAndType(uri, mimeType);  //floor.getValue()
////			Log.d(TAG, f + " = " + mimeType);
////			//}
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
////			final String data = (String) item.getContentDescription();
////			AndroidUtils.copyToClipboard(getContext(), data);
////			return true;
////		}
////
////		public void onClick(final View v) {
////			select(true);
////			if (v.getId() == R.id.more) {
////				MenuBuilder menuBuilder = new MenuBuilder(activity);
////				MenuInflater inflater = new MenuInflater(activity);
////				inflater.inflate(R.menu.file_commands, menuBuilder);
////				MenuPopupHelper optionsMenu = new MenuPopupHelper(activity , menuBuilder, allSize);
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
////			final String fPath = (String) v.getContentDescription();
////			Log.d(TAG, "onClick, " + fPath + ", " + v);
////			if (fPath == null) {
////				return;
////			}
////			final File f = new File(fPath);
////			//Log.d(TAG, "currentSelectedList " + Util.collectionToString(selectedInList1, true, "\r\n"));
////			//Log.d(TAG, "selectedInList.contains(f) " + selectedInList1.contains(f));
////			//Log.d(TAG, "multiFiles " + multiFiles);
////			//Log.d(TAG, "f.exists() " + f.exists());
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
////					} else if (v.getId() == R.id.cbx) {//file and folder
////						if (selectedInList1.contains(f)) {
////							selectedInList1.remove(f);
////							if (selectedInList1.size() == 0 && activity.copyl.size() == 0 && activity.cutl.size() == 0 && activity.rightCommands.getVisibility() == View.VISIBLE) {
////								activity.horizontalDivider6.setVisibility(View.GONE);
////								activity.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
////								activity.rightCommands.setVisibility(View.GONE);
////							}
////						} else {
////							selectedInList1.add(f);
////							if (activity.rightCommands.getVisibility() == View.GONE) {
////								activity.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
////								activity.rightCommands.setVisibility(View.VISIBLE);
////								activity.horizontalDivider6.setVisibility(View.VISIBLE);
////							}
////						}
////					} else if (f.isDirectory()) { //&& curSelectedFiles.size() == 0 
////						if (selectedInList1.size() == 0) { //} && !(v instanceof CheckBox)) {dir == null || dir.length() > 0
////							changeDir(f, true);
////						} else {
////							if (selectedInList1.contains(f)) {
////								selectedInList1.remove(f);
////								if (selectedInList1.size() == 0 && activity.copyl.size() == 0 && activity.cutl.size() == 0 && activity.rightCommands.getVisibility() == View.VISIBLE) {
////									activity.horizontalDivider6.setVisibility(View.GONE);
////									activity.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
////									activity.rightCommands.setVisibility(View.GONE);
////								}
////							} else {
////								selectedInList1.add(f);
////								if (activity.rightCommands.getVisibility() == View.GONE) {
////									activity.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
////									activity.rightCommands.setVisibility(View.VISIBLE);
////									activity.horizontalDivider6.setVisibility(View.VISIBLE);
////								}
////							}
////						}
////					} else if (f.isFile()) { //file
////						if (selectedInList1.size() == 0) { //} && !(v instanceof CheckBox)) {dir == null || dir.length() > 0
////							openFile(f);
////						} else {
////							if (selectedInList1.contains(f)) {
////								selectedInList1.remove(f);
////								if (selectedInList1.size() == 0 && activity.copyl.size() == 0 && activity.cutl.size() == 0 && activity.rightCommands.getVisibility() == View.VISIBLE) {
////									activity.horizontalDivider6.setVisibility(View.GONE);
////									activity.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
////									activity.rightCommands.setVisibility(View.GONE);
////								}
////							} else {
////								selectedInList1.add(f);
////								if (activity.rightCommands.getVisibility() == View.GONE) {
////									activity.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
////									activity.rightCommands.setVisibility(View.VISIBLE);
////									activity.horizontalDivider6.setVisibility(View.VISIBLE);
////								}
////							}
////						}
////					}
////					if ((path == null || path.length() > 0)) {
////						selectionStatus1.setText(selectedInList1.size() 
////												 + "/" + dataSourceL1.size());
////					}
////					srcAdapter.notifyDataSetChanged();
////				}
////			} else {
////				changeDir(f.getParentFile(), true);
////			}
////			updateDelPaste();
////		}
////		
////		private void load(final File f, final String fPath) throws IllegalStateException {
////			final String mime = MimeTypes.getMimeType(f);
////			Log.d(TAG, fPath + "=" + mime);
////			int i = 0;
////			SlidingTabsFragment2.PagerAdapter pagerAdapter = activity.slideFrag2.pagerAdapter;
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
////		private void openFile(final File f) {
////			try {
////				final Uri uri = Uri.fromFile(f);
////				final Intent i = new Intent(Intent.ACTION_VIEW); 
////				i.addCategory(Intent.CATEGORY_DEFAULT);
////				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
////				i.setData(uri);
////				Log.d("i.setData(uri)", uri + "." + i);
////				final String mimeType = MimeTypes.getMimeType(f);
////				i.setDataAndType(uri, mimeType);//floor.getValue()
////				Log.d(TAG, f + "=" + mimeType);
////				final Intent createChooser = Intent.createChooser(i, "View");
////				Log.i("createChooser.getExtras()", AndroidUtils.bundleToString(createChooser.getExtras()));
////				startActivity(createChooser);
////			} catch (Throwable e) {
////				Toast.makeText(getContext(), "unable to view !\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
////			}
////		}
////
////		public boolean onLongClick(final View v) {
////			select(true);
////			final String fPath = (String) v.getContentDescription();
////			final File f = new File(fPath);
////
////			if (!f.exists()) {
////				changeDir(f, true);
////				return true;
////			} else if (!f.canRead()) {
////				showToast(f + " cannot be read");
////				return true;
////			}
////			Log.d(TAG, "onLongClick, " + fPath);
////			Log.d(TAG, "currentSelectedList" + Util.collectionToString(selectedInList1, true, "\r\n"));
////			Log.d(TAG, "selectedInList.contains(f) " + selectedInList1.contains(f));
////
////			if (selectedInList1.contains(f)) {
////				selectedInList1.remove(f);
////				if (selectedInList1.size() == 0 && activity.copyl.size() == 0 && activity.cutl.size() == 0 && activity.rightCommands.getVisibility() == View.VISIBLE) {
////					activity.horizontalDivider6.setVisibility(View.GONE);
////					activity.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
////					activity.rightCommands.setVisibility(View.GONE);
////				}
////			} else {
////				selectedInList1.add(f);
////				if (activity.rightCommands.getVisibility() == View.GONE) {
////					activity.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
////					activity.rightCommands.setVisibility(View.VISIBLE);
////					activity.horizontalDivider6.setVisibility(View.VISIBLE);
////				}
////			}
////			srcAdapter.notifyDataSetChanged();
////			if ((path == null || path.length() > 0)) {
////				selectionStatus1.setText(selectedInList1.size() 
////										 + "/" + dataSourceL1.size());
////
////			}
////			updateDelPaste();
////			return true;
////		}
////	}
//}
//