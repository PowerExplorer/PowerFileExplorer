package net.gnu.p7zip;

import java.io.File;

import android.util.Log;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import net.gnu.util.*;
import android.content.Context;
import org.magiclen.magiccommand.Command;
import android.os.AsyncTask;
import net.gnu.explorer.ExplorerApplication;

/**
 * <code>Andro7za</code> provided the 7za JNI interface.
 * 
 */
public final class Andro7za {
	private static String TAG = "Andro7za";

	static {
		// Dynamically load stl_port, see jni/Application.mk
		// System.loadLibrary("stlport_shared");
		//System.loadLibrary("7z");
	}

	//	7z <command> [<switch>...] <base_archive_name> [<arguments>...]
	//	<arguments> ::= <switch> | <wildcard> | <filename> | <list_file>
	//	<switch>::= <switch_symbol><switch_characters>[<option>]
	//	<switch_symbol> ::= '/' | '-' 
	//	<list_file> ::= @{filename}
	/**
	 * command a x l
	 * archive 7z zip
	 * -t7z -tzip |  | 
	 * pPassword | -pPassword | -pPassword
	 * compression level | outputDir  |
	 * pathToCompress/fList | fileToExtract/fList |  
	 * 
	 * In compress use type. In extract use overwrite mode
	 *   -aoa	Overwrite All existing files without prompt.
	 *   -aos	Skip extracting of existing files.
	 *   -aou	aUto rename extracting file (for example, name.txt will be renamed to name_1.txt).
	 *   -aot	auto rename existing file (for example, name.txt will be renamed to name_1.txt).
	 *
	 Code Meaning
	 0 No error
	 1 Warning (Non fatal error(s)). For example,
	 one or more files were locked by some other
	 application, so they were not compressed.
	 2 Fatal error
	 7 Command line error
	 8 Not enough memory for operation
	 255 User stopped the process
	 */

	public native int a7zaCommandAll(String... command7z);

	public native String stringFromJNI(String outfile, String infile);
	public native void closeStreamJNI();

	//public static final String PRIVATE_PATH = "/sdcard/.net.gnu.explorer";
	private String mOutfile = ExplorerApplication.PRIVATE_PATH + "/7zaOut.txt";
	private String mInfile = ExplorerApplication.PRIVATE_PATH + "/7zaIn.txt";
	private String listFile = ExplorerApplication.PRIVATE_PATH + "/7zaFileList.txt";

	public Command command;
	public static final String DATA_DIR = "/data/data/net.gnu.explorer/";
	public static final String p7z_x86 = DATA_DIR + "commands/x86/7z";
	public static final String p7z_arm = DATA_DIR + "commands/armeabi-v7a/7z";

	AsyncTask task;

	public Andro7za() {
	}

