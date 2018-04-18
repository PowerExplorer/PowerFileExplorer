package net.gnu.explorer;

import android.content.*;
import android.view.*;
import android.widget.*;
import android.util.*;

public class TabClicks {

	private static final String TAG = "TabClicks";

	private final int maxTabs;
	
	public TabClicks(final int maxTabs) {
		this.maxTabs = maxTabs;
	}

	public void click(final Context ctx, SlidingTabsFragment.PagerAdapter adapter, final TabAction tabAction, final View v, Frag.TYPE type) {
		Log.d(TAG, "click " + ctx + ", " + tabAction + ", " + v);
		if (tabAction == null || ctx == null || v == null) {
			return;
		}
		final PopupMenu popup = new PopupMenu(ctx, v);

		popup.getMenuInflater().inflate(R.menu.tabs_popup, popup.getMenu());
		final int count = tabAction.size();
		final Menu menu = popup.getMenu();
		if (count == 1) {
			menu.getItem(1).setVisible(false);
			menu.getItem(2).setVisible(false);
		}
		if (type != Frag.TYPE.EXPLORER) {
			menu.getItem(0).setVisible(false);
		}
		int ordinal = type.ordinal();
		if (ordinal == Frag.TYPE.EXPLORER.ordinal()) {
			menu.findItem(R.id.explorer).setVisible(false);
		}
		final int no = adapter.getCount() - 2;
		Frag frag;
		if (no > 1) {
			for (int i = 1; i <= no; i++) {
				frag = adapter.getItem(i);
				ordinal = frag.type.ordinal();
				if (ordinal == Frag.TYPE.SELECTION.ordinal()) {
					menu.findItem(R.id.selection).setVisible(false);
				} else if (ordinal == Frag.TYPE.APP.ordinal()) {
					menu.findItem(R.id.apps).setVisible(false);
				} else if (ordinal == Frag.TYPE.PROCESS.ordinal()) {
					menu.findItem(R.id.process).setVisible(false);
				} else if (ordinal == Frag.TYPE.WEB.ordinal()) {
					menu.findItem(R.id.web).setVisible(false);
				} else if (ordinal == Frag.TYPE.MEDIA.ordinal()) {
					menu.findItem(R.id.media).setVisible(false);
				} else if (ordinal == Frag.TYPE.TRAFFIC_STATS.ordinal()) {
					menu.findItem(R.id.traffic).setVisible(false);
				} else if (ordinal == Frag.TYPE.TEXT.ordinal()) {
					menu.findItem(R.id.text).setVisible(false);
				} else if (ordinal == Frag.TYPE.PDF.ordinal()) {
					menu.findItem(R.id.pdf).setVisible(false);
				} else if (ordinal == Frag.TYPE.PHOTO.ordinal()) {
					menu.findItem(R.id.photo).setVisible(false);
				} 
			}
		}
		
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				public boolean onMenuItemClick(final MenuItem item) {
					Log.d(TAG, "clicked " + item);
					final int itemId = item.getItemId();
					if (R.id.close == itemId) {
						tabAction.closeCurTab();
					} else if (R.id.closeOthers == itemId) {
						tabAction.closeOtherTabs();
					} else if (R.id.newTab == itemId) {
						if (count < maxTabs) {
							tabAction.addTab((Frag.TYPE)null, (String)null);
						} else {
							Toast.makeText(ctx, "Maximum " + count + " tabs only", Toast.LENGTH_SHORT).show();
						}
					} else if (R.id.explorer == itemId) {
						tabAction.addTab(Frag.TYPE.EXPLORER, "/storage");
					} else if (R.id.selection == itemId) {
						tabAction.addTab(Frag.TYPE.SELECTION, null);
					} else if (R.id.text == itemId) {
						tabAction.addTab(Frag.TYPE.TEXT, null);
					} else if (R.id.web == itemId) {
						tabAction.addTab(Frag.TYPE.WEB, null);
					} else if (R.id.pdf == itemId) {
						tabAction.addTab(Frag.TYPE.PDF, null);
					} else if (R.id.photo == itemId) {
						tabAction.addTab(Frag.TYPE.PHOTO, null);
					} else if (R.id.media == itemId) {
						tabAction.addTab(Frag.TYPE.MEDIA, null);
					} else if (R.id.apps == itemId) {
						tabAction.addTab(Frag.TYPE.APP, null);
					} else if (R.id.traffic == itemId) {
						tabAction.addTab(Frag.TYPE.TRAFFIC_STATS, null);
					} else if (R.id.process == itemId) {
						tabAction.addTab(Frag.TYPE.PROCESS, null);
					} 
					return true;
				}
			});
		popup.show();
	}
}
