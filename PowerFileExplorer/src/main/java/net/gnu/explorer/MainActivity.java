//package net.gnu.explorer;
//
//import android.os.Bundle;
//import android.support.v4.app.FragmentTransaction;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.ViewAnimator;
//
//import net.gnu.common.activities.SampleActivityBase;
//import net.gnu.common.logger.Log;
//import net.gnu.common.logger.LogFragment;
//import net.gnu.common.logger.LogWrapper;
//import net.gnu.common.logger.MessageOnlyLogFilter;
//
////import com.amaze.filemanager.fragments.*;
//
///**
// * A simple launcher activity containing a summary sample description, sample
// * log and a custom {@link android.support.v4.app.Fragment} which can display a
// * view.
// * <p>
// * For devices with displays with a width of 720dp or greater, the sample log is
// * always visible, on other devices it's visibility is controlled by an item on
// * the Action Bar.
// */
//public class MainActivity extends SampleActivityBase {
//
//	public static final String TAG = "MainActivity";
//
//	// Whether the Log Fragment is currently shown
//	private boolean mLogShown;
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_main);
//
//		if (savedInstanceState == null) {
//			FragmentTransaction transaction = getSupportFragmentManager()
//					.beginTransaction();
//			// SlidingTabsFragment fragment = new SlidingTabsFragment();
//			AppsFragment fragment = new AppsFragment();
//			transaction.replace(R.id.content_fragment, fragment);
//			transaction.commit();
//		}
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onPrepareOptionsMenu(Menu menu) {
//		MenuItem logToggle = menu.findItem(R.id.menu_toggle_log);
//		logToggle
//				.setVisible(findViewById(R.id.sample_output) instanceof ViewAnimator);
//		logToggle.setTitle(mLogShown ? R.string.sample_hide_log
//				: R.string.sample_show_log);
//
//		return super.onPrepareOptionsMenu(menu);
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.menu_toggle_log:
//			mLogShown = !mLogShown;
//			ViewAnimator output = (ViewAnimator) findViewById(R.id.sample_output);
//			if (mLogShown) {
//				output.setDisplayedChild(1);
//			} else {
//				output.setDisplayedChild(0);
//			}
//			supportInvalidateOptionsMenu();
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
//
//	/** Create a chain of targets that will receive log data */
//	@Override
//	public void initializeLogging() {
//		// Wraps Android's native log framework.
//		LogWrapper logWrapper = new LogWrapper();
//		// Using Log, front-end to the logging chain, emulates android.util.log
//		// method signatures.
//		Log.setLogNode(logWrapper);
//
//		// Filter strips out everything except the message text.
//		MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
//		logWrapper.setNext(msgFilter);
//
//		// On screen logging via a fragment with a TextView.
//		LogFragment logFragment = (LogFragment) getSupportFragmentManager()
//				.findFragmentById(R.id.log_fragment);
//		msgFilter.setNext(logFragment.getLogView());
//
//		Log.i(TAG, "Ready");
//	}
//}
