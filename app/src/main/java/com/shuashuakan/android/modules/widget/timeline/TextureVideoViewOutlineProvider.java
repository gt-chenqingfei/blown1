package com.shuashuakan.android.modules.widget.timeline;

import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/10/26
 * Description:
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TextureVideoViewOutlineProvider extends ViewOutlineProvider {
  private float mRadius;

  public TextureVideoViewOutlineProvider(float radius) {
    this.mRadius = radius;
  }

  @Override
  public void getOutline(View view, Outline outline) {
    Rect rect = new Rect();
    view.getGlobalVisibleRect(rect);
    int leftMargin = 0;
    int topMargin = 0;
    Rect selfRect = new Rect(leftMargin, topMargin,
        rect.right - rect.left - leftMargin, rect.bottom - rect.top - topMargin);
    outline.setRoundRect(selfRect, mRadius);
  }
}