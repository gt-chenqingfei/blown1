package com.shuashuakan.android.widget;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/07/30
 * Description:
 */
public class HorizontalDivider extends RecyclerView.ItemDecoration {
  private int itemSpace;

  /**
   * @param itemSpace item间隔
   */
  public HorizontalDivider(int itemSpace) {
    this.itemSpace = itemSpace;
  }

  @Override
  public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
    super.getItemOffsets(outRect, view, parent, state);
    outRect.right = itemSpace;
  }
}
