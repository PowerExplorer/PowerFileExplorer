package net.gnu.explorer;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.amaze.filemanager.activities.ThemedActivity;
import com.amaze.filemanager.adapters.DrawerAdapter;
import com.amaze.filemanager.database.CloudContract;
import com.amaze.filemanager.database.CloudHandler;
import com.amaze.filemanager.database.CryptHandler;
import com.amaze.filemanager.database.TabHandler;
import com.amaze.filemanager.database.UtilsHandler;
import com.amaze.filemanager.database.models.CloudEntry;
import com.amaze.filemanager.exceptions.CloudPluginException;
import com.amaze.filemanager.filesystem.BaseFile;
import com.amaze.filemanager.filesystem.HFile;
import com.amaze.filemanager.filesystem.RootHelper;
import com.amaze.filemanager.fragments.CloudSheetFragment;
import com.amaze.filemanager.fragments.CloudSheetFragment.CloudConnectionCallbacks;
import com.amaze.filemanager.fragments.ProcessViewer;
import com.amaze.filemanager.fragments.preference_fragments.QuickAccessPref;
import com.amaze.filemanager.services.CopyService;
import com.amaze.filemanager.services.DeleteTask;
import com.amaze.filemanager.services.asynctasks.MoveFiles;
import com.amaze.filemanager.ui.LayoutElement;
import com.amaze.filemanager.ui.dialogs.RenameBookmark;
import com.amaze.filemanager.ui.dialogs.RenameBookmark.BookmarkCallback;
import com.amaze.filemanager.ui.dialogs.SmbConnectDialog;
import com.amaze.filemanager.ui.dialogs.SmbConnectDialog.SmbConnectionListener;
import com.amaze.filemanager.ui.drawer.EntryItem;
import com.amaze.filemanager.ui.drawer.Item;
import com.amaze.filemanager.ui.drawer.SectionItem;
import com.amaze.filemanager.utils.AppConfig;
import com.amaze.filemanager.utils.BookSorter;
import com.amaze.filemanager.utils.DataUtils;
import com.amaze.filemanager.utils.DataUtils.DataChangeListener;
import com.amaze.filemanager.utils.MainActivityHelper;
import com.amaze.filemanager.utils.OTGUtil;
import com.amaze.filemanager.utils.OpenMode;
import com.amaze.filemanager.utils.PreferenceUtils;
import com.amaze.filemanager.utils.ServiceWatcherUtil;
import com.amaze.filemanager.utils.TinyDB;
import com.amaze.filemanager.utils.color.ColorUsage;
import com.amaze.filemanager.utils.files.Futils;
import com.bumptech.glide.Glide;
import com.cloudrail.si.CloudRail;
import com.cloudrail.si.exceptions.AuthenticationException;
import com.cloudrail.si.exceptions.ParseException;
import com.cloudrail.si.interfaces.CloudStorage;
import com.cloudrail.si.services.Box;
import com.cloudrail.si.services.Dropbox;
import com.cloudrail.si.services.GoogleDrive;
import com.cloudrail.si.services.OneDrive;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import eu.chainfire.libsuperuser.Shell;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import net.gnu.androidutil.AndroidUtils;
import net.gnu.androidutil.ImageThreadLoader;
import net.gnu.p7zip.CompressFragment;
import net.gnu.p7zip.DecompressFragment;
import net.gnu.util.FileUtil;
import net.gnu.util.Util;

import static android.os.Build.VERSION.SDK_INT;
import static com.amaze.filemanager.fragments.preference_fragments.Preffrag.PREFERENCE_SHOW_SIDEBAR_FOLDERS;
import static com.amaze.filemanager.fragments.preference_fragments.Preffrag.PREFERENCE_SHOW_SIDEBAR_QUICKACCESSES;
import android.support.v4.app.Fragment;
import net.gnu.p7zip.DecompressTask;


