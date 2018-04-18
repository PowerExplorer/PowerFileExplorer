//package net.gnu.explorer;
//
//import android.text.TextWatcher;
//import android.util.Log;
//import android.os.AsyncTask;
//import android.text.Editable;
//import java.io.File;
//import java.util.Collection;
//import net.gnu.util.Util;
//import net.gnu.util.FileUtil;
//import android.view.View;
//import com.amaze.filemanager.ui.LayoutElements;
//
//class TextSearch implements TextWatcher {
//
//	private static final String TAG = "TextSearch";
//	
//	private final FileFrag fileFrag;
//	
//	TextSearch(FileFrag fileFrag) {
//		this.fileFrag = fileFrag;
//	}
//
//	public void beforeTextChanged(CharSequence s, int start, int end, int count) {
//	}
//
//	public void onTextChanged(CharSequence s, int start, int end, int count) {
//	}
//
//	public void afterTextChanged(final Editable text) {
//		final String filesearch = text.toString();
//		Log.d(TAG, "quicksearch " + filesearch);
//		if (filesearch.length() > 0) {
//			if (fileFrag.searchTask != null
//				&& fileFrag.searchTask.getStatus() == AsyncTask.Status.RUNNING) {
//				fileFrag.searchTask.cancel(true);
//			}
//			
//			fileFrag.searchTask = new SearchFileNameTask(fileFrag);
//			fileFrag.searchTask.execute(filesearch);
//		}
//	}
//
//}
//
//class SearchFileNameTask extends AsyncTask<String, Long, Long> {
//
//	private static final String TAG = "SearchFileNameTask";
//
//	private final FileFrag fileFrag;
//
//	SearchFileNameTask(final FileFrag fileFrag) {
//		this.fileFrag = fileFrag;
//	}
//	
//	@Override
//	protected void onPreExecute() {
//		fileFrag.setSearchMode(true);// srcAdapter.dirStr = null;curContentFrag.
//		fileFrag.searchVal = fileFrag.quicksearch.getText().toString();//curContentFrag.
//		if (!fileFrag.mSwipeRefreshLayout.isRefreshing()) {
//			fileFrag.mSwipeRefreshLayout.setRefreshing(true);
//		}
//	}
//
//	@Override
//	protected Long doInBackground(String... params) {
//		Log.d("SearchFileNameTask", "dirTemp4Search " + fileFrag.dirTemp4Search);
//		File file = new File(fileFrag.dirTemp4Search);
//		fileFrag.dataSourceL1.clear();
//		fileFrag.activity.runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					fileFrag.showToast("Searching...");
//					fileFrag.notifyDataSetChanged();// srcAdapter.notifyDataSetChanged();curContentFrag.
//				}
//			});
//
//		if (file.exists()) {
//			Collection<File> c = FileUtil.getFilesBy(file.listFiles(),
//													 params[0], true);
//			Log.d(TAG, "getFilesBy " + Util.collectionToString(c, true, "\n"));
//			for (File le : c) {
//				fileFrag.dataSourceL1.add(new LayoutElements(le));
//			}
//			//fileFrag.dataSourceL1.addAll(c);
//			//addAllDS1(Util.collectionFile2CollectionString(c));// dataSourceL1.addAll(Util.collectionFile2CollectionString(c));curContentFrag.
//			// Log.d("dataSourceL1 new task",
//			// Util.collectionToString(dataSourceL1, true, "\n"));
//		} else {
//			fileFrag.showToast(fileFrag.dirTemp4Search + " is not existed");
//		}
//		return null;
//	}
//
//	@Override
//	protected void onPostExecute(Long result) {
//		fileFrag.notifyDataSetChanged();// srcAdapter.notifyDataSetChanged();curContentFrag.
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
