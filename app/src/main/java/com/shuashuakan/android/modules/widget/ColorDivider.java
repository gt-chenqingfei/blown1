package com.shuashuakan.android.modules.widget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by twocity on 14-11-13.
 */
public class ColorDivider extends RecyclerView.ItemDecoration {

  private final int height;
  private final Paint paint = new Paint();
  private final Rect dividerRect = new Rect();
  private int padding = 0;

  public ColorDivider(int heightInPixel) {
    if (heightInPixel <= 0) {
      throw new IllegalArgumentException("divider height must be positive");
    }
    paint.setAntiAlias(true);
    paint.setColor(Color.TRANSPARENT);
    this.height = heightInPixel;
  }

  public ColorDivider(int heightInPixel, int color, int padding) {
    if (heightInPixel < 0) {
      throw new IllegalArgumentException("divider height must be positive");
    }
    paint.setAntiAlias(true);
    paint.setColor(color);
    this.height = heightInPixel;
    this.padding = padding;
  }

  @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
      RecyclerView.State state) {
    super.getItemOffsets(outRect, view, parent, state);
    if (parent.getChildAdapterPosition(view) < 1) return;

    if (getOrientation(parent) == LinearLayoutManager.VERTICAL) {
      outRect.top = height;
    } else {
      outRect.left = height;
    }
  }

  @Override public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
    if (getOrientation(parent) == LinearLayoutManager.VERTICAL) {
      drawVertical(c, parent);
    } else { //horizontal
      drawHorizontal(c, parent);
    }
  }

  public void drawVertical(Canvas c, RecyclerView parent) {
    final int left = parent.getPaddingLeft() + padding;
    final int right = parent.getWidth() - parent.getPaddingRight() - padding;

    final int childCount = parent.getChildCount();
    for (int i = 0; i < childCount; i++) {
      final View child = parent.getChildAt(i);
      final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
      final int top = child.getBottom() + params.bottomMargin;
      final int bottom = top + height;
      dividerRect.set(left, top, right, bottom);
      c.drawRect(dividerRect, paint);
      //divider.draw(c);
    }
  }

  public void drawHorizontal(Canvas c, RecyclerView parent) {
    final int top = parent.getPaddingTop() + padding;
    final int bottom = parent.getHeight() - parent.getPaddingBottom() - padding;

    final int childCount = parent.getChildCount();
    for (int i = 0; i < childCount; i++) {
      final View child = parent.getChildAt(i);
      final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
      final int left = child.getRight() + params.rightMargin;
      final int right = left + height;
      dividerRect.set(left, top, right, bottom);
      c.drawRect(dividerRect, paint);
    }
  }

  private int getOrientation(RecyclerView parent) {
    if (parent.getLayoutManager() instanceof LinearLayoutManager) {
      LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
      return layoutManager.getOrientation();
    } else {
      throw new IllegalStateException(
          "DividerItemDecoration can only be used with a LinearLayoutManager.");
    }
  }
}
