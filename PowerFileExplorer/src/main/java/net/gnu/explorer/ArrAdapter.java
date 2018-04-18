package net.gnu.explorer;

import android.widget.TextView;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.view.View;
import java.io.File;
import java.util.ArrayList;
import android.util.Log;
import android.content.res.TypedArray;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import net.gnu.androidutil.AndroidPathUtils;
import com.amaze.filemanager.utils.files.Futils;
import android.content.Intent;
import android.net.Uri;
import net.gnu.androidutil.AndroidUtils;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.view.MenuInflater;
import net.gnu.util.FileUtil;
import net.dongliu.apk.parser.ApkParser;
import com.amaze.filemanager.database.CryptHandler;
import com.amaze.filemanager.database.models.EncryptedEntry;
import com.amaze.filemanager.utils.files.CryptUtil;

import android.graphics.Typeface;
import android.view.Gravity;
import com.amaze.filemanager.utils.OpenMode;
import android.util.TypedValue;
import android.view.MenuItem;
import com.amaze.filemanager.utils.ServiceWatcherUtil;
import net.gnu.util.Util;
import android.graphics.Color;
import com.amaze.filemanager.ui.icons.MimeTypes;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import com.amaze.filemanager.ui.LayoutElement;
import com.amaze.filemanager.activities.BasicActivity;
import com.amaze.filemanager.filesystem.BaseFile;
import android.content.SharedPreferences;
import com.amaze.filemanager.services.EncryptService;

import android.preference.PreferenceManager;
import javax.crypto.Cipher;
import android.text.format.Formatter;
import com.afollestad.materialdialogs.MaterialDialog;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import com.amaze.filemanager.ui.dialogs.GeneralDialogCreation;
import com.amaze.filemanager.activities.ThemedActivity;
import com.afollestad.materialdialogs.DialogAction;
import com.amaze.filemanager.filesystem.HFile;
import com.amaze.filemanager.utils.DataUtils;
import com.amaze.filemanager.utils.files.EncryptDecryptUtils;
import com.amaze.filemanager.fragments.preference_fragments.Preffrag;
import com.amaze.filemanager.utils.cloud.CloudUtil;
import jcifs.smb.SmbFile;
import java.net.MalformedURLException;
import com.amaze.filemanager.utils.OTGUtil;

public class ArrAdapter extends RecyclerAdapter<LayoutElement, ArrAdapter.ViewHolder> {

	private static final String TAG = "ArrAdapter";

	private final int backgroundResource;
	private final ContentFragment fileFrag;
	
	public void toggleChecked(final boolean checked) {
		if (checked) {
			fileFrag.allCbx.setSelected(true);
			fileFrag.selectedInList1.clear();
			fileFrag.selectedInList1.addAll(fileFrag.dataSourceL1);
			fileFrag.allCbx.setImageResource(R.drawable.ic_accept);
		} else {
			fileFrag.allCbx.setSelected(false);
			fileFrag.selectedInList1.clear();
			fileFrag.allCbx.setImageResource(R.drawable.dot);
		}
		notifyDataSetChanged();
	}

	final class ViewHolder extends RecyclerView.ViewHolder {
		private final TextView name;
		private final TextView size;
		private final TextView attr;
		private final TextView lastModified;
		private final TextView type;
		private final ImageButton cbx;
		private final ImageView image;
		private final ImageButton more;
		private final View convertedView;

		public ViewHolder(final View convertView) {
			super(convertView);
			name = (TextView) convertView.findViewById(R.id.name);
			size = (TextView) convertView.findViewById(R.id.items);
			attr = (TextView) convertView.findViewById(R.id.attr);
			lastModified = (TextView) convertView.findViewById(R.id.lastModified);
			type = (TextView) convertView.findViewById(R.id.type);
			cbx = (ImageButton) convertView.findViewById(R.id.cbx);
			image = (ImageView)convertView.findViewById(R.id.icon);
			more = (ImageButton)convertView.findViewById(R.id.more);

			more.setColorFilter(ExplorerActivity.TEXT_COLOR);

			name.setTextColor(ExplorerActivity.DIR_COLOR);
			size.setTextColor(ExplorerActivity.TEXT_COLOR);
			attr.setTextColor(ExplorerActivity.TEXT_COLOR);
			lastModified.setTextColor(ExplorerActivity.TEXT_COLOR);
			if (type != null) {
				type.setTextColor(ExplorerActivity.TEXT_COLOR);
			}
			image.setScaleType(ImageView.ScaleType.FIT_CENTER);
			convertView.setTag(this);
			this.convertedView = convertView;
		}
	}

	public ArrAdapter(final ContentFragment fileFrag, final ArrayList<LayoutElement> objects) {
		super(objects);
		this.fileFrag = fileFrag;
		
		final int[] attrs = new int[]{R.attr.selectableItemBackground};
		final TypedArray typedArray = fileFrag.activity.obtainStyledAttributes(attrs);
		backgroundResource = typedArray.getResourceId(0, 0);
		typedArray.recycle();
	}

	@Override
	public int getItemViewType(final int position) {
		if (fileFrag.dataSourceL1.size() == 0) {
			return 0;
		} else if (fileFrag.spanCount == 1 
				   || (fileFrag.spanCount == 2 && (fileFrag.activity.right.getVisibility() == View.GONE || fileFrag.activity.left.getVisibility() == View.GONE))) {
			return 1;
		} else if (fileFrag.spanCount == 2 && fileFrag.slidingTabsFragment.width >= 0) {
			return 2;
		} else {
			return 3;
		}
	}

