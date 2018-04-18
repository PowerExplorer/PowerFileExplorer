package net.gnu.androidutil;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import dalvik.system.*;
import java.io.*;
import net.gnu.util.*;
import net.gnu.explorer.*;

/**
 * Utility class for helping parsing file systems.
 */
public final class AndroidPathUtils {
	private static DexClassLoader cl = null;
	private static Uri treeUri;

	//public static Context applicationContext;
	
	public static DateFormat df = DateFormat.getDateTimeInstance();
	
	/**
	 * The name of the primary volume (LOLLIPOP).
	 */
	private static final String PRIMARY_VOLUME_NAME = "primary";

	/**
	 * Hide default constructor.
	 */
	private AndroidPathUtils() {
		throw new UnsupportedOperationException();
	}

	public static void setTreeUri(Uri treeUri)
	{
		AndroidPathUtils.treeUri = treeUri;
	}

	public static Uri getTreeUri(Context ctx) {
		if (treeUri == null) {
			treeUri = getSharedPreferenceUri("key_internal_uri_extsdcard", ctx);
		}
		return treeUri;
	}

	public static synchronized DexClassLoader loadSecondDex(Context ctx, String dexNameInAsset) {
		if (cl != null) {
			return cl;
		}
		final int BUF_SIZE = 8 * 1024;
		// Before the secondary dex file can be processed by the DexClassLoader,
		// it has to be first copied from asset resource to a storage location.
		File dexInternalStoragePath = new File(ctx.getDir("dex", Context.MODE_PRIVATE),
											   dexNameInAsset);
		dexInternalStoragePath.getParentFile().mkdirs();
		Log.i("dexInternalStoragePath", dexInternalStoragePath.getAbsolutePath());
		BufferedInputStream bis = null;
		OutputStream dexWriter = null;
		try {
			bis = new BufferedInputStream(ctx.getAssets().open(dexNameInAsset));
			dexWriter = new BufferedOutputStream(
				new FileOutputStream(dexInternalStoragePath));
			byte[] buf = new byte[BUF_SIZE];
			int len;
			while((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
				dexWriter.write(buf, 0, len);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			FileUtil.close(bis);
			FileUtil.flushClose(dexWriter);
		}

		// Internal storage where the DexClassLoader writes the optimized dex file to
		final File optimizedDexOutputPath = ctx.getDir("outdex", Context.MODE_PRIVATE);
		Log.i("optimizedDexOutputPath", optimizedDexOutputPath.getAbsolutePath());
		optimizedDexOutputPath.getParentFile().mkdirs();
		DexClassLoader cl = new DexClassLoader(dexInternalStoragePath.getAbsolutePath(),
											   optimizedDexOutputPath.getAbsolutePath(),
											   null,
											   ctx.getClassLoader());
		//Class libProviderClazz = null;
		//try {
			// Load the library.
			//libProviderClazz = cl.loadClass("Main");
			// Cast the return object to the library interface so that the
			// caller can directly invoke methods in the interface.
			// Alternatively, the caller can invoke methods through reflection,
			// which is more verbose.
			// libProviderClazz.getMethod("main", String[].class).invoke(null, null);
//			LibraryInterface lib = (LibraryInterface) libProviderClazz.newInstance();
//			lib.showAwesomeToast(this, "hello");
		//} catch (ClassNotFoundException e) {
			//e.printStackTrace();
		//}
		return cl;
	}
	
	public static OutputStream getOutputStream(@NonNull final File target, Context context)//,long s) 
	throws IOException {

        OutputStream outStream = null;
        try {
            // First try the normal way
            if (isWritable(target)) {
                // standard way
                outStream = new FileOutputStream(target);
            } else {
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                    // Storage Access Framework
                    DocumentFile targetDocument = getDocumentFile(target, false, false, context);
                    outStream =
                            context.getContentResolver().openOutputStream(targetDocument.getUri());
                } else if (Build.VERSION.SDK_INT==Build.VERSION_CODES.KITKAT) {
                    // Workaround for Kitkat ext SD card
					//return com.amaze.filemanager.filesystem.MediaStoreHack.getOutputStream(context,target.getPath());
                }



            }
        } catch (IOException e) {
            Log.e("AmazeFileUtils",
                    "Error when copying file from " +  target.getAbsolutePath(), e);
					throw e;
        }
      return outStream;
        }
	
	/**
	 * Determine the camera folder. There seems to be no Android API to work for real devices, so this is a best guess.
	 *
	 * @return the default camera folder.
	 */
	public static String getDefaultCameraFolder() {
		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		if (path.exists()) {
			File test1 = new File(path, "Camera/");
			if (test1.exists()) {
				path = test1;
			}
			else {
				File test2 = new File(path, "100ANDRO/");
				if (test2.exists()) {
					path = test2;
				}
				else {
					path = new File(path, "100MEDIA/");
				}
			}
		}
		else {
			path = new File(path, "Camera/");
		}
		return path.getAbsolutePath();
	}


	/**
	 * Determine the main folder of the external SD card containing the given file.
	 *
	 * @param file the file.
	 * @return The main folder of the external SD card containing this file, if the file is on an SD card. Otherwise,
	 * null is returned.
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static String getExtSdCardFolder(@NonNull final File file, final Context c) {
		String[] extSdPaths = getExtSdCardPaths(c);
		try {
			for (String extSdPath : extSdPaths) {
				Log.d("path", extSdPath + "");
				if (file.getCanonicalPath().startsWith(extSdPath)) {
					return extSdPath;
				}
			}
		} catch (IOException e) {
			return null;
		}
		return null;
	}

	/**
	 * Determine if a file is on external sd card. (Kitkat or higher.)
	 *
	 * @param file The file.
	 * @return true if on external sd card.
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static boolean isOnExtSdCard(@NonNull final File file, final Context c) {
		return getExtSdCardFolder(file, c) != null;
	}

	/**
	 * Retrieve an Uri shared preference.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @return the corresponding preference value.
	 */
	public static Uri getSharedPreferenceUri(final int preferenceId, final Context c) {
		String uriString = getSharedPreferences(c).getString(c.getString(preferenceId), null);
		Log.d("AndroidPathUtils", "getSharedPreferenceUri = " + uriString);
		if (uriString == null) {
			return null;
		} else {
			return Uri.parse(uriString);
		}
	}

	public static Uri getSharedPreferenceUri(final String preferenceId, final Context c) {
		String uriString = getSharedPreferences(c).getString(preferenceId, null);
		Log.d("AndroidPathUtils", "getSharedPreferenceUri = " + uriString);
		if (uriString == null) {
			return null;
		} else {
			return Uri.parse(uriString);
		}
	}

	/**
	 * Retrieve the default shared preferences of the application.
	 *
	 * @return the default shared preferences.
	 */
	public static SharedPreferences getSharedPreferences(final Context c) {
		return PreferenceManager.getDefaultSharedPreferences(c);
	}

	/**
	 * Set a shared preference for an Uri.
	 *
	 * @param preferenceId the id of the shared preference.
	 * @param uri          the target value of the preference.
	 */
	public static void setSharedPreferenceUri(final int preferenceId, @Nullable final Uri uri, final Context c) {
		treeUri = uri;
		SharedPreferences.Editor editor = getSharedPreferences(c).edit();
		if (uri == null) {
			editor.putString(c.getString(preferenceId), null);
		} else {
			editor.putString(c.getString(preferenceId), uri.toString());
		}
		editor.apply();
	}

	public static void setSharedPreferenceUri(final String preferenceId, @Nullable final Uri uri, final Context c) {
		treeUri = uri;
		SharedPreferences.Editor editor = getSharedPreferences(c).edit();
		if (uri == null) {
			editor.putString(preferenceId, null);
		} else {
			editor.putString(preferenceId, uri.toString());
		}
		editor.apply();
	}

	/**
	 * Get the full path of a document from its tree URI.
	 *
	 * @param treeUri The tree RI.
	 * @return The path (without trailing file separator).
	 */
	@Nullable
	public static String getFullPathFromTreeUri(@Nullable final Uri treeUri, final Context c) {
		if (treeUri == null) {
			return null;
		}
		String volumePath = AndroidPathUtils.getVolumePath(AndroidPathUtils.getVolumeIdFromTreeUri(treeUri), c);
		if (volumePath == null) {
			return File.separator;
		}
		if (volumePath.endsWith(File.separator)) {
			volumePath = volumePath.substring(0, volumePath.length() - 1);
		}

		String documentPath = AndroidPathUtils.getDocumentPathFromTreeUri(treeUri);
		if (documentPath.endsWith(File.separator)) {
			documentPath = documentPath.substring(0, documentPath.length() - 1);
		}

		if (documentPath.length() > 0) {
			if (documentPath.startsWith(File.separator)) {
				return volumePath + documentPath;
			} else {
				return volumePath + File.separator + documentPath;
			}
		}
		else {
			return volumePath;
		}
	}

	/**
	 * Get the path of a certain volume.
	 *
	 * @param volumeId The volume id.
	 * @return The path.
	 */
	private static String getVolumePath(final String volumeId, final Context c) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			return null;
		}

