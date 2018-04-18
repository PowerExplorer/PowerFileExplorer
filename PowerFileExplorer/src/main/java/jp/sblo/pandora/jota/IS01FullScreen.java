package jp.sblo.pandora.jota;

import java.lang.reflect.Method;
import android.view.inputmethod.InputMethodManager;
import android.app.Activity;
import android.content.Context;
import android.view.WindowManager;

public class IS01FullScreen {
    static Method setFullScreenMode=null;

    static public void createInstance() {
        try {
            Class<?> sgManager = Class.forName("jp.co.sharp.android.softguide.SoftGuideManager");
            Class<?> paramstype[] = {boolean.class};
            setFullScreenMode = sgManager.getMethod("setFullScreenMode", paramstype);
        } catch (Exception o) {
			//o.printStackTrace();
        }
    }

    static public boolean isIS01orLynx() {
        return setFullScreenMode != null;
    }

    static public void setFullScreenOnIS01() {
        if (setFullScreenMode != null) {
            try {
                setFullScreenMode.invoke(null, true);
            } catch (Exception e) {
				e.printStackTrace();
			}
//        } else if (activity != null) {
//			InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
////			imm.showSoftInput(searchET, InputMethodManager.SHOW_IMPLICIT);
//			imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
////			imm.showSoftInput(quicksearch, InputMethodManager.SHOW_FORCED);
////			imm.showSoftInput(searchET, InputMethodManager.SHOW_IMPLICIT);
////			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
//			//activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		}
    }

}
