package net.gnu.p7zip;

import android.widget.*;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.afollestad.materialdialogs.MaterialDialog;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import net.gnu.androidutil.AndroidUtils;
import net.gnu.androidutil.ForegroundService;
import net.gnu.explorer.ExplorerActivity;
import net.gnu.explorer.ExplorerApplication;
import net.gnu.explorer.R;
import net.gnu.util.FileUtil;
import android.content.Intent;
import java.util.List;
import java.util.LinkedList;
import net.gnu.common.*;

public class DecompressFragment extends DialogFragment implements Serializable, OnItemSelectedListener, 
OnCheckedChangeListener, OnClickListener {

	private static final long serialVersionUID = 8239392959025238961L;

	private static final String TAG = "DecompressFragment";

	String[] modes = new String[] {
		"Overwrite All existing files without prompt (-aoa)",
		"Skip extracting of existing files (-aos)",
		"aUto rename extracting file (-aou)",
		"auto rename existing file (-aot)"};

	public String files = "";
	public String saveTo = "";
	public String include = "";
	String exclude = "";
	String otherArgs = "";
	transient String password = "";
	int overwriteMode = 0;
	String command = "x";

	transient Button mBtnOK;
	private transient Button mBtnCancel;
	private transient ImageButton filesBtn;
	private transient ImageButton saveToBtn;
	private transient ImageButton historyBtn;
	private transient ImageButton historySaveBtn;

	transient EditText fileET;
	transient EditText saveToET;
	transient EditText includeET;
	transient EditText excludeET;
	transient EditText otherArgsET;
	transient ShowHidePasswordEditText passwordET;
	transient Spinner overwriteModeSpinner;
	transient CheckBox extractWithFullPathsCB;

	private transient View config;
	private transient View status;
	
	transient ListView statusLV;
	static DecompressTask decompressTask;

	private transient Toast mToast;
	transient Activity activity;

	private ArrayList<String> historyList = new ArrayList<>();
	private ArrayList<String> historySaveList = new ArrayList<>();

	private boolean needStopService = false;

	ArrayAdapter<String> adapter;
	private List<String> outputList = new LinkedList<>();

	public void stopService() {
		if (activity != null) {
			activity.stopService(new Intent(activity, ForegroundService.class));
			needStopService = false;
		} else {
			needStopService = true;
		}
	}
	
	@Override
	public void onAttach(final Activity activity) {
		//Log.d(TAG, "onAttach");
		super.onAttach(activity);
		this.activity = activity;
	}

	@Override
	public void onDetach() {
		//Log.d(TAG, "onDetach");
		super.onDetach();
		this.activity = null;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate " + savedInstanceState);
		super.onCreate(savedInstanceState);

		if (this.getShowsDialog()) {
			setStyle(DialogFragment.STYLE_NO_TITLE, 0);
		} else {
			setHasOptionsMenu(true);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView " + savedInstanceState);
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.decompress, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.d(TAG, "onViewCreated " + savedInstanceState);
		super.onViewCreated(view, savedInstanceState);
		setRetainInstance(true);
		activity = getActivity();

		if (needStopService) {
			activity.stopService(new Intent(activity, ForegroundService.class));
			needStopService = false;
		}
		mBtnOK = (Button) view.findViewById(R.id.okDir);
		mBtnCancel = (Button) view.findViewById(R.id.cancelDir);
		fileET = (EditText) view.findViewById(R.id.files);
		saveToET = (EditText) view.findViewById(R.id.saveTo);
		includeET = (EditText) view.findViewById(R.id.include);
		excludeET = (EditText) view.findViewById(R.id.exclude);
		otherArgsET = (EditText) view.findViewById(R.id.otherParametersET);
		passwordET = (ShowHidePasswordEditText) view.findViewById(R.id.password);
		overwriteModeSpinner = (Spinner) view.findViewById(R.id.overwrite);
		filesBtn = (ImageButton) view.findViewById(R.id.filesBtn);
		saveToBtn = (ImageButton) view.findViewById(R.id.saveToBtn);
		statusLV = (ListView) view.findViewById(R.id.status);
		extractWithFullPathsCB = (CheckBox) view.findViewById(R.id.extractWithFullPathsCB);
		historyBtn = (ImageButton) view.findViewById(R.id.historyBtn);
		historySaveBtn = (ImageButton) view.findViewById(R.id.historySaveBtn);
		historyBtn.setColorFilter(Constants.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
		historySaveBtn.setColorFilter(Constants.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
		passwordET.setTintColor(Constants.TEXT_COLOR);
		
		filesBtn.setColorFilter(Constants.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
		saveToBtn.setColorFilter(Constants.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
		
		config = view.findViewById(R.id.config);
		status = view.findViewById(R.id.status);
		
		adapter = new ArrayAdapter<String>(activity, R.layout.textview_item, R.id.outputTV, outputList);
		statusLV.setAdapter(adapter);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
			overwriteModeSpinner.getContext(), android.R.layout.simple_spinner_item, modes);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		overwriteModeSpinner.setAdapter(adapter);
		overwriteModeSpinner.setOnItemSelectedListener(this);
		extractWithFullPathsCB.setOnCheckedChangeListener(this);
		getView().findViewById(R.id.config).setVisibility(View.VISIBLE);
		getView().findViewById(R.id.status).setVisibility(View.GONE);
		Log.d(TAG, "onViewCreated files " + files + ", fileET " + fileET.getText() + ", saveTo " + saveTo + ", saveToET " + saveToET.getText());
		//Log.d(TAG, Util.arrayToString(fileET.getText().toString().split("\\|+\\s*"), true, "\n"));

		if (decompressTask == null || decompressTask.isCancelled() || decompressTask.getStatus() == AsyncTask.Status.FINISHED) {
			config.setVisibility(View.VISIBLE);
			status.setVisibility(View.GONE);
			mBtnOK.setText("Decompress");
		} else {
			config.setVisibility(View.GONE);
			status.setVisibility(View.VISIBLE);
			mBtnOK.setText("Cancel");
		}
		
		historyBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View p1) {
					new MaterialDialog.Builder(activity)
						.title("Proposed paths")
						.items(historyList)
						.itemsCallback(new MaterialDialog.ListCallback() {
							@Override
							public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
								Log.d(TAG, historyList.get(which) + ": " + text);
								fileET.setText(text);
							}
						})
						.positiveText(android.R.string.cancel)
						.show();
				}
			});

		historySaveBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View p1) {
					new MaterialDialog.Builder(activity)
						.title("Proposed paths")
						.items(historySaveList)
						.itemsCallback(new MaterialDialog.ListCallback() {
							@Override
							public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
								Log.d(TAG, historySaveList.get(which) + ": " + text);
								saveToET.setText(text);
							}
						})
						.positiveText(android.R.string.cancel)
						.show();
				}
			});

		filesBtn.setOnClickListener(new GetFileListener(this, Constants.ACTION_PICK_FILE, 
														Constants.ZIP_TITLE, 
														Constants.ZIP_SUFFIX, 
														"",
														fileET, 
														Constants.FILES_REQUEST_CODE, 
														Constants.MULTI_FILES));
		mBtnOK.setOnClickListener(this);

		mBtnCancel.setOnClickListener(this);

		saveToBtn.setOnClickListener(new GetFileListener(this, Constants.ACTION_PICK_DIRECTORY, 
														 "Output Folder", 
														 "", 
														 "", 
														 saveToET, 
														 Constants.SAVETO_REQUEST_CODE,
														 !Constants.MULTI_FILES));

	}

	@Override
	public void onClick(final View p1) {
		Log.d(TAG, "restore");
		switch (p1.getId()) {
			case R.id.cancelDir:
				dismiss();
				break;
			case R.id.okDir:
				Log.d("DECOMP_REQUEST_CODE.selectedFiles", files + ".");

				save();

				if (files.length() == 0) {
					showToast("Invalid \"files\"");
					return;
				}
				if (saveTo.length() == 0) {
					showToast("Invalid folder");
					return;
				}

				File file = new File(saveTo);
				if (file.isFile()) {
					showToast("Error: Destination folder is a file");
					return;
				}
				file.mkdirs();
				if (!file.exists()) {
					showToast("Error: Destination folder cannnot be written");
					return;
				}

				int size = historyList.size();
				historyList.add(0, files);
				if (size > 20) {
					historyList.remove(size);
				}
				size = historySaveList.size();
				historySaveList.add(0, saveTo);
				if (historySaveList.size() > 20) {
					historySaveList.remove(size);
				}

				if (decompressTask == null || decompressTask.isCancelled() || decompressTask.getStatus() == AsyncTask.Status.FINISHED) {
//					ForegroundService.ticker = "Decompressing";
//					ForegroundService.title = "Touch to Open";
//					ForegroundService.text = "Decompressing";
//					AndroidUtils.startService(activity, ForegroundService.class, ForegroundService.ACTION_FOREGROUND, TAG);
					config.setVisibility(View.GONE);
					status.setVisibility(View.VISIBLE);
					decompressTask = new DecompressTask(this);
					decompressTask.execute();
					mBtnOK.setText("Cancel");
				} else {
					decompressTask.cancel(true);
					mBtnOK.setText("Decompress");
					config.setVisibility(View.VISIBLE);
					status.setVisibility(View.GONE);
				}
				break;
		}
	}

	private void showToast(String message) {
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
        mToast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    @Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState " + outState);
		super.onSaveInstanceState(outState);
		if (outState != null) {
			outState.putString("password", passwordET.getText().toString());
			outState.putStringArrayList("outputList", new ArrayList<String>(outputList));
		}
	}

	public void onViewStateRestored(Bundle savedInstanceState) {
		Log.d(TAG, "onViewStateRestored files " + files + ", fileET " + fileET.getText() + ", saveTo " + saveTo + ", saveToET " + saveToET.getText());
		super.onViewStateRestored(savedInstanceState);
		if (savedInstanceState != null) {
			passwordET.setText(savedInstanceState.getString("password"));
			outputList.clear();
			outputList.addAll(savedInstanceState.getStringArrayList("outputList"));
			adapter.notifyDataSetChanged();
		}
		restore();
	}

	public void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
		save();
	}

	@Override
	public void onCheckedChanged(CompoundButton p1, boolean p2) {
		if (p1.getId() == R.id.extractWithFullPathsCB) {
			if (p1.isChecked()) {
				command = "x";
			} else {
				command = "e";
			}
		}
	}

	public void onItemSelected(
		AdapterView<?> parent, View view, int position, long id) {
		overwriteMode = overwriteModeSpinner.getSelectedItemPosition();
		Log.i("on overwriteMode", overwriteMode + "");
	}

	public void onNothingSelected(AdapterView<?> parent) {
	}

	void save() {
		Log.d(TAG, "save " + this);
		if (fileET != null) {
			files = fileET.getText().toString();
			saveTo = saveToET.getText().toString();
			include = includeET.getText().toString();
			exclude = excludeET.getText().toString();
			otherArgs = otherArgsET.getText().toString();
			overwriteMode = overwriteModeSpinner.getSelectedItemPosition();
			command = extractWithFullPathsCB.isChecked() ? "x" : "e";
			password = passwordET.getText().toString();
			try {
				FileOutputStream fos = new FileOutputStream(ExplorerApplication.DATA_DIR + DecompressFragment.class.getSimpleName() + ".ser");
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				oos.writeObject(this);
				FileUtil.flushClose(bos, fos);
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
	}

	public static DecompressFragment newInstance() {

		File fi = new File(ExplorerApplication.DATA_DIR + DecompressFragment.class.getSimpleName() + ".ser");
		Log.d(TAG, fi.getAbsolutePath() + ", exist " + fi.exists() + ", length " + fi.length());
		DecompressFragment decompressFrag = null;
		if (fi.exists() && fi.length() > 0) {
			try	{
				FileInputStream fis = new FileInputStream(fi);
				BufferedInputStream bis = new BufferedInputStream(fis);
				ObjectInputStream ois = new ObjectInputStream(bis);
				decompressFrag = (DecompressFragment) ois.readObject();
				FileUtil.close(ois, bis, fis);

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			decompressFrag = new DecompressFragment();
		}
		Log.d(TAG, "newInstance " + decompressFrag);
		return decompressFrag;
	}

	private void restore() {
		Log.d(TAG, "restore");
		fileET.setText(files);
		saveToET.setText(saveTo);
		includeET.setText(include);
		excludeET.setText(exclude);
		otherArgsET.setText(otherArgs);
		//passwordET.setText(password);
		overwriteModeSpinner.setSelection(overwriteMode);

		if ("x".equals(command)) {
			extractWithFullPathsCB.setChecked(true);
		} else {
			extractWithFullPathsCB.setChecked(false);
		}
	}

}

