package net.gnu.explorer;

import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import com.amaze.filemanager.ui.dialogs.GeneralDialogCreation;
import com.amaze.filemanager.ui.icons.MimeTypes;
import com.amaze.filemanager.utils.files.Futils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.dongliu.apk.parser.ApkParser;
import net.gnu.androidutil.AndroidUtils;
import net.gnu.p7zip.DecompressTask;
import net.gnu.util.FileUtil;
import net.gnu.util.Util;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.DialogAction;
import android.widget.Toast;
import net.gnu.p7zip.ZipEntry;

public class ZipAdapter extends RecyclerAdapter<ZipEntry, ZipAdapter.ViewHolder> {

	private static final String TAG = "ZipAdapter";

	private final int backgroundResource;
	private final ZipFragment zipFrag;

	public void toggleChecked(final boolean checked) {
		if (checked) {
			zipFrag.allCbx.setSelected(true);
			zipFrag.selectedInList1.clear();
			zipFrag.selectedInList1.addAll(zipFrag.dataSourceL1);
			zipFrag.allCbx.setImageResource(R.drawable.ic_accept);
		} else {
			zipFrag.allCbx.setSelected(false);
			zipFrag.selectedInList1.clear();
			zipFrag.allCbx.setImageResource(R.drawable.dot);
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
			//attr.setTextColor(ExplorerActivity.TEXT_COLOR);
			lastModified.setTextColor(ExplorerActivity.TEXT_COLOR);
			if (type != null) {
				type.setTextColor(ExplorerActivity.TEXT_COLOR);
			}
			image.setScaleType(ImageView.ScaleType.FIT_CENTER);
			convertView.setTag(this);
			this.convertedView = convertView;
		}
	}

	public ZipAdapter(final ZipFragment fileFrag, final List<ZipEntry> objects) {
		super(objects);
		this.zipFrag = fileFrag;

		final int[] attrs = new int[]{R.attr.selectableItemBackground};
		final TypedArray typedArray = fileFrag.activity.obtainStyledAttributes(attrs);
		backgroundResource = typedArray.getResourceId(0, 0);
		typedArray.recycle();
	}

	@Override
	public int getItemViewType(final int position) {
		if (zipFrag.dataSourceL1.size() == 0) {
			return 0;
		} else if (zipFrag.spanCount == 1 && zipFrag.slidingTabsFragment.width >= 0
				   || (zipFrag.spanCount == 2 && (zipFrag.activity.right.getVisibility() == View.GONE || zipFrag.activity.left.getVisibility() == View.GONE))
				   ) {
			return 1;
		} else if (zipFrag.spanCount == 1 && zipFrag.slidingTabsFragment.width < 0
				   || zipFrag.spanCount == 2 && zipFrag.slidingTabsFragment.width >= 0) {
			return 2;
		} else {
			return 3;
		}
	}

	@Override
	public ZipAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent,
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

		final ZipEntry le = mDataset.get(position);
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

		name.setText(fName);
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

		if (zipFrag.currentPathTitle == null || zipFrag.currentPathTitle.length() > 0) {
			name.setEllipsize(TextUtils.TruncateAt.MIDDLE);
		} else {
			name.setEllipsize(TextUtils.TruncateAt.START);
		}

