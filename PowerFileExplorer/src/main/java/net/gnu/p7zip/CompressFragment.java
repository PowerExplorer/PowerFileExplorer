package net.gnu.p7zip;

import android.widget.*;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import net.gnu.explorer.R;
import net.gnu.util.FileUtil;
import com.afollestad.materialdialogs.MaterialDialog;
import java.util.ArrayList;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import net.gnu.androidutil.AndroidUtils;
import net.gnu.explorer.ExplorerActivity;
import net.gnu.androidutil.ForegroundService;
import android.graphics.PorterDuff;
import net.gnu.explorer.ExplorerApplication;
import android.support.v4.app.DialogFragment;

public class CompressFragment extends DialogFragment implements Serializable, OnItemSelectedListener, OnCheckedChangeListener, TextWatcher, OnClickListener, android.widget.RadioGroup.OnCheckedChangeListener {

	private static final long serialVersionUID = 3972884849642358507L;

	private static final String TAG = "CompressFragment";

	private transient static final String[] levels = new String[] {"Fastest, no compression (-mx0)", "Very fast (-mx1)", "Fast (-mx3)", "Normal (-mx5)", "Slow (-mx7)", "Very Slow, smallest (-mx9)"};
	//transient static final String[] types = new String[]{"7z", "zip", "bz2", "gz", "tar", "wim", "swm", "xz", "zipx", "jar", "xpi", "odt", "ods", "docx", "xlsx", "epub"};
	//private transient static final String[] volumes = new String[]{"bytes", "kilobytes", "megabytes", "gigabytes"};

	public String files = "";
	public String saveTo = "";
	String volumeVal = "";
	int volumeUnit;
	String excludes = "";
	private ArrayList<String> historyList = new ArrayList<>();
	private ArrayList<String> historySaveList = new ArrayList<>();

	int level = 3;
	int type = 0;
	String otherParameters = "";
	transient String password = "";

	//private transient OnFragmentInteractionListener mListener;

	transient Button mBtnOK;
	private transient Button mBtnCancel;
	private transient View filesBtn;
	private transient View saveToBtn;
	private transient ImageButton historyBtn;
	private transient ImageButton historySaveBtn;
	transient EditText fileET;
	transient EditText saveToET;
	transient ShowHidePasswordEditText passwordET;

	transient Spinner compressLevelSpinner;
	transient RadioGroup typeRadioGroup;
	transient RadioGroup volUnitRadioGroup;
	transient EditText volumeValET;
	transient EditText excludeET;

	transient EditText otherParametersET;
	transient EditText solidArchiveET;
	//transient EditText workingDirectoryET;
	transient EditText archiveNameMaskET;

	transient CheckBox encryptFileNamesCB;
	transient CheckBox deleteFilesAfterArchivingCB;

	transient CheckBox solidArchiveCB;
	transient CheckBox testCB;
	transient CheckBox createSeparateArchivesCB;
	transient CheckBox archiveNameMaskCB;
	//transient CheckBox workingDirectoryCB;
	transient CheckBox otherParametersCB;
	//transient Button workingDirectoryBtn;

	String solidArchive = "";
	boolean test = false;
	boolean createSeparateArchives = false;
	String archiveNameMask;
	//String workingDirectory;

	transient TextView statusTV;

	String deleteFilesAfterArchiving = "";
	String encryptFileNames = "";
	transient CompressTask compressTask;

	private transient Toast mToast;
	private transient Activity activity;

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

