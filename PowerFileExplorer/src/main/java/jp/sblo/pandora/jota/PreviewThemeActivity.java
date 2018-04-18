package jp.sblo.pandora.jota;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import net.gnu.explorer.R;

public class PreviewThemeActivity extends Main {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mRotationControl = true;
        super.onCreate(savedInstanceState);
        mEditor.showIme(false);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if ( mWallpaper != null ){

            int width = mWallpaper.getWidth();
            int height = mWallpaper.getHeight();
            String sample = getString( R.string.label_wallpaper_size ,width , height );
            mEditor.setText( sample );
        }
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mBackkeyDown) {
            mBackkeyDown = false;
            finish();
        }
        return super.onKeyUp(keyCode, event);
    }
}