		//Log.d("f.getAbsolutePath()", f.getAbsolutePath());
		//Log.d("curSelectedFiles", curSelectedFiles.toString());
		if (zipFrag.selectedInList1.contains(le)) {
			convertedView.setBackgroundColor(ExplorerActivity.SELECTED_IN_LIST);
			cbx.setImageResource(R.drawable.ic_accept);
			cbx.setSelected(true);
			cbx.setEnabled(true);
			if ((zipFrag.currentPathTitle == null || zipFrag.currentPathTitle.length() > 0) && zipFrag.selectedInList1.size() == zipFrag.dataSourceL1.size()) {
				zipFrag.allCbx.setSelected(true);
				zipFrag.allCbx.setImageResource(R.drawable.ic_accept);
			}
		} else {
			//Log.d(TAG, "inDataSource2 " + inDataSource2 + ", " + dir);
			convertedView.setBackgroundResource(backgroundResource);
			if (zipFrag.selectedInList1.size() > 0) {
				cbx.setImageResource(R.drawable.ready);
				zipFrag.allCbx.setImageResource(R.drawable.ready);
			} else {
				cbx.setImageResource(R.drawable.dot);
				zipFrag.allCbx.setImageResource(R.drawable.dot);
			}
			cbx.setSelected(false);
			cbx.setEnabled(true);
			zipFrag.allCbx.setSelected(false);
		}
		if (zipFrag.tempPreviewL2 != null && zipFrag.tempPreviewL2.equals(le)) {
			convertedView.setBackgroundColor(ExplorerActivity.LIGHT_GREY);
		}

