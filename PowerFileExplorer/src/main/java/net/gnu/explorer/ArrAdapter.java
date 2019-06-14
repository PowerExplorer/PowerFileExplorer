package net.gnu.explorer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.amaze.filemanager.activities.ThemedActivity;
import com.amaze.filemanager.filesystem.BaseFile;
import com.amaze.filemanager.fragments.preference_fragments.Preffrag;
import com.amaze.filemanager.services.EncryptService;
import com.amaze.filemanager.ui.LayoutElement;
import com.amaze.filemanager.ui.dialogs.GeneralDialogCreation;
import com.amaze.filemanager.ui.icons.MimeTypes;
import com.amaze.filemanager.utils.DataUtils;
import com.amaze.filemanager.utils.OTGUtil;
import com.amaze.filemanager.utils.cloud.CloudUtil;
import com.amaze.filemanager.utils.files.CryptUtil;
import com.amaze.filemanager.utils.files.EncryptDecryptUtils;
import com.amaze.filemanager.utils.files.Futils;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import jcifs.smb.SmbFile;
import net.dongliu.apk.parser.ApkParser;
import net.gnu.androidutil.AndroidUtils;
import net.gnu.util.FileUtil;
import net.gnu.util.Util;
import net.gnu.common.*;

public class ArrAdapter extends RecyclerAdapter<LayoutElement, ArrAdapter.ViewHolder> {

	private static final String TAG = "ArrAdapter";

	private final int backgroundResource;
	private final ContentFragment contentFrag;

	public void toggleChecked(final boolean checked) {
		if (checked) {
			contentFrag.allCbx.setSelected(true);
			contentFrag.selectedInList1.clear();
			contentFrag.selectedInList1.addAll(contentFrag.dataSourceL1);
			contentFrag.allCbx.setImageResource(R.drawable.ic_accept);
		} else {
			contentFrag.allCbx.setSelected(false);
			contentFrag.selectedInList1.clear();
			contentFrag.allCbx.setImageResource(R.drawable.dot);
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

			more.setColorFilter(Constants.TEXT_COLOR);

			name.setTextColor(Constants.DIR_COLOR);
			size.setTextColor(Constants.TEXT_COLOR);
			attr.setTextColor(Constants.TEXT_COLOR);
			lastModified.setTextColor(Constants.TEXT_COLOR);
			if (type != null) {
				type.setTextColor(Constants.TEXT_COLOR);
			}
			image.setScaleType(ImageView.ScaleType.FIT_CENTER);
			convertView.setTag(this);
			this.convertedView = convertView;
		}
	}

	public ArrAdapter(final ContentFragment fileFrag, final List<LayoutElement> objects) {
		super(objects);
		this.contentFrag = fileFrag;

		final int[] attrs = new int[]{R.attr.selectableItemBackground};
		final TypedArray typedArray = fileFrag.activity.obtainStyledAttributes(attrs);
		backgroundResource = typedArray.getResourceId(0, 0);
		typedArray.recycle();
	}

