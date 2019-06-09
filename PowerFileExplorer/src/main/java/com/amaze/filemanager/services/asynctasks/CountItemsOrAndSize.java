package com.amaze.filemanager.services.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.util.Pair;
import android.text.format.Formatter;
import android.widget.TextView;

import net.gnu.explorer.R;
import com.amaze.filemanager.filesystem.BaseFile;
import com.amaze.filemanager.utils.files.Futils;
import com.amaze.filemanager.utils.OnProgressUpdate;
import net.gnu.util.FileUtil;
import net.gnu.util.Util;
import java.util.*;
import com.amaze.filemanager.utils.*;

/**
 * @author Emmanuel
 *         on 12/5/2017, at 19:40.
 */

public class CountItemsOrAndSize extends AsyncTask<Void, Long, String> {

    private Context context;
    private TextView itemsText;
    private List<BaseFile> files;
    private boolean isStorage;

    public CountItemsOrAndSize(Context c, TextView itemsText, BaseFile f, boolean storage) {
        this.context = c;
        this.itemsText = itemsText;
        files = new LinkedList<>();
		files.add(f);
        isStorage = storage;
    }
	
    public CountItemsOrAndSize(Context c, TextView itemsText, List<BaseFile> f, boolean storage) {
        this.context = c;
        this.itemsText = itemsText;
        files = f;
        isStorage = storage;
    }

    @Override
    protected String doInBackground(Void[] params) {
        String items = "";
		
		if (isStorage) {
			long folderSize = files.get(0).getUsableSpace();
			items = Formatter.formatFileSize(context, folderSize) + (" (" + net.gnu.util.Util.nf.format(folderSize) + " "
				+ context.getResources().getQuantityString(R.plurals.bytes, (int) folderSize) //truncation is insignificant
					+ ")");
		} else {
			long[] folderSize = FileUtil.getFolderSize(files, context, false, new OnProgressUpdate3<Long>() {
						@Override
						public void onUpdate(Long[] data) {
							publishProgress(data);
						}
					});


			items = getText(new Long[]{Long.valueOf(folderSize[0]), Long.valueOf(folderSize[1]), Long.valueOf(folderSize[2])}, false);
//			} else {
//				fileLength = file.length(context);
//				items = Formatter.formatFileSize(context, fileLength) + (" (" + net.gnu.util.Util.nf.format(fileLength) + " "
//					+ context.getResources().getQuantityString(R.plurals.bytes, (int) fileLength) //truncation is insignificant
//					+ ")");
//			}
        }

        return items;
    }

    @Override
    protected void onProgressUpdate(Long... dataArr) {
        itemsText.setText(getText(dataArr, true));
    }

    private String getText(Long[] filesInFolder, boolean loading) {
        String numOfItems = net.gnu.util.Util.nf.format(filesInFolder[2]) + " folder, " 
			+ net.gnu.util.Util.nf.format(filesInFolder[1]) + " files, " 
			+ net.gnu.util.Util.nf.format(filesInFolder[0]) + " bytes";

        return numOfItems + " " + (loading? ">":"");
    }

    protected void onPostExecute(String items) {
        itemsText.setText(items);
    }
}