		try {
			StorageManager mStorageManager =
				(StorageManager) c.getSystemService(Context.STORAGE_SERVICE);

			Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");

			Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
			Method getUuid = storageVolumeClazz.getMethod("getUuid");
			Method getPath = storageVolumeClazz.getMethod("getPath");
			Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
			Object result = getVolumeList.invoke(mStorageManager);

			final int length = Array.getLength(result);
			for (int i = 0; i < length; i++) {
				Object storageVolumeElement = Array.get(result, i);
				String uuid = (String) getUuid.invoke(storageVolumeElement);
				Boolean primary = (Boolean) isPrimary.invoke(storageVolumeElement);

				// primary volume?
				if (primary && PRIMARY_VOLUME_NAME.equals(volumeId)) {
					return (String) getPath.invoke(storageVolumeElement);
				}

				// other volumes?
				if (uuid != null) {
					if (uuid.equals(volumeId)) {
						return (String) getPath.invoke(storageVolumeElement);
					}
				}
			}

			// not found.
			return null;
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Get the volume ID from the tree URI.
	 *
	 * @param treeUri The tree URI.
	 * @return The volume ID.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private static String getVolumeIdFromTreeUri(final Uri treeUri) {
		final String docId = DocumentsContract.getTreeDocumentId(treeUri);
		final String[] split = docId.split(":");

		if (split.length > 0) {
			return split[0];
		} else {
			return null;
		}
	}

	/**
	 * Get the document path (relative to volume name) for a tree URI (LOLLIPOP).
	 *
	 * @param treeUri The tree URI.
	 * @return the document path.
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private static String getDocumentPathFromTreeUri(final Uri treeUri) {
		final String docId = DocumentsContract.getTreeDocumentId(treeUri);
		final String[] split = docId.split(":");
		if ((split.length >= 2) && (split[1] != null)) {
			return split[1];
		} else {
			return File.separator;
		}
	}
	
	/**
	 * Get a DocumentFile corresponding to the given file (for writing on ExtSdCard on Android 5). If the file is not
	 * existing, it is created.
	 *
	 * @param file              The file.
	 * @param isDirectory       flag indicating if the file should be a directory.
	 * @param createDirectories flag indicating if intermediate path directories should be created if not existing.
	 * @return The DocumentFile
	 */
	public static DocumentFile getDocumentFile(@NonNull final File file, final boolean isDirectory,
											   final boolean createDirectories, final Context c) {
//		Log.d("getDocumentFile start", df.format(System.currentTimeMillis()));
		String baseFolder = null;
		baseFolder = getExtSdCardFolder(file, c);
		if (baseFolder == null) {
			return null;
		}
		String relativePath;
		try {
			String fullPath = file.getCanonicalPath();
			relativePath = fullPath.substring(baseFolder.length() + 1);
		} catch (IOException e) {
			return null;
		}

		if (treeUri == null) {
			treeUri = getSharedPreferenceUri("key_internal_uri_extsdcard", c);
			if (treeUri == null) { 
				return null; 
			} 
		}

		// start with root of SD card and then parse through document tree.
		DocumentFile document = DocumentFile.fromTreeUri(c, treeUri);

		String[] parts = relativePath.split("\\/");
		for (int i = 0; i < parts.length; i++) {
			DocumentFile nextDocument = document.findFile(parts[i]);

			if (nextDocument == null) {
				if (i < parts.length - 1) {
					if (createDirectories) {
						nextDocument = document.createDirectory(parts[i]);
					} else {
						return null;
					}
				} else if (isDirectory) {
					nextDocument = document.createDirectory(parts[i]);
				} else {
					nextDocument = document.createFile("image", parts[i]);
				}
			}
			document = nextDocument;
		}
//		Log.d("getDocumentFile end", df.format(System.currentTimeMillis()));
		return document;
	}
	
	/**
	 * Get an Uri from an file path.
	 *
	 * @param path The file path.
	 * @return The Uri.
	 */
	public static Uri getUriFromFile(final String path, final Context c) {
		ContentResolver resolver = c.getContentResolver();

		Cursor filecursor = resolver.query(MediaStore.Files.getContentUri("external"),
										   new String[] {BaseColumns._ID}, MediaColumns.DATA + " = ?",
										   new String[] {path}, MediaColumns.DATE_ADDED + " desc");
		if (filecursor == null) {
			return null;
		}
		filecursor.moveToFirst();

		if (filecursor.isAfterLast()) {
			filecursor.close();
			ContentValues values = new ContentValues();
			values.put(MediaColumns.DATA, path);
			return resolver.insert(MediaStore.Files.getContentUri("external"), values);
		}
		else {
			int imageId = filecursor.getInt(filecursor.getColumnIndex(BaseColumns._ID));
			Uri uri = MediaStore.Files.getContentUri("external").buildUpon().appendPath(
				Integer.toString(imageId)).build();
			filecursor.close();
			return uri;
		}
	}
	

	/**
	 * Get the Album Id from an Audio file.
	 *
	 * @param file The audio file.
	 * @return The Album ID.
	 */
	@SuppressWarnings("resource")
	public static int getAlbumIdFromAudioFile(@NonNull final File file, final Context c) {
		ContentResolver resolver = c.getContentResolver();
		Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
									   new String[] {MediaStore.Audio.AlbumColumns.ALBUM_ID},
									   MediaStore.MediaColumns.DATA + "=?",
									   new String[] {file.getAbsolutePath()}, null);
		if (cursor == null || !cursor.moveToFirst()) {
			// Entry not available - create entry.
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
			ContentValues values = new ContentValues();
			values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
			values.put(MediaStore.MediaColumns.TITLE, "{MediaWrite Workaround}");
			values.put(MediaStore.MediaColumns.SIZE, file.length());
			values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg");
			values.put(MediaStore.Audio.AudioColumns.IS_MUSIC, true);
			resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
		}
		cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
								new String[] {MediaStore.Audio.AlbumColumns.ALBUM_ID},
								MediaStore.MediaColumns.DATA + "=?",
								new String[] {file.getAbsolutePath()}, null);
		if (cursor == null) {
			return 0;
		}
		if (!cursor.moveToFirst()) {
			cursor.close();
			return 0;
		}
		int albumId = cursor.getInt(0);
		cursor.close();
		return albumId;
	}
	
	/**
	 * Copy a file. The target file may even be on external SD card for Kitkat.
	 *
	 * @param source The source file
	 * @param target The target file
	 * @return true if the copying was successful.
	 */
	@SuppressWarnings("null")
	public static boolean copyFile(@NonNull final File source, @NonNull final File target, final Context c) {
		InputStream inStream = null;
		OutputStream outStream = null;
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		try {
			
			// First try the normal way
			if (isWritable(target)) {
				// standard way
				outStream = new FileOutputStream(target);
				inStream = new FileInputStream(source);
				inChannel = ((FileInputStream)inStream).getChannel();
				outChannel = ((FileOutputStream) outStream).getChannel();
				inChannel.transferTo(0, inChannel.size(), outChannel);
			}
			else {
				if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
					// Storage Access Framework
					DocumentFile targetDocument = getDocumentFile(target, false, true, c);
					if (targetDocument != null) {
						outStream = new BufferedOutputStream(c.getContentResolver().openOutputStream(targetDocument.getUri()));
					}
				}
				else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
					// Workaround for Kitkat ext SD card
					Uri uri = getUriFromFile(target.getAbsolutePath(), c);
					if (uri != null) {
						outStream = c.getContentResolver().openOutputStream(uri);
					}
				}
				else {
					return false;
				}

				if (outStream != null) {
					inStream = new BufferedInputStream(new FileInputStream(source));
					// Both for SAF and for Kitkat, write to output stream.
					byte[] buffer = new byte[65536]; // MAGIC_NUMBER
					int bytesRead;
					while ((bytesRead = inStream.read(buffer)) != -1) {
						outStream.write(buffer, 0, bytesRead);
					}
				}
			}
		} catch (Exception e) {
			Log.e("Uri",
					"Error when copying file from " + source.getAbsolutePath() + " to " + target.getAbsolutePath(), e);
			return false;
		} finally {
			FileUtil.close(inStream, inChannel, outChannel);
			FileUtil.flushClose(outStream);
		}
		return true;
	}

	/**
	 * Delete a file. May be even on external SD card.
	 *
	 * @param file the file to be deleted.
	 * @return True if successfully deleted.
	 */
	public static boolean deleteFile(@NonNull final String filePath, final Context c) {
		return deleteFile(new File(filePath), c);
	}
	
	public static boolean deleteFile(@NonNull final File file, final Context c) {
		// First try the normal deletion.
		if (file.delete()) {
			return true;
		}

		// Try with Storage Access Framework.
		if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
			DocumentFile document = getDocumentFile(file, false, false, c);
			return document != null && document.delete();
		}

		// Try the Kitkat workaround.
		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
			ContentResolver resolver = c.getContentResolver();

			try {
				Uri uri = getUriFromFile(file.getAbsolutePath(), c);
				if (uri != null) {
					resolver.delete(uri, null, null);
				}
				return !file.exists();
			}
			catch (Exception e) {
				Log.e("Uri", "Error when deleting file " + file.getAbsolutePath(), e);
				return false;
			}
		}

		return !file.exists();
	}

	/**
	 * Move a file. The target file may even be on external SD card.
	 *
	 * @param source The source file
	 * @param target The target file
	 * @return true if the copying was successful.
	 */
	public static boolean moveFile(@NonNull final File source, @NonNull final File target, final Context c) {
		// First try the normal rename.
		boolean success = source.renameTo(target);

		if (!success) {
			success = copyFile(source, target, c);
			if (success) {
				success = deleteFile(source, c);
			}
		}

//		if (success) {
//			PupilAndIrisDetector.notifyFileRename(source.getAbsolutePath(), target.getAbsolutePath());
//		}

		return success;
	}

	/**
	 * Rename a folder. In case of extSdCard in Kitkat, the old folder stays in place, but files are moved.
	 *
	 * @param source The source folder.
	 * @param target The target folder.
	 * @return true if the renaming was successful.
	 */
	public static boolean renameFolder(@NonNull final File source, @NonNull final File target, final Context c) {
		// First try the normal rename.
		if (source.renameTo(target)) {
			return true;
		}
		if (target.exists()) {
			return false;
		}

		// Try the Storage Access Framework if it is just a rename within the same parent folder.
		if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) && source.getParent().equals(target.getParent())) {
			DocumentFile document = getDocumentFile(source, true, true, c);
			if (document != null && document.renameTo(target.getName())) {
				return true;
			}
		}

		// Try the manual way, moving files individually.
		if (!mkdir(target, c)) {
			return false;
		}

		File[] sourceFiles = source.listFiles();

		if (sourceFiles == null) {
			return true;
		}

		for (File sourceFile : sourceFiles) {
			String fileName = sourceFile.getName();
			File targetFile = new File(target, fileName);
			if (!copyFile(sourceFile, targetFile, c)) {
				// stop on first error
				return false;
			}
		}
		// Only after successfully copying all files, delete files on source folder.
		for (File sourceFile : sourceFiles) {
			if (!deleteFile(sourceFile, c)) {
				// stop on first error
				return false;
			}
		}
		return true;
	}

	/**
	 * Get a temp file.
	 *
	 * @param file The base file for which to create a temp file.
	 * @return The temp file.
	 */
	@NonNull
	public static File getTempFile(@NonNull final File file, final Context c) {
		File extDir = new File(c.getExternalCacheDir(), "temp");
		if (!extDir.exists()) {
			//noinspection ResultOfMethodCallIgnored
			extDir.mkdirs();
		}
		return new File(extDir, file.getName());
	}

	/**
	 * Get a file for temporarily storing a Jpeg file.
	 *
	 * @return a non-existing Jpeg file in the cache dir.
	 */
