package com.amaze.filemanager.ui.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import net.gnu.explorer.R;
import com.amaze.filemanager.activities.BasicActivity;
import com.amaze.filemanager.activities.ThemedActivity;
import com.amaze.filemanager.adapters.HiddenAdapter;
import com.amaze.filemanager.exceptions.CryptException;
import com.amaze.filemanager.exceptions.RootNotPermittedException;
import com.amaze.filemanager.filesystem.BaseFile;
import com.amaze.filemanager.filesystem.HFile;
import com.amaze.filemanager.filesystem.RootHelper;
//import com.amaze.filemanager.fragments.AppsList;
import com.amaze.filemanager.fragments.preference_fragments.Preffrag;
import com.amaze.filemanager.services.asynctasks.CountItemsOrAndSize;
import com.amaze.filemanager.services.asynctasks.GenerateHashes;
import com.amaze.filemanager.services.asynctasks.LoadFolderSpaceData;
import com.amaze.filemanager.ui.LayoutElement;
import com.amaze.filemanager.utils.DataUtils;
import com.amaze.filemanager.utils.FingerprintHandler;
import com.amaze.filemanager.utils.OpenMode;
import com.amaze.filemanager.utils.Utils;
import com.amaze.filemanager.utils.color.ColorUsage;
import com.amaze.filemanager.utils.files.CryptUtil;
import com.amaze.filemanager.utils.files.EncryptDecryptUtils;
import com.amaze.filemanager.utils.files.Futils;
import com.amaze.filemanager.utils.theme.AppTheme;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.chainfire.libsuperuser.Shell;

import static android.os.Build.VERSION_CODES.M;
import static com.amaze.filemanager.utils.files.Futils.toHFileArray;
import net.gnu.explorer.ExplorerActivity;
import net.gnu.explorer.Frag;
import net.gnu.explorer.ContentFragment;
import com.amaze.filemanager.services.DeleteTask;
import com.amaze.filemanager.utils.MainActivityHelper;
import net.gnu.p7zip.ZipEntry;
import net.gnu.p7zip.Zip;
import net.gnu.explorer.ExplorerApplication;
import net.gnu.p7zip.DecompressTask;
import android.util.Log;
import net.gnu.util.Util;

/**
 * Here are a lot of function that create material dialogs
 *
 * @author Emmanuel
 *         on 17/5/2017, at 13:27.
 */

public class GeneralDialogCreation {

    public static MaterialDialog showBasicDialog(BasicActivity m, String[] texts) {
        int accentColor = m.getColorPreference().getColor(ColorUsage.ACCENT);
        MaterialDialog.Builder a = new MaterialDialog.Builder(m)
			.content(texts[0])
			.widgetColor(accentColor)
			.theme(m.getAppTheme().getMaterialDialogTheme())
			.title(texts[1])
			.positiveText(texts[2])
			.positiveColor(accentColor)
			.negativeText(texts[3])
			.negativeColor(accentColor);
        if (texts[4]!=(null)) {
            a.neutralText(texts[4]).neutralColor(accentColor);
        }
        return a.build();
    }

    public static MaterialDialog showNameDialog(final ExplorerActivity m, String[] texts) {
        int accentColor = m.getColorPreference().getColor(ColorUsage.ACCENT);
        MaterialDialog.Builder a = new MaterialDialog.Builder(m);
        a.input(texts[0], texts[1], false,
			new MaterialDialog.InputCallback() {
				@Override
				public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {

				}
			});
        a.widgetColor(accentColor);

        a.theme(m.getAppTheme().getMaterialDialogTheme());
        a.title(texts[2]);

        a.positiveText(texts[3]);

        if (texts[4]!=null) {
            a.neutralText(texts[4]);
        }

        if (texts[5]!=null) {
            a.negativeText(texts[5]);
            a.negativeColor(accentColor);
        }
        return a.build();
    }

