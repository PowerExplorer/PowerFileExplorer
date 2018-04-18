package jp.sblo.pandora.jota;

import android.app.Application;
import android.os.Build;
import net.gnu.explorer.ExplorerApplication;
//import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;

public class JotaTextEditor extends Application {
    public static boolean sFroyo = ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO );
    public static boolean sHoneycomb = ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB );
    public static boolean sIceCreamSandwich = ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH );
    public static boolean sNorLater = Build.VERSION.SDK_INT > Build.VERSION_CODES.M;
	
    @Override
    public void onCreate() {
        super.onCreate();
		SettingsActivity.isVersionUp(this);
        IS01FullScreen.createInstance();
    }
	
	
	
}