	public Andro7za(Context ctx) {

		try {
			//Log.d(TAG, "dir " + Util.collectionToString(Arrays.asList(ctx.getAssets().list(".")), true, "\n"));

			Command command;

			if (!new File(p7z_x86).exists()) {
				command = new Command("mkdir", Andro7za.DATA_DIR + "commands");//"/android_asset/"
				command.setCommandListener(new CommandListener(command));
				command.run();
				command = new Command("mkdir", Andro7za.DATA_DIR + "commands/x86");//"/android_asset/"
				command.setCommandListener(new CommandListener(command));
				command.run();
				FileUtil.is2File(ctx.getAssets().open("x86/7z"), p7z_x86);///android_asset/
				command = new Command("chmod", "777", p7z_x86);
				command.setCommandListener(new CommandListener(command));
				command.run();
				command = new Command(p7z_x86, "i");
				command.setCommandListener(new CommandListener(command));
				command.run();
			}

			if (!new File(p7z_arm).exists()) {
				command = new Command("mkdir", Andro7za.DATA_DIR + "commands");//"/android_asset/"
				command.setCommandListener(new CommandListener(command));
				command.run();
				command = new Command("mkdir", Andro7za.DATA_DIR + "commands/armeabi-v7a");//"/android_asset/"
				command.setCommandListener(new CommandListener(command));
				command.run();
				FileUtil.is2File(ctx.getAssets().open("armeabi-v7a/7z"), p7z_arm);///android_asset/
				command = new Command("chmod", "777", Andro7za.DATA_DIR + "commands/armeabi-v7a/7z");
				command.setCommandListener(new CommandListener(command));
				command.run();
				command = new Command(p7z_arm, "i");
				command.setCommandListener(new CommandListener(command));
				command.run();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	public Andro7za(String logPath) {
		mOutfile = logPath + "/7zaOut.txt";
		mInfile = logPath + "/7zaIn.txt";
		listFile = logPath + "/7zaFileList.txt";
	}

	public void initStream() throws IOException {
		resetFile(mOutfile);
		resetFile(mInfile);
		resetFile(listFile);
		stringFromJNI(mOutfile, mInfile);
	}

	private void resetFile(String f) throws IOException {
		File file = new File(f);
		File parentFile = file.getParentFile();
		if (!parentFile.exists()) {
			parentFile.mkdirs();
		} else {
			file.delete();
		}
		file.createNewFile();
	}

	public Object[] run7za(boolean showDebug, String... args) throws IOException {
		try {
			initStream();

			Log.d(TAG, "Call run7za(): " + args[0]);
			int ret = a7zaCommandAll(args);
			Log.d(TAG, "run7za() ret " + ret);
			FileReader fileReader = new FileReader(mOutfile);
			BufferedReader br = new BufferedReader(fileReader, 32768);
			StringBuilder sb = new StringBuilder();
			if (!showDebug) {
				while (br.ready()) {
					sb.append(br.readLine()).append("\n");
				}
			} else {
				String readLine;
				while (br.ready()) {
					readLine = br.readLine();
					Log.d(TAG, readLine);
					sb.append(readLine).append("\n");
				}
			}
			return new Object[] {ret, sb};
		} finally {
			closeStreamJNI();
		}
	}

	public Object[] runListing7za(boolean showDebug, String... args) throws IOException {
		Object[] run7za = run7za(showDebug, args);
		String stRet = run7za[1].toString();
		Collection<String> nameList = new HashSet<String>();
		String line ="";
		BufferedReader br = new BufferedReader(new StringReader(stRet));
		int count = 0;
		//System.out.println(ENTRY_PATTERN);
		while (count < 2 && line != null) {
			line = br.readLine();
			if (line == null) {
				break;
			}
			//System.out.println(line);
			if ("------------------- ----- ------------ ------------  ------------------------".equals(line)) {
				count++;
			}
			if (count == 1) {
				Matcher matcher = ENTRY_PATTERN.matcher(line);
				//System.out.println(line);
				if (matcher.matches()) {
					if ("D".equals(matcher.group(1))) {
						nameList.add(matcher.group(2) + "/");
					} else {
						nameList.add(matcher.group(2));
					}
				}
			}
		}
		br.close();
		//Collections.sort(nameList);
		//Log.i("nameList", collectionToString(nameList, true, "\n"));
		return new Object[] {run7za[0], nameList};
	}

	final private static StringBuilder sb = new StringBuilder();
	public String read() throws IOException {
		synchronized (sb) {
			sb.setLength(0);
			BufferedReader in = null;
			try {
				//initStream(); // not check
				in = new BufferedReader(new FileReader(mOutfile));
				while (in.ready()) {
					String readLine = in.readLine();
					if (readLine == null || "".equals(readLine)) {
						in.close();
						closeStreamJNI();
						break;
					} else {
						sb.append(readLine);
					}
				}
				return sb.toString();
			} finally {
				FileUtil.close(in);
			}
		}
	}

	public void write(String content) {
		BufferedWriter out = null;
		try {
			//initStream(); // not check
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mInfile)));
			out.write(content.toCharArray(), 0, content.toCharArray().length);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtil.flushClose(out);
		}
	}

	public int compress(String pathArchive7z, String type, String password, String compressLevel, String pathToCompress) {
		if (type == null || type.trim().length() == 0) {
			type = "";
		} else {
			type = "-t" + type;
		}
		if (password == null || password.trim().length() == 0) {
			password = "";
		} else {
			password = "-p\"" + password + "\"";
		}
		if (compressLevel == null) {
			compressLevel = "";
		}
		try {
			initStream();
			Log.d(TAG, "Call a7zaCommand(), compress: a " + pathArchive7z + " " + type + " " + password + " " + compressLevel + " " + pathToCompress);
			int ret = a7zaCommandAll("a", type, password, compressLevel, pathArchive7z, pathToCompress);
			Log.d(TAG, "a7zaCommand() compress ret " + ret + ", file: " + pathArchive7z);
			return ret;
		} catch (IOException e) {
			return 2;
		} finally {
			closeStreamJNI();
		}
	}

