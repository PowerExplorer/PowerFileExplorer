package net.gnu.p7zip;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;
import java.util.regex.*;
import java.io.*;
import net.gnu.util.*;

public class ExtractFile {
	
	private File file = null;
	private String outputDirOrCompressionLevel = "";
//	private Archive arch = null;
	private Iterator<String> iterOfEntries = null;
	private String filePath = "";
//	private FileHeader curFileHeader = null;
	private Collection<String> fList = null;
	private Andro7za andro7z = null;
	private String fNameLower = "";
	
	private String type = "";
	private String password = "";
	
	public ExtractFile() {
	}
	
	public ExtractFile(File file, File outputDirOrCompressionLevel) throws IOException, Exception {
		this(file.getAbsolutePath(), outputDirOrCompressionLevel.getAbsolutePath());
	}
	
	public ExtractFile(String f, String outputDirOrCompressionLevel) throws IOException {
		this.file = new File(f);
		filePath = file.getAbsolutePath();
		fNameLower = file.getName().toLowerCase();
		this.outputDirOrCompressionLevel = outputDirOrCompressionLevel;
		Log.d("initializing ExtractFile", filePath);
		init();
	}

	public static void compressAFileOrFolderRecursiveTo7Z(
		File file, String zipFileName, String excepts, String includes)
	throws IOException {
		// String folderName;
		Collection<File> filesNeedCompress;
		Log.d(file.toString(), "" + file.exists());
		if (file.isDirectory()) {
			filesNeedCompress = FileUtil.getFiles(file.listFiles(), false);
		} else if (file.isFile()) {
			filesNeedCompress = new LinkedList<File>();
			filesNeedCompress.add(file);
		} else {
			return;
		}
		Log.d("list", Util.collectionToString(filesNeedCompress, true, "\r\n"));

		Pattern patExcept = Pattern.compile(excepts, Pattern.CASE_INSENSITIVE);

		if (zipFileName == null || zipFileName.trim().length() == 0) {
			zipFileName = file.getAbsolutePath() + ".7z";
		}

		String zipFileTmp = zipFileName + ".tmp";
		BufferedWriter bw = new BufferedWriter(new FileWriter(zipFileTmp));
		try {
			int filePathLength = file.getAbsolutePath().length() + 1;
			for (File f : filesNeedCompress) {
				String fPath = f.getAbsolutePath();
				if (patExcept.matcher(fPath).matches()) { // !
					bw.write(fPath, filePathLength, fPath.length()
							 - filePathLength);
					bw.newLine();
				}
			}
		} finally {
			bw.flush();
			bw.close();
		}
		new Andro7za().compress(zipFileName, "-t7z", "", "-xr@" + zipFileTmp,
								"-ir!" + file.getAbsolutePath() + "/" + includes);
		new File(zipFileTmp).delete();
	}
	
