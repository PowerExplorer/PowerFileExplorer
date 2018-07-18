package net.gnu.searcher;

import java.io.File;
import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.*;
import android.content.*;
import net.gnu.androidutil.AndroidUtils;

class Pdf2ImageTask extends AsyncTask<Void, String, String> {

	String pdfPath;
	String outDir;
	Context ctx;
	public Pdf2ImageTask(Context ctx, String pdfPath, String outDir) {
		this.pdfPath = pdfPath;
		this.outDir = outDir;
		this.ctx = ctx;
	}

	@Override
	protected String doInBackground(Void... p1) {
		try {
			publishProgress("Starting convert " + pdfPath + " into " + outDir + ". Please wait...");
			AndroidUtils.pdfToImage(new File(pdfPath), outDir);
		} catch (IOException e) {
			e.printStackTrace();
			publishProgress(e.getMessage());
		}
		return null;
	}

	protected void onProgressUpdate(String... progress) {
		if (progress != null && progress.length > 0 
			&& progress[0] != null && progress[0].trim().length() > 0) {
				AndroidUtils.showToast(ctx, progress[0]);
		}
	}

	protected void onPostExecute(String result) {
		publishProgress("Convert " + pdfPath + " into " + outDir + " finished");
	}
}

