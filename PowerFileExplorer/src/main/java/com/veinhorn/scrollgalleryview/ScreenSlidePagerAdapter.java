package com.veinhorn.scrollgalleryview;

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
import java.util.*;
import net.gnu.util.*;
import net.gnu.explorer.*;

public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {//}implements Runnable {

    private static final String TAG = "ScreenSlidePagerAdapter";

	private final ArrayList<File> mListOfMedia;
	private final ViewPager viewPager;
	private final List<ComparableEntry<Integer, ImageFragment>> fragMap = new ArrayList<>(3);
	private final PhotoFragment onDoubleTapListener;
	private int numOfPages = 1;
	private final int sizeMediaFiles;
	
    public ScreenSlidePagerAdapter(final FragmentManager fm, 
								   final ViewPager vp,
								   final ArrayList<File> listOfMedia, 
								   final PhotoFragment onDoubleTapListener
								   ) {
        super(fm);
		this.viewPager = vp;
        this.mListOfMedia = listOfMedia;
		this.onDoubleTapListener = onDoubleTapListener;
		sizeMediaFiles = mListOfMedia.size();
		Log.i(TAG, "sizeMediaFiles " + sizeMediaFiles + ", this " + this + ", photoFrag " + onDoubleTapListener);
    }

	@Override
    public Fragment getItem(final int pagerPos) {
		final int mediaPos = pagerPos == 0 ? (sizeMediaFiles - 1) : pagerPos == (sizeMediaFiles + 1) ? 0 : (pagerPos - 1);
		Log.d(TAG, "getItem pagerPos " + pagerPos + ", mediaPos " + mediaPos + ", sizeMediaFiles " + sizeMediaFiles + ", this " + this + ", viewPager.getCurrentItem() " + viewPager.getCurrentItem() + ", photoFrag " + onDoubleTapListener);
		final ImageFragment curFrag = loadImageFragment(mListOfMedia.get(mediaPos));
		if (fragMap.size() >= 3) {
			fragMap.remove(0);
		}
        fragMap.add(new ComparableEntry<Integer, ImageFragment>(Integer.valueOf(pagerPos), curFrag));
		return curFrag;
    }

	private ImageFragment loadImageFragment(final File mediaInfo) {
        final ImageFragment fragment = new ImageFragment();
        fragment.setMediaInfo(mediaInfo);
		fragment.setPhotoFragment(onDoubleTapListener);
		//fragment.setCallback(this);
        return fragment;
    }
	
	public ImageFragment getCurrentItem() {
		final int pagerPos = viewPager.getCurrentItem();
		for (ComparableEntry<Integer, ImageFragment> e : fragMap) {
			//Log.d(TAG, "getCurrentItem pagerPos " + pagerPos + ", key " + e.getKey() + ", viewPager.getCurrentItem() " + viewPager.getCurrentItem());
			if (e.getKey().intValue() == pagerPos) {
				return e.getValue();
			}
		}
		return null;
	}

//	@Override
//	public void run() {
//		final int currentItem = viewPager.getCurrentItem();
//		final int count = getCount();
//		if (count > 1) {
//			ImageFragment img;
//			if (currentItem == 0) {
//				for (int i = currentItem; i < Math.min(count - 2, numOfPages); i++) {
//					Log.d(TAG, "setZoom " + (i+1));
//					img = fragMap.get(i + 1);
//					if (img != null) {
//						final TouchImageView image = img.getImage();
//						image.setZoom(ImageFragment.curZoom);
//					}
//				}
////				final TouchImageView image = fragMap.get(currentItem + 1).getImage();
////				image.setZoom(ImageFragment.curZoom);
//			} else if (currentItem == count - 1) {
//				for (int i = currentItem; i > Math.max(0, count - 1 - numOfPages); i--) {
//					Log.d(TAG, "setZoom " + (i-1));
//					img = fragMap.get(i - 1);
//					if (img != null) {
//						final TouchImageView image = img.getImage();
//						image.setZoom(ImageFragment.curZoom);
//					}
//				}
////				final TouchImageView image = fragMap.get(currentItem - 1).getImage();
////				image.setZoom(ImageFragment.curZoom);
//			} else {
//				for (int i = Math.max(0, currentItem - numOfPages/2-1); i < Math.min(count - 2, currentItem+numOfPages/2+1); i++) {
//					Log.d(TAG, "setZoom " + i);
//					img = fragMap.get(i);
//					if (img != null) {
//						final TouchImageView image = img.getImage();
//						image.setZoom(ImageFragment.curZoom);
//					}
//				}
////				TouchImageView image = fragMap.get(currentItem - 1).getImage();
////				image.setZoom(ImageFragment.curZoom);
////				image = fragMap.get(currentItem + 1).getImage();
////				image.setZoom(ImageFragment.curZoom);
//			}
//		}
//	}
	
	@Override
    public float getPageWidth(int position) {
		return 1f/numOfPages;
    }
	
    @Override
    public int getCount() {
		if(sizeMediaFiles == 1) {
			return 1;
		} else {
			return sizeMediaFiles + 2;
		}
    }
}