	public int compress(String pathArchive7z, String type, String password, String compressLevel, Collection<String> fileList) throws IOException {
		if (type == null || type.trim().length() == 0) {
			type = "";
		} else {
			type = "-t" + type;
		}
		if (password == null || password.trim().length() == 0) {
			password = "";
		} else {
			password = "-p\"" + password + "\"";
		}
		if (compressLevel == null) {
			compressLevel = "";
		}
		if (fileList.size() > 0) {
			String outputList = Util.collectionToString(fileList, false, "\n");
			FileUtil.stringToFile(listFile, outputList);

			Log.d(TAG, "Call a7zaCommand(), compress: \"" + fileList + " to " + pathArchive7z + "\" level " + compressLevel);
			int ret = a7zaCommandAll("a", type, password, compressLevel, pathArchive7z, "@" + listFile, "");
			Log.d(TAG, "a7zaCommand() compress ret " + ret + ", file: " + pathArchive7z);
			new File(listFile).delete();
			return ret;
		} else {
			return -1;
		}
	}

	public int compress(
		String archiveName, 
		String password, 
		String level, 
		String volume, 
		List<String> fileList, 
		String excludes,
		final List<String> otherArgs, 
		UpdateProgress update) 
	throws IOException {

		Log.d(TAG, "compress " + archiveName + "," +
			  level + "," +
			  fileList + "," +
			  volume + "," +
			  excludes + ", " + 
			  otherArgs
			  );

		String fileListTmp = archiveName + ".tmp";
		String fileListTmp2 = "";
		String excludesTmp = "";
		String excludesTmp2 = "";
		if (archiveName.endsWith(".7z") || archiveName.endsWith(".zip") || archiveName.endsWith(".tar")) {
			FileWriter fileWriter = new FileWriter(fileListTmp);
			BufferedWriter bw = new BufferedWriter(fileWriter);
			try {
				for (String st : fileList) {
					bw.write(st);
					bw.newLine();
				}
			} finally {
				FileUtil.flushClose(bw);
			}
			fileListTmp2 = "@" + fileListTmp;

			if (excludes != null && excludes.trim().length() > 0) {
				excludesTmp = archiveName + ".exc." + System.currentTimeMillis();
				bw = new BufferedWriter(new FileWriter(excludesTmp));
				try {
					List<String> l = Util.stringToList(excludes, "[\r\n]+");
					for (String st : l) {
						bw.write(st);
						bw.newLine();
					}
				} finally {
					FileUtil.flushClose(bw);
				}
				excludesTmp2 = " -xr@" + excludesTmp;
			}
		} else {
			fileListTmp2 = archiveName.substring(0, archiveName.lastIndexOf(".")) + ".tar";
			List<String> args = new ArrayList<String>(otherArgs);
			compress(fileListTmp2, password, level, volume, fileList, excludes, args, update);
			excludesTmp2 = "";
		}

		if (password != null && password.length() > 0) {
			otherArgs.add(0, "-p" + password);
		}
		if (volume != null && volume.trim().length() > 0) {
			otherArgs.add(0, "-v" + volume);
		}

		if (otherArgs.indexOf("-bb") < 0 && otherArgs.indexOf("-bb0") < 0 && otherArgs.indexOf("-bb1") < 0 
			&& otherArgs.indexOf("-bb2") < 0 && otherArgs.indexOf("-bb3") < 0) {
			otherArgs.add(0, "-bb");
		}
		otherArgs.add(0, level);
		otherArgs.add(0, "a");
		if (true) {
			otherArgs.add(0, p7z_arm);
		} else {
			otherArgs.add(0, p7z_x86);
		}
		otherArgs.add(archiveName);
		otherArgs.add(fileListTmp2);
		otherArgs.add(excludesTmp2);

		command = new Command(otherArgs);
		Log.d(TAG, "command: " + command);
		CommandListener commandListener = new CommandListener(command, update);
		command.setCommandListener(commandListener);
		command.run();
		int ret = commandListener.ret;

		Log.d(TAG, "ret " + ret);

		new File(fileListTmp).delete();
		new File(excludesTmp).delete();
		if (!fileListTmp2.startsWith("@")) {
			new File(fileListTmp2).delete();
		}
		return ret;
	}

