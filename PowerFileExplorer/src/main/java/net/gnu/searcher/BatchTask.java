package net.gnu.searcher;

import java.io.File;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import java.util.*;
import net.gnu.util.FileUtil;
import net.gnu.util.Util;
import net.gnu.util.FileSorter;

public class BatchTask  extends AsyncTask<Void, Void, String> {

	private BatchFragment batchFrag = null;
	private Activity activity;
	String filePaths;
	private static final String TAG = "BatchTask";
	List<File> newFileList3;

	public BatchTask(BatchFragment batchFrag, String filePaths) {
		this.batchFrag = batchFrag;
		activity = batchFrag.getActivity();
		this.filePaths = filePaths;
	}

	protected String doInBackground(Void... s) {
		String[] stringExtra = Util.stringToArray(filePaths, "|");
		Collection<File> files = FileUtil.getFiles(stringExtra, false);
		
		if (newFileList3 == null) {
			newFileList3 = filteringConditions(files);
		}
		
		if ("delete".equals(batchFrag.command)) {//}"".equals(batchFrag.saveTo) && "".equals(batchFrag.patternFrom) && "".equals(batchFrag.patternTo)) {
			for (File f : newFileList3) {
				f.delete();
			}
			return "Deleting Task was successful";
		}
		
		if (batchFrag.byDate) {
			Collections.sort(newFileList3, new FileSorter(FileSorter.DIR_TOP, FileSorter.DATE, FileSorter.ASCENDING));
		} else if (batchFrag.byName) {
			Collections.sort(newFileList3, new FileSorter(FileSorter.DIR_TOP, FileSorter.NAME, FileSorter.ASCENDING));
		} else {
			Collections.sort(newFileList3, new FileSorter(FileSorter.DIR_TOP, FileSorter.SIZE, FileSorter.ASCENDING));
		}
		boolean rename = false;
		List<File> newFileList5 = new LinkedList<File>();
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumIntegerDigits(6);
		if ("".equals(batchFrag.patternFrom) && !"".equals(batchFrag.patternTo)) {
			int i = 0;
			List<File> newFileList4 = new LinkedList<File>();
			for (File f : newFileList3) {
				String name = f.getName();
				int lastIndexOf = name.lastIndexOf(".");
				name = lastIndexOf >= 0 ? name.substring(lastIndexOf) : "";
				File file = new File(f.getParent() + "/" + batchFrag.patternTo + "-" + nf.format(++i) + name + "000000");
				f.renameTo(file);
				newFileList4.add(file);
			}
			
			for (File f : newFileList4) {
				String name = f.getAbsolutePath();
				File file = new File(name.substring(0, name.length() - 6));
				f.renameTo(file);
				newFileList5.add(file);
				rename = true;
			}
			if ("".equals(batchFrag.saveTo) && rename)
				return "Renaming Task was successful";
		} else if (!"".equals(batchFrag.patternFrom)) {
			int i = 0;
			List<File> newFileList4 = new LinkedList<File>();
			for (File f : newFileList3) {
				String name = f.getName();
				Log.i(name, name.replaceAll(batchFrag.patternFrom, batchFrag.patternTo));
				File file = new File(f.getParent() + "/" + name.replaceAll(batchFrag.patternFrom, batchFrag.patternTo).replace("{d}", nf.format(++i)) + "000000");
				f.renameTo(file);
				newFileList4.add(file);
			}

			for (File f : newFileList4) {
				String name = f.getAbsolutePath();
				File file = new File(name.substring(0, name.length() - 6));
				f.renameTo(file);
				newFileList5.add(file);
				rename = true;
			}
			if ("".equals(batchFrag.saveTo) && rename)
				return "Renaming Task was successful";
		} else {
			newFileList5.addAll(newFileList3);
		}
		
		if (!"".equals(batchFrag.saveTo)) {
			boolean move = false;
			for (File f : newFileList5) {
				File file = new File((batchFrag.saveTo + f.getAbsolutePath()));
				File parentFile = file.getParentFile();
				if (!parentFile.exists()) {
					parentFile.mkdirs();
				}
				f.renameTo(file);
				move = true;
			}
			if (!rename && move) {
				return "Moving Task was successful";
			} else if (rename && !move) {
				return "Renaming Task was successful";
			} else if (rename && move) {
				return "Renaming and Moving Task was successful";
			} else {
				return "Nothing to do";
			}
		} else {
			if (rename) {
				return "Renaming Task was successful";
			} else {
				return "Nothing to do"; // delete
			}
		}
	}

