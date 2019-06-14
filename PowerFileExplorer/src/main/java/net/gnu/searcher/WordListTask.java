package net.gnu.searcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import java.io.*;
import net.gnu.util.Util;
import net.gnu.util.DoubleComparableEntry;
import net.gnu.explorer.ExplorerActivity;
import net.gnu.explorer.ExplorerApplication;
import android.app.Activity;
import net.gnu.common.*;

public class WordListTask extends AsyncTask<String, String, String> {

	private Activity activity = null;
	private Collection<File> lf = null;
	private String saveTo;
	private List<StardictReader> stardictL = null;
	private static final String TAG = "WordListTask";
	private List<String> retL = new ArrayList<String>();
	private static final String WORDLIST_TITLE = Constants.HTML_STYLE
	+ "<title>Word List</title>\r\n" 
	+ Constants.HEAD_TABLE;
	private static final String TD1 = "<td>";// width='3%' valign='top' style='border:solid black 1.0pt; padding:0cm 1.4pt 0cm 1.4pt'>\r\n";
	private int totalWords = 0;
	private String allPath;
	private long start = 0;
	public WordListTask(Activity activity, Collection<File> lf, String saveTo, String stardict) {
		this.activity = activity;
		this.lf = lf;
		this.saveTo = saveTo;
		if (stardict.length() > 0) {
			stardictL = new ArrayList<StardictReader>();
			List<String> stringToList = Util.stringToList(stardict, "|");
			for (String st : stringToList) {
				int lastIdx = st.lastIndexOf(".");
				Log.d("stardict.substring(0, lastIdx)", stardict.substring(0, lastIdx) + ".idx");
				stardictL.add(StardictReader.instance(st.substring(0, lastIdx) + ".idx", st.substring(0, lastIdx) + ".dict"));
			}
		}
		
	}
	
