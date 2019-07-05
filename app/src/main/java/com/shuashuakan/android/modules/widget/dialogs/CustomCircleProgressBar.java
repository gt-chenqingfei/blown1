package com.shuashuakan.android.modules.widget.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import com.shuashuakan.android.R;

/**
 * 下载视频时的实时进度提示
 * https://github.com/SolveBugs/CustomProgress
 */
public class CustomCircleProgressBar extends View {

  // 画实心圆的画笔
  private Paint mCirclePaint;
  // 画圆环的画笔
  private Paint mRingPaint;
  // 画字体的画笔
  private Paint mTextPaint;
  // 圆形颜色
  private int mCircleColor;
  // 圆环颜色
  private int mRingColor;
  // 半径
  private float mRadius;
  // 圆环半径
  private float mRingRadius;
  // 圆环宽度
  private float mStrokeWidth;
  // 字的高度
  private float mTxtHeight;
  // 总进度
  private int mTotalProgress = 100;
  // 当前进度
  private int mProgress = 0;
  //大圆
  private Paint mBigPatient;
  //字体颜色
  private int mTextColor;
  //外圆颜色
  private int mBigCircleColor;

  public CustomCircleProgressBar(Context context, AttributeSet attrs) {
    super(context, attrs);
    // 获取自定义的属性
    initAttrs(context, attrs);
    initVariable();
  }

  private void initAttrs(Context context, AttributeSet attrs) {
    TypedArray typeArray = context.getTheme().obtainStyledAttributes(attrs,
        R.styleable.CustomCircleProgressBar, 0, 0);
    mRadius = typeArray.getDimension(R.styleable.CustomCircleProgressBar_radius, 300);
    mStrokeWidth = typeArray.getDimension(R.styleable.CustomCircleProgressBar_strokeWidth, 20);
    mCircleColor = typeArray.getColor(R.styleable.CustomCircleProgressBar_circleColor, Color.TRANSPARENT);
    mRingColor = typeArray.getColor(R.styleable.CustomCircleProgressBar_ringColor, Color.TRANSPARENT);
    mTotalProgress = typeArray.getInt(R.styleable.CustomCircleProgressBar_totalProgress, 100);
    mTextColor = typeArray.getColor(R.styleable.CustomCircleProgressBar_textColor, Color.TRANSPARENT);
    mBigCircleColor = typeArray.getColor(R.styleable.CustomCircleProgressBar_bigCircleColor, Color.TRANSPARENT);

    typeArray.recycle();//注意这里要释放掉

    mRingRadius = mRadius + mStrokeWidth / 2;
  }

  private void initVariable() {
    mCirclePaint = new Paint();
    mCirclePaint.setAntiAlias(true);
    mCirclePaint.setColor(mCircleColor);
    mCirclePaint.setStrokeCap(Paint.Cap.ROUND);
    mCirclePaint.setStyle(Paint.Style.FILL);

    mRingPaint = new Paint();
    mRingPaint.setAntiAlias(true);
    mRingPaint.setColor(mRingColor);
    mRingPaint.setStrokeCap(Paint.Cap.ROUND);
    mRingPaint.setStyle(Paint.Style.STROKE);
    mRingPaint.setStrokeWidth(mStrokeWidth);

    mTextPaint = new Paint();
    mTextPaint.setAntiAlias(true);
    mTextPaint.setStyle(Paint.Style.FILL);
    mTextPaint.setColor(mTextColor);
    mTextPaint.setTextSize(mRadius / 2);


    mBigPatient = new Paint();
    mBigPatient.setColor(mBigCircleColor);
    mBigPatient.setAntiAlias(true);
    mBigPatient.setStyle(Paint.Style.FILL);

    Paint.FontMetrics fm = mTextPaint.getFontMetrics();
    mTxtHeight = (int) Math.ceil(fm.descent - fm.ascent);

  }

  @Override
  protected void onDraw(Canvas canvas) {

    // 圆心x坐标
    int mXCenter = getWidth() / 2;
    // 圆心y坐标
    int mYCenter = getHeight() / 2;

    canvas.drawCircle(mXCenter, mYCenter, mRadius + mStrokeWidth, mBigPatient);
    canvas.drawCircle(mXCenter, mYCenter, mRadius, mCirclePaint);

    // 字的长度
    float mTxtWidth;
    if (mProgress == 0) {
      String txt1 = 0 + "%";
      mTxtWidth = mTextPaint.measureText(txt1, 0, txt1.length());
      canvas.drawText(txt1, mXCenter - mTxtWidth / 2, mYCenter + mTxtHeight / 4, mTextPaint);
    }

    if (mProgress > 0) {
      @SuppressLint("DrawAllocation")
      RectF oval = new RectF();
      oval.left = (mXCenter - mRingRadius);
      oval.top = (mYCenter - mRingRadius);
      oval.right = mRingRadius * 2 + (mXCenter - mRingRadius);
      oval.bottom = mRingRadius * 2 + (mYCenter - mRingRadius);
      canvas.drawArc(oval, -90, ((float) mProgress / mTotalProgress) * 360, false, mRingPaint); //

      String txt = (int) (mProgress * 1.0f / mTotalProgress * 100) + "%";
      mTxtWidth = mTextPaint.measureText(txt, 0, txt.length());
      canvas.drawText(txt, mXCenter - mTxtWidth / 2, mYCenter + mTxtHeight / 4, mTextPaint);
    }
  }

  public void setProgress(int progress) {
    mProgress = progress;
    postInvalidate();
  }


}
