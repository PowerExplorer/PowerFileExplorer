package net.gnu.explorer;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amaze.filemanager.activities.ThemedActivity;
import com.amaze.filemanager.filesystem.BaseFile;
import com.amaze.filemanager.services.DeleteTask;
import com.amaze.filemanager.services.asynctasks.CopyFileCheck;
import com.amaze.filemanager.ui.LayoutElement;
import com.amaze.filemanager.ui.dialogs.GeneralDialogCreation;
import com.amaze.filemanager.utils.DataUtils;
import com.amaze.filemanager.utils.OpenMode;
import com.amaze.filemanager.utils.files.EncryptDecryptUtils;
import com.amaze.filemanager.utils.files.Futils;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.demo.MediaPlayerFragment;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.gnu.androidutil.AndroidUtils;
import net.gnu.texteditor.TextFrag;
import android.view.MotionEvent;
import com.thefinestartist.finestwebview.WebFragment;
import android.app.Activity;
//import org.geometerplus.android.fbreader.FBReader;

public abstract class Frag extends Fragment implements View.OnTouchListener, Cloneable, Serializable {

	private static final String TAG = "Frag";

	public TYPE type = TYPE.EXPLORER;
	public String currentPathTitle = "";
	protected String title;

	protected ViewGroup sortBarLayout;
	public ExplorerActivity activity;
	protected FragmentActivity fragActivity;
	public SharedPreferences sharedPref;
	
	public int accentColor, primaryColor, primaryTwoColor;

	public SlidingTabsFragment slidingTabsFragment;
	
	protected boolean fake = false;
	private Toast toast = null;
    
	public static final enum TYPE {
		EMPTY, EXPLORER, ZIP, SELECTION, FTP, TEXT, WEB, PDF, CHM, PHOTO, MEDIA, APP, TRAFFIC_STATS, PROCESS//FBReader, 
		};

	public static Frag getFrag(final SlidingTabsFragment sliding, final TYPE t, final String path) {
		Frag frag = null;
		if (t == TYPE.SELECTION) {
			frag = new ContentFragment();
			frag.type = TYPE.SELECTION;
			frag.title = "Selection";
			if (path != null && path.length() > 0) {
				final List<String> asList = Arrays.asList(path.split("\\|"));
				final ContentFragment cfrag = (ContentFragment)frag;
				for (String le : asList) {
					cfrag.dataSourceL1.add(new LayoutElement(new File(le)));
				}
			}
		} else if (t == TYPE.APP) {
			frag = new AppsFragment();
		} else if (t == TYPE.PROCESS) {
			frag = new ProcessFragment();
		} else if (t == TYPE.TEXT) {
			frag = TextFrag.newInstance(null, "Text", path);
		} else if (t == TYPE.PDF) {
			frag = new PDFFragment();
		} else if (t == TYPE.WEB) {
			frag = new WebFragment();
//		} else if (t == TYPE.FBReader) {
//			return new FBReader();
		} else if (t == TYPE.MEDIA) {
			frag = new MediaPlayerFragment();
		} else if (t == TYPE.ZIP) {
			frag = new ZipFragment();
		} else if (t == TYPE.CHM) {
			frag = new CHMFrag();
		} else if (t == TYPE.PHOTO) {
			frag = new PhotoFragment();
		} else if (t == TYPE.TRAFFIC_STATS) {
			frag = new DataTrackerFrag();
		} else if (t == TYPE.FTP) {
			frag = new FTPServerFragment();
		} else if (t == TYPE.EXPLORER) {
			frag = new ContentFragment();
		} 
		if (frag != null) {
			frag.currentPathTitle = path;
			frag.slidingTabsFragment = sliding;
		}
		return frag;
	}

	public void updateList() {} //TODO
	public void load(String path) {}
	public void load(String path, Runnable run) {}
	public void open(final int curPos, final List<LayoutElement> path) {}
	public void updateColor(View rootView) {}

	public void clone(final Frag frag, final boolean fake) {
		this.title = frag.title;
		this.currentPathTitle = frag.currentPathTitle;
		this.slidingTabsFragment = frag.slidingTabsFragment;
		this.fake = fake;
	}

	public Frag clone(final boolean fake) {
		final Frag frag = Frag.getFrag(slidingTabsFragment, type, currentPathTitle);
		frag.clone(this, fake);
		return frag;
	}

