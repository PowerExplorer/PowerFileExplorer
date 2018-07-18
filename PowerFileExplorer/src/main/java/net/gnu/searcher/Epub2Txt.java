package net.gnu.searcher;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.util.Log;
import net.gnu.util.HtmlUtil;
import net.gnu.util.Util;
import net.gnu.util.FileUtil;

public class Epub2Txt {
	
	public static String epub2txt(File inFile) throws IOException {
		long millis = System.currentTimeMillis();
		Log.d("epub2txt", inFile.getAbsolutePath());
		String wholeFile = readZipEntryContent(inFile.getAbsolutePath());
		int fileLength = wholeFile.length();
		
		wholeFile = HtmlUtil.removeTags(wholeFile);
		wholeFile = HtmlUtil.fixCharCode(wholeFile);
		wholeFile = Util.replaceAll(wholeFile, HtmlUtil.ENTITY_NAME,
									HtmlUtil.ENTITY_CODE);
		
		Log.d("epub to text", inFile.getAbsolutePath() + " char num: "
			  + fileLength + " used: "
			  + (System.currentTimeMillis() - millis));

		return wholeFile;
	}
	
	public static String readZipEntryContent(String inFile) 
			throws IOException {
		Log.d("epub readZipEntryContent", inFile);
		FileInputStream fis = new FileInputStream(inFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ZipInputStream zis = new ZipInputStream(bis);
		StringBuilder sb = new StringBuilder();
		try {
			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {
				String zeName = ze.getName().toLowerCase();
				//System.out.println("ze, entryName " + ze);
				if (!ze.isDirectory() 
					&& (zeName.endsWith("htm")
					|| zeName.endsWith("html")
					|| zeName.endsWith("xml")
					|| zeName.endsWith("xhtm")
					|| zeName.endsWith("xhtml"))) {
					System.out.println("read entryName " + ze + ", size " + ze.getSize());
					
					byte[] bb = FileUtil.is2Barr(zis, false);
					
					sb.append(new String(bb, "utf-8")).append("\r\n");
					//System.out.println(sb);
				}
			}
		} catch (IOException e) {
			Log.e("readZipEntryContent", e.getMessage(), e);
		} finally {
			FileUtil.close(zis);
			FileUtil.close(bis);
			FileUtil.close(fis);
		}
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		System.out.println(epub2txt(new  File("/storage/emulated/0/Alice in Wonderland.epub")));
	}
}