public class ExplorerActivity extends ThemedActivity implements OnRequestPermissionsResultCallback,
SmbConnectionListener, DataChangeListener, BookmarkCallback,
CloudConnectionCallbacks, //SearchWorkerFragment.HelperCallbacks, 
LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener, ListView.OnItemClickListener {

	private static final String COMPRESS = CompressFragment.class.getSimpleName();
	private static final String DECOMPRESS = DecompressFragment.class.getSimpleName();

	private CompressFragment compressFrag;
	private DecompressFragment decompressFrag;

	private String currentDialog = "";
	
	public void compress(final String filePaths, final String archiveFilePath) {
		currentDialog = COMPRESS;
		Log.d(TAG, "compress(View view) " + compressFrag);
		if (compressFrag == null) {
			compressFrag = CompressFragment.newInstance();
		}
		compressFrag.files = filePaths;
		compressFrag.saveTo = archiveFilePath;

		compressFrag.show(getSupportFragmentManager(), COMPRESS);
	}

	public void decompress(final String filePaths, final String extractToPath, String includes, boolean showDialog) {
		currentDialog = DECOMPRESS;
		if (decompressFrag == null) {
			decompressFrag = DecompressFragment.newInstance();
		}
		decompressFrag.files = filePaths;
		decompressFrag.saveTo = extractToPath;
		decompressFrag.include = includes;
		if (showDialog) {
			decompressFrag.show(getSupportFragmentManager(), DECOMPRESS);
		}
	}

	
	private static final String TAG = "ExplorerActivity";
	
	public static int SELECTED_IN_LIST = 0xFFFEF8BA;//0xFFFFF0A0
	public static int BASE_BACKGROUND = 0xFFFFFFE8;
	public static int IN_DATA_SOURCE_2 = 0xFFFFF8D9;
	public static int IS_PARTIAL = 0xFFFFF0CF;
	//public static final int GREY = Color.parseColor("#ff444444");
	public static final int LIGHT_GREY = 0xff909090;
	public static int TEXT_COLOR = 0xff404040;
	public static int DIR_COLOR = Color.BLACK;
	public static int DOT = 0xffa0a0a0;
	public static int FILE_COLOR = Color.BLACK;
	public static int DIVIDER_COLOR = 0xff707070;//-16777216
	
	public static final String PREVIOUS_SELECTED_FILES = "net.gnu.explorer.selectedFiles";
	
	public static final String ALL_SUFFIX = "*";
	public static final String ALL_SUFFIX_TITLE = "Select Files/Folders";
	public static final String ZIP_SUFFIX = ".zpaq; .7z; .bz2; .bzip2; .tbz2; .tbz; .001; .gz; .gzip; .tgz; .tar; .dump; .swm; .xz; .txz; .zip; .zipx; .jar; .apk; .xpi; .odt; .ods; .odp; .docx; .xlsx; .pptx; .epub; .apm; .ar; .a; .deb; .lib; .arj; .cab; .chm; .chw; .chi; .chq; .msi; .msp; .doc; .xls; .ppt; .cpio; .cramfs; .dmg; .ext; .ext2; .ext3; .ext4; .img; .fat; .hfs; .hfsx; .hxs; .hxi; .hxr; .hxq; .hxw; .lit; .ihex; .iso; .lzh; .lha; .lzma; .mbr; .mslz; .mub; .nsis; .ntfs; .rar; .r00; .rpm; .ppmd; .qcow; .qcow2; .qcow2c; .squashfs; .udf; .iso; .scap; .uefif; .vdi; .vhd; .vmdk; .wim; .esd; .xar; .pkg; .z; .taz";
	public static final String ZIP_TITLE = "Compressed file (" + ZIP_SUFFIX + ")";
	public static final int FILES_REQUEST_CODE = 13;
	public static final int SAVETO_REQUEST_CODE = 14;
	public static final boolean MULTI_FILES = true;

	/**
	 * Select multi files and folders
	*/
	public static final String EXTRA_MULTI_SELECT = "org.openintents.extra.MULTI_SELECT";//"multiFiles";
	
    public static final String ACTION_PICK_FILE = "org.openintents.action.PICK_FILE";

    public static final String ACTION_PICK_DIRECTORY = "org.openintents.action.PICK_DIRECTORY";

    public static final String ACTION_MULTI_SELECT = "org.openintents.action.MULTI_SELECT";

    public static final String ACTION_SEARCH_STARTED = "org.openintents.action.SEARCH_STARTED";

    public static final String ACTION_SEARCH_FINISHED = "org.openintens.action.SEARCH_FINISHED";

    public static final String EXTRA_TITLE = "org.openintents.extra.TITLE";

    public static final String EXTRA_BUTTON_TEXT = "org.openintents.extra.BUTTON_TEXT";

    public static final String EXTRA_WRITEABLE_ONLY = "org.openintents.extra.WRITEABLE_ONLY";

    public static final String EXTRA_SEARCH_INIT_PATH = "org.openintents.extra.SEARCH_INIT_PATH";

    public static final String EXTRA_SEARCH_QUERY = "org.openintents.extra.SEARCH_QUERY";
    //public static final String EXTRA_DIR_PATH = "org.openintents.extra.DIR_PATH";
    public static final String EXTRA_ABSOLUTE_PATH = "org.openintents.extra.ABSOLUTE_PATH";
    public static final String EXTRA_FILTER_FILETYPE = "org.openintents.extra.FILTER_FILETYPE";
    public static final String EXTRA_FILTER_MIMETYPE = "org.openintents.extra.FILTER_MIMETYPE";
    public static final String EXTRA_DIRECTORIES_ONLY = "org.openintents.extra.DIRECTORIES_ONLY";
    public static final String EXTRA_DIALOG_FILE_HOLDER = "org.openintents.extra.DIALOG_FILE";
    public static final String EXTRA_IS_GET_CONTENT_INITIATED = "org.openintents.extra.ENABLE_ACTIONS";
    public static final String EXTRA_FILENAME = "org.openintents.extra.FILENAME";
    public static final String EXTRA_FROM_OI_FILEMANAGER = "org.openintents.extra.FROM_OI_FILEMANAGER";
	//single file, multi files (pick file+multi extra), single dir, multi dir (pick dir + multi extra), files and dirs (Action_MULTI_SELECT), search query
	//mime, extension
	boolean multiFiles = false;
	private String suffix = ""; // ".*" : all file types, "" only folder, "; *"
	private String[] previousSelectedStr = new String[0];

	private String mimes = "";
	String dir = "";
	
	
	public final static int REQUEST_CODE_PREFERENCES = 1, REQUEST_CODE_SRV_FORM = 2, REQUEST_CODE_OPEN = 3;
    public final static int FIND_ACT = 1017, SMB_ACT = 2751, FTP_ACT = 4501, SFTP_ACT = 2450;
    public final static String PREF_RESTORE_ACTION = "com.ghostsq.commander.PREF_RESTORE";

	final static String DOCTYPE = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /><script src=\"file:///android_asset/run_prettify.js?skin=sons-of-obsidian\"></script></head><body bgcolor=\"#000000\"><pre class=\"prettyprint \">";
	final static String END_PRE = "</pre></body></html>";
	
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	public boolean isDrawerLocked = false;
	
	SlidingTabsFragment slideFrag = null;
	public ContentFragment curContentFrag;
	int curContentFragIndex = 1;
	ContentFragment curSelectionFrag;
	int curSelectionFragIndex = -1;
	
	SlidingTabsFragment slideFrag2 = null;
	ContentFragment curSelectionFrag2;
	int curSelectionFragIndex2 = 2;
	public ContentFragment curExplorerFrag;
	int curExplorerFragIndex = 1;
	
//	public int operation = -1;
//    public ArrayList<BaseFile> oparrayList;
//    public ArrayList<ArrayList<BaseFile>> oparrayListList;
	
    public static final int INTENT_WRITE_REQUEST_CODE = 1;

	OpenMode mode;
	String zippath = "";
	private Intent intent;
	
	public boolean swap;

	public ViewGroup left;
	public ViewGroup right;
	
    static final int NUM_BACK = 32;
	int mCurTheme = 0;
	private View horizontalDivider5;

	public boolean slideFrag1Selected = true;
	boolean configurationChanged = false;
	private Handler scheduleHandler = new Handler();
	private Runnable runSchedule = new Schedule(new int[] {6, 19}, new int[] {0, 0}, new int[] {0, 0});
	
	private static final int REQUEST_WRITE_EXTERNAL = 0;

    private static final int REQUEST_CAMERA = 1;

    private static String[] PERMISSIONS_STORAGE = {
		Manifest.permission.WRITE_EXTERNAL_STORAGE,
		Manifest.permission.WRITE_MEDIA_STORAGE,
//		Manifest.permission.ACCESS_WIFI_STATE,
//		Manifest.permission.CHANGE_WIFI_STATE,
//		Manifest.permission.ACCESS_NETWORK_STATE,
//		Manifest.permission.CHANGE_NETWORK_STATE,
//		Manifest.permission.INTERNET,
//		Manifest.permission.BLUETOOTH,
//		Manifest.permission.BLUETOOTH_ADMIN,
//		Manifest.permission.RECORD_AUDIO,
//		Manifest.permission.CAMERA,
//		Manifest.permission.INSTALL_SHORTCUT,
//		Manifest.permission.UNINSTALL_SHORTCUT,
//		Manifest.permission.SET_WALLPAPER,
//		Manifest.permission.GET_TASKS,
//		Manifest.permission.REORDER_TASKS,
//		Manifest.permission.KILL_BACKGROUND_PROCESSES,
//		Manifest.permission.WAKE_LOCK,
	};
	
	static int density;// = (int)(4 * getResources().getDisplayMetrics().density);
	LinkedList<String> historyList = new LinkedList<>();
	int balance = 0; //0 =; -1 <; 1 >
	private Resources resources;
	
	public static Shell.Interactive shellInteractive;
	public static Handler handler;
	private static HandlerThread handlerThread;
	private AsyncTask<Void, Void, Boolean> cloudSyncTask;
	
	public static final String KEY_PREF_OTG = "uri_usb_otg";
	public static final String KEY_INTENT_PROCESS_VIEWER = "openprocesses";
	public static final String TAG_INTENT_FILTER_FAILED_OPS = "failedOps";
    public static final String TAG_INTENT_FILTER_GENERAL = "general_communications";
    public static final String ARGS_KEY_LOADER = "loader_cloud_args_service";
	
	private static final String KEY_PREFERENCE_BOOKMARKS_ADDED = "books_added";
	
	private static final int REQUEST_CODE_CLOUD_LIST_KEYS = 5463;
    private static final int REQUEST_CODE_CLOUD_LIST_KEY = 5472;
    private static final int REQUEST_CODE_CLOUD_LIST_KEY_CLOUD = 5434;
	private static final String CLOUD_AUTHENTICATOR_GDRIVE = "android.intent.category.BROWSABLE";
    private static final String CLOUD_AUTHENTICATOR_REDIRECT_URI = "net.gnu.explorer:/oauth2redirect";
	private static final int REQUEST_CODE_SAF = 223;
    private static final String VALUE_PREF_OTG_NULL = "n/a";
	private static final int image_selector_request_code = 31;
	public ArrayList<BaseFile> COPY_PATH = null, MOVE_PATH = null;
	public List<String> EXTRACT_PATH = null;
	public List<String> EXTRACT_MOVE_PATH = null;
	public boolean isEncryptOpen = false;       // do we have to open a file when service is begin destroyed
    public BaseFile encryptBaseFile;            // the cached base file which we're to open, delete it later
	public boolean mReturnIntent = false;
    public boolean useGridView, openzip = false;
    public boolean mRingtonePickerIntent = false, colourednavigation = false;
	private String pendingPath;
	public static final Pattern DIR_SEPARATOR = Pattern.compile("/");
	public int storage_count = 0; // number of storage available (internal/external/otg etc)
	private DrawerAdapter adapter;
	//private HistoryManager history, grid;
	private static final int SELECT_MINUS_2 = -2, NO_VALUE = -1, SELECT_0 = 0, SELECT_102 = 102;
	private int selectedStorage = 0;
	
	private FragmentTransaction pending_fragmentTransaction;
	
	
	public MainActivityHelper mainActivityHelper;
	private boolean openProcesses = false;
    DataUtils dataUtils = DataUtils.getInstance();
	private TabHandler tabHandler;
	private UtilsHandler utilsHandler;
    private CloudHandler cloudHandler;
	private Futils utils;
    public int skinStatusBar;
	private int hidemode;
    private boolean mIntentInProgress, showHidden = false;
	private Toast toast = null;
    Window window;
	ProcessViewer processViewer;
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		Log.d(TAG, "onCreate " + savedInstanceState);
		super.onCreate(savedInstanceState);
		
		window = getWindow();
		window.requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		window.requestFeature(Window.FEATURE_ACTION_MODE_OVERLAY);
		
		resources = getResources();
		final ActionBar actionBar = getSupportActionBar();
		intent = getIntent();
		final String action = intent.getAction();
		
		initialisePreferences();
        initializeInteractiveShell();

        dataUtils.registerOnDataChangedListener(this);
		
		setContentView(R.layout.activity_folder_chooser);
		
		if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			//setContentView(R.layout.activity_folder_chooser_vertical);
			Log.d(TAG, "ORIENTATION_PORTRAIT");
		} else {
			//setContentView(R.layout.activity_folder_chooser);
			Log.d(TAG, "ORIENTATION_LANDSCAPE");
		}
		
		final FragmentManager supportFragmentManager = getSupportFragmentManager();
		
		openProcesses = intent.getBooleanExtra(KEY_INTENT_PROCESS_VIEWER, false);
        if (openProcesses) {
			Fragment findFragmentByTag;
			if ((findFragmentByTag = supportFragmentManager.findFragmentByTag(KEY_INTENT_PROCESS_VIEWER)) != null) {
				processViewer = (ProcessViewer)findFragmentByTag;
			} else {
				processViewer = new ProcessViewer();
			}
			processViewer.show(supportFragmentManager, KEY_INTENT_PROCESS_VIEWER);
//			final FragmentTransaction transaction = supportFragmentManager.beginTransaction();
//			final ProcessViewer processViewer = new ProcessViewer();
//			transaction.replace(R.id.content_frame, processViewer, KEY_INTENT_PROCESS_VIEWER);
//			//transaction.addToBackStack(null);
			selectedStorage = SELECT_102;
			openProcesses = false;
//			//title.setText(utils.getString(con, R.string.process_viewer));
//
//			transaction.commit();
//			//supportInvalidateOptionsMenu();
		} 
		updateColor();
		initialiseViews();
        tabHandler = new TabHandler(this);
        utilsHandler = new UtilsHandler(this);
        cloudHandler = new CloudHandler(this);
		mainActivityHelper = new MainActivityHelper(this);
		utils = getFutils();
        
		if (intent.getStringArrayListExtra(TAG_INTENT_FILTER_FAILED_OPS) != null) {
            ArrayList<BaseFile> failedOps = intent.getParcelableArrayListExtra(TAG_INTENT_FILTER_FAILED_OPS);
            if (failedOps != null) {
                mainActivityHelper.showFailedOperationDialog(failedOps, intent.getBooleanExtra("move", false), this);
            }
        }
		
		horizontalDivider5 = findViewById(R.id.horizontalDivider5);

		left = (ViewGroup) findViewById(R.id.left);
		right = (ViewGroup) findViewById(R.id.right);
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setOnItemClickListener(this);
		mDrawerList.setDivider(null);
		final ImageView imageView = new ImageView(this);
		imageView.setImageResource(R.drawable.file_browser);
		imageView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		mDrawerList.addHeaderView(imageView);
		
		if (Intent.ACTION_MAIN.equals(action) || Intent.ACTION_VIEW.equals(action)) {
			findViewById(R.id.buttons).setVisibility(View.GONE);
		}

		if (actionBar != null) {
			if (Intent.ACTION_MAIN.equals(action)) {
				actionBar.hide();
			} else {
				actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
				actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
				actionBar.setDisplayShowTitleEnabled(false);
				actionBar.setDisplayHomeAsUpEnabled(true);
				actionBar.setHomeButtonEnabled(true);
				setTitle("Power File Explorer");
				actionBar.show();
			}
		}
		
		if (savedInstanceState == null) {
			density = (int)(resources.getDisplayMetrics().density);
			
			suffix = intent.getStringExtra(EXTRA_FILTER_FILETYPE);
			suffix = (suffix == null) ? "" : suffix;
			
			mimes = intent.getStringExtra(EXTRA_FILTER_MIMETYPE);
			mimes = (mimes == null) ? "" : mimes;

			final String extraTitle = intent.getStringExtra(EXTRA_TITLE);
			if (intent.hasExtra(EXTRA_TITLE)) {
				setTitle(extraTitle);
			} else {
				setTitle(R.string.pick_title);
			}
			
			multiFiles = intent.getBooleanExtra(EXTRA_MULTI_SELECT, true);
			if (ACTION_MULTI_SELECT.equals(action)) {//both dir & file
				if (suffix.length() == 0) {
					suffix = "*";
				}
			} else if (ACTION_PICK_DIRECTORY.equals(action)) {//dir
				if (suffix.length() > 0) {
					suffix = "";
				}
			} else if (ACTION_PICK_FILE.equals(action)) {//file
				if (suffix.length() == 0) {
					suffix = ".*";
				}
			} else if (Intent.ACTION_GET_CONTENT.equals(action)) {
				multiFiles = false;
				if (suffix.length() == 0) {
					suffix = ".*";
				}
			} else {
				suffix = "*";
			}
			previousSelectedStr = intent.getStringArrayExtra(PREVIOUS_SELECTED_FILES);
			Log.d(TAG, "previousSelectedStr " + Util.arrayToString(previousSelectedStr, true, "\n"));
			
			slideFrag = new SlidingTabsFragment();
			slideFrag.side = SlidingTabsFragment.Side.LEFT;
			final Uri data = intent.getData();
			dir = intent.getStringExtra(EXTRA_ABSOLUTE_PATH) == null ? data == null ? null : data.getPath() : intent.getStringExtra(EXTRA_ABSOLUTE_PATH) ;
			if (dir != null) {//} || !"*".equals(suffix) || mimes.length() != 0 || previousSelectedStr != null) {
				Log.d(TAG, "slideFrag.addTab(dir, suffix, mimes, multiFiles)");
				File file = new File(dir);
				if (file.isDirectory()) {
					//slideFrag.addTab(dir, suffix, mimes, multiFiles);
					slideFrag.initLeftContentFragmentTabs(AndroidUtils.getSharedPreference(this, "curContentFragPath", dir));
//					final Bundle bundle = new Bundle();
//					bundle.putString(EXTRA_ABSOLUTE_PATH, dir);
//					bundle.putString(EXTRA_FILTER_FILETYPE, suffix);
//					bundle.putBoolean(EXTRA_MULTI_SELECT, multiFiles);
//					bundle.putString(EXTRA_FILTER_MIMETYPE, mimes);
//					bundle.putBoolean(EXTRA_WRITEABLE_ONLY, intent.getBooleanExtra(EXTRA_WRITEABLE_ONLY, false));
//					bundle.putBoolean(EXTRA_DIRECTORIES_ONLY, intent.getBooleanExtra(EXTRA_DIRECTORIES_ONLY, false));
//					bundle.putStringArray(PREVIOUS_SELECTED_FILES, previousSelectedStr);
//
//					final ContentFragment contentFrag = new ContentFragment();
//					contentFrag.setArguments(bundle);
//					contentFrag.slidingTabsFragment = slideFrag;
//					slideFrag.addPagerItem(contentFrag);
				} else if (FileUtil.extractiblePattern.matcher(file.getName()).matches()) {
					slideFrag.addTab(file.getParent(), suffix, mimes, multiFiles);
					slideFrag.addZip(Frag.TYPE.ZIP, dir);
				} else {
					slideFrag.addTab(file.getParent(), suffix, mimes, multiFiles);
					getFutils().openFile(file, this);
				}
			} else {
				final File defaultFile = new File(AndroidUtils.getDefaultPickFilePath(this));
				if (defaultFile.exists()) {
					dir = defaultFile.getAbsolutePath();
				} else {
					dir = "/storage";
					AndroidUtils.setDefaultPickFilePath(this, dir);
				}
				Log.d(TAG, "slideFrag.initContentFragmentTabs()");
				slideFrag.initLeftContentFragmentTabs(AndroidUtils.getSharedPreference(this, "curContentFragPath", dir));
			}
		} else {
			suffix = savedInstanceState.getString(EXTRA_FILTER_FILETYPE, "");
			mimes = savedInstanceState.getString(EXTRA_FILTER_MIMETYPE, "");
			multiFiles = savedInstanceState.getBoolean(EXTRA_MULTI_SELECT, true);
			dir = savedInstanceState.getString(EXTRA_ABSOLUTE_PATH);
			previousSelectedStr = savedInstanceState.getStringArray(PREVIOUS_SELECTED_FILES);
			slideFrag1Selected = savedInstanceState.getBoolean("slideFrag1Selected");
			slideFrag = (SlidingTabsFragment) supportFragmentManager.findFragmentByTag("slideFrag");
			curContentFragIndex = savedInstanceState.getInt("curContentFragIndex");
			curSelectionFragIndex = savedInstanceState.getInt("curSelectionFragIndex");
			
			COPY_PATH = savedInstanceState.getParcelableArrayList("COPY_PATH");
			MOVE_PATH = savedInstanceState.getParcelableArrayList("MOVE_PATH");
			originPath_oppathe = savedInstanceState.getString("oppathe");
			newPath_oppathe1 = savedInstanceState.getString("oppathe1");
			originPaths_oparrayList = savedInstanceState.getParcelableArrayList("oparrayList");
			operation = savedInstanceState.getInt("operation");
			selectedStorage = savedInstanceState.getInt("selectitem", SELECT_0);
			//mainFragment = (Main) savedInstanceState.getParcelable("main_fragment");
			if (adapter != null) {
				adapter.toggleChecked(selectedStorage);
			}
		}
		
		balance = AndroidUtils.getSharedPreference(this, "biggerequalpanel", balance);
		Log.i(TAG, "intent " + action + ", suffix " + suffix + ", mime " + mimes + ", multiFiles " + multiFiles + ", balance=" + balance + ", dir " + dir + ", Category " + intent.getCategories() + ", DataString " + intent.getDataString() + ", Type " + intent.getType() + ", Package " + intent.getPackage() + ", Scheme " + intent.getScheme() + ", Extras " + intent.getExtras() + ", Component " + intent.getComponent() + ", Flags " + intent.getFlags());
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)left.getLayoutParams();
		if (balance > 0) {
			params.weight = 1.0f;
			left.setLayoutParams(params);
			params = (LinearLayout.LayoutParams)right.getLayoutParams();
			params.weight = 2.0f;
			right.setLayoutParams(params);
		} else if (balance < 0) {
			params.weight = 2.0f;
			left.setLayoutParams(params);
			params = (LinearLayout.LayoutParams)right.getLayoutParams();
			params.weight = 1.0f;
			right.setLayoutParams(params);
		} else {
			params.weight = 1.0f;
			left.setLayoutParams(params);
			params = (LinearLayout.LayoutParams)right.getLayoutParams();
			params.weight = 1.0f;
			right.setLayoutParams(params);
		}
		
		final FragmentTransaction transaction = supportFragmentManager.beginTransaction();
		swap = AndroidUtils.getSharedPreference(this, "swap", false);
		transaction.replace(R.id.content_fragment, slideFrag, "slideFrag");
		if (multiFiles) {
			horizontalDivider5.setBackgroundColor(DIVIDER_COLOR);
			if (savedInstanceState == null) {
				slideFrag2 = new SlidingTabsFragment();
				slideFrag2.side = SlidingTabsFragment.Side.RIGHT;
				if (intent.getStringExtra(EXTRA_ABSOLUTE_PATH) != null ||
					!"*".equals(suffix) || mimes.length() != 0 || previousSelectedStr != null) {
					Log.d(TAG, "slideFrag2.addTab(previousSelectedStr)");
					slideFrag2.addTab(previousSelectedStr);
				} else {
					Log.d(TAG, "slideFrag2.addTab(\"/storage\", suffix, mimes, multiFiles)");
					slideFrag2.addTab(AndroidUtils.getSharedPreference(this, "curExplorerFragPath", "/storage"), suffix, mimes, multiFiles);
					slideFrag2.addTab(null);
				}
			} else {
				slideFrag2 = (SlidingTabsFragment) supportFragmentManager.findFragmentByTag("slideFrag2");
				curExplorerFragIndex = savedInstanceState.getInt("curExplorerFragIndex");
				curSelectionFragIndex2 = savedInstanceState.getInt("curSelectionFragIndex2");
				//Log.d(TAG, "curExploreFragIndex " + curExploreFragIndex + ", curSelectionFrag2Index " + curSelectionFrag2Index);
			}
			transaction.replace(R.id.content_fragment2, slideFrag2, "slideFrag2");
		} else {
			right.setVisibility(View.GONE);
			horizontalDivider5.setVisibility(View.GONE);
		}
		transaction.commit();
		
		if (swap && multiFiles) {
			final ViewGroup parent = (ViewGroup)left.getParent();
			parent.removeView(left);
			parent.removeView(right);
			parent.addView(right, 0);
			parent.addView(left, 2);
		}
		
        // setting window background color instead of each item, in order to reduce pixel overdraw
        //final AppTheme appTheme = getAppTheme();
		//Log.d(TAG, "appTheme " + appTheme);