	public boolean isClosed() {
		return /*arch == null && */fList == null;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setOutputDirOrCompressionLevel(String outputDirOrCompressionLevel) {
		this.outputDirOrCompressionLevel = outputDirOrCompressionLevel;
	}

	public String getOutputDirOrCompressionLevel() {
		return outputDirOrCompressionLevel;
	}

	private void init() throws IOException {
//		if (arch != null) {
//			arch.close();
//			arch = new Archive(file);
//			curFileHeader = null;
//			Log.d("reinited rar", filePath);
//		} else 
		if (andro7z != null) {
			iterOfEntries = fList.iterator();
			Log.d("reinited andro7z", filePath);
//		} else if (fNameLower.endsWith(".rar")) {
//			arch = new Archive(file);
//			curFileHeader = null;
//			Log.d("inited arch", filePath);
		} else {
			andro7z = new Andro7za();
			fList = andro7z.listing(filePath, password);
			iterOfEntries = fList.iterator();
			Log.d("inited andro7z", filePath);
		}
	}
	
	public void copyTo(ExtractFile ef) throws IOException {
		Log.d("copyTo ExtractFile", filePath);
		ef.file = file;
		ef.filePath = filePath;
		ef.type = type;
		ef.password = password;
		ef.outputDirOrCompressionLevel = outputDirOrCompressionLevel;
		ef.fNameLower = fNameLower;
		ef.fList = fList;
//		if (arch != null) {
//			ef.arch = new Archive(file);
//		} else {
			ef.andro7z = new Andro7za();
			iterOfEntries = fList.iterator();
		//}
		ef.init();
	}

	public String getNextEntry() {
		String entry = null;
//		if (arch != null) {
//			curFileHeader = arch.nextFileHeader();
//			if (curFileHeader != null) {
//				entry = curFileHeader.getFileNameString().replaceAll("\\\\", "/");
//				if (curFileHeader.isDirectory() && !entry.endsWith("/")) {
//					entry = entry + "/";
//				}
//			}
//		} else 
		if (andro7z != null) {
			if (iterOfEntries.hasNext()) {
				entry = iterOfEntries.next();
			}
		}
		Log.d("getNextEntry", entry + "");
		return entry;
	}
	
//	private void extractCurFHRar(boolean overwrite) throws IOException, RarException {
//		Log.d("extractCurFHRar", curFileHeader.getFileNameString());
//		String extractPath = outputDirOrCompressionLevel + "/" + curFileHeader.getFileNameString().replaceAll("\\\\", "/");
//		Log.d("extractPath", extractPath);
//		File extractFile = new File(extractPath);
//		if (extractFile.exists() && !overwrite) {
//			return;
//		}
//		File parentFile = extractFile.getParentFile();
//		if (!parentFile.exists()) {
//			parentFile.mkdirs();
//		}
//		File storeFileNameTmp = new File(extractPath + ".tmp");
//		FileOutputStream fos = new FileOutputStream(storeFileNameTmp);
//		BufferedOutputStream bos = new BufferedOutputStream(fos);
//		try {
//			arch.extractFile(curFileHeader, bos);
//		} finally {
//			bos.flush();
//			fos.flush();
//			bos.close();
//			fos.close();
//		}
//		extractFile.delete();
//		storeFileNameTmp.renameTo(extractFile);
//	}
	
	public void close() {
		Log.d("ExtractFile close", /*arch + */", " + fList);
//		if (arch != null) {
//			curFileHeader = null;
//			try {
//				arch.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			arch = null;
//		}
		andro7z = null;
		fList = null;
	}
	
	// thuong goi khi moi new
	public String extractFile(String entryName) throws IOException, Exception {
		Log.d("extractFile", entryName);
		String fName = outputDirOrCompressionLevel + "/" + entryName;
//		boolean finished = false;
//		if (arch != null) {
//			if (fList != null && !fList.contains(entryName)) {
//				return "";
//			}
//			init();
//			Log.d("extract rar", entryName);
//			while (!finished && (curFileHeader = arch.nextFileHeader()) != null) {
//				if (curFileHeader.getFileNameString().replaceAll("\\\\", "/").equalsIgnoreCase(entryName)) {
////					Log.d("fh.getFileNameString()", fh.getFileNameString());
////					Log.d("fh.isDirectory()", fh.isDirectory() + "");
//					extractCurFHRar(false);
//					finished = true;
//				}
//			}
//		} else 
		if (andro7z != null) {
			Log.d("extract by 7z", entryName);
			if (fList.contains(entryName)) {
				andro7z.extract(filePath, "-aos", password, outputDirOrCompressionLevel, entryName);
				Log.d("extracted by 7z", entryName);
			}
		} 
		return fName;
	}

	public void extractEntries(Collection<String> entryNames, boolean overwrite) throws Exception {
		Log.d("extractEntries", " " + entryNames);
//		int count = 0;
//		int size = entryNames.size();
//		if (arch != null) {
//			init();
//			while (count < size && ((curFileHeader = arch.nextFileHeader()) != null)) {
//				String entryName = curFileHeader.getFileNameString().replaceAll("\\\\", "/");
//				if (entryNames.contains(entryName)) {
//					count++;
//					extractCurFHRar(overwrite);
//				}
//			}
//		} else 
		if (andro7z!= null) {
			List<String> l = new ArrayList<String>(entryNames.size());
			for (String entryName : entryNames) {
				if (fList.contains(entryName) && (!new File(outputDirOrCompressionLevel + "/" + entryName).exists() || overwrite)) {
					l.add(entryName);
				}
			}
//			for (int i = entryNames.size() - 1; i >= 0; i--) {
//				String entryName = entryNames.get(i);
//				if (!fList.contains(entryName) || new File(outputDirOrCompressionLevel + "/" + entryName).exists() && !overwrite) {
//					entryNames.remove(i);
//				}
//			}
			andro7z.extract(filePath, "-aos", password, outputDirOrCompressionLevel, l);
		}
	}
	
	public String fileNames2UrlStr(Collection<String> entryNames, boolean number, String sep) {
		Log.d("fileNames2UrlStr", filePath);
		StringBuilder sb = new StringBuilder();
		int counter = 0;
		for (String entryName : entryNames) {
			if (!number) {
				sb.append("<a href=\"").append("file:/").append(filePath).append("/").append(
					entryName).append("\">").append(entryName).append("</a>").append(sep);
			} else {
				sb.append(++counter + ": ")
					.append("<a href=\"").append("file:/").append(filePath).append("/").append(
					entryName).append("\">").append(entryName).append("</a>").append(sep);
			}
		}
		return sb.toString();
	}

//	public String rar2UrlStr(String folderName, boolean number, String sep) throws IOException {
//		Log.d("rar2UrlStr", filePath + ", folder:" + folderName);
//		long start = System.currentTimeMillis();
//		List<String> entryNames = null;
//		if (fList == null) {
//			init();
//			fList = new LinkedList<String>();
//			arch.getMainHeader().print();
//			int counter = 0;
//			while ((curFileHeader = arch.nextFileHeader()) != null) {
//				counter++;
//				String entryName = curFileHeader.getFileNameString().replaceAll("\\\\", "/");
//				//Log.d("rar2UrlStr entryName", entryName);
//				fList.add(curFileHeader.isDirectory() && !entryName.endsWith("/") ? entryName + "/" : entryName);
//			}
//			Log.d("Loaded 3, total files", counter + " files, took: "
//				  + (System.currentTimeMillis() - start) + " milliseconds.");
//		}
//		entryNames = filesFoldersInFolder(fList, folderName);
//		Log.d("no files in folder", entryNames.size() + ".");
//		return fileNames2UrlStr(entryNames, number, sep);
//	}

	public String andro2UrlStr(String folderName, boolean number, String sep) throws Exception {
		Log.d("andro2UrlStr", filePath + ", folder:" + folderName);
		long start = System.currentTimeMillis();
		Collection<String> entryNames;
		
		int counter = 0;
		if (fNameLower.endsWith(".chm")) {
			entryNames = new LinkedList<String>();
			for (String name : fList) {
				counter++;
				if (!("/".equals(name) || name.matches("(::DataSpace|/\\$WWKeywordLinks|/\\$WWAssociativeLinks|/#IDXHDR|/#ITBITS|/#STRINGS|/#SYSTEM|/#TOPICS|/#URLSTR|/#WINDOWS|/#URLTBL|/\\$FIftiMain|/\\$OBJINST).*"))) {
					entryNames.add(name.substring(1, name.length()));
				}
			}
		} else {
			counter = fList.size();
			entryNames = fList;
		}
		Log.d("Loaded 4, total files", counter + " files, took: "
			  + (System.currentTimeMillis() - start) + " milliseconds.");
		entryNames = filesFoldersInFolder(entryNames, folderName);
		Log.d("no files in folder", entryNames.size() + ".");
		return fileNames2UrlStr(entryNames, number, sep);
	}

	/**
	 * folderName rong hoac co / o cuoi
	 */
	private List<String> filesFoldersInFolder(Collection<String> entryNames, String folderName) {
		Log.d("filesFoldersInFolder", folderName + " ");
		String folderLowerCase = folderName.toLowerCase();
		int folderLength = folderName.length();
		List<String> list = new LinkedList<String>();
		for (String name : entryNames) {
			//Log.d("name, folderName", name + ", " + folderName);
			if (name.toLowerCase().startsWith(folderLowerCase) && name.length() > folderLength) {
				int indexOf = name.indexOf("/", folderLength);
				String substring = name.substring(0, indexOf + 1); //substring luon "" neu indexOf == -1, khong dung thang list.add(substring) duoc
				if ((indexOf < 0 // file
					|| indexOf == name.length() - 1)// for folder of zip
					&& !list.contains(name))
				{
					list.add(name);
					Log.d("list.add(name):", name + " in folder " + folderName);
				} else if (!list.contains(substring)){
					list.add(substring);
					Log.d("list.add(substring):", substring + " in folder " + folderName);
				}
			}
		}
		return list;
	}
	
	public String compressedFile2UrlStr(String folderName, boolean number, String sep) throws IOException, Exception {
		Log.d("compressedFile2UrlStr", filePath + ", " + folderName);
//		if (fNameLower.endsWith(".rar")) {
//			return rar2UrlStr(folderName, number, sep);
//		} else {
			return andro2UrlStr(folderName, number, sep);
		//}
	}

	public static void main(String[] args) throws IOException, Exception {

		ExtractFile ar = new ExtractFile("/storage/emulated/0/rar/httpcomponents-client-4.3.6-src.tar.gz", "/sdcard/tmp");
		ar.compressedFile2UrlStr("httpcomponents-client-4.3.6/", true, "\n");
		ar.extractFile("httpcomponents-client-4.3.6/README.txt");
		ar = new ExtractFile("/storage/emulated/0/rar/stardict-cmupd-2.4.2.tar.bz2", "/sdcard/tmp");
		ar.extractFile("stardict-cmupd-2.4.2/cmupd.ifo");

	}
}