    @SuppressWarnings("ConstantConditions")
    public static void deleteFilesDialog(final Context c, //final ArrayList<LayoutElement> layoutElements,
                                         final ThemedActivity mainActivity, final List<LayoutElement> positions,
                                         final AppTheme appTheme, final Runnable run) {

        final ArrayList<BaseFile> itemsToDelete = new ArrayList<>();
        int accentColor = mainActivity.getColorPreference().getColor(ColorUsage.ACCENT);

        // Build dialog with custom view layout and accent color.
        MaterialDialog dialog = new MaterialDialog.Builder(c)
			.title(c.getString(R.string.dialog_delete_title))
			.customView(R.layout.dialog_delete, true)
			.theme(appTheme.getMaterialDialogTheme())
			.negativeText(c.getString(R.string.cancel).toUpperCase())
			.positiveText(c.getString(R.string.delete).toUpperCase())
			.positiveColor(accentColor)
			.negativeColor(accentColor)
			.onPositive(new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
					Toast.makeText(c, c.getString(R.string.deleting), Toast.LENGTH_SHORT).show();
					//mainActivity.mainActivityHelper.deleteFiles(itemsToDelete);
					if (itemsToDelete == null || itemsToDelete.size() == 0) return;
					if (itemsToDelete.get(0).isSmb()) {
						new DeleteTask(mainActivity, run).execute((itemsToDelete));
						return;
					}
					int mode = MainActivityHelper.checkFolder(new File(itemsToDelete.get(0).getPath()).getParentFile(), mainActivity);
					if (mode == 2) {
						mainActivity.originPaths_oparrayList = (itemsToDelete);
						mainActivity.operation = DataUtils.DELETE;
						mainActivity.callback = run;
					} else if (mode == 1 || mode == 0)
						new DeleteTask(mainActivity, run).execute((itemsToDelete));
					else 
						Toast.makeText(mainActivity, R.string.not_allowed, Toast.LENGTH_SHORT).show();
				}
			})
			.build();

        // Get views from custom layout to set text values.
        final TextView categoryDirectories = (TextView) dialog.getCustomView().findViewById(R.id.category_directories);
        final TextView categoryFiles = (TextView) dialog.getCustomView().findViewById(R.id.category_files);
        final TextView listDirectories = (TextView) dialog.getCustomView().findViewById(R.id.list_directories);
        final TextView listFiles = (TextView) dialog.getCustomView().findViewById(R.id.list_files);
        final TextView total = (TextView) dialog.getCustomView().findViewById(R.id.total);

        // Parse items to delete.

        new AsyncTask<Void, Object, Void>() {

            long sizeTotal = 0;
            final StringBuilder files = new StringBuilder();
            final StringBuilder directories = new StringBuilder();
            int counterDirectories = 0;
            int counterFiles = 0;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                listFiles.setText(c.getString(R.string.loading));
                listDirectories.setText(c.getString(R.string.loading));
                total.setText(c.getString(R.string.loading));
            }

            @Override
            protected Void doInBackground(Void... params) {

                for (int i = 0; i<positions.size(); i++) {
                    final LayoutElement layoutElement = positions.get(i);
                    itemsToDelete.add(layoutElement.generateBaseFile());

                    // Build list of directories to delete.
                    if (layoutElement.isDirectory) {
                        // Don't add newline between category and list.
                        if (counterDirectories!=0) {
                            directories.append("\n");
                        }

                        long sizeDirectory = layoutElement.generateBaseFile().folderSize(c);

                        directories.append(++counterDirectories)
							.append(". ")
							.append(layoutElement.name)
							.append(" (")
							.append(net.gnu.util.Util.nf.format(sizeDirectory))
							.append(" B)");
                        sizeTotal += sizeDirectory;
                        // Build list of files to delete.
                    } else {
                        // Don't add newline between category and list.
                        if (counterFiles!=0) {
                            files.append("\n");
                        }

                        files.append(++counterFiles)
							.append(". ")
							.append(layoutElement.name)
							.append(" (")
							.append(net.gnu.util.Util.nf.format(layoutElement.length))
							.append(" B)");
                        sizeTotal += layoutElement.length;
                    }

                    publishProgress(sizeTotal, counterFiles, counterDirectories, files, directories);
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Object... result) {
                super.onProgressUpdate(result);

                int tempCounterFiles = (int) result[1];
                int tempCounterDirectories = (int) result[2];
                long tempSizeTotal = (long) result[0];
                StringBuilder tempFilesStringBuilder = (StringBuilder) result[3];
                StringBuilder tempDirectoriesStringBuilder = (StringBuilder) result[4];

                updateViews(tempSizeTotal, tempFilesStringBuilder, tempDirectoriesStringBuilder,
							tempCounterFiles, tempCounterDirectories);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                updateViews(sizeTotal, files, directories, counterFiles, counterDirectories);
            }

            private void updateViews(long tempSizeTotal, StringBuilder filesStringBuilder,
                                     StringBuilder directoriesStringBuilder, int... values) {

                int tempCounterFiles = values[0];
                int tempCounterDirectories = values[1];

                // Hide category and list for directories when zero.
                if (tempCounterDirectories==0) {

                    if (tempCounterDirectories==0) {

                        categoryDirectories.setVisibility(View.GONE);
                        listDirectories.setVisibility(View.GONE);
                    }
                    // Hide category and list for files when zero.
                }

                if (tempCounterFiles==0) {


                    categoryFiles.setVisibility(View.GONE);
                    listFiles.setVisibility(View.GONE);
                }

                if (tempCounterDirectories!=0||tempCounterFiles!=0) {
                    listDirectories.setText(directoriesStringBuilder);
                    if (listDirectories.getVisibility()!=View.VISIBLE&&tempCounterDirectories!=0)
                        listDirectories.setVisibility(View.VISIBLE);
                    listFiles.setText(filesStringBuilder);
                    if (listFiles.getVisibility()!=View.VISIBLE&&tempCounterFiles!=0)
                        listFiles.setVisibility(View.VISIBLE);

                    if (categoryDirectories.getVisibility()!=View.VISIBLE&&tempCounterDirectories!=0)
                        categoryDirectories.setVisibility(View.VISIBLE);
                    if (categoryFiles.getVisibility()!=View.VISIBLE&&tempCounterFiles!=0)
                        categoryFiles.setVisibility(View.VISIBLE);
                }

                // Show total size with at least one directory or file and size is not zero.
                if (tempCounterFiles+tempCounterDirectories>1&&tempSizeTotal>0) {
                    StringBuilder builderTotal = new StringBuilder()
						.append(c.getString(R.string.total))
						.append(" ")
						.append(Formatter.formatFileSize(c, tempSizeTotal));
                    total.setText(builderTotal);
                    if (total.getVisibility()!=View.VISIBLE)
                        total.setVisibility(View.VISIBLE);
                } else {
                    total.setVisibility(View.GONE);
                }
            }
        }.execute();

        // Set category text color for Jelly Bean (API 16) and later.
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
            categoryDirectories.setTextColor(accentColor);
            categoryFiles.setTextColor(accentColor);
        }

        // Show dialog on screen.
        dialog.show();
    }

    @SuppressWarnings("ConstantConditions")
    public static void deleteFilesDialog(final Context c, //final ArrayList<LayoutElement> layoutElements,
                                         final ThemedActivity mainActivity, final Zip zip, final List<ZipEntry> itemsToDelete,
                                         AppTheme appTheme, final Runnable rr) {

        //final ArrayList<ZipEntry> itemsToDelete = new ArrayList<>();
        int accentColor = mainActivity.getColorPreference().getColor(ColorUsage.ACCENT);

        // Build dialog with custom view layout and accent color.
        MaterialDialog dialog = new MaterialDialog.Builder(c)
			.title(c.getString(R.string.dialog_delete_title))
			.customView(R.layout.dialog_delete, true)
			.theme(appTheme.getMaterialDialogTheme())
			.negativeText(c.getString(R.string.cancel).toUpperCase())
			.positiveText(c.getString(R.string.delete).toUpperCase())
			.positiveColor(accentColor)
			.negativeColor(accentColor)
			.onPositive(new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
					Toast.makeText(c, c.getString(R.string.deleting), Toast.LENGTH_SHORT).show();
					//mainActivity.mainActivityHelper.deleteFiles(itemsToDelete);
					if (itemsToDelete == null || itemsToDelete.size() == 0) return;
					int mode = MainActivityHelper.checkFolder(zip.file.getParentFile(), mainActivity);
					if (mode == 2) {
						mainActivity.filesInZip = (itemsToDelete);
						mainActivity.operation = DataUtils.DELETE_IN_ZIP;
						mainActivity.zip = zip;
					} else if (mode == 1 || mode == 0) {
						Runnable r = new Runnable() {
							@Override
							public void run() {
								if (rr != null) {
									rr.run();
								}
								Toast.makeText(mainActivity, "Deletion finished", Toast.LENGTH_SHORT).show();
							}
						};
						final StringBuilder sb = new StringBuilder();
						for (ZipEntry ze : itemsToDelete) {
							sb.append(ze.path).append("\n");
						}
						new DecompressTask(mainActivity,
										   zip.file.getAbsolutePath(),
										   ExplorerApplication.PRIVATE_PATH,
										   sb.toString(),
										   "",
										   "",
										   "",
										   0,
										   "d",
										   r).execute();
					} else 
						Toast.makeText(mainActivity, R.string.not_allowed, Toast.LENGTH_SHORT).show();
				}
			})
			.build();

        // Get views from custom layout to set text values.
        final TextView categoryDirectories = (TextView) dialog.getCustomView().findViewById(R.id.category_directories);
        final TextView categoryFiles = (TextView) dialog.getCustomView().findViewById(R.id.category_files);
        final TextView listDirectories = (TextView) dialog.getCustomView().findViewById(R.id.list_directories);
        final TextView listFiles = (TextView) dialog.getCustomView().findViewById(R.id.list_files);
        final TextView total = (TextView) dialog.getCustomView().findViewById(R.id.total);

        // Parse items to delete.

        new AsyncTask<Void, Object, Void>() {

            long sizeTotal = 0;
            final StringBuilder files = new StringBuilder();
            final StringBuilder directories = new StringBuilder();
            int counterDirectories = 0;
            int counterFiles = 0;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                listFiles.setText(c.getString(R.string.loading));
                listDirectories.setText(c.getString(R.string.loading));
                total.setText(c.getString(R.string.loading));
            }

            @Override
            protected Void doInBackground(Void... params) {

                for (ZipEntry layoutElement : itemsToDelete) {
                    
                    // Build list of directories to delete.
                    if (layoutElement.isDirectory) {
                        // Don't add newline between category and list.
                        if (counterDirectories!=0) {
                            directories.append("\n");
                        }

                        long sizeDirectory = zip.folderSize(layoutElement)[0];

                        directories.append(++counterDirectories)
							.append(". ")
							.append(layoutElement.name)
							.append(" (")
							.append(net.gnu.util.Util.nf.format(sizeDirectory))
							.append(" B)");
                        sizeTotal += sizeDirectory;
                        // Build list of files to delete.
                    } else {
                        // Don't add newline between category and list.
                        if (counterFiles!=0) {
                            files.append("\n");
                        }

                        files.append(++counterFiles)
							.append(". ")
							.append(layoutElement.name)
							.append(" (")
							.append(net.gnu.util.Util.nf.format(layoutElement.length))
							.append(" B)");
                        sizeTotal += layoutElement.length;
                    }

                    publishProgress(sizeTotal, counterFiles, counterDirectories, files, directories);
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Object... result) {
                super.onProgressUpdate(result);

                int tempCounterFiles = (int) result[1];
                int tempCounterDirectories = (int) result[2];
                long tempSizeTotal = (long) result[0];
                StringBuilder tempFilesStringBuilder = (StringBuilder) result[3];
                StringBuilder tempDirectoriesStringBuilder = (StringBuilder) result[4];

                updateViews(tempSizeTotal, tempFilesStringBuilder, tempDirectoriesStringBuilder,
							tempCounterFiles, tempCounterDirectories);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                updateViews(sizeTotal, files, directories, counterFiles, counterDirectories);
            }

            private void updateViews(long tempSizeTotal, StringBuilder filesStringBuilder,
                                     StringBuilder directoriesStringBuilder, int... values) {

                int tempCounterFiles = values[0];
                int tempCounterDirectories = values[1];

                // Hide category and list for directories when zero.
                if (tempCounterDirectories==0) {

                    if (tempCounterDirectories==0) {

                        categoryDirectories.setVisibility(View.GONE);
                        listDirectories.setVisibility(View.GONE);
                    }
                    // Hide category and list for files when zero.
                }

                if (tempCounterFiles==0) {


                    categoryFiles.setVisibility(View.GONE);
                    listFiles.setVisibility(View.GONE);
                }

                if (tempCounterDirectories!=0||tempCounterFiles!=0) {
                    listDirectories.setText(directoriesStringBuilder);
                    if (listDirectories.getVisibility()!=View.VISIBLE&&tempCounterDirectories!=0)
                        listDirectories.setVisibility(View.VISIBLE);
                    listFiles.setText(filesStringBuilder);
                    if (listFiles.getVisibility()!=View.VISIBLE&&tempCounterFiles!=0)
                        listFiles.setVisibility(View.VISIBLE);

                    if (categoryDirectories.getVisibility()!=View.VISIBLE&&tempCounterDirectories!=0)
                        categoryDirectories.setVisibility(View.VISIBLE);
                    if (categoryFiles.getVisibility()!=View.VISIBLE&&tempCounterFiles!=0)
                        categoryFiles.setVisibility(View.VISIBLE);
                }

                // Show total size with at least one directory or file and size is not zero.
                if (tempCounterFiles+tempCounterDirectories>1&&tempSizeTotal>0) {
                    StringBuilder builderTotal = new StringBuilder()
						.append(c.getString(R.string.total))
						.append(" ")
						.append(Formatter.formatFileSize(c, tempSizeTotal));
                    total.setText(builderTotal);
                    if (total.getVisibility()!=View.VISIBLE)
                        total.setVisibility(View.VISIBLE);
                } else {
                    total.setVisibility(View.GONE);
                }
            }
        }.execute();

        // Set category text color for Jelly Bean (API 16) and later.
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
            categoryDirectories.setTextColor(accentColor);
            categoryFiles.setTextColor(accentColor);
        }

        // Show dialog on screen.
        dialog.show();
    }

    public static void showPropertiesDialogWithPermissions(BaseFile baseFile, final String permissions,
                                                           ThemedActivity activity, boolean isRoot, AppTheme appTheme) {
        showPropertiesDialog(baseFile, permissions, activity, isRoot, appTheme, true, false);
    }

    public static void showPropertiesDialogWithoutPermissions(final BaseFile f, ThemedActivity activity, AppTheme appTheme) {
        showPropertiesDialog(f, null, activity, false, appTheme, false, false);
    }
    public static void showPropertiesDialogForStorage(final BaseFile f, ThemedActivity activity, AppTheme appTheme) {
        showPropertiesDialog(f, null, activity, false, appTheme, false, true);
    }

    private static void showPropertiesDialog(final BaseFile baseFile, final String permissions,
                                             ThemedActivity base, boolean isRoot, AppTheme appTheme,
                                             boolean showPermissions, boolean forStorage) {
        final ExecutorService executor = Executors.newFixedThreadPool(3);
        final Context ctx = base.getApplicationContext();
        int accentColor = base.getColorPreference().getColor(ColorUsage.ACCENT);
        long last = baseFile.getDate();
        final String date = Utils.getDate(last),
			items = ctx.getString(R.string.calculating),
			name  = baseFile.getName(),
			parent = baseFile.getReadablePath(baseFile.getParent(ctx));

        MaterialDialog.Builder builder = new MaterialDialog.Builder(base);
        builder.title(ctx.getString(R.string.properties));
        builder.theme(appTheme.getMaterialDialogTheme());

        View view = base.getLayoutInflater().inflate(R.layout.properties_dialog, null);
        TextView itemsText = (TextView) view.findViewById(R.id.size);

        /*View setup*/ {
            TextView mNameTitle = (TextView) view.findViewById(R.id.title_name);
            mNameTitle.setTextColor(accentColor);

            TextView mDateTitle = (TextView) view.findViewById(R.id.title_date);
            mDateTitle.setTextColor(accentColor);

            TextView mSizeTitle = (TextView) view.findViewById(R.id.title_size);
            mSizeTitle.setTextColor(accentColor);

            TextView mLocationTitle = (TextView) view.findViewById(R.id.title_location);
            mLocationTitle.setTextColor(accentColor);

            TextView md5Title = (TextView) view.findViewById(R.id.title_md5);
            md5Title.setTextColor(accentColor);

            TextView sha256Title = (TextView) view.findViewById(R.id.title_sha256);
            sha256Title.setTextColor(accentColor);

            ((TextView) view.findViewById(R.id.name)).setText(name);
            ((TextView) view.findViewById(R.id.location)).setText(parent);
            itemsText.setText(items);
            ((TextView) view.findViewById(R.id.date)).setText(date);

            LinearLayout mNameLinearLayout = (LinearLayout) view.findViewById(R.id.properties_dialog_name);
            LinearLayout mLocationLinearLayout = (LinearLayout) view.findViewById(R.id.properties_dialog_location);
            LinearLayout mSizeLinearLayout = (LinearLayout) view.findViewById(R.id.properties_dialog_size);
            LinearLayout mDateLinearLayout = (LinearLayout) view.findViewById(R.id.properties_dialog_date);

            // setting click listeners for long press
            mNameLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						Futils.copyToClipboard(ctx, name);
						Toast.makeText(ctx, ctx.getResources().getString(R.string.name)+" "+
									   ctx.getResources().getString(R.string.properties_copied_clipboard), Toast.LENGTH_SHORT).show();
						return false;
					}
				});
            mLocationLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						Futils.copyToClipboard(ctx, parent);
						Toast.makeText(ctx, ctx.getResources().getString(R.string.location)+" "+
									   ctx.getResources().getString(R.string.properties_copied_clipboard), Toast.LENGTH_SHORT).show();
						return false;
					}
				});
            mSizeLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						Futils.copyToClipboard(ctx, items);
						Toast.makeText(ctx, ctx.getResources().getString(R.string.size)+" "+
									   ctx.getResources().getString(R.string.properties_copied_clipboard), Toast.LENGTH_SHORT).show();
						return false;
					}
				});
            mDateLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						Futils.copyToClipboard(ctx, date);
						Toast.makeText(ctx, ctx.getResources().getString(R.string.date)+" "+
									   ctx.getResources().getString(R.string.properties_copied_clipboard), Toast.LENGTH_SHORT).show();
						return false;
					}
				});
        }

        CountItemsOrAndSize countItemsOrAndSize = new CountItemsOrAndSize(ctx, itemsText, baseFile, forStorage);
        countItemsOrAndSize.executeOnExecutor(executor);


        GenerateHashes hashGen = new GenerateHashes(baseFile, ctx, view);
        hashGen.executeOnExecutor(executor);

        /*Chart creation and data loading*/ {
            boolean isRightToLeft = ctx.getResources().getBoolean(R.bool.is_right_to_left);
            boolean isDarkTheme = appTheme.getMaterialDialogTheme()==Theme.DARK;
            PieChart chart = (PieChart) view.findViewById(R.id.chart);

            chart.setTouchEnabled(false);
            chart.setDrawEntryLabels(false);
            chart.setDescription(null);
            chart.setNoDataText(ctx.getString(R.string.loading));
            chart.setRotationAngle(!isRightToLeft? 0f:180f);
            chart.setHoleColor(Color.TRANSPARENT);
            chart.setCenterTextColor(isDarkTheme? Color.WHITE:Color.BLACK);

            chart.getLegend().setEnabled(true);
            chart.getLegend().setForm(Legend.LegendForm.CIRCLE);
            chart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            chart.getLegend().setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            chart.getLegend().setTextColor(isDarkTheme? Color.WHITE:Color.BLACK);

            chart.animateY(1000);

            if (forStorage) {
                final String[] LEGENDS = new String[]{ctx.getString(R.string.used), ctx.getString(R.string.free)};
                final int[] COLORS = {Utils.getColor(ctx, R.color.piechart_red), Utils.getColor(ctx, R.color.piechart_green)};

                long totalSpace = baseFile.getTotal(ctx),
					freeSpace = baseFile.getUsableSpace(),
					usedSpace = totalSpace-freeSpace;

                List<PieEntry> entries = new ArrayList<>();
                entries.add(new PieEntry(usedSpace, LEGENDS[0]));
                entries.add(new PieEntry(freeSpace, LEGENDS[1]));

                PieDataSet set = new PieDataSet(entries, null);
                set.setColors(COLORS);
                set.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
                set.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
                set.setSliceSpace(5f);
                set.setAutomaticallyDisableSliceSpacing(true);
                set.setValueLinePart2Length(1.05f);
                set.setSelectionShift(0f);

                PieData pieData = new PieData(set);
                pieData.setValueFormatter(new SizeFormatter(ctx));
                pieData.setValueTextColor(isDarkTheme? Color.WHITE:Color.BLACK);

                String totalSpaceFormatted = Formatter.formatFileSize(ctx, totalSpace);

                chart.setCenterText(new SpannableString(ctx.getString(R.string.total)+"\n"+totalSpaceFormatted));
                chart.setData(pieData);
            } else {
                LoadFolderSpaceData loadFolderSpaceData = new LoadFolderSpaceData(ctx, appTheme, chart, baseFile);
                loadFolderSpaceData.executeOnExecutor(executor);
            }

            chart.invalidate();
        }

        if (!forStorage&&showPermissions) {
            //final Frag main = ((MainActivity) base).mainFragment;
            ExplorerActivity ma = (ExplorerActivity) base;
            final Frag main = ma.slideFrag1Selected? ma.curContentFrag :ma.curExplorerFrag;//ma.mainFragment;
            AppCompatButton appCompatButton = (AppCompatButton) view.findViewById(R.id.permissionsButton);
            appCompatButton.setAllCaps(true);

            final View permissionsTable = view.findViewById(R.id.permtable);
            final View button = view.findViewById(R.id.set);
            if (isRoot&&permissions.length()>6) {
                appCompatButton.setVisibility(View.VISIBLE);
                appCompatButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (permissionsTable.getVisibility()==View.GONE) {
								permissionsTable.setVisibility(View.VISIBLE);
								button.setVisibility(View.VISIBLE);
								setPermissionsDialog(permissionsTable, button, baseFile, permissions, ctx,
													 main);
							} else {
								button.setVisibility(View.GONE);
								permissionsTable.setVisibility(View.GONE);
							}
						}
					});
            }
        }

        builder.customView(view, true);
        builder.positiveText(base.getResources().getString(R.string.ok));
        builder.positiveColor(accentColor);
        builder.dismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					executor.shutdown();
				}
			});

        MaterialDialog materialDialog = builder.build();
        materialDialog.show();
        materialDialog.getActionButton(DialogAction.NEGATIVE).setEnabled(false);

        /*
		 View bottomSheet = c.findViewById(R.id.design_bottom_sheet);
		 BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
		 bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
		 bottomSheetBehavior.setPeekHeight(BottomSheetBehavior.STATE_DRAGGING);
		 */
    }

    public static void showPropertiesDialog(final ZipEntry zipEntry, ThemedActivity base, AppTheme appTheme,
											long totalZipLength, long totalUnzipLength) {
        final Context c = base.getApplicationContext();
        int accentColor = base.getColorPreference().getColor(ColorUsage.ACCENT);
        final String date = Utils.getDate(zipEntry.lastModified);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(base);
        builder.title(c.getString(R.string.properties));
        builder.theme(appTheme.getMaterialDialogTheme());

        View v = base.getLayoutInflater().inflate(R.layout.properties_dialog, null);
        TextView itemsText = (TextView) v.findViewById(R.id.size);

        /*View setup*/ {
            TextView mNameTitle = (TextView) v.findViewById(R.id.title_name);
            mNameTitle.setTextColor(accentColor);

            TextView mDateTitle = (TextView) v.findViewById(R.id.title_date);
            mDateTitle.setTextColor(accentColor);

            TextView mSizeTitle = (TextView) v.findViewById(R.id.title_size);
            mSizeTitle.setTextColor(accentColor);

            TextView mLocationTitle = (TextView) v.findViewById(R.id.title_location);
            mLocationTitle.setTextColor(accentColor);

            TextView md5Title = (TextView) v.findViewById(R.id.title_md5);
            md5Title.setTextColor(accentColor);

            TextView sha256Title = (TextView) v.findViewById(R.id.title_sha256);
            sha256Title.setTextColor(accentColor);

            ((TextView) v.findViewById(R.id.name)).setText(zipEntry.name);
            ((TextView) v.findViewById(R.id.location)).setText(zipEntry.parentPath);
            ((TextView) v.findViewById(R.id.date)).setText(date);
			itemsText.setText(Util.nf.format(zipEntry.length) + "B");
            LinearLayout mNameLinearLayout = (LinearLayout) v.findViewById(R.id.properties_dialog_name);
            LinearLayout mLocationLinearLayout = (LinearLayout) v.findViewById(R.id.properties_dialog_location);
            LinearLayout mSizeLinearLayout = (LinearLayout) v.findViewById(R.id.properties_dialog_size);
            LinearLayout mDateLinearLayout = (LinearLayout) v.findViewById(R.id.properties_dialog_date);

            // setting click listeners for long press
            mNameLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						Futils.copyToClipboard(c, zipEntry.name);
						Toast.makeText(c, c.getResources().getString(R.string.name) + " " +
									   c.getResources().getString(R.string.properties_copied_clipboard), Toast.LENGTH_SHORT).show();
						return false;
					}
				});
            mLocationLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						Futils.copyToClipboard(c, zipEntry.parentPath);
						Toast.makeText(c, c.getResources().getString(R.string.location) + " " +
									   c.getResources().getString(R.string.properties_copied_clipboard), Toast.LENGTH_SHORT).show();
						return false;
					}
				});
            mSizeLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						Futils.copyToClipboard(c, "");//items
						Toast.makeText(c, c.getResources().getString(R.string.size) + " " +
									   c.getResources().getString(R.string.properties_copied_clipboard), Toast.LENGTH_SHORT).show();
						return false;
					}
				});
            mDateLinearLayout.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						Futils.copyToClipboard(c, date);
						Toast.makeText(c, c.getResources().getString(R.string.date) + " " +
									   c.getResources().getString(R.string.properties_copied_clipboard), Toast.LENGTH_SHORT).show();
						return false;
					}
				});
			builder.customView(v, true);
			builder.positiveText(base.getResources().getString(R.string.ok));
			builder.positiveColor(accentColor);
			builder.dismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						//executor.shutdown();
					}
				});

			MaterialDialog materialDialog = builder.build();
			materialDialog.show();
			materialDialog.getActionButton(DialogAction.NEGATIVE).setEnabled(false);

        }
    }
	
	public static class SizeFormatter implements IValueFormatter {

        private Context context;

        public SizeFormatter(Context c) {
            context = c;
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex,
                                        ViewPortHandler viewPortHandler) {
            String prefix = entry.getData()!=null&&entry.getData() instanceof String?
				(String) entry.getData():"";

            return prefix+Formatter.formatFileSize(context, (long) value);
        }
    }

    public static void showCloudDialog(final ExplorerActivity mainActivity, AppTheme appTheme, final OpenMode openMode) {
        int accentColor = mainActivity.getColorPreference().getColor(ColorUsage.ACCENT);
        final MaterialDialog.Builder builder = new MaterialDialog.Builder(mainActivity);

        switch (openMode) {
            case DROPBOX:
                builder.title(mainActivity.getResources().getString(R.string.cloud_dropbox));
                break;
            case BOX:
                builder.title(mainActivity.getResources().getString(R.string.cloud_box));
                break;
            case GDRIVE:
                builder.title(mainActivity.getResources().getString(R.string.cloud_drive));
                break;
            case ONEDRIVE:
                builder.title(mainActivity.getResources().getString(R.string.cloud_onedrive));
                break;
        }

        builder.theme(appTheme.getMaterialDialogTheme());
        builder.content(mainActivity.getResources().getString(R.string.cloud_remove));

        builder.positiveText(mainActivity.getResources().getString(R.string.yes));
        builder.positiveColor(accentColor);
        builder.negativeText(mainActivity.getResources().getString(R.string.no));
        builder.negativeColor(accentColor);

        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
					mainActivity.deleteConnection(openMode);
				}
			});

        builder.onNegative(new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
					dialog.cancel();
				}
			});

        builder.show();
    }

    public static void showEncryptWarningDialog(final Intent intent, final Frag main,
                                                AppTheme appTheme,
                                                final EncryptDecryptUtils.EncryptButtonCallbackInterface
												encryptButtonCallbackInterface) {
        int accentColor = main.activity.getColorPreference().getColor(ColorUsage.ACCENT);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(main.getContext());
        final MaterialDialog.Builder builder = new MaterialDialog.Builder(main.getActivity());
        builder.title(main.getResources().getString(R.string.warning));
        builder.content(main.getResources().getString(R.string.crypt_warning_key));
        builder.theme(appTheme.getMaterialDialogTheme());
        builder.negativeText(main.getResources().getString(R.string.warning_never_show));
        builder.positiveText(main.getResources().getString(R.string.warning_confirm));
        builder.positiveColor(accentColor);

        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
					try {
						encryptButtonCallbackInterface.onButtonPressed(intent);
					} catch (Exception e) {
						e.printStackTrace();

						Toast.makeText(main.getActivity(),
									   main.getResources().getString(R.string.crypt_encryption_fail),
									   Toast.LENGTH_LONG).show();
					}
				}
			});

        builder.onNegative(new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
					preferences.edit().putBoolean(Preffrag.PREFERENCE_CRYPT_WARNING_REMEMBER, true).apply();
					try {
						encryptButtonCallbackInterface.onButtonPressed(intent);
					} catch (Exception e) {
						e.printStackTrace();

						Toast.makeText(main.getActivity(),
									   main.getResources().getString(R.string.crypt_encryption_fail),
									   Toast.LENGTH_LONG).show();
					}
				}
			});

        builder.show();
    }

    public static void showEncryptAuthenticateDialog(final Context c, final Intent intent,
                                                     final ExplorerActivity main, AppTheme appTheme,
                                                     final EncryptDecryptUtils.EncryptButtonCallbackInterface
													 encryptButtonCallbackInterface) {
        int accentColor = main.getColorPreference().getColor(ColorUsage.ACCENT);
        MaterialDialog.Builder builder = new MaterialDialog.Builder(c);
        builder.title(main.getResources().getString(R.string.crypt_encrypt));

        View rootView = View.inflate(c, R.layout.dialog_encrypt_authenticate, null);

        final AppCompatEditText passwordEditText = (AppCompatEditText)
			rootView.findViewById(R.id.edit_text_dialog_encrypt_password);
        final AppCompatEditText passwordConfirmEditText = (AppCompatEditText)
			rootView.findViewById(R.id.edit_text_dialog_encrypt_password_confirm);

        builder.customView(rootView, true);

        builder.positiveText(c.getString(R.string.ok));
        builder.negativeText(c.getString(R.string.cancel));
        builder.theme(appTheme.getMaterialDialogTheme());
        builder.positiveColor(accentColor);
        builder.negativeColor(accentColor);

        builder.onNegative(new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
					dialog.cancel();
				}
			});

        builder.onPositive(new MaterialDialog.SingleButtonCallback() {

				@Override
				public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

					if (TextUtils.isEmpty(passwordEditText.getText())||
                        TextUtils.isEmpty(passwordConfirmEditText.getText())) {
						dialog.cancel();
						return;
					}

					try {
						encryptButtonCallbackInterface.onButtonPressed(intent,
																	   passwordEditText.getText().toString());
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(c, c.getString(R.string.crypt_encryption_fail), Toast.LENGTH_LONG).show();
					}
				}
			});

        builder.show();
    }

    @RequiresApi(api = M)
    public static void showDecryptFingerprintDialog(final Context c, ExplorerActivity main,
                                                    final Intent intent, AppTheme appTheme,
                                                    final EncryptDecryptUtils.DecryptButtonCallbackInterface
													decryptButtonCallbackInterface) throws CryptException {

        int accentColor = main.getColorPreference().getColor(ColorUsage.ACCENT);
        MaterialDialog.Builder builder = new MaterialDialog.Builder(c);
        builder.title(c.getString(R.string.crypt_decrypt));

        View rootView = View.inflate(c, R.layout.dialog_decrypt_fingerprint_authentication, null);

        Button cancelButton = (Button) rootView.findViewById(R.id.button_decrypt_fingerprint_cancel);
        cancelButton.setTextColor(accentColor);
        builder.customView(rootView, true);
        builder.canceledOnTouchOutside(false);

        builder.theme(appTheme.getMaterialDialogTheme());

        final MaterialDialog dialog = builder.show();
        cancelButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.cancel();
				}
			});

        FingerprintManager manager = (FingerprintManager) c.getSystemService(Context.FINGERPRINT_SERVICE);
        FingerprintManager.CryptoObject object = new
			FingerprintManager.CryptoObject(CryptUtil.initCipher(c));

        FingerprintHandler handler = new FingerprintHandler(c, intent, dialog, decryptButtonCallbackInterface);
        handler.authenticate(manager, object);
    }

    public static void showDecryptDialog(Context c, final ExplorerActivity main, final Intent intent,
                                         AppTheme appTheme, final String password,
                                         final EncryptDecryptUtils.DecryptButtonCallbackInterface
										 decryptButtonCallbackInterface) {
        int accentColor = main.getColorPreference().getColor(ColorUsage.ACCENT);
        MaterialDialog.Builder builder = new MaterialDialog.Builder(c);
        builder.title(c.getString(R.string.crypt_decrypt));
        builder.input(c.getString(R.string.authenticate_password), "", false,
			new MaterialDialog.InputCallback() {
				@Override
				public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
				}
			});
        builder.theme(appTheme.getMaterialDialogTheme());
        builder.positiveText(c.getString(R.string.ok));
        builder.negativeText(c.getString(R.string.cancel));
        builder.positiveColor(accentColor);
        builder.negativeColor(accentColor);
        builder.onPositive(new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

					EditText editText = dialog.getInputEditText();

					if (editText.getText().toString().equals(password))
						decryptButtonCallbackInterface.confirm(intent);
					else decryptButtonCallbackInterface.failed();
				}
			});
        builder.onNegative(new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
					dialog.cancel();
				}
			});
        builder.show();
    }

    public static void showSMBHelpDialog(Context m, int accentColor) {
        MaterialDialog.Builder b=new MaterialDialog.Builder(m);
        b.content(m.getText(R.string.smb_instructions));
        b.positiveText(R.string.doit);
        b.positiveColor(accentColor);
        b.build().show();
    }

    public static void showPackageDialog(final File f, final ExplorerActivity m) {
        int accentColor = m.getColorPreference().getColor(ColorUsage.ACCENT);
        MaterialDialog.Builder mat = new MaterialDialog.Builder(m);
        mat.title(R.string.packageinstaller).content(R.string.pitext)
			.positiveText(R.string.install)
			.negativeText(R.string.view)
			.neutralText(R.string.cancel)
			.positiveColor(accentColor)
			.negativeColor(accentColor)
			.neutralColor(accentColor)
			.callback(new MaterialDialog.ButtonCallback() {
				@Override
				public void onPositive(MaterialDialog materialDialog) {
					Futils.openunknown(f, m, false);
				}

				@Override
				public void onNegative(MaterialDialog materialDialog) {
					//m.openZip(f.getPath());
				}
			})
			.theme(m.getAppTheme().getMaterialDialogTheme())
			.build()
			.show();
    }