//		if (appTheme.equals(AppTheme.LIGHT)) {
//            //window.setBackgroundDrawableResource(android.R.color.white);
			mDrawerList.setBackgroundColor(BASE_BACKGROUND);
//        } else {
//            //window.setBackgroundDrawableResource(R.color.holo_dark_background);
//			if (appTheme.equals(AppTheme.DARK)) {
//				mDrawerList.setBackgroundColor(ContextCompat.getColor(this, R.color.holo_dark_background));
//			}
//		}
        
        if (!isDrawerLocked) {
            mDrawerToggle = new ActionBarDrawerToggle(
				this,                  /* host Activity */
				mDrawerLayout,         /* DrawerLayout object */
				R.drawable.ic_drawer_l,  /* nav drawer image to replace 'Up' caret */
				R.string.drawer_open,  /* "open drawer" description for accessibility */
				R.string.drawer_close  /* "close drawer" description for accessibility */
            ) {
                public void onDrawerClosed(View view) {
                    //ExplorerActivity.this.onDrawerClosed();
                }

                public void onDrawerOpened(View drawerView) {
                    //title.setText("Amaze File Manager");
                    // creates call to onPrepareOptionsMenu()
                }
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);
            mDrawerToggle.syncState();
			if (actionBar != null && Intent.ACTION_MAIN.equals(action)) {
				actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer_l);
				actionBar.setDisplayHomeAsUpEnabled(true);
				actionBar.setHomeButtonEnabled(true);
			}
        }
        
        if (mDrawerToggle != null) {
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_drawer_l);
        }

        if (!sharedPref.getBoolean(KEY_PREFERENCE_BOOKMARKS_ADDED, false)) {
            utilsHandler.addCommonBookmarks();
            sharedPref.edit().putBoolean(KEY_PREFERENCE_BOOKMARKS_ADDED, true).commit();
        }
		adapter = new DrawerAdapter(this, this, new ArrayList<Item>(0), this, sharedPref);
        mDrawerList.setAdapter(adapter);
			new AsyncTask<Void, Void, Void>() {
				@Override
				public Void doInBackground(Void... o) {
					dataUtils.setHiddenfiles(utilsHandler.getHiddenList());
					dataUtils.setGridfiles(utilsHandler.getGridViewList());
					dataUtils.setListfiles(utilsHandler.getListViewList());
					dataUtils.setBooks(utilsHandler.getBookmarksList());
					dataUtils.setServers(utilsHandler.getSmbList());
					if (CloudSheetFragment.isCloudProviderAvailable(ExplorerActivity.this)) {
						getSupportLoaderManager().initLoader(REQUEST_CODE_CLOUD_LIST_KEYS, null, ExplorerActivity.this);
					}
					return null;
				}

				@Override
				public void onPostExecute(Void result) {
					refreshDrawer();
					//return null;
				}

				@Override
				public void onPreExecute() {
					//return null;
				}
			}.execute();
		cleanFiles();
	}

	public ExplorerActivity() {
		super();
		prevTheme = - 1;
	}
	
	@Override
	public void onItemClick(AdapterView parent, View view, int position, long id) {
		selectItem(position);
	}
	
    /**
     * Initializes an interactive shell, which will stay throughout the app lifecycle
     * The shell is associated with a handler thread which maintain the message queue from the
     * callbacks of shell as we certainly cannot allow the callbacks to run on same thread because
     * of possible deadlock situation and the asynchronous behaviour of LibSuperSU
     */
    private void initializeInteractiveShell() {
        // only one looper can be associated to a thread. So we're making sure not to create new
        // handler threads every time the code relaunch.
        if (rootMode) {
            handlerThread = new HandlerThread("handler");
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
            shellInteractive = (new Shell.Builder()).useSU().setHandler(handler).open();

            // TODO: check for busybox
            /*try {
			 if (!RootUtils.isBusyboxAvailable()) {
			 Toast.makeText(this, getString(R.string.error_busybox), Toast.LENGTH_LONG).show();
			 closeInteractiveShell();
			 sharedPref.edit().putBoolean(PreferenceUtils.KEY_ROOT, false).apply();
			 }
			 } catch (RootNotPermittedException e) {
			 e.printStackTrace();
			 sharedPref.edit().putBoolean(PreferenceUtils.KEY_ROOT, false).apply();
			 }*/
        }
    }

    /**
     * Returns all available SD-Cards in the system (include emulated)
     * <p>
     * Warning: Hack! Based on Android source code of version 4.3 (API 18)
     * Because there is no standard way to get it.
     * TODO: Test on future Android versions 4.4+
     *
     * @return paths to all available SD-Cards in the system (include emulated)
     */
    public synchronized ArrayList<String> getStorageDirectories() {
        // Final set of paths
        final ArrayList<String> rv = new ArrayList<>();
        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            // Device has physical external storage; use plain paths.
            if (TextUtils.isEmpty(rawExternalStorage)) {
                // EXTERNAL_STORAGE undefined; falling back to default.
                rv.add("/storage/sdcard0");
            } else {
                rv.add(rawExternalStorage);
            }
        } else {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            if (SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                rawUserId = "";
            } else {
                final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                final String[] folders = DIR_SEPARATOR.split(path);
                final String lastFolder = folders[folders.length - 1];
                boolean isDigit = false;
                try {
                    Integer.valueOf(lastFolder);
                    isDigit = true;
                } catch (NumberFormatException ignored) {
                }
                rawUserId = isDigit ? lastFolder : "";
            }
            // /storage/emulated/0[1,2,...]
            if (TextUtils.isEmpty(rawUserId)) {
                rv.add(rawEmulatedStorageTarget);
            } else {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }
        if (SDK_INT >= Build.VERSION_CODES.M && checkStoragePermission())
            rv.clear();
        if (SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String strings[] = FileUtil.getExtSdCardPathsForActivity(this);
            for (String s : strings) {
                File f = new File(s);
                if (!rv.contains(s) && Futils.canListFiles(f))
                    rv.add(s);
            }
        }
        if (ThemedActivity.rootMode)
            rv.add("/");
        File usb = getUsbDrive();
        if (usb != null && !rv.contains(usb.getPath())) rv.add(usb.getPath());

        if (SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (isUsbDeviceConnected()) rv.add(OTGUtil.PREFIX_OTG + "/");
        }
        return rv;
    }

    /**
     * Method finds whether a USB device is connected or not
     * @return true if device is connected
     */
    private boolean isUsbDeviceConnected() {
        UsbManager usbManager = (UsbManager) getSystemService(USB_SERVICE);
        if (usbManager.getDeviceList().size()!=0) {
            // we need to set this every time as there is no way to know that whether USB device was
            // disconnected after closing the app and another one was connected
            // in that case the uri will obviously change
            // other wise we could persist the uri even after reopening the app by not writing
            // this preference when it's not null
            sharedPref.edit().putString(KEY_PREF_OTG, VALUE_PREF_OTG_NULL).apply();
            return true;
        } else {
            sharedPref.edit().putString(KEY_PREF_OTG, null).apply();
            return false;
        }
    }

    public void updateDrawer(String path) {
        new AsyncTask<String, Void, Integer>() {
            @Override
            protected Integer doInBackground(String... strings) {
                String path = strings[0];
                int k = 0, i = 0;
                String entryItemPathOld = "";
                for (Item item : dataUtils.getList()) {
                    if (!item.isSection()) {

                        String entryItemPath = ((EntryItem) item).getPath();

                        if (path.contains(((EntryItem) item).getPath())) {

                            if (entryItemPath.length() > entryItemPathOld.length()) {


                                // we don't need to match with the quick search drawer items
                                // whether current entry item path is bigger than the older one found,
                                // for eg. when we have /storage and /storage/Movies as entry items
                                // we would choose to highlight /storage/Movies in drawer adapter
                                k = i;

                                entryItemPathOld = entryItemPath;
                            }
                        }
                    }
                    i++;
                }
                return k;
            }

            @Override
            public void onPostExecute(Integer integers) {
                if (adapter != null)
                    adapter.toggleChecked(integers.intValue());
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);

    }

    public void selectItem(final int i) {
        ArrayList<Item> directoryItems = dataUtils.getList();
        if (!directoryItems.get(i).isSection()) {
            if ((selectedStorage == NO_VALUE || selectedStorage >= directoryItems.size())) {
//                TabFragment tabFragment = new TabFragment();
//                Bundle a = new Bundle();
//                a.putString("path", ((EntryItem) directoryItems.get(i)).getPath());
//
//                tabFragment.setArguments(a);
//
//                android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//                transaction.replace(R.id.content_frame, tabFragment);
//
//                transaction.addToBackStack("tabt1" + 1);
//                pending_fragmentTransaction = transaction;
//                selectedStorage = i;
//                adapter.toggleChecked(selectedStorage);
//                if (!isDrawerLocked) mDrawerLayout.closeDrawer(mDrawerLinear);
//                else onDrawerClosed();
//                floatingActionButton.setVisibility(View.VISIBLE);
//                floatingActionButton.showMenuButton(true);
            } else {
                pendingPath = ((EntryItem) directoryItems.get(i)).getPath();

                selectedStorage = i;
                adapter.toggleChecked(selectedStorage);

                if (((EntryItem) directoryItems.get(i)).getPath().contains(OTGUtil.PREFIX_OTG) &&
					sharedPref.getString(KEY_PREF_OTG, null).equals(VALUE_PREF_OTG_NULL)) {
                    // we've not gotten otg path yet
                    // start system request for storage access framework
                    Toast.makeText(getApplicationContext(),
								   getString(R.string.otg_access), Toast.LENGTH_LONG).show();
                    Intent safIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    startActivityForResult(safIntent, REQUEST_CODE_SAF);
                } else {
					onDrawerClosed();
                    if (!isDrawerLocked) {
						mDrawerLayout.closeDrawer(mDrawerList);
                    } 
                }
            }
        }
    }

    void showToast(String message) {
        if (this.toast == null) {
            // Create toast if found null, it would he the case of first call only
            this.toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        } else if (this.toast.getView() == null) {
            // Toast not showing, so create new one
            this.toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        } else {
            // Updating toast message is showing
            this.toast.setText(message);
        }
        // Showing toast finally
        this.toast.show();
    }

    void killToast() {
        if (this.toast != null)
            this.toast.cancel();
    }
	
	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onPostCreate " + savedInstanceState);
		super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle != null) 
			mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
		Log.d(TAG, "configurationChanged " + configurationChanged);
        super.onConfigurationChanged(newConfig);
		configurationChanged = true;
        // Pass any configuration change to the drawer toggls
        if (mDrawerToggle != null) 
			mDrawerToggle.onConfigurationChanged(newConfig);
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState " + outState);
		super.onSaveInstanceState(outState);

		outState.putString(EXTRA_ABSOLUTE_PATH, dir);//EXTRA_DIR_PATH
		outState.putString(EXTRA_FILTER_FILETYPE, suffix);
		outState.putString(EXTRA_FILTER_MIMETYPE, mimes);
		outState.putBoolean(EXTRA_MULTI_SELECT, multiFiles);
		outState.putStringArray(PREVIOUS_SELECTED_FILES, previousSelectedStr);
		
		outState.putBoolean("slideFrag1Selected", slideFrag1Selected);
		outState.putInt("curContentFragIndex", (curContentFragIndex=slideFrag.realFragCount() == 1 ? 0 : slideFrag.indexOfMTabs(curContentFrag)+1));
		AndroidUtils.setSharedPreference(this, "curContentFragPath", curContentFrag.currentPathTitle);
		outState.putInt("curSelectionFragIndex", (curSelectionFragIndex=curSelectionFrag != null ? slideFrag.indexOfMTabs(curSelectionFrag) + 1: -1));
		if (slideFrag2 != null) {
			outState.putInt("curExplorerFragIndex", (curExplorerFragIndex=slideFrag2.realFragCount() == 1 ? 0 : slideFrag2.indexOfMTabs(curExplorerFrag)+1));
			AndroidUtils.setSharedPreference(this, "curExplorerFragPath", curExplorerFrag.currentPathTitle);
			outState.putInt("curSelectionFragIndex2", (curSelectionFragIndex2 = curSelectionFrag2 != null ? slideFrag2.indexOfMTabs(curSelectionFrag2) + 1: -1));
		}
		
        if (selectedStorage != NO_VALUE)
            outState.putInt("selectitem", selectedStorage);
        if (COPY_PATH != null)
            outState.putParcelableArrayList("COPY_PATH", COPY_PATH);
        if (MOVE_PATH != null)
            outState.putParcelableArrayList("MOVE_PATH", MOVE_PATH);
        if (originPath_oppathe != null) {
            outState.putString("oppathe", originPath_oppathe);
            outState.putString("oppathe1", newPath_oppathe1);
            outState.putParcelableArrayList("oparraylist", (originPaths_oparrayList));
            outState.putInt("operation", operation);
        }

	}

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
		Glide.get(this).clearMemory();
        super.onPause();
        unregisterReceiver(mainActivityHelper.mNotificationReceiver);
        unregisterReceiver(receiver2);

        if (SDK_INT >= Build.VERSION_CODES.KITKAT) {
            unregisterReceiver(mOtgReceiver);
        }
		scheduleHandler.removeCallbacks(runSchedule);
        killToast();
    }

	@Override
	public void onStop() {
		super.onStop();
		if (rootMode) {
            // close interactive shell and handler thread associated with it
            if (SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                // let it finish up first with what it's doing
                handlerThread.quitSafely();
            } else handlerThread.quit();
            shellInteractive.close();
        }
	}