	@Override
	public ArrAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent,
													final int viewType) {
		View v;
		if (viewType <= 1) {
			v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
		} else if (viewType == 2) {
			v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_small, parent, false);
		} else {
			v = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
		}
		return new ViewHolder(v);
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position) {
		//Log.d(TAG, "onBindViewHolder " + position);

		final LayoutElement le = mDataset.get(position);
		final String fPath = le.path;
		final String fName = le.name;

		final TextView name = holder.name;
		final TextView size = holder.size;
		final TextView attr = holder.attr;
		final TextView lastModified = holder.lastModified;
		final TextView type = holder.type;
		final ImageButton cbx = holder.cbx;
		final ImageView image = holder.image;
		final ImageButton more = holder.more;
		final View convertedView = holder.convertedView;

		if (fileFrag.type == Frag.TYPE.EXPLORER) {
			name.setText(fName);
		} else {
			name.setText(fPath);
		}
		image.setContentDescription(fPath);

		final OnClickListener onClickListener = new OnClickListener(position);
		convertedView.setOnClickListener(onClickListener);
		cbx.setOnClickListener(onClickListener);
		image.setOnClickListener(onClickListener);
		more.setOnClickListener(onClickListener);

		final OnLongClickListener onLongClickListener = new OnLongClickListener(position);
		convertedView.setOnLongClickListener(onLongClickListener);
		cbx.setOnLongClickListener(onLongClickListener);
		image.setOnLongClickListener(onLongClickListener);
		more.setOnLongClickListener(onLongClickListener);

		if (fileFrag.currentPathTitle == null || fileFrag.currentPathTitle.length() > 0) {
			name.setEllipsize(TextUtils.TruncateAt.MIDDLE);
		} else {
			name.setEllipsize(TextUtils.TruncateAt.START);
		}
		
		//Log.d("f.getAbsolutePath()", f.getAbsolutePath());
		//Log.d("curSelectedFiles", curSelectedFiles.toString());
		if (fileFrag.selectedInList1.contains(le)) {
			convertedView.setBackgroundColor(ExplorerActivity.SELECTED_IN_LIST);
			cbx.setImageResource(R.drawable.ic_accept);
			cbx.setSelected(true);
			cbx.setEnabled(true);
			if ((fileFrag.currentPathTitle == null || fileFrag.currentPathTitle.length() > 0) && fileFrag.selectedInList1.size() == fileFrag.dataSourceL1.size()) {
				fileFrag.allCbx.setSelected(true);
				fileFrag.allCbx.setImageResource(R.drawable.ic_accept);
			}
		} else {
			boolean inDataSource2 = false;
			boolean isPartial = false;
			//Log.d(TAG, "dataSource2" + Util.collectionToString(dataSourceL2, true, "\n"));
			if (fileFrag.multiFiles && fileFrag.dataSourceL2 != null) {
				final String fPathD = fPath + "/";
				String f2Path;
				for (LayoutElement f2 : fileFrag.dataSourceL2) {
					f2Path = f2.path;
					if (f2.equals(le) || fPath.startsWith(f2Path + "/")) {
						inDataSource2 = true;
						break;
					} else if (f2Path.startsWith(fPathD)) {
						isPartial = true;
						break;
					}
				}
			}
			//Log.d(TAG, "inDataSource2 " + inDataSource2 + ", " + dir);
			if (inDataSource2) {
				convertedView.setBackgroundColor(ExplorerActivity.IN_DATA_SOURCE_2);
				cbx.setImageResource(R.drawable.ic_accept);
				cbx.setSelected(true);
				cbx.setEnabled(false);
				if ((fileFrag.currentPathTitle == null || fileFrag.currentPathTitle.length() > 0) && fileFrag.selectedInList1.size() == fileFrag.dataSourceL1.size()) {
					fileFrag.allCbx.setSelected(true);
					fileFrag.allCbx.setImageResource(R.drawable.ic_accept);
				}
			} else if (isPartial) {
				convertedView.setBackgroundColor(ExplorerActivity.IS_PARTIAL);
				cbx.setImageResource(R.drawable.ready);
				cbx.setSelected(false);
				cbx.setEnabled(true);
				fileFrag.allCbx.setSelected(false);
				if (fileFrag.selectedInList1.size() == 0) {
					fileFrag.allCbx.setImageResource(R.drawable.dot);
				} else {
					fileFrag.allCbx.setImageResource(R.drawable.ready);
				}
			} else {
				convertedView.setBackgroundResource(backgroundResource);
				if (fileFrag.selectedInList1.size() > 0) {
					cbx.setImageResource(R.drawable.ready);
					fileFrag.allCbx.setImageResource(R.drawable.ready);
				} else {
					cbx.setImageResource(R.drawable.dot);
					fileFrag.allCbx.setImageResource(R.drawable.dot);
				}
				cbx.setSelected(false);
				cbx.setEnabled(true);
				fileFrag.allCbx.setSelected(false);

			}
		}
		if (fileFrag.tempPreviewL2 != null && fileFrag.tempPreviewL2.equals(le)) {
			convertedView.setBackgroundColor(ExplorerActivity.LIGHT_GREY);
		}

		final int viewType = getItemViewType(position);
		final boolean canRead = le.bf.f.canRead();
		final boolean canWrite = le.bf.f.canWrite();
		if (!le.isDirectory) {
			String st;
			if (canWrite) {
				st = "-rw";
			} else if (canRead) {
				st = "-r-";
			} else {
				st = "---";
				cbx.setEnabled(false);
			}
			attr.setText(st);
			if (viewType <= 1) {
				final int lastIndexOf = fName.lastIndexOf(".");
				type.setText(lastIndexOf >= 0 && lastIndexOf < fName.length() - 1 ? fName.substring(lastIndexOf + 1) : "");
				size.setText(Util.nf.format(le.length) + " B");
				lastModified.setText(Util.dtf.format(le.lastModified));
			} else {
				size.setText(Formatter.formatFileSize(fileFrag.activity, le.length));
				lastModified.setText(Util.df.format(le.lastModified));
			}
		} else {
			final String[] list = le.bf.f.list();
			final int length = list == null ? 0 : list.length;
			size.setText(Util.nf.format(length) + " item");
			final String st;
			if (canWrite) {
				st = "drw";
			} else if (canRead) {
				st = "dr-";
			} else {
				st = "d--";
				cbx.setEnabled(false);
			}
			attr.setText(st);
			if (viewType <= 1) {
				type.setText("Folder");
				lastModified.setText(Util.dtf.format(le.lastModified));
			} else {
				lastModified.setText(Util.df.format(le.lastModified));
			}
		}
		fileFrag.imageLoader.displayImage(le.bf.f, fileFrag.getContext(), image, fileFrag.spanCount);
	}

	private class OnClickListener implements View.OnClickListener {
		private final int pos;
		private OnClickListener(int pos) {
			this.pos = pos;
		}
		@Override
		public void onClick(final View v) {
			fileFrag.select(true);
			//final Integer pos = Integer.valueOf(v.getContentDescription().toString());
			final LayoutElement rowItem = mDataset.get(pos);
			if (v.getId() == R.id.more) {

				final MenuBuilder menuBuilder = new MenuBuilder(fileFrag.activity);
				final MenuInflater inflater = new MenuInflater(fileFrag.activity);
				inflater.inflate(R.menu.file_commands, menuBuilder);
				final MenuPopupHelper optionsMenu = new MenuPopupHelper(fileFrag.activity , menuBuilder, fileFrag.searchButton);
				optionsMenu.setForceShowIcon(true);

				int num= menuBuilder.size();
				for (int i = 0; i < num; i++) {
					Drawable icon = menuBuilder.getItem(i).getIcon();
					if (icon != null) {
						icon.setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
					}
				}

				final MenuItem findItem = menuBuilder.findItem(R.id.extract);
				//Log.d(TAG, rowItem.name + ", " + findItem + ", " + FileUtil.extractiblePattern.matcher(rowItem.name).matches());
				if (rowItem.isDirectory || !FileUtil.extractiblePattern.matcher(rowItem.name).matches()) {
					findItem.setVisible(false);
				} else {
					findItem.setVisible(true);
				}
				menuBuilder.setCallback(new MenuBuilder.Callback() {
						@Override
						public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
							final ExplorerActivity activity = fileFrag.activity;
							switch (item.getItemId()) {
								case R.id.copy:
									//copy(v);
									fileFrag.activity.MOVE_PATH = null;
									ArrayList<BaseFile> copies = new ArrayList<>(1);
									copies.add(rowItem.generateBaseFile());
									activity.COPY_PATH = copies;
									if (activity.curExplorerFrag.commands.getVisibility() == View.GONE) {
										activity.curExplorerFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
										activity.curExplorerFrag.commands.setVisibility(View.VISIBLE);
										activity.curExplorerFrag.horizontalDivider6.setVisibility(View.VISIBLE);
										activity.curExplorerFrag.updateDelPaste();
									}
									if (activity.curContentFrag.commands.getVisibility() == View.GONE) {
										activity.curContentFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
										activity.curContentFrag.commands.setVisibility(View.VISIBLE);
										activity.curContentFrag.horizontalDivider6.setVisibility(View.VISIBLE);
										activity.curContentFrag.updateDelPaste();
									}
									break;
								case R.id.cut:
									//cut(v);
									activity.COPY_PATH = null;
									ArrayList<BaseFile> copie = new ArrayList<>(1);
									copie.add(rowItem.generateBaseFile());
									activity.MOVE_PATH = copie;
									//activity1.supportInvalidateOptionsMenu();
									if (activity.curExplorerFrag.commands.getVisibility() == View.GONE) {
										activity.curExplorerFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
										activity.curExplorerFrag.commands.setVisibility(View.VISIBLE);
										activity.curExplorerFrag.horizontalDivider6.setVisibility(View.VISIBLE);
										activity.curExplorerFrag.updateDelPaste();
									}
									if (activity.curContentFrag.commands.getVisibility() == View.GONE) {
										activity.curContentFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
										activity.curContentFrag.commands.setVisibility(View.VISIBLE);
										activity.curContentFrag.horizontalDivider6.setVisibility(View.VISIBLE);
										activity.curContentFrag.updateDelPaste();
									}
									break;
								case R.id.rename:
									fileFrag.rename(rowItem.generateBaseFile());
									break;
								case R.id.delete:
									ArrayList<LayoutElement> ele = new ArrayList<LayoutElement>(1);
									ele.add(rowItem);
									//new Futils().deleteFiles(ele, fileFrag.activity, /*positions, */activity.getAppTheme());
									GeneralDialogCreation.deleteFilesDialog(activity, //getLayoutElements(),
																			activity, ele, activity.getAppTheme());
									break;
								case R.id.share:
									switch (rowItem.getMode()) {
										case DROPBOX:
										case BOX:
										case GDRIVE:
										case ONEDRIVE:
											activity.getFutils().shareCloudFile(rowItem.path, rowItem.getMode(), activity);
											break;
										default:
											ArrayList<File> arrayList = new ArrayList<>(1);
											arrayList.add(new File(rowItem.path));
											activity.getFutils().shareFiles(arrayList, activity, activity.getAppTheme(), fileFrag.accentColor);
											break;
									}
									break;
								case R.id.scan:
									AndroidUtils.scanMedia(activity, rowItem.bf.f.getAbsolutePath(), false);
									break;
								case R.id.addshortcut:
									AndroidUtils.addShortcut(activity, rowItem.bf.f);
									break;
								case R.id.info:
//									new Futils().showProps((rowItem).generateBaseFile(),
//														   rowItem.permissions, fileFrag,
//														   BaseActivity.rootMode, activity.getAppTheme());
									GeneralDialogCreation.showPropertiesDialogWithPermissions(rowItem.generateBaseFile(),
																							  rowItem.permissions, activity, ThemedActivity.rootMode,
																							  activity.getAppTheme());
									break;
								case R.id.name:
									AndroidUtils.copyToClipboard(activity, rowItem.bf.f.getName());
									break;
								case R.id.path:
									AndroidUtils.copyToClipboard(activity, rowItem.bf.f.getParent());
									break;
								case R.id.fullname:
									AndroidUtils.copyToClipboard(activity, rowItem.bf.f.getAbsolutePath());
									break;
								case R.id.open_with:
									Futils.openunknown(new File(rowItem.path), activity, true);
									break;

								case R.id.encrypt:
									final Intent encryptIntent = new Intent(activity, EncryptService.class);
									encryptIntent.putExtra(EncryptService.TAG_OPEN_MODE, rowItem.getMode().ordinal());
									encryptIntent.putExtra(EncryptService.TAG_CRYPT_MODE,
														   EncryptService.CryptEnum.ENCRYPT.ordinal());
									encryptIntent.putExtra(EncryptService.TAG_SOURCE, rowItem.generateBaseFile());

									final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);

									final EncryptDecryptUtils.EncryptButtonCallbackInterface encryptButtonCallbackInterfaceAuthenticate =
										new EncryptDecryptUtils.EncryptButtonCallbackInterface() {
										@Override
										public void onButtonPressed(Intent intent) {
										}

										@Override
										public void onButtonPressed(Intent intent, String password) throws Exception {
											EncryptDecryptUtils.startEncryption(activity,
																				rowItem.generateBaseFile().getPath(), password, intent);
										}
									};

									EncryptDecryptUtils.EncryptButtonCallbackInterface encryptButtonCallbackInterface =
										new EncryptDecryptUtils.EncryptButtonCallbackInterface() {

										@Override
										public void onButtonPressed(Intent intent) throws Exception {
											// check if a master password or fingerprint is set
											if (!preferences.getString(Preffrag.PREFERENCE_CRYPT_MASTER_PASSWORD,
																	   Preffrag.PREFERENCE_CRYPT_MASTER_PASSWORD_DEFAULT).equals("")) {

												EncryptDecryptUtils.startEncryption(activity,
																					rowItem.generateBaseFile().getPath(),
																					Preffrag.ENCRYPT_PASSWORD_MASTER, encryptIntent);
											} else if (preferences.getBoolean(Preffrag.PREFERENCE_CRYPT_FINGERPRINT,
																			  Preffrag.PREFERENCE_CRYPT_FINGERPRINT_DEFAULT)) {

												EncryptDecryptUtils.startEncryption(activity,
																					rowItem.generateBaseFile().getPath(),
																					Preffrag.ENCRYPT_PASSWORD_FINGERPRINT, encryptIntent);
											} else {
												// let's ask a password from user
												GeneralDialogCreation.showEncryptAuthenticateDialog(activity, encryptIntent,
																									activity, activity.getAppTheme(),
																									encryptButtonCallbackInterfaceAuthenticate);
											}
										}

										@Override
										public void onButtonPressed(Intent intent, String password) {
										}
									};

									if (preferences.getBoolean(Preffrag.PREFERENCE_CRYPT_WARNING_REMEMBER,
															   Preffrag.PREFERENCE_CRYPT_WARNING_REMEMBER_DEFAULT)) {
										// let's skip warning dialog call
										try {
											encryptButtonCallbackInterface.onButtonPressed(encryptIntent);
										} catch (Exception e) {
											e.printStackTrace();
											Toast.makeText(activity,
														   activity.getResources().getString(R.string.crypt_encryption_fail),
														   Toast.LENGTH_LONG).show();
										}
									} else {
										GeneralDialogCreation.showEncryptWarningDialog(encryptIntent,
																					   fileFrag, activity.getAppTheme(), encryptButtonCallbackInterface);
									}
									break;
								case R.id.decrypt:
									EncryptDecryptUtils.decryptFile(activity, activity, fileFrag,
																	fileFrag.openMode, rowItem.generateBaseFile(),
																	rowItem.generateBaseFile().getParent(activity), activity, false);
									break;
								case R.id.hide:
									fileFrag.dataUtils.addHiddenFile(rowItem.path);
//									if (new File(rowItem.path).isDirectory()) {
//										File f1 = new File(rowItem.path + "/" + ".nomedia");
//										if (!f1.exists()) {
//											try {
//												activity.mainActivityHelper.mkFile(new HFile(OpenMode.FILE, f1.getPath()), fileFrag);
//											} catch (Exception e) {
//												e.printStackTrace();
//											}
//										}
//										Futils.scanFile(rowItem.path, activity);
//									}
									fileFrag.updateList();
									//GeneralDialogCreation.showHiddenDialog(activity.dataUtils, activity.getFutils(), fileFrag, activity.getAppTheme());
									break;
								case R.id.book:
									DataUtils dataUtils = DataUtils.getInstance();
									dataUtils.addBook(new String[]{rowItem.name, rowItem.path}, true);
									activity.refreshDrawer();
									Toast.makeText(activity, activity.getResources().getString(R.string.bookmarksadded), Toast.LENGTH_LONG).show();
									break;
								case R.id.extract:
									//activity.mainActivityHelper.extractFile(new File(rowItem.path));
									activity.decompress(rowItem.path, fileFrag.currentPathTitle + "/" + rowItem.name.substring(0, rowItem.name.lastIndexOf(".")));
									break;
								case R.id.compress:
									ArrayList<BaseFile> copies1 = new ArrayList<>();
									copies1.add(rowItem.bf);
									GeneralDialogCreation.showCompressDialog(activity, copies1, fileFrag.currentPathTitle);
									break;
							}

							return true ;
						}
						@Override
						public void onMenuModeChange(MenuBuilder menu) {}
					});
				optionsMenu.show();
				return;
			}
			final File f = rowItem.bf.f;//new File(fPath);
			final String fPath = f.getAbsolutePath();//(String) v.getContentDescription();
			final String path;
			if (!rowItem.hasSymlink()) {
				path = rowItem.path;
			} else {
				path = rowItem.getSymlink();
			}
			Log.d(TAG, "onClick, " + fPath + ", " + v);
			if (fPath == null) {
				return;
			}
			//Log.d(TAG, "currentSelectedList " + Util.collectionToString(selectedInList1, true, "\r\n"));
			//Log.d(TAG, "selectedInList.contains(f) " + selectedInList1.contains(f));
			//Log.d(TAG, "multiFiles " + multiFiles);
			//Log.d(TAG, "f.exists() " + f.exists());
			if (f.exists()) {
				if (!f.canRead()) {
					fileFrag.showToast(f + " cannot be read");
				} else {
					boolean inSelected = false;
					if (fileFrag.dataSourceL2 != null)
						for (LayoutElement st : fileFrag.dataSourceL2) {
							if (f.equals(st) || fPath.startsWith(st.path + "/")) {
								inSelected = true;
								break;
							}
						}
					if (!inSelected) {
						if (fileFrag.multiFiles) {// || fileFrag.suffix != null && fileFrag.suffix.length() == 0
							final int id = v.getId();
							if (id == R.id.icon) {
								fileFrag.tempPreviewL2 = rowItem;
								if (f.isFile()) {
									load(rowItem, f, fPath, pos);
								} else if (fileFrag.slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {//ContentFragment dir//fileFrag.type == -1
									if (fileFrag.activity.slideFrag2 != null) {
										Frag frag = fileFrag.activity.slideFrag2.getCurrentFragment();
										if (frag.type == Frag.TYPE.EXPLORER) {
											((ContentFragment)frag).changeDir(path, true);
										} else {
											fileFrag.activity.slideFrag2.setCurrentItem(fileFrag.activity.slideFrag2.indexOfAdapter(fileFrag.activity.curExplorerFrag), true);
											fileFrag.activity.curExplorerFrag.changeDir(path, true);
										}
									}
								} else {//dir
									Frag frag = fileFrag.activity.slideFrag.getCurrentFragment();
									if (frag.type == Frag.TYPE.EXPLORER) {
										((ContentFragment)frag).changeDir(path, true);
									} else {
										fileFrag.activity.slideFrag.setCurrentItem(fileFrag.activity.slideFrag.indexOfAdapter(fileFrag.activity.curContentFrag), true);
										fileFrag.activity.curContentFrag.changeDir(path, true);
									}
//									fileFrag.activity.curContentFrag.changeDir(fPath, true);
								}
								if (fileFrag.selectedInList1.size() > 0) {
									if (fileFrag.selectedInList1.remove(rowItem)) {
										if (fileFrag.selectedInList1.size() == 0 && fileFrag.activity.COPY_PATH == null && fileFrag.activity.MOVE_PATH == null && fileFrag.commands.getVisibility() == View.VISIBLE) {
											fileFrag.horizontalDivider6.setVisibility(View.GONE);
											fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(fileFrag.activity, R.anim.shrink_from_top));
											fileFrag.commands.setVisibility(View.GONE);
										}
									} else {
										fileFrag.selectedInList1.add(rowItem);
									}
								}
							} else if (id == R.id.cbx) {//file and folder
								if (fileFrag.selectedInList1.remove(rowItem)) {
									if (fileFrag.selectedInList1.size() == 0 && fileFrag.activity.COPY_PATH == null && fileFrag.activity.MOVE_PATH == null && fileFrag.commands.getVisibility() == View.VISIBLE) {
										fileFrag.horizontalDivider6.setVisibility(View.GONE);
										fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(fileFrag.activity, R.anim.shrink_from_top));
										fileFrag.commands.setVisibility(View.GONE);
									}
								} else {
									fileFrag.selectedInList1.add(rowItem);
									if (fileFrag.commands.getVisibility() == View.GONE) {
										fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(fileFrag.activity, R.anim.grow_from_bottom));
										fileFrag.commands.setVisibility(View.VISIBLE);
										fileFrag.horizontalDivider6.setVisibility(View.VISIBLE);
									}
								}
							} else if (f.isDirectory()) { 
								if (fileFrag.selectedInList1.size() == 0 && fileFrag.type != Frag.TYPE.SELECTION) { 
									fileFrag.changeDir(path, true);
								} else {
									if (fileFrag.selectedInList1.remove(rowItem)) {
										if (fileFrag.selectedInList1.size() == 0 && fileFrag.activity.COPY_PATH == null && fileFrag.activity.MOVE_PATH == null && fileFrag.commands.getVisibility() == View.VISIBLE) {
											fileFrag.horizontalDivider6.setVisibility(View.GONE);
											fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(fileFrag.activity, R.anim.shrink_from_top));
											fileFrag.commands.setVisibility(View.GONE);
										} 
									} else {
										fileFrag.selectedInList1.add(rowItem);
										if (fileFrag.commands.getVisibility() == View.GONE) {
											fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(fileFrag.activity, R.anim.grow_from_bottom));
											fileFrag.commands.setVisibility(View.VISIBLE);
											fileFrag.horizontalDivider6.setVisibility(View.VISIBLE);
										}
									}
								}
							} else if (f.isFile()) { 
								if (fileFrag.selectedInList1.size() == 0) { 
									openFile(f);
								} else {
									if (fileFrag.selectedInList1.remove(rowItem)) {
										if (fileFrag.selectedInList1.size() == 0 && fileFrag.activity.COPY_PATH == null && fileFrag.activity.MOVE_PATH == null && fileFrag.commands.getVisibility() == View.VISIBLE) {
											fileFrag.horizontalDivider6.setVisibility(View.GONE);
											fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(fileFrag.activity, R.anim.shrink_from_top));
											fileFrag.commands.setVisibility(View.GONE);
										} 
									} else {
										fileFrag.selectedInList1.add(rowItem);
										if (fileFrag.commands.getVisibility() == View.GONE) {
											fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(fileFrag.activity, R.anim.grow_from_bottom));
											fileFrag.commands.setVisibility(View.VISIBLE);
											fileFrag.horizontalDivider6.setVisibility(View.VISIBLE);
										}
									}
								}
							}
							if ((fileFrag.currentPathTitle == null || fileFrag.currentPathTitle.length() > 0)) {
								fileFrag.selectionStatus1.setText(fileFrag.selectedInList1.size() 
																  + "/" + fileFrag.dataSourceL1.size());
							}
						} else { //!multifile no preview
							if (f.isFile()) {
								// chọn mới đầu tiên
								if (v.getId() == R.id.cbx) {
									if (fileFrag.selectedInList1.size() == 0) {
										fileFrag.selectedInList1.add(rowItem);
										if (fileFrag.commands.getVisibility() == View.GONE) {
											fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(fileFrag.activity, R.anim.grow_from_bottom));
											fileFrag.commands.setVisibility(View.VISIBLE);
											fileFrag.horizontalDivider6.setVisibility(View.VISIBLE);
										}
									} else if (fileFrag.selectedInList1.size() > 0) {
										if (fileFrag.selectedInList1.remove(rowItem)) { // đã chọn
											if (fileFrag.selectedInList1.size() == 0 && fileFrag.activity.COPY_PATH == null && fileFrag.activity.MOVE_PATH == null && fileFrag.commands.getVisibility() == View.VISIBLE) {
												fileFrag.horizontalDivider6.setVisibility(View.GONE);
												fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(fileFrag.activity, R.anim.shrink_from_top));
												fileFrag.commands.setVisibility(View.GONE);
											}
										} else { // chọn mới bỏ cũ
											fileFrag.selectedInList1.clear();
											fileFrag.selectedInList1.add(rowItem);
										}
									}
								} else {
									openFile(f);
								}
							} else { //", "Directory
								fileFrag.selectedInList1.clear();
								if (fileFrag.currentPathTitle == null || fileFrag.currentPathTitle.length() > 0) {
									fileFrag.changeDir(path, true);
								}
							}
						}
						notifyDataSetChanged();
					} else { // inselected
						if (f.isFile()) {
							if (v.getId() == R.id.icon) {
								load(rowItem, f, fPath, pos);
							} else {
								openFile(f);
							}
						} else if (v.getId() == R.id.icon) { //dir
							fileFrag.tempPreviewL2 = rowItem;
							fileFrag.activity.slideFrag2.setCurrentItem(Frag.TYPE.EXPLORER.ordinal(), true);
							fileFrag.activity.curExplorerFrag.changeDir(fPath, true);
						} 
					}
				}
			} else {
				fileFrag.changeDir(f.getParentFile().getAbsolutePath(), true);
			}
			fileFrag.updateDelPaste();
		}
	}

	static int getFragIndex(final ContentFragment fileFrag, final Frag.TYPE t) {
		final SlidingTabsFragment.PagerAdapter pagerAdapter;
		if (fileFrag.slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
			pagerAdapter = fileFrag.activity.slideFrag2.pagerAdapter;
		} else {
			pagerAdapter = fileFrag.activity.slideFrag.pagerAdapter;
		}
		final int count = pagerAdapter.getCount();
		if (count > 1) {
			for (int i = 1; i < count - 1; i++) {
				if (pagerAdapter.getItem(i).type == t) {
					return i;
				}
			}
			return -1;
		} else {
			return pagerAdapter.getItem(0).type == t ? 0 : -1;
		}
	}

	private void load(final LayoutElement ele, final File f, final String fPath, final int pos) throws IllegalStateException {
		if (fileFrag.activity.slideFrag2 == null) {
			Log.d(TAG, "Single panel only");
			return;
		}
		// check if we're trying to click on encrypted file
		if (!f.isDirectory() &&
			fPath.endsWith(CryptUtil.CRYPT_EXTENSION)) {
			// decrypt the file
			fileFrag.isEncryptOpen = true;

			fileFrag.encryptBaseFile = new BaseFile(fileFrag.activity.getExternalCacheDir().getPath()
													+ "/"
													+ new LayoutElement(f).generateBaseFile().getName().replace(CryptUtil.CRYPT_EXTENSION, ""));

			EncryptDecryptUtils.decryptFile(fileFrag.activity, fileFrag.activity, fileFrag, fileFrag.openMode,
											new LayoutElement(f).generateBaseFile(), fileFrag.activity.getExternalCacheDir().getPath(),
											fileFrag.activity, true);
			return;
		} else {
			switch (ele.getMode()) {
				case SMB:
					try {
						SmbFile smbFile = new SmbFile(ele.path);
						fileFrag.launchSMB(smbFile, f.length(), fileFrag.activity);
					} catch (MalformedURLException ex) {
						ex.printStackTrace();
					}
					break;
				case OTG:
					fileFrag.activity.getFutils().openFile(OTGUtil.getDocumentFile(ele.path, fileFrag.activity, false),
														   fileFrag.activity);
					break;
				case DROPBOX:
				case BOX:
				case GDRIVE:
				case ONEDRIVE:
					Toast.makeText(fileFrag.activity, fileFrag.activity.getResources().getString(R.string.please_wait), Toast.LENGTH_LONG).show();
					CloudUtil.launchCloud(ele.generateBaseFile(), fileFrag.openMode, fileFrag.activity);
					break;
				default:
					fileFrag.activity.getFutils().openFile(f, fileFrag.activity);
					break;
			}
			fileFrag.dataUtils.addHistoryFile(ele.path);
		}
		final String mime = MimeTypes.getMimeType(f);
		Log.d(TAG, fPath + "=" + mime);
		//int i = 0;
		int tabIndex1 = 0;
		int tabIndex2 = 0;
		final SlidingTabsFragment.PagerAdapter pagerAdapter;
		final SlidingTabsFragment slidingTabsFragment;
		if (fileFrag.slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
			pagerAdapter = fileFrag.activity.slideFrag2.pagerAdapter;
			slidingTabsFragment = fileFrag.activity.slideFrag2;
		} else {
			pagerAdapter = fileFrag.activity.slideFrag.pagerAdapter;
			slidingTabsFragment = fileFrag.activity.slideFrag;
		}
		if (mime.startsWith("text/html") || mime.startsWith("text/xhtml")) {
			tabIndex1 = getFragIndex(fileFrag, Frag.TYPE.TEXT);
			tabIndex2 = getFragIndex(fileFrag, Frag.TYPE.WEB);
			if (tabIndex1 >= 0) {
				pagerAdapter.getItem(tabIndex1).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex1, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.TEXT, fPath);
				fileFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
						}
					}, 50);
			}
			if (tabIndex2 >= 0) {
				pagerAdapter.getItem(tabIndex2).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				fileFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							slidingTabsFragment.addTab(Frag.TYPE.WEB, fPath);
							fileFrag.listView.postDelayed(new Runnable() {
									@Override
									public void run() {
										pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
									}
								}, 50);
						}
					}, 100);
			}
			//pagerAdapter.getItem(i = Frag.TYPE.TEXT.ordinal()).load(fPath);
			//pagerAdapter.getItem(i = Frag.TYPE.WEB.ordinal()).load(fPath);
		} else if (mime.startsWith("application/vnd.android.package-archive")) {
			final StringBuilder sb = new StringBuilder(ExplorerActivity.DOCTYPE);
			try {
				ApkParser apkParser = new ApkParser(f);
				sb.append(AndroidUtils.getSignature(fileFrag.activity, fPath));
				sb.append("\nVerify apk " + apkParser.verifyApk());
				sb.append("\nMeta data " + apkParser.getApkMeta());

				String sb1 = sb.toString();

				String sb2 = "\nAndroidManifest.xml \n" + apkParser.getManifestXml().replaceAll("&", "&amp;")
					.replaceAll("\"", "&quot;")
					.replaceAll("'", "&#39;")
					.replaceAll("<", "&lt;")
					.replaceAll(">", "&gt;");
				sb.append(sb2);
				sb.append(ExplorerActivity.END_PRE);
				final String name = ExplorerApplication.PRIVATE_PATH + "/" + f.getName() + ".html";
				FileUtil.writeFileAsCharset(new File(name), sb.toString(), "utf-8");
				//pagerAdapter.getItem(i = Frag.TYPE.WEB.ordinal()).load(name);
				tabIndex2 = getFragIndex(fileFrag, Frag.TYPE.WEB);
				if (tabIndex2 >= 0) {
					pagerAdapter.getItem(tabIndex2).load(name);
					slidingTabsFragment.setCurrentItem(tabIndex2, true);
				} 
				if (tabIndex2 < 0) {
					slidingTabsFragment.addTab(Frag.TYPE.WEB, name);
					fileFrag.listView.postDelayed(new Runnable() {
							@Override
							public void run() {
								pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(name);
							}
						}, 50);
				}
				byte[] bytes = FileUtil.readFileToMemory(f);
				new FillClassesNamesThread(fileFrag, bytes, f, sb1, sb2, ExplorerActivity.END_PRE).start();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} else if (mime.startsWith("application/pdf")) {
			//pagerAdapter.getItem(i = Frag.TYPE.PDF.ordinal()).load(fPath);
			tabIndex2 = getFragIndex(fileFrag, Frag.TYPE.PDF);
			if (tabIndex2 >= 0) {
				pagerAdapter.getItem(tabIndex2).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.PDF, fPath);
				fileFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
						}
					}, 50);
			}
		} else if (mime.startsWith("image/svg+xml")) {
			//pagerAdapter.getItem(i = Frag.TYPE.TEXT.ordinal()).load(fPath);
			//pagerAdapter.getItem(i = Frag.TYPE.PHOTO.ordinal()).load(fPath);
			tabIndex1 = getFragIndex(fileFrag, Frag.TYPE.TEXT);
			tabIndex2 = getFragIndex(fileFrag, Frag.TYPE.PHOTO);
			if (tabIndex1 >= 0) {
				pagerAdapter.getItem(tabIndex1).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex1, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.TEXT, fPath);
				fileFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
						}
					}, 50);
			}
			if (tabIndex2 >= 0) {
				pagerAdapter.getItem(tabIndex2).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				fileFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							slidingTabsFragment.addTab(Frag.TYPE.PHOTO, fPath);
							fileFrag.listView.postDelayed(new Runnable() {
									@Override
									public void run() {
										pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
									}
								}, 50);
						}
					}, 100);
			}

