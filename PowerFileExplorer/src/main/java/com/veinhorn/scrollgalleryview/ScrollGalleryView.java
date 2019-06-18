package com.veinhorn.scrollgalleryview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.gnu.explorer.R;
import android.util.Log;
import net.gnu.explorer.ExplorerActivity;
import android.widget.ImageButton;
import android.widget.TextView;
import java.io.File;
import net.gnu.util.Util;
import com.ortiz.touch.TouchImageView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import com.amaze.filemanager.ui.icons.MimeTypes;
import android.net.Uri;
import android.content.Intent;
import android.widget.Toast;
import java.util.TreeSet;
import java.util.HashSet;
import android.media.ExifInterface;
import java.io.IOException;
import net.gnu.androidutil.BitmapUtil;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapFactory;
import android.widget.FrameLayout;
import android.support.v4.app.FragmentActivity;

import net.gnu.androidutil.AndroidUtils;
import com.amaze.filemanager.ui.LayoutElement;
import com.amaze.filemanager.utils.files.Futils;
import com.amaze.filemanager.activities.ThemedActivity;
import android.graphics.Color;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amaze.filemanager.utils.OpenMode;
import com.amaze.filemanager.filesystem.BaseFile;
import android.view.animation.AnimationUtils;
import android.os.AsyncTask;
import android.graphics.drawable.Drawable;
import java.lang.ref.WeakReference;
import android.graphics.Matrix;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.graphics.PointF;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.GestureDetector.OnDoubleTapListener;
import android.app.WallpaperManager;
import java.io.FileInputStream;
import net.gnu.explorer.LayoutElementSorter;
import net.gnu.util.FileSorter;
import android.view.GestureDetector.SimpleOnGestureListener;
import com.ToxicBakery.viewpager.transforms.ForegroundToBackgroundTransformer;
import com.ToxicBakery.viewpager.transforms.ABaseTransformer;
import com.ToxicBakery.viewpager.transforms.AccordionTransformer;
import com.ToxicBakery.viewpager.transforms.BackgroundToForegroundTransformer;
import com.ToxicBakery.viewpager.transforms.CubeInTransformer;
import com.ToxicBakery.viewpager.transforms.CubeOutTransformer;
import com.ToxicBakery.viewpager.transforms.ZoomOutSlideTransformer;
import com.ToxicBakery.viewpager.transforms.DepthPageTransformer;
import com.ToxicBakery.viewpager.transforms.FlipHorizontalTransformer;
import com.ToxicBakery.viewpager.transforms.FlipVerticalTransformer;
import com.ToxicBakery.viewpager.transforms.RotateDownTransformer;
import com.ToxicBakery.viewpager.transforms.RotateUpTransformer;
import com.ToxicBakery.viewpager.transforms.ScaleInOutTransformer;
import com.ToxicBakery.viewpager.transforms.StackTransformer;
import com.ToxicBakery.viewpager.transforms.TabletTransformer;
import com.ToxicBakery.viewpager.transforms.ZoomInTransformer;
import com.ToxicBakery.viewpager.transforms.ZoomOutTranformer;
import com.ToxicBakery.viewpager.transforms.DefaultTransformer;
import com.ToxicBakery.viewpager.transforms.FadeTransformer;
import android.os.Parcelable;
import android.os.Parcel;
import com.ToxicBakery.viewpager.transforms.DrawFromBackTransformer;
import com.amaze.filemanager.ui.dialogs.GeneralDialogCreation;
import com.amaze.filemanager.utils.MainActivityHelper;
import com.amaze.filemanager.utils.color.ColorUsage;
import net.gnu.explorer.MediaPlayerActivity;
import java.util.LinkedList;
import android.os.*;

public class ScrollGalleryView extends LinearLayout implements OnDoubleTapListener, OnClickListener {//OnTouchListener, 

    private static String TAG = "ScrollGalleryView";

	private FragmentManager fragmentManager;
    private ScreenSlidePagerAdapter imageViewPagerAdapter;
    private ArrayList<File> mListOfMedia;
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
    
	boolean scrolledByViewPager = true;

    public ScrollGalleryView(final Context context) {
		this(context, null);
	}

