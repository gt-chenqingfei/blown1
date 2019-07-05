package com.shuashuakan.android.modules.widget.customview;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.airbnb.epoxy.EpoxyRecyclerView;
import com.luck.picture.lib.tools.ScreenUtils;
import com.shuashuakan.android.R;

/**
 * Author:  lijie
 * Date:   2019/1/10
 * Email:  2607401801@qq.com
 */
public class PullZoomRecyclerView extends EpoxyRecyclerView {
  private static final String TAG = "MyRecyclerView";
  private LinearLayoutManager mLayoutManager;
  private int mTouchSlop;
  private View zoomView;
  // 记录首次按下位置
  private float mFirstPosition = 0;
  // 是否正在放大
  private Boolean mScaling = false;
  LinearLayoutManager mLinearLayoutManager ;
  private int screenWidth;
  LinearLayout topBottomLayout;
  private Context context;
  public PullZoomRecyclerView(Context context) {
    this(context,null);
  }

  public PullZoomRecyclerView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PullZoomRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    this.context=context;
    mLayoutManager = new LinearLayoutManager(context);
    setLayoutManager(mLayoutManager);
    mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
  }
  public void setZoomView(View v, LinearLayoutManager linearLayoutManager) {
    //获取屏幕宽度
    screenWidth = ScreenUtils.getScreenWidth(context);
    //此处我的图片命名为img，大家根据实际情况修改
    ImageView img = v.findViewById(R.id.bg_view);
    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) img.getLayoutParams();

    //获取屏幕宽度
    lp.width =screenWidth;
    //设置宽高比为16:9
    lp.height = screenWidth * 9 / 16;
    //给imageView重新设置宽高属性
    img.setLayoutParams(lp);
    this.zoomView = img;
    mLinearLayoutManager = linearLayoutManager ;

  }
  @Override
  public boolean onTouchEvent(MotionEvent event) {

    if(zoomView !=null){
      FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) zoomView.getLayoutParams();
      //判断触摸事件
      switch (event.getAction()) {
        //触摸结束
        case MotionEvent.ACTION_UP:
          mScaling = false;
          replyImage();
          break;
        //触摸中
        case MotionEvent.ACTION_MOVE:
          //判断是否正在放大 mScaling 的默认值为false
          if (!mScaling) {
            //当图片也就是第一个item完全可见的时候，记录触摸屏幕的位置
            if (mLinearLayoutManager.findViewByPosition(mLinearLayoutManager.findFirstVisibleItemPosition()).getTop() == 0) {
              //记录首次按下位置
              mFirstPosition = event.getY();
            } else {
              break;
            }
          }
          // 滚动距离乘以一个系数
          int distance = (int) ((event.getY() - mFirstPosition) * 0.5);
          if (distance < 0) {
            break;
          }
          // 处理放大
          mScaling = true;
          int width=zoomView.getWidth() + distance;
          lp.width = width>=1760?1760:width;
//          lp.width =width;
          int height=(zoomView.getWidth()  + distance) * 9 / 16;
          lp.height = height>=990?990:height;
//          lp.height = height;
          // 设置控件水平居中（如果不设置，图片的放大缩小是从图片顶点开始）
          lp.setMargins(-(lp.width -screenWidth) / 2, 0, 0, 0);
          zoomView.setLayoutParams(lp);

          return true; // 返回true表示已经完成触摸事件，不再处理
      }
    }

    return super.onTouchEvent(event);
  }
  /**
   * 图片回弹动画
   */
  private void replyImage() {
    final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) zoomView.getLayoutParams();
    final float wPresent = zoomView.getLayoutParams().width;// 图片当前宽度
    final float hPresent = zoomView.getLayoutParams().height;// 图片当前高度

    final float width = screenWidth;// 图片原宽度
    final float heigh = screenWidth * 9 / 16;// 图片原高度

    // 设置动画
    ValueAnimator anim = ObjectAnimator.ofFloat(0.0F, 1.0F).setDuration(200);

    anim.addUpdateListener(animation -> {
      float cVal = (Float) animation.getAnimatedValue();
      lp.width = (int) (wPresent- (wPresent- width ) * cVal);
      lp.height = (int) (hPresent - (hPresent - heigh ) * cVal);
      lp.setMargins(-(lp.width - screenWidth) / 2, 0, 0, 0);
      zoomView.setLayoutParams(lp);
    });
    anim.start();

  }
}