	public String getTitle() {
		if (currentPathTitle != null && currentPathTitle.length() > 0) {
			return currentPathTitle.substring(currentPathTitle.lastIndexOf("/") + 1);
		} else {
			return title;
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		//Log.d(TAG, "onSaveInstanceState" + path + ", " + outState);
		super.onSaveInstanceState(outState);
		outState.putString(ExplorerActivity.EXTRA_ABSOLUTE_PATH, currentPathTitle);
		outState.putString("title", title);
	}

	@Override
	public boolean onTouch(final View p1, final MotionEvent p2) {
		//Log.d(TAG, "onTouch " + p1 + ", " + p2);
		if (activity != null) {
			select(true);
		}
		return false;
	}

	public boolean select(boolean sel) {
		ViewGroup sortBarLayoutOther;
		if (sel) {
			if (sortBarLayout != null) {
				sortBarLayout.setBackgroundColor(ExplorerActivity.IN_DATA_SOURCE_2);
			}
			if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
				activity.dir = activity.curContentFrag.currentPathTitle;
				if (activity.slideFrag2 != null && (sortBarLayoutOther = activity.slideFrag2.getCurrentFragment().sortBarLayout) != null) {
					sortBarLayoutOther.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
				}
				activity.slideFrag1Selected = sel;
			} else {
				activity.dir = activity.curExplorerFrag.currentPathTitle;
				if ((sortBarLayoutOther = activity.slideFrag.getCurrentFragment().sortBarLayout) != null) {
					sortBarLayoutOther.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
				}
				activity.slideFrag1Selected = !sel;
			}
		} else {
			if (sortBarLayout != null) {
				sortBarLayout.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
			}
			if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
				activity.dir = activity.curExplorerFrag.currentPathTitle;
				if ((sortBarLayoutOther = activity.slideFrag2.getCurrentFragment().sortBarLayout) != null) {
					sortBarLayoutOther.setBackgroundColor(ExplorerActivity.IN_DATA_SOURCE_2);
				}
				activity.slideFrag1Selected = sel;
			} else {
				activity.dir = activity.curContentFrag.currentPathTitle;
				if ((sortBarLayoutOther = activity.slideFrag.getCurrentFragment().sortBarLayout) != null) {
					sortBarLayoutOther.setBackgroundColor(ExplorerActivity.IN_DATA_SOURCE_2);
				}
				activity.slideFrag1Selected = !sel;
			}
		}
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if ((fragActivity = getActivity()) instanceof ExplorerActivity) {
			activity = (ExplorerActivity)fragActivity;
		}
		sharedPref = PreferenceManager.getDefaultSharedPreferences(fragActivity);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		//Log.d(TAG, "onViewCreated " + savedInstanceState);
		super.onViewCreated(view, savedInstanceState);
		if ((fragActivity = getActivity()) instanceof ExplorerActivity) {
			activity = (ExplorerActivity)fragActivity;
		}

		setRetainInstance(true);
		AndroidUtils.setOnTouchListener(view, this);
		final Bundle args = getArguments();
		if ((currentPathTitle == null || currentPathTitle.length() == 0) && args != null) {
			title = args.getString("title");
			currentPathTitle = args.getString(ExplorerActivity.EXTRA_ABSOLUTE_PATH);
		}
		if (savedInstanceState != null) {
			title = savedInstanceState.getString("title");
			currentPathTitle = savedInstanceState.getString(ExplorerActivity.EXTRA_ABSOLUTE_PATH);
		}
        sortBarLayout = (ViewGroup)view.findViewById(R.id.sortBarLayout);
		
		if (activity != null && slidingTabsFragment != null) {
			if (slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {//type == Frag.TYPE.EXPLORER && 
				slidingTabsFragment.width = activity.balance;
			} else {
				slidingTabsFragment.width = -activity.balance;
			}
		}
	}

	@Override
	public void onLowMemory() {
		Log.d(TAG, "onLowMemory " + Runtime.getRuntime().freeMemory());
		super.onLowMemory();
		Glide.get(getContext()).clearMemory();
	}

	@Override
	public void onStart() {
		//Log.d(TAG, "onStart");
		if ((fragActivity = getActivity()) instanceof ExplorerActivity) {
			activity = (ExplorerActivity)fragActivity;
		}
		super.onStart();
	}

	@Override
    public void onResume() {
        //Log.d(TAG, "onResume " + title);
		super.onResume();
		if ((fragActivity = getActivity()) instanceof ExplorerActivity) {
			activity = (ExplorerActivity)fragActivity;
		}
        //startFileObserver();
        //fixIcons(false);
    }

    protected void showToast(final String message) {
        if (this.toast == null) {
            // Create toast if found null, it would he the case of first call only
            this.toast = Toast.makeText(fragActivity, message, Toast.LENGTH_SHORT);
        } else if (this.toast.getView() == null) {
            // Toast not showing, so create new one
            this.toast = Toast.makeText(fragActivity, message, Toast.LENGTH_SHORT);
        } else {
			this.toast.cancel();
            // Updating toast message is showing
            this.toast.setText(message);
        }
        // Showing toast finally
        this.toast.show();
    }

	@Override
	public void onAttach(final Activity activity) {
		//Log.d(TAG, "onAttach " + title);
		super.onAttach(activity);
		this.fragActivity = (FragmentActivity) activity;
		if (fragActivity instanceof ExplorerActivity) {
			this.activity = (ExplorerActivity)fragActivity;
		}
	}

	@Override
	public void onDetach() {
		//Log.d(TAG, "onDetach " + title);
		super.onDetach();
		this.fragActivity = null;
		//this.activity = null;
	}

	
}