	public ScrollGalleryView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScrollGalleryView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        setOrientation(VERTICAL);
        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.scroll_gallery_view, this, true);
		//touch = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO;

        viewPager = (ViewPager) findViewById(R.id.photoViewPager);
        thumbnailsRecyclerView = (RecyclerView) findViewById(R.id.thumbnails_container);
		infoLayout = (LinearLayout) findViewById(R.id.info);

		fileNameTV = (TextView) findViewById(R.id.fileName);
		fileOrderTV = (TextView) findViewById(R.id.fileOrder);
		fileDimensionTV = (TextView) findViewById(R.id.fileDimension);
		fileSizeTV = (TextView) findViewById(R.id.fileSize);
		fileDateTV = (TextView) findViewById(R.id.fileDate);

		slideshowButton = (ImageButton) findViewById(R.id.slideshowButton);
		shareButton = (ImageButton) findViewById(R.id.shareButton);
		clockwiseButton = (ImageButton) findViewById(R.id.clockwiseButton);
		counterClockwiseButton = (ImageButton) findViewById(R.id.counterClockwiseButton);
		wallpaperButton = (ImageButton) findViewById(R.id.wallpaperButton);
		//chromecastButton = (ImageButton) findViewById(R.id.chromecastButton);
		addShortcutButton = (ImageButton) findViewById(R.id.addshortcut);
		renameButton = (ImageButton) findViewById(R.id.renameButton);
		copyButton = (ImageButton) findViewById(R.id.copyButton);
		cutButton = (ImageButton) findViewById(R.id.cutButton);
		removeButton = (ImageButton) findViewById(R.id.removeButton);
		editButton = (ImageButton) findViewById(R.id.editButton);
		scanButton = (ImageButton) findViewById(R.id.scanButton);

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
		mLayoutManager = new LinearLayoutManager(context);
		mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		thumbnailsRecyclerView.setLayoutManager(mLayoutManager);
		thumbnailsRecyclerView.addOnScrollListener(mScrollListener);
        thumbnailsRecyclerView.setHasFixedSize(true);
		thumbnailsRecyclerView.setItemViewCacheSize(20);
		//thumbnailsRecyclerView.setDrawingCacheEnabled(true);
		//thumbnailsRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

		Log.d(TAG, mContext + ".");
		if (mContext instanceof ExplorerActivity) {
			copyButton.setColorFilter(0xffffffff);
			cutButton.setColorFilter(0xffffffff);
		} else {
			copyButton.setVisibility(GONE);
			cutButton.setVisibility(GONE);
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
    public Parcelable onSaveInstanceState() {
        Log.d(TAG, "onSaveInstanceState ");
		final Bundle bundle = new Bundle();
		
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
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
		removeCallbacks(runSlideshow);
        return bundle;
	}

	@Override
    public void onRestoreInstanceState(Parcelable state) {
        Log.d(TAG, "onRestoreInstanceState " + state);
		if (state != null && state instanceof Bundle) {
			final Bundle bundle = (Bundle) state;
			SLIDESHOW = bundle.getBoolean("SLIDESHOW", false);
			hidden = bundle.getBoolean("hidden", false);
			final ArrayList<String> lm = (ArrayList<String>) bundle.getStringArrayList("mListOfMedia");
			if (lm != null && lm.size() > 0) {
				mListOfMedia = new ArrayList<>();
				for (String st : lm) {
					mListOfMedia.add(new File(st));
				}
			}
			sizeMediaFiles = mListOfMedia.size();
			thumbnailSize = bundle.getInt("thumbnailSize", 54);
			pageSelected = bundle.getInt("pageSelected", pageSelected);
			
			super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
		} else {
			super.onRestoreInstanceState(state);
		}
		hideThumbnails(hidden);
		initializeViewPager();
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
				postDelayed(this, ImageFragment.curDelay);
			}
		}
	};

	public void resetDelay() {
		removeCallbacks(runSlideshow);
		postDelayed(runSlideshow, ImageFragment.curDelay);
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
			final int measuredWidth = getMeasuredWidth();
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
			final int measuredWidth = getMeasuredWidth();
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
					final int mid = (getMeasuredWidth() + thumbnailSize) / 2;//
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
        final int width = getMeasuredWidth();
		Log.d(TAG, "x " + x + ", width " + width + ", " + getWidth());
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
					final Intent i = new Intent(mContext, MediaPlayerActivity.class);
					i.setAction(Intent.ACTION_VIEW);
					i.setDataAndType(uri, mimeType);
					mContext.startActivity(i);
				} catch (Throwable e) {
					Toast.makeText(mContext, "unable to view !\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		}
        return false;
    }

//    private void setFileMedia(final List<File> infos) {
//		final List<Uri> uriInfos = new ArrayList<>(infos.size());
//		for (File f : infos) {
//			uriInfos.add(Uri.fromFile(f));
//		}
//		setUriMedia(uriInfos);
//		infoLayout.setVisibility(View.VISIBLE);
//		thumbnailsRecyclerView.setVisibility(View.VISIBLE);
//	}

//    public void setUriMedia(final List<Uri> infos) {
//		SLIDESHOW = false;
//		mListOfMedia = infos;
//		sizeMediaFiles = mListOfMedia.size();
//		if (sizeMediaFiles == 1) {
//			pageSelected = 0;
//		} else {
//			pageSelected = 1;
//		}
//		initializeViewPager();
//	}

//    private void initializeViewPager() {
//		if (fragmentManager == null) {
//			fragmentManager = fragActivity.getSupportFragmentManager();
//		}
//        viewPagerAdapter = new ScreenSlidePagerAdapter(fragmentManager, viewPager, mListOfMedia, this);
//        viewPager.setAdapter(viewPagerAdapter);
//		recyclerAdapter = new ThumbnailAdapter(getContext(), mListOfMedia, thumbnailOnClickListener, thumbnailSize);//mimes, parentPath, 
//        thumbnailsRecyclerView.setAdapter(recyclerAdapter);
//		final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(((infoLayout.getMeasuredWidth() - thumbnailSize) / 2), ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
////		leftRecycler.setLayoutParams(lp);
////		rightRecycler.setLayoutParams(lp);
//	}

    public void setFileMedia(final ArrayList<File> infos) {
		SLIDESHOW = false;
		mListOfMedia = infos;
		sizeMediaFiles = mListOfMedia.size();
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
			fragmentManager = ((FragmentActivity)mContext).getSupportFragmentManager();
		}
        imageViewPagerAdapter = new ScreenSlidePagerAdapter(fragmentManager, viewPager, mListOfMedia, this);
        viewPager.setAdapter(imageViewPagerAdapter);
		thumbnailRecyclerAdapter = new ThumbnailAdapter(mContext, mListOfMedia, thumbnailOnClickListener, thumbnailSize);//mimes, parentPath, 
        thumbnailsRecyclerView.setAdapter(thumbnailRecyclerAdapter);
		if (sizeMediaFiles == 1) {
			thumbnailsRecyclerView.setPadding((getMeasuredWidth() - thumbnailSize) / 2, 0, 0, 0);
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

    public void setThumbnailSize(final int thumbnailSize) {
        this.thumbnailSize = thumbnailSize;
    }

    public void hideThumbnails(final boolean thumbnailsHiddenEnabled) {
        this.hidden = thumbnailsHiddenEnabled;
		if (thumbnailsHiddenEnabled) {
			thumbnailsRecyclerView.setVisibility(GONE);
			infoLayout.setVisibility(GONE);
		} else {
			thumbnailsRecyclerView.setVisibility(VISIBLE);
			infoLayout.setVisibility(VISIBLE);
			postDelayed(runUpdateInfo, 10);
		}
    }

//    private void scrollRecycler(final int mediaPos, final View thumbnail) {
//        Log.d(TAG, "scrollRecycler mediaPos " + mediaPos + ", thumbnailSize " + thumbnailSize + ", paddingLeft " + thumbnailsRecyclerView.getPaddingLeft() + ", paddingRight " + thumbnailsRecyclerView.getPaddingRight());
//		//mLayoutManager.scrollToPositionWithOffset(pos, (getMeasuredWidth() - thumbnailSize) / 2);
//		//int itemLength = thumbnailSize;//thumbnailsRecyclerView.getLayoutManager().getChildAt(0).getMeasuredWidth();
//		int length = infoLayout.getMeasuredWidth();
//		mLayoutManager.scrollToPositionWithOffset(mediaPos, (length - thumbnailSize) / 2);//(mediaPos * thumbnailSize < (length - thumbnailSize) / 2) ? mediaPos * thumbnailSize : (length - thumbnailSize) / 2);
//		if (thumbnail != null) {
//			thumbnail.setBackgroundColor(0xc0ffffff);
//		} else {
//			infoLayout.postDelayed(new Runnable() {
//					@Override
//					public void run() {
//						final View findViewByPosition = mLayoutManager.findViewByPosition(mediaPos);
//						if 	(findViewByPosition != null) {
//							findViewByPosition.setBackgroundColor(0xc0ffffff);
//						}
//					}
//				}, 20);
//		}
//		setupBar(mediaPos);
//    }

    private void scrollRecycler(final int mediaPos, final View thumbnail) {
        Log.d(TAG, "scrollRecycler mediaPos " + mediaPos + ", thumbnailSize " + thumbnailSize + ", paddingLeft " + thumbnailsRecyclerView.getPaddingLeft() + ", paddingRight " + thumbnailsRecyclerView.getPaddingRight());
		//mLayoutManager.scrollToPositionWithOffset(pos, (getMeasuredWidth() - thumbnailSize) / 2);
		//int itemLength = thumbnailSize;//thumbnailsRecyclerView.getLayoutManager().getChildAt(0).getMeasuredWidth();
		int length = getMeasuredWidth();
		mLayoutManager.scrollToPositionWithOffset(mediaPos, Math.min(mediaPos * thumbnailSize, (length - thumbnailSize) / 2));// ? mediaPos * thumbnailSize : (length - thumbnailSize) / 2);
		if (thumbnail != null) {
			thumbnail.setBackgroundColor(0xc0ffffff);
		} else {
			postDelayed(new Runnable() {
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
		if (infoLayout.getVisibility() == VISIBLE) {
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
					Futils.shareFiles(arrayList, (ExplorerActivity)mContext, ((ExplorerActivity)mContext).getAppTheme(), 0x80ffff00);
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
							postDelayed(new Runnable() {
									@Override
									public void run() {
										imageViewPagerAdapter.notifyDataSetChanged();
										thumbnailRecyclerAdapter.notifyDataSetChanged();
									}
								}, 0);
						}
					};
					GeneralDialogCreation.deleteFilesDialog(mContext, //getLayoutElements(),
															(ThemedActivity)mContext, ele, ((ThemedActivity)mContext).getAppTheme(), r);
					break;
				case R.id.slideshowButton:
					if (sizeMediaFiles > 1) {
						SLIDESHOW = true;
						hideThumbnails(true);
						postDelayed(runSlideshow, ImageFragment.curDelay);
					}
					break;
				case R.id.wallpaperButton:
					WallpaperManager myWallpaperManager = WallpaperManager
						.getInstance(mContext);
					try {
						myWallpaperManager.setStream(new FileInputStream(f));
						Toast.makeText(mContext, "Wallpaper successfully changed", Toast.LENGTH_LONG).show();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case R.id.renameButton:
					MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext);
					final String nameOri = f.getName();
					builder.input("", nameOri, false, new MaterialDialog.InputCallback() {
							@Override
							public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {

							}
						});
					builder.theme(((ThemedActivity)mContext).getAppTheme().getMaterialDialogTheme());
					builder.title(((ThemedActivity)mContext).getResources().getString(R.string.rename));
					builder.callback(new MaterialDialog.ButtonCallback() {
							@Override
							public void onPositive(MaterialDialog materialDialog) {
								final String name = materialDialog.getInputEditText().getText().toString().replaceAll("[/?*<>|:\"]", "_");
//							if (rowItem.bf.isSmb())
//								if (rowItem.bf.isDirectory() && !name.endsWith("/"))
//									name = name + "/";

								final String newName = f.getParent() + "/" + name;
								MainActivityHelper.rename(OpenMode.FILE, f.getAbsolutePath(),//mListOfMedia.get(pageSelected).getAbsolutePath(),
														  newName, (ThemedActivity)mContext, ThemedActivity.rootMode);
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
					final int accentColor = ((ThemedActivity)mContext).getColorPreference().getColor(ColorUsage.ACCENT);
					builder.positiveColor(accentColor)
						.negativeColor(accentColor)
						.widgetColor(accentColor);
					builder.build().show();
					break;
				case R.id.copyButton:
					ExplorerActivity activity = ((ExplorerActivity)mContext);
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
					activity = ((ExplorerActivity)mContext);
					activity.COPY_PATH = null;
					ArrayList<BaseFile> copie = new ArrayList<>();
					copie.add(new LayoutElement(f).generateBaseFile());
					((ExplorerActivity)mContext).MOVE_PATH = copie;
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
					AndroidUtils.scanMedia(mContext, f.getAbsolutePath(), false);
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
					setFileMedia(mListOfMedia);
					pageSelected = t;
					postDelayed(runSorting, 20);
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
					setFileMedia(mListOfMedia);
					pageSelected = t;
					postDelayed(runSorting, 20);
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
					setFileMedia(mListOfMedia);
					pageSelected = t;
					postDelayed(runSorting, 20);
					break;
				case R.id.addshortcut:
					AndroidUtils.addShortcut(mContext, f);
					break;
			}
		} else {
			Toast.makeText(mContext, f.getAbsolutePath() + " is not existed", Toast.LENGTH_LONG).show();
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
                TouchImageView image = imageViewPagerAdapter.getCurrentItem().getImage();
				image.setVisibility(View.VISIBLE);
                image.setImageBitmap(bmp);

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



