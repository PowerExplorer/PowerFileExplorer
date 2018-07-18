package net.gnu.searcher;

import android.app.Activity;
import android.widget.Toast;
import android.os.AsyncTask;
import android.util.Log;

public class GenStardictTask extends AsyncTask<Void, String, String> {
	
	private String fName;
	private Activity s;

	public GenStardictTask(Activity s, String f) {
		this.s = s;
		fName = f;
	}

	@Override
	protected String doInBackground(Void... p1) {
		try {
			publishProgress("Generating Stardict files...");
			StardictReader.createStardictData(fName);
			return "Generate Stardict files successfully";
		} catch (Throwable e) {
			publishProgress(e.getMessage());
			Log.e("Generate Stardict files unsuccessfully", e.getMessage(),e);
			return "Generate Stardict files unsuccessfully";
		}
	}
	
	@Override
	protected void onProgressUpdate(String... progress) {
		if (progress != null && progress.length > 0 
			&& progress[0] != null && progress[0].trim().length() > 0) {
			//s.statusView.setText(progress[0]);
			Toast.makeText(s, progress[0], Toast.LENGTH_SHORT);
		}
	}

	@Override
	protected void onPostExecute(String result) {
		if (s!= null) {
			Toast.makeText(s, result, Toast.LENGTH_SHORT);
		}
		//s.statusView.setText(result);
	}
}
