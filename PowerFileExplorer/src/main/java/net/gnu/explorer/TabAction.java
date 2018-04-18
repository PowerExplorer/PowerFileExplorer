package net.gnu.explorer;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.content.Intent;

public interface TabAction {
	TabClicks tabClicks;
	public boolean circular();
	public void closeCurTab();
	public void closeTab(Frag m);
	public void closeOtherTabs();
	public void addTab(final Frag.TYPE t, final String path);//String dir, String suffix, boolean multi, Bundle bundle
	public void addTab(final Intent intent, final String title);
	public int size();
	public int getFragIndex(final Frag.TYPE t);
	public Frag getFragmentIndex(final int idx);
	
}


