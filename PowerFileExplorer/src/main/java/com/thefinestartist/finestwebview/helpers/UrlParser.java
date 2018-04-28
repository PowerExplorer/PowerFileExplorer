package com.thefinestartist.finestwebview.helpers;

import java.net.MalformedURLException;
import java.net.URL;
import android.util.Log;

/**
 * Created by Leonardo on 11/23/15.
 */
public class UrlParser {

	private UrlParser() {
	}

	public static String getHost(String url) {
		Log.d("UrlParser", url + ".");
		try {
			return new URL(url).getHost();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return url;
	}
}