	@Override
	public int getItemViewType(final int position) {
		if (contentFrag.dataSourceL1.size() == 0) {
			return 0;
		} else if (contentFrag.spanCount == 1 && contentFrag.slidingTabsFragment.width >= 0
				   || (contentFrag.spanCount == 2 && (contentFrag.activity.right.getVisibility() == View.GONE || contentFrag.activity.left.getVisibility() == View.GONE))
				   ) {
			return 1;
		} else if (contentFrag.spanCount == 1 && contentFrag.slidingTabsFragment.width < 0
				   || contentFrag.spanCount == 2 && contentFrag.slidingTabsFragment.width >= 0) {
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
//		if (!le.bf.exists()) {
//			mDataset.remove(le);
//			contentFrag.selectedInList1.remove(le);
//			notifyItemRemoved(position);
//		}
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

		if (contentFrag.type == Frag.TYPE.EXPLORER) {
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

		if (contentFrag.currentPathTitle == null || contentFrag.currentPathTitle.length() > 0) {
			name.setEllipsize(TextUtils.TruncateAt.MIDDLE);
		} else {
			name.setEllipsize(TextUtils.TruncateAt.START);
		}

		//Log.d("f.getAbsolutePath()", f.getAbsolutePath());
		//Log.d("curSelectedFiles", curSelectedFiles.toString());
		if (contentFrag.selectedInList1.contains(le)) {
			convertedView.setBackgroundColor(Constants.SELECTED_IN_LIST);
			cbx.setImageResource(R.drawable.ic_accept);
			cbx.setSelected(true);
			cbx.setEnabled(true);
			if ((contentFrag.currentPathTitle == null || contentFrag.currentPathTitle.length() > 0) && contentFrag.selectedInList1.size() == contentFrag.dataSourceL1.size()) {
				contentFrag.allCbx.setSelected(true);
				contentFrag.allCbx.setImageResource(R.drawable.ic_accept);
			}
		} else {
			boolean inDataSource2 = false;
			boolean isPartial = false;
			//Log.d(TAG, "dataSource2" + Util.collectionToString(dataSourceL2, true, "\n"));
			if (contentFrag.multiFiles && contentFrag.dataSourceL2 != null) {
				final String fPathD = fPath + "/";
				String f2Path;
				for (LayoutElement f2 : contentFrag.dataSourceL2) {
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
				convertedView.setBackgroundColor(Constants.IN_DATA_SOURCE_2);
				cbx.setImageResource(R.drawable.ic_accept);
				cbx.setSelected(true);
				cbx.setEnabled(false);
				if ((contentFrag.currentPathTitle == null || contentFrag.currentPathTitle.length() > 0) && contentFrag.selectedInList1.size() == contentFrag.dataSourceL1.size()) {
					contentFrag.allCbx.setSelected(true);
					contentFrag.allCbx.setImageResource(R.drawable.ic_accept);
				}
			} else if (isPartial) {
				convertedView.setBackgroundColor(Constants.IS_PARTIAL);
				cbx.setImageResource(R.drawable.ready);
				cbx.setSelected(false);
				cbx.setEnabled(true);
				contentFrag.allCbx.setSelected(false);
				if (contentFrag.selectedInList1.size() == 0) {
					contentFrag.allCbx.setImageResource(R.drawable.dot);
				} else {
					contentFrag.allCbx.setImageResource(R.drawable.ready);
				}
			} else {
				convertedView.setBackgroundResource(backgroundResource);
				if (contentFrag.selectedInList1.size() > 0) {
					cbx.setImageResource(R.drawable.ready);
					contentFrag.allCbx.setImageResource(R.drawable.ready);
				} else {
					cbx.setImageResource(R.drawable.dot);
					contentFrag.allCbx.setImageResource(R.drawable.dot);
				}
				cbx.setSelected(false);
				cbx.setEnabled(true);
				contentFrag.allCbx.setSelected(false);

			}
		}
		if (contentFrag.tempPreviewL2 != null && contentFrag.tempPreviewL2.equals(le)) {
			convertedView.setBackgroundColor(Constants.LIGHT_GREY);
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
			if (viewType == 1) {
				final int lastIndexOf = fName.lastIndexOf(".");
				type.setText(lastIndexOf >= 0 && lastIndexOf < fName.length() - 1 ? fName.substring(lastIndexOf + 1) : "");
				lastModified.setText(Util.dtf.format(le.lastModified));
			} else if (viewType == 2) {
				if (contentFrag.slidingTabsFragment.width != 0) {
					lastModified.setText(Util.dtf.format(le.lastModified));
				} else {
					lastModified.setText(Util.df.format(le.lastModified));
				}
			} else {
				size.setText(Formatter.formatFileSize(contentFrag.activity, le.length));
				lastModified.setText(Util.df.format(le.lastModified));
			}
			size.setText(Util.nf.format(le.length) + " B");
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
			if (viewType == 1) {
				type.setText("Folder");
				lastModified.setText(Util.dtf.format(le.lastModified));
			} else if (viewType == 2) {
				if (contentFrag.slidingTabsFragment.width != 0) {
					lastModified.setText(Util.dtf.format(le.lastModified));
				} else {
					lastModified.setText(Util.df.format(le.lastModified));
				}
			} else {
				lastModified.setText(Util.df.format(le.lastModified));
			}
		}
		contentFrag.imageLoader.displayImage(le.bf.f, contentFrag.getContext(), image, contentFrag.spanCount);
	}

	static void encrypt(final ExplorerActivity activity, final ContentFragment contentFrag, final LayoutElement... rowItems) throws Resources.NotFoundException {
		final Intent encryptIntent = new Intent(activity, EncryptService.class);
		encryptIntent.putExtra(EncryptService.TAG_OPEN_MODE, rowItems[0].getMode().ordinal());
		encryptIntent.putExtra(EncryptService.TAG_CRYPT_MODE,
							   EncryptService.CryptEnum.ENCRYPT.ordinal());
		//encryptIntent.putExtra(EncryptService.TAG_SOURCE, rowItem.generateBaseFile());
		Log.d(TAG, "encrypt " + encryptIntent);

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);

		final EncryptDecryptUtils.EncryptButtonCallbackInterface encryptButtonCallbackInterfaceAuthenticate =
			new EncryptDecryptUtils.EncryptButtonCallbackInterface() {
			@Override
			public void onButtonPressed(Intent intent) {
			}

			@Override
			public void onButtonPressed(Intent intent, String password) throws Exception {
				EncryptDecryptUtils.startEncryption(activity,
													rowItems
													//.generateBaseFile().getPath()
													, password
													//, intent
													);
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
														rowItems
														//.generateBaseFile().getPath()
														,
														Preffrag.ENCRYPT_PASSWORD_MASTER
														//, encryptIntent
														);
				} else if (preferences.getBoolean(Preffrag.PREFERENCE_CRYPT_FINGERPRINT,
												  Preffrag.PREFERENCE_CRYPT_FINGERPRINT_DEFAULT)) {

					EncryptDecryptUtils.startEncryption(activity,
														rowItems
														//.generateBaseFile().getPath()
														,
														Preffrag.ENCRYPT_PASSWORD_FINGERPRINT
														//, encryptIntent
														);
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
														   contentFrag, activity.getAppTheme(), encryptButtonCallbackInterface);
		}
	}

	private class OnClickListener implements View.OnClickListener {
		private final int pos;
		private OnClickListener(int pos) {
			this.pos = pos;
		}
		@Override
		public void onClick(final View v) {
			contentFrag.select(true);
			//final Integer pos = Integer.valueOf(v.getContentDescription().toString());
			final LayoutElement rowItem = mDataset.get(pos);
			if (v.getId() == R.id.more) {

				final MenuBuilder menuBuilder = new MenuBuilder(contentFrag.activity);
				final MenuInflater inflater = new MenuInflater(contentFrag.activity);
				inflater.inflate(R.menu.file_commands, menuBuilder);
				final MenuPopupHelper optionsMenu = new MenuPopupHelper(contentFrag.activity , menuBuilder, contentFrag.searchButton);
				optionsMenu.setForceShowIcon(true);

				int num= menuBuilder.size();
				for (int i = 0; i < num; i++) {
					Drawable icon = menuBuilder.getItem(i).getIcon();
					if (icon != null) {
						icon.setColorFilter(Constants.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
					}
				}

				MenuItem findItem = menuBuilder.findItem(R.id.extract);
				//Log.d(TAG, rowItem.name + ", " + findItem + ", " + FileUtil.extractiblePattern.matcher(rowItem.name).matches());
				if (rowItem.isDirectory || !FileUtil.extractiblePattern.matcher(rowItem.name).matches()) {
					findItem.setTitle("Compress");
				}

				findItem = menuBuilder.findItem(R.id.encrypt);
				//Log.d(TAG, rowItem.name + ", " + findItem + ", " + FileUtil.extractiblePattern.matcher(rowItem.name).matches());
				if (!rowItem.isDirectory && rowItem.name.toLowerCase().endsWith(CryptUtil.CRYPT_EXTENSION)) {
					findItem.setTitle("Decrypt");
				}
				menuBuilder.setCallback(new MenuBuilder.Callback() {
						@Override
						public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
							final ExplorerActivity activity = contentFrag.activity;
							switch (item.getItemId()) {
								case R.id.copy:
									//copy(v);
									contentFrag.activity.MOVE_PATH = null;
									ArrayList<BaseFile> copies = new ArrayList<>(1);
									copies.add(rowItem.generateBaseFile());
									activity.COPY_PATH = copies;
									if (activity.curExplorerFrag.commands.getVisibility() == View.GONE) {
										activity.curExplorerFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
										activity.curExplorerFrag.commands.setVisibility(View.VISIBLE);
										activity.curExplorerFrag.horizontalDivider6.setVisibility(View.VISIBLE);
									}
									activity.curExplorerFrag.updateDelPaste();
									if (activity.curContentFrag.commands.getVisibility() == View.GONE) {
										activity.curContentFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
										activity.curContentFrag.commands.setVisibility(View.VISIBLE);
										activity.curContentFrag.horizontalDivider6.setVisibility(View.VISIBLE);
									}
									activity.curContentFrag.updateDelPaste();
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
									}
									activity.curExplorerFrag.updateDelPaste();
									if (activity.curContentFrag.commands.getVisibility() == View.GONE) {
										activity.curContentFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
										activity.curContentFrag.commands.setVisibility(View.VISIBLE);
										activity.curContentFrag.horizontalDivider6.setVisibility(View.VISIBLE);
									}
									activity.curContentFrag.updateDelPaste();
									break;
								case R.id.rename:
									contentFrag.rename(rowItem.generateBaseFile());
									break;
								case R.id.delete:
									ArrayList<LayoutElement> ele = new ArrayList<LayoutElement>(1);
									ele.add(rowItem);
									final Runnable r = new Runnable() {
										@Override
										public void run() {
											contentFrag.listView.postDelayed(new Runnable() {
													@Override
													public void run() {
														contentFrag.dataSourceL1.remove(rowItem);
														contentFrag.selectedInList1.remove(rowItem);
														contentFrag.srcAdapter.notifyDataSetChanged();
													}
											}, 0);
										}
									};
									//new Futils().deleteFiles(ele, fileFrag.activity, /*positions, */activity.getAppTheme());
									GeneralDialogCreation.deleteFilesDialog(activity, //getLayoutElements(),
																			activity, ele, activity.getAppTheme(), r);
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
											Futils.shareFiles(arrayList, activity, activity.getAppTheme(), contentFrag.accentColor);
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
									if (!rowItem.isDirectory) {
										if (!rowItem.name.toLowerCase().endsWith(CryptUtil.CRYPT_EXTENSION)) {
											encrypt(activity, contentFrag, rowItem);
											break;
										} else {
											//case R.id.decrypt:
											EncryptDecryptUtils.decryptFile(activity, activity, contentFrag,
																			contentFrag.openMode, rowItem.generateBaseFile(),
																			rowItem.generateBaseFile().getParent(activity), activity, false);
											break;
										}
									}
									break;
								case R.id.hide:
									contentFrag.dataUtils.addHiddenFile(rowItem.path);
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
									contentFrag.updateList();
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
									if (!rowItem.isDirectory && FileUtil.extractiblePattern.matcher(rowItem.name).matches()) {
										activity.decompress(rowItem.path, contentFrag.currentPathTitle + "/" + rowItem.name.substring(0, rowItem.name.lastIndexOf(".")), "", true);
									} else {
										activity.compress(rowItem.path, contentFrag.currentPathTitle + "/" + (rowItem.name.lastIndexOf(".") > 0 ? rowItem.name.substring(0, rowItem.name.lastIndexOf(".")) : rowItem.name));
									}
									break;
								case R.id.compress:
									ArrayList<BaseFile> copies1 = new ArrayList<>();
									copies1.add(rowItem.bf);
									GeneralDialogCreation.showCompressDialog(activity, copies1, contentFrag.currentPathTitle);
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
					contentFrag.showToast(f + " cannot be read");
				} else {
					final int id = v.getId();
					if (contentFrag.multiFiles) {// || fileFrag.suffix != null && fileFrag.suffix.length() == 0
						boolean inSelected = false;
						if (contentFrag.dataSourceL2 != null)
							for (LayoutElement st : contentFrag.dataSourceL2) {
								if (rowItem.path.equals(st.path) || fPath.startsWith(st.path + "/")) {
									inSelected = true;
									break;
								}
							}
						if (!inSelected) {
							if (id == R.id.icon) {
								contentFrag.tempPreviewL2 = rowItem;
								notifyDataSetChanged();
								if (f.isFile()) {
									load(rowItem, f, fPath, pos);
								} else if (contentFrag.slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {//dir
									if (contentFrag.activity.slideFrag2 != null) {
										Frag frag = contentFrag.activity.slideFrag2.getCurrentFragment();
										if (frag.type == Frag.TYPE.EXPLORER) {
											((ContentFragment)frag).changeDir(path, true);
										} else {
											contentFrag.activity.slideFrag2.setCurrentItem(contentFrag.activity.slideFrag2.indexOfAdapter(contentFrag.activity.curExplorerFrag), true);
											contentFrag.activity.curExplorerFrag.changeDir(path, true);
										}
									}
								} else {//dir
									Frag frag = contentFrag.activity.slideFrag.getCurrentFragment();
									if (frag.type == Frag.TYPE.EXPLORER) {
										((ContentFragment)frag).changeDir(path, true);
									} else {
										contentFrag.activity.slideFrag.setCurrentItem(contentFrag.activity.slideFrag.indexOfAdapter(contentFrag.activity.curContentFrag), true);
										contentFrag.activity.curContentFrag.changeDir(path, true);
									}
								}
							} else if (id == R.id.cbx) {//file and folder
								if (contentFrag.selectedInList1.remove(rowItem)) {
									if (contentFrag.selectedInList1.size() == 0 && contentFrag.activity.COPY_PATH == null && contentFrag.activity.MOVE_PATH == null && contentFrag.commands.getVisibility() == View.VISIBLE) {
										contentFrag.horizontalDivider6.setVisibility(View.GONE);
										contentFrag.commands.setAnimation(AnimationUtils.loadAnimation(contentFrag.activity, R.anim.shrink_from_top));
										contentFrag.commands.setVisibility(View.GONE);
									}
								} else {
									contentFrag.selectedInList1.add(rowItem);
									if (contentFrag.commands.getVisibility() == View.GONE) {
										contentFrag.commands.setAnimation(AnimationUtils.loadAnimation(contentFrag.activity, R.anim.grow_from_bottom));
										contentFrag.commands.setVisibility(View.VISIBLE);
										contentFrag.horizontalDivider6.setVisibility(View.VISIBLE);
									}
								}
								notifyDataSetChanged();
							} else if (f.isDirectory()) { 
								if (contentFrag.selectedInList1.size() == 0 && contentFrag.type == Frag.TYPE.EXPLORER) { 
									contentFrag.changeDir(path, true);
								} else {
									if (contentFrag.selectedInList1.remove(rowItem)) {
										if (contentFrag.selectedInList1.size() == 0 && contentFrag.activity.COPY_PATH == null && contentFrag.activity.MOVE_PATH == null && contentFrag.commands.getVisibility() == View.VISIBLE) {
											contentFrag.horizontalDivider6.setVisibility(View.GONE);
											contentFrag.commands.setAnimation(AnimationUtils.loadAnimation(contentFrag.activity, R.anim.shrink_from_top));
											contentFrag.commands.setVisibility(View.GONE);
										} 
									} else {
										contentFrag.selectedInList1.add(rowItem);
										if (contentFrag.commands.getVisibility() == View.GONE) {
											contentFrag.commands.setAnimation(AnimationUtils.loadAnimation(contentFrag.activity, R.anim.grow_from_bottom));
											contentFrag.commands.setVisibility(View.VISIBLE);
											contentFrag.horizontalDivider6.setVisibility(View.VISIBLE);
										}
									}
									notifyDataSetChanged();
								}
							} else if (f.isFile()) { 
								if (contentFrag.selectedInList1.size() == 0) { 
									openFile(rowItem, f, fPath);
								} else {
									if (contentFrag.selectedInList1.remove(rowItem)) {
										if (contentFrag.selectedInList1.size() == 0 && contentFrag.activity.COPY_PATH == null && contentFrag.activity.MOVE_PATH == null && contentFrag.commands.getVisibility() == View.VISIBLE) {
											contentFrag.horizontalDivider6.setVisibility(View.GONE);
											contentFrag.commands.setAnimation(AnimationUtils.loadAnimation(contentFrag.activity, R.anim.shrink_from_top));
											contentFrag.commands.setVisibility(View.GONE);
										} 
									} else {
										contentFrag.selectedInList1.add(rowItem);
										if (contentFrag.commands.getVisibility() == View.GONE) {
											contentFrag.commands.setAnimation(AnimationUtils.loadAnimation(contentFrag.activity, R.anim.grow_from_bottom));
											contentFrag.commands.setVisibility(View.VISIBLE);
											contentFrag.horizontalDivider6.setVisibility(View.VISIBLE);
										}
									}
									notifyDataSetChanged();
								}
							}
							if ((contentFrag.currentPathTitle == null || contentFrag.currentPathTitle.length() > 0)) {
								contentFrag.selectionStatusTV.setText(contentFrag.selectedInList1.size() 
																	+ "/" + contentFrag.dataSourceL1.size());
							}
						} else { // inselected
							if (id == R.id.icon) { //dir
								contentFrag.tempPreviewL2 = rowItem;
								notifyDataSetChanged();
								if (f.isFile()) {
									load(rowItem, f, fPath, pos);
								} else if (contentFrag.slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {//ContentFragment dir//fileFrag.type == -1
									if (contentFrag.activity.slideFrag2 != null) {
										Frag frag = contentFrag.activity.slideFrag2.getCurrentFragment();
										if (frag.type == Frag.TYPE.EXPLORER) {
											((ContentFragment)frag).changeDir(path, true);
										} else {
											contentFrag.activity.slideFrag2.setCurrentItem(contentFrag.activity.slideFrag2.indexOfAdapter(contentFrag.activity.curExplorerFrag), true);
											contentFrag.activity.curExplorerFrag.changeDir(path, true);
										}
									}
								} else {//dir
									//if (fileFrag.activity.slideFrag2 != null) {
									Frag frag = contentFrag.activity.slideFrag.getCurrentFragment();
									if (frag.type == Frag.TYPE.EXPLORER) {
										((ContentFragment)frag).changeDir(path, true);
									} else {
										contentFrag.activity.slideFrag.setCurrentItem(contentFrag.activity.slideFrag.indexOfAdapter(contentFrag.activity.curContentFrag), true);
										contentFrag.activity.curContentFrag.changeDir(path, true);
									}
								}
							} else if (f.isFile()) {
//								if (v.getId() == R.id.icon) {
//									contentFrag.tempPreviewL2 = rowItem;
//									load(rowItem, f, fPath, pos);
//								} else {
								openFile(rowItem, f, fPath);
//								}
							} 
						}
					} else { //!multifile no preview
						if (id == R.id.cbx) {
							// chọn mới đầu tiên
							if (contentFrag.selectedInList1.size() == 0) {
								contentFrag.selectedInList1.add(rowItem);
//								if (contentFrag.commands.getVisibility() == View.GONE) {
//									contentFrag.commands.setAnimation(AnimationUtils.loadAnimation(contentFrag.activity, R.anim.grow_from_bottom));
//									contentFrag.commands.setVisibility(View.VISIBLE);
//									contentFrag.horizontalDivider6.setVisibility(View.VISIBLE);
//								}
							} else {
								if (contentFrag.selectedInList1.remove(rowItem)) { // đã chọn
//									if (contentFrag.selectedInList1.size() == 0 && contentFrag.activity.COPY_PATH == null && contentFrag.activity.MOVE_PATH == null && contentFrag.commands.getVisibility() == View.VISIBLE) {
//										contentFrag.horizontalDivider6.setVisibility(View.GONE);
//										contentFrag.commands.setAnimation(AnimationUtils.loadAnimation(contentFrag.activity, R.anim.shrink_from_top));
//										contentFrag.commands.setVisibility(View.GONE);
//									}
								} else { // chọn mới bỏ cũ
									contentFrag.selectedInList1.clear();
									contentFrag.selectedInList1.add(rowItem);
								}
							} 
							notifyDataSetChanged();
						} else {
							if (f.isFile()) {
								openFile(rowItem, f, fPath);
							} else { //", "Directory
								//fileFrag.selectedInList1.clear();
								if (contentFrag.currentPathTitle == null || contentFrag.currentPathTitle.length() > 0) {
									contentFrag.changeDir(path, true);
								}
							}
						}
					}
				}
			} else {
				contentFrag.changeDir(f.getParentFile().getAbsolutePath(), true);
			}
			contentFrag.updateDelPaste();
		}
	}

	private void load(final LayoutElement ele, final File f, final String fPath, final int pos) throws IllegalStateException {
		if (contentFrag.activity.slideFrag2 == null) {
			Log.d(TAG, "Single panel only");
			return;
		}
		
		final String mime = MimeTypes.getMimeType(f);
		Log.d(TAG, fPath + "=" + mime + ", " + ele.name + ",  pdf " + FileUtil.muPdfPattern.matcher(ele.name).matches());
		//int i = 0;
		int tabIndex1 = 0;
		int tabIndex2 = 0;
		final SlidingTabsFragment.PagerAdapter pagerAdapter;
		final SlidingTabsFragment slidingTabsFragment;
		if (contentFrag.slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
			slidingTabsFragment = contentFrag.activity.slideFrag2;
		} else {
			slidingTabsFragment = contentFrag.activity.slideFrag;
		}
		pagerAdapter = slidingTabsFragment.pagerAdapter;
		if (mime.startsWith("text/html") || mime.startsWith("text/xhtml")) {
			tabIndex1 = SlidingTabsFragment.getFragTypeIndex(contentFrag, Frag.TYPE.TEXT);
			tabIndex2 = SlidingTabsFragment.getFragTypeIndex(contentFrag, Frag.TYPE.WEB);
			if (tabIndex1 >= 0) {
				pagerAdapter.getItem(tabIndex1).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex1, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.TEXT, fPath);
				contentFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
						}
					}, 100);
			}
			if (tabIndex2 >= 0) {
				pagerAdapter.getItem(tabIndex2).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				contentFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							slidingTabsFragment.addTab(Frag.TYPE.WEB, fPath);
							contentFrag.listView.postDelayed(new Runnable() {
									@Override
									public void run() {
										pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
									}
								}, 100);
						}
					}, 200);
			}
			//pagerAdapter.getItem(i = Frag.TYPE.TEXT.ordinal()).load(fPath);
			//pagerAdapter.getItem(i = Frag.TYPE.WEB.ordinal()).load(fPath);
		} else if (mime.startsWith("application/vnd.android.package-archive")) {
			final StringBuilder sb = new StringBuilder(ExplorerActivity.DOCTYPE);
			try {
				ApkParser apkParser = new ApkParser(f);
				sb.append(AndroidUtils.getSignature(contentFrag.activity, fPath));
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
				tabIndex2 = SlidingTabsFragment.getFragTypeIndex(contentFrag, Frag.TYPE.WEB);
				if (tabIndex2 >= 0) {
					pagerAdapter.getItem(tabIndex2).load(name);
					slidingTabsFragment.setCurrentItem(tabIndex2, true);
				} 
				if (tabIndex2 < 0) {
					slidingTabsFragment.addTab(Frag.TYPE.WEB, name);
					contentFrag.listView.postDelayed(new Runnable() {
							@Override
							public void run() {
								pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(name);
							}
						}, 100);
				}
				byte[] bytes = FileUtil.readFileToMemory(f);
				new FillClassesNamesThread(contentFrag, bytes, f, sb1, sb2, ExplorerActivity.END_PRE).start();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} else if (mime.startsWith("application/x-chm")) {
			
			tabIndex2 = SlidingTabsFragment.getFragTypeIndex(contentFrag, Frag.TYPE.CHM);
			if (tabIndex2 >= 0) {
				pagerAdapter.getItem(tabIndex2).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.CHM, fPath);
				contentFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
						}
					}, 100);
			}
		} else if (FileUtil.muPdfPattern.matcher(ele.name).matches()) {
			tabIndex2 = SlidingTabsFragment.getFragTypeIndex(contentFrag, Frag.TYPE.PDF);
			if (tabIndex2 >= 0) {
				try {
					pagerAdapter.getItem(tabIndex2).load(fPath);
					slidingTabsFragment.setCurrentItem(tabIndex2, true);
				} catch (Throwable t) {
					contentFrag.activity.showToast(t.getMessage());
				}
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.PDF, fPath);
			}