//	@Override
//	public void onStart() {
//		Log.d(TAG, "onStart");
//		super.onStart();
//	}

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
		super.onResume();
		curContentFrag = (ContentFragment) slideFrag.getFragmentIndex(curContentFragIndex);
		if (curSelectionFragIndex >= 0) {
			curSelectionFrag = (ContentFragment) slideFrag.getFragmentIndex(curSelectionFragIndex);
		}
		//Log.d(TAG, "onResume curContentFrag " + curContentFrag);
		if (slideFrag2 != null) {
			curExplorerFrag = (ContentFragment) slideFrag2.getFragmentIndex(curExplorerFragIndex);
			if (curSelectionFragIndex2 >= 0) {
				curSelectionFrag2 = (ContentFragment) slideFrag2.getFragmentIndex(curSelectionFragIndex2);
			}
			Log.d(TAG, "onResume curContentFrag2 " + curSelectionFrag2);
		}

		scheduleHandler.postDelayed(runSchedule, 5000);
        
        final IntentFilter newFilter = new IntentFilter();
        newFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        newFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        newFilter.addDataScheme(ContentResolver.SCHEME_FILE);
        registerReceiver(mainActivityHelper.mNotificationReceiver, newFilter);
        registerReceiver(receiver2, new IntentFilter(TAG_INTENT_FILTER_GENERAL));

        if (SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Registering intent filter for OTG
            final IntentFilter otgFilter = new IntentFilter();
            otgFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            otgFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            registerReceiver(mOtgReceiver, otgFilter);
        }
    }

	/**
     * Receiver to check if a USB device is connected at the runtime of application
     * If device is not connected at runtime (i.e. it was connected when the app was closed)
     * then {@link #isUsbDeviceConnected()} method handles the connection through
     * {@link #getStorageDirectories()}
     */
    BroadcastReceiver mOtgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                sharedPref.edit().putString(KEY_PREF_OTG, VALUE_PREF_OTG_NULL).apply();
                refreshDrawer();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                sharedPref.edit().putString(KEY_PREF_OTG, null).apply();
                refreshDrawer();
                //goToMain("");
            }
        }
    };
	
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO: 6/5/2017 Android may choose to not call this method before destruction
        // TODO: https://developer.android.com/reference/android/app/Activity.html#onDestroy%28%29
        closeInteractiveShell();

        tabHandler.close();
        utilsHandler.close();
        cloudHandler.close();

        final CryptHandler cryptHandler = new CryptHandler(this);
        cryptHandler.close();

        /*if (mainFragment!=null)
            mainFragment = null;*/
    }

    /**
     * Closes the interactive shell and threads associated
     */
    private void closeInteractiveShell() {
        if (rootMode) {
            // close interactive shell and handler thread associated with it
            if (SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                // let it finish up first with what it's doing
                handlerThread.quitSafely();
            } else {
				handlerThread.quit();
			}
            shellInteractive.close();
        }
    }

    public File getUsbDrive() {
        File parent = new File("/storage");

        try {
            for (File f : parent.listFiles())
                if (f.exists() && f.getName().toLowerCase().contains("usb") && f.canExecute())
                    return f;
        } catch (Exception e) {}

        parent = new File("/mnt/sdcard/usbStorage");
        if (parent.exists() && parent.canExecute())
            return (parent);
        parent = new File("/mnt/sdcard/usb_storage");
        if (parent.exists() && parent.canExecute())
            return parent;

        return null;
    }

    public void refreshDrawer() {

        final ArrayList<Item> sectionItems = new ArrayList<>();
        ArrayList<String> storageDirectories = getStorageDirectories();
        storage_count = 0;
        sectionItems.add(new SectionItem());
		for (String file : storageDirectories) {
            File f = new File(file);
            String name;
            Drawable icon1 = ContextCompat.getDrawable(this, R.drawable.ic_sd_storage_white_56dp);
            if ("/storage/emulated/legacy".equals(file) || "/storage/emulated/0".equals(file)) {
                name = getResources().getString(R.string.storage);
            } else if ("/storage/sdcard1".equals(file)) {
                name = getResources().getString(R.string.extstorage);
            } else if ("/".equals(file)) {
                name = getResources().getString(R.string.rootdirectory);
                icon1 = ContextCompat.getDrawable(this, R.drawable.ic_drawer_root_white);
            } else if (file.contains(OTGUtil.PREFIX_OTG)) {
                name = "OTG";
                icon1 = ContextCompat.getDrawable(this, R.drawable.ic_usb_white_48dp);
            } else name = f.getName();
            if (!f.isDirectory() || f.canExecute()) {
                storage_count++;
                sectionItems.add(new EntryItem(name, file, icon1));
            }
        }
        dataUtils.setStorages(storageDirectories);
        sectionItems.add(new SectionItem());

        if (dataUtils.getServers().size() > 0) {
            Collections.sort(dataUtils.getServers(), new BookSorter());
            synchronized (dataUtils.getServers()) {
                for (String[] file : dataUtils.getServers()) {
                    sectionItems.add(new EntryItem(file[0], file[1], ContextCompat.getDrawable(this,
																							   R.drawable.ic_settings_remote_white_48dp)));
                }
            }
            sectionItems.add(new SectionItem());
        }

        ArrayList<String[]> accountAuthenticationList = new ArrayList<>();

        if (CloudSheetFragment.isCloudProviderAvailable(this)) {
            for (CloudStorage cloudStorage : dataUtils.getAccounts()) {
                if (cloudStorage instanceof Dropbox) {

                    sectionItems.add(new EntryItem(CloudHandler.CLOUD_NAME_DROPBOX,
												   CloudHandler.CLOUD_PREFIX_DROPBOX + "/",
												   ContextCompat.getDrawable(this, R.drawable.ic_dropbox_white_24dp)));

                    accountAuthenticationList.add(new String[] {
													  CloudHandler.CLOUD_NAME_DROPBOX,
													  CloudHandler.CLOUD_PREFIX_DROPBOX + "/",
												  });
                } else if (cloudStorage instanceof Box) {

                    sectionItems.add(new EntryItem(CloudHandler.CLOUD_NAME_BOX,
												   CloudHandler.CLOUD_PREFIX_BOX + "/",
												   ContextCompat.getDrawable(this, R.drawable.ic_box_white_24dp)));

                    accountAuthenticationList.add(new String[] {
													  CloudHandler.CLOUD_NAME_BOX,
													  CloudHandler.CLOUD_PREFIX_BOX + "/",
												  });
                } else if (cloudStorage instanceof OneDrive) {

                    sectionItems.add(new EntryItem(CloudHandler.CLOUD_NAME_ONE_DRIVE,
												   CloudHandler.CLOUD_PREFIX_ONE_DRIVE + "/",
												   ContextCompat.getDrawable(this, R.drawable.ic_onedrive_white_24dp)));

                    accountAuthenticationList.add(new String[] {
													  CloudHandler.CLOUD_NAME_ONE_DRIVE,
													  CloudHandler.CLOUD_PREFIX_ONE_DRIVE + "/",
												  });
                } else if (cloudStorage instanceof GoogleDrive) {

                    sectionItems.add(new EntryItem(CloudHandler.CLOUD_NAME_GOOGLE_DRIVE,
												   CloudHandler.CLOUD_PREFIX_GOOGLE_DRIVE + "/",
												   ContextCompat.getDrawable(this, R.drawable.ic_google_drive_white_24dp)));

                    accountAuthenticationList.add(new String[] {
													  CloudHandler.CLOUD_NAME_GOOGLE_DRIVE,
													  CloudHandler.CLOUD_PREFIX_GOOGLE_DRIVE + "/",
												  });
                }
            }
            Collections.sort(accountAuthenticationList, new BookSorter());

            if (accountAuthenticationList.size() != 0)
                sectionItems.add(new SectionItem());
        }

        if (sharedPref.getBoolean(PREFERENCE_SHOW_SIDEBAR_FOLDERS, true)) {
            if (dataUtils.getBooks().size() > 0) {

                Collections.sort(dataUtils.getBooks(), new BookSorter());

                ArrayList<String[]> books = dataUtils.getBooks();
				synchronized (books) {
                    for (String[] file : books) {
                        sectionItems.add(new EntryItem(file[0], file[1],
													   ContextCompat.getDrawable(this, R.drawable.folder_fab)));
                    }
                }
                sectionItems.add(new SectionItem());
            }
        }

        Boolean[] quickAccessPref = TinyDB.getBooleanArray(sharedPref, QuickAccessPref.KEY,
														   QuickAccessPref.DEFAULT);

        if (sharedPref.getBoolean(PREFERENCE_SHOW_SIDEBAR_QUICKACCESSES, true)) {
            if (quickAccessPref[0])
                sectionItems.add(new EntryItem(getResources().getString(R.string.quick), "5",
											   ContextCompat.getDrawable(this, R.drawable.ic_star_white_18dp)));
            if (quickAccessPref[1])
                sectionItems.add(new EntryItem(getResources().getString(R.string.recent), "6",
											   ContextCompat.getDrawable(this, R.drawable.ic_history_black_36dp)));
            if (quickAccessPref[2])
                sectionItems.add(new EntryItem(getResources().getString(R.string.images), "0",
											   ContextCompat.getDrawable(this, R.drawable.ic_doc_image)));
            if (quickAccessPref[3])
                sectionItems.add(new EntryItem(getResources().getString(R.string.videos), "1",
											   ContextCompat.getDrawable(this, R.drawable.ic_doc_video_am)));
            if (quickAccessPref[4])
                sectionItems.add(new EntryItem(getResources().getString(R.string.audio), "2",
											   ContextCompat.getDrawable(this, R.drawable.ic_doc_audio_am)));
            if (quickAccessPref[5])
                sectionItems.add(new EntryItem(getResources().getString(R.string.documents), "3",
											   ContextCompat.getDrawable(this, R.drawable.ic_doc_doc_am)));
            if (quickAccessPref[6])
                sectionItems.add(new EntryItem(getResources().getString(R.string.apks), "4",
											   ContextCompat.getDrawable(this, R.drawable.ic_doc_apk_grid)));
        } else {
            sectionItems.remove(sectionItems.size() - 1); //Deletes last divider
        }

        dataUtils.setList(sectionItems);

		horizontalDivider5.post(new Runnable() {
				@Override
				public void run() {
					adapter.clear();
					adapter.addAll(sectionItems);
					adapter.toggleChecked(selectedStorage);
				}
				
			
		});
		
    }
	
	ContentFragment getCurrentContentFragment() {
		return slideFrag1Selected ? curContentFrag : curExplorerFrag;
	}

	@Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
		Log.d(TAG, "onActivityResult: " + requestCode + ", " + intent);
		
		if (requestCode == FILES_REQUEST_CODE) {

			if (responseCode == Activity.RESULT_OK) {
				List<String> stringExtra = intent.getStringArrayListExtra(PREVIOUS_SELECTED_FILES);
				if (COMPRESS.equals(currentDialog)) {

					compressFrag.files = Util.collectionToString(stringExtra, false, "| ");

					compressFrag.show(getSupportFragmentManager(), COMPRESS);
					Log.d(TAG, "onActivityResult FILES_REQUEST_CODE Compress " + compressFrag);
				} else if (DECOMPRESS.equals(currentDialog)) {

					decompressFrag.files = Util.collectionToString(stringExtra, false, "| ");

					decompressFrag.show(getSupportFragmentManager(), DECOMPRESS);
					Log.d(TAG, "decompressFrag " + decompressFrag);
				} 
			} else { // RESULT_CANCEL
				showToast("No file selected");
				if (COMPRESS.equals(currentDialog)) {
					compressFrag.show(getSupportFragmentManager(), COMPRESS);
				} else if (DECOMPRESS.equals(currentDialog)) {
					decompressFrag.show(getSupportFragmentManager(), DECOMPRESS);
				} 
			}

		} else if (requestCode == SAVETO_REQUEST_CODE) {

			if (responseCode == Activity.RESULT_OK) {
				List<String> stringExtra = intent.getStringArrayListExtra(PREVIOUS_SELECTED_FILES);
				if (COMPRESS.equals(currentDialog)) {

					compressFrag.saveTo = stringExtra.get(0);

					compressFrag.show(getSupportFragmentManager(), COMPRESS);
					Log.d(TAG, "Compress " + compressFrag);
				} else if (DECOMPRESS.equals(currentDialog)) {

					decompressFrag.saveTo = stringExtra.get(0);

					decompressFrag.show(getSupportFragmentManager(), DECOMPRESS);
					Log.d(TAG, "decompressFrag " + decompressFrag);
				} 
			} else { // RESULT_CANCEL
				showToast("No folder selected");
				if (COMPRESS.equals(currentDialog)) {
					compressFrag.show(getSupportFragmentManager(), COMPRESS);
				} else if (DECOMPRESS.equals(currentDialog)) {
					decompressFrag.show(getSupportFragmentManager(), DECOMPRESS);
				} 
			}
		} else if (requestCode == image_selector_request_code) {
            if (sharedPref != null && intent != null && intent.getData() != null) {
                if (SDK_INT >= 19)
                    getContentResolver().takePersistableUriPermission(intent.getData(),
																	  Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sharedPref.edit().putString("drawer_header_path", intent.getData().toString()).commit();
                //setDrawerHeaderBackground();
            }
        } else if (requestCode == FROM_PREVIOUS_IO_ACTION) {
            Uri treeUri;
            if (responseCode == Activity.RESULT_OK) {
                // Get Uri from Storage Access Framework.
                treeUri = intent.getData();
                // Persist URI - this is required for verification of writability.
                if (treeUri != null) sharedPref.edit().putString("URI", treeUri.toString()).commit();
            } else {
                // If not confirmed SAF, or if still not writable, then revert settings.
                /* DialogUtil.displayError(getActivity(), R.string.message_dialog_cannot_write_to_folder_saf, false, currentFolder);
				 ||!FileUtil.isWritableNormalOrSaf(currentFolder)*/
                return;
            }

            // After confirmation, update stored value of folder.
            // Persist access permissions.

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
																  | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            switch (operation) {
                case DataUtils.DELETE://deletion
                    new DeleteTask(this, null).execute((originPaths_oparrayList));
                    break;
                case DataUtils.DELETE_IN_ZIP:
                    Runnable r = new Runnable() {
						@Override
						public void run() {
							Toast.makeText(ExplorerActivity.this, "Deletion finished", Toast.LENGTH_SHORT).show();
						}
					};
					final StringBuilder sb = new StringBuilder();
					for (ZipEntry ze : filesInZip) {
						sb.append(ze.path).append("\n");
					}
					new DecompressTask(this,
									   zip.file.getAbsolutePath(),
									   ExplorerApplication.PRIVATE_PATH,
									   sb.toString(),
									   "",
									   "",
									   "",
									   0,
									   "d",
									   r).execute();
                    break;
                case DataUtils.ADD_TO_ZIP:
                    new DeleteTask(this, null).execute((originPaths_oparrayList));
                    break;
                case DataUtils.UPDATE_ZIP:
                    new DeleteTask(this, null).execute((originPaths_oparrayList));
                    break;
                case DataUtils.COPY://copying
                    //legacy compatibility
                    if(originPaths_oparrayList != null && originPaths_oparrayList.size() != 0) {
                        oparrayListList = new ArrayList<>();
                        oparrayListList.add(originPaths_oparrayList);
                        originPaths_oparrayList = null;
                        originPaths_oppatheList = new ArrayList<>();
                        originPaths_oppatheList.add(originPath_oppathe);
                        originPath_oppathe = "";
                    }
                    for (int i = 0; i < oparrayListList.size(); i++) {
                        Intent intent1 = new Intent(this, CopyService.class);
                        intent1.putExtra(CopyService.TAG_COPY_SOURCES, oparrayListList.get(i));
                        intent1.putExtra(CopyService.TAG_COPY_TARGET, originPaths_oppatheList.get(i));
                        ServiceWatcherUtil.runService(this, intent1);
                    }
                    break;
                case DataUtils.MOVE://moving
                    //legacy compatibility
                    if(originPaths_oparrayList != null && originPaths_oparrayList.size() != 0) {
                        oparrayListList = new ArrayList<>();
                        oparrayListList.add(originPaths_oparrayList);
                        originPaths_oparrayList = null;
                        originPaths_oppatheList = new ArrayList<>();
                        originPaths_oppatheList.add(originPath_oppathe);
                        originPath_oppathe = "";
                    }
                    new MoveFiles(oparrayListList, slideFrag1Selected ? curContentFrag : curExplorerFrag,
								  this, OpenMode.FILE, null)
						.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, originPaths_oppatheList);
                    break;
                case DataUtils.NEW_FOLDER://mkdir
                    mainActivityHelper.mkDir(RootHelper.generateBaseFile(new File(originPath_oppathe), true),
											 slideFrag1Selected ? curContentFrag : curExplorerFrag);
                    break;
                case DataUtils.RENAME:
                    ContentFragment ma = slideFrag1Selected ? curContentFrag : curExplorerFrag;
                    mainActivityHelper.rename(ma.openMode, (originPath_oppathe),
											  (newPath_oppathe1), this, ThemedActivity.rootMode);
                    ma.updateList();
                    break;
                case DataUtils.NEW_FILE:
                    mainActivityHelper.mkFile(new HFile(OpenMode.FILE, originPath_oppathe), slideFrag1Selected ? curContentFrag : curExplorerFrag);
                    break;
//                case DataUtils.EXTRACT:
//                    mainActivityHelper.extractFile(new File(oppathe));
//                    break;
                case DataUtils.COMPRESS:
                    mainActivityHelper.compressFiles(new File(originPath_oppathe), originPaths_oparrayList);
            }
            operation = -1;
        } else if (requestCode == REQUEST_CODE_SAF && responseCode == Activity.RESULT_OK) {
            // otg access
            sharedPref.edit().putString(KEY_PREF_OTG, intent.getData().toString()).apply();

            if (!isDrawerLocked) 
				mDrawerLayout.closeDrawer(mDrawerList);
            else 
				onDrawerClosed();
        } else if (requestCode == REQUEST_CODE_SAF && responseCode != Activity.RESULT_OK) {
            // otg access not provided
            pendingPath = null;
        }
    }

    void initialisePreferences() {
        hidemode = sharedPref.getInt("hidemode", 0);
        showHidden = sharedPref.getBoolean("showHidden", false);
        useGridView = sharedPref.getBoolean("view", true);
        //currentTab = sharedPref.getInt(PreferenceUtils.KEY_CURRENT_TAB, PreferenceUtils.DEFAULT_CURRENT_TAB);
        skinStatusBar = (PreferenceUtils.getStatusColor(getColorPreference().getColorAsString(ColorUsage.PRIMARY)));
        colourednavigation = sharedPref.getBoolean("colorednavigation", false);
    }
	
	@Override
	public void onClick(View v) {
//		switch (v.getId()) {
//			case R.id.settingsbutton:
//				Intent in = new Intent(ExplorerActivity.this, PreferencesActivity.class);
//				startActivity(in);
//				finish();
//				break;
//			case R.id.settingsbutton:
//				break;
//			case R.id.settingsbutton:
//				break;
//				
//		}
	}

    void initialiseViews() {

//        mDrawerLinear = (ScrimInsetsRelativeLayout) findViewById(R.id.left_drawer);
//        if (getAppTheme().equals(AppTheme.DARK)) 
//			mDrawerLinear.setBackgroundColor(Utils.getColor(this, R.color.holo_dark_background));
//        else 
//			mDrawerLinear.setBackgroundColor(Color.WHITE);
//        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        //mDrawerLayout.setStatusBarBackgroundColor(Color.parseColor((currentTab==1 ? skinTwo : skin)));
//        mDrawerList = (ListView) findViewById(R.id.menu_drawer);
//        drawerHeaderView.setBackgroundResource(R.drawable.amaze_header);
//        //drawerHeaderParent.setBackgroundColor(Color.parseColor((currentTab==1 ? skinTwo : skin)));
//        if (findViewById(R.id.tab_frame) != null) {
//            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN, mDrawerLinear);
//            mDrawerLayout.openDrawer(mDrawerLinear);
//            mDrawerLayout.setScrimColor(Color.TRANSPARENT);
//            isDrawerLocked = true;
//        } else if (findViewById(R.id.tab_frame) == null) {

//		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, mDrawerList);
//            mDrawerLayout.closeDrawer(mDrawerList);
//            isDrawerLocked = false;
////        }
//        View settingsButton = findViewById(R.id.settingsbutton);
//        if (getAppTheme().equals(AppTheme.DARK)) {
//            settingsButton.setBackgroundResource(R.drawable.safr_ripple_black);
//            ((ImageView) settingsButton.findViewById(R.id.settingicon)).setImageResource(R.drawable.ic_settings_white_48dp);
//            ((TextView) settingsButton.findViewById(R.id.settingtext)).setTextColor(Utils.getColor(this, android.R.color.white));
//        }
//        settingsButton.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					Intent in = new Intent(ExplorerActivity.this, PreferencesActivity.class);
//					startActivity(in);
//					finish();
//				}
//
//			});
//        View appButton = findViewById(R.id.appbutton);
//        if (getAppTheme().equals(AppTheme.DARK)) {
//            appButton.setBackgroundResource(R.drawable.safr_ripple_black);
//            ((ImageView) appButton.findViewById(R.id.appicon)).setImageResource(R.drawable.ic_doc_apk_white);
//            ((TextView) appButton.findViewById(R.id.apptext)).setTextColor(Utils.getColor(this, android.R.color.white));
//        }
//        appButton.setOnClickListener(new View.OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					android.support.v4.app.FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
//					transaction2.replace(R.id.content_frame, new AppsList());
//					//appBarLayout.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
//					pending_fragmentTransaction = transaction2;
//					if (!isDrawerLocked) mDrawerLayout.closeDrawer(mDrawerList);
//					else onDrawerClosed();
//					selectedStorage = SELECT_MINUS_2;
//					adapter.toggleChecked(false);
//				}
//			});
//
//        View ftpButton = findViewById(R.id.ftpbutton);
//        if (getAppTheme().equals(AppTheme.DARK)) {
//            ftpButton.setBackgroundResource(R.drawable.safr_ripple_black);
//            ((ImageView) ftpButton.findViewById(R.id.ftpicon)).setImageResource(R.drawable.ic_ftp_dark);
//            ((TextView) ftpButton.findViewById(R.id.ftptext)).setTextColor(Utils.getColor(this, android.R.color.white));
//        }
//        ftpButton.setOnClickListener(new View.OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					android.support.v4.app.FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
//					transaction2.replace(R.id.content_frame, new FTPServerFragment());
//					//appBarLayout.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
//					pending_fragmentTransaction = transaction2;
//					if (!isDrawerLocked) mDrawerLayout.closeDrawer(mDrawerList);
//					else onDrawerClosed();
//					selectedStorage = SELECT_MINUS_2;
//					adapter.toggleChecked(false);
//				}
//			});
        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor((currentTab==1 ? skinTwo : skin))));

        // status bar0
        if (SDK_INT == 20 || SDK_INT == 19) {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            //tintManager.setStatusBarTintColor(Color.parseColor((currentTab==1 ? skinTwo : skin)));
            FrameLayout.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) findViewById(R.id.drawer_layout).getLayoutParams();
            SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
            if (!isDrawerLocked) p.setMargins(0, config.getStatusBarHeight(), 0, 0);
        } else if (SDK_INT >= 21) {
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            if (isDrawerLocked) {
//                window.setStatusBarColor(skinStatusBar);
//            } else {
//				window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//			}
            if (colourednavigation)
                window.setNavigationBarColor(skinStatusBar);
        }
    }

    public void renameBookmark(final String title, final String path) {
        if (dataUtils.containsBooks(new String[]{title, path}) != -1) {
            RenameBookmark renameBookmark = RenameBookmark.getInstance(title, path, getColorPreference().getColor(ColorUsage.ACCENT));
            if (renameBookmark != null)
                renameBookmark.show(getFragmentManager(), "renamedialog");
        }
    }

    void onDrawerClosed() {
        if (pending_fragmentTransaction != null) {
            pending_fragmentTransaction.commit();
            pending_fragmentTransaction = null;
        }
		
		final String path = this.pendingPath;
		this.pendingPath = null;
		if (path != null) {
            HFile hFile = new HFile(OpenMode.UNKNOWN, path);
            hFile.generateMode(this);
            Log.d(TAG, "pendingPath " + path + ", " + hFile + ", " + hFile.isSimpleFile());
			if (hFile.isSimpleFile()) {
                utils.openFile(new File(path), this);
                return;
            }
			(slideFrag1Selected ? slideFrag : slideFrag2).addTab(hFile.getMode(), path);
//            ContentFragment mainFrag = slideFrag1Selected ? curContentFrag : curExplorerFrag;
//            if (mainFrag != null) {
//                mainFrag.changeDir(pendingPath, OpenMode.UNKNOWN);
////            } else {
////                goToMain(pendingPath);
////                return;
//            }
//            pendingPath = null;
        }
        //supportInvalidateOptionsMenu();
    }
	
	private BroadcastReceiver receiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent i) {
            if (i.getStringArrayListExtra(TAG_INTENT_FILTER_FAILED_OPS) != null) {
                ArrayList<BaseFile> failedOps = i.getParcelableArrayListExtra(TAG_INTENT_FILTER_FAILED_OPS);
                if (failedOps != null) {
                    mainActivityHelper.showFailedOperationDialog(failedOps, i.getBooleanExtra("move", false), ExplorerActivity.this);
                }
            }
        }
    };

	public void showSMBDialog(String name, String path, boolean edit) {
        if (path.length() > 0 && name.length() == 0) {
            int i = dataUtils.containsServer(new String[]{name, path});
            if (i != -1)
                name = dataUtils.getServers().get(i)[0];
        }
        SmbConnectDialog smbConnectDialog = new SmbConnectDialog();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString(EXTRA_ABSOLUTE_PATH, path);
        bundle.putBoolean("edit", edit);
        smbConnectDialog.setArguments(bundle);
        smbConnectDialog.show(getFragmentManager(), "smbdailog");
    }
	
    @Override
    public void addConnection(boolean edit, final String name, final String path, final String encryptedPath,
                              final String oldname, final String oldPath) {

        String[] s = new String[]{name, path};
        if (!edit) {
            if ((dataUtils.containsServer(path)) == -1) {
                dataUtils.addServer(s);
                refreshDrawer();

                AppConfig.runInBackground(new Runnable() {
						@Override
						public void run() {
							utilsHandler.addSmb(name, encryptedPath);
						}
					});
                //grid.addPath(name, encryptedPath, DataUtils.SMB, 1);
                ContentFragment ma = slideFrag1Selected ? curContentFrag : curExplorerFrag;
                if (ma != null) 
					ma.loadlist(path, OpenMode.UNKNOWN);//false, 
            } else {
                Snackbar.make(mDrawerLayout, getResources().getString(R.string.connection_exists), Snackbar.LENGTH_SHORT).show();
            }
        } else {
            int i = dataUtils.containsServer(new String[]{oldname, oldPath});
            if (i != -1) {
                dataUtils.removeServer(i);

                AppConfig.runInBackground(new Runnable() {
						@Override
						public void run() {
							utilsHandler.renameSMB(oldname, oldPath, name, path);
						}
					});
                //ExplorerActivity.grid.removePath(oldname, oldPath, DataUtils.SMB);
            }
            dataUtils.addServer(s);
            Collections.sort(dataUtils.getServers(), new BookSorter());
            refreshDrawer();
            //ExplorerActivity.grid.addPath(name, encryptedPath, DataUtils.SMB, 1);
        }
    }

    @Override
    public void deleteConnection(final String name, final String path) {

        int i = dataUtils.containsServer(new String[]{name, path});
        if (i != -1) {
            dataUtils.removeServer(i);

            AppConfig.runInBackground(new Runnable() {
					@Override
					public void run() {
						utilsHandler.removeSmbPath(name, path);
					}
				});
            //grid.removePath(name, path, DataUtils.SMB);
            refreshDrawer();
        }

    }

    @Override
    public void onHiddenFileAdded(String path) {

        utilsHandler.addHidden(path);
    }

    @Override
    public void onHiddenFileRemoved(String path) {

        utilsHandler.removeHiddenPath(path);
    }

    @Override
    public void onHistoryAdded(String path) {
        utilsHandler.addHistory(path);
    }

    @Override
    public void onBookAdded(String[] path, boolean refreshdrawer) {
        utilsHandler.addBookmark(path[0], path[1]);
        if (refreshdrawer)
            refreshDrawer();
    }

    @Override
    public void onHistoryCleared() {

        utilsHandler.clearHistoryTable();
    }

    @Override
    public void delete(String title, String path) {

        utilsHandler.removeBookmarksPath(title, path);
        refreshDrawer();

    }

    @Override
    public void modify(String oldpath, String oldname, String newPath, String newname) {

        utilsHandler.renameBookmark(oldname, oldpath, newname, newPath);
        refreshDrawer();
    }

    @Override
    public void addConnection(OpenMode service) {

        try {
            if (cloudHandler.findEntry(service) != null) {
                // cloud entry already exists
                Toast.makeText(this, getResources().getString(R.string.connection_exists),
							   Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ExplorerActivity.this, getResources().getString(R.string.please_wait), Toast.LENGTH_LONG).show();
                Bundle args = new Bundle();
                args.putInt(ARGS_KEY_LOADER, service.ordinal());

                // check if we already had done some work on the loader
                Loader loader = getSupportLoaderManager().getLoader(REQUEST_CODE_CLOUD_LIST_KEY);
                if (loader != null && loader.isStarted()) {
                    // making sure that loader is not started
                    getSupportLoaderManager().destroyLoader(REQUEST_CODE_CLOUD_LIST_KEY);
                }
                getSupportLoaderManager().initLoader(REQUEST_CODE_CLOUD_LIST_KEY, args, this);
            }
        } catch (CloudPluginException e) {
            e.printStackTrace();
            Toast.makeText(this, getResources().getString(R.string.cloud_error_plugin),
						   Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void deleteConnection(OpenMode service) {
        cloudHandler.clear(service);
        dataUtils.removeAccount(service);

        runOnUiThread(new Runnable() {
				@Override
				public void run() {
					refreshDrawer();
				}
			});
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (cloudSyncTask != null && cloudSyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            cloudSyncTask.cancel(true);

        }

        Uri uri = Uri.withAppendedPath(Uri.parse("content://" + CloudContract.PROVIDER_AUTHORITY), "/keys.db/secret_keys");

        String[] projection = new String[] {
			CloudContract.COLUMN_ID,
			CloudContract.COLUMN_CLIENT_ID,
			CloudContract.COLUMN_CLIENT_SECRET_KEY
        };

        switch (id) {
            case REQUEST_CODE_CLOUD_LIST_KEY:
                Uri uriAppendedPath = uri;
                switch (OpenMode.getOpenMode(args.getInt(ARGS_KEY_LOADER, 2))) {
                    case GDRIVE:
                        uriAppendedPath = ContentUris.withAppendedId(uri, 2);
                        break;
                    case DROPBOX:
                        uriAppendedPath = ContentUris.withAppendedId(uri, 3);
                        break;
                    case BOX:
                        uriAppendedPath = ContentUris.withAppendedId(uri, 4);
                        break;
                    case ONEDRIVE:
                        uriAppendedPath = ContentUris.withAppendedId(uri, 5);
                        break;
                }
                return new CursorLoader(this, uriAppendedPath, projection, null, null, null);
            case REQUEST_CODE_CLOUD_LIST_KEYS:
                // we need a list of all secret keys

                try {
                    List<CloudEntry> cloudEntries = cloudHandler.getAllEntries();

                    // we want keys for services saved in database, and the cloudrail app key which
                    // is at index 1
                    String ids[] = new String[cloudEntries.size() + 1];

                    ids[0] = 1 + "";
                    for (int i=1; i<=cloudEntries.size(); i++) {

                        // we need to get only those cloud details which user wants
                        switch (cloudEntries.get(i-1).getServiceType()) {
                            case GDRIVE:
                                ids[i] = 2 + "";
                                break;
                            case DROPBOX:
                                ids[i] = 3 + "";
                                break;
                            case BOX:
                                ids[i] = 4 + "";
                                break;
                            case ONEDRIVE:
                                ids[i] = 5 + "";
                                break;
                        }
                    }
                    return new CursorLoader(this, uri, projection, CloudContract.COLUMN_ID, ids, null);
                } catch (CloudPluginException e) {
                    e.printStackTrace();

                    Toast.makeText(this, getResources().getString(R.string.cloud_error_plugin),
								   Toast.LENGTH_LONG).show();
                }
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {

        if (data == null) {
            Toast.makeText(this, getResources().getString(R.string.cloud_error_failed_restart),
						   Toast.LENGTH_LONG).show();
            return;
        }

        cloudSyncTask = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {

                if (data.getCount() > 0 && data.moveToFirst()) {
                    do {

                        switch (data.getInt(0)) {
                            case 1:
                                try {
                                    CloudRail.setAppKey(data.getString(1));
                                } catch (Exception e) {
                                    // any other exception due to network conditions or other error
                                    e.printStackTrace();
                                    AppConfig.toast(ExplorerActivity.this, getResources().getString(R.string.failed_cloud_api_key));
                                    return false;
                                }
                                break;
                            case 2:
                                // DRIVE
                                try {

                                    CloudEntry cloudEntryGdrive = null;
                                    CloudEntry savedCloudEntryGdrive;


                                    GoogleDrive cloudStorageDrive = new GoogleDrive(getApplicationContext(),
																					data.getString(1), "", CLOUD_AUTHENTICATOR_REDIRECT_URI, data.getString(2));
                                    cloudStorageDrive.useAdvancedAuthentication();

                                    if ((savedCloudEntryGdrive = cloudHandler.findEntry(OpenMode.GDRIVE)) != null) {
                                        // we already have the entry and saved state, get it

                                        try {
                                            cloudStorageDrive.loadAsString(savedCloudEntryGdrive.getPersistData());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                            // we need to update the persist string as existing one is been compromised

                                            cloudStorageDrive.login();
                                            cloudEntryGdrive = new CloudEntry(OpenMode.GDRIVE, cloudStorageDrive.saveAsString());
                                            cloudHandler.updateEntry(OpenMode.GDRIVE, cloudEntryGdrive);
                                        }

                                    } else {

                                        cloudStorageDrive.login();
                                        cloudEntryGdrive = new CloudEntry(OpenMode.GDRIVE, cloudStorageDrive.saveAsString());
                                        cloudHandler.addEntry(cloudEntryGdrive);
                                    }

                                    dataUtils.addAccount(cloudStorageDrive);
                                } catch (CloudPluginException e) {

                                    e.printStackTrace();
                                    AppConfig.toast(ExplorerActivity.this, getResources().getString(R.string.cloud_error_plugin));
                                    deleteConnection(OpenMode.GDRIVE);
                                    return false;
                                } catch (AuthenticationException e) {
                                    e.printStackTrace();
                                    AppConfig.toast(ExplorerActivity.this, getResources().getString(R.string.cloud_fail_authenticate));
                                    deleteConnection(OpenMode.GDRIVE);
                                    return false;
                                } catch (Exception e) {
                                    // any other exception due to network conditions or other error
                                    e.printStackTrace();
                                    AppConfig.toast(ExplorerActivity.this, getResources().getString(R.string.failed_cloud_new_connection));
                                    deleteConnection(OpenMode.GDRIVE);
                                    return false;
                                }
                                break;
                            case 3:
                                // DROPBOX
                                try {

                                    CloudEntry cloudEntryDropbox = null;
                                    CloudEntry savedCloudEntryDropbox;

                                    CloudStorage cloudStorageDropbox = new Dropbox(getApplicationContext(),
																				   data.getString(1), data.getString(2));

                                    if ((savedCloudEntryDropbox = cloudHandler.findEntry(OpenMode.DROPBOX)) != null) {
                                        // we already have the entry and saved state, get it

                                        try {
                                            cloudStorageDropbox.loadAsString(savedCloudEntryDropbox.getPersistData());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                            // we need to persist data again

                                            cloudStorageDropbox.login();
                                            cloudEntryDropbox = new CloudEntry(OpenMode.DROPBOX, cloudStorageDropbox.saveAsString());
                                            cloudHandler.updateEntry(OpenMode.DROPBOX, cloudEntryDropbox);
                                        }

                                    } else {

                                        cloudStorageDropbox.login();
                                        cloudEntryDropbox = new CloudEntry(OpenMode.DROPBOX, cloudStorageDropbox.saveAsString());
                                        cloudHandler.addEntry(cloudEntryDropbox);
                                    }

                                    dataUtils.addAccount(cloudStorageDropbox);
                                } catch (CloudPluginException e) {
                                    e.printStackTrace();
                                    AppConfig.toast(ExplorerActivity.this, getResources().getString(R.string.cloud_error_plugin));
                                    deleteConnection(OpenMode.DROPBOX);
                                    return false;
                                } catch (AuthenticationException e) {
                                    e.printStackTrace();
                                    AppConfig.toast(ExplorerActivity.this, getResources().getString(R.string.cloud_fail_authenticate));
                                    deleteConnection(OpenMode.DROPBOX);
                                    return false;
                                } catch (Exception e) {
                                    // any other exception due to network conditions or other error
                                    e.printStackTrace();
                                    AppConfig.toast(ExplorerActivity.this, getResources().getString(R.string.failed_cloud_new_connection));
                                    deleteConnection(OpenMode.DROPBOX);
                                    return false;
                                }
                                break;
                            case 4:
                                // BOX
                                try {

                                    CloudEntry cloudEntryBox = null;
                                    CloudEntry savedCloudEntryBox;

                                    CloudStorage cloudStorageBox = new Box(getApplicationContext(),
																		   data.getString(1), data.getString(2));

                                    if ((savedCloudEntryBox = cloudHandler.findEntry(OpenMode.BOX)) != null) {
                                        // we already have the entry and saved state, get it

                                        try {
                                            cloudStorageBox.loadAsString(savedCloudEntryBox.getPersistData());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                            // we need to persist data again

                                            cloudStorageBox.login();
                                            cloudEntryBox = new CloudEntry(OpenMode.BOX, cloudStorageBox.saveAsString());
                                            cloudHandler.updateEntry(OpenMode.BOX, cloudEntryBox);
                                        }

                                    } else {

                                        cloudStorageBox.login();
                                        cloudEntryBox = new CloudEntry(OpenMode.BOX, cloudStorageBox.saveAsString());
                                        cloudHandler.addEntry(cloudEntryBox);
                                    }

                                    dataUtils.addAccount(cloudStorageBox);
                                } catch (CloudPluginException e) {

                                    e.printStackTrace();
                                    AppConfig.toast(ExplorerActivity.this, getResources().getString(R.string.cloud_error_plugin));
                                    deleteConnection(OpenMode.BOX);
                                    return false;
                                } catch (AuthenticationException e) {
                                    e.printStackTrace();
                                    AppConfig.toast(ExplorerActivity.this, getResources().getString(R.string.cloud_fail_authenticate));
                                    deleteConnection(OpenMode.BOX);
                                    return false;
                                } catch (Exception e) {
                                    // any other exception due to network conditions or other error
                                    e.printStackTrace();
                                    AppConfig.toast(ExplorerActivity.this, getResources().getString(R.string.failed_cloud_new_connection));
                                    deleteConnection(OpenMode.BOX);
                                    return false;
                                }
                                break;
                            case 5:
                                // ONEDRIVE
                                try {

                                    CloudEntry cloudEntryOnedrive = null;
                                    CloudEntry savedCloudEntryOnedrive;

                                    CloudStorage cloudStorageOnedrive = new OneDrive(getApplicationContext(),
																					 data.getString(1), data.getString(2));

                                    if ((savedCloudEntryOnedrive = cloudHandler.findEntry(OpenMode.ONEDRIVE)) != null) {
                                        // we already have the entry and saved state, get it

                                        try {
                                            cloudStorageOnedrive.loadAsString(savedCloudEntryOnedrive.getPersistData());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                            // we need to persist data again

                                            cloudStorageOnedrive.login();
                                            cloudEntryOnedrive = new CloudEntry(OpenMode.ONEDRIVE, cloudStorageOnedrive.saveAsString());
                                            cloudHandler.updateEntry(OpenMode.ONEDRIVE, cloudEntryOnedrive);
                                        }

                                    } else {

                                        cloudStorageOnedrive.login();
                                        cloudEntryOnedrive = new CloudEntry(OpenMode.ONEDRIVE, cloudStorageOnedrive.saveAsString());
                                        cloudHandler.addEntry(cloudEntryOnedrive);
                                    }

                                    dataUtils.addAccount(cloudStorageOnedrive);
                                } catch (CloudPluginException e) {

                                    e.printStackTrace();
                                    AppConfig.toast(ExplorerActivity.this, getResources().getString(R.string.cloud_error_plugin));
                                    deleteConnection(OpenMode.ONEDRIVE);
                                    return false;
                                } catch (AuthenticationException e) {
                                    e.printStackTrace();
                                    AppConfig.toast(ExplorerActivity.this, getResources().getString(R.string.cloud_fail_authenticate));
                                    deleteConnection(OpenMode.ONEDRIVE);
                                    return false;
                                } catch (Exception e) {
                                    // any other exception due to network conditions or other error
                                    e.printStackTrace();
                                    AppConfig.toast(ExplorerActivity.this, getResources().getString(R.string.failed_cloud_new_connection));
                                    deleteConnection(OpenMode.ONEDRIVE);
                                    return false;
                                }
                                break;
                            default:
                                Toast.makeText(ExplorerActivity.this, getResources().getString(R.string.cloud_error_failed_restart),
											   Toast.LENGTH_LONG).show();
                                return false;
                        }
                    } while (data.moveToNext());
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean refreshDrawer) {
                super.onPostExecute(refreshDrawer);
                if (refreshDrawer) {
                    refreshDrawer();
                }
            }
        }.execute();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
	
	
	
	
	private static int prevTheme = - 1;
	private void updateColor() {
		
		final int theme = getAppTheme().getSimpleTheme().ordinal();
		Log.d(TAG, "updateColor " + theme + ", " + prevTheme + ", hourOfDay " + PreferenceUtils.hourOfDay() + ", configurationChanged " + configurationChanged);
		
		if (prevTheme != theme) {
			if (Build.VERSION.SDK_INT >= 21) {
				if (theme == 1) {
					mCurTheme = android.R.style.Theme_Material_Wallpaper;
					
					TEXT_COLOR = 0xfff0f0f0;
					BASE_BACKGROUND = 0xff303030;
					DIVIDER_COLOR = Color.DKGRAY;
					DIR_COLOR = Color.WHITE;
					FILE_COLOR = Color.WHITE;
					SELECTED_IN_LIST = 0xffb0b0b0;
					IN_DATA_SOURCE_2 = 0xff505050;
					IS_PARTIAL = 0xff707070;
				} else {
					mCurTheme = android.R.style.Theme_Material_Light;//AndroidUtils.getSharedPreference(this, "theme", android.R.style.Theme_Material_Light);
					
					SELECTED_IN_LIST = 0xFFFEF8BA;
					BASE_BACKGROUND = 0xFFFFFFE8;
					DIVIDER_COLOR = Color.LTGRAY;
					IN_DATA_SOURCE_2 = 0xFFFFF8D9;
					IS_PARTIAL = 0xFFFFF0CF;
					TEXT_COLOR = 0xff404040;
					DIR_COLOR = 0xff404040;
					FILE_COLOR = 0xff404040;
				}
			} else {
				if (theme == 1) {
					mCurTheme = android.R.style.Theme_Wallpaper;
					
					TEXT_COLOR = 0xfff0f0f0;
					BASE_BACKGROUND = 0xff303030;
					DIVIDER_COLOR = Color.DKGRAY;
					DIR_COLOR = Color.WHITE;
					FILE_COLOR = Color.WHITE;
					SELECTED_IN_LIST = 0xffb0b0b0;
					IN_DATA_SOURCE_2 = 0xff505050;
					IS_PARTIAL = 0xff707070;
				} else {
					mCurTheme = android.R.style.Theme_Holo_Light;
					
					SELECTED_IN_LIST = 0xFFFEF8BA;
					BASE_BACKGROUND = 0xFFFFFFE8;
					DIVIDER_COLOR = Color.LTGRAY;
					IN_DATA_SOURCE_2 = 0xFFFFF8D9;
					IS_PARTIAL = 0xFFFFF0CF;
					TEXT_COLOR = 0xff404040;
					DIR_COLOR = 0xff404040;
					FILE_COLOR = 0xff404040;
				}
			}
			
			if (ImageThreadLoader.compressIcon != null) {
				ImageThreadLoader.compressIcon.setColorFilter(TEXT_COLOR, PorterDuff.Mode.SRC_IN);
			}
			
			setTheme(mCurTheme);
			window.getDecorView().setBackgroundColor(BASE_BACKGROUND);

			if (configurationChanged) {
				configurationChanged = false;
			} else if (slideFrag != null) {
				prevTheme = theme;
				slideFrag.getView().setBackgroundColor(BASE_BACKGROUND);
				if (curContentFrag != null) {
					slideFrag.updateLayout(true);
				}
				slideFrag.notifyTitleChange();
				if (curExplorerFrag != null && curExplorerFrag.getContext() != null) {
					slideFrag2.getView().setBackgroundColor(BASE_BACKGROUND);
					slideFrag2.notifyTitleChange();
					slideFrag2.updateLayout(true);
					curContentFrag.select(slideFrag1Selected);
				}
			}
		}
	}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CAMERA) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.
            Log.d(TAG, "Received response for Camera permission request.");

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed
                Log.d(TAG, "CAMERA permission has now been granted. Showing preview.");
                Snackbar.make(left, R.string.permision_available_camera,
							  Snackbar.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "CAMERA permission was NOT granted.");
                Snackbar.make(left, R.string.permissions_not_granted,
							  Snackbar.LENGTH_SHORT).show();

            }
            // END_INCLUDE(permission_result)

        } else if (requestCode == REQUEST_WRITE_EXTERNAL) {
            Log.d(TAG, "Received response for WRITE EXTERNAL STORAGE permissions request.");

            // We have requested multiple permissions for contacts, so all of them need to be
            // checked.
            if (AndroidUtils.verifyPermissions(grantResults)) {
                Log.d(TAG, "All required permissions have been granted, display contacts fragment.");
                Snackbar.make(left, "WRITE EXTERNAL STORAGE Permission has been granted.",
							  Snackbar.LENGTH_SHORT)
					.show();
            } else {
                Log.d(TAG, "WRITE EXTERNAL STORAGE permissions were NOT granted.");
                Snackbar.make(left, R.string.permissions_not_granted, Snackbar.LENGTH_SHORT)
					.show();
            }
        } else if (requestCode == 77) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                refreshDrawer();
//                TabFragment tabFragment = getTabFragment();
//                boolean b = sharedPref.getBoolean("needtosethome", true);
//                //reset home and current paths according to new storages
//                if (b) {
//                    tabHandler.clear();
//                    if (storage_count > 1)
//                        tabHandler.addTab(new Tab(1, "", ((EntryItem) dataUtils.getList().get(1)).getPath(), "/"));
//                    else
//                        tabHandler.addTab(new Tab(1, "", "/", "/"));
//                    if (!dataUtils.getList().get(0).isSection()) {
//                        String pa = ((EntryItem) dataUtils.getList().get(0)).getPath();
//                        tabHandler.addTab(new Tab(2, "", pa, pa));
//                    } else
//                        tabHandler.addTab(new Tab(2, "", ((EntryItem) dataUtils.getList().get(1)).getPath(), "/"));
//                    if (tabFragment != null) {
//                        Fragment main = tabFragment.getFragmentAtIndex(0);
//                        if (main != null)
//                            ((MainFragment) main).updateTabWithDb(tabHandler.findTab(1));
//                        Fragment main1 = tabFragment.getFragmentAtIndex(1);
//                        if (main1 != null)
//                            ((MainFragment) main1).updateTabWithDb(tabHandler.findTab(2));
//                    }
//                    sharedPref.edit().putBoolean("needtosethome", false).commit();
//                } else {
//                    //just refresh list
//                    if (tabFragment != null) {
//                        Fragment main = tabFragment.getFragmentAtIndex(0);
//                        if (main != null)
//                            ((MainFragment) main).updateList();
//                        Fragment main1 = tabFragment.getFragmentAtIndex(1);
//                        if (main1 != null)
//                            ((MainFragment) main1).updateList();
//                    }
//                }
            } else {
                Toast.makeText(this, R.string.grantfailed, Toast.LENGTH_SHORT).show();
                requestStoragePermission();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }
	
	private void cleanFiles() {
		new Thread(new Runnable() {
				@Override
				public void run() {
					File cacheDir = Glide.getPhotoCacheDir(ExplorerActivity.this);
					Log.d(TAG, "cacheDir " + cacheDir.getAbsolutePath());
					new File(
						"/storage/sdcard0/AppProjects/PowerFileExplorer/PowerFileExplorer/build/bin/resources.ap_")
						.delete();
					new File(
						"/storage/sdcard0/AppProjects/PowerFileExplorer/PowerFileExplorer/build/bin/classes.dex")
						.delete();
					new File(
						"/storage/sdcard0/AppProjects/PowerFileExplorer/PowerFileExplorer/build/bin/PowerFileExplorer.apk")
						.delete();
					File ff = new File("/storage/emulated/0/.aide/enginecache");
					if (ff.exists()) {
						File[] fs = ff.listFiles();
						for (File f : fs) {
							f.delete();
						}
					}
				}
			}).start();
	}

	@Override
    public void onNewIntent(final Intent intent) {
        this.intent = intent;
        final Uri data = intent.getData();
		final String path = intent.getStringExtra(EXTRA_ABSOLUTE_PATH) == null ? data == null ? null : data.getPath() : intent.getStringExtra(EXTRA_ABSOLUTE_PATH);
        final String action = intent.getAction();
		Log.d(TAG, "onNewIntent " + path);
		if (path != null) {
			final File file = new File(path);
			if (file.isDirectory()) {
				dir = path;
				curContentFrag.suffix = intent.getStringExtra(EXTRA_FILTER_FILETYPE);
				curContentFrag.suffix = (curContentFrag.suffix == null) ? "*" : curContentFrag.suffix;
				curContentFrag.multiFiles = intent.getBooleanExtra(EXTRA_MULTI_SELECT, true);

				curContentFrag.mimes = intent.getStringExtra(EXTRA_FILTER_MIMETYPE);
				curContentFrag.mimes = curContentFrag.mimes == null ? "*/*" : curContentFrag.mimes.toLowerCase();
				curContentFrag.mWriteableOnly = intent.getBooleanExtra(EXTRA_WRITEABLE_ONLY, false);

				curContentFrag.previousSelectedStr = intent.getStringArrayExtra(PREVIOUS_SELECTED_FILES);

				curContentFrag.changeDir(path, true);
//				slideFrag.addNewTab(path, i.getStringExtra(ExplorerActivity.EXTRA_SUFFIX), i.getBooleanExtra(ExplorerActivity.EXTRA_MULTI_SELECT, true), true);
//				slideFrag.setCurrentItem(slideFrag.getCount() - 1);
            } else {
				if (FileUtil.extractiblePattern.matcher(new File(path).getName()).matches()) {
					final SlidingTabsFragment.PagerAdapter pagerAdapter = slideFrag.pagerAdapter;
					final int tabIndex2 = slideFrag.getFragIndex(Frag.TYPE.ZIP);
						if (tabIndex2 >= 0) {
							final ZipFragment zFrag = (ZipFragment) pagerAdapter.getItem(tabIndex2);
							zFrag.load(path, null);
							slideFrag.setCurrentItem(tabIndex2, true);
						} else {
							horizontalDivider5.postDelayed(new Runnable() {
									@Override
									public void run() {
										slideFrag.addTab(Frag.TYPE.ZIP, path);
									}
							}, 200);
						}
				} else {
					getFutils().openFile(file, this);
				}
			}
        } else if (intent.getStringArrayListExtra(TAG_INTENT_FILTER_FAILED_OPS) != null) {
            ArrayList<BaseFile> failedOps = intent.getParcelableArrayListExtra(TAG_INTENT_FILTER_FAILED_OPS);
            if (failedOps != null) {
                mainActivityHelper.showFailedOperationDialog(failedOps, intent.getBooleanExtra("move", false), this);
            }
        } else if (intent.getCategories() != null && intent.getCategories().contains(CLOUD_AUTHENTICATOR_GDRIVE)) {
            // we used an external authenticator instead of APIs. Probably for Google Drive
            CloudRail.setAuthenticationResponse(intent);
        } else if ((openProcesses = intent.getBooleanExtra(KEY_INTENT_PROCESS_VIEWER, false))) {
			Fragment findFragmentByTag;
			FragmentManager supportFragmentManager = getSupportFragmentManager();
			if ((findFragmentByTag = supportFragmentManager.findFragmentByTag(KEY_INTENT_PROCESS_VIEWER)) != null) {
				processViewer = (ProcessViewer)findFragmentByTag;
			} else {
				processViewer = new ProcessViewer();
			}
			processViewer.show(supportFragmentManager, KEY_INTENT_PROCESS_VIEWER);
//            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//            transaction.replace(R.id.content_frame, new ProcessViewer(), KEY_INTENT_PROCESS_VIEWER);
//            //   transaction.addToBackStack(null);
            selectedStorage = SELECT_102;
            openProcesses = false;
//            //title.setText(utils.getString(con, R.string.process_viewer));
//            //Commit the transaction
//            transaction.commitAllowingStateLoss();
//            supportInvalidateOptionsMenu();
        } else {
            if (SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                    if (sharedPref.getString(KEY_PREF_OTG, null) == null) {
                        sharedPref.edit().putString(KEY_PREF_OTG, VALUE_PREF_OTG_NULL).apply();
                        refreshDrawer();
                    }
                } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                    sharedPref.edit().putString(KEY_PREF_OTG, null).apply();
                    refreshDrawer();
                }
            }
        }
    }

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).trimMemory(level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e(TAG, "onLowMemory " + Runtime.getRuntime().freeMemory());
		Glide.get(this).clearMemory();
    }

	


