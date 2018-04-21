package net.gnu.zpaq;
import java.io.*;
import android.util.*;
import android.os.AsyncTask;
import android.content.Context;
import net.gnu.util.Util;
import org.magiclen.magiccommand.Command;
import net.gnu.p7zip.CommandListener;
import net.gnu.util.FileUtil;
import java.util.Arrays;
import net.gnu.p7zip.UpdateProgress;
import java.util.List;
import net.gnu.p7zip.Andro7za;
import net.gnu.explorer.ExplorerApplication;

public class Zpaq
{
	private static String TAG = "ZPAQ";
	
	public static final String zpaq = ExplorerApplication.DATA_DIR + "commands/zpaq";
	public Command command;
	
	AsyncTask task;
	
	private String mOutfile = ExplorerApplication.PRIVATE_PATH + "/zpaqOut.txt";
	private String mInfile = ExplorerApplication.PRIVATE_PATH + "/zpaqIn.txt";
	private String listFile = ExplorerApplication.PRIVATE_PATH + "/zpaqFileList.txt";

	public native int runZpaq(String... args);
	public native String stringFromJNI(String outfile, String infile);
	public native void closeStreamJNI();
	
	static {
        //System.loadLibrary("zpaq");
    }
	
	public Zpaq() {
	}

	public Zpaq(String logPath) {
		mOutfile = logPath + "/zpaqOut.txt";
		mInfile = logPath + "/zpaqIn.txt";
		listFile = logPath + "/zpaqFileList.txt";
	}

	public Zpaq(Context ctx) {

		try {
			//Log.d("HelloJni", "dir " + Util.collectionToString(Arrays.asList(ctx.getAssets().list(".")), true, "\n"));

			Command command;

			if (!new File(zpaq).exists()) {
				command = new Command("mkdir", ExplorerApplication.DATA_DIR + "commands");//"/android_asset/"
				command.setCommandListener(new CommandListener(command));
				command.run();
				
				if (System.getProperty("os.arch").contains("x86")) {
					FileUtil.is2File(ctx.getAssets().open("x86/zpaq"), zpaq);
				} else {
					FileUtil.is2File(ctx.getAssets().open("armeabi-v7a/zpaq"), zpaq);
				}
				
				command = new Command("chmod", "777", zpaq);
				command.setCommandListener(new CommandListener(command));
				command.run();
				command = new Command(zpaq);
				command.setCommandListener(new CommandListener(command));
				command.run();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
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
	
	public Object[] runZpaq(boolean showDebug, String... args) throws IOException {
		try {
			initStream();

			if (args == null && args.length == 0) {
				return new Object[] {2, new StringBuilder()};
			}
			
			Log.d(TAG, "Call runZpaq(): " + args);
			
			int ret = runZpaq(args);
			Log.d(TAG, "runZpaq() ret " + ret);
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

	public int compress(
		String archiveName, 
		String password, 
		String level, 
		String files, 
		String excludes,
		final List<String> otherArgs, 
		UpdateProgress update) {

		Log.i(TAG, archiveName + "," +
			  level + "," +
			  files + "," +
			  excludes + ", " + 
			  otherArgs
			  );

		if (password != null && password.trim().length() > 0) {
			otherArgs.add(0, password);
			otherArgs.add(0, "-key");
		}

		if (excludes != null && excludes.trim().length() > 0) {
			otherArgs.add("-not");
			otherArgs.addAll(Arrays.asList(excludes.split("\\|+\\s*")));
		}

		otherArgs.add(0, level);
		otherArgs.addAll(0, Arrays.<String>asList(files.split("\\|+\\s*")));
		otherArgs.add(0, archiveName);
		otherArgs.add(0, "a");
		otherArgs.add(0, zpaq);

		//Log.d(TAG, Util.collectionToString(otherArgs, false, "\n"));
		command = new Command(otherArgs);
		Log.d(TAG, "command: " + command);
		CommandListener commandListener = new CommandListener(command, update);
		command.setCommandListener(commandListener);
		command.run();
		int ret = commandListener.ret;

		Log.d(TAG, "ret " + ret);

		return ret;
	}
	
	public int decompress(
		String archiveName, 
		String password, 
		String saveTo, 
		String mode, 
		String include, 
		String excludes,
		final List<String> otherArgs, 
		UpdateProgress update) {

		Log.i(TAG, archiveName + "," +
			  saveTo + "," +
			  excludes + ", " + 
			  otherArgs
			  );

		if (password != null && password.trim().length() > 0) {
			otherArgs.add(0, password);
			otherArgs.add(0, "-key");
		}
		
		if (include != null && include.trim().length() > 0) {
			otherArgs.add("-only");
			otherArgs.addAll(Arrays.asList(include.split("\\|+\\s+")));
		}
		
		if (excludes != null && excludes.trim().length() > 0) {
			otherArgs.add("-not");
			otherArgs.addAll(Arrays.asList(excludes.split("\\|+\\s+")));
		}
		
		otherArgs.add(0, saveTo);
		otherArgs.add(0, "-to");
		otherArgs.add(0, mode);
		otherArgs.add(0, archiveName);
		otherArgs.add(0, "x");
		otherArgs.add(0, zpaq);
		
		//Log.d(TAG, Util.collectionToString(otherArgs, false, "\n"));
		command = new Command(otherArgs);
		Log.d(TAG, "command: " + command);
		CommandListener commandListener = new CommandListener(command, update);
		command.setCommandListener(commandListener);
		command.run();
		int ret = commandListener.ret;

		Log.d(TAG, "ret " + ret);

		return ret;
	}
}
