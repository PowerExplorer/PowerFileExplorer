package com.amaze.filemanager.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.amaze.filemanager.filesystem.BaseFile;
import com.amaze.filemanager.filesystem.RootHelper;

import java.util.ArrayList;
import net.gnu.explorer.ExplorerActivity;
import android.os.AsyncTask;
import com.amaze.filemanager.ui.LayoutElement;
import java.util.List;
import net.gnu.explorer.ContentFragment;

/**
 * Created by Vishal on 27-04-2017.
 */

public class OTGUtil {

    public static final String PREFIX_OTG = "otg:/";


    /**
     * Returns an array of list of files at a specific path in OTG
     *
     * @param path    the path to the directory tree, starts with prefix 'otg:/'
     *                Independent of URI (or mount point) for the OTG
     * @param context context for loading
     * @return an array of list of files at the path
     */
    public static ArrayList<BaseFile> getDocumentFilesList(final String path, final Context context, final ContentFragment.LoadFiles updater) {
        final SharedPreferences manager = PreferenceManager.getDefaultSharedPreferences(context);
        final String rootUriString = manager.getString(ExplorerActivity.KEY_PREF_OTG, null);
        DocumentFile rootUri = DocumentFile.fromTreeUri(context, Uri.parse(rootUriString));
        ArrayList<BaseFile> files = new ArrayList<>(1024);

        final String[] parts = path.split("/");
        for (int i = 0; i < parts.length; i++) {

            // first omit 'otg:/' before iterating through DocumentFile
            if (path.equals(OTGUtil.PREFIX_OTG + "/")) break;
            if (parts[i].equals("otg:") || parts[i].equals("")) continue;
            Log.d(context.getClass().getSimpleName(), "Currently at: " + parts[i]);
            // iterating through the required path to find the end point
            rootUri = rootUri.findFile(parts[i]);
        }

		long prevUpdate = System.currentTimeMillis();
		Log.d(context.getClass().getSimpleName(), "Found URI for: " + rootUri.getName());
        // we have the end point DocumentFile, list the files inside it and return
        for (DocumentFile file : rootUri.listFiles()) {
            try {
                if (file.exists()) {
                    long size = 0;
                    if (!file.isDirectory()) size = file.length();
                    Log.d(context.getClass().getSimpleName(), "Found file: " + file.getName());
                    BaseFile baseFile = new BaseFile(path + "/" + file.getName(),
                            RootHelper.parseDocumentFilePermission(file), file.lastModified(), size, file.isDirectory());
                    baseFile.setName(file.getName());
                    baseFile.setMode(OpenMode.OTG);
                    files.add(baseFile);
					final long present = System.currentTimeMillis();
					if (updater != null && present - prevUpdate > 1000 && !updater.busyNoti) {
						prevUpdate = present;
						updater.publish(files);
						files = new ArrayList<>(1024);
					}
                }
            } catch (Exception e) {
            }
        }
		if (updater != null) {
			updater.publish(files);
		}
        return files;
    }

    /**
     * Traverse to a specified path in OTG
     *
     * @param path
     * @param context
     * @param createRecursive flag used to determine whether to create new file while traversing to path,
     *                        in case path is not present. Notably useful in opening an output stream.
     * @return
     */
    public static DocumentFile getDocumentFile(String path, Context context, boolean createRecursive) {
        SharedPreferences manager = PreferenceManager.getDefaultSharedPreferences(context);
        String rootUriString = manager.getString(ExplorerActivity.KEY_PREF_OTG, null);

        // start with root of SD card and then parse through document tree.
        DocumentFile rootUri = DocumentFile.fromTreeUri(context, Uri.parse(rootUriString));

        String[] parts = path.split("/");
        for (int i = 0; i < parts.length; i++) {

            if (path.equals("otg:/")) break;
            if (parts[i].equals("otg:") || parts[i].equals("")) continue;
            Log.d(context.getClass().getSimpleName(), "Currently at: " + parts[i]);
            // iterating through the required path to find the end point

            DocumentFile nextDocument = rootUri.findFile(parts[i]);
            if (createRecursive) {
                if (nextDocument == null || !nextDocument.exists()) {
                    nextDocument = rootUri.createFile(parts[i].substring(parts[i].lastIndexOf(".")), parts[i]);
                    Log.d(context.getClass().getSimpleName(), "NOT FOUND! File created: " + parts[i]);
                }
            }
            rootUri = nextDocument;
        }
        return rootUri;
    }
}
