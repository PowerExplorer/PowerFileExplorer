package net.gnu.explorer;

import java.io.File;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.gnu.common.view.SlidingHorizontalScroll;
import android.support.v7.widget.*;
import android.support.v4.app.*;
import net.gnu.texteditor.*;
import net.gnu.explorer.SlidingTabsFragment.*;
import android.os.*;
import java.util.*;
import android.view.animation.*;
import android.content.Intent;
import android.widget.Button;
import android.graphics.PorterDuff;
import android.app.Activity;
import net.gnu.util.Util;
import com.amaze.filemanager.utils.OpenMode;
import android.widget.ImageView;
import android.view.ViewGroup.LayoutParams;
import android.support.v7.app.*;
import net.gnu.common.*;

public class SlidingTabsFragment extends Fragment implements TabAction {

	private static final String TAG = "SlidingTabsFragment";
	private FragmentManager childFragmentManager;

	private SlidingHorizontalScroll mSlidingHorizontalScroll;

	private ViewPager mViewPager;
	public PagerAdapter pagerAdapter;
	int pageSelected = 1;

	private ArrayList<PagerItem> mTabs = new ArrayList<PagerItem>();
	public static final enum Side {LEFT, RIGHT, MONO};
	Side side;// = Side.LEFT;
	int width;
	
	public static SlidingTabsFragment newInstance(Side side) {
		final SlidingTabsFragment s = new SlidingTabsFragment();
		s.side = side;
		return s;
	}

