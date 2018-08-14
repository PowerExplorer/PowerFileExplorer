package net.gnu.explorer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;
import android.view.View.OnClickListener;
import java.io.File;
import android.util.Log;
import net.gnu.util.Util;
import android.support.v4.view.ViewPager;
import java.util.TreeMap;
import android.view.View;
import android.view.GestureDetector;
import com.ortiz.touch.TouchImageView;
import android.net.Uri;

public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter implements Runnable {

    private static final String TAG = "ScreenSlidePagerAdapter";

	private final List<Uri> mListOfMedia;
	private final ViewPager viewPager;
	TreeMap<Integer, ImageFragment> fragMap = new TreeMap<>();
	final PhotoFragment photoFragment;
	static int numOfPages = 1;

    public ScreenSlidePagerAdapter(final FragmentManager fm, 
								   final ViewPager vp,
								   final List<Uri> listOfMedia, 
								   final PhotoFragment photoFragment
								   ) {
        super(fm);
		this.viewPager = vp;
        this.mListOfMedia = listOfMedia;
		this.photoFragment = photoFragment;
    }

	@Override
    public Fragment getItem(final int pagerPos) {
		ImageFragment fragment = null;
        final int size = mListOfMedia.size();
		int mediaPos = pagerPos;
		if (mediaPos == 0) {
			mediaPos = size - 1;
		} else if (mediaPos == size + 1) {
			mediaPos = 0;
		} else {
			mediaPos--;
		}
        Log.d(TAG, "getItem pagerPos " + pagerPos + ", mListOfMedia.get(" + mediaPos + ") " + mListOfMedia.get(mediaPos) + ", viewPager.getCurrentItem() " + viewPager.getCurrentItem() + ", size " + size + ", " + fragMap);
		//if (mediaPos < size) {
            fragment = loadImageFragment(mListOfMedia.get(mediaPos));//, mimes.get(position)new File(parentPath, 
			fragMap.put(Integer.valueOf(pagerPos), fragment);
        //}
		return fragment;
    }

    private ImageFragment loadImageFragment(final Uri mediaInfo) {//, final String mime
		Log.d(TAG, "loadImageFragment " + mediaInfo);
        final ImageFragment fragment = new ImageFragment();
        fragment.setMediaInfo(mediaInfo);
		fragment.setOnDoubleTapListener(photoFragment);
		fragment.setPostScaleCallback(this);
        return fragment;
    }

	@Override
	public void run() {
		final int currentItem = viewPager.getCurrentItem();
		final int count = getCount();
		if (count > 1) {
			ImageFragment img;
			if (currentItem == 0) {
				for (int i = currentItem; i < Math.min(count - 2, currentItem + numOfPages); i++) {
					Log.d(TAG, "setZoom " + (i+1));
					img = fragMap.get(i + 1);
					if (img != null) {
						final TouchImageView image = img.getImage();
						image.setZoom(ImageFragment.curZoom);
					}
				}
//				final TouchImageView image = fragMap.get(currentItem + 1).getImage();
//				image.setZoom(ImageFragment.curZoom);
			} else if (currentItem == count - 1) {
				for (int i = currentItem; i > Math.max(0, count - 1 - numOfPages); i--) {
					Log.d(TAG, "setZoom " + (i-1));
					img = fragMap.get(i - 1);
					if (img != null) {
						final TouchImageView image = img.getImage();
						image.setZoom(ImageFragment.curZoom);
					}
				}
//				final TouchImageView image = fragMap.get(currentItem - 1).getImage();
//				image.setZoom(ImageFragment.curZoom);
			} else {
				for (int i = Math.max(0, currentItem - numOfPages/2-1); i < Math.min(count - 2, currentItem+numOfPages/2+1); i++) {
					Log.d(TAG, "setZoom " + i);
					img = fragMap.get(i);
					if (img != null) {
						final TouchImageView image = img.getImage();
						image.setZoom(ImageFragment.curZoom);
					}
				}
//				TouchImageView image = fragMap.get(currentItem - 1).getImage();
//				image.setZoom(ImageFragment.curZoom);
//				image = fragMap.get(currentItem + 1).getImage();
//				image.setZoom(ImageFragment.curZoom);
			}
		}
	}

	@Override
    public float getPageWidth(int position) {
		return 1f/numOfPages;
    }

    @Override
    public int getCount() {
		final int size = mListOfMedia.size();
		if(size == 1) {
			return 1;
		} else {
			return size + 2;
		}
    }
}
