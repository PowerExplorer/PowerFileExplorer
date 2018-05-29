package net.gnu.texteditor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import net.gnu.explorer.R;
import net.gnu.explorer.SlidingTabsFragment;
import android.support.v4.app.FragmentActivity;

public class TextEditorActivity extends FragmentActivity {//

    private static final String TAG = "TextEditorActivity";

    private SlidingTabsFragment slideFrag;
	private FragmentManager supportFragmentManager;
	public TextFrag main;
	
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

		supportFragmentManager = getSupportFragmentManager();
		if (savedInstanceState == null) {
            slideFrag = SlidingTabsFragment.newInstance(SlidingTabsFragment.Side.LEFT);
        } else {
			slideFrag = (SlidingTabsFragment) supportFragmentManager.findFragmentByTag("slideFrag");
		}
		final FragmentTransaction transaction = supportFragmentManager.beginTransaction();
		transaction.replace(R.id.content_fragment, slideFrag, "slideFrag");
		transaction.commit();
		final Intent intent = getIntent();
		setIntent(null);
		Log.d(TAG, "onCreate intent " + intent + ", savedInstanceState=" + savedInstanceState);
		if (savedInstanceState == null) {
			if (intent != null && intent.getData() != null) {
				slideFrag.addTextTab(intent, intent.getData().getLastPathSegment());//, "utf-8", false, '\n');
			} else {
				slideFrag.addTextTab((Intent)null, (String)null);
			}
		}
    }
	
	@Override
	protected void onStart() {
		Log.d(TAG, "onStart intent=" + getIntent() + ", main=" + main);
		super.onStart();
		if (main == null) {
			main = (TextFrag) slideFrag.getCurrentFragment();
		}
	}
	
	@Override
	public void onResume() {
		Log.d(TAG, "onResume main=" + main);
		super.onResume();
		if (main == null) {
			main = (TextFrag) slideFrag.getCurrentFragment();
		}
		Log.d(TAG, "onResume main=" + main);
	}
	
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//		Log.d(TAG, "onPrepareOptionsMenu " + menu);
//        
//        MenuItem logToggle = menu.findItem(R.id.menu_toggle_log);
//        logToggle.setVisible(findViewById(R.id.sample_output) instanceof ViewAnimator);
//        logToggle.setTitle(mLogShown ? R.string.sample_hide_log : R.string.sample_show_log);
//
//		//main.onPrepareOptionsMenu(menu);
//        return super.onPrepareOptionsMenu(menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch(item.getItemId()) {
//            case R.id.menu_toggle_log:
//                mLogShown = !mLogShown;
//                ViewAnimator output = (ViewAnimator) findViewById(R.id.sample_output);
//                if (mLogShown) {
//					output.setVisibility(View.VISIBLE);
//                    output.setDisplayedChild(0);
//                } else {
//                    output.setVisibility(View.GONE);//.setDisplayedChild(0);
//                }
//                supportInvalidateOptionsMenu();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    /** Create a chain of targets that will receive log data */
//    @Override
//    public void initializeLogging() {
//        // Wraps Android's native log framework.
//        LogWrapper logWrapper = new LogWrapper();
//        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
//        com.free.common.logger.Log.setLogNode(logWrapper);
//
//        // Filter strips out everything except the message text.
//        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
//        logWrapper.setNext(msgFilter);
//
//        // On screen logging via a fragment with a TextView.
//        LogFragment logFragment = (LogFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.log_fragment);
//        msgFilter.setNext(logFragment.getLogView());
//
//        Log.d(TAG, "Ready");
//    }
	
	public void quit() {
		Log.d(TAG, "quit " + main);
		TextFrag.saved = 0;
		TextFrag.count = slideFrag.pagerAdapter.getCount();
		for (int i = TextFrag.count == 1 ? 0 : TextFrag.count - 2; TextFrag.count == 1 ? i==0 : i > 0; i--) {
			final TextFrag item = (TextFrag) slideFrag.pagerAdapter.getItem(slideFrag.pagerAdapter.getCount() == 1 ? 0 : i);
			item.confirmSave(item.mProcQuit);
		}
		//m.confirmSave(m.mProcQuit);
	}
	
	@Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent " + intent + ", " + intent.getData().getLastPathSegment());
		super.onNewIntent(intent);
		//m.onNewIntent(intent);
		//intent = getIntent();
		//setIntent(null);
		if (intent != null && !Intent.ACTION_MAIN.equals(intent.getAction())) {
			slideFrag.addTextTab(intent, intent.getData().getLastPathSegment());//, "utf-8", false, '\n');
		}
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		return main.onKeyDown(keyCode, event);
	}
	
	@Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
		return main.onKeyUp(keyCode, event);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		//this.menu = menu;
		Log.d(TAG, "onCreateOptionsMenu " + menu);
        super.onCreateOptionsMenu(menu);
		
        //getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }
	
//	public boolean onMenuItemSelected(int featureId, MenuItem item) {
//		return main.onMenuItemSelected(featureId, item);
////		if (main.onMenuItemSelected(featureId, item)) {
////			return true;
////		} else {
////			return super.onMenuItemSelected(featureId, item);
////		}
//    }
}
