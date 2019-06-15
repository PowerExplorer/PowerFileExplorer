package net.gnu.explorer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.veinhorn.scrollgalleryview.*;
import android.net.Uri;
import java.io.File;
import android.util.Log;
import net.gnu.explorer.R;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.text.TextUtils;
import android.content.Intent;
import android.graphics.BitmapFactory;
import java.io.InputStream;
import android.content.ContentResolver;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.net.URI;
import android.os.Build;
import net.gnu.util.FileUtil;

import com.bumptech.glide.GenericRequestBuilder;
import com.caverock.androidsvg.SVG;
import android.graphics.drawable.PictureDrawable;
import com.bumptech.glide.samples.svg.SvgDrawableTranscoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.samples.svg.SvgDecoder;
import com.bumptech.glide.samples.svg.SvgSoftwareLayerSetter;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import android.app.ProgressDialog;
import android.widget.Toast;
import java.io.FilenameFilter;
import android.webkit.MimeTypeMap;
import com.amaze.filemanager.ui.icons.MimeTypes;
import net.gnu.util.Util;
import java.io.FileFilter;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import net.gnu.androidutil.AndroidUtils;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.regex.Pattern;
import com.amaze.filemanager.ui.LayoutElement;
import java.io.FileNotFoundException;
import com.ortiz.touch.*;
import com.veinhorn.example.*;

public class PhotoFragment extends Frag {
	private static final String TAG = "PhotoFragment";
	private ArrayList<File> infos;
    private ScrollGalleryView scrollGalleryView;
	private TouchImageView image;
	
	public static final Pattern IMAGE_PATTERN = Pattern.compile("^[^\n]*?\\.(jpg|jpeg|gif|png|avi|mpg|mpeg|mp4|3gpp|3gp|3gpp2|vob|asf|wmv|flv|mkv|asx|qt|mov|webm|bmp|ico|tiff|tif|psd|cur|pcx|svg|dwg|pct|pic|jpe|mpe|3g2|m4v|wm|wmx|mpa)$", Pattern.CASE_INSENSITIVE);

	private long lastModified = 0;
	private File lastParentFolder;

    public PhotoFragment() {
		super();
		type = Frag.TYPE.PHOTO;
		title = "Photo";
	}

	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
							 final Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView " + currentPathTitle + ", " + savedInstanceState);
		return inflater.inflate(R.layout.imageview, container, false);
    }

	@Override
    public void onViewCreated(final View v, final Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
		Bundle args = getArguments();
		Log.d(TAG, "onViewCreated " + currentPathTitle + ", " + "args=" + args + ", " + savedInstanceState);

		scrollGalleryView = (ScrollGalleryView) v.findViewById(R.id.scroll_gallery_view);
		image = (TouchImageView) v.findViewById(R.id.image);
		scrollGalleryView.setThumbnailSize(AndroidUtils.dpToPx(54, fragActivity));
		
		updateColor(v);
		Log.d(TAG, "currentPathTitle " + currentPathTitle);
        final Intent intent = fragActivity.getIntent();
		if (intent != null) {
			final Uri uri = intent.getData();
			Log.d(TAG, "data " + uri);
			if (uri != null) {
				final String scheme = uri.getScheme();
				if (ContentResolver.SCHEME_FILE.equals(scheme)) {
					File f = new File(Uri.decode(uri.getPath()));
					if (f.exists()) {
						load(f.getAbsolutePath());
					} else {
						Toast.makeText(fragActivity, f.getAbsolutePath() + " is not existed", Toast.LENGTH_LONG).show();
					}
				} else {
					load(uri);
				}
			} else {
				final ArrayList<Uri> arrList = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
				if (arrList != null) {
					final int size = arrList.size();
					if (size >= 1) {
						final String[] arr = new String[arrList.size()];
//						final Uri uri2 = arrList.get(0);
//						if (ContentResolver.SCHEME_FILE.equals(uri2.getScheme())) {
							int i = 0;
							for (Uri u : arrList) {
								arr[i++] = Uri.decode(u.getPath());
							}
							open(0, arr);
//						} else {
//							scrollGalleryView.setUriMedia(arrList);
//						}
					}
				} else {
					final Bundle extras = intent.getExtras();
					if (extras != null) {
						final Object get = extras.get(Intent.EXTRA_STREAM);
						if (get instanceof Uri) {
							final Uri uri2 = (Uri)get;
							if (ContentResolver.SCHEME_FILE.equals(uri2.getScheme())) {
								final File f = new File(Uri.decode(uri2.getPath()));
								if (f.exists()) {
									load(f.getAbsolutePath());
								} else {
									Toast.makeText(fragActivity, f.getAbsolutePath() + " is not existed", Toast.LENGTH_LONG).show();
								}
							} else {
								load(uri2);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void open(final int curPos, final List<LayoutElement> paths) {
		Log.d(TAG, "open list curPos " + curPos + ", paths.size() " + paths.size());
		image.setVisibility(View.GONE);
		scrollGalleryView.setVisibility(View.VISIBLE);
		if (paths != null) {
			File f;
			infos = new ArrayList<>(paths.size());
			int found = 0;
			int counter = 0;
			for (LayoutElement st : paths) {
				f = new File(st.path);
				if (f.isFile()) {//IMAGE_PATTERN.matcher(st).matches()
					if (IMAGE_PATTERN.matcher(st.name).matches()) { //}extension.length() > 0 && imageVideoSet.contains(extension)) {
						infos.add(f);
						if (counter < curPos) {
							found++;
						}
					}
				}
				counter++;
			}

			final int foundFinal = found;
			//showWait();
			infos.trimToSize();
			scrollGalleryView
				.setFileMedia(infos);
			scrollGalleryView.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (infos.size() > 1) {
							scrollGalleryView.setCurrentItem(foundFinal + 1, true);
						} else {
							scrollGalleryView.setCurrentItem(0, true);
						}
						//hideWait();
					}
				}, 10);
		}
	}

	public void open(final int curPos, final String... paths) {
		Log.d(TAG, "open String..." + curPos);
		image.setVisibility(View.GONE);
		scrollGalleryView.setVisibility(View.VISIBLE);
		if (paths != null) {
			File f;
			infos = new ArrayList<>(paths.length);
			for (String st : paths) {
				f = new File(st);
				if (f.isFile()) {
					if (IMAGE_PATTERN.matcher(f.getName()).matches()) { 
						infos.add(f);
					}
				}
			}
			//showWait();
			infos.trimToSize();
			scrollGalleryView
				.setFileMedia(infos);
			scrollGalleryView.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (infos.size() > 1) {
							scrollGalleryView.setCurrentItem(curPos + 1, true);
						} else {
							scrollGalleryView.setCurrentItem(0, true);
						}
						//hideWait();
					}
				}, 10);
		}
	}

	public void load(final Uri uri) {
		Log.d(TAG, "load uri " + uri);
		if (uri != null) {
			final String scheme = uri.getScheme();
			InputStream is = null;
			try {
				if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
//					ContentResolver cr = fragActivity.getContentResolver();
//					is = cr.openInputStream(uri);
//					Bitmap bmp = BitmapFactory.decodeStream(is);
//					image.setImageBitmap(bmp);
					scrollGalleryView.setVisibility(View.GONE);
					image.setVisibility(View.VISIBLE);
					GlideImageLoader.loadMedia(uri, getContext(), image, DiskCacheStrategy.NONE);
					
				} else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
					load(Uri.decode(uri.getPath()));
				}
			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
				FileUtil.close(is);
			}
		}
    }

	@Override
	public void load(final String path) {
		Log.d(TAG, "path " + path);
		image.setVisibility(View.GONE);
		if (path != null) {
			scrollGalleryView.setVisibility(View.VISIBLE);
			this.currentPathTitle = path;
			final File file = new File(path);
			File parentFile;
			long lastModified2;
			if (file.isFile()) {
				parentFile = file.getParentFile();
			} else {
				parentFile = file;
			}
			lastModified2 = parentFile.lastModified();
			Log.d(TAG, "parentFile " + parentFile.getAbsolutePath());
			if (!parentFile.equals(lastParentFolder) || lastModified != lastModified2) {
				lastParentFolder = parentFile;
				lastModified = lastModified2;
				final File[] fs = parentFile.listFiles(new FileFilter() {
						@Override
						public boolean accept(final File p1) {
							if (p1.isFile() && IMAGE_PATTERN.matcher(p1.getName()).matches()) {
								return true;
							}
							return false;
						}
					});
				//Log.d(TAG, "fs " + fs);
				infos = new ArrayList<File>(Arrays.asList(fs));
				final int cur;
				if (file.isFile()) {
					cur = infos.indexOf(file);
				} else {
					cur = 0;
				}
				Log.d(TAG, "cur1 " + cur);
				if (fs.length > 0) {
					//showWait();
					scrollGalleryView
						.setFileMedia(infos);
					scrollGalleryView.postDelayed(new Runnable() {
							@Override
							public void run() {
								if (infos.size() > 1) {
									scrollGalleryView.setCurrentItem(cur + 1, true);
								} else {
									scrollGalleryView.setCurrentItem(0, true);
								}
								//hideWait();
							}
						}, 10);
				} else {
					Toast.makeText(fragActivity, "No image or video file", Toast.LENGTH_LONG).show();
				}
			} else {
				int i = 0;
				int cur = 0;
				if (file.isFile()) {
					final String fName = file.getName();
					for (File url : infos) {
						if (url.getName().equals(fName)) {
							cur = i;
							break;
						}
						i++;
					}
					if (infos.size() > 1) {
						cur = cur + 1;
					}
				}
				Log.d(TAG, "cur2 " + cur);
				scrollGalleryView.setCurrentItem(cur, true);
			}
		}
    }

	public void updateColor(final View rootView) {
		getView().setBackgroundColor(0xff000000);
	}
	
}
