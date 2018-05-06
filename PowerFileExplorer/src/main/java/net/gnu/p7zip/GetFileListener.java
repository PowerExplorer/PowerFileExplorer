package net.gnu.p7zip;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import net.gnu.explorer.ExplorerActivity;
import android.support.v4.app.DialogFragment;

public class GetFileListener implements OnClickListener {

	private DialogFragment frag;
	String action;
	private EditText filesET;
	private String title;
	private String suffix;
	private String mimes;
	private int requestCode;
	private boolean multi;

	public GetFileListener(DialogFragment frag, String action, String title, String suffix, String mimes, EditText filesET, int requestCode, boolean multi) {
		this.frag = frag;
		this.filesET = filesET;
		this.title = title;
		this.suffix = suffix;
		this.mimes = mimes;
		this.requestCode = requestCode;
		this.multi = multi;
		this.action = action;
	}

	public void onClick(final View v) {
		frag.dismiss();
		final Intent intent = new Intent(action);
		final String file = filesET.getText().toString();
		if (file.length() > 0) {
			intent.putExtra(ExplorerActivity.PREVIOUS_SELECTED_FILES, file.split("\\|+\\s*"));
		}
		intent.putExtra(ExplorerActivity.EXTRA_FILTER_FILETYPE, suffix);
		intent.putExtra(ExplorerActivity.EXTRA_FILTER_MIMETYPE, mimes);
		intent.putExtra(ExplorerActivity.EXTRA_MULTI_SELECT, multi);
		intent.putExtra(ExplorerActivity.EXTRA_TITLE, title);
		frag.getActivity().startActivityForResult(intent, requestCode);
	}
}

