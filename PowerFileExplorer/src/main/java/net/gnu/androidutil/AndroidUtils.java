package net.gnu.androidutil;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.pm.PackageManager.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.graphics.pdf.*;
import android.net.*;
import android.net.wifi.*;
import android.os.*;
import android.preference.*;
import android.print.*;
import android.print.pdf.*;
import android.provider.*;
import android.support.annotation.*;
import android.support.v7.view.menu.*;
import android.telephony.*;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import net.gnu.explorer.*;
import net.gnu.util.*;
import java.io.*;
import java.util.*;
import android.app.usage.*;
import android.view.View.*;
import java.security.cert.*;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import javax.crypto.Cipher;
import java.security.InvalidAlgorithmParameterException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.lang.reflect.Method;
import android.os.Process;
import com.amaze.filemanager.ui.icons.MimeTypes;
import android.media.MediaScannerConnection;
import javax.crypto.KeyGenerator;
import java.security.SecureRandom;
import javax.crypto.SecretKey;
import net.gnu.common.*;
import android.content.Intent.*;


public class AndroidUtils {

	private static String TAG = "AndroidUtils";
	public static final String PREFS_DEFAULTPICKFILEPATH = "defaultpickfilepath";

//    public static Point getDisplaySize(final Context context) {
//        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        final Display display = windowManager.getDefaultDisplay();
//        final Point point = new Point(getMeasuredWidth(), this.getMeasuredHeight());
//		//Log.d(TAG, display.getWidth() + ":" + display.getHeight() + ", " + point.x + ":" + point.y);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            display.getSize(point);
//        } else {
//            point.set(display.getWidth(), display.getHeight());
//        }
//		//displayProps = point;
//        return point;
//    }


	public static void startService(final Activity activity, final Class<? extends Service> service, final String action, final String tag) {
		Log.i(tag, "Starting service");
		final Intent intent = new Intent(action);//ForegroundService.ACTION_FOREGROUND);//ACTION_BACKGROUND
		intent.setClass(activity, service);
		activity.startService(intent);
	}