//		} else if (mime.startsWith("application/pdf")) {
//			//pagerAdapter.getItem(i = Frag.TYPE.PDF.ordinal()).load(fPath);
//			tabIndex2 = SlidingTabsFragment.getFragTypeIndex(contentFrag, Frag.TYPE.PDF);
//			if (tabIndex2 >= 0) {
//				pagerAdapter.getItem(tabIndex2).load(fPath);
//				slidingTabsFragment.setCurrentItem(tabIndex2, true);
//			} else {
//				slidingTabsFragment.addTab(Frag.TYPE.PDF, fPath);
//				contentFrag.listView.postDelayed(new Runnable() {
//						@Override
//						public void run() {
//							pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
//						}
//					}, 100);
//			}
		} else if (mime.startsWith("image/svg+xml")) {
			//pagerAdapter.getItem(i = Frag.TYPE.TEXT.ordinal()).load(fPath);
			//pagerAdapter.getItem(i = Frag.TYPE.PHOTO.ordinal()).load(fPath);
			tabIndex1 = SlidingTabsFragment.getFragTypeIndex(contentFrag, Frag.TYPE.TEXT);
			tabIndex2 = SlidingTabsFragment.getFragTypeIndex(contentFrag, Frag.TYPE.PHOTO);
			if (tabIndex1 >= 0) {
				pagerAdapter.getItem(tabIndex1).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex1, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.TEXT, fPath);
				contentFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
						}
					}, 100);
			}
			if (tabIndex2 >= 0) {
				pagerAdapter.getItem(tabIndex2).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				contentFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							slidingTabsFragment.addTab(Frag.TYPE.PHOTO, fPath);
							contentFrag.listView.postDelayed(new Runnable() {
									@Override
									public void run() {
										pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
									}
								}, 100);
						}
					}, 200);
			}