//	private void selectItem(int position) {
//		// Create a new fragment and specify the planet to show based on
//		// position
//		android.support.v4.app.Fragment fragment = null;// new
//		// OpertingSystemFragment();
//		// switch (position) {
//		// case 0:
//		// fragment = new SplitMergeFragment();
//		// break;
//		// case 1:
//		// fragment = new CompressFragment();
//		// break;
//		// default:
//		//
//		// }
//		// Bundle args = new Bundle();
//		// args.putInt(OpertingSystemFragment.ARG_OS, position);
//		// fragment.setArguments(args);
//
//		// currentDialog = initDialog[position];
//		// Insert the fragment by replacing any existing fragment
//		FragmentManager fragmentManager = getSupportFragmentManager();
//		fragmentManager.beginTransaction()
//			.replace(R.id.content_frame, fragment).commit();
//
//		// Highlight the selected item, update the title, and close the drawer
//		mDrawerList.setItemChecked(position, true);
//		getActionBar().setTitle((mOperationsSystem[position]));
//		mDrawerLayout.closeDrawer(mDrawerList);
//	}



//	public boolean onOptionsItemSelected(MenuItem item) {
//		Log.d(TAG, "onOptionsItemSelected " + item);
//		// Pass the event to ActionBarDrawerToggle, if it returns
//		// true, then it has handled the app icon touch event
//		if (mDrawerToggle.onOptionsItemSelected(item)) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}

