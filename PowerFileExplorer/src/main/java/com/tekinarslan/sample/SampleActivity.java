package com.tekinarslan.sample;

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
import net.gnu.explorer.ExplorerActivity;

/**
 * Created by selim_tekinarslan on 10.10.2014.
 */
public class SampleActivity extends FragmentActivity {
    //private static final String TAG = "SampleActivity";
    
    //private static final String SEARCH_TEXT = "text";
    private PdfFragment fragment;
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdf_main);
        context = SampleActivity.this;
        openPdfWithFragment();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query)//burada klavyeden ara ya basiyor user
            {
                fragment.search(1, query);
                return true;
            }
        };
        searchView.setOnQueryTextListener(textChangeListener);
        return super.onCreateOptionsMenu(menu);
    }

    public void openPdfWithFragment() {
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
				if (toString.startsWith("file://")) {
					String f = URLDecoder.decode(toString).substring("file://".length());
					fragment = new PdfFragment();
					Bundle args = new Bundle();

					args.putString(ExplorerActivity.EXTRA_ABSOLUTE_PATH, f);
					fragment.setArguments(args);
					FragmentManager fragmentManager = getSupportFragmentManager();
					fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

				}
			}
		}
		
    }

}
