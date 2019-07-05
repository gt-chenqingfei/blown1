package com.shuashuakan.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.util.AttributeSet;
import android.widget.TextView;

import com.shuashuakan.android.base.ui.R;


public class UnderLineTextView extends AppCompatTextView {

    private Rect mRect;
    private Paint mPaint;
    private int mColor;
    private float density;
    private float mStrokeWidth;
    private float mLineTopMargin=0;

    public UnderLineTextView(Context context) {
        this(context, null, 0);
    }
    public UnderLineTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public UnderLineTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr){
        density=context.getResources().getDisplayMetrics().density;
        TypedArray array=context.obtainStyledAttributes(attrs,R.styleable.UnderlinedTextView,defStyleAttr,0);
        mColor=array.getColor(R.styleable.UnderlinedTextView_underlineColor,0xFFFF0000);
        mStrokeWidth=array.getDimension(R.styleable.UnderlinedTextView_underlineWidth,density*2);
        mLineTopMargin=array.getDimension(R.styleable.UnderlinedTextView_underlineTopMargin,density*2);

        setLineSpacing(mLineTopMargin,(float) 1.5);
        setPadding(getLeft(),getTop(),getRight(),getBottom());

        array.recycle();




        mRect=new Rect();
        mPaint =new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mColor);
        mPaint.setStrokeWidth(mStrokeWidth);

        }

    @Override
    protected void onDraw(Canvas canvas) {
        int count=getLineCount();
        final Layout layout=getLayout();


        float x_start,x_stop,x_diff;
        int firstCharInLine, lastCharInLine;

        for (int i = 0; i < count; i++) {

            int baseline=getLineBounds(i,mRect);
           // mRect.bottom+=mLineTopMargin;
            firstCharInLine=layout.getLineStart(i);
            lastCharInLine = layout.getLineEnd(i);

            x_start = layout.getPrimaryHorizontal(firstCharInLine);
            x_diff = layout.getPrimaryHorizontal(firstCharInLine + 1) - x_start;
            x_stop = layout.getPrimaryHorizontal(lastCharInLine - 1) + x_diff;

            //canvas.drawLine(x_start,baseline+  mStrokeWidth,x_stop, baseline + mStrokeWidth, mPaint);
            canvas.drawLine(x_start,baseline+ mLineTopMargin+ mStrokeWidth,x_stop, baseline +mLineTopMargin+ mStrokeWidth, mPaint);
            //canvas.drawRect(x_start,baseline + mStrokeWidth,x_stop, baseline + mStrokeWidth, mPaint);


        }
        super.onDraw(canvas);

    }

    public int getUnderLineColor() {
        return mColor;
    }

    public void setUnderLineColor(int mColor) {
        this.mColor = mColor;
        invalidate();
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom+(int)mLineTopMargin+(int)mStrokeWidth);
    }

    public float getUnderlineWidth() {
        return mStrokeWidth;
    }

    public void setUnderlineWidth(float mStrokeWidth) {
        this.mStrokeWidth = mStrokeWidth;
        invalidate();
    }

}
