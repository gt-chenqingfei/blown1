package com.shuashuakan.android.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Rect
import android.graphics.RectF
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import com.shuashuakan.android.base.ui.R

class CountDownCircleView : AppCompatImageView {

  constructor(context: Context) : super(context)
  constructor(
      context: Context,
      attributeSet: AttributeSet
  ) : super(context, attributeSet)

  constructor(
      context: Context,
      attributeSet: AttributeSet,
      defStyleAttr: Int
  ) : super(context, attributeSet, defStyleAttr)

  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val outBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val outBorder = resources.getDimensionPixelOffset(R.dimen.count_down_circle_border)
  private val borderCenter = outBorder / 2f
  private val bound = Rect()
  private val arcRect = RectF()

  var maxTime: Long = 0L
    set(value) {
      field = value
      countDownTime = field
    }

  var countDownTime: Long = 0
    set(value) {
      field = value
      invalidate()
    }

  init {
    paint.style = Style.STROKE
    paint.strokeWidth = outBorder.toFloat()
    paint.color = ContextCompat.getColor(context,R.color.black40)
    outBorderPaint.color = ContextCompat.getColor(context,R.color.out_circle_color)
    outBorderPaint.style = Style.STROKE
    outBorderPaint.strokeWidth = outBorder.toFloat()
    setPadding(outBorder, outBorder, outBorder, outBorder)
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    getDrawingRect(bound)
    arcRect.set(
        bound.left + borderCenter,
        bound.top + borderCenter,
        bound.right - borderCenter,
        bound.bottom - borderCenter
    )
    canvas.drawCircle(bound.centerX().toFloat(), bound.centerY().toFloat(),
        width / 2 - borderCenter, paint
    )
    if (maxTime > 0) {
      canvas.drawArc(arcRect, -90f, 360 * ((maxTime - countDownTime).toFloat() / maxTime),
          false, outBorderPaint
      )
    }
  }

  fun startAnim() {
    val rotate = RotateAnimation(0f, 360f,
        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
    rotate.interpolator = DecelerateInterpolator()
    rotate.duration = 1000
    animation = rotate
  }
}