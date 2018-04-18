///*
// * Copyright (C) 2009 The Android Open Source Project
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package net.gnu.p7zip;
//
//import android.app.Activity;
//import android.widget.TextView;
//import android.os.Bundle;
//import java.io.*;
//import net.gnu.util.*;
//import android.util.*;
//import org.magiclen.magiccommand.Command;
//import java.util.Arrays;
//import android.widget.EditText;
//
//public class HelloJni extends Activity {
//    /** Called when the activity is first created. */
//	EditText  tv;
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        /* Create a TextView and set its content.
//         * the text is retrieved by calling a native
//         * function.
//         */
//        tv = new EditText(this);
//		try {
////			InputStream is = getAssets().open("7za");
////			FileUtil.is2OS(is, new FileOutputStream("/data/data/com.free.p7zip/7za"));
////			System.out.println(CommandUtils.exec("chmod", "777", "/data/data/net.gnu.p7zip/7z"));
////			System.out.println(CommandUtils.exec("ls", "-l", "/data/data/net.gnu.p7zip"));
////			System.out.println(CommandUtils.exec("/data/data/net.gnu.p7zip/7z"));
//			Object[] run7za;// = new Andro7za().runListing7za(true, "l", "/storage/emulated/0/ftjj500.7z");
////			System.out.println(run7za[0]);
////			System.out.println(run7za[1]);
//			
//			//Log.d("HelloJni", "dir " + Util.collectionToString(Arrays.asList(getAssets().list(".")), true, "\n"));
//			
////			Command command = new Command("mkdir", "/data/data/net.gnu.p7zip/commands/x86");//"/android_asset/"
////			command.setCommandListener(new CommandListener(command));
////			command.run();
////
////			command = new Command("mkdir", "/data/data/net.gnu.p7zip/commands/armeabi-v7a");//"/android_asset/"
////			command.setCommandListener(new CommandListener(command));
////			command.run();
////			
////			FileUtil.is2File(getAssets().open("x86/7z"), "/data/data/net.gnu.p7zip/commands/x86/7z");///android_asset/
////			FileUtil.is2File(getAssets().open("armeabi-v7a/7z"), "/data/data/net.gnu.p7zip/commands/armeabi-v7a/7z");///android_asset/
////			
////			command = new Command("chmod", "777", "/data/data/net.gnu.p7zip/commands/x86/7z");
////			command.setCommandListener(new CommandListener(command));
////			command.run();
////
////			command = new Command("chmod", "777", "/data/data/net.gnu.p7zip/commands/armeabi-v7a/7z");
////			command.setCommandListener(new CommandListener(command));
////			command.run();
////
////			command = new Command("ls", "-lR", "/data/data/net.gnu.p7zip");//"/android_asset/"
////			command.setCommandListener(new CommandListener(command));
////			command.run();
////			
////			command = new Command("ls", "-lR", "/data/assets/net.gnu.p7zip");//"/android_asset/"
////			command.setCommandListener(new CommandListener(command));
////			command.run();
////
////			command = new Command("ls", "-lR", "/data/assets/net.gnu.p7zip-1");//"/android_asset/"
////			command.setCommandListener(new CommandListener(command));
////			command.run();
////
////			command = new Command("ls", "-lR", "/data/assets/net.gnu.p7zip-2");//"/android_asset/"
////			command.setCommandListener(new CommandListener(command));
////			command.run();
////
////			command = new Command("ls", "-lR", "/data/app/net.gnu.p7zip");//"/android_asset/"
////			command.setCommandListener(new CommandListener(command));
////			command.run();
////
////			command = new Command("ls", "-lR", "/data/app-lib/net.gnu.p7zip");//"/android_asset/"
////			command.setCommandListener(new CommandListener(command));
////			command.run();
////
////			command = new Command("ls", "-lR", "/data/app/net.gnu.p7zip-1");//"/android_asset/"
////			command.setCommandListener(new CommandListener(command));
////			command.run();
////
////			command = new Command("ls", "-lR", "/data/app-lib/net.gnu.p7zip-1");//"/android_asset/"
////			command.setCommandListener(new CommandListener(command));
////			command.run();
////
////			command = new Command("ls", "-lR", "/data/app/net.gnu.p7zip-2");//"/android_asset/"
////			command.setCommandListener(new CommandListener(command));
////			command.run();
////			
////			command = new Command("ls", "-lR", "/data/app-lib/net.gnu.p7zip-2");//"/android_asset/"
////			command.setCommandListener(new CommandListener(command));
////			command.run();
////
////			command = new Command("/data/data/net.gnu.p7zip/commands/x86/7z", "i");
////			command.setCommandListener(new CommandListener(command));
////			command.run();
//			///data/app/net.gnu.p7zip-2/lib/arm/lib7z.so
//			//Log.d("HelloJni", FileUtil.getFiles(new File("/data/app/net.gnu.p7zip-2/"), false) + ".");
//			//run7za = new Andro7za().run7za(true, "i");
//			//tv.setText(run7za[1].toString() + tv.getText());
//			//com.free.util.CommandUtils.exec("/data/data/com.free.p7zip/").toString();
//			setContentView(tv);
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//
//	}
//    static {
//        //System.loadLibrary("7z");
//    }
//	
//	class CommandListener implements org.magiclen.magiccommand.CommandListener {
//		private Command command;
//		CommandListener(Command cmd) {
//			command = cmd;
//		}
//		@Override
//		public void commandStart(final String id) {
//			//Log.d("HelloJni", command.getCommand() + ": " + id + ": commandStart");
//		}
//
//		@Override
//		public void commandRunning(final String id, final String message, final boolean isError) {
//			Log.d("HelloJni", message);
//			tv.setText(tv.getText() + command.getCommand() + ": " + message + "\n");
//			//System.out.println(message);
//		}
//
//		@Override
//		public void commandException(final String id, final Exception exception) {
//			Log.d("HelloJni", command.getCommand() + ": " + id + ": commandException");
//			tv.setText(tv.getText() + command.getCommand() + ": \n" + exception + "\n");
//			exception.printStackTrace(System.out);
//		}
//
//		@Override
//		public void commandEnd(final String id, final int returnValue) {
//			//Log.d("HelloJni", command.getCommand() + ": " + id + ": commandEnd");
//		}
//	}
//}
//
//
