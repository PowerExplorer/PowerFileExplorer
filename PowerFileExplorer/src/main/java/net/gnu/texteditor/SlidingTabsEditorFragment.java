//package net.gnu.texteditor;
//
//import net.gnu.common.view.SlidingHorizontalScroll;
//
//import android.graphics.Color;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentPagerAdapter;
//import android.support.v4.view.ViewPager;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.os.Bundle;
//import android.os.Parcelable;
//import android.support.v4.view.PagerAdapter;
//import android.util.Log;
//import android.view.View;
//import android.view.ViewGroup;
//
//import java.util.ArrayList;
//import java.util.List;
//import net.gnu.explorer.R;
//
//import android.content.res.*;
//import android.os.*;
//import java.io.*;
//
//import android.content.Intent;
//import android.util.Log;
//import android.support.v4.app.FragmentTransaction;
//import java.util.ArrayList;
//import net.gnu.explorer.*;
//
///**
// * A basic sample which shows how to use {@link com.free.common.view.SlidingTabLayout}
// * to display a custom {@link ViewPager} title strip which gives continuous feedback to the user
// * when scrolling.
// */
//public class SlidingTabsEditorFragment extends Fragment implements TabAction {
//
//	private static final String TAG = "SlidingTabsEditorFragment";
//
//    /**
//     * A custom {@link ViewPager} title strip which looks much like Tabs present in Android v4.0 and
//     * above, but is designed to give continuous feedback to the user when scrolling.
//     */
//    private SlidingHorizontalScroll mSlidingHorizontalScroll;
//
//    /**
//     * A {@link ViewPager} which will be used in conjunction with the {@link SlidingTabLayout} above.
//     */
//    private ViewPager mViewPager;
//	private FragmentManager childFragmentManager;
//	public PagerAdapter pagerAdapter;
//	private int pageSelected;
//	
//    /**
//     * List of {@link SamplePagerItem} which represent this sample's tabs.
//     */
//    private List<PagerItem> mTabs = new ArrayList<PagerItem>();
//
//	public void addTab() {
//		addTab(null, null);
//	}
//	
//	public void addTab(final Intent intent, String title) {//}, final String charset, final boolean changed, final int linebreak) {
//		if (title == null || title.length() == 0) {
//			title = "Untitled " + ++Main.no + ".txt";
//		}
//		Log.d(TAG, "addTab1 pagerAdapter=" + pagerAdapter + ", filename=" + title + ", mTabs=" + mTabs);
//		final PagerItem pagerItem = new PagerItem(title);//, charset, changed, linebreak);
//		final Main main = pagerItem.createFrag(intent, title);
//		main.slidingTabsFragment = this;
//		
//		mTabs.add(pagerItem);
//		if (mViewPager != null) {
//			pagerAdapter.notifyDataSetChanged();
//			mViewPager.setCurrentItem(pagerAdapter.getCount() - 1);
//			notifyTitleChange();
//			//main.onPrepareOptionsMenu(((MainActivity)getActivity()).menu);
//		}
//		Log.d(TAG, "addTab2 " + title + ", " + mTabs);
//	}
//
//	void closeTab(Main m) {
//		Log.d(TAG, "closeTab " + m + ", " + mTabs);
//
//		int i = 0;
//		final ArrayList<PagerItem> mTabs2 = new ArrayList<PagerItem>(mTabs);
//		for (PagerItem pi : mTabs) {
//			if (pi.frag == m) {
//				Log.d(TAG, "closeTab " + i);
//				break;
//			}
//			i++;
//		}
//		final FragmentTransaction ft = childFragmentManager.beginTransaction();
//		for (int j = mTabs2.size() - 1; j >= i; j--) {
//			ft.remove(mTabs.remove(j).frag);
//		}
//		pagerAdapter.notifyDataSetChanged();
//		ft.commitNow();
//
//		mTabs2.remove(i);
//		for (int j = i; j < mTabs2.size(); j++) {
//			mTabs.add(mTabs2.get(j));
//		}
//		
//		pagerAdapter.notifyDataSetChanged();
//		mViewPager.setCurrentItem(i < mTabs.size() ? i : --i);
//		notifyTitleChange();
//	}
//
//	public void closeCurTab() {
//		final Main main = getCurFrag();
//		Main.saved = 0;
//		Main.count = mTabs.size();
//		Log.d(TAG, "closeCurTab " + main);
//		main.confirmSave(main.mProcQuit);
//	}
//
//	public void closeOtherTabs() {
//		final Main cur = getCurFrag();
//		Main.saved = 0;
//		Main.count = mTabs.size();
//		Log.d(TAG, "closeOtherTabs " + cur);
//		for (int i = mTabs.size() - 1; i >= 0; i--) {
//			final Main m = pagerAdapter.getItem(i);
//			if (m != cur) {
//				m.confirmSave(m.mProcQuit);
//				Log.d(TAG, "closeOtherTabs2 " + m);
//			}
//		}
//	}
//	
//	public int size() {
//		return mTabs.size();
//	}
//	
//	void updateTitle(Main m, String name) {
//		Log.d(TAG, "updateTitle " + name + ", " + mTabs);
//		for (PagerItem pi : mTabs) {
//			if (pi.frag == m) {
//				//Log.d(TAG, "updateTitle " + pi.title + ", " + name);
//				pi.title = name;
//				break;
//			}
//		}
//		notifyTitleChange();
//	}
//
//	/**
//	 * This class represents a tab to be displayed by {@link ViewPager} and it's
//	 * associated {@link SlidingTabLayout}.
//	 */
//	static class PagerItem implements Parcelable {
//		private String title = null;
//		private Main frag;
//
//		private static final String TAG = "PagerItem";
//
//		protected PagerItem(Parcel in) {
//			title = in.readString();
//		}
//
//		@Override
//		public int describeContents() {
//			return 0;
//		}
//
//		@Override
//		public void writeToParcel(Parcel dest, int flags) {
//			dest.writeString(title);
//		}
//
//		public static final Parcelable.Creator<PagerItem> CREATOR = new Parcelable.Creator<PagerItem>() {
//			public PagerItem createFromParcel(Parcel in) {
//				return new PagerItem(in);
//			}
//
//			public PagerItem[] newArray(int size) {
//				return new PagerItem[size];
//			}
//		};
//
//		@Override
//		public Object clone() {
//			final PagerItem pagerItem = new PagerItem(title);
//			return pagerItem;
//		}
//
//		PagerItem(final Fragment frag1) {
//			Log.d(TAG, frag1 + ".");
//			this.frag = (Main) frag1;
//		}
//
//		PagerItem(final String title) {
//			this.title = title;
//		}
//
//		/**
//		 * @return A new {@link Fragment} to be displayed by a {@link ViewPager}
//		 */
//		private Main createFrag(Intent intent, String title) {
//			Log.d(TAG, "createFrag() " + title + ", " + frag);
//			if (frag == null) {
//				frag = Main.newInstance(intent, title, null);
//			}
//			return frag;
//		}
//
//		private Main getFrag() {
//			Log.d(TAG, "getFrag() " + frag);
//			if (frag == null) {
//				frag = Main.newInstance(null, title, null);
//			}
//			return frag;
//		}
//
//		/**
//		 * @return the title which represents this tab. In this sample this is
//		 *         used directly by
//		 *         {@link android.support.v4.view.PagerAdapter#getPageTitle(int)}
//		 */
//		private String getTitle() {
//			//if (title != null || (frag == null || frag.mInstanceState.filename == null || frag.mInstanceState.filename.length() == 0)) {
//				return title;
//			//} else {
//			//	return new File(frag.mInstanceState.filename).getName();
//			//}
//		}
//
//		@Override
//		public String toString() {
//			return frag + ".";
//		}
//	}
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        Log.d(TAG, "onCreate " + savedInstanceState);
//        super.onCreate(savedInstanceState);
//    }
//
//    /**
//     * Inflates the {@link View} which will be displayed by this {@link Fragment}, from the app's
//     * resources.
//     */
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//							 Bundle savedInstanceState) {
//		Log.d(TAG, "onCreateView " + savedInstanceState);
//        return inflater.inflate(R.layout.fragment_sample, container, false);
//    }
//
//    // BEGIN_INCLUDE (fragment_onviewcreated)
//    /**
//     * This is called after the {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has finished.
//     * Here we can pick out the {@link View}s we need to configure from the content view.
//     *
//     * We set the {@link ViewPager}'s adapter to be an instance of
//     * {@link SampleFragmentPagerAdapter}. The {@link SlidingTabLayout} is then given the
//     * {@link ViewPager} so that it can populate itself.
//     *
//     * @param view View created in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
//     */
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        // BEGIN_INCLUDE (setup_viewpager)
//        // Get the ViewPager and set it's PagerAdapter so that it can display items
//        Log.d(TAG, "onViewCreated.savedInstanceState=" + savedInstanceState);
//        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
//        childFragmentManager = getChildFragmentManager();
//		
//		Log.d(TAG, "onViewCreated.mTabs=" + mTabs);
//		if (savedInstanceState == null) {
//			pagerAdapter = new PagerAdapter(childFragmentManager);
//			mViewPager.setAdapter(pagerAdapter);
//			Log.d(TAG, "mViewPager " + mViewPager);
//		} else {
//			mTabs.clear();
//			for (Fragment frag : childFragmentManager.getFragments()) {
//				if (frag != null) {
//					PagerItem pagerItem = new PagerItem(frag);
//					((Main)frag).slidingTabsFragment = this;
//					pagerItem.title = savedInstanceState.getString(frag.getTag());
//
//					mTabs.add(pagerItem);
//				}
//
//				Log.d(TAG, "frag " + frag);
//			}
//			// Get the ViewPager and set it's PagerAdapter so that it can
//			// display items
//			pagerAdapter = new PagerAdapter(childFragmentManager);
//			mViewPager.setAdapter(pagerAdapter);
//			int pos1 = savedInstanceState.getInt("pos", 0);
//			mViewPager.setCurrentItem(pos1);
//		}
//		mViewPager.setOffscreenPageLimit(8);
//		//childFragmentManager.enableDebugLogging(true);
//
//		// BEGIN_INCLUDE (setup_slidingtablayout)
//		// Give the SlidingTabLayout the ViewPager, this must be done AFTER the
//		// ViewPager has had
//		// it's PagerAdapter set.
//		mSlidingHorizontalScroll = (SlidingHorizontalScroll) view.findViewById(R.id.sliding_tabs);
//		mSlidingHorizontalScroll.fra = SlidingTabsEditorFragment.this;
//		tabClicks = new TabClicks(16);
//		
//		mSlidingHorizontalScroll.setViewPager(mViewPager);
//		mSlidingHorizontalScroll.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//				@Override
//				public void onPageScrolled(int p1, float p2, int p3) {
//				}
//
//				@Override
//				public void onPageSelected(int position) {
//					Log.d(TAG, "onPageSelected pos " + position);
//					TextEditorActivity activity = (TextEditorActivity)getActivity();
//					//activity.main.onPrepareOptionsMenu(activity.menu);
//					int size = mTabs.size();
//					final int newpos = position == 0 ? (size - 1) : position == (size + 1) ? 0 : (position - 1);
//					activity.main = pagerAdapter.getItem(newpos);
//					pageSelected = position;
//				}
//				
//				@Override 
//				public void onPageScrollStateChanged(int state) {
//					Log.d(TAG, "onPageScrollStateChanged state " + state + ", pageSelected " + pageSelected);
//					int size = mTabs.size();
//					if (state == 0) {
//						if (pageSelected == 0) {
//							mViewPager.setCurrentItem(size, true);
//							pageSelected = size;
//						} else if (pageSelected == size + 1) {
//							mViewPager.setCurrentItem(1, true);
//							pageSelected = 1;
//						}
//					}
//				}
//			});
//			
//		Log.d(TAG, "mSlidingHorizontalScroll " + mSlidingHorizontalScroll);
//		
//		// BEGIN_INCLUDE (tab_colorizer)
//		// Set a TabColorizer to customize the indicator and divider colors.
//		// Here we just retrieve
//		// the tab at the position, and return it's set color
//		mSlidingHorizontalScroll
//			.setCustomTabColorizer(new SlidingHorizontalScroll.TabColorizer() {
//				@Override
//				public int getIndicatorColor(int position) {
//					return 0xffff0000;
//				}
//
//				@Override
//				public int getDividerColor(int position) {
//					return 0xff888888;
//				}
//
//			});
//		// END_INCLUDE (tab_colorizer)
//		// END_INCLUDE (setup_slidingtablayout)
//    }
//    // END_INCLUDE (fragment_onviewcreated)
//
//	@Override
//	public void onSaveInstanceState(Bundle outState) {
//		Log.d(TAG, "onSaveInstanceState " + outState + ", " + childFragmentManager);
//		super.onSaveInstanceState(outState);
//		try {
//			// int i = 0;
//			// if(Sp!=null)
//			// Sp.edit().putInt(PreferenceUtils.KEY_CURRENT_TAB,
//			// MainActivity.currentTab).commit();
//			// Log.d(TAG, "fragmentManager.getFragments() " +
//			// fragmentManager.getFragments());
//			if (mTabs != null && mTabs.size() > 0) {
//				int i = 0;
//				for (PagerItem pi : mTabs) {
//					Log.d(TAG, "onSaveInstanceState pi " + pi);
//					childFragmentManager.putFragment(outState, "tabb" + i++, pi.frag);
//					outState.putString(pi.frag.getTag(), pi.title);
//				}
//				outState.putInt("pos", mViewPager.getCurrentItem());
//			}
//		} catch (Exception e) {
//			// Logger.log(e,"puttingtosavedinstance",getActivity());
//			e.printStackTrace();
//		}
//		Log.d(TAG, "onSaveInstanceState2 " + outState + ", " + childFragmentManager);
//	}
//	
//	public boolean circular() {
//		return false;
//	}
//
////	@Override
////	public void onActivityCreated(Bundle savedInstanceState) {
////		Log.d(TAG, "onActivityCreated.savedInstanceState=" + savedInstanceState);
////		super.onActivityCreated(savedInstanceState);
////	}
//
////	@Override
////	public void onAttachFragment(Fragment childFragment) {
////		Log.d(TAG, "onAttachFragment.childFragment=" + childFragment);
////		super.onAttachFragment(childFragment);
////	}
//
////	@Override
////	public void onConfigurationChanged(Configuration newConfig) {
////		Log.d(TAG, "onConfigurationChanged.newConfig=" + newConfig);
////		super.onConfigurationChanged(newConfig);
////	}
//
////	@Override
////	public void onViewStateRestored(Bundle savedInstanceState) {
////		Log.d(TAG, "onViewStateRestored.savedInstanceState=" + savedInstanceState);
////		super.onViewStateRestored(savedInstanceState);
////	}
//
////	@Override
////	public void onStart() {
////		Log.d(TAG, "onStart");
////		super.onStart();
////	}
//
////	@Override
////	public void onStop() {
////		Log.d(TAG, "onStop " + mViewPager + ", " + mSlidingHorizontalScroll);
////		super.onStop();
////	}
//
////	@Override
////	public void onPause() {
////		Log.d(TAG, "onPause ");
////		super.onPause();
////	}
//
////	@Override
////	public void onDestroyView() {
////		Log.d(TAG, "onDestroyView");
////		super.onDestroyView();
////	}
//
////	@Override
////	public void onDestroy() {
////		Log.d(TAG, "onDestroy");
////		super.onDestroy();
////	}
//
////	@Override
////    public void onResume() {
////        Log.d(TAG, "onResume " + mViewPager + ", act=" + getActivity());
////		super.onResume();
////    }
//
//	public void notifyTitleChange() {
//		mSlidingHorizontalScroll.setViewPager(mViewPager);
//	}
//
//	public int getCurrentItem() {
//		return mViewPager.getCurrentItem();
//	}
//
//	public Main getCurFrag() {
//		final int currentItem = mViewPager.getCurrentItem();
//		Log.d(TAG, "getCurFrag()=" + currentItem + ", " + this);
//		return mTabs.get(currentItem).getFrag();
//	}
//
//    /**
//     * The {@link FragmentPagerAdapter} used to display pages in this sample. The individual pages
//     * are instances of {@link ContentFragment} which just display three lines of text. Each page is
//     * created by the relevant {@link SamplePagerItem} for the requested position.
//     * <p>
//     * The important section of this class is the {@link #getPageTitle(int)} method which controls
//     * what is displayed in the {@link SlidingTabLayout}.
//     */
//    public class PagerAdapter extends FragmentPagerAdapter {
//
//		private static final String TAG = "PagerAdapter";
//		int numOfPages = 1;
//		
//        PagerAdapter(FragmentManager fm) {
//            super(fm);
//        }
//
//        /**
//         * Return the {@link android.support.v4.app.Fragment} to be displayed at {@code position}.
//         * <p>
//         * Here we return the value returned from {@link SamplePagerItem#createFragment()}.
//         */
//        @Override
//        public Main getItem(int positionOri) {
//			Log.d(TAG, "getItem " + positionOri);
//            final int size = mTabs.size();
//			int position = positionOri;
//			if (position == 0) {
//				position = size - 1;
//			} else if (position == size + 1) {
//				position = 0;
//			} else {
//				position--;
//			}
//			return mTabs.get(position).getFrag();
//        }
//
//        @Override
//        public int getCount() {
//			final int size = mTabs.size();
//			if (size == 1) {
//				return 1;
//			} else {
//				return size + 2;
//			}
//        }
//
//        @Override
//		public float getPageWidth(int position) {
//			return 1f/numOfPages;
//		}
//
//		// BEGIN_INCLUDE (pageradapter_getpagetitle)
//        /**
//         * Return the title of the item at {@code position}. This is important as what this method
//         * returns is what is displayed in the {@link SlidingTabLayout}.
//         * <p>
//         * Here we return the value returned from {@link SamplePagerItem#getTitle()}.
//         */
//        @Override
//        public CharSequence getPageTitle(final int positionOri) {
//			final int size = mTabs.size();
//			int position = positionOri;
//			if (position == 0) {
//				position = size - 1;
//			} else if (position == size + 1) {
//				position = 0;
//			} else {
//				position--;
//			}
//			final PagerItem pi = mTabs.get(position);
//			final String title = pi.getTitle();
//			Log.d(TAG, "getPageTitle " + positionOri + ", " + pi.frag);
//            return title;
//        }
//        // END_INCLUDE (pageradapter_getpagetitle)
//
//		@Override
//		public int getItemPosition(final Object object) {
//			for (PagerItem pi : mTabs) {
//				if (pi.frag == object) {
//					Log.d(TAG, "getItemPosition POSITION_UNCHANGED" + ", " + object);
//					return POSITION_UNCHANGED;
//				}
//			}
//			Log.d(TAG, "getItemPosition POSITION_NONE" + ", " + object);
//			return POSITION_NONE;
//		}
//    }
//}
