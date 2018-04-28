package net.gnu.explorer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import com.thefinestartist.finestwebview.WebFragment;
import net.gnu.androidutil.AndroidUtils;
import net.gnu.common.logger.Log;
import net.gnu.explorer.R;

public class WebActivity extends FragmentActivity {
    
    private WebFragment fragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdf_main);
        onNewIntent(getIntent());
    }

	@Override
	protected void onNewIntent(Intent intent) {
		Log.i("intent", intent + ".");
		if (intent != null) {
			Log.d("intent.getData()", intent.getData() + ".");
			Log.d("intent.getPackage()", intent.getPackage() + ".");
			Log.d("intent.getFlags()", intent.getFlags() + ".");
			Log.d("intent.getType()", intent.getType() + ".");
			Log.d("intent.getComponent()", intent.getComponent() + ".");
			Bundle extras = intent.getExtras();
			Log.d("intent.getExtras()", AndroidUtils.bundleToString(extras));
			if (Intent.ACTION_SEND.equals(intent.getAction()) 
				|| Intent.ACTION_VIEW.equals(intent.getAction())) {
				Uri data = intent.getData();
				String url = data.toString();
				FragmentManager fragmentManager = getSupportFragmentManager();
				if (fragment == null || fragmentManager.findFragmentByTag("web") == null) {
					fragment = new WebFragment();
					Bundle args = new Bundle();
					args.putString("url", url);
					fragment.setArguments(args);
					fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, "web").commit();
				} else {
					fragment.load(url);
				}
			}
		}
	}
}

