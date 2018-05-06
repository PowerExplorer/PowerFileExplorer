package com.amaze.filemanager.utils.theme;

import android.content.SharedPreferences;

/**
 * Saves and restores the AppTheme
 */
public class AppThemeManager {
    private SharedPreferences preferences;
    private AppTheme appTheme;

    public AppThemeManager(SharedPreferences preferences) {
        this.preferences = preferences;
        appTheme = AppTheme.getTheme(preferences.getInt("theme", AppTheme.TIME_INDEX)).getSimpleTheme();
    }

    /**
     * @return The current Application theme
     */
    public AppTheme getAppTheme() {
        return appTheme.getSimpleTheme();
    }

    /**
     * Change the current theme of the application. The change is saved.
     *
     * @param appTheme The new theme
     * @return The theme manager.
     */
    public AppThemeManager setAppTheme(AppTheme appTheme) {
        this.appTheme = appTheme;
        preferences.edit().putInt("theme", appTheme.getId()).apply();
        return this;
    }

}
