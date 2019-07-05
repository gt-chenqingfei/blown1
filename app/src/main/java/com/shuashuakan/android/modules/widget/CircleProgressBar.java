package com.shuashuakan.android.modules.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.luck.picture.lib.tools.ScreenUtils;

/**
 * @author: zhaoningqiang
 * @time: 2019/5/21
 * @Description:
 */
public class CircleProgressBar extends ProgressBar {
    private Paint mBackCirclePaint;
    private Paint mProgressCirclePaint;
    private int mCircleWidth;
    private RectF mRectF = new RectF();
    private float mProgress;

    public CircleProgressBar(Context context) {
        super(context);
        init(context);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }


    @Override
    public  void setProgress(int progress) {
        mProgress = progress;
        invalidate();
    }

    private void init(Context context) {
        mCircleWidth = ScreenUtils.dip2px(context, 1.5f);

        mBackCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackCirclePaint.setColor(0x22ffffff);
        mBackCirclePaint.setStyle(Paint.Style.STROKE);
        mBackCirclePaint.setStrokeWidth(mCircleWidth);


        mProgressCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressCirclePaint.setColor(0xffffffff);
        mProgressCirclePaint.setStrokeCap(Paint.Cap.ROUND);
        mProgressCirclePaint.setStyle(Paint.Style.STROKE);
        mProgressCirclePaint.setStrokeWidth(mCircleWidth);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int padding = (int) (mCircleWidth * 0.5f);
        mRectF.set(padding, padding, w - padding, h - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawArc(mRectF, 0, 360, false, mBackCirclePaint);
        float progress =  360f * (mProgress / getMax());
        canvas.drawArc(mRectF, 0, progress, false, mProgressCirclePaint);
    }
}