	// extract all
	public int extract(String pathArchive7z, String overwriteMode, String password, String pathToExtract) {

		if (Util.isEmpty(overwriteMode)) {
			overwriteMode = "-aos";
		}

		if (password == null || password.trim().length() == 0) {
			password = "";
		} else {
			password = "-p\"" + password + "\"";
		}

		File f = new File(pathToExtract);
		if (!f.exists()) {
			f.mkdirs();
		}
		pathToExtract = "-o\"" + pathToExtract + "\"";

		Log.d(TAG, "Call a7zaCommand(), extracting: \"" + pathArchive7z + "\" to " + pathToExtract);
		int ret = a7zaCommandAll("x", pathArchive7z, overwriteMode, password, pathToExtract);
		Log.d(TAG, "a7zaCommand() extracting ret " + ret + ", file: " + pathArchive7z);
		return ret;
	}

	// extract nhieu file
	public int extract(String pathArchive7z, String overwriteMode, String password, String pathToExtract, Collection<String> fileList) throws IOException {
		if (fileList.size() > 0) {
			if (Util.isEmpty(overwriteMode)) {
				overwriteMode = "-aos";
			}

			if (password == null || password.trim().length() == 0) {
				password = "";
			} else {
				password = "-p\"" + password + "\"";
			}

			File f = new File(pathToExtract);
			if (!f.exists()) {
				f.mkdirs();
			}
			pathToExtract = "-o\"" + pathToExtract + "\"";

			String outputList = Util.collectionToString(fileList, false, "\n");
			File outListF = new File(listFile);
			outListF.delete();
			FileUtil.stringToFile(listFile, outputList);

			Log.d(TAG, "Call a7zaCommand(), extract: \"" + fileList + " in " + pathArchive7z + "\" to " + pathToExtract);
			int ret = a7zaCommandAll("x", pathArchive7z, overwriteMode, password, pathToExtract, "@" + listFile);
			Log.d(TAG, "a7zaCommand() extract ret " + ret + ", file: " + pathArchive7z);
			return ret;
		} else {
			return -1;
		}
	}

	// extract 1 file
	public int extract(String pathArchive7z, String overwriteMode, String password, String pathToExtract, String fileName) {
		if (Util.isEmpty(overwriteMode)) {
			overwriteMode = "-aos";
		}

		if (password == null || password.length() == 0) {
			password = "";
		} else {
			password = "-p\"" + password + "\"";
		}

		File f = new File(pathToExtract);
		if (!f.exists()) {
			f.mkdirs();
		}
		pathToExtract = "-o\"" + pathToExtract + "\"";

		Log.d(TAG, "Call a7zaCommand(), extract: \"" + fileName + " in " + pathArchive7z + "\" to " + pathToExtract);
		int ret = a7zaCommandAll("x", pathArchive7z, overwriteMode, password, pathToExtract, fileName, "");
		Log.d(TAG, "a7zaCommand() extract ret " + ret + ", file: " + pathArchive7z);
		return ret;
	}

