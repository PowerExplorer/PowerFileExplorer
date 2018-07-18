package net.gnu.searcher;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;
import net.gnu.util.FileUtil;
import net.gnu.util.HtmlUtil;
import net.gnu.util.Util;
import net.gnu.util.ZipUtils;

public class OdtToText {
	private static Pattern CR_TAGS = Pattern.compile(
		"</?(text:p [^>]*?>|text:p>|text:h [^>]*?>|text:h>)",
		Pattern.UNICODE_CASE);

	private static Pattern REMOVE_TAGS = Pattern.compile(
		"</?(office:|config:|style:|text:|table:|draw:|\\?xml)[^>]*?>",
		Pattern.UNICODE_CASE);

	private static Pattern REMOVE_REF = Pattern.compile(
		"<text:note .*?text:note-class=\"(.*?)\".*?><text:note-citation>(.*?)</text:note-citation><text:note-body>([\\u0000-\\uFFFF]*?)</text:note-body></text:note>",
		Pattern.UNICODE_CASE);

//	<text:note text:id="ftn0" text:note-class="footnote">
//	<text:note-citation>1</text:note-citation>
//	<text:note-body>
//	<text:p text:style-name="Footnote">Trảxemracủacứutấn củacứu</text:p>
//	</text:note-body>
//	</text:note>

//	<text:note text:id="ftn1" text:note-class="endnote">
//	<text:note-citation>i</text:note-citation>
//	<text:note-body>
//	<text:p text:style-name="Endnote">Rẻ tiền</text:p>
//	</text:note-body>
//	</text:note>


	public static String odtToText(File inFile) {
		String wholeFile = odtToString(inFile.getAbsolutePath(), "content.xml");
		return wholeFile;
	}

	public static String odtToText(File inFile,  String outDir) throws IOException {
		String wholeFile = odtToString(inFile.getAbsolutePath(), "content.xml");
		FileUtil.writeFileAsCharset(new File(outDir + "/" + inFile.getName() + ".txt"), wholeFile, "utf-8");
		return wholeFile;
	}

	private static String odtToString(String inFile, String v) {
		Log.d("odtToText, v ", inFile + ", " + v);
		long millis = System.currentTimeMillis();
		try {
			String wholeFile = ZipUtils.readZipEntryContent(inFile, v);
			wholeFile = removeTags(wholeFile);
			wholeFile = Util.replaceAll(wholeFile, HtmlUtil.ENTITY_NAME, HtmlUtil.ENTITY_CODE);
			wholeFile = HtmlUtil.fixCharCode(wholeFile);
			Log.d("Odt to text: ", inFile + " char num: "
				  + wholeFile.length() + " used: "
				  + (System.currentTimeMillis() - millis));

			return wholeFile;
		} catch (IOException e) {
			Log.e("docxToString", e.getMessage(), e);
		}
		return "";
	}

	private static String removeTags(String wholeFile) {

		long millis = System.currentTimeMillis();

		wholeFile = CR_TAGS.matcher(wholeFile).replaceAll("\n");
		Matcher m = REMOVE_REF.matcher(wholeFile);
		StringBuffer sb = new StringBuffer();
		StringBuilder footNotes = new StringBuilder();
		StringBuilder endNotes = new StringBuilder();
		while (m.find()) {
			boolean footNote = Boolean.valueOf("footnote".equals(m.group(1)));
			String noteNo = m.group(2);
			String noteContent = m.group(3);
			if (footNote) {
				footNotes.append(noteNo).append(" ").append(noteContent);
			} else {
				endNotes.append(noteNo).append(" ").append(noteContent);
			}
			m.appendReplacement(sb, noteNo);
		}
		m.appendTail(sb);
		
		if (footNotes.toString().trim().length() > 0) {
			sb.append("\nFootnotes\n").append(footNotes);
		}
		
		if (endNotes.toString().trim().length() > 0) {
			sb.append("\nEndnotes\n").append(endNotes);
		}
		
		wholeFile = REMOVE_TAGS.matcher(sb).replaceAll("");

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
		
	}
}
