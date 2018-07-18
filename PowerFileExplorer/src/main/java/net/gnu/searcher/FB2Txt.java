package net.gnu.searcher;

import java.io.File;
import java.io.IOException;

import android.util.Log;
import net.gnu.util.FileUtil;
import net.gnu.util.HtmlUtil;
import net.gnu.util.Util;

public class FB2Txt {

	public static String fb2txt(File inFile) throws IOException {
		long millis = System.currentTimeMillis();
		Log.d("fb2txt", inFile.getAbsolutePath());
		String wholeFile = FileUtil.readFileAsCharsetMayCheckEncode(inFile.getAbsolutePath(), "utf-8");
		int fileLength = wholeFile.length();

		wholeFile = HtmlUtil.removeTags(wholeFile);
		wholeFile = HtmlUtil.fixCharCode(wholeFile);
		wholeFile = Util.replaceAll(wholeFile, HtmlUtil.ENTITY_NAME,
									HtmlUtil.ENTITY_CODE);

		Log.d("fb2txt", inFile.getAbsolutePath() + " char num: "
			  + fileLength + " used: "
			  + (System.currentTimeMillis() - millis));

		return wholeFile;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(fb2txt(new  File("/storage/emulated/0/MiniHelp.en.fb2")));
	}
}
