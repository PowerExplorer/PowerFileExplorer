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

public class Zpaq
{
	private static String TAG = "Zpaq";
	public static final String zpaqx86 = "/data/data/net.gnu.explorer/commands/x86/zpaq";
	public static final String zpaqarm = "/data/data/net.gnu.explorer/commands/armeabi-v7a/zpaq";
	public Command command;
	
	AsyncTask task;
	
	public native int runZpaq(String... args);
	public native String stringFromJNI(String outfile, String infile);
	public native void closeStreamJNI();
	
	static {
        //System.loadLibrary("zpaq");
    }
	
	public static final String PRIVATE_PATH = "/sdcard/.net.gnu.explorer";
	private String mOutfile = PRIVATE_PATH + "/zpaqOut.txt";
	private String mInfile = PRIVATE_PATH + "/zpaqIn.txt";
	private String listFile = PRIVATE_PATH + "/zpaqFileList.txt";

	public Zpaq() {
		String sPath = PRIVATE_PATH;
		mOutfile = sPath + "/zpaqOut.txt";
		mInfile = sPath + "/zpaqIn.txt";
		listFile = sPath + "/zpaqFileList.txt";
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

			if (!new File(zpaqx86).exists()) {
				command = new Command("mkdir", "/data/data/net.gnu.explorer/commands");//"/android_asset/"
				command.setCommandListener(new CommandListener(command));
				command.run();
				command = new Command("mkdir", "/data/data/net.gnu.explorer/commands/x86");//"/android_asset/"
				command.setCommandListener(new CommandListener(command));
				command.run();
				FileUtil.is2File(ctx.getAssets().open("x86/zpaq"), zpaqx86);///android_asset/
				
				command = new Command("chmod", "777", zpaqx86);
				command.setCommandListener(new CommandListener(command));
				command.run();
				command = new Command(zpaqx86);
				command.setCommandListener(new CommandListener(command));
				command.run();
			}

			if (!new File(zpaqarm).exists()) {
				command = new Command("mkdir", "/data/data/net.gnu.explorer/commands");
				command.setCommandListener(new CommandListener(command));
				command.run();
				command = new Command("mkdir", "/data/data/net.gnu.explorer/commands/armeabi-v7a");
				command.setCommandListener(new CommandListener(command));
				command.run();
				FileUtil.is2File(ctx.getAssets().open("armeabi-v7a/zpaq"), zpaqarm);
				command = new Command("chmod", "777", zpaqarm);
				command.setCommandListener(new CommandListener(command));
				command.run();
				command = new Command(zpaqarm);
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
		otherArgs.addAll(0, Arrays.asList(files.split("\\|+\\s*")));
		otherArgs.add(0, archiveName);
		otherArgs.add(0, "a");
		otherArgs.add(0, zpaqx86);

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
		otherArgs.add(0, zpaqx86);
		
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
