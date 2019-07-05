package com.shuashuakan.android.modules.widget;

import android.content.Context;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public abstract class MarqueeFactory<V extends View, D> {

  protected Context context;
  protected OnItemClickListener<V, D> onItemClickListener;
  protected List<V> views;
  protected List<D> datas;
  private boolean isOnItemClickRegistered;
  private MarqueeView marqueeView;

  public MarqueeFactory(Context context) {
    this.context = context;
  }

  public abstract V generateMarqueeItemView(D data);

  public void setData(List<D> datas) {
    if (datas == null || datas.size() == 0) {
      return;
    }
    this.datas = datas;
    views = new ArrayList<>();
    for (int i = 0; i < datas.size(); i++) {
      D data = datas.get(i);
      V view = generateMarqueeItemView(data);
      views.add(view);
    }
    registerOnItemClick();
    if (marqueeView != null) {
      marqueeView.setMarqueeFactory(this);
    }
  }

  public void setOnItemClickListener(OnItemClickListener<V, D> onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
    registerOnItemClick();
  }

  public List<V> getMarqueeViews() {
    return views;
  }

  private void registerOnItemClick() {
    if (!isOnItemClickRegistered && onItemClickListener != null && datas != null) {
      for (int i = 0; i < datas.size(); i++) {
        V view = views.get(i);
        D data = datas.get(i);
        ViewHolder tag = new ViewHolder<>(view, data, i);
        view.setTag(tag);
        view.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {
            onItemClickListener.onItemClick((ViewHolder<V, D>)v.getTag());
          }
        });
      }
      isOnItemClickRegistered = true;
    }
  }

  public interface OnItemClickListener<V extends View, D> {
    void onItemClick(ViewHolder<V, D> holder);
  }

  public static class ViewHolder<V extends View, D> {
    public V view;
    public D data;
    public int position;

    public ViewHolder(V view, D data, int position) {
      this.view = view;
      this.data = data;
      this.position = position;
    }
  }

  public void setAttachedToMarqueeView(MarqueeView marqueeView) {
    this.marqueeView = marqueeView;
  }
}