//		} else if (mime.startsWith("application/epub+zip")
//				 || mime.startsWith("application/x-pilot-prc")
//				 || mime.startsWith("application/x-mobipocket-ebook")
//				 || mime.startsWith("application/x-fictionbook+xml")
//				 || mime.startsWith("application/x-fictionbook")
//				 || f.getName().toLowerCase().endsWith(".doc")) {
//			pagerAdapter.getItem(i = Frag.TYPE.FBReader.ordinal()).load(fPath);
		} else if (mime.startsWith("text")) {
			//pagerAdapter.getItem(i = Frag.TYPE.TEXT.ordinal()).load(fPath);
			tabIndex2 = getFragIndex(fileFrag, Frag.TYPE.TEXT);
			if (tabIndex2 >= 0) {
				pagerAdapter.getItem(tabIndex2).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.TEXT, fPath);
				fileFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
						}
					}, 50);
			}
		} else if (mime.startsWith("video")) {
			//pagerAdapter.getItem(i = Frag.TYPE.MEDIA.ordinal()).load(fPath);
			tabIndex2 = getFragIndex(fileFrag, Frag.TYPE.MEDIA);
			if (tabIndex2 >= 0) {
				pagerAdapter.getItem(tabIndex2).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.MEDIA, fPath);
				fileFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
						}
					}, 500);
			}
		} else if (mime.startsWith("image")) {
			//pagerAdapter.getItem(i = Frag.TYPE.PHOTO.ordinal()).open(pos, mDataset);
			tabIndex2 = getFragIndex(fileFrag, Frag.TYPE.PHOTO);
			if (tabIndex2 >= 0) {
				pagerAdapter.getItem(tabIndex2).open(pos, mDataset);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.PHOTO, fPath);
				fileFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							pagerAdapter.getItem(slidingTabsFragment.pageSelected).open(pos, mDataset);
						}
					}, 50);
			}
		} else if (mime.startsWith("audio")) {
			//pagerAdapter.getItem(i = Frag.TYPE.MEDIA.ordinal()).load(fPath);
			tabIndex2 = getFragIndex(fileFrag, Frag.TYPE.MEDIA);
			if (tabIndex2 >= 0) {
				pagerAdapter.getItem(tabIndex2).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.MEDIA, fPath);
				fileFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
						}
					}, 500);
			}
		} else {
			fileFrag.tempPreviewL2 = null;
		}
	}

	private void openFile(final File f) {
		try {
			final Uri uri = Uri.fromFile(f);
			final Intent i = new Intent(Intent.ACTION_VIEW); 
			i.addCategory(Intent.CATEGORY_DEFAULT);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

			Log.d("i.setData(uri)", uri + "." + i);
			final String mimeType = MimeTypes.getMimeType(f);
			i.setDataAndType(uri, mimeType);//floor.getValue()
			Log.d(TAG, f + "=" + mimeType);
			final Intent createChooser = Intent.createChooser(i, "View");
			Log.i("createChooser.getExtras()", AndroidUtils.bundleToString(createChooser.getExtras()));
			fileFrag.startActivity(createChooser);
		} catch (Throwable e) {
			Toast.makeText(fileFrag.activity, "unable to view !\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	private class OnLongClickListener implements View.OnLongClickListener {
		private final int pos;
		OnLongClickListener(int pos) {
			this.pos = pos;
		}
		@Override
		public boolean onLongClick(final View v) {
			fileFrag.select(true);
//			if (fileFrag.type == -1) {
//				fileFrag.activity.slideFrag2.getCurrentFragment().select(false);
//			} else {
//				fileFrag.activity.slideFrag2.getCurrentFragment().select(true);
//			}
			final LayoutElement rowItem = mDataset.get(pos);//Integer.valueOf(v.getContentDescription().toString())
			final File f = rowItem.bf.f;//new File(fPath);
			final String fPath = f.getAbsolutePath();//(String) v.getContentDescription();

			if (!f.exists()) {
				fileFrag.changeDir(fPath, true);
				return true;
			} else if (!f.canRead()) {
				fileFrag.showToast(f + " cannot be read");
				return true;
			}
			Log.d(TAG, "onLongClick, " + fPath);
			Log.d(TAG, "currentSelectedList" + Util.collectionToString(fileFrag.selectedInList1, true, "\r\n"));
			Log.d(TAG, "selectedInList.contains(f) " + fileFrag.selectedInList1.contains(f));
			Log.d(TAG, "multiFiles " + fileFrag.multiFiles);

			boolean inSelectedFiles = false;
			if (fileFrag.dataSourceL2 != null)
				for (LayoutElement st : fileFrag.dataSourceL2) {
					if (f.equals(st) || fPath.startsWith(st.path + "/")) {
						inSelectedFiles = true;
						break;
					}
				}
			if (!inSelectedFiles) {
				if (fileFrag.multiFiles || fileFrag.suffix.length() == 0) {
					if (fileFrag.selectedInList1.remove(rowItem)) {
						if (fileFrag.selectedInList1.size() == 0 && fileFrag.activity.COPY_PATH == null && fileFrag.activity.MOVE_PATH == null && fileFrag.commands.getVisibility() == View.VISIBLE) {
							fileFrag.horizontalDivider6.setVisibility(View.GONE);
							fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(fileFrag.activity, R.anim.shrink_from_top));
							fileFrag.commands.setVisibility(View.GONE);
						} 
					} else {
						fileFrag.selectedInList1.add(rowItem);
						if (fileFrag.commands.getVisibility() == View.GONE) {
							fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(fileFrag.activity, R.anim.grow_from_bottom));
							fileFrag.commands.setVisibility(View.VISIBLE);
							fileFrag.horizontalDivider6.setVisibility(View.VISIBLE);
						}
					}
					if ((fileFrag.currentPathTitle == null || fileFrag.currentPathTitle.length() > 0)) {
						fileFrag.selectionStatus1.setText(fileFrag.selectedInList1.size() 
														  + "/" + fileFrag.dataSourceL1.size());
					}
				} else { // single file
					if (f.isFile()) {
						// chọn mới đầu tiên
						if (fileFrag.selectedInList1.size() == 0) {
							fileFrag.selectedInList1.add(rowItem);
							if (fileFrag.commands.getVisibility() == View.GONE) {
								fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(fileFrag.activity, R.anim.grow_from_bottom));
								fileFrag.commands.setVisibility(View.VISIBLE);
								fileFrag.horizontalDivider6.setVisibility(View.VISIBLE);
							}
						} else if (fileFrag.selectedInList1.size() > 0) {
							if (fileFrag.selectedInList1.remove(rowItem)) {
								// đã chọn
								if (fileFrag.selectedInList1.size() == 0 && fileFrag.activity.COPY_PATH == null && fileFrag.activity.MOVE_PATH == null && fileFrag.commands.getVisibility() == View.VISIBLE) {
									fileFrag.horizontalDivider6.setVisibility(View.GONE);
									fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(fileFrag.activity, R.anim.shrink_from_top));
									fileFrag.commands.setVisibility(View.GONE);
								} 
							} else {
								// chọn mới bỏ cũ
								fileFrag.selectedInList1.clear();
								fileFrag.selectedInList1.add(rowItem);
							}
						}
					} else { //", "Directory
						fileFrag.selectedInList1.clear();
						if (fileFrag.currentPathTitle == null || fileFrag.currentPathTitle.length() > 0) {
							fileFrag.changeDir(fileFrag.dirTemp4Search, true);
						}
					}
				}
				notifyDataSetChanged();
			} 
			fileFrag.updateDelPaste();
			return true;
		}
	}

}