	List<File> filteringConditions(Collection<File> files) throws NumberFormatException {
		LinkedList<File> newFileList1 = new LinkedList<File>();
		if (batchFrag.dateFrom.length() > 0 && batchFrag.dateTo.length() > 0) {
			for (File f : files) {
				Calendar from = Calendar.getInstance();
				String[] fr = batchFrag.dateFrom.split("\\s*[/-]\\s*");
				from.set(Integer.parseInt(fr[2]), Integer.parseInt(fr[1]), Integer.parseInt(fr[0]), 0, 0, 0);
				Log.i(TAG, from.toString());
				Log.i(f.lastModified() + "", from.getTimeInMillis() + "/" + from.getTimeInMillis());
				Calendar toCal = Calendar.getInstance();
				String[] to = batchFrag.dateTo.split("\\s*[/-]\\s*");
				toCal.set(Integer.parseInt(to[2]), Integer.parseInt(to[1]), Integer.parseInt(to[0]), 0, 0, 0);
				Log.i(TAG, toCal.toString());
				Log.i(f.lastModified() + "", from.getTimeInMillis() + "/" + toCal.getTimeInMillis());
				if (from.getTimeInMillis() <= f.lastModified() && f.lastModified() <= toCal.getTimeInMillis()) {
					newFileList1.add(f);
				}
			}
		}
		else if (batchFrag.dateFrom.length() > 0) {
			for (File f : files) {
				Calendar from = Calendar.getInstance();
				String[] fr = batchFrag.dateFrom.split("\\s*[/-]\\s*");
				from.set(Integer.parseInt(fr[2]), Integer.parseInt(fr[1]), Integer.parseInt(fr[0]), 0, 0, 0);
				Log.i(TAG, from.toString());
				Log.i(TAG, f.lastModified() + "/" + from.getTimeInMillis());
				if (from.getTimeInMillis() <= f.lastModified())
				{
					newFileList1.add(f);
				}
			}
		} else if (batchFrag.dateTo.length() > 0) {
			for (File f : files) {
				Calendar toCal = Calendar.getInstance();
				String[] to = batchFrag.dateTo.split("\\s*[/-]\\s*");
				toCal.set(Integer.parseInt(to[2]), Integer.parseInt(to[1]), Integer.parseInt(to[0]), 0, 0, 0);
				Log.i(TAG, toCal.toString());
				Log.i(TAG, f.lastModified() + "/" + toCal.getTimeInMillis());
				if (f.lastModified() <= toCal.getTimeInMillis())
				{
					newFileList1.add(f);
				}
			}
		} else {
			newFileList1.addAll(files);
		}
		files = null;
		List<File> newFileList2 = new LinkedList<File>();
		if (batchFrag.sizeFrom >= 0 && batchFrag.sizeTo >= batchFrag.sizeFrom) {
			for (File f : newFileList1) {
				if (batchFrag.sizeFrom <= f.length() && f.length() <= batchFrag.sizeTo) {
					newFileList2.add(f);
				}
			}
		} else if (batchFrag.sizeFrom > 0) {
			for (File f : newFileList1) {
				if (batchFrag.sizeFrom <= f.length()) {
					newFileList2.add(f);
				}
			}
		} else if (batchFrag.sizeTo > 0) {
			for (File f : newFileList1) {
				if (f.length() <= batchFrag.sizeTo) {
					newFileList2.add(f);
				}
			}
		} else {
			newFileList2.addAll(newFileList1);
		}
		newFileList1 = null;
		List<File> newFileList3 = new LinkedList<File>();
		String include = batchFrag.include.trim();
		String exclude = batchFrag.exclude.trim();
		if (include.length() > 0 && exclude.length() > 0) {
			String[] splitIn = include.split(";");
			String[] splitEx = exclude.split(";");
			for (File f : newFileList2) {
				String fName = f.getName();
				for (String in :splitIn) {
					Matcher m = Pattern.compile(in.trim()).matcher(fName);
					if (m.find()) {
						for (String ex :splitEx) {
							Matcher m2 = Pattern.compile(ex.trim()).matcher(fName);
							if (!m2.find()) {
								newFileList3.add(f);
							}
						}
					}
				}
			}
		} else if (include.length() > 0) {
			String[] splitIn = include.split(";");
			for (File f : newFileList2) {
				String fName = f.getName();
				for (String in :splitIn) {
					Matcher m = Pattern.compile(in.trim()).matcher(fName);
					if (m.find()) {
						newFileList3.add(f);
					}
				}
			}
		} else if (exclude.length() > 0) {
			String[] splitEx = exclude.split(";");
			for (File f : newFileList2) {
				String fName = f.getName();
				for (String in :splitEx) {
					Matcher m = Pattern.compile(in.trim()).matcher(fName);
					if (!m.find())
					{
						newFileList3.add(f);
					}
				}
			}
		} else {
			newFileList3.addAll(newFileList2);
		}
		newFileList2 = null;
		return newFileList3;
	}

	@Override
	protected void onPostExecute(String result) {
		Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
		Log.d(TAG, result);
	}
}
