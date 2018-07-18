package net.gnu.searcher;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import java.io.Serializable;
import net.gnu.explorer.R;
import net.gnu.p7zip.GetFileListener;
import net.gnu.explorer.ExplorerActivity;
import android.support.v4.app.DialogFragment;
import java.io.File;
import android.widget.Toast;

/**
 * Activities that contain this fragment must implement the
 * {@link net.rdrei.android.dirchooser.DirectoryChooserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link net.rdrei.android.dirchooser.DirectoryChooserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Pdf2ImageFragment extends DialogFragment implements  Serializable, View.OnClickListener {

	private static final long serialVersionUID = 7105871643003630949L;

	private static final String TAG = "Pdf2ImageFragment";
	
	String files = "";
	String saveTo = "";
	
	private transient Button mBtnConfirm;
	private transient Button mBtnCancel;
	private transient Button filesBtn;
	private transient Button saveToBtn;
	transient EditText oriDocET;
	transient EditText modifiedDocET;
	private transient Activity activity;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.e(TAG, "onSaveInstanceState");
		if (outState == null) {
			return;
		}
		super.onSaveInstanceState(outState);

		outState.putString("Files", oriDocET.getText() + "");
		outState.putString("SaveTo", modifiedDocET.getText() + "");
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate");
		
		if (savedInstanceState != null && oriDocET != null) {
			oriDocET.setText(savedInstanceState.getString("Files"));
			modifiedDocET.setText(savedInstanceState.getString("SaveTo"));
			
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
		final View view = inflater.inflate(R.layout.pdf2image, container, false);

		Log.e(TAG, "onCreateView");
		mBtnConfirm = (Button) view.findViewById(R.id.okDir);
		mBtnCancel = (Button) view.findViewById(R.id.cancelDir);
		oriDocET = (EditText) view.findViewById(R.id.files);
		modifiedDocET = (EditText) view.findViewById(R.id.saveTo);
		
		filesBtn = (Button) view.findViewById(R.id.filesBtn);
		saveToBtn = (Button) view.findViewById(R.id.saveToBtn);

		restore();
		//}

		filesBtn.setOnClickListener(new GetFileListener(this, ExplorerActivity.ACTION_MULTI_SELECT, ExplorerActivity.ALL_SUFFIX_TITLE, 
														".pdf", 
														"application/pdf",
														oriDocET, 
														ExplorerActivity.FILES_REQUEST_CODE, 
														!ExplorerActivity.MULTI_FILES));
		saveToBtn.setOnClickListener(new GetFileListener(this, ExplorerActivity.ACTION_MULTI_SELECT, "Otput Folder", 
														 "", 
														 "",
														 modifiedDocET, 
														 ExplorerActivity.SAVETO_REQUEST_CODE, 
														 !ExplorerActivity.MULTI_FILES));
		mBtnConfirm.setOnClickListener(this);
		mBtnCancel.setOnClickListener(this);
		return view;
	}

	public void restore() {
		oriDocET.setText(files);
		modifiedDocET.setText(saveTo);
		
	}

	public void save() {
		files = oriDocET.getText().toString();
		saveTo = modifiedDocET.getText().toString();
		
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d(TAG, "onAttach");
		this.activity = activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.d(TAG, "onDetach");
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
				File oriF = new File(files);
				File modiF = new File(saveTo);
				if ((!oriF.exists() || oriF.length() == 0)) {
					Toast.makeText(activity, "Pdf file is not existed or empty", Toast.LENGTH_SHORT).show();
				} else if (!modiF.exists()) {
					Toast.makeText(activity, "Target folder is not existed", Toast.LENGTH_SHORT).show();
				} 
				new Pdf2ImageTask(activity, files, saveTo).execute();
				break;
		}
	}
}

