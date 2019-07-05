package com.shuashuakan.android.modules.widget.customview

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import com.shuashuakan.android.R
import com.shuashuakan.android.utils.dip
import com.shuashuakan.android.utils.getColor1

@SuppressLint("ViewConstructor")
class InfiniteLineLoadingView : View {

    constructor(context: Context, attrs: AttributeSet? = null):super(context, attrs){
        paint.color = context.getColor1(R.color.white_60)
        paint.strokeWidth = context.dip(4).toFloat()
    }


    private var paint: Paint = Paint()
    private var startX: Int = 0
    private var incrementX: Int = 0
    private var incrementY: Int = 0
    private var alphaColor: Int = 255

    private var valueAnimatorLeft = ValueAnimator()
    private var valueAnimatorAlpha = ValueAnimator()
    private var valueAnimatorRight = ValueAnimator()
    val animatorSet = AnimatorSet()



    fun setStrikeWidth(width:Float){
        paint.strokeWidth = width
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        startX = w / 2
        initValueAnimator(startX)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.alpha = alphaColor
        canvas?.drawLine(startX.toFloat(), 0f, incrementX.toFloat(), 0f, paint)
        canvas?.drawLine(startX.toFloat(), 0f, incrementY.toFloat(), 0f, paint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        releaseValueAnimator()
    }


    private fun initValueAnimator(startX: Int) {
        valueAnimatorLeft.setIntValues(startX, 0)
        valueAnimatorLeft.addUpdateListener { animation ->
            incrementX = animation!!.animatedValue as Int
            invalidate()
        }
        valueAnimatorLeft.repeatCount = ValueAnimator.INFINITE

        valueAnimatorAlpha .setIntValues(255, 100)
        valueAnimatorAlpha.addUpdateListener {
            alphaColor = it.animatedValue as Int
        }
        valueAnimatorAlpha.repeatCount = ValueAnimator.INFINITE

        valueAnimatorRight .setIntValues(startX, startX * 2)
        valueAnimatorRight.addUpdateListener { animation ->
            incrementY = animation!!.animatedValue as Int
            invalidate()
        }
        valueAnimatorRight.repeatCount = ValueAnimator.INFINITE

        val accelerateInterpolator = AccelerateInterpolator()
        animatorSet.interpolator = accelerateInterpolator // 加速
        animatorSet.duration = 500
        animatorSet.playTogether(valueAnimatorLeft, valueAnimatorRight, valueAnimatorAlpha)
        animatorSet.start()
    }

    private fun releaseValueAnimator() {
        valueAnimatorLeft.cancel()
        valueAnimatorAlpha.cancel()
        valueAnimatorRight.cancel()
        animatorSet.cancel()
    }
}