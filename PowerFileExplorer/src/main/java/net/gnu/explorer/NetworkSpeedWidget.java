//package net.gnu.explorer;
//
//import android.appwidget.AppWidgetManager;
//import android.appwidget.AppWidgetProvider;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.preference.PreferenceManager;
//import android.util.Log;
//
//public class NetworkSpeedWidget extends AppWidgetProvider {
//
//    /*This onUpdate is called when the widget is first configured, and also updates over
//    the interval defined in the updatePeriodMillis in my widget_details.xml file.
//    Though, remember, this can only work every 15 minutes, which is why I must
//    start a service, to update every second.
//    */
//    @Override
//    public void onDeleted(Context context, int[] appWidgetIds) {
//        ExplorerApplication.getInstance().stopService(new Intent(ExplorerApplication.getInstance(), NetTrackerService.class));
//        ExplorerApplication.getInstance().startService(new Intent(ExplorerApplication.getInstance(), NetTrackerService.class));
//        super.onDeleted(context, appWidgetIds);
//    }
//
//    @Override
//    public void onEnabled(Context context) {
//        super.onEnabled(context);
//    }
//
//    @Override
//    public void onDisabled(Context context) {
//
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ExplorerApplication.getInstance());
//        SharedPreferences.Editor edit = sharedPref.edit();
//        edit.putBoolean("widget_exists",false);
//        edit.commit();
//        ExplorerApplication.getInstance().stopService(new Intent(ExplorerApplication.getInstance(),NetTrackerService.class));
//        ExplorerApplication.getInstance().startService(new Intent(ExplorerApplication.getInstance(), NetTrackerService.class));
//        super.onDisabled(context);
//    }
//
//    @Override
//	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
//			int[] appWidgetIds) {
//		super.onUpdate(context, appWidgetManager, appWidgetIds);
//	}
//
//}
//
//
