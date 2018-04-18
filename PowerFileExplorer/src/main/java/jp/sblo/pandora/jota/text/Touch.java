/*
 * Copyright (C) 2008 The Android Open Source Project
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

package jp.sblo.pandora.jota.text;

import jp.sblo.pandora.jota.text.Layout.Alignment;
import android.content.Context;
import android.text.NoCopySpan;
import android.text.Spannable;
import android.text.method.MetaKeyKeyListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.Scroller;

public class Touch {

    private static int sLineNumberWidth;    // Jota Text Editor

    private Touch() {
    }
    /**
     * Scrolls the specified widget to the specified coordinates, except
     * constrains the X scrolling position to the horizontal regions of
     * the text that will be visible after scrolling to the specified
     * Y position.
     */
    public static void scrollTo(TextView widget, Layout layout, int x, int y) {
        int padding = widget.getTotalPaddingTop() +
                      widget.getTotalPaddingBottom();
        int top = layout.getLineForVertical(y);
        int bottom = layout.getLineForVertical(y + widget.getHeight() -
                                               padding);

        int left = Integer.MAX_VALUE;
        int right = 0;
        Alignment a = null;

        for (int i = top; i <= bottom; i++) {
            left = (int) Math.min(left, layout.getLineLeft(i));
            right = (int) Math.max(right, layout.getLineRight(i)+sLineNumberWidth);    // Jota Text Editor

            if (a == null) {
                a = layout.getParagraphAlignment(i);
            }
        }

        padding = widget.getTotalPaddingLeft() + widget.getTotalPaddingRight();
        int width = widget.getWidth();
        int diff = 0;

        if (right - left < width - padding) {
            if (a == Alignment.ALIGN_CENTER) {
                diff = (width - padding - (right - left)) / 2;
            } else if (a == Alignment.ALIGN_OPPOSITE) {
                diff = width - padding - (right - left);
            }
        }

        x = Math.min(x, right - (width - padding) - diff);
        x = Math.max(x, left - diff);

        widget.scrollTo(x, y);
    }

    /**
     * @hide
     * Returns the maximum scroll value in x.
     */
    public static int getMaxScrollX(TextView widget, Layout layout, int y) {
        int top = layout.getLineForVertical(y);
        int bottom = layout.getLineForVertical(y + widget.getHeight()
                - widget.getTotalPaddingTop() -widget.getTotalPaddingBottom());
        int left = Integer.MAX_VALUE;
        int right = 0;
        for (int i = top; i <= bottom; i++) {
            left = (int) Math.min(left, layout.getLineLeft(i));
            right = (int) Math.max(right, layout.getLineRight(i)+sLineNumberWidth);    // Jota Text Editor
        }
        return right - left - widget.getWidth() - widget.getTotalPaddingLeft()
                - widget.getTotalPaddingRight();
    }

    /**
     * Handles touch events for dragging.  You may want to do other actions
     * like moving the cursor on touch as well.
     */
    public static boolean onTouchEvent(TextView widget, Spannable buffer,
                                       MotionEvent event) {
        DragState[] ds;

        ds = buffer.getSpans(0, buffer.length(), DragState.class);

        if ( ds.length > 0 ){
            if ( ds[0].mVelocityTracker == null) {
                ds[0].mVelocityTracker = VelocityTracker.obtain();
            }
            ds[0].mVelocityTracker.addMovement(event);
        }

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            if ( ds.length>0 ){
                if ( ds[0].mFlingRunnable != null ){
                    ds[0].mFlingRunnable.endFling();
                    widget.cancelLongPress();
                }
            }
            for (int i = 0; i < ds.length; i++) {
                buffer.removeSpan(ds[i]);
            }

            buffer.setSpan(new DragState(event.getX(), event.getY(),
                            widget.getScrollX(), widget.getScrollY()),
                    0, 0, Spannable.SPAN_MARK_MARK);

            return true;

        case MotionEvent.ACTION_UP:
        {
            boolean result = false;
//            for (int i = 0; i < ds.length; i++) {
//                buffer.removeSpan(ds[i]);
//            }
            boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                    KeyEvent.META_SHIFT_ON) == 1) ||
                    (JotaTextKeyListener.getMetaStateSelecting(buffer) != 0);

            if (ds.length > 0 && ds[0].mUsed) {
                result = true;
                if ( !cap ){
                    final VelocityTracker velocityTracker = ds[0].mVelocityTracker;
                    int mMinimumVelocity = ViewConfiguration.get(widget.getContext()).getScaledMinimumFlingVelocity();
                    int mMaximumVelocity = ViewConfiguration.get(widget.getContext()).getScaledMaximumFlingVelocity();
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    final int initialVelocity = (int) velocityTracker.getYVelocity();

                    if (Math.abs(initialVelocity) > mMinimumVelocity) {
                        if (ds[0].mFlingRunnable == null) {
                            ds[0].mFlingRunnable = new FlingRunnable( widget.getContext() );
                        }
    //                    reportScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);

                        ds[0].mFlingRunnable.start(widget , -initialVelocity);
                    } else {
    //                    mTouchMode = TOUCH_MODE_REST;
    //                    reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                        widget.moveCursorToVisibleOffset();
                    }
                }else{
                    widget.moveCursorToVisibleOffset();
                }
            } else {
                widget.moveCursorToVisibleOffset();
            }

            if (ds[0].mVelocityTracker != null) {
                ds[0].mVelocityTracker.recycle();
                ds[0].mVelocityTracker = null;
            }

            return result;
        }
        case MotionEvent.ACTION_MOVE:

            if (ds.length > 0) {
                if (ds[0].mFarEnough == false) {
                    int slop = ViewConfiguration.get(widget.getContext()).getScaledTouchSlop();

                    if (Math.abs(event.getX() - ds[0].mX) >= slop ||
                        Math.abs(event.getY() - ds[0].mY) >= slop) {
                        ds[0].mFarEnough = true;
                    }
                }

                if (ds[0].mFarEnough) {
                    ds[0].mUsed = true;
                    boolean cap = (MetaKeyKeyListener.getMetaState(buffer,
                                   KeyEvent.META_SHIFT_ON) == 1) ||
                                   (MetaKeyKeyListener.getMetaState(buffer,
                                    MetaKeyKeyListener.META_SELECTING) != 0);
                    float dx;
                    float dy;
                    if (cap) {
                        // if we're selecting, we want the scroll to go in
                        // the direction of the drag
                        dx = event.getX() - ds[0].mX;
                        dy = event.getY() - ds[0].mY;
                    } else {
                        dx = ds[0].mX - event.getX();
                        dy = ds[0].mY - event.getY();
                    }
                    ds[0].mX = event.getX();
                    ds[0].mY = event.getY();

                    int nx = widget.getScrollX() + (int) dx;
                    int ny = widget.getScrollY() + (int) dy;

                    int padding = widget.getTotalPaddingTop() +
                                  widget.getTotalPaddingBottom();
                    Layout layout = widget.getLayout();

                    ny = Math.min(ny, layout.getHeight() - (widget.getHeight() -
                                                            padding));
                    ny = Math.max(ny, 0);

                    int oldX = widget.getScrollX();
                    int oldY = widget.getScrollY();

                    scrollTo(widget, layout, nx, ny);

                    // If we actually scrolled, then cancel the up action.
                    if (oldX != widget.getScrollX()
                            || oldY != widget.getScrollY()) {
                        widget.cancelLongPress();
                    }

                    return true;
                }
            }
        }

        return false;
    }

    public static int getInitialScrollX(TextView widget, Spannable buffer) {
        DragState[] ds = buffer.getSpans(0, buffer.length(), DragState.class);
        return ds.length > 0 ? ds[0].mScrollX : -1;
    }

    public static int getInitialScrollY(TextView widget, Spannable buffer) {
        DragState[] ds = buffer.getSpans(0, buffer.length(), DragState.class);
        return ds.length > 0 ? ds[0].mScrollY : -1;
    }

    public static void cancelFling(TextView widget, Spannable buffer)
    {
        DragState[] ds;

        ds = buffer.getSpans(0, buffer.length(), DragState.class);

        if ( ds.length > 0 ){
            if ( ds[0].mFlingRunnable != null ){
                ds[0].mFlingRunnable.endFling();
                widget.cancelLongPress();
            }
        }

    }


    private static class DragState implements NoCopySpan {
        public float mX;
        public float mY;
        public int mScrollX;
        public int mScrollY;
        public boolean mFarEnough;
        public boolean mUsed;
        public VelocityTracker mVelocityTracker;
        public FlingRunnable mFlingRunnable;

        public DragState(float x, float y, int scrollX, int scrollY) {
            mX = x;
            mY = y;
            mScrollX = scrollX;
            mScrollY = scrollY;
            mVelocityTracker = null;
            mFlingRunnable = null;
        }
    }

    /**
     * Responsible for fling behavior. Use {@link #start(int)} to
     * initiate a fling. Each frame of the fling is handled in {@link #run()}.
     * A FlingRunnable will keep re-posting itself until the fling is done.
     *
     */
    private static class FlingRunnable implements Runnable {

        static final int TOUCH_MODE_REST = -1;
        static final int TOUCH_MODE_FLING = 3;

        int mTouchMode = TOUCH_MODE_REST;

        /**
         * Tracks the decay of a fling scroll
         */
        private final Scroller mScroller;

        /**
         * Y value reported by mScroller on the previous fling
         */
        private int mLastFlingY;

        private TextView mWidget=null;

        FlingRunnable(Context context) {
            mScroller = new Scroller(context);
        }

        void start(TextView parent , int initialVelocity) {
            mWidget = parent;
            int initialX = parent.getScrollX(); //initialVelocity < 0 ? Integer.MAX_VALUE : 0;
            int initialY = parent.getScrollY(); //initialVelocity < 0 ? Integer.MAX_VALUE : 0;
            mLastFlingY = initialY;
            mScroller.fling(initialX, initialY, 0, initialVelocity,
                    0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            mTouchMode = TOUCH_MODE_FLING;

            mWidget.post(this);

//            if (PROFILE_FLINGING) {
//                if (!mFlingProfilingStarted) {
//                    Debug.startMethodTracing("AbsListViewFling");
//                    mFlingProfilingStarted = true;
//                }
//            }
        }

//        void startScroll(int distance, int duration) {
//            int initialY = distance < 0 ? Integer.MAX_VALUE : 0;
//            mLastFlingY = initialY;
//            mScroller.startScroll(0, initialY, 0, distance, duration);
//            mTouchMode = TOUCH_MODE_FLING;
//            post(this);
//        }

        private void endFling() {
            mTouchMode = TOUCH_MODE_REST;

//            reportScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
//            clearScrollingCache();

            if ( mWidget != null ){
                mWidget.removeCallbacks(this);
                mWidget.moveCursorToVisibleOffset();

                mWidget = null;
            }

//            if (mPositionScroller != null) {
//                removeCallbacks(mPositionScroller);
//            }
        }

        public void run() {
            switch (mTouchMode) {
            default:
                return;

            case TOUCH_MODE_FLING: {
//                if (mItemCount == 0 || getChildCount() == 0) {
//                    endFling();
//                    return;
//                }

                final Scroller scroller = mScroller;
                boolean more = scroller.computeScrollOffset();

                int x = scroller.getCurrX();
                int y = scroller.getCurrY();


//                // Pretend that each frame of a fling scroll is a touch scroll
//                if (delta > 0) {
//                    // List is moving towards the top. Use first view as mMotionPosition
//                    mMotionPosition = mFirstPosition;
//                    final View firstView = getChildAt(0);
//                    mMotionViewOriginalTop = firstView.getTop();
//
//                    // Don't fling more than 1 screen
//                    delta = Math.min(getHeight() - mPaddingBottom - mPaddingTop - 1, delta);
//                } else {
//                    // List is moving towards the bottom. Use last view as mMotionPosition
//                    int offsetToLast = getChildCount() - 1;
//                    mMotionPosition = mFirstPosition + offsetToLast;
//
//                    final View lastView = getChildAt(offsetToLast);
//                    mMotionViewOriginalTop = lastView.getTop();
//
//                    // Don't fling more than 1 screen
//                    delta = Math.max(-(getHeight() - mPaddingBottom - mPaddingTop - 1), delta);
//                }

                Layout layout = mWidget.getLayout();

                int padding = mWidget.getTotalPaddingTop() +
                                mWidget.getTotalPaddingBottom();

                y = Math.min(y, layout.getHeight() - (mWidget.getHeight() -
                                                        padding));
                y = Math.max(y, 0);
//                final boolean atEnd = trackMotionScroll(delta, delta);

                Touch.scrollTo( mWidget , layout , x , y );
                int delta = mLastFlingY - y;

                if (more && delta != 0) {
                    mWidget.invalidate();
                    mLastFlingY = y;
                    mWidget.post(this);
                } else {
                    endFling();

//                    if (PROFILE_FLINGING) {
//                        if (mFlingProfilingStarted) {
//                            Debug.stopMethodTracing();
//                            mFlingProfilingStarted = false;
//                        }
//                    }
                }
                break;
            }
            }

        }
    }
    // Jota Text Editor
    public static void setLineNumberWidth( int lineNumberWidth )
    {
        sLineNumberWidth = lineNumberWidth;
    }

}
