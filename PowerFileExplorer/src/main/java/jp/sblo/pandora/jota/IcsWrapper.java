package jp.sblo.pandora.jota;

import android.os.Build;
import android.view.MenuItem;

public class IcsWrapper {

	public void setShowAsActionIfRoomWithText(MenuItem menuitem)
	{
        menuitem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	}
}

