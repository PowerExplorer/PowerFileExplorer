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
import net.gnu.util.Util;
import net.gnu.explorer.ExplorerActivity;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

public class SplitMergeFragment extends DialogFragment implements Serializable, View.OnClickListener {
	
	private static final long serialVersionUID = 994806168350285521L;
	String files = "";
	String saveTo = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
	String parts = "2";
	String partSize = "0";
	boolean byChar = false;
	boolean byWord = false;
	
    private transient Button mBtnConfirm;
    private transient Button mBtnCancel;
    private transient Button filesBtn;
    private transient Button saveToBtn;
    transient EditText fileET;
    transient EditText saveToET;
    transient EditText partsET;
    transient EditText partSizeET;
	transient CheckBox byCharCB;
	transient CheckBox byWordCB;
	transient ExplorerActivity activity;
	

    @Override
    public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.e("SplitMergeFragment", "onSaveInstanceState");
        if (outState == null) {
            return;
        }
        super.onSaveInstanceState(outState);
		
		outState.putString("Files", fileET.getText() + "");
		outState.putString("SaveTo", saveToET.getText() + "");
		outState.putString("Parts", partsET.getText() + "");
		outState.putString("PartSize", partSizeET.getText() + "");
		outState.putBoolean("byChar", byCharCB.isChecked());
		outState.putBoolean("byWord", byWordCB.isChecked());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.e("SplitMergeFragment", "onCreate");
//        if (getArguments() == null) {
//            throw new IllegalArgumentException(
//                    "You must create DirectoryChooserFragment via newInstance().");
//        } else {
////            files = getArguments().getString("Files");
////            saveTo = getArguments().getString("SaveTo");
////			fileET.setText(getArguments().getString("Files"));
////			saveToET.setText(getArguments().getString("SaveTo"));
//        }

        if (savedInstanceState != null && fileET != null) {
//            files = savedInstanceState.getString("Files");
//			saveTo = savedInstanceState.getString("SaveTo");
//			parts = savedInstanceState.getLong("Parts");
//			partSize = savedInstanceState.getLong("PartSize");
			fileET.setText(savedInstanceState.getString("Files"));
			saveToET.setText(savedInstanceState.getString("SaveTo"));
			partsET.setText(savedInstanceState.getString("Parts"));
			partSizeET.setText(savedInstanceState.getString("PartSize"));
			byCharCB.setChecked(savedInstanceState.getBoolean("byChar"));
			byWordCB.setChecked(savedInstanceState.getBoolean("byWord"));
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
        final View view = inflater.inflate(R.layout.split_merge, container, false);

		Log.e("SplitMergeFragment", "onCreateView");
        mBtnConfirm = (Button) view.findViewById(R.id.okDir);
        mBtnCancel = (Button) view.findViewById(R.id.cancelDir);
        fileET = (EditText) view.findViewById(R.id.files);
        saveToET = (EditText) view.findViewById(R.id.saveTo);
        partsET = (EditText) view.findViewById(R.id.parts);
        partSizeET = (EditText) view.findViewById(R.id.partSize);
		filesBtn = (Button) view.findViewById(R.id.filesBtn);
        saveToBtn = (Button) view.findViewById(R.id.saveToBtn);
		byCharCB = (CheckBox) view.findViewById(R.id.asChar);
		byWordCB = (CheckBox) view.findViewById(R.id.asWord);
		byWordCB.setVisibility(View.GONE);
		//if (savedInstanceState != null) {
//            files = savedInstanceState.getString("Files");
//			saveTo = savedInstanceState.getString("SaveTo");
//			parts = savedInstanceState.getLong("Parts");
//			partSize = savedInstanceState.getLong("PartSize");
			restore();
		Log.i("SplitMergeFragment files1", files + ".");
		if (files != null && files.length() > 0) {
			filesBtn.setOnClickListener(new GetFileListener(this, ExplorerActivity.ACTION_MULTI_SELECT, ExplorerActivity.ALL_SUFFIX_TITLE, 
															ExplorerActivity.ALL_SUFFIX, 
															"*/*",
															fileET, 
															ExplorerActivity.FILES_REQUEST_CODE, 
															ExplorerActivity.MULTI_FILES));
		}
        //}

        mBtnConfirm.setOnClickListener(this);

        mBtnCancel.setOnClickListener(this);

		saveToBtn.setOnClickListener(new GetFileListener(this, ExplorerActivity.ACTION_MULTI_SELECT, "File/Folder", 
														 ExplorerActivity.ZIP_SUFFIX, 
														 "",
														 fileET, 
														 ExplorerActivity.SAVETO_REQUEST_CODE, 
														 !ExplorerActivity.MULTI_FILES));
		
        return view;
    }

	void save() {
		files = fileET.getText().toString();
		saveTo = saveToET.getText().toString();
		parts = partsET.getText().toString();
		partSize = partSizeET.getText().toString();
		byChar = byCharCB.isChecked();
		byWord = byWordCB.isChecked();
	}
	
	private void restore() {
		fileET.setText(files);//savedInstanceState.getString("Files"));
		saveToET.setText(saveTo);//savedInstanceState.getString("SaveTo"));
		partsET.setText(parts);//savedInstanceState.getString("Parts") + "");
		partSizeET.setText(partSize);//savedInstanceState.getString("PartSize") + "");
		byCharCB.setChecked(byChar);
		byWordCB.setChecked(byWord);
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
		Log.e("SplitMergeFragment", "onAttach");
        this.activity = (ExplorerActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
		Log.e("SplitMergeFragment", "onDetach");
        activity = null;
    }

	@Override
	public void onClick(final View p1) {
		switch (p1.getId()) {
			case R.id.cancelDir:
				dismiss();
				break;
			case R.id.okDir:
				save();
				String p = partsET.getText().toString();
				String s = partSizeET.getText().toString();
				if (p.length() == 0 && s.length() == 0 || "0".equals(p) && "0".equals(s) || "1".equals(p)) {
					Toast.makeText(activity, "Invalid number", Toast.LENGTH_SHORT).show();
					return;
				}
				SplitMergeTask ms = new SplitMergeTask(activity, 
													   Util.stringToList(fileET.getText() + "", "|"), 
													   saveToET.getText() + "", 
													   Util.toNumberWithDefault(Util.toNumberWithDefault(p, "0"), 0), Util.toNumberWithDefault(Util.toNumberWithDefault(s, "0"), 0),
													   byCharCB.isChecked(),
													   byWordCB.isChecked());
				ms.execute();
				break;
		}
	}
	
}


