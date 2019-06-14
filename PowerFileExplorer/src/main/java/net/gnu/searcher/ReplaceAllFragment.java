package net.gnu.searcher;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import java.io.Serializable;
import net.gnu.explorer.R;
import net.gnu.p7zip.GetFileListener;
import net.gnu.util.Util;
import net.gnu.explorer.ExplorerActivity;
import android.support.v4.app.DialogFragment;
import java.io.File;
import net.gnu.androidutil.ForegroundService;
import net.gnu.androidutil.AndroidUtils;
import android.widget.Toast;
import java.util.List;
import net.gnu.util.FileUtil;
import java.util.regex.Pattern;
import net.gnu.common.*;

public class ReplaceAllFragment extends DialogFragment implements  Serializable, View.OnClickListener {

	private static final long serialVersionUID = -5465687454817950658L;
	public String files = "";
	public String saveTo = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
	public String stardict = "";
	String replace = "";
	String include = ".*?\\.([dxs]?htm[l]?|txt|java|c|cpp|h|hpp|xml|md|lua|sh|bat|list|depend|js|jsp|mk|config|configure|machine|asm|css|desktop|inc|i|plist|pro|py|s)";
	String exclude = ".*?\\.(jp[e]?g|png|gif|bmp|psd|doc[x]?|xls[x]?|ppt[x]?|odt|ods|odp|pps|rtf|avi|mpe?g|mp3|mp4|ogg|apk|zip|7z|bz2|gz|tar|wim|xz|chm|fb2)";
	String by = "";
	boolean isRegex = false;
	boolean caseSensitive = false;
	boolean includeEnter = false;
	boolean backup = false;

    private transient Button mBtnConfirm;
    private transient Button mBtnCancel;
    private transient Button filesBtn;
    private transient Button saveToBtn;
	private transient Button stardictBtn;
    transient EditText fileET;
    transient EditText saveToET;
    transient EditText includeET;
    transient EditText excludeET;
	transient EditText stardictET;
	transient EditText replaceET;
    transient EditText byET;
    transient CheckBox isRegexCB;
    transient CheckBox caseSensitiveCB;
	transient CheckBox includeEnterCB;
	transient CheckBox backupCB;
	transient TextView statusTV;
	transient ExplorerActivity activity;
	transient ReplaceAllTask replaceAllTask = new ReplaceAllTask(null, null, null, null);
	
    @Override
    public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.e("ReplaceAllFragment", "onSaveInstanceState");
        if (outState == null) {
            return;
        }
        super.onSaveInstanceState(outState);

		outState.putString("statusTV", statusTV.getText() + "");
		outState.putString("Files", fileET.getText() + "");
		outState.putString("SaveTo", saveToET.getText() + "");
		outState.putString("include", includeET.getText() + "");
		outState.putString("exclude", excludeET.getText() + "");
		outState.putString("stardict", stardictET.getText() + "");
		outState.putBoolean("isRegex", isRegex);
		outState.putBoolean("caseSensitive", caseSensitive);
		outState.putBoolean("includeEnter", includeEnter);
		outState.putBoolean("backup", backup);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.e("ReplaceAllFragment", "onCreate");


        if (savedInstanceState != null && fileET != null) {
//            files = savedInstanceState.getString("Files");
//			saveTo = savedInstanceState.getString("SaveTo");

			fileET.setText(savedInstanceState.getString("Files"));
			saveToET.setText(savedInstanceState.getString("SaveTo"));
			includeET.setText(savedInstanceState.getString("include"));
			excludeET.setText(savedInstanceState.getString("exclude"));
			stardictET.setText(savedInstanceState.getString("stardict"));
			isRegexCB.setChecked(savedInstanceState.getBoolean("isRegex", false));
			caseSensitiveCB.setChecked(savedInstanceState.getBoolean("caseSensitive", false));
			includeEnterCB.setChecked(savedInstanceState.getBoolean("includeEnter", false));
			backupCB.setChecked(savedInstanceState.getBoolean("backup", false));
			statusTV.setText(savedInstanceState.getString("statusTV"));
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
        assert getActivity() != null;
		Point op = new Point();
		getActivity().getWindowManager().getDefaultDisplay().getSize(op);
//		Log.d("getDefaultDisplay", getWindowManager().getDefaultDisplay() + ".");
//		Log.d("op", op + ".");
//		Log.d("getComponentName", getComponentName().toString());
//		Log.d("getRequestedOrientation()", getRequestedOrientation() + ".");
		View view;
		if (op.x < op.y) {
			view = inflater.inflate(R.layout.replace_portrait, container, false);
		} else {
			view = inflater.inflate(R.layout.replace_dialog, container, false);
		}

		Log.e("ReplaceAllFragment", "onCreateView");
        mBtnConfirm = (Button) view.findViewById(R.id.okDir);
        mBtnCancel = (Button) view.findViewById(R.id.cancelDir);
        fileET = (EditText) view.findViewById(R.id.files);
        saveToET = (EditText) view.findViewById(R.id.saveTo);
        includeET = (EditText) view.findViewById(R.id.include);
        excludeET = (EditText) view.findViewById(R.id.exclude);
		stardictET = (EditText) view.findViewById(R.id.stardict);
		replaceET = (EditText) view.findViewById(R.id.replace);
        byET = (EditText) view.findViewById(R.id.by);
        isRegexCB = (CheckBox) view.findViewById(R.id.regex);
        caseSensitiveCB = (CheckBox) view.findViewById(R.id.caseSensitive);
		includeEnterCB = (CheckBox) view.findViewById(R.id.includeEnter);
		backupCB = (CheckBox) view.findViewById(R.id.backup);
		filesBtn = (Button) view.findViewById(R.id.filesBtn);
        saveToBtn = (Button) view.findViewById(R.id.saveToBtn);
		stardictBtn = (Button) view.findViewById(R.id.stardictBtn);
		statusTV = (TextView) view.findViewById(R.id.status);

		restore();

		Log.i("ReplaceAllFragment files1", files + ".");
		filesBtn.setOnClickListener(new GetFileListener(this, Constants.ACTION_MULTI_SELECT, Constants.ALL_SUFFIX_TITLE, 
															Constants.ALL_SUFFIX, 
															"*/*",
															fileET, 
														Constants.FILES_REQUEST_CODE, 
														Constants.MULTI_FILES));

        mBtnConfirm.setOnClickListener(this);

        mBtnCancel.setOnClickListener(this);

		saveToBtn.setOnClickListener(new GetFileListener(this, Constants.ACTION_MULTI_SELECT, "Otput Folder", 
														 "", 
														 "",
														 saveToET, 
														 Constants.SAVETO_REQUEST_CODE, 
														 !Constants.MULTI_FILES));

		stardictBtn.setOnClickListener(new GetFileListener(this, Constants.ACTION_MULTI_SELECT, "txt file", 
														   Constants.TXT_SUFFIX, 
														   "text/txt",
														   stardictET, 
														   Constants.STARDICT_REQUEST_CODE, 
														   !Constants.MULTI_FILES));

        return view;
    }

