package jp.sblo.pandora.jota;


import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.WebView;
import net.gnu.explorer.R;

public class AboutActivity extends Activity
{

	protected String DEFAULT_PAGE = "file:///android_asset/about.html";
	public final static String EXTRA_URL = "URL";
	public final static String EXTRA_TITLE = "TITLE";
    protected JsCallbackObj mjsobj;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		String url = DEFAULT_PAGE;

		Intent it = getIntent();
		if ( it != null ){
			Bundle extras = it.getExtras();
			if ( extras !=null ){
				String iturl = extras.getString(EXTRA_URL);
				if ( iturl !=null ){
					url = iturl;
				}
				String ittitle = extras.getString(EXTRA_TITLE);
				if ( ittitle !=null ){
					setTitle( ittitle );
				}
			}else{
			    url = getString(R.string.about_url);
                setTitle( R.string.about_title );
			}
		}

		WebView webview = (WebView)findViewById(R.id.WebView01);
		webview.loadUrl( url );

		mjsobj = new JsCallbackObj();
		webview.addJavascriptInterface(mjsobj, "jscallback");

		webview.getSettings().setJavaScriptEnabled(true);
		webview.setFocusable(true);
		webview.setFocusableInTouchMode(true);
	}

	public class JsCallbackObj
	{
        public Runnable mProcBilling = null;
        public Runnable mProcConfirm1 = null;
        public Runnable mProcConfirm2 = null;
        public String mOrderid="";

		public JsCallbackObj()
		{
		}

        public String getAboutStrings(String key)
        {
            if (key.equals("version")) {

                String versionName = "-.-";
                int versionCode = 0;
                PackageManager pm = getPackageManager();
                try {
                    String pkgname = AboutActivity.this.getPackageName();
                    PackageInfo info = pm.getPackageInfo(pkgname, 0);
                    versionName = info.versionName;
                    versionCode = info.versionCode;
                } catch (NameNotFoundException e) {
                }
                return "Ver. " + String.format("%s (%d)", versionName, versionCode);

            } else if (key.equals("stars")) {
//                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AboutActivity.this);
//                int count = prefs.getInt(DonateActivity.DONATION_COUNTER,0);
//                String stars = "";
//                if ( count >0 ){
//                    String star = getString( R.string.label_star );
//                    for( int i=0;i<count;i++ ){
//                        stars += star;
//                    }
//                }else{
//                    String star = getString( R.string.label_no_star );
//                    stars = star;
//                }
//                stars += "<br />";
                return "<br />";
            } else if (key.equals("donators")) {
                String list="";
                AssetManager as = getResources().getAssets();
                try{
                    String line;
                    BufferedReader br = new BufferedReader( new InputStreamReader(as.open("donator.txt"),"utf-8") );
                    while( (line = br.readLine()) != null ){
                        list += line + "<br/>";
                    }
                    br.close();
                }
                catch(Exception e)
                {}
                return list;
            } else {
                return "";
            }
        }

        public void startBilling()
        {
            if ( mProcBilling != null ){
                mProcBilling.run();
            }
        }

        public void confirmBilling1(String orderid)
        {
            if ( mProcConfirm1 != null ){
                mOrderid = orderid;
                mProcConfirm1.run();
            }
        }

        public void confirmBilling2()
        {
            if ( mProcConfirm2 != null ){
                mProcConfirm2.run();
            }
        }

        public void throwIntentByUrl( String url , int requestcode )
		{
			if ( url!=null && url.length()>0 ){
				Intent it = new Intent( Intent.ACTION_VIEW , Uri.parse(url) );
				startActivityForResult(it, requestcode);
			}
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{

		super.onActivityResult(requestCode, resultCode, data);
		if ( resultCode == RESULT_OK && requestCode==1000 ){		// DL rerquest
			setResult( RESULT_OK , data );
			finish();
		}
	}

}
