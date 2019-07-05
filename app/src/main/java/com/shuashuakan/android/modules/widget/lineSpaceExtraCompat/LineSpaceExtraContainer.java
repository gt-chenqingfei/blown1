package com.shuashuakan.android.modules.widget.lineSpaceExtraCompat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/10/12
 * Description:
 */
public class LineSpaceExtraContainer extends ViewGroup {

  public LineSpaceExtraContainer(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

    if (getChildCount() < 1) {
      throw new IllegalStateException("must has one child view");
    }

    View view = getChildAt(0);
    if (!(view instanceof IGetLineSpaceExtra)) {
      throw new IllegalStateException("child view mast is child of DividerLineTextView");
    }

    view.measure(widthMeasureSpec, heightMeasureSpec);
    //总高度减去多余的行间距高作为该容器的高
    setMeasuredDimension(view.getMeasuredWidth(), view.getMeasuredHeight() - ((IGetLineSpaceExtra) view).getSpaceExtra());
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    if (getChildCount() < 1) {
      throw new IllegalStateException("must has one child view");
    }

    //填充整个个容器，忽略padding属性
    getChildAt(0).layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
  }

}