package net.gnu.explorer;

import android.support.v4.app.Fragment;

public abstract class DeferredFragmentTransaction {
    private int contentFrameId;
    private Fragment replacingFragment;
    
	public abstract void commit();
	
    public int getContentFrameId() {
        return contentFrameId;
    }
	
    public void setContentFrameId(int contentFrameId) {
        this.contentFrameId = contentFrameId;
    }
	
    public Fragment getReplacingFragment() {
        return replacingFragment;
    }
	
    public void setReplacingFragment(Fragment replacingFragment) {
        this.replacingFragment = replacingFragment;
    }
}
