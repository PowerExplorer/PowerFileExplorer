package net.gnu.explorer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.amaze.filemanager.activities.BasicActivity;
import net.gnu.androidutil.AndroidPathUtils;
import net.gnu.androidutil.AndroidUtils;
import net.gnu.util.FileUtil;
import net.gnu.util.Util;
import com.amaze.filemanager.services.asynctasks.*;
import com.amaze.filemanager.utils.*;
import com.amaze.filemanager.filesystem.*;
import com.amaze.filemanager.services.*;
import android.media.*;
import android.view.*;
import android.app.*;
import android.support.v7.widget.*;
//import net.gnu.intents.*;
//import net.gnu.filemanager.*;
import android.widget.ImageButton;
import java.util.*;
import android.widget.Button;
import com.bumptech.glide.*;
import android.graphics.*;
import android.content.res.*;
import android.graphics.drawable.Drawable;
import android.Manifest;
import android.support.design.widget.Snackbar;
import android.content.pm.*;
import android.support.v4.app.ActivityCompat;
import android.os.*;
import eu.chainfire.libsuperuser.*;
import com.amaze.filemanager.ui.dialogs.*;
import com.amaze.filemanager.exceptions.*;
import com.amaze.filemanager.database.*;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import com.amaze.filemanager.fragments.CloudSheetFragment.CloudConnectionCallbacks;
import com.amaze.filemanager.utils.provider.*;
import android.database.*;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.content.ContentUris;
import android.support.v4.app.LoaderManager;
import com.cloudrail.si.services.*;
import com.cloudrail.si.exceptions.*;
import com.cloudrail.si.interfaces.*;
import com.cloudrail.si.*;
import android.support.v4.content.*;
import com.amaze.filemanager.fragments.*;
import com.amaze.filemanager.ui.drawer.*;
import static android.os.Build.VERSION.SDK_INT;
import android.hardware.usb.*;
import android.text.*;
import android.graphics.drawable.ColorDrawable;
import java.util.regex.Pattern;
import com.amaze.filemanager.adapters.DrawerAdapter;
import com.amaze.filemanager.ui.LayoutElement;
import com.amaze.filemanager.activities.ThemedActivity;
import com.amaze.filemanager.database.models.CloudEntry;