//    public static void showArchiveDialog(final File f, final ExplorerActivity m) {
//        int accentColor = m.getColorPreference().getColor(ColorUsage.ACCENT);
//        MaterialDialog.Builder mat = new MaterialDialog.Builder(m);
//        mat.title(R.string.archive)
//                .content(R.string.archtext)
//                .positiveText(R.string.extract)
//                .negativeText(R.string.view)
//                .neutralText(R.string.cancel)
//                .positiveColor(accentColor)
//                .negativeColor(accentColor)
//                .neutralColor(accentColor)
//                .callback(new MaterialDialog.ButtonCallback() {
//                    @Override
//                    public void onPositive(MaterialDialog materialDialog) {
//                        m. mainActivityHelper.extractFile(f);
//                    }
//
//                    @Override
//                    public void onNegative(MaterialDialog materialDialog) {
//                        //m.addZipViewTab(f.getPath());
////                        if (f.getName().toLowerCase().endsWith(".rar"))
////                            m.openRar(Uri.fromFile(f).toString());
////                        else
////                            m.openZip(Uri.fromFile(f).toString());
//                    }
//                });
//        if (m.getAppTheme().equals(AppTheme.DARK)) mat.theme(Theme.DARK);
//        MaterialDialog b = mat.build();
//
//        if (!f.getName().toLowerCase().endsWith(".rar") && !f.getName().toLowerCase().endsWith(".jar") && !f.getName().toLowerCase().endsWith(".apk") && !f.getName().toLowerCase().endsWith(".zip"))
//            b.getActionButton(DialogAction.NEGATIVE).setEnabled(false);
//        b.show();
//    }

    public static void showCompressDialog(final ExplorerActivity m, final ArrayList<BaseFile> b, final String current) {
        int accentColor = m.getColorPreference().getColor(ColorUsage.ACCENT);
        MaterialDialog.Builder a = new MaterialDialog.Builder(m);
        a.input(m.getResources().getString(R.string.enterzipname), ".zip", false, new
			MaterialDialog.InputCallback() {
				@Override
				public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {

				}
			});
        a.widgetColor(accentColor);
        a.theme(m.getAppTheme().getMaterialDialogTheme());
        a.title(m.getResources().getString(R.string.enterzipname));
        a.positiveText(R.string.create);
        a.positiveColor(accentColor);
        a.onPositive(new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
					if (materialDialog.getInputEditText().getText().toString().equals(".zip"))
						Toast.makeText(m, m.getResources().getString(R.string.no_name), Toast.LENGTH_SHORT).show();
					else {
						String name = current+"/"+materialDialog.getInputEditText().getText().toString();
						m.mainActivityHelper.compressFiles(new File(name), b);
					}
				}
			});
        a.negativeText(m.getResources().getString(R.string.cancel));
        a.negativeColor(accentColor);
        final MaterialDialog materialDialog = a.build();
        materialDialog.show();

        // place cursor at the starting of edit text by posting a runnable to edit text
        // this is done because in case android has not populated the edit text layouts yet, it'll
        // reset calls to selection if not posted in message queue
        materialDialog.getInputEditText().post(new Runnable() {
				@Override
				public void run() {
					materialDialog.getInputEditText().setSelection(0);
				}
			});
    }