		final int viewType = getItemViewType(position);
		if (!le.isDirectory) {
			if (viewType == 1) {
				final int lastIndexOf = fName.lastIndexOf(".");
				type.setText(lastIndexOf >= 0 && lastIndexOf < fName.length() - 1 ? fName.substring(lastIndexOf + 1) : "");
				lastModified.setText(Util.dtf.format(le.lastModified));
			} else if (viewType == 2) {
				if (zipFrag.slidingTabsFragment.width != 0) {
					lastModified.setText(Util.dtf.format(le.lastModified));
				} else {
					lastModified.setText(Util.df.format(le.lastModified));
				}
			} else {
				size.setText(Formatter.formatFileSize(zipFrag.activity, le.length));
				lastModified.setText(Util.df.format(le.lastModified));
			}
			size.setText(Util.nf.format(le.length) + " B");
			if (le.zipLength >= 0) {
				attr.setText(Util.nf.format(le.zipLength) + " B");
			} else {
				attr.setText("n/a");
			}
		} else {
			//final String[] list = le.list();
			final int length = le.list.size();//list == null ? 0 : list.length;
			size.setText(Util.nf.format(length) + " item");
			attr.setText(Util.nf.format(le.length));
			if (viewType == 1) {
				type.setText("Folder");
				lastModified.setText(Util.dtf.format(le.lastModified));
			} else if (viewType == 2) {
				if (zipFrag.slidingTabsFragment.width != 0) {
					lastModified.setText(Util.dtf.format(le.lastModified));
				} else {
					lastModified.setText(Util.df.format(le.lastModified));
				}
			} else {
				lastModified.setText(Util.df.format(le.lastModified));
			}
		}
		zipFrag.imageLoader.displayImage(le, zipFrag.getContext(), image, zipFrag.spanCount);
	}

	private class OnClickListener implements View.OnClickListener {
		private final int pos;
		private OnClickListener(int pos) {
			this.pos = pos;
		}
		@Override
		public void onClick(final View v) {
			zipFrag.select(true);
			//final Integer pos = Integer.valueOf(v.getContentDescription().toString());
			final ZipEntry rowItem = mDataset.get(pos);
			final String path = rowItem.path;
			Log.d(TAG, "onClick, " + rowItem.path + ", " + v.getTag());

			final int id = v.getId();
			if (id == R.id.more) {
				final MenuBuilder menuBuilder = new MenuBuilder(zipFrag.activity);
				final MenuInflater inflater = new MenuInflater(zipFrag.activity);
				inflater.inflate(R.menu.zip_commands, menuBuilder);
				final MenuPopupHelper optionsMenu = new MenuPopupHelper(zipFrag.activity , menuBuilder, zipFrag.searchButton);
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
							Log.d(TAG, "onClick, " + rowItem.path + ", " + item);
							final ExplorerActivity activity = zipFrag.activity;
							switch (item.getItemId()) {
								case R.id.copy:
									//copy(v);
									activity.COPY_PATH = null;
									activity.MOVE_PATH = null;
									activity.EXTRACT_MOVE_PATH = null;
									ArrayList<String> copies = new ArrayList<>(1);
									copies.add(rowItem.path);//zipFrag.currentPathTitle + "|" + 
									activity.EXTRACT_PATH = copies;

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
									activity.COPY_PATH = null;
									activity.MOVE_PATH = null;
									activity.EXTRACT_PATH = null;
									copies = new ArrayList<>(1);
									copies.add(rowItem.path);
									activity.zip = zipFrag.zip;
									activity.EXTRACT_MOVE_PATH = copies;
									activity.callback = new Runnable() {
										@Override
										public void run() {
											zipFrag.changeDir(zipFrag.currentPathTitle, true, false, new Runnable() {
													@Override
													public void run() {
														zipFrag.changeDir(rowItem.parentPath, false, true, null);
													}
												});
										}
									};
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
									zipFrag.rename(rowItem);
									break;
								case R.id.delete:
									ArrayList<ZipEntry> ele = new ArrayList<ZipEntry>(1);
									ele.add(rowItem);
									//new Futils().deleteFiles(ele, fileFrag.activity, /*positions, */activity.getAppTheme());
									activity.callback = new Runnable() {
										@Override
										public void run() {
											zipFrag.changeDir(zipFrag.currentPathTitle, true, false, new Runnable() {
													@Override
													public void run() {
														zipFrag.changeDir(rowItem.parentPath, false, true, null);
													}
												});
										}
									};
									GeneralDialogCreation.deleteFilesDialog(activity, //getLayoutElements(),
																			activity, zipFrag.zip, ele, activity.getAppTheme(), activity.callback);
									break;
								case R.id.share:
									Runnable r = new Runnable() {
										@Override
										public void run() {
											ArrayList<File> arrayList = new ArrayList<>(1);
											arrayList.add(new File(ExplorerApplication.PRIVATE_PATH + "/" + rowItem.path));
											Futils.shareFiles(arrayList, activity, activity.getAppTheme(), zipFrag.accentColor);
										}
									};
									extractZe(rowItem, r);
									break;
								case R.id.info:
									r = new Runnable() {
										@Override
										public void run() {
											GeneralDialogCreation.showPropertiesDialog(rowItem,
																					   activity, 
																					   activity.getAppTheme(), zipFrag.totalZipLength, zipFrag.totalUnzipLength);
										}
									};
									extractZe(rowItem, r);
									break;
								case R.id.name:
									AndroidUtils.copyToClipboard(activity, rowItem.name);
									break;
								case R.id.path:
									AndroidUtils.copyToClipboard(activity, rowItem.parentPath);
									break;
								case R.id.fullname:
									AndroidUtils.copyToClipboard(activity, rowItem.path);
									break;
								case R.id.open_with:
									r = new Runnable() {
										@Override
										public void run() {
											Futils.openunknown(new File(ExplorerApplication.PRIVATE_PATH + "/" + rowItem.path), activity, true);
										}
									};
									extractZe(rowItem, r);
									break;
								case R.id.extract:
									activity.decompress(zipFrag.currentPathTitle, new File(zipFrag.currentPathTitle).getParent(), rowItem.path, true);
									break;
							}

							return true ;
						}
						@Override
						public void onMenuModeChange(MenuBuilder menu) {}
					});
				optionsMenu.show();
				return;
			} else if (id == R.id.icon) {
				zipFrag.tempPreviewL2 = rowItem;
				notifyDataSetChanged();
				if (!rowItem.isDirectory) {
					Runnable r = new Runnable() {
						@Override
						public void run() {
							load(new File(rowItem.path), ExplorerApplication.PRIVATE_PATH + "/" + path, pos);
						}
					};
					extractZe(rowItem, r);
				} else {
					final SlidingTabsFragment.PagerAdapter pagerAdapter;
					final SlidingTabsFragment slidingTabsFragment;
					if (zipFrag.slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {//dir
						if (zipFrag.activity.slideFrag2 != null) {
							pagerAdapter = zipFrag.activity.slideFrag2.pagerAdapter;
							slidingTabsFragment = zipFrag.activity.slideFrag2;
						} else {
							pagerAdapter = null;
							slidingTabsFragment = null;
						}
					} else {//dir
						pagerAdapter = zipFrag.activity.slideFrag.pagerAdapter;
						slidingTabsFragment = zipFrag.activity.slideFrag;
					}
					if (slidingTabsFragment != null && pagerAdapter != null) {
						final int tabIndex2 = SlidingTabsFragment.getFragTypeIndex(zipFrag, Frag.TYPE.ZIP);
						if (tabIndex2 >= 0) {
							final ZipFragment zFrag = (ZipFragment) pagerAdapter.getItem(tabIndex2);
							final Runnable r = new Runnable() {
								@Override
								public void run() {
									zFrag.changeDir(rowItem.path, false, true, null);
								}
							};
							zFrag.load(zipFrag.currentPathTitle, r);
							slidingTabsFragment.setCurrentItem(tabIndex2, true);
						} else {
							slidingTabsFragment.addTab(Frag.TYPE.ZIP, zipFrag.currentPathTitle);
							zipFrag.listView.postDelayed(new Runnable() {
									@Override
									public void run() {
										final ZipFragment zFrag = (ZipFragment) pagerAdapter.getItem(slidingTabsFragment.pageSelected);
										zFrag.changeDir(rowItem.path, false, true, null);
									}
								}, 100);
						}
					}
				}
			} else if (id == R.id.cbx) {//file and folder
				if (zipFrag.selectedInList1.remove(rowItem)) {
					if (zipFrag.selectedInList1.size() == 0 && zipFrag.activity.COPY_PATH == null && zipFrag.activity.MOVE_PATH == null && zipFrag.commands.getVisibility() == View.VISIBLE) {
						zipFrag.horizontalDivider6.setVisibility(View.GONE);
						zipFrag.commands.setAnimation(AnimationUtils.loadAnimation(zipFrag.activity, R.anim.shrink_from_top));
						zipFrag.commands.setVisibility(View.GONE);
					}
				} else {
					zipFrag.selectedInList1.add(rowItem);
					if (zipFrag.commands.getVisibility() == View.GONE) {
						zipFrag.commands.setAnimation(AnimationUtils.loadAnimation(zipFrag.activity, R.anim.grow_from_bottom));
						zipFrag.commands.setVisibility(View.VISIBLE);
						zipFrag.horizontalDivider6.setVisibility(View.VISIBLE);
					}
				}
				notifyDataSetChanged();
			} else if (rowItem.isDirectory) { 
				if (zipFrag.selectedInList1.size() == 0) { 
					zipFrag.changeDir(path, false, true, null);
				} else {
					if (zipFrag.selectedInList1.remove(rowItem)) {
						if (zipFrag.selectedInList1.size() == 0 && zipFrag.activity.COPY_PATH == null && zipFrag.activity.MOVE_PATH == null && zipFrag.commands.getVisibility() == View.VISIBLE) {
							zipFrag.horizontalDivider6.setVisibility(View.GONE);
							zipFrag.commands.setAnimation(AnimationUtils.loadAnimation(zipFrag.activity, R.anim.shrink_from_top));
							zipFrag.commands.setVisibility(View.GONE);
						} 
					} else {
						zipFrag.selectedInList1.add(rowItem);
						if (zipFrag.commands.getVisibility() == View.GONE) {
							zipFrag.commands.setAnimation(AnimationUtils.loadAnimation(zipFrag.activity, R.anim.grow_from_bottom));
							zipFrag.commands.setVisibility(View.VISIBLE);
							zipFrag.horizontalDivider6.setVisibility(View.VISIBLE);
						}
					}
					notifyDataSetChanged();
				}
			} else { //file
				if (zipFrag.selectedInList1.size() == 0) { //open
					Runnable r = new Runnable() {
						@Override
						public void run() {
							zipFrag.activity.getFutils().openFile(new File(ExplorerApplication.PRIVATE_PATH + "/" + rowItem.path), zipFrag.activity);
						}
					};
					extractZe(rowItem, r);
				} else {
					if (zipFrag.selectedInList1.remove(rowItem)) {
						if (zipFrag.selectedInList1.size() == 0 && zipFrag.activity.COPY_PATH == null && zipFrag.activity.MOVE_PATH == null && zipFrag.commands.getVisibility() == View.VISIBLE) {
							zipFrag.horizontalDivider6.setVisibility(View.GONE);
							zipFrag.commands.setAnimation(AnimationUtils.loadAnimation(zipFrag.activity, R.anim.shrink_from_top));
							zipFrag.commands.setVisibility(View.GONE);
						} 
					} else {
						zipFrag.selectedInList1.add(rowItem);
						if (zipFrag.commands.getVisibility() == View.GONE) {
							zipFrag.commands.setAnimation(AnimationUtils.loadAnimation(zipFrag.activity, R.anim.grow_from_bottom));
							zipFrag.commands.setVisibility(View.VISIBLE);
							zipFrag.horizontalDivider6.setVisibility(View.VISIBLE);
						}
					}
					notifyDataSetChanged();
				}
			}
			if ((zipFrag.currentPathTitle == null || zipFrag.currentPathTitle.length() > 0)) {
				zipFrag.selectionStatusTV.setText(zipFrag.selectedInList1.size() 
												  + "/" + zipFrag.dataSourceL1.size());
			}
			zipFrag.updateDelPaste();
		}

		private void extractZe(final ZipEntry rowItem, Runnable r) {
			Log.d(TAG, "extractZe " + rowItem);
			new DecompressTask(zipFrag.fragActivity,
							   zipFrag.currentPathTitle,
							   ExplorerApplication.PRIVATE_PATH,
							   rowItem.path,
							   "",
							   "",
							   "",
							   0,
							   "x",
							   r).execute();
		}
	}

	private void load(final File f, final String fPath, final int pos) throws IllegalStateException {
		Log.d(TAG, "load " + fPath);
		if (zipFrag.activity.slideFrag2 == null) {
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
		if (zipFrag.slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
			pagerAdapter = zipFrag.activity.slideFrag2.pagerAdapter;
			slidingTabsFragment = zipFrag.activity.slideFrag2;
		} else {
			pagerAdapter = zipFrag.activity.slideFrag.pagerAdapter;
			slidingTabsFragment = zipFrag.activity.slideFrag;
		}
		if (mime.startsWith("text/html") || mime.startsWith("text/xhtml")) {
			tabIndex1 = SlidingTabsFragment.getFragTypeIndex(zipFrag, Frag.TYPE.TEXT);
			tabIndex2 = SlidingTabsFragment.getFragTypeIndex(zipFrag, Frag.TYPE.WEB);
			if (tabIndex1 >= 0) {
				pagerAdapter.getItem(tabIndex1).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex1, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.TEXT, fPath);
				zipFrag.listView.postDelayed(new Runnable() {
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
				zipFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							slidingTabsFragment.addTab(Frag.TYPE.WEB, fPath);
							zipFrag.listView.postDelayed(new Runnable() {
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
				sb.append(AndroidUtils.getSignature(zipFrag.activity, fPath));
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
				tabIndex2 = SlidingTabsFragment.getFragTypeIndex(zipFrag, Frag.TYPE.WEB);
				if (tabIndex2 >= 0) {
					pagerAdapter.getItem(tabIndex2).load(name);
					slidingTabsFragment.setCurrentItem(tabIndex2, true);
				} 
				if (tabIndex2 < 0) {
					slidingTabsFragment.addTab(Frag.TYPE.WEB, name);
					zipFrag.listView.postDelayed(new Runnable() {
							@Override
							public void run() {
								pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(name);
							}
						}, 100);
				}
				byte[] bytes = FileUtil.readFileToMemory(f);
				new FillClassesNamesThread(zipFrag.activity.slideFrag1Selected ? zipFrag.activity.curContentFrag : zipFrag.activity.curExplorerFrag, bytes, f, sb1, sb2, ExplorerActivity.END_PRE).start();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} else if (mime.startsWith("application/x-chm")) {

			tabIndex2 = SlidingTabsFragment.getFragTypeIndex(zipFrag, Frag.TYPE.CHM);
			if (tabIndex2 >= 0) {
				pagerAdapter.getItem(tabIndex2).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.CHM, fPath);
				zipFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
						}
					}, 100);
			}
		} else if (mime.startsWith("application/pdf")) {
			//pagerAdapter.getItem(i = Frag.TYPE.PDF.ordinal()).load(fPath);
			tabIndex2 = SlidingTabsFragment.getFragTypeIndex(zipFrag, Frag.TYPE.PDF);
			if (tabIndex2 >= 0) {
				pagerAdapter.getItem(tabIndex2).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.PDF, fPath);
				zipFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
						}
					}, 100);
			}
		} else if (mime.startsWith("image/svg+xml")) {
			//pagerAdapter.getItem(i = Frag.TYPE.TEXT.ordinal()).load(fPath);
			//pagerAdapter.getItem(i = Frag.TYPE.PHOTO.ordinal()).load(fPath);
			tabIndex1 = SlidingTabsFragment.getFragTypeIndex(zipFrag, Frag.TYPE.TEXT);
			tabIndex2 = SlidingTabsFragment.getFragTypeIndex(zipFrag, Frag.TYPE.PHOTO);
			if (tabIndex1 >= 0) {
				pagerAdapter.getItem(tabIndex1).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex1, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.TEXT, fPath);
				zipFrag.listView.postDelayed(new Runnable() {
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
				zipFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							slidingTabsFragment.addTab(Frag.TYPE.PHOTO, fPath);
							zipFrag.listView.postDelayed(new Runnable() {
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
		} else if (mime.startsWith("text")) {
			//pagerAdapter.getItem(i = Frag.TYPE.TEXT.ordinal()).load(fPath);
			tabIndex2 = SlidingTabsFragment.getFragTypeIndex(zipFrag, Frag.TYPE.TEXT);
			if (tabIndex2 >= 0) {
				pagerAdapter.getItem(tabIndex2).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.TEXT, fPath);
				zipFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
						}
					}, 100);
			}
		} else if (mime.startsWith("video")) {
			//pagerAdapter.getItem(i = Frag.TYPE.MEDIA.ordinal()).load(fPath);
			tabIndex2 = SlidingTabsFragment.getFragTypeIndex(zipFrag, Frag.TYPE.MEDIA);
			if (tabIndex2 >= 0) {
				pagerAdapter.getItem(tabIndex2).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.MEDIA, fPath);
				zipFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
						}
					}, 500);
			}
		} else if (FileUtil.extractiblePattern.matcher(fPath).matches()) {
			//pagerAdapter.getItem(i = Frag.TYPE.PHOTO.ordinal()).open(pos, mDataset);
			tabIndex2 = SlidingTabsFragment.getFragTypeIndex(zipFrag, Frag.TYPE.ZIP);
			if (tabIndex2 >= 0) {
				pagerAdapter.getItem(tabIndex2).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.ZIP, fPath);
			}
		} else if (mime.startsWith("image")) {
			//pagerAdapter.getItem(i = Frag.TYPE.PHOTO.ordinal()).open(pos, mDataset);
			tabIndex2 = SlidingTabsFragment.getFragTypeIndex(zipFrag, Frag.TYPE.PHOTO);
			final String[] arr = new String[mDataset.size()];
			int i = 0;
			//TODO extract
			for (ZipEntry ze : mDataset) {
				arr[i++] = ze.path;
			}
			if (tabIndex2 >= 0) {
				((PhotoFragment)pagerAdapter.getItem(tabIndex2)).open(pos, arr);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.PHOTO, fPath);
				zipFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							((PhotoFragment)pagerAdapter.getItem(slidingTabsFragment.pageSelected)).open(pos, arr);
						}
					}, 100);
			}
		} else if (mime.startsWith("audio")) {
			//pagerAdapter.getItem(i = Frag.TYPE.MEDIA.ordinal()).load(fPath);
			tabIndex2 = SlidingTabsFragment.getFragTypeIndex(zipFrag, Frag.TYPE.MEDIA);
			if (tabIndex2 >= 0) {
				pagerAdapter.getItem(tabIndex2).load(fPath);
				slidingTabsFragment.setCurrentItem(tabIndex2, true);
			} else {
				slidingTabsFragment.addTab(Frag.TYPE.MEDIA, fPath);
				zipFrag.listView.postDelayed(new Runnable() {
						@Override
						public void run() {
							pagerAdapter.getItem(slidingTabsFragment.pageSelected).load(fPath);
						}
					}, 500);
			}
		} else {
			zipFrag.tempPreviewL2 = null;
		}
	}

