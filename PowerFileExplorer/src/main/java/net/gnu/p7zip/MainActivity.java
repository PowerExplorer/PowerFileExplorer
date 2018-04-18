//package net.gnu.p7zip;
//
//import android.view.View;
//import android.util.Log;
//import android.widget.Toast;
//import android.app.Activity;
//import java.io.FileOutputStream;
//import java.io.BufferedOutputStream;
//import java.io.ObjectOutputStream;
//import android.app.DialogFragment;
//import java.io.FileInputStream;
//import java.io.File;
//import java.io.BufferedInputStream;
//import java.io.IOException;
//import android.app.AlertDialog;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.os.Bundle;
//
//import android.app.Service;
//import android.os.AsyncTask;
//import android.widget.RadioButton;
//import net.gnu.util.FileUtil;
//import net.gnu.util.Util;
//import net.gnu.zpaq.Zpaq;
//import org.magiclen.magiccommand.Command;
//import java.util.List;
//import net.gnu.explorer.R;
//
//public class MainActivity extends Activity implements OnFragmentInteractionListener {
//
//	public static final String EXTRA_DIR_PATH = "org.openintents.extra.DIR_PATH";
//    //public static final String EXTRA_ABSOLUTE_PATH = "org.openintents.extra.ABSOLUTE_PATH";
//    public static final String EXTRA_FILTER_FILETYPE = "org.openintents.extra.FILTER_FILETYPE";
//    public static final String EXTRA_FILTER_MIMETYPE = "org.openintents.extra.FILTER_MIMETYPE";
//	
//    public static final String ACTION_PICK_FILE = "org.openintents.action.PICK_FILE";
//    public static final String ACTION_PICK_DIRECTORY = "org.openintents.action.PICK_DIRECTORY";
//    public static final String ACTION_MULTI_SELECT = "org.openintents.action.MULTI_SELECT";
//	
//	public static final String EXTRA_TITLE = "org.openintents.extra.TITLE";
//	public static final String EXTRA_MULTI_SELECT = "org.openintents.extra.MULTI_SELECT";//"multiFiles";
//	static final String ALL_SUFFIX = "*";
//	static final String ALL_SUFFIX_TITLE = "Select Files/Folders";
//	static final String ZIP_SUFFIX = ".zpaq; .7z; .bz2; .bzip2; .tbz2; .tbz; .001; .gz; .gzip; .tgz; .tar; .dump; .swm; .xz; .txz; .zip; .zipx; .jar; .apk; .xpi; .odt; .ods; .odp; .docx; .xlsx; .pptx; .epub; .apm; .ar; .a; .deb; .lib; .arj; .cab; .chm; .chw; .chi; .chq; .msi; .msp; .doc; .xls; .ppt; .cpio; .cramfs; .dmg; .ext; .ext2; .ext3; .ext4; .img; .fat; .hfs; .hfsx; .hxs; .hxi; .hxr; .hxq; .hxw; .lit; .ihex; .iso; .lzh; .lha; .lzma; .mbr; .mslz; .mub; .nsis; .ntfs; .rar; .r00; .rpm; .ppmd; .qcow; .qcow2; .qcow2c; .squashfs; .udf; .iso; .scap; .uefif; .vdi; .vhd; .vmdk; .wim; .esd; .xar; .pkg; .z; .taz";
//	static final String ZIP_TITLE = "Compressed file (" + ZIP_SUFFIX + ")";
//	static final int FILES_REQUEST_CODE = 13;
//	static final int SAVETO_REQUEST_CODE = 14;
//	static final boolean MULTI_FILES = true;
//	public static final String PREVIOUS_SELECTED_FILES = "net.gnu.explorer.selectedFiles";
//	
//	private static final String TAG = "MainActivity";
//	private static final String COMPRESS = CompressFragment.class.getSimpleName();
//	private static final String DECOMPRESS = DecompressFragment.class.getSimpleName();
//
//	CompressFragment compressFrag;
//	static CompressTask compressTask;
//	static DecompressTask decompressTask;
//	DecompressFragment decompressFrag;
//
//	String[] initDialog = new String[] {
//		COMPRESS,
//		DECOMPRESS,
//	};
//
//	@Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.p7zpaq);
//		Log.d(TAG, "onCreate " + savedInstanceState);
//		
//		for (String clazz : initDialog) {
//			if (COMPRESS.equals(clazz) && compressFrag == null) {
//				compressFrag = CompressFragment.newInstance();
//			} else if (DECOMPRESS.equals(clazz) && decompressFrag == null) {
//				decompressFrag = DecompressFragment.newInstance();
//			} 
//		}
//    }
//
//	@Override
//    protected void onPause() {
//        Log.d(TAG, "onPause");
//		super.onPause();
//		//saveAppStatus();
//	}
//
//	void showToast(CharSequence st) {
//		Toast.makeText(this, st, Toast.LENGTH_SHORT).show();
//	}
//
//	public void compress(View view) {
//		currentDialog = COMPRESS;
//		Log.d(TAG, "compress(View view) " + compressFrag);
//		compressFrag.show(getFragmentManager(), COMPRESS);
//		
//	}
//
//	public void decompress(View view) {
//		currentDialog = DECOMPRESS;
//		decompressFrag.show(getFragmentManager(), DECOMPRESS);
//	}
//
//	private void compress(CompressFragment compressFrag) {
//		ForegroundService.ticker = "Compressing";
//		ForegroundService.title = "Touch to Open";
//		ForegroundService.text = "Compressing";
//		startService(MainActivity.this, ForegroundService.class, ForegroundService.ACTION_FOREGROUND, TAG);
//		compressTask = new CompressTask(compressFrag);
//		compressTask.execute();
//		compressFrag.mBtnOK.setText("Cancel");
//
//	}
//
//	public static void startService(Activity activity, Class<? extends Service> service, String action, String tag) {
//		Log.i(tag, "Starting service");
//		Intent intent = new Intent(action);//ForegroundService.ACTION_FOREGROUND);//ACTION_BACKGROUND
//		intent.setClass(activity, service);
//		activity.startService(intent);
//	}
//
//	@Override
//	public void onHelp(DialogFragment fra) {
//		Log.i(TAG, "onHelp " + fra);
//		fra.dismiss();
//	}
//
//	@Override
//	public void onDefault(DialogFragment fra) {
//		Log.i(TAG, "onDefault ");
//		fra.dismiss();
//		showToast("Nothing to do");
//	}
//
//	@Override
//	public void onCancel(DialogFragment fra) {
//		Log.i(TAG, "onCancel " + fra);
//		fra.dismiss();
//	}
//
//	@Override
//	public void onOk(DialogFragment fra) {
//		Log.i(TAG, "onOk " + fra);
//		if (fra instanceof CompressFragment) {
//			compressFrag = (CompressFragment)fra;
//			
//			Log.d("COMPRESS_REQUEST_CODE.selectedFiles", compressFrag.files + ", " + compressFrag.saveTo);// + compressFrag.fName);
//			compressFrag.save();
//
//			if (compressFrag.files.length() == 0) {
//				showToast("Invalid \"files\"");
//				return;
//			}
//			if (compressFrag.saveTo.length() == 0) {
//				showToast("Invalid file name");
//				return;
//			}
//
//			String fN;
//			if (compressFrag.saveTo.matches(CompressTask.sevenZipSupport)) {
//				fN = compressFrag.saveTo;
//			} else {
//				fN = compressFrag.saveTo + "." + ((RadioButton)compressFrag.getView().findViewById(compressFrag.typeRadioGroup.getCheckedRadioButtonId() <= 0 ? R.id.sevenz: compressFrag.typeRadioGroup.getCheckedRadioButtonId())).getTag();// + "/" + compressFrag.fName
//			}
//			final File file = new File(fN);
//			Log.d(TAG, file.getAbsolutePath() + ", " + "file.isDirectory() = " + file.isDirectory() + ", file.exists() = " + file.exists());
//			if (file.isDirectory()) {
//				showToast("File name must be not a folder");
//				return;
//			} else {
//				File parentFile = file.getParentFile();
//				if (parentFile != null) {
//					if (!parentFile.exists() && !parentFile.mkdirs()) {
//						showToast(parentFile + " cannot be created");
//						return;
//					}
//					if (!parentFile.canWrite()) {
//						showToast("Folder " + parentFile + " cannot be written");
//						return;
//					}
//				} else {
//					showToast("Parent File is not existed");
//					return;
//				}
//			}
//			if (compressTask == null || compressTask.isCancelled() || compressTask.getStatus() == AsyncTask.Status.FINISHED) {
//				if (file.exists()) {
//					AlertDialog.Builder alert = new AlertDialog.Builder(this);
//					alert.setIconAttribute(android.R.attr.alertDialogIcon);
//					alert.setTitle("Overwrite?");
//					alert.setMessage("Do you really want to overwrite file \"" + file.getAbsolutePath() + "\"?");
//					alert.setCancelable(true);
//					alert.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(final DialogInterface dialog, final int which) {
//								if (file.delete()) {
//									showToast("Delete " + file + " successfully");
//								} else {
//									showToast("Delete " + file + " unsuccessfully");
//								}
//								compress(compressFrag);
//							}
//						});
//					alert.setPositiveButton("No", new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog, int which) {
//								compress(compressFrag);
//								dialog.cancel();
//							}
//						});
//					AlertDialog alertDialog = alert.create();
//					alertDialog.show();
//				} else {
//					compress(compressFrag);
//				}
//			} else {
//				compressTask.cancel(true);
//				compressFrag.mBtnOK.setText("Compress");
//			}
//			return;
//		} else if (fra instanceof DecompressFragment) {
//			decompressFrag = (DecompressFragment)fra;
//			
//			Log.d("DECOMP_REQUEST_CODE.selectedFiles", decompressFrag.files + ".");
//			decompressFrag.save();
//			if (decompressFrag.files.length() == 0) {
//				showToast("Invalid \"files\"");
//				return;
//			}
//			if (decompressFrag.saveTo.length() == 0) {
//				showToast("Invalid folder");
//				return;
//			}
//			File file = new File(decompressFrag.saveTo);
//			if (file.isFile()) {
//				showToast("Error: Destination folder is a file");
//				return;
//			}
//			file.mkdirs();
//			if (!file.exists()) {
//				showToast("Error: Destination folder cannnot be written");
//				return;
//			}
//			if (decompressTask == null || decompressTask.isCancelled() || decompressTask.getStatus() == AsyncTask.Status.FINISHED) {
//				ForegroundService.ticker = "Decompressing";
//				ForegroundService.title = "Touch to Open";
//				ForegroundService.text = "Decompressing";
//				startService(this, ForegroundService.class, ForegroundService.ACTION_FOREGROUND, TAG);
//				decompressTask = new DecompressTask(decompressFrag);
//				decompressTask.execute();
//				decompressFrag.mBtnOK.setText("Cancel");
//			} else {
//				decompressTask.cancel(true);
//				decompressFrag.mBtnOK.setText("Decompress");
//			}
//			return;
//		} 
//		fra.dismiss();
//	}
//
//	String currentDialog = "";
//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		Log.d(TAG, "onActivityResult: " + requestCode + ", " + data);
//		if (requestCode == FILES_REQUEST_CODE) {
//			
//			if (resultCode == Activity.RESULT_OK) {
//				List<String> stringExtra = data.getStringArrayListExtra(PREVIOUS_SELECTED_FILES);
//				if (COMPRESS.equals(currentDialog)) {
//
//					compressFrag.files = Util.collectionToString(stringExtra, false, "| ");
//					
//					compressFrag.show(getFragmentManager(), COMPRESS);
//					Log.d(TAG, "onActivityResult FILES_REQUEST_CODE Compress " + compressFrag);
//				} else if (DECOMPRESS.equals(currentDialog)) {
//
//					decompressFrag.files = Util.collectionToString(stringExtra, false, "| ");
//					
//					decompressFrag.show(getFragmentManager(), DECOMPRESS);
//					Log.d(TAG, "decompressFrag " + decompressFrag);
//				} 
//			} else { // RESULT_CANCEL
//				showToast("No file selected");
//				if (COMPRESS.equals(currentDialog)) {
//					compressFrag.show(getFragmentManager(), COMPRESS);
//				} else if (DECOMPRESS.equals(currentDialog)) {
//					decompressFrag.show(getFragmentManager(), DECOMPRESS);
//				} 
//			}
//
//		} else if (requestCode == SAVETO_REQUEST_CODE) {
//			
//			if (resultCode == Activity.RESULT_OK) {
//				List<String> stringExtra = data.getStringArrayListExtra(PREVIOUS_SELECTED_FILES);
//				if (COMPRESS.equals(currentDialog)) {
//
//					compressFrag.saveTo = stringExtra.get(0);
//					
//					compressFrag.show(getFragmentManager(), COMPRESS);
//					Log.d(TAG, "Compress " + compressFrag);
//				} else if (DECOMPRESS.equals(currentDialog)) {
//
//					decompressFrag.saveTo = stringExtra.get(0);
//					
//					decompressFrag.show(getFragmentManager(), DECOMPRESS);
//					Log.d(TAG, "decompressFrag " + decompressFrag);
//				} 
//			} else { // RESULT_CANCEL
//				showToast("No folder selected");
//				if (COMPRESS.equals(currentDialog)) {
//					compressFrag.show(getFragmentManager(), COMPRESS);
//				} else if (DECOMPRESS.equals(currentDialog)) {
//					decompressFrag.show(getFragmentManager(), DECOMPRESS);
//				} 
//			}
//		} 
//	}
//}