//	public static File getTempJpegFile() {
//		File tempDir = getTempCameraFolder();
//		File tempFile;
//		do {
//			int tempFileCounter = incrementCounter(R.string.key_internal_counter_tempfiles);
//			tempFile = new File(tempDir, "tempFile_" + tempFileCounter + ".jpg");
//		}
//		while (tempFile.exists());
//		return tempFile;
//	}

	/**
	 * Get all temp files.
	 *
	 * @return The list of existing temp files.
	 */
	public static File[] getTempCameraFiles(final Context c) {
		File tempDir = getTempCameraFolder(c);

		File[] files = tempDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(@NonNull final File file) {
				return file.isFile();
			}
		});
		if (files == null) {
			files = new File[0];
		}
		Arrays.sort(files);

		return files;
	}

	/**
	 * Get the folder where temporary files from the camera are stored.
	 *
	 * @return The temp folder.
	 */
	@NonNull
	public static File getTempCameraFolder(final Context c) {
		File result = new File(c.getExternalCacheDir(), "Camera");
		if (!result.exists()) {
			//noinspection ResultOfMethodCallIgnored
			result.mkdirs();
		}
		return result;
	}

	
	/**
	 * Create a folder. The folder may even be on external SD card for Kitkat.
	 *
	 * @param file The folder to be created.
	 * @return True if creation was successful.
	 */
	public static boolean mkdir(@NonNull final File file, final Context c) {
		if (file.exists()) {
			// nothing to create.
			return file.isDirectory();
		}

		// Try the normal way
		if (file.mkdir()) {
			return true;
		}

		// Try with Storage Access Framework.
		if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
			DocumentFile document = getDocumentFile(file, true, true, c);
			// getDocumentFile implicitly creates the directory.
			return document != null && document.exists();
		}

		// Try the Kitkat workaround.
		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
			File tempFile = new File(file, "dummyImage.jpg");

			File dummySong = copyDummyFiles(c);
			if (dummySong == null) {
				return false;
			}
			int albumId = getAlbumIdFromAudioFile(dummySong, c);
			Uri albumArtUri = Uri.parse("content://media/external/audio/albumart/" + albumId);

			ContentValues contentValues = new ContentValues();
			contentValues.put(MediaStore.MediaColumns.DATA, tempFile.getAbsolutePath());
			contentValues.put(MediaStore.Audio.AlbumColumns.ALBUM_ID, albumId);

			ContentResolver resolver = c.getContentResolver();
			if (resolver.update(albumArtUri, contentValues, null, null) == 0) {
				resolver.insert(Uri.parse("content://media/external/audio/albumart"), contentValues);
			}
			try {
				ParcelFileDescriptor fd = resolver.openFileDescriptor(albumArtUri, "r");
				if (fd != null) {
					fd.close();
				}
			}
			catch (Exception e) {
				Log.e("Uri", "Could not open file", e);
				return false;
			}
			finally {
				AndroidPathUtils.deleteFile(tempFile, c);
			}

			return true;
		}

		return false;
	}

	/**
	 * Delete a folder.
	 *
	 * @param file The folder name.
	 * @return true if successful.
	 */
	public static boolean rmdir(@NonNull final File file, final Context c) {
		if (!file.exists()) {
			return true;
		}
		if (!file.isDirectory()) {
			return false;
		}
		String[] fileList = file.list();
		if (fileList != null && fileList.length > 0) {
			// Delete only empty folder.
			return false;
		}

		// Try the normal way
		if (file.delete()) {
			return true;
		}

		// Try with Storage Access Framework.
		if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
			DocumentFile document = getDocumentFile(file, true, true, c);
			return document != null && document.delete();
		}

		// Try the Kitkat workaround.
		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
			ContentResolver resolver = c.getContentResolver();
			ContentValues values = new ContentValues();
			values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
			resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

			// Delete the created entry, such that content provider will delete the file.
			resolver.delete(MediaStore.Files.getContentUri("external"), MediaStore.MediaColumns.DATA + "=?",
					new String[] {file.getAbsolutePath()});
		}

		return !file.exists();
	}

	/**
	 * Delete all files in a folder.
	 *
	 * @param folder the folder
	 * @return true if successful.
	 */
	public static boolean deleteFilesInFolder(@NonNull final File folder, final Context c) {
		boolean totalSuccess = true;

		String[] children = folder.list();
		if (children != null) {
			for (String child : children) {
				File file = new File(folder, child);
				if (!file.isDirectory()) {
					boolean success = AndroidPathUtils.deleteFile(file, c);
					if (!success) {
						Log.w("Uri", "Failed to delete file" + child);
						totalSuccess = false;
					}
				}
			}
		}
		return totalSuccess;
	}

	/**
	 * Delete a directory asynchronously.
	 *
	 * @param activity    The activity calling this method.
	 * @param file        The folder name.
	 * @param postActions Commands to be executed after success.
	 */
	public static void rmdirAsynchronously(@NonNull final Activity activity, @NonNull final File file, final Runnable postActions, final Context c) {
		new Thread() {
			@Override
			public void run() {
				int retryCounter = 5; // MAGIC_NUMBER
				while (!AndroidPathUtils.rmdir(file, c) && retryCounter > 0) {
					try {
						Thread.sleep(100); // MAGIC_NUMBER
					}
					catch (InterruptedException e) {
						// do nothing
					}
					retryCounter--;
				}
				if (file.exists()) {
//					DialogUtil.displayError(activity, R.string.message_dialog_failed_to_delete_folder, false,
//							file.getAbsolutePath());
				}
				else {
					activity.runOnUiThread(postActions);
				}

			}
		}.start();
	}

	/**
	 * Check is a file is writable. Detects write issues on external SD card.
	 *
	 * @param file The file
	 * @return true if the file is writable.
	 */
	public static boolean isWritable(@NonNull final File file) {
		boolean isExisting = file.exists();

		try {
			FileOutputStream output = new FileOutputStream(file, true);
			try {
				output.close();
			} catch (IOException e) {
				// do nothing.
			}
		} catch (FileNotFoundException e) {
			return false;
		}
		boolean result = file.canWrite();

		// Ensure that file is not created during this process.
		if (!isExisting) {
			//noinspection ResultOfMethodCallIgnored
			file.delete();
		}

		return result;
	}

	// Utility methods for Android 5

	/**
	 * Check for a directory if it is possible to create files within this directory, either via normal writing or via
	 * Storage Access Framework.
	 *
	 * @param folder The directory
	 * @return true if it is possible to write in this directory.
	 */
	public static boolean isWritableNormalOrSaf(@Nullable final File folder, Context c) {
		// Verify that this is a directory.
		if (folder == null || !folder.exists() || !folder.isDirectory()) {
			return false;
		}

		// Find a non-existing file in this directory.
		int i = 0;
		File file;
		do {
			String fileName = "AugendiagnoseDummyFile" + (++i);
			file = new File(folder, fileName);
		} while (file.exists());

		// First check regular writability
		if (isWritable(file)) {
			return true;
		}

		// Next check SAF writability.
		DocumentFile document = getDocumentFile(file, false, false, c);

		if (document == null) {
			return false;
		}

		// This should have created the file - otherwise something is wrong with access URL.
		boolean result = document.canWrite() && file.exists();

		// Ensure that the dummy file is not remaining.
		document.delete();

		return result;
	}

	/**
	 * Get the SD card directory.
	 *
	 * @return The SD card directory.
	 */
	@NonNull
	public static String getSdCardPath() {
		String sdCardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();

		try {
			sdCardDirectory = new File(sdCardDirectory).getCanonicalPath();
		} catch (IOException ioe) {
			Log.e("Uri", "Could not get SD directory", ioe);
		}
		return sdCardDirectory;
	}

	/**
	 * Get a list of external SD card paths. (Kitkat or higher.)
	 *
	 * @return A list of external SD card paths.
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	private static String[] getExtSdCardPaths(final Context c) {
		List<String> paths = new ArrayList<String>();
		for (File file : c.getExternalFilesDirs("external")) {
			if (file != null && !file.equals(c.getExternalFilesDir("external"))) {
				int index = file.getAbsolutePath().lastIndexOf("/Android/data");
				if (index < 0) {
					Log.w("Uri", "Unexpected external file dir: " + file.getAbsolutePath());
				} else {
					String path = file.getAbsolutePath().substring(0, index);
					try {
						path = new File(path).getCanonicalPath();
					} catch (IOException e) {
						// Keep non-canonical path.
					}
					paths.add(path);
				}
			}
		}
		return paths.toArray(new String[paths.size()]);
	}

	// Utility methods for Kitkat

	/**
	 * Copy a resource file into a private target directory, if the target does not yet exist. Required for the Kitkat
	 * workaround.
	 *
	 * @param resource   The resource file.
	 * @param folderName The folder below app folder where the file is copied to.
	 * @param targetName The name of the target file.
	 * @return the dummy file.
	 * @throws IOException thrown if there are issues while copying.
	 */
	private static File copyDummyFile(final int resource, final String folderName, @NonNull final String targetName, final Context c)
			throws IOException {
		File externalFilesDir = c.getExternalFilesDir(folderName);
		if (externalFilesDir == null) {
			return null;
		}
		File targetFile = new File(externalFilesDir, targetName);

		if (!targetFile.exists()) {
			InputStream in = null;
			OutputStream out = null;
			try {
				in = c.getResources().openRawResource(resource);
				out = new BufferedOutputStream(new FileOutputStream(targetFile));
				byte[] buffer = new byte[4096]; // MAGIC_NUMBER
				int bytesRead;
				while ((bytesRead = in.read(buffer)) != -1) {
					out.write(buffer, 0, bytesRead);
				}
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException ex) {
						// do nothing
					}
				}
				if (out != null) {
					try {
						out.close();
					} catch (IOException ex) {
						// do nothing
					}
				}
			}
		}
		return targetFile;
	}

	/**
	 * Copy the dummy image and dummy mp3 into the private folder, if not yet there. Required for the Kitkat workaround.
	 *
	 * @return the dummy mp3.
	 */
	private static File copyDummyFiles(final Context c) {
		try {
			copyDummyFile(R.raw.temptrack, "mkdirFiles", "albumart.jpg", c);
			return copyDummyFile(R.raw.temptrack, "mkdirFiles", "silence.mp3", c);
		} catch (IOException e) {
			Log.e("Uri", "Could not copy dummy files.", e);
			return null;
		}
	}

}
