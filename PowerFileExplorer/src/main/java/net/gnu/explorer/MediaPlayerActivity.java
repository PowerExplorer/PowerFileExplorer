package net.gnu.explorer;

import android.support.v4.app.*;
import android.os.*;
import android.content.*;
import android.view.*;
import net.gnu.explorer.R;

import com.google.android.exoplayer2.demo.MediaPlayerFragment;

public class MediaPlayerActivity extends FragmentActivity {
	
	private MediaPlayerFragment mediaPlayerFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.player);
		
		FragmentManager supportFragmentManager = getSupportFragmentManager();

		if (savedInstanceState == null) {
			mediaPlayerFragment = new MediaPlayerFragment();
			FragmentTransaction transaction = supportFragmentManager.beginTransaction();
			transaction.replace(R.id.content_fragment, mediaPlayerFragment, "mediaPlayerFragment");
			transaction.commit();
		} else {
			mediaPlayerFragment = (MediaPlayerFragment) supportFragmentManager.findFragmentByTag("mediaPlayerFragment");
		}
		
	}
	
	public void onNewIntent(Intent intent) {
		mediaPlayerFragment.onNewIntent(intent);
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions,
										   int[] grantResults) {
		mediaPlayerFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	// Activity input

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return super.dispatchKeyEvent(event) || mediaPlayerFragment.dispatchKeyEvent(event);
	}
	
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		return mediaPlayerFragment.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
//	}
}
