package net.gnu.searcher;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;
import net.gnu.util.Util;
import net.gnu.util.HtmlUtil;
import net.gnu.util.FileUtil;
import net.gnu.util.ZipUtils;

public class ODSToText {
	
	private static Pattern TABLE_TAGS = Pattern.compile(
		"<table:table .*?table:name=\"([^\"]*?)\"[^>]*?>",
		Pattern.UNICODE_CASE);
	private static Pattern CR_TAGS = Pattern.compile(
		"</table:table-row>",
		Pattern.UNICODE_CASE);
	private static Pattern REMOVE_TAGS = Pattern.compile(
		"</?(office:|config:|style:|text:|table:|draw:|anim:|presentation:|\\?xml)[^>]*?>",
		Pattern.UNICODE_CASE);
	private static Pattern C_TAGS = Pattern.compile(
		"</table:table-cell>|<table:table-cell[^/>]*?/>",
		Pattern.UNICODE_CASE);
	private static Pattern C_REPEAT_TAGS = Pattern.compile(
		"<table:table-cell [^/>]*?table:number-columns-repeated=\"(\\d+)\"[^/>]*?/>",
		Pattern.UNICODE_CASE);

	public static String odsToText(File inFile) {
		String wholeFile = odsToString(inFile.getAbsolutePath(), "content.xml");
		return wholeFile;
	}

	public static String odsToText(File inFile,  String outDir) throws IOException {
		String wholeFile = odsToString(inFile.getAbsolutePath(), "content.xml");
		FileUtil.writeFileAsCharset(new File(outDir + "/" + inFile.getName() + ".txt"), wholeFile, "utf-8");
		return wholeFile;
	}

	private static String odsToString(String inFile, String v) {
		Log.d("odsToString, v ", inFile + ", " + v);
		long millis = System.currentTimeMillis();
		try {
			String wholeFile = ZipUtils.readZipEntryContent(inFile, v);
			wholeFile = removeTags(wholeFile);
			wholeFile = Util.replaceAll(wholeFile, HtmlUtil.ENTITY_NAME, HtmlUtil.ENTITY_CODE);
			wholeFile = HtmlUtil.fixCharCode(wholeFile);
			Log.d("odsToString", inFile + " char num: "
				  + wholeFile.length() + " used: "
				  + (System.currentTimeMillis() - millis));
			return wholeFile;
		} catch (IOException e) {
			Log.e("odsToString", e.getMessage(), e);
		}
		return "";
	}

	private static String removeTags(String wholeFile) {
		long millis = System.currentTimeMillis();
		wholeFile = TABLE_TAGS.matcher(wholeFile).replaceAll("\nTable: $1\n");
		wholeFile = CR_TAGS.matcher(wholeFile).replaceAll("\n");
		StringBuffer sb = new StringBuffer();
		Matcher m  = C_REPEAT_TAGS.matcher(wholeFile);
		while (m.find()) {
			int count = Integer.valueOf(m.group(1));
			StringBuilder sb2 = new StringBuilder();
			for (int i = 0; i < count; i++) {
				sb2.append("\t");
			}
			m.appendReplacement(sb, sb2.toString());
		}
		m.appendTail(sb);
		
		wholeFile = C_TAGS.matcher(sb).replaceAll("\t");
		
		wholeFile = REMOVE_TAGS.matcher(wholeFile).replaceAll("");

		Log.d("Time for converting: ", (System.currentTimeMillis() - millis) + "");
		return wholeFile;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(odsToText(new  File("/storage/emulated/0/a.ods"), "/storage/emulated/0/.temp/"));
	}
}
