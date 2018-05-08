package net.gnu.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import java.util.*;
import java.lang.reflect.*;
import java.io.*;
import java.nio.*;
import java.security.*;
import java.math.*;
import android.util.*;
import org.mozilla.universalchardet.*;
import android.content.Context;
import android.annotation.TargetApi;
import android.os.Build;
import com.amaze.filemanager.ui.LayoutElement;
import com.amaze.filemanager.utils.OnProgressUpdate;

public class FileUtil {

	private static final String ISO_8859_1 = "ISO-8859-1";
	private static final String TAG = "FileUtil";
	public static final String compressibleExtension = ".*?\\.(7z|zip|zipx|lzma2|bz2|tbz2|bzip2|tbzip2|bz|tbz|xz|txz|gz|tgz|tgzip|tz|tar|wim)";
	public static final String extractibleExtension = "(zpaq|7z|bz2|bzip2|tbz2|tbz|001|gz|gzip|tgz|tar|dump|swm|xz|txz|zip|zipx|jar|apk|xpi|odt|ods|odp|docx|xlsx|pptx|epub|apm|ar|a|deb|lib|arj|cab|chm|chw|chi|chq|msi|msp|doc|xls|ppt|cpio|cramfs|dmg|ext|ext2|ext3|ext4|img|fat|hfs|hfsx|hxs|hxi|hxr|hxq|hxw|lit|ihex|iso|lzh|lha|lzma|mbr|mslz|mub|nsis|ntfs|rar|r00|rpm|ppmd|qcow|qcow2|qcow2c|squashfs|udf|iso|scap|uefif|vdi|vhd|vmdk|wim|esd|xar|pkg|z|taz)";
	public static Pattern extractibleExtensionPattern = Pattern.compile(FileUtil.extractibleExtension, Pattern.CASE_INSENSITIVE);
	public static Pattern extractiblePattern = Pattern.compile(".*?\\." + FileUtil.extractibleExtension, Pattern.CASE_INSENSITIVE);
	public static final Pattern TEXT_PATTERN = Pattern.compile(".*?\\.([dxs]?htm[l]?|txt|java|c|cpp|h|hpp|xml|md|lua|sh|bat|list|depend|js|jsp|mk|config|configure|machine|asm|css|desktop|inc|i|plist|pro|py|s|iml|pro|gradle|cfg|gitignore|sh|properties|pod|classpath|mk|md|javascript|json|bak)", Pattern.CASE_INSENSITIVE);
	
	public static void main(String[] args) throws Exception {
////		exec("/data/data/com.aide.ui/files/ndksupport-20150805/bin/ls", new String[] {"-l", "/sdcard/"}, null);
////		exec("/data/data/com.aide.ui/files/ndksupport-20150805/bin/chmod", new String[] {"777", "/data/data/com.aide.ui/files/ndksupport-20150805/bin/tar"}, new File("/sdcard/tmp/"));
////		exec("/data/data/com.aide.ui/files/ndksupport-20150805/bin/tar", new String[] {"-cvjf", "/sdcard/aide.bz2", "/data/data/com.aide.ui/"}, new File("/sdcard/tmp/"));
////		String inPath = "/sdcard/Android-SDK";//"/data/data/com.aide.ui/";
////		String sz = "/sdcard/a.7z";
////		SevenZOutputFile sevenZOutput = new SevenZOutputFile(new File(sz));
////		Method[] ms = FileUtil.class.getMethods();
////		for (Method m : ms) {
////			System.out.println(m);
////		}
////		FileUtil.executeFiles(inPath, sevenZOutput, FileUtil.class.getMethod("compressBy7Z", 
////																					new Class[] {File.class, SevenZOutputFile.class, Integer.class}));
//
////		String collectionToString = Util.collectionToString(getFiles(new File("/data/data/com.aide.ui").listFiles()), true, "\r\n");
////		Log.d("list", collectionToString);
////		writeContentToFile("/sdcard/backups/aide.txt", collectionToString);
//		//compressAFileOrFolderRecursiveToZip(new File("/data/data/com.aide.ui"), "/sdcard/aide.zip");
////		extractAFolderInZipToDisk(new File("/sdcard/aide-ndk.zip"), "", "/data/data/com.aide.ui", true);
//		//		for (int i = 0; i < 256; i++) {
//		//			Log.d(i + "", (char)i + "");
//		//		}
//		//		createJAR("D:/backups/workspace/Searcher/android-23.jar", "D:/backups/workspace/android");
////		File f = new File("/data/data/com.aide.ui/a.txt");
////		writeAppendFileAsCharset(f, "hello there", "utf-8");
////		String string = readFileAsCharsetMayCheckEncode(f, "utf-8");
////		Log.d("test writing", string);
////		List<File> files = getFiles(new File("D:/app/"), 
////				Pattern.compile(".*?\\.([dxs]?htm[l]?|txt|java|c|cpp|h|hpp|xml|md|lua|sh|bat|list|depend|js|jsp|mk|config|configure|machine|asm|css|desktop|inc|i|plist|pro|py|s)", Pattern.CASE_INSENSITIVE),
////				Pattern.compile("", Pattern.CASE_INSENSITIVE));
////		String collectionToString = Util.collectionToString(files, true, "\r\n");
////		System.out.println(collectionToString);
////		System.out.println(Util.collectionToString(System.getProperties().entrySet(), true, "\n"));
////		System.out.println(getMemoryFree());
////		String result = null;
////		try {
////			String[] args2 = { "/system/bin/cat", "/proc/meminfo" };
////			result = run(args2, "/system/bin/");
////		} catch (IOException ex) {
////			Log.i("fetch_process_info", "ex=" + ex.toString());
////		}
////
////		System.out.println(result);
//		//System.out.println(System.getenv());
//
//		//System.out.println(System.getenv("SECONDARY_STORAGE").split(":")[0]);
//		//System.out.println(System.getenv("SECONDARY_STORAGE").split(":")[1]);
//		//deleteDrawable("/sdcard/AppProjects/0SearchExplore/PowerExplorer/PowerExplorer/src/main", "");
//		//exec("/system/bin/chmod", "-R", "777", "/data/data/com.aide.ui/cache/");
//		//exec("/system/bin/rm", "-r", "/data/data/com.aide.ui/cache/");
//		//exec("/system/bin/rm", "/data/data/com.aide.ui/cache/1493867303508.tmp");
//		exec("/system/bin/ls", "-l", "/data/data/com.aide.ui/files");
//		//exec("/system/bin/cd /data/data/com.aide.ui/");
//		//exec("ls", "-l");
//		//exec("/system/bin/cp", "/sdcard/.aide/7za", "/data/data/com.aide.ui/");
//		exec("/system/bin/chmod", "777", "/data/data/com.aide.ui/7za");
////		exec("/system/bin/chmod", "777", "/data/data/com.aide.ui/*");
////		exec("/system/bin/chmod", "777", "/data/data/com.aide.ui/");
////		exec("/system/bin/chmod", "777", "/data/data/com.aide.ui");
//		//exec("/data/data/com.aide.ui/7za", "i");
//		exec("/data/data/com.aide.ui/7za");
//		//exec("/system/bin/ls", "-l", "/data/data/com.aide.ui/");
//		//exec("/system/bin/ls", "-l", "/data/data/com.aide.ui/cache");
//		//exec("/system/bin/ls", "-l", "/data/data/com.aide.ui/files");
//		//exec("/system/bin/ls", "-l", "/data/data/com.aide.ui/shared_prefs");
////		exec("/system/bin/cp", "-R", "/sdcard/tmp/files", "/data/data/com.aide.ui/files");//, "/sdcard/tmp");
////		exec("/system/bin/ls", "-l", "/data/data/com.aide.ui/files");
////		exec("/system/bin/ls", "-l", "/data/data/com.aide.ui/shared_prefs");
//		//exec("/system/bin/mkdir", "/data/data/com.aide.ui/files/ndksupport-20150805/android-ndk-aide/sources/cxx-stl/stlport/libs/armeabi-v7a/thumb/");
//		//exec("/system/bin/ls", "-l", "/data/data/com.aide.ui/files/ndksupport-20150805/android-ndk-aide/sources/cxx-stl/stlport/libs/armeabi-v7a");
////		exec("/system/bin/cp", "/data/data/com.aide.ui/files/ndksupport-20150805/android-ndk-aide/sources/cxx-stl/stlport/libs/armeabi-v7a/libstlport_static.a", 
////			 "/data/data/com.aide.ui/files/ndksupport-20150805/android-ndk-aide/sources/cxx-stl/stlport/libs/armeabi-v7a/thumb/");
////		exec("/system/bin/cp", "/data/data/com.aide.ui/files/ndksupport-20150805/android-ndk-aide/sources/cxx-stl/stlport/libs/armeabi-v7a/libstlport_shared.so", 
////			 "/data/data/com.aide.ui/files/ndksupport-20150805/android-ndk-aide/sources/cxx-stl/stlport/libs/armeabi-v7a/thumb/");
////		exec("/system/bin/chmod", "-R", "777", "/data/data/com.aide.ui/files/ndksupport-20150805/android-ndk-aide/sources/cxx-stl/stlport/libs/armeabi-v7a/thumb/");
////		
//		exec("/system/bin/mkdir", "/data/data/com.aide.ui/files/ndksupport-1.707060002E9/android-ndk-aide/sources/cxx-stl/stlport/libs/armeabi-v7a/thumb/");
//		exec("/system/bin/cp", "/data/data/com.aide.ui/files/ndksupport-1.707060002E9/android-ndk-aide/sources/cxx-stl/stlport/libs/armeabi-v7a/libstlport_static.a", 
//			 "/data/data/com.aide.ui/files/ndksupport-1.707060002E9/android-ndk-aide/sources/cxx-stl/stlport/libs/armeabi-v7a/thumb/");
//		exec("/system/bin/cp", "/data/data/com.aide.ui/files/ndksupport-1.707060002E9/android-ndk-aide/sources/cxx-stl/stlport/libs/armeabi-v7a/libstlport_shared.so", 
//			 "/data/data/com.aide.ui/files/ndksupport-1.707060002E9/android-ndk-aide/sources/cxx-stl/stlport/libs/armeabi-v7a/thumb/");
//		exec("/system/bin/chmod", "-R", "777", "/data/data/com.aide.ui/files/ndksupport-1.707060002E9/android-ndk-aide/sources/cxx-stl/stlport/libs/armeabi-v7a/thumb/");
//		exec("/system/bin/ls", "-l", "/data/data/com.aide.ui/files/ndksupport-1.707060002E9/android-ndk-aide/sources/cxx-stl/stlport/libs/armeabi-v7a/thumb/");
//		//exec("/data/data/com.aide.ui/7za", "x", "-aoa", "-o/data/data/com.aide.ui", "/storage/MicroSD/Apps/ndk-aide-new.7z");
//		//exec("/data/data/com.aide.ui/7za", "x", "-aoa", "-o/data/data/com.aide.ui", "/storage/MicroSD/FrVN/aide.7z");
//		//exec("/data/data/com.aide.ui/7za", "x", "-aoa", "-o/data/data/com.aide.ui", "/storage/MicroSD/Apps/aide-lic.zip");
//		//exec("/system/bin/cp", "-f", "/sdcard/.aide/shared_prefs/com.aide.ui_preferences.xml", "/data/data/com.aide.ui/shared_prefs");//, "/sdcard/tmp");
//		//exec("/system/bin/ls", "-lR", "/data/data/com.aide.ui/files/ndksupport-20150805/android-ndk-aide/sources/cxx-stl/stlport/libs/armeabi-v7a");
//		//exec("/system/bin/ls", "-lR", "/data/data/com.aide.ui/");
//		//Log.d("/data/data/com.aide.ui/", new File("/data/data/com.aide.ui/").list().length + "");
//		//exec("/system/bin/rm", "/sdcard/rar/ndkaide.7z");
//		//exec("/data/data/com.aide.ui/7za", "a", "-mqs", "/sdcard/rar/ndkaide.7z", "/data/data/com.aide.ui/*");//"-r", "-mqs", "-ssw",  , "/data/data/com.aide.ui/shared_prefs" "/data/data/com.aide.ui/"
//		//exec("ls", "-l", "/sdcard/rar/");
		exec("", "/data/data/com.aide.ui/7za", "x", "-bsp1", "-bb", "-aou", "/sdcard/rar/ndkaide.7z", "-o/sdcard/rar/", ">", "/sdcard/progress.txt");
////		Entry<BufferedReader, PrintStream> e = execInteract("/system/bin/cp", "-R", "-i", "/sdcard/rar", "/sdcard/.com.free.searcher");
////		PrintStream p = e.getValue();
////		BufferedReader b = e.getKey();
////		Scanner input = new Scanner(System.in);
////
////		//System.out.print("Enter: ");
////		String str = "";//input.nextLine();
////		while (!"exit".equals(str)) {
////			while (b.ready()) {
////				System.out.println(b.readLine());
////			}
////			str = input.nextLine();
////			p.println(str);
////		}
//		//exec();
	}
	
