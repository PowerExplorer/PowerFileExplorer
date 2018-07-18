package net.gnu.searcher;

import android.os.*;
import java.io.*;
import android.util.*;
import android.app.*;
import java.util.*;
import android.widget.*;
import net.gnu.util.ComparableEntry;
import net.gnu.util.FileUtil;
import net.gnu.util.HtmlUtil;
import net.gnu.util.Util;

public class ReplaceAllTask extends AsyncTask<String, String, String> {

	private ReplaceAllFragment replaceFrag = null;
	private Collection<String> filePaths;
	private boolean isRegex = false;
	private boolean caseSensitive = false;
	private boolean backup = false;
	private String[] froms;
	private String[] tos;
	private String saveTo;
	private String stardict;
	private static final String TAG = "ReplaceAllTask";
	
	private ComparableEntry<String, String>[] dictArr = null;
	private ComparableEntry<String, String>[] replaceByArr = null;
	
	public ReplaceAllTask(ReplaceAllFragment replaceFrag, Collection<File> files, String[] froms, String[] tos) {//}, String saveTo, String stardict, boolean isRegex, boolean caseSensitive, boolean backup) {

		filePaths = new ArrayList<String>(files.size());
		for (File f : files) {
			filePaths.add(f.getAbsolutePath());
		}
		this.caseSensitive = replaceFrag.caseSensitive;
		this.isRegex = replaceFrag.isRegex;
		this.saveTo = replaceFrag.saveTo;
		this.stardict = replaceFrag.stardict;
		this.backup = replaceFrag.backup;
		this.froms = froms;
		this.tos = tos;
		this.replaceFrag = replaceFrag;
	}
	
	protected String doInBackground(String... urls) {
		try {
			Set<ComparableEntry<String, String>> set = new TreeSet<ComparableEntry<String, String>>();
			if (stardict.length() > 0) {
				Log.d(TAG, "use stardict");
				BufferedReader reader = new BufferedReader(new FileReader(stardict));
				String line;
				String[] entry;

				while (reader.ready()) {
					line = reader.readLine().trim();
					if (line.length() > 0) {
						entry = line.split("\t");
						set.add(new ComparableEntry<String, String>(entry[0], entry[1]));
						Log.d(TAG, entry[0] + ", " + entry[1] + isRegex + ", " + caseSensitive);
					}
				}
				dictArr = new ComparableEntry[set.size()];
				set.toArray(dictArr);
				FileUtil.close(reader);
			} else {
				Log.d(TAG, "no stardict");
			}
			
			set = new TreeSet<ComparableEntry<String, String>>();
			int length = Math.min(froms.length, tos.length);
			for (int i = 0; i < length; i++) {
				set.add(new ComparableEntry<String, String>(froms[i], tos[i]));
				Log.d(TAG, froms[i] + ", " + tos[i] + isRegex + ", " + caseSensitive);
			}
			replaceByArr = new ComparableEntry[set.size()];
			set.toArray(replaceByArr);
			for (String st : filePaths) {
				if (!this.isCancelled()) {
					replaceAll(st);
				}
			}
			return "Replace All is finished";
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return "Replace All is unsuccessful";
	}

	private void replaceAll(String fileP) {
		publishProgress("replacing " + fileP);
		try {
			String fileContent = FileUtil.readFileWithCheckEncode(fileP);
			if (stardict.length() > 0) {
				for (ComparableEntry<String, String> e : dictArr) {
					fileContent = HtmlUtil.replaceRegexAll(fileContent, e.getKey(), e.getValue(), isRegex, caseSensitive);
				}
			}
			
			for (ComparableEntry<String, String> e : replaceByArr) {
				fileContent = HtmlUtil.replaceRegexAll(fileContent, e.getKey(), e.getValue(), isRegex, caseSensitive);
			}

			if (saveTo == null || saveTo.trim().length() == 0) {
				if (backup) {
					new File(fileP).renameTo(new File(fileP + "_" + Util.dtf.format(System.currentTimeMillis()).replaceAll("[:/]", "_") + ".old"));
				}
				FileUtil.stringToFile(fileP, fileContent);
			} else {
				FileUtil.stringToFile(saveTo + fileP, fileContent);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			publishProgress(t.toString());
		}
	}

	@Override
	protected void onPostExecute(String result) {
		Toast.makeText(replaceFrag.getActivity(), result, Toast.LENGTH_LONG).show();
		replaceFrag.statusTV.setText(result);
		Log.d("ReplaceAllTask", result);
	}
	
	protected void onProgressUpdate(String... progress) {
		if (progress != null && progress.length > 0 
			&& progress[0] != null && progress[0].trim().length() > 0) {
			//Toast.makeText(activity, progress[0], Toast.LENGTH_LONG).show();
			replaceFrag.statusTV.setText(progress[0]);
			Log.d("ReplaceAllTask", progress[0]);
		}
	}
}