public class ExplorerActivity extends ThemedActivity implements CloudConnectionCallbacks,
			UtilitiesProviderInterface,
			LoaderManager.LoaderCallbacks<Cursor> {
	private static final String TAG = "ExplorerActivity";
	
	public static int SELECTED_IN_LIST = 0xFFFEF8BA;//0xFFFFF0A0
	public static int BASE_BACKGROUND = 0xFFFFFFE8;
	public static int IN_DATA_SOURCE_2 = 0xFFFFF8D9;
	public static int IS_PARTIAL = 0xFFFFF0CF;
	//public static final int GREY = Color.parseColor("#ff444444");
	public static final int LIGHT_GREY = 0xff909090;
	public static int TEXT_COLOR = 0xff404040;
	public static int DIR_COLOR = Color.BLACK;
	public static int FILE_COLOR = Color.BLACK;
	public static int DIVIDER_COLOR = 0xff707070;//-16777216

	public static final String PREVIOUS_SELECTED_FILES = "net.gnu.explorer.selectedFiles";
	
	/**
	 * Select multi files and folders
	*/
	public static final String EXTRA_MULTI_SELECT = "org.openintents.extra.MULTI_SELECT";//"multiFiles";
	
    /**
     * Activity Action: Pick a file through the file manager, or let user
     * specify a custom file name.
     * Data is the current file name or file name suggestion.
     * Returns a new file name as file URI in data.
     * <p>
     * <p>Constant Value: "org.openintents.action.PICK_FILE"</p>
     */
    public static final String ACTION_PICK_FILE = "org.openintents.action.PICK_FILE";

    /**
     * Activity Action: Pick a directory through the file manager, or let user
     * specify a custom file name.
     * Data is the current directory name or directory name suggestion.
     * Returns a new directory name as file URI in data.
     * <p>
     * <p>Constant Value: "org.openintents.action.PICK_DIRECTORY"</p>
     */
    public static final String ACTION_PICK_DIRECTORY = "org.openintents.action.PICK_DIRECTORY";

    /**
     * Activity Action: Move, copy or delete after select entries.
     * Data is the current directory name or directory name suggestion.
     * <p>
     * <p>Constant Value: "org.openintents.action.MULTI_SELECT"</p>
     */
    public static final String ACTION_MULTI_SELECT = "org.openintents.action.MULTI_SELECT";

    public static final String ACTION_SEARCH_STARTED = "org.openintents.action.SEARCH_STARTED";

    public static final String ACTION_SEARCH_FINISHED = "org.openintens.action.SEARCH_FINISHED";

    /**
     * The title to display.
     * <p>
     * <p>This is shown in the title bar of the file manager.</p>
     * <p>
     * <p>Constant Value: "org.openintents.extra.TITLE"</p>
     */
    public static final String EXTRA_TITLE = "org.openintents.extra.TITLE";

    /**
     * The text on the button to display.
     * <p>
     * <p>Depending on the use, it makes sense to set this to "Open" or "Save".</p>
     * <p>
     * <p>Constant Value: "org.openintents.extra.BUTTON_TEXT"</p>
     */
    public static final String EXTRA_BUTTON_TEXT = "org.openintents.extra.BUTTON_TEXT";

    /**
     * Flag indicating to show only writeable files and folders.
     * <p>
     * <p>Constant Value: "org.openintents.extra.WRITEABLE_ONLY"</p>
     */
    public static final String EXTRA_WRITEABLE_ONLY = "org.openintents.extra.WRITEABLE_ONLY";

    /**
     * The path to prioritize in search. Usually denotes the path the user was on when the search was initiated.
     * <p>
     * <p>Constant Value: "org.openintents.extra.SEARCH_INIT_PATH"</p>
     */
    public static final String EXTRA_SEARCH_INIT_PATH = "org.openintents.extra.SEARCH_INIT_PATH";

    /**
     * The search query as sent to SearchService.
     * <p>
     * <p>Constant Value: "org.openintents.extra.SEARCH_QUERY"</p>
     */
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
	String[] mOperationsSystem = { 
//	"Split & Merge", "Compress", "Decompress",
//		"Text to Speech", "Replace All", "Generate Word List",
//		"Generate Index Title", "Compare Document...",
//		"Batch Delete & Rename", "ProxyFragment", "Pdf to Image",
		};
	SlidingTabsFragment slideFrag = null;
	public ContentFragment curContentFrag;
	private int curContentFragIndex = 1;
	ContentFragment curSelectionFrag;
	private int curSelectionFragIndex = -1;
	
	SlidingTabsFragment slideFrag2 = null;
	ContentFragment curSelectionFrag2;
	private int curSelectionFrag2Index = 2;
	public ContentFragment curExploreFrag;
	private int curExploreFragIndex = 1;
	
	public int operation = -1;
    public ArrayList<BaseFile> oparrayList;
    public ArrayList<ArrayList<BaseFile>> oparrayListList;
	
    // oppathe - the path at which certain operation needs to be performed
    // oppathe1 - the new path which user wants to create/modify
    // oppathList - the paths at which certain operation needs to be performed (pairs with oparrayList)
    public String oppathe, oppathe1;
	public ArrayList<String> oppatheList;
	
    public static final int INTENT_WRITE_REQUEST_CODE = 1;
//	Button deletePastesBtn;
//	Button deletePastesBtn2;
	//ArrayList<LayoutElements> copyl = new ArrayList<>();
	//ArrayList<LayoutElements> cutl = new ArrayList<>();
	OpenMode mode;
	String zippath = "";
	private Intent intent;
	//private String path;
	
	public boolean swap;
//	ViewGroup leftCommands;
//	ViewGroup rightCommands;
//	ViewGroup leftScroll;
//	ViewGroup rightScroll;
	public ViewGroup left;
	public ViewGroup right;
	//static int SPAN_COUNT = 3;
    static final int NUM_BACK = 32;
	int mCurTheme = 0;
	private View horizontalDivider5;
//	View horizontalDivider6;
//	View horizontalDivider11;
	boolean slideFrag1Selected = true;
	boolean configurationChanged = false;
	private Handler scheduleHandler;
	
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
	//View mLayout;
	static int density;// = (int)(4 * getResources().getDisplayMetrics().density);
	LinkedList<String> historyList = new LinkedList<>();
	int leftSize = 0; //0 =; -1 <; 1 >
	
	public static Shell.Interactive shellInteractive;
	public static Handler handler;
	private static HandlerThread handlerThread;
	private AsyncTask<Void, Void, Boolean> cloudSyncTask;
	
	public static final String KEY_PREF_OTG = "uri_usb_otg";
	public static final String KEY_INTENT_PROCESS_VIEWER = "openprocesses";
	public static final String TAG_INTENT_FILTER_FAILED_OPS = "failedOps";
    public static final String TAG_INTENT_FILTER_GENERAL = "general_communications";
    public static final String ARGS_KEY_LOADER = "loader_cloud_args_service";
	
	static final int FILES_REQUEST_CODE = 13;
	static final int SAVETO_REQUEST_CODE = 14;
	
	private static final int REQUEST_CODE_CLOUD_LIST_KEYS = 5463;
    private static final int REQUEST_CODE_CLOUD_LIST_KEY = 5472;
    private static final int REQUEST_CODE_CLOUD_LIST_KEY_CLOUD = 5434;
	private static final String CLOUD_AUTHENTICATOR_GDRIVE = "android.intent.category.BROWSABLE";
    private static final String CLOUD_AUTHENTICATOR_REDIRECT_URI = "net.gnu.explorer:/oauth2redirect";
	private static final int REQUEST_CODE_SAF = 223;
    private static final String VALUE_PREF_OTG_NULL = "n/a";
	private static final int image_selector_request_code = 31;
	public ArrayList<BaseFile> COPY_PATH = null, MOVE_PATH = null;
	public boolean isEncryptOpen = false;       // do we have to open a file when service is begin destroyed
    public BaseFile encryptBaseFile;            // the cached base file which we're to open, delete it later
	public boolean mReturnIntent = false;
    public boolean useGridView, openzip = false;
    public boolean mRingtonePickerIntent = false, colourednavigation = false;
	private String pendingPath;
	public static final Pattern DIR_SEPARATOR = Pattern.compile("/");
	public MainActivityHelper mainActivityHelper;
	public int storage_count = 0; // number of storage available (internal/external/otg etc)
	public DrawerAdapter adapter;
	private HistoryManager history, grid;
	private static final int SELECT_MINUS_2 = -2, NO_VALUE = -1, SELECT_0 = 0, SELECT_102 = 102;
	private int selectedStorage;
	public boolean isDrawerLocked = false;
	//public ScrimInsetsRelativeLayout mDrawerLinear;
	private FragmentTransaction pending_fragmentTransaction;
	
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate " + savedInstanceState);
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		getWindow().requestFeature(Window.FEATURE_ACTION_MODE_OVERLAY);
		mainActivityHelper = new MainActivityHelper(this);
		final ActionBar actionBar = getActionBar();
		intent = getIntent();

		setContentView(R.layout.activity_folder_chooser);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			//setContentView(R.layout.activity_folder_chooser_vertical);
			Log.d(TAG, "ORIENTATION_PORTRAIT");
		} else {
			//setContentView(R.layout.activity_folder_chooser);
			Log.d(TAG, "ORIENTATION_LANDSCAPE");
		}
		
		//mLayout = findViewById(R.id.container);//getWindow().getDecorView().findViewById(android.R.id.content);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
				|| ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_MEDIA_STORAGE) != PackageManager.PERMISSION_GRANTED
				) {
				Log.d(TAG, "ActivityCompat.checkSelfPermission permission has not been granted.");
				requestWriteStoragePermission();
			}
		}
		
		horizontalDivider5 = findViewById(R.id.horizontalDivider5);

		left = (ViewGroup) findViewById(R.id.left);
		right = (ViewGroup) findViewById(R.id.right);
		
		if (Intent.ACTION_MAIN.equals(intent.getAction())) {
			if (actionBar != null) {
				actionBar.hide();
			}
		} else {
			if (actionBar != null) {
				actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
				actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
				actionBar.setDisplayShowTitleEnabled(false);
				actionBar.setDisplayHomeAsUpEnabled(true);
				actionBar.setHomeButtonEnabled(true);
				setTitle("Power File Explorer");
				actionBar.show();
			}
		}
		final FragmentManager supportFragmentManager = getSupportFragmentManager();
		
		if (savedInstanceState == null) {
			density = (int)(getResources().getDisplayMetrics().density);

			final String action = intent.getAction();
			if (Intent.ACTION_MAIN.equals(action)) {
				findViewById(R.id.buttons).setVisibility(View.GONE);
			}
			
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
			
			if (ACTION_MULTI_SELECT.equals(action)) {
				multiFiles = true;
				if (suffix.length() == 0) {
					suffix = "*";
				}
			} else if (ACTION_PICK_DIRECTORY.equals(action)) {
				multiFiles = intent.getBooleanExtra(EXTRA_MULTI_SELECT, true);
				if (suffix.length() > 0) {
					suffix = "";
				}
			} else if (ACTION_PICK_FILE.equals(action)) {
				multiFiles = intent.getBooleanExtra(EXTRA_MULTI_SELECT, true);
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
				multiFiles = true;
			}
			previousSelectedStr = intent.getStringArrayExtra(PREVIOUS_SELECTED_FILES);
			//path = intent.getStringExtra(ExplorerActivity.EXTRA_DIR_PATH);
			updateColor();
			slideFrag = new SlidingTabsFragment();
			if ((intent.getStringExtra(EXTRA_ABSOLUTE_PATH) != null ||
				!"*".equals(suffix) || mimes.length() != 0 || previousSelectedStr != null)) {
				Log.d(TAG, "slideFrag.addTab(intent.getStringExtra(EXTRA_ABSOLUTE_PATH), suffix, mimes, multiFiles)");
				slideFrag.addTab(intent.getStringExtra(EXTRA_ABSOLUTE_PATH), suffix, mimes, multiFiles);
			} else {
				Log.d(TAG, "slideFrag.initContentFragmentTabs()");
				slideFrag.initContentFragmentTabs();
			}
			slideFrag.side = SlidingTabsFragment.Side.LEFT;
			if ((dir = intent.getStringExtra(EXTRA_ABSOLUTE_PATH)) != null) {
				final File file = new File(intent.getStringExtra(dir));//path);
				if (file.exists()) {
					if (file.isDirectory()) {
						final Bundle bundle = new Bundle();
						bundle.putString(EXTRA_ABSOLUTE_PATH, dir);//path);EXTRA_DIR_PATH
						bundle.putString(EXTRA_FILTER_FILETYPE, suffix);
						bundle.putBoolean(EXTRA_MULTI_SELECT, multiFiles);
						bundle.putString(EXTRA_FILTER_MIMETYPE, intent.getStringExtra(EXTRA_FILTER_MIMETYPE));
						bundle.putBoolean(EXTRA_WRITEABLE_ONLY, intent.getBooleanExtra(EXTRA_WRITEABLE_ONLY, false));
						bundle.putBoolean(EXTRA_DIRECTORIES_ONLY, intent.getBooleanExtra(EXTRA_DIRECTORIES_ONLY, false));
						bundle.putStringArray(PREVIOUS_SELECTED_FILES, previousSelectedStr);

						final ContentFragment fragment = new ContentFragment();
						fragment.setArguments(bundle);
						fragment.slidingTabsFragment = slideFrag;
						slideFrag.addPagerItem(fragment);//path, suffix, multiFiles, null));
						//slideFrag.addNewTab(path, suffix, multiFiles, false);
					} else {
						getFutils().openFile(file, this);
					}
				}
			} else {
				File defaultFile = new File(AndroidUtils.getDefaultPickFilePath(this));
				if (!defaultFile.exists()) {
					defaultFile = Environment.getExternalStorageDirectory();
					AndroidUtils.setDefaultPickFilePath(this, defaultFile.getAbsolutePath());
				}
				dir = defaultFile.getAbsolutePath();
			}
			
		} else {
			suffix = savedInstanceState.getString(EXTRA_FILTER_FILETYPE, "");
			mimes = savedInstanceState.getString(EXTRA_FILTER_MIMETYPE, "");
			multiFiles = savedInstanceState.getBoolean(EXTRA_MULTI_SELECT, true);
			dir = savedInstanceState.getString(EXTRA_ABSOLUTE_PATH);//EXTRA_DIR_PATH);
			previousSelectedStr = savedInstanceState.getStringArray(PREVIOUS_SELECTED_FILES);
			slideFrag1Selected = savedInstanceState.getBoolean("slideFrag1Selected");
			slideFrag = (SlidingTabsFragment) supportFragmentManager.findFragmentByTag("slideFrag");
			curContentFragIndex = savedInstanceState.getInt("curContentFragIndex");
			curSelectionFragIndex = savedInstanceState.getInt("curSelectionFragIndex");
			MOVE_PATH = savedInstanceState.getParcelableArrayList("cutl");
			COPY_PATH = savedInstanceState.getParcelableArrayList("copyl");
		}
		Log.d(TAG, "previousSelectedStr " + Util.arrayToString(previousSelectedStr, true, "\n"));
		
		leftSize = AndroidUtils.getSharedPreference(this, "biggerequalpanel", leftSize);
		Log.d(TAG, "action " + intent.getAction() + ", suffix " + suffix + ", mime " + mimes + ", multiFiles " + multiFiles + ", leftSize=" + leftSize);
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)left.getLayoutParams();
		if (leftSize > 0) {
			params.weight = 1.0f;
			left.setLayoutParams(params);
			params = (LinearLayout.LayoutParams)right.getLayoutParams();
			params.weight = 2.0f;
			right.setLayoutParams(params);
		} else if (leftSize < 0) {
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
		
		horizontalDivider5.setBackgroundColor(DIVIDER_COLOR);
		//mDrawerLinear = (ScrimInsetsRelativeLayout) findViewById(R.id.left_drawer);
		final FragmentTransaction transaction = supportFragmentManager.beginTransaction();
		swap = AndroidUtils.getSharedPreference(this, "swap", false);
		transaction.replace(R.id.content_fragment, slideFrag, "slideFrag");
		if (multiFiles) {
			if (savedInstanceState == null) {
				slideFrag2 = new SlidingTabsFragment();
				if (multiFiles && (intent.getStringExtra(EXTRA_ABSOLUTE_PATH) != null ||
					!"*".equals(suffix) || mimes.length() != 0 || previousSelectedStr != null)) {
					Log.d(TAG, "slideFrag2.addTab(previousSelectedStr)");
					slideFrag2.addTab(previousSelectedStr);
				} else {
					Log.d(TAG, "slideFrag2.addTab(\"/storage\", suffix, mimes, multiFiles)");
					slideFrag2.addTab("/storage", suffix, mimes, multiFiles);
					slideFrag2.addTab(null);
				}
				slideFrag2.side = SlidingTabsFragment.Side.RIGHT;
			} else {
				slideFrag2 = (SlidingTabsFragment) supportFragmentManager.findFragmentByTag("slideFrag2");
				curExploreFragIndex = savedInstanceState.getInt("curExploreFragIndex");
				curSelectionFrag2Index = savedInstanceState.getInt("curSelectionFrag2Index");
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
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
														R.layout.drawer_item, R.id.content, mOperationsSystem));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
												  mDrawerLayout, /* DrawerLayout object */
												  R.drawable.ic_drawer, /* nav drawer icon to replace 'Up' caret */
												  R.string.drawer_open, /* "open drawer" description */
												  R.string.app_name /* "close drawer" description */
												  ) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				final ActionBar actionBar = getActionBar();
				if (actionBar != null) {
					actionBar.setTitle(R.string.app_name);
				}
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				final ActionBar actionBar = getActionBar();
				if (actionBar != null) {
					actionBar.setTitle("Open Utilities...");
				}
			}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);

//		deletePastesBtn = (Button) findViewById(R.id.deletes_pastes);
//		deletePastesBtn2 = (Button) findViewById(R.id.deletes_pastes2);

		cleanFiles();
		
		if (rootMode) {
            handlerThread = new HandlerThread("handler");
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
            shellInteractive = (new Shell.Builder()).useSU().setHandler(handler).open();

            // check for busybox
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
		// setDirectoryButtons();
//		if (getIntent().getAction().equals(Intent.ACTION_GET_CONTENT)) {
//			// file picker intent
//			mReturnIntent = true;
//			Toast.makeText(this, getResources().getString(R.string.pick_a_file), Toast.LENGTH_LONG).show();
//		} else if (getIntent().getAction().equals(RingtoneManager.ACTION_RINGTONE_PICKER)) {
//			// ringtone picker intent
//			mReturnIntent = true;
//			mRingtonePickerIntent = true;
//			Toast.makeText(this, getResources().getString(R.string.pick_a_file), Toast.LENGTH_LONG).show();
//		} else if (getIntent().getAction().equals(Intent.ACTION_VIEW)) {
//			// zip viewer intent
//			Uri uri = intent.getData();
//			openzip = true;
//			zippath = uri.toString();
//		}
	}

	public ExplorerActivity() {
		super();
		prevTheme = - 1;
	}
	
	private static int prevTheme = - 1;
	private void updateColor() {
		int theme=sharedPref.getInt("theme", 2);
		Log.d(TAG, "updateColor " + theme + ", " + prevTheme + ", hourOfDay " + PreferenceUtils.hourOfDay());
		theme = theme == 2 ? PreferenceUtils.hourOfDay() : theme;
		Log.d(TAG, "updateColor " + theme + ", " + prevTheme + ", configurationChanged " + configurationChanged + slideFrag);
		if (prevTheme != theme) {

			if (Build.VERSION.SDK_INT >= 21) {
				if (theme == 1) {
					mCurTheme = android.R.style.Theme_Material_Wallpaper;//AndroidUtils.getSharedPreference(this, "theme", android.R.style.Theme_Material_Wallpaper);
					//getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.holo_dark_background));
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
					//getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.holo));
					SELECTED_IN_LIST = 0xFFFEF8BA;//0xFFFFF0A0
					BASE_BACKGROUND = 0xFFFFFFE8;
					DIVIDER_COLOR = Color.LTGRAY;
					IN_DATA_SOURCE_2 = 0xFFFFF8D9;
					IS_PARTIAL = 0xFFFFF0CF;
					TEXT_COLOR = 0xff404040;
					DIR_COLOR = 0xff404040;
					FILE_COLOR = 0xff404040;
				}
				Log.d(TAG, "setupColor BASE_BACKGROUND=" + BASE_BACKGROUND);
			} else {
				if (theme == 1) {
					mCurTheme = android.R.style.Theme_Wallpaper;//AndroidUtils.getSharedPreference(this, "theme", android.R.style.Theme_Wallpaper);
					//getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.holo_dark_background));
					TEXT_COLOR = 0xfff0f0f0;
					BASE_BACKGROUND = 0xff303030;
					DIVIDER_COLOR = Color.DKGRAY;
					DIR_COLOR = Color.WHITE;
					FILE_COLOR = Color.WHITE;
					SELECTED_IN_LIST = 0xffb0b0b0;
					IN_DATA_SOURCE_2 = 0xff505050;
					IS_PARTIAL = 0xff707070;
				} else {
					mCurTheme = android.R.style.Theme_Holo_Light;//AndroidUtils.getSharedPreference(this, "theme", android.R.style.Theme_Holo_Light);
					//getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.holo));
					SELECTED_IN_LIST = 0xFFFEF8BA;//0xFFFFF0A0
					BASE_BACKGROUND = 0xFFFFFFE8;
					DIVIDER_COLOR = Color.LTGRAY;
					IN_DATA_SOURCE_2 = 0xFFFFF8D9;
					IS_PARTIAL = 0xFFFFF0CF;
					TEXT_COLOR = 0xff404040;
					DIR_COLOR = 0xff404040;
					FILE_COLOR = 0xff404040;
				}
			}
			setTheme(mCurTheme);
			getWindow().getDecorView().setBackgroundColor(BASE_BACKGROUND);

//			int no = leftCommands.getChildCount();
//			Button b;
//			for (int i = 0; i < no; i++) {
//				b = (Button) leftCommands.getChildAt(i);
//				b.setTextColor(TEXT_COLOR);
//				b.getCompoundDrawables()[1].setAlpha(0xff);
//				b.getCompoundDrawables()[1].setColorFilter(TEXT_COLOR, PorterDuff.Mode.SRC_IN);
//			}
//			no = rightCommands.getChildCount();
//			for (int i = 0; i < no; i++) {
//				b = (Button) rightCommands.getChildAt(i);
//				b.setTextColor(TEXT_COLOR);
//				b.getCompoundDrawables()[1].setAlpha(0xff);
//				b.getCompoundDrawables()[1].setColorFilter(TEXT_COLOR, PorterDuff.Mode.SRC_IN);
//			}
			
			if (configurationChanged) {
				configurationChanged = false;
			} else if (slideFrag != null) {
				prevTheme = theme;
				slideFrag.getView().setBackgroundColor(BASE_BACKGROUND);
				if (curContentFrag != null) {
					slideFrag.updateLayout(true);
				}
				slideFrag.notifyTitleChange();
				slideFrag2.getView().setBackgroundColor(BASE_BACKGROUND);
				slideFrag2.notifyTitleChange();
				if (curExploreFrag != null && curExploreFrag.getContext() != null) {
					curExploreFrag.refreshRecyclerViewLayoutManager();
					curExploreFrag.setDirectoryButtons();
					curSelectionFrag2.refreshRecyclerViewLayoutManager();
					//slideFrag2.getCurrentFragment().select(slideFrag1Selected);
					curContentFrag.select(slideFrag1Selected);
					slideFrag2.updateLayout(true);
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
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
	
	private void requestWriteStoragePermission() {
        Log.d(TAG, "WRITE_EXTERNAL permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(camera_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
			|| ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_MEDIA_STORAGE)
			) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.d(TAG, "Displaying REQUEST_WRITE_EXTERNAL permission rationale to provide additional context.");
            Snackbar.make(left, "WRITE_EXTERNAL_STORAGE permission is needed to use the app.", Snackbar.LENGTH_INDEFINITE)
				.setAction("OK", new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						ActivityCompat.requestPermissions(ExplorerActivity.this,
														  PERMISSIONS_STORAGE,
														  REQUEST_WRITE_EXTERNAL);
					}
				})
				.show();
        } else {
            Log.d(TAG, "WRITE_EXTERNAL permission has not been granted yet. Request it directly.");
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_WRITE_EXTERNAL);
        }
    }
	
	private void cleanFiles() {
		new Thread(new Runnable() {
				@Override
				public void run() {
					File cacheDir = Glide.getPhotoCacheDir(ExplorerActivity.this);
					Log.d(TAG, "cacheDir " + cacheDir.getAbsolutePath());
					new File(
						"/sdcard/AppProjects/0SearchExplore/PowerExplorer/PowerExplorer/app/build/bin/resources.ap_")
						.delete();
					new File(
						"/sdcard/AppProjects/0SearchExplore/PowerExplorer/PowerExplorer/app/build/bin/classes.dex")
						.delete();
					new File(
						"/sdcard/AppProjects/0SearchExplore/PowerExplorer/PowerExplorer/app/build/bin/app.apk")
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
        final String path = intent.getStringExtra(EXTRA_ABSOLUTE_PATH);//EXTRA_DIR_PATH);
        Log.d(TAG, "onNewIntent " + path);
		if (path != null) {
			final File file = new File(path);
			if (file.isDirectory()) {
				dir = path;
				curContentFrag.suffix = intent.getStringExtra(EXTRA_FILTER_FILETYPE);
				curContentFrag.suffix = (curContentFrag.suffix == null) ? "*" : curContentFrag.suffix;
				curContentFrag.multiFiles = intent.getBooleanExtra(EXTRA_MULTI_SELECT, true);
				//curContentFrag.mFilterFiletype = intent.getStringExtra(EXTRA_FILTER_FILETYPE);
				curContentFrag.mimes = intent.getStringExtra(EXTRA_FILTER_MIMETYPE);
				curContentFrag.mWriteableOnly = intent.getBooleanExtra(EXTRA_WRITEABLE_ONLY, false);
				//curContentFrag.mDirectoriesOnly = intent.getBooleanExtra(EXTRA_DIRECTORIES_ONLY, false);
				curContentFrag.previousSelectedStr = intent.getStringArrayExtra(PREVIOUS_SELECTED_FILES);
				
				curContentFrag.changeDir(path, true);
//				slideFrag.addNewTab(path, i.getStringExtra(ExplorerActivity.EXTRA_SUFFIX), i.getBooleanExtra(ExplorerActivity.EXTRA_MULTI_SELECT, true), true);
//				slideFrag.setCurrentItem(slideFrag.getCount() - 1);
                //Fragment f = getDFragment();
//                if ((f.getClass().getName().contains("TabFragment"))) {
//                    Main m = ((Main) getFragment().getTab());
//                    m.loadlist(path, false, OpenMode.FILE);
//                } else goToMain(path);
            } else {
				getFutils().openFile(file, this);
			}
			//path = null;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Activity.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }
        };
        searchView.setOnQueryTextListener(textChangeListener);
        return super.onCreateOptionsMenu(menu);
    }

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void onActivityResultLollipop(final int requestCode,
										  final int resultCode, 
										  @NonNull final Intent data) {

		if (requestCode == INTENT_WRITE_REQUEST_CODE + 65536
			|| requestCode == INTENT_WRITE_REQUEST_CODE) {

			if (resultCode == Activity.RESULT_OK) {
				// Get Uri from Storage Access Framework.
				Uri treeUri = data.getData();
				// Persist URI in shared preference so that you can use it
				// later.
				// Use your own framework here instead of PreferenceUtil.

				AndroidPathUtils.setSharedPreferenceUri(
					"key_internal_uri_extsdcard", treeUri, this);
				// Persist access permissions.
				final int takeFlags = data.getFlags()
					& (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				Log.d(TAG, "treeUri:" + treeUri);
				Log.d(TAG, "takeFlags" + String.valueOf(takeFlags));
				Log.d(TAG, "data.getFlags()" + String.valueOf(data.getFlags()));
				this.getContentResolver().takePersistableUriPermission(treeUri,
																	   takeFlags);
			}
		}
	}
	
	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
//        if (requestCode == RC_SIGN_IN && !mGoogleApiKey && mGoogleApiClient != null) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    mIntentInProgress = false;
//                    mGoogleApiKey = true;
//                    // !mGoogleApiClient.isConnecting
//                    if (mGoogleApiClient.isConnecting()) {
//                        mGoogleApiClient.connect();
//                    } else
//                        mGoogleApiClient.disconnect();
//
//                }
//            }).run();
//        } else 
		if (requestCode == image_selector_request_code) {
            if (sharedPref != null && intent != null && intent.getData() != null) {
                if (SDK_INT >= 19)
                    getContentResolver().takePersistableUriPermission(intent.getData(),
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sharedPref.edit().putString("drawer_header_path", intent.getData().toString()).commit();
                //setDrawerHeaderBackground();
            }
        } else if (requestCode == 3) {
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
            final int takeFlags = intent.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
            switch (operation) {
                case DataUtils.DELETE://deletion
                    new DeleteTask(null, this).execute((oparrayList));
                    break;
                case DataUtils.COPY://copying
                    //legacy compatibility
                    if(oparrayList != null && oparrayList.size() != 0) {
                        oparrayListList = new ArrayList<>();
                        oparrayListList.add(oparrayList);
                        oparrayList = null;
                        oppatheList = new ArrayList<>();
                        oppatheList.add(oppathe);
                        oppathe = "";
                    }
                    for (int i = 0; i < oparrayListList.size(); i++) {
                        Intent intent1 = new Intent(this, CopyService.class);
                        intent1.putExtra(CopyService.TAG_COPY_SOURCES, oparrayList.get(i));
                        intent1.putExtra(CopyService.TAG_COPY_TARGET, oppatheList.get(i));
                        ServiceWatcherUtil.runService(this, intent1);
                    }
                    break;
                case DataUtils.MOVE://moving
                    //legacy compatibility
                    if(oparrayList != null && oparrayList.size() != 0) {
                        oparrayListList = new ArrayList<>();
                        oparrayListList.add(oparrayList);
                        oparrayList = null;
                        oppatheList = new ArrayList<>();
                        oppatheList.add(oppathe);
                        oppathe = "";
                    }

                    new MoveFiles(oparrayListList, slideFrag1Selected ? curContentFrag : curExploreFrag,
                            this, OpenMode.FILE)
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, oppatheList);
                    break;
                case DataUtils.NEW_FOLDER://mkdir
                    FileFrag ma1 = slideFrag1Selected ? curContentFrag : curExploreFrag;
                    mainActivityHelper.mkDir(RootHelper.generateBaseFile(new File(oppathe), true), ma1);
                    break;
                case DataUtils.RENAME:
                    FileFrag ma2 = slideFrag1Selected ? curContentFrag : curExploreFrag;
                    mainActivityHelper.rename(ma2.openMode, (oppathe), (oppathe1), this, ThemedActivity.rootMode);
                    ma2.updateList();
                    break;
                case DataUtils.NEW_FILE:
                    FileFrag ma3 = slideFrag1Selected ? curContentFrag : curExploreFrag;
                    mainActivityHelper.mkFile(new HFile(OpenMode.FILE, oppathe), ma3);

                    break;
                case DataUtils.EXTRACT:
                    mainActivityHelper.extractFile(new File(oppathe));
                    break;
                case DataUtils.COMPRESS:
                    //mainActivityHelper.compressFiles(new File(oppathe), oparrayList);
            }
            operation = -1;
        } else if (requestCode == REQUEST_CODE_SAF && responseCode == Activity.RESULT_OK) {
            // otg access
            sharedPref.edit().putString(KEY_PREF_OTG, intent.getData().toString()).apply();

//            if (!isDrawerLocked) mDrawerLayout.closeDrawer(mDrawerLinear);
//            else onDrawerClosed();
        } else if (requestCode == REQUEST_CODE_SAF && responseCode != Activity.RESULT_OK) {
            // otg access not provided
            pendingPath = null;
        }
    }

	
//	@Override
//	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
//		Log.d(TAG, "onActivityResult " + requestCode + ", " + responseCode + ", "
//			  + intent);
//		if (requestCode == INTENT_WRITE_REQUEST_CODE + 65536
//			|| requestCode == INTENT_WRITE_REQUEST_CODE) {
//			if (Build.VERSION.SDK_INT >= 21) { // Build.VERSION_CODES.LOLLIPOP)
//				// {
//				onActivityResultLollipop(requestCode, responseCode, intent);
//			}
//		}
//
////		if (requestCode == image_selector_request_code) {
////            if (Sp != null && intent != null && intent.getData() != null) {
////                if (Build.VERSION.SDK_INT >= 19)
////                    getContentResolver().takePersistableUriPermission(intent.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION);
////                Sp.edit().putString("drawer_header_path", intent.getData().toString()).commit();
////                setDrawerHeaderBackground();
////            }
////        } else 
//		if (requestCode == INTENT_WRITE_REQUEST_CODE + 65536
//			|| requestCode == INTENT_WRITE_REQUEST_CODE) {
//            String p = sharedPref.getString("key_internal_uri_extsdcard", null);
//
//            Uri oldUri = p != null ? Uri.parse(p): null;
//            Uri treeUri = null;
//            if (responseCode == Activity.RESULT_OK) {
//                // Get Uri from Storage Access Framework.
//                treeUri = intent.getData();
//                // Persist URI - this is required for verification of writability.
//                if (treeUri != null) 
//					sharedPref.edit().putString("key_internal_uri_extsdcard", treeUri.toString()).commit();
//            }
//            // If not confirmed SAF, or if still not writable, then revert settings.
//            if (responseCode != Activity.RESULT_OK) {
//				/* DialogUtil.displayError(getActivity(), R.string.message_dialog_cannot_write_to_folder_saf, false,
//				 currentFolder);||!FileUtil.isWritableNormalOrSaf(currentFolder)
//				 */
//                if (treeUri != null)
//					sharedPref.edit().putString("key_internal_uri_extsdcard", oldUri.toString()).commit();
//                return;
//            }
//
//            // After confirmation, update stored value of folder.
//            // Persist access permissions.
//            final int takeFlags = intent.getFlags()
//				& (Intent.FLAG_GRANT_READ_URI_PERMISSION
//				| Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
//			Log.d(TAG, takeFlags + ", " + operation + ", " + p);
//            switch (operation) {
//                case DataUtils.DELETE://deletion
//                    new DeleteTask(null, this).execute((oparrayList));
//                    break;
//                case DataUtils.COPY://copying
//                    Intent intent1 = new Intent(this, CopyService.class);
//                    intent1.putExtra("FILE_PATHS", (oparrayList));
//                    intent1.putExtra("COPY_DIRECTORY", oppathe);
//                    startService(intent1);
//                    break;
//                case DataUtils.MOVE://moving
//                    new MoveFiles((oparrayList), /*((Main) getFragment().getTab()), ((Main) getFragment().getTab()).getActivity()*/this, OpenMode.FILE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dir);//path
//                    break;
//                case DataUtils.NEW_FOLDER://mkdir
//                    //Main ma1 = ((Main) getFragment().getTab());
//                    new MainActivityHelper(this).mkDir(RootHelper.generateBaseFile(new File(oppathe), true), this);
//                    break;
//                case DataUtils.RENAME:
//                    new MainActivityHelper(this).rename(OpenMode.FILE, (oppathe), (oppathe1), this, BaseActivity.rootMode);
////                    Main ma2 = ((Main) getFragment().getTab());
////                    ma2.updateList();
//                    break;
//                case DataUtils.NEW_FILE:
//                    //Main ma3 = ((Main) getFragment().getTab());
//                    new MainActivityHelper(this).mkFile(new HFile(OpenMode.FILE, oppathe), this);
//
//                    break;
//                case DataUtils.EXTRACT:
//                    //new MainActivityHelper(this).extractFile(new File(oppathe));
//                    break;
//                case DataUtils.COMPRESS:
//                    //new MainActivityHelper(this).compressFiles(new File(oppathe), oparrayList);
//            }
//            operation = -1;
//        }
//	}

    /**
     * Call this method when you need to update the MainActivity view components' colors based on
     * update in the {@link MainActivity#currentTab}
     * Warning - All the variables should be initialised before calling this method!
     */
//    public void updateViews(ColorDrawable colorDrawable) {
//        // appbar view color
//        mainActivity.buttonBarFrame.setBackgroundColor(colorDrawable.getColor());
//        // action bar color
//        mainActivity.getSupportActionBar().setBackgroundDrawable(colorDrawable);
//        // drawer status bar I guess
//        mainActivity.mDrawerLayout.setStatusBarBackgroundColor(colorDrawable.getColor());
//        // drawer header background
//        mainActivity.drawerHeaderParent.setBackgroundColor(colorDrawable.getColor());
//
//        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            // for lollipop devices, the status bar color
//            mainActivity.getWindow().setStatusBarColor(colorDrawable.getColor());
//            if (colourednavigation)
//                mainActivity.getWindow().setNavigationBarColor(PreferenceUtils
//                        .getStatusColor(colorDrawable.getColor()));
//        } else if (SDK_INT == 20 || SDK_INT == 19) {
//
//            // for kitkat devices, the status bar color
//            SystemBarTintManager tintManager = new SystemBarTintManager(this);
//            tintManager.setStatusBarTintEnabled(true);
//            tintManager.setStatusBarTintColor(colorDrawable.getColor());
//        }
//    }

	public void renameBookmark(final String title, final String path) {

        if (DataUtils.containsBooks(new String[]{title, path}) != -1) {
            RenameBookmark renameBookmark = RenameBookmark.getInstance(title, path, Color.parseColor(ThemedActivity.accentSkin));
            if (renameBookmark != null)
                renameBookmark.show(getFragmentManager(), "renamedialog");
        }
    }

    void onDrawerClosed() {
        if (pending_fragmentTransaction != null) {
            pending_fragmentTransaction.commit();
            pending_fragmentTransaction = null;
        }

        if (pendingPath != null) {
            try {
                HFile hFile = new HFile(OpenMode.UNKNOWN, pendingPath);
                hFile.generateMode(this);
                if (hFile.isSimpleFile()) {
                    getFutils().openFile(new File(pendingPath), this);
                    pendingPath = null;
                    return;
                }
//                TabFragment m = getFragment();
//                if (m == null) {
//                    goToMain(pendingPath);
//                    return;
//                }
//                MainFragment mainFrag = ((MainFragment) m.getTab());
//                if (mainFrag != null) 
//					mainFrag.loadlist(pendingPath, false, OpenMode.UNKNOWN);
            } catch (ClassCastException e) {
//                selectedStorage = NO_VALUE;
//                goToMain("");
            }
            pendingPath = null;
        }
        //supportInvalidateOptionsMenu();
    }

	public void updateDrawer() {
        ArrayList<Item> sectionItems = new ArrayList<>();
        List<String> storageDirectories = getStorageDirectories();
        ArrayList<String[]> books = new ArrayList<>();
        ArrayList<String[]> servers = new ArrayList<>();

        storage_count = 0;
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
        DataUtils.setStorages(storageDirectories);
        sectionItems.add(new SectionItem());
        try {
            for (String[] file : grid.readTableSecondary(DataUtils.SMB))
                servers.add(file);
            DataUtils.setServers(servers);
            if (servers.size() > 0) {
                Collections.sort(servers, new BookSorter());
                for (String[] file : servers)
                    sectionItems.add(new EntryItem(file[0], file[1], ContextCompat.getDrawable(this,
                            R.drawable.ic_settings_remote_white_48dp)));
                sectionItems.add(new SectionItem());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String[] file : grid.readTableSecondary(DataUtils.BOOKS)) {
            books.add(file);
        }
        DataUtils.setBooks(books);
        if (books.size() > 0) {
            Collections.sort(books, new BookSorter());
            for (String[] file : books)
                sectionItems.add(new EntryItem(file[0], file[1], ContextCompat.getDrawable(this, R.drawable
                        .folder_fab)));
            sectionItems.add(new SectionItem());
        }

        sectionItems.add(new EntryItem(getResources().getString(R.string.quick), "5",
                ContextCompat.getDrawable(this, R.drawable.ic_star_white_18dp)));
        sectionItems.add(new EntryItem(getResources().getString(R.string.recent), "6",
                ContextCompat.getDrawable(this, R.drawable.ic_history_white_48dp)));
        sectionItems.add(new EntryItem(getResources().getString(R.string.images), "0",
                ContextCompat.getDrawable(this, R.drawable.ic_doc_image)));
        sectionItems.add(new EntryItem(getResources().getString(R.string.videos), "1",
                ContextCompat.getDrawable(this, R.drawable.ic_doc_video_am)));
        sectionItems.add(new EntryItem(getResources().getString(R.string.audio), "2",
                ContextCompat.getDrawable(this, R.drawable.ic_doc_audio_am)));
        sectionItems.add(new EntryItem(getResources().getString(R.string.documents), "3",
                ContextCompat.getDrawable(this, R.drawable.ic_doc_doc_am)));
        sectionItems.add(new EntryItem(getResources().getString(R.string.apks), "4",
                ContextCompat.getDrawable(this, R.drawable.ic_doc_apk_grid)));
        DataUtils.setList(sectionItems);
        adapter = new DrawerAdapter(this, this, sectionItems, this, sharedPref);
        mDrawerList.setAdapter(adapter);
    }

    public void updateDrawer(String path) {
        new AsyncTask<String, Void, Integer>() {
            @Override
            protected Integer doInBackground(String... strings) {
                String path = strings[0];
                int k = 0, i = 0;
                String entryItemPathOld = "";
                for (Item item : DataUtils.getList()) {
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
                    adapter.toggleChecked(integers);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, path);

    }

    public void refreshDrawer() {

        new AsyncTask<Void, Void, ArrayList<Item>>() {

            @Override
            protected ArrayList<Item> doInBackground(Void... params) {

                List<String> val = DataUtils.getStorages();
                if (val == null)
                    val = getStorageDirectories();
                final ArrayList<Item> items = new ArrayList<>();
                storage_count = 0;
                for (String file : val) {
                    File f = new File(file);
                    String name;
                    Drawable icon1 = ContextCompat.getDrawable(ExplorerActivity.this, R.drawable.ic_sd_storage_white_56dp);
                    if ("/storage/emulated/legacy".equals(file) || "/storage/emulated/0".equals(file)) {
                        name = getResources().getString(R.string.storage);
                    } else if ("/storage/sdcard1".equals(file)) {
                        name = getResources().getString(R.string.extstorage);
                    } else if ("/".equals(file)) {
                        name = getResources().getString(R.string.rootdirectory);
                        icon1 = ContextCompat.getDrawable(ExplorerActivity.this, R.drawable.ic_drawer_root_white);
                    } else if (file.contains(OTGUtil.PREFIX_OTG)) {
                        name = "OTG";
                        icon1 = ContextCompat.getDrawable(ExplorerActivity.this, R.drawable.ic_usb_white_48dp);
                    } else name = f.getName();
                    if (!f.isDirectory() || f.canExecute()) {
                        storage_count++;
                        items.add(new EntryItem(name, file, icon1));
                    }
                }
                items.add(new SectionItem());
                ArrayList<String[]> Servers = DataUtils.getServers();
                if (Servers != null && Servers.size() > 0) {
                    for (String[] file : Servers) {
                        items.add(new EntryItem(file[0], file[1], ContextCompat.getDrawable(ExplorerActivity.this, R.drawable.ic_settings_remote_white_48dp)));
                    }

                    items.add(new SectionItem());
                }

                ArrayList<String[]> accountAuthenticationList = new ArrayList<>();

                if (CloudSheetFragment.isCloudProviderAvailable(ExplorerActivity.this)) {

                    for (CloudStorage cloudStorage : DataUtils.getAccounts()) {

                        if (cloudStorage instanceof Dropbox) {

                            try {

                                items.add(new EntryItem(cloudStorage.getUserName(),
                                        CloudHandler.CLOUD_PREFIX_DROPBOX + "/",
														ContextCompat.getDrawable(ExplorerActivity.this, R.drawable.ic_dropbox_white_24dp)));

                                accountAuthenticationList.add(new String[] {
                                        cloudStorage.getUserName(),
                                        CloudHandler.CLOUD_PREFIX_DROPBOX + "/",
                                });
                            } catch (Exception e) {
                                e.printStackTrace();

                                items.add(new EntryItem(CloudHandler.CLOUD_NAME_DROPBOX,
                                        CloudHandler.CLOUD_PREFIX_DROPBOX + "/",
                                        ContextCompat.getDrawable(ExplorerActivity.this, R.drawable.ic_dropbox_white_24dp)));

                                accountAuthenticationList.add(new String[] {
                                        CloudHandler.CLOUD_NAME_DROPBOX,
                                        CloudHandler.CLOUD_PREFIX_DROPBOX + "/",
                                });
                            }
                        } else if (cloudStorage instanceof Box) {

                            try {

                                items.add(new EntryItem(cloudStorage.getUserName(),
                                        CloudHandler.CLOUD_PREFIX_BOX + "/",
														ContextCompat.getDrawable(ExplorerActivity.this, R.drawable.ic_box_white_24dp)));

                                accountAuthenticationList.add(new String[] {
                                        cloudStorage.getUserName(),
                                        CloudHandler.CLOUD_PREFIX_BOX + "/",
                                });
                            } catch (Exception e) {
                                e.printStackTrace();

                                items.add(new EntryItem(CloudHandler.CLOUD_NAME_BOX,
                                        CloudHandler.CLOUD_PREFIX_BOX + "/",
														ContextCompat.getDrawable(ExplorerActivity.this, R.drawable.ic_box_white_24dp)));

                                accountAuthenticationList.add(new String[] {
                                        CloudHandler.CLOUD_NAME_BOX,
                                        CloudHandler.CLOUD_PREFIX_BOX + "/",
                                });
                            }
                        } else if (cloudStorage instanceof OneDrive) {

                            try {
                                items.add(new EntryItem(cloudStorage.getUserName(),
                                        CloudHandler.CLOUD_PREFIX_ONE_DRIVE + "/",
														ContextCompat.getDrawable(ExplorerActivity.this, R.drawable.ic_onedrive_white_24dp)));

                                accountAuthenticationList.add(new String[] {
                                        cloudStorage.getUserName(),
                                        CloudHandler.CLOUD_PREFIX_ONE_DRIVE + "/",
                                });
                            } catch (Exception e) {
                                e.printStackTrace();

                                items.add(new EntryItem(CloudHandler.CLOUD_NAME_ONE_DRIVE,
                                        CloudHandler.CLOUD_PREFIX_ONE_DRIVE + "/",
														ContextCompat.getDrawable(ExplorerActivity.this, R.drawable.ic_onedrive_white_24dp)));

                                accountAuthenticationList.add(new String[] {
                                        CloudHandler.CLOUD_NAME_ONE_DRIVE,
                                        CloudHandler.CLOUD_PREFIX_ONE_DRIVE + "/",
                                });
                            }
                        } else if (cloudStorage instanceof GoogleDrive) {

                            try {
                                items.add(new EntryItem(cloudStorage.getUserName(),
                                        CloudHandler.CLOUD_PREFIX_GOOGLE_DRIVE + "/",
														ContextCompat.getDrawable(ExplorerActivity.this, R.drawable.ic_google_drive_white_24dp)));

                                accountAuthenticationList.add(new String[] {
                                        cloudStorage.getUserName(),
                                        CloudHandler.CLOUD_PREFIX_GOOGLE_DRIVE + "/",
                                });
                            } catch (Exception e) {
                                e.printStackTrace();

                                items.add(new EntryItem(CloudHandler.CLOUD_NAME_GOOGLE_DRIVE,
                                        CloudHandler.CLOUD_PREFIX_GOOGLE_DRIVE + "/",
														ContextCompat.getDrawable(ExplorerActivity.this, R.drawable.ic_google_drive_white_24dp)));

                                accountAuthenticationList.add(new String[] {
                                        CloudHandler.CLOUD_NAME_GOOGLE_DRIVE,
                                        CloudHandler.CLOUD_PREFIX_GOOGLE_DRIVE + "/",
                                });
                            }
                        }
                    }
                    Collections.sort(accountAuthenticationList, new BookSorter());

                    if (accountAuthenticationList.size() != 0)
                        items.add(new SectionItem());
                }

                ArrayList<String[]> books = DataUtils.getBooks();
                if (books != null && books.size() > 0) {
                    Collections.sort(books, new BookSorter());
                    for (String[] file : books) {
                        items.add(new EntryItem(file[0], file[1], ContextCompat.getDrawable(ExplorerActivity.this, R.drawable.folder_fab)));
                    }
                    items.add(new SectionItem());
                }
                items.add(new EntryItem(getResources().getString(R.string.quick), "5", ContextCompat.getDrawable(ExplorerActivity.this, R.drawable.ic_star_white_18dp)));
                items.add(new EntryItem(getResources().getString(R.string.recent), "6", ContextCompat.getDrawable(ExplorerActivity.this, R.drawable.ic_history_white_48dp)));
                items.add(new EntryItem(getResources().getString(R.string.images), "0", ContextCompat.getDrawable(ExplorerActivity.this, R.drawable.ic_doc_image)));
                items.add(new EntryItem(getResources().getString(R.string.videos), "1", ContextCompat.getDrawable(ExplorerActivity.this, R.drawable.ic_doc_video_am)));
                items.add(new EntryItem(getResources().getString(R.string.audio), "2", ContextCompat.getDrawable(ExplorerActivity.this, R.drawable.ic_doc_audio_am)));
                items.add(new EntryItem(getResources().getString(R.string.documents), "3", ContextCompat.getDrawable(ExplorerActivity.this, R.drawable.ic_doc_doc_am)));
                items.add(new EntryItem(getResources().getString(R.string.apks), "4", ContextCompat.getDrawable(ExplorerActivity.this, R.drawable.ic_doc_apk_grid)));
                DataUtils.setList(items);
                return items;
            }

            @Override
            protected void onPostExecute(ArrayList<Item> items) {
                super.onPostExecute(items);
                adapter = new DrawerAdapter(ExplorerActivity.this, ExplorerActivity.this, items, ExplorerActivity.this, sharedPref);
                mDrawerList.setAdapter(adapter);
            }
        }.execute();
    }

    @Override
    public void addConnection(OpenMode service) {
        CloudHandler cloudHandler = new CloudHandler(this);
        try {
            if (cloudHandler.findEntry(service) != null) {
                // cloud entry already exists
                Toast.makeText(this, getResources().getString(R.string.connection_exists),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ExplorerActivity.this, getResources().getString(R.string.please_wait), Toast.LENGTH_LONG).show();
                Bundle args = new Bundle();
                args.putInt(ARGS_KEY_LOADER, service.ordinal());
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
        CloudHandler cloudHandler = new CloudHandler(this);
        cloudHandler.clear(service);
        DataUtils.removeAccount(service);
        refreshDrawer();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri uri = Uri.withAppendedPath(Uri.parse("content://" + CloudContract.PROVIDER_AUTHORITY), "/keys.db/secret_keys/");

        String[] projection = new String[] {
                CloudContract.COLUMN_ID,
                CloudContract.COLUMN_CLIENT_ID,
                CloudContract.COLUMN_CLIENT_SECRET_KEY
        };

        switch (id) {
            case REQUEST_CODE_CLOUD_LIST_KEY:
                Uri uriAppendedPath = uri;
                switch (OpenMode.getOpenMode(args.getInt(ARGS_KEY_LOADER, 6))) {
                    case GDRIVE:
                        uriAppendedPath = ContentUris.withAppendedId(uri, 1);
                        break;
                    case DROPBOX:
                        uriAppendedPath = ContentUris.withAppendedId(uri, 2);
                        break;
                    case BOX:
                        uriAppendedPath = ContentUris.withAppendedId(uri, 3);
                        break;
                    case ONEDRIVE:
                        uriAppendedPath = ContentUris.withAppendedId(uri, 4);
                        break;
                }
                return new CursorLoader(this, uriAppendedPath, projection, null, null, null);
            case REQUEST_CODE_CLOUD_LIST_KEYS:
                // we need a list of all secret keys
                Uri uriAll = Uri.withAppendedPath(Uri.parse("content://" +
                        CloudContract.PROVIDER_AUTHORITY), "/keys.db/secret_keys");
                CloudHandler cloudHandler = new CloudHandler(getApplicationContext());
                try {
                    List<CloudEntry> cloudEntries = cloudHandler.getAllEntries();

                    String ids[] = new String[cloudEntries.size()];

                    for (int i=0; i<cloudEntries.size(); i++) {

                        // we need to get only those cloud details which user wants
                        switch (cloudEntries.get(i).getServiceType()) {
                            case GDRIVE:
                                ids[i] = 1 + "";
                                break;
                            case DROPBOX:
                                ids[i] = 2 + "";
                                break;
                            case BOX:
                                ids[i] = 3 + "";
                                break;
                            case ONEDRIVE:
                                ids[i] = 4 + "";
                                break;
                        }
                    }
                    return new CursorLoader(this, uriAll, projection, CloudContract.COLUMN_ID, ids, null);
                } catch (CloudPluginException e) {
                    e.printStackTrace();

                    Toast.makeText(this, getResources().getString(R.string.cloud_error_plugin),
                            Toast.LENGTH_LONG).show();
                }
            case REQUEST_CODE_CLOUD_LIST_KEY_CLOUD:
                Uri uriAppendedPathCloud = ContentUris.withAppendedId(uri, 5);
                return new CursorLoader(this, uriAppendedPathCloud, projection, null, null, null);
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

        if (cloudSyncTask != null && cloudSyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            cloudSyncTask.cancel(true);
        }

        cloudSyncTask = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {


                CloudHandler cloudHandler = new CloudHandler(ExplorerActivity.this);

                if (data.getCount() > 0 && data.moveToFirst()) {
                    do {

                        switch (data.getInt(0)) {
                            case 1:
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

                                    DataUtils.addAccount(cloudStorageDrive);
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
                                }
                                break;
                            case 2:
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

                                    DataUtils.addAccount(cloudStorageDropbox);
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
                                }
                                break;
                            case 3:
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

                                    DataUtils.addAccount(cloudStorageBox);
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
                                }
                                break;
                            case 4:
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

                                    DataUtils.addAccount(cloudStorageOnedrive);
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
                                }
                                break;
                            case 5:
                                CloudRail.setAppKey(data.getString(1));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        getSupportLoaderManager().initLoader(REQUEST_CODE_CLOUD_LIST_KEYS, null, ExplorerActivity.this);
                                    }
                                });
                                return false;
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
                if (refreshDrawer)
                    refreshDrawer();
            }
        }.execute();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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
    public synchronized List<String> getStorageDirectories() {
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
                if (!rv.contains(s) && getFutils().canListFiles(f))
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

	public void showSMBDialog(String name, String path, boolean edit) {
        if (path.length() > 0 && name.length() == 0) {
            int i = DataUtils.containsServer(new String[]{name, path});
            if (i != -1)
                name = DataUtils.servers.get(i)[0];
        }
        SmbConnectDialog smbConnectDialog = new SmbConnectDialog();
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString(EXTRA_ABSOLUTE_PATH, path);//EXTRA_DIR_PATH
        bundle.putBoolean("edit", edit);
        smbConnectDialog.setArguments(bundle);
        smbConnectDialog.show(getFragmentManager(), "smbdailog");
    }

    
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView parent, View view, int position,
								long id) {
			selectItem(position);
		}
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
	/** Swaps fragments in the main content view */
    public void selectItem(final int i) {
        ArrayList<Item> directoryItems = DataUtils.getList();
        if (!directoryItems.get(i).isSection()) {
            if ((selectedStorage == NO_VALUE || selectedStorage >= directoryItems.size())) {
				curContentFrag.changeDir(((EntryItem) directoryItems.get(i)).getPath(), false);
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
                selectedStorage = i;
                adapter.toggleChecked(selectedStorage);
//                if (!isDrawerLocked) 
//					mDrawerLayout.closeDrawer(mDrawerLinear);
//                else 
//					onDrawerClosed();
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
//                    if (!isDrawerLocked) 
//						mDrawerLayout.closeDrawer(mDrawerLinear);
//                    else 
//						onDrawerClosed();
                }
            }
        }
    }

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
		configurationChanged = true;
		Log.d(TAG, "configurationChanged " + configurationChanged);
	}

	protected void onPostCreate(android.os.Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		Log.d(TAG, "onPostCreate " + savedInstanceState);

		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected " + item);
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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
		outState.putParcelableArrayList("cutl", MOVE_PATH);
		outState.putParcelableArrayList("copyl", COPY_PATH);
		outState.putBoolean("slideFrag1Selected", slideFrag1Selected);
		outState.putInt("curContentFragIndex", (curContentFragIndex=slideFrag.size() == 1 ? 0 : slideFrag.indexOfMTabs(curContentFrag)+1));
		if (slideFrag2 != null) {
			outState.putInt("curExploreFragIndex", (curExploreFragIndex=slideFrag2.size() == 1 ? 0 : slideFrag2.indexOfMTabs(curExploreFrag)+1));
		}
		outState.putInt("curSelectionFragIndex", (curSelectionFragIndex=curSelectionFrag != null ? slideFrag.indexOfMTabs(curSelectionFrag) + 1: -1));
		outState.putInt("curSelectionFrag2Index", (curSelectionFrag2Index = curSelectionFrag2 != null ? slideFrag2.indexOfMTabs(curSelectionFrag2) + 1: -1));
		//curSelectionFragIndex = curSelectionFrag != null ? slideFrag.indexOf(curSelectionFrag) + 1: -1;
		//curSelectionFrag2Index = curSelectionFrag2 != null ? slideFrag2.indexOf(curSelectionFrag2) + 1: -1;
		
		
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.d(TAG, "onRestoreInstanceState " + savedInstanceState);
//		super.onRestoreInstanceState(savedInstanceState);
////		dir = savedInstanceState.getString(ExplorerActivity.EXTRA_DIR_PATH);
////		suffix = savedInstanceState.getString(ExplorerActivity.EXTRA_SUFFIX);
////		multiFiles = savedInstanceState.getBoolean(ExplorerActivity.EXTRA_MULTI_SELECT);
////		slideFrag1Selected = savedInstanceState.getBoolean("slideFrag1Selected");
//		//previousSelectedStr = savedInstanceState.getStringArray(ExplorerActivity.PREVIOUS_SELECTED_FILES);
////		cutl.addAll(Util.collectionString2FileArrayList(savedInstanceState.getStringArrayList("cutl")));
////		copyl.addAll(Util.collectionString2FileArrayList(savedInstanceState.getStringArrayList("copyl")));
	}

	private void showToast(CharSequence st) {
		Toast.makeText(this, st, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (Build.VERSION.SDK_INT > 23 && scheduleHandler != null) {
			scheduleHandler.removeCallbacks(run);
		}
		if (rootMode) {
            // close interactive shell and handler thread associated with it
            if (SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                // let it finish up first with what it's doing
                handlerThread.quitSafely();
            } else handlerThread.quit();
            shellInteractive.close();
        }
	}
	@Override
	public void onPause() {
		Log.d(TAG, "onPause");
		Glide.get(this).clearMemory();
        super.onPause();
		if (Build.VERSION.SDK_INT <= 23 && scheduleHandler != null) {
			scheduleHandler.removeCallbacks(run);
		}
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
		if (Build.VERSION.SDK_INT > 23) {
			schedule();
		}
	}
	
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
			curExploreFrag = (ContentFragment) slideFrag2.getFragmentIndex(curExploreFragIndex);
			if (curSelectionFrag2Index >= 0) {
				curSelectionFrag2 = (ContentFragment) slideFrag2.getFragmentIndex(curSelectionFrag2Index);
			}
			//curContentFrag2.deletePastes = deletePastesBtn2;
			Log.d(TAG, "onResume curContentFrag2 " + curSelectionFrag2);
		}
		
		if (Build.VERSION.SDK_INT <= 23) {
			schedule();
		}
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
	
	private Runnable run;
	private void schedule() {
		if (scheduleHandler == null) {
			scheduleHandler = new Handler();
			run = new Schedule(new int[] {6, 19}, new int[] {0, 0}, new int[] {0, 0});
			scheduleHandler.postDelayed(run, 500);
		} else {
			scheduleHandler.postDelayed(run, 500);
		}
	}

	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		super.onBackPressed();
		return true;
	}

	private static final int TIME_INTERVAL = 250;
	private long mBackPressed;
	@Override
	public void onBackPressed() {
		if (mBackPressed + TIME_INTERVAL >= System.currentTimeMillis()) {
			super.onBackPressed();
		} else {
			boolean remain = false;
			if (slideFrag1Selected) {
				remain = curContentFrag.back();
			} else if (slideFrag2.getCurrentFragment() == curExploreFrag) {
				remain = curExploreFrag.back();
			}

			if (!remain) {
				mBackPressed = System.currentTimeMillis();
				Toast.makeText(getBaseContext(), "Press one more time to exit",
							   Toast.LENGTH_SHORT).show(); 
			}
		}
		if (curContentFrag != null && curContentFrag.searchMode && curContentFrag.searchET.isFocused()) {
			curContentFrag.searchButton();
		} else if (curExploreFrag != null && curExploreFrag.searchMode && curExploreFrag.searchET.isFocused()) {
			curExploreFrag.searchButton();
		} else if (curSelectionFrag2 != null && curSelectionFrag2.searchMode && curSelectionFrag2.searchET.isFocused()) {
			curSelectionFrag2.searchButton();
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

	void swap(View v) {
		swap = !swap;
		
		AndroidUtils.setSharedPreference(this, "swap", swap);
		final int leftVisible = left.getVisibility();
		final int rightVisible = right.getVisibility();
		final ViewGroup parent = (ViewGroup)left.getParent();

//		left.setAnimation(AnimationUtils.loadAnimation(this, R.anim.shrink_from_top));
//		right.setAnimation(AnimationUtils.loadAnimation(this, R.anim.shrink_from_top));
		parent.removeView(left);
		parent.removeView(right);
//		left.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_top));
//		right.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_top));
		if (swap) {
			parent.addView(right, 0);
			parent.addView(left, 2);
		} else {
			parent.addView(left, 0);
			parent.addView(right, 2);
		}
		left.setVisibility(rightVisible);
		right.setVisibility(leftVisible);

	}

//	public void copys(View view) {
//		curContentFrag.copys(view);
//		if (curExploreFrag.commands.getVisibility() == View.GONE) {
//			curExploreFrag.commands.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_bottom));
//			curExploreFrag.commands.setVisibility(View.VISIBLE);
//			curExploreFrag.horizontalDivider.setVisibility(View.VISIBLE);
//			curExploreFrag.updateDelPaste();
//		}
//	}

//	public void cuts(View view) {
//		curContentFrag.cuts(view);
//		if (curExploreFrag.commands.getVisibility() == View.GONE) {
//			curExploreFrag.commands.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_bottom));
//			curExploreFrag.commands.setVisibility(View.VISIBLE);
//			curExploreFrag.horizontalDivider.setVisibility(View.VISIBLE);
//			curExploreFrag.updateDelPaste();
//		}
//	}

//	public void deletesPastes(View view) {
//		Log.d(TAG, "deletesPastes selectedInList1.size() " + curContentFrag.selectedInList1.size());
//        if (curContentFrag.selectedInList1.size() > 0) {
//			//curContentFrag.deletes(view);
//			//ArrayList<Integer> positions = new ArrayList<>();
//			//positions.add(pos);
//			new Futils().deleteFiles(curContentFrag.selectedInList1, curContentFrag, /*positions, */curContentFrag.activity.getAppTheme());
//		} else {
//			//curContentFrag.pastes(view);
//			String path = curContentFrag.CURRENT_PATH;
//			ArrayList<BaseFile> arrayList = COPY_PATH != null? COPY_PATH:MOVE_PATH;
//			boolean move = MOVE_PATH != null;
//			new CopyFileCheck(curContentFrag, path, move, this, BaseActivity.rootMode)
//				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, arrayList);
//			//COPY_PATH = null;
//			MOVE_PATH = null;
//		}
//	}

//	public void renames(View view) {
//		curContentFrag.renames(view);
//	}
//
//	public void compresss(View view) {
//		curContentFrag.compresss(view);
//	}
//
//	public void shares(View view) {
//		if (curContentFrag.selectedInList1.size() > 0) {
//			curContentFrag.share();
//		} 
//	}

//	public void sends(View view) {
//		curContentFrag.sends(view);
//	}

//	public void hides(View view) {
//		
//	}

//	public void addScreens(View view) {
//		curContentFrag.addScreens(view);
//	}
//
//	public void favourites(View view) {
//		
//	}
//
//	public void encrypts(View view) {
//		
//	}
//
//	public void infos(View view) {
//		curContentFrag.infos(view);
//	}

	public void addFolder(View view) {
		final EditText editText = new EditText(this);
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

		AlertDialog dialog = new AlertDialog.Builder(this)
			.setIconAttribute(android.R.attr.dialogIcon)
			.setTitle("New Folder")
			.setView(editText)
			.setPositiveButton(R.string.ok,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
									int whichButton) {
					String name = editText.getText().toString();

					File f = new File(dir, name);
					if (f.exists()) {
						showToast("\""
								  + f
								  + "\" is existing. Please choose another name");
					} else {
						boolean ok = f.mkdirs();
						if (ok) {
							showToast(f
									  + " was created successfully");
						} else {
							showToast(f + " can't be created");
						}
						dialog.dismiss();
					}

				}
			})
			.setNegativeButton(R.string.cancel,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
									int whichButton) {
					dialog.dismiss();
				}
			}).create();
		dialog.show();

	}

//	public void copys2(View view) {
//		slideFrag2.getCurrentFragment().copys(view);
//		if (curContentFrag.commands.getVisibility() == View.GONE) {
//			curContentFrag.commands.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_bottom));
//			curContentFrag.commands.setVisibility(View.VISIBLE);
//			curContentFrag.horizontalDivider.setVisibility(View.VISIBLE);
//			curContentFrag.updateDelPaste();
//		}
//	}

//	public void cuts2(View view) {
//		slideFrag2.getCurrentFragment().cuts(view);
//		if (curContentFrag.commands.getVisibility() == View.GONE) {
//			curContentFrag.commands.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_bottom));
//			curContentFrag.commands.setVisibility(View.VISIBLE);
//			curContentFrag.horizontalDivider.setVisibility(View.VISIBLE);
//			curContentFrag.updateDelPaste();
//		}
//	}

//	public void deletesPastes2(View view) {
//		//slideFrag2.getCurrentFragment().deletes(view);
//		final FileFrag currentFragment2 = (FileFrag) slideFrag2.getCurrentFragment();
//		Log.d(TAG, "deletesPastes2 selectedInList1.size() " + currentFragment2.selectedInList1.size());
//        if (currentFragment2.selectedInList1.size() > 0) {
//			new Futils().deleteFiles(currentFragment2.selectedInList1, currentFragment2, /*positions, */currentFragment2.activity.getAppTheme());
//			//currentFragment2.deletes(view);
//		} else {
//			//curContentFrag.pastes(view);
//			String path = currentFragment2.CURRENT_PATH;
//			ArrayList<BaseFile> arrayList = COPY_PATH != null? COPY_PATH:MOVE_PATH;
//			boolean move = MOVE_PATH != null;
//			new CopyFileCheck(currentFragment2, path, move, this, BaseActivity.rootMode)
//				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, arrayList);
//			COPY_PATH = null;
//			MOVE_PATH = null;
//		}
//	}
//
//	public void renames2(View view) {
//		//slideFrag2.getCurrentFragment().renames(view);
//	}
//
//	public void compresss2(View view) {
//		slideFrag2.getCurrentFragment().compresss(view);
//	}
//
//	public void shares2(View view) {
//		//slideFrag2.getCurrentFragment().shares(view);
//		final FileFrag currentFragment2 = (FileFrag) slideFrag2.getCurrentFragment();
//		if (currentFragment2.selectedInList1.size() > 0) {
//			currentFragment2.share();
//		} 
//	}

