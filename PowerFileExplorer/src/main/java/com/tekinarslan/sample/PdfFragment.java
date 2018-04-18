package com.tekinarslan.sample;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;
import com.artifex.mupdfdemo.SearchTask;
import com.artifex.mupdfdemo.SearchTaskResult;
import net.gnu.explorer.R;
import net.gnu.explorer.*;

public class PdfFragment extends Frag {

	private RelativeLayout mainLayout;
    private MuPDFCore core;
    private MuPDFReaderView mDocView;
    private Context mContext;
    //public String mFilePath;
    Bundle args = new Bundle();
    private static final String TAG = "PdfFragment";
    private SearchTask mSearchTask;
	
    public PdfFragment() {
		super();
		type = Frag.TYPE.PDF;
		title = "Pdf";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        View rootView = inflater.inflate(R.layout.pdf, container, false);
        mainLayout = (RelativeLayout) rootView.findViewById(R.id.pdflayout);

        load2(savedInstanceState);
		
        mSearchTask = new SearchTask(mContext, core) {

            @Override
            protected void onTextFound(SearchTaskResult result) {
                SearchTaskResult.set(result);
                mDocView.setDisplayedViewIndex(result.pageNumber);
                mDocView.resetupChildren();
            }
        };
//		if (args != null) {
//			title = args.getString("title");
//			path = args.getString("path");
//		}
//		if (savedInstanceState != null) {
//			title = savedInstanceState.getString("title");
//			path = savedInstanceState.getString("path");
//		}
		updateColor(rootView);
		Log.d(TAG, "path " + currentPathTitle);
		load(currentPathTitle);
        return rootView;
    }
	
	@Override
	public void load(String path) {
		this.currentPathTitle = path;
		load2(null);
	}

	public void load2(Bundle savedInstanceState) {
		args = this.getArguments();
        if (savedInstanceState != null) {
			title = savedInstanceState.getString("title");
			currentPathTitle = savedInstanceState.getString(ExplorerActivity.EXTRA_ABSOLUTE_PATH);
		} else if (args != null) {
			title = args.getString("title");
			if (args.getString(ExplorerActivity.EXTRA_ABSOLUTE_PATH) != null) {
				currentPathTitle = args.getString(ExplorerActivity.EXTRA_ABSOLUTE_PATH);
			}
		}
		Log.d(TAG, "load " + currentPathTitle);
		if (currentPathTitle != null && currentPathTitle.length() > 0) {
			core = openFile(Uri.decode(currentPathTitle));

			if (core != null && core.countPages() == 0) {
				core = null;
			}
			if (core == null || core.countPages() == 0 || core.countPages() == -1) {
				Log.e(TAG, "Document Not Opening");
			}
			if (core != null) {
				mDocView = new MuPDFReaderView(getActivity()) {
					@Override
					protected void onMoveToChild(int i) {
						if (core == null)
							return;
						super.onMoveToChild(i);
					}

				};

				mDocView.setAdapter(new MuPDFPageAdapter(mContext, core));
				mainLayout.removeAllViews();
				mainLayout.addView(mDocView);
			}
		}
		
	}

	public void updateColor(View rootView) {
		if (rootView == null) {
			getView().setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
		} else {
			rootView.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}

    public void search(int direction, String text) {
        int displayPage = mDocView.getDisplayedViewIndex();
        SearchTaskResult r = SearchTaskResult.get();
        int searchPage = r != null ? r.pageNumber : -1;
        mSearchTask.go(text, direction, displayPage, searchPage);
    }

    private MuPDFCore openBuffer(byte[] buffer) {
        System.out.println("Trying to open byte buffer");
        try {
            core = new MuPDFCore(mContext, buffer);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
        return core;
    }

    private MuPDFCore openFile(String path) {
        int lastSlashPos = path.lastIndexOf('/');
        currentPathTitle = new String(lastSlashPos == -1
                ? path
                : path.substring(lastSlashPos + 1));
		Log.d(TAG, "openFile " + currentPathTitle);
        try {
            core = new MuPDFCore(mContext, path);
            // New file: drop the old outline data
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
        return core;
    }

    public void onDestroy() {
        if (core != null)
            core.onDestroy();
        core = null;
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSearchTask != null)
            mSearchTask.stop();
    }
	
	@Override
	public void onSaveInstanceState(android.os.Bundle outState) {
		outState.putString(ExplorerActivity.EXTRA_ABSOLUTE_PATH, currentPathTitle);
		outState.putString("title", title);
		Log.d(TAG, "onSaveInstanceState " + currentPathTitle + ", " + outState);
		super.onSaveInstanceState(outState);
	}
}