	public static List<String> filterFiles(String path, /*String tempPath, */String pattern, String subPattern) {
		//filterFiles("/storage/emulated/0/", "/sdcard/222.txt", "-\\S{9}\\s+\\S+\\s+\\S+\\s+\\d*?\\s+\\S+\\s+\\S+\\s+([^/]+?)\\n", ".*?\\.(bmp|gif|ico|cur|jpeg|jpg|jpe|pcx|png|tiff|tif|psd|dwg|pct|pic|3gpp|3gp|3gpp2|3g2|avi|m4v|mpeg|mpg|mpe|mp4|vob|qt|mov|webm|asf|asx|wm|wmv|wmx|mpa|flv|mkv)");
		final long start = System.currentTimeMillis();
		final List<String> ret = new LinkedList<>();
		final StringBuilder sb = FileUtil.exec(null, "ls", "-l", path);

		final Pattern pat = Pattern.compile(pattern);
		final Pattern patSub = Pattern.compile(subPattern);
		final Matcher mat = pat.matcher(sb);
		String group;
		while (mat.find()) {
			group = mat.group(1);
			if (patSub.matcher(group).matches()) {
				ret.add(group);
			}
		}
		//String collectionToString = Util.collectionToString(ret, false, "\n");
		//FileUtil.writeContentToFile(tempPath, collectionToString);
		//System.out.println(collectionToString);
		System.out.println("filterFiles took " + Util.nf.format(System.currentTimeMillis() - start));
		return ret;
	}
	