		return inflater.inflate(R.layout.compress, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.d(TAG, "onViewCreated " + savedInstanceState);
		super.onViewCreated(view, savedInstanceState);

		activity = getActivity();

		mBtnOK = (Button) view.findViewById(R.id.okDir);
		mBtnCancel = (Button) view.findViewById(R.id.cancelDir);
		fileET = (EditText) view.findViewById(R.id.files);

		saveToET = (EditText) view.findViewById(R.id.saveTo);
		volumeValET = (EditText) view.findViewById(R.id.volumeVal);
		excludeET = (EditText) view.findViewById(R.id.exclude);
		otherParametersET = (EditText) view.findViewById(R.id.otherParametersET);
		passwordET = (ShowHidePasswordEditText) view.findViewById(R.id.password);
		passwordET.setTintColor(ExplorerActivity.TEXT_COLOR);

		solidArchiveET = (EditText) view.findViewById(R.id.solidArchiveET);
		//workingDirectoryET = (EditText) view.findViewById(R.id.workingDirectoryET);
		archiveNameMaskET = (EditText) view.findViewById(R.id.archiveNameMaskET);
		//view.findViewById(R.id.mode).setOnClickListener(this);
		compressLevelSpinner = (Spinner) view.findViewById(R.id.level);
		typeRadioGroup = (RadioGroup) view.findViewById(R.id.type);
		volUnitRadioGroup = (RadioGroup) view.findViewById(R.id.volumeUnit);
		filesBtn = view.findViewById(R.id.filesBtn);
		saveToBtn = view.findViewById(R.id.saveToBtn);
		statusTV = (TextView) view.findViewById(R.id.status);
		historyBtn = (ImageButton) view.findViewById(R.id.historyBtn);
		historySaveBtn = (ImageButton) view.findViewById(R.id.historySaveBtn);
		historyBtn.setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
		historySaveBtn.setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);

		deleteFilesAfterArchivingCB = (CheckBox)view.findViewById(R.id.deleteFilesAfterArchivingCB);
		encryptFileNamesCB = (CheckBox)view.findViewById(R.id.encryptFileNamesCB);

		solidArchiveCB = (CheckBox)view.findViewById(R.id.solidArchiveCB);
		testCB = (CheckBox)view.findViewById(R.id.testCB);
		createSeparateArchivesCB = (CheckBox)view.findViewById(R.id.createSeparateArchivesCB);
		archiveNameMaskCB = (CheckBox)view.findViewById(R.id.archiveNameMaskCB);
		//workingDirectoryCB = (CheckBox)view.findViewById(R.id.workingDirectoryCB);
		//workingDirectoryBtn = (Button)view.findViewById(R.id.workingDirectoryBtn);
		
		getView().findViewById(R.id.config).setVisibility(View.VISIBLE);
		getView().findViewById(R.id.status).setVisibility(View.GONE);
		
		typeRadioGroup.setOnCheckedChangeListener(this);
		deleteFilesAfterArchivingCB.setOnCheckedChangeListener(this);

		encryptFileNamesCB.setOnCheckedChangeListener(this);

		passwordET.addTextChangedListener(this);

		solidArchiveCB.setOnCheckedChangeListener(this);

//		testCB.setOnCheckedChangeListener(this);
//
//		createSeparateArchivesCB.setOnCheckedChangeListener(this);
//
		archiveNameMaskCB.setOnCheckedChangeListener(this);

		//workingDirectoryCB.setOnCheckedChangeListener(this);
		if (compressTask == null || compressTask.isCancelled() || compressTask.getStatus() == AsyncTask.Status.FINISHED) {
			mBtnOK.setText("Compress");
		} else {
			mBtnOK.setText("Cancel");
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
			compressLevelSpinner.getContext(), android.R.layout.simple_spinner_item, levels);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		compressLevelSpinner.setAdapter(adapter);
		compressLevelSpinner.setOnItemSelectedListener(this);

		Log.d(TAG, "onViewCreated files " + files + ", fileET " + fileET.getText() + ", saveTo " + saveTo + ", saveToET " + saveToET.getText());

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

		filesBtn.setOnClickListener(new GetFileListener(this, ExplorerActivity.ACTION_MULTI_SELECT, ExplorerActivity.ALL_SUFFIX_TITLE, 
														ExplorerActivity.ALL_SUFFIX, 
														"*/*",
														fileET, 
														ExplorerActivity.FILES_REQUEST_CODE, 
														ExplorerActivity.MULTI_FILES));

		mBtnOK.setOnClickListener(this);

		mBtnCancel.setOnClickListener(this);