	// extract with include and exclude
	public int extractInEx(
		String cmd, 
		String zArchive, 
		String password, 
		String overwriteMode, 
		String pathToExtract, 
		String includes, 
		String excludes,
		List<String> otherArgs, 
		UpdateProgress update
	)  throws IOException {

		Log.i(TAG, 
			  cmd + ", " +
			  zArchive + "," +
			  overwriteMode + "," +
			  pathToExtract + "," +
			  includes + "," +
			  excludes + ", " +
			  otherArgs);

		if (password == null || password.length() == 0) {
			password = "";
		} else {
			password = "-p" + password;
			otherArgs.add(0, password);
		}

		if (Util.isEmpty(overwriteMode)) {
			overwriteMode = " -aos ";
		}
		otherArgs.add(0, overwriteMode);

		File f = new File(pathToExtract);
		if (!f.exists()) {
			if (!f.mkdirs()) {
				throw new IOException("Can't create " + f.getAbsolutePath());
			}
		}
		pathToExtract = "-o" + pathToExtract;
		otherArgs.add(pathToExtract);

		String includesTmp = "";
		if (includes != null && includes.trim().length() > 0) {
			includesTmp = zArchive + ".inc." + System.currentTimeMillis();
			BufferedWriter bw = new BufferedWriter(new FileWriter(includesTmp));
			try {
				List<String> l = Util.stringToList(includes, "[\r\n]+");
				for (String st : l) {
					bw.write(st);
					bw.newLine();
				}
			} finally {
				bw.flush();
				bw.close();
			}
		}
		String includesTmp2 = Util.isEmpty(includes) ? "" :  " -xr@" + includesTmp + " ";
		otherArgs.add(includesTmp2);

		String excludesTmp = "";
		if (excludes != null && excludes.trim().length() > 0) {
			excludesTmp = zArchive + ".exc." + System.currentTimeMillis();BufferedWriter bw = new BufferedWriter(new FileWriter(excludesTmp));
			try {
				List<String> l = Util.stringToList(excludes, "[\r\n]+");
				for (String st : l) {
					bw.write(st);
					bw.newLine();
				}
			} finally {
				bw.flush();
				bw.close();
			}
		}
		String excludesTmp2 = Util.isEmpty(excludes) ? "" :  " -xr@" + excludesTmp + " ";
		otherArgs.add(excludesTmp2);

		int ret = 0;
		Log.d(TAG, "overwrite mode" + overwriteMode);
		otherArgs.add("-bb");
		otherArgs.add(0, zArchive);
		otherArgs.add(0, cmd);
		if (true) {
			otherArgs.add(0, p7z_arm);
		} else {
			otherArgs.add(0, p7z_x86);
		}
		command = new Command(otherArgs);//p7z + " " + cmd + " " + zArchive + " " + " -bb " + " " + password + " " + overwriteMode + " " +  pathToExtract + " " + otherArgs + includesTmp2 + excludesTmp2);
		Log.d(TAG, "command: " + command);
		CommandListener commandListener = new CommandListener(command, update);
		command.setCommandListener(commandListener);
		command.run();
		ret = commandListener.ret;

		Log.d(TAG, "ret " + ret + ", command: " + command);

		Log.d(TAG, "a7zaCommand() compress ret " + ret + ", file: " + zArchive);
		new File(includesTmp).delete();
		new File(excludesTmp).delete();
		return ret;
	}

	// ------------------- ----- ------------ ------------  ------------------------
	// 2016-03-13 22:49:48 ....A         4212               jni/CPP/myWindows/makefile.depend
	// 2016-03-13 23:44:49 D....            0            0  gen/com/hostzi
	// 2015-08-02 17:49:11 .R..A         9405               CPP/Windows/Window.h
	//                     .....                            p7zip_15.14_src_all.tar
	private final static Pattern ENTRY_PATTERN = Pattern.compile("[ \\d]{4}[-/ ][ \\d]{2}[- /][ \\d]{2} [ \\d]{2}[ :][ \\d]{2}[ :][ \\d]{2} ([D\\.]).{3}[A\\.] [ \\d]{12}[ \\d]{15}([^\r\n]+)", Pattern.UNICODE_CASE);

	public Collection<String> listing(File archive7z, String password) throws IOException {
		return listing(archive7z.getAbsolutePath(), password);
	}

	public Collection<String> listing(String pathArchive7z, String password) throws IOException {
		try {
			initStream();
			if (password == null || password.trim().length() == 0) {
				password = "";
			} else {
				password = "-p\"" + password + "\"";
			}
			Log.d(TAG, "Call a7zaCommand(), listing: " + pathArchive7z);
			int ret = a7zaCommandAll("l", pathArchive7z, "", password, "", "", "");
			Log.d(TAG, "a7zaCommand() listing ret " + ret + ", file: " + pathArchive7z);

			Collection<String> nameList = new HashSet<String>();
			String line ="";
			FileReader fileReader = new FileReader(mOutfile);
			BufferedReader br = new BufferedReader(fileReader, 32768);
			int count = 0;
			while (br.ready() && count < 2) {
				line = br.readLine();
				//System.out.println(line);
				if ("------------------- ----- ------------ ------------  ------------------------".equals(line)) {
					count++;
				}
				if (count == 1) {
					Matcher matcher = ENTRY_PATTERN.matcher(line);
					//				System.out.println(line);
					//				System.out.println(ENTRY_PATTERN);
					if (matcher.matches()) {
						if ("D".equals(matcher.group(1))) {
							nameList.add(matcher.group(2) + "/");
						} else {
							nameList.add(matcher.group(2));
						}
					}
				}
			}
			br.close();
			fileReader.close();
			//Collections.sort(nameList);
			//			Log.i("nameList", collectionToString(nameList, true, "\n"));
			return nameList;
		} finally {
			closeStreamJNI();
		}
	}
}
