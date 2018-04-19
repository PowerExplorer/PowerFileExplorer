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
import com.amaze.filemanager.utils.files.CryptUtil;
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
import java.util.Comparator;
import java.util.Calendar;
import java.util.Date;
import android.widget.Button;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import com.amaze.filemanager.utils.color.ColorPreference;
import android.view.inputmethod.InputMethodManager;
import android.support.v7.widget.PopupMenu;

public abstract class FileFrag extends Frag {

	private static final String TAG = "FileFrag";

	public boolean selection, results = false, SHOW_HIDDEN, CIRCULAR_IMAGES, 
	SHOW_PERMISSIONS, SHOW_SIZE, SHOW_LAST_MODIFIED;
    public boolean GO_BACK_ITEM, SHOW_THUMBS, COLORISE_ICONS, SHOW_DIVIDERS, 
	SHOW_HEADERS;

	public SwipeRefreshLayout mSwipeRefreshLayout;
    private DisplayMetrics displayMetrics;
	
	public BitmapDrawable folder, apk, DARK_IMAGE, DARK_VIDEO;
	public boolean IS_LIST = true;

    public float[] color;

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

	GridLayoutManager gridLayoutManager;
	TextView diskStatus;
	GridDividerItemDecoration dividerItemDecoration;
	
	long lastScroll = System.currentTimeMillis();
	protected InputMethodManager imm;
	Resources res;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		//Log.d(TAG, "onCreate " + savedInstanceState);
		super.onCreate(savedInstanceState);
		res = getResources();

		folder = new BitmapDrawable(res, BitmapFactory.decodeResource(res, R.drawable.ic_grid_folder_new));
		ColorPreference colorPreference = activity.getColorPreference();
		accentColor = colorPreference.getColor(ColorUsage.ACCENT);
        primaryColor = colorPreference.getColor(ColorUsage.PRIMARY);
        primaryTwoColor = colorPreference.getColor(ColorUsage.PRIMARY_TWO);
		SHOW_THUMBS = sharedPref.getBoolean("showThumbs", true);
        SHOW_PERMISSIONS = sharedPref.getBoolean("showPermissions", false);
        SHOW_SIZE = sharedPref.getBoolean("showFileSize", false);
        SHOW_DIVIDERS = sharedPref.getBoolean("showDividers", true);
        SHOW_HEADERS = sharedPref.getBoolean("showHeaders", true);
        GO_BACK_ITEM = sharedPref.getBoolean("goBack_checkbox", false);
        CIRCULAR_IMAGES = sharedPref.getBoolean("circularimages", true);
        SHOW_LAST_MODIFIED = sharedPref.getBoolean("showLastModified", true);
		SHOW_HIDDEN = sharedPref.getBoolean("showHidden", false);
        COLORISE_ICONS = sharedPref.getBoolean("coloriseIcons", true);


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

		icons.setOnClickListener(this);
		allCbx.setOnClickListener(this);
		allName.setOnClickListener(this);
		if (allDate != null) {
			allDate.setOnClickListener(this);
		}
		allSize.setOnClickListener(this);
		if (allType != null) {
			allType.setOnClickListener(this);
		}



	}

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

    public static void launchSMB(final SmbFile smbFile, final long si, final Activity activity) {
        final Streamer s = Streamer.getInstance();
        new Thread() {
            public void run() {
                try {
                    /*
					 List<SmbFile> subtitleFiles = new ArrayList<SmbFile>();

					 // finding subtitles
					 for (Layoutelements layoutelement : LIST_ELEMENTS) {
					 SmbFile smbFile = new SmbFile(layoutelement.path);
					 if (smbFile.getName().contains(smbFile.getName())) subtitleFiles.add(smbFile);
					 }
					 */

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

	void refreshRecyclerViewLayoutManager() {
		setRecyclerViewLayoutManager();
		horizontalDivider0.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
		horizontalDivider12.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
		horizontalDivider7.setBackgroundColor(ExplorerActivity.DIVIDER_COLOR);
	}

	void notifyDataSetChanged() {
		srcAdapter.notifyDataSetChanged();
	}

	void swap(View v) {
		activity.swap = !activity.swap;

		AndroidUtils.setSharedPreference(activity, "swap", activity.swap);
		final int leftVisible = activity.left.getVisibility();
		final int rightVisible = activity.right.getVisibility();
		final ViewGroup parent = (ViewGroup)activity.left.getParent();

//		left.setAnimation(AnimationUtils.loadAnimation(this, R.anim.shrink_from_top));
//		right.setAnimation(AnimationUtils.loadAnimation(this, R.anim.shrink_from_top));
		parent.removeView(activity.left);
		parent.removeView(activity.right);
//		left.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_top));
//		right.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_top));
		if (activity.swap) {
			parent.addView(activity.right, 0);
			parent.addView(activity.left, 2);
		} else {
			parent.addView(activity.left, 0);
			parent.addView(activity.right, 2);
		}
		activity.left.setVisibility(rightVisible);
		activity.right.setVisibility(leftVisible);

	}

	abstract void rangeSelection();
	abstract void inversion();
	abstract void clearSelection();
	abstract void undoClearSelection();
	abstract void updateStatus();
	void setRecyclerViewLayoutManager() {}

	void hide() throws Resources.NotFoundException {
		if (right.getVisibility() == View.VISIBLE && left.getVisibility() == View.VISIBLE) {
//			if (spanCount == 4) {
//				spanCount = 8;
//				setRecyclerViewLayoutManager();
//			}
			if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
				activity.left.setVisibility(View.GONE);
			} else {
				activity.right.setVisibility(View.GONE);
			}
		} else {
//			if (spanCount == 8) {
//				spanCount = 4;
//				setRecyclerViewLayoutManager();
//			}
			if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
				activity.right.setVisibility(View.VISIBLE);
			} else {
				activity.left.setVisibility(View.VISIBLE);
			}
		}
	}

	void biggerequalpanel() {
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
		activity.curExplorerFrag.setRecyclerViewLayoutManager();
		AndroidUtils.setSharedPreference(activity, "biggerequalpanel", activity.leftSize);
	}

	void moreInPanel(final View v) {
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
		if (activity.left.getVisibility() == View.VISIBLE) {
			mi.setTitle("Hide");
		} else {
			mi.setTitle("2 panels");
		}
        mi = menu.findItem(R.id.biggerequalpanel);
		if (activity.left.getVisibility() == View.GONE || activity.right.getVisibility() == View.GONE) {
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
					switch (item.getItemId()) {
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
					}
					return true;
				}
			});
		popup.show();
	}

}
