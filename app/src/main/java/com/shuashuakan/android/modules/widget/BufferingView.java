package com.shuashuakan.android.modules.widget;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2019/01/02
 * Description:
 */
public class BufferingView extends View {
  public BufferingView(Context context) {
    this(context, null);
  }

  public BufferingView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public BufferingView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
  }
}
