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
import android.widget.*;
import android.support.v7.widget.*;
import com.ToxicBakery.viewpager.transforms.*;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.*;
import android.view.GestureDetector.*;
import net.gnu.androidutil.*;
import com.amaze.filemanager.utils.files.*;
import android.app.*;
import com.amaze.filemanager.ui.dialogs.*;
import com.afollestad.materialdialogs.*;
import com.amaze.filemanager.activities.*;
import java.io.*;
import com.amaze.filemanager.utils.*;
import com.amaze.filemanager.filesystem.*;
import java.util.*;
import android.view.animation.*;
import net.gnu.util.*;
import android.graphics.drawable.*;
import android.os.*;
import java.lang.ref.*;
import android.graphics.*;
import com.amaze.filemanager.utils.color.*;

public class PhotoFragment extends Frag implements OnDoubleTapListener, OnClickListener {
	private static final String TAG = "PhotoFragment";
	
    private TouchImageView image;
	private View scrollGalleryView;
	public static final Pattern IMAGE_PATTERN = Pattern.compile("^[^\n]*?\\.(jpg|jpeg|gif|png|avi|mpg|mpeg|mp4|3gpp|3gp|3gpp2|vob|asf|wmv|flv|mkv|asx|qt|mov|webm|bmp|ico|tiff|tif|psd|cur|pcx|svg|dwg|pct|pic|jpe|mpe|3g2|m4v|wm|wmx|mpa)$", Pattern.CASE_INSENSITIVE);

	private long lastModified = 0;
	private File lastParentFolder;
	private List<LayoutElement> paths = null;
	private int initPos = 0;
	
	
	private FragmentManager fragmentManager;
    private ScreenSlidePagerAdapter imageViewPagerAdapter;
    private ArrayList<File> mListOfMedia = new ArrayList<>();
	private int sizeMediaFiles;

    private int thumbnailSize = 54; // width and height in pixels
    private boolean hidden = false;
	private int pageSelected;

    // Views
    private RecyclerView thumbnailsRecyclerView;
    private ViewPager viewPager;
	private LinearLayout infoLayout;

	private TextView fileNameTV;
	private TextView fileOrderTV;
	private TextView fileDimensionTV;
	private TextView fileSizeTV;
	private TextView fileDateTV;
	private String orderType = "";
	private String asc = "";

	private boolean SLIDESHOW = false;
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

	protected ThumbnailAdapter thumbnailRecyclerAdapter;
    protected LinearLayoutManager mLayoutManager;

	private boolean scrolledByViewPager = true;
	
	
	
    public PhotoFragment() {
		super();
		type = Frag.TYPE.PHOTO;
		title = "Photo";
	}

	@Override
	public void clone(final Frag frag, final boolean fake) {
		Log.i(TAG, "clone " + this + ", origin " + frag);
		super.clone(frag, fake);
		this.initPos = ((PhotoFragment)frag).initPos;
		this.paths = ((PhotoFragment)frag).paths;
		this.mListOfMedia = ((PhotoFragment)frag).mListOfMedia;
		this.sizeMediaFiles = mListOfMedia.size();
	}

