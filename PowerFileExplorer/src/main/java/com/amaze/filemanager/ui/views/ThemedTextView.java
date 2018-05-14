package com.amaze.filemanager.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.amaze.filemanager.utils.Utils;
import com.amaze.filemanager.utils.theme.AppTheme;
import net.gnu.explorer.ExplorerActivity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.amaze.filemanager.utils.theme.AppThemeManager;
import android.util.Log;

/**
 * Created by vishal on 18/1/17.
 *
 * Class sets text color based on current theme, without explicit method call in app lifecycle
 */

public class ThemedTextView extends TextView {

    public ThemedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        AppThemeManager appThemeManager = new AppThemeManager(sharedPreferences);
		//Log.d("ThemedTextView", appThemeManager.getAppTheme() + ".");
        if (appThemeManager.getAppTheme().equals(AppTheme.LIGHT)) {
            setTextColor(Utils.getColor(context, android.R.color.black));
        } else if (appThemeManager.equals(AppTheme.DARK)) {
            setTextColor(Utils.getColor(context, android.R.color.white));
        }
    }
}
