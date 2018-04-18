//package net.gnu.explorer;
//
//import android.os.AsyncTask;
//import java.util.ArrayList;
//import java.io.File;
//import com.amaze.filemanager.ui.LayoutElements;
//import java.util.Map;
//import android.util.Log;
//import java.util.Collections;
//import java.util.List;
//import net.gnu.util.FileUtil;
//import android.view.animation.AnimationUtils;
//import android.view.View;
//import net.gnu.util.Util;
//import com.amaze.filemanager.utils.OpenMode;
//import java.util.Arrays;
//
//class LoadFiles extends AsyncTask<Object, String, Void> {
//	private File curDir;
//	private Boolean doScroll;
//	final FileFrag fileFrag;
//	final ArrayList<LayoutElements> dataSourceL1a = new ArrayList<>();
//	final ExplorerActivity activity;
//	OpenMode openmode;
//
//	public LoadFiles(final FileFrag fileFrag, OpenMode openmode) {
//		this.fileFrag = fileFrag;
//		this.activity = fileFrag.activity;
//		this.openmode = openmode;
//	}
//
//	@Override
//	protected void onPreExecute() {
//		if (!fileFrag.mSwipeRefreshLayout.isRefreshing()) {
//			fileFrag.mSwipeRefreshLayout.setRefreshing(true);
//		}
//	}
//
//	@Override
//	protected Void doInBackground(final Object... objs) {
//		curDir = (File) objs[0];
//		doScroll = (Boolean) objs[1];
//
//		while (curDir != null && !curDir.exists()) {
//			publishProgress(curDir.getAbsolutePath() + " is not existed");
//			curDir = curDir.getParentFile();
//		}
//		if (curDir == null) {
//			publishProgress("Current directory is not existed. Change to root");
//			curDir = new File("/");
//		}
//
//		final String curPath = curDir.getAbsolutePath();
//		if (!fileFrag.dirTemp4Search.equals(curPath)) {
//			if (fileFrag.backStack.size() > ExplorerActivity.NUM_BACK) {
//				fileFrag.backStack.remove(0);
//			}
//			final Map<String, Object> bun = fileFrag.onSaveInstanceState();
//			fileFrag.backStack.push(bun);
//
//			fileFrag.history.remove(curPath);
//			if (fileFrag.history.size() > ExplorerActivity.NUM_BACK) {
//				fileFrag.history.remove(0);
//			}
//			fileFrag.history.push(curPath);
//
//			activity.historyList.remove(curPath);
//			if (activity.historyList.size() > ExplorerActivity.NUM_BACK) {
//				activity.historyList.remove(0);
//			}
//			activity.historyList.push(curPath);
//			fileFrag.tempPreviewL2 = null;
//		}
//
//		fileFrag.path = curPath;
//		fileFrag.dirTemp4Search = curPath;
//		if (fileFrag.tempPreviewL2 != null && !fileFrag.tempPreviewL2.bf.f.exists()) {
//			fileFrag.tempPreviewL2 = null;
//		}
//		if (fileFrag.mFileObserver != null) {
//			fileFrag.mFileObserver.stopWatching();
//		}
//		fileFrag.mFileObserver = fileFrag.createFileObserver(fileFrag.path);
//		fileFrag.mFileObserver.startWatching();
//		final List<File> files = FileUtil.currentFileFolderListing(curDir);
//		if (".*".equals(fileFrag.suffix)) {	// always dir, already checked
//			for (File f : files) {
//				dataSourceL1a.add(new LayoutElements(f));
//			}
//		} else {
//			// tìm danh sách các file có ext thích hợp
//			//Log.d("suffix", suffix);
//			String[] suffixes = fileFrag.suffix.toLowerCase().split("; *");
//			Arrays.sort(suffixes);
//			for (File f : files) {
//				if (f.exists()) {
//					String fName = f.getName();
//					//Log.d("changeDir fName", fName + ", isDir " + f.isDirectory());
//					if (f.isDirectory()) {
//						dataSourceL1a.add(new LayoutElements(f));
//					} else {
//						if (fileFrag.suffix.length() > 0) {
//							if (".*".equals(fileFrag.suffix)) {
//								dataSourceL1a.add(new LayoutElements(f));
//							} else {
//								int lastIndexOf = fName.lastIndexOf(".");
//								if (lastIndexOf >= 0) {
//									String ext = fName.substring(lastIndexOf);
//									boolean chosen = Arrays.binarySearch(suffixes, ext.toLowerCase()) >= 0;
//									if (chosen) {
//										dataSourceL1a.add(new LayoutElements(f));
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//			// điền danh sách vào allFiles
//		}
//		//dataSourceL1a.addAll(currentFileFolderListing);
//		//Log.d("filesListing", Util.collectionToString(files, true, "\r\n"));
//
//		Log.d("LoadFiles", "changeDir dataSourceL1a.size=" + dataSourceL1a.size());
//		//String dirSt = dir.getText().toString();
//		return null;
//	}
//
//	protected void onProgressUpdate(String...values) {
//		fileFrag.showToast(values[0]);
//	}
//
//	protected void onPostExecute(Object result) {
//		fileFrag.diskStatus.setText(
//			"Free " + Util.nf.format(curDir.getFreeSpace() / (1 << 20))
//			+ " MiB. Usable " + Util.nf.format(curDir.getUsableSpace() / (1 << 20))
//			+ " MiB. Total " + Util.nf.format(curDir.getTotalSpace() / (1 << 20)) + " MiB");
//		Collections.sort(dataSourceL1a, fileFrag.fileListSorter);
//
//		fileFrag.dataSourceL1.clear();
//		fileFrag.dataSourceL1.addAll(dataSourceL1a);
//		fileFrag.srcAdapter.notifyDataSetChanged();
//		dataSourceL1a.clear();
//		fileFrag.selectedInList1.clear();
//		if (fileFrag.multiFiles) {
//			boolean allInclude = (fileFrag.dataSourceL2 != null && dataSourceL1a.size() > 0) ? true : false;
//			if (allInclude) {
//				for (LayoutElements st : dataSourceL1a) {
//					if (!fileFrag.dataSourceL2.contains(st)) {
//						allInclude = false;
//						break;
//					}
//				}
//			}
//			if (allInclude) {
//				fileFrag.allCbx.setSelected(true);//.setChecked(true);
//				fileFrag.allCbx.setImageResource(R.drawable.ic_accept);
//				fileFrag.allCbx.setEnabled(false);
//			} else {
//				fileFrag.allCbx.setSelected(false);//setChecked(false);
//				fileFrag.allCbx.setImageResource(R.drawable.dot);
//				fileFrag.allCbx.setEnabled(true);
//			}
//		}
//		if (activity.copyl.size() == 0 && activity.cutl.size() == 0 && activity.rightCommands.getVisibility() == View.VISIBLE) {
//			activity.horizontalDivider6.setVisibility(View.GONE);
//			activity.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
//			activity.rightCommands.setVisibility(View.GONE);
//		}
//		//Log.d("changeDir dataSourceL1", Util.collectionToString(dataSourceL1, true, "\r\n"));
//		fileFrag.listView1.setActivated(true);
//		if (doScroll) {
//			fileFrag.listView1.scrollToPosition(0);
//		}
//
//		if (fileFrag.allCbx.isSelected()) {//}.isChecked()) {
//			fileFrag.selectionStatus1.setText(fileFrag.dataSourceL1.size() 
//											  + "/" + fileFrag.dataSourceL1.size());
//		} else {
//			fileFrag.selectionStatus1.setText(fileFrag.selectedInList1.size() 
//											  + "/" + fileFrag.dataSourceL1.size());
//		}
//
//		Log.d("LoadFile", "changeDir " + fileFrag.path + ", " + this);
//		fileFrag.updateDir((fileFrag.path != null) ? fileFrag.path : fileFrag.dirTemp4Search, fileFrag);
//		if (fileFrag.mSwipeRefreshLayout.isRefreshing()) {
//			fileFrag.mSwipeRefreshLayout.setRefreshing(false);
//		}
//		if (fileFrag.dataSourceL1.size() == 0) {
//			fileFrag.nofilelayout.setVisibility(View.VISIBLE);
//			fileFrag.mSwipeRefreshLayout.setVisibility(View.GONE);
//		} else {
//			fileFrag.nofilelayout.setVisibility(View.GONE);
//			fileFrag.mSwipeRefreshLayout.setVisibility(View.VISIBLE);
//		}
//	}
//}
