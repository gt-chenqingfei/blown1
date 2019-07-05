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
public class GridDivider extends RecyclerView.ItemDecoration {
  private int itemSpace;
  private int itemNum;

  /**
   *
   * @param itemSpace item间隔
   * @param itemNum 每行item的个数
   */
  public GridDivider(int itemSpace, int itemNum) {
    this.itemSpace = itemSpace;
    this.itemNum = itemNum;
  }

  @Override
  public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
    super.getItemOffsets(outRect, view, parent, state);
    outRect.bottom = itemSpace;
    if (parent.getChildLayoutPosition(view)%itemNum == 0){  //parent.getChildLayoutPosition(view) 获取view的下标
      outRect.left = 0;
    } else {
      outRect.left = itemSpace;
    }
  }
}