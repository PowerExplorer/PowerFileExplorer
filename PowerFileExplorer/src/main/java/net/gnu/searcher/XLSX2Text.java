package net.gnu.searcher;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.util.Log;
import net.gnu.util.FileUtil;
import net.gnu.util.Util;
import net.gnu.util.HtmlUtil;
import net.gnu.util.ZipUtils;

public class XLSX2Text {
	//	private static Pattern CR_TAGS = Pattern.compile(
	//		"</row>",
	//		Pattern.UNICODE_CASE);

	private static Pattern REMOVE_TAGS = Pattern.compile(
			"</?(v|row|sheetData|printOptions|pageMargins|pageSetup|worksheet|sheetViews|sheetView|sheetFormatPr|cols|col|dimension|selection|pane|mergeCells|mergeCell|phoneticPr|headerFooter|\\?xml)[^>]*?>",
			Pattern.UNICODE_CASE);

	//	private static Pattern C0_TAGS = Pattern.compile(
	//		"<c [^>/]*?/>",
	//		Pattern.UNICODE_CASE);

	private static Pattern R_TAGS = Pattern.compile(
			"<row .*?>(.*?)</row>",
			Pattern.UNICODE_CASE);
	private static Pattern C_TAGS = Pattern.compile(
			"<c .*?r=\"([A-Z]+)\\d+\"[^/]*?>.*?<v>(.*?)</v></c>|<c .*?r=\"([A-Z]+)\\d+\".*?/>",
			Pattern.UNICODE_CASE);
	private static Pattern LIST_CONTENT = Pattern.compile(
			"<t[^>]*?>(.*?)</t>",
			Pattern.UNICODE_CASE);
	private static Pattern LIST_TABLES = Pattern.compile(
			"<sheet .*?name=\"([^\"]+?)\"[^>]*?>",
			Pattern.UNICODE_CASE);

	public static String getText(File inFile) {
		long millis = System.currentTimeMillis();
		StringBuilder document = new StringBuilder(
				xlsxToString(inFile, "xl/worksheets/sheet", ".xml"));
		Log.d("xlsx2Text: ", inFile.getAbsolutePath() + " char num: "
				+ document.length() + " used: "
				+ (System.currentTimeMillis() - millis));
		return document.toString();
	}

	public static String getText(File inFile,  String outDir) throws IOException {
		String toString = getText(inFile);
		FileUtil.writeFileAsCharset(new File(outDir + "/" + inFile.getName() + ".txt"), toString, "utf-8");
		return toString;
	}

	private static String xlsxToString(File inFile, String prefix, String suffix) {
		Log.d("xlsxToString, v ", inFile + ", " + prefix);
		try {
			String tableNames = ZipUtils.readZipEntryContent(
					inFile.getAbsolutePath(), "xl/workbook.xml");
			Matcher m2 = LIST_TABLES.matcher(tableNames);
			List<String> tableList = new LinkedList<String>();
			while (m2.find()) {
				System.out.println(m2.group(1));
				tableList.add(m2.group(1));
			}
			String sharedStrings = ZipUtils.readZipEntryContent(
					inFile.getAbsolutePath(), "xl/sharedStrings.xml");
			Matcher m = LIST_CONTENT.matcher(sharedStrings);
			List<String> sharedList = new LinkedList<String>();
			while (m.find()) {
				sharedList.add(m.group(1));
			}
			String wholeFile = readZipEntryContent(inFile.getAbsolutePath(), prefix, suffix, tableList);
			wholeFile = removeTags(wholeFile, sharedList);
			wholeFile = Util.replaceAll(wholeFile, HtmlUtil.ENTITY_NAME, HtmlUtil.ENTITY_CODE);
			wholeFile = HtmlUtil.fixCharCode(wholeFile);
			return wholeFile;
		} catch (IOException e) {
			Log.e("xlsxToString", e.getMessage(), e);
		}
		return "";
	}

	private static String removeTags(String wholeFile, List<String> sharedList) {

		long millis = System.currentTimeMillis();

		//		wholeFile = CR_TAGS.matcher(wholeFile).replaceAll("\n");
		//
		//		wholeFile = C0_TAGS.matcher(wholeFile).replaceAll("\t");

		Matcher m = R_TAGS.matcher(wholeFile);
		StringBuffer sb = new StringBuffer();

		while (m.find()) {
			String cols = m.group(1);
			//System.out.println("cols " + cols);
			int current = 1;
			Matcher m2 = C_TAGS.matcher(cols);
			//			System.out.println("groupCount " + m2.groupCount());
			StringBuilder sb2 = new StringBuilder();
			while (m2.find()) {
				String col = m2.group(1);
				//				System.out.println("col " + col);
				if (col == null) {
					col = m2.group(3);
				}
				//				System.out.println("col " + col);
				int tabs = 0;
				if (col.length() > 1) {
					int charAt = (col.charAt(0) - 64) * 26 + (col.charAt(1) - 64);
					tabs = charAt - current;
					current = charAt;
				} else {
					int charAt = (col.charAt(0) - 64);
					tabs = charAt - current;
					current = charAt;
				}
				//System.out.println("m2, tab, cur " + m2.group() + ", " + tabs +"," +current);

				for (int i = 0; i < tabs; i++) {
					sb2.append("\t");
				}
				String st = m2.group(2);

				//System.out.println("st "+st);
				if (st != null && st.matches("\\d+")) {
					sb2.append(sharedList.get(Integer.valueOf(st)));
					//				Log.d("sharedList.get(Integer.valueOf(st))", sharedList.get(Integer.valueOf(st)));
				} else if (st != null) {
					sb2.append(st);
				}
			}
			//System.out.println("sb2 " + sb2);
			m.appendReplacement(sb, sb2.append("\n").toString());
		}
		m.appendTail(sb);
		wholeFile = REMOVE_TAGS.matcher(sb).replaceAll("");
		Log.d("Time for converting: ", (System.currentTimeMillis() - millis) + "");
		return wholeFile;
	}

	public static String readZipEntryContent(String inFile, String prefix, String suffix, List<String> l) 
	throws IOException {
		Log.d("inFile", inFile);
		FileInputStream fis = new FileInputStream(inFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ZipInputStream zis = new ZipInputStream(bis);
		ZipEntry ze;
		try {
			ByteArrayOutputStream bb;
			StringBuilder sb = new StringBuilder(65536);
			int tables = 0;
			byte[] bytes;
			while ((ze = zis.getNextEntry()) != null) {
				String zeName = ze.getName();
				System.out.println("ze, entryName " + ze + ", " + prefix);
				if (!ze.isDirectory() && zeName.startsWith(prefix) 
					&& zeName.endsWith(suffix)) {
					System.out.println("read entryName " + prefix + ", " + suffix + ", " + ze.getSize());
					int length = 4096;
					byte[] buffer = new byte[length];
					String sheetName = "\nSheet " + l.get(tables++) + ":\n";
					bytes = sheetName.getBytes("utf-8");
					bb = new ByteArrayOutputStream(65536);
					bb.write(bytes, 0, bytes.length);
					int count = 0;
					while ((count = zis.read(buffer, 0, length)) != -1) {
						bb.write(buffer, 0, count);
					}
					sb.append(new String(bb.toByteArray()));
					System.out.println(zeName + " was read, size: " + bb.size());
				}
			}
			return sb.toString();
		}
		catch (IOException e) {
			Log.e("readZipEntryContent", e.getMessage(), e);
		} finally {
			FileUtil.close(zis);
			FileUtil.close(bis);
			FileUtil.close(fis);
		}
		return "";
	}

	public static void main(String[] args) throws Exception {
	}
}
