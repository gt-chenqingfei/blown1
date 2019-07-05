package com.shuashuakan.android.modules.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ProgressBar;

/**
 * Created by caizepeng on 16/9/6.
 */
public class ArcProgress extends ProgressBar {
  private int mBoardWidth = dp2px(7);
  private RectF mArcRectf;
  private Paint mArcPaint;
  private int mUnmProgressColor = 0xffeeeeee;

  public ArcProgress(Context context) {
    this(context, null);
  }

  public ArcProgress(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ArcProgress(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    mArcPaint.setColor(mUnmProgressColor);
    mArcPaint.setStrokeCap(Paint.Cap.ROUND);
    mArcPaint.setStrokeWidth(mBoardWidth);
    mArcPaint.setStyle(Paint.Style.STROKE);
  }

  @SuppressLint("DrawAllocation")
  @Override
  protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int width = getMeasuredWidth();
    int height = getMeasuredHeight();

    int size = Math.min(width, height);
    setMeasuredDimension(size, size);
    mArcRectf = new RectF(mBoardWidth, mBoardWidth, size - mBoardWidth, size - mBoardWidth);
  }

  @Override
  protected synchronized void onDraw(Canvas canvas) {
    canvas.save();
    float roate = getProgress() * 1.0f / getMax();

    int mDegree = 130;
    float targetmDegree = mDegree - mDegree * roate - 65;

    //绘制未完成部分
    mArcPaint.setColor(mUnmProgressColor);
    canvas.drawArc(mArcRectf, -65, mDegree, false, mArcPaint);

//    //绘制完成部分
    int mProgressColor = 0xffffde60;
    mArcPaint.setColor(mProgressColor);
    canvas.drawArc(mArcRectf, targetmDegree, mDegree * roate, false, mArcPaint);

    canvas.restore();
  }

  /**
   * dp 2 px
   *
   * @param dpVal
   */
  protected int dp2px(int dpVal) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        dpVal, getResources().getDisplayMetrics());
  }
}
