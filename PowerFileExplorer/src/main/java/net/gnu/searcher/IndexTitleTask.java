package net.gnu.searcher;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import net.gnu.util.FileUtil;
import net.gnu.util.Util;
import net.gnu.util.HtmlUtil;
import android.app.Activity;
import net.gnu.androidutil.ForegroundService;
import java.util.Collection;
import java.util.Arrays;
import net.gnu.explorer.ExplorerActivity;
import net.gnu.util.FileSorter;

public class IndexTitleTask extends AsyncTask<String, String, String> {

	private ExplorerActivity activity = null;
	private File dir = null;
	private String pattern;
	private boolean useFolderName;
	private static final String TAG = "IndexTitleTask";
	private IndexTitleFragment indexingFrag;
	
	public IndexTitleTask(IndexTitleFragment indexingFrag) {
		this.indexingFrag = indexingFrag;
		this.dir = new File(indexingFrag.files);
		this.pattern = indexingFrag.pattern;
		this.useFolderName = indexingFrag.useFolderName;
	}

	protected String doInBackground(String... urls) {
		PowerManager pm = (PowerManager)activity.getSystemService(
				Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK,
				TAG);
		wl.acquire();
		try {
			createIndex(dir.getAbsolutePath(), pattern, useFolderName);
			return "Titles were created";
		} catch (Throwable e) {
			e.printStackTrace();
			String message = "Titles were not created. " + e.getMessage();
			publishProgress(message);
			return message;
		} finally {
			wl.release();
			activity.stopService(new Intent(activity, ForegroundService.class));
		}
	}

	public static void createIndex(String parentFolder, String indexPattern, boolean useFolderName) throws IOException {
		File parent = new File(parentFolder);
		int parentStrLength = parent.getAbsolutePath().length() + 1;
		Collection<File> lf = FileUtil.getFilesAndFolder(parent);
		StringBuilder sb = new StringBuilder(ExplorerActivity.HTML_STYLE);
		if (lf.size() == 0) {
			sb.append("No files for listing</body>");
		} else {
			sb.append(ExplorerActivity.HEAD_TABLE);
			File[] fArr = Util.collection2FileArray(lf);
			Arrays.sort(fArr, new FileSorter(FileSorter.DIR_TOP, FileSorter.NAME, FileSorter.ASCENDING));
			// System.out.println(Utils.collectionToString(lf, true, "\n"));
			Pattern pat = Pattern.compile(indexPattern);

			Pattern htmlPattern = Pattern.compile(indexPattern, Pattern.CASE_INSENSITIVE);
			int counter = 0;
			for (File f : fArr) {
				String fAbsolutePath = f.getAbsolutePath();
				Log.d("f", fAbsolutePath);
				if (f.isDirectory()) {
					//				Log.d("isDirectory", fAbsolutePath);
					File[] fs = f.listFiles();
					if (fs != null && fs.length > 0) {
						String retHtml = fAbsolutePath + "/" + f.getName() + "-listing.html";
						File retF = new File(retHtml);
						StringBuilder sb2 = new StringBuilder(ExplorerActivity.HTML_STYLE);
						sb2.append(ExplorerActivity.HEAD_TABLE);
						int prev = sb2.length();
						int counter2 = 0;
						for (File f2 : fs) {
							if (f2.isFile()) {// && !f2.getAbsolutePath().equalsIgnoreCase(retF.getAbsolutePath())) {
								Log.d("f2", f2.getName());
								Matcher m = htmlPattern.matcher(f2.getName());
								if (m.matches()) {
									counter2 = tdContent(fAbsolutePath.length() + 1, sb2, counter2, f2);
								}
							}
						}
						if (sb2.length() != prev) {
							sb2.append("</table></div></body></html>");
							String string = sb2.toString();
							FileUtil.writeFileAsCharset(retF, string, "utf-8");
						}
					}
				}
				String fName = f.getName();
				Matcher mat = pat.matcher(fName);
				if (mat.matches()) {
					if (useFolderName) {
						sb.append("\n<tr><td>").append(++counter).append("</td>")
						.append("<td><a href=\"").append(f.getAbsolutePath().substring(parentStrLength).replaceAll("\\\\", "/"))
						.append("\">").append(f.getParentFile().getName())
						.append("</a></td></tr>");
					} else if (f.isFile()) {
						counter = tdContent(parentStrLength, sb, counter, f);
					}
				}
			}
		}
		sb.append("</table></div></body></html>");
		String retHtml = parentFolder + "/" + parent.getName() + "-listing.html";
		FileUtil.writeFileAsCharset(new File(retHtml), sb.toString(), "utf-8");
	}

	private static int tdContent(int parentStrLength, StringBuilder sb,
			int counter, File f) throws IOException {
		String fileContent = FileUtil.readFileAsCharset(f, "utf-8");
		String tagValue = HtmlUtil.getTagValue("title", fileContent);
		Log.d("title", tagValue);
		if (tagValue.length() > 0) {
			sb.append("\n<tr><td>").append(++counter).append("</td>")
			.append("<td><a href=\"").append(f.getAbsolutePath().substring(parentStrLength).replaceAll("\\\\", "/"))
			.append("\">").append(Util.replace(tagValue, 
					new String[]{"&rsquo;", "&lsquo;", "&ndash;", "&nbsp;", "&quot;", "&apos;",
					"&lt;", "&gt;", "&ldquo;", "&rdquo;", "&hellip;", "&amp;"}, 
					new String[] {"Ã¢ÂÂ", "Ã¢ÂÂ", "Ã¢ÂÂ", " ", "\"", "\'", "<", ">", "Ã¢ÂÂ", "Ã¢ÂÂ", "Ã¢ÂÂ¦", "&"},
					false, false))
					.append("</a></td></tr>\n");
		} else {
			sb.append("<tr><td>").append(++counter).append("</td>")
			.append("<td><a href=\"").append(f.getAbsolutePath().substring(parentStrLength))
			.append("\">").append(f.getParentFile().getName())
			.append("</a></td></tr>\n");
		}
		return counter;
	}

	@Override
	protected void onPostExecute(String result) {
		Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
		Log.d(TAG, result + ".");
		//try {
			String dirListing = dir.getAbsolutePath() + "/" + dir.getName() + "-listing.html";
			Log.d("dirListing", dirListing);
			//activity.curFrag.currentUrl = new File(dirListing).toURI().toURL().toString();
			indexingFrag.mBtnConfirm.setText("Index");
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		}
		//(activity.curFrag.webTask = new WebTask(activity.curFrag, activity.curFrag.webView, activity.curFrag.currentUrl, true, result)).execute();

	}

	protected void onProgressUpdate(String... progress) {
		if (progress != null && progress.length > 0 
				&& progress[0] != null && progress[0].trim().length() > 0) {
			Toast.makeText(activity, progress[0], Toast.LENGTH_LONG).show();
			Log.d(TAG, progress[0]);
		}
	}
}

