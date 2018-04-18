package net.gnu.explorer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by Jade Byfield on 3/29/2014.
 */

// ImageView that draws a grid on top of it's canvas
public class ZoomEditTextView extends EditText {

	// touch tools
	private static final int INVALID_POINTER_ID = -1;
	private ScaleGestureDetector mScaleDetector;
	private float mScaleFactor = 1.f;
	// The �active pointer� is the one currently moving our object.
	private int mActivePointerId = INVALID_POINTER_ID;

	private float mPosX;
	private float mPosY;
	private float mLastTouchX;
	private float mLastTouchY;

	private Bitmap mBitmap = null;
	private Paint mPaint;
	private Path mPath;
	private Paint mBitmapPaint;
	private Paint mCirclePaint;
	private Path mCirclePath;
	private Paint mDrawingPaint;
	private List<Path> mPaths = new ArrayList<Path>();
	private List<Paint> mPaints = new ArrayList<Paint>();
	private float mBrushSize = 12.0f;
	private Drawable mDrawable;
	private Paint mRectPaint;
	private Context mContext;

	public ZoomEditTextView(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
		this.mContext = context;
		init();

	}

	public ZoomEditTextView(Context context) {
		super(context, null, 0);
		this.mContext = context;
		init();

		// this.setOnTouchListener(new OnTouchListener() {
		//
		// @Override
		// public boolean onTouch(View v, MotionEvent me) {
		//
		// InputMethodManager imm = (InputMethodManager) mContext
		// .getSystemService(mContext.INPUT_METHOD_SERVICE);
		// imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
		//
		// return true;
		// }
		//
		// });

	}

	private void init() {

		// Create our ScaleGestureDetector
		mScaleDetector = new ScaleGestureDetector(mContext, new ScaleListener());

		// Sets up drawing tools
		mPath = new Path();
		mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		mCirclePaint = new Paint();
		mCirclePath = new Path();
		mCirclePaint.setAntiAlias(true);
		mCirclePaint.setColor(Color.CYAN);
		mCirclePaint.setStyle(Paint.Style.STROKE);
		mCirclePaint.setStrokeJoin(Paint.Join.MITER);
		mCirclePaint.setStrokeWidth(4f);

		//

		mDrawingPaint = new Paint();

		mDrawingPaint.setAntiAlias(true);
		mDrawingPaint.setDither(true);
		mDrawingPaint.setColor(Color.GREEN);
		mDrawingPaint.setStyle(Paint.Style.STROKE);
		mDrawingPaint.setStrokeJoin(Paint.Join.ROUND);
		mDrawingPaint.setStrokeCap(Paint.Cap.ROUND);
		mDrawingPaint.setStrokeWidth(mBrushSize);

		mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mRectPaint.setStyle(Paint.Style.STROKE);
		mRectPaint.setColor(Color.WHITE);

		mDrawable = mContext.getResources().getDrawable(R.drawable.ic_launcher);
		mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(),
				mDrawable.getIntrinsicHeight());
		// setDrawingCacheEnabled(true);
		// buildDrawingCache();

	}

	@Override
	protected void onDraw(Canvas canvas) {

		canvas.save();
		//
		canvas.translate(mPosX, mPosY);
		canvas.scale(mScaleFactor, mScaleFactor);
		// canvas.drawRect(new Rect(0, 0, canvas.getWidth(),
		// canvas.getHeight()), mRectPaint);

		super.onDraw(canvas);

		// mDrawable.draw(canvas);
		// if (mBitmap != null && canvas != null) {
		// canvas.drawBitmap(mBitmap, mPosX, mPosY, mPaint);
		// }

		canvas.restore();

	}

	// Listen for multi-touch drag event and redraw the view accordingly
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Let the ScaleGestureDetector inspect all events.
		mScaleDetector.onTouchEvent(event);

		final int action = event.getAction();
		switch (action) {

		// a touch down
		case MotionEvent.ACTION_DOWN: {
			// Scale detector is not in progress
			if (!mScaleDetector.isInProgress()) {
				final float x = event.getX();
				final float y = event.getY();
				// Save the ID of this pointer
				mActivePointerId = event.getPointerId(0);

				// Remember where we started
				mLastTouchX = x;
				mLastTouchY = y;

				// Save the ID of this pointer
				mActivePointerId = event.getPointerId(0);

				break;

			}
		}

		case MotionEvent.ACTION_MOVE: {

			// Only move the image if the scale detector is not in progress
			if (!mScaleDetector.isInProgress()) {
				// Find the index of active pointer and save its position
				final int pointerIndex = event
						.findPointerIndex(mActivePointerId);
				final float x = event.getX(pointerIndex);
				final float y = event.getY(pointerIndex);

				// mBitmap = getDrawingCache();

				// Calculate the distance moved
				float dx = x - mLastTouchX;
				float dy = y - mLastTouchY;

				// Move the object
				mPosX += dx;
				mPosY += dy;

				// Remember this touch position for the next move event
				mLastTouchX = x;
				mLastTouchY = y;

				// Invalidate to request a redraw
				invalidate();

				// break;

			} /*
			 * else {
			 * 
			 * final float gx = mScaleDetector.getFocusX(); final float gy =
			 * mScaleDetector.getFocusY();
			 * 
			 * final float gdx = gx - mLastGestureX; final float gdy = gy -
			 * mLastGestureY;
			 * 
			 * mPosX += gdx; mPosY += gdy;
			 * 
			 * invalidate();
			 * 
			 * mLastGestureX = gx; mLastGestureY = gy;
			 * 
			 * }
			 */

			break;
		}

		case MotionEvent.ACTION_UP: {

			InputMethodManager imm = (InputMethodManager) mContext
					.getSystemService(mContext.INPUT_METHOD_SERVICE);
			imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);

			// Reset the active pointer id
			mActivePointerId = INVALID_POINTER_ID;

			break;
		}

		case MotionEvent.ACTION_CANCEL: {

			mActivePointerId = INVALID_POINTER_ID;
			break;

		}

		case MotionEvent.ACTION_POINTER_UP: {
			// Extract the index of the pointer that left the touch sensor
			final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			final int pointerId = event.getPointerId(pointerIndex);
			if (pointerId == mActivePointerId) {
				// This was our active pointer going up. Choose a new
				// active pointer and adjust accordingly.
				final int newPointerIndex = pointerIndex == 0 ? 1 : 0;

				if (event.getPointerCount() >= 2) {
					mLastTouchX = event.getX(newPointerIndex);
					mLastTouchY = event.getY(newPointerIndex);
				}
				mActivePointerId = event.getPointerId(newPointerIndex);

			} else {
				final int tempPointerIndex = event
						.findPointerIndex(mActivePointerId);
				mLastTouchX = event.getX(tempPointerIndex);
				mLastTouchY = event.getY(tempPointerIndex);
			}
			break;

		}

		}

		return true;
	}

	private class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScaleFactor *= detector.getScaleFactor();

			// Don't let the object get too small or too large.
			mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

			invalidate();

			return true;
		}
	}

}
