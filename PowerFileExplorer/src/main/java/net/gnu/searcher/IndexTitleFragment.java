package net.gnu.searcher;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import java.io.Serializable;
import net.gnu.explorer.R;
import net.gnu.p7zip.GetFileListener;
import net.gnu.explorer.ExplorerActivity;
import android.support.v4.app.DialogFragment;
import java.io.File;
import net.gnu.androidutil.ForegroundService;
import net.gnu.androidutil.AndroidUtils;
import android.widget.Toast;
import net.gnu.common.*;

public class IndexTitleFragment extends DialogFragment implements Serializable, View.OnClickListener {

	private static final long serialVersionUID = -9202349178126229769L;
	public String files = "";
	String pattern = ".*?\\.([sdx]?htm[l]?)";
	boolean useFolderName = false;
	
    transient Button mBtnConfirm;
    private transient Button mBtnCancel;
    private transient Button filesBtn;
    transient EditText fileET;
    transient EditText patternET;
	transient CheckBox useFolderNameCB;
	private transient final String TAG = "IndexTitleFragment";
	private transient ExplorerActivity activity;
	private transient IndexTitleTask indexTitleTask;
	
    @Override
    public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.e(TAG, "onSaveInstanceState");
        if (outState == null) {
            return;
        }
        
		outState.putString("Files", fileET.getText() + "");
		outState.putString("pattern", patternET.getText() + "");
		outState.putString("useFolderName", useFolderNameCB.getText() + "");
		
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate");

        if (savedInstanceState != null && fileET != null) {
//            files = savedInstanceState.getString("Files");
//			pattern = savedInstanceState.getString("pattern");

			fileET.setText(savedInstanceState.getString("Files"));
			patternET.setText(savedInstanceState.getString("pattern"));
			useFolderNameCB.setText(savedInstanceState.getString("useFolderName"));
	    }

        if (this.getShowsDialog()) {
            setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        } else {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
        		View view  = inflater.inflate(R.layout.index_title_dialog, container, false);
		Log.e(TAG, "onCreateView");
        mBtnConfirm = (Button) view.findViewById(R.id.okDir);
        mBtnCancel = (Button) view.findViewById(R.id.cancelDir);
        fileET = (EditText) view.findViewById(R.id.files);
        patternET = (EditText) view.findViewById(R.id.pattern);
		useFolderNameCB = (CheckBox) view.findViewById(R.id.useFolderName);
		filesBtn = (Button) view.findViewById(R.id.filesBtn);

		restore();
		
		Log.i("IndexTitleFragment files1", files + ".");
		if (files != null && files.length() > 0) {
			filesBtn.setOnClickListener(new GetFileListener(this, Constants.ACTION_MULTI_SELECT, "File/Folder", 
															Constants.ZIP_SUFFIX, 
															"",
															fileET, 
															Constants.SAVETO_REQUEST_CODE, 
															!Constants.MULTI_FILES));
		} 
        mBtnConfirm.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);

        return view;
    }

	void save() {
		files = fileET.getText().toString();
		pattern = patternET.getText().toString();
		useFolderName = useFolderNameCB.isChecked();
	}
	
	private void restore() {
		fileET.setText(files);//savedInstanceState.getString("Files"));
		patternET.setText(pattern);//savedInstanceState.getString("pattern"));
		useFolderNameCB.setChecked(useFolderName);
	}

	@Override
	public void onClick(final View p1) {
		switch (p1.getId()) {
			case R.id.cancelDir:
				dismiss();
				break;
			case R.id.okDir:

				Log.d("COMPRESS_REQUEST_CODE.selectedFiles", files + ", " + files);// + fName);

				save();
				if (files.length() == 0 || !new File(files).exists()) {
					Toast.makeText(activity, "Invalid \"files\"", Toast.LENGTH_SHORT).show();
					return;
				}
				ForegroundService.ticker = "Indexing Title";
				ForegroundService.title = "Touch to Open";
				ForegroundService.text = "Indexing";
				AndroidUtils.startService(activity, ForegroundService.class, ForegroundService.ACTION_FOREGROUND, TAG);
				indexTitleTask = new IndexTitleTask(this);
				indexTitleTask.execute();
				break;
		}
	}

	@Override
	public void onAttach(final Activity activity) {
		//Log.d(TAG, "onAttach");
		super.onAttach(activity);
		this.activity = (ExplorerActivity) activity;
	}

	@Override
	public void onDetach() {
		//Log.d(TAG, "onDetach");
		super.onDetach();
		this.activity = null;
	}
	
}


