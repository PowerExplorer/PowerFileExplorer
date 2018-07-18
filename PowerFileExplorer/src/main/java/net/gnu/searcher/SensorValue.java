package net.gnu.searcher;

import java.io.Serializable;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.hardware.*;
import android.app.*;
import android.widget.*;
import net.gnu.util.Util;

/**
 * Activities that contain this fragment must implement the
 * {@link net.rdrei.android.dirchooser.DirectoryChooserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link net.rdrei.android.dirchooser.DirectoryChooserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SensorValue extends Activity implements SensorEventListener {
    private SensorManager mgr;
    private Sensor compass;
    private TextView text;

	public static final int TYPE_ACCELEROMETER = 1;

    public static final int TYPE_ALL = -1;

    public static final int TYPE_AMBIENT_TEMPERATURE = 13;

    public static final int TYPE_GAME_ROTATION_VECTOR = 15;

    public static final int TYPE_GEOMAGNETIC_ROTATION_VECTOR = 20;

    public static final int TYPE_GRAVITY = 9;

    public static final int TYPE_GYROSCOPE = 4;

    public static final int TYPE_GYROSCOPE_UNCALIBRATED = 16;

    public static final int TYPE_HEART_RATE = 21;

    public static final int TYPE_LIGHT = 5;

    public static final int TYPE_LINEAR_ACCELERATION = 10;

    public static final int TYPE_MAGNETIC_FIELD = 2;

    public static final int TYPE_MAGNETIC_FIELD_UNCALIBRATED = 14;

    /** @deprecated */
    @java.lang.Deprecated()
    public static final int TYPE_ORIENTATION = 3;

    public static final int TYPE_PRESSURE = 6;

    public static final int TYPE_PROXIMITY = 8;

    public static final int TYPE_RELATIVE_HUMIDITY = 12;

    public static final int TYPE_ROTATION_VECTOR = 11;

    public static final int TYPE_SIGNIFICANT_MOTION = 17;

    public static final int TYPE_STEP_COUNTER = 19;

    public static final int TYPE_STEP_DETECTOR = 18;

    /** @deprecated */
    @java.lang.Deprecated()
    public static final int TYPE_TEMPERATURE = 7;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		text = new TextView(this);
        setContentView(text);

        mgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        compass = mgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		if(compass == null) {
        	Toast.makeText(this, "This device has no gyroscope sensor", Toast.LENGTH_LONG ).show();
        	finish();
        }
        //text = (TextView) findViewById(R.id.text);
    }

    @Override
    protected void onResume() {
        mgr.registerListener(this, compass, SensorManager.SENSOR_DELAY_NORMAL);
    	super.onResume();
    }

    @Override
    protected void onPause() {
        mgr.unregisterListener(this, compass);
    	super.onPause();
    }

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// ignore
	}

	public void onSensorChanged(SensorEvent event) {
		String msg = event.sensor.getName() + ": " + event.sensor.getStringType() + "\n" + Util.dtf.format(event.timestamp) + "\n";
		if (event.values.length == 3) {
			msg = String.format("X: %8.4f\nY: %8.4f\nZ: %8.4f",
								   event.values[0], event.values[1], event.values[2]);
		} else if (event.values.length == 2) {
			msg = String.format("X: %8.4f\nY: %8.4f",
								event.values[0], event.values[1]);
		} else if (event.values.length == 1) {
			msg = String.format("X: %8.4f",
								event.values[0]);
		} else {
			for (float f : event.values) {
				msg += f + "\n";
			}
		}
		text.setText(msg);
		text.invalidate();
	}
}