	@Override
	public String toString() {
		return side + ", pageSelected=" + pageSelected + ", width=" + width;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
							 final Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		setRetainInstance(true);
		Log.d(TAG, "onCreateView " + savedInstanceState);
		return inflater.inflate(R.layout.fragment_sample, container, false);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
		// Give the SlidingTabLayout the ViewPager, this must be done AFTER the
		// ViewPager has had it's PagerAdapter set.
		mSlidingHorizontalScroll = (SlidingHorizontalScroll) view.findViewById(R.id.sliding_tabs);
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		childFragmentManager = getChildFragmentManager();
		final Activity activ = getActivity();
		activ.getWindow().getDecorView().setBackgroundColor(Constants.BASE_BACKGROUND_LIGHT);
		final Bundle args = getArguments();
		Log.d(TAG, "onActivityCreated side " + side + ", args " + args + ", savedInstanceState " + savedInstanceState);
		
		if (savedInstanceState == null) {
			if (args == null) {
				//initContentFragmentTabs();
			} else {
				final int no = args.getInt("no");
				for (int i = 0; i < no; i++) {
					Log.d(TAG, no + " onActivityCreated args.getString(Constants.EXTRA_DIR_PATH" + i + ")=" + args.getString(Constants.EXTRA_ABSOLUTE_PATH + i));
					ContentFragment cf = ContentFragment.newInstance(SlidingTabsFragment.this, 
																	 args.getString(Constants.EXTRA_ABSOLUTE_PATH + i),
																	 args.getString(Constants.EXTRA_FILTER_FILETYPE + i),
																	 args.getString(Constants.EXTRA_FILTER_MIMETYPE + i),
																	 args.getBoolean(Constants.EXTRA_MULTI_SELECT + i),
																	 args.getBundle("frag" + i));
					mTabs.add(new PagerItem(cf));
				}
			}

			pagerAdapter = new PagerAdapter(childFragmentManager);
			mViewPager.setAdapter(pagerAdapter);
			Log.d(TAG, "onActivityCreated mViewPager " + mViewPager + ", mTabs " + mTabs);
			if (args != null) {
				mViewPager.setCurrentItem(args.getInt("pos", pageSelected), true);
			} else {
				mViewPager.setCurrentItem(pageSelected);
			}
		} else {
			mTabs.clear();
			final List<Fragment> fragments = childFragmentManager.getFragments();
			final String firstTag = savedInstanceState.getString("fake0");
			final String lastTag = savedInstanceState.getString("fakeEnd");
			String tag;
			PagerItem pagerItem;
			Frag frag;
			final int size = fragments.size();
			for (int i = 0; i < size; i++) {
				tag = savedInstanceState.getString(i + "");
				frag = (Frag) childFragmentManager.findFragmentByTag(tag);
				if (frag != null) {
					pagerItem = new PagerItem(frag);
					//Log.d(TAG, "onViewCreated frag " + i + ", " + tag + ", " + frag.getTag() + ", " + pagerItem.dir + ", " + frag);
					mTabs.add(pagerItem);
				}
			}
			if (firstTag != null) {
				final SlidingTabsFragment.PagerItem get0 = mTabs.get(0);
				get0.fakeFrag = (Frag) childFragmentManager.findFragmentByTag(firstTag);
				get0.fakeFrag.slidingTabsFragment = this;
				final SlidingTabsFragment.PagerItem last = mTabs.get((mTabs.size() - 1));
				last.fakeFrag = (Frag) childFragmentManager.findFragmentByTag(lastTag);
				last.fakeFrag.slidingTabsFragment = this;
			}
			//Log.d(TAG, "mTabs=" + mTabs);
			//Log.d(TAG, "fragments=" + fragments);
			pagerAdapter = new PagerAdapter(childFragmentManager);
			mViewPager.setAdapter(pagerAdapter);
			mViewPager.setCurrentItem(savedInstanceState.getInt("pos", pageSelected), true);
		}
		mViewPager.setOffscreenPageLimit(16);

		final TextEditorActivity textActivity;
		if (activ instanceof TextEditorActivity) {
			textActivity = (TextEditorActivity)activ;
			final View v = mSlidingHorizontalScroll.getChildAt(0);
			final ViewGroup.LayoutParams lp = v.getLayoutParams();
			lp.height = (int)(30 * getResources().getDisplayMetrics().density);
			v.setLayoutParams(lp);
		} else {
			textActivity = null;
		}
		
		mSlidingHorizontalScroll.fra = SlidingTabsFragment.this;
		tabClicks = new TabClicks(12);

		mSlidingHorizontalScroll.setViewPager(mViewPager);
		mSlidingHorizontalScroll.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

				@Override
				public void onPageScrolled(final int pageSelected, final float positionOffset,
										   final int positionOffsetPixel) {
//					Log.e("onPageScrolled", "pageSelected: " + pageSelected
//						+ ", positionOffset: " + positionOffset
//						+ ", positionOffsetPixel: " + positionOffsetPixel);
					if (positionOffset == 0 && positionOffsetPixel == 0) {
						final int size = mTabs.size();
						if (size > 1) {
							if (pageSelected == 0) {
								mViewPager.setCurrentItem(size, false);
							} else if (pageSelected == size + 1) {
								mViewPager.setCurrentItem(1, false);
							}
						}
					} 
				}

				@Override
				public void onPageSelected(final int position) {
					final int size = mTabs.size();
					Log.d(TAG, "onPageSelected: " + position + ", mTabs.size() " + size + ", side " + side);
					pageSelected = position;
					if (size > 1) {
						if (position == 1 || position == size) {
							final int newpos = position == 1 ? (size - 1) : position == size ? 0 : (position - 1);
							final PagerItem pi = mTabs.get(newpos);
							Log.d(TAG, "onPageSelected: " + position + ", side " + side + ", pi.frag " + pi.frag + ", pi.fakeFrag " + pi.fakeFrag);
							if (pi.fakeFrag != null) {
								pi.fakeFrag.clone(pi.frag, true);
							} else {
								pi.createFakeFragment();
							}
						}
						if (textActivity != null) {
							final ActionBar actionBar = textActivity.getSupportActionBar();
							if (actionBar != null) {
								final TextFrag textFrag = (TextFrag)pagerAdapter.getItem(position);
								actionBar.setCustomView(textFrag.mToolbarBase);
								textFrag.mEditor.requestFocus();
							}
						}
					}
					final Frag createFragment = pagerAdapter.getItem(position);
					
					if (activ instanceof ExplorerActivity) {
						final ExplorerActivity activity = (ExplorerActivity) activ;
						createFragment.select(true);
						if (side == Side.LEFT) {
							createFragment.slidingTabsFragment.width = activity.balance;
						} else {
							createFragment.slidingTabsFragment.width = -activity.balance;
						}
						if (createFragment.type == Frag.TYPE.EXPLORER && ((ContentFragment)createFragment).openMode == OpenMode.FILE) {
							activity.dir = ((ContentFragment) createFragment).currentPathTitle;
							if (side == Side.LEFT) {
								activity.curContentFrag = (ContentFragment) createFragment;
								activity.curContentFragIndex = mTabs.size() == 1 ? 0 : indexOfMTabs(activity.curContentFrag) + 1;
							} else {
								activity.curExplorerFrag = (ContentFragment) createFragment;
								activity.curExplorerFragIndex = mTabs.size() == 1 ? 0 : indexOfMTabs(activity.curExplorerFrag) + 1;
							}
						} else if (createFragment.type == Frag.TYPE.SELECTION) {
							if (side == Side.LEFT) {
								activity.curSelectionFrag = (ContentFragment) createFragment;
							} else {
								activity.curSelectionFrag2 = (ContentFragment) createFragment;
							}
						}
						
						if (createFragment instanceof FileFrag) {
							FileFrag fileFrag = ((FileFrag)createFragment);
							if (fileFrag.selectedInList1.size() == 0 && 
								(((fileFrag.type == Frag.TYPE.EXPLORER) && activity.COPY_PATH == null && activity.MOVE_PATH == null
								&& activity.EXTRACT_PATH == null && activity.EXTRACT_MOVE_PATH == null) 
								|| (fileFrag.type != Frag.TYPE.EXPLORER))) {
								if (fileFrag.commands.getVisibility() == View.VISIBLE) {
									fileFrag.horizontalDivider6.setVisibility(View.GONE);
									fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.shrink_from_top));
									fileFrag.commands.setVisibility(View.GONE);
								}
							} else {
								if (fileFrag.commands.getVisibility() == View.GONE) {
									fileFrag.commands.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.grow_from_bottom));
									fileFrag.commands.setVisibility(View.VISIBLE);
									fileFrag.horizontalDivider6.setVisibility(View.VISIBLE);
								}
								if (fileFrag instanceof ContentFragment) {
									((ContentFragment)createFragment).updateDelPaste();
								}
							}
//							if (fileFrag instanceof ContentFragment) {
//								if (activity.balance == 0) {
//									if (side == SlidingTabsFragment.Side.LEFT && !activity.swap
//										|| side == SlidingTabsFragment.Side.RIGHT && activity.swap) {
//										((ContentFragment)fileFrag).moreLeft.setVisibility(View.VISIBLE);
//										((ContentFragment)fileFrag).moreRight.setVisibility(View.GONE);
//									} else {
//										((ContentFragment)fileFrag).moreLeft.setVisibility(View.GONE);
//										((ContentFragment)fileFrag).moreRight.setVisibility(View.VISIBLE);
//									}
//									View findViewById = ((ContentFragment)fileFrag).getView().findViewById(R.id.book);
//									findViewById.setVisibility(View.GONE);
//
//									findViewById = ((ContentFragment)fileFrag).getView().findViewById(R.id.hiddenfiles);
//									findViewById.setVisibility(View.GONE);
//
//									findViewById = ((ContentFragment)fileFrag).getView().findViewById(R.id.shortcuts);
//									findViewById.setVisibility(View.GONE);
//								} else {
//									((ContentFragment)fileFrag).moreLeft.setVisibility(View.GONE);
//									((ContentFragment)fileFrag).moreRight.setVisibility(View.GONE);
//
//									View findViewById = ((ContentFragment)fileFrag).getView().findViewById(R.id.book);
//									findViewById.setVisibility(View.VISIBLE);
//
//									findViewById = ((ContentFragment)fileFrag).getView().findViewById(R.id.hiddenfiles);
//									findViewById.setVisibility(View.VISIBLE);
//
//									findViewById = ((ContentFragment)fileFrag).getView().findViewById(R.id.shortcuts);
//									findViewById.setVisibility(View.VISIBLE);
//								}
//							} 
						} 
					} // end if (activ instanceof ExplorerActivity) 
				}

				@Override
				public void onPageScrollStateChanged(final int state) {
					Log.d(TAG, "onPageScrollStateChanged1 state " + state + ", pageSelected " + pageSelected + ", " + side);
					if (state == 0) {
						final int size = mTabs.size();
						if (pageSelected == 0) {
							pageSelected = size;
							mViewPager.setCurrentItem(pageSelected, false);
						} else if (pageSelected == size + 1) {
							pageSelected = 1;
							mViewPager.setCurrentItem(pageSelected, false);
						}
						if (textActivity != null) {
							final ActionBar actionBar = textActivity.getSupportActionBar();
							if (actionBar != null) {
								final TextFrag textFrag = (TextFrag)pagerAdapter.getItem(pageSelected);
								actionBar.setCustomView(textFrag.mToolbarBase);
								textFrag.mEditor.requestFocus();
							}
						}
					}
					Log.d(TAG, "onPageScrollStateChanged2 state " + state + ", pageSelected " + pageSelected + ", " + side);
				}
			});
		//Log.d(TAG, "mSlidingHorizontalScroll " + mSlidingHorizontalScroll);
		mSlidingHorizontalScroll.setCustomTabColorizer(new SlidingHorizontalScroll.TabColorizer() {
				@Override
				public int getIndicatorColor(int position) {
					return 0xFF039BE5;
				}
				@Override
				public int getDividerColor(int position) {
					return 0xff888888;
				}
			});
	}

	void initLeftContentFragmentTabs(final String prevPath, final String suffix, final boolean multiFiles, final String mimes,
									 final String[] previousSelectedStr, final boolean writeableOnly) {
		final File storage = new File("/storage");
		final File[] fs = storage.listFiles();

		//String[] st = sdCardPath.split(":");
		File f;
		
		ContentFragment contentFrag;

		contentFrag = new ContentFragment();
		contentFrag.type = Frag.TYPE.EXPLORER;
		
		contentFrag.currentPathTitle = prevPath;
		contentFrag.suffix = (suffix == null) ? "*" : suffix;
		contentFrag.multiFiles = multiFiles;
		contentFrag.mimes = mimes == null || mimes.length() == 0 ? "*/*" : mimes.toLowerCase();
		contentFrag.previousSelectedStr = previousSelectedStr;
		contentFrag.mWriteableOnly = writeableOnly;
		
		//contentFrag.setArguments(bundle);
		contentFrag.slidingTabsFragment = this;
		mTabs.add(new PagerItem(contentFrag));

		if (fs != null)
		for (int i = fs.length - 1; i >= 0; i--) {
			f = fs[i];
			Log.d(TAG, f + ".");
			if (f.canWrite()) {
				contentFrag = new ContentFragment();
				contentFrag.type = Frag.TYPE.EXPLORER;
				
				contentFrag.currentPathTitle = f.getAbsolutePath();
				contentFrag.suffix = "*";
				contentFrag.multiFiles = true;
				contentFrag.mimes = "*/*";

				contentFrag.slidingTabsFragment = this;
				mTabs.add(new PagerItem(contentFrag));//f.getAbsolutePath(), ".*", true, null));
			}
		}
	}

	void addSelectionTab(final String[] previousSelectedStr) {
		mTabs.add(new PagerItem(Frag.getFrag(this, Frag.TYPE.SELECTION, Util.arrayToString(previousSelectedStr, false, "|"))));
	}

	Fragment addContentFragTab(final String path, final String suffix, final String mimes, final boolean multi) {

		final Frag fragment = new ContentFragment();
		fragment.type = Frag.TYPE.EXPLORER;

		final Bundle bundle = new Bundle();
		bundle.putString(Constants.EXTRA_ABSOLUTE_PATH, path == null ? "/storage" : path);//EXTRA_DIR_PATH
		bundle.putString(Constants.EXTRA_FILTER_FILETYPE, suffix);
		bundle.putString(Constants.EXTRA_FILTER_MIMETYPE, mimes);
		bundle.putBoolean(Constants.EXTRA_MULTI_SELECT, multi);
		//bundle.putStringArray(ExplorerActivity.PREVIOUS_SELECTED_FILES, previousSelectedStr);
		fragment.setArguments(bundle);

		fragment.slidingTabsFragment = this;
		mTabs.add(new PagerItem(fragment));
		return fragment;
	}

	Fragment addZip(final Frag.TYPE t, final String path) {

		final Frag fragment = new ZipFragment();
		fragment.type = Frag.TYPE.ZIP;
		
		final Bundle bundle = new Bundle();
		bundle.putString(Constants.EXTRA_ABSOLUTE_PATH, path);
		fragment.setArguments(bundle);

		fragment.slidingTabsFragment = this;
		mTabs.add(new PagerItem(fragment));
		return fragment;
	}

	public boolean circular() {
		return true;
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume pagerAdapter=" + pagerAdapter + ", mTabs=" + mTabs + ", newIntent " + newIntent);
		super.onResume();
		if (newIntent) {
			newIntent = false;
			addFrag(main, pagerItem);
			main = null;
			pagerItem = null;
		}
	}
	
	private boolean newIntent = false;
	private TextFrag main = null;
	private PagerItem pagerItem = null;
	public void addTextTab(final Intent intent, String title) {
		if (title == null || title.length() == 0) {
			title = "Untitled " + ++TextFrag.no + ".txt";
		}
		Log.d(TAG, "addTextTab1 mViewPager=" + mViewPager + ", pagerAdapter=" + pagerAdapter + ", filename=" + title + ", mTabs=" + mTabs);
		main = TextFrag.newInstance(intent, title, null);
		pagerItem = new PagerItem(main);
		main.slidingTabsFragment = this;

		if (mViewPager != null && mTabs.size() == 0) {
			mTabs.add(pagerItem);
			pagerAdapter.notifyDataSetChanged();
			mViewPager.setCurrentItem(pagerAdapter.getCount() - 1);
			notifyTitleChange();
		} else if (mTabs.size() >= 1) {
			newIntent = true;
		} else {
			mTabs.add(pagerItem);
		}
		Log.d(TAG, "addTextTab2 " + title + ", " + mTabs);
	}

	public void addTab(final OpenMode openmode, final String path) {
		final Frag frag = Frag.getFrag(this, Frag.TYPE.EXPLORER, path);
		((ContentFragment)frag).openMode = openmode;
		final PagerItem pagerItem = new PagerItem(frag);
		addFrag(frag, pagerItem);
	}

	public void addTab(final Frag frag) {
		addFrag(frag, new PagerItem(frag));
	}
	
	public Frag addTab(final Frag.TYPE t, final String path) {
		Log.d(TAG, "addTab TYPE " + t + ", path=" + path + ", mTabs=" + mTabs);
		final PagerItem pagerItem;
		final Frag frag;
		if (t == null) {
			frag = getCurrentFragment();
			if (getActivity() instanceof TextEditorActivity) {
				final TextFrag main = TextFrag.newInstance(null, "Untitled " + ++TextFrag.no + ".txt", path);
				main.slidingTabsFragment = this;
				pagerItem = new PagerItem(main);
			} else {//}if (frag.type == Frag.TYPE.EXPLORER) {
				final Frag clone = frag.clone(false);
				//Log.e(TAG, "addTab frag " + frag);
				//Log.e(TAG, "addTab clone " + clone);
				pagerItem = new PagerItem(clone);
			//} else {
			//	return;
			}
		} else {
			frag = Frag.getFrag(this, t, path);
			pagerItem = new PagerItem(frag);
		}
		addFrag(frag, pagerItem);
		return frag;
	}

	private void addFrag(final Frag frag, final PagerItem pagerItem) {
		final FragmentTransaction ft = childFragmentManager.beginTransaction();
		final ArrayList<PagerItem> mTabs2 = new ArrayList<PagerItem>(mTabs);
		final int size = mTabs.size();
		int currentItem = 0;
		if (size > 1) {
			currentItem = mViewPager.getCurrentItem();
			Log.d(TAG, "addFrag1 currentItem " + currentItem + ", dir=" + frag.currentPathTitle + ", mTabs=" + mTabs);

			PagerItem pi = mTabs.get(0);
			ft.remove(pi.fakeFrag);
			pi.fakeFrag = null;

			pi = mTabs.get(size - 1);
			ft.remove(pi.fakeFrag);
			pi.fakeFrag = null;

			for (int j = 0; j < size; j++) {
				ft.remove(mTabs.remove(0).frag);
			}
			pagerAdapter.notifyDataSetChanged();
			ft.commitNow();

			for (PagerItem pi2 : mTabs2) {
				mTabs.add(pi2);
			}
			mTabs.add(currentItem++, pagerItem);
			mViewPager.setAdapter(pagerAdapter);
			mViewPager.setCurrentItem(currentItem, false);
		} else {
			final PagerItem remove = mTabs.remove(0);
			ft.remove(remove.frag);
			pagerAdapter.notifyDataSetChanged();
			ft.commitNow();
			mTabs.add(remove);
			mTabs.add(pagerItem);
			pagerAdapter.notifyDataSetChanged();
			currentItem = 2;
			mViewPager.setCurrentItem(currentItem);
		}
		notifyTitleChange();
		final FragmentActivity activity = getActivity();
		if (activity instanceof TextEditorActivity) {
			final ActionBar actionBar = ((TextEditorActivity)activity).getSupportActionBar();
			if (actionBar != null) {
				final TextFrag textFrag = ((TextFrag)pagerAdapter.getItem(currentItem));
				actionBar.setCustomView(textFrag.mToolbarBase);
				textFrag.mEditor.requestFocus();
			}
		}
		Log.d(TAG, "addFrag2 " + frag.currentPathTitle + ", mViewPager.getCurrentItem() " + mViewPager.getCurrentItem() + ", " + mTabs);
	}

	public int realFragCount() {
		return mTabs.size();
	}

	public void closeTab(Frag m) {
		int i = 0;
		final ArrayList<PagerItem> mTabs2 = new ArrayList<PagerItem>(mTabs);
		for (PagerItem pi : mTabs) {
			if (pi.frag == m) {
				Log.i(TAG, "closeTab " + i);
				break;
			}
			i++;
		}
		Log.i(TAG, "closeTab " + i + ", " + m + ", " + mTabs);
		final FragmentTransaction ft = childFragmentManager.beginTransaction();
		SlidingTabsFragment.PagerItem pi;
		if (mTabs.size() > 1) {
			pi = mTabs.get(0);
			ft.remove(pi.fakeFrag);
			pi.fakeFrag = null;
			pi = mTabs.get(mTabs.size() - 1);
			ft.remove(pi.fakeFrag);
			pi.fakeFrag = null;
		}
		for (int j = mTabs2.size() - 1; j >= i; j--) {
			ft.remove(mTabs.remove(j).frag);
		}
		if (mTabs.size() == 1 && mTabs2.size() == 2) {
			pi = mTabs.remove(0);
			ft.remove(pi.frag);
			pi.fakeFrag = null;
		}
		//mTabs.clear();
		pagerAdapter.notifyDataSetChanged();
		ft.commitNow();

		mTabs2.remove(i);

		if (mTabs.size() == 0 && i > 0) {
			mTabs.add(mTabs2.get(0));
		}
		for (int j = i; j < mTabs2.size(); j++) {
			mTabs.add(mTabs2.get(j));
		}
		pagerAdapter.notifyDataSetChanged();
		mTabs2.clear();
		notifyTitleChange();
		final int currentItem = i <= mTabs.size() - 1 && mTabs.size() > 1 ? i + 1: mTabs.size() == 1 ? 0 : i;
		mViewPager.setCurrentItem(currentItem);
		final FragmentActivity activity = getActivity();
		if (activity instanceof TextEditorActivity) {
			final ActionBar actionBar = ((TextEditorActivity)activity).getSupportActionBar();
			if (actionBar != null) {
				final TextFrag textFrag = (TextFrag)pagerAdapter.getItem(currentItem);
				actionBar.setCustomView(textFrag.mToolbarBase);
				textFrag.mEditor.requestFocus();
			}
		}
	}

	public void closeCurTab() {
		final Frag main = getCurrentFragment();
		Log.d(TAG, "closeCurTab " + main);

		if (main.activity != null) {
			if (main == main.activity.curSelectionFrag) {
				main.activity.curSelectionFrag = null;
			} else if (main == main.activity.curSelectionFrag2) {
				main.activity.curSelectionFrag2 = null;
			}
		}
		closeTab(main);
	}

	public void closeOtherTabs() {
		final Frag curFrag = getCurrentFragment();
		Log.d(TAG, "closeOtherTabs " + curFrag);
		final int size = mTabs.size();
		final int curIndex = indexOfMTabs(curFrag);
		final int curExplore;
		final Activity activ = getActivity();
		if (curFrag.type == Frag.TYPE.EXPLORER || !(activ instanceof ExplorerActivity)) {
			curExplore = -1;
		} else {
			final ExplorerActivity activity = (ExplorerActivity) activ;
			if (side == Side.LEFT) {
				curExplore = indexOfMTabs(activity.curContentFrag);
				if (activity.curSelectionFrag != curFrag) {
					activity.curSelectionFrag = null;
				}
			} else 	{
				curExplore = indexOfMTabs(activity.curExplorerFrag);
				if (activity.curSelectionFrag2 != curFrag) {
					activity.curSelectionFrag2 = null;
				}
			}
		}
		final ArrayList<PagerItem> mTabs2 = new ArrayList<PagerItem>(mTabs);

		final FragmentTransaction ft = childFragmentManager.beginTransaction();
		SlidingTabsFragment.PagerItem pi = mTabs.get(0);
		ft.remove(pi.fakeFrag);
		pi.fakeFrag = null;
		pi = mTabs.get(size - 1);
		ft.remove(pi.fakeFrag);
		pi.fakeFrag = null;
		for (int j = 0; j < size; j++) {
			ft.remove(mTabs.remove(0).frag);
		}
		pagerAdapter.notifyDataSetChanged();
		ft.commitNow();

		if (curExplore >= 0) {
			mTabs.add(mTabs2.get(curExplore));
		}
		mTabs.add(mTabs2.get(curIndex));
		mTabs2.clear();
		pagerAdapter.notifyDataSetChanged();
		notifyTitleChange();
		mViewPager.setCurrentItem(0);
		final FragmentActivity activity = getActivity();
		if (activity instanceof TextEditorActivity) {
			final ActionBar actionBar = ((TextEditorActivity)activity).getSupportActionBar();
			if (actionBar != null) {
				final TextFrag textFrag = (TextFrag)pagerAdapter.getItem(0);
				actionBar.setCustomView(textFrag.mToolbarBase);
				textFrag.mEditor.requestFocus();
			}
		}
	}

	public Frag getCurrentFragment() {
		final int currentItem = mViewPager.getCurrentItem();
		//Log.d(TAG, "getCurrentFragment = " + currentItem + ", " + side + ", " + mTabs);
		return pagerAdapter.getItem(currentItem);
	}

	static int getFragTypeIndex(final Frag fileFrag, final Frag.TYPE t) {
		final SlidingTabsFragment.PagerAdapter pagerAdapter;
		if (fileFrag.slidingTabsFragment.side == SlidingTabsFragment.Side.LEFT) {
			pagerAdapter = fileFrag.activity.slideFrag2.pagerAdapter;
		} else {
			pagerAdapter = fileFrag.activity.slideFrag.pagerAdapter;
		}
		final int count = pagerAdapter.getCount();
		if (count > 1) {
			for (int i = 1; i < count - 1; i++) {
				if (pagerAdapter.getItem(i).type == t) {
					return i;
				}
			}
			return -1;
		} else {
			return pagerAdapter.getItem(0).type == t ? 0 : -1;
		}
	}

	public Frag getFragmentIndex(final int idx) {
		Log.d(TAG, "getContentFragment index " + idx + ", " + side + ", " + mTabs);
		return pagerAdapter.getItem(idx);
	}

	public int indexOfAdapter(final Frag frag) {
		int i = 0;
		for (PagerItem pi : mTabs) {
			//Log.d(TAG, "indexOf frag " + frag + ", pi.frag " + pi.frag);
			if (frag == pi.frag) {
				return mTabs.size() == 1 ? i : i + 1;
			} else {
				i++;
			}
		}
		return -1;
	}

	public int indexOfMTabs(final Frag frag) {
		int i = 0;
		for (PagerItem pi : mTabs) {
			//Log.d(TAG, "indexOf frag " + frag + ", pi.frag " + pi.frag);
			if (frag == pi.frag) {
				return i;
			} else {
				i++;
			}
		}
		return -1;
	}

	public Frag getFrag(final Frag.TYPE t) {
		for (PagerItem pi : mTabs) {
			//Log.d(TAG, "indexOf frag " + frag + ", pi.frag " + pi.frag);
			if (t == pi.frag.type) {
				return pi.frag;
			} 
		}
		return null;
	}

	public int getFragIndex(final Frag.TYPE t) {
		final int count = pagerAdapter.getCount();
		if (count > 1) {
			for (int i = 1; i < count - 1; i++) {
				if (pagerAdapter.getItem(i).type == t) {
					return i;
				}
			}
			return -1;
		} else {
			return pagerAdapter.getItem(0).type == t ? 0 : -1;
		}
	}

	void updateSpan() {
		Log.d(TAG, "updateSpan ");
		for (PagerItem pi : mTabs) {
			if (pi.frag != null) {
				if (pi.frag instanceof ContentFragment) {
					((FileFrag)pi.frag).refreshRecyclerViewLayoutManager();
				}
			}
		}
	}

	void updateLayout(final boolean changeTime) {
		Log.d(TAG, "updateLayout " + changeTime);
		for (PagerItem pi : mTabs) {
			if (pi.frag != null) {
				if (pi.frag instanceof FileFrag) {
					final FileFrag frag = ((FileFrag)pi.frag);
					frag.refreshRecyclerViewLayoutManager();
					final int no = frag.commands.getChildCount();
					Button b;
					for (int i = 0; i < no; i++) {
						final View childAt = frag.commands.getChildAt(i);
						if (childAt instanceof Button) {
							b = (Button) childAt;
							b.setTextColor(Constants.TEXT_COLOR);
							b.getCompoundDrawables()[1].setAlpha(0xff);
							b.getCompoundDrawables()[1].setColorFilter(Constants.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
						} else {
							((ImageView)childAt).setColorFilter(Constants.TEXT_COLOR, PorterDuff.Mode.SRC_IN);
						}
					}
				}
				if (changeTime && pi.frag.getContext() != null) {
					pi.frag.updateColor(null);
					if (pi.frag instanceof ContentFragment) {
						((ContentFragment)pi.frag).setDirectoryButtons();
					}
				}
			}
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		//Log.d(TAG, "onSaveInstanceState1 " + outState + ", " + childFragmentManager.getFragments());
		try {
			final int size = mTabs.size();
			if (mTabs != null && size > 0) {
				int i = 0;
				for (PagerItem pi : mTabs) {
					Log.d(TAG, "onSaveInstanceState pi.frag.getTag() " + pi.frag.getTag() + ", " + side + ", " + pi);
					//childFragmentManager.putFragment(outState, "tabb" + i++, pi.frag);
					outState.putString(i++ + "", pi.frag.getTag());
					outState.putString(pi.frag.getTag(), pi.frag.currentPathTitle);
				}
	 			if (size > 1) {
					//Log.d(TAG, "fakeStart 0 tag" + mTabs.get(0).fakeFrag.getTag());
					outState.putString("fake0", mTabs.get(0).fakeFrag.getTag());
					//Log.d(TAG, "fakeEnd tag  " + mTabs.get(mTabs.size()-1).fakeFrag.getTag());
		 			outState.putString("fakeEnd", mTabs.get(size - 1).fakeFrag.getTag());
				}
			}
			outState.putInt("width", width);
			outState.putInt("side", side.ordinal());
			outState.putInt("pos", mViewPager.getCurrentItem());
		} catch (Exception e) {
			// Logger.log(e,"puttingtosavedinstance",getActivity());
			e.printStackTrace();
		}
		//Log.d(TAG, "onSaveInstanceState2 " + outState + ", " + childFragmentManager);
	}

//	@Override 
//	public void onDestroyView() {
//		//mViewPager.setAdapter(null);
//		super.onDestroyView();
//	}
	
	public void notifyTitleChange() {
		mSlidingHorizontalScroll.setViewPager(mViewPager);
	}

	public void setCurrentItem(final int pos, final boolean smooth) {
		mViewPager.setCurrentItem(pos, smooth);
	}

	private class PagerItem implements Parcelable {
		private static final String TAG = "PagerItem";
		private final Frag frag;
		private Frag fakeFrag;

		private PagerItem(final Frag frag1) {
			//Log.d(TAG, "tag=" + frag1.getTag() + ", " + frag1);
			this.frag = frag1;
			this.frag.slidingTabsFragment = SlidingTabsFragment.this;
		}

		protected PagerItem(Parcel in) {
			frag = (ContentFragment) in.readSerializable();
		}

		@Override
		public int describeContents() {
			return frag.type.ordinal();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeSerializable(frag);
		}

		public final Parcelable.Creator<PagerItem> CREATOR = new Parcelable.Creator<PagerItem>() {
			public PagerItem createFromParcel(Parcel in) {
				return new PagerItem(in);
			}

			public PagerItem[] newArray(int size) {
				return new PagerItem[size];
			}
		};

		@Override
		public Object clone() {
			return new PagerItem(frag.clone(true));
		}

		/**
		 * @return A new {@link Fragment} to be displayed by a {@link ViewPager}
		 */
//		private Frag createFragment(SlidingTabsFragment s) {
//			Log.d(TAG, "createFragment() " + frag);
////			if (frag == null) {
////				//frag = ContentFragment.newInstance(s, dir, suffix, multi, bundle);
////				frag = new Frag();
//			frag.slidingTabsFragment = s;
//			//}
////			if (fakeFrag != null) {
////				fakeFrag.clone(frag);
////			}
//			return frag;
//		}

		private Frag createFakeFragment() {
			Log.d(TAG, "createFakeFragment() fakeFrag " + fakeFrag + ", frag " + frag);
//			if (frag == null && fakeFrag == null) {
//				//fakeFrag = ContentFragment.newInstance(s, dir, suffix, multi, bundle);
//				fakeFrag = frag.clone();//createFragment(s).clone();
//			} else 
			if (fakeFrag == null) {
				//fakeFrag = ContentFragment.newOriFakeInstance(frag);
				fakeFrag = frag.clone(true);
			} else if (fakeFrag != null && frag != null) {
				fakeFrag.clone(frag, true);
				//fakeFrag.refreshDirectory();
			}
			//fakeFrag.slidingTabsFragment = s;
			return fakeFrag;
		}

		public String getTitle() {
			return frag.getTitle();
		}

		@Override
		public String toString() {
			return "frag=" + frag + ", fakeFrag=" + fakeFrag;
		}
	}

	public class PagerAdapter extends FragmentPagerAdapter {
		int numOfPages = 1;

		PagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public float getPageWidth(int position) {
			return 1f / numOfPages;
		}

		@Override
		public Frag getItem(final int position) {
			final int size = mTabs.size();
			//Log.d(TAG, "getItem " + position + "/" + size + ", " + side);
			if (size > 1) {
				if (position == 0) {
					return mTabs.get(size - 1).createFakeFragment();
				} else if (position == size + 1) {
					return mTabs.get(0).createFakeFragment();
				} else {
					return mTabs.get(position - 1).frag;
				}
			} else {
				return mTabs.get(0).frag;
			}
		}

		@Override
		public int getCount() {
			final int size = mTabs.size();
			if (size > 1) {
				return size + 2;
			} else {
				return size;
			}
		}

		@Override
		public CharSequence getPageTitle(final int position) {
			final int size = mTabs.size();
			if (size > 1) {
				if (position == 0 || position == size + 1) {
					return "";
				} else {
					return mTabs.get(position - 1).getTitle();
				}
			} else {
				return mTabs.get(position).getTitle();
			}
		}

		@Override
		public int getItemPosition(final Object object) {
			for (PagerItem pi : mTabs) {
				if (pi.frag == object) {
					//Log.d(TAG, "getItemPosition POSITION_UNCHANGED" + ", " + object);
					return POSITION_UNCHANGED;
				}
			}
			//Log.d(TAG, "getItemPosition POSITION_NONE" + ", " + object);
			return POSITION_NONE;
		}
	}

}
