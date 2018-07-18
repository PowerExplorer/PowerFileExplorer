package net.gnu.searcher;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import java.io.Serializable;
import net.gnu.explorer.R;
import net.gnu.p7zip.GetFileListener;
import net.gnu.util.Util;
import net.gnu.explorer.ExplorerActivity;
import android.support.v4.app.DialogFragment;
import java.io.File;
import android.widget.Toast;
import java.util.List;
import net.gnu.util.FileUtil;
import java.util.regex.Pattern;
import java.util.Collection;

public class WordListFragment extends DialogFragment implements  Serializable, View.OnClickListener {

	private static final long serialVersionUID = 1174954124107005098L;
	public String files = "";
	public String saveTo = "";
	public String stardict = "";
	
    private transient Button mBtnConfirm;
    private transient Button mBtnCancel;
    private transient Button filesBtn;
    private transient Button saveToBtn;
	private transient Button stardictBtn;
    transient EditText fileET;
    transient EditText saveToET;
	transient EditText stardictET;
	private transient final String TAG = "WordListFragment";
	private transient Activity activity;
	private transient WordListTask wordListTask = new WordListTask(null, null, "", "");
	
    @Override
    public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.e(TAG, "onSaveInstanceState");
        if (outState == null) {
            return;
        }
        
		outState.putString("Files", fileET.getText() + "");
		outState.putString("SaveTo", saveToET.getText() + "");
		outState.putString("stardict", stardictET.getText() + "");
		
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate");

        if (savedInstanceState != null && fileET != null) {
//            files = savedInstanceState.getString("Files");
//			saveTo = savedInstanceState.getString("SaveTo");

			fileET.setText(savedInstanceState.getString("Files"));
			saveToET.setText(savedInstanceState.getString("SaveTo"));
			stardictET.setText(savedInstanceState.getString("stardict"));
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
        		View view  = inflater.inflate(R.layout.wordlist_dialog, container, false);
		Log.e(TAG, "onCreateView");
        mBtnConfirm = (Button) view.findViewById(R.id.okDir);
        mBtnCancel = (Button) view.findViewById(R.id.cancelDir);
        fileET = (EditText) view.findViewById(R.id.files);
        saveToET = (EditText) view.findViewById(R.id.saveTo);
		stardictET = (EditText) view.findViewById(R.id.stardict);
		filesBtn = (Button) view.findViewById(R.id.filesBtn);
        saveToBtn = (Button) view.findViewById(R.id.saveToBtn);
		stardictBtn = (Button) view.findViewById(R.id.stardictBtn);

		restore();
		
		Log.i("WordListFragment files1", files + ".");
		filesBtn.setOnClickListener(new GetFileListener(this, ExplorerActivity.ACTION_MULTI_SELECT, ExplorerActivity.ALL_SUFFIX_TITLE, 
															ExplorerActivity.ALL_SUFFIX, 
															"*/*",
															fileET, 
															ExplorerActivity.FILES_REQUEST_CODE, 
															ExplorerActivity.MULTI_FILES));
		
        mBtnConfirm.setOnClickListener(this);

        mBtnCancel.setOnClickListener(this);

		saveToBtn.setOnClickListener(new GetFileListener(this, ExplorerActivity.ACTION_MULTI_SELECT, "Output Folder", 
														 "", 
														 "",
														 saveToET, 
														 ExplorerActivity.SAVETO_REQUEST_CODE, 
														 !ExplorerActivity.MULTI_FILES));

		stardictBtn.setOnClickListener(new GetFileListener(this, ExplorerActivity.ACTION_MULTI_SELECT, ExplorerActivity.IFO_SUFFIX_TITLE, 
														   ExplorerActivity.IFO_SUFFIX, 
														   "*/*",
														   stardictET, 
														   ExplorerActivity.STARDICT_REQUEST_CODE, 
														   ExplorerActivity.MULTI_FILES));
        return view;
    }

	void save() {
		files = fileET.getText().toString();
		saveTo = saveToET.getText().toString();
		stardict = stardictET.getText().toString();
	}
	
	private void restore() {
		fileET.setText(files);//savedInstanceState.getString("Files"));
		saveToET.setText(saveTo);//savedInstanceState.getString("SaveTo"));
		stardictET.setText(stardict);
	}

	@Override
	public void onClick(final View p1) {
		switch (p1.getId()) {
			case R.id.cancelDir:
				dismiss();
				break;
			case R.id.okDir:
				Log.d(TAG, files + ".");
				
				String[] stringExtra = Util.stringToArray(fileET.getText() + "", "|");
				Log.d(TAG, stringExtra[0] + ".");
				save();
				Collection<File> lf = FileUtil.getFiles(stringExtra, 
												  Pattern.compile(ExplorerActivity.TXT_SUFFIX, Pattern.CASE_INSENSITIVE), 
												  false);
				if (saveTo.length() > 0 && !new File(saveTo).exists()) {
					Toast.makeText(activity, "Invalid \"Save to\" folder", Toast.LENGTH_SHORT).show();
					return;
				}
				if (files.length() == 0) {
					Toast.makeText(activity, "Invalid \"files\"", Toast.LENGTH_SHORT).show();
					return;
				}
				Log.d(TAG, "Generating Word List...");
				Toast.makeText(activity, "Generating Word List...", Toast.LENGTH_SHORT).show();
				wordListTask = new WordListTask(activity, lf, saveTo, stardict);
				wordListTask.execute();
				break;
		}
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
		Log.e(TAG, "onAttach");
        this.activity = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
		Log.e(TAG, "onDetach");
        activity = null;
    }
}


