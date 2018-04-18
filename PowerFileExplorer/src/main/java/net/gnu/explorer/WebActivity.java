package net.gnu.explorer;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.SearchView;

import java.io.File;
import net.gnu.explorer.R;
import android.support.v4.app.*;
import android.content.*;
import net.gnu.common.logger.*;
import net.gnu.androidutil.*;
import java.util.*;
import android.net.*;
import java.net.*;

/**
 * Created by selim_tekinarslan on 10.10.2014.
 */
public class WebActivity extends FragmentActivity {
    
    private WebFragment fragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdf_main);
        openWebWithFragment();
    }

    

    public void openWebWithFragment() {
        Intent intent = getIntent();

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
				String toString = data.toString();
				fragment = new WebFragment();
				Bundle args = new Bundle();
				args.putString("url", toString);
				fragment.setArguments(args);
				FragmentManager fragmentManager = getSupportFragmentManager();
				fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
			}
		}

    }
}