	public static boolean createShortCut(final Context context, final String packageName) {
		final PackageManager pm = context.getPackageManager();
		final Intent iMain = new Intent(Intent.ACTION_MAIN);
		iMain.addCategory(Intent.CATEGORY_LAUNCHER);
		final List<ResolveInfo> res = pm.queryIntentActivities(iMain, 0);
		ActivityInfo ai = null;
		ActivityInfo activityInfo;
		for (ResolveInfo ri : res) {
			activityInfo = ri.activityInfo;
			//System.out.println("the application name is: " + activityInfo.loadLabel(pm) + ", " + activityInfo.packageName + ", " + activityInfo.name);
			if (packageName.equalsIgnoreCase(activityInfo.packageName)) {
				ai = activityInfo;
				break;
			}
		}
		if (ai != null) {
			final Intent shortcutIntent = new Intent();
			shortcutIntent.setClassName(ai.packageName, ai.name);
			shortcutIntent.setAction(Intent.ACTION_MAIN);
			shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			final Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
			intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
			intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, ai.loadLabel(pm) + "");
			intent.putExtra("duplicate", false);

			final Drawable d = ai.loadIcon(pm);
			System.out.println("d " + d);
			if (d != null) {
				final int size = (int) context.getResources().getDimension(android.R.dimen.app_icon_size);
				final Bitmap icon = BitmapUtil.resizeKeepScale(BitmapUtil.drawableToBitmap(d), size);
				intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
			} else {
				final Intent.ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(context.getApplicationContext(), R.drawable.ic_launcher);
				intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
			}
			context.sendBroadcast(intent);
			//Log.d(TAG, "in the shortcutapp on create method completed");
			return true;
		} else {
			//Log.d(TAG, "appllicaton not found");
			return false;
		}
	}

	public static void createShortCut(final Context ctx, Class<? extends Activity> activity, String name, int resIcon){
		//<uses-permission android:name="com.android.launcher.permission.INSTALL_SHORTCUT" />
    	//<uses-permission android:name="com.android.launcher.permission.UNINSTALL_SHORTCUT" />
		Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		shortcutintent.putExtra("duplicate", false);
		shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
		Parcelable icon = Intent.ShortcutIconResource.fromContext(ctx.getApplicationContext(), resIcon);
		shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
		Intent intent = new Intent(ctx.getApplicationContext(), activity);
		intent.setAction(Intent.ACTION_MAIN);
		shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		ctx.sendBroadcast(shortcutintent);
	}

	public static void addShortcut(final Context context, final File f) {
        //Adding shortcut for MainActivity on Home screen
		Log.d(TAG, "addShortcut " + f.getAbsolutePath());
		final String absolutePath = f.getAbsolutePath();

		final Intent addIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, f.getName());
		addIntent.putExtra("duplicate", false);

		final Intent shortcutIntent;
		if (f.isFile()) {
			if (MimeTypes.getMimeType(f).startsWith("image")) {
				final int size = (int) context.getResources().getDimension(android.R.dimen.app_icon_size);
				addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapUtil.resizeKeepScale(BitmapFactory.decodeFile(absolutePath), size));
				shortcutIntent = new Intent(context.getApplicationContext(),
											PhotoActivity.class);
			} else {
				addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
								   Intent.ShortcutIconResource.fromContext(context, ImageThreadLoader.getResId(f)));//R.mipmap.ic_launcher));
				shortcutIntent = new Intent(context.getApplicationContext(),
											context.getClass());
			}
		} else {
			addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
							   Intent.ShortcutIconResource.fromContext(context, R.drawable.myfolder72));
			shortcutIntent = new Intent(context.getApplicationContext(),
										context.getClass());
        }

        shortcutIntent.setData(Uri.fromFile(f));
        shortcutIntent.putExtra(Constants.EXTRA_ABSOLUTE_PATH, absolutePath);
        shortcutIntent.setAction(Intent.ACTION_MAIN);
        shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		if (f.isDirectory()) {
			shortcutIntent.putExtra(Constants.EXTRA_MULTI_SELECT, true);
			shortcutIntent.putExtra(Constants.EXTRA_FILTER_FILETYPE, "*");
		}

        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        context.sendBroadcast(addIntent);
    }

	

	public static void scanMedia(final Context ctx, final String root, final boolean includeSubFolder) {
		if (Build.VERSION.SDK_INT >= 19) {
			final Collection<File> fs = FileUtil.getFiles(root, includeSubFolder);
//			final int size = fs.size();
//			final String[] paths = new String[size];
//			final String[] mimetypes = new String[size];
//			int i = 0;
			String mimeType;
			ContentValues values;
			final ArrayList<ContentValues> arr = new ArrayList<>(fs.size());
			for (File f : fs) {
				mimeType = MimeTypes.getMimeType(f);
				if (mimeType.startsWith("image") || mimeType.startsWith("video")) {
					values = new ContentValues();
					values.put(MediaStore.Images.Media.DATA, f.getAbsolutePath());
					values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
					arr.add(values);
				}
//				paths[i] = f.getAbsolutePath();
//				mimetypes[i] = mimeType;
//				Log.d(TAG, "scanMedia " + mimetypes[i] + paths[i]);
//				i++;
			}
			final ContentValues[] contentValuesArr = new ContentValues[arr.size()];
			arr.toArray(contentValuesArr);
			ctx.getContentResolver().bulkInsert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValuesArr);
//			MediaScannerConnection.scanFile(ctx, paths, mimetypes, new MediaScannerConnection.OnScanCompletedListener() {
//					@Override
//					public void onScanCompleted(String p1, Uri p2) {
//						//showToast(ctx, "Scan " + root + " completed");
//						Log.d(TAG, "scanMedia " + p1 + ", " + p2);
//					}
//				});
//			Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//			Uri contentUri = Uri.fromFile(new File(root));
//			mediaScanIntent.setData(contentUri);
//			ctx.sendBroadcast(mediaScanIntent);
		} else {
			Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
			Uri contentUri = Uri.fromFile(new File(root));
			mediaScanIntent.setData(contentUri);
			ctx.sendBroadcast(mediaScanIntent);
		}
	}

	public static void setBrightness(Activity activity, int brightness) {
		//set screen brightness
		Settings.System.putInt(activity.getContentResolver(),
							   Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
		Settings.System.putInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, (int) Math.ceil(brightness / 100.0f * 255));

		//refresh state
		// window manager accepts brightness in float hence dividing brightness by 100.0f
		final Window window = activity.getWindow();
		final WindowManager.LayoutParams lp = window.getAttributes();
		lp.screenBrightness = brightness / 100.0f;
		window.setAttributes(lp);
	}

	public static void setScreenBrightnessAuto(Activity activity) {
		final WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
		attrs.screenBrightness = -1.0f;
		activity.getWindow().setAttributes(attrs);
	}

	public static void setScreenBrightnessSystem(Activity activity, float level) {
		final WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
		attrs.screenBrightness = level;
		activity.getWindow().setAttributes(attrs);
	}

	public static float getScreenBrightnessSystem(Activity activity) {
		final float level = activity.getWindow().getAttributes().screenBrightness;
		return level >= 0 ? level : .5f;
	}

	public static boolean killProcess(Context context, int pid, String packageName) {
		ActivityManager manager  = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		if (pid <= 0) { return false; }
		if (pid == android.os.Process.myPid()) {
			Log.d(TAG, "Killing own process");
			android.os.Process.killProcess(pid);
			return true;
		}
		try {
			manager.killBackgroundProcesses(packageName);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		Method method = null;
		try {
// Since API_LEVEL 8 : v2.2
			method = manager.getClass().getMethod("killBackgroundProcesses", new Class[] { String.class});
		} catch (NoSuchMethodException e) {
// less than 2.2
			try {
				method = manager.getClass().getMethod("restartPackage", new Class[] { String.class });
			} catch (NoSuchMethodException ee) {
				ee.printStackTrace();
			}
		}
		if (method != null) {
			try {
				method.invoke(manager, packageName);
				Log.d(TAG, "kill method  " + method.getName() + " invoked " + packageName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Process.killProcess(pid);
		return true;
	}

	public static Drawable getProcessIcon(PackageManager pk, String pkg_name, Drawable defaultDrawable) {
		try {
			return pk.getApplicationIcon(pkg_name);
		} catch (Exception e) {
			return defaultDrawable;//getResources().getDrawable(R.drawable.ic_doc_apk);
		}
	}

    /**
     * @param value data to encrypt
     * @param key a secret key used for encryption
     * @return String result of encryption
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static String encrypt(String value, String key)
	throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] value_bytes = value.getBytes("UTF-8");
        byte[] key_bytes = getKeyBytes(key);
        return Base64.encodeToString(encrypt(value_bytes, key_bytes, key_bytes), 0);
    }

    public static String encrypt(byte[] value_bytes, String key)
	throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] key_bytes = getKeyBytes(key);
        return Base64.encodeToString(encrypt(value_bytes, key_bytes, key_bytes), 0);
    }

    public static byte[] encrypt(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2, byte[] paramArrayOfByte3)
	throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        // setup AES cipher in CBC mode with PKCS #5 padding
        Cipher localCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // encrypt
        localCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(paramArrayOfByte2, "AES"), new IvParameterSpec(paramArrayOfByte3));
        return localCipher.doFinal(paramArrayOfByte1);
    }

    /**
     *
     * @param value data to decrypt
     * @param key a secret key used for encryption
     * @return String result after decryption
     * @throws KeyException
     * @throws GeneralSecurityException
     * @throws GeneralSecurityException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws IOException
     */
    public static String decrypt(String value, String key) throws GeneralSecurityException, IOException {
        byte[] value_bytes = Base64.decode(value, 0);
        byte[] key_bytes = getKeyBytes(key);
        return new String(decrypt(value_bytes, key_bytes, key_bytes), "UTF-8");
    }

    public static byte[] decrypt(byte[] value_bytes, String key) throws GeneralSecurityException, IOException {
        byte[] key_bytes = getKeyBytes(key);
        return decrypt(value_bytes, key_bytes, key_bytes);
    }

    public static byte[] decrypt(byte[] ArrayOfByte1, byte[] ArrayOfByte2, byte[] ArrayOfByte3)
	throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        // setup AES cipher in CBC mode with PKCS #5 padding
        Cipher localCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // decrypt
        localCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(ArrayOfByte2, "AES"), new IvParameterSpec(ArrayOfByte3));
        return localCipher.doFinal(ArrayOfByte1);
    }

    private static byte[] getKeyBytes(String paramString) throws UnsupportedEncodingException {
        byte[] arrayOfByte1 = new byte[16];
        byte[] arrayOfByte2 = paramString.getBytes("UTF-8");
        System.arraycopy(arrayOfByte2, 0, arrayOfByte1, 0, Math.min(arrayOfByte2.length, arrayOfByte1.length));
        return arrayOfByte1;
    }

	public static String getProcessName(final Context activity) {
		String foregroundProcess = "" ;
		ActivityManager activityManager = (ActivityManager ) activity.
			getApplicationContext().getSystemService(Activity.ACTIVITY_SERVICE);
// Process running
		if (Build .VERSION .SDK_INT >= Build .VERSION_CODES . LOLLIPOP) {
			UsageStatsManager mUsageStatsManager =
				( UsageStatsManager )activity.getSystemService(Activity.USAGE_STATS_SERVICE);
			long time = System . currentTimeMillis();
// We get usage stats for the last 10 seconds
			List < UsageStats> stats = mUsageStatsManager.queryUsageStats
			(UsageStatsManager .INTERVAL_DAILY , time - 1000 * 10, time);
// Sort the stats by the last time used
			if (stats != null) {
				SortedMap<Long ,UsageStats > mySortedMap = new
					TreeMap <Long , UsageStats>();
				for (UsageStats usageStats : stats) {
					mySortedMap . put(usageStats.getLastTimeUsed(), usageStats);
				}
				if (mySortedMap != null && !mySortedMap .isEmpty()) {
					String topPackageName = mySortedMap .get
					(mySortedMap .lastKey()). getPackageName();
					foregroundProcess = topPackageName ;
				}
			}
		} else {
			@SuppressWarnings ( "deprecation" ) ActivityManager . RunningTaskInfo
				foregroundTaskInfo = activityManager . getRunningTasks(1). get(0);
			foregroundProcess = foregroundTaskInfo. topActivity. getPackageName
			();
		}
		return foregroundProcess ;
	}

	public static StringBuilder getSignature(final Context context, final String apkPath) {
		final StringBuilder sb = new StringBuilder();
		try {
			final PackageManager packageManager = context.getPackageManager();
			final PackageInfo packageInfo = packageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_SIGNATURES);
			if (Build.VERSION.SDK_INT >= 5 && packageInfo != null) {
				final ApplicationInfo applicationInfo = packageInfo.applicationInfo;
				if (applicationInfo != null) {
					applicationInfo.sourceDir = apkPath;
					applicationInfo.publicSourceDir = apkPath;
					final CharSequence strName = applicationInfo.loadLabel(packageManager);
					final String strVendor = packageInfo.packageName;

					sb.append(strName + " / " + strVendor);

					final Signature[] arrSignatures = packageInfo.signatures;
					for (final Signature sig : arrSignatures) {

						final byte[] rawCert = sig.toByteArray();
						final InputStream certStream = new ByteArrayInputStream(rawCert);

						try {
							final CertificateFactory certFactory = CertificateFactory.getInstance("X509");
							final X509Certificate x509Cert = (X509Certificate) certFactory.generateCertificate(certStream);

							sb.append("\nCertificate subject: ").append(x509Cert.getSubjectDN());
							sb.append("\nCertificate issuer: ").append(x509Cert.getIssuerDN());
							sb.append("\nCertificate serial number: ").append(x509Cert.getSerialNumber());
							sb.append("\nCertificate type: ").append(x509Cert.getType());
							sb.append("\nCertificate SigAlgName: ").append(x509Cert.getSigAlgName());
							sb.append("\nCertificate SigAlgOID: ").append(x509Cert.getSigAlgOID());
							sb.append("\nCertificate public key: ").append(x509Cert.getPublicKey());
							sb.append("\nCertificate public key algorithm: ").append(x509Cert.getPublicKey().getAlgorithm());
							sb.append("\nCertificate public keyformat: ").append(x509Cert.getPublicKey().getFormat());
							sb.append("\nCertificate version: ").append(x509Cert.getVersion());
							sb.append("\nCertificate not before: ").append(x509Cert.getNotBefore());
							sb.append("\nCertificate not after: ").append(x509Cert.getNotAfter());

						} catch (CertificateException e) {
							e.printStackTrace();
						} finally {
							FileUtil.close(certStream);
						}
					}
				}
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb;
	}

	public static InputStream getStreamFromUri(Context context, Uri uriFromIntent) throws FileNotFoundException {
        return context.getContentResolver().openInputStream(uriFromIntent);
    }

    public static boolean isAttach(Uri uriFromIntent) {
        return (uriFromIntent != null) && (uriFromIntent.getScheme().contains("content"));
    }

//	public static int getToolbarHeight(Context context) {
//		final TypedArray styledAttributes = context.getTheme()
//			.obtainStyledAttributes(
//			new int[] { android.R.attr.actionBarSize });
//		int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
//		styledAttributes.recycle();
//
//		return toolbarHeight;
//	}

	public static void setOnTouchListener(View v, OnTouchListener l) {
		if (v instanceof ViewGroup) {
			final LinkedList<ViewGroup> arr = new LinkedList<>();
			ViewGroup vg = (ViewGroup)v;
			arr.push(vg);
			int total;
			View childAt;
			while (arr.size() > 0) {
				vg = arr.pop();
				vg.setOnTouchListener(l);
				total = vg.getChildCount();
				for (int i = 0; i < total; i++) {
					childAt = vg.getChildAt(i);
					if (childAt instanceof ViewGroup) {
						arr.push((ViewGroup)childAt);
					} else {
						childAt.setOnTouchListener(l);
					}
				}
			}
		} else {
			v.setOnTouchListener(l);
		}
	}

	public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

	public static MenuBuilder createContextIconPopupMenu(Activity activity, View v, int menuRes) {
		final MenuBuilder menuBuilder = new MenuBuilder(activity);
		final MenuInflater inflater = new MenuInflater(activity);
		inflater.inflate(menuRes, menuBuilder);
		final MenuPopupHelper optionsMenu = new MenuPopupHelper(activity , menuBuilder, v);
		optionsMenu.setForceShowIcon(true);
		return menuBuilder;
	}

	public static void setDefaultPickFilePath(Context context, String path) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PREFS_DEFAULTPICKFILEPATH, path);
		editor.commit();
	}


	public static String getDefaultPickFilePath(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getString(PREFS_DEFAULTPICKFILEPATH, Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getAbsolutePath() : "/");
	}

	public static int dpToPx(final int dp, final Context context) {
		final Resources resources = context.getResources();
		final DisplayMetrics metrics = resources.getDisplayMetrics();
		final int px = dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
		return px;
	}

	public static float pxToDp(float px, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float dp = px / (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
		return dp;
	}

	public static LayoutInflater getLayoutInflater(Context ctx) {
		return (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public static CharSequence getClipboardData(final Context context) {
		final int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) { //Android 2.3 and below
			final android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			return clipboard.getText();
		} else { //Android 3.0 and higher
			final android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			return clipboard.getText();
		}
	}

	public static void copyToClipboard(Context context, String name) {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) { //Android 2.3 and below
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(name);
		} else { //Android 3.0 and higher
			try {
				android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
				android.content.ClipData clip = android.content.ClipData.newPlainText("pasted data", name);
				clipboard.setPrimaryClip(clip);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		showToast(context, "Copied \"" + name + "\" to clipboard");
	}

	public static void showToast(View v, String st) {
//		LayoutInflater inflater = LayoutInflater.from(v.getContext());
//		View layout = inflater. inflate (R.layout.dir , null);
//		TextView text = (TextView) v.findViewById (R.id.name);
		TextView text = new TextView(v.getContext());
		text.setText(st);
		Toast toast = new Toast(v.getContext()) ;
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(text);
		toast.show() ;
	}

	public void writePdf(Context context) {

		// Create a shiny new (but blank) PDF document in memory
		// We want it to optionally be printable, so add PrintAttributes
		// and use a PrintedPdfDocument. Simpler: new PdfDocument().
		PrintAttributes printAttrs = new PrintAttributes.Builder().
			setColorMode(PrintAttributes.COLOR_MODE_COLOR).
			setMediaSize(PrintAttributes.MediaSize.NA_LETTER).
			setResolution(new PrintAttributes.Resolution("zooey", "PRINT_SERVICE", 300, 300)).
			setMinMargins(PrintAttributes.Margins.NO_MARGINS).
			build();
		PdfDocument document = new PrintedPdfDocument(context, printAttrs);

		// crate a page description
		PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 300, 1).create();

		// create a new page from the PageInfo
		PdfDocument.Page page = document.startPage(pageInfo);

		// repaint the user's text into the page
//		View content = findViewById(R.id.textArea);
//		content.draw(page.getCanvas());
//
//		// do final processing of the page
//		document.finishPage(page);

		// Here you could add more pages in a longer doc app, but you'd have
		// to handle page-breaking yourself in e.g., write your own word processor...

		// Now write the PDF document to a file; it actually needs to be a file
		// since the Share mechanism can't accept a byte[]. though it can
		// accept a String/CharSequence. Meh.
//		try {
//			File pdfDirPath = new File(getFilesDir(), "pdfs");
//			pdfDirPath.mkdirs();
//			File file = new File(pdfDirPath, "pdfsend.pdf");
//			Uri contentUri = FileProvider.getUriForFile(this, "com.example.fileprovider", file);
//			os = new FileOutputStream(file);
//			document.writeTo(os);
//			document.close();
//			os.close();
//
//			//shareDocument(contentUri);
//		} catch (IOException e) {
//			throw new RuntimeException("Error generating file", e);
//		}
	}

	public static void pdfToImage(File f, String outDir) throws IOException {
        // In this sample, we read a PDF from the assets directory.
        ParcelFileDescriptor mFileDescriptor = null;
		PdfRenderer mPdfRenderer = null;
		PdfRenderer.Page mCurrentPage = null;
		try {
			mFileDescriptor = ParcelFileDescriptor.open(f,  ParcelFileDescriptor.MODE_READ_ONLY);//context.getAssets().openFd("sample.pdf").getParcelFileDescriptor();
			// This is the PdfRenderer we use to render the PDF.
			mPdfRenderer = new PdfRenderer(mFileDescriptor);

			int count = mPdfRenderer.getPageCount();
			// Use `openPage` to open a specific page in PDF.
			String name = outDir + "/" + f.getName();
			for (int i = 0; i < count; i++) {
				mCurrentPage = mPdfRenderer.openPage(i);
				// Important: the destination bitmap must be ARGB (not RGB).
				Bitmap bitmap = Bitmap.createBitmap(mCurrentPage.getWidth(), mCurrentPage.getHeight(),
													Bitmap.Config.ARGB_8888);

				// Here, we render the page onto the Bitmap.
				// To render a portion of the page, use the second and third parameter. Pass nulls to get
				// the default result.
				// Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
				mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
				// Make sure to close the current page before opening another one.
				mCurrentPage.close();
				BitmapUtil.saveBitmap(bitmap, name + "_" + Util.nf.format(i) + ".png");
			}
		} finally {
			mPdfRenderer.close();
			mFileDescriptor.close();
		}
    }

	public static void showToast(Context ctx, String st) {
		Toast.makeText(ctx, st, Toast.LENGTH_SHORT).show();
	}

	public static String bundleToString(Bundle b) {
		StringBuilder sb = new StringBuilder();
		if (b != null) {
			Set<String> s = b.keySet();
			for (String st : s) {
				sb.append(st).append("=").append(b.get(st)).append("\n");
			}
		}
		return sb.toString();
	}

	public static String fetch_tel_status(Context cx) {
		String result = null;
		TelephonyManager tm = (TelephonyManager) cx
			.getSystemService(Context.TELEPHONY_SERVICE);//
		String str = "";
		str += "DeviceId(IMEI) = " + tm.getDeviceId() + "\n";
		str += "DeviceSoftwareVersion = " + tm.getDeviceSoftwareVersion()
			+ "\n";
		str += "Line1Number = " + tm.getLine1Number() + "\n";
		str += "NetworkCountryIso = " + tm.getNetworkCountryIso() + "\n";
		str += "NetworkOperator = " + tm.getNetworkOperator() + "\n";
		str += "NetworkOperatorName = " + tm.getNetworkOperatorName() + "\n";
		str += "NetworkType = " + tm.getNetworkType() + "\n";
		str += "PhoneType = " + tm.getPhoneType() + "\n";
		str += "SimCountryIso = " + tm.getSimCountryIso() + "\n";
		str += "SimOperator = " + tm.getSimOperator() + "\n";
		str += "SimOperatorName = " + tm.getSimOperatorName() + "\n";
		str += "SimSerialNumber = " + tm.getSimSerialNumber() + "\n";
		str += "SimState = " + tm.getSimState() + "\n";
		str += "SubscriberId(IMSI) = " + tm.getSubscriberId() + "\n";
		str += "VoiceMailNumber = " + tm.getVoiceMailNumber() + "\n";

		int mcc = cx.getResources().getConfiguration().mcc;
		int mnc = cx.getResources().getConfiguration().mnc;
		str += "IMSI MCC (Mobile Country Code):" + String.valueOf(mcc) + "\n";
		str += "IMSI MNC (Mobile Network Code):" + String.valueOf(mnc) + "\n";
		result = str;
		return result;
	}

	public static void setImageDrawable(ImageView imgView, Context ctx, int resId) {
		imgView.setImageBitmap(BitmapFactory.decodeResource(ctx.getResources(), resId));
	}

	public static void setImageDrawable(ImageView imgView, String path) {
		imgView.setImageDrawable(Drawable.createFromPath(path));
	}

	public static boolean isRoot() {
		boolean flag = false;

		try {
			if ((!new File("/system/bin/su").exists())
				&& (!new File("/system/xbin/su").exists())) {
				flag = false;
			} else {
				flag = true;
			}
		} catch (Exception e) {

		}
		return flag;
	}

	public static Intent getApkFileIntent(File file) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),
							  "application/vnd.android.package-archive");
		return intent;
	}

	public static String getApkVersionName(Context context, String pkgName)
	throws NameNotFoundException {
		PackageInfo pkgInfo = context.getApplicationContext().getPackageManager()
			.getPackageInfo(pkgName, 0);
		return pkgInfo.versionName;
	}

	public static String getApkVersionName(PackageManager context, String pkgName)
	throws NameNotFoundException {
		return context.getPackageInfo(pkgName, 0).versionName;
	}

	public static int getApkVersionCode(Context context, String pkgName)
	throws NameNotFoundException {
		PackageInfo pkgInfo = context.getApplicationContext().getPackageManager()
			.getPackageInfo(pkgName, 0);
		return pkgInfo.versionCode;
	}

	public static String getApkMetaData(Context context, String apkPath,
										String key) {

		if (context == null || key == null) {
			return null;
		}

		PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkPath,
													PackageManager.GET_META_DATA);
		if (info != null) {
			ApplicationInfo appInfo = info.applicationInfo;

			if (appInfo == null || appInfo.metaData == null) {
				return null;
			}

			return appInfo.metaData.getString(key);
		}

		return null;
	}

	public static ArrayList<String> getApkPath(Context context) {
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> pkginfolist = pm.getInstalledPackages(PackageManager.GET_META_DATA);
		ArrayList<String> apkPathList = new ArrayList<String>(pkginfolist.size());
		for (PackageInfo pi : pkginfolist) {
			ApplicationInfo applicationInfo = pi.applicationInfo;
			if (applicationInfo != null) {
				apkPathList.add(applicationInfo.sourceDir);
			}
		}
		return apkPathList;
	}

	public static Drawable getApkIcon(Context ctx, String apkPath, int defRes) {
		Drawable icon = null;
		try {
			PackageManager packageManager = ctx.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
			if (Build.VERSION.SDK_INT >= 5 && packageInfo != null) {
				ApplicationInfo appInfo = packageInfo.applicationInfo;
				if (appInfo != null) {
					appInfo.sourceDir = apkPath;
					appInfo.publicSourceDir = apkPath;
					icon = appInfo.loadIcon(packageManager);
				}
//				if(icon.getIntrinsicHeight() >50 && icon.getIntrinsicWidth()>50){
//					//Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
//					// int dp5 = (int)(activity.getResources().getDisplayMetrics().densityDpi/120);
//					//icon= new BitmapDrawable(activity.getResources(),Bitmap.createScaledBitmap(bitmap, 50*dp5, 50*dp5, true));
//				}
			} else {
				icon = ctx.getResources().getDrawable(defRes); //R.drawable.apk_file
			}
			return icon;
		} catch (Exception e) {
			return ctx.getResources().getDrawable(defRes);
		}
	}

	public static Drawable getApkIcon(final Context context, final String apkPath) {
        final PackageManager pm = context.getPackageManager();
        final PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            final ApplicationInfo appInfo = packageInfo.applicationInfo;
            if (appInfo != null) {
				appInfo.sourceDir = apkPath;
				appInfo.publicSourceDir = apkPath;
       	     	final Drawable icon = appInfo.loadIcon(pm);
      	      	return icon;
			}
        }
        return null;
    }

	public static boolean isAppRunningTop(Context context) {

		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
		if (list.size() > 0) {
			String packageName = context.getPackageName();
			ActivityManager.RunningTaskInfo topRunningTaskinfo  = list.get(0);
			if (topRunningTaskinfo.topActivity.getPackageName().equals(packageName)) {
				return true;
			}
		}

		return false;
	}

	public static boolean startApp(Context context, String packageName,
								   String className, Map<String, String> data) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClassName(packageName, className);

		if (data != null) {
			Iterator<Map.Entry<String, String>> iter = data.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, String> entry = iter.next();
				intent.putExtra(entry.getKey(), entry.getValue());
			}
		}
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {

			return false;
		}

		return true;
	}

	public static boolean getAirplaneMode(Context context) {
		try {
			int airplaneModeSetting = Settings.System.getInt(
				context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON);
			return airplaneModeSetting == 1 ?true: false;
		} catch (Settings.SettingNotFoundException e) {
			return false;
		}
	}

	public static StringBuilder getLogcat() {
		StringBuilder log=new StringBuilder();
		try {
			java.lang.Process process = Runtime.getRuntime().exec("logcat -d");
			BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				log.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return log;
	}

	public static final boolean isWifiEnabled(Context context) {
        return ((WifiManager)context.getSystemService(Context.WIFI_SERVICE)).isWifiEnabled();
    }

	public static boolean isWiFiConnected(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
			.getApplicationContext().getSystemService(
            Context.CONNECTIVITY_SERVICE);

		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getTypeName().equals("WIFI")
						&& info[i].isConnected())
						return true;
				}
			}
		}
		return false;
	}

	public static void enableWifi(Context ctx) {
		WifiManager wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		wifi.setWifiEnabled(true);
//		Method[] wmMethods = wifi.getClass().getDeclaredMethods();
//		for(Method method: wmMethods){
//			if(method.getName().equals("setWifiEnabled")){
//				WifiConfiguration netConfig = new WifiConfiguration();
//				netConfig.SSID = "\"PROVAAP\"";
//				netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
//				netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//				netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//				netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);    netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//				netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//				netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//				netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);  
//
//				try {
//					method.invoke(wifi, netConfig,true);
//				} catch (IllegalArgumentException e) {
//					e.printStackTrace();
//				} catch (IllegalAccessException e) {
//					e.printStackTrace();
//				} catch (InvocationTargetException e) {
//					e.printStackTrace();
//				}
//			}
//		}
	}

	public static Integer getSystemWifiIpAddress(Context context) {
		WifiManager wManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = wManager.getConnectionInfo();

		int ipAddress = wInfo.getIpAddress();
		if (ipAddress == 0)
			return null;
		return ipAddress;
	}

	public static boolean isConnected(Context mContext) {

		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo info = cm.getActiveNetworkInfo();
		//int netType = info.getType();

//		if (netType == ConnectivityManager.TYPE_WIFI) {
//			if (allowWiFi && info.isConnected()) 
//				return true;
//		} else if (netType == ConnectivityManager.TYPE_MOBILE) {
//			if (allowMobile && info.isConnected()) 
//				return true;
//		}
        return info.isConnected();
	}
	public static String getSharedPreference(final Context ctx, final String id) {
		final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		return defaultSharedPreferences.getString(id, null);
	}

	public static boolean getSharedPreference(final Context ctx, final String id, boolean defaultValue) {
		final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		return defaultSharedPreferences.getBoolean(id, defaultValue);
	}

	public static void setSharedPreference(final Context ctx, final String id, @Nullable final boolean value) {
		final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		final SharedPreferences.Editor editor = defaultSharedPreferences.edit();
		editor.putBoolean(id, value);
		editor.commit();
	}

	public static String getSharedPreference(final Context ctx, final String id, String defaultValue) {
		final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		return defaultSharedPreferences.getString(id, defaultValue);
	}

	public static void setSharedPreference(final Context ctx, final String id, @Nullable final String value) {
		final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		final SharedPreferences.Editor editor = defaultSharedPreferences.edit();
		editor.putString(id, value);
		editor.commit();
	}

	public static int getSharedPreference(final Context ctx, final String id, final int defaultValue) {
		final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		return defaultSharedPreferences.getInt(id, defaultValue);
	}

	public static void setSharedPreference(final Context ctx, final String id, @Nullable final int value) {
		final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		final SharedPreferences.Editor editor = defaultSharedPreferences.edit();
		editor.putInt(id, value);
		editor.commit();
	}