//    public static void showSortDialog(final Frag m, AppTheme appTheme, final SharedPreferences sharedPref) {
//        int accentColor = m.activity.getColorPreference().getColor(ColorUsage.ACCENT);
//        String[] sort = m.getResources().getStringArray(R.array.sortby);
//        int current = Integer.parseInt(sharedPref.getString("sortby", "0"));
//        MaterialDialog.Builder a = new MaterialDialog.Builder(m.getActivity());
//        a.theme(appTheme.getMaterialDialogTheme());
//        a.items(sort).itemsCallbackSingleChoice(current > 3 ? current - 4 : current, new MaterialDialog.ListCallbackSingleChoice() {
//            @Override
//            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
//
//                return true;
//            }
//        });
//
//        a.negativeText(R.string.ascending).positiveColor(accentColor);
//        a.positiveText(R.string.descending).negativeColor(accentColor);
//        a.onNegative(new MaterialDialog.SingleButtonCallback() {
//            @Override
//            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                sharedPref.edit().putString("sortby", "" + dialog.getSelectedIndex()).commit();
//                m.getSortModes();
//                m.updateList();
//                dialog.dismiss();
//            }
//        });
//
//        a.onPositive(new MaterialDialog.SingleButtonCallback() {
//            @Override
//            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                sharedPref.edit().putString("sortby", "" + (dialog.getSelectedIndex() + 4)).commit();
//                m.getSortModes();
//                m.updateList();
//                dialog.dismiss();
//            }
//        });
//        a.title(R.string.sortby);
//        a.build().show();
//    }

