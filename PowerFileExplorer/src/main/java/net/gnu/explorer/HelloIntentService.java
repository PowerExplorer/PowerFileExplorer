package net.gnu.explorer;
import android.content.Intent;
import android.app.IntentService;
import android.widget.Toast;

public class HelloIntentService extends IntentService {

	/**
	 * A constructor is required, and must call the super IntentService(String)
	 * constructor with a name for the worker thread.
	 */
	public HelloIntentService() {
		super("HelloIntentService");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
		return super.onStartCommand(intent,flags,startId);
	}
	
	/**
	 * The IntentService calls this method from the default worker thread with
	 * the intent that started the service. When this method returns, IntentService
	 * stops the service, as appropriate.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		// Normally we would do some work here, like download a file.
		// For our sample, we just sleep for 5 seconds.
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// Restore interrupt status.
			Thread.currentThread().interrupt();
		}
	}
}
