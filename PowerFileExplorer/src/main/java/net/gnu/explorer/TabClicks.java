package net.gnu.explorer;

import android.content.*;
import android.view.*;
import android.widget.*;
import android.util.*;
import net.gnu.texteditor.TextEditorActivity;

public class TabClicks {

	private static final String TAG = "TabClicks";

	private final int maxTabs;

	public TabClicks(final int maxTabs) {
		this.maxTabs = maxTabs;
	}

	public void click(final Context ctx, final SlidingTabsFragment.PagerAdapter adapter, final TabAction tabAction, final View v, final Frag fra) {
		Log.d(TAG, "click " + ctx + ", " + tabAction + ", " + v);
		if (tabAction == null || ctx == null || v == null) {
			return;
		}
		final PopupMenu popup = new PopupMenu(ctx, v);
		final Frag.TYPE type = fra.type;
		if (type == Frag.TYPE.TEXT && fra.fragActivity instanceof TextEditorActivity) {
			popup.getMenuInflater().inflate(R.menu.newtexttab, popup.getMenu());
		} else {
			popup.getMenuInflater().inflate(R.menu.tabs_popup, popup.getMenu());
		}

		final int count = tabAction.realFragCount();
		final Menu menu = popup.getMenu();
		if (count == 1) {
			menu.findItem(R.id.close).setVisible(false);
			menu.findItem(R.id.closeOthers).setVisible(false);
		}

		if (type == Frag.TYPE.TEXT && fra.fragActivity instanceof TextEditorActivity) {
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
						} 
						if (fra.slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
							fra.activity.curContentFragIndex = fra.activity.slideFrag.realFragCount() == 1 ? 0 : fra.activity.slideFrag.indexOfMTabs(fra.activity.curContentFrag) + 1;
							//fra.activity.curSelectionFragIndex = fra.activity.slideFrag.getFragIndex(Frag.TYPE.SELECTION);
						} else {
							fra.activity.curExplorerFragIndex = fra.activity.slideFrag2.realFragCount() == 1 ? 0 : fra.activity.slideFrag2.indexOfMTabs(fra.activity.curExplorerFrag) + 1;
							//fra.activity.curSelectionFragIndex2 = fra.activity.slideFrag2.getFragIndex(Frag.TYPE.SELECTION);
						}
						return true;
					}
				});
		} else {
			if (type != Frag.TYPE.EXPLORER) {
				menu.findItem(R.id.newTab).setVisible(false);
			} else {
				menu.findItem(R.id.explorer).setVisible(false);
			}
			final int no = adapter.getCount() - 2;
			Frag frag;
			int explorerCount = 0;
			if (no > 1) {
				for (int i = 1; i <= no; i++) {
					frag = adapter.getItem(i);

					if (frag.type == Frag.TYPE.SELECTION) {
						menu.findItem(R.id.selection).setVisible(false);
					} else if (frag.type == Frag.TYPE.APP) {
						menu.findItem(R.id.apps).setVisible(false);
					} else if (frag.type == Frag.TYPE.PROCESS) {
						menu.findItem(R.id.process).setVisible(false);
					} else if (frag.type == Frag.TYPE.WEB) {
						menu.findItem(R.id.web).setVisible(false);
					} else if (frag.type == Frag.TYPE.MEDIA) {
						menu.findItem(R.id.media).setVisible(false);
					} else if (frag.type == Frag.TYPE.TRAFFIC_STATS) {
						menu.findItem(R.id.traffic).setVisible(false);
					} else if (frag.type == Frag.TYPE.TEXT) {
						menu.findItem(R.id.text).setVisible(false);
					} else if (frag.type == Frag.TYPE.PDF) {
						menu.findItem(R.id.pdf).setVisible(false);
					} else if (frag.type == Frag.TYPE.PHOTO) {
						menu.findItem(R.id.photo).setVisible(false);
					} else if (frag.type == Frag.TYPE.CHM) {
						menu.findItem(R.id.chm).setVisible(false);
					} else if (frag.type == Frag.TYPE.FTP) {
						menu.findItem(R.id.ftp).setVisible(false);
					} else if (frag.type == Frag.TYPE.EXPLORER) {
						explorerCount++;
					} 
				}
				if (explorerCount == 1 && type == Frag.TYPE.EXPLORER) {
					menu.findItem(R.id.close).setVisible(false);
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
						} else if (R.id.ftp == itemId) {
							tabAction.addTab(Frag.TYPE.FTP, null);
						} else if (R.id.chm == itemId) {
							tabAction.addTab(Frag.TYPE.CHM, null);
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
						if (fra.slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
							fra.activity.curContentFragIndex = fra.activity.slideFrag.realFragCount() == 1 ? 0 : fra.activity.slideFrag.indexOfMTabs(fra.activity.curContentFrag) + 1;
							//fra.activity.curSelectionFragIndex = fra.activity.slideFrag.getFragIndex(Frag.TYPE.SELECTION);
						} else {
							fra.activity.curExplorerFragIndex = fra.activity.slideFrag2.realFragCount() == 1 ? 0 : fra.activity.slideFrag2.indexOfMTabs(fra.activity.curExplorerFrag) + 1;
							//fra.activity.curSelectionFragIndex2 = fra.activity.slideFrag2.getFragIndex(Frag.TYPE.SELECTION);
						}
						return true;
					}
				});
		}
		popup.show();
	}
}
