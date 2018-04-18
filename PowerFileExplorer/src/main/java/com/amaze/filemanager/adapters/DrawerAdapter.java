/*
 * Copyright (C) 2014 Arpit Khurana <arpitkh96@gmail.com>, Vishal Nehra <vishalmeham2@gmail.com>
 *
 * This file is part of Amaze File Manager.
 *
 * Amaze File Manager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.amaze.filemanager.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.amaze.filemanager.activities.ThemedActivity;
import com.amaze.filemanager.database.CloudHandler;
import com.amaze.filemanager.filesystem.HFile;
import com.amaze.filemanager.filesystem.Operations;
import com.amaze.filemanager.filesystem.RootHelper;
import com.amaze.filemanager.ui.dialogs.GeneralDialogCreation;
import com.amaze.filemanager.ui.drawer.EntryItem;
import com.amaze.filemanager.ui.drawer.Item;
import com.amaze.filemanager.utils.DataUtils;
import com.amaze.filemanager.utils.OpenMode;
import com.amaze.filemanager.utils.Utils;
import com.amaze.filemanager.utils.cloud.CloudUtil;
import com.amaze.filemanager.utils.color.ColorUsage;
import com.amaze.filemanager.utils.provider.UtilitiesProviderInterface;
import com.amaze.filemanager.utils.theme.AppTheme;
import java.io.File;
import java.util.ArrayList;
import net.gnu.explorer.ExplorerActivity;
import net.gnu.explorer.R;
import android.content.res.TypedArray;

public class DrawerAdapter extends ArrayAdapter<Item> {
    private final Context context;
    private UtilitiesProviderInterface utilsProvider;
    private final ArrayList<Item> values;
    private ExplorerActivity m;
    private SparseBooleanArray myChecked = new SparseBooleanArray();
    private DataUtils dataUtils = DataUtils.getInstance();

    public void toggleChecked(int position) {
        toggleChecked(false);
        myChecked.put(position, true);
        notifyDataSetChanged();
    }

    public void toggleChecked(boolean b) {
        for (int i = 0; i < values.size(); i++) {
            myChecked.put(i, b);
        }
        notifyDataSetChanged();
    }

    private LayoutInflater inflater;
	
    public DrawerAdapter(Context context, UtilitiesProviderInterface utilsProvider,
                         ArrayList<Item> values, ExplorerActivity m, SharedPreferences Sp) {
        super(context, R.layout.drawerrow, values);
        this.utilsProvider = utilsProvider;

        this.context = context;
        this.values = values;

        for (int i = 0; i < values.size(); i++) {
            myChecked.put(i, false);
        }
        this.m = m;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);//LayoutInflater.from(context)

    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {
        final AppTheme appTheme = utilsProvider.getAppTheme();
		final Item item = values.get(position);
		if (item.isSection()) {
            final ImageView view = new ImageView(context);
            if (appTheme.equals(AppTheme.LIGHT)) {
                view.setImageResource(R.color.divider);
            } else {
                view.setImageResource(R.color.divider_dark);
				//view.setBackgroundResource(R.color.background_material_dark);
			}
			view.setBackgroundColor(ExplorerActivity.TEXT_COLOR);
            view.setClickable(false);
            view.setFocusable(false);

            view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPx(m, 1)));
            //view.setPadding(0, Utils.dpToPx(m, 8), 0, Utils.dpToPx(m, 8));
            return view;
        } else {
            convertView = inflater.inflate(R.layout.drawerrow, parent, false);
            final TextView txtTitle = (TextView) convertView.findViewById(R.id.firstline);
            final ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);
            if (appTheme.equals(AppTheme.LIGHT)) {
                convertView.setBackgroundResource(R.drawable.safr_ripple_white);
            } else {
                convertView.setBackgroundResource(R.drawable.safr_ripple_black);
            }
            convertView.setOnClickListener(new View.OnClickListener() {

					public void onClick(final View v) {
						final EntryItem item = (EntryItem) getItem(position);

						final String title = item.getTitle();
						final String path = item.getPath();

						if (dataUtils.containsBooks(new String[]{title, path}) != -1) {
							checkForPath(path);
						}

						if (dataUtils.getAccounts().size() > 0 && 
							(path.startsWith(CloudHandler.CLOUD_PREFIX_BOX) ||
							path.startsWith(CloudHandler.CLOUD_PREFIX_DROPBOX) ||
							path.startsWith(CloudHandler.CLOUD_PREFIX_ONE_DRIVE) ||
							path.startsWith(CloudHandler.CLOUD_PREFIX_GOOGLE_DRIVE))) {
							// we have cloud accounts, try see if token is expired or not
							CloudUtil.checkToken(path, m);
						}
						m.selectItem(position);
					}
					// TODO: Implement this method

				});
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(final View v) {
						final Item it = getItem(position);
						if (!it.isSection())
                        // not to remove the first bookmark (storage) and permanent bookmarks
							if (position > m.storage_count && position < values.size() - 7) {
								final EntryItem item = (EntryItem) it;
								final String title = item.getTitle();
								final String path = item.getPath();
								if (dataUtils.containsBooks(new String[]{title, path}) != -1) {
									m.renameBookmark(title, path);
								} else if (path.startsWith("smb:/")) {
									m.showSMBDialog(title, path, true);
								} else if (path.startsWith(CloudHandler.CLOUD_PREFIX_DROPBOX)) {
									GeneralDialogCreation.showCloudDialog(m, utilsProvider.getAppTheme(), OpenMode.DROPBOX);
								} else if (path.startsWith(CloudHandler.CLOUD_PREFIX_GOOGLE_DRIVE)) {
									GeneralDialogCreation.showCloudDialog(m, utilsProvider.getAppTheme(), OpenMode.GDRIVE);
								} else if (path.startsWith(CloudHandler.CLOUD_PREFIX_BOX)) {
									GeneralDialogCreation.showCloudDialog(m, utilsProvider.getAppTheme(), OpenMode.BOX);
								} else if (path.startsWith(CloudHandler.CLOUD_PREFIX_ONE_DRIVE)) {
									GeneralDialogCreation.showCloudDialog(m, utilsProvider.getAppTheme(), OpenMode.ONEDRIVE);
								}
							} else if (position < m.storage_count) {
								final String path = ((EntryItem) it).getPath();
								if (!path.equals("/"))
									GeneralDialogCreation.showPropertiesDialogForStorage(RootHelper.generateBaseFile(new File(path), true), m, utilsProvider.getAppTheme());
							}

						// return true to denote no further processing
						return true;
					}
				});

            txtTitle.setText(((EntryItem) item).getTitle());
            imageView.setImageDrawable(getDrawable(position));
            imageView.clearColorFilter();
			
            if (myChecked.get(position)) {
                final int accentColor = m.getColorPreference().getColor(ColorUsage.ACCENT);
                //if (utilsProvider.getAppTheme().equals(AppTheme.LIGHT)) {
				convertView.setBackgroundColor(ExplorerActivity.IN_DATA_SOURCE_2);
                //} else {
				//convertView.setBackgroundColor(Color.parseColor("#ff424242"));
                //}
                imageView.setColorFilter(accentColor);
                txtTitle.setTextColor(accentColor);
            } else {
                //if (utilsProvider.getAppTheme().equals(AppTheme.LIGHT)) {
				imageView.setColorFilter(ExplorerActivity.TEXT_COLOR);
				txtTitle.setTextColor(ExplorerActivity.TEXT_COLOR);
				convertView.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
                //} else {
				//imageView.setColorFilter(Color.WHITE);
				//txtTitle.setTextColor(Utils.getColor(m, android.R.color.white));
                //}
            }

            return convertView;
        }
    }

    /**
     * Checks whether path for bookmark exists
     * If path is not found, empty directory is created
     *
     * @param path
     */
    private void checkForPath(String path) {
        // TODO: Add support for SMB and OTG in this function
        if (!new File(path).exists()) {
            Toast.makeText(getContext(), getContext().getString(R.string.bookmark_lost), Toast.LENGTH_SHORT).show();
            Operations.mkdir(RootHelper.generateBaseFile(new File(path), true), getContext(),
				ThemedActivity.rootMode, new Operations.ErrorCallBack() {
					//TODO empty
					@Override
					public void exists(HFile file) {

					}

					@Override
					public void launchSAF(HFile file) {

					}

					@Override
					public void launchSAF(HFile file, HFile file1) {

					}

					@Override
					public void done(HFile hFile, boolean b) {

					}

					@Override
					public void invalidName(HFile file) {

					}
				});
        }
    }

    private Drawable getDrawable(int position) {
        return ((EntryItem) getItem(position)).getIcon();
    }
}
