package net.gnu.searcher;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import android.util.Log;
import net.gnu.util.HtmlUtil;
import net.gnu.util.Util;
import net.gnu.util.FileUtil;
import net.gnu.util.ZipUtils;


public class DocxToText {
	private static Pattern CR_TAGS = Pattern.compile(
		"</?(w:p [^>]*?|w:p)>",
		Pattern.UNICODE_CASE);

	private static Pattern REMOVE_TAGS = Pattern.compile(
		"</?(w:|mc:|wp:|a:|wps:|a14:|wp14:|w14:|v:|pic:|wpc:|o:|w10:|\\?xml)[^>]*?>",
		Pattern.UNICODE_CASE);
		
	private static Pattern REMOVE_REF = Pattern.compile(
		"<w:(footnoteReference|footnote|endnoteReference|endnote) w:id=\"([^\"]+)\"/?>",
		Pattern.UNICODE_CASE);
	//<w:footnoteReference w:id="180"/>
	//<w:footnote w:id="180">
	private static Pattern REMOVE_CONTENT = Pattern.compile(
		"<(wp:|wp14:|w:instrText)[^>]*?>.*?</(wp:|wp14:|w:instrText)[^>]*?>",
		Pattern.UNICODE_CASE);

	public static String docxToText(File inFile) {
		long millis = System.currentTimeMillis();
		
		StringBuilder document = new StringBuilder(docxToString(inFile, "word/document.xml"));
		
		String footnotes = docxToString(inFile, "word/footnotes.xml");
		if (footnotes.trim().length() > 0) {
			document.append("\nFootnotes\n").append(footnotes);
		}
		
		String endnotes = docxToString(inFile, "word/endnotes.xml");
		if (endnotes.trim().length() > 0) {
			document.append("\nEndnotes\n").append(endnotes);
		}
			
		Log.d("Docx to text: ", inFile.getAbsolutePath() + " char num: "
			+ document.length() + " used: "
			  + (System.currentTimeMillis() - millis));
		
		return document.toString();
	}
	
	public static String docxToText(File inFile,  String outDir) throws IOException {
		String toString = docxToText(inFile);
		FileUtil.writeFileAsCharset(new File(outDir + "/" + inFile.getName() + ".txt"), toString, "utf-8");
		return toString;
	}

	private static String docxToString(File inFile, String v) {
		Log.d("docxToString, v ", inFile + ", " + v);
		try {
			String wholeFile = ZipUtils.readZipEntryContent(inFile.getAbsolutePath(), v);
			wholeFile = removeTags(wholeFile);
			wholeFile = Util.replaceAll(wholeFile, HtmlUtil.ENTITY_NAME, HtmlUtil.ENTITY_CODE);
			wholeFile = HtmlUtil.fixCharCode(wholeFile);
			return wholeFile;
		} catch (IOException e) {
			Log.e("docxToString", e.getMessage(), e);
		}
		return "";
	}

	private static String removeTags(String wholeFile) {

		long millis = System.currentTimeMillis();

		wholeFile = CR_TAGS.matcher(wholeFile).replaceAll("\n");
		
		wholeFile = REMOVE_REF.matcher(wholeFile).replaceAll("$2");

		wholeFile = REMOVE_CONTENT.matcher(wholeFile).replaceAll("");

		wholeFile = REMOVE_TAGS.matcher(wholeFile).replaceAll("");

		Log.d("Time for converting: ", (System.currentTimeMillis() - millis) + "");
		return wholeFile;
	}

	public static void main(String[] args) throws Exception {
		String temp = removeTags("<hr>a<br><font face=<strong>Phần I - Đức Phật</strong></font><p><br>");
		String temp2 = removeTags("hello <![IF !supportFootnotes]>world<![ENDIF]> wide web");
		String temp3 = removeTags("<![ENDIF]>");
		String temp4 = removeTags("<p xmlns=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" class=\"NoSpacing \" style=\"; margin-top:0cm; \">DUYÊN SANH (12 NHÂN DUYÊN)");
		System.out.println(temp);
		System.out.println(temp2);
		System.out.println(temp3);
		System.out.println(temp4);

		System.out.println(removeTags("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"));
		
		System.out.println(docxToText(new  File("/storage/emulated/0/.temp/xml.docx"), "/storage/emulated/0/.temp/"));
		System.out.println(docxToText(new  File("/storage/emulated/0/.temp/a.docx"), "/storage/emulated/0/.temp/"));
	}
}