	protected String doInBackground(String... urls) {
		start = System.currentTimeMillis();
		PowerManager pm = (PowerManager)activity.getSystemService(
			Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(
			PowerManager.PARTIAL_WAKE_LOCK,
			TAG);
		wl.acquire();
		try {
			TreeSet<DoubleComparableEntry<String, DoubleComparableEntry<Integer, String>>> allSet = new TreeSet<DoubleComparableEntry<String, DoubleComparableEntry<Integer, String>>>();
			for (File st : lf) {
				if (!this.isCancelled()) {
					getWL(st, allSet);
				}
			}
			
			List<DoubleComparableEntry<String, DoubleComparableEntry<Integer, String>>> l = 
					new ArrayList<DoubleComparableEntry<String, DoubleComparableEntry<Integer, String>>>(allSet.size());
			l.addAll(allSet);
			
			allSet = null;
			Collections.sort(l, new DoubleComparableEntry.RevertValueOrder<String, DoubleComparableEntry<Integer, String>>());
			allPath = ExplorerApplication.PRIVATE_PATH + "/all.byFreq." + Util.dtf.format(System.currentTimeMillis()).replaceAll("[/\\?<>\"':|\\\\]+", "_") + ".html";
			Log.d("all.byFreq", allPath + ".");
			writeCollection(l, 
			allPath, 
			false, 
			null, 
			totalWords);
			
			return "Word list was created";
		} catch (Throwable e) {
			e.printStackTrace();
			String message = "Word list was not created. " + e.getMessage();
			publishProgress(message);
			return message;
		} finally {
			wl.release();
		}
	}

	private void getWL(File fileP, TreeSet<DoubleComparableEntry<String, DoubleComparableEntry<Integer, String>>> allSet) throws IOException {
		
		TreeSet<DoubleComparableEntry<String, DoubleComparableEntry<Integer, String>>> set = 
				getWordList(fileP);
		
		writeCollection(set, 
		saveTo + fileP + ".byAlpha.html", 
		true, 
		null, 
		0);
		
		List<DoubleComparableEntry<String, DoubleComparableEntry<Integer, String>>> l = 
				new ArrayList<DoubleComparableEntry<String, DoubleComparableEntry<Integer, String>>>(set.size());
		l.addAll(set);
		
		set = null;
		Collections.sort(l, new DoubleComparableEntry.RevertValueOrder<String, DoubleComparableEntry<Integer, String>>());
		writeCollection(l, 
		saveTo + fileP + ".byFreq.html", 
		false, 
		allSet, 
		0);
	}
	
	public static TreeSet<DoubleComparableEntry<String, DoubleComparableEntry<Integer, String>>> getWordList(File file) throws FileNotFoundException, IOException {
		FileReader fis = new FileReader(file);
		BufferedReader bis = new BufferedReader(fis);
		char[] barr = new char[64];
		int idxB = 0;
		int b = 0;
		String s = "";
		TreeSet<DoubleComparableEntry<String, DoubleComparableEntry<Integer, String>>> set = new TreeSet<DoubleComparableEntry<String, DoubleComparableEntry<Integer, String>>>();
		DoubleComparableEntry<String, DoubleComparableEntry<Integer, String>> e, en;

		while (bis.ready()) {
			b = bis.read();
			if (" Ã¢ÂÂÃ¢ÂÂÃ¯Â»Â¿ Ã¢ÂÂÃ¢ÂÂ¦Ã¢ÂÂÃ¢ÂÂ\r\n\f\t0123456789@#Ã¢ÂÂ«$%&*-+({}ÃÂ·ÃÂÃÂ£ÃÂ¢Ã¢ÂÂ¬)!.,\"':;/?_[]=~`|^Ã¢ÂÂ¢ÃÂ®ÃÂ©ÃÂ¶<>\\".indexOf(b) >= 0) {
				if (idxB > 0) {
					s = new String(barr, 0, idxB).toLowerCase();
					idxB = 0;
					e = new DoubleComparableEntry<String, DoubleComparableEntry<Integer, String>>(s, new DoubleComparableEntry<Integer, String>(1, ""));
					if ((en = set.floor(e)) != null && en.equals(e)) {
						DoubleComparableEntry<Integer, String> value = en.getValue();
						value.setKey(value.getKey() + 1);
					} else {
						set.add(e);
					}
				} else {
					// skip
				}
			} else {
				barr[idxB++] = (char)b;
			}
		}
		bis.close();
		fis.close();
//		for (DoubleComparableEntry<String, Integer> ee : set) {
//			System.out.println(ee.getKey() + ": " + ee.getValue());
//		}
		return set;
	}

	public void writeCollection(
			Collection<DoubleComparableEntry<String, DoubleComparableEntry<Integer, String>>> set, 
			String fName, 
			boolean useDict, 
			TreeSet<DoubleComparableEntry<String, DoubleComparableEntry<Integer, String>>> allSet, 
			int total) throws IOException {
		
		FileWriter fw = new FileWriter(fName);

		retL.add(fName);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(WORDLIST_TITLE);
		long totLen = 0;
		for (File st : lf) {
			long length = st.length();
			totLen += length;
			bw.write(st.getAbsolutePath() + ": " + Util.nf.format(length) + " bytes.<br/>");
		}
		bw.write("Total of Bytes: " + Util.nf.format(totLen) + " bytes.<br/>");
		bw.write("Number of Words: " + Util.nf.format(set.size()) + "<br/>");
		if (total != 0) {
			bw.write("Total Words: " + Util.nf.format(total) + "<br/>");
			bw.write("Rate: " + Util.nf.format((float)set.size() * 100/total) + "%<br/>");
		}

		bw.write("<div align='center'>\r\n"
				+ "<table border='0' cellspacing='0' cellpadding='0' width='100%' style='width:100.0%;border-collapse:collapse'>\r\n"
				);
		StringBuilder value = new StringBuilder("<tr>")
		.append(TD1).append("No").append("</td>")
		.append(TD1).append("Word").append("</td>")
		.append(TD1).append("Frequent").append("</td>");
		if (stardictL != null) {
			value.append(TD1).append("Definition").append("</td>");
		}
		value.append("</tr>");
		bw.write(value.toString());
		int c = 0;
		int accu = 0;
		DoubleComparableEntry<String, DoubleComparableEntry<Integer, String>> eAll;
		DoubleComparableEntry<Integer, String> value2;
		for (DoubleComparableEntry<String, DoubleComparableEntry<Integer, String>> ee : set) {
			String key = ee.getKey();
			c++;
			value2 = ee.getValue();
			if (stardictL != null && useDict) {
				StringBuilder sb = new StringBuilder();
				int l = stardictL.size();
				for (int i = 0; i < l; i++) {
					List<String> readDef = stardictL.get(i).readDef(key);
					if (readDef != null && readDef.size() > 0) {
							sb.append(Util.collectionToString(readDef, false, "<br/><hr/>"));
					} else {
						sb.append("(not found)");
					}
					if (i < l - 1) {
						sb.append("<hr/>");
					}
				}
				value2.setValue(sb.toString());
			} else {
			}
			Integer key2 = value2.getKey();
			value = new StringBuilder("<tr>")
			.append(TD1).append(c).append("</td>")
			.append(TD1).append(key).append("</td>");
			if (total == 0) {
				value.append(TD1).append(key2).append("</td>");
				if (stardictL != null) {
					value.append(TD1).append(value2.getValue().replaceAll("\\n", "<br/>").replaceAll("\\\\", "\\")).append("</td>");
				}
				value.append("</tr>");
			} else {
				accu += key2;
				value.append(TD1).append(key2).append(" (" + Util.nf.format((float)key2 * 100/total) + "% / " + Util.nf.format((float)accu * 100 / total) + "%)").append("</td>");
				if (stardictL != null) {
					value.append(TD1).append(value2.getValue().replaceAll("\\n", "<br/>").replaceAll("\\\\", "\\")).append("</td>");
				}
				value.append("</tr>");
			}
			if (allSet != null) {
				totalWords += key2;
				if ((eAll = allSet.floor(ee)) != null && eAll.equals(ee)) {
					DoubleComparableEntry<Integer, String> v = eAll.getValue();
					v.setKey(v.getKey() + key2);
				} else {
					allSet.add(ee);
				}
			} 
			//System.out.println(value);
			bw.write(value.toString());
			bw.newLine();
		}

		bw.write("</table></div>Took " + Util.nf.format((System.currentTimeMillis() - start)) + " milliseconds.</body></html>");
		bw.flush();
		bw.close();
	}

	@Override
	protected void onPostExecute(String result) {
		Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
		Log.d(TAG, result);
		
	}
	
	protected void onProgressUpdate(String... progress) {
		if (progress != null && progress.length > 0 
			&& progress[0] != null && progress[0].trim().length() > 0) {
			Toast.makeText(activity, progress[0], Toast.LENGTH_LONG).show();
			Log.d(TAG, progress[0]);
		}
	}
}

