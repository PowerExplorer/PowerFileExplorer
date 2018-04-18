package net.gnu.explorer;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import java.io.File;
import android.util.Log;
import android.view.View;
import android.view.KeyEvent;
import com.bumptech.glide.Glide;
import android.annotation.TargetApi;
import android.os.Build;

public class PhotoActivity extends FragmentActivity {

	private PhotoFragment photoFragment;
	public static final String ACTION_VIEW_LIST = "net.gnu.explorer.photo.action.VIEW_LIST";
	public static final String URI_LIST_EXTRA = "uri_list";

	private String TAG = "PhotoActivity";
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		Log.d(TAG, "onCreate " + savedInstanceState);
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.player);

		final FragmentManager supportFragmentManager = getSupportFragmentManager();

		if (savedInstanceState == null) {
			photoFragment = new PhotoFragment();
			final FragmentTransaction transaction = supportFragmentManager.beginTransaction();
			transaction.replace(R.id.content_fragment, photoFragment, "photoFragment");
			transaction.commit();
		} else {
			photoFragment = (PhotoFragment) supportFragmentManager.findFragmentByTag("photoFragment");
		}
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LOW_PROFILE);
	}
	
	@Override
	public void onNewIntent(final Intent intent) {
		final String action = intent.getAction();
		Log.d(TAG, "onNewIntent " + intent + ", action " + action + " , data " + intent.getData());
		if (Intent.ACTION_SEND.equals(action) 
			|| Intent.ACTION_VIEW.equals(action)) {
			final Uri uri = intent.getData();
			photoFragment.load(uri.getPath());
		} else if (ACTION_VIEW_LIST.equals(action)) {
			final String[] uriStrings = intent.getStringArrayExtra(URI_LIST_EXTRA);
			photoFragment.open(0, uriStrings);
		}
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LOW_PROFILE);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onTrimMemory(int level) {
		Glide.get(this).trimMemory(level);
        Glide.get(photoFragment.getContext()).trimMemory(level);
        super.onTrimMemory(level);
        Log.e(TAG, "onTrimMemory " + level + ", " + Runtime.getRuntime().freeMemory());
    }

    @Override
    public void onLowMemory() {
		Glide.get(this).clearMemory();
        Glide.get(photoFragment.getContext()).clearMemory();
        super.onLowMemory();
        Log.e(TAG, "onLowMemory " + Runtime.getRuntime().freeMemory());
    }

	@Override
	protected void onPause() {
//		Glide.get(this).clearMemory();
//        Glide.get(photoFragment.getContext()).clearMemory();
        super.onPause();
	}

    @Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions,
										   int[] grantResults) {
		photoFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

//	@Override
//	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
//		super.onBackPressed();
//		return true;
//	}
//
//	private static final int TIME_INTERVAL = 250;
//	private long mBackPressed;
//	@Override
//	public void onBackPressed() {
//		if (mBackPressed + TIME_INTERVAL >= System.currentTimeMillis()) {
//			super.onBackPressed();
//		} else {
//			if (photoFragment.getCurrentItem() == 0) {
//				// If the user is currently looking at the first step, allow t
//				// Back button. This calls finish() on this activity and pops
//				super.onBackPressed();
//			} else {
//				// Otherwise, select the previous step.
//				photoFragment.setCurrentItem(photoFragment.getCurrentItem() - 1);
//			}
//		}
//	}

	
	
	// Activity input

//	@Override
//	public boolean dispatchKeyEvent(KeyEvent event) {
//		return super.dispatchKeyEvent(event) || mediaPlayerFragment.dispatchKeyEvent(event);
//	}
	
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		return mediaPlayerFragment.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
//	}
}

