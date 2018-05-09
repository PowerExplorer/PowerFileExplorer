package net.gnu.util;

import java.util.*;
import java.io.*;
import java.util.Map.Entry;
import java.net.*;
import java.text.*;
import java.lang.reflect.*;
import android.util.*;
import com.amaze.filemanager.ui.LayoutElement;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
//import org.apache.james.mime4j.stream.MimeConfig;
//import org.apache.james.mime4j.message.DefaultMessageBuilder;
//import org.apache.james.mime4j.dom.Message;
//import org.apache.james.mime4j.dom.Multipart;
//import org.apache.james.mime4j.dom.field.ContentTypeField;

public class Util {
	private static final String TAG = "Util";
	
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss:SSS");
    public static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("#,###0.0");
    public static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("#,##0");

	public static final DateFormat dtf = DATETIME_FORMAT;
	public static final DateFormat df = DATE_FORMAT;
	public static final NumberFormat nf = NumberFormat.getInstance();

//	public static void parseMhtToFile(File mhtFile, File outDir) throws IOException {
//		if (outDir == null) {
//			outDir = mhtFile.getParentFile(); 
//		}
//		// File baseName will be used in generating new filenames
//		String mhtBaseName = mhtFile.getName().replaceFirst("~/\\.[^\\.]+$/", "");
//
//		// -- Set up Mime parser, using Default Message Builder
//		MimeConfig parserConfig  = new MimeConfig();
//		parserConfig.setMaxHeaderLen(-1); // The default is a mere 10k
//		parserConfig.setMaxLineLen(-1); // The default is only 1000 characters.
//		parserConfig.setMaxHeaderCount(-1); // Disable the check for header count.
//		DefaultMessageBuilder builder = new DefaultMessageBuilder();
//		builder.setMimeEntityConfig(parserConfig);
//
//		// -- Parse the MHT stream data into a Message object
//		System.out.println("Parsing ${mhtFile}...");
//		InputStream mhtStream = new FileInputStream(mhtFile);
//		Message message = builder.parseMessage(mhtStream);
//
//		// -- Process the resulting body parts, writing to file
//		//assert message.getBody() instanceof Multipart
//		Multipart multipart = (Multipart) message.getBody();
//		List<org.apache.james.mime4j.dom.Entity> parts = multipart.getBodyParts();
//		int i = 0;
//		for (org.apache.james.mime4j.dom.Entity p : parts) {
//			ContentTypeField cType = (ContentTypeField) p.getHeader().getField("mimeType");
//			System.out.println("${p.class.simpleName}\t${i}\t${cType.mimeType}");
//
//
//			// Assume mime sub-type is a "good enough" file-name extension 
//			// e.g. text/html = html, image/png = png, application/json = json
//			String partFileName = mhtBaseName + i + "." + cType.getSubType();
//			File partFile = new File(outDir, partFileName);
//
//			// Write part body stream to file
//			System.out.println("Writing ${partFile}...");
//			if (partFile.exists()) partFile.delete();
//			InputStream partStream = p.getBody().getinputStream;
//			partFile.append(partStream);
//		}
//	}

	public static <T> List<T> array2Collection(T[] oriArr) {
		if (oriArr != null) {
			return new ArrayList<T>(Arrays.asList(oriArr));
		} else {
			return new ArrayList<T>(0);
		}
	}

	public static <T> T[] collection2Array(Collection<T> col) {
		Object[] ts = new Object[col.size()];
		int i = 0;
		for (T t : col) {
			ts[i++] = t;
		}
		return (T[])ts;
	}

	public static File[] collectionString2FileArray(Collection<String> oriCollection) {
		File[] array = new File[oriCollection.size()];
		int i = 0;
		for (String f : oriCollection) {
			array[i++] = new File(f);
		}
		return array;
	}

	public static File[] collection2FileArray(final Collection<File> oriCollection) {
		final File[] array = new File[oriCollection.size()];
		int i = 0;
		for (File f : oriCollection) {
			array[i++] = f;
		}
		return array;
	}

	public static File[] collectionString2FileArray(final Collection<String> oriCollection, String parentPath) {
		final File[] array = new File[oriCollection.size()];
		int i = 0;
		for (String s : oriCollection) {
			array[i++] = new File(parentPath, s);
		}
		return array;
	}

