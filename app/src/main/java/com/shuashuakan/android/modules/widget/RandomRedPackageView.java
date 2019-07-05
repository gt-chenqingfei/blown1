package com.shuashuakan.android.modules.widget;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shuashuakan.android.R;

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/10/13
 * Description:
 */
public class RandomRedPackageView extends ConstraintLayout {

  private ProgressBar star;
  private ImageView coin;
  private TextView coinTv;
  private LinearLayout coinLl;

  public RandomRedPackageView(Context context) {
    this(context, null);
  }

  public RandomRedPackageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RandomRedPackageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_random_red_package, this);
    star = findViewById(R.id.star);
    coin = findViewById(R.id.coin);
    coinLl = findViewById(R.id.coin_ll);
    coinTv = findViewById(R.id.coin_tv);
  }

  public void showAnim() {
    setVisibility(View.VISIBLE);

    star.setVisibility(View.INVISIBLE);

    ScaleAnimation scaleAnimation = new ScaleAnimation(
        0.0f, 1.0f, 0.0f, 1.0f,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    scaleAnimation.setDuration(1000);
    scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {
      }

      @Override
      public void onAnimationEnd(Animation animation) {
        star.setVisibility(View.VISIBLE);
        rockCoin();
      }

      @Override
      public void onAnimationRepeat(Animation animation) {
      }
    });
    ScaleAnimation scaleAnimation1 = new ScaleAnimation(
        0.0f, 1.0f, 0.0f, 1.0f,
        Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_PARENT, 0.5f);

    scaleAnimation1.setDuration(1000);
    coinTv.clearAnimation();
    coinTv.setAnimation(scaleAnimation1);
    scaleAnimation1.start();

    coin.setAnimation(scaleAnimation);
    scaleAnimation.start();
  }

  private void rockCoin() {
    CustomerAnimation animation = new CustomerAnimation();
    animation.setInterpolator(new DecelerateInterpolator());
    animation.setDuration(900);
    coin.startAnimation(animation);
    animation.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {

      }

      @Override
      public void onAnimationEnd(Animation animation) {
        star.setVisibility(View.INVISIBLE);
        createAnimation();
      }

      @Override
      public void onAnimationRepeat(Animation animation) {
      }
    });
    animation.start();
  }

  private void createAnimation() {
    ScaleAnimation scaleAnimation = new ScaleAnimation(
        1.0f, 0.0f, 1.0f, 0.0f,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    scaleAnimation.setDuration(1000);
    coin.setAnimation(scaleAnimation);
    coinLl.setAnimation(scaleAnimation);
    scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {

      }

      @Override
      public void onAnimationEnd(Animation animation) {
        setVisibility(View.GONE);
        onListener.onAnimFinish();
      }

      @Override
      public void onAnimationRepeat(Animation animation) {

      }
    });


    ScaleAnimation scaleAnimation1 = new ScaleAnimation(
        1.0f, 0.0f, 1.0f, 0.0f,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

    scaleAnimation1.setDuration(1000);
    coinLl.setAnimation(scaleAnimation1);
    scaleAnimation1.start();

    scaleAnimation.start();
  }

  private OnListener onListener;

  public void setOnListener(OnListener onListener) {
    this.onListener = onListener;
  }


  class CustomerAnimation extends Animation {
    private int mWaveTimes = 3;//摇摆次数
    private int mWaveRange = 50;//摇摆幅度

    public CustomerAnimation() {

    }

    @Override
    protected void applyTransformation(float interpolatedTime,
                                       Transformation t) {
      //运用周期性函数，实现左右摇摆
      t.getMatrix().setTranslate((int) (Math.sin(interpolatedTime * Math.PI * mWaveTimes) * mWaveRange), 0);
    }
  }

  public interface OnListener {
    void onAnimFinish();
  }
}