//    public static void showSortDialog(final AppsList m, AppTheme appTheme) {
//        int accentColor = ((ThemedActivity) m.getActivity()).getColorPreference().getColor(ColorUsage.ACCENT);
//        String[] sort = m.getResources().getStringArray(R.array.sortbyApps);
//        int current = Integer.parseInt(m.Sp.getString("sortbyApps", "0"));
//        MaterialDialog.Builder a = new MaterialDialog.Builder(m.getActivity());
//        a.theme(appTheme.getMaterialDialogTheme());
//        a.items(sort).itemsCallbackSingleChoice(current > 2 ? current - 3 : current, new MaterialDialog.ListCallbackSingleChoice() {
//            @Override
//            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
//
//                return true;
//            }
//        });
//        a.negativeText(R.string.ascending).positiveColor(accentColor);
//        a.positiveText(R.string.descending).negativeColor(accentColor);
//        a.onNegative(new MaterialDialog.SingleButtonCallback() {
//            @Override
//            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//
//                m.Sp.edit().putString("sortbyApps", "" + dialog.getSelectedIndex()).commit();
//                m.getSortModes();
//                m.getLoaderManager().restartLoader(AppsList.ID_LOADER_APP_LIST, null, m);
//                dialog.dismiss();
//            }
//        });
//
//        a.onPositive(new MaterialDialog.SingleButtonCallback() {
//            @Override
//            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//
//                m.Sp.edit().putString("sortbyApps", "" + (dialog.getSelectedIndex() + 3)).commit();
//                m.getSortModes();
//                m.getLoaderManager().restartLoader(AppsList.ID_LOADER_APP_LIST, null, m);
//                dialog.dismiss();
//            }
//        });
//
//        a.title(R.string.sortby);
//        a.build().show();
//    }


    public static void showHistoryDialog(final DataUtils dataUtils, Futils utils, final ContentFragment m, AppTheme appTheme) {
        int accentColor = m.activity.getColorPreference().getColor(ColorUsage.ACCENT);
        final MaterialDialog.Builder a = new MaterialDialog.Builder(m.getActivity());
        a.positiveText(R.string.cancel);
        a.positiveColor(accentColor);
        a.negativeText(R.string.clear);
        a.negativeColor(accentColor);
        a.title(R.string.history);
        a.onNegative(new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
					dataUtils.clearHistory();
				}
			});
        a.theme(appTheme.getMaterialDialogTheme());

        a.autoDismiss(true);
        HiddenAdapter adapter = new HiddenAdapter(m.getActivity(), m, utils, R.layout.bookmarkrow,
												  toHFileArray(dataUtils.getHistory()), null, true);
        a.adapter(adapter, null);

        MaterialDialog x= a.build();
        adapter.updateDialog(x);
        x.show();
    }

    public static void showHiddenDialog(DataUtils dataUtils, Futils utils, final ContentFragment m, AppTheme appTheme) {
        int accentColor = m.activity.getColorPreference().getColor(ColorUsage.ACCENT);
        final MaterialDialog.Builder a = new MaterialDialog.Builder(m.activity);
        a.positiveText(R.string.cancel);
        a.positiveColor(accentColor);
        a.title(R.string.hiddenfiles);
        a.theme(appTheme.getMaterialDialogTheme());
        a.autoDismiss(true);
        HiddenAdapter adapter = new HiddenAdapter(m.activity, m, utils, R.layout.bookmarkrow,
												  toHFileArray(dataUtils.getHiddenfiles()), null, false);
        a.adapter(adapter, null);
        a.dividerColor(Color.GRAY);
        MaterialDialog x= a.build();
        adapter.updateDialog(x);
        x.show();

    }

	class MaterialDialog2 extends MaterialDialog {
		protected MaterialDialog2(com.afollestad.materialdialogs.MaterialDialog.Builder builder) {
			super(builder);
		}

	}

    public static void setPermissionsDialog(final View v, View but, final HFile file,
											final String f, final Context context, final Frag mainFrag) {
        final CheckBox readown = (CheckBox) v.findViewById(R.id.creadown);
        final CheckBox readgroup = (CheckBox) v.findViewById(R.id.creadgroup);
        final CheckBox readother = (CheckBox) v.findViewById(R.id.creadother);
        final CheckBox writeown = (CheckBox) v.findViewById(R.id.cwriteown);
        final CheckBox writegroup = (CheckBox) v.findViewById(R.id.cwritegroup);
        final CheckBox writeother = (CheckBox) v.findViewById(R.id.cwriteother);
        final CheckBox exeown = (CheckBox) v.findViewById(R.id.cexeown);
        final CheckBox exegroup = (CheckBox) v.findViewById(R.id.cexegroup);
        final CheckBox exeother = (CheckBox) v.findViewById(R.id.cexeother);
        String perm = f;
        if (perm.length()<6) {
            v.setVisibility(View.GONE);
            but.setVisibility(View.GONE);
            Toast.makeText(context, R.string.not_allowed, Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<Boolean[]> arrayList = Futils.parse(perm);
        Boolean[] read = arrayList.get(0);
        Boolean[] write = arrayList.get(1);
        final Boolean[] exe = arrayList.get(2);
        readown.setChecked(read[0]);
        readgroup.setChecked(read[1]);
        readother.setChecked(read[2]);
        writeown.setChecked(write[0]);
        writegroup.setChecked(write[1]);
        writeother.setChecked(write[2]);
        exeown.setChecked(exe[0]);
        exegroup.setChecked(exe[1]);
        exeother.setChecked(exe[2]);
        but.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int a = 0, b = 0, c = 0;
					if (readown.isChecked()) a = 4;
					if (writeown.isChecked()) b = 2;
					if (exeown.isChecked()) c = 1;
					int owner = a+b+c;
					int d = 0;
					int e = 0;
					int f = 0;
					if (readgroup.isChecked()) d = 4;
					if (writegroup.isChecked()) e = 2;
					if (exegroup.isChecked()) f = 1;
					int group = d+e+f;
					int g = 0, h = 0, i = 0;
					if (readother.isChecked()) g = 4;
					if (writeother.isChecked()) h = 2;
					if (exeother.isChecked()) i = 1;
					int other = g+h+i;
					String finalValue = owner+""+group+""+other;

					String command = "chmod "+finalValue+" "+file.getPath();
					if (file.isDirectory())
						command = "chmod -R "+finalValue+" \""+file.getPath()+"\"";

					try {
						RootHelper.runShellCommand(command, new Shell.OnCommandResultListener() {
								@Override
								public void onCommandResult(int commandCode, int exitCode, List<String> output) {
									if (exitCode<0) {
										Log.e("GeneralDialogCreation.setPermissionsDialog", output + ".");
										Toast.makeText(context, mainFrag.getString(R.string.operationunsuccesful),
													   Toast.LENGTH_LONG).show();
									} else {
										Toast.makeText(context,
													   mainFrag.getResources().getString(R.string.done), Toast.LENGTH_LONG).show();
									}
								}
							});
						mainFrag.updateList();
					} catch (RootNotPermittedException e1) {
						Toast.makeText(context, mainFrag.getResources().getString(R.string.rootfailure),
									   Toast.LENGTH_LONG).show();
						e1.printStackTrace();
					}

				}
			});
    }

