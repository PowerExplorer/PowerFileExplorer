
package jp.sblo.pandora.jota;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import net.gnu.explorer.R;

public class ColorPickerDialog extends Dialog {

    public interface OnColorChangedListener {
        void colorChanged(int fg , int bg);
    }

    private OnColorChangedListener mListener;
    private String mTitle;
    private boolean mBgMode;
    private int mBgColor;
    private int mFgColor;


    private static class ColorPickerView extends View {

    	private Paint mPaint, mPaintC;
        private Paint mOKPaint;
        private Paint mTextPaint;
        private final int[] mColors;
        private int[] mChroma;
        private OnColorChangedListener mListener;
        private Shader sg, lg;
        private int selectColor;
        private float selectHue = 0;
        private String mLabelOk;
        int bgColor;
        int fgColor;
        boolean mBgMode;


        ColorPickerView(Context c, OnColorChangedListener l, int fg , int bg , boolean bgmode) {
            super(c);
            mListener = l;
            bgColor = bg;
            fgColor = fg;
            mBgMode = bgmode;

            if ( mBgMode ){
                selectColor = bg;
            }else{
                selectColor = fg;
            }

            selectHue = getHue(selectColor);
            mColors = new int[] {
                0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00,
                0xFFFFFF00, 0xFFFF0000
            };

            mChroma = new int[] {
            		0xFF000000, 0xFF888888, 0xFFFFFFFF
            };

            sg = new SweepGradient(0, 0, mColors, null);
            lg = new LinearGradient(OK_X0, 0, OK_X1, 0, mChroma, null, Shader.TileMode.CLAMP);

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setShader(sg);
            mPaint.setStrokeWidth(CENTER_RADIUS);

            mPaintC = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaintC.setStyle(Paint.Style.FILL);
            mPaintC.setShader(lg);
            mPaintC.setStrokeWidth(2);

            mOKPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mOKPaint.setStyle(Paint.Style.FILL);
            mOKPaint.setColor(bgColor);
            mOKPaint.setStrokeWidth(5);

            mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mTextPaint.setColor(fgColor);
            mTextPaint.setTextSize(20);

            mLabelOk = getContext().getString(R.string.label_ok);


            float colorickersize = getContext().getResources().getDimension(R.dimen.color_picker_size);

            CENTER_X = (int)colorickersize;
            CENTER_Y = (int)colorickersize;
            CENTER_RADIUS = 24;
            OK_X0 = - CENTER_X/2;
            OK_X1 =   CENTER_X/2;
            OK_Y0 = (float) (CENTER_X * 1.2);
            OK_Y1 = (float) (CENTER_X * 1.5);
}

        private boolean mTrackingOK;
        private boolean mHighlightOK;

        private int CENTER_X = 150;
        private int CENTER_Y = 150;
        private int CENTER_RADIUS = 24;
        private float OK_X0 = - CENTER_X/2;
        private float OK_X1 =   CENTER_X/2;
        private float OK_Y0 = (float) (CENTER_X * 1.2);
        private float OK_Y1 = (float) (CENTER_X * 1.5);

