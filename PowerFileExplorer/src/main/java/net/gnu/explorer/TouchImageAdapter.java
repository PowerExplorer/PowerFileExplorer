//package net.gnu.explorer;
//import android.support.v4.view.*;
//import android.support.annotation.*;
//import com.ortiz.touchview.*;
//import android.view.*;
//import android.widget.*;
//import java.util.*;
//import android.net.*;
//import android.util.*;
//
//public class TouchImageAdapter extends PagerAdapter {
//
//	private static final String TAG = "TouchImageAdapter";
//
//	private final List<Uri> mListOfMedia;
//	private final ViewPager viewPager;
//	
//	private final PhotoFragment photoFragment;
//	private int numOfPages = 1;
//
//    public TouchImageAdapter(final ViewPager vp,
//								   final List<Uri> listOfMedia, 
//								   final PhotoFragment photoFragment
//								   ) {
//        super();
//		this.viewPager = vp;
//        this.mListOfMedia = listOfMedia;
//		this.photoFragment = photoFragment;
//    }
//
//	
//	@NonNull
//	@Override
//	public View instantiateItem(@NonNull ViewGroup container, int position) {
//		
//		final TouchImageView img = new TouchImageView(container.getContext());
//		final int size = mListOfMedia.size();
//		int mediaPos = position;
//		if (mediaPos == 0) {
//			mediaPos = size - 1;
//		} else if (mediaPos == size + 1) {
//			mediaPos = 0;
//		} else {
//			mediaPos--;
//		}
//        Log.d(TAG, "instantiateItem position " + position + ", ViewGroup " + container + ", mListOfMedia.get(" + mediaPos + ") " + mListOfMedia.get(mediaPos) + ", viewPager.getCurrentItem() " + viewPager.getCurrentItem() + ", size " + size);
//		
//		img.setImageURI(mListOfMedia.get(mediaPos));
//		img.setOnDoubleTapListener(photoFragment);
//		container.addView(img, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//		return img;
//	}
//
//	@Override
//	public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
//		container.removeView((View) object);
//	}
//
//	@Override
//	public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
//		return view == object;
//	}
//
//	@Override
//    public float getPageWidth(int position) {
//		return 1f/numOfPages;
//    }
//
//    @Override
//    public int getCount() {
//		final int size = mListOfMedia.size();
//		if(size == 1) {
//			return 1;
//		} else {
//			return size + 2;
//		}
//    }
//}