//	private void openFile(File f, String fPath) {
//		//final File f = getZe(ele);
////		try {
////			final Uri uri = Uri.fromFile(f);
////			final Intent i = new Intent(Intent.ACTION_VIEW); 
////			i.addCategory(Intent.CATEGORY_DEFAULT);
////			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
////
////			Log.d("i.setData(uri)", uri + "." + i);
////			final String mimeType = MimeTypes.getMimeType(f);
////			i.setDataAndType(uri, mimeType);//floor.getValue()
////			Log.d(TAG, f + "=" + mimeType);
////			final Intent createChooser = Intent.createChooser(i, "View");
////			Log.i("createChooser.getExtras()", AndroidUtils.bundleToString(createChooser.getExtras()));
////			fileFrag.startActivity(createChooser);
////		} catch (Throwable e) {
////			Toast.makeText(fileFrag.activity, "unable to view !\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
////		}
//
//		// check if we're trying to click on encrypted file
//		zipFrag.activity.getFutils().openFile(f, zipFrag.activity);
//	}

	private class OnLongClickListener implements View.OnLongClickListener {
		private final int pos;
		OnLongClickListener(int pos) {
			this.pos = pos;
		}
		@Override
		public boolean onLongClick(final View v) {
			final ZipEntry rowItem = mDataset.get(pos);//Integer.valueOf(v.getContentDescription().toString())

			Log.d(TAG, "onLongClick, " + rowItem);
			Log.d(TAG, "currentSelectedList" + Util.collectionToString(zipFrag.selectedInList1, true, "\r\n"));

			if (zipFrag.selectedInList1.remove(rowItem)) {
				if (zipFrag.selectedInList1.size() == 0 && zipFrag.activity.COPY_PATH == null && zipFrag.activity.MOVE_PATH == null && zipFrag.commands.getVisibility() == View.VISIBLE) {
					zipFrag.horizontalDivider6.setVisibility(View.GONE);
					zipFrag.commands.setAnimation(AnimationUtils.loadAnimation(zipFrag.activity, R.anim.shrink_from_top));
					zipFrag.commands.setVisibility(View.GONE);
				} 
			} else {
				zipFrag.selectedInList1.add(rowItem);
				if (zipFrag.commands.getVisibility() == View.GONE) {
					zipFrag.commands.setAnimation(AnimationUtils.loadAnimation(zipFrag.activity, R.anim.grow_from_bottom));
					zipFrag.commands.setVisibility(View.VISIBLE);
					zipFrag.horizontalDivider6.setVisibility(View.VISIBLE);
				}
			}
			if ((zipFrag.currentPathTitle == null || zipFrag.currentPathTitle.length() > 0)) {
				zipFrag.selectionStatusTV.setText(zipFrag.selectedInList1.size() 
												  + "/" + zipFrag.dataSourceL1.size());
			}
			zipFrag.updateDelPaste();
			return true;
		}
	}

}