        private void drawSVRegion(Canvas canvas) {
        	final float RESOLUTION = (float)0.01;

        	for(float y = 0; y < 1; y += RESOLUTION) {
            	mChroma = new int[10];

            	int i = 0;
            	for(float x = 0; i < 10; x += 0.1, i+=1) {
            		mChroma[i] = setHSVColor(selectHue, x, y);
            	}
                lg = new LinearGradient(OK_X0, 0, OK_X1, 0, mChroma, null, Shader.TileMode.CLAMP);
                mPaintC.setShader(lg);

                //canvas.drawRect(OK_X0, OK_X0 + (CENTER_X * y), OK_X1, OK_X0 + (float)(CENTER_X * (y)), mPaintC);
            	canvas.drawLine(OK_X0, OK_X0 + (CENTER_X * y), OK_X1, OK_X0 + (float)(CENTER_X * (y)), mPaintC);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float r = CENTER_X - mPaint.getStrokeWidth() * 0.5f;

            canvas.translate(CENTER_X, CENTER_X);
            canvas.drawOval(new RectF(-r, -r, r, r), mPaint);

            drawSVRegion(canvas);

            canvas.drawRoundRect(new RectF(OK_X0, OK_Y0, OK_X1, OK_Y1), 5, 5, mOKPaint);

            canvas.drawText( mLabelOk , 0 - 14, (float) (CENTER_X * 1.4) + 2, mTextPaint);

            if (mTrackingOK) {
                int c = mOKPaint.getColor();
                mOKPaint.setStyle(Paint.Style.STROKE);

                if (mHighlightOK)
                    mOKPaint.setAlpha(0xFF);
                else
                    mOKPaint.setAlpha(0x80);

                float padding = 5;
                //canvas.drawCircle(0, 0, CENTER_RADIUS + mOKPaint.getStrokeWidth(), mOKPaint);
                canvas.drawRoundRect(new RectF(OK_X0 - padding, OK_Y0 - padding, OK_X1 + padding, OK_Y1 + padding), 5, 5, mOKPaint);
                mOKPaint.setStyle(Paint.Style.FILL);
                mOKPaint.setColor(c);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(CENTER_X * 2, (int)(CENTER_Y * 2.8));
        }

//        private int floatToByte(float x) {
//            int n = java.lang.Math.round(x);
//            return n;
//        }
//
//        private int pinToByte(int n) {
//            if (n < 0)
//                n = 0;
//            else if (n > 255)
//                n = 255;
//            return n;
//        }

        private float getHue(int color) {
        	float hsv[] = new float[3];
        	Color.colorToHSV(color, hsv);
        	return hsv[0];
        }

        private int ave(int s, int d, float p) {
            return s + java.lang.Math.round(p * (d - s));
        }

        private int interpColor(int colors[], float unit) {
            if (unit <= 0) {
                return colors[0];
            }
            if (unit >= 1) {
                return colors[colors.length - 1];
            }

            float p = unit * (colors.length - 1);
            int i = (int)p;
            p -= i;

            // now p is just the fractional part [0...1) and i is the index
            int c0 = colors[i];
            int c1 = colors[i+1];
            int a = ave(Color.alpha(c0), Color.alpha(c1), p);
            int r = ave(Color.red(c0),   Color.red(c1),   p);
            int g = ave(Color.green(c0), Color.green(c1), p);
            int b = ave(Color.blue(c0),  Color.blue(c1),  p);

            return Color.argb(a, r, g, b);
        }

//        private int rotateColor(int color, float rad) {
//            float deg = rad * 180 / PI;
//            int r = Color.red(color);
//            int g = Color.green(color);
//            int b = Color.blue(color);
//
//            ColorMatrix cm  = new ColorMatrix();
//            ColorMatrix tmp = new ColorMatrix();
//
//            cm.setRGB2YUV();
//            tmp.setRotate(0, deg);
//            cm.postConcat(tmp);
//            tmp.setYUV2RGB();
//            cm.postConcat(tmp);
//
//            final float[] a = cm.getArray();
//
//            int ir = floatToByte(a[0] * r +  a[1] * g +  a[2] * b);
//            int ig = floatToByte(a[5] * r +  a[6] * g +  a[7] * b);
//            int ib = floatToByte(a[10] * r + a[11] * g + a[12] * b);
//
//            return Color.argb(Color.alpha(color), pinToByte(ir), pinToByte(ig), pinToByte(ib));
//        }

        private int setHSVColor(float hue, float saturation, float value) {
            float[] hsv = new float[3];
            if(hue >= 360)
            	hue = 359;
            else if(hue < 0)
            	hue = 0;

            if(saturation > 1)
            	saturation = 1;
            else if(saturation < 0)
            	saturation = 0;

            if(value > 1)
            	value = 1;
            else if(value < 0)
            	value = 0;

            hsv[0] = hue;
            hsv[1] = saturation;
            hsv[2] = value;

            return Color.HSVToColor(hsv);
        }

        private static final float PI = 3.1415927f;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX() - CENTER_X;
            float y = event.getY() - CENTER_Y;
            float r = (float)(java.lang.Math.sqrt(x*x + y*y));
            boolean inOK = false;
            boolean inOval = false;
            boolean inRect = false;

            if(r <= CENTER_X) {
            	if(r > CENTER_X - CENTER_RADIUS)
            		inOval = true;
            	else if(x >= OK_X0 && x < OK_X1 && y >= OK_X0 && y < OK_X1)
            		inRect = true;
            }
            else if(x >= OK_X0 && x < OK_X1 && y >= OK_Y0 && y < OK_Y1){
            	inOK = true;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTrackingOK = inOK;
                    if (inOK) {
                        mHighlightOK = true;
                        invalidate();
                        break;
                    }
                case MotionEvent.ACTION_MOVE:
                    if (mTrackingOK) {
                        if (mHighlightOK != inOK) {
                            mHighlightOK = inOK;
                            invalidate();
                        }
                    }
                    else if(inOval) {
                        float angle = (float)java.lang.Math.atan2(y, x);
                        // need to turn angle [-PI ... PI] into unit [0....1]
                        float unit = angle/(2*PI);
                        if (unit < 0) {
                            unit += 1;
                        }
                        selectColor = interpColor(mColors, unit);
                        if ( mBgMode ){
                            mOKPaint.setColor(selectColor);
                            bgColor = selectColor;
                        }else{
                            mTextPaint.setColor(selectColor);
                            fgColor = selectColor;
                        }
                        //mChroma[1] = selectColor;
                        selectHue = getHue(selectColor);
                        //lg = new LinearGradient(OK_X0, 0, OK_X1, 0, mChroma, null, Shader.TileMode.CLAMP);
                        //mPaintC.setShader(lg);
                        invalidate();
                    }
                    else if(inRect){
                    	int selectColor2 = setHSVColor(selectHue, (x - OK_X0)/CENTER_X, (y - OK_X0)/CENTER_Y);
                    	selectColor = selectColor2;
                        if ( mBgMode ){
                            mOKPaint.setColor(selectColor);
                            bgColor = selectColor;
                        }else{
                            mTextPaint.setColor(selectColor);
                            fgColor = selectColor;
                        }
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mTrackingOK) {
                        if (inOK) {
                            mListener.colorChanged(fgColor , bgColor);
                        }
                        mTrackingOK = false;    // so we draw w/o halo
                        invalidate();
                    }
                    break;
            }
            return true;
        }
    }


    public ColorPickerDialog(Context context, OnColorChangedListener listener, int fgColor , int bgColor , boolean bgmode , String title ) {
    	super(context);
    	mListener = listener;
    	mBgMode = bgmode;
    	mBgColor = bgColor;
    	mFgColor = fgColor;
    	mTitle = title;
    }

    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnColorChangedListener l = new OnColorChangedListener() {
            public void colorChanged(int fg , int bg) {
                mListener.colorChanged( fg , bg );
                dismiss();
            }
        };

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        setContentView(new ColorPickerView(getContext(), l, mFgColor ,  mBgColor , mBgMode ), lp);
        setTitle(mTitle);
    }

}