	@Override
	public Frag clone(final boolean fake) {
		final PhotoFragment frag = (PhotoFragment) Frag.getFrag(slidingTabsFragment, TYPE.PHOTO, currentPathTitle);
		frag.clone(this, fake);
		return frag;
	}
	
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
							 final Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView " + currentPathTitle + ", " + savedInstanceState);
		return inflater.inflate(R.layout.imageview, container, false);
    }

	@Override
    public void onViewCreated(final View v, final Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
		Bundle args = getArguments();
		Log.d(TAG, "onViewCreated " + currentPathTitle + ", args=" + args + ", " + savedInstanceState);

		scrollGalleryView = v.findViewById(R.id.scroll_gallery_view);
		image = (TouchImageView) v.findViewById(R.id.image);
		thumbnailSize = AndroidUtils.dpToPx(54, fragActivity);
		initScrollGalleryView(v);
		if (paths != null) {
			open(initPos, paths);
			paths = null;
		}
		updateColor(v);
		final Intent intent = fragActivity.getIntent();
		//Log.d(TAG, "currentPathTitle " + currentPathTitle + ", intent " + intent);
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
	
	
	public void  initScrollGalleryView(final View v) {
        
        viewPager = (ViewPager) v.findViewById(R.id.photoViewPager);
        thumbnailsRecyclerView = (RecyclerView) v.findViewById(R.id.thumbnails_container);
		infoLayout = (LinearLayout) v.findViewById(R.id.info);

		fileNameTV = (TextView) v.findViewById(R.id.fileName);
		fileOrderTV = (TextView) v.findViewById(R.id.fileOrder);
		fileDimensionTV = (TextView) v.findViewById(R.id.fileDimension);
		fileSizeTV = (TextView) v.findViewById(R.id.fileSize);
		fileDateTV = (TextView) v.findViewById(R.id.fileDate);

		slideshowButton = (ImageButton) v.findViewById(R.id.slideshowButton);
		shareButton = (ImageButton) v.findViewById(R.id.shareButton);
		clockwiseButton = (ImageButton) v.findViewById(R.id.clockwiseButton);
		counterClockwiseButton = (ImageButton) v.findViewById(R.id.counterClockwiseButton);
		wallpaperButton = (ImageButton) v.findViewById(R.id.wallpaperButton);
		//chromecastButton = (ImageButton) findViewById(R.id.chromecastButton);
		addShortcutButton = (ImageButton) v.findViewById(R.id.addshortcut);
		renameButton = (ImageButton) v.findViewById(R.id.renameButton);
		copyButton = (ImageButton) v.findViewById(R.id.copyButton);
		cutButton = (ImageButton) v.findViewById(R.id.cutButton);
		removeButton = (ImageButton) v.findViewById(R.id.removeButton);
		editButton = (ImageButton) v.findViewById(R.id.editButton);
		scanButton = (ImageButton) v.findViewById(R.id.scanButton);

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
		viewPager.setPageTransformer(true, transforms[ImageFragment.curTransform]);
		mLayoutManager = new LinearLayoutManager(fragActivity);
		mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		thumbnailsRecyclerView.setLayoutManager(mLayoutManager);
		thumbnailsRecyclerView.addOnScrollListener(mScrollListener);
        thumbnailsRecyclerView.setHasFixedSize(true);
		thumbnailsRecyclerView.setItemViewCacheSize(20);
		//thumbnailsRecyclerView.setDrawingCacheEnabled(true);
		//thumbnailsRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

		//Log.d(TAG, mContext + ".");
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

	}
	

    @Override
    public void onSaveInstanceState(final Bundle bundle) {
		Log.d(TAG, "onSaveInstanceState " + bundle + ", mListOfMedia.size() " + mListOfMedia.size());
		
        bundle.putBoolean("SLIDESHOW", SLIDESHOW);
		bundle.putBoolean("hidden", hidden);
		//bundle.putSerializable("mListOfMedia", mListOfMedia);
		final ArrayList<String> ml = new ArrayList<>(mListOfMedia.size());
		for (File f : mListOfMedia) {
			ml.add(f.getAbsolutePath());
		}
		bundle.putStringArrayList("mListOfMedia", ml);

		bundle.putInt("thumbnailSize", thumbnailSize);
		bundle.putInt("pageSelected", pageSelected);
		scrollGalleryView.removeCallbacks(runSlideshow);
	}

	@Override
	public void onDestroy() {
		scrollGalleryView.removeCallbacks(runSlideshow);
		scrollGalleryView.removeCallbacks(runUpdateInfo);
		scrollGalleryView.removeCallbacks(runSorting);
		super.onDestroy();
	}
	
	@Override
    public void onViewStateRestored(final Bundle bundle) {
		Log.d(TAG, "onViewStateRestored " + bundle +", size " + mListOfMedia.size());
		if (bundle != null && bundle instanceof Bundle) {
			SLIDESHOW = bundle.getBoolean("SLIDESHOW", false);
			hidden = bundle.getBoolean("hidden", false);
			final ArrayList<String> lm = bundle.getStringArrayList("mListOfMedia");
			if (lm != null && lm.size() > 0) {
				mListOfMedia.clear();// = new ArrayList<>();
				for (String st : lm) {
					mListOfMedia.add(new File(st));
				}
			}
			sizeMediaFiles = mListOfMedia.size();
			thumbnailSize = bundle.getInt("thumbnailSize", 54);
			pageSelected = bundle.getInt("pageSelected", pageSelected);

			super.onViewStateRestored(bundle);
		} else {
			super.onViewStateRestored(bundle);
		}
		hideThumbnails(hidden);
		
		runSlideshow.run();
	}

	private Runnable runSorting = new Runnable() {
		@Override
		public void run() {
			setCurrentItem(pageSelected, false);
		}
	};

	private Runnable runUpdateInfo = new Runnable() {
		@Override
		public void run() {
			final int newpos = pageSelected == 0 ? (sizeMediaFiles - 1) : pageSelected == (sizeMediaFiles + 1) ? 0 : (pageSelected - 1);
			final ImageView childAt = (ImageView) mLayoutManager.findViewByPosition(newpos);
			scrollRecycler(newpos, childAt);
		}
	};

	private Runnable runSlideshow = new Runnable() {
		@Override
		public void run() {
			if (SLIDESHOW) {
				if (pageSelected < sizeMediaFiles + 1) {
					setCurrentItem(pageSelected + 1, true);
				} else {
					setCurrentItem(1, true);
				}
				scrollGalleryView.postDelayed(this, ImageFragment.curDelay);
			}
		}
	};

	public void resetDelay() {
		scrollGalleryView.removeCallbacks(runSlideshow);
		scrollGalleryView.postDelayed(runSlideshow, ImageFragment.curDelay);
	}

    private final ViewPager.SimpleOnPageChangeListener viewPagerChangeListener = new ViewPager.SimpleOnPageChangeListener() {

		@Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//			Log.d(TAG, "onPageScrolled pos " + position 
//				  + ", positionOffset " + positionOffset + ", positionOffsetPixels " + positionOffsetPixels);
		}

		@Override 
		public void onPageSelected(int pagerPos) {
			scrolledByViewPager = true;
			final int mediaPos = pagerPos == 0 ? (sizeMediaFiles - 1) : pagerPos == (sizeMediaFiles + 1) ? 0 : (pagerPos - 1);
			final int childCount = thumbnailsRecyclerView.getLayoutManager().getChildCount();
			final int measuredWidth = scrollGalleryView.getMeasuredWidth();
			final int mid = (measuredWidth - thumbnailSize) / 2;
			Log.d(TAG, "onPageSelected pagerPos " + pagerPos + ", mediaPos " + mediaPos + ", mid " + mid + ", childCount " + childCount);
			if ((mediaPos) <= mid / thumbnailSize) {
				thumbnailsRecyclerView.setPadding(Math.max(mid - (mediaPos) * thumbnailSize, 0), 0, 0, 0);
			} else if ((sizeMediaFiles - 1 - (mediaPos)) <= childCount / 2) {
				thumbnailsRecyclerView.setPadding(0, 0, Math.max(mid - (sizeMediaFiles - 1 - (mediaPos)) * thumbnailSize, 0), 0);
			} else {
				thumbnailsRecyclerView.setPadding(0, 0, 0, 0);
			}
			final ImageView childAt = (ImageView) mLayoutManager.findViewByPosition(mediaPos);
			scrollRecycler(mediaPos, childAt);
			pageSelected = pagerPos;
		}


		@Override 
		public void onPageScrollStateChanged(final int state) {
			Log.d(TAG, "onPageScrollStateChanged state " + state + ", pageSelected " + pageSelected);
			final int mediaPos = pageSelected == 0 ? (sizeMediaFiles - 1) : pageSelected == (sizeMediaFiles + 1) ? 0 : (pageSelected - 1);
			final ImageView childAt = (ImageView) mLayoutManager.findViewByPosition(mediaPos);
			if (state == ViewPager.SCROLL_STATE_IDLE) {// && scrolledByViewPager
				scrollRecycler(mediaPos, childAt);
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
			} else { // if (state == ViewPager.SCROLL_STATE_SETTLING) 
				if (childAt != null) {
					childAt.setBackgroundColor(0x80808080);
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
            // dx am: index lon xuong nho, photo di sang phai, tay qua phai, scrollbar qua trai
			final int paddingLeft = thumbnailsRecyclerView.getPaddingLeft();
			final int measuredWidth = scrollGalleryView.getMeasuredWidth();
			final int childCount = mLayoutManager.getChildCount();
			//Log.d(TAG, "onScrolled dx " + dx + ", PaddingLeft " + paddingLeft + ", measuredWidth " + measuredWidth + ", thumbnailSize " + thumbnailSize + ", pageSelected " + pageSelected + ", childCount " + childCount);
			if (paddingLeft > 0) {
				thumbnailsRecyclerView.setPadding(Math.min(Math.max(paddingLeft - dx, 0), (measuredWidth - thumbnailSize) / 2), 0, 0, 0);
			}
			int paddingRight = thumbnailsRecyclerView.getPaddingRight();
			//Log.d(TAG, "onScrolled dx " + dx + ", paddingRight " + paddingRight + ", pageSelected " + pageSelected);
			if (paddingRight > 0) {
				//Log.d(TAG, "(measuredWidth - thumbnailSize) / 2) " + (measuredWidth - thumbnailSize) / 2);
				///Log.d(TAG, "Math.min(Math.max(dl + dx, 0), (measuredWidth - thumbnailSize) / 2) " + Math.min(Math.max(dl + dx, 0), (measuredWidth - thumbnailSize) / 2));
				thumbnailsRecyclerView.setPadding(0, 0, Math.min(Math.max(paddingRight + dx, 0), (measuredWidth - thumbnailSize) / 2), 0);
			}
			//paddingLeft = thumbnailsRecyclerView.getPaddingLeft();
			//Log.d(TAG, "onScrolled dx " + dx + ", PaddingLeft " + paddingLeft + ", PaddingRight " + thumbnailsRecyclerView.getPaddingRight());
			final int mid;
			//de -thumbnailSize là bi chay lui
			mid = ((measuredWidth) / 2 - paddingLeft) / thumbnailSize;//
			//} else {
			//	mid = ((measuredWidth + thumbnailSize) / 2 - paddingLeft) / thumbnailSize;
			//}
			ImageView childAt = (ImageView) mLayoutManager.getChildAt(mid);
			if (childAt == null) {
				Log.d(TAG, "onScrolled childAt == null, pageSelected " + pageSelected + ", mid " + mid + ", ChildCount " + childCount);
				//thumbnailsRecyclerView.setPadding(measuredWidth / 2 - (sizeMediaFiles - 1 - pageSelected) * thumbnailSize / 2, 0, 0, 0);
				childAt = (ImageView) mLayoutManager.getChildAt(mid - 1);
			}
			int mediaPos = Integer.valueOf(childAt.getContentDescription() + "");
			//Log.d(TAG, "onScrolled paddingLeft " + paddingLeft + ", mid  " + mid + ", mediaPos " + mediaPos + ", childAt " + childAt);
			setCurrentItem(mediaPos + 1, false);

        }

        @Override
        public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
            super.onScrollStateChanged(recyclerView, newState);
			Log.d(TAG, "onScrollStateChanged newState " + newState + ", pageSelected " + pageSelected);
            switch (newState) {
					//case RecyclerView.SCROLL_STATE_DRAGGING:
					//break;
				case RecyclerView.SCROLL_STATE_IDLE:
					final int mid = (scrollGalleryView.getMeasuredWidth() + thumbnailSize) / 2;//
					final ImageView childAt = (ImageView) mLayoutManager.getChildAt(mid / thumbnailSize);
					if (childAt != null) {
						final int pos = Integer.valueOf(childAt.getContentDescription() + "");
						//Log.d(TAG, "onScrollStateChanged newState " + newState + ", mid  " + mid + ", thumbnailSize " + thumbnailSize + ", pos " + pos);
						scrollRecycler(pos, childAt);
						setCurrentItem(pos + 1, false);
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
			scrollRecycler(mediaPos, v);
			viewPager.setPageTransformer(true, null);
            setCurrentItem(mediaPos + 1, false);
			viewPager.setPageTransformer(true, transforms[ImageFragment.curTransform]);
			//setupBar(pos);
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
        final int width = scrollGalleryView.getMeasuredWidth();
		Log.d(TAG, "x " + x + ", width " + width + ", " + scrollGalleryView.getWidth());
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
			hidden = !hidden;
			hideThumbnails(hidden);
			final int newpos = pageSelected == 0 ? sizeMediaFiles - 1 : pageSelected == sizeMediaFiles + 1 ? 0 : pageSelected - 1;
			final File f = mListOfMedia.get(newpos);
			final String mimeType = MimeTypes.getMimeType(f);
			Log.d(TAG, f + " " + mimeType);
			if (mimeType.startsWith("video/")) {
				try {
					final Uri uri = Uri.fromFile(f);
					final Intent i = new Intent(fragActivity, MediaPlayerActivity.class);
					i.setAction(Intent.ACTION_VIEW);
					i.setDataAndType(uri, mimeType);
					fragActivity.startActivity(i);
				} catch (Throwable e) {
					Toast.makeText(fragActivity, "unable to view !\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		}
        return false;
    }

    public void setFileMedia() {
		SLIDESHOW = false;
		
		sizeMediaFiles = mListOfMedia.size();
		//Log.d(TAG, "setFileMedia.mListOfMedia " + mListOfMedia);
		if (sizeMediaFiles == 1) {
			pageSelected = 0;
		} else {
			pageSelected = 1;
		}
		initializeViewPager();
		infoLayout.setVisibility(View.VISIBLE);
		thumbnailsRecyclerView.setVisibility(View.VISIBLE);
	}

    private void initializeViewPager() {
		if (fragmentManager == null) {
			fragmentManager = fragActivity.getSupportFragmentManager();
		}
        imageViewPagerAdapter = new ScreenSlidePagerAdapter(fragmentManager, viewPager, mListOfMedia, this);
        viewPager.setAdapter(imageViewPagerAdapter);
		thumbnailRecyclerAdapter = new ThumbnailAdapter(fragActivity, mListOfMedia, thumbnailOnClickListener, thumbnailSize);//mimes, parentPath, 
        thumbnailsRecyclerView.setAdapter(thumbnailRecyclerAdapter);
		if (sizeMediaFiles == 1) {
			thumbnailsRecyclerView.setPadding((scrollGalleryView.getMeasuredWidth() - thumbnailSize) / 2, 0, 0, 0);
		}
	}

    public void setCurrentItem(final int newPagerPos, boolean smoothScroll) {
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

    public void hideThumbnails(final boolean thumbnailsHiddenEnabled) {
        this.hidden = thumbnailsHiddenEnabled;
		if (thumbnailsHiddenEnabled) {
			thumbnailsRecyclerView.setVisibility(View.GONE);
			infoLayout.setVisibility(View.GONE);
		} else {
			thumbnailsRecyclerView.setVisibility(View.VISIBLE);
			infoLayout.setVisibility(View.VISIBLE);
			scrollGalleryView.postDelayed(runUpdateInfo, 10);
		}
    }

    private void scrollRecycler(final int mediaPos, final View thumbnail) {
        Log.d(TAG, "scrollRecycler mediaPos " + mediaPos + ", thumbnailSize " + thumbnailSize + ", paddingLeft " + thumbnailsRecyclerView.getPaddingLeft() + ", paddingRight " + thumbnailsRecyclerView.getPaddingRight());
		//mLayoutManager.scrollToPositionWithOffset(pos, (getMeasuredWidth() - thumbnailSize) / 2);
		//int itemLength = thumbnailSize;//thumbnailsRecyclerView.getLayoutManager().getChildAt(0).getMeasuredWidth();
		int length = scrollGalleryView.getMeasuredWidth();
		mLayoutManager.scrollToPositionWithOffset(mediaPos, Math.min(mediaPos * thumbnailSize, (length - thumbnailSize) / 2));// ? mediaPos * thumbnailSize : (length - thumbnailSize) / 2);
		if (thumbnail != null) {
			thumbnail.setBackgroundColor(0xc0ffffff);
		} else {
			scrollGalleryView.postDelayed(new Runnable() {
					@Override
					public void run() {
						final View findViewByPosition = mLayoutManager.findViewByPosition(mediaPos);
						if 	(findViewByPosition != null) {
							findViewByPosition.setBackgroundColor(0xc0ffffff);
						}
					}
				}, 20);
		}
		setupBar(mediaPos);
    }

	private void setupBar(final int mediaPos) {
		if (infoLayout.getVisibility() == View.VISIBLE) {
			final File file = mListOfMedia.get(mediaPos);
			fileNameTV.setText((orderType.equals("Name") ? asc : "") + file.getName());
			fileSizeTV.setText((orderType.equals("Size") ? asc : "") + Util.nf.format(file.length()) + " B");
			fileDateTV.setText((orderType.equals("Date") ? asc : "") + Util.dtf.format(file.lastModified()));
			fileOrderTV.setText((mediaPos + 1) + "/" + sizeMediaFiles);

			final BitmapFactory.Options bitmapDimesions = BitmapUtil.getBitmapDimesions(file.getAbsolutePath());
			//final TouchImageView image = pagerAdapter.fragMap.get(pos).getImage();
			fileDimensionTV.setText(bitmapDimesions.outWidth + " x " + bitmapDimesions.outHeight);// + " (current " + Math.round(image.getCurrentZoom() * 100) + "%)"
		}
	}

	@Override
	public void onClick(final View p1) {
		final int mediaPos = pageSelected == 0 ? sizeMediaFiles - 1 : pageSelected == sizeMediaFiles + 1 ? 0 : pageSelected - 1;
		final File f = mListOfMedia.get(mediaPos);
		Log.d(TAG, "onClick pageSelected " + pageSelected + ", mediaPos " + mediaPos + ", ContentDescription " + p1.getContentDescription());// + ", " + viewPager.getCurrentItem());// + ", " + pagerAdapter.getItem(pageSelected));
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
					Futils.shareFiles(arrayList, (ExplorerActivity)fragActivity, ((ExplorerActivity)fragActivity).getAppTheme(), 0x80ffff00);
					break;
					//}
				case R.id.clockwiseButton:
					new RotateTask(imageViewPagerAdapter.getCurrentItem().getImage().getDrawable(), true).execute();
//					Glide
//						.with(mContext)
//						.load(f)
//						.transform(new RotateTransformation(mContext, 90f))
//						.into(pagerAdapter.fragMap.get(pageSelected).getBackgroundImage());
					break;
				case R.id.counterClockwiseButton:
					new RotateTask(imageViewPagerAdapter.getCurrentItem().getImage().getDrawable(), false).execute();
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
							scrollGalleryView.postDelayed(new Runnable() {
									@Override
									public void run() {
										imageViewPagerAdapter.notifyDataSetChanged();
										thumbnailRecyclerAdapter.notifyDataSetChanged();
									}
								}, 0);
						}
					};
					GeneralDialogCreation.deleteFilesDialog(fragActivity, //getLayoutElements(),
															(ThemedActivity)fragActivity, ele, ((ThemedActivity)fragActivity).getAppTheme(), r);
					break;
				case R.id.slideshowButton:
					if (sizeMediaFiles > 1) {
						SLIDESHOW = true;
						hideThumbnails(true);
						scrollGalleryView.postDelayed(runSlideshow, ImageFragment.curDelay);
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
								mListOfMedia.add(mediaPos, new File(newName));
								fileNameTV.setText(name);
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
							Collections.sort(mListOfMedia, new FileSorter(FileSorter.DIR_TOP, FileSorter.NAME, FileSorter.DESCENDING));
						} else {
							asc = "▲ ";
							Collections.sort(mListOfMedia, new FileSorter(FileSorter.DIR_TOP, FileSorter.NAME, FileSorter.ASCENDING));
						}
					} else {
						orderType = "Name";
						asc = "▲ ";
						Collections.sort(mListOfMedia, new FileSorter(FileSorter.DIR_TOP, FileSorter.NAME, FileSorter.ASCENDING));
					}
					int t = pageSelected;
					setFileMedia();
					pageSelected = t;
					scrollGalleryView.postDelayed(runSorting, 20);
					break;
				case R.id.fileSize:
					if (orderType.equals("Size")) {
						if (asc.equals("▲ ")) {
							asc = "▼ ";
							Collections.sort(mListOfMedia, new FileSorter(FileSorter.DIR_TOP, FileSorter.SIZE, FileSorter.DESCENDING));
						} else {
							asc = "▲ ";
							Collections.sort(mListOfMedia, new FileSorter(FileSorter.DIR_TOP, FileSorter.SIZE, FileSorter.ASCENDING));
						}
					} else {
						orderType = "Size";
						asc = "▲ ";
						Collections.sort(mListOfMedia, new FileSorter(FileSorter.DIR_TOP, FileSorter.SIZE, FileSorter.ASCENDING));
					}
					t = pageSelected;
					setFileMedia();
					pageSelected = t;
					scrollGalleryView.postDelayed(runSorting, 20);
					break;
				case R.id.fileDate:
					if (orderType.equals("Date")) {
						if (asc.equals("▲ ")) {
							asc = "▼ ";
							Collections.sort(mListOfMedia, new FileSorter(FileSorter.DIR_TOP, FileSorter.DATE, FileSorter.DESCENDING));
						} else {
							asc = "▲ ";
							Collections.sort(mListOfMedia, new FileSorter(FileSorter.DIR_TOP, FileSorter.DATE, FileSorter.ASCENDING));
						}
					} else {
						orderType = "Date";
						asc = "▲ ";
						Collections.sort(mListOfMedia, new FileSorter(FileSorter.DIR_TOP, FileSorter.DATE, FileSorter.ASCENDING));
					}
					t = pageSelected;
					setFileMedia();
					pageSelected = t;
					scrollGalleryView.postDelayed(runSorting, 20);
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
                ImageView image = imageViewPagerAdapter.getCurrentItem().getImage();//TouchImageView
				image.setVisibility(View.VISIBLE);
                image.setImageBitmap(bmp);

                return;
            }
        } catch (Throwable e) {
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

	public void setInitPos(int initPos) {
		this.initPos = initPos;
	}

	public void setPaths(List<LayoutElement> paths) {
		this.paths = paths;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (paths != null) {
			open(initPos, paths);
			paths = null;
		}
	}

	@Override
	public void open(final int curPos, final List<LayoutElement> paths) {
		Log.d(TAG, "open list curPos " + curPos + ", paths.size() " + paths.size());
		image.setVisibility(View.GONE);
		scrollGalleryView.setVisibility(View.VISIBLE);
		if (paths != null) {
			File f;
			mListOfMedia.clear();
			int found = 0;
			int counter = 0;
			for (LayoutElement st : paths) {
				f = new File(st.path);
				if (f.isFile()) {//IMAGE_PATTERN.matcher(st).matches()
					if (IMAGE_PATTERN.matcher(st.name).matches()) { //}extension.length() > 0 && imageVideoSet.contains(extension)) {
						mListOfMedia.add(f);
						if (counter < curPos) {
							found++;
						}
					}
				}
				counter++;
			}

			final int foundFinal = found;
			//showWait();
			setFileMedia();
			scrollGalleryView.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (mListOfMedia.size() > 1) {
							setCurrentItem(foundFinal + 1, true);
						} else {
							setCurrentItem(0, true);
						}
						//hideWait();
					}
				}, 100);
		}
	}

	public void open(final int curPos, final String... paths) {
		Log.d(TAG, "open String..." + curPos);
		image.setVisibility(View.GONE);
		scrollGalleryView.setVisibility(View.VISIBLE);
		if (paths != null) {
			File f;
			mListOfMedia.clear();
			for (String st : paths) {
				f = new File(st);
				if (f.isFile()) {
					if (IMAGE_PATTERN.matcher(f.getName()).matches()) { 
						mListOfMedia.add(f);
					}
				}
			}
			//showWait();
			setFileMedia();
			scrollGalleryView.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (mListOfMedia.size() > 1) {
							setCurrentItem(curPos + 1, true);
						} else {
							setCurrentItem(0, true);
						}
						//hideWait();
					}
				}, 100);
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
				mListOfMedia.clear();
				mListOfMedia.addAll(Arrays.asList(fs));
				final int cur;
				if (file.isFile()) {
					cur = mListOfMedia.indexOf(file);
				} else {
					cur = 0;
				}
				Log.d(TAG, "cur1 " + cur);
				if (fs.length > 0) {
					//showWait();
					setFileMedia();
					scrollGalleryView.postDelayed(new Runnable() {
							@Override
							public void run() {
								if (mListOfMedia.size() > 1) {
									setCurrentItem(cur + 1, true);
								} else {
									setCurrentItem(0, true);
								}
								//hideWait();
							}
						}, 100);
				} else {
					Toast.makeText(fragActivity, "No image or video file", Toast.LENGTH_LONG).show();
				}
			} else {
				int i = 0;
				int cur = 0;
				if (file.isFile()) {
					final String fName = file.getName();
					for (File url : mListOfMedia) {
						if (url.getName().equals(fName)) {
							cur = i;
							break;
						}
						i++;
					}
					if (mListOfMedia.size() > 1) {
						cur = cur + 1;
					}
				}
				Log.d(TAG, "cur2 " + cur);
				setCurrentItem(cur, true);
			}
		}
    }

	public void updateColor(final View rootView) {
		getView().setBackgroundColor(0xff000000);
	}
	
}