//	public static void startService(Context ctx, Class<? extends Service> service, String action, String tag) {
//		Log.e(tag, "Starting service");
//		Intent intent = new Intent(action);//ForegroundService.ACTION_FOREGROUND);//ACTION_BACKGROUND
//		intent.setClass(ctx, service);
//		ctx.startService(intent);
//	}

	public static boolean isPhone(Activity activity) {
		Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		defaultDisplay.getMetrics(outMetrics);
		Log.i("defaultDisplay.outMetrics", outMetrics + ".");
		Point op = new Point();
		defaultDisplay.getSize(op);
		Log.i("defaultDisplay.getSize()", op + ".");
		return op.x / outMetrics.xdpi < 3;
	}

	public static void uninstall(Context ctx, String pkgName) {
		Uri packageUri = Uri.parse("package:" + pkgName);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
		//Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
		uninstallIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
		ctx.startActivity(uninstallIntent);
	}

	public boolean uninstallPackage(Context context, String packageName) {
		//ComponentName name = new ComponentName(MyAppName, MyDeviceAdminReceiver.class.getCanonicalName());
		PackageManager packageManger = context.getPackageManager();
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			PackageInstaller packageInstaller = packageManger.getPackageInstaller();
			PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
			params.setAppPackageName(packageName);
			int sessionId = 0;
			try {
				sessionId = packageInstaller.createSession(params);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			packageInstaller.uninstall(packageName, PendingIntent.getBroadcast(context, sessionId,
																			   new Intent("android.intent.action.MAIN"), 0).getIntentSender());
			return true;
		}
		System.err.println("old sdk");
		return false;
	}

	public boolean installPackage(Context context,
								  String packageName, String packagePath) {
		//ComponentName name = new ComponentName(MyAppName, MyDeviceAdminReceiver.class.getCanonicalName());
		PackageManager packageManger = context.getPackageManager();
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			PackageInstaller packageInstaller = packageManger.getPackageInstaller();
			PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
			params.setAppPackageName(packageName);
			try {
				int sessionId = packageInstaller.createSession(params);
				PackageInstaller.Session session = packageInstaller.openSession(sessionId);
				OutputStream out = session.openWrite(packageName + ".apk", 0, -1);
				FileUtil.is2OS(new FileInputStream(packagePath), out); //read the apk content and write it to out
				session.fsync(out);
				out.close();
				Log.d(TAG, "installing...");
				session.commit(PendingIntent.getBroadcast(context, sessionId,
														  new Intent("android.intent.action.MAIN"), 0).getIntentSender());
				Log.d(TAG, "install request sent");
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		System.err.println("old sdk");
		return false;
	}

	public static PackageInfo getPackageInfo(Context ctx, String pkg) {
		Log.d(TAG, "getPackageInfo " + pkg);
		PackageManager pm = ctx.getPackageManager();
		List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_META_DATA);
		PackageInfo info1 = null;//=item.getPackageInfo().applicationInfo;
		for (PackageInfo info:packages) {
			if (info.packageName.equals(pkg)) {
				info1 = info;
			}
		}
		return info1;
	}

	public static ApplicationInfo getAppInfo(Context ctx, String pkg) {
		PackageManager pm = ctx.getPackageManager();
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		ApplicationInfo info1 = null;//=item.getPackageInfo().applicationInfo;
		for (ApplicationInfo info:packages) {
			if (info.packageName.equals(pkg)) {
				info1 = info;
			}
		}
		return info1;
	}

	public static void investigateApps(Context ctx) {
		PackageManager pm = ctx.getPackageManager();
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
		StringBuilder sb = new StringBuilder();
		for (ApplicationInfo applicationInfo : packages) {
			sb.append("\nInstalled package :" + applicationInfo.packageName);
			String sourceDir = applicationInfo.sourceDir;
			sb.append("\nSource dir : " + sourceDir);

			File f = new File(sourceDir);
			sb.append("\nSize: " + Util.nf.format(f.length()) + " byte(s) can read " + f.canRead() + ", can write " + f.canWrite());
			Log.d(TAG, "Package Name :" + applicationInfo.packageName);

			Log.d(TAG, "Launch Intent For Package :"   +  
				  pm.getLaunchIntentForPackage(applicationInfo.packageName));

			Log.d(TAG, "Application Label :"   + pm.getApplicationLabel(applicationInfo));

//			try {
//				System.out.println("Application Label :"   + 
//								   pm.getApplicationIcon(packageInfo.packageName).toString());
//			} catch (PackageManager.NameNotFoundException e) {}


			/*if(i==2) {
			 startActivity(pm.getLaunchIntentForPackage(packageInfo.packageName));
			 break;
			 }*/


			// the getLaunchIntentForPackage returns an intent that you can use with startActivity() 
			sb.append("\nLaunch Activity :" + pm.getLaunchIntentForPackage(applicationInfo.packageName) + "\n"); 
		}

		List<PackageInfo> packages2 = pm.getInstalledPackages(PackageManager.GET_META_DATA);
		for (PackageInfo p : packages2) {
			sb.append("\"" + p.packageName + "_" + p.versionName + "\" is System app " + isSystemPackage(p) + "\n");
			if (!isSystemPackage(p)) {
				ctx.startActivity(pm.getLaunchIntentForPackage(p.packageName));
				break;
			}
		}
	}

	public static List<String> getInstalledComponentList(Context ctx)
	throws PackageManager.NameNotFoundException {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> ril = ctx.getPackageManager().queryIntentActivities(mainIntent, 0);
        List<String> componentList = new ArrayList<String>();
        String name = null;

        PackageManager packageManager = ctx.getPackageManager();
		for (ResolveInfo ri : ril) {
            if (ri.activityInfo != null) {
                Resources res = packageManager.getResourcesForApplication(ri.activityInfo.applicationInfo);
                if (ri.activityInfo.labelRes != 0) {
                    name = res.getString(ri.activityInfo.labelRes);
					Log.d(TAG, "has res " + name);
                } else {
                    name = ri.activityInfo.applicationInfo.loadLabel(
						packageManager).toString();
					Log.d(TAG, "no res " + name);
                }

                componentList.add(name);
            }
        }
        return componentList;
    }

	public static boolean isSystemPackage(PackageInfo pkgInfo) {
		return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? 
			true : false;
	}

