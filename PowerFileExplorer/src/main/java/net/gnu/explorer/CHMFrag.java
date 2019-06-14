package net.gnu.explorer;

import android.webkit.WebView;
import android.app.ProgressDialog;
import android.widget.ProgressBar;
import java.util.ArrayList;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import chm.cblink.nb.chmreader.lib.Utils;
import android.webkit.WebResourceResponse;
import android.os.AsyncTask;
import java.io.File;
import android.os.Environment;
import java.io.IOException;
import android.app.Dialog;
import android.app.Activity;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.EditText;
import java.io.InputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import android.webkit.WebViewClient;
import android.graphics.Bitmap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.webkit.WebChromeClient;
import android.support.annotation.RequiresApi;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.net.Uri;
import java.util.List;
import com.amaze.filemanager.ui.LayoutElement;
import java.lang.reflect.Method;
import android.app.SearchManager;
import android.content.Context;
import android.widget.SearchView;
import net.gnu.common.*;

public class CHMFrag extends Frag {
    private WebView webview;
    public static String extractPath, md5File;
    private ProgressDialog progress;
    private ProgressBar progressLoadWeb;
    private ArrayList<String> listSite;
    private ArrayList<String> listBookmark;
    private int tempIndex;
	private View root;
	private static final String TAG = "CHMFrag";

    public CHMFrag() {
		super();
		type = Frag.TYPE.CHM;
		title = "CHM";
	}

	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
							 final Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView " + currentPathTitle + ", " + savedInstanceState);
		root = inflater.inflate(R.layout.activity_chm, container, false);
		return root;
    }

	@Override
    public void onViewCreated(final View v, final Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
		Bundle args = getArguments();
		Log.d(TAG, "onViewCreated " + currentPathTitle + ", " + "args=" + args + ", " + savedInstanceState);
	
		updateColor(v);
		final Intent intent = fragActivity.getIntent();
		
		if (args != null) {
			currentPathTitle = args.getString("path");
        }
		if (savedInstanceState != null) {
			currentPathTitle = savedInstanceState.getString("path");
		}
		
		if (intent != null) {
			final Uri data = intent.getData();
			Log.d(TAG, "data " + data);
			if (data != null) {
				File f = new File(Uri.decode(data.getPath()));
				if (f.exists()) {
					load(f.getAbsolutePath());
				} else {
					Toast.makeText(fragActivity, f.getAbsolutePath() + " is not existed", Toast.LENGTH_LONG).show();
				}
			} 
		} else if (currentPathTitle != null && currentPathTitle.length() > 0) {
			File f = new File(currentPathTitle);
			if (f.exists()) {
				load(f.getAbsolutePath());
			} else {
				Toast.makeText(fragActivity, f.getAbsolutePath() + " is not existed", Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void open(final int curPos, final List<LayoutElement> paths) {
		Log.d(TAG, "open list " + curPos);// + ", " + paths);
		load(paths.get(curPos).path);
    }

	@Override
	public void load(final String path) {
		Log.d(TAG, "path " + path);
		if (path != null) {
			this.currentPathTitle = path;
			Utils.chm = null;
			listSite = new ArrayList<>();
			initView();
			initFile();
		}
    }

	public void updateColor(final View rootView) {
		getView().setBackgroundColor(Constants.BASE_BACKGROUND);
	}

	public boolean back() {
		if (webview.canGoBack()) {
			webview.goBack();
		}
		return true;
	}

    //@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webview.canGoBack()) {
                        webview.goBack();
                    }
                    return true;
            }
        }
        //return super.onKeyDown(keyCode, event);
		return false;
    }

    @Override
	public void onCreateOptionsMenu(android.view.Menu menu, android.view.MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_chm, menu);
	
        SearchManager searchManager =
			(SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
			(SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(
			searchManager.getSearchableInfo(activity.getComponentName()));
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
				@Override
				public boolean onClose() {
					webview.clearMatches();
					return false;
				}
			});
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
				@Override
				public boolean onQueryTextSubmit(String query) {
					return false;
				}

				@Override
				public boolean onQueryTextChange(String newText) {
					webview.findAllAsync(newText);
					try {
						for (Method m : WebView.class.getDeclaredMethods()) {
							if (m.getName().equals("setFindIsUp")) {
								m.setAccessible(true);
								m.invoke((webview), true);
								break;
							}
						}
					} catch (Exception ignored) {
					}
					return false;
				}
			});
        
    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        handleIntent(intent);
//    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            webview.findAllAsync(query);
            try {
                for (Method m : WebView.class.getDeclaredMethods()) {
                    if (m.getName().equals("setFindIsUp")) {
                        m.setAccessible(true);
                        m.invoke((webview), true);
                        break;
                    }
                }
            } catch (Exception ignored) {
            }
        } else {
			currentPathTitle = intent.getDataString().substring("file://".length());//.getStringExtra("fileName");
			Log.d(TAG, "chmFilePath " + currentPathTitle);
			Utils.chm = null;
			listSite = new ArrayList<>();
			initView();
			initFile();
		}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String temp;
        switch (item.getItemId()) {
            case R.id.menu_home:
                webview.loadUrl("file://" + extractPath + "/" + listSite.get(1));
                break;
            case R.id.menu_sitemap:
                webview.loadUrl("file://" + extractPath + "/" + listSite.get(0));
                break;
            case R.id.menu_back_page:
                temp = webview.getUrl().replaceAll("%20", " ").substring(("file://" + extractPath).length() + 1);
                if (temp.contains("#")) {
                    temp = temp.substring(0, temp.indexOf("#"));
                }
                if (temp.contains("?")) {
                    temp = temp.substring(0, temp.indexOf("?"));
                }
                tempIndex = listSite.indexOf(temp);
                if (tempIndex == 1) {
                    Toast.makeText(activity, "First site", Toast.LENGTH_SHORT).show();
                } else {
                    webview.loadUrl("file://" + extractPath + "/" + listSite.get(tempIndex - 1));
                }
                break;
            case R.id.menu_next_page:
                temp = webview.getUrl().replaceAll("%20", " ").substring(("file://" + extractPath).length() + 1);
                if (temp.contains("#")) {
                    temp = temp.substring(0, temp.indexOf("#"));
                }
                if (temp.contains("?")) {
                    temp = temp.substring(0, temp.indexOf("?"));
                }
                tempIndex = listSite.indexOf(temp);
                if (tempIndex == listSite.size() - 1) {
                    Toast.makeText(activity, "End site", Toast.LENGTH_SHORT).show();
                } else {
                    webview.loadUrl("file://" + extractPath + "/" + listSite.get(tempIndex + 1));
                }
                break;
            case R.id.menu_zoom_in:
                webview.setInitialScale(300);
                break;
            case R.id.menu_zoom_out:
                webview.setInitialScale(100);
                break;
            case R.id.menu_bookmark:
                CustomDialogBookmark bookmarkDialog = new CustomDialogBookmark(activity);
                bookmarkDialog.show();
                break;
            case android.R.id.home:
                Utils.chm = null;
                //this.finish();
                break;
            case R.id.menu_search_all:
                CustomDialogSearchAll searchALlDialog = new CustomDialogSearchAll(activity);
                searchALlDialog.show();
                break;
        }
        return true;
    }

    private void initView() {
        webview = (WebView) root.findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebChromeClient(new WebChromeClient() {
				@Override
				public void onProgressChanged(WebView view, int newProgress) {
					super.onProgressChanged(view, newProgress);
					progressLoadWeb.setProgress(newProgress);
				}
			});
        webview.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					if (!url.startsWith("http") && !url.endsWith(md5File)) {
						String temp = url.substring("file://".length());
						if (!temp.startsWith(extractPath)) {
							url = "file://" + extractPath + temp;
						}
					}

					super.onPageStarted(view, url, favicon);
					progressLoadWeb.setProgress(50);
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					progressLoadWeb.setProgress(100);
				}

				@Override
				public void onLoadResource(WebView view, String url) {
					if (!url.startsWith("http") && !url.endsWith(md5File)) {
						String temp = url.substring("file://".length());
						if (!temp.startsWith(extractPath)) {
							url = "file://" + extractPath + temp;
						}
					}
					super.onLoadResource(view, url);
				}


				@Override
				public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
					if (!url.startsWith("http") && !url.endsWith(md5File)) {
						String temp = url.substring("file://".length());
						String insideFileName;
						if (!temp.startsWith(extractPath)) {
							url = "file://" + extractPath + temp;
							insideFileName = temp;
						} else {
							insideFileName = temp.substring(extractPath.length());
						}
						if (insideFileName.contains("#")) {
							insideFileName = insideFileName.substring(0, insideFileName.indexOf("#"));
						}
						if (insideFileName.contains("?")) {
							insideFileName = insideFileName.substring(0, insideFileName.indexOf("?"));
						}
						if (insideFileName.contains("%20")) {
							insideFileName = insideFileName.replaceAll("%20", " ");
						}
						if (url.endsWith(".gif") || url.endsWith(".jpg") || url.endsWith(".png")) {
							try {
								return new WebResourceResponse("image/*", "", Utils.chm.getResourceAsStream(insideFileName));
							} catch (IOException e) {
								e.printStackTrace();
								return super.shouldInterceptRequest(view, url);
							}
						} else if (url.endsWith(".css") || url.endsWith(".js")) {
							try {
								return new WebResourceResponse("", "", Utils.chm.getResourceAsStream(insideFileName));
							} catch (IOException e) {
								e.printStackTrace();
								return super.shouldInterceptRequest(view, url);
							}
						} else {
							Utils.extractSpecificFile(currentPathTitle, extractPath + insideFileName, insideFileName);
						}
					}
					Log.e("2, webviewrequest", url);
					return super.shouldInterceptRequest(view, url);
				}


				@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
				@Override
				public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
					return shouldInterceptRequest(view, request.getUrl().toString());
				}

				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if (!url.startsWith("http") && !url.endsWith(md5File)) {
						String temp = url.substring("file://".length());
						if (!temp.startsWith(extractPath)) {
							url = "file://" + extractPath + temp;
							view.loadUrl(url);
							return true;
						}
					}
					return false;
				}

				@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
					return shouldOverrideUrlLoading(view, request.getUrl().toString());
					//return super.shouldOverrideUrlLoading(view, request);
				}
			});
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setDisplayZoomControls(false);
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setLoadsImagesAutomatically(true);
//        ((ClickableWebView) webview).setOnWebViewClickListener(new OnWebViewClicked() {
//            @Override
//            public void onClick(String url) {
//                Toast.makeText(MainActivity.this, url, Toast.LENGTH_SHORT).show();
//            }
//        });
        progressLoadWeb = (ProgressBar) root.findViewById(R.id.progressBar);
        progressLoadWeb.setMax(100);

    }

    private void initFile() {
        new AsyncTask<Void, Void, Void>() {
            int historyIndex = 1;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progress = new ProgressDialog(fragActivity);
                progress.setTitle("Waiting");
                progress.setMessage("Extracting...");
                progress.setCancelable(false);
                progress.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                md5File = Utils.checkSum(currentPathTitle);
                extractPath = fragActivity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/CHMReader/" + md5File;
                if (!(new File(extractPath).exists())) {
                    if (Utils.extract(currentPathTitle, extractPath)) {
                        try {
                            listSite = Utils.domparse(currentPathTitle, extractPath, md5File);
                            listBookmark = Utils.getBookmark(extractPath, md5File);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        (new File(extractPath)).delete();
                    }
                } else {
                    listSite = Utils.getListSite(extractPath, md5File);
                    listBookmark = Utils.getBookmark(extractPath, md5File);
                    historyIndex = Utils.getHistory(extractPath, md5File);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                webview.loadUrl("file://" + extractPath + "/" + listSite.get(historyIndex));
                if (progress != null) {
                    progress.dismiss();
                    progress = null;
                }
            }
        }.execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            Utils.saveBookmark(extractPath, md5File, listBookmark);
            String url = webview.getUrl().replaceAll("%20", " ").substring(("file://" + extractPath).length() + 1);
            int index = listSite.indexOf(url);
            if (index != -1) {
                Utils.saveHistory(extractPath, md5File, index);
            }
        } catch (Exception ignored) {
        }
    }

    class CustomDialogBookmark extends Dialog implements
	android.view.View.OnClickListener {

        Activity mainActivity;
        Button add;
        ListView listView;
        ArrayAdapter adapter;

        public CustomDialogBookmark(Activity activity) {
            super(activity);
            // TODO Auto-generated constructor stub
            this.mainActivity = activity;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setTitle("List bookmarks");
            setContentView(R.layout.dialog_bookmark);
            setCanceledOnTouchOutside(true);
            setCancelable(true);
            add = (Button) findViewById(R.id.btn_addbookmark);
            add.setOnClickListener(this);
            listView = (ListView) findViewById(R.id.listView);
            adapter = new ArrayAdapter<String>(mainActivity, android.R.layout.simple_list_item_1, listBookmark);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
						webview.loadUrl("file://" + extractPath + "/" + listBookmark.get(i));
						dismiss();
					}
				});
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_addbookmark:
                    String url = webview.getUrl().replaceAll("%20", " ").substring(("file://" + extractPath).length() + 1);
                    if (listBookmark.indexOf(url) == -1) {
                        listBookmark.add(url);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(mainActivity, "Bookmark already exist", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

    class CustomDialogSearchAll extends Dialog implements
	android.view.View.OnClickListener {

        Activity mainActivity;
        Button search;
        ListView listView;
        EditText editText;
        ArrayAdapter adapter;
        ProgressBar searchProgress;
        ArrayList<String> searchResult;

        public CustomDialogSearchAll(Activity activity) {
            super(activity);
            // TODO Auto-generated constructor stub
            this.mainActivity = activity;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setTitle("Search all");
            setContentView(R.layout.dialog_search_all);
            setCanceledOnTouchOutside(true);
            setCancelable(true);
            search = (Button) findViewById(R.id.btn_search);
            search.setOnClickListener(this);
            listView = (ListView) findViewById(R.id.list_result);
            editText = (EditText) findViewById(R.id.edit_search);
            searchProgress = (ProgressBar) findViewById(R.id.progressBar);
            searchProgress.setMax(listSite.size() - 1);
            searchResult = new ArrayList<>();
            adapter = new ArrayAdapter<String>(mainActivity, android.R.layout.simple_list_item_1, searchResult);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
						webview.loadUrl("file://" + extractPath + "/" + searchResult.get(i));
						dismiss();
					}
				});
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_search:
                    if (editText.getText().toString().length() > 0)
                        new AsyncTask<Void, Integer, Void>() {
                            String textSearch;

                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                searchProgress.setProgress(0);
                                textSearch = editText.getText().toString();
                            }

                            @Override
                            protected Void doInBackground(Void... voids) {
                                for (int i = 1; i < listSite.size(); i++) {
                                    if (searchDoc(listSite.get(i), textSearch)) {
                                        publishProgress(i, 1);
                                    } else {
                                        publishProgress(i, 0);
                                    }
                                }
                                return null;
                            }

                            @Override
                            protected void onProgressUpdate(Integer... values) {
                                super.onProgressUpdate(values);
                                searchProgress.setProgress(values[0]);
                                if (values[1] == 1) {
                                    searchResult.add(listSite.get(values[0]));
                                    adapter.notifyDataSetChanged();
                                    CustomDialogSearchAll.this.setTitle(searchResult.size() + " Results");
                                }
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                CustomDialogSearchAll.this.setTitle(searchResult.size() + " Results");
                            }
                        }.execute();
            }
        }

        boolean searchDoc(String siteName, String textSearch) {
            StringBuilder reval = new StringBuilder();
            try {
                InputStream in = Utils.chm.getResourceAsStream("/" + siteName);
                byte[] buf = new byte[1024];
                int c;
                while ((c = in.read(buf)) >= 0) {
                    reval.append(new String(buf, 0, c));
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Document doc = Jsoup.parse(reval.toString());
            if (doc.text().indexOf(textSearch) > 0) return true;
            else return false;
        }
    }
}
