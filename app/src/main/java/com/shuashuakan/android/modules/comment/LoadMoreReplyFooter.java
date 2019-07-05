package com.shuashuakan.android.modules.comment;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.shuashuakan.android.R;

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/08/13
 * Description:
 */
public class LoadMoreReplyFooter implements RecyclerArrayAdapter.ItemView {

  private Context context;
  private View view;
  private TextView loadMoreTv, packUpTv;

  public LoadMoreReplyFooter(Context context) {
    this.context = context;
    view = View.inflate(context, R.layout.view_load_more_reply, null);
    loadMoreTv = view.findViewById(R.id.load_more_tv);
    packUpTv = view.findViewById(R.id.pack_up_tv);
  }

  public TextView getLoadMoreTv() {
    return loadMoreTv;
  }

  public TextView getPackUpTv() {
    return packUpTv;
  }

  @Override
  public View onCreateView(ViewGroup parent) {
    return view;
  }

  @Override
  public void onBindView(View headerView) {
    ViewGroup viewParent = (ViewGroup) view.getParent();
    if (viewParent != null)
      viewParent.removeView(view);
  }
}
