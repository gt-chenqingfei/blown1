package com.shuashuakan.android.modules.widget.loadmoreview;

import android.util.Log;

import com.chad.library.adapter.base.loadmore.LoadMoreView;
import com.shuashuakan.android.R;

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/11/30
 * Description:
 */
public class SskSecLoadMoreView extends LoadMoreView {

  public static final String TOPIC = "topic";
  public static final String IMAGE = "image";

  public static final String LOAD_MORE_CHAINS_END = "load_more_chains_end";
  public static final String LOAD_MORE_LOAD_END = "load_more_load_end";
  public static final String LOAD_MORE_NONE_END = "load_more_none_end";

  private boolean isChains = false;
  private String type = "";

  public SskSecLoadMoreView() {
    type = LOAD_MORE_LOAD_END; // 默认
  }

  public SskSecLoadMoreView(boolean isChains) {
    this.isChains = isChains;
    type = LOAD_MORE_CHAINS_END;
  }

  public SskSecLoadMoreView(String type) {
    this.type = type;
  }

  @Override
  public void setLoadMoreStatus(int loadMoreStatus) {
    super.setLoadMoreStatus(loadMoreStatus);
  }

  @Override
  public int getLayoutId() {
    return R.layout.view_right_layout;
  }

  @Override
  protected int getLoadingViewId() {
    return R.id.sec_load_more_loading_view;
  }

  @Override
  protected int getLoadFailViewId() {
    return R.id.load_more_load_fail_view;
  }

  @Override
  protected int getLoadEndViewId() {
    if (type.equals(TOPIC)) {
      return R.id.load_more_topic_end_view;
    } else if (type.equals(LOAD_MORE_LOAD_END)) {
      return R.id.load_more_load_end_view;
    } else if (type.equals(LOAD_MORE_CHAINS_END)) {
      return R.id.load_more_second_bird;
    } else if (type.equals(LOAD_MORE_NONE_END)) {
      return 0;
    }
    return R.id.load_more_load_end_view;
  }

}
