package net.gnu.explorer;
import android.os.Bundle;

import android.app.Activity;
import android.widget.TextView;
import android.content.Intent;
import android.util.Log;

public class ServiceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("ServiceActivity", "onCreate");
		super.onCreate(savedInstanceState);
        final String action = getIntent().getAction();
		final Intent intent = new Intent(this, DataTrackerService.class); //getApp
		Log.d("ServiceActivity", action);
		if ("pause".equals(action)) {
			intent.putExtra("command", "pause");
			//stopService(intent);
			startService(intent);
		} else if ("start".equals(action)) {
			intent.putExtra("command", "start");
			//stopService(intent);
			startService(intent);
		} else if ("exit".equals(action)) {
			stopService(intent);
		}
		finish();
		
    }

}