	public static ArrayList<String> collectionFile2StringArrayList(final Collection<LayoutElement> oriCollection) {
		final ArrayList<String> l = new ArrayList<>(oriCollection.size());
		for (LayoutElement s : oriCollection) {
			l.add(s.path);
		}
		return l;
	}

//	public static ArrayList<String> collectionFile2StringArrayList(final Collection<File> oriCollection) {
//		final ArrayList<String> l = new ArrayList<>(oriCollection.size());
//		for (File s : oriCollection) {
//			l.add(s.getAbsolutePath());
//		}
//		return l;
//	}

	public static ArrayList<File> collectionString2FileArrayList(final Collection<String> oriCollection) {
		final ArrayList<File> array = new ArrayList<File>(oriCollection.size());
		for (String s : oriCollection) {
			array.add(new File(s));
		}
		return array;
	}

	public static List<StringBuilder> propertiesToListString(final Map prop) {
		final ArrayList<StringBuilder> l = new ArrayList<>();
		StringBuilder sb;
		if (prop != null) {
			final Set<Map.Entry<String, String>> s = prop.entrySet();
			for (Map.Entry<String, String> st : s) {
				sb = new StringBuilder();
				sb.append(st.getKey()).append(": ").append(st.getValue());
				l.add(sb);
			}
		}
		return l;
	}

	public static String propertiesToString(Map prop) {
		StringBuilder sb = new StringBuilder();
		if (prop != null) {
			Set<Map.Entry<String, String>> s = prop.entrySet();
			for (Map.Entry<String, String> st : s) {
				sb.append(st.getKey()).append("=").append(st.getValue()).append("\n");
			}
		}
		return sb.toString();
	}

	public static String collectionToSlashString(Collection<?> collection) {
		if (collection != null && collection.size() > 0) {
			StringBuilder sb = new StringBuilder();
			Object obj = null;
			for (Iterator<?> iterator = collection.iterator(); iterator
				 .hasNext();) {
				obj = iterator.next();
				sb.append(obj.toString());
				if (iterator.hasNext()) {
					sb.append("/");
				}
			}
			return sb.toString();
		} else {
			return "";
		}
	}

