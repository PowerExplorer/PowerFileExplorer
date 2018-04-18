//package net.gnu.util;
//
//import java.util.*;
//import java.util.zip.*;
//import java.io.*;
//import java.util.regex.*;
//import android.util.*;
//
//public class ZipUtils {
//	/**
//	 * 
//	 * @param file
//	 *            zip file
//	 * @return
//	 * @throws ZipException
//	 * @throws IOException
//	 */
//	public static List<ZipEntry> loadFiles(File file) throws ZipException,
//	IOException {
//		return loadFiles(file, "");
//	}
//
//	/**
//	 * 
//	 * @param zipFile
//	 *            zip file
//	 * @param folderName
//	 *            folder to load
//	 * @return list of entry in the folder
//	 * @throws ZipException
//	 * @throws IOException
//	 */
//	public static List<ZipEntry> loadFiles(File zipFile, String folderName)
//	throws ZipException, IOException {
//		Log.d("loadFiles", folderName);
//		long start = System.currentTimeMillis();
//		FileInputStream fis = new FileInputStream(zipFile);
//		BufferedInputStream bis = new BufferedInputStream(fis);
//		ZipInputStream zis = new ZipInputStream(bis);
//
//		ZipEntry zipEntry = null;
//		List<ZipEntry> list = new LinkedList<ZipEntry>();
//		String folderLowerCase = folderName.toLowerCase();
//		int counter = 0;
//		while ((zipEntry = zis.getNextEntry()) != null) {
//			counter++;
//			String zipEntryName = zipEntry.getName();
//			Log.d("loadFiles.zipEntry.getName()", zipEntryName);
//			if (zipEntryName.toLowerCase().startsWith(folderLowerCase)
//				&& !zipEntry.isDirectory()) {
//				list.add(zipEntry);
//			}
//		}
//		Log.d("Loaded 1", counter + " files, took: "
//			  + (System.currentTimeMillis() - start) + " milliseconds.");
//		FileUtil.close(zis, bis, fis);
//		return list;
//	}
//
//	public static void compressAFileToZip(File zip, File source) throws IOException {
//		Log.d("source: " + source.getAbsolutePath(), ", length: " + source.length());
//		Log.d("zip:", zip.getAbsolutePath());
//		zip.getParentFile().mkdirs();
//		File tempFile = new File(zip.getAbsolutePath() + ".tmp");
//		FileOutputStream fos = new FileOutputStream(tempFile);
//		BufferedOutputStream bos = new BufferedOutputStream(fos);
//		ZipOutputStream zos = new ZipOutputStream(bos);
//		ZipEntry zipEntry = new ZipEntry(source.getName());
//		zipEntry.setTime(source.lastModified());
//		zos.putNextEntry(zipEntry);
//
//		FileInputStream fis = new FileInputStream(source);
//		BufferedInputStream bis = new BufferedInputStream(fis);
//		byte[] bArr = new byte[32768];
//		int byteRead = 0;
//		try {
//			while ((byteRead = bis.read(bArr)) != -1) {
//				zos.write(bArr, 0, byteRead);
//			}
//			zos.flush();
//			zos.closeEntry();
//			Log.d("zipEntry: " + zipEntry, "compressedSize: "
//				  + zipEntry.getCompressedSize());
//			bos.flush();
//			fos.flush();
//		} finally {
//			FileUtil.close(zos, bos, fos);
//			FileUtil.close(bis, fis);
//			zip.delete();
//			tempFile.renameTo(zip);
//		}
//	}
//
//	public static void writeToZipFileAsDateTime(File source) throws IOException {
//		String dateTime = new Date(System.currentTimeMillis()).toString()
//			.replaceAll("[:/]", ".");
//		String sourceAbsolute = source.getAbsolutePath();
//		Log.d("sourceAbsolute: ", sourceAbsolute);
//		String fullName = (sourceAbsolute + "-" + dateTime + ".zip");
//		compressAFileToZip(new File(fullName), source);
//	}
//
//	/**
//	 * Compress a String to a zip file that has only one entry like zipName
//	 * The name shouldn't have .zip. Charset UTF-8
//	 * 
//	 * @param content
//	 * @param fName
//	 * @throws IOException 
//	 */
//	public static void zipAContentAsFileName(String content, String fName) throws IOException {
//		zipAContentAsFileName(fName, content, "UTF-8");
//	}
//	
//	/**
//	 * Compress a String to a zip file that has only one entry like zipName
//	 * The name shouldn't have .zip
//	 * 
//	 * @param content
//	 * @param fName
//	 * @throws IOException 
//	 */
//	public static void zipAContentAsFileName(String fPath, String content, String charset) throws IOException {
//		File f = new File(fPath);
//		File parentFile = f.getParentFile();
//		if (!parentFile.exists()) {
//			parentFile.mkdirs();
//		}
//		ZipOutputStream zos = new ZipOutputStream(
//			new BufferedOutputStream(new FileOutputStream(fPath + ".zip")));
//
//		ZipEntry entry = new ZipEntry(f.getName());
//		zos.putNextEntry(entry);
//		byte[] barr = content.getBytes(charset);
//		zos.write(barr, 0, barr.length);
//		FileUtil.flushClose(zos);
//	}
//
//	/**
//	 * 
//	 * @param zfile
//	 *            zip file
//	 * @param entry
//	 *            entry to save to disk
//	 * @throws ZipException
//	 * @throws IOException
//	 */
//	public static File extractAZipEntryToDisk(File zfile, ZipEntry entry, String outputFolder)
//	throws ZipException, IOException {
//		Log.d("saveZipEntryToDisk.zfile", zfile.toString());
//		Log.d("saveZipEntryToDisk.entry", entry.toString());
//		Log.d("saveZipEntryToDisk.outputFolder", outputFolder);
//		ZipFile zipFile = new ZipFile(zfile);
//		String zipEntryName = outputFolder + "/" + entry.getName();
//		if (!entry.isDirectory()) {
//			new File(zipEntryName).getParentFile().mkdirs();
//			InputStream inputStream = zipFile.getInputStream(entry);
//			FileUtil.is2File(inputStream, zipEntryName);
//		}
//		FileUtil.close(zipFile);
//		return new File(zipEntryName);
//	}
//	
//	public static File extractAZipEntry(String inFile, String entryName, String outDirFilePath) 
//	throws IOException {
//		Log.d("extractAZipEntry", inFile);
//		FileInputStream fis = new FileInputStream(inFile);
//		BufferedInputStream bis = new BufferedInputStream(fis);
//		ZipInputStream zis = new ZipInputStream(bis);
//		try {
//			ZipEntry ze;
//			while ((ze = zis.getNextEntry()) != null) {
//				String zeName = ze.getName();
//				System.out.println("zeName, entryName " + zeName.toString() + ", " + entryName);
//				if (!ze.isDirectory() && zeName.equals(entryName)) {
//					return extractAZipEntryToDisk(new File(inFile), ze, outDirFilePath);
//				}
//			}
//		} catch (IOException e) {
//			Log.d("readZipEntry", e.getMessage(), e);
//		} finally {
//			FileUtil.close(zis, bis, fis);
//		}
//		return null;
//	}
//	
//	public static void extractEntriesInZipToDisk(File zFile, List<ZipEntry> entries, String outputFolder)
//	throws ZipException, IOException {
//		Log.d("extractAFolderInZipToDisk.zfile", zFile.toString());
//		Log.d("extractAFolderInZipToDisk.outputFolder", outputFolder);
//		ZipFile zipF = new ZipFile(zFile);
//		for (ZipEntry zipEntry : entries) {
//			if (!zipEntry.isDirectory()) {
//				String name = outputFolder + "/" + zipEntry.getName();
//				new File(name).getParentFile().mkdirs();
//				InputStream inputStream = zipF.getInputStream(zipEntry);
//				FileUtil.is2File(inputStream, name);
//			}
//		}
//		FileUtil.close(zipF);
//	}
//
//	public static void extractAFolderInZipToDisk(File zipFile, String folderNameInZip, String outputFolder, boolean overwrite) 
//	throws ZipException, IOException {
//		Log.d("extractAFolderInZipToDisk: ", zipFile.toString() + ", folderNameInZip = " + folderNameInZip);
//		long start = System.currentTimeMillis();
//
//		ZipFile zipF = new ZipFile(zipFile);
//		Enumeration<? extends java.util.zip.ZipEntry> en = zipF.entries();
//		ZipEntry zipEntry = null;
//
//		String folderLowerCase = folderNameInZip.toLowerCase();
//		int counter = 0;
//		List<String> failEntries = new LinkedList<String>();
//		outputFolder = outputFolder.endsWith("/") ? outputFolder : outputFolder + "/";
//		while (en.hasMoreElements()) {
//			try {
//				zipEntry = en.nextElement();
//				String zipEntryName = zipEntry.getName();
//				counter++;
//				Log.d("extractAFolderInZipToDisk", zipEntryName);
//				if (zipEntryName.toLowerCase().startsWith(folderLowerCase)
//					&& !zipEntry.isDirectory()) {
//					String name = outputFolder + zipEntryName;
//
//					File f = new File(name);
//					File parentFile = f.getParentFile();
//					if (!parentFile.exists()) {
//						parentFile.mkdirs();
//					}
//					if (!f.exists() || overwrite) {
//						InputStream inputStream = zipF.getInputStream(zipEntry);
//						FileUtil.is2File(inputStream, name);
//					}
//				}
//			} catch (Exception e) {
//				failEntries.add(zipEntry.getName());
//				e.printStackTrace();
//			}
//		}
//		FileUtil.close(zipF);
//		Log.d("Loaded ", counter + " files, took: "
//			  + (System.currentTimeMillis() - start) + " milliseconds.");
//		Log.d("fail extract", Util.collectionToString(failEntries, true, "\n"));
//	}
//
//	public static byte[] readZipEntry(File zfile, ZipEntry entry)
//	throws ZipException, IOException {
//		Log.d("file3: ", zfile.toString());
//		Log.d("zipEntry3: ", entry.toString());
//		ZipFile zipFile = new ZipFile(zfile);
//		if (entry != null && !entry.isDirectory()) {
//			InputStream is = zipFile.getInputStream(entry);
//			byte[] barr = FileUtil.is2Barr(is, true);
//			FileUtil.close(zipFile);
//			return barr;
//		} else {
//			FileUtil.close(zipFile);
//			return new byte[0];
//		}
//	}
//
//	public static String readZipEntryContent(String inFile, String entryName) 
//	throws IOException {
//		Log.d("readZipEntryContent", inFile);
//		ZipFile zf = new ZipFile(inFile);
//		ZipEntry ze = new ZipEntry(entryName);
//		InputStream zis = zf.getInputStream(ze);
//		try {
//			String zeName = ze.getName();
//			System.out.println("ze, entryName " + ze + ", " + entryName);
//			if (!ze.isDirectory() && zeName.equals(entryName)) {
//				int length = (int) ze.getSize();
//				System.out.println("read entryName " + entryName + ", " + length);
//				if (length > 0) {
//					int count = 0;
//					int read = 0;
//					byte[] buffer = new byte[length];
//					while ((count = zis.read(buffer, read, length - read)) != -1) {
//						read += count;
//					}
//					System.out.println("entryName " + ze + " read, size: " + buffer);
//					return new String(buffer, "utf-8");
//				} else {
//					byte[] bb = FileUtil.is2Barr(zis, false);
//					System.out.println("entryName " + ze + " read, size: " + bb.length);
//					return new String(bb, "utf-8");
//				}
//			}
//		}
//		catch (IOException e) {
//			Log.d("readZipEntryContent", e.getMessage(), e);
//			throw e;
//		} finally {
//			FileUtil.close(zis, zf);
//		}
//		return "";
//	}
//
//	public static String readZipEntryContent(String inFile, String prefix, String suffix) 
//	throws IOException {
//		Log.d("readZipEntryContent", inFile);
//		FileInputStream fis = new FileInputStream(inFile);
//		BufferedInputStream bis = new BufferedInputStream(fis);
//		ZipInputStream zis = new ZipInputStream(bis);
//		ZipEntry ze;
//		try {
//			byte[] byteArray = new byte[0];
//			StringBuilder sb = new StringBuilder(65536);
//			ByteArrayOutputStream bb;
//			int length;
//			while ((ze = zis.getNextEntry()) != null) {
//				String zeName = ze.getName();
//				if (!ze.isDirectory() && zeName.startsWith(prefix) 
//					&& zeName.endsWith(suffix)) {
//					int size = (int) ze.getSize();
//					System.out.println("ze, entryName " + ze + ", " + prefix + ", " + suffix + ", " + size);
//					int buflen = 4096;
//					byte[] buffer = new byte[buflen];
//					String string = "\n" + zeName + "\n";
//					byte[] bytes = string.getBytes("utf-8");
//					length = bytes.length;
//
//					bb = new ByteArrayOutputStream((size > 0 ? size : 65536) + length);
//					bb.write(bytes, 0, length);
//					int count = 0;
//					while ((count = zis.read(buffer, 0, buflen)) != -1) {
//						bb.write(buffer, 0, count);
//					}
//					byteArray = bb.toByteArray();
//					sb.append(new String(byteArray));
//					System.out.println("entryName " + ze + " read, size: " + byteArray.length);
//				}
//			}
//			return sb.toString();
//		}
//		catch (IOException e) {
//			Log.d("readZipEntryContent", e.getMessage(), e);
//			throw e;
//		} finally {
//			FileUtil.close(zis, bis, fis);
//		}
//		//return "";
//	}
//
//	/**
//	 * N?n file hay th? m?c v?o file zipFileName
//	 * 
//	 * @param file
//	 * @throws IOException
//	 */
//	public static void compressAFileOrFolderRecursiveToZip(File file) throws IOException {
//		compressAFileOrFolderRecursiveToZip(file, null);
//	}
//
//	/**
//	 * N?n file hay th? m?c v?o file zipFileName
//	 * 
//	 * @param file
//	 * @param zipFileName
//	 * @throws IOException
//	 */
//	public static void compressAFileOrFolderRecursiveToZip(File file, String zipFileName) 
//	throws IOException {
//		String folderName;
//		Collection<File> filesNeedCompress;
//		Log.d(file.toString(), "" + file.exists());
//		if (file.isDirectory()) {
//			filesNeedCompress = FileUtil.getFiles(file.listFiles(), false);
//			folderName = file.getAbsolutePath();
//		} else if (file.isFile()) {
//			filesNeedCompress = new LinkedList<File>();
//			filesNeedCompress.add(file);
//			folderName = file.getParent();
//		} else {
//			return;
//		}
//		Log.d("list", filesNeedCompress.size() + ".");
//
//		if (zipFileName == null || zipFileName.trim().length() == 0) {
//			zipFileName = file.getAbsolutePath() + ".zip";
//		}
//
//		String zipFileNameTmp = zipFileName + ".tmp";
//		File fileZFTmp = new File(zipFileNameTmp);
//		FileOutputStream fos = new FileOutputStream(fileZFTmp);
//		BufferedOutputStream bos = new BufferedOutputStream(fos);
//		ZipOutputStream zos = new ZipOutputStream(bos);
//		zos.setLevel(Deflater.BEST_COMPRESSION);
//
//		try {
//			for (File f : filesNeedCompress) {
//				ZipEntry zipEntry = new ZipEntry(
//					f.getAbsolutePath().substring(folderName.length() + 1));
//				zipEntry.setTime(f.lastModified());
//				zos.putNextEntry(zipEntry);
//				Log.d("compressing", zipEntry.getName());
//				FileInputStream fis = new FileInputStream(f);
//				BufferedInputStream bis = new BufferedInputStream(fis);
//				byte[] bArr = new byte[4096];
//				int byteRead = 0;
//				while ((byteRead = bis.read(bArr)) != -1) {
//					zos.write(bArr, 0, byteRead);
//				}
//				zos.flush();
//				zos.closeEntry();
//				bis.close();
//				fis.close();
//			}
//		} finally {
//			FileUtil.close(zos);
//			FileUtil.flushClose(bos, fos);
//		}
//		File fileZ = new File(zipFileName);
//		fileZ.delete();
//		fileZFTmp.renameTo(fileZ);
//	}
//
//	public static void compressAFileOrFolderRecursiveToZip(File file, String zipFileName, String exceptFiles) 
//	throws IOException {
//		String folderName;
//		Collection<File> filesNeedCompress;
//		Log.d(file.toString(), "" + file.exists());
//		if (file.isDirectory()) {
//			filesNeedCompress = FileUtil.getFiles(file.listFiles(), false);
//			folderName = file.getAbsolutePath();
//		} else if (file.isFile()) {
//			filesNeedCompress = new LinkedList<File>();
//			filesNeedCompress.add(file);
//			folderName = file.getParent();
//		} else {
//			return;
//		}
//		Log.d("list", Util.collectionToString(filesNeedCompress, true, "\r\n"));
//
//		Pattern pat = Pattern.compile(exceptFiles, Pattern.CASE_INSENSITIVE);
//		if (zipFileName == null || zipFileName.trim().length() == 0) {
//			zipFileName = file.getAbsolutePath() + ".zip";
//		}
//
//		String zipFileNameTmp = zipFileName + ".tmp";
//		File fileZFTmp = new File(zipFileNameTmp);
//		FileOutputStream fos = new FileOutputStream(fileZFTmp);
//		BufferedOutputStream bos = new BufferedOutputStream(fos);
//		ZipOutputStream zos = new ZipOutputStream(bos);
//
//		try {
//			for (File f : filesNeedCompress) {
//				if (!pat.matcher(f.getAbsolutePath()).matches()) {
//					ZipEntry zipEntry = new ZipEntry(
//						f.getAbsolutePath().substring(folderName.length() + 1));
//					zipEntry.setTime(f.lastModified());
//					zos.putNextEntry(zipEntry);
//
//					FileInputStream fis = new FileInputStream(f);
//					BufferedInputStream bis = new BufferedInputStream(fis);
//					byte[] bArr = new byte[4096];
//					int byteRead = 0;
//					while ((byteRead = bis.read(bArr)) != -1) {
//						zos.write(bArr, 0, byteRead);
//					}
//					zos.flush();
//					zos.closeEntry();
//					FileUtil.close(bis, fis);
//				}
//			}
//		} finally {
//			FileUtil.close(zos);
//			FileUtil.flushClose(bos, fos);
//		}
//		File fileZ = new File(zipFileName);
//		fileZ.delete();
//		fileZFTmp.renameTo(fileZ);
//	}
//
//	/**
//	 * Gi?i n?n zipFileName v?o folder c?a zip
//	 * 
//	 * @param zipFileName
//	 * @throws IOException
//	 */
//	public static void extractZipToFolder(String zipFileName) throws IOException {
//		extractZipToFolder(zipFileName, null);
//	}
//
//	/**
//	 * Gi?i n?n zipFileName v?o parentFolder, t? t?o th? m?c n?u parentFolder ch?a t?n t?i
//	 * 
//	 * @param zipFileName
//	 * @param parentFolder
//	 * @throws IOException
//	 */
//	public static void extractZipToFolder(String zipFileName, String parentFolder)
//	throws IOException {
//		File inF = new File(zipFileName);
//		if (parentFolder == null || parentFolder.trim().length() == 0) {
//			parentFolder = inF.getParent();
//		}
//		ZipFile zf = new ZipFile(inF);
//
//		File parentFile = new File(parentFolder);
//		if (!parentFile.exists()) {
//			parentFile.mkdirs();
//		}
//
//		Enumeration<? extends ZipEntry> entries = zf.entries();
//
//		while (entries.hasMoreElements()) {
//			ZipEntry entry = entries.nextElement();
//			if (!entry.isDirectory()) {
//				String fileName = parentFolder + "/" + entry.getName();
//				File entryF = new File(fileName);
//				if (!entryF.exists() || entryF.lastModified() < inF.lastModified()) {
//					InputStream is = zf.getInputStream(entry);
//					FileUtil.is2File(is, fileName);;
//				}
//			}
//		}
//		zf.close();
//	}
//	
//}