//		} else if (mime.startsWith("application/epub+zip")
//				 || mime.startsWith("application/x-pilot-prc")
//				 || mime.startsWith("application/x-mobipocket-ebook")
//				 || mime.startsWith("application/x-fictionbook+xml")
//				 || mime.startsWith("application/x-fictionbook")
//				 || f.getName().toLowerCase().endsWith(".doc")) {
//			pagerAdapter.getItem(i = Frag.TYPE.FBReader.ordinal()).load(fPath);
		} else if (mime.startsWith("text") || FileUtil.TEXT_PATTERN.matcher(ele.name).matches()) {
			//pagerAdapter.getItem(i = Frag.TYPE.TEXT.ordinal()).load(fPath);
			tabIndex2 = SlidingTabsFragment.getFragTypeIndex(contentFrag, Frag.TYPE.TEXT);
			if (tabIndex2 >= 0) {
				pagerAdapter.getItem(tabIndex2).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.TEXT, fPath);
				contentFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
						}
					}, 100);
			}
		} else if (mime.startsWith("video")) {
			//pagerAdapter.getItem(i = Frag.TYPE.MEDIA.ordinal()).load(fPath);
			tabIndex2 = SlidingTabsFragment.getFragTypeIndex(contentFrag, Frag.TYPE.MEDIA);
			if (tabIndex2 >= 0) {
				pagerAdapter.getItem(tabIndex2).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.MEDIA, fPath);
				contentFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
						}
					}, 500);
			}
		} else if (FileUtil.extractiblePattern.matcher(ele.name).matches()) {
			//pagerAdapter.getItem(i = Frag.TYPE.PHOTO.ordinal()).open(pos, mDataset);
			tabIndex2 = SlidingTabsFragment.getFragTypeIndex(contentFrag, Frag.TYPE.ZIP);
			if (tabIndex2 >= 0) {
				pagerAdapter.getItem(tabIndex2).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.ZIP, fPath);
			}
		} else if (mime.startsWith("image")) {
			//pagerAdapter.getItem(i = Frag.TYPE.PHOTO.ordinal()).open(pos, mDataset);
			tabIndex2 = SlidingTabsFragment.getFragTypeIndex(contentFrag, Frag.TYPE.PHOTO);
			if (tabIndex2 >= 0) {
				pagerAdapter.getItem(tabIndex2).open(pos, mDataset);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.PHOTO, fPath);
//				contentFrag.listView.postDelayed(new Runnable() {
//						@Override
//						public void run() {
//							pagerAdapter.getItem(slidingTabsFragment.pageSelected).open(pos, mDataset);
//						}
//					}, 100);
			}
		} else if (mime.startsWith("audio")) {
			//pagerAdapter.getItem(i = Frag.TYPE.MEDIA.ordinal()).load(fPath);
			tabIndex2 = SlidingTabsFragment.getFragTypeIndex(contentFrag, Frag.TYPE.MEDIA);
			if (tabIndex2 >= 0) {
				pagerAdapter.getItem(tabIndex2).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.MEDIA, fPath);
				contentFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
						}
					}, 500);
			}
		} else {
			contentFrag.tempPreviewL2 = null;
		}
	}

	private void openFile(LayoutElement ele, final File f, String fPath) {
//		try {
//			final Uri uri = Uri.fromFile(f);
//			final Intent i = new Intent(Intent.ACTION_VIEW); 
//			i.addCategory(Intent.CATEGORY_DEFAULT);
//			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
//
//			Log.d("i.setData(uri)", uri + "." + i);
//			final String mimeType = MimeTypes.getMimeType(f);
//			i.setDataAndType(uri, mimeType);//floor.getValue()
//			Log.d(TAG, f + "=" + mimeType);
//			final Intent createChooser = Intent.createChooser(i, "View");
//			Log.i("createChooser.getExtras()", AndroidUtils.bundleToString(createChooser.getExtras()));
//			fileFrag.startActivity(createChooser);
//		} catch (Throwable e) {
//			Toast.makeText(fileFrag.activity, "unable to view !\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
//		}

		// check if we're trying to click on encrypted file
		if (!f.isDirectory() &&
			fPath.endsWith(CryptUtil.CRYPT_EXTENSION)) {
			// decrypt the file
			contentFrag.isEncryptOpen = true;

			contentFrag.encryptBaseFile = new BaseFile(contentFrag.activity.getExternalCacheDir().getPath()
													   + "/"
													   + new LayoutElement(f).generateBaseFile().getName().replace(CryptUtil.CRYPT_EXTENSION, ""));

			EncryptDecryptUtils.decryptFile(contentFrag.activity, contentFrag.activity, contentFrag, contentFrag.openMode,
											new LayoutElement(f).generateBaseFile(), contentFrag.activity.getExternalCacheDir().getPath(),
											contentFrag.activity, true);
			return;
		} else {
			switch (ele.getMode()) {
				case SMB:
					try {
						SmbFile smbFile = new SmbFile(ele.path);
						contentFrag.launchSMB(smbFile, f.length(), contentFrag.activity);
					} catch (MalformedURLException ex) {
						ex.printStackTrace();
					}
					break;
				case OTG:
					contentFrag.activity.getFutils().openFile(OTGUtil.getDocumentFile(ele.path, contentFrag.activity, false),
															  contentFrag.activity);
					break;
				case DROPBOX:
				case BOX:
				case GDRIVE:
				case ONEDRIVE:
					Toast.makeText(contentFrag.activity, contentFrag.activity.getResources().getString(R.string.please_wait), Toast.LENGTH_LONG).show();
					CloudUtil.launchCloud(ele.generateBaseFile(), contentFrag.openMode, contentFrag.activity);
					break;
				default:
					contentFrag.activity.getFutils().openFile(f, contentFrag.activity);
					break;
			}
			contentFrag.dataUtils.addHistoryFile(ele.path);
		}
	}

	private class OnLongClickListener implements View.OnLongClickListener {
		private final int pos;
		OnLongClickListener(int pos) {
			this.pos = pos;
		}
		@Override
		public boolean onLongClick(final View v) {
			contentFrag.select(true);
//			if (fileFrag.type == -1) {
//				fileFrag.activity.slideFrag2.getCurrentFragment().select(false);
//			} else {
//				fileFrag.activity.slideFrag2.getCurrentFragment().select(true);
//			}
			final LayoutElement rowItem = mDataset.get(pos);//Integer.valueOf(v.getContentDescription().toString())
			final File f = rowItem.bf.f;//new File(fPath);
			final String fPath = f.getAbsolutePath();//(String) v.getContentDescription();

			if (!f.exists()) {
				contentFrag.changeDir(fPath, true);
				return true;
			} else if (!f.canRead()) {
				contentFrag.showToast(f + " cannot be read");
				return true;
			}
			Log.d(TAG, "onLongClick, " + fPath);
			Log.d(TAG, "currentSelectedList" + Util.collectionToString(contentFrag.selectedInList1, true, "\r\n"));
			Log.d(TAG, "selectedInList.contains(f) " + contentFrag.selectedInList1.contains(f));
			Log.d(TAG, "multiFiles " + contentFrag.multiFiles);

			boolean inSelectedFiles = false;
			if (contentFrag.dataSourceL2 != null)
				for (LayoutElement st : contentFrag.dataSourceL2) {
					if (f.equals(st) || fPath.startsWith(st.path + "/")) {
						inSelectedFiles = true;
						break;
					}
				}
			if (!inSelectedFiles) {
				if (contentFrag.multiFiles || contentFrag.suffix.length() == 0) {
					if (contentFrag.selectedInList1.remove(rowItem)) {
						if (contentFrag.selectedInList1.size() == 0 && contentFrag.activity.COPY_PATH == null && contentFrag.activity.MOVE_PATH == null && contentFrag.commands.getVisibility() == View.VISIBLE) {
							contentFrag.horizontalDivider6.setVisibility(View.GONE);
							contentFrag.commands.setAnimation(AnimationUtils.loadAnimation(contentFrag.activity, R.anim.shrink_from_top));
							contentFrag.commands.setVisibility(View.GONE);
						} 
					} else {
						contentFrag.selectedInList1.add(rowItem);
						if (contentFrag.commands.getVisibility() == View.GONE) {
							contentFrag.commands.setAnimation(AnimationUtils.loadAnimation(contentFrag.activity, R.anim.grow_from_bottom));
							contentFrag.commands.setVisibility(View.VISIBLE);
							contentFrag.horizontalDivider6.setVisibility(View.VISIBLE);
						}
					}
					if ((contentFrag.currentPathTitle == null || contentFrag.currentPathTitle.length() > 0)) {
						contentFrag.selectionStatusTV.setText(contentFrag.selectedInList1.size() 
															+ "/" + contentFrag.dataSourceL1.size());
					}
				} else { // single file
					if (f.isFile()) {
						if (contentFrag.selectedInList1.size() == 0) {
							contentFrag.selectedInList1.add(rowItem);
//							if (contentFrag.commands.getVisibility() == View.GONE) {
//								contentFrag.commands.setAnimation(AnimationUtils.loadAnimation(contentFrag.activity, R.anim.grow_from_bottom));
//								contentFrag.commands.setVisibility(View.VISIBLE);
//								contentFrag.horizontalDivider6.setVisibility(View.VISIBLE);
//							}
						} else {
							if (contentFrag.selectedInList1.remove(rowItem)) {
//								if (contentFrag.selectedInList1.size() == 0 && contentFrag.activity.COPY_PATH == null && contentFrag.activity.MOVE_PATH == null && contentFrag.commands.getVisibility() == View.VISIBLE) {
//									contentFrag.horizontalDivider6.setVisibility(View.GONE);
//									contentFrag.commands.setAnimation(AnimationUtils.loadAnimation(contentFrag.activity, R.anim.shrink_from_top));
//									contentFrag.commands.setVisibility(View.GONE);
//								} 
							} else {
								// chọn mới bỏ cũ
								contentFrag.selectedInList1.clear();
								contentFrag.selectedInList1.add(rowItem);
							}
						}
					} else { //", "Directory
						contentFrag.selectedInList1.clear();
						contentFrag.changeDir(contentFrag.currentPathTitle, true);
					}
				}
				notifyDataSetChanged();
			} 
			contentFrag.updateDelPaste();
			return true;
		}
	}

}