//    public static void showChangePathsDialog(final WeakReference<ExplorerActivity> m, final SharedPreferences prefs) {
//        final MaterialDialog.Builder a = new MaterialDialog.Builder(m.get());
//        ExplorerActivity get = m.get();
//		final Frag main = get.slideFrag1Selected ? get.curContentFrag : get.curExplorerFrag;//ma.mainFragment;
//		
//		a.input(null, main.currentPathTitle, false,
//                new MaterialDialog.InputCallback() {
//                    @Override
//                    public void onInput(@NonNull MaterialDialog dialog, CharSequence charSequence) {
//                        boolean isAccessible = Futils.isPathAccesible(charSequence.toString(), prefs);
//                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(isAccessible);
//                    }
//                });
//
//        a.alwaysCallInputCallback();
//
//        ExplorerActivity mainActivity = m.get();
//
//        int accentColor = mainActivity.getColorPreference().getColor(ColorUsage.ACCENT);
//
//        a.widgetColor(accentColor);
//
//        a.theme(m.get().getAppTheme().getMaterialDialogTheme());
//        a.title(R.string.enterpath);
//
//        a.positiveText(R.string.go);
//        a.positiveColor(accentColor);
//
//        a.negativeText(R.string.cancel);
//        a.negativeColor(accentColor);
//
//        a.onPositive(new MaterialDialog.SingleButtonCallback() {
//            @Override
//            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                m.get().getCurrentMainFragment().loadlist(dialog.getInputEditText().getText().toString(),
//                        false, OpenMode.UNKNOWN);
//            }
//
//        });
//
//        a.show();
//    }

}