	public static String collectionToString(Collection<?> list, boolean number, String sep) {
		if (list == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		int len = list.size() - 1;
		int c = 0;
		if (!number) {
			for (Object obj : list) {
				sb.append(obj);
				if (c++ < len) {
					sb.append(sep);
				}
			}
		} else {
			int counter = 0;
			for (Object obj : list) {
				sb.append(++counter + ": ").append(obj);
				if (c++ < len) {
					sb.append(sep);
				}
			}
		}
		return sb.toString();
	}

	public static List<String> stringToList(final String s, String sep) {
		sep = sep.replaceAll(HtmlUtil.SPECIAL_CHAR_PATTERNSTR, "\\\\$1");
		final String[] split = s.split(sep);
		final ArrayList<String> l = new ArrayList<String>(split.length);
		for (String st : split) {
			l.add(st);
		}
		return l;
	}

	public static String[] stringToArray(String s, String sep) {
		sep = sep.replaceAll(HtmlUtil.SPECIAL_CHAR_PATTERNSTR, "\\\\$1");
		String[] split = s.split(sep);
		return split;
	}

	public static String arrayToString(final Object[] list, final boolean number, final String sep) {
		if (list == null || list.length == 0) {
			return "";
		}
		final StringBuilder sb = new StringBuilder();
		final int len = list.length;
		int c = 0;
		if (!number) {
			for (Object obj : list) {
				sb.append(obj);
				if (++c < len) {
					sb.append(sep);
				}
			}
		} else {
			for (Object obj : list) {
				sb.append(++c).append(": ").append(obj);
				if (c < len) {
					sb.append(sep);
				}
			}
		}
		return sb.toString();
	}

	public static StringBuilder mapToString(Map<?, ?> m) {
		StringBuilder sb = new StringBuilder();
		for (Entry<?, ?> entry : m.entrySet()) {
			sb.append(entry.getKey()).append(": ").append(entry.getValue())
				.append("\n");
		}
		Log.d("print", sb.toString());
		return sb;
	}

	public static String iteratorToString(Iterator<?> iter) {
		if (iter != null) {
			StringBuilder sb = new StringBuilder();
			while (iter.hasNext()) {
				sb.append(iter.next().toString());
			}
			return sb.toString();
		} else {
			return "";
		}
	}

	public static final List<String> getListFromBundle(ResourceBundle rb,
													   String prefix) {
		String name = null;
		List<String> ret = new LinkedList<String>();
		Enumeration<String> names = rb.getKeys();
		while (names.hasMoreElements()) {
			name = names.nextElement();
			if (name != null && name.startsWith(prefix)
				&& isInteger(name.substring(name.length() - 1))) {
				ret.add(rb.getString(name));
			}
		}
		Collections.sort(ret);
		return ret;
	}

	public static <T> List<T> collection2List(Collection<T> c) {
		ArrayList<T> l = new ArrayList<>(c.size());
		l.addAll(c);
		return l;
	}

	public static boolean isInteger(String substring) {
		try {
			Integer.parseInt(substring);
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}

	public static int toNumberWithDefault(String c, int def) {
		try {
			return Integer.parseInt(c);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	public static String toNumberWithDefault(CharSequence c, String def) {
		return (c == null || c.length() == 0) ? def : c + "";
	}

	public static boolean isNotNull(Object str) {
		return str != null && !"null".equals(str)
			&& str.toString().trim().length() > 0;
	}

	public static boolean isEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}

	public static String trim(String st) {
		return (st == null) ? "" : st.trim();
	}

	public static String replace(String fileContent, String[] froms, String[] tos, boolean isRegex, boolean caseSensitive) {
		try {
			Set<Entry<String, String>> set = new TreeSet<Entry<String, String>>();
			int length = Math.min(froms.length, tos.length);
			for (int i = 0; i < length; i++) {
				set.add(new ComparableEntry<String, String>(froms[i], tos[i]));

			}
			LinkedList<Entry<String, String>> stk = new LinkedList<Entry<String, String>>();
			for (Entry<String, String> e2 : set) {
				stk.push(e2);
			}
			Entry<String, String> e;
			while (stk.size() > 0) {
				e = stk.pop();
				fileContent = HtmlUtil.replaceRegexAll(fileContent, e.getKey(), e.getValue(), isRegex, caseSensitive);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return fileContent;
	}

	public static void gc() {
		new Thread(new Runnable() {
				@Override
				public void run() {
					System.gc();
				}
			}).start();
	}

	public static String getUrlStatus(String ss) {
		String st = "";
		try {
			st = URLDecoder.decode(ss.replaceAll("\\+", "-000000000000000-"), "utf-8").replaceAll("-000000000000000-", "+");
		} catch (UnsupportedEncodingException e) {
			Log.d("decode", ss, e);
		}
		return st.substring("file:".length(), st.length()).replaceAll("///", "/").replaceAll("//", "/");
	}

	public static void debug(String tag, String message, Object... args) {
        Log.d(tag, String.format(message, args));
    }

	public static String replaceAll(String s, String as[], String as1[]) {
		// long millis = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		for (int k = 0; k < as.length; k++) {
			if (as[k].length() > 0) {
				int i = 0;
				sb.setLength(0);
				int j;
				while ((j = s.indexOf(as[k], i)) >= 0) {
					sb.append(s, i, j);
					sb.append(as1[k]);
					// LOGGER.info("replaced " + as[k] + " = " + as1[k]);
					i = j + as[k].length();
				}
				sb.append(s, i, s.length());
				s = sb.toString();
			}
		}
		// LOGGER.info("replaced result: " + s);
		return s;
	}

	public static StringBuilder replaceAll(String s, String s1, String s2) {
		StringBuilder sb = new StringBuilder(s.length());
		if (s1.length() > 0) {
			int i = 0;
			int j;
			while ((j = s.indexOf(s1, i)) >= 0) {
				sb.append(s, i, j);
				sb.append(s2);
				i = j + s1.length();
			}
			sb.append(s, i, s.length());
			return sb;
		} else {
			return new StringBuilder(s);
		}
	}

	public static StringBuilder replaceAll(String s, StringBuilder sb,
										   String s1, String s2) {
		int s1Length = s1.length();
		if (s1Length > 0) {
			int i = 0;
			int j;
			while ((j = s.indexOf(s1, i)) >= 0) {
				sb.append(s, i, j);
				sb.append(s2);
				i = j + s1Length;
			}
			sb.append(s, i, s.length());
			return sb;
		} else {
			return sb.append(s);
		}
	}

	public static void replaceAll(CharSequence s, int start, int end,
								  StringBuilder sb2, String[] s10, String[] s20) {
		replaceAll(s.toString(), start, end, sb2, s10, s20);
	}

	public static void replaceAll(String s, int start, int end,
								  StringBuilder sb2, String[] s10, String[] s20) {
		s = s.substring(start, end);
		for (int k = 0; k < s10.length; k++) {
			String s1 = s10[k];
			String s2 = s20[k];
			int s1Length = s1.length();
			if (s1Length > 0) {
				int i = 0;
				int j;
				StringBuilder sb = new StringBuilder();
				end = s.length();
				while (((j = s.indexOf(s1, i)) >= 0) && j < end) {
					sb.append(s, i, j);
					sb.append(s2);
					i = j + s1Length;
				}
				sb.append(s, i, end);
				s = sb.toString();
			}
		}
		sb2.append(s);
	}

	public static StringBuilder replaceAll(String s, int start, int end,
										   StringBuilder sb, String s1, String s2) {
		int s1Length = s1.length();
		if (s1Length > 0) {
			int i = start;
			int j;
			while (((j = s.indexOf(s1, i)) >= 0) && j < end) {
				sb.append(s, i, j);
				sb.append(s2);
				i = j + s1Length;
			}
			sb.append(s, i, end);
			return sb;
		} else {
			return sb.append(s);
		}
	}

	public static StringBuilder replaceAll(StringBuilder s, int start, int end,
										   StringBuilder sb, String s1, String s2) {
		int s1Length = s1.length();
		if (s1Length > 0) {
			int i = start;
			int j;
			while (((j = s.indexOf(s1, i)) >= 0) && j < end) {
				sb.append(s, i, j);
				sb.append(s2);
				i = j + s1Length;
			}
			sb.append(s, i, end);
			return sb;
		} else {
			return sb.append(s);
		}
	}

	public static StringBuilder replaceAll(String sourceCase, String sourceLower, int start, int end,
										   StringBuilder sb, String pattern, String tagStart, String tagEnd) {
		Log.d("sourceCase, sourceLower, start, end", sourceCase.length() + ", " + sourceLower.length()
			  + "," + start + "," + end + pattern + "," + tagStart + "," + tagEnd);
		int patternLength = pattern.length();
		if (patternLength > 0) {
			int i = start;
			int j;
			while (((j = sourceLower.indexOf(pattern, i)) >= 0) && ((j + patternLength) <= end)) {
				sb.append(sourceCase, i, j);
				sb.append(tagStart);
				i = j + patternLength;
				sb.append(sourceCase, j, i);
				sb.append(tagEnd);
			}
			Log.d("sourceCase, i, end", sourceCase.length() + ", " + i + ", " + end);
			sb.append(sourceCase, i, end);
			return sb;
		} else {
			return sb.append(sourceCase);
		}
	}

	public static StringBuilder replaceAll(StringBuilder sourceCase, StringBuilder sourceLower, int start, int end,
										   StringBuilder sb, String pattern, String tagStart, String tagEnd) {
		Log.d("sourceCase, sourceLower, start, end", sourceCase.length() + ", " + sourceLower.length()
			  + "," + start + "," + end + pattern + "," + tagStart + "," + tagEnd);
		int patternLength = pattern.length();
		if (patternLength > 0) {
			int i = start;
			int j;
			while (((j = sourceLower.indexOf(pattern, i)) >= 0) && ((j + patternLength) <= end)) {
				sb.append(sourceCase, i, j);
				sb.append(tagStart);
				i = j + patternLength;
				sb.append(sourceCase, j, i);
				sb.append(tagEnd);
			}
			Log.d("sourceCase, i, end", sourceCase.length() + ", " + i + ", " + end);
			sb.append(sourceCase, i, end);
			return sb;
		} else {
			return sb.append(sourceCase);
		}
	}

	public static String replace1Char(String origin, String[] oldChar,
									  String[] newChar) {
		for (int i = 0; i < oldChar.length; i++) {
			// Util.LOGGER.info("origin: " + origin + ", oldChar[i]: " +
			// oldChar[i] + ", newChar[i]: " + newChar[i]);
			if (oldChar[i].equals(origin)) {
				// Util.LOGGER.info("Equal: " + origin + ", oldChar[i]: " +
				// oldChar[i] + ", newChar[i]: " + newChar[i]);
				return newChar[i];
			}
		}
		return origin;
	}

	public static String replace1Chars(String content, String[] oldChar,
									   String[] newChar) {
		int len = content.length();
		int i = 0;
		StringBuilder sb = new StringBuilder();
		while (i < len) {
			// System.gc();
			final int endIndex = i + 1;
			final String substring = content.substring(i, endIndex);
			boolean appended = false;
			for (int j = 0; j < oldChar.length; j++) {
				if (substring.equals(oldChar[j])) {
					sb.append(newChar[j]);
					appended = true;
					break;
				}
			}
			if (!appended) {
				sb.append(substring);
			}
			i++;
		}
		return sb.toString();
	}

	private static int parseHex(byte b) {
        if (b >= '0' && b <= '9') return (b - '0');
        if (b >= 'A' && b <= 'F') return (b - 'A' + 10);
        if (b >= 'a' && b <= 'f') return (b - 'a' + 10);

        throw new IllegalArgumentException("Invalid hex char '" + b + "'");
    }

	public static byte[] decode(byte[] url) throws IllegalArgumentException {
        if (url.length == 0) {
            return new byte[0];
        }

        // Create a new byte array with the same length to ensure capacity
        byte[] tempData = new byte[url.length];

        int tempCount = 0;
        for (int i = 0; i < url.length; i++) {
            byte b = url[i];
            if (b == '%') {
                if (url.length - i > 2) {
                    b = (byte) (parseHex(url[i + 1]) * 16
						+ parseHex(url[i + 2]));
                    i += 2;
                } else {
                    throw new IllegalArgumentException("Invalid format");
                }
            }
            tempData[tempCount++] = b;
        }
        byte[] retData = new byte[tempCount];
        System.arraycopy(tempData, 0, retData, 0, tempCount);
        return retData;
    }

	public static String decodeUrlToFS(String filename) {
		byte[] bArr = filename.getBytes();
		byte[] bDest = decode(bArr);
		try {
			return new String(bDest, "utf-8");
		} catch (UnsupportedEncodingException e) {
			return new String(bDest);
		}
	}

	private static final String FILE_ENCODING = "file.encoding";

	public static void restartAppAsUTF8IfNeeded() throws IOException {
		if (!System.getProperty(FILE_ENCODING).equalsIgnoreCase(HtmlUtil.UTF8)) {

			System.out.println("Starting without encoding UTF-8");

			String java = "\"" + System.getProperty("sun.boot.library.path")
				+ "\\java.exe\"";

			List<String> cmdList = new LinkedList<String>();

			cmdList.add(java);
			cmdList.add("-jar");
			cmdList.add("-Dfile.encoding=UTF-8");
			cmdList.add(System.getProperty("sun.java.command"));

			ProcessBuilder newProcess = new ProcessBuilder();
			newProcess.directory(null);
			List<String> command = newProcess.command();
			command.addAll(cmdList);
			newProcess.start();

			System.out.println(Util.collectionToString(command, false, " "));
			System.out.println("Restarted as encoding UTF-8");

			System.exit(0);
		} else {
			System.out.println("Already started with encoding UTF-8");
		}
	}

	public static String splitWords(String src) {
		return src.replaceAll("[0-9 \n\t\f~!@#$%^&*()_+{}|:\"<>?\\-=\\[\\]\\\\;',./`ÃÃÃÃÂ¢ÃÃÃÃÃÃÃÃÃÃÃÃÂ¢ÃÃÃÃÃÃÃÃÃÃÃÃÂ¢ÃÃÃÃÃÃÃÃÃÃÃÃÂ¢ÃÃÃÃÃÃÃÃ]+", "\n");
	}


}
