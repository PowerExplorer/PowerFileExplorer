package net.gnu.searcher;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import net.gnu.explorer.ExplorerActivity;
import net.gnu.explorer.ExplorerApplication;
import net.gnu.explorer.R;
import net.gnu.p7zip.DecompressFragment;
import net.gnu.p7zip.GetFileListener;
import net.gnu.util.FileUtil;
import net.gnu.util.Util;
import net.gnu.common.*;

/**
 * Activities that contain this fragment must implement the
 * {@link net.rdrei.android.dirchooser.DirectoryChooserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link net.rdrei.android.dirchooser.DirectoryChooserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BatchFragment extends DialogFragment implements Serializable, View.OnClickListener {

	private static final long serialVersionUID = 1455789504140020365L;
	private static final String TAG = "BatchFragment";

	public String files = "";
	public String saveTo = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
	String include;
    String exclude;
    long sizeFrom = -1;
	long sizeTo = -1;
	String dateFrom;
	String dateTo;
	boolean bySize;
	boolean byDate;
	boolean byName;
	String patternTo;
	String patternFrom;
	transient String command;

    private transient Button mBtnConfirm;
    private transient Button mBtnCancel;
	private transient Button mBtnDelete;
	private transient Button filesBtn;
    private transient Button saveToBtn;

    transient EditText fileET;
	private transient EditText saveToET;
    private transient EditText includeET;
    private transient EditText excludeET;
    private transient EditText sizeFromET;
	private transient EditText sizeToET;
	private transient EditText dateFromET;
	private transient EditText dateToET;
	private transient EditText patternFromET;
	private transient EditText patternToET;
	private transient CheckBox bySizeCB;
	private transient CheckBox byDateCB;
	private transient CheckBox byNameCB;
	private transient Activity activity;


	public static BatchFragment newInstance() {

		File fi = new File(ExplorerApplication.DATA_DIR + BatchFragment.class.getSimpleName() + ".ser");
		Log.d(TAG, fi.getAbsolutePath() + ", exist " + fi.exists() + ", length " + fi.length());
		BatchFragment batchFragment = null;
		if (fi.exists() && fi.length() > 0) {
			try	{
				FileInputStream fis = new FileInputStream(fi);
				BufferedInputStream bis = new BufferedInputStream(fis);
				ObjectInputStream ois = new ObjectInputStream(bis);
				batchFragment = (BatchFragment) ois.readObject();
				FileUtil.close(ois, bis, fis);

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			batchFragment = new BatchFragment();
		}
		Log.d(TAG, "newInstance " + batchFragment);
		return batchFragment;
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.e(TAG, "onSaveInstanceState: " + outState);
        if (outState == null) {
            return;
        }

		outState.putString("fileET", fileET.getText() + "");
		outState.putString("saveToET", saveToET.getText() + "");
		outState.putString("includeET", includeET.getText() + "");
		outState.putString("excludeET", excludeET.getText() + "");
		outState.putString("sizeFromET", sizeFromET.getText() + "");
		outState.putString("sizeToET", sizeToET.getText() + "");
		outState.putString("dateFromET", dateFromET.getText() + "");
		outState.putString("dateToET", dateToET.getText() + "");
		outState.putString("patternFromET", patternFromET.getText() + "");
		outState.putString("patternToET", patternToET.getText() + "");
		outState.putBoolean("bySizeCB", bySizeCB.isChecked());
		outState.putBoolean("byDateCB", byDateCB.isChecked());
		outState.putBoolean("byNameCB", byNameCB.isChecked());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate");

        if (savedInstanceState != null && fileET != null) {
			fileET.setText(savedInstanceState.getString("fileET"));
			saveToET.setText(savedInstanceState.getString("saveToET"));
			includeET.setText(savedInstanceState.getString("includeET"));
			excludeET.setText(savedInstanceState.getString("excludeET"));
			sizeFromET.setText(savedInstanceState.getString("sizeFromET"));
			sizeToET.setText(savedInstanceState.getString("sizeToET"));
			dateFromET.setText(savedInstanceState.getString("dateFromET"));
			dateToET.setText(savedInstanceState.getString("dateToET"));
			patternFromET.setText(savedInstanceState.getString("patternFromET"));
			patternToET.setText(savedInstanceState.getString("patternToET"));
			bySizeCB.setChecked(savedInstanceState.getBoolean("bySizeCB"));
			byDateCB.setChecked(savedInstanceState.getBoolean("byDateCB"));
			byNameCB.setChecked(savedInstanceState.getBoolean("byNameCB"));
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
        final View view = inflater.inflate(R.layout.batch_delete, container, false);

		Log.e(TAG, "onCreateView");
		filesBtn = (Button) view.findViewById(R.id.filesBtn);
		saveToBtn = (Button) view.findViewById(R.id.saveToBtn);
        mBtnConfirm = (Button) view.findViewById(R.id.okDir);
        mBtnCancel = (Button) view.findViewById(R.id.cancelDir);
        mBtnDelete = (Button) view.findViewById(R.id.deleteBtn);
		fileET = (EditText) view.findViewById(R.id.files);
		saveToET = (EditText) view.findViewById(R.id.saveTo);
		includeET = (EditText) view.findViewById(R.id.include);
		excludeET = (EditText) view.findViewById(R.id.exclude);
		sizeFromET = (EditText) view.findViewById(R.id.sizeFrom);
		sizeToET = (EditText) view.findViewById(R.id.sizeTo);
		dateFromET = (EditText) view.findViewById(R.id.dateFrom);
		dateToET = (EditText) view.findViewById(R.id.dateTo);
		patternToET = (EditText) view.findViewById(R.id.patternTo);
		patternFromET = (EditText) view.findViewById(R.id.patternFrom);
		bySizeCB = (CheckBox) view.findViewById(R.id.bySize);
		byDateCB = (CheckBox) view.findViewById(R.id.byDate);
		byNameCB = (CheckBox) view.findViewById(R.id.byName);

		restore();
		Log.i("BatchFragment files1", files + ".");
		filesBtn.setOnClickListener(new GetFileListener(this, Constants.ACTION_MULTI_SELECT, Constants.ALL_SUFFIX_TITLE, 
														Constants.ALL_SUFFIX, 
														"*/*",
														fileET, 
														Constants.FILES_REQUEST_CODE, 
														Constants.MULTI_FILES));

		saveToBtn.setOnClickListener(new GetFileListener(this, Constants.ACTION_MULTI_SELECT, "Output Folder", 
														 "", 
														 "",
														 saveToET, 
														 Constants.SAVETO_REQUEST_CODE, 
														 !Constants.MULTI_FILES));

        mBtnConfirm.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);
		mBtnDelete.setOnClickListener(this);

        return view;
    }

	public void restore() {
		fileET.setText(files);
		saveToET.setText(saveTo);
		includeET.setText(include);
		excludeET.setText(exclude);
		sizeFromET.setText(sizeFrom == -1 ? "" : sizeFrom + "");
        sizeToET.setText(sizeTo == -1 ? "" : sizeTo + "");
		dateFromET.setText(dateFrom);
        dateToET.setText(dateTo);
		patternFromET.setText(patternFrom);
		patternToET.setText(patternTo);
		byNameCB.setChecked(byName);
		bySizeCB.setChecked(bySize);
		byDateCB.setChecked(byDate);
	}

	public void save() {
		files = fileET.getText().toString();
		saveTo = saveToET.getText().toString();
		include = includeET.getText().toString().trim();
		exclude = excludeET.getText().toString().trim();
		sizeFrom = Long.parseLong(sizeFromET.getText().length() > 0 ? sizeFromET.getText().toString() : "-1");
        sizeTo = Long.parseLong(sizeToET.getText().length() > 0 ? sizeToET.getText().toString() : "-1");
		dateFrom = dateFromET.getText().toString();
        dateTo = dateToET.getText().toString();
		patternFrom = patternFromET.getText().toString();
		patternTo = patternToET.getText().toString();
		byName = byNameCB.isChecked();
		bySize = bySizeCB.isChecked();
		byDate = byDateCB.isChecked();
	}

	@Override
	public void onClick(final View p1) {
		switch (p1.getId()) {
			case R.id.cancelDir:
				dismiss();
				break;
			case R.id.okDir:
				Log.d(TAG, files + ".");

				save();
				try {
					if (dateFrom.length() > 0) {
						Calendar from = Calendar.getInstance();
						String[] fr = dateFrom.split("\\s*[/-]\\s*");
						from.set(Integer.parseInt(fr[2]), Integer.parseInt(fr[1]), Integer.parseInt(fr[0]), 0, 0, 0);
					}
				} catch (RuntimeException e) {
					Toast.makeText(activity, "Invalid \"Date from\" value", Toast.LENGTH_SHORT).show();
				}
				try {
					if (dateTo.length() > 0) {
						Calendar toCal = Calendar.getInstance();
						String[] to = dateTo.split("\\s*[/-]\\s*");
						toCal.set(Integer.parseInt(to[2]), Integer.parseInt(to[1]), Integer.parseInt(to[0]), 0, 0, 0);
					}
				} catch (RuntimeException e) {
					Toast.makeText(activity, "Invalid \"Date to\" value", Toast.LENGTH_SHORT).show();
				}
				if (files.length() > 0) {

					String[] stringExtra = Util.stringToArray(fileET.getText().toString(), "|");
					Collection<File> files = FileUtil.getFiles(stringExtra, false);
					final BatchTask batchTask = new BatchTask(this, fileET.getText().toString());

					String choice = "";
					if (!"delete".equals(command)) {
						if ("".equals(saveTo) && (!"".equals(patternFrom) || !"".equals(patternTo))) {
							choice = "Are you sure to rename these files?";
						} else if (!"".equals(saveTo) && (!"".equals(patternFrom) || !"".equals(patternTo))) {
							choice = "Are you sure to rename and move these files?";
						} else if (!"".equals(saveTo) && "".equals(patternFrom) && "".equals(patternTo)) {
							choice = "Are you sure to move these files?";
						}
					} else { // if ("".equals(batchFrag.saveTo) && "".equals(batchFrag.patternFrom) && "".equals(batchFrag.patternTo)) 
						choice = "Are you sure to delete these files?";
					}
					batchTask.newFileList3 = batchTask.filteringConditions(files);

					AlertDialog dialog = new AlertDialog.Builder(activity)
						.setIconAttribute(android.R.attr.alertDialogIcon)
						.setTitle(choice)
						//.setView(new TextView(activity, c)
						.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								batchTask.execute();
								dialog.dismiss();
							}
						})
						.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								dialog.dismiss();
							}
						}).create();
					dialog.show();
				} else {
					Toast.makeText(activity, "There is no job to do", Toast.LENGTH_SHORT).show();
				}
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


