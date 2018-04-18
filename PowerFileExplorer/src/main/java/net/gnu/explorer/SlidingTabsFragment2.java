//package net.gnu.explorer;
//
//import java.util.ArrayList;
//import android.os.Bundle;
//import android.os.Parcel;
//import android.os.Parcelable;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentPagerAdapter;
//import android.support.v4.view.ViewPager;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import net.gnu.common.view.SlidingHorizontalScroll;
//import android.content.res.*;
//import android.support.v7.widget.*;
//import net.gnu.texteditor.*;
//import android.support.v4.app.*;
//import android.os.*;
//import java.util.*;
//import android.view.animation.*;
//import android.widget.Button;
//import android.graphics.PorterDuff;
//
//public class SlidingTabsFragment2 extends Fragment implements Width {
//
//	private static String TAG = "SlidingTabsFragment2";
//	private FragmentManager fragmentManager;
//	private int pageSelected;
//
//	private SlidingHorizontalScroll mSlidingHorizontalScroll;
//
//	ViewPager mViewPager;
//	PagerAdapter pagerAdapter;
//	private ArrayList<PagerItem2> mTabs = new ArrayList<PagerItem2>();
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//							 Bundle savedInstanceState) {
//		Log.d(TAG, "onCreateView " + savedInstanceState);
//		return inflater.inflate(R.layout.fragment_sample, container, false);
//	}
//
//	@Override
//	public void onViewCreated(final View view, final Bundle savedInstanceState) {
//		Log.d(TAG, "onViewCreated " + this + ", " + savedInstanceState);
//
//		final Bundle args = getArguments();
//		if (savedInstanceState == null) {
//			if (args == null) {
//				initTabs();
//			} else {
//				final int no = args.getInt("no");
//				for (int i = 0; i < no; i++) {
//					final Frag cf = Frag.newInstance(this, 
//													 args.getString("title" + i),
//													 args.getInt("type" + i),
//													 args.getString("path" + i),
//													 args.getBundle("frag" + i));
//					mTabs.add(new PagerItem2(cf));
//				}
//			}
//		} else {
//			initTabs();
//		}
//		mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
//		if (fragmentManager == null)
//			fragmentManager = getChildFragmentManager();
//
//		if (savedInstanceState == null) {
//			Log.d(TAG, "onViewCreated " + mTabs);
//			pagerAdapter = new PagerAdapter(fragmentManager);
//			mViewPager.setAdapter(pagerAdapter);
//			Log.d(TAG, "mViewPager " + mViewPager);
//			
//			if (args != null) {
//				mViewPager.setCurrentItem(args.getInt("pos", 1), true);
////				Frag fr = pagerAdapter.getItem(currentItem);
////				fr.load(args.getString("path" + (currentItem - 1)));
//			} else {
//				mViewPager.setCurrentItem(1);
//			}
//		} else {
//			mTabs.clear();
//			int i = 0;
//			final List<Fragment> fragments = fragmentManager.getFragments();
//			final int total = fragments.size();
//			ProcessFragment proFrag = null;
//			for (Fragment frag : fragments) {
//				if (frag != null) {
//					if (i++ == 1) {
//						proFrag = (ProcessFragment) frag;
//					} else if (i == total) {
//						mTabs.get(0).fakeFrag = (ContentFragment) frag;
//						mTabs.get(0).fakeFrag.currentPathTitle = mTabs.get(0).frag.currentPathTitle;
//					} else {
//						SlidingTabsFragment2.PagerItem2 pagerItem = new PagerItem2(frag);
//						pagerItem.title = savedInstanceState.getString(frag.getTag());
//						pagerItem.path = savedInstanceState.getString(frag.getTag() + "path");
//						((Frag) frag).currentPathTitle = pagerItem.title;
//						((Frag) frag).currentPathTitle = pagerItem.path;
//						mTabs.add(pagerItem);
//					}
//				}
//				Log.d(TAG, "frag " + frag);
//			}
//			mTabs.get(mTabs.size() - 1).fakeFrag = proFrag;
//			
//			pagerAdapter = new PagerAdapter(fragmentManager);
//			mViewPager.setAdapter(pagerAdapter);
//			int pos1 = savedInstanceState.getInt("pos", 0);
//			mViewPager.setCurrentItem(pos1, true);
//		}
//		mViewPager.setOffscreenPageLimit(10);
//		
//		// Give the SlidingTabLayout the ViewPager, this must be done AFTER the
//		// ViewPager has had it's PagerAdapter set.
//		mSlidingHorizontalScroll = (SlidingHorizontalScroll) view.findViewById(R.id.sliding_tabs);
//		//mSlidingHorizontalScroll.fra = SlidingTabsFragment2.this;
//		mSlidingHorizontalScroll.setViewPager(mViewPager);
//		mSlidingHorizontalScroll.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//
//				@Override
//				public void onPageScrolled(int pageSelected, float positionOffset,
//										   int positionOffsetPixel) {
////					Log.e("onPageScrolled", "pageSelected" + pageSelected
////						  + ",positionOffset:" + positionOffset
////						  + ",positionOffsetPixel:" + positionOffsetPixel);
//				}
//
//				@Override
//				public void onPageSelected(int position) {
//					Log.e("onPageSelected", "pageSelected:" + position);
//					pageSelected = position;
//					int size = mTabs.size();
//					if (position == 0) {
//						position = size;
//					} else if (position == size + 1) {
//						position = 1;
//					}
//
//					if (position == size || position == 1) {
//						final PagerItem2 pi2 = mTabs.get(size - position);
//						final Frag frag = pi2.frag;
//						final Frag fakeFrag = pi2.fakeFrag;
//						Log.d(TAG, "frag " + frag + ", fakeFrag " + fakeFrag);
//						if (fakeFrag != null) {
//							fakeFrag.clone(frag);
//							if (fakeFrag.status != null) {
//								fakeFrag.status.setBackgroundColor(ExplorerActivity.IN_DATA_SOURCE_2);
//							}
//							if (frag instanceof FileFrag && ((FileFrag)frag).gridLayoutManager != null) {
//								final int index = ((FileFrag)frag).gridLayoutManager.findFirstVisibleItemPosition();
//								final View vi = ((FileFrag)frag).listView.getChildAt(0); 
//								final int top = (vi == null) ? 0 : vi.getTop();
//								((FileFrag)fakeFrag).gridLayoutManager.scrollToPositionWithOffset(index, top);
//							}
//						}
//					}
//
//					final Frag createFragment = pagerAdapter.getItem(position);
//					createFragment.select(true);
//
//					final ExplorerActivity activity = (ExplorerActivity) getActivity();
//
//					createFragment.horizontalDivider11 = activity.slideFrag.getCurrentFragment().horizontalDivider11;
//					createFragment.leftCommands = activity.slideFrag.getCurrentFragment().leftCommands;
//					Log.d(TAG, "createFragment.leftCommands: " + createFragment.leftCommands);
//					Log.d(TAG, "createFragment.rightCommands: " + createFragment.rightCommands);
//					//if (activity.slideFrag2.pagerAdapter != null && activity.slideFrag2.getContentFragment2(Frag.TYPE.EXPLORER.ordinal()) == createFragment) {
//					createFragment.commands = createFragment.rightCommands;
//					createFragment.right = activity.left;
//					createFragment.left = activity.right;
//					createFragment.horizontalDivider = createFragment.horizontalDivider6;
//					createFragment.width.size = -activity.leftSize;
//
//					if (position == Frag.TYPE.SELECTION.ordinal()) {
//						activity.curContentFrag2 = (ContentFragment2) createFragment;
//						if (activity.curContentFrag2.selectedInList1.size() == 0 && activity.COPY_PATH == null && activity.MOVE_PATH == null) {
//							if (createFragment.rightCommands.getVisibility() == View.VISIBLE) {
//								createFragment.horizontalDivider6.setVisibility(View.GONE);
//								createFragment.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
//								createFragment.rightCommands.setVisibility(View.GONE);
//							}
//						} else if (createFragment.rightCommands.getVisibility() == View.GONE) {
//							createFragment.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
//							createFragment.rightCommands.setVisibility(View.VISIBLE);
//							createFragment.horizontalDivider6.setVisibility(View.VISIBLE);
//						}
//					} else if (position == Frag.TYPE.EXPLORER.ordinal()) {
//						activity.curExploreFrag = (ContentFragment) createFragment;
//						if (activity.curExploreFrag.selectedInList1.size() == 0 && activity.COPY_PATH == null && activity.MOVE_PATH == null) {
//							if (createFragment.rightCommands.getVisibility() == View.VISIBLE) {
//								createFragment.horizontalDivider6.setVisibility(View.GONE);
//								createFragment.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
//								createFragment.rightCommands.setVisibility(View.GONE);
//							}
//						} else if (createFragment.rightCommands.getVisibility() == View.GONE) {
//							createFragment.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
//							createFragment.rightCommands.setVisibility(View.VISIBLE);
//							createFragment.horizontalDivider6.setVisibility(View.VISIBLE);
//							activity.curExploreFrag.updateDelPaste();
//						}
//					} else if (position == Frag.TYPE.APP.ordinal()) {
//						if (((AppsFragment)createFragment).selectedInList1.size() == 0) {
//							if (createFragment.rightCommands.getVisibility() == View.VISIBLE) {
//								createFragment.horizontalDivider6.setVisibility(View.GONE);
//								createFragment.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
//								createFragment.rightCommands.setVisibility(View.GONE);
//							}
//						} else if (createFragment.rightCommands.getVisibility() == View.GONE) {
//							createFragment.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
//							createFragment.rightCommands.setVisibility(View.VISIBLE);
//							createFragment.horizontalDivider6.setVisibility(View.VISIBLE);
//						}
//					} else if (position == Frag.TYPE.PROCESS.ordinal()) {
//						if (((ProcessFragment)createFragment).selectedInList1.size() == 0) {
//							if (createFragment.rightCommands.getVisibility() == View.VISIBLE) {
//								createFragment.horizontalDivider6.setVisibility(View.GONE);
//								createFragment.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
//								createFragment.rightCommands.setVisibility(View.GONE);
//							}
//						} else if (createFragment.rightCommands.getVisibility() == View.GONE) {
//							createFragment.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
//							createFragment.rightCommands.setVisibility(View.VISIBLE);
//							createFragment.horizontalDivider6.setVisibility(View.VISIBLE);
//						}
//					} else if (createFragment.rightCommands != null && createFragment.rightCommands.getVisibility() == View.GONE) {
//						createFragment.rightCommands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
//						createFragment.rightCommands.setVisibility(View.VISIBLE);
//						createFragment.horizontalDivider6.setVisibility(View.VISIBLE);
//					}
//				}
//
//				@Override
//				public void onPageScrollStateChanged(final int state) {
//					Log.d(TAG, "onPageScrollStateChanged state " + state + ", pageSelected " + pageSelected);
//					final int size = mTabs.size();
//					if (state == 0) {
//						if (pageSelected == 0) {
//							mViewPager.setCurrentItem(size, false);
//							pageSelected = size;
//						} else if (pageSelected == size + 1) {
//							mViewPager.setCurrentItem(1, false);
//							pageSelected = 1;
//						}
//					}
//				}
//			});
//		
//		mSlidingHorizontalScroll.setCustomTabColorizer(new SlidingHorizontalScroll.TabColorizer() {
//				@Override
//				public int getIndicatorColor(final int position) {
//					return 0xffff0000;
//				}
//				@Override
//				public int getDividerColor(final int position) {
//					return 0xff888888;
//				}
//			});
//	}
//
//	private void initTabs() {
//		mTabs.add(new PagerItem2("sdcard", Frag.TYPE.EXPLORER.ordinal(), "/sdcard", null));
//
//		mTabs.add(new PagerItem2("Selection", Frag.TYPE.SELECTION.ordinal(), null, null));
//
//		mTabs.add(new PagerItem2("Text", Frag.TYPE.TEXT.ordinal(), null, null));
//
//		mTabs.add(new PagerItem2("Web", Frag.TYPE.WEB.ordinal(), null, null));
//
//		//mTabs.add(new PagerItem2("Reader", Frag.TYPE.FBReader.ordinal(), null, null));
//
//		mTabs.add(new PagerItem2("Pdf", Frag.TYPE.PDF.ordinal(), null, null));
//
//		mTabs.add(new PagerItem2("Photo", Frag.TYPE.PHOTO.ordinal(), null, null));
//
//		mTabs.add(new PagerItem2("Media", Frag.TYPE.MEDIA.ordinal(), null, null));
//
//		mTabs.add(new PagerItem2("Apps", Frag.TYPE.APP.ordinal(), null, null));
//
//		mTabs.add(new PagerItem2("Traffic", Frag.TYPE.TRAFFIC_STATS.ordinal(), null, null));
//
//		mTabs.add(new PagerItem2("Process", Frag.TYPE.PROCESS.ordinal(), null, null));
//
//	}
//
//	void updateLayout(boolean changeTime) {
//		if (changeTime) {
//			for (PagerItem2 pi2 : mTabs) {
//				if (pi2.frag.getContext() != null) {
//					pi2.frag.updateColor(null);
//				}
//				int no = pi2.frag.rightCommands.getChildCount();
//				Button b;
//				for (int i = 0; i < no; i++) {
//					b = (Button) pi2.frag.rightCommands.getChildAt(i);
//					b.setTextColor(ExplorerActivity.TEXT_COLOR);
//					b.getCompoundDrawables()[1].setAlpha(0xff);
//					b.getCompoundDrawables()[1].setColorFilter(ExplorerActivity.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
//				}
//			}
////		} else {
////			((ExploreFragment)mTabs.get(Frag.TYPE.EXPLORER.ordinal() - 1).frag).refreshRecyclerViewLayoutManager(false);
////			((ContentFragment2)mTabs.get(Frag.TYPE.SELECTION.ordinal() - 1).frag).refreshRecyclerViewLayoutManager(false);
//		}
//
//	}
//
////	Bundle saveStates() {
////		Bundle b = new Bundle();
////		final int size = mTabs.size();
////		b.putInt("no", size);
////		for (int i = 0; i < size; i++) {
////			Frag frag = mTabs.get(i).createFragment2();
////			b.putString("title" + i, frag.title);
////			b.putInt("type" + i, frag.type);
////			b.putString("path" + i, frag.path);
////			Bundle bfrag = new Bundle();
////			frag.onSaveInstanceState(bfrag);
////			b.putBundle("frag" + i, bfrag);
////		}
////		b.putInt("pos", mViewPager.getCurrentItem());
////		return b;
////	}
//
//	@Override
//	public void onSaveInstanceState(Bundle outState) {
//		Log.d(TAG, "onSaveInstanceState " + outState + ", " + fragmentManager);
//		super.onSaveInstanceState(outState);
//		try {
//			if (mTabs != null && mTabs.size() > 0) {
//				int i = 0;
//				for (PagerItem2 pi : mTabs) {
//					Log.d(TAG, "onSaveInstanceState pi " + pi);
//					fragmentManager.putFragment(outState, "tabb" + i++, pi.frag);
//					outState.putString(pi.frag.getTag(), pi.frag.currentPathTitle);
//					outState.putString(pi.frag.getTag() + "path", pi.frag.currentPathTitle);
//				}
//				outState.putInt("pos", mViewPager.getCurrentItem());
//			}
//		} catch (Exception e) {
//			// Logger.log(e,"puttingtosavedinstance",getActivity());
//			e.printStackTrace();
//		}
//		Log.d(TAG, "onSaveInstanceState2 " + outState + ", " + fragmentManager);
//	}
//
////	void updateColor() {
////		ExploreFragment exFrag = (ExploreFragment)mTabs.get(0).frag;
////		if (exFrag != null) {
////			exFrag.setDirectoryButtons();
////			exFrag.setAdapter();
////		}
////		mSlidingHorizontalScroll.setViewPager(mViewPager);
////		//mSlidingHorizontalScroll.setBackgroundColor(ExplorerActivity.BASE_BACKGROUND);
////	}
//
//	public void notifyTitleChange() {
//		mSlidingHorizontalScroll.setViewPager(mViewPager);
//	}
//
//	public Frag getCurrentFragment2() {
//		if (mViewPager != null) {
//			final int currentItem = mViewPager.getCurrentItem();
//			Log.d(TAG, "getCurrentFragment2." + currentItem + ", " + this);
//			return pagerAdapter.getItem(currentItem);
//		} else {
//			return null;
//		}
//	}
//
//	public Frag getContentFragment2(final int idx) {
//		Log.d(TAG, "getContentFragment2." + idx + ", " + mTabs + ", " + this);
//		return pagerAdapter.getItem(idx);
//	}
//
//	private class PagerItem2 implements Parcelable {
//		private String title;
//		private int type;
//		Bundle bundle;
//		String path;
//		final String suffix = ".*";
//		final boolean multi = true;
//		private Frag frag;
//		private Frag fakeFrag;
//
//		private static final String TAG = "PagerItem2";
//
//		protected PagerItem2(final Parcel in) {
//			title = in.readString();
//			type = in.readInt();
//		}
//
//		@Override
//		public int describeContents() {
//			return frag.type;
//		}
//
//		@Override
//		public void writeToParcel(final Parcel dest, final int flags) {
//			dest.writeString(title);
//			dest.writeInt(type);
//		}
//
//		@Override
//		public Object clone() {
//			SlidingTabsFragment2.PagerItem2 pagerItem = new PagerItem2(title, type, path, bundle);
//			return pagerItem;
//		}
//
//		PagerItem2(final Fragment frag1) {
//			Log.d(TAG, frag1 + ".");
//			this.frag = (Frag) frag1;
//			title = frag.currentPathTitle;
//			type = frag.type;
//			path = frag.currentPathTitle;
//		}
//
//		PagerItem2(final String title, int type, String path, Bundle bundle) {
//			this.title = title;
//			this.type = type;
//			this.bundle = bundle;
//			this.path = path;
//		}
//
//		Frag createFragment2() {
//			Log.d(TAG, "createFragment2 " + title + ", " + type + ", " + path + frag);
//			if (frag == null) {
//				frag = Frag.newInstance(SlidingTabsFragment2.this, title, type, path, bundle);
//			}
//			return frag;
//		}
//
//		Frag createFakeFragment2() {
//			Log.d(TAG, "createFakeFragment2 " + title + ", " + type + ", " + path + frag);
//			if (frag == null && fakeFrag == null) {
//				fakeFrag = Frag.newInstance(SlidingTabsFragment2.this, title, type, path, bundle);
//			} else if (fakeFrag == null) {
//				fakeFrag = Frag.newInstance(SlidingTabsFragment2.this, frag);
//				//fakeFrag.clone(frag);
//			} else if (fakeFrag != null && frag != null) {
//				fakeFrag.clone(frag);
//			}
//			return fakeFrag;
//		}
//
//		String getTitle() {
//			Log.d(TAG, "getTitle2 " + title + ", " + type + ", " + frag);
//			if (frag == null || frag.currentPathTitle == null || frag.currentPathTitle.length() == 0) {
//				return title;
//			} else {
//				return frag.currentPathTitle;
//			}
//		}
//
//		@Override
//		public String toString() {
//			return title + ", " + frag;
//		}
//	}
//
//	public class PagerAdapter extends FragmentPagerAdapter {
//
//		PagerAdapter(final FragmentManager fm) {
//			super(fm);
//		}
//
//		@Override
//		public Frag getItem(final int position) {
//			Log.d(TAG, "getItem2 " + position);
//			final int size = mTabs.size();
//			if (size > 1) {
//				if (position == 0) {
//					return mTabs.get(size - 1).createFakeFragment2();
//				} else if (position == size + 1) {
//					return mTabs.get(0).createFakeFragment2();
//				} else {
//					return mTabs.get(position - 1).createFragment2();
//				}
//			} else {
//				return mTabs.get(position).createFragment2();
//			}
//		}
//
//		@Override
//		public int getCount() {
//			final int size = mTabs.size();
//			if (size > 1) {
//				return size + 2;
//			} else {
//				return size;
//			}
//		}
//
//		@Override
//		public CharSequence getPageTitle(final int position) {
//			final int size = mTabs.size();
//			if (size > 1) {
//				if (position == 0) {
//					return "";
//				} else if (position == size + 1) {
//					return "";
//				} else {
//					return mTabs.get(position - 1).getTitle();
//				}
//			} else {
//				return mTabs.get(position).getTitle();
//			}
//		}
//
//		@Override
//		public int getItemPosition(final Object object) {
//			for (PagerItem2 pi : mTabs) {
//				if (pi.frag == object) {
//					//Log.d(TAG, "getItemPosition POSITION_UNCHANGED" + ", " + object);
//					return POSITION_UNCHANGED;
//				}
//			}
//			//Log.d(TAG, "getItemPosition POSITION_NONE" + ", " + object);
//			return POSITION_NONE;
//		}
//	}
//}
