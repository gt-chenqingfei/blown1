package com.shuashuakan.android.modules.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.facebook.drawee.view.SimpleDraweeView
import com.shuashuakan.android.R
import com.shuashuakan.android.modules.player.FeedPlayerControllerImpl
import com.shuashuakan.android.modules.player.VideoPlayer
import com.shuashuakan.android.modules.timeline.AdapterToPlayerListener
import com.shuashuakan.android.utils.isWebPSupportableHost

class PlayerView @kotlin.jvm.JvmOverloads constructor(context: Context,
                                                      attrs: AttributeSet? = null, defStyleAttr: Int = -1
) : FrameLayout(context, attrs, defStyleAttr) {

  private var videoPlayer: VideoPlayer
  private var blurView: SimpleDraweeView

  private var heartDrawable: Drawable

  private var feedId: String? = null

  private var mGestureDetector: GestureDetector
  private var showOrHidden: AdapterToPlayerListener? = null

  var videoUrl: String? = ""

  var controller: FeedPlayerControllerImpl

  init {
    View.inflate(context, R.layout.layout_video_view, this)
    videoPlayer = findViewById(R.id.texture_view)
    blurView = findViewById(R.id.blur_view)
    controller = FeedPlayerControllerImpl(context)
    heartDrawable = ContextCompat.getDrawable(context, R.drawable.ic_heart)!!
    videoPlayer.setController(controller)
    mGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
      override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        if (videoPlayer.isPlaying()) {
          pausePlay()
          showOrHidden?.showLabel()
        } else if (videoPlayer.isPaused()) {
          startPlay()
          showOrHidden?.hiddenLabel()
        }
        return false
      }
    })
  }

  fun bind(uuid:String,feedId: String, videoUrl: String, previewUrl: String, width: Int,
           height: Int, showOrHidden: AdapterToPlayerListener) {
    this.feedId = feedId
    this.videoUrl = videoUrl
    this.showOrHidden = showOrHidden
    requestFocus()
    //显示封面图
    val buildUrl = StringBuilder(previewUrl).apply {
      if (!previewUrl.contains("?imageMogr2") && isWebPSupportableHost(previewUrl)) {
        append("?imageMogr2")
        append("/strip")
        append("/gravity/center")
        append("/thumbnail")
        append("/${width}x$height")
        append("/format/webp")
      }
    }
    blurView.setImageURI(buildUrl.toString())
//    showUrlBlur(blurView, buildUrl.toString(), 6, 6)
//            Glide.with(context)
//                    .load(buildUrl.toString())
//                    .transition(object : BlurTransformation(context, 6,6))
//                    .into(blurView)

    videoPlayer.setUp(videoUrl,uuid)
  }

  fun pausePlay() {
    videoPlayer.pause()
  }

  fun startPlay() {
    if (videoPlayer.isPaused() || videoPlayer.isBufferingPaused()) {
      videoPlayer.restart()
    } else {
      videoPlayer.start()
    }
  }

  fun getPlayer(): VideoPlayer {
    return videoPlayer
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    mGestureDetector.onTouchEvent(event)
    return true
  }

  fun addHeartView(x: Int, y: Int) {
    var lp = LayoutParams(this.heartDrawable.intrinsicWidth, this.heartDrawable.intrinsicHeight)
    lp.leftMargin = (x - (this.heartDrawable.intrinsicWidth / 2).toFloat()).toInt()
    lp.topMargin = y - (this.heartDrawable.intrinsicHeight)
    var img = ImageView(this.context)
    img.scaleType = ImageView.ScaleType.MATRIX
    img.setImageDrawable(heartDrawable)
    img.layoutParams = lp

    this.addView(img)

    val animSet = this.getShowAnimSet(img)

    val hideSet = this.getHideAnimSet(img)

    animSet.start()

    animSet.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(animation: Animator?) {
        super.onAnimationEnd(animation)
        hideSet.start()
      }
    })
    hideSet.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(animation: Animator?) {
        super.onAnimationEnd(animation)
        this@PlayerView.removeView(img)
      }
    })
  }

  private fun getShowAnimSet(imageView: ImageView): AnimatorSet {
    val scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1.2f, 1.0f)
    val scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1.2f, 1.0f)
    val animSet = AnimatorSet()
    animSet.playTogether(scaleX, scaleY)
    animSet.duration = 100
    return animSet
  }

  private fun getHideAnimSet(imageView: ImageView): AnimatorSet {
    val alpha = ObjectAnimator.ofFloat(imageView, "alpha", 1.0f, 0.1f)
    val scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1.0f, 2.0f)
    val scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1.0f, 2.0f)
    val translation = ObjectAnimator.ofFloat(imageView, "translationY", 0.0F, -150.0F)
    val animSet = AnimatorSet()
    animSet.playTogether(alpha, scaleX, scaleY, translation)
    animSet.duration = 500
    return animSet
  }

  fun makeGif() {

  }
}