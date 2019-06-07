package net.gnu.texteditor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.sblo.pandora.jota.Search.OnSearchFinishedListener;
import jp.sblo.pandora.jota.Search.Record;
import jp.sblo.pandora.jota.TextLoadTask.OnFileLoadListener;
import jp.sblo.pandora.jota.text.JotaDocumentWatcher;
import jp.sblo.pandora.jota.text.Layout;
import jp.sblo.pandora.jota.text.Selection;
import jp.sblo.pandora.jota.text.SpannableStringBuilder;
import jp.sblo.pandora.jota.text.TextUtils;
import jp.sblo.pandora.jota.text.EditText.ShortcutListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import net.gnu.explorer.R;
import jp.sblo.pandora.jota.*;
import android.support.v4.app.Fragment;
import net.gnu.explorer.Frag;
import net.gnu.explorer.SlidingTabsFragment;
import android.util.Log;
import android.view.LayoutInflater;
import net.gnu.explorer.ExplorerActivity;
import android.graphics.PorterDuff;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;

public class TextFrag extends Frag implements JotaDocumentWatcher, ShortcutListener,
OnFileLoadListener {
    private static final String TAG = "TextFrag";
    private static final int REQUESTCODE_OPEN = 0;
    private static final int REQUESTCODE_SAVEAS = 1;
    private static final int REQUESTCODE_MUSHROOM = 2;
    private static final int REQUESTCODE_SEARCHBYINTENT = 3;
    private static final int REQUESTCODE_APPCHOOSER = 4;
    private static final String DEF_CHARSET = "utf-8";
    private static final int DEF_LINEBREAK = LineBreak.LF;
    // SL4A
    private static final String EXTRA_SCRIPT_PATH = "com.googlecode.android_scripting.extra.SCRIPT_PATH";
    private static final String EXTRA_SCRIPT_CONTENT = "com.googlecode.android_scripting.extra.SCRIPT_CONTENT";
    private static final String ACTION_EDIT_SCRIPT = "com.googlecode.android_scripting.action.EDIT_SCRIPT";
    public jp.sblo.pandora.jota.text.EditText mEditor;
    private LinearLayout mLlSearch;
    private jp.sblo.pandora.jota.text.EditText mEdtSearchWord;
    private ImageButton mBtnForward;
    private ImageButton mBtnBackward;
    private Button mChkReplace;
    private ImageButton mBtnClose;
    private LinearLayout mLlReplace;
    private jp.sblo.pandora.jota.text.EditText mEdtReplaceWord;
    private Button mBtnReplace;
    private Button mBtnReplaceAll;
    private String mNewFilename;
    private TextLoadTask mTask;
    // private String mSearchWord;
    private int mLine;
    private Intent mReservedIntent;
    private int mReservedRequestCode;
    private Runnable mProcAfterSaveConfirm = null;
    InstanceState mInstanceState = new InstanceState();
    private ArrayList<Search.Record> mSearchResult;
    private boolean mSearchForward;
    private SettingsActivity.Settings mSettings;
    private SettingsActivity.BootSettings mBootSettings;
    private String mSharedString = null;;
    private String mReplaceWord;
    private boolean mChangeCancel = false;
    private boolean mSharedPreferenceChanged=false;
    protected boolean mBackkeyDown = false;
    private static  boolean mRebootingForConfigChange = false;
    protected ImageView mWallpaper;
    private View mTransparency;
    private Bitmap mWallpaperBmp;
    private LinearLayout mToolbar;
    public LinearLayout mToolbarBase;
    private Handler mHandler = new Handler();
    protected boolean mRotationControl=false;
    private Button mMenuButton;
    private String mTempCandidate=null;
    private CharSequence mSl4aContents=null;
	
	private LinearLayout mContent;
	//private Activity fragActivity;
	private Intent intent;
//	private String title;
	public SlidingTabsFragment slidingTabsFragment;
	public static int no = 0;
    class InstanceState {
        String filename;

        String charset;

        // String text;
        int linebreak;

        // int selstart;
        // int selend;
        boolean changed;
        String nameCandidate;

		@Override
		public String toString() {
			return filename + ", " + charset + ", " + changed + ", " + linebreak;
		}
		
    };

	public TextFrag() {
		super();
		type = Frag.TYPE.TEXT;
		if (!(fragActivity instanceof TextEditorActivity)) {
			title = "Text";
		}
	}
	
	public static TextFrag newInstance(Intent intent, String title, String path) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("intent", intent);
        bundle.putString("title", title);
        bundle.putString("path", path);
        
        TextFrag fragment = new TextFrag();
        fragment.setArguments(bundle);

        return fragment;
    }

    /** Called when the activity is first created. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
		//fragActivity = this.getActivity();
		setHasOptionsMenu(true);
		return inflater.inflate(R.layout.textviewer, container, false);
    }
	
	@Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
		Log.d(TAG, "onViewCreated savedInstanceState " + savedInstanceState);
        //fragActivity = this.getActivity();
		Bundle args = getArguments();
		Log.d(TAG, "onViewCreated args=" + args);
		if (args != null) {
			intent = args.getParcelable("intent");
			title = args.getString("title");
			currentPathTitle = args.getString("path");
        }
		if (savedInstanceState != null) {
			title = savedInstanceState.getString("title");
			currentPathTitle = savedInstanceState.getString("path");
		}
		
        applyBootSetting();
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.textviewer);

        mEditor = (jp.sblo.pandora.jota.text.EditText)view.findViewById(R.id.textedit);
        Log.d(TAG, "onViewCreated mEditor " + currentPathTitle + ", " + mEditor);

        mWallpaper = (ImageView)view.findViewById(R.id.wallpaper);

        PreferenceManager.getDefaultSharedPreferences(fragActivity)
                .registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);

        fragActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC);


        mEditor.setDocumentChangedListener(this);
        mEditor.setShortcutListener(this);
        mEditor.setChanged(false);
        mLlSearch = (LinearLayout)view.findViewById(R.id.search);
        mLlReplace = (LinearLayout)view.findViewById(R.id.replace);

        mEdtSearchWord = (jp.sblo.pandora.jota.text.EditText)view.findViewById(R.id.edtSearchWord);
        mEdtSearchWord.setShortcutListener(null);
        mBtnForward = (ImageButton)view.findViewById(R.id.btnForward);
        mBtnBackward = (ImageButton)view.findViewById(R.id.btnBackward);
        mChkReplace = (Button)view.findViewById(R.id.chkReplace);
        mBtnClose = (ImageButton)view.findViewById(R.id.btnClose);
        mEdtReplaceWord = (jp.sblo.pandora.jota.text.EditText)view.findViewById(R.id.edtReplaceWord);
        mEdtReplaceWord.setShortcutListener(null);
        mBtnReplace = (Button)view.findViewById(R.id.btnReplace);
        mBtnReplaceAll = (Button)view.findViewById(R.id.btnReplaceAll);
        mToolbar = (LinearLayout)view.findViewById(R.id.toolbar);
        mToolbarBase = (LinearLayout) view.findViewById(R.id.toolbarbase);
        mTransparency = view.findViewById(R.id.trasparencylayer);
        mMenuButton = (Button)view.findViewById(R.id.menubutton);
        applySetting();
		mContent = ((LinearLayout)view.findViewById(R.id.content));
		if (fragActivity instanceof TextEditorActivity) {
			mContent.removeView(mToolbarBase);
			final ActionBar actionBar = ((TextEditorActivity)fragActivity).getSupportActionBar();
			if (actionBar != null) {
				actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
				actionBar.setCustomView(mToolbarBase);
				//actionBar.setBackgroundDrawable(getResources().getDrawable((R.drawable.list_item_background_normal)));
//				if (!(mToolbarBase.getChildAt(0) instanceof ImageView)) {
//					final ImageView iv = new ImageView(fragActivity);
//					iv.setImageResource(R.drawable.textpng);
//					iv.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
//					mToolbarBase.addView(iv, 0);
//					
//					final Toolbar parent =(Toolbar) mToolbarBase.getParent();
//					parent.setContentInsetsAbsolute(0, 0);
//				}
				final Toolbar parent =(Toolbar) mToolbarBase.getParent();
				parent.setContentInsetsAbsolute(0, 0);
			}
			view.findViewById(R.id.horizontalDivider).setVisibility(View.GONE);
		} else {
			view.findViewById(R.id.horizontalDivider).setVisibility(View.VISIBLE);
		}
		
		//if ( savedInstanceState == null ){
        //} else {
        //    onRestoreInstanceState(savedInstanceState);
        //}
		
        if (mBootSettings.screenOrientation.equals(SettingsActivity.ORI_AUTO) || mRotationControl){
            // Do nothing
        } else if (mBootSettings.screenOrientation.equals(SettingsActivity.ORI_PORTRAIT)){
            fragActivity.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
            if ( getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT ){
                if ( savedInstanceState == null ){
                    mRebootingForConfigChange = true;
                    return;
                }
            }
        } else if (mBootSettings.screenOrientation.equals(SettingsActivity.ORI_LANDSCAPE)){
            fragActivity.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );
            if ( getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE ){
                if ( savedInstanceState == null ){
                    mRebootingForConfigChange = true;
                    return;
                }
            }
        }

        mEditor.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSearchResult = null;
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });

        mEdtSearchWord.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean enabled = (s.length() > 0);
                mBtnForward.setEnabled(enabled);
                mBtnBackward.setEnabled(enabled);
                mSearchResult = null;
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });

        mEdtSearchWord.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
                        && event.getAction() == KeyEvent.ACTION_UP) {
                    if (mBtnForward.isEnabled()) {
                        mBtnForward.performClick();
                        return true;
                    }
                }
                return false;
            }
        });
        mEdtSearchWord.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_FULLSCREEN
                | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        mEdtReplaceWord.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });

        mEdtReplaceWord.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // if ( (keyCode == KeyEvent.KEYCODE_ENTER || keyCode ==
                // KeyEvent.KEYCODE_DPAD_CENTER )
                // && event.getAction() == KeyEvent.ACTION_UP ) {
                // if ( mBtnForward.isEnabled() ){
                // mBtnForward.performClick();
                // return true;
                // }
                // }
                return false;
            }
        });
        mEdtReplaceWord.setImeOptions(EditorInfo.IME_ACTION_DONE
                | EditorInfo.IME_FLAG_NO_FULLSCREEN | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        mBtnForward.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String searchword = mEdtSearchWord.getText().toString();
                mSearchForward = true;
                doSearch(searchword);
            }
        });
        mBtnBackward.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String searchword = mEdtSearchWord.getText().toString();
                mSearchForward = false;
                doSearch(searchword);
            }
        });
        mChkReplace.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mLlReplace.setVisibility(View.VISIBLE);
                mChkReplace.setVisibility(View.GONE);
                mEdtReplaceWord.requestFocus();
                if (mSearchResult == null) {
                    String searchword = mEdtSearchWord.getText().toString();
                    doSearch(searchword);
                }
            }
        });
        mBtnClose.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mSearchResult = null;
                mLlSearch.setVisibility(View.GONE);
                mLlReplace.setVisibility(View.GONE);
                mChkReplace.setVisibility(View.VISIBLE);
                Selection.setDisableLostFocus(true);
            }
        });
        // edtReplaceWord
        // btnSkip
        // btnReplaceAll

        mBtnReplace.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String searchword = mEdtSearchWord.getText().toString();
                mReplaceWord = mEdtReplaceWord.getText().toString();
                if ( mSettings.re ){
                    mReplaceWord = mReplaceWord.replace("\\n", "\n");
                    mReplaceWord = mReplaceWord.replace("\\t", "\t");
					mReplaceWord = mReplaceWord.replace("\\r", "\r");
					mReplaceWord = mReplaceWord.replace("\\f", "\f");
					mReplaceWord = mReplaceWord.replace("\\\\", "\\");
                }
                doReplace(searchword);
            }
        });
        mBtnReplaceAll.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String searchword = mEdtSearchWord.getText().toString();
                mReplaceWord = mEdtReplaceWord.getText().toString();
                if ( mSettings.re ){
                    mReplaceWord = mReplaceWord.replace("\\n", "\n");
                    mReplaceWord = mReplaceWord.replace("\\t", "\t");
					mReplaceWord = mReplaceWord.replace("\\r", "\r");
					mReplaceWord = mReplaceWord.replace("\\f", "\f");
					mReplaceWord = mReplaceWord.replace("\\\\", "\\");
                }
                doReplaceAll(searchword);
            }
        });
        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragActivity.openOptionsMenu();
            }
        });

        mProcNew.run();

//		Log.d(TAG, "intent1 " + activity.getIntent() + intent);

		if (mRebootingForConfigChange || savedInstanceState == null) {
            mRebootingForConfigChange = false;
            Intent it = intent;//activity.getIntent();//
			//intent = null;
			if (it != null &&
                    (Intent.ACTION_VIEW.equals(it.getAction())
                  || Intent.ACTION_EDIT.equals(it.getAction()) )
            ) {
                mLine = -1;
                Uri data = it.getData();
                if ( data != null ){
                    String scheme = data.getScheme();
                    String path = null;
                    if (ContentResolver.SCHEME_FILE.equals(scheme)) {
                        path = Uri.decode(data.getEncodedPath());
                        String lineparam = data.getQueryParameter("line");
                        if (lineparam != null) {
                            try {
                                mLine = Integer.parseInt(lineparam);
                            } catch (Exception e) {
                            }
                        }
                    } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
					ContentResolver cr = fragActivity.getContentResolver();
                        Cursor cur = null;
                        try {
                            cur = cr.query(data, null, null, null, null);
                        } catch (Exception e) {
                        }
                        if (cur != null) {
                            cur.moveToFirst();
                            try {
                                path = cur.getString(cur.getColumnIndex("_data"));
                                if (path == null
                                        || !path.startsWith(Environment.getExternalStorageDirectory()
                                                .getPath())) {
                                    // from content provider
                                    path = data.toString();
                                }
                            } catch (Exception e) {
                                path = data.toString();
                            }
                        } else {
                            path = data.toString();
                        }
                    } else {
                    }

                    // mSearchWord = null;
                    // mLine = -1;
                    //
                    // Bundle extra = it.getExtras();
                    // if ( extra!=null ){
                    // mSearchWord = extra.getString("query");
                    // mLine = extra.getInt("line");
                    // }
                    if (path != null) {
					mTask = new TextLoadTask(fragActivity, this, mLine,mSettings.suppressMessage);
                        mTask.execute(path, mSettings.CharsetOpen);
                    }
                }
            } else if (it != null && Intent.ACTION_SEND.equals(it.getAction())) {
                Bundle extras = it.getExtras();
                CharSequence text = extras.getCharSequence(Intent.EXTRA_TEXT);
                if (text != null) {
                    mEditor.setText(text.toString());
					mInstanceState.changed = true;
                }

            } else if (it != null && ACTION_EDIT_SCRIPT.equals(it.getAction())) {
                Bundle extras = it.getExtras();
                CharSequence path = extras.getCharSequence(EXTRA_SCRIPT_PATH);

                CharSequence contents = extras.getCharSequence(EXTRA_SCRIPT_CONTENT);
                if (contents != null) {
                    mTempCandidate=path.toString();
                    mSl4aContents = contents;
                    mProcOpenSl4a.run();
//                    mEditor.setText(contents);
                } else {
                    if (path != null) {
                        mTask = new TextLoadTask(fragActivity, this, mLine,mSettings.suppressMessage);
                        mTask.execute(path.toString(), mSettings.CharsetOpen);
                    }
                }
            } else {
				Log.d(TAG, "startupAction " + mSettings.startupAction);
                if ( SettingsActivity.STARTUP_LASTFILE.equals( mSettings.startupAction) ) {
                    File[] fl = getHistory();
                    if (fl != null) {
                        mTask = new TextLoadTask(fragActivity, this, -1,mSettings.suppressMessage);
                        mTask.execute(fl[0].getPath(), mSettings.CharsetOpen);
                    }
                }else if ( SettingsActivity.STARTUP_HISTORY.equals( mSettings.startupAction) ){
                    mProcHistory.run();
                }else if ( SettingsActivity.STARTUP_OPEN.equals( mSettings.startupAction) ){
                    mProcOpen.run();
                }else{
                    // New File (Do Nothing)
					Log.d(TAG, "New File " + mInstanceState.toString());
                }
            }
        } else {
            mInstanceState.filename = savedInstanceState.getString("filename");
            mInstanceState.charset = savedInstanceState.getString("charset");
            // mInstanceState.text = savedInstanceState.getString("text" );
            mInstanceState.linebreak = savedInstanceState.getInt("linebreak");
            // mInstanceState.selstart = savedInstanceState.getInt("selstart" );
            // mInstanceState.selend = savedInstanceState.getInt("selend" );
            mInstanceState.nameCandidate = savedInstanceState.getString("nameCandidate");

            mInstanceState.changed = savedInstanceState.getBoolean("changed");
            // mEditor.setText(mInstanceState.text);
            // mEditor.setSelection(mInstanceState.selstart,
            // mInstanceState.selend);
            mEditor.setChanged(mInstanceState.changed);
			//Log.d(TAG, "onViewCreated isChanged " + mEditor.isChanged());
        }
        SettingsActivity.showWelcomeMessage(fragActivity);
        if (savedInstanceState==null){
            mHandler.postDelayed(mShowImeProc, 1000);
        }
		updateColor(null);
		Log.d(TAG, "path " + currentPathTitle);
		load(currentPathTitle);
    }

	public void load(String fPathFromFrag) {
		Log.d(TAG, "loadNew.fPathFromFrag " + fPathFromFrag);
		if (fPathFromFrag != null) {
			currentPathTitle = fPathFromFrag;
			File file = new File(fPathFromFrag);
			if (file.exists() && file.isFile()) {
				mProcNew.run();
				mTask = new TextLoadTask(fragActivity, this, mLine,mSettings.suppressMessage);
				mTask.execute(currentPathTitle, mSettings.CharsetOpen);
			}
		}
	}
    
//	public void loadNew(String fPathFromFrag) {
//		Log.d(TAG, "loadNew.fPathFromFrag " + fPathFromFrag);
//		if (fPathFromFrag != null) {
//			File file = new File(fPathFromFrag);
//			if (file.exists() && file.isFile()) {
//				mProcNew.run();
//				mTask = new TextLoadTask(activity, this, mLine,mSettings.suppressMessage);
//				mTask.execute(fPathFromFrag, mSettings.CharsetOpen);
//			}
//		}
//	}
	
//	public void onAttach(android.content.Context context) {
//		Log.d(TAG, "onAttach " + currentPathTitle + ", " + context);
//		super.onAttach(context);
//	}
	
//	public void onAttachFragment(Fragment fragment) {
//		Log.d(TAG, "onAttachFragment " + currentPathTitle + ", " + fragment);
//		super.onAttachFragment(fragment);
//	}
	
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy " + toString());
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(fragActivity)
                .unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        if ( mWallpaperBmp!=null ){
            mWallpaperBmp.recycle();
        }
    }

	@Override
	public void onStart() {
		Log.d(TAG, "onStart " + toString());
        super.onStart();
		fragActivity = getActivity();
	}
	
	@Override
	public void onStop() {
		Log.d(TAG, "onStop " + toString());
		mInstanceState.changed = mEditor.isChanged();
        super.onStop();
	}
	
    @Override
    public void onPreFileLoad() {
    }

	@Override
	public String toString() {
		return "title=" + title + ", filename=" + mInstanceState.filename + ", " + super.toString();
	}

    @Override
    public void onFileLoaded(SpannableStringBuilder result, String filename, String charset,
            int linebreak, int offset) {
        mTask = null;
        if (result != null) {
            mInstanceState.filename = filename;
            mInstanceState.charset = charset;
            mInstanceState.linebreak = linebreak;

            mSearchResult = null;

            SpannableStringBuilder ss = result;
            mEditor.setText(ss);
            mEditor.setChanged(false);

            SharedPreferences sp = getActivity().getSharedPreferences(SettingsActivity.PREF_HISTORY, Activity.MODE_PRIVATE);
            String sel = sp.getString(filename, "-1,-1");

            if (offset != -1) {
                try {
                    mEditor.setSelection(offset);
                    mEditor.centerCursor();
                } catch (Exception e) {
                    offset = -1;
                }
            }
            if (offset == -1) {
                int selStart = -1;
                int selEnd = -1;
                if (sel != null) {
                    String[] sels = sel.split(",");
                    if (sels.length >= 2) {
                        try {
                            selStart = Integer.parseInt(sels[0]);
                            selEnd = Integer.parseInt(sels[1]);

                            if (selStart >= 0 && selEnd >= 0) {
                                int len = mEditor.length();
                                if (selStart >= len) {
                                    selStart = len - 1;
                                }
                                if (selEnd >= len) {
                                    selEnd = len - 1;
                                }
                                mEditor.setSelection(selStart, selEnd);
                                mEditor.centerCursor();
                            }

                        } catch (Exception e) {
                            selStart = -1;
                            selEnd = -1;
                        }
                    }
                }
            }
            mLine = -1;
            saveHistory();
            if (mBootSettings.viewerMode) {
                mEditor.showIme(false);
                mHandler.removeCallbacks(mShowImeProc);
            }
            KeywordHighlght.loadHighlight(fragActivity,filename);
        	//onPrepareOptionsMenu(activity.menu);
		}
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Log.d(TAG, "onSaveInstanceState " + outState);

//        if ( mRebootingForConfigChange ){;
//            return;
//        }

        if (mEditor.isChanged() && mInstanceState.filename != null & mSettings.autosave) {
            save();
            mEditor.setChanged(false);
        }

        // Log.e(TAG,"onSaveInstanceState=========================================================>");
        // mInstanceState.text = mEditor.getText().toString();
        // mInstanceState.selstart = mEditor.getSelectionStart();
        // mInstanceState.selend = mEditor.getSelectionEnd();

        outState.putString("filename", mInstanceState.filename);
        outState.putString("charset", mInstanceState.charset);
        // outState.putString("text" , mInstanceState.text );
        outState.putInt("linebreak", mInstanceState.linebreak);
        // outState.putInt("selstart" , mInstanceState.selstart );
        // outState.putInt("selend" , mInstanceState.selend );
        outState.putBoolean("changed", mEditor.isChanged());
        outState.putString("nameCandidate", mInstanceState.nameCandidate);
		
		outState.putString("title", title);
		
        Log.d(TAG, "onSaveInstanceState " + currentPathTitle + ", " + outState);
        super.onSaveInstanceState(outState);
    }

    //@Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState " + currentPathTitle + ", " + savedInstanceState);
        //super.onRestoreInstanceState(savedInstanceState);

        mInstanceState.filename = savedInstanceState.getString("filename");
        mInstanceState.charset = savedInstanceState.getString("charset");
        // mInstanceState.text = savedInstanceState.getString("text" );
        mInstanceState.linebreak = savedInstanceState.getInt("linebreak");
        // mInstanceState.selstart = savedInstanceState.getInt("selstart" );
        // mInstanceState.selend = savedInstanceState.getInt("selend" );
        mInstanceState.changed = savedInstanceState.getBoolean("changed");
        mInstanceState.nameCandidate = savedInstanceState.getString("nameCandidate");
		
		title = savedInstanceState.getString("title");
		
        // mEditor.setText(mInstanceState.text);
        // mEditor.setSelection(mInstanceState.selstart, mInstanceState.selend);
        mEditor.setChanged(mInstanceState.changed);
		//Log.d(TAG, "onRestoreInstanceState " + mEditor.isChanged());
    }
	
	@Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        Log.d(TAG, "onViewStateRestored " + currentPathTitle + ", " + savedInstanceState);
		super.onViewStateRestored(savedInstanceState);
        if ( savedInstanceState !=  null ) {
        mInstanceState.filename = savedInstanceState.getString("filename");
        mInstanceState.charset = savedInstanceState.getString("charset");
        // mInstanceState.text = savedInstanceState.getString("text" );
        mInstanceState.linebreak = savedInstanceState.getInt("linebreak");
        // mInstanceState.selstart = savedInstanceState.getInt("selstart" );
        // mInstanceState.selend = savedInstanceState.getInt("selend" );
        mInstanceState.changed = savedInstanceState.getBoolean("changed");
        mInstanceState.nameCandidate = savedInstanceState.getString("nameCandidate");
		
		title = savedInstanceState.getString("title");
		
        // mEditor.setText(mInstanceState.text);
        // mEditor.setSelection(mInstanceState.selstart, mInstanceState.selend);
        }
        mEditor.setChanged(mInstanceState.changed);
		//Log.d(TAG, "onViewStateRestored " + mEditor.isChanged());
    }
	
    @Override
    public void onLowMemory() {
		Log.e(TAG, "onLowMemory() " + toString());
        super.onLowMemory();
	}

    //@Override
    protected void onNewIntent(Intent intent) {
        //super.onNewIntent(intent);

        if (intent != null &&
                ( Intent.ACTION_VIEW.equals(intent.getAction())
                ||Intent.ACTION_EDIT.equals(intent.getAction()) )
            ) {
            mLine = -1;
            Uri data = intent.getData();
            String scheme = data.getScheme();
            if (ContentResolver.SCHEME_FILE.equals(scheme)) {
                mNewFilename = Uri.decode(data.getEncodedPath());
                String lineparam = data.getQueryParameter("line");
                if (lineparam != null) {
                    try {
                        mLine = Integer.parseInt(lineparam);
                    } catch (Exception e) {
                    }
                }
            } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                ContentResolver cr = fragActivity.getContentResolver();
                Cursor cur = cr.query(data, null, null, null, null);
                if (cur != null) {
                    cur.moveToFirst();
                    try {
                        mNewFilename = cur.getString(cur.getColumnIndex("_data"));
                        if (mNewFilename == null
                                || !mNewFilename.startsWith(Environment
                                        .getExternalStorageDirectory().getPath())) {
                            // from content provider
                            mNewFilename = data.toString();
                        }
                    } catch (Exception e) {
                        mNewFilename = data.toString();
                    }
                } else {
                    mNewFilename = data.toString();
                }
            } else {
            }

            if (!mNewFilename.equals(mInstanceState.filename)) {
                confirmSave(mProcReopen);
            } else {
                if (mLine > 0) {
                    Editable text = mEditor.getText();
                    int offset = getOffsetOfLine(text, mLine);
                    if (offset < 0) {
                        offset = 0;
                    }
                    if (offset > text.length() - 1) {
                        offset = text.length() - 1;
                    }
                    mEditor.setSelection(offset);
                    mEditor.centerCursor();
                }
            }

        } else if (intent != null && Intent.ACTION_SEND.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            CharSequence inserttext = extras.getCharSequence(Intent.EXTRA_TEXT);
            if (inserttext != null) {
                if (mSettings.actionShare.equals(SettingsActivity.AS_INSERT)) {
                    Editable text = mEditor.getText();
                    int startsel = mEditor.getSelectionStart();
                    int endsel = mEditor.getSelectionEnd();
                    if (endsel < startsel) {
                        int temp = startsel;
                        startsel = endsel;
                        endsel = temp;
                    }
                    text.replace(startsel, endsel, inserttext);
                } else if (mSettings.actionShare.equals(SettingsActivity.AS_NEWFILE)) {
                    mSharedString = inserttext.toString();
                    confirmSave(mProcReceiveShare);
                }
            }
        } else if (intent != null && ACTION_EDIT_SCRIPT.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            mNewFilename = extras.getCharSequence(EXTRA_SCRIPT_PATH).toString();

            CharSequence contents = extras.getCharSequence(EXTRA_SCRIPT_CONTENT);
            if (contents != null) {
                mSl4aContents = contents;
                mTempCandidate = mNewFilename.toString();
                confirmSave(mProcOpenSl4a);
//                mEditor.setText(contents);
            } else {
                if (mNewFilename != null) {
                    confirmSave(mProcReopen);
                }
            }
        }
    }

    private int getOffsetOfLine(CharSequence text, int line) {
        int pos = 0;
        line = line - 1;
        Pattern pattern = Pattern.compile("\n");
        Matcher m = pattern.matcher(text);
        for (int i = 0; i < line; i++) {
            if (m.find()) {
                pos = m.start() + 1;
            } else {
                break;
            }
        }
        return pos;
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause " + toString());
        super.onPause();
		
        saveHistory();
    }

    private void saveHistory() {
        if (mInstanceState.filename != null) {
            int selstart = mEditor.getSelectionStart();
            int selend = mEditor.getSelectionEnd();

            SharedPreferences sp = fragActivity.getSharedPreferences(SettingsActivity.PREF_HISTORY, Activity.MODE_PRIVATE);
            Editor editor = sp.edit();
            editor.putString(mInstanceState.filename, String.format("%d,%d,%d", selstart, selend,
                    System.currentTimeMillis()));
            editor.commit();
        }
    }
//int co = 0;
    // @Override JotaDocumentWatcher#onChanged()
    public void onChanged() {
        if (mChangeCancel) {
            return;
        }

        boolean changed = mEditor.isChanged();
		//Log.e(TAG, "changed " + changed + ", " + title + ", " + slidingTabsFragment);
//		if (++co == 7) {
//			throw new RuntimeException("test");
//		}
        mSearchResult = null;
		String name;
        if (mInstanceState.filename != null) {
            name = new File(mInstanceState.filename).getName();
        } else {
			name = title.endsWith("*") ? title.substring(0, title.length() - 1) : title;
		}
        if (changed) {
            name += "*";
        }
		//Log.e(TAG, "changed " + changed + ", " + title + ", " + name + ", " + slidingTabsFragment);
        //activity.setTitle(name);//
		if (slidingTabsFragment != null) {
			//slidingTabsFragment.updateTitle(this, name);
			title = name;
			slidingTabsFragment.notifyTitleChange();
		}
		
    }

    //@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ( event.getRepeatCount() == 0){
                mBackkeyDown = true;
                return true;
            }
        }
        //return super.onKeyDown(keyCode, event);
		return false;
    }

    //@Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            mProcSearch.run();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK && mBackkeyDown) {
            mBackkeyDown = false;
            if (mLlSearch.getVisibility() == View.VISIBLE) {
                mBtnClose.performClick();
                return true;
            }
//            if (confirmSave(mProcQuit)) {
			if (fragActivity instanceof TextEditorActivity) {
				((TextEditorActivity)fragActivity).quit();
                return true;
            }
        }
        //return super.onKeyUp(keyCode, event);
		return false;
    }

    public boolean confirmSave(Runnable procAfterSaveConfirm) {
        mProcAfterSaveConfirm = null;
        if (mEditor.isChanged()) {
            mProcAfterSaveConfirm = procAfterSaveConfirm;

            if (mInstanceState.filename != null & mSettings.autosave) {
                save();
            } else {
                String msg;
                if (mInstanceState.filename == null) {
                    msg = getString(R.string.confirmation_message_null);//slidingTabsFragment.
                } else {
                    msg = getString(R.string.confirmation_message, mInstanceState.filename);//slidingTabsFragment.
                }

                new AlertDialog.Builder(fragActivity).setTitle(R.string.confirmation).setMessage(msg)//slidingTabsFragment.
                        .setPositiveButton(R.string.label_yes,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        save();
                                    }
                                }).setNeutralButton(R.string.label_no,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        if (mProcAfterSaveConfirm != null) {
											saved++;
                                            mProcAfterSaveConfirm.run();
                                        }
                                        mProcAfterSaveConfirm = null;
                                    }
                                }).setNegativeButton(R.string.label_cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        mProcAfterSaveConfirm = null;
                                    }
                                }).show();
            }

            return true;
        } else {
			if (procAfterSaveConfirm != null) {
				saved++;
				procAfterSaveConfirm.run();
			}
        }
        return false;
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        activity.getMenuInflater().inflate(R.menu.mainmenu, menu);
//        return true;
//    }
	
	@Override
	public void onCreateOptionsMenu(android.view.Menu menu, android.view.MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.mainmenu, menu);
	}
	
    private void save() {
        String charset = mSettings.CharsetSave;
        if (charset.length() == 0) {
            charset = mInstanceState.charset;
        }

        int linebreak = mSettings.LinebreakSave;
        if (linebreak == -1) {
            linebreak = mInstanceState.linebreak;
        }
        save(charset, linebreak);
    }

    private void save(String charset, int linebreak) {
        String filename = mInstanceState.filename;
        if (filename != null) {
            // charset= mInstanceState.charset;
            // linebreak = mInstanceState.linebreak;
            CharSequence text = mEditor.getText();
            String lb = "\n";
            if (linebreak == LineBreak.CR) {
                lb = "\r";
            } else if (linebreak == LineBreak.CRLF) {
                lb = "\r\n";
            }

            new TextSaveTask(fragActivity, null, new Runnable() {
                public void run() {
                    saveHistory();
					saved++;
                    if (mProcAfterSaveConfirm != null) {
                        mProcAfterSaveConfirm.run();
                        mProcAfterSaveConfirm = null;
                    }
                    mEditor.setChanged(false);
                    onChanged();
                }
            },mSettings.suppressMessage).execute(filename, charset, lb, text, mSettings.createbackup ? "true" : "false");
        } else {
            saveAs();
        }

    }

    private void saveAs() {
        Intent intent = new Intent(fragActivity, FileSelectorActivity.class);
        intent.putExtra(FileSelectorActivity.INTENT_MODE, FileSelectorActivity.MODE_SAVE);
        String filename = mInstanceState.filename;

        if (filename == null || filename.length() == 0) {
            if ( mInstanceState.nameCandidate != null ){
                filename = mInstanceState.nameCandidate;
            }
        }
        if (filename == null || filename.length() == 0) {
            // if file is unnamed. then use 1st line of text as filename.
            Editable text = mEditor.getText();
            int namelength = Math.min(text.length(), 20);

            CharSequence firstline = text.subSequence(0, namelength);
            int length = firstline.length();
            StringBuilder newline = new StringBuilder();
            newline.insert(0, mSettings.defaultdirectory);
            newline.append('/');
            boolean named = false;
            for (int i = 0; i < length; i++) {
                char c = firstline.charAt(i);
                if (c < ' ') {
                    break;
                }
                if (c != '\\' && c != '/' && c != ':' && c != '*' && c != '?' && c != '\"'
                        && c != '<' && c != '>'
						&& c != '|') {
                    newline.append(c);
                    named = true;
                }
            }
            if (!named) {
                newline.append(title);//"Untitled"
            }
            newline.append(".txt");
            filename = newline.toString();
        }
        if (mSettings.CharsetSave.length() > 0) {
            intent.putExtra(FileSelectorActivity.INTENT_CHARSET, mSettings.CharsetSave);
        }
        if (mSettings.LinebreakSave != -1) {
            intent.putExtra(FileSelectorActivity.INTENT_LINEBREAK, mSettings.LinebreakSave);
        }
        intent.putExtra(FileSelectorActivity.INTENT_INIT_PATH, filename);
        startActivityForResult(intent, REQUESTCODE_SAVEAS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUESTCODE_OPEN: {
                    Bundle extras = data.getExtras();
                    String path = extras.getString(FileSelectorActivity.INTENT_FILEPATH);
                    String charset = extras.getString(FileSelectorActivity.INTENT_CHARSET);
						mTask = new TextLoadTask(fragActivity, this, -1,mSettings.suppressMessage);
                    mTask.execute(path, charset);
                }
                    break;
                case REQUESTCODE_SAVEAS: {
                    Bundle extras = data.getExtras();
                    mInstanceState.filename = extras
                            .getString(FileSelectorActivity.INTENT_FILEPATH);

                    String charset = extras.getString(FileSelectorActivity.INTENT_CHARSET);
                    if (charset == null || charset.length() == 0) {
                        charset = mInstanceState.charset;
                    }
                    int linebreak = extras.getInt(FileSelectorActivity.INTENT_LINEBREAK, -1);
                    if (linebreak == -1) {
                        linebreak = mInstanceState.linebreak;
                    }
                    save(charset, linebreak);
                }
                    break;
                case REQUESTCODE_MUSHROOM: {
                    Bundle extras = data.getExtras();
                    String insertstr = extras.getString("replace_key");
                    if (insertstr != null) {
                        Editable text = mEditor.getText();
                        int startsel = mEditor.getSelectionStart();
                        int endsel = mEditor.getSelectionEnd();
                        if (endsel < startsel) {
                            int temp = startsel;
                            startsel = endsel;
                            endsel = temp;
                        }
                        text.replace(startsel, endsel, insertstr);
                    }
                }
                    break;
                case REQUESTCODE_APPCHOOSER: {
                    Intent intent = mReservedIntent;
                    int request = mReservedRequestCode;

                    ComponentName component = data.getComponent();
                    intent.setComponent(component);

                    try {
                        if (request == REQUESTCODE_MUSHROOM) {
                            startActivityForResult(intent, request);
                        } else {
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                    }
                }
                    break;

            }
        } else if (resultCode == Activity.RESULT_FIRST_USER) {
            switch (requestCode) {
                case REQUESTCODE_APPCHOOSER: {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri
                            .parse(getString(R.string.no_reciever_url)));
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                    }
                }
            }
        }
        mReservedIntent = null;
        mReservedRequestCode = 0;
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {//boolean
		Log.d(TAG, "onPrepareOptionsMenu.filename=" + mInstanceState.filename + ", " + menu );
        MenuItem menuitem = menu.findItem(R.id.menu_direct);
        if ( mSettings != null ){
            String name = mSettings.intentname;
            if (name != null && name.length() > 0) {
                menuitem.setTitle(name);
                menuitem.setEnabled(true);
            } else {
                menuitem.setTitle(R.string.menu_direct);
                menuitem.setEnabled(false);
            }
        }else{
            menuitem.setTitle(R.string.menu_direct);
            menuitem.setEnabled(false);
        }
        menuitem = menu.findItem(R.id.menu_insert);
        if ( mSettings != null ){
            String name = mSettings.intentname2;
            if (name != null && !SettingsActivity.DI_INSERT.equals(name)) {
                menuitem.setTitle(name);
                menuitem.setIcon(R.drawable.ic_menu_direct);
            } else {
                menuitem.setTitle(R.string.menu_insert);
                menuitem.setIcon(R.drawable.ic_menu_compose);
            }
        }else{
            menuitem.setTitle(R.string.menu_insert);
            menuitem.setIcon(R.drawable.ic_menu_compose);
        }

        menuitem = menu.findItem(R.id.menu_file_shortcut);
        menuitem.setEnabled(mInstanceState.filename != null);

//        menuitem = menu.findItem(R.id.menu_help_donate);
//        menuitem.setVisible( mSettings.donateCounter == 0 );

//        if ( JotaTextEditor.sHoneycomb ){
//	        menuitem = menu.findItem(R.id.menu_edit);
//			new IcsWrapper().setShowAsActionIfRoomWithText(menuitem);
//	        menuitem.setIcon(R.drawable.ic_menu_edit_ab);
//        }
//		if (fragActivity instanceof TextEditorActivity) {
//			final ActionBar actionBar = ((TextEditorActivity)fragActivity).getSupportActionBar();
//			actionBar.setCustomView(mToolbarBase);
//			actionBar.setBackgroundDrawable(getResources().getDrawable((R.drawable.list_item_background_normal)));
//			if (!(mToolbarBase.getChildAt(0) instanceof ImageView)) {
//				final ImageView iv = new ImageView(fragActivity);
//				iv.setImageResource(R.drawable.textpng);
//				iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
//				mToolbarBase.addView(iv, 0);
//				final ViewGroup.LayoutParams lp = mToolbarBase.getLayoutParams();
//				lp.width = LayoutParams.MATCH_PARENT;
//				mToolbarBase.setLayoutParams(lp);
//				final Toolbar parent =(Toolbar) mToolbarBase.getParent();
//				parent.setContentInsetsAbsolute(0, 0);
//			}
//		}
        super.onPrepareOptionsMenu(menu);//return 
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
	//public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_insert: {
                mProcInsert.run();
            }
                return true;
            case R.id.menu_search: {
                mProcSearch.run();
            }
            return true;
            case R.id.menu_preferences: {
					Intent intent = new Intent(fragActivity,SettingsActivity.class);
                intent.putExtra( SettingsActivity.EXTRA_CATEGORY, SettingsActivity.CAT_TOP);
                startActivity(intent);
            }
            return true;
            case R.id.menu_file_history:{
                confirmSave(mProcHistory);
            }
                return true;
            case R.id.menu_file_save: {
                save();
            }
                return true;
            case R.id.menu_file_open: {
                confirmSave(mProcOpen);
            }
                return true;
            case R.id.menu_file_saveas: {
                saveAs();
            }
                return true;
            case R.id.menu_file_new: {
                confirmSave(mProcNew);
            }
                return true;
            case R.id.menu_file_charcode: {
                mProcCharSet.run();
            }
                return true;
            case R.id.menu_file_lbcode: {
                mProcLineBreak.run();
            }
                return true;
            case R.id.menu_file_shortcut: {
                mProcCreateShortcut.run();
            }
                return true;
            case R.id.menu_file_property: {
                mProcProperty.run();
            }
                return true;
            case R.id.menu_file_quit: {
                //confirmSave(mProcQuit);//
				if (fragActivity instanceof TextEditorActivity) {
					((TextEditorActivity)fragActivity).quit();
				}
            }
                return true;
            case R.id.menu_edit_undo: {
                mEditor.doFunction(jp.sblo.pandora.jota.text.EditText.FUNCTION_UNDO);
            }
                return true;
            case R.id.menu_edit_redo: {
                mEditor.doFunction(jp.sblo.pandora.jota.text.EditText.FUNCTION_REDO);
            }
                return true;
            case R.id.menu_edit_cut: {
                mEditor.doFunction(jp.sblo.pandora.jota.text.EditText.FUNCTION_CUT);
            }
                return true;
            case R.id.menu_edit_copy: {
                mEditor.doFunction(jp.sblo.pandora.jota.text.EditText.FUNCTION_COPY);
            }
                return true;
            case R.id.menu_edit_paste: {
                mEditor.doFunction(jp.sblo.pandora.jota.text.EditText.FUNCTION_PASTE);
            }
                return true;
            case R.id.menu_edit_select_all: {
                mEditor.doFunction(jp.sblo.pandora.jota.text.EditText.FUNCTION_SELECT_ALL);
            }
                return true;
            case R.id.menu_edit_jump: {
                mProcJump.run();
            }
                return true;
            case R.id.menu_search_byintent: {
                mProcSearchByIntent.run();
            }
                return true;
            case R.id.menu_file_view: {
                confirmSave(mProcViewByIntent);
            }
                return true;
            case R.id.menu_file_share: {
                confirmSave(mProcShareFile);
            }
                return true;
            case R.id.menu_share: {
                mProcShare.run();
            }
                return true;

            case R.id.menu_share_screenshot: {
                mProcShareScreenshot.run();
            }
                return true;

            case R.id.menu_direct: {
                mProcDirect.run();
            }
                return true;
//            case R.id.menu_pref_search: {
//                Intent intent = new Intent(this, SettingsActivity.class);
//                intent.putExtra( SettingsActivity.EXTRA_CATEGORY, SettingsActivity.CAT_SEARCH);
//                startActivity(intent);
//            }
//                return true;
//            case R.id.menu_pref_view: {
//                Intent intent = new Intent(this, SettingsActivity.class);
//                intent.putExtra( SettingsActivity.EXTRA_CATEGORY, SettingsActivity.CAT_VIEW);
//                startActivity(intent);
//            }
//                return true;
//            case R.id.menu_pref_font: {
//                Intent intent = new Intent(this, SettingsActivity.class);
//                intent.putExtra( SettingsActivity.EXTRA_CATEGORY, SettingsActivity.CAT_FONT);
//                startActivity(intent);
//            }
//                return true;
//            case R.id.menu_pref_file: {
//                Intent intent = new Intent(this, SettingsActivity.class);
//                intent.putExtra( SettingsActivity.EXTRA_CATEGORY, SettingsActivity.CAT_FILE);
//                startActivity(intent);
//            }
//                return true;
//            case R.id.menu_pref_input: {
//                Intent intent = new Intent(this, SettingsActivity.class);
//                intent.putExtra( SettingsActivity.EXTRA_CATEGORY, SettingsActivity.CAT_INPUT);
//                startActivity(intent);
//            }
//                return true;
//            case R.id.menu_pref_misc: {
//                Intent intent = new Intent(this, SettingsActivity.class);
//                intent.putExtra( SettingsActivity.EXTRA_CATEGORY, SettingsActivity.CAT_MISC);
//                startActivity(intent);
//            }
//                return true;
//            case R.id.menu_help_donate: {
//                Intent intent = new Intent( Main.this,DonateActivity.class);
//                intent.putExtra( AboutActivity.EXTRA_URL ,  getString( R.string.url_donate ) );
//                intent.putExtra( AboutActivity.EXTRA_TITLE ,  getString( R.string.label_donate ) );
//                startActivity(intent);
//            }
//            return true;
//            case R.id.menu_help_about: {
//                Intent intent = new Intent( Main.this,AboutActivity.class);
//                startActivity(intent);
//            }
//                return true;

        }
        // // Intent intent = new Intent();
        // // intent.setClassName("jp.sblo.pandora.adice",
        // "jp.sblo.pandora.adice.SettingsActivity");
        // // startActivity(intent);
        // return true;
        // }
        // if (id == R.id.help) {
        // Intent intent = new Intent();
        // intent.setClassName("jp.sblo.pandora.adice",
        // "jp.sblo.pandora.adice.AboutActivity");
        // startActivity(intent);
        // return true;
        // }

        //return super.onMenuItemSelected(featureId, item);
		return super.onOptionsItemSelected(item);
		//return false;
    }

    // @Override
    public boolean onCommand(int function) {
        switch (function) {
            case jp.sblo.pandora.jota.text.TextView.FUNCTION_SAVE:
                save();
                return true;
            case jp.sblo.pandora.jota.text.TextView.FUNCTION_SAVEAS:
                saveAs();
                return true;
            case jp.sblo.pandora.jota.text.TextView.FUNCTION_DIRECTINTENT:
                mProcDirect.run();
                return true;
            case jp.sblo.pandora.jota.text.TextView.FUNCTION_OPEN:
                confirmSave(mProcOpen);
                return true;
            case jp.sblo.pandora.jota.text.TextView.FUNCTION_NEWFILE:
                confirmSave(mProcNew);
                return true;
            case jp.sblo.pandora.jota.text.TextView.FUNCTION_SEARCH:
                mProcSearch.run();
                return true;
            case jp.sblo.pandora.jota.text.TextView.FUNCTION_JUMP:
                mProcJump.run();
                return true;
            case jp.sblo.pandora.jota.text.TextView.FUNCTION_PROPERTY:
                mProcProperty.run();
                return true;
            case jp.sblo.pandora.jota.text.TextView.FUNCTION_HISTORY:
                confirmSave(mProcHistory);
                return true;
            case jp.sblo.pandora.jota.text.TextView.FUNCTION_OPENAPP:
                confirmSave(mProcViewByIntent);
                return true;
            case jp.sblo.pandora.jota.text.TextView.FUNCTION_SHARE:
                mProcShare.run();
                return true;
            case jp.sblo.pandora.jota.text.TextView.FUNCTION_SHAREFILE:
                confirmSave(mProcShareFile);
                return true;
            case jp.sblo.pandora.jota.text.TextView.FUNCTION_INSERT:
                mProcInsert.run();
                return true;
            case jp.sblo.pandora.jota.text.TextView.FUNCTION_QUIT:
				if (fragActivity instanceof TextEditorActivity) {
					((TextEditorActivity)fragActivity).quit();
				}
                //confirmSave(mProcQuit);//
                return true;
            case jp.sblo.pandora.jota.text.TextView.FUNCTION_SEARCHAPP:
                mProcSearchByIntent.run();
                return true;
            case jp.sblo.pandora.jota.text.TextView.FUNCTION_FONTUP:
                mProcFontUp.run();
                return true;
            case jp.sblo.pandora.jota.text.TextView.FUNCTION_FONTDOWN:
                mProcFontDown.run();
                return true;
            case jp.sblo.pandora.jota.text.TextView.FUNCTION_SELECT_LINE:
                mProcSelectLine.run();
                return true;
            case jp.sblo.pandora.jota.text.TextView.FUNCTION_SELECT_BLOCK:
                mProcSelectBlock.run();
                return true;
            case jp.sblo.pandora.jota.text.TextView.FUNCTION_LAUNCH_BY_SL4A:
                confirmSave(mProcSL4AByIntent);
                return true;
            case jp.sblo.pandora.jota.text.TextView.FUNCTION_MENU:
                fragActivity.openOptionsMenu();
                return true;

        }
        return false;
    }

	public static int saved = 0;
	public static int count = 0;
    public Runnable mProcQuit = new Runnable() {
        public void run() {
			Log.d(TAG, "saved " + saved + ", count " + count);
			if (count > 1 ? saved == count - 2 : saved == count) {
				//Intent intent = activity.getIntent();//
				if (intent != null) {
					if (Intent.ACTION_GET_CONTENT.equals(intent.getAction())) {
						if (mInstanceState.filename != null) {
							intent.setData(Uri.parse("file://" + mInstanceState.filename));
							fragActivity.setResult(Activity.RESULT_OK, intent);
						}
					}
				}
				fragActivity.finish();
			} else {
				slidingTabsFragment.closeTab(TextFrag.this);
			}
        }
    };

    private Runnable mProcOpen = new Runnable() {
        public void run() {
            Intent intent = new Intent(fragActivity, FileSelectorActivity.class);
            intent.putExtra(FileSelectorActivity.INTENT_MODE, FileSelectorActivity.MODE_OPEN);
            File[] fl = getHistory();
            if (fl != null) {
                intent.putExtra(FileSelectorActivity.INTENT_INIT_PATH, fl[0].getPath());
            } else {
                intent.putExtra(FileSelectorActivity.INTENT_INIT_PATH, mSettings.defaultdirectory);
            }
            if (mSettings.CharsetOpen.length() > 0) {
                intent.putExtra(FileSelectorActivity.INTENT_CHARSET, mSettings.CharsetOpen);
            }

            startActivityForResult(intent, REQUESTCODE_OPEN);
        }
    };

    private Runnable mProcNew = new Runnable() {
        public void run() {
			Log.d(TAG, "mProcNew isChanged " + mEditor.isChanged());
            mInstanceState.filename = null;
            mInstanceState.charset = DEF_CHARSET;
            mInstanceState.linebreak = DEF_LINEBREAK;
            mEditor.setText("");
            mEditor.setChanged(false);
            mEditor.setSelection(0, 0);
            mEditor.showIme(true);

            mLlSearch.setVisibility(View.GONE);
            mLlReplace.setVisibility(View.GONE);
            Selection.setDisableLostFocus(true);

            mEdtSearchWord.setText("");
            mBtnForward.setEnabled(false);
            mBtnBackward.setEnabled(false);
            mChkReplace.setVisibility(View.VISIBLE);
            mEdtReplaceWord.setText("");
            mBtnReplace.setEnabled(true);
            mBtnReplaceAll.setEnabled(true);

            mSearchResult = null;
            mInstanceState.nameCandidate = null;
        }
    };

    private Runnable mProcReopen = new Runnable() {
        public void run() {
            mTask = new TextLoadTask(fragActivity, TextFrag.this, mLine,mSettings.suppressMessage);
            mTask.execute(mNewFilename, mSettings.CharsetOpen);
            mNewFilename = null;
        }
    };

    private Runnable mProcReceiveShare = new Runnable() {
        public void run() {
            if (mSharedString != null && mSharedString.length() > 0) {
                mProcNew.run();
                Editable text = mEditor.getText();
                text.replace(0, 0, mSharedString);
                mSharedString = null;
            }
        }
    };

    abstract class PostProcess implements Runnable, DialogInterface.OnClickListener {
    }

    private PostProcess mProcHistory = new PostProcess() {

        class ListStruct {
            String main;

            String sub;
        }

        File[] fl = null;

        public void run() {
            fl = getHistory();
            if (fl != null) {
                ArrayList<ListStruct> items = new ArrayList<ListStruct>();
                int max = fl.length;
                for (int i = 0; i < max; i++) {
                    ListStruct item = new ListStruct();
                    item.main = fl[i].getName();
                    item.sub = fl[i].getPath();
                    items.add(item);
                }
                ListAdapter adapter = new DialogListAdapter(fragActivity, R.layout.dialog_list_row,
                        R.id.txtMain, items);
                new AlertDialog.Builder(fragActivity).setTitle(R.string.history).setAdapter(adapter,
                        this).show();
            }
        }

        public void onClick(DialogInterface dialog, int which) {
            CharSequence path = fl[which].getPath();
            mTask = new TextLoadTask(fragActivity, TextFrag.this, -1,mSettings.suppressMessage);
            mTask.execute(path.toString(), mSettings.CharsetOpen);
        }

        class DialogListAdapter extends ArrayAdapter<ListStruct> {

            class ViewHolder {
                TextView main;

                TextView sub;
            }

            public DialogListAdapter(Context context, int resource, int textViewResourceId,
                    List<ListStruct> objects) {
                super(context, resource, textViewResourceId, objects);
            }

            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                final View view;
                ViewHolder holder;
                if (convertView != null) {
                    view = convertView;
                    holder = (ViewHolder)view.getTag();
                } else {
                    view = View.inflate(fragActivity, R.layout.dialog_list_row, (ViewGroup)null);

                    holder = new ViewHolder();
                    holder.main = (TextView)view.findViewById(R.id.txtMain);
                    holder.sub = (TextView)view.findViewById(R.id.txtSub);

                    view.setTag(holder);
                }
                ListStruct d = getItem(position);

                holder.main.setText(d.main);
                holder.sub.setText(d.sub);

                return view;

            }

        }

    };

    public File[] getHistory() {
        // get history
        class FileInfo {
            String path;

            long lastaccess;
        }

        SharedPreferences sp = fragActivity.getSharedPreferences(SettingsActivity.PREF_HISTORY, Activity.MODE_PRIVATE);
        ArrayList<FileInfo> fl = new ArrayList<FileInfo>();
        fl.removeAll(fl);
        Map<String, ?> map = sp.getAll();

        // enumerate all of history
        for (Entry<String, ?> entry : map.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof String) {
                String[] vals = ((String)val).split(",");
                if (vals.length >= 3) {
                    try {
                        FileInfo fi = new FileInfo();
                        fi.path = entry.getKey();
                        fi.lastaccess = Long.parseLong(vals[2]);
                        if ( new File(fi.path).exists() ){
                            fl.add(fi);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }

        if (fl.size() == 0) {
            return null;
        }

        Collections.sort(fl, new Comparator<FileInfo>() {
            public int compare(FileInfo object1, FileInfo object2) {
                if (object2.lastaccess < object1.lastaccess) {
                    return -1;
                } else if (object2.lastaccess > object1.lastaccess) {
                    return 1;
                }
                return 0;
            }
        });

        int historymax = fl.size();
        if (historymax > 20) {
            historymax = 20;
        }
        File[] items = new File[historymax];
        int max = fl.size();
        for (int i = 0; i < max; i++) {
            if (i < historymax) {
                File f = new File(fl.get(i).path);
                items[i] = f;
            } else {
                // remove a record over 20 counts
                sp.edit().remove(fl.get(i).path);
            }
        }
        sp.edit().commit();
        return items;
    }

    private PostProcess mProcCharSet = new PostProcess() {
        String[] items;

        public void run() {
            items = getResources().getStringArray(R.array.CharcterSet);

            if (mInstanceState.charset != null) {
                int max = items.length;
                for (int i = 0; i < max; i++) {
                    if (mInstanceState.charset.equalsIgnoreCase(items[i])) {
                        items[i] = "*" + items[i];
                    }
                }
            }
            new AlertDialog.Builder(fragActivity).setTitle(R.string.charset).setItems(items, this)
                    .show();
        }

        public void onClick(DialogInterface dialog, int which) {
            mInstanceState.charset = items[which].replace("*", "");
        }
    };

    private PostProcess mProcLineBreak = new PostProcess() {
        String[] items;

        public void run() {
            items = getResources().getStringArray(R.array.LineBreak);

            int lb = mInstanceState.linebreak;
            items[lb] = "*" + items[lb];

            new AlertDialog.Builder(fragActivity).setTitle(R.string.linebreak).setItems(items, this)
                    .show();
        }

        public void onClick(DialogInterface dialog, int which) {
            mInstanceState.linebreak = which;
        }
    };

    private PostProcess mProcCreateShortcut = new PostProcess() {
        String[] items;

        public void run() {
            items = getResources().getStringArray(R.array.CreateShortcut);

            new AlertDialog.Builder(fragActivity).setTitle(R.string.menu_file_shortcut).setItems(
                    items, this).show();
        }

        public void onClick(DialogInterface dialog, int which) {
            // create shortcut
            Intent shortcutIntent = new Intent(Intent.ACTION_VIEW);
            shortcutIntent.setDataAndType(Uri.parse("file://" + mInstanceState.filename),
                    "text/plain");

            if (which == 1) {
                shortcutIntent.setPackage(fragActivity.getApplicationInfo().packageName);
            }
			if (mInstanceState.filename != null && mInstanceState.filename.startsWith("/")) {
				String name = new File(mInstanceState.filename).getName();

				Intent intent = new Intent();
				intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
				intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
				Parcelable iconResource = Intent.ShortcutIconResource.fromContext(fragActivity,
																				  R.drawable.icon_note);
				intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
				intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
				fragActivity.sendBroadcast(intent);
			} else {
				Toast.makeText(fragActivity, "File is not existed", Toast.LENGTH_SHORT).show();//
			}
        }
    };

    private Runnable mProcProperty = new Runnable() {

        public void run() {
            WordCounter.Result result = WordCounter.count(mEditor);

            TextView tv = new TextView(fragActivity);

            String filename = mInstanceState.filename;
            if ( filename == null ){
                filename = "";
            }
            String[] items = getResources().getStringArray(R.array.LineBreak);
            String codelb = mInstanceState.charset + "/" + items[mInstanceState.linebreak];

            String resstr = String.format(getString(R.string.result_word_count),
                    result.charactrers, result.lines, result.logicallines, result.words,
                    filename , codelb);

            tv.setText(resstr);

            new AlertDialog.Builder(fragActivity).setMessage(R.string.menu_file_property).setView(tv)
                    .setPositiveButton(R.string.label_ok, null).show();
        }

    };

    private Runnable mProcInsert = new Runnable() {
        public void run() {

            if (mSettings.directintent2 == null) {
                Intent intent = new Intent("com.adamrocker.android.simeji.ACTION_INTERCEPT");
                intent.addCategory("com.adamrocker.android.simeji.REPLACE");

                int startsel = mEditor.getSelectionStart();
                int endsel = mEditor.getSelectionEnd();
                Editable text = mEditor.getText();

                String substr = "";
                if (startsel != endsel) {
                    if (endsel < startsel) {
                        int temp = startsel;
                        startsel = endsel;
                        endsel = temp;
                    }
                    if (endsel - startsel > jp.sblo.pandora.jota.text.TextView.MAX_PARCELABLE) {
                        Toast.makeText(fragActivity, R.string.toast_overflow_of_limit,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    substr = text.subSequence(startsel, endsel).toString();
                }
                intent.putExtra("replace_key", substr);

                try {
                    Intent pickIntent = new Intent(fragActivity, ActivityPicker.class);
                    pickIntent.putExtra(Intent.EXTRA_INTENT, intent);
                    mReservedIntent = intent;
                    mReservedRequestCode = REQUESTCODE_MUSHROOM;
                    startActivityForResult(pickIntent, REQUESTCODE_APPCHOOSER);
                } catch (Exception e) {
                }
            } else {
                sendDirectIntent(mSettings.directintent2);
            }
        }

    };

    private void sendDirectIntent(Intent intent) {
        if (intent == null) {
            return;
        } else {

            String substr = null;

            Editable text = mEditor.getText();
            int startsel = mEditor.getSelectionStart();
            int endsel = mEditor.getSelectionEnd();
            if (startsel != endsel) {
                if (endsel < startsel) {
                    int temp = startsel;
                    startsel = endsel;
                    endsel = temp;
                }
                if (endsel - startsel > jp.sblo.pandora.jota.text.TextView.MAX_PARCELABLE) {
                    Toast.makeText(fragActivity, R.string.toast_overflow_of_limit, Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                substr = text.subSequence(startsel, endsel).toString();
            }

            if (intent.getAction().equals(Intent.ACTION_SEND)) {
                if (substr != null) {
                    intent.putExtra(Intent.EXTRA_TEXT, substr);
                } else {
                    if (text.length() <= jp.sblo.pandora.jota.text.TextView.MAX_PARCELABLE) {
                        intent.putExtra(Intent.EXTRA_TEXT, text.toString());
                    } else {
                        Toast.makeText(fragActivity, R.string.toast_overflow_of_limit,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                }
            } else if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
                if (substr != null) {
                    intent.putExtra(SearchManager.QUERY, substr);
                } else {
                    return;
                }
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                }
            } else if (intent.getAction().equals("com.adamrocker.android.simeji.ACTION_INTERCEPT")) {
                if (substr != null) {
                    intent.putExtra("replace_key", substr);
                } else {
                    intent.putExtra("replace_key", "");
                }
                startActivityForResult(intent, REQUESTCODE_MUSHROOM);
            } else if (intent.getAction().equals(Intent.ACTION_VIEW)) {
                if (mInstanceState.filename != null || mEditor.isChanged()) {
                    mProcViewByIntent.setIntent(intent);
                    confirmSave(mProcViewByIntent);
                }
            }
        }
    }

    private Runnable mProcJump = new Runnable() {
        private EditText mEditText;

        @Override
        public void run() {
            mEditText = (EditText)fragActivity.getLayoutInflater().inflate(R.layout.jump, null);

            final AlertDialog dialog = new AlertDialog.Builder(fragActivity).setTitle(
                    R.string.menu_edit_jump).setMessage(R.string.message_edit_jump).setView(
                    mEditText).setPositiveButton(R.string.label_ok,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialogAction();
                        }
                    }).create();

            mEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_GO
                            || (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                        dialogAction();
                        dialog.dismiss();
                    }

                    return true;
                }
            });

            dialog.show();

        }

        private void dialogAction() {
            String val = mEditText.getText().toString();
            try {
                int line = Integer.parseInt(val) - 1;
                if (line >= mEditor.getLineCount()) {
                    line = mEditor.getLineCount() - 1;
                }
                if (line < 0) {
                    line = 0;
                }
                Layout layout = mEditor.getLayout();
                int pos = layout.getLineStart(line);
                mEditor.setSelection(pos);
            } catch (Exception e) {
            }
        }

    };

    private Runnable mProcSearchByIntent = new Runnable() {

        public void run() {
            int startsel = mEditor.getSelectionStart();
            int endsel = mEditor.getSelectionEnd();
            Editable text = mEditor.getText();

            String substr = "";
            if (startsel != endsel) {
                if (endsel < startsel) {
                    int temp = startsel;
                    startsel = endsel;
                    endsel = temp;
                }
                if (endsel - startsel > jp.sblo.pandora.jota.text.TextView.MAX_PARCELABLE) {
                    Toast.makeText(fragActivity, R.string.toast_overflow_of_limit, Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                substr = text.subSequence(startsel, endsel).toString();
                searchWord(substr);
            } else {
                final EditText mInput;
                final View layout = View.inflate(fragActivity, R.layout.input_search_word, null);
                mInput = (EditText)layout.findViewById(R.id.search_word);

                AlertDialog.Builder builder = new AlertDialog.Builder(fragActivity);
                builder.setTitle(getString(R.string.menu_search_byintent));
                builder.setCancelable(true);
                builder.setPositiveButton(getString(R.string.label_ok),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                String substr = mInput.getText().toString();
                                searchWord(substr);
                            }
                        });
                builder.setView(layout);

                final AlertDialog dialog = builder.create();

                mInput.setOnKeyListener(new View.OnKeyListener() {
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_ENTER
                                && event.getAction() == KeyEvent.ACTION_UP) {
                            String substr = mInput.getText().toString();
                            searchWord(substr);
                            dialog.cancel();
                        }
                        return false;
                    }
                });

                dialog.show();

            }
        }
        private void searchWord(String substr) {
            Intent intent = new Intent(Intent.ACTION_SEARCH);
            intent.putExtra(SearchManager.QUERY, substr);

            try {
                Intent pickIntent = new Intent(fragActivity, ActivityPicker.class);
                pickIntent.putExtra(Intent.EXTRA_INTENT, intent);
                mReservedIntent = intent;
                mReservedRequestCode = REQUESTCODE_SEARCHBYINTENT;
                startActivityForResult(pickIntent, REQUESTCODE_APPCHOOSER);
            } catch (Exception e) {
            }
        }
    };

    private Runnable mProcFontUp = new Runnable() {
        public void run() {
            float size = mEditor.getTextSize();
            size +=2;
            if ( size <= 96 ){
                mEditor.setTextSize(TypedValue.COMPLEX_UNIT_PX,size);
            }
        }
    };
    private Runnable mProcFontDown = new Runnable() {
        public void run() {
            float size = mEditor.getTextSize();
            size -= 2;
            if ( size >= 2 ){
                mEditor.setTextSize(TypedValue.COMPLEX_UNIT_PX,size);
            }
        }
    };
    private Runnable mProcSelectLine = new Runnable() {
        public void run() {
            mEditor.selectLine();
        }
    };
    private Runnable mProcSelectBlock = new Runnable() {
        public void run() {
            mEditor.selectBlock();
        }
    };

    abstract class ViewByIntent implements Runnable {
        abstract public void setIntent(Intent i);
    }


    private ViewByIntent mProcViewByIntent = new ViewByIntent() {
        Intent mIntent = null;

        public void run() {
            if (mIntent == null) {
                mIntent = new Intent();
                mIntent.setAction(Intent.ACTION_VIEW);
            }
            mIntent.setDataAndType(Uri.parse("file://" + mInstanceState.filename), "text/plain");
            try {
                startActivity(mIntent);
            } catch (ActivityNotFoundException e) {
            }
            mIntent = null;
        }

        public void setIntent(Intent i) {
            mIntent = i;
        }

    };

    private Runnable mProcSL4AByIntent = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent();
            intent.setAction("com.googlecode.android_scripting.action.LAUNCH_FOREGROUND_SCRIPT");
            intent.putExtra("com.googlecode.android_scripting.extra.SCRIPT_PATH",  mInstanceState.filename );
            intent.setClassName("com.googlecode.android_scripting", "com.googlecode.android_scripting.activity.ScriptingLayerServiceLauncher");
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
            }
        }
    };

    private ViewByIntent mProcShareFile = new ViewByIntent() {
        Intent mIntent = null;

        public void run() {
            if (mIntent == null) {
                mIntent = new Intent();
                mIntent.setAction(Intent.ACTION_SEND);
            }
            String url = "file://" + mInstanceState.filename;
            mIntent.setType("text/plain");
            mIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(url));
            mIntent.putExtra(Intent.EXTRA_TITLE, url);
            try {
                startActivity(mIntent);
            } catch (ActivityNotFoundException e) {
            }
            mIntent = null;
        }

        public void setIntent(Intent i) {
            mIntent = i;
        }

    };

    private Runnable mProcShare = new Runnable() {
        public void run() {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");

            int startsel = mEditor.getSelectionStart();
            int endsel = mEditor.getSelectionEnd();
            Editable text = mEditor.getText();

            String substr = null;
            if (startsel != endsel) {
                if (endsel < startsel) {
                    int temp = startsel;
                    startsel = endsel;
                    endsel = temp;
                }
                if (endsel - startsel > jp.sblo.pandora.jota.text.TextView.MAX_PARCELABLE) {
                    Toast.makeText(fragActivity, R.string.toast_overflow_of_limit, Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                substr = text.subSequence(startsel, endsel).toString();
            }
            if (substr == null) {
                if (text.length() <= jp.sblo.pandora.jota.text.TextView.MAX_PARCELABLE) {
                    substr = text.toString();
                }
            }
            if (substr != null) {
                intent.putExtra(Intent.EXTRA_TEXT, substr);

                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                }
            }
        }
    };

    private Runnable mProcShareScreenshot = new Runnable() {
        public void run() {
            final View view = fragActivity.getWindow().getDecorView();
            InputMethodManager imm = (InputMethodManager)fragActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
            mHandler.postDelayed( new Runnable() {
                @Override
                public void run() {
                    view.setDrawingCacheEnabled(true);
                    Bitmap bitmap=view.getDrawingCache();
                    if (bitmap!=null){
                        try{
                            ContentResolver cr = fragActivity.getContentResolver();
                            String url = MediaStore.Images.Media.insertImage(cr, bitmap, "Jota screen shot", "Jota screen shot");
                            Uri U = Uri.parse(url);

                            Intent i = new Intent(Intent.ACTION_SEND);
                            i.setType("image/jpg");
                            i.putExtra(Intent.EXTRA_STREAM, U);
                            startActivity(Intent.createChooser(i,getString(R.string.label_share_to)));
                        }
                        catch(ActivityNotFoundException e){}
                    }
                    view.setDrawingCacheEnabled(false);
                }
            },100);
        }
    };

    private Runnable mProcDirect = new Runnable() {
        public void run() {
            sendDirectIntent(mSettings.directintent);
        }
    };

    private Runnable mProcSearch = new Runnable() {
        public void run() {
            int v = mLlSearch.getVisibility();

            if (v != View.VISIBLE) {
                mLlSearch.setVisibility(View.VISIBLE);
                mChkReplace.setVisibility(View.VISIBLE);
                mEdtSearchWord.requestFocus();
                Selection.setDisableLostFocus(false);


                int startsel = mEditor.getSelectionStart();
                int endsel = mEditor.getSelectionEnd();
                Editable text = mEditor.getText();

                String substr = "";
                if (startsel != endsel) {
                    if (endsel < startsel) {
                        int temp = startsel;
                        startsel = endsel;
                        endsel = temp;
                    }
                    if (endsel - startsel < 256) {
                        substr = text.subSequence(startsel, endsel).toString();
                        int cr = TextUtils.indexOf(substr, '\n');
                        if (cr != -1) {
                            substr = substr.subSequence(0, cr).toString();
                        }
                        mEdtSearchWord.setText(substr);
                    }
                }

            } else {
                mChkReplace.performClick();
            }
        }
    };

    private void doSearch(String searchword) {
        if (mSearchResult != null) {
            mProcOnSearchResult.search();
            return;
        }
        new Search(fragActivity, searchword, mEditor.getText(), mSettings.re, mSettings.ignorecase,
                mProcOnSearchResult);
    }

    private void doReplace(String searchword) {
        if (mSearchResult != null) {
            mProcReplace.run();
            return;
        }
        new Search(fragActivity, searchword, mEditor.getText(), mSettings.re, mSettings.ignorecase,
                mProcOnSearchResult);
    }

    private Runnable mProcReplace = new Runnable() {

        public void run() {
            int cursor = mEditor.getSelectionStart();
            int cursorend = mEditor.getSelectionEnd();

            Record cursorRecord = new Record();
            cursorRecord.start = cursor;

            int cursorpos = Collections.binarySearch(mSearchResult, cursorRecord,
                    new Comparator<Record>() {
                        public int compare(Record object1, Record object2) {
                            return object1.start - object2.start;
                        }
                    });

            if (cursorpos >= 0) { // on the word
                Record r = mSearchResult.get(cursorpos);

                if (r.start == cursor && r.end == cursorend) {

                    String replaceword = mReplaceWord;
                    int difflen = replaceword.length() - (r.end - r.start);

                    mEditor.getEditableText().replace(r.start, r.end, replaceword);

                    if (mSearchResult != null) {
                        mSearchResult.remove(r);
                        for (int i = cursorpos; i < mSearchResult.size(); i++) {
                            Record adjustr = mSearchResult.get(i);
                            adjustr.start += difflen;
                            adjustr.end += difflen;
                        }
                    }
                }

            } else { // not on the word
            }
            fragActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mSearchForward) {
                        mBtnForward.performClick();
                    } else {
                        mBtnBackward.performClick();
                    }
                }
            });
        }
    };

    private void doReplaceAll(String searchword) {
        new Search(fragActivity, searchword, mEditor.getText(), mSettings.re, mSettings.ignorecase,
                mProcOnReplaceAllResult);
    }

    abstract class PostSearchProcess implements OnSearchFinishedListener {
        abstract public void search();
    }

    private OnSearchFinishedListener mProcOnReplaceAllResult = new OnSearchFinishedListener() {
        public void onSearchFinished(ArrayList<Record> data) {
            mSearchResult = null;

            Editable text = mEditor.getEditableText();
            CharSequence replaceword = mReplaceWord;
            mChangeCancel = true;

            if (data.size() > 100) {
                mEditor.enableUndo(false);
            }
            for (int i = data.size() - 1; i >= 0; i--) {
                Record record = data.get(i);
                text.replace(record.start, record.end, replaceword);
            }
            mChangeCancel = false;
            onChanged();
            mEditor.enableUndo(true);
        }
    };

    public class ReplaceAllTask extends AsyncTask<Integer, Integer, Integer> {
        private Activity mParent;

        private Editable mText;

        private CharSequence mReplaceWord;

        private ProgressDialog mProgressDialog;

        ArrayList<Record> mData;

        public ReplaceAllTask(Activity parent, Editable text, CharSequence replaceword,
                ArrayList<Record> data) {
            super();
            mParent = parent;
            mText = text;
            mReplaceWord = replaceword;
            mData = data;
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(mParent);
            mProgressDialog.setTitle(R.string.spinner_message);
            // mProgressDialog.setMessage(R.string.spinner_message);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                }
            });
            mProgressDialog.show();
            mChangeCancel = true;
            mParent = null;
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            Editable text = mText;
            CharSequence replaceword = mReplaceWord;
            ArrayList<Record> data = mData;
            for (int i = data.size() - 1; i >= 0; i--) {
                Record record = data.get(i);
                text.replace(record.start, record.end, replaceword);
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
            mData = null;
            mChangeCancel = false;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }

    private PostSearchProcess mProcOnSearchResult = new PostSearchProcess() {
        @Override
        public void search() {
            int cursor = mEditor.getSelectionStart();

            Record cursorRecord = new Record();
            cursorRecord.start = cursor;

            int cursorpos = Collections.binarySearch(mSearchResult, cursorRecord,
                    new Comparator<Record>() {
                        public int compare(Record object1, Record object2) {
                            return object1.start - object2.start;
                        }
                    });

            if (cursorpos >= 0) { // on the word

                if (mSearchForward) {
                    cursorpos++;
                    if (cursorpos < mSearchResult.size()) {
                        // found
                        highlight(cursorpos);
                    }
                } else {
                    cursorpos--;
                    if (cursorpos >= 0) {
                        // found
                        highlight(cursorpos);
                    }
                }

            } else { // not on the word
                cursorpos = -1 - cursorpos;
                if (mSearchForward) {
                    if (cursorpos < mSearchResult.size()) {
                        // found
                        highlight(cursorpos);
                    }
                } else {
                    cursorpos--;
                    if (cursorpos >= 0) {
                        // found
                        highlight(cursorpos);
                    }
                }
            }

            // Log.e(TAG , "found="+cursorpos);
            // for( Record record : mSearchResult ){
            // Log.e(TAG , ""+record.start + "," + record.end );
            // }
        }

        public void onSearchFinished(ArrayList<Record> data) {
            mSearchResult = data;
            search();
        }

        private void highlight(int pos) {
            // Editable text = mEditor.getText();
            Record r = mSearchResult.get(pos);
            mEditor.setSelection(r.start, r.end);
            // mEditor.requestFocus();
            // text.setSpan(bgspan, r.start, r.end,
            // Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            // text.setSpan(fgspan, r.start, r.end,
            // Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }

    };

    private Runnable mProcOpenSl4a = new Runnable() {
        public void run() {
            mEditor.setText(mSl4aContents);
            mInstanceState.filename = null;
            mInstanceState.nameCandidate = mTempCandidate;
            mTempCandidate = null;
            onChanged();
        }
    };


    private OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            //applySetting();
            mSharedPreferenceChanged=true;
        }
    };

    void applySetting() {
        boolean landscape=false;
        jp.sblo.pandora.jota.text.EditText editor = mEditor;

        mSharedPreferenceChanged=false;
        editor.setAutoCapitalize(mBootSettings.autoCapitalize);

        mSettings = SettingsActivity.readSettings(fragActivity);
        editor.setNameDirectIntent(mSettings.intentname);
        editor.setTypeface(mSettings.fontface);
        editor.setTextSize(mSettings.fontsize);

        int altkey = (mSettings.shortcutaltleft ? KeyEvent.META_ALT_LEFT_ON : 0)
        | (mSettings.shortcutaltright ? KeyEvent.META_ALT_RIGHT_ON : 0);

        int ctrlkey =  (mSettings.shortcutctrl ? 8 : 0) // ctrl key on Dynabook AZ
        | 0x1000                            // ctrl key on Honeycomb and Lifetouch Note;
        ;

        editor.setShortcutMetaKey(altkey,ctrlkey);
        mEdtSearchWord.setShortcutMetaKey(altkey,ctrlkey);
        mEdtReplaceWord.setShortcutMetaKey(altkey,ctrlkey);

        editor.setHorizontallyScrolling(!mSettings.wordwrap);

        editor.setTextColor(mSettings.textcolor);
        editor.setHighlightColor(mSettings.highlightcolor);

        String wallpaper;
        landscape = getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT;

        if ( landscape ){
            wallpaper = mSettings.wallpaperLandscape;
        }else{
            wallpaper = mSettings.wallpaperPortrait;
        }

        if ( TextUtils.isEmpty(wallpaper) ){
			if (fragActivity instanceof ExplorerActivity) {
				editor.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
				editor.setTextColor(ExplorerActivity.TEXT_COLOR);
			} else if (SettingsActivity.THEME_DEFAULT.equals(mSettings.theme)) {
                editor.setBackgroundResource(R.drawable.textfield_default);
            } else if (SettingsActivity.THEME_BLACK.equals(mSettings.theme)) {
                editor.setBackgroundResource(R.drawable.textfield_black);
            }
            mWallpaper.setVisibility(View.GONE);
            mTransparency.setVisibility(View.GONE);
        }else{
            int transparency = 30;
            try{
                transparency = Integer.parseInt( mSettings.wallpaperTransparency );
            }catch(Exception e){}

            int tr = 255*(100-transparency)/100;
            tr <<= 24;

            if (SettingsActivity.THEME_DEFAULT.equals(mSettings.theme)) {
                mTransparency.setBackgroundColor(tr|0xF0F0F0);
            }else{
                mTransparency.setBackgroundColor(tr|0x101010);
            }
            editor.setBackgroundColor(0);

            mWallpaper.setVisibility(View.VISIBLE);
            mTransparency.setVisibility(View.VISIBLE);
            mWallpaperBmp = BitmapFactory.decodeFile(wallpaper);
            if ( mWallpaperBmp != null ){
                mWallpaper.setImageBitmap(mWallpaperBmp);
            }
        }

        editor.enableUnderline(mSettings.underline);
        mEdtSearchWord.enableUnderline(false);
        mEdtReplaceWord.enableUnderline(false);
        editor.setUnderlineColor(mSettings.underlinecolor);
        editor.setShortcutSettings(mSettings.shortcuts);
        mEdtSearchWord.setShortcutSettings(mSettings.shortcuts);
        mEdtReplaceWord.setShortcutSettings(mSettings.shortcuts);

        editor.setUseVolumeKey(mSettings.useVolumeKey);

        if ( landscape ) {
            editor.setWrapWidth(mSettings.WrapCharL, mSettings.WrapWidthL);
        } else {
            editor.setWrapWidth(mSettings.WrapCharP, mSettings.WrapWidthP);
        }
        editor.setTabWidth(mSettings.TabChar, mSettings.TabWidth);
        if (mSettings.TrackballButton.equals(SettingsActivity.TB_CENTERING)) {
            editor.setDpadCenterFunction(jp.sblo.pandora.jota.text.EditText.FUNCTION_CENTERING);
        } else if (mSettings.TrackballButton.equals(SettingsActivity.TB_ENTER)) {
            editor.setDpadCenterFunction(jp.sblo.pandora.jota.text.EditText.FUNCTION_ENTER);
        } else if (mSettings.TrackballButton.equals(SettingsActivity.TB_CONTEXTMENU)) {
            editor.setDpadCenterFunction(jp.sblo.pandora.jota.text.EditText.FUNCTION_CONTEXTMENU);
        } else {
            editor.setDpadCenterFunction(jp.sblo.pandora.jota.text.EditText.FUNCTION_NONE);
        }
		if (fragActivity instanceof TextEditorActivity) {
			editor.setShowLineNumbers(mSettings.showLineNumbers);
		} else {
			editor.setShowLineNumbers(false);
		}
        editor.setAutoIndent(mSettings.autoIndent);
        editor.setLineSpacing(0.0F, (100.0F + mSettings.lineSpace) / 100.0F);
        editor.setShowTab(mSettings.showTab);

        editor.setNavigationDevice( mSettings.shortcutctrl || (getResources().getConfiguration().navigation != Configuration.NAVIGATION_NONAV && Build.VERSION.SDK_INT < 11) );
        if ( mSettings.shortcutctrlltn ){
            editor.setKeycodes(111,112,113);
            mEdtSearchWord.setKeycodes(111,112,113);
            mEdtReplaceWord.setKeycodes(111,112,113);
        }
        editor.setDontUseSoftkeyWithHardkey( mSettings.specialkey_desirez );
        editor.enableBlinkCursor(mSettings.blinkCursor);
        mEdtSearchWord.setDontUseSoftkeyWithHardkey( mSettings.specialkey_desirez );
        mEdtSearchWord.enableBlinkCursor(mSettings.blinkCursor);
        mEdtReplaceWord.setDontUseSoftkeyWithHardkey( mSettings.specialkey_desirez );
        mEdtReplaceWord.enableBlinkCursor(mSettings.blinkCursor);
        boolean toolbarVisible = mSettings.showToolbar&&(!landscape || !mSettings.toolbarHideLandscape);
		//Log.d(TAG, "mSettings.showToolbar " + mSettings.showToolbar + ", " + landscape + ", " + mSettings.toolbarHideLandscape);
        mToolbarBase.setVisibility(toolbarVisible?View.VISIBLE:View.GONE);
        editor.setForceScroll(mSettings.forceScroll);
        editor.setCtrlPreIme(mSettings.ctrlPreIme);
        initToolbar(mSettings.toolbars,mSettings.toolbarBigButton);

        if ( JotaTextEditor.sHoneycomb && mBootSettings.hideTitleBar) {
            mMenuButton.setVisibility(View.VISIBLE);
        }else{
            mMenuButton.setVisibility(View.GONE);
        }

        editor.setNeedMenu( JotaTextEditor.sIceCreamSandwich && mBootSettings.hideTitleBar && !toolbarVisible);
    }

    void applyBootSetting() {
        mBootSettings = SettingsActivity.readBootSettings(fragActivity);

        if ( mBootSettings.hideTitleBar) {
            if ( JotaTextEditor.sIceCreamSandwich ){
                fragActivity.setTheme(R.style.Theme_AppCompat_DayNight_NoActionBar);
            } else {
                fragActivity.setTheme(R.style.Theme_AppCompat_NoActionBar);
            }
        } 
//        if ( SettingsActivity.checkDonate(this) ){
//            Toast.makeText(this, R.string.summary_wallpaper, Toast.LENGTH_LONG ).show();
//        }
    }

	@Override
    public void updateColor(View rootView) {
		if (!(fragActivity instanceof TextEditorActivity)) {
			getView().setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
			mEditor.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
			mEditor.setTextColor(ExplorerActivity.TEXT_COLOR);
			mToolbarBase.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
			mToolbar.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
	
			int n = mToolbar.getChildCount();
			for (int i = 0; i < n; i++) {
				View childAt = mToolbar.getChildAt(i);
				if (childAt instanceof ImageView) {
					((ImageView)childAt).setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
				} else if (childAt instanceof Button) {
					((Button)childAt).setTextColor(ExplorerActivity.TEXT_COLOR);
				}
			}
		}
	}

	@Override
    public void onResume() {
        Log.d(TAG, "onResume " + toString());
		try {
        super.onResume();
		fragActivity =  this.getActivity();
		if (mSharedPreferenceChanged ){
            applySetting();
        }
        if (mBootSettings.hideSoftkeyIS01) {
            IS01FullScreen.setFullScreenOnIS01();//fragActivity
        }
		} catch (Throwable re) {
			re.printStackTrace();
		}
    }

    private OnClickListener mOnClickSave = new OnClickListener() {
        @Override
        public void onClick(View v) {
            save();
        }
    };

    private jp.sblo.pandora.jota.text.EditText getCurrentFocusEditText( )
    {
        jp.sblo.pandora.jota.text.EditText target = null;
        if ( mEditor.hasFocus() ){
            target = mEditor;
        }else if ( mEdtSearchWord.hasFocus() ){
            target = mEdtSearchWord;
        }else if ( mEdtReplaceWord.hasFocus() ){
            target = mEdtReplaceWord;
        }
        return target;
    }

    private OnClickListener mOnClickToolbar = new OnClickListener() {
        @Override
        public void onClick(View v) {

            Integer function = (Integer)v.getTag();
            switch( function ){
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_UNDO:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_COPY:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_CUT:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_PASTE:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_REDO:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_SELECT_ALL:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_SELECT:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_SELECT_WORD:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_CURSOR_LEFT:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_CURSOR_RIGHT:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_HOME:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_END:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_TAB:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_DEL:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_FORWARD_DEL:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_PARENTHESIS:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_CURLY:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_BRACKETS:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_XMLBRACE:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_CCOMMENT:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_DOUBLEQUOTE:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_SINGLEQUOTE:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_KAGIKAKKO:
                case jp.sblo.pandora.jota.text.TextView.FUNCTION_NIJUKAGI:
                    jp.sblo.pandora.jota.text.EditText target = getCurrentFocusEditText();
                    if ( target != null ){
                        target.doFunction(function);
                    }
                    break;
                default:
                    mEditor.doFunction(function);
                    break;
            }
        }
    };

    private String getToolbarLabel(int f) {

        int[] tblFunc = SettingsShortcutActivity.TBL_FUNCTION;
//        String[] tblLabel = SettingsShortcutActivity.TBL_TOOLNAME;
        for (int i = 0; i < tblFunc.length; i++) {
            if (tblFunc[i] == f) {
                return SettingsShortcutActivity.getToolbarLabel(fragActivity, i);
            }
        }
        return "";
    }


    private void initToolbar( ArrayList<Integer> toolbars , boolean bigButton)
    {
        mToolbar.removeAllViews();
		if (!(fragActivity instanceof TextEditorActivity)) {
			mToolbarBase.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
			mToolbar.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
			mMenuButton.setBackgroundResource(R.drawable.ripple);
		} else {
			//activity.getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.textfield_default));
		}
		
		Button button;
		ImageView iv = null;
		final int density = (int)getResources().getDisplayMetrics().density;
		final LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER;
		lp.width = 44 * density;
		final Context ctx = getContext();
		for( Integer function : toolbars ){
            if ( bigButton ){
                button = new Button(ctx);//, null, R.style.Widget_AppCompat_Button);
                button.setTextSize(16);
                button.setBackgroundResource( R.drawable.btn_default );
				//button.setPadding(0, 5 * density, 0, 0);
            } else {
                button = new Button(ctx);//, null, R.style.Widget_AppCompat_Button_Small);
                button.setTextSize(14);
                button.setBackgroundResource(R.drawable.btn_default_small);
				//button.setPadding(0, 5 * density, 0, 0);
			}
			button.setMinWidth(72 * density);
			iv = new ImageView(fragActivity);
			iv.setBackgroundResource(R.drawable.ripple);
			if (!(fragActivity instanceof TextEditorActivity)) {
				iv.setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
			} else {
				iv.setColorFilter(0xff404040, PorterDuff.Mode.SRC_IN);
			}
				if (function == jp.sblo.pandora.jota.text.TextView.FUNCTION_SELECT_ALL) {//}"Select All".equals(toolname)) {
					iv.setImageResource(R.drawable.ic_select_all_white_36dp);
					button = null;
				} else if (function == jp.sblo.pandora.jota.text.TextView.FUNCTION_UNDO) {//"Undo".equals(toolname)) {
					iv.setImageResource(R.drawable.ic_action_undo);
					button = null;
				} else if (function == jp.sblo.pandora.jota.text.TextView.FUNCTION_COPY) {//"Copy".equals(toolname)) {
					iv.setImageResource(R.drawable.ic_action_copy);
					button = null;
				} else if (function == jp.sblo.pandora.jota.text.TextView.FUNCTION_CUT) {//" Cut ".equals(toolname)) {
					iv.setImageResource(R.drawable.ic_action_cut);
					button = null;
				} else if (function == jp.sblo.pandora.jota.text.TextView.FUNCTION_PASTE) {//"Paste".equals(toolname)) {
					iv.setImageResource(R.drawable.ic_action_paste);
					button = null;
				} else if (function == jp.sblo.pandora.jota.text.TextView.FUNCTION_SAVE) {//"Save".equals(toolname)) {
					iv.setImageResource(R.drawable.ic_action_save);
					button = null;
				} else if (function == jp.sblo.pandora.jota.text.TextView.FUNCTION_SEARCH) {//"Search".equals(toolname)) {
					iv.setImageResource(R.drawable.ic_action_search);
					button = null;
				} else if (function == jp.sblo.pandora.jota.text.TextView.FUNCTION_REDO) {//"Redo".equals(toolname)) {
					iv.setImageResource(R.drawable.ic_action_redo);
					button = null;
				} else {
					button.setText(getToolbarLabel(function));
					button.setTextColor(ExplorerActivity.TEXT_COLOR);
					iv = null;
				}
//			} else {
//				button.setText(getToolbarLabel(function));
//				button.setTextColor(0xff404040);
//			}
			final View v = button != null ? button : iv;
			v.setMinimumWidth(44 * density);
            v.setTag(function);
            v.setOnClickListener(mOnClickToolbar);
            v.setFocusable(false);
            mToolbar.addView(v, lp);
        }

    }

    private Runnable mShowImeProc = new Runnable() {
        @Override
        public void run() {
            if ( mEditor != null ){
                mEditor.showIme(true);
            }
        }
    };

}