		saveToBtn.setOnClickListener(new GetFileListener(this, ExplorerActivity.ACTION_MULTI_SELECT, "File/Folder", 
														 ExplorerActivity.ZIP_SUFFIX, 
														 "",
														 saveToET, 
														 ExplorerActivity.SAVETO_REQUEST_CODE, 
														 !ExplorerActivity.MULTI_FILES));

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState " + outState);
		super.onSaveInstanceState(outState);
		if (outState != null) {
			outState.putString("password", passwordET.getText().toString());
		}
	}

	public void onViewStateRestored(Bundle savedInstanceState) {
		Log.d(TAG, "onViewStateRestored files " + files + ", fileET " + fileET.getText() + ", saveTo " + saveTo + ", saveToET " + saveToET.getText());
		super.onViewStateRestored(savedInstanceState);
		if (savedInstanceState != null) {
			passwordET.setText(savedInstanceState.getString("password"));
		}
		restore();
	}

	public void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
		save();
	}

	private void compress() {
		ForegroundService.ticker = "Compressing";
		ForegroundService.title = "Touch to Open";
		ForegroundService.text = "Compressing";
		AndroidUtils.startService(activity, ForegroundService.class, ForegroundService.ACTION_FOREGROUND, TAG);
		compressTask = new CompressTask(this);
		compressTask.execute();
		mBtnOK.setText("Cancel");
		getView().findViewById(R.id.config).setVisibility(View.GONE);
		getView().findViewById(R.id.status).setVisibility(View.VISIBLE);
	}
	
	public void onItemSelected(
		AdapterView<?> parent, View view, int position, long id) {
		//Log.d(TAG, view + ", " + parent);
		if (parent == compressLevelSpinner) {
			level = compressLevelSpinner.getSelectedItemPosition();
			Log.d(TAG, "onItemSelected compressLevel" + level);
		}
	}

	public void onNothingSelected(AdapterView<?> parent) {
	}

	@Override
	public void onClick(final View p1) {
		switch (p1.getId()) {
//			case R.id.mode: 
//				final View view = getView();
//				if (((ToggleButton)p1).isChecked()) {
//					//view.findViewById(R.id.advancedLayout).setVisibility(View.VISIBLE);
//					view.findViewById(R.id.basicLayout).setVisibility(View.GONE);
//				} else {
//					view.findViewById(R.id.basicLayout).setVisibility(View.VISIBLE);
//					//view.findViewById(R.id.advancedLayout).setVisibility(View.GONE);
//				}
//				break;
			case R.id.cancelDir:
				dismiss();
				break;
			case R.id.okDir:

				Log.d("COMPRESS_REQUEST_CODE.selectedFiles", files + ", " + saveTo);// + fName);

				save();

				if (files.length() == 0) {
					showToast("Invalid \"files\"");
					return;
				}
				if (saveTo.length() == 0) {
					showToast("Invalid file name");
					return;
				}

				String fN;
				if (saveTo.matches(FileUtil.compressibleExtension)) {
					fN = saveTo;
				} else {
					fN = saveTo + "." + ((RadioButton)getView().findViewById(typeRadioGroup.getCheckedRadioButtonId() <= 0 ? R.id.sevenz: typeRadioGroup.getCheckedRadioButtonId())).getTag();// + "/" + fName
				}
				final File file = new File(fN);
				Log.d(TAG, file.getAbsolutePath() + ", " + "file.isDirectory() = " + file.isDirectory() + ", file.exists() = " + file.exists());
				if (file.isDirectory()) {
					showToast("File name must be not a folder");
					return;
				} else {
					File parentFile = file.getParentFile();
					if (parentFile != null) {
						if (!parentFile.exists() && !parentFile.mkdirs()) {
							showToast(parentFile + " cannot be created");
							return;
						}
						if (!parentFile.canWrite()) {
							showToast("Folder " + parentFile + " cannot be written");
							return;
						}
					} else {
						showToast("Parent File is not existed");
						return;
					}
				}

				int size = historyList.size();
				historyList.add(0, fileET.getText().toString());
				if (size > 20) {
					historyList.remove(size);
				}
				size = historySaveList.size();
				historySaveList.add(0, saveToET.getText().toString());
				if (historySaveList.size() > 20) {
					historySaveList.remove(size);
				}

				if (compressTask == null || compressTask.isCancelled() || compressTask.getStatus() == AsyncTask.Status.FINISHED) {
					if (file.exists()) {
						AlertDialog.Builder alert = new AlertDialog.Builder(activity);
						alert.setIconAttribute(android.R.attr.alertDialogIcon);
						alert.setTitle("Overwrite?");
						alert.setMessage("Do you really want to overwrite file \"" + file.getAbsolutePath() + "\"?");
						alert.setCancelable(true);
						alert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(final DialogInterface dialog, final int which) {
									if (file.delete()) {
										showToast("Delete " + file + " successfully");
									} else {
										showToast("Delete " + file + " unsuccessfully");
									}
									compress();
								}
							});
						alert.setPositiveButton("No", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									compress();
									dialog.cancel();
								}
							});
						AlertDialog alertDialog = alert.create();
						alertDialog.show();
					} else {
						compress();
					}
				} else {
					compressTask.cancel(true);
					mBtnOK.setText("Compress");
					getView().findViewById(R.id.config).setVisibility(View.VISIBLE);
					getView().findViewById(R.id.status).setVisibility(View.GONE);
				}
				break;
		}
	}

	@Override
	public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {
	}

	@Override
	public void onTextChanged(CharSequence p1, int p2, int p3, int p4) {
		if (p1.length() > 0) {
			if (typeRadioGroup.getCheckedRadioButtonId() == R.id.sevenz) {
				encryptFileNamesCB.setEnabled(true);
			}
		} else {
			encryptFileNamesCB.setChecked(false);
			encryptFileNamesCB.setEnabled(false);
			encryptFileNames = "";
		}
	}

	@Override
	public void afterTextChanged(Editable p1) {
	}

	@Override
	public void onCheckedChanged(RadioGroup p1, int p2) {
		if (p1.getCheckedRadioButtonId() == R.id.sevenz) {
			solidArchiveCB.setEnabled(true);
			getView().findViewById(R.id.solidArchiveParameterInfo).setEnabled(true);
			if (passwordET.getText().length() > 0) {
				encryptFileNamesCB.setEnabled(true);
			}
			if (otherParametersET.getText().length() == 0) {
				otherParametersET.setText("-mqs=on");
			}
		} else {
			solidArchiveET.setText("-ms=off");
			solidArchiveCB.setChecked(false);
			solidArchiveCB.setEnabled(false);
			getView().findViewById(R.id.solidArchiveParameterInfo).setEnabled(false);
		}
		if (p1.getCheckedRadioButtonId() != R.id.zpaq) {
			deleteFilesAfterArchivingCB.setEnabled(true);
		} else {
			deleteFilesAfterArchivingCB.setChecked(false);
			deleteFilesAfterArchivingCB.setEnabled(false);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton checkBox, boolean p2) {
		switch (checkBox.getId()) {
			case (R.id.deleteFilesAfterArchivingCB) : {
					if (checkBox.isChecked()) {
						deleteFilesAfterArchiving = "-sdel";
					} else {
						deleteFilesAfterArchiving = "";
					}
					break;
				}
			case (R.id.encryptFileNamesCB) : {
					if (checkBox.isChecked()) {
						encryptFileNames = "-mhe=on";
					} else {
						encryptFileNames = "";
					}
					break;
				}
			case (R.id.solidArchiveCB) : {
					if (checkBox.isChecked()) {
						solidArchiveET.setEnabled(true);
						solidArchiveET.setText("-mse");
					} else {
						solidArchiveET.setText("-ms=off");
						solidArchiveET.setEnabled(false);
					}
					break;
				}
			case (R.id.testCB) : {
//					if (checkBox.isChecked()) {
//						otherArgsET.setEnabled(true);
//					} else {
//						otherArgsET.setText("");
//						otherArgsET.setEnabled(false);
//					}
					break;
				}
			case (R.id.createSeparateArchivesCB) : {
//					if (checkBox.isChecked()) {
//						otherArgsET.setEnabled(true);
//					} else {
//						otherArgsET.setText("");
//						otherArgsET.setEnabled(false);
//					}
					break;
				}
			case (R.id.archiveNameMaskCB) : {
					if (checkBox.isChecked()) {
						archiveNameMaskET.setEnabled(true);
						archiveNameMaskET.setText("yyyy-MM-dd HH.mm.ss");
					} else {
						archiveNameMaskET.setText("");
						archiveNameMaskET.setEnabled(false);
					}
					break;
				}
//			case (R.id.workingDirectoryCB) : {
//					if (checkBox.isChecked()) {
//						workingDirectoryET.setEnabled(true);
//						workingDirectoryBtn.setEnabled(true);
//					} else {
//						workingDirectoryET.setText("");
//						workingDirectoryET.setEnabled(false);
//						workingDirectoryBtn.setEnabled(false);
//					}
//					break;
//				}
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

    void save() {
		Log.d(TAG, "save " + this);
		if (fileET != null) {
			files = fileET.getText().toString();
			saveTo = saveToET.getText().toString();
			volumeVal = volumeValET.getText().toString();
			excludes = excludeET.getText().toString();
			level = compressLevelSpinner.getSelectedItemPosition();
			type = typeRadioGroup.getCheckedRadioButtonId();
			volumeUnit = volUnitRadioGroup.getCheckedRadioButtonId();

			deleteFilesAfterArchiving = deleteFilesAfterArchivingCB.isChecked() ? "-sdel" : "";
			encryptFileNames = encryptFileNamesCB.isChecked() ? "-mhe=on" : "";
			solidArchive = solidArchiveET.getText().toString();
			test = testCB.isChecked();
			createSeparateArchives = createSeparateArchivesCB.isChecked();
			archiveNameMask = archiveNameMaskET.getText().toString();
			//workingDirectory = workingDirectoryET.getText().toString();
			otherParameters = otherParametersET.getText().toString();

			try {
				FileOutputStream fos = new FileOutputStream(ExplorerApplication.DATA_DIR + CompressFragment.class.getSimpleName() + ".ser");
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				oos.writeObject(this);
				FileUtil.flushClose(bos, fos);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static CompressFragment newInstance() {
		Log.d(TAG, "newInstance()");
		File fi = new File(ExplorerApplication.DATA_DIR + CompressFragment.class.getSimpleName() + ".ser");
		Log.d(TAG, fi.getAbsolutePath() + ", exist " + fi.exists() + ", length " + fi.length());
		CompressFragment compressFrag = null;
		if (fi.exists() && fi.length() > 0) {
			try	{
				FileInputStream fis = new FileInputStream(fi);
				BufferedInputStream bis = new BufferedInputStream(fis);
				ObjectInputStream ois = new ObjectInputStream(bis);
				compressFrag = (CompressFragment) ois.readObject();
				FileUtil.close(ois, bis, fis);
				//Log.d(TAG, "newInstance(), compressFrag " + compressFrag);

				//restore(compressFrag);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			compressFrag = new CompressFragment();
		}
		Log.d(TAG, "newInstance() 2 " + compressFrag);
		return compressFrag;
	}

	void restore() {
		Log.d(TAG, "restore() 3 " + this);
		fileET.setText(files);
		saveToET.setText(saveTo);
		volumeValET.setText(volumeVal);
		excludeET.setText(excludes);

		typeRadioGroup.check(type <= 0 ? R.id.sevenz : type);
		volUnitRadioGroup.check(volumeUnit);
		compressLevelSpinner.setSelection(level);

		deleteFilesAfterArchivingCB.setChecked(deleteFilesAfterArchiving.equals("-sdel") ? true : false);
		encryptFileNamesCB.setChecked(passwordET.getText().length() > 0 && encryptFileNames.equals("-mhe=on") ? true : false);
		solidArchiveET.setText(solidArchive);
		solidArchiveCB.setChecked(solidArchive != null && !solidArchive.trim().equals("-ms=off"));

		testCB.setChecked(test);
		createSeparateArchivesCB.setChecked(createSeparateArchives);
		archiveNameMaskET.setText(archiveNameMask);
//		workingDirectoryET.setText(workingDirectory);
//		boolean dir = workingDirectory.length() > 0;
//		workingDirectoryET.setEnabled(dir);
//		workingDirectoryBtn.setEnabled(dir);
//		workingDirectoryCB.setChecked(dir);

		otherParametersET.setText(otherParameters);
		Log.d(TAG, "restore() 4 " + this);

	}
	@Override
	public String toString() {
		return "files " + files + ", fileET " + (fileET == null ?null: fileET.getText()) + ", saveTo " + saveTo + ", saveToET " + (saveToET == null ?null: saveToET.getText());
	}

//	@Override
//	public void onAttach(Activity activity) {
//		Log.d(TAG, "onAttach " + activity);
//		super.onAttach(activity);
//		try {
//			mListener = (OnFragmentInteractionListener) activity;
//		} catch (ClassCastException e) {
//			throw new ClassCastException(activity
//										 + " must implement OnFragmentInteractionListener");
//		}
//	}

//	@Override
//	public void onDetach() {
//		Log.d(TAG, "onDetach");
//		super.onDetach();
//		mListener = null;
//	}
}