//	public static String getRunningApps(Context ctx) {
//		ActivityManager actvityManager = (ActivityManager)ctx.getSystemService(ctx.ACTIVITY_SERVICE);
//		List<ActivityManager.RunningAppProcessInfo> procInfos = actvityManager.getRunningAppProcesses();
//		StringBuilder sb = new StringBuilder();
//		for (ActivityManager.RunningAppProcessInfo procInfo : procInfos) {
//			sb.append(procInfo.processName + "\n");
//		}
//		return sb.toString();
//	}

	public static boolean isPortrait(Activity activity) {
		Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
		//Log.i("defaultDisplay", defaultDisplay + ".");
		Point op = new Point();
		defaultDisplay.getSize(op);
		//Log.i("defaultDisplay.getSize()", op + ".");
		return op.x < op.y;
	}

	public static void copyAssetToDir(Context activity, String dest, String src) {
		try {
			final String newDest = dest + "/" + src;
			final File file = new File(newDest);
			if (!file.exists()) {
				Log.d("copyAssetToDir", newDest);
				final InputStream ins = activity.getAssets().open(src);
				FileUtil.is2File(ins, newDest);
			} else {
				System.out.println("already existed" + newDest);
			}
		} catch (Exception e) {
			Log.e("copyAssetToDir", e.getMessage(), e);
		}
	}

	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
			|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

//	public File getAlbumStorageDir(String albumName) {
//		// Get the directory for the user's public pictures directory.
//		File file = new File(
//			Environment
//			.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
//			albumName);
//		if (!file.mkdirs()) {
//			Log.d("getAlbumStorageDir", "Directory not created");
//		}
//		return file;
//	}

//	public File getAlbumStorageDir(Context context, String albumName) {
//		// Get the directory for the app's private pictures directory.
//		File file = new File(
//			context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
//			albumName);
//		if (!file.mkdirs()) {
//			Log.d("getAlbumStorageDir", "Directory not created");
//		}
//		return file;
//	}

}
