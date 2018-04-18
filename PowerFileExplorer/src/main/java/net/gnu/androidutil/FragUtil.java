//package net.gnu.androidutil;
//
//import java.io.*;
//import android.util.*;
//import android.app.*;
//import android.content.*;
//import android.os.*;
//
//public class FragUtil {
//
//	private static final String TAG = "FragUtil";
//	CompressTask compressTask;
//	private void compressing(final CompressFragment compressFrag) {
//		if (compressTask == null || compressTask.isCancelled() || compressTask.getStatus() == AsyncTask.Status.FINISHED) {
//			ForegroundService.ticker = "Compressing";
//			ForegroundService.title = "Touch to Open";
//			ForegroundService.text = "Compressing";
//			AndroidUtils.startService(compressFrag.getActivity(), TAG);
//			compressTask = new CompressTask(compressFrag);
//			compressTask.execute();
//			compressFrag.mBtnConfirm.setText("Cancel");
//		} else {
//			compressTask.cancel(true);
//			compressFrag.mBtnConfirm.setText("Compress");
//		}
//	}
//
//	static final String sevenZipSupport = ".*?\\.(7z|zip|bz2|gz|tar|wim|swm|xz|zipx|jar|xpi|odt|ods|docx|xlsx|epub)";
//	public void compress(final CompressFragment compressFrag) {
//		compressFrag.save();
//		if (!compressFrag.password.equals(compressFrag.password2)) {
//			AndroidUtils.showToast(compressFrag.getActivity(), "Comfirm password does not match");
//			return;
//		}
//		if (compressFrag.files.length() == 0) {
//			AndroidUtils.showToast(compressFrag.getActivity(), "Invalid \"files\"");
//			return;
//		}
//		if (compressFrag.saveTo.length() == 0) {
//			AndroidUtils.showToast(compressFrag.getActivity(), "Invalid folder name");
//			return;
//		}
//		if (!new File(compressFrag.saveTo).canWrite()) {
//			AndroidUtils.showToast(compressFrag.getActivity(), "Folder " + compressFrag.saveTo + " cannot be written");
//			return;
//		}
//		if (compressFrag.fName.length() == 0) {
//			AndroidUtils.showToast(compressFrag.getActivity(), "Invalid file name");
//			return;
//		}
//		String fN;
//		if (compressFrag.fName.matches(sevenZipSupport)) {
//			fN = compressFrag.saveTo + "/" + compressFrag.fName;
//		} else {
//			fN = compressFrag.saveTo + "/" + compressFrag.fName + "." + CompressFragment.types[compressFrag.typeSpinner.getSelectedItemPosition()];
//		}
//		final File file = new File(fN);
//		Log.d(TAG, "file.isDirectory() = " + file.getAbsolutePath() + ", " + file.isDirectory() + ", file.exists() = " + file.exists());
//		if (file.isDirectory()) {
//			AndroidUtils.showToast(compressFrag.getActivity(), "File name must be not a folder");
//			return;
//		}
//		if (file.exists()) {
//			AlertDialog.Builder alert = new AlertDialog.Builder(compressFrag.getActivity());
//			alert.setIconAttribute(android.R.attr.alertDialogIcon);
//			alert.setTitle("Overwrite?");
//			alert.setMessage("Do you really want to overwrite file \""+ file.getAbsolutePath() + "\"?");
//			alert.setCancelable(true);
//			alert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						boolean del = file.delete();
//						if (del) {
//							AndroidUtils.showToast(compressFrag.getActivity(), "Delete " + file + " successfully");
//						} else {
//							AndroidUtils.showToast(compressFrag.getActivity(), "Delete " + file + " unsuccessfully");
//						}
//						compressing(compressFrag);
//					}
//				});
//			alert.setPositiveButton("No", new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.cancel();
//					}
//				});
//			AlertDialog alertDialog = alert.create();
//			alertDialog.show();
//		} else {
//			compressing(compressFrag);
//		}
//	}
//
//	
//	
//}
