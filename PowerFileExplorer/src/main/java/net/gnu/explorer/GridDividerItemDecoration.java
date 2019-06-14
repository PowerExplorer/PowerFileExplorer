package net.gnu.explorer;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.content.res.*;
import android.content.*;
import android.support.v7.widget.*;
import android.util.*;
import android.graphics.Color;
import android.graphics.PorterDuff;
import net.gnu.common.*;

/**
 * @author dgreenhalgh
 * Adds interior dividers to a RecyclerView with a GridLayoutManager.
 */
public class GridDividerItemDecoration extends RecyclerView.ItemDecoration {

    static final int[] ATTRS = new int[]{
		android.R.attr.dividerHorizontal,
		android.R.attr.dividerVertical,
		android.R.attr.listDivider
	};

	private Drawable mHorizontalDivider = null;
    private Drawable mVerticalDivider = null;
    private int mNumColumns = -1;

    /**
     * Sole constructor. Takes in {@link Drawable} objects to be used as
     * horizontal and vertical dividers.
     *
     * @param horizontalDivider A divider {@code Drawable} to be drawn on the
     *                          rows of the grid of the RecyclerView
     * @param verticalDivider A divider {@code Drawable} to be drawn on the
     *                        columns of the grid of the RecyclerView
     * @param numColumns The number of columns in the grid of the RecyclerView
     */
    public GridDividerItemDecoration(final Drawable horizontalDivider, final Drawable verticalDivider) {
        mHorizontalDivider = horizontalDivider;
        mVerticalDivider = verticalDivider;
    }

    public GridDividerItemDecoration(final Context context) {
        final TypedArray ta = context.obtainStyledAttributes(ATTRS);
        mHorizontalDivider = ta.getDrawable(0);
        mVerticalDivider = ta.getDrawable(1);
        ta.recycle();
    }

	public GridDividerItemDecoration(final Context context, final boolean horizontal) {
        final TypedArray ta = context.obtainStyledAttributes(ATTRS);
		if (horizontal) {
			mVerticalDivider = ta.getDrawable(2);
			mVerticalDivider.setColorFilter(Constants.DIVIDER_COLOR, PorterDuff.Mode.OVERLAY);
		} else {
			mHorizontalDivider = ta.getDrawable(2);
			mHorizontalDivider.setColorFilter(Constants.DIVIDER_COLOR, PorterDuff.Mode.OVERLAY);
		}
        ta.recycle();
    }

	/**
     * Draws horizontal and/or vertical dividers onto the parent RecyclerView.
     *
     * @param canvas The {@link Canvas} onto which dividers will be drawn
     * @param parent The RecyclerView onto which dividers are being added
     * @param state The current RecyclerView.State of the RecyclerView
     */
    @Override
    public void onDraw(final Canvas canvas, final RecyclerView parent, final RecyclerView.State state) {
		if (mHorizontalDivider != null) {
			drawHorizontalDividers(canvas, parent);
		}
		if (mVerticalDivider != null) {
			drawVerticalDividers(canvas, parent);
		}
    }

    /**
     * Determines the size and location of offsets between items in the parent
     * RecyclerView.
     *
     * @param outRect The {@link Rect} of offsets to be added around the child view
     * @param view The child view to be decorated with an offset
     * @param parent The RecyclerView onto which dividers are being added
     * @param state The current RecyclerView.State of the RecyclerView
     */
    @Override
    public void getItemOffsets(final Rect outRect, final View view, final RecyclerView parent, final RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

		if (mNumColumns <= 0) {
			mNumColumns = ((GridLayoutManager)parent.getLayoutManager()).getSpanCount();
		}
		final boolean childIsInLeftmostColumn = (parent.getChildAdapterPosition(view) % mNumColumns) == 0;
        if (!childIsInLeftmostColumn && mHorizontalDivider != null) {
            outRect.left = mHorizontalDivider.getIntrinsicWidth();
        }

        final boolean childIsInFirstRow = (parent.getChildAdapterPosition(view)) < mNumColumns;
        if (!childIsInFirstRow && mVerticalDivider != null) {
            outRect.top = mVerticalDivider.getIntrinsicHeight();
        }
    }

    /**
     * Adds horizontal dividers to a RecyclerView with a GridLayoutManager or
     * its subclass.
     *
     * @param canvas The {@link Canvas} onto which dividers will be drawn
     * @param parent The RecyclerView onto which dividers are being added
     */
    private void drawHorizontalDividers(final Canvas canvas, final RecyclerView parent) {
        final int parentTop = parent.getPaddingTop();
        final int parentBottom = parent.getHeight() - parent.getPaddingBottom();
		RecyclerView.LayoutParams params;
		int parentLeft;
		int parentRight;
        if (mNumColumns <= 0) {
			mNumColumns = ((GridLayoutManager)parent.getLayoutManager()).getSpanCount();
		}
		//Log.d("GridDividerItemDecoration", "drawHorizontalDividers parentTop=" + parentTop + ", parentBottom=" + parentBottom + ", mNumColumns=" + mNumColumns);
		View child;
		for (int i = 0; i < mNumColumns; i++) {
            child = parent.getChildAt(i);
			if (child != null) {
				params = (RecyclerView.LayoutParams) child.getLayoutParams();

				parentLeft = child.getRight() + params.rightMargin;
				parentRight = parentLeft + mHorizontalDivider.getIntrinsicWidth();

				mHorizontalDivider.setBounds(parentLeft, parentTop, parentRight, parentBottom);
				mHorizontalDivider.draw(canvas);
			}
        }
    }

    /**
     * Adds vertical dividers to a RecyclerView with a GridLayoutManager or its
     * subclass.
     *
     * @param canvas The {@link Canvas} onto which dividers will be drawn
     * @param parent The RecyclerView onto which dividers are being added
     */
    private void drawVerticalDividers(final Canvas canvas, final RecyclerView parent) {
        final int parentLeft = parent.getPaddingLeft();
        final int parentRight = parent.getWidth() - parent.getPaddingRight();
		if (mNumColumns <= 0) {
			mNumColumns = ((GridLayoutManager)parent.getLayoutManager()).getSpanCount();
		}
		final int width = (parentRight - parentLeft)/mNumColumns;
		
        final int childCount = parent.getChildCount();
        final int numChildrenOnLastRow = childCount % mNumColumns;
        int numRows = childCount / mNumColumns;
        if (numChildrenOnLastRow == 0) { // TODO: Replace this with math
            numRows--;
        }
//		Log.d("GridDividerItemDecoration", "drawVerticalDividers childCount=" + childCount + ", numChildrenOnLastRow=" + numChildrenOnLastRow + ", numRows=" + numRows + ", mNumColumns=" + mNumColumns
//			  + ", parentLeft=" + parentLeft + ", parentRight=" + parentRight);
		View child;
		RecyclerView.LayoutParams params;
		int parentTop;
		int parentBottom;
        for (int i = 0; i <= numRows; i++) {
			for (int j = 0; j < mNumColumns; j++) {
				child = parent.getChildAt(i * mNumColumns + j);
				if (child != null) {
					params = (RecyclerView.LayoutParams) child.getLayoutParams();

					parentTop = child.getBottom() + params.bottomMargin;
					parentBottom = parentTop + mVerticalDivider.getIntrinsicHeight();

					mVerticalDivider.setBounds(parentLeft + width*j, parentTop, parentLeft + width*(j+1), parentBottom);
					mVerticalDivider.draw(canvas);
				}
			}
            
        }
    }
}