	private void restore() {
		fileET.setText(files);//savedInstanceState.getString("Files"));
		saveToET.setText(saveTo);//savedInstanceState.getString("SaveTo"));
		stardictET.setText(stardict);
		replaceET.setText(replace);
		includeET.setText(include);
		excludeET.setText(exclude);
		byET.setText(by);
		isRegexCB.setChecked(isRegex);
		caseSensitiveCB.setChecked(caseSensitive);
		includeEnterCB.setChecked(includeEnter);
		backupCB.setChecked(backup);
	}

	void save() {
		files = fileET.getText().toString();
		saveTo = saveToET.getText().toString();
		include = includeET.getText().toString();
		exclude = excludeET.getText().toString();
		stardict = stardictET.getText().toString();
		replace = replaceET.getText().toString();
		by = byET.getText().toString();
		isRegex = isRegexCB.isChecked();
		caseSensitive = caseSensitiveCB.isChecked();
		includeEnter = includeEnterCB.isChecked();
		backup = backupCB.isChecked();
	}

	@Override
	public void onClick(final View p1) {
		switch (p1.getId()) {
			case R.id.cancelDir:
				dismiss();
				break;
			case R.id.okDir:

				String[] stringExtra = Util.stringToArray(fileET.getText() + "", "|");
				Log.d("REPLACE_REQUEST_CODE.selectedFiles", stringExtra[0] + ".");
				save();
				if (saveTo.length() > 0 && !new File(saveTo).exists()) {
					Toast.makeText(activity, "Invalid \"Save to\" folder", Toast.LENGTH_SHORT).show();
					return;
				}
				if (stardict.length() > 0 && !new File(stardict).exists()) {
					Toast.makeText(activity, "Invalid \"Stardict text\" folder", Toast.LENGTH_SHORT).show();
					return;
				}
				List<File> lf = FileUtil.getFiles(stringExtra, 
												  Pattern.compile(include, Pattern.CASE_INSENSITIVE), 
												  Pattern.compile(exclude, Pattern.CASE_INSENSITIVE));
				Log.d("replace", Util.collectionToString(lf, true, "\n"));
				replaceAllTask.cancel(true);
				if (includeEnter) { // multiline
					(replaceAllTask = new ReplaceAllTask(this, lf, null, null)).execute();//, lf, replaceFrag.saveTo, replaceFrag.stardict, replaceFrag.isRegex, replaceFrag.caseSensitive, replaceFrag.backup, new String[]{replaceFrag.replace}, new String[]{replaceFrag.by}).execute();
				} else {
					String[] replaces = replaceET.getText().toString().split("\r?\n");
					String[] bys = byET.getText().toString().split("\r?\n");
					Log.d("bys.length ", bys.length + ".");
					if (replaces.length == bys.length) {
						(replaceAllTask = new ReplaceAllTask(this, lf, replaces, bys)).execute();//, lf, replaceFrag.saveTo, replaceFrag.stardict, replaceFrag.isRegex, replaceFrag.caseSensitive, replaceFrag.backup, replaces, bys).execute();
					} else {
						Toast.makeText(activity, "The number of lines of replace and by are not equal", Toast.LENGTH_SHORT).show();
					}
				}
				break;
		}
	}
	
    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
		Log.e("ReplaceAllFragment", "onAttach");
        this.activity = (ExplorerActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
		Log.e("ReplaceAllFragment", "onDetach");
        activity = null;
    }

}


