package com.shuashuakan.android.modules.widget.bounceview;

import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2019/01/02
 * Description:
 */
public interface BounceViewAnim {

  BounceViewAnim setScaleForPushInAnim(float scaleX, float scaleY);

  BounceViewAnim setScaleForPopOutAnim(float scaleX, float scaleY);

  BounceViewAnim setPushInAnimDuration(int timeInMillis);

  BounceViewAnim setPopOutAnimDuration(int timeInMillis);

  BounceViewAnim setInterpolatorPushIn(AccelerateDecelerateInterpolator interpolatorPushIn);

  BounceViewAnim setInterpolatorPopOut(AccelerateDecelerateInterpolator interpolatorPopOut);

}