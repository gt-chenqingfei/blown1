package com.shuashuakan.android.modules.message.badage

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation

/**
 * @author hushiguang
 * @since 2019-05-23.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class BadgeViewChangeUtil {


    companion object {
        fun showPointWithAnim(pointView: View, animView: View) {
            changeAlpha(pointView, 0f, 300) {}
            changeAlpha(animView, 1f, 300) {
                animView.startAnimation(getUnReadScaleAnim())
            }
        }

        fun hidePointWithAnim(pointView: View, animView: View) {
            changeAlpha(pointView, 1f, 300) {}
            changeAlpha(animView, 0f, 300) {}
        }

        private fun changeAlpha(view: View, alphaValue: Float, duration: Long, onAnimationEnd: () -> Unit) {
            view.animate()
                    .alpha(alphaValue)
                    .setDuration(duration)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            view.alpha = alphaValue
                            onAnimationEnd.invoke()
                        }

                    }).start()
        }

        fun hideAll(pointView: View, animView: View) {
            animView.clearAnimation()
            pointView.clearAnimation()
            pointView.animate().cancel()
            animView.animate().cancel()
            animView.alpha = 0f
            pointView.alpha = 0f
        }


        private fun getUnReadScaleAnim(): ScaleAnimation {
            val unReadAnimation = ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            unReadAnimation.repeatCount = Animation.INFINITE
            unReadAnimation.duration = 500
            unReadAnimation.interpolator = LinearInterpolator()
            unReadAnimation.repeatMode = Animation.REVERSE
            return unReadAnimation
        }
    }


}