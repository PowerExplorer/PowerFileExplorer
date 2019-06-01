package net.gnu.explorer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import android.widget.ImageButton;
import android.support.v7.widget.LinearLayoutManager;
import com.ToxicBakery.viewpager.transforms.ABaseTransformer;
import android.support.v7.widget.RecyclerView;
import android.support.v4.view.ViewPager;
import android.support.v4.app.FragmentManager;
import android.widget.LinearLayout;
import com.ToxicBakery.viewpager.transforms.DefaultTransformer;
import com.ToxicBakery.viewpager.transforms.AccordionTransformer;
import com.ToxicBakery.viewpager.transforms.DepthPageTransformer;
import com.ToxicBakery.viewpager.transforms.DrawFromBackTransformer;
import com.ToxicBakery.viewpager.transforms.BackgroundToForegroundTransformer;
import com.ToxicBakery.viewpager.transforms.FadeTransformer;
import com.ToxicBakery.viewpager.transforms.ForegroundToBackgroundTransformer;
import com.ToxicBakery.viewpager.transforms.RotateDownTransformer;
import com.ToxicBakery.viewpager.transforms.RotateUpTransformer;
import com.ToxicBakery.viewpager.transforms.ScaleInOutTransformer;
import com.ToxicBakery.viewpager.transforms.StackTransformer;
import com.ToxicBakery.viewpager.transforms.TabletTransformer;
import com.ToxicBakery.viewpager.transforms.ZoomInTransformer;
import com.ToxicBakery.viewpager.transforms.ZoomOutSlideTransformer;
import com.ToxicBakery.viewpager.transforms.ZoomOutTranformer;
import android.view.GestureDetector.OnDoubleTapListener;
import net.gnu.androidutil.BitmapUtil;
import com.amaze.filemanager.utils.files.Futils;
import com.amaze.filemanager.activities.ThemedActivity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import java.lang.ref.WeakReference;
import android.graphics.Matrix;
import java.util.Collections;
import net.gnu.util.UriSorter;
import android.view.animation.AnimationUtils;
import com.amaze.filemanager.filesystem.BaseFile;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amaze.filemanager.utils.MainActivityHelper;
import com.amaze.filemanager.utils.OpenMode;
import android.app.WallpaperManager;
import com.amaze.filemanager.ui.dialogs.GeneralDialogCreation;
import java.io.IOException;
import com.amaze.filemanager.utils.color.ColorUsage;
import android.widget.HorizontalScrollView;
import android.view.Gravity;

public class PhotoFragment extends Frag implements OnDoubleTapListener, OnClickListener {
	private static final String TAG = "PhotoFragment";
	
	private ArrayList<File> infos;
	//private ImageView placeholderImage;
	
	public static final Pattern IMAGE_PATTERN = Pattern.compile("^[^\n]*?\\.(jpg|jpeg|gif|png|avi|mpg|mpeg|mp4|3gpp|3gp|3gpp2|vob|asf|wmv|flv|mkv|asx|qt|mov|webm|bmp|ico|tiff|tif|psd|cur|pcx|svg|dwg|pct|pic|jpe|mpe|3g2|m4v|wm|wmx|mpa)$", Pattern.CASE_INSENSITIVE);

	private long lastModified = 0;
	private File lastParentFolder;

	private FragmentManager fragmentManager;
    private ScreenSlidePagerAdapter viewPagerAdapter;
    private List<Uri> mListOfMedia = new LinkedList<>();
	private int sizeMediaFiles;

    private int thumbnailSize = 54; // width and height in pixels
    private boolean thumbnailsHiddenEnabled = false;
	private int pageSelected;

    // Views
    private RecyclerView thumbnailsRecyclerView;
    private ViewPager viewPager;
	private LinearLayout infoLayout;
	private LinearLayout detailInfoLayout;
	private HorizontalScrollView toolbarSV;
	private HorizontalScrollView thumbRecyclerScrollView;
	private View leftRecycler;
	private View rightRecycler;
	
	private TextView fileNameTV;
	private TextView fileOrderTV;
	private TextView fileDimensionTV;
	private TextView fileSizeTV;
	private TextView fileDateTV;
	private String orderType = "";
	private String asc = "";

	static boolean SLIDESHOW = false;
	//static int DELAY = 1000;
	static ABaseTransformer[] transforms = new ABaseTransformer[]{
		new DefaultTransformer(),
		new AccordionTransformer(),
		new BackgroundToForegroundTransformer(),
		//new CubeInTransformer(),
		//new CubeOutTransformer(),
		new DepthPageTransformer(),
		new DrawFromBackTransformer(),
		//new FlipHorizontalTransformer(),
		//new FlipVerticalTransformer(),
		new FadeTransformer(),
		new ForegroundToBackgroundTransformer(),
		new RotateDownTransformer(),
		new RotateUpTransformer(),
		new ScaleInOutTransformer(),
		new StackTransformer(),
		new TabletTransformer(),
		new ZoomInTransformer(),
		new ZoomOutSlideTransformer(),
		new ZoomOutTranformer(),
	};
	private ImageButton slideshowButton;
	private ImageButton shareButton;
	private ImageButton clockwiseButton;
	private ImageButton counterClockwiseButton;
	private ImageButton wallpaperButton;
	//private ImageButton chromecastButton;
	private ImageButton addShortcutButton;
	private ImageButton renameButton;
	private ImageButton copyButton;
	private ImageButton cutButton;
	private ImageButton removeButton;
	private ImageButton editButton;
	private ImageButton scanButton;

