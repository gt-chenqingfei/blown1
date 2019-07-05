package com.shuashuakan.android.modules.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

/**
 * Author:  lijie
 * Date:   2018/12/20
 * Email:  2607401801@qq.com
 */
public class StrickRoundSpan extends ReplacementSpan {

  private int bgColor;
  private int textColor;
  private Context context;
  private int mSize;

  public StrickRoundSpan(Context context, int bgColor, int textColor) {
    super();
    this.context = context;
    this.bgColor = bgColor;
    this.textColor = textColor;
  }

  @Override
  public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
    mSize=(int) (paint.measureText(text, start, end) + 2 * 8);
    return mSize+5;
  }

  @Override
  public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
    int color1 = paint.getColor();
    paint.setColor(this.bgColor);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth(2);
    paint.setAntiAlias(true);

    RectF rectF = new RectF(x + 2.5f, y + 2.5f + paint.ascent(), x + mSize+10, y + paint.descent());
    canvas.drawRoundRect(rectF, 8, 8, paint);


    paint.setStrokeWidth(1);
    paint.setStyle(Paint.Style.FILL);
    paint.setFakeBoldText(true);
    paint.setColor(this.textColor);
    paint.setTextSize(sp2px(context,12));
    canvas.drawText(text, start, end, x + 20, y, paint);
    paint.setColor(color1);
  }

  /**
   * 将sp值转换为px值，保证文字大小不变
   */
  public static int sp2px(Context context, float spValue) {
    final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
    return (int) (spValue * fontScale + 0.5f);
  }
  public static int dip2px(Context context, float dpValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dpValue * scale + 0.5f);
  }
}
