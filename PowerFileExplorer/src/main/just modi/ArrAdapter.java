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
//import com.amaze.filemanager.fragments.preference_fragments.Preffrag;
import android.preference.PreferenceManager;
import javax.crypto.Cipher;
import android.text.format.Formatter;
import com.afollestad.materialdialogs.MaterialDialog;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import com.amaze.filemanager.ui.dialogs.GeneralDialogCreation;
import com.amaze.filemanager.activities.ThemedActivity;

public class ArrAdapter extends RecyclerAdapter<LayoutElement, ArrAdapter.ViewHolder> {

	private static final String TAG = "ArrAdapter";

	private final int backgroundResource;
	private final ContentFragment fileFrag;
	//private final ViewGroup commands;
	//private final View horizontalDivider;

	public void toggleChecked(boolean checked) {
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

	class ViewHolder extends RecyclerView.ViewHolder {
		private TextView name;
		private TextView size;
		private TextView attr;
		private TextView lastModified;
		private TextView type;
		private ImageButton cbx;
		private ImageView image;
		private ImageButton more;
		private View convertedView;

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

	public ArrAdapter(final ContentFragment fileFrag, final ArrayList<LayoutElement> objects, final ViewGroup commands, final View horizontalDivider) {
		super(objects);
		this.fileFrag = fileFrag;
		//Log.d(TAG, "ArrAdapter " + objects);
		final int[] attrs = new int[]{R.attr.selectableItemBackground};
		final TypedArray typedArray = fileFrag.activity.obtainStyledAttributes(attrs);
		backgroundResource = typedArray.getResourceId(0, 0);
		typedArray.recycle();
		//this.commands = commands;
		//this.horizontalDivider = horizontalDivider;
		//setHasStableIds(true);
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

		// set the view's size, margins, paddings and layout parameters
		final ViewHolder vh = new ViewHolder(v);
		return vh;
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position) {
		//Log.d(TAG, "onBindViewHolder " + position);
		
		final LayoutElement le = mDataset.get(position);
		final String fPath = le.path;
		final String name = le.name;
		if (fileFrag.type == Frag.TYPE.EXPLORER) {
			holder.name.setText(name);
		} else {
			holder.name.setText(fPath);
		}
		holder.image.setContentDescription(fPath);

		final OnClickListener onClickListener = new OnClickListener(position);
		holder.convertedView.setOnClickListener(onClickListener);
		holder.cbx.setOnClickListener(onClickListener);
		holder.image.setOnClickListener(onClickListener);
		holder.more.setOnClickListener(onClickListener);

		final OnLongClickListener onLongClickListener = new OnLongClickListener(position);
		holder.convertedView.setOnLongClickListener(onLongClickListener);
		holder.cbx.setOnLongClickListener(onLongClickListener);
		holder.image.setOnLongClickListener(onLongClickListener);
		holder.more.setOnLongClickListener(onLongClickListener);

		if (fileFrag.currentPathTitle == null || fileFrag.currentPathTitle.length() > 0) {
			holder.name.setEllipsize(TextUtils.TruncateAt.MIDDLE);
		} else {
			holder.name.setEllipsize(TextUtils.TruncateAt.START);
		}

//	        if (!f.exists()) {
//				dataSourceL1.remove(f);
//				selectedInList1.remove(f);
//				notifyItemRemoved(position);
//				return;//convertView;
//			}

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
				}
			}
		}
//			if (activity.theme1 == 1) {
//				holder.convertedView.setBackgroundResource(R.drawable.safr_ripple_white);
//			} else {
//				holder.convertedView.setBackgroundResource(R.drawable.safr_ripple_black);
//			}
		//Log.d(TAG, "inDataSource2 " + inDataSource2 + ", " + dir);
		//Log.d("f.getAbsolutePath()", f.getAbsolutePath());
		//Log.d("curSelectedFiles", curSelectedFiles.toString());
		if (inDataSource2) {
			holder.convertedView.setBackgroundColor(ExplorerActivity.IN_DATA_SOURCE_2);
			holder.cbx.setImageResource(R.drawable.ic_accept);
			holder.cbx.setSelected(true);
			holder.cbx.setEnabled(false);
			if ((fileFrag.currentPathTitle == null || fileFrag.currentPathTitle.length() > 0) && fileFrag.selectedInList1.size() == fileFrag.dataSourceL1.size()) {
				fileFrag.allCbx.setSelected(true);//.setChecked(true);
				fileFrag.allCbx.setImageResource(R.drawable.ic_accept);
			}
		} else if (fileFrag.selectedInList1.contains(le)) {
			holder.convertedView.setBackgroundColor(ExplorerActivity.SELECTED_IN_LIST);
			holder.cbx.setImageResource(R.drawable.ic_accept);
			holder.cbx.setSelected(true);
			holder.cbx.setEnabled(true);
			if ((fileFrag.currentPathTitle == null || fileFrag.currentPathTitle.length() > 0) && fileFrag.selectedInList1.size() == fileFrag.dataSourceL1.size()) {
				fileFrag.allCbx.setSelected(true);//.setChecked(true);
				fileFrag.allCbx.setImageResource(R.drawable.ic_accept);
			}
		} else if (isPartial) {
			holder.convertedView.setBackgroundColor(ExplorerActivity.IS_PARTIAL);
			holder.cbx.setImageResource(R.drawable.ready);
			holder.cbx.setSelected(false);
			holder.cbx.setEnabled(true);
			fileFrag.allCbx.setSelected(false);//.setChecked(false);
			if (fileFrag.selectedInList1.size() == 0) {
				fileFrag.allCbx.setImageResource(R.drawable.dot);
			} else {
				fileFrag.allCbx.setImageResource(R.drawable.ready);
			}
		} else {
			holder.convertedView.setBackgroundResource(backgroundResource);
			if (fileFrag.selectedInList1.size() > 0) {
				holder.cbx.setImageResource(R.drawable.ready);
				fileFrag.allCbx.setImageResource(R.drawable.ready);
			} else {
				holder.cbx.setImageResource(R.drawable.dot);
				fileFrag.allCbx.setImageResource(R.drawable.dot);
			}
			holder.cbx.setSelected(false);
			holder.cbx.setEnabled(true);
			fileFrag.allCbx.setSelected(false);

		}
		if (fileFrag.tempPreviewL2 != null && fileFrag.tempPreviewL2.equals(le)) {
			holder.convertedView.setBackgroundColor(ExplorerActivity.LIGHT_GREY);
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
				holder.cbx.setEnabled(false);
			}
			holder.attr.setText(st);
			if (viewType <= 1) {
				final int lastIndexOf = name.lastIndexOf(".");
				holder.type.setText(lastIndexOf >= 0 && lastIndexOf < name.length() - 1 ? name.substring(lastIndexOf + 1) : "");
				holder.size.setText(Util.nf.format(le.length) + " B");
				holder.lastModified.setText(Util.dtf.format(le.lastModified));
			} else {
				holder.size.setText(Formatter.formatFileSize(fileFrag.activity, le.length));
				holder.lastModified.setText(Util.df.format(le.lastModified));
			}
		} else {
			final String[] list = le.bf.f.list();
			final int length = list == null ? 0 : list.length;
			holder.size.setText(Util.nf.format(length) + " item");
			final String st;
			if (canWrite) {
				st = "drw";
			} else if (canRead) {
				st = "dr-";
			} else {
				st = "d--";
				holder.cbx.setEnabled(false);
			}
			holder.attr.setText(st);
			if (viewType <= 1) {
				holder.type.setText("Folder");
				holder.lastModified.setText(Util.dtf.format(le.lastModified));
			} else {
				holder.lastModified.setText(Util.df.format(le.lastModified));
			}
		}
		fileFrag.imageLoader.displayImage(le.bf.f, fileFrag.getContext(), holder.image, fileFrag.spanCount);
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
				final MenuPopupHelper optionsMenu = new MenuPopupHelper(fileFrag.activity , menuBuilder, fileFrag.allSize);
				optionsMenu.setForceShowIcon(true);

				int num= menuBuilder.size();
				for (int i = 0; i < num; i++) {
					Drawable icon = menuBuilder.getItem(i).getIcon();
					if (icon != null) {
						icon.setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
					}
				}
				menuBuilder.setCallback(new MenuBuilder.Callback() {
						@Override
						public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
							final ExplorerActivity activity = fileFrag.activity;
							switch (item.getItemId()) {
								case R.id.copy:
									//copy(v);
									fileFrag.activity.MOVE_PATH = null;
									ArrayList<BaseFile> copies = new ArrayList<>();
									copies.add(rowItem.generateBaseFile());
									activity.COPY_PATH = copies;
									if (activity.curExploreFrag.commands.getVisibility() == View.GONE) {
										activity.curExploreFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
										activity.curExploreFrag.commands.setVisibility(View.VISIBLE);
										activity.curExploreFrag.horizontalDivider.setVisibility(View.VISIBLE);
										activity.curExploreFrag.updateDelPaste();
									}
									if (activity.curContentFrag.commands.getVisibility() == View.GONE) {
										activity.curContentFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
										activity.curContentFrag.commands.setVisibility(View.VISIBLE);
										activity.curContentFrag.horizontalDivider.setVisibility(View.VISIBLE);
										activity.curContentFrag.updateDelPaste();
									}
									break;
								case R.id.cut:
									//cut(v);
									activity.COPY_PATH = null;
									ArrayList<BaseFile> copie = new ArrayList<>();
									copie.add(rowItem.generateBaseFile());
									activity.MOVE_PATH = copie;
									//activity1.supportInvalidateOptionsMenu();
									if (activity.curExploreFrag.commands.getVisibility() == View.GONE) {
										activity.curExploreFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
										activity.curExploreFrag.commands.setVisibility(View.VISIBLE);
										activity.curExploreFrag.horizontalDivider.setVisibility(View.VISIBLE);
										activity.curExploreFrag.updateDelPaste();
									}
									if (activity.curContentFrag.commands.getVisibility() == View.GONE) {
										activity.curContentFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
										activity.curContentFrag.commands.setVisibility(View.VISIBLE);
										activity.curContentFrag.horizontalDivider.setVisibility(View.VISIBLE);
										activity.curContentFrag.updateDelPaste();
									}
									break;
								case R.id.rename:
									//rename(v);
									//fileFrag.rename(rowItem.bf);
									//ArrayList<Integer> plist = adapter.getCheckedItemPositions();
									//final BaseFile f = (LIST_ELEMENTS.get((plist.get(0)))).generateBaseFile();
									MaterialDialog.Builder builder = new MaterialDialog.Builder(activity);
									String name = rowItem.bf.f.getName();
									builder.input("", name, false, new MaterialDialog.InputCallback() {
											@Override
											public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {

											}
										});
									builder.theme(activity.getAppTheme().getMaterialDialogTheme());
									builder.title(activity.getResources().getString(R.string.rename));
									builder.callback(new MaterialDialog.ButtonCallback() {
											@Override
											public void onPositive(MaterialDialog materialDialog) {
												String name = materialDialog.getInputEditText().getText().toString();
												if (rowItem.bf.isSmb())
													if (rowItem.bf.isDirectory() && !name.endsWith("/"))
														name = name + "/";

												activity.mainActivityHelper.rename(fileFrag.openMode, rowItem.bf.f.getPath(),
																				   fileFrag.currentPathTitle + "/" + name, activity, BaseActivity.rootMode);
											}

											@Override
											public void onNegative(MaterialDialog materialDialog) {

												materialDialog.cancel();
											}
										});
									builder.positiveText(R.string.save);
									builder.negativeText(R.string.cancel);
									int color = Color.parseColor(fileFrag.fabSkin);
									builder.positiveColor(color).negativeColor(color).widgetColor(color);
									builder.build().show();
									break;
								case R.id.delete:
									//delete(v);
//								ArrayList<Integer> positions = new ArrayList<>();
//								positions.add(pos);
									ArrayList<LayoutElement> ele = new ArrayList<LayoutElement>();
									ele.add(rowItem);
									new Futils().deleteFiles(ele, fileFrag.activity, /*positions, */activity.getAppTheme());
									break;
								case R.id.share:
									switch (rowItem.getMode()) {
										case DROPBOX:
										case BOX:
										case GDRIVE:
										case ONEDRIVE:
											new Futils().shareCloudFile(rowItem.path, rowItem.getMode(), activity);
											break;
										default:
											ArrayList<File> arrayList = new ArrayList<>();
											arrayList.add(new File(rowItem.path));
											new Futils().shareFiles(arrayList, activity, activity.getAppTheme(), Color.parseColor(fileFrag.fabSkin));
											break;
									}
									//share(v);
									break;
								case R.id.scan:
									AndroidUtils.scanMedia(activity, rowItem.bf.f.getAbsolutePath(), false);
									break;
								case R.id.addshortcut:
									AndroidUtils.addShortcut(activity, rowItem.bf.f);
									break;
								case R.id.info:
									GeneralDialogCreation.showPropertiesDialogWithPermissions((rowItem).generateBaseFile(), rowItem.permissions, activity, ThemedActivity.rootMode, activity.getAppTheme(), true, false);
//									new Futils().showProps((rowItem).generateBaseFile(),
//														   rowItem.permissions, fileFrag,
//														   BaseActivity.rootMode, activity.getAppTheme());

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
									new Futils().openWith(new File(rowItem.path), activity);
									break;
								case R.id.encrypt:
									final Intent encryptIntent = new Intent(activity, EncryptService.class);
									encryptIntent.putExtra(EncryptService.TAG_OPEN_MODE, rowItem.getMode().ordinal());
									encryptIntent.putExtra(EncryptService.TAG_CRYPT_MODE,
														   EncryptService.CryptEnum.ENCRYPT.ordinal());
									encryptIntent.putExtra(EncryptService.TAG_SOURCE, rowItem.generateBaseFile());

									final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(fileFrag.activity);

									final EncryptButtonCallbackInterface encryptButtonCallbackInterfaceAuthenticate =
										new EncryptButtonCallbackInterface() {
										@Override
										public void onButtonPressed(Intent intent) {
											// do nothing
										}

										@Override
										public void onButtonPressed(Intent intent, String password) throws Exception {

											startEncryption(rowItem.generateBaseFile().getPath(), password, intent);
										}
									};

									EncryptButtonCallbackInterface encryptButtonCallbackInterface =
										new EncryptButtonCallbackInterface() {

										@Override
										public void onButtonPressed(Intent intent) throws Exception {


											// check if a master password or fingerprint is set
//											if (!preferences.getString(Preffrag.PREFERENCE_CRYPT_MASTER_PASSWORD,
//																	   Preffrag.PREFERENCE_CRYPT_MASTER_PASSWORD_DEFAULT).equals("")) {
//
//												startEncryption(rowItem.generateBaseFile().getPath(),
//																Preffrag.ENCRYPT_PASSWORD_MASTER, encryptIntent);
//											} else if (preferences.getBoolean(Preffrag.PREFERENCE_CRYPT_FINGERPRINT,
//																			  Preffrag.PREFERENCE_CRYPT_FINGERPRINT_DEFAULT)) {
//
//												startEncryption(rowItem.generateBaseFile().getPath(),
//																Preffrag.ENCRYPT_PASSWORD_FINGERPRINT, encryptIntent);
//											} else {
//												// let's ask a password from user
//												new Futils().showEncryptAuthenticateDialog(encryptIntent,
//																						   fileFrag, fileFrag.activity.getAppTheme(),
//																						   encryptButtonCallbackInterfaceAuthenticate);
//											}
											GeneralDialogCreation.showEncryptAuthenticateDialog(context, encryptIntent,
																								mainFragment.getMainActivity(), utilitiesProvider.getAppTheme(),
																								encryptButtonCallbackInterfaceAuthenticate);
										}

										@Override
										public void onButtonPressed(Intent intent, String password) {
											// do nothing
										}
									};

//									if (preferences.getBoolean(Preffrag.PREFERENCE_CRYPT_WARNING_REMEMBER,
//															   Preffrag.PREFERENCE_CRYPT_WARNING_REMEMBER_DEFAULT)) {
//										// let's skip warning dialog call
//										try {
//											encryptButtonCallbackInterface.onButtonPressed(encryptIntent);
//										} catch (Exception e) {
//											e.printStackTrace();
//											Toast.makeText(fileFrag.activity,
//														   fileFrag.getResources().getString(R.string.crypt_encryption_fail),
//														   Toast.LENGTH_LONG).show();
//										}
//									} else {
//
//										new Futils().showEncryptWarningDialog(encryptIntent,
//																			  fileFrag, fileFrag.activity.getAppTheme(), encryptButtonCallbackInterface);
//									}
									GeneralDialogCreation.showEncryptWarningDialog(encryptIntent,
																				   mainFragment, utilitiesProvider.getAppTheme(), encryptButtonCallbackInterface);
									break;
								case R.id.decrypt:
									ContentFragment.decryptFile(fileFrag, fileFrag.openMode, rowItem.generateBaseFile(),
																rowItem.generateBaseFile().getParent(fileFrag.activity),
																fileFrag.activity);
									break;
							}

							return false ;
						}
						@Override
						public void onMenuModeChange(MenuBuilder menu) {}
					});
				optionsMenu.show();
				return;
			}
			final File f = rowItem.bf.f;//new File(fPath);
			final String fPath = f.getAbsolutePath();//(String) v.getContentDescription();
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
									load(f, fPath, pos);
								} else if (fileFrag.slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {//ContentFragment dir//fileFrag.type == -1
									if (fileFrag.activity.slideFrag2 != null) {
										Frag frag = fileFrag.activity.slideFrag2.getCurrentFragment();
										if (frag.type == Frag.TYPE.EXPLORER) {
											((ContentFragment)frag).changeDir(fPath, true);
										} else {
											fileFrag.activity.slideFrag2.setCurrentItem(fileFrag.activity.slideFrag2.indexOfAdapter(fileFrag.activity.curExploreFrag), true);
											fileFrag.activity.curExploreFrag.changeDir(fPath, true);
										}
									}
								} else {//dir
									Frag frag = fileFrag.activity.slideFrag.getCurrentFragment();
									if (frag.type == Frag.TYPE.EXPLORER) {
										((ContentFragment)frag).changeDir(fPath, true);
									} else {
										fileFrag.activity.slideFrag.setCurrentItem(fileFrag.activity.slideFrag.indexOfAdapter(fileFrag.activity.curContentFrag), true);
										fileFrag.activity.curContentFrag.changeDir(fPath, true);
									}
//									fileFrag.activity.curContentFrag.changeDir(fPath, true);
								}
								if (fileFrag.selectedInList1.size() > 0) {
									if (fileFrag.selectedInList1.remove(rowItem)) {
										if (fileFrag.selectedInList1.size() == 0 && fileFrag.activity.COPY_PATH == null && fileFrag.activity.MOVE_PATH == null && fileFrag.commands.getVisibility() == View.VISIBLE) {
											fileFrag.horizontalDivider.setVisibility(View.GONE);
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
										fileFrag.horizontalDivider.setVisibility(View.GONE);
										fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(fileFrag.activity, R.anim.shrink_from_top));
										fileFrag.commands.setVisibility(View.GONE);
									}
								} else {
									fileFrag.selectedInList1.add(rowItem);
									if (fileFrag.commands.getVisibility() == View.GONE) {
										fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(fileFrag.activity, R.anim.grow_from_bottom));
										fileFrag.commands.setVisibility(View.VISIBLE);
										fileFrag.horizontalDivider.setVisibility(View.VISIBLE);
									}
								}
							} else if (f.isDirectory()) { 
								if (fileFrag.selectedInList1.size() == 0 && fileFrag.type != Frag.TYPE.SELECTION) { 
									fileFrag.changeDir(fPath, true);
								} else {
									if (fileFrag.selectedInList1.remove(rowItem)) {
										if (fileFrag.selectedInList1.size() == 0 && fileFrag.activity.COPY_PATH == null && fileFrag.activity.MOVE_PATH == null && fileFrag.commands.getVisibility() == View.VISIBLE) {
											fileFrag.horizontalDivider.setVisibility(View.GONE);
											fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(fileFrag.activity, R.anim.shrink_from_top));
											fileFrag.commands.setVisibility(View.GONE);
										} 
									} else {
										fileFrag.selectedInList1.add(rowItem);
										if (fileFrag.commands.getVisibility() == View.GONE) {
											fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(fileFrag.activity, R.anim.grow_from_bottom));
											fileFrag.commands.setVisibility(View.VISIBLE);
											fileFrag.horizontalDivider.setVisibility(View.VISIBLE);
										}
									}
								}
							} else if (f.isFile()) { 
								if (fileFrag.selectedInList1.size() == 0) { 
									openFile(f);
								} else {
									if (fileFrag.selectedInList1.remove(rowItem)) {
										if (fileFrag.selectedInList1.size() == 0 && fileFrag.activity.COPY_PATH == null && fileFrag.activity.MOVE_PATH == null && fileFrag.commands.getVisibility() == View.VISIBLE) {
											fileFrag.horizontalDivider.setVisibility(View.GONE);
											fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(fileFrag.activity, R.anim.shrink_from_top));
											fileFrag.commands.setVisibility(View.GONE);
										} 
									} else {
										fileFrag.selectedInList1.add(rowItem);
										if (fileFrag.commands.getVisibility() == View.GONE) {
											fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(fileFrag.activity, R.anim.grow_from_bottom));
											fileFrag.commands.setVisibility(View.VISIBLE);
											fileFrag.horizontalDivider.setVisibility(View.VISIBLE);
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
											fileFrag.horizontalDivider.setVisibility(View.VISIBLE);
										}
									} else if (fileFrag.selectedInList1.size() > 0) {
										if (fileFrag.selectedInList1.remove(rowItem)) { // đã chọn
											if (fileFrag.selectedInList1.size() == 0 && fileFrag.activity.COPY_PATH == null && fileFrag.activity.MOVE_PATH == null && fileFrag.commands.getVisibility() == View.VISIBLE) {
												fileFrag.horizontalDivider.setVisibility(View.GONE);
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
									fileFrag.changeDir(fPath, true);
								}
							}
						}
						notifyDataSetChanged();
					} else { // inselected
						if (f.isFile()) {
							if (v.getId() == R.id.icon) {
								load(f, fPath, pos);
							} else {
								openFile(f);
							}
						} else if (v.getId() == R.id.icon) { //dir
							fileFrag.tempPreviewL2 = rowItem;
							fileFrag.activity.slideFrag2.setCurrentItem(Frag.TYPE.EXPLORER.ordinal(), true);
							fileFrag.activity.curExploreFrag.changeDir(fPath, true);
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
			for (int i = 1; i < count-1; i++) {
				if (pagerAdapter.getItem(i).type == t) {
					return i;
				}
			}
			return -1;
		} else {
			return pagerAdapter.getItem(0).type == t ? 0 : -1;
		}
	}

	private void load(final File f, final String fPath, final int pos) throws IllegalStateException {
		if (fileFrag.activity.slideFrag2 == null) {
			Log.d(TAG, "Single panel only");
			return;
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
							fileFrag.horizontalDivider.setVisibility(View.GONE);
							fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(fileFrag.activity, R.anim.shrink_from_top));
							fileFrag.commands.setVisibility(View.GONE);
						} 
					} else {
						fileFrag.selectedInList1.add(rowItem);
						if (fileFrag.commands.getVisibility() == View.GONE) {
							fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(fileFrag.activity, R.anim.grow_from_bottom));
							fileFrag.commands.setVisibility(View.VISIBLE);
							fileFrag.horizontalDivider.setVisibility(View.VISIBLE);
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
								fileFrag.horizontalDivider.setVisibility(View.VISIBLE);
							}
						} else if (fileFrag.selectedInList1.size() > 0) {
							if (fileFrag.selectedInList1.remove(rowItem)) {
								// đã chọn
								if (fileFrag.selectedInList1.size() == 0 && fileFrag.activity.COPY_PATH == null && fileFrag.activity.MOVE_PATH == null && fileFrag.commands.getVisibility() == View.VISIBLE) {
									fileFrag.horizontalDivider.setVisibility(View.GONE);
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



	/**
	 * Queries database to map path and password.
	 * Starts the encryption process after database query
	 * @param path the path of file to encrypt
	 * @param password the password in plaintext
	 */
	private void startEncryption(final String path, final String password, Intent intent) throws Exception {

		CryptHandler cryptHandler = new CryptHandler(fileFrag.activity);
		EncryptedEntry encryptedEntry = new EncryptedEntry(path.concat(CryptUtil.CRYPT_EXTENSION),
														   password);
		cryptHandler.addEntry(encryptedEntry);
		Cipher k;
		// start the encryption process
		ServiceWatcherUtil.runService(fileFrag.activity, intent);
	}

	public interface EncryptButtonCallbackInterface {

		/**
		 * Callback fired when we've just gone through warning dialog before encryption
		 * @param intent
		 * @throws Exception
		 */
		void onButtonPressed(Intent intent) throws Exception;

		/**
		 * Callback fired when user has entered a password for encryption
		 * Not called when we've a master password set or enable fingerprint authentication
		 * @param intent
		 * @param password the password entered by user
		 * @throws Exception
		 */
		void onButtonPressed(Intent intent, String password) throws Exception;
	}

	public interface DecryptButtonCallbackInterface {
		/**
		 * Callback fired when we've confirmed the password matches the database
		 * @param intent
		 */
		void confirm(Intent intent);

		/**
		 * Callback fired when password doesn't match the value entered by user
		 */
		void failed();
	}


}
