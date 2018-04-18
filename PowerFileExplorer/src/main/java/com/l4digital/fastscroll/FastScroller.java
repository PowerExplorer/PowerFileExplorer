/*
 * Copyright 2016 L4 Digital LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.l4digital.fastscroll;

import android.animation.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.support.annotation.*;
import android.support.design.widget.*;
import android.support.v4.content.*;
import android.support.v4.graphics.drawable.*;
import android.support.v4.view.*;
import android.support.v7.widget.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import net.gnu.explorer.*;
import android.support.v4.widget.*;
import android.widget.LinearLayout.LayoutParams;

public class FastScroller extends LinearLayout {

    public interface SectionIndexer {

        String getSectionText(int position);
    }

    private static final int sBubbleAnimDuration = 100;
    private static final int sScrollbarAnimDuration = 300;
    private static final int sScrollbarHideDelay = 5000;
    private static final int sTrackSnapRange = 5;

    @ColorInt private int mBubbleColor;
    @ColorInt private int mHandleColor;

    private int mLength;
    private boolean mHideScrollbar;
    private boolean vertical = true;
	private boolean center = false;

	private SectionIndexer mSectionIndexer;
    private ViewPropertyAnimator mScrollbarAnimator;
    private ViewPropertyAnimator mBubbleAnimator;
    private FastScrollRecyclerView mRecyclerView;
    private TextView mBubbleView;
    private ImageView mHandleView;
    private ImageView mTrackView;
    private View mScrollbar;
    private Drawable mBubbleImage;
    private Drawable mHandleImage;
    private Drawable mTrackImage;

    private FastScrollStateChangeListener mFastScrollStateChangeListener;
	SwipeRefreshLayout swipeRefreshLayout;

    private final Runnable mScrollbarHider = new Runnable() {
        @Override
        public void run() {
            hideScrollbar();
        }
    };

    private final RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
            if (!mHandleView.isSelected() && isEnabled()) {
                setViewPositions(getScrollProportion(recyclerView));
            }
        }

        @Override
        public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if (isEnabled()) {
                switch (newState) {
					case RecyclerView.SCROLL_STATE_DRAGGING:
						getHandler().removeCallbacks(mScrollbarHider);
						cancelAnimation(mScrollbarAnimator);
						if (!isViewVisible(mScrollbar)) {
							showScrollbar();
						}
						break;
					case RecyclerView.SCROLL_STATE_IDLE:
						if (mHideScrollbar && !mHandleView.isSelected()) {
							getHandler().postDelayed(mScrollbarHider, sScrollbarHideDelay);
						}
						break;
                }
            }
        }
    };

    public FastScroller(final Context context) {
        super(context);
        layout(context, null);
		if (vertical) {
			setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		} else {
			setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		}
    }

    public FastScroller(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FastScroller(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        layout(context, attrs);
        setLayoutParams(generateLayoutParams(attrs));
    }

    @Override
    public void setLayoutParams(final @NonNull ViewGroup.LayoutParams params) {
		if (vertical) {
			params.width = LayoutParams.WRAP_CONTENT;
		} else {
			params.height = LayoutParams.WRAP_CONTENT;
		}
        super.setLayoutParams(params);
    }

    public void setLayoutParams(final @NonNull ViewGroup viewGroup) {
        if (viewGroup instanceof CoordinatorLayout) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) getLayoutParams();
            @IdRes int layoutId = mRecyclerView.getId();

            if (layoutId != NO_ID) {
                layoutParams.setAnchorId(layoutId);
                layoutParams.anchorGravity = GravityCompat.END;
            } else {
                layoutParams.gravity = GravityCompat.END;
            }
            setLayoutParams(layoutParams);
        } else if (viewGroup instanceof FrameLayout) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
            //layoutParams.gravity = GravityCompat.END;
            if (vertical) {
				layoutParams.gravity = Gravity.RIGHT;
			} else {
				layoutParams.gravity = Gravity.BOTTOM;
			}
            setLayoutParams(layoutParams);
        } else if (viewGroup instanceof LinearLayout) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
			if (vertical) {
				layoutParams.gravity = Gravity.RIGHT;
			} else {
				layoutParams.gravity = Gravity.BOTTOM;
			}
            setLayoutParams(layoutParams);
        } else if (viewGroup instanceof RelativeLayout) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            } else {
				if (vertical) {
					layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				} else {
					layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				}
            }
            setLayoutParams(layoutParams);
        } else {
            throw new IllegalArgumentException("Parent ViewGroup must be a CoordinatorLayout, FrameLayout, or RelativeLayout");
        }
    }

    public void setSectionIndexer(final SectionIndexer sectionIndexer) {
        mSectionIndexer = sectionIndexer;
    }

    public void attachRecyclerView(final FastScrollRecyclerView recyclerView) {
        mRecyclerView = recyclerView;

        if (mRecyclerView != null) {
            mRecyclerView.addOnScrollListener(mScrollListener);
        }
    }

    public void detachRecyclerView() {
        if (mRecyclerView != null) {
            mRecyclerView.removeOnScrollListener(mScrollListener);
            mRecyclerView = null;
        }
    }

    /**
     * Hide the scrollbar when not scrolling.
     *
     * @param hideScrollbar True to hide the scrollbar, false to show
     */
    public void setHideScrollbar(final boolean hideScrollbar) {
        mHideScrollbar = hideScrollbar;
        mScrollbar.setVisibility(hideScrollbar ? GONE : VISIBLE);
    }

    /**
     * Display a scroll track while scrolling.
     *
     * @param visible True to show scroll track, false to hide
     */
    public void setTrackVisible(final boolean visible) {
        mTrackView.setVisibility(visible ? VISIBLE : GONE);
    }

	public void setCenter(boolean center) {
		this.center = center;
	}

	public void setVertical(final boolean vertical) {
        this.vertical = vertical;
    }

    /**
     * Set the color of the scroll track.
     *
     * @param color The color for the scroll track
     */
    public void setTrackColor(final @ColorInt int color) {
        final @ColorInt int trackColor = color;

        if (mTrackImage == null) {
            if (vertical) {
				mTrackImage = DrawableCompat.wrap(ContextCompat.getDrawable(getContext(), R.drawable.fastscroll_track));
			} else {
				mTrackImage = DrawableCompat.wrap(ContextCompat.getDrawable(getContext(), R.drawable.fastscroll_track_horizontal));
			}
            mTrackImage.mutate();
        }

        DrawableCompat.setTint(mTrackImage, trackColor);
        mTrackView.setImageDrawable(mTrackImage);
    }

    /**
     * Set the color for the scroll handle.
     *
     * @param color The color for the scroll handle
     */
    public void setHandleColor(final @ColorInt int color) {
        mHandleColor = color;

        if (mHandleImage == null) {
			if (vertical) {
				mHandleImage = DrawableCompat.wrap(ContextCompat.getDrawable(getContext(), R.drawable.fastscroll_handle));
			} else {
				mHandleImage = DrawableCompat.wrap(ContextCompat.getDrawable(getContext(), R.drawable.fastscroll_handle_horizontal));
			}
            mHandleImage.mutate();
        }

        DrawableCompat.setTint(mHandleImage, mHandleColor);
        mHandleView.setImageDrawable(mHandleImage);
    }

    /**
     * Set the background color of the index bubble.
     *
     * @param color The background color for the index bubble
     */
    public void setBubbleColor(final @ColorInt int color) {
        mBubbleColor = color;

        if (mBubbleImage == null) {
            mBubbleImage = DrawableCompat.wrap(ContextCompat.getDrawable(getContext(), R.drawable.fastscroll_bubble));
            mBubbleImage.mutate();
        }

        DrawableCompat.setTint(mBubbleImage, mBubbleColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mBubbleView.setBackground(mBubbleImage);
        } else {
            //noinspection deprecation
            mBubbleView.setBackgroundDrawable(mBubbleImage);
        }
    }

    /**
     * Set the text color of the index bubble.
     *
     * @param color The text color for the index bubble
     */
    public void setBubbleTextColor(final @ColorInt int color) {
        mBubbleView.setTextColor(color);
    }

    /**
     * Set the fast scroll state change listener.
     *
     * @param fastScrollStateChangeListener The interface that will listen to fastscroll state change events
     */
    public void setFastScrollStateChangeListener(final FastScrollStateChangeListener fastScrollStateChangeListener) {
        mFastScrollStateChangeListener = fastScrollStateChangeListener;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        setVisibility(enabled ? VISIBLE : GONE);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (vertical) {
					if (event.getX() < mHandleView.getX() - ViewCompat.getPaddingStart(mHandleView)) {
						return false;
					}
				} else {
					if (event.getY() < mHandleView.getY() - ViewCompat.getPaddingStart(mHandleView)) {
						return false;
					}
				}

				if (swipeRefreshLayout != null) {
					swipeRefreshLayout.setEnabled(false);
				}
				setHandleSelected(true);

				getHandler().removeCallbacks(mScrollbarHider);
				cancelAnimation(mScrollbarAnimator);
				cancelAnimation(mBubbleAnimator);

				if (!isViewVisible(mScrollbar)) {
					showScrollbar();
				}

				if (mSectionIndexer != null && !isViewVisible(mBubbleView)) {
					showBubble();
				}

				if (mFastScrollStateChangeListener != null) {
					mFastScrollStateChangeListener.onFastScrollStart();
				}
				//break;
			case MotionEvent.ACTION_MOVE:
				float y;
				if (vertical) {
					y = event.getY();
				} else {
					y = event.getX();
				}
				//final float y = event.getY();
				setViewPositions(y);
				setRecyclerViewPosition(y);
				return true;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				setHandleSelected(false);

				if (mHideScrollbar) {
					getHandler().postDelayed(mScrollbarHider, sScrollbarHideDelay);
				}

				if (isViewVisible(mBubbleView)) {
					hideBubble();
				}

				if (mFastScrollStateChangeListener != null) {
					mFastScrollStateChangeListener.onFastScrollStop();
				}
				if (swipeRefreshLayout != null) {
					swipeRefreshLayout.setEnabled(true);
				}
				return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
		if (vertical) {
			mLength = h;
		} else {
			mLength = w;
		}
    }

    private void setRecyclerViewPosition(final float y) {
        if (mRecyclerView != null && mRecyclerView.getAdapter() != null) {
            final int itemCount = mRecyclerView.getAdapter().getItemCount();
            final float proportion;

//			if (vertical) {
//				if (mHandleView.getY() == 0) {
//					proportion = 0f;
//				} else if (mHandleView.getY() + mHandleView.getHeight() >= mHeight - sTrackSnapRange) {
//					proportion = 1f;
//				} else {
//					proportion = y / (float) mHeight;
//				}
//			} else {
//				if (mHandleView.getX() == 0) {
//					proportion = 0f;
//				} else if (mHandleView.getX() + mHandleView.getWidth() >= mHeight - sTrackSnapRange) {
//					proportion = 1f;
//				} else {
//					proportion = y / (float) mHeight;
//				}
//			}
			proportion = y / (float) (mLength);
            final int targetPos = getValueInRange(0, itemCount - 1, (int) (proportion * (float) itemCount));
			Log.d("FastScroller", "mHandleView.getX() " + mHandleView.getX() + ", mLength " + mLength + ", y " + y + ", proportion " + proportion + ", vertical " + vertical + ", center " + center + ", itemCount " + itemCount + ", targetPos " + targetPos);
			if (center) {
				final int itemLength;
				final int childCount = mRecyclerView.getLayoutManager().getChildCount();
				if (vertical) {
					itemLength = mRecyclerView.getLayoutManager().getChildAt(childCount/2).getMeasuredHeight();
					if (targetPos <= (childCount) / 2) {
						mRecyclerView.setPadding(0, Math.max((mLength - itemLength) / 2 - (targetPos * itemLength), 0), 0, 0);
					} else if ((itemCount - 1 - targetPos) <= (childCount) / 2) {
						mRecyclerView.setPadding(0, 0, 0, Math.max((mLength + itemLength) / 2 - ((itemCount - targetPos) * itemLength), 0));
					}
				} else {
					itemLength = mRecyclerView.getLayoutManager().getChildAt(childCount/2).getMeasuredWidth();
					if (targetPos <= (childCount) / 2) {
						mRecyclerView.setPadding(Math.max((mLength - itemLength) / 2 - (targetPos * itemLength), 0), 0, 0, 0);
					} else if ((itemCount - 1 - targetPos) <= (childCount) / 2) {
						mRecyclerView.setPadding(0, 0, Math.max((mLength + itemLength) / 2 - ((itemCount - targetPos) * itemLength), 0), 0);
					}
				}
				((LinearLayoutManager)mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(
					targetPos, 
					(targetPos * itemLength < (mLength - itemLength) / 2) ? targetPos * itemLength : (mLength - itemLength) / 2);
			} else {
				mRecyclerView.getLayoutManager().scrollToPosition(targetPos);
			}
			if (mSectionIndexer != null) {
                mBubbleView.setText(mSectionIndexer.getSectionText(targetPos));
            }
        }
    }

    private float getScrollProportion(final RecyclerView recyclerView) {
		int scrollOffset;
		int scrollRange;
		float proportion;
		if (vertical) {
			scrollOffset = recyclerView.computeVerticalScrollOffset();
			scrollRange = recyclerView.computeVerticalScrollRange();
//			if (center) {
//				proportion = (float) scrollOffset / ((float) scrollRange);
//			} else {
			//proportion = (float) scrollOffset / ((float) scrollRange - mHeight);
			//}
		} else {
			scrollOffset = recyclerView.computeHorizontalScrollOffset();
			scrollRange = recyclerView.computeHorizontalScrollRange();
//			if (center) {
//				proportion = (float) scrollOffset / ((float) scrollRange);
//			} else {
			//proportion = (float) scrollOffset / ((float) scrollRange - mHeight);
			//}
		}
		proportion = (float) scrollOffset / ((float) scrollRange - mLength);
        return mLength * proportion;
    }

    private int getValueInRange(final int min, final int max, final int value) {
        return Math.min(Math.max(min, value), max);
    }

    private void setViewPositions(final float y) {
		if (vertical) {
			final int bubbleHeight = mBubbleView.getHeight();
			final int handleHeight = mHandleView.getHeight();
			mBubbleView.setY(getValueInRange(0, mLength - bubbleHeight - handleHeight / 2, (int) (y - bubbleHeight)));
			mHandleView.setY(getValueInRange(0, mLength - handleHeight, (int) (y - handleHeight / 2)));
		} else {
			final int bubbleHeight = mBubbleView.getWidth();
			final int handleHeight = mHandleView.getWidth();
			mBubbleView.setX(getValueInRange(0, mLength - bubbleHeight - handleHeight / 2, (int) (y - bubbleHeight)));
			mHandleView.setX(getValueInRange(0, mLength - handleHeight, (int) (y - handleHeight / 2)));
		}
    }

    private boolean isViewVisible(final View view) {
        return view != null && view.getVisibility() == VISIBLE;
    }

    private void cancelAnimation(final ViewPropertyAnimator animator) {
        if (animator != null) {
            animator.cancel();
        }
    }

    private void showBubble() {
        mBubbleView.setVisibility(VISIBLE);
        mBubbleAnimator = mBubbleView.animate().alpha(1f)
			.setDuration(sBubbleAnimDuration)
			.setListener(new AnimatorListenerAdapter() {
				// adapter required for new alpha value to stick
			});
    }

    private void hideBubble() {
        mBubbleAnimator = mBubbleView.animate().alpha(0f)
			.setDuration(sBubbleAnimDuration)
			.setListener(new AnimatorListenerAdapter() {

				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					mBubbleView.setVisibility(GONE);
					mBubbleAnimator = null;
				}

				@Override
				public void onAnimationCancel(Animator animation) {
					super.onAnimationCancel(animation);
					mBubbleView.setVisibility(GONE);
					mBubbleAnimator = null;
				}
			});
    }

    private void showScrollbar() {
		if (vertical) {
			if (mRecyclerView.computeVerticalScrollRange() - mLength > 0) {
				float transX = getResources().getDimensionPixelSize(R.dimen.fastscroll_scrollbar_padding);

				mScrollbar.setTranslationX(transX);
				mScrollbar.setVisibility(VISIBLE);
				mScrollbarAnimator = mScrollbar.animate().translationX(0f).alpha(1f)
					.setDuration(sScrollbarAnimDuration)
					.setListener(new AnimatorListenerAdapter() {
						// adapter required for new alpha value to stick
					});
			}
		} else {
			if (mRecyclerView.computeHorizontalScrollRange() - mLength > 0) {
				float transY = getResources().getDimensionPixelSize(R.dimen.fastscroll_scrollbar_padding);

				mScrollbar.setTranslationY(transY);
				mScrollbar.setVisibility(VISIBLE);
				mScrollbarAnimator = mScrollbar.animate().translationY(0f).alpha(1f)
					.setDuration(sScrollbarAnimDuration)
					.setListener(new AnimatorListenerAdapter() {
						// adapter required for new alpha value to stick
					});
			}
		}

    }

    private void hideScrollbar() {
		if (vertical) {
			float transX = getResources().getDimensionPixelSize(R.dimen.fastscroll_scrollbar_padding);

			mScrollbarAnimator = mScrollbar.animate().translationX(transX).alpha(0f)
				.setDuration(sScrollbarAnimDuration)
				.setListener(new AnimatorListenerAdapter() {

					@Override
					public void onAnimationEnd(Animator animation) {
						super.onAnimationEnd(animation);
						mScrollbar.setVisibility(GONE);
						mScrollbarAnimator = null;
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						super.onAnimationCancel(animation);
						mScrollbar.setVisibility(GONE);
						mScrollbarAnimator = null;
					}
				});
		} else {
			float transY = getResources().getDimensionPixelSize(R.dimen.fastscroll_scrollbar_padding);

			mScrollbarAnimator = mScrollbar.animate().translationY(transY).alpha(0f)
				.setDuration(sScrollbarAnimDuration)
				.setListener(new AnimatorListenerAdapter() {

					@Override
					public void onAnimationEnd(Animator animation) {
						super.onAnimationEnd(animation);
						mScrollbar.setVisibility(GONE);
						mScrollbarAnimator = null;
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						super.onAnimationCancel(animation);
						mScrollbar.setVisibility(GONE);
						mScrollbarAnimator = null;
					}
				});
		}

    }

    private void setHandleSelected(final boolean selected) {
        mHandleView.setSelected(selected);
        DrawableCompat.setTint(mHandleImage, selected ? mBubbleColor : mHandleColor);
    }

    private void layout(final Context context, final AttributeSet attrs) {
        inflate(context, R.layout.fastscroller, this);

        setClipChildren(false);

		mBubbleView = (TextView) findViewById(R.id.fastscroll_bubble);
        mHandleView = (ImageView) findViewById(R.id.fastscroll_handle);
        mTrackView = (ImageView) findViewById(R.id.fastscroll_track);
        mScrollbar = (ViewGroup) findViewById(R.id.fastscroll_scrollbar);

		@ColorInt int bubbleColor = Color.GRAY;
        @ColorInt int handleColor = Color.DKGRAY;
        @ColorInt int trackColor = Color.LTGRAY;
        @ColorInt int textColor = Color.WHITE;

        boolean hideScrollbar = true;
        boolean showTrack = false;
		boolean vertical = true;

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FastScrollRecyclerView, 0, 0);

            if (typedArray != null) {
                try {
                    bubbleColor = typedArray.getColor(R.styleable.FastScrollRecyclerView_bubbleColor, bubbleColor);
                    handleColor = typedArray.getColor(R.styleable.FastScrollRecyclerView_handleColor, handleColor);
                    trackColor = typedArray.getColor(R.styleable.FastScrollRecyclerView_trackColor, trackColor);
                    textColor = typedArray.getColor(R.styleable.FastScrollRecyclerView_bubbleTextColor, textColor);
                    showTrack = typedArray.getBoolean(R.styleable.FastScrollRecyclerView_showTrack, showTrack);
                    hideScrollbar = typedArray.getBoolean(R.styleable.FastScrollRecyclerView_hideScrollbar, hideScrollbar);
					vertical = typedArray.getBoolean(R.styleable.FastScrollRecyclerView_vertical, vertical);
					center = typedArray.getBoolean(R.styleable.FastScrollRecyclerView_center, false);
				} finally {
                    typedArray.recycle();
                }
            }
        }

        this.vertical = vertical;
        if (vertical) {
			setOrientation(HORIZONTAL);
		} else {
			setOrientation(VERTICAL);
		}

        if (vertical) {
			mScrollbar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT);
			layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
			mTrackView.setLayoutParams(layoutParams);
			layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
			mHandleView.setLayoutParams(layoutParams);
		} else {
			mScrollbar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.CENTER_VERTICAL;
			mTrackView.setLayoutParams(layoutParams);
			layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.CENTER_VERTICAL;
			mHandleView.setLayoutParams(layoutParams);
		}

		setTrackColor(trackColor);
        setHandleColor(handleColor);
        setBubbleColor(bubbleColor);
        setBubbleTextColor(textColor);
        setHideScrollbar(hideScrollbar);
        setTrackVisible(showTrack);
    }
}