//	public void sends2(View view) {
//		slideFrag2.getCurrentFragment().sends(view);
//	}

//	public void hides2(View view) {
//		
//	}
//
//	public void addScreens2(View view) {
//		slideFrag2.getCurrentFragment().addScreens(view);
//	}
//
//	public void favourites2(View view) {
//		
//	}
//
//	public void encrypts2(View view) {
//		
//	}
//
//	public void infos2(View view) {
//		
//	}

//	public void hideCommand(boolean hide) {
//		View commandsVg = findViewById(R.id.commands);
//		View rightCommandsVg = findViewById(R.id.rightCommands);
//		if (hide) {
//			commandsVg.setAnimation(AnimationUtils.loadAnimation(this, R.anim.disappear));
//			rightCommandsVg.setAnimation(AnimationUtils.loadAnimation(this, R.anim.appear));
//			commandsVg.setVisibility(View.GONE);
//		} else {
//			rightCommandsVg.setAnimation(AnimationUtils.loadAnimation(this, R.anim.appear));
//			commandsVg.setAnimation(AnimationUtils.loadAnimation(this, R.anim.appear));
//			commandsVg.setVisibility(View.VISIBLE);
//		}
//	}

	public void removeFiles(View view) {
		if (!swap) {
			removeFiles();
		} else {
			addFiles();
		}
	}

	private void removeFiles() {
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
			curContentFrag.selectionStatus1
				.setText(curContentFrag.selectedInList1.size() + "/"
						 + curContentFrag.dataSourceL1.size());
			curSelectionFrag2.selectionStatus1
				.setText(curSelectionFrag2.selectedInList1.size() + "/"
						 + curSelectionFrag2.dataSourceL1.size());
		}
	}

	public void removeAllFiles(View view) {
		if (!swap) {
			removeAllFiles();
		} else {
			addAllFiles();
		}
	}

	private void removeAllFiles() {
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
		curContentFrag.selectionStatus1
			.setText(curContentFrag.selectedInList1.size() + "/"
					 + curContentFrag.dataSourceL1.size());
		curSelectionFrag2.selectionStatus1
			.setText(curSelectionFrag2.selectedInList1.size() + "/"
					 + curSelectionFrag2.dataSourceL1.size());
	}

	public void addFiles(View view) {
		if (!swap) {
			addFiles();
		} else {
			removeFiles();
		}
	}

	private void addFiles() {
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
					curSelectionFrag2.gridLayoutManager = new GridLayoutManager(this, curSelectionFrag2.spanCount);
					curSelectionFrag2.listView.setLayoutManager(curSelectionFrag2.gridLayoutManager);
				}
				curSelectionFrag2.listView.removeItemDecoration(curSelectionFrag2.dividerItemDecoration);
				curSelectionFrag2.listView.invalidateItemDecorations();
				if (curSelectionFrag2.spanCount <= 2) {
					curSelectionFrag2.dividerItemDecoration = new GridDividerItemDecoration(this, true);
					curSelectionFrag2.listView.addItemDecoration(curSelectionFrag2.dividerItemDecoration);
				}
				curSelectionFrag2.searchButton.setEnabled(true);
				curSelectionFrag2.nofilelayout.setVisibility(View.GONE);
				curSelectionFrag2.mSwipeRefreshLayout.setVisibility(View.VISIBLE);
			}
			curContentFrag.notifyDataSetChanged();// srcAdapter.notifyDataSetChanged();
			curSelectionFrag2.notifyDataSetChanged();// destAdapter.notifyDataSetChanged();
			curContentFrag.selectionStatus1
				.setText(curContentFrag.selectedInList1.size() + "/"
						 + curContentFrag.dataSourceL1.size());
			curSelectionFrag2.selectionStatus1
				.setText(curSelectionFrag2.selectedInList1.size() + "/"
						 + curSelectionFrag2.dataSourceL1.size());
		}
	}

	public void addAllFiles(View view) {
		if (!swap) {
			addAllFiles();
		} else {
			removeAllFiles();
		}
	}

	private void addAllFiles() {
		curContentFrag.dataSourceL2 = curSelectionFrag2.dataSourceL1;
		final String dirSt = dir.endsWith("/") ? dir : dir + "/";
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
					curSelectionFrag2.gridLayoutManager = new GridLayoutManager(this, curSelectionFrag2.spanCount);
					curSelectionFrag2.listView.setLayoutManager(curSelectionFrag2.gridLayoutManager);
				}
				curSelectionFrag2.listView.removeItemDecoration(curSelectionFrag2.dividerItemDecoration);
				curSelectionFrag2.listView.invalidateItemDecorations();
				if (curSelectionFrag2.spanCount <= 2) {
					curSelectionFrag2.dividerItemDecoration = new GridDividerItemDecoration(this, true);
					curSelectionFrag2.listView.addItemDecoration(curSelectionFrag2.dividerItemDecoration);
				}
				curSelectionFrag2.searchButton.setEnabled(true);
				curSelectionFrag2.nofilelayout.setVisibility(View.GONE);
				curSelectionFrag2.mSwipeRefreshLayout.setVisibility(View.VISIBLE);
			}
			curSelectionFrag2.allCbx.setEnabled(true);
			curContentFrag.selectionStatus1
				.setText(curContentFrag.dataSourceL1.size() + "/"
						 + curContentFrag.dataSourceL1.size());
			curSelectionFrag2.selectionStatus1
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

	public void ok(View view) {
		// if (currentSelectedList.size() == 0 && multiFiles) {
		// currentSelectedList.add(new File(dir.getText().toString()));
		// } else
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
