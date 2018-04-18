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

public abstract class FileFrag extends Frag {

	private static final String TAG = "FileFrag";

	public boolean selection, results = false, SHOW_HIDDEN, CIRCULAR_IMAGES, 
	SHOW_PERMISSIONS, SHOW_SIZE, SHOW_LAST_MODIFIED;
    public boolean GO_BACK_ITEM, SHOW_THUMBS, COLORISE_ICONS, SHOW_DIVIDERS, 
	SHOW_HEADERS;
	
	public SwipeRefreshLayout mSwipeRefreshLayout;
    private DisplayMetrics displayMetrics;
	//public ArrayList<LayoutElements> LIST_ELEMENTS;
//    public com.amaze.filemanager.adapters.RecyclerAdapter adapter;
	//public ActionMode mActionMode;
    //public SharedPreferences sharedPref;
	//public RecyclerView listView;
	//public com.amaze.filemanager.adapters.RecyclerAdapter adapter;
	//LoadList loadList;
	private View nofilesview;
//	public OpenMode openMode = OpenMode.FILE;
	public BitmapDrawable folder, apk, DARK_IMAGE, DARK_VIDEO;
	public boolean IS_LIST = true;

	//public String iconskin;
    public float[] color;
//    public int skin_color;
//    public int skinTwoColor;
//    public int icon_skin_color;
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

	public abstract void refreshRecyclerViewLayoutManager();

	void notifyDataSetChanged() {
		srcAdapter.notifyDataSetChanged();
	}
	
}
