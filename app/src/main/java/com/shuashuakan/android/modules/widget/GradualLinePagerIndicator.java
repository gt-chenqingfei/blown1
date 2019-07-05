package com.shuashuakan.android.modules.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import net.lucode.hackware.magicindicator.FragmentContainerHelper;
import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.model.PositionData;

import java.util.List;

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/11/05
 * Description:
 */
public class GradualLinePagerIndicator extends View implements IPagerIndicator {
  public static final int MODE_MATCH_EDGE = 0;
  public static final int MODE_WRAP_CONTENT = 1;
  public static final int MODE_EXACTLY = 2;
  private int mMode;
  private Interpolator mStartInterpolator = new LinearInterpolator();
  private Interpolator mEndInterpolator = new LinearInterpolator();
  private float mYOffset;
  private float mLineHeight;
  private float mXOffset;
  private float mLineWidth;
  private float mRoundRadius;
  private Paint mPaint;
  private List<PositionData> mPositionDataList;
  private int[] mColors;
  private RectF mLineRect = new RectF();

  public GradualLinePagerIndicator(Context context) {
    super(context);
    this.init(context);
  }

  private void init(Context context) {
    this.mPaint = new Paint(1);
    this.mPaint.setStyle(Paint.Style.FILL);
    this.mLineHeight = (float) UIUtil.dip2px(context, 3.0D);
    this.mLineWidth = (float) UIUtil.dip2px(context, 10.0D);
  }

  protected void onDraw(Canvas canvas) {
    canvas.drawRoundRect(this.mLineRect, this.mRoundRadius, this.mRoundRadius, this.mPaint);
  }

  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    if (this.mPositionDataList != null && !this.mPositionDataList.isEmpty()) {

      PositionData current = FragmentContainerHelper.getImitativePositionData(this.mPositionDataList, position);
      PositionData next = FragmentContainerHelper.getImitativePositionData(this.mPositionDataList, position + 1);
      float nextLeftX;
      float rightX;
      float nextRightX;
      float leftX;
      if (this.mMode == 0) {
        leftX = (float) current.mLeft + this.mXOffset;
        nextLeftX = (float) next.mLeft + this.mXOffset;
        rightX = (float) current.mRight - this.mXOffset;
        nextRightX = (float) next.mRight - this.mXOffset;
      } else if (this.mMode == 1) {
        leftX = (float) current.mContentLeft + this.mXOffset;
        nextLeftX = (float) next.mContentLeft + this.mXOffset;
        rightX = (float) current.mContentRight - this.mXOffset;
        nextRightX = (float) next.mContentRight - this.mXOffset;
      } else {
        leftX = (float) current.mLeft + ((float) current.width() - this.mLineWidth) / 2.0F;
        nextLeftX = (float) next.mLeft + ((float) next.width() - this.mLineWidth) / 2.0F;
        rightX = (float) current.mLeft + ((float) current.width() + this.mLineWidth) / 2.0F;
        nextRightX = (float) next.mLeft + ((float) next.width() + this.mLineWidth) / 2.0F;
      }

      this.mLineRect.left = leftX + (nextLeftX - leftX) * this.mStartInterpolator.getInterpolation(positionOffset);
      this.mLineRect.right = rightX + (nextRightX - rightX) * this.mEndInterpolator.getInterpolation(positionOffset);
      this.mLineRect.top = (float) this.getHeight() - this.mLineHeight - this.mYOffset;
      this.mLineRect.bottom = (float) this.getHeight() - this.mYOffset;

      LinearGradient backGradient = new LinearGradient(mLineRect.left, mLineRect.top, this.mLineRect.right,this.mLineRect.bottom ,mColors, null, Shader.TileMode.REPEAT);
      this.mPaint.setShader(backGradient);

      this.invalidate();
    }
  }

  public void onPageSelected(int position) {
  }

  public void onPageScrollStateChanged(int state) {
  }

  public void onPositionDataProvide(List<PositionData> dataList) {
    this.mPositionDataList = dataList;
  }

  public float getYOffset() {
    return this.mYOffset;
  }

  public void setYOffset(float yOffset) {
    this.mYOffset = yOffset;
  }

  public float getXOffset() {
    return this.mXOffset;
  }

  public void setXOffset(float xOffset) {
    this.mXOffset = xOffset;
  }

  public float getLineHeight() {
    return this.mLineHeight;
  }

  public void setLineHeight(float lineHeight) {
    this.mLineHeight = lineHeight;
  }

  public float getLineWidth() {
    return this.mLineWidth;
  }

  public void setLineWidth(float lineWidth) {
    this.mLineWidth = lineWidth;
  }

  public float getRoundRadius() {
    return this.mRoundRadius;
  }

  public void setRoundRadius(float roundRadius) {
    this.mRoundRadius = roundRadius;
  }

  public int getMode() {
    return this.mMode;
  }

  public void setMode(int mode) {
    if (mode != 2 && mode != 0 && mode != 1) {
      throw new IllegalArgumentException("mode " + mode + " not supported.");
    } else {
      this.mMode = mode;
    }
  }

  public Paint getPaint() {
    return this.mPaint;
  }

  public int[] getColors() {
    return this.mColors;
  }

  public void setColors(int... colors) {
    this.mColors = colors;
  }

  public Interpolator getStartInterpolator() {
    return this.mStartInterpolator;
  }

  public void setStartInterpolator(Interpolator startInterpolator) {
    this.mStartInterpolator = startInterpolator;
    if (this.mStartInterpolator == null) {
      this.mStartInterpolator = new LinearInterpolator();
    }

  }

  public Interpolator getEndInterpolator() {
    return this.mEndInterpolator;
  }

  public void setEndInterpolator(Interpolator endInterpolator) {
    this.mEndInterpolator = endInterpolator;
    if (this.mEndInterpolator == null) {
      this.mEndInterpolator = new LinearInterpolator();
    }

  }
}