//	@Override
//	protected void onRestoreInstanceState(Bundle savedInstanceState) {
//		Log.d(TAG, "onRestoreInstanceState " + savedInstanceState);
////		super.onRestoreInstanceState(savedInstanceState);
//////		dir = savedInstanceState.getString(ExplorerActivity.EXTRA_DIR_PATH);
//////		suffix = savedInstanceState.getString(ExplorerActivity.EXTRA_SUFFIX);
//////		multiFiles = savedInstanceState.getBoolean(ExplorerActivity.EXTRA_MULTI_SELECT);
//////		slideFrag1Selected = savedInstanceState.getBoolean("slideFrag1Selected");
////		//previousSelectedStr = savedInstanceState.getStringArray(ExplorerActivity.PREVIOUS_SELECTED_FILES);
//////		cutl.addAll(Util.collectionString2FileArrayList(savedInstanceState.getStringArrayList("cutl")));
//////		copyl.addAll(Util.collectionString2FileArrayList(savedInstanceState.getStringArrayList("copyl")));
//	}

	private void showToast(CharSequence st) {
		Toast.makeText(this, st, Toast.LENGTH_LONG).show();
	}

	private class Schedule implements Runnable {
		int[] l;
		
		Schedule(int[]... all) {
			int[] h = all[0];
			int[] m = new int[h.length];
			int[] s = new int[h.length];
			
			if (all.length > 1) {
				m = all[1];
			}
			if (all.length > 2) {
				s = all[2];
			}
			
			l = new int[h.length];
			final int length = h.length;
			final int mlength = m.length;
			final int slength = s.length;
			int mm;
			int ss;
			
			for (int i = 0; i < length; i++) {
				mm = i < mlength ? m[i] : 0;
				ss = i < slength ? s[i] : 0;
				l[i] = (h[i] * 3600 + mm * 60 + ss) * 1000;
			}
			Arrays.sort(l);
		}
		
		@Override
		public void run() {
			final Calendar cal = Calendar.getInstance();
			
			cal.set(cal.get(Calendar.YEAR), 
					cal.get(Calendar.MONTH), 
					cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			final long today = cal.getTimeInMillis();
			
			final long now = System.currentTimeMillis() - today;
			Log.d(TAG, "now " + (now / 3600000 + ":" + (now % 3600000) / 60000 + ":" + (now % 60000)));
			
			int i = 0;
			for (i = 0; i < l.length; i++) {
				if (now < l[i]) {
					final long currentTimeMillis = l[i] - (System.currentTimeMillis() - today);
					scheduleHandler.postDelayed(this, currentTimeMillis >= 0 ? currentTimeMillis : 0);
					Log.d(TAG, "delay 1: " + l[i] + ", " + (System.currentTimeMillis() - today) + ", " + currentTimeMillis);
					break;
				} else if (l[i] <= now && now < l[i] + 1000) {
					updateColor();
					if (i < l.length - 1) {
						final long currentTimeMillis = l[i + 1] - (System.currentTimeMillis() - today);
						scheduleHandler.postDelayed(this, currentTimeMillis >= 0 ? currentTimeMillis : 0);
						Log.d(TAG, "delay 2: " + l[i+1] + ", " + (System.currentTimeMillis() - today) + ", " + currentTimeMillis);
					} else {
						final long currentTimeMillis = (today + 24 * 3600 * 1000) - System.currentTimeMillis();
						scheduleHandler.postDelayed(this, currentTimeMillis >= 0 ? currentTimeMillis : 0);
						Log.d(TAG, "delay 3: " + currentTimeMillis);
					}
					break;
				} 
			}
			if (i == l.length - 1 && l[i] + 1000 <= now) {
				final long currentTimeMillis = (today + 24 * 3600 * 1000) - System.currentTimeMillis();
				scheduleHandler.postDelayed(this, currentTimeMillis >= 0 ? currentTimeMillis : 0);
				Log.d(TAG, "delay 4: " + currentTimeMillis);
			}
		}
	}
	
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AndroidUtils.setSharedPreference(this, "curContentFragPath", curContentFrag.currentPathTitle);
			if (slideFrag2 != null) {
				AndroidUtils.setSharedPreference(this, "curExplorerFragPath", curExplorerFrag.currentPathTitle);
			}
			super.onBackPressed();
		}
		return true;
	}

	private static final int TIME_INTERVAL = 250;
	private long mBackPressed;
	@Override
	public void onBackPressed() {
		FragmentManager supportFragmentManager = getSupportFragmentManager();
		Fragment findFragmentByTag;
		if (!isDrawerLocked && mDrawerLayout.isDrawerOpen(mDrawerList)) {
			mDrawerLayout.closeDrawer(mDrawerList);
		} else if ((findFragmentByTag = supportFragmentManager.findFragmentByTag(KEY_INTENT_PROCESS_VIEWER)) != null) {
			processViewer = (ProcessViewer) findFragmentByTag;
			processViewer.dismiss();
//			final FragmentTransaction transaction = supportFragmentManager.beginTransaction();
//			transaction.remove(findFragmentByTag);
//			//transaction.addToBackStack(null);
			selectedStorage = SELECT_102;
			openProcesses = false;
//			//title.setText(utils.getString(con, R.string.process_viewer));
//
//			transaction.commit();
		} else {
			if (mBackPressed + TIME_INTERVAL >= System.currentTimeMillis()) {
				AndroidUtils.setSharedPreference(this, "curContentFragPath", curContentFrag.currentPathTitle);
				if (slideFrag2 != null) {
					AndroidUtils.setSharedPreference(this, "curExplorerFragPath", curExplorerFrag.currentPathTitle);
				}
				super.onBackPressed();
			} else {
				if (slideFrag1Selected) {
					final Frag currentFragment = slideFrag.getCurrentFragment();
					if (currentFragment == curContentFrag) {
						if (curContentFrag.searchMode) {
							curContentFrag.searchButton();
						} else if (!curContentFrag.back()) {
							Toast.makeText(getBaseContext(), "Press one more time to exit",
										   Toast.LENGTH_SHORT).show(); 
						}
					} else if (currentFragment == curSelectionFrag && curSelectionFrag.searchMode) {
						curSelectionFrag.searchButton();
					} else if (currentFragment.type == Frag.TYPE.ZIP) {
						((ZipFragment)currentFragment).back();
					}
				} else if (slideFrag2 != null) {
					final Frag currentFragment = slideFrag2.getCurrentFragment();
					if (currentFragment == curExplorerFrag) {
						if (curExplorerFrag.searchMode) {
							curExplorerFrag.searchButton();
						} else if (!curExplorerFrag.back()) {
							Toast.makeText(getBaseContext(), "Press one more time to exit",
										   Toast.LENGTH_SHORT).show(); 
						}
					} else if (currentFragment == curSelectionFrag2 && curSelectionFrag2.searchMode) {
						curSelectionFrag2.searchButton();
					} else if (currentFragment.type == Frag.TYPE.ZIP) {
						((ZipFragment)currentFragment).back();
					}
				}
				mBackPressed = System.currentTimeMillis();
			}
		}
	}

	public void clearCache(View v) {
        Log.d(TAG, "clearing cache");
        Glide.clear(v);
        //Glide.clear(imageViewNet);
        Glide.get(this).clearMemory();
        File cacheDir = Glide.getPhotoCacheDir(this);
        if (cacheDir.isDirectory()) {
            for (File child : cacheDir.listFiles()) {
                if (!child.delete()) {
                    Log.w(TAG, "cannot delete: " + child);
                }
            }
        }
        //reload();
    }
	
	public void ok(View view) {
		
		if ((curSelectionFrag2 == null || curSelectionFrag2.dataSourceL1.size() == 0)
			&& curContentFrag.selectedInList1.size() == 0 //&& !multiFiles
			&& (".*".equals(suffix))) {
			Toast.makeText(this, "Please select a file", Toast.LENGTH_LONG).show();
			return;
		}
		Log.d("selected file", curSelectionFrag2!=null?Util.collectionToString(curSelectionFrag2.dataSourceL1, false, "\r\n") : "null");
		ArrayList<String> fileArr = null;
		if (multiFiles) {
			if (curSelectionFrag2.dataSourceL1.size() > 0) {
				fileArr = Util.collectionFile2StringArrayList(curSelectionFrag2.dataSourceL1);
				Collections.sort(fileArr);
			} else if (curContentFrag.selectedInList1.size() > 0) {
				fileArr = Util.collectionFile2StringArrayList(curContentFrag.selectedInList1);
				Collections.sort(fileArr);
			} else {
				fileArr = new ArrayList<String>(1);
				fileArr.add(dir);
			}
		} else {
			if (curContentFrag.selectedInList1.size() > 0) {
				fileArr = new ArrayList<String>(1);
				fileArr.add(((LayoutElement)curContentFrag.selectedInList1.get(0)).path);
			} else {
				Toast.makeText(this, "Please select a file", Toast.LENGTH_LONG).show();
				return;
				//
				// fileArr = new String[] {dir};
			}
		}

		Intent intent = this.getIntent();
		intent.putStringArrayListExtra(PREVIOUS_SELECTED_FILES, fileArr);
		intent.putExtra(EXTRA_MULTI_SELECT, multiFiles);
		setResult(RESULT_OK, intent);
		this.finish();
	}

	public void cancel(View view) {
		Log.d("select previous file",
			  Util.arrayToString(previousSelectedStr, true, "\r\n"));
		Intent intent = this.getIntent();
		if (previousSelectedStr != null && previousSelectedStr.length > 0) {
			Arrays.sort(previousSelectedStr);
		}
		intent.putExtra(PREVIOUS_SELECTED_FILES, previousSelectedStr);
		intent.putExtra(EXTRA_MULTI_SELECT, multiFiles);
		setResult(RESULT_CANCELED, intent);
		this.finish();
	}

}
