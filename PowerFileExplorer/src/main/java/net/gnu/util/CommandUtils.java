package net.gnu.util;
import java.io.*;
import java.util.*;
import android.util.*;

public class CommandUtils {

	public static StringBuilder exec(String... cmd) {
		if (cmd == null || cmd.length == 0) {
			return new StringBuilder();
		}
		
		BufferedReader pout = null;
		PrintStream pin = null;
		try {
			ProcessBuilder builder = new ProcessBuilder(cmd);
			builder.redirectErrorStream(true);
//			pb.redirectInput(ProcessBuilder.Redirect.from(new File("in.txt")));
//			pb.redirectOutput(ProcessBuilder.Redirect.to(new File("out.txt")));
//			pb.redirectError(ProcessBuilder.Redirect.appendTo(new File("error.log")));

			Process p = builder.start();
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
			StringBuilder sb = new StringBuilder();
			int ch;
			while ((ch = pout.read()) != -1) {
				sb.append((char)ch);
				//System.out.print((char)ch);
			}
			//System.out.println(sb);
//			BufferedReader perr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//			while ((ch = perr.read()) != -1) {
//				System.out.print((char)ch);
//			}
			int exitValue = p.waitFor();
			System.out.println("Process Completed with exit value of " + exitValue);
			return sb;
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} finally {
			FileUtil.close(pin, pout);
		}
		return new StringBuilder();
	}
	
	public static Object[]/*<BufferedReader, PrintStream>*/ execInteract(String... cmd) {
		if (cmd == null || cmd.length == 0) {
			return new Object[]{null, null};//Entry<BufferedReader, PrintStream>(null, null);
		}
		BufferedReader pout = null;
		PrintStream pin = null;
		try {
			ProcessBuilder builder = new ProcessBuilder(cmd);
			builder.redirectErrorStream(true);
//			pb.redirectInput(ProcessBuilder.Redirect.from(new File("in.txt")));
//			pb.redirectOutput(ProcessBuilder.Redirect.to(new File("out.txt")));
//			pb.redirectError(ProcessBuilder.Redirect.appendTo(new File("error.log")));

			Process p = builder.start();
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
//			StringBuilder sb = new StringBuilder();
//			int ch;
//			while ((ch = pout.read()) != -1) {
//				sb.append((char)ch);
//				//System.out.print((char)ch);
//			}
			//System.out.println(sb);
//			BufferedReader perr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//			while ((ch = perr.read()) != -1) {
//				System.out.print((char)ch);
//			}
//			int exitValue = p.waitFor();
//			System.out.println("Process Completed with exit value of " + exitValue);
			return new Object[]{pout, pin};//new Entry<BufferedReader, PrintStream>(null, null);
		} catch (IOException ex) {
			ex.printStackTrace();
//		} catch (InterruptedException ex) {
//			ex.printStackTrace();
		} finally {
			FileUtil.close(pin, pout);
		}
		return null;
	}
	
	public static StringBuilder copyF(String pathSrc, String pathDest) {
		return exec("/system/bin/cp", "-rf", pathSrc, pathDest);
	}

	public static StringBuilder copyI(String pathSrc, String pathDest) {
		return exec("/system/bin/cp", "-ri", pathSrc, pathDest);
	}

	public static StringBuilder delete(String path) {
		return exec("/system/bin/rm", "-r", path);
	}

	public static StringBuilder fetch_cpu_info() {
		Log.i("fetch_cpu_info", "start....");
		return exec("/system/bin/cat", "/proc/cpuinfo");
	}

	public static StringBuilder fetch_netcfg_info() {
		Log.i("fetch_netcfg_info", "start....");
		return exec("/system/bin/netcfg");
	}

	public static StringBuilder fetch_netstat_info() {
		Log.i("fetch_netstat_info", "start....");
		return exec("/system/bin/netstat");
	}

	public static StringBuilder fetch_mount_info() {
		Log.i("fetch_mount_info", "start....");
		return exec("/system/bin/mount");
	}

	public static StringBuilder fetch_process_info() {
		Log.i("fetch_process_info", "start....");
		return exec("/system/bin/top", "-n", "1");
	}

	public static StringBuilder fetch_disk_info() {
		return exec("/system/bin/df");
	}
	
}