	static void removeDup(File existedF, File allF) {
		try {
			String st = readFileAsCharset(existedF, "ASCII");
			String[] arr = st.split("\n");
			Set<String> existedSet = new TreeSet<>();
			existedSet.addAll(Arrays.asList(arr));

			st = readFileAsCharset(allF, "ASCII");
			arr = st.split("\n");
			Set<String> allSet = new TreeSet<>();
			allSet.addAll(Arrays.asList(arr));
			arr = null;
			st = null;
			for (String s : existedSet) {
				allSet.remove(s);
			}
			String resu = Util.collectionToString(allSet, false, "\n");
			writeFileAsCharset("/sdcard/newList.txt", resu, "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//https?:\/\/\S+(\/\S+)*(\/)?
	private static final String PATTERN_STR = 
	"\\b(src|href)\\s*=\\s*([^\\s'\">]+|'[^'\\s]+'|\"[^\"\\s]+\")"
	//+ "|href\\s*=\\s*([^\\s'\">]+|'[^'\\s]+'|\"[^\"\\s]+\")"
	+ "|\\burl\\s*\\(\\s*([^\\s'\">\\)]+|'[^'\\s]+'|\"[^\"\\s]+\")"
	;

//	"(src\\s*=|href\\s*=|url\\s*\\()\\s*" +
//	"([^\\s'\">\\)]+" +
//	"|'[^']+'" +
//	"|\"[^\"]+\")";
	private static final Pattern LINK_PATTTERN = Pattern.compile(PATTERN_STR, Pattern.CASE_INSENSITIVE);
	public static final Set<String> getSaveOtherUrl(final String contentStr, String baseRefOri, String extractUrlPattern) {
//		Log.d("contentStr", contentStr);
		final Set<String> lF = new HashSet<String>();
		// File currentFile = new File(curEntryName);
		//Log.d("getSaveOtherUrl curFullEntryName", curFullEntryName);
		String baseRef;
		//Log.d("baseRefOri", baseRefOri);
		//Log.d("extractUrlPattern", extractUrlPattern);

		final Matcher mat = LINK_PATTTERN.matcher(contentStr);
		String extractedUrl;
		int indexOf;
		if (baseRefOri.endsWith("/") && baseRefOri.length() > 1) {
			baseRefOri = baseRefOri.substring(0, baseRefOri.length()-1);
		}
		while (mat.find()) {
			baseRef = baseRefOri;
			//System.out.println("mat.group() " + mat.group());
			if ((extractedUrl = mat.group(2)) != null) {
			} else if ((extractedUrl = mat.group(3)) != null) {
//			} else if ((extractedUrl = mat.group(6)) != null) {
//			} else {
				extractedUrl = "all null";
			}

			//contentStr.substring(mat.start(2), mat.end(2));
			//System.out.println("extractedUrl1 " + extractedUrl);

			if (extractedUrl.startsWith("'") || extractedUrl.startsWith("\"")) {
				extractedUrl = extractedUrl.substring(1, extractedUrl.length() - 1);
			}

			extractedUrl = skipParam(extractedUrl, "?");
			extractedUrl = skipParam(extractedUrl, "#");
			extractedUrl = Util.decodeUrlToFS(extractedUrl);
			if (!extractedUrl.matches(extractUrlPattern)) {
				continue;
			}
			String tempBaseRef;
			int lastIndexOf;
			//Log.d("extractedUrl", extractedUrl);
			// không cần extract link http://
			if (extractedUrl.length() > 0) {
				indexOf = extractedUrl.indexOf(":");
				if (indexOf < 0) {//tuong doi
					if (!extractedUrl.startsWith(".")) {
						// thư mục con tương đối
						if (baseRef.length() > 0) {
							baseRef = baseRef + "/" + extractedUrl; 
						} else {
							baseRef = extractedUrl;
						}
					} else if (extractedUrl.startsWith("./")) {
						// thư mục hiện hành
						extractedUrl = extractedUrl.substring(2);//"./".length());
						baseRef = baseRef + "/" + extractedUrl;
					} else if (extractedUrl.startsWith("../")) {
						// thư mục cha tương đối
						tempBaseRef = baseRef;
						while (extractedUrl.startsWith("../")) {
							extractedUrl = extractedUrl.substring(3);//"../".length());
							lastIndexOf = tempBaseRef.lastIndexOf("/");
							if (lastIndexOf > 0) {
								tempBaseRef = tempBaseRef.substring(0, lastIndexOf);
							} else {
								tempBaseRef = "";
							}
							//Log.d("tempBaseRef", tempBaseRef);
						}
						if (tempBaseRef.length() > 0) {
							baseRef = tempBaseRef + "/" + extractedUrl;
						} else {
							baseRef = extractedUrl;
						}
						//Log.d("baseRef", baseRef);
					}
					lF.add(baseRef);
					//Log.d("retUrl1", baseRef);
				} else if (indexOf <= 6) {
					//tuyet doi
					lF.add(extractedUrl);
					//Log.d("retUrl2", extractedUrl);
				}
			}
		}
		return lF;
	}

	private static String skipParam(String newLink, String sign) {
		final int indexQ = newLink.indexOf(sign);
		if (indexQ > 0) {
			newLink = newLink.substring(0, indexQ);
		} else if (indexQ == 0) {
			newLink = "";
		}
		return newLink;
	}

	public static void  extractUrl(File root, String urlPattern, File resultUrlFile) throws IOException {
		long start = System.currentTimeMillis();
		final Collection<File> l1 = getFiles(root, false);
		String htmlContent;
		int width = root.getParentFile().getAbsolutePath().length();
		Set<String> listUrls = new TreeSet<String>();
		for (File f : l1) {
			try {
				htmlContent = readFileAsCharset(f, "utf-8");
				listUrls.addAll(getSaveOtherUrl(htmlContent, f.getAbsolutePath().substring(width+1), urlPattern));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//System.out.println(Util.collectionToString(listUrls, true, "\n"));
		String ret = Util.collectionToString(listUrls, false, "\n");
		writeFileAsCharset(resultUrlFile.getAbsolutePath(), ret, "utf-8");
		long took = System.currentTimeMillis() - start;
		long h = took/3600000;
		long m = (took - h * 3600000) / 60000;
		long s = (took - h*3600000 - m * 60000)/1000;
		System.out.println("finish " + h + ":" + m + ":" + s + "." + took%1000);
	}

//	public static StringBuilder execInteract() {
//		BufferedReader pout = null;
//		PrintStream pin = null;
//
//		StringBuilder sb = new StringBuilder();
//		int ch;
//		Scanner input = new Scanner(System.in);
//		String inputStr = input.nextLine();
//
//		if (inputStr.length() > 0) {
//			while (!"".equals(inputStr.toLowerCase())) {
//				try {
//					ProcessBuilder builder = new ProcessBuilder(inputStr);
//					builder.redirectErrorStream(true);
//
//					Process p = builder.start();
//					// Write into the standard input of the subprocess
//					pin = new PrintStream(new BufferedOutputStream(p.getOutputStream()));
//					// Read from the standard output of the subprocess
//					pout = new BufferedReader(new InputStreamReader(p.getInputStream()));
//
//					while ((ch = pout.read()) != -1) {
//						sb.append((char)ch);
//						System.out.print((char)ch);
//					}
//					System.out.println("Enter to exit command: " + inputStr);
//					inputStr = input.nextLine();
//
//					while (!"".equals(inputStr.toLowerCase())) {
//						pin.write(inputStr.getBytes());
//						while ((ch = pout.read()) != -1) {
//							sb.append((char)ch);
//							System.out.print((char)ch);
//						}
//						inputStr = input.nextLine();
//					}
//					int exitValue = p.waitFor();
//					System.out.println("exit code " + exitValue);
//					System.out.println("Enter to exit console");
//					inputStr = input.nextLine();
//				} catch (IOException ex) {
//					ex.printStackTrace();
//					inputStr = input.nextLine();
//				} catch (InterruptedException ex) {
//					ex.printStackTrace();
//					inputStr = input.nextLine();
//				} finally {
//					FileUtil.close(pin, pout);
//				}
//			}
//		}
//
//		//System.out.println(sb);
//		return sb;
//
//	}

	public static StringBuilder exec(final String workingDir, final String... cmd) {
		final StringBuilder sb = new StringBuilder();
		if (cmd == null || cmd.length == 0) {
			return sb;
		}
		BufferedReader pout = null;
		PrintStream pin = null;
		try {
			ProcessBuilder builder = new ProcessBuilder(cmd);
			if (workingDir != null && workingDir.length() > 0) {
				builder.directory(new File(workingDir));
			}
			builder.redirectErrorStream(true);
//			builder.redirectInput(ProcessBuilder.Redirect.from(new File("in.txt")));
//			builder.redirectOutput(ProcessBuilder.Redirect.to(new File("out.txt")));
//			builder.redirectError(ProcessBuilder.Redirect.appendTo(new File("error.log")));

			final Process p = builder.start();
			//Process p = Runtime.getRuntime().exec(cmd);  
			// Execute with input/output

			// Write into the standard input of the subprocess
			pin = new PrintStream(new BufferedOutputStream(p.getOutputStream()));
			// Read from the standard output of the subprocess
			pout = new BufferedReader(new InputStreamReader(p.getInputStream()));

			// Pump in input
//			pin.print("1 2");
//			pin.close();

			// Save the output in a StringBuffer for further processing
			int ch;
			String arrayToString = Util.arrayToString(cmd, false, " ");
			System.out.println("\n" + "\n" + arrayToString);

			while ((ch = pout.read()) != -1) {
				sb.append((char)ch);
				//System.out.print((char)ch);
			}

			//System.out.println(sb);
//			BufferedReader perr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//			while ((ch = perr.read()) != -1) {
//				System.out.print((char)ch);
//			}
			final int exitValue = p.waitFor();
			//System.out.print(sb);
			System.out.println(arrayToString + " : exit code " + exitValue);
			return sb;
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} finally {
			FileUtil.close(pin, pout);
		}
		return sb;
	}
	
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String[] getExtSdCardPathsForActivity(Context context) {
        List<String> paths = new ArrayList<>();
        for (File file : context.getExternalFilesDirs("external")) {
            Log.d(TAG, "external: " + file.getAbsolutePath());
			if (file != null) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w("AmazeFileUtils", "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        if (paths.isEmpty()) 
			paths.add("/storage/sdcard1");
        return paths.toArray(new String[0]);
    }
	
	public static String getExtension(final String fName) {
		if (fName != null) {//} && !fName.endsWith(".")) {
			int lastIndexOf = fName.lastIndexOf(".");
			if (lastIndexOf >= 0)
				return fName.substring(++lastIndexOf).toLowerCase();
		}
		return "";
	}

//	public static String getExtension(final File f) {
//		return getExtension(f.getName());
//	}

//	public static String getExtension(final String fName) {
//		if (fName != null) {
//			final int idx = fName.lastIndexOf(".");
//			final String suff = ((idx >= 0 && idx < fName.length()-1)? fName.substring(idx+1) : "");
//			return suff.toLowerCase();
//		} else {
//			return "";
//		}
//	}

	public static String getPathHash(final File f) {
		try {
			final MessageDigest md = MessageDigest.getInstance("MD5");
			byte data[];
			if (f.exists()) {
				data = (f.getAbsolutePath() + f.length() + f.lastModified()).getBytes();
			} else {
				data = f.getAbsolutePath().getBytes();
			}
			final BigInteger bigInteger = new BigInteger(1, md.digest(data));
			return bigInteger.toString(16);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("cannot initialize MD5 hash function", e);
		}
	}
	
	public static String getFastHash(String filepath){
		MessageDigest md;
		String hash;
		final File file = new File(filepath);
		try {
			try {
				md = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException("cannot initialize MD5 hash function", e);
			}
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			byte data[] = null;
			int length = (int) file.length();
			if (length > 1048576L) {
                data = new byte[length >> 6];
				//} else if (file.length() > 1024L) {
				//	data = new byte[(int) file.length()/10];
			} else {
				data = new byte[length];
			}
			bis.read(data);
			close(bis, fis);
			hash = new BigInteger(1, md.digest(data)).toString(16);
		} catch (IOException e) {
			throw new RuntimeException("cannot read file " + file.getAbsolutePath(), e);
		}
		return hash ;
    }

	public static void sort(String dirSt, ArrayList<String> fNameList, Comparator<File> c) {
		dirSt = (dirSt == null) ? "" : dirSt;
		ArrayList<File> fs = new ArrayList<File>(fNameList.size());
		for (String st : fNameList) {
			fs.add(new File(dirSt, st));
		}
		Collections.sort(fs, c);
		fNameList.clear();
		if (dirSt.length() > 0) {
			for (File f : fs) {
				fNameList.add(f.getName());
			}
		} else {
			for (File f : fs) {
				fNameList.add(f.getAbsolutePath());
			}
		}
	}
	

	public static boolean compareFileContent(final File f1, final File f2) throws IOException {
		//Log.d("compare file ", f1.getName() + " and " + f2.getName());
		final long length2 = f2.length();
//		if (f1.length() != length2) {
//			return false;
//		}
		FileInputStream fis1 = null;
		FileInputStream fis2 = null;
		BufferedInputStream bis1 = null;
		BufferedInputStream bis2 = null;
		try {
			fis1 = new FileInputStream(f1);
			fis2 = new FileInputStream(f2);
			bis1 = new BufferedInputStream(fis1);
			bis2 = new BufferedInputStream(fis2);
			long counter = 0;
			int n;
			while (((n = bis1.read()) != -1) && n == bis2.read()) {
				counter++;
			}
			return n == -1 && counter == length2;
		} finally {
			close(bis1, bis2, fis1, fis2);
		}
	}

	public static void compare2Folder(final File root1, final File root2) throws IOException {
		final Collection<File> l1 = getFiles(root1, false);

		String absolutePath1 = root1.getAbsolutePath();
		String absolutePath2 = root2.getAbsolutePath();
		final Collection<File> l2;
		if (absolutePath1.equalsIgnoreCase(absolutePath2)) {
			l2 = l1;
		} else {
			l2 = getFiles(root2, false);
		}

		Map<Long, LinkedList<File>> mSize1 = new TreeMap<>();
		Map<Long, LinkedList<File>> mSize2 = new TreeMap<>();
		Map<File, TreeSet<File>> mDup = new TreeMap<>();

		Map<String, LinkedList<File>> mName1 = new TreeMap<>();
		Map<String, LinkedList<File>> mName2 = new TreeMap<>();

		long fLength;
		LinkedList<File> ll = new LinkedList<>();
		for (File f : l1) {
			fLength = f.length();
			if ((ll = mSize1.get(fLength)) == null) {
				ll = new LinkedList<>();
				mSize1.put(fLength, ll);
			}
			ll.add(f);
		}

		if (l1 == l2) {
			mSize2 = mSize1;
		} else {
			for (File f : l2) {
				fLength = f.length();
				if ((ll = mSize2.get(fLength)) == null) {
					ll = new LinkedList<>();
					mSize2.put(fLength, ll);
				}
				ll.add(f);
			}
		}

		String fName;
		for (File f : l1) {
			fName = f.getName().toLowerCase();
			if ((ll = mName1.get(fName)) == null) {
				ll = new LinkedList<>();
				mName1.put(fName, ll);
			}
			ll.add(f);
		}

		if (l1 == l2) {
			mName2 = mName1;
		} else {
			for (File f : l2) {
				fName = f.getName().toLowerCase();
				if ((ll = mName2.get(fName)) == null) {
					ll = new LinkedList<>();
					mName2.put(fName, ll);
				}
				ll.add(f);
			}
		}

		new File("/sdcard/tmp/").mkdirs();
		String root1AbsolutePath = absolutePath1.replaceAll("/", "_");
		String root2AbsolutePath = absolutePath2.replaceAll("/", "_");
		final File dup1 = new File("/sdcard/tmp/" + root1AbsolutePath + "_dup_" + root2AbsolutePath + ".txt");
		final FileWriter fwDup1 = new FileWriter(dup1);
		final BufferedWriter bwDup1 = new BufferedWriter(fwDup1);
		final File dup2 = new File("/sdcard/tmp/" + root2AbsolutePath + "_dup_" + root1AbsolutePath + ".txt");
		final FileWriter fwDup2 = new FileWriter(dup2);
		final BufferedWriter bwDup2 = new BufferedWriter(fwDup2);

		final File mod1 = new File("/sdcard/tmp/" + root1AbsolutePath + "_mod_" + root2AbsolutePath + ".txt");
		final FileWriter fwMod1 = new FileWriter(mod1);
		final BufferedWriter bwMod1 = new BufferedWriter(fwMod1);
		final File mod2 = new File("/sdcard/tmp/" + root2AbsolutePath + "_mod_" + root1AbsolutePath + ".txt");
		final FileWriter fwMod2 = new FileWriter(mod2);
		final BufferedWriter bwMod2 = new BufferedWriter(fwMod2);

		boolean same = false;
		boolean sameName = false;

		final Map<File, Boolean> deleteSet = new TreeMap<>();
		for (File f : l1) {
			deleteSet.put(f, true);
		}

		final Map<File, Boolean> newFiles = new TreeMap<>();
		for (File f : l2) {
			newFiles.put(f, true);
		}

		LinkedList<File> ll1;
		LinkedList<File> ll2;
		TreeSet<File> dupList;
		Long fLengthKey;
		for (Map.Entry<Long, LinkedList<File>> e : mSize1.entrySet()) {
			fLengthKey = e.getKey();
			if ((ll2 = mSize2.get(fLengthKey)) != null) {
				ll1 = e.getValue();
				for (File f1 : ll1) {
					for (File f2 : ll2) {
						try {
							if (f1 == f2) {
								deleteSet.put(f1, false);
								newFiles.put(f2, false);
								continue;
							}
							same = compareFileContent(f1, f2);
							sameName = f1.getName().equalsIgnoreCase(f2.getName());
							if (same && sameName) {
								absolutePath1 = f1.getAbsolutePath();
								absolutePath2 = f2.getAbsolutePath();
								if ((dupList = mDup.get(f1)) == null) {
									dupList = new TreeSet<>();
									mDup.put(f1, dupList);
								}
								dupList.add(f2);

								System.out.print(absolutePath1);
								System.out.print("\t was copied to \t");
								System.out.println(absolutePath2);

								deleteSet.put(f1, false);
								newFiles.put(f2, false);

							} else if (same) {
								absolutePath1 = f1.getAbsolutePath();
								absolutePath2 = f2.getAbsolutePath();
								if ((dupList = mDup.get(f1)) == null) {
									dupList = new TreeSet<>();
									mDup.put(f1, dupList);
								}
								dupList.add(f2);

								System.out.print(absolutePath1);
								System.out.print("\t was renamed to \t");
								System.out.println(absolutePath2);

								deleteSet.put(f1, false);
								newFiles.put(f2, false);
							}
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}
					}

				}
			}
		}

		TreeSet<File> s = new TreeSet<>();
		for (Map.Entry<File, TreeSet<File>> e : mDup.entrySet()) {
			bwDup1.write(e.getKey().getAbsolutePath());
			bwDup1.write("\n");
			s.addAll(e.getValue());
		}
		flush(bwDup1);
		File fi;
		Iterator<File> iter = s.iterator();
		while (iter.hasNext()) {
			fi = iter.next();
			bwDup2.write(fi.getAbsolutePath());
			bwDup2.write("\n");
		}
		flush(bwDup2);

		int i, j;
		boolean[][] contains;
		int size1;
		int size2;
		for (Map.Entry<String, LinkedList<File>> e : mName1.entrySet()) {
			if ((ll2 = mName2.get(e.getKey())) != null) {
				ll1 = e.getValue();
				size1 = ll1.size();
				size2 = ll2.size();
				contains = new boolean[size1][size2];
				for (i = 0; i < size1; i++) {
					for (j = 0; j < size2; j++) {
						contains[i][j] = false;
					}
				}
				i = 0;
				for (File f1 : ll1) {
					s = mDup.get(f1);
					if (s != null) {
						j = 0;
						for (File f2 : ll2) {
							if (s.contains(f2)) {
								contains[i][j] = true;
							}
							j++;
						}
					} 
					i++;
				}
				File f1, f2;
				for (i = 0; i < size1; i++) {
					boolean contain = false;
					for (j = 0; j < size2; j++) {
						if (contains[i][j]) {
							contain = true;
							break;
						}
					}
					if (!contain) {
						for (j = 0; j < size2; j++) {
							f1 = ll1.get(i);
							f2 = ll2.get(j);
							if (f1 == f2) {
								continue;
							}
							absolutePath1 = f1.getAbsolutePath();
							absolutePath2 = f2.getAbsolutePath();

							System.out.print(absolutePath1);
							System.out.print("\t was modified to \t");
							System.out.println(absolutePath2);

							deleteSet.put(f1, false);
							newFiles.put(f2, false);
							bwMod1.write(absolutePath1);
							bwMod1.write("\n");

							bwMod2.write(absolutePath2);
							bwMod2.write("\n");

						}
					}
				}
			}
			flush(bwMod1, bwMod2);
		}

		flushClose(bwDup1, bwDup2, fwDup1, fwDup2, bwMod1, bwMod2, fwMod1, fwMod2);
		for (File f : l1) {
			if (deleteSet.get(f)) {
				absolutePath1 = f.getAbsolutePath();
				System.out.print(absolutePath1);
				System.out.println("\t was deleted");
			}
		}
		for (File f : l2) {
			if (newFiles.get(f)) {
				absolutePath2 = f.getAbsolutePath();
				System.out.print(absolutePath2);
				System.out.println("\t is a new file \t");
			}
		}
	}

	public static void flush(Flushable... flush) {
		if (flush != null && flush.length > 0) {
			for (Flushable c : flush) {
				try {
					if (c != null) {
						c.flush();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void close(Closeable... closable) {
		if (closable != null && closable.length > 0) {
			for (Closeable c : closable) {
				try {
					if (c != null) {
						c.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void flushClose(OutputStream... closable) {
		if (closable != null && closable.length > 0) {
			for (OutputStream c : closable) {
				if (c != null) {
					try {
						c.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						c.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static void flushClose(Writer... closable) {
		if (closable != null && closable.length > 0) {
			for (Writer c : closable) {
				if (c != null) {
					try {
						c.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						c.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	// loi khong dong user maped Channel duoc
//		public static byte[] readFileToMemory(String file)
//		throws IOException {
//			FileInputStream fis = new FileInputStream(file);
//			FileChannel fileChannel = fis.getChannel();
//			long size = fileChannel.size();
//			MappedByteBuffer mappedByteBuffer =
//				fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
//			byte[] data = new byte[(int) size];
//			mappedByteBuffer.get(data, 0, (int) size);
//			fileChannel.close();
//			fis.close();
//			return data;
//		}

	public static byte[] readFileToMemory(final String file)
	throws IOException {
		return readFileToMemory(new File(file));
	}

	public static byte[] readFileToMemory(final File file)
	throws IOException {
		if (file.length() < Integer.MAX_VALUE) {
			final FileInputStream fis = new FileInputStream(file);
			final BufferedInputStream bis = new BufferedInputStream(fis);
			final int length = (int) file.length();
			final byte[] data = new byte[length];
			int start = 0;
			int read = 0;
			try {
				while ((read = bis.read(data, start, length - start)) > 0) {
					start += read;
				}
			} finally {
				close(bis, fis);
			}
			return data;
		} else {
			throw new IOException("File is bigger than " + Util.nf.format(Integer.MAX_VALUE) + " bytes");
		}
	}

	public static String readFileAsCharset(File fileName, String charsetName)
	throws IOException {
		byte[] arr = readFileToMemory(fileName);
		String st = new String(arr, charsetName);
		return st;
	}

	public static String readFileAsCharsetMayCheckEncode(String fileName, String charsetName)
	throws IOException {
		byte[] arr = readFileToMemory(fileName);
		String st = "";
		try {
			st = new String(arr, charsetName);
		} catch (UnsupportedEncodingException e) {
			st = readFileWithCheckEncode(fileName, arr);
		}
		return st;
	}

	public static String readFileAsCharsetMayCheckEncode(File file, String charsetName)
	throws IOException {
		return readFileAsCharsetMayCheckEncode(file.getAbsolutePath(), charsetName);
	}

	public static String convertFileContentToUTF8(File file, String oriCharset)
	throws IOException {
		if (file != null) {
			String encode = readFileWithCheckEncode(file.getAbsolutePath());
			return new String(encode.getBytes(oriCharset), "UTF-8");
		} else {
			return null;
		}
	}
	
	public static String readFileWithCheckEncode(String filePath)
	throws FileNotFoundException, IOException,
	UnsupportedEncodingException {
		byte[] byteArr = FileUtil.readFileToMemory(filePath);
		return readFileWithCheckEncode(filePath, byteArr);
	}

	public static String readFileWithCheckEncode(String filePath, byte[] byteArr)
	throws FileNotFoundException, IOException,
	UnsupportedEncodingException {
		if (byteArr.length > 3) {
			Log.d("readFileWithCheckEncode", filePath);
			Log.d("char", (char) byteArr[0] + ": " + (int) byteArr[0]);
			Log.d("char", (char) byteArr[1] + ": " + (int) byteArr[1]);
			Log.d("char", (char) byteArr[2] + ": " + (int) byteArr[2]);

			if (byteArr.length > 3 
				&& (byteArr[0] == -17 && byteArr[1] == -69 && byteArr[2] == -65
				|| byteArr[0] == -61 && byteArr[1] == -96 && byteArr[2] == 13
				|| byteArr[0] == 49 && byteArr[1] == 48 && byteArr[2] == 41
				|| byteArr[0] == 77 && byteArr[1] == -31 && byteArr[2] == -69)) {

				String str = new String(byteArr, HtmlUtil.UTF8);
				Log.d("is UTF8", filePath + " 1");
				return str;
			} else {
				String cs = UniversalDetector.detectCharset(new File(filePath));
				Log.d(TAG, "cs " + filePath + ": " + cs);
				String str;
				if (cs != null) {
					str = new String(byteArr, cs);
				} else {
					str = new String(byteArr, "utf-8");
				}
				// System.err.println("utf8: " + utf8);
				String fontName = HtmlUtil.guessFontName(str);
				Log.d("fontName", "is " + fontName);
				if (HtmlUtil.VU_TIMES.equals(fontName)
					|| HtmlUtil.TIMES_CSX.equals(fontName)
					|| HtmlUtil.TIMES_CSX_1.equals(fontName)) {
					Log.d("is UTF8", filePath + " 2");
					return str;
				} else if (filePath.toLowerCase().contains(".pdf.")) {
					Log.d("is not UTF8", filePath + " pdf String(byteArr)");
					return new String(byteArr);
				} else {
					String notUTF8 = new String(byteArr, ISO_8859_1);
					Log.d("is not UTF8", filePath + " txt String(byteArr, ISO_8859_1)");
					return notUTF8;
				}
			}
		} else {
			return new String(byteArr);
		}
	}

	public static String readFileByMetaTag(File file)
	throws FileNotFoundException, IOException {
		String content = FileUtil.readFileWithCheckEncode(file.getAbsolutePath());
		String charsetName = "";
		if ((charsetName = HtmlUtil.readValue(content, "charset")).length() > 0) {
			Log.d(file.getAbsolutePath() + " charset: "
				  , charsetName);
		}
		try {
			if (charsetName.length() > 0) {
				content = new String(FileUtil.readFileToMemory(file.getAbsolutePath()), charsetName);
				// Log.d(content);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return content;
	}

	public static void printInputStream(InputStream is, OutputStream os,
										String inCharset, String outCharset) throws IOException {
		if (Util.isEmpty(inCharset)) {
			inCharset = "UTF-8";
		}
		if (Util.isEmpty(outCharset)) {
			outCharset = "UTF-8";
		}
		try {
			BufferedReader from = new BufferedReader(
				// new InputStreamReader(new FileInputStream("kanji.txt"),
				new InputStreamReader(is, inCharset));
			PrintWriter to = new PrintWriter(new OutputStreamWriter(
												 // check
												 // enco
												 os, outCharset));
			// new FileOutputStream("sverige.txt"), "ISO8859_3"));

			// reading and writing here...
			String line = from.readLine();
			System.out.println("-->" + line + "<--");
			to.println(line);
			close(from);
			flushClose(to);
		} catch (UnsupportedEncodingException exc) {
			System.err.println("Bad encoding" + exc);
		} catch (IOException err) {
			System.err.println("I/O Error: " + err);
		}
	}

	private static final byte[] UTF8_ESCAPE = new byte[] { -17, -69, -65 };

	public static void writeFileAsCharset(String fileName, String contents,
										  String newCharset) throws IOException {
		writeFileAsCharset(new File(fileName), contents, newCharset);
	}
	
	public static void writeFileAsCharset(File file, String contents,
										  String newCharset) throws IOException {
		Log.d("Writing file", file.getAbsolutePath());
		//		System.out.println(contents);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		File tempF = new File(file.getAbsolutePath() + ".tmp");
		FileOutputStream fos = new FileOutputStream(tempF);
		FileChannel fileChannel = fos.getChannel();
		byte[] oldCharArr = contents.getBytes(newCharset);
		if ("UTF-8".equalsIgnoreCase(newCharset) && 
			!(oldCharArr[0] == -17 && oldCharArr[1] == -69 && oldCharArr[2] == -65
			|| oldCharArr[0] == -61 && oldCharArr[1] == -96 && oldCharArr[2] == 13
			|| oldCharArr[0] == 49 && oldCharArr[1] == 48 && oldCharArr[2] == 41
			|| oldCharArr[0] == 77 && oldCharArr[1] == -31 && oldCharArr[2] == -69)
			) {
			fileChannel.write(ByteBuffer.wrap(UTF8_ESCAPE));
		}
		fileChannel.write(ByteBuffer.wrap(oldCharArr));
		fileChannel.force(true);
		close(fileChannel);
		flushClose(fos);
		file.delete();
		tempF.renameTo(file);
		//boolean renameRet = tempF.renameTo(file);
		//		if (renameRet) {
		//			Log.d("writeFileAsCharset", "rename " + " to " + file + " successfully");
		//		} else {
		//			Log.d("writeFileAsCharset", "rename " + " to " + file + " unsuccessfully");
		//		}
	}

	public static void writeAppendFileAsCharset(File file, String contents,
												String newCharset) throws IOException {
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		raf.seek(raf.length());
		FileChannel fileChannel = raf.getChannel();
		byte[] oldCharArr = contents.getBytes(newCharset);
		if (raf.length() == 0 && "UTF-8".equalsIgnoreCase(newCharset) && 
			! (oldCharArr[0] == -17 && oldCharArr[1] == -69 && oldCharArr[2] == -65
			|| oldCharArr[0] == -61 && oldCharArr[1] == -96 && oldCharArr[2] == 13
			|| oldCharArr[0] == 49 && oldCharArr[1] == 48 && oldCharArr[2] == 41
			|| oldCharArr[0] == 77 && oldCharArr[1] == -31 && oldCharArr[2] == -69)
			) {
			fileChannel.write(ByteBuffer.wrap(UTF8_ESCAPE));
		}
		fileChannel.write(ByteBuffer.wrap(oldCharArr));
		fileChannel.close();
		raf.close();
	}

	public static void changeUtf8NotBOM2Utf8(String src, String dest)
	throws UnsupportedEncodingException, FileNotFoundException,
	IOException {
		writeFileAsCharset(new File(dest), 
						   new String(readFileToMemory(new File(src)), HtmlUtil.UTF8), 
						   HtmlUtil.UTF8);
	}

	public static void saveObj(Object obj, String fileName) throws IOException {
		File f = new File(fileName + ".tmp");
		f.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(f);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(obj);
		flushClose(oos, bos, fos);
		File file = new File(fileName);
		file.delete();
		f.renameTo(file);
	}

	public static Object restore(String fileName) throws IOException,
	ClassNotFoundException {
		FileInputStream fis = new FileInputStream(fileName);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ObjectInputStream in = new ObjectInputStream(bis);
		Object obj = in.readObject();
		close(in, bis, fis);
		return obj;
	}
	
	public static void is2OsNotCloseOs(InputStream is, BufferedOutputStream bos)
	throws IOException {
		BufferedInputStream bis = new BufferedInputStream(is);
		byte[] barr = new byte[32768];
		int read = 0;
		while ((read = bis.read(barr)) > 0) {
			bos.write(barr, 0, read);
		}
		close(bis, is);
		bos.flush();
	}

	public static void is2OS(InputStream is, OutputStream os) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(os);
		BufferedInputStream bis = new BufferedInputStream(is);
		byte[] barr = new byte[32768];
		int read = 0;
		try {
			while ((read = bis.read(barr)) != -1) {
				bos.write(barr, 0, read);
			}
		} finally {
			close(bis);
			flushClose(bos);
		}
	}

	public static byte[] is2Barr(InputStream is, boolean autoClose) throws IOException {
		int count = 0;
		int len = 65536;
		byte[] buffer = new byte[len];
		BufferedInputStream bis = new BufferedInputStream(is);
		ByteArrayOutputStream bb = new ByteArrayOutputStream(20 << 1);
		while ((count = bis.read(buffer, 0, len)) != -1) {
			bb.write(buffer, 0, count);
		}
		if (autoClose) {
			FileUtil.close(bis, is);
		} 
		
		return bb.toByteArray();
	}
	
	public static byte[] is2Barr(final InputStream in, byte[] bytes)
	throws IOException {
		int length = bytes.length;
		int count = 0;
		int read = 0;
		while (length - read > 0 && (count = in.read(bytes, read, length - read)) != -1) {
			read += count;
		}
		if (length - read > 0) {
			Arrays.fill(bytes, (byte)0);
		}
		return bytes;
	}

	public static void bArr2File(byte[] barr, String fileName)
	throws IOException {
		File file = new File(fileName);
		file.getParentFile().mkdirs();
		File tempFile = new File(fileName + ".tmp");
		FileOutputStream fos = new FileOutputStream(tempFile);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		try {
			bos.write(barr, 0, barr.length);
		} finally {
			flushClose(bos, fos);
			file.delete();
			tempFile.renameTo(file);
		}
	}

	public static void is2File(InputStream is, String fileName)
	throws IOException {
		File file = new File(fileName);
		file.getParentFile().mkdirs();
		File tempFile = new File(fileName + ".tmp");
		FileOutputStream fos = new FileOutputStream(tempFile);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		BufferedInputStream bis = new BufferedInputStream(is);
		byte[] barr = new byte[32768];
		int read = 0;
		try {
			while ((read = bis.read(barr)) > 0) {
				bos.write(barr, 0, read);
			}
		} finally {
			close(is, bis);
			flushClose(bos, fos);
			file.delete();
			tempFile.renameTo(file);
		}
	}

	public static void stringToFile(String fileName, String contents)
	throws IOException {
		Log.d("writeContentToFile", fileName);
		File f = new File(fileName);
		f.getParentFile().mkdirs();
		File tempFile = new File(fileName + ".tmp");
		FileWriter fw = new FileWriter(tempFile);
		BufferedWriter bw = new BufferedWriter(fw);
		if (contents != null && contents.length() > 0) {
			bw.write(contents);
			flushClose(bw); //, bo fw vi loi
			f.delete();
			tempFile.renameTo(f);
		}
	}

	public static void collectionToFile(Collection<?> collection, File file)
	throws IOException {
		if (collection != null && file != null) {
			file.getParentFile().mkdirs();
			File tempFile = new File(file.getAbsolutePath() + ".tmp");
			FileWriter fw = new FileWriter(tempFile);
			BufferedWriter bw = new BufferedWriter(fw);
			for (Object obj : collection) {
				bw.append(obj.toString()).append("\n");
			}
			flushClose(bw);
			file.delete();
			tempFile.renameTo(file);
		}
	}

	/**
	 * Lưu từng item của iterator thành 1 dòng của file
	 * 
	 * @param iter
	 * @param file
	 * @throws IOException
	 */
	public static void iter2File(Iterator<?> iter, File file) throws IOException {
		if (iter != null && file != null) {
			file.getParentFile().mkdirs();
			File tempFile = new File(file.getAbsolutePath() + ".tmp");
			FileWriter fw = new FileWriter(tempFile);
			BufferedWriter bw = new BufferedWriter(fw);
			for (; iter.hasNext();) {
				bw.append(iter.next().toString()).append("\n");
			}
			flushClose(bw, fw);
			file.delete();
			tempFile.renameTo(file);
		}
	}

	public static void copyResourceToFile(File root, String resourcePath) {
		File destFile = new File(root, resourcePath);
		Log.d("resourcePath", resourcePath);
		Log.d("destFile", destFile.getAbsolutePath());
		if (!destFile.exists()) {
			InputStream is = null;
			try {
				is = FileUtil.class.getClassLoader().getResourceAsStream(resourcePath);
				is2File(is, destFile.getAbsolutePath());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
	}

	/**
	 0: number of files
	 1: total length
	 2: number of directories
	 */
	public static void getDirSize(final File f, final long[] l) {
		if (!f.isDirectory()) {
			l[0]++;
			l[1] += f.length();
		} else {
			LinkedList<File> stk = new LinkedList<File>();
			stk.add(f);
			File fi = null;
			File[] fs;
			while (stk.size() > 0) {
				fi = stk.pop();
				fs = fi.listFiles();
				if (fs != null)
					for (File f2 : fs) {
						if (f2.isDirectory()) {
							stk.push(f2);
							l[2]++;
						} else {
							l[0]++;
							l[1] += f2.length();
						}
					}
			}
		}
	}

	public static Collection<File> getFiles(String fs, boolean includeFolder) {
		if (fs != null) {
			return FileUtil.getFiles(new File(fs), includeFolder);
		} else {
			return new LinkedList<File>();
		}
	}

	public static Collection<File> getFilesAndFolder(File fs) {
		if (fs == null || !fs.exists()) {
			return new ArrayList<File>(0);
		}
		if (!fs.isDirectory()) {
			List<File> lf = new ArrayList<File>(1);
			lf.add(fs);
			return lf;
		} else {
			return getFilesAndFolders(fs.listFiles());
		}
	}

	public static Collection<File> getFilesAndFolders(Collection<String> fs) {
		File[] fss = Util.collectionString2FileArray(fs,"");
		return getFilesAndFolders(fss);
	}
	
	public static Collection<File> getFilesAndFolders(File[] fs) {
		final Set<File> set = new TreeSet<File>(); // dung set de tranh trung
		final LinkedList<File> folderQueue = new LinkedList<File>();
		for (File f : fs) {
			set.add(f);
			if (f.isDirectory()) {
				folderQueue.add(f);
			} 
		}
		File fi = null;
		while (folderQueue.size() > 0) {
			fi = folderQueue.removeFirst();
			fs = fi.listFiles();
			if (fs != null) {
				for (File f : fs) {
					set.add(f);
					if (f.isDirectory()) {
						folderQueue.add(f);
					} 
				}
			}
		}
		return set;
	}

	public static long getFolderSize(final File f, final OnProgressUpdate<Long> updateState) {
		Log.d("getFolderSize f", f.getAbsolutePath());
		long size = 0;
		long start = System.currentTimeMillis();
		if (f != null && f.exists()) {
			final LinkedList<File> folderQueue = new LinkedList<File>();
			if (f.isDirectory()) {
				folderQueue.push(f);
			} else {
				size += f.length();
			}
			File fi = null;
			File[] fs;
			if (updateState != null) {
				while (folderQueue.size() > 0) {
					fi = folderQueue.removeFirst();
					fs = fi.listFiles();
					if (fs != null) {
						for (File f2 : fs) {
							if (f2.isDirectory()) {
								folderQueue.push(f2);
							} else {
								size += f2.length();
							}
							updateState.onUpdate(size);
						}
					}
				}
			} else {
				while (folderQueue.size() > 0) {
					fi = folderQueue.removeFirst();
					fs = fi.listFiles();
					if (fs != null) {
						for (File f2 : fs) {
							if (f2.isDirectory()) {
								folderQueue.push(f2);
							} else {
								size += f2.length();
							}
						}
					}
				}
			}
			
		}
		Log.d("getFolderSize f", f.getAbsolutePath() + ": " + Util.nf.format(size) + ", took " + (System.currentTimeMillis() - start));
		return size;
	}

	public static Collection<File> getFiles(File f, boolean includeFolder) {
		Log.d("getFiles f", f.getAbsolutePath());
		final List<File> fList = new LinkedList<File>();
		if (f != null) {
			final LinkedList<File> folderQueue = new LinkedList<File>();
			if (f.isDirectory()) {
				if (includeFolder) {
					fList.add(f);
				}
				folderQueue.push(f);
			} else {
				fList.add(f);
			}
			File fi = null;
			File[] fs;
			while (folderQueue.size() > 0) {
				fi = folderQueue.removeFirst();
				fs = fi.listFiles();
				if (fs != null) {
					for (File f2 : fs) {
						if (f2.isDirectory()) {
							folderQueue.push(f2);
							if (includeFolder) {
								fList.add(f2);
							}
						} else {
							fList.add(f2);
						}
					}
				}
			}
		}
		return fList;
	}

	public static Collection<File> getFiles(String[] fs, boolean includeFolder) {
		if (fs != null) {
			File[] farr = new File[fs.length];
			int i = 0;
			for (String f : fs) {
				farr[i++] = new File(f);
			}
			return FileUtil.getFiles(farr, includeFolder);
		} else {
			return new LinkedList<File>();
		}
	}

	public static Collection<File> getFiles(File[] fs, boolean includeFolder) {
		if (fs == null) {
			return new LinkedList<File>();
		}
		final Set<File> set = new TreeSet<File>();
		final LinkedList<File> folderQueue = new LinkedList<File>();
		for (File f : fs) {
			if (f.isDirectory()) {
				folderQueue.push(f);
				if (includeFolder) {
					set.add(f);
				}
			} else {
				set.add(f);
			}
		}
		File fi = null;
		while (folderQueue.size() > 0) {
			fi = folderQueue.pop();
			fs = fi.listFiles();
			if (fs != null)
				for (File f : fs) {
					if (f.isDirectory()) {
						folderQueue.push(f);
						if (includeFolder) {
							set.add(f);
						}
					} else {
						set.add(f);
					}
				}
		}
		return set;
	}

	// patStr == null || "": accept all
	public static Collection<File> getFiles(final String ff[], final String patternStr, boolean includeFolder) {
		Pattern pat = null;
		if (patternStr != null && patternStr.trim().length() > 0) {
			pat = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
		}
		return getFiles(ff, pat, includeFolder);
	}

	public static Collection<File> getFiles(final String ff[], final Pattern pattern, boolean includeFolder) {
		File[] farr = new File[ff.length];
		if (ff != null) {
			int i = 0;
			for (String f : ff) {
				farr[i++] = new File(f);
			}
		}
		return getFiles(farr, pattern, includeFolder);
	}

	public static Collection<File> getFiles(final File ff[], final Pattern pattern, boolean includeFolder) {
		final Set<File> set = new TreeSet<File>();
		if (ff != null)
			for (File f : ff) {
				set.addAll(getFiles(f, pattern, includeFolder));
			}
		return set;
	}
	
	public static Collection<File> getFiles(final File ff, final Pattern pattern, boolean includeFolder) {
		final Set<File> l = new TreeSet<File>();
		if (ff != null) {
			if (!ff.isDirectory()) {
				if (pattern == null) {
					l.add(ff);
				} else {
					Matcher mat = pattern.matcher(ff.getName());
					if (mat.matches()) {
						l.add(ff);
					}
				}
			} else {
				final LinkedList<File> folderQueue = new LinkedList<>();
				folderQueue.add(ff);
				File[] fs;
				File fi = null;
				if (includeFolder) {
					l.add(ff);
				}
				while (folderQueue.size() > 0) {
					fi = folderQueue.pop();
					fs = fi.listFiles();
					if (fs != null) {
						for (File f : fs) {
							if (f.isDirectory()) {
								folderQueue.push(f);
								if (includeFolder) {
									l.add(f);
								}
							} else {
								if (pattern == null) {
									l.add(f);
								} else {
									Matcher mat = pattern.matcher(f.getName());
									if (mat.matches()) {
										l.add(f);
									}
								}
							}
						}
					}
				}
				
			}
		}
		return l;
	}
	
	public static Collection<LayoutElement> getFilesBy(final Collection<LayoutElement> ff, String in, boolean includeFolder) {
		if (ff != null && ff.size() > 0) {
			final List<LayoutElement> lf = new LinkedList<>();
			for (LayoutElement f : ff) {
				final Collection<File> filesBy = getFilesBy(f.bf.f, in, includeFolder);
				for (File fi : filesBy) {
					lf.add(new LayoutElement(fi));
				}
			}
			return lf;
		} else {
			return new LinkedList<LayoutElement>();
		}
	}

//	public static Collection<File> getFilesBy(final Collection<File> ff, String in, boolean includeFolder) {
//		if (ff != null && ff.size() > 0) {
//			final List<File> lf = new LinkedList<File>();
//			for (File f : ff) {
//				lf.addAll(getFilesBy(f, in, includeFolder));
//			}
//			return lf;
//		} else {
//			return new LinkedList<File>();
//		}
//	}

	public static Collection<File> getFilesBy(final File[] ff, String in, boolean includeFolder) {
		if (ff != null && ff.length > 0) {
			final List<File> lf = new LinkedList<>();
			for (File f : ff) {
				lf.addAll(getFilesBy(f, in, includeFolder));
			}
			return lf;
		} else {
			return new LinkedList<File>();
		}
	}
	
	public static Collection<File> getFilesBy(final File ff, String in, final boolean includeFolder) {
		final Set<File> lf = new TreeSet<>();
		if (ff != null && !Util.isEmpty(in)) {
			in = ".*?" + in.replaceAll(HtmlUtil.SPECIAL_CHAR_PATTERNSTR, "\\\\$1") + ".*?";
			final Pattern pat = Pattern.compile(in, Pattern.CASE_INSENSITIVE);
			if (!ff.isDirectory()) {
				if (pat.matcher(ff.getName()).matches()) {
					lf.add(ff);
				}
			} else {
				File[] fs;
				File fi = null;
				final LinkedList<File> folders = new LinkedList<>();
				folders.push(ff);
				if (includeFolder && pat.matcher(ff.getName()).matches()) {
					lf.add(ff);
				}
				String fName;
				while (folders.size() > 0) {
					fi = folders.pop();
					fs = fi.listFiles();
					if (fs != null) {
						for (File f : fs) {
							fName = f.getName();
							if (f.isDirectory()) {
								folders.push(f);
								if (includeFolder && pat.matcher(fName).matches() && !lf.contains(f)) {
									lf.add(f);
								}
							} else {
								if (pat.matcher(fName).matches() && !lf.contains(f)) {
									lf.add(f);
								}

							}
						}
					}
				}
			}
		}
		return lf;
	}
	
	public static Collection<File> getFilesBySuffix(final File ff, final String suffix, boolean includeFolder) {
		final List<File> lf = new LinkedList<File>();
		if (ff != null) {
			boolean isSuffixEmpty = Util.isEmpty(suffix);
			String[] suffixes = null;
			if (!isSuffixEmpty) {
				suffixes = suffix.toLowerCase().split(";\\s*");
				Arrays.sort(suffixes);
			}
			if (!ff.isDirectory()) {
				if (isSuffixEmpty) {
					lf.add(ff);
				} else {
					String fName = ff.getName();
					int lastIndexOf = fName.lastIndexOf(".");
					if (lastIndexOf >= 0) {
						String extLower = fName.substring(lastIndexOf).toLowerCase();
						boolean chosen = Arrays.binarySearch(suffixes, extLower) >= 0;
						if (chosen) {
							lf.add(ff);
						}
					}
				}
			} else {
				File[] fs;
				File fi = null;
				final LinkedList<File> folders = new LinkedList<>();
				if (includeFolder) {
					lf.add(ff);
				}
				while (folders.size() > 0) {
					fi = folders.pop();
					fs = fi.listFiles();
					if (fs != null) {
						for (File f : fs) {
							if (f.isDirectory()) {
								folders.push(f);
								if (includeFolder) {
									lf.add(f);
								}
							} else {
								if (isSuffixEmpty) {
									lf.add(f);
								} else {
									String fName = f.getName();
									int lastIndexOf = fName.lastIndexOf(".");
									if (lastIndexOf >= 0) {
										String extLower = fName.substring(lastIndexOf).toLowerCase();
										boolean chosen = Arrays.binarySearch(suffixes, extLower) >= 0;
										if (chosen) {
											lf.add(f);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return lf;
	}

	public static List<File> getFiles(final String ff[], final Pattern includePattern, final Pattern excludePattern) {
		File[] farr = null;
		if (ff != null) {
			farr = new File[ff.length];
			int i = 0;
			for (String f : ff) {
				farr[i++] = new File(f);
			}
		}
		return getFiles(farr, includePattern, excludePattern);
	}

	public static List<File> getFiles(final File ff[], final Pattern includePattern, final Pattern excludePattern) {
		if (ff != null) {
			final Set<File> set = new TreeSet<File>();
			for (File f : ff) {
				set.addAll(getFiles(f, includePattern, excludePattern));
			}
			ArrayList<File> arrayList = new ArrayList<File>(set.size());
			arrayList.addAll(set);
			return arrayList;
		} else {
			return new LinkedList<File>();
		}
	}

	public static Collection<File> getFiles(final File ff, Pattern includePattern, Pattern excludePattern) {
		final List<File> lf = new LinkedList<File>();
		if (ff != null) {
			final LinkedList<File> folders = new LinkedList<File>();
			if (includePattern == null || includePattern.pattern().trim().length() == 0) {
				includePattern = Pattern.compile(".*");
			}
			if (excludePattern == null || excludePattern.pattern().trim().length() == 0) {
				excludePattern = Pattern.compile("[^\u0000-\uffff]+");
			}
			if (!ff.isDirectory()) {
				String fName = ff.getName();
				Matcher inMatcher = includePattern.matcher(fName);
				Matcher exMatcher = excludePattern.matcher(fName);
				if (inMatcher.matches() && !exMatcher.matches()) {
					lf.add(ff);
				}
			} else {
				folders.push(ff);
//				Log.d("ff", ff.getAbsolutePath());
				File[] fs;
				File fi = null;
				while (folders.size() > 0) {
					fi = folders.pop();
					fs = fi.listFiles();
					if (fs != null)
//					Log.d("fs", fs + " != null");
						for (File f : fs) {
							if (f.isDirectory()) {
								folders.push(f);
							} else {
								String fName = f.getName();
								Matcher inMatcher = includePattern.matcher(fName);
								Matcher exMatcher = excludePattern.matcher(fName);
//								Log.d("inMatcher", includePattern.pattern());
//								Log.d("exMatcher", excludePattern.pattern());
//								Log.d("fName", fName);
//								Log.d("in.matches", inMatcher.matches() + "");
//								Log.d("ex.matches", exMatcher.matches() + "");
								if (inMatcher.matches() && !exMatcher.matches()) {
									lf.add(f);
								}
							}
						}
				}
			}
		}
		return lf;
	}

	// suffixFile la danh sach 4 chu cuoi
	//	public static void getFiles(File[] files, List<File> fileList, String suffixFiles) {
	//		for (File file : files) {
	//			if (file.isDirectory()) {
	//				getFiles(file.listFiles(), fileList, suffixFiles);
	//			} else if (!file.isHidden()) {
	//				if (suffixFiles != null && suffixFiles.trim().length() > 0) {
	//					String path = file.getAbsolutePath().toLowerCase();
	//					String suffix = path.substring(path.lastIndexOf(".") + 1);
	//					if (suffixFiles.toLowerCase().contains(suffix)) {
	//						fileList.add(file);
	//					}
	//				} else {
	//					fileList.add(file);
	//				}
	//			}
	//		}
	//	}

	public static boolean delete(File file) {
		file.setWritable(true);
		try {
			if (!file.delete()) {
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(0);
				fos.flush();
				fos.close();
			}
			Log.d("delete", "Deleted file: " + file + " successfully");
			return true;
		} catch (IOException e) {
			Log.d("delete", "The deleting file: " + file + " is not successfully", e);
			return false;
		}
	}

	public static List<File> currentFileFolderListing(File f) {
		List<File> l = new LinkedList<File>();
		if (f != null) {
			if (f.isFile()) {
				l.add(f);
			} else {
				File[] files = f.listFiles();
				if (files == null) {
					l.add(f);
				} else {
					for (File file : files) {
						l.add(file);
					}
				}
			}
		}
		return l;
	}

	public static StringBuilder listFilesByName(File f, String lineSep) {
		Log.d("listFileByName f", "" + f);
		StringBuilder sb = new  StringBuilder();
		if (f != null) {
			final LinkedList<File> stk = new LinkedList<File>();
			if (f.isDirectory()) {
				stk.push(f);
			} else {
				sb.append(f.getAbsolutePath()).append(": ").append(f.length()).append(" bytes.").append(lineSep);
			}
			File fi = null;
			File[] fs;
			while (stk.size() > 0) {
				fi = stk.pop();
				fs = fi.listFiles();
				if (fs != null)
					for (File f2 : fs) {
						if (f2.isDirectory()) {
							stk.push(f2);
						} else {
							sb.append(f2.getAbsolutePath()).append(": ").append(f2.length()).append(" bytes.").append(lineSep);
						}
					}
			}

		}
		return sb;
	}

	public static StringBuilder deleteFiles(File file, String lineSep, boolean includeFolder, String ext) {
		StringBuilder sb = new StringBuilder();
		return deleteFiles(file, sb, includeFolder,ext);
	}

	/**
	 * @param file file to delete
	 * @param lineSep lineSep for summary
	 * @param sb StringBuilder to store results
	 * @param includeFolder whether also delete folder or not
	 * @param ext file extension nào sẽ bị xóa
	 * @return
	 */
	public static StringBuilder deleteFiles(File file, 
											StringBuilder sb, boolean includeFolder, 
											String ext) {
		Collection<File> l = getFilesBySuffix(file, ext, includeFolder);
		if (sb == null) {
			sb = new StringBuilder();
		}
		for (File f : l) {
			delete(f, sb);
		}
		return sb;
	}

	private static void delete(File file, StringBuilder sb) {
		long length = file.length();
		boolean deleted = file.delete();
		if (deleted) {
			sb.append(file.getAbsolutePath() + " length " + length + " bytes, deleted.\n");
		} else {
			sb.append(file.getAbsolutePath() + " length " + length + " bytes, can't delete.\n");
		}
	}

	public static int deleteFiles(File file, boolean includeFolder) {
		Collection<File> l = getFiles(file, includeFolder);
		for (File f : l) {
			delete(f);
		}
		return l.size();
	}

	public static void fastFileCopy(File source, File target) {
		FileChannel in = null;
		FileChannel out = null;
		long start =  System.currentTimeMillis();
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(source);
			in = fis.getChannel();
			fos = new FileOutputStream(target);
			out = fos.getChannel();

			long size = in.size();
			long transferred = in.transferTo(0, size, out);

			while(transferred != size){
				transferred += in.transferTo(transferred, size - transferred, out);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(fis);
			close(fos);
			close(in);
			close(out);
		}
		long end = System.currentTimeMillis();

		Log.d(target.getAbsolutePath(), 
			  Util.nf.format(source.length()) + "B: " + 
			  ((end - start > 0) ? Util.nf.format(source.length() / (end - start)) : 0) + " KB/s");
	}

	public static void fastBufferFileCopy(File source, File target) {
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		long start =  System.currentTimeMillis();
		FileInputStream fis = null;
		FileOutputStream fos = null;
		long size = source.length();
		try {
			fis = new FileInputStream(source);
			bis = new BufferedInputStream(fis);
			fos = new FileOutputStream(target);
			bos = new BufferedOutputStream(fos);

			byte[] barr = new byte[Math.min((int)size, 1 << 20)];
			int read = 0;
			while((read = bis.read(barr)) != -1){
				bos.write(barr, 0, read);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(fis);
			close(fos);
			close(bis);
			close(bos);
		}
		long end = System.currentTimeMillis();

		String srcPath = source.getAbsolutePath();
		Log.d("Copied " + srcPath + " to " + target, 
			  ", took " + (end - start)/1000 + " ms, total size " + Util.nf.format(size) + " Bytes, speed " + 
			  ((end - start > 0) ? Util.nf.format(size / (end - start)) : 0) + "KB/s");
	}

	public static void copySaveLastModified(String[] fs, String destDir, boolean includeSrcDir) {
		for (String f : fs) {
			FileUtil.copySaveLastModified(f, destDir, includeSrcDir);
		}
	}

	public static void copySaveLastModified(File[] fs, File destDir, boolean includeSrcDir) {
		for (File f : fs) {
			copySaveLastModified(f, destDir, includeSrcDir);
		}
	}

	public static void copySaveLastModified(String srcDir, String destDir, boolean includeSrcDir) {
		copySaveLastModified(new File(srcDir), new File(destDir), includeSrcDir);
	}

	public static void copySaveLastModified(File srcDir, File destDir, boolean includeSrcDir) {
		long size = 0;
		long start = System.currentTimeMillis();
		if (srcDir.isDirectory()) {
			final Collection<File> lf = getFiles(srcDir, false);
			int len = 0;
			if (includeSrcDir) {
				len = srcDir.getParentFile().getAbsolutePath().length();
			} else {
				len = srcDir.getAbsolutePath().length();
			}
			String destPath = destDir.getAbsolutePath();
			int counter = 0;

			for (File f : lf) {
				String sub = f.getAbsolutePath().substring(len);//.replace('\\', '/');
				File destF = new File(destPath + "/" + sub);
				if (!destF.exists() || f.length() != destF.length() || f.lastModified() != destF.lastModified()) {
					File parentFile = destF.getParentFile();
					if (!parentFile.exists()) {
						parentFile.mkdirs();
					}
					size += f.length();
					System.out.print(++counter + "/" + lf.size() + " copying: " + f + ": ");
					fastFileCopy(f, destF);
					//					fastBufferFileCopy(f, destF);
					destF.setLastModified(f.lastModified());
				}
			}

		} else {
			if (!destDir.exists() || srcDir.length() != destDir.length() || srcDir.lastModified() != destDir.lastModified()) {
				destDir.getParentFile().mkdirs();
				size += srcDir.length();
				//				fastFileCopy(srcDir, destDir);
				fastBufferFileCopy(srcDir, destDir);
				destDir.setLastModified(srcDir.lastModified());
			}
		}
		long end = System.currentTimeMillis();
		Log.d("Copied " + srcDir.getAbsolutePath() + " to " + destDir, 
			  ", took " + (end - start)/1000 + " s, total size " + Util.nf.format(size) + " Bytes, speed " + 
			  ((end - start > 0) ? Util.nf.format(size / (end - start)) : 0) + "KB/s");
	}


	/**
	 * Accessing Resources from a JAR
	 File
	 You can construct a URL object by using the
	 reference of a resource in a JAR file.
	 The JAR file URL syntax is
	 jar:<url>!/{entry}
	 The following URL refers to an images/logo.bmp
	 JAR entry in a test.jar file on www.java2s.com
	 using the HTTP protocol:
	 jar:http://www.java2s.com/test.jar!/images/logo.bmp
	 The following URL refers to an images/logo.bmp
	 JAR entry in a test.jar file on the local file system
	 in the c:\jarfiles\ directory using the file protocol:
	 jar:file:/c:/jarfiles/test.jar!/images/logo.bmp
	 To read the images/logo.bmp file from a JAR file
	 in the classpath, you can get an input stream
	 object using a class object as follows:
	 // Assuming that the   Test class is in the   CLASSPATH
	 Class cls = Test.class;
	 InputStream in = cls.getResourceAsStream( "/images/logo.bmp" )
	 You can also get a URL object for an entry in your
	 JAR file, which is in your classpath as follows:
	 URL  url = cls.getResource( "/images/logo.bmp" );
	 */

	public static void createJAR(String jarFileName, String root) throws Exception {
		Collection<File> lf = FileUtil.getFiles(root, false);

		if (!root.endsWith("/")) {
			root = root + "/";
		}

		Manifest manifest = getManifest();

		Log.i("createJar", jarFileName);
		FileOutputStream fos = new FileOutputStream(jarFileName);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		JarOutputStream jos = new JarOutputStream(bos, manifest); 
		jos.setLevel(Deflater.DEFAULT_COMPRESSION);


		//		ArrayList<File> lf = new ArrayList<File>(jarEntries.length);
		//		lf.addAll(Arrays.asList(jarEntries));
		//		Collections.sort(lf);
		//		File manif = new File(root + "META-INF/MANIFEST.MF");
		//		int idx = Collections.binarySearch(lf, manif);
		//		System.out.println(idx);
		//		System.out.println(lf.get(0));
		//		System.out.println(lf.get(idx));
		//		File f = lf.remove(0);
		//		lf.add(0, manif);
		//		lf.remove(idx);
		//		lf.add(idx, f);
		//		System.out.println(lf.get(0));
		//		System.out.println(lf.get(idx));


		File[] toArray = lf.toArray(new File[lf.size()]);
		for (File entryFile : toArray) {//lf
			if (!entryFile.exists()) {
				Log.e("Error", entryFile + " is not existed, continue next files");
				continue;
			}
			String absolutePath = entryFile.getAbsolutePath().replaceAll("\\\\", "/");
			String substring = absolutePath.substring(root.length());
			Log.d("JarEntry", substring);
			JarEntry je = new JarEntry(substring);
			jos.putNextEntry(je);
			copyFileToOutputStreamFlushNotClose(jos, entryFile);
			jos.closeEntry();
		}

		jos.close();

		//		ZipFile zf = new ZipFile(jarFileName);
		//		Enumeration<? extends ZipEntry> entries = zf.entries();
		//		while (entries.hasMoreElements()) {
		//			System.out.println(entries.nextElement());
		//		}
		//		zf.close();
	}

	public static void copyFileToOutputStreamFlushNotClose(JarOutputStream jos, File
														   entryFileName)
	throws IOException, FileNotFoundException {
		FileInputStream fis = new FileInputStream(entryFileName);
		BufferedInputStream bis = new BufferedInputStream(fis);
		byte[] buffer = new byte[4096];
		int count = -1;
		while ((count = bis.read(buffer)) != -1) {
			jos.write(buffer, 0, count);
		}
		jos.flush();
		bis.close();
		fis.close();
	}

//	Manifest-Version: 1.0
//	Created-By: 1.7.0_06 (Oracle Corporation)
//	Main-Class: MyPackage.MyClass
//	Class-Path: MyUtils.jar
//	Sealed: true
//	Name: java/util/
//	Sealed: false
//	Specification-Title: Java Utility Classes
//	Specification-Version: 1.2
//	Specification-Vendor: Example Tech, Inc.
//	Implementation-Title: java.util 
//	Implementation-Version: build57
//	Implementation-Vendor: Example Tech, Inc.
	public static Manifest getManifest() {
		Manifest manifest = new Manifest();
		Attributes mainAttribs = manifest.getMainAttributes();
		mainAttribs.put(Attributes.Name.MANIFEST_VERSION, "1.0" );
		//mainAttribs.put(Attributes.Name.MAIN_CLASS, "com.java2s.Test" );
		//mainAttribs.put(Attributes.Name.SEALED, "true" );
		//Map<String, Attributes> attribsMap = manifest.getEntries();
		//Attributes a1 = getAttribute( "Sealed" , "false" );
		//attribsMap.put( "com/java2s/" , a1);
		//Attributes a2 = getAttribute( "Content-Type" , "image/bmp" );
		//attribsMap.put( "images/logo.bmp" , a2);
		return manifest;
	}

	public static Attributes getAttribute(String name, String value) {
		Attributes a = new Attributes();
		Attributes.Name attribName = new Attributes.Name(name);
		a.put(attribName, value);
		return a;
	}

}



