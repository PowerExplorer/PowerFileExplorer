package net.gnu.p7zip;

import org.magiclen.magiccommand.Command;
import android.util.Log;
import android.os.AsyncTask;

public class CommandListener implements org.magiclen.magiccommand.CommandListener {
	
	private Command command;
	public int ret = -1;
	private UpdateProgress task;
	
	public CommandListener(Command cmd) {
		command = cmd;
	}
	
	public CommandListener(Command cmd, UpdateProgress task) {
		command = cmd;
		this.task = task;
	}
	
	@Override
	public void commandStart(final String id) {
		//Log.d("CommandListener", command.getCommand() + ": " + id + ": commandStart");
	}

	@Override
	public void commandRunning(final String id, final String message, final boolean isError) {
		if (task != null) {
			task.updateProgress(message);
		} else {
			Log.d("CommandListener", "isError " + isError + ": " + message);
		}
		//tv.setText(tv.getText() + command.getCommand() + ": " + message + "\n");
		//Log.d("CommandListener.commandRunning", message + ".");
	}

	@Override
	public void commandException(final String id, final Exception exception) {
		Log.d("CommandListener", command.getCommand() + ": " + id + ": commandException");
		if (task != null) {
			task.updateProgress(exception.toString());
		}
		//tv.setText(tv.getText() + command.getCommand() + ": \n" + exception + "\n");
		exception.printStackTrace(System.out);
	}

	@Override
	public void commandEnd(final String id, final int returnValue) {
		//Log.d("CommandListener", command.getCommand() + ": " + id + ": commandEnd");
		ret = returnValue;
	}
}