	protected ThumbnailAdapter recyclerAdapter;
    protected LinearLayoutManager mLayoutManager;
    protected String[] mDataset;
	boolean scrolledByViewPager = true;
	
    public PhotoFragment() {
		super();
		type = Frag.TYPE.PHOTO;
		title = "Photo";
	}

	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
							 final Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView " + currentPathTitle + ", " + savedInstanceState);
		return inflater.inflate(R.layout.photo_frag, container, false);
    }

	@Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
		Bundle args = getArguments();
		Log.d(TAG, "onViewCreated " + currentPathTitle + ", " + "args=" + args + ", " + savedInstanceState);

        viewPager = (ViewPager) view.findViewById(R.id.photoViewPager);
        thumbnailsRecyclerView = (RecyclerView) view.findViewById(R.id.thumbnails_container);
		infoLayout = (LinearLayout) view.findViewById(R.id.info);
		detailInfoLayout = (LinearLayout) view.findViewById(R.id.detailInfo);
		toolbarSV = (HorizontalScrollView) view.findViewById(R.id.toolbar);
		thumbRecyclerScrollView = (HorizontalScrollView) view.findViewById(R.id.thumbRecyclerScrollView);
		leftRecycler = view.findViewById(R.id.leftRecycler);
		rightRecycler = view.findViewById(R.id.rightRecycler);
		
		fileNameTV = (TextView) view.findViewById(R.id.fileName);
		fileOrderTV = (TextView) view.findViewById(R.id.fileOrder);
		fileDimensionTV = (TextView) view.findViewById(R.id.fileDimension);
		fileSizeTV = (TextView) view.findViewById(R.id.fileSize);
		fileDateTV = (TextView) view.findViewById(R.id.fileDate);

		slideshowButton = (ImageButton) view.findViewById(R.id.slideshowButton);
		shareButton = (ImageButton) view.findViewById(R.id.shareButton);
		clockwiseButton = (ImageButton) view.findViewById(R.id.clockwiseButton);
		counterClockwiseButton = (ImageButton) view.findViewById(R.id.counterClockwiseButton);
		wallpaperButton = (ImageButton) view.findViewById(R.id.wallpaperButton);
		//chromecastButton = (ImageButton) view.findViewById(R.id.chromecastButton);
		addShortcutButton = (ImageButton) view.findViewById(R.id.addshortcut);
		renameButton = (ImageButton) view.findViewById(R.id.renameButton);
		copyButton = (ImageButton) view.findViewById(R.id.copyButton);
		cutButton = (ImageButton) view.findViewById(R.id.cutButton);
		removeButton = (ImageButton) view.findViewById(R.id.removeButton);
		editButton = (ImageButton) view.findViewById(R.id.editButton);
		scanButton = (ImageButton) view.findViewById(R.id.scanButton);

		fileNameTV.setOnClickListener(this);
		fileSizeTV.setOnClickListener(this);
		fileDateTV.setOnClickListener(this);
		slideshowButton.setOnClickListener(this);
		shareButton.setOnClickListener(this);
		clockwiseButton.setOnClickListener(this);
		counterClockwiseButton.setOnClickListener(this);
		wallpaperButton.setOnClickListener(this);
		//chromecastButton.setOnClickListener(this);
		addShortcutButton.setOnClickListener(this);
		renameButton.setOnClickListener(this);
		copyButton.setOnClickListener(this);
		cutButton.setOnClickListener(this);
		removeButton.setOnClickListener(this);
		editButton.setOnClickListener(this);
		scanButton.setOnClickListener(this);

		viewPager.addOnPageChangeListener(viewPagerChangeListener);
		mLayoutManager = new LinearLayoutManager(fragActivity);
		mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		thumbnailsRecyclerView.setLayoutManager(mLayoutManager);
		thumbnailsRecyclerView.addOnScrollListener(mScrollListener);
        thumbnailsRecyclerView.setHasFixedSize(true);
		thumbnailsRecyclerView.setItemViewCacheSize(20);
		//thumbnailsRecyclerView.setDrawingCacheEnabled(true);
		//thumbnailsRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

		Log.d(TAG, fragActivity + ".");
		if (fragActivity instanceof ExplorerActivity) {
			copyButton.setColorFilter(0xffffffff);
			cutButton.setColorFilter(0xffffffff);
		} else {
			copyButton.setVisibility(View.GONE);
			cutButton.setVisibility(View.GONE);
		}
		//slideshowButton.setColorFilter(color);
		shareButton.setColorFilter(0xffffffff);
		//clockwiseButton.setColorFilter(color);
		//counterClockwiseButton.setColorFilter(color);
		wallpaperButton.setColorFilter(0xffffffff);
		addShortcutButton.setColorFilter(0xffffffff);
		removeButton.setColorFilter(0xffffffff);
		editButton.setColorFilter(0xffffffff);
		scanButton.setColorFilter(0xffffffff);
		renameButton.setColorFilter(0xffffffff);
		
		updateColor(view);
		Log.d(TAG, "currentPathTitle " + currentPathTitle);
        final Intent intent = fragActivity.getIntent();
		Log.d(TAG, "currentPathTitle " + currentPathTitle + ", intent " + intent);
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
//					if (size == 1) {
//						final Uri uri2 = arrList.get(0);
//						if (ContentResolver.SCHEME_FILE.equals(uri2.getScheme())) {
//							final File f = new File(Uri.decode(uri2.getPath()));
//							if (f.exists()) {
//								load(f.getAbsolutePath());
//							} else {
//								Toast.makeText(fragActivity, f.getAbsolutePath() + " is not existed", Toast.LENGTH_LONG).show();
//							}
//						} else {
//							load(uri2);
//						}
//					} else 
					if (size >= 1) {
						final String[] arr = new String[arrList.size()];
						final Uri uri2 = arrList.get(0);
						if (ContentResolver.SCHEME_FILE.equals(uri2.getScheme())) {
							int i = 0;
							for (Uri u : arrList) {
								arr[i++] = Uri.decode(u.getPath());
							}
							open(0, arr);
						} else {
							infoLayout.setVisibility(View.VISIBLE);
							detailInfoLayout.setVisibility(View.GONE);
							toolbarSV.setVisibility(View.INVISIBLE);
							setUriMedia(arrList);
							infoLayout.postDelayed(runShowCurPageSelected, 10);
						}
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
		if (paths != null) {
			File f;
			infos = new ArrayList<>(paths.size());
			int found = 0;
			int counter = 0;
			for (LayoutElement st : paths) {
				f = new File(st.path);
				if (f.isFile()) {
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
			setMedia(infos);
			if (infos.size() > 1) {
				pageSelected = foundFinal + 1;
			} else {
				pageSelected = 0;
			}
			infoLayout.postDelayed(runShowCurPageSelected, 10);
			infos.trimToSize();
		}
	}

	void open(final int curPos, final String... paths) {
		Log.d(TAG, "open String..." + curPos);
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
			setMedia(infos);
			if (infos.size() > 1) {
				pageSelected = curPos + 1;
			} else {
				pageSelected = 0;
			}
			infoLayout.postDelayed(runShowCurPageSelected, 10);
			infos.trimToSize();
		}
	}

	@Override
	public void load(final String path) {
		Log.d(TAG, "path " + path);
		if (path != null) {
			this.currentPathTitle = path;
			final File file = new File(path);
			final File parentFile;
			final long lastModified2;
			if (file.isFile()) {
				parentFile = file.getParentFile();
				lastModified2 = parentFile.lastModified();
			} else {
				parentFile = file;
				lastModified2 = parentFile.lastModified();
			}
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
				Log.d(TAG, "cur " + cur);
				if (fs.length > 0) {
					//showWait();
					setMedia(infos);
					if (infos.size() > 1) {
						pageSelected = cur + 1;
					} else {
						pageSelected = 0;
					}
					infoLayout.postDelayed(runShowCurPageSelected, 10);
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
				}
				Log.d(TAG, "cur " + cur);
				setCurrentItem(cur + 1, true);
			}
		}
    }

	//@Override
	public void load(final Uri uri) {
		Log.d(TAG, "path " + uri);
		if (uri != null) {
			final String scheme = uri.getScheme();
			//InputStream is = null;
			try {
				if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
					final ArrayList<Uri> arrayList = new ArrayList<Uri>(1);
					arrayList.add(uri);
					setUriMedia(arrayList);
//					final ContentResolver cr = fragActivity.getContentResolver();
//					is = cr.openInputStream(uri);
					detailInfoLayout.setVisibility(View.GONE);
					toolbarSV.setVisibility(View.INVISIBLE);
				} else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
					detailInfoLayout.setVisibility(View.VISIBLE);
					toolbarSV.setVisibility(View.VISIBLE);
					load(Uri.decode(uri.getPath()));
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
    }

    private void setMedia(final List<File> infos) {
		if (infos == null) {
            throw new NullPointerException("Infos may not be null!");
        }
		final List<Uri> uriInfos = new ArrayList<>(infos.size());
		for (File f : infos) {
			uriInfos.add(Uri.fromFile(f));
		}
		setUriMedia(uriInfos);
		infoLayout.setVisibility(View.VISIBLE);
		thumbnailsRecyclerView.setVisibility(View.VISIBLE);
	}

    private void setUriMedia(final List<Uri> infos) {
		if (infos == null) {
            throw new NullPointerException("Infos may not be null!");
        }
		setThumbnailSize(AndroidUtils.dpToPx(60, fragActivity));
		SLIDESHOW = false;
		mListOfMedia = infos;
		sizeMediaFiles = mListOfMedia.size();
		if (sizeMediaFiles == 1) {
			pageSelected = 0;
		} else {
			pageSelected = 1;
		}
		initializeViewPager();
	}

    private void initializeViewPager() {
		if (fragmentManager == null) {
			fragmentManager = fragActivity.getSupportFragmentManager();
		}
        viewPagerAdapter = new ScreenSlidePagerAdapter(fragmentManager, viewPager, mListOfMedia, this);
        viewPager.setAdapter(viewPagerAdapter);
		viewPager.setPageTransformer(true, transforms[ImageFragment.curTransform]);
		recyclerAdapter = new ThumbnailAdapter(fragActivity, mListOfMedia, thumbnailOnClickListener, thumbnailSize);//mimes, parentPath, 
        thumbnailsRecyclerView.setAdapter(recyclerAdapter);
		final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(((infoLayout.getMeasuredWidth() - thumbnailSize) / 2), ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		leftRecycler.setLayoutParams(lp);
		rightRecycler.setLayoutParams(lp);
	}

	public void updateColor(final View rootView) {
		getView().setBackgroundColor(0xff000000);
	};

	private Runnable runShowCurPageSelected = new Runnable() {
		@Override
		public void run() {
			setCurrentItem(pageSelected, true);
		}
	};

	private Runnable runUpdateInfo = new Runnable() {
		@Override
		public void run() {
			final int mediaPos = pageSelected == 0 ? (sizeMediaFiles - 1) : pageSelected == (sizeMediaFiles + 1) ? 0 : (pageSelected - 1);
			final ImageView childAt = (ImageView) mLayoutManager.findViewByPosition(mediaPos);
			recyclerScroll(mediaPos, childAt);
			setupBar(mediaPos);
		}
	};

	private Runnable runSlideShow = new Runnable() {
		@Override
		public void run() {
			if (SLIDESHOW) {
				if (pageSelected < sizeMediaFiles + 1) {
					setCurrentItem(pageSelected + 1, true);
				} else {
					setCurrentItem(1, true);
				}
				infoLayout.postDelayed(this, ImageFragment.curDelay);
			}
		}
	};

	public void resetDelay() {
		infoLayout.removeCallbacks(runSlideShow);
		infoLayout.postDelayed(runSlideShow, ImageFragment.curDelay);
	}

    private final ViewPager.SimpleOnPageChangeListener viewPagerChangeListener = new ViewPager.SimpleOnPageChangeListener() {

		@Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//			Log.d(TAG, "onPageScrolled pos " + position 
//				  + ", positionOffset " + positionOffset + ", positionOffsetPixels " + positionOffsetPixels);
		}

		@Override 
		public void onPageSelected(final int pagerPos) {
			scrolledByViewPager = true;
			final int mediaPos = pagerPos == 0 ? (sizeMediaFiles - 1) : pagerPos == (sizeMediaFiles + 1) ? 0 : (pagerPos - 1);
			final int measuredWidth = infoLayout.getMeasuredWidth();
			final int mid = (measuredWidth - thumbnailSize) / 2;
			//Log.d(TAG, "onPageSelected pagerPos " + pagerPos + ", mediaPos " + mediaPos + ", mid " + mid + ", childCount " + childCount + ", measuredWidth " + measuredWidth);
			final int lenl = mid - mediaPos * thumbnailSize;
			leftRecycler.setLayoutParams(new LinearLayout.LayoutParams(Math.max(lenl, 0), ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
			if (lenl > 0) {
				thumbRecyclerScrollView.postDelayed(new Runnable() {
						public void run() {
							thumbRecyclerScrollView.fullScroll(HorizontalScrollView.FOCUS_LEFT);
						}
					}, 1L);
			}
			//Log.d(TAG, "(sizeMediaFiles - 1 - (mediaPos)) <= childCount / 2 " + ((sizeMediaFiles - 1 - (mediaPos)) <= childCount / 2));
			//Log.d(TAG, "Math.max(mid - (sizeMediaFiles - 1 - mediaPos) * thumbnailSize, 0) " + Math.max(mid - (sizeMediaFiles - 1 - mediaPos) * thumbnailSize, 0));
			final int lenr = mid - (sizeMediaFiles - 1 - mediaPos) * thumbnailSize;
			rightRecycler.setLayoutParams(new LinearLayout.LayoutParams(Math.max(lenr, 0), ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
			if (lenr > 0) {
				thumbRecyclerScrollView.postDelayed(new Runnable() {
						public void run() {
							thumbRecyclerScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
						}
					}, 1L);
			}
			final ImageView childAt = (ImageView) mLayoutManager.findViewByPosition(mediaPos);
			recyclerScroll(mediaPos, childAt);
			setupBar(mediaPos);
			pageSelected = pagerPos;
		}

		@Override 
		public void onPageScrollStateChanged(final int state) {
			int mediaPos = pageSelected == 0 ? (sizeMediaFiles - 1) : pageSelected == (sizeMediaFiles + 1) ? 0 : (pageSelected - 1);
			final ImageView childAt = (ImageView) mLayoutManager.findViewByPosition(mediaPos);
			Log.d(TAG, "onPageScrollStateChanged state " + state + ", scrolledByViewPager " + scrolledByViewPager + ", pageSelected " + pageSelected + ", mediaPos " + mediaPos);
			if (state == 2) {
				if (childAt != null) {
					childAt.setBackgroundColor(0x80808080);
				}
			} else if (state == 0) {// && scrolledByViewPager
				if (pageSelected == 0) {
					viewPager.setPageTransformer(true, null);
					viewPager.setCurrentItem(sizeMediaFiles, false);
					pageSelected = sizeMediaFiles;
					viewPager.setPageTransformer(true, transforms[ImageFragment.curTransform]);
				} else if (pageSelected == sizeMediaFiles + 1) {
					viewPager.setPageTransformer(true, null);
					viewPager.setCurrentItem(1, false);
					pageSelected = 1;
					viewPager.setPageTransformer(true, transforms[ImageFragment.curTransform]);
				}
			}
		}
	};

	private final RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
		@Override
        public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
			if (scrolledByViewPager) {
				scrolledByViewPager = false;
				return;
			}
            int dl = leftRecycler.getMeasuredWidth();
			final int measuredWidth = infoLayout.getMeasuredWidth();
			final int childCount = thumbnailsRecyclerView.getLayoutManager().getChildCount();
			Log.d(TAG, "onScrolled dx " + dx + ", dy " + dy + ", PaddingLeft " + dl + ", measuredWidth " + measuredWidth + ", thumbnailSize " + thumbnailSize + ", pageSelected " + pageSelected + ", childCount " + childCount);
			if (dl >= 0) {
				leftRecycler.setLayoutParams(new LinearLayout.LayoutParams(Math.min(Math.max(dl + dx, 0), (measuredWidth - thumbnailSize) / 2), ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
			}
			dl = rightRecycler.getMeasuredWidth();
			Log.d(TAG, "onScrolled dx " + dx + ", dy " + dy + ", PaddingRight " + dl + ", pageSelected " + pageSelected);
			if (dl >= 0) {
				rightRecycler.setLayoutParams(new LinearLayout.LayoutParams(Math.min(Math.max(dl + dx, 0), (measuredWidth - thumbnailSize) / 2), ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
			}
        }

        @Override
        public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
            super.onScrollStateChanged(recyclerView, newState);
			Log.d(TAG, "onScrollStateChanged newState " + newState + ", pageSelected " + pageSelected);
            switch (newState) {
					//case RecyclerView.SCROLL_STATE_DRAGGING:
					//break;
				case RecyclerView.SCROLL_STATE_IDLE:
					final int mid = (infoLayout.getMeasuredWidth() + thumbnailSize) / 2;//
					final ImageView childAt = (ImageView) mLayoutManager.getChildAt(mid / thumbnailSize);
					if (childAt != null) {
						final int mediaPos = Integer.valueOf(childAt.getContentDescription() + "");
						Log.d(TAG, "onScrollStateChanged newState " + newState + ", mid  " + mid + ", thumbnailSize " + thumbnailSize + ", mediaPos " + mediaPos);
						recyclerScroll(mediaPos, childAt);
						setCurrentItem(mediaPos + 1, false);
						setupBar(mediaPos);
					}
					break;
//				case RecyclerView.SCROLL_STATE_SETTLING:
//					break;
			}
        }
    };

    private final OnClickListener thumbnailOnClickListener = new OnClickListener() {
        @Override public void onClick(final View v) {
            final int mediaPos = Integer.valueOf(v.getContentDescription() + "");
			Log.d(TAG, "thumbnailOnClickListener mediaPos " + mediaPos + ", cur pageSelected " + pageSelected);
            scrolledByViewPager = false;
			recyclerScroll(mediaPos, v);
            viewPager.setPageTransformer(true, null);
			setCurrentItem(mediaPos + 1, false);
			setupBar(mediaPos);
			viewPager.setPageTransformer(true, transforms[ImageFragment.curTransform]);
        }
    };

	// --- GestureDetector.OnDoubleTapListener ---
    @Override
    public boolean onDoubleTap(final MotionEvent arg0) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(final MotionEvent arg0) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(final MotionEvent event) {
		//final TouchImageView backgroundImage = pagerAdapter.fragMap.get(pageSelected).getImage();
//		if (backgroundImage.isZoomed()) //touch && 
//			return false;
		final float x = event.getX();
        final int width = infoLayout.getMeasuredWidth();
		Log.d(TAG, "x " + x + ", width " + width + ", " + infoLayout.getWidth());
		if (x > 4 * width / 5) {
			if (pageSelected < sizeMediaFiles - 1) {
				setCurrentItem(pageSelected + 1, true);
			} else {
				//Toast.makeText(mContext, "This is the last image", Toast.LENGTH_LONG).show();
				setCurrentItem(0, true);
			}
		} else if (x < width / 5) {
			if (pageSelected > 0) {
				setCurrentItem(pageSelected - 1, true);
			} else {
				//Toast.makeText(mContext, "This is the first image", Toast.LENGTH_LONG).show();
				setCurrentItem(sizeMediaFiles - 1, true);
			}
		} else {
			SLIDESHOW = false;
			thumbnailsHiddenEnabled = !thumbnailsHiddenEnabled;
			hideThumbnails(thumbnailsHiddenEnabled);
			final int newpos = pageSelected == 0 ? sizeMediaFiles - 1 : pageSelected == sizeMediaFiles + 1 ? 0 : pageSelected - 1;
			final Uri uri = mListOfMedia.get(newpos);
			if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
				final File f = new File(Uri.decode(uri.getPath()));
				final String mimeType = MimeTypes.getMimeType(f);
				Log.d(TAG, f + " " + mimeType);
				if (mimeType.startsWith("video/")) {
					try {
						//final Uri uri = Uri.fromFile(f);
						final Intent i = new Intent(fragActivity, MediaPlayerActivity.class);
						i.setAction(Intent.ACTION_VIEW);
						i.setDataAndType(uri, mimeType);
						fragActivity.startActivity(i);
					} catch (Throwable e) {
						Toast.makeText(fragActivity, "unable to view !\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
        return false;
    }

    private void setCurrentItem(final int newPagerPos, boolean smoothScroll) {
		if (sizeMediaFiles < 1) {
			return;
		}
		Log.d(TAG, "setCurrentItem curPagerPos " + pageSelected + " to newPagerPos " + newPagerPos);
		int mediaPos = pageSelected == 0 ? (sizeMediaFiles - 1) : pageSelected == (sizeMediaFiles + 1) ? 0 : (pageSelected - 1);
		ImageView childAt = (ImageView) mLayoutManager.findViewByPosition(mediaPos);
		if (childAt != null) {
			childAt.setBackgroundColor(0x80808080);
		}
		viewPager.setCurrentItem(newPagerPos, smoothScroll);

		mediaPos = newPagerPos == 0 ? (sizeMediaFiles - 1) : newPagerPos == (sizeMediaFiles + 1) ? 0 : (newPagerPos - 1);
		childAt = (ImageView) mLayoutManager.findViewByPosition(mediaPos);
		if (childAt != null) {
			childAt.setBackgroundColor((0xc0ffffff));
		}
    }

    public void setThumbnailSize(final int thumbnailSize) {
        this.thumbnailSize = thumbnailSize;
    }

    public void hideThumbnails(final boolean thumbnailsHiddenEnabled) {
        this.thumbnailsHiddenEnabled = thumbnailsHiddenEnabled;
		if (thumbnailsHiddenEnabled) {
			thumbnailsRecyclerView.setVisibility(View.GONE);
			infoLayout.setVisibility(View.GONE);
		} else {
			thumbnailsRecyclerView.setVisibility(View.VISIBLE);
			infoLayout.setVisibility(View.VISIBLE);
			infoLayout.postDelayed(runUpdateInfo, 10);
		}
    }

    private void recyclerScroll(final int mediaPos, final View thumbnail) {
        Log.d(TAG, "scroll Recycler mediaPos " + mediaPos + ", thumbnailSize " + thumbnailSize + ", paddingLeft " + thumbnailsRecyclerView.getPaddingLeft() + ", paddingRight " + thumbnailsRecyclerView.getPaddingRight());
		//mLayoutManager.scrollToPositionWithOffset(pos, (getMeasuredWidth() - thumbnailSize) / 2);
		//int itemLength = thumbnailSize;//thumbnailsRecyclerView.getLayoutManager().getChildAt(0).getMeasuredWidth();
		int length = infoLayout.getMeasuredWidth();
		mLayoutManager.scrollToPositionWithOffset(mediaPos, (length - thumbnailSize) / 2);//(mediaPos * thumbnailSize < (length - thumbnailSize) / 2) ? mediaPos * thumbnailSize : (length - thumbnailSize) / 2);
		if (thumbnail != null) {
			thumbnail.setBackgroundColor(0xc0ffffff);
		} else {
			infoLayout.postDelayed(new Runnable() {
					@Override
					public void run() {
						final View findViewByPosition = mLayoutManager.findViewByPosition(mediaPos);
						if 	(findViewByPosition != null) {
							findViewByPosition.setBackgroundColor(0xc0ffffff);
						}
					}
				}, 20);
		}
    }

	private void setupBar(final int mediaPos) {
		if (infoLayout.getVisibility() == View.VISIBLE) {
			final Uri uri = mListOfMedia.get(mediaPos);
			if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
				final File file = new File(Uri.decode(uri.getPath()));
				fileNameTV.setText((orderType.equals("Name") ? asc : "") + file.getName());
				fileSizeTV.setText((orderType.equals("Size") ? asc : "") + Util.nf.format(file.length()) + " B");
				fileDateTV.setText((orderType.equals("Date") ? asc : "") + Util.dtf.format(file.lastModified()));
				final BitmapFactory.Options bitmapDimesions = BitmapUtil.getBitmapDimesions(file.getAbsolutePath());
				//final TouchImageView image = pagerAdapter.fragMap.get(pos).getImage();
				fileDimensionTV.setText(bitmapDimesions.outWidth + " x " + bitmapDimesions.outHeight);// + " (current " + Math.round(image.getCurrentZoom() * 100) + "%)"
			} else {
				fileNameTV.setText((orderType.equals("Name") ? asc : "") + uri.toString());
				fileSizeTV.setText("");
				fileDateTV.setText("");
			}
			fileOrderTV.setText((mediaPos + 1) + "/" + sizeMediaFiles);

		}
	}

	@Override
	public void onClick(final View p1) {
		final int mediaPos = pageSelected == 0 ? sizeMediaFiles - 1 : pageSelected == sizeMediaFiles + 1 ? 0 : pageSelected - 1;
		final Uri uri = mListOfMedia.get(mediaPos);
		if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
			return;
		}
		final File f = new File(Uri.decode(uri.getPath()));
		Log.d(TAG, "onClick pageSelected " + pageSelected + ", mediaPos " + mediaPos + ", contentDescription " + p1.getContentDescription());// + ", " + viewPager.getCurrentItem());// + ", " + pagerAdapter.getItem(pageSelected));
		if (f.exists()) {
			switch (p1.getId()) {
				case R.id.shareButton:
//				switch (rowItem.getMode()) {
//					case DROPBOX:
//					case BOX:
//					case GDRIVE:
//					case ONEDRIVE:
//						new Futils().shareCloudFile(rowItem.path, rowItem.getMode(), activity);
//						break;
//					default:
					ArrayList<File> arrayList = new ArrayList<>();
					arrayList.add(f);
					Futils.shareFiles(arrayList, fragActivity, ((ThemedActivity)fragActivity).getAppTheme(), 0x80ffff00);
					break;
					//}
				case R.id.clockwiseButton:
					new RotateTask(viewPagerAdapter.fragMap.get(pageSelected).getImage().getDrawable(), true).execute();
//					Glide
//						.with(mContext)
//						.load(f)
//						.transform(new RotateTransformation(mContext, 90f))
//						.into(pagerAdapter.fragMap.get(pageSelected).getBackgroundImage());
					break;
				case R.id.counterClockwiseButton:
					new RotateTask(viewPagerAdapter.fragMap.get(pageSelected).getImage().getDrawable(), false).execute();
//					Glide
//						.with(mContext)
//						.load(f)
//						.transform(new RotateTransformation(mContext, -90f))
//						.into(pagerAdapter.fragMap.get(pageSelected).getBackgroundImage());
					break;
//				case R.id.chromecastButton:
//					break;
				case R.id.removeButton:
					ArrayList<LayoutElement> ele = new ArrayList<LayoutElement>();
					ele.add(new LayoutElement(f));
					//if (mContext instanceof ExplorerActivity) {
					final Runnable r = new Runnable() {
						@Override
						public void run() {
							infoLayout.postDelayed(new Runnable() {
									@Override
									public void run() {
										final Uri uri = Uri.fromFile(f);
										for (Uri st : mListOfMedia) {
											if (uri.equals(st)) {
												mListOfMedia.remove(st);
												sizeMediaFiles = mListOfMedia.size();
											}
										}
										//if (sizeMediaFiles > 0) {
											viewPagerAdapter.notifyDataSetChanged();
										//}
										recyclerAdapter.notifyDataSetChanged();
									}
								}, 0);
						}
					};
					GeneralDialogCreation.deleteFilesDialog(fragActivity, //getLayoutElements(),
															(ThemedActivity)fragActivity, ele, ((ThemedActivity)fragActivity).getAppTheme(), r);
					//}
					//new Futils().deleteFiles(ele, (ExplorerActivity)mContext, /*positions, */((ThemedActivity)mContext).getAppTheme());

					break;
				case R.id.slideshowButton:
					if (sizeMediaFiles > 1) {
						SLIDESHOW = true;
						hideThumbnails(true);
						infoLayout.postDelayed(runSlideShow, ImageFragment.curDelay);
					}
					break;
				case R.id.wallpaperButton:
					WallpaperManager myWallpaperManager = WallpaperManager
						.getInstance(fragActivity);
					try {
						myWallpaperManager.setStream(new FileInputStream(f));
						Toast.makeText(fragActivity, "Wallpaper successfully changed", Toast.LENGTH_LONG).show();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case R.id.renameButton:
					MaterialDialog.Builder builder = new MaterialDialog.Builder(fragActivity);
					final String nameOri = f.getName();
					builder.input("", nameOri, false, new MaterialDialog.InputCallback() {
							@Override
							public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {

							}
						});
					builder.theme(((ThemedActivity)fragActivity).getAppTheme().getMaterialDialogTheme());
					builder.title(((ThemedActivity)fragActivity).getResources().getString(R.string.rename));
					builder.callback(new MaterialDialog.ButtonCallback() {
							@Override
							public void onPositive(MaterialDialog materialDialog) {
								final String name = materialDialog.getInputEditText().getText().toString().replaceAll("[/?*<>|:\"]", "_");
//							if (rowItem.bf.isSmb())
//								if (rowItem.bf.isDirectory() && !name.endsWith("/"))
//									name = name + "/";

								final String newName = f.getParent() + "/" + name;
								MainActivityHelper.rename(OpenMode.FILE, f.getAbsolutePath(),//mListOfMedia.get(pageSelected).getAbsolutePath(),
														  newName, (ThemedActivity)fragActivity, ThemedActivity.rootMode);
								mListOfMedia.remove(mediaPos);
								mListOfMedia.add(mediaPos, Uri.fromFile(new File(newName)));
								fileNameTV.setText(name);
								viewPagerAdapter.notifyDataSetChanged();
								recyclerAdapter.notifyDataSetChanged();
							}

							@Override
							public void onNegative(MaterialDialog materialDialog) {
								materialDialog.cancel();
							}
						});
					builder.positiveText(R.string.save);
					builder.negativeText(R.string.cancel);
					final int accentColor = ((ThemedActivity)fragActivity).getColorPreference().getColor(ColorUsage.ACCENT);
					builder.positiveColor(accentColor)
						.negativeColor(accentColor)
						.widgetColor(accentColor);
					builder.build().show();
					break;
				case R.id.copyButton:
					ExplorerActivity activity = ((ExplorerActivity)fragActivity);
					activity.MOVE_PATH = null;
					ArrayList<BaseFile> copies = new ArrayList<>();
					copies.add(new LayoutElement(f).generateBaseFile());
					activity.COPY_PATH = copies;
					if (activity.curExplorerFrag.commands.getVisibility() == View.GONE) {
						activity.curExplorerFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
						activity.curExplorerFrag.commands.setVisibility(View.VISIBLE);
						activity.curExplorerFrag.horizontalDivider6.setVisibility(View.VISIBLE);
						activity.curExplorerFrag.updateDelPaste();
					}
					if (activity.curContentFrag.commands.getVisibility() == View.GONE) {
						activity.curContentFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
						activity.curContentFrag.commands.setVisibility(View.VISIBLE);
						activity.curContentFrag.horizontalDivider6.setVisibility(View.VISIBLE);
						activity.curContentFrag.updateDelPaste();
					}
					break;
				case R.id.cutButton:
					activity = ((ExplorerActivity)fragActivity);
					activity.COPY_PATH = null;
					ArrayList<BaseFile> copie = new ArrayList<>();
					copie.add(new LayoutElement(f).generateBaseFile());
					((ExplorerActivity)fragActivity).MOVE_PATH = copie;
					//activity1.supportInvalidateOptionsMenu();
					if (activity.curExplorerFrag.commands.getVisibility() == View.GONE) {
						activity.curExplorerFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
						activity.curExplorerFrag.commands.setVisibility(View.VISIBLE);
						activity.curExplorerFrag.horizontalDivider6.setVisibility(View.VISIBLE);
						activity.curExplorerFrag.updateDelPaste();
					}
					if (activity.curContentFrag.commands.getVisibility() == View.GONE) {
						activity.curContentFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
						activity.curContentFrag.commands.setVisibility(View.VISIBLE);
						activity.curContentFrag.horizontalDivider6.setVisibility(View.VISIBLE);
						activity.curContentFrag.updateDelPaste();
					}
					break;
				case R.id.editButton:
					break;
				case R.id.scanButton:
					AndroidUtils.scanMedia(fragActivity, f.getAbsolutePath(), false);
					break;
				case R.id.fileName:
					if (orderType.equals("Name")) {
						if (asc.equals("▲ ")) {
							asc = "▼ ";
							Collections.sort(mListOfMedia, new UriSorter(UriSorter.DIR_TOP, UriSorter.NAME, UriSorter.DESCENDING));
						} else {
							asc = "▲ ";
							Collections.sort(mListOfMedia, new UriSorter(UriSorter.DIR_TOP, UriSorter.NAME, UriSorter.ASCENDING));
						}
					} else {
						orderType = "Name";
						asc = "▲ ";
						Collections.sort(mListOfMedia, new UriSorter(UriSorter.DIR_TOP, UriSorter.NAME, UriSorter.ASCENDING));
					}
					int t = pageSelected;
					setUriMedia(mListOfMedia);
					pageSelected = t;
					infoLayout.postDelayed(runShowCurPageSelected, 20);
					break;
				case R.id.fileSize:
					if (orderType.equals("Size")) {
						if (asc.equals("▲ ")) {
							asc = "▼ ";
							Collections.sort(mListOfMedia, new UriSorter(UriSorter.DIR_TOP, UriSorter.SIZE, UriSorter.DESCENDING));
						} else {
							asc = "▲ ";
							Collections.sort(mListOfMedia, new UriSorter(UriSorter.DIR_TOP, UriSorter.SIZE, UriSorter.ASCENDING));
						}
					} else {
						orderType = "Size";
						asc = "▲ ";
						Collections.sort(mListOfMedia, new UriSorter(UriSorter.DIR_TOP, UriSorter.SIZE, UriSorter.ASCENDING));
					}
					t = pageSelected;
					setUriMedia(mListOfMedia);
					pageSelected = t;
					infoLayout.postDelayed(runShowCurPageSelected, 20);
					break;
				case R.id.fileDate:
					if (orderType.equals("Date")) {
						if (asc.equals("▲ ")) {
							asc = "▼ ";
							Collections.sort(mListOfMedia, new UriSorter(UriSorter.DIR_TOP, UriSorter.DATE, UriSorter.DESCENDING));
						} else {
							asc = "▲ ";
							Collections.sort(mListOfMedia, new UriSorter(UriSorter.DIR_TOP, UriSorter.DATE, UriSorter.ASCENDING));
						}
					} else {
						orderType = "Date";
						asc = "▲ ";
						Collections.sort(mListOfMedia, new UriSorter(UriSorter.DIR_TOP, UriSorter.DATE, UriSorter.ASCENDING));
					}
					t = pageSelected;
					setUriMedia(mListOfMedia);
					pageSelected = t;
					infoLayout.postDelayed(runShowCurPageSelected, 20);
					break;
				case R.id.addshortcut:
					AndroidUtils.addShortcut(fragActivity, f);
					break;
			}
		} else {
			Toast.makeText(fragActivity, f.getAbsolutePath() + " is not existed", Toast.LENGTH_LONG).show();
		}

	}

	public class RotateTask extends AsyncTask<Void, Void, Void> {
        private Drawable from_view;
        private WeakReference<Bitmap> wrRotatedBitmap;
        private boolean clockwise;

        public RotateTask(Drawable from_view, boolean clockwise) {
            this.from_view = from_view;
            this.clockwise = clockwise;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (from_view == null) {
                    Log.e(TAG, "No drawable");
                    return null;
                }

                if (!(from_view instanceof BitmapDrawable)) {
                    Log.e(TAG, "drawable is not a bitmap");
                    return null;
                }
                BitmapDrawable bd = (BitmapDrawable)from_view;
                Bitmap old_bmp = bd.getBitmap();
                float degrees = clockwise ? 90 : 270;
                wrRotatedBitmap = new WeakReference<Bitmap>(rotateBitmap(old_bmp, degrees));
            } catch ( Throwable e ) {
                Log.e(TAG, "", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            if (wrRotatedBitmap == null) return;
            Bitmap bmp = wrRotatedBitmap.get();
            if (bmp != null)
                setBitmapToView(bmp, null);
        }
    }

	public final void setBitmapToView(Bitmap bmp, String name) {
        try {
            //Log.v( TAG, "Bitmap is ready" );
            //hideWait();
            if (bmp != null) {
                viewPagerAdapter.fragMap.get(pageSelected).getImage().setVisibility(View.VISIBLE);
                viewPagerAdapter.fragMap.get(pageSelected).getImage().setImageBitmap(bmp);

                return;
            }
        } catch ( Throwable e ) {
            e.printStackTrace();
        }
    }

	public static Bitmap rotateBitmap(Bitmap old_bmp, float degrees) {
        final Matrix m = new Matrix();
        m.postRotate(degrees);
        final int old_w = old_bmp.getWidth(); 
        final int old_h = old_bmp.getHeight();
        for (int i = 1; i <= 8; i <<= 1) {
            try {
                if (i > 1) {
                    float scale = 1.f / i;
                    m.postScale(scale, scale);
                }
                Bitmap new_bmp = Bitmap.createBitmap(old_bmp, 0, 0, old_w, old_h, m, false);
                if (new_bmp != null) {
                    //old_bmp.recycle();
                    return new_bmp;
                }
            } catch ( OutOfMemoryError e ) {
				e.printStackTrace();
			}
        }
        return null;
    }
	


}
