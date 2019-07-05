package com.shuashuakan.android.modules.player

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.shuashuakan.android.R
import com.shuashuakan.android.modules.widget.OnPlayChangeListener
import com.shuashuakan.android.modules.widget.customview.InfiniteLineLoadingView
import com.shuashuakan.android.utils.dip

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2019/01/02
 * Description:
 */
class FeedPlayerControllerImpl(context: Context) : com.shuashuakan.android.modules.player.IVideoPlayerController(context) {
  private val fullShadow: View
  private val playButton: ImageView
  private val loadingView: RelativeLayout
  private val seek: ProgressBar
  var playChangeListener: OnPlayChangeListener? = null

  var makeGifListener: OnMakeGifListener? = null

  private var startPoint: Long = 0
  private var endPoint: Long = 0

  private var catchPostion: Long = 0
  private var catchTime: Long = 0

  init {
    LayoutInflater.from(context).inflate(R.layout.layout_playback_controller, this, true)
    fullShadow = findViewById(R.id.full_shadow)
    playButton = findViewById(R.id.play_button)
    loadingView = findViewById(R.id.loading_view)
    seek = findViewById(R.id.seek)
    val infiniteLineLoadingView = InfiniteLineLoadingView(context.applicationContext)
    infiniteLineLoadingView.setStrikeWidth(context.dip(4f).toFloat())
    loadingView.addView(infiniteLineLoadingView)
  }

  override fun setTitle(title: String) {

  }

  override fun setImage(resId: Int) {

  }

  override fun imageView(): ImageView? {
    return null
  }

  override fun setLength(length: Long) {

  }

  override fun onPlayStateChanged(playState: Int) {
    when (playState) {
      VideoPlayer.STATE_PLAYING -> {
        loadingView.visibility = View.GONE
        seek.visibility = View.VISIBLE
        startPoint = mVideoPlayer.getCurrentPosition()
      }
      VideoPlayer.STATE_PAUSED -> {
        loadingView.visibility = View.GONE
        seek.visibility = View.VISIBLE
        endPoint = mVideoPlayer.getCurrentPosition()
        playChangeListener?.onVideoPauseRecord(mVideoPlayer, startPoint, endPoint, false)
      }
      VideoPlayer.STATE_PREPARED -> {
        startUpdateProgressTimer()
      }
      VideoPlayer.STATE_BUFFERING_PAUSED -> {
        seek.visibility = View.GONE
        loadingView.visibility = View.VISIBLE
        catchTime = System.currentTimeMillis()
        catchPostion = mVideoPlayer.getCurrentPosition()
      }
      VideoPlayer.STATE_BUFFERING_PLAYING -> {
        seek.visibility = View.GONE
        loadingView.visibility = View.VISIBLE
        playChangeListener?.onVideoStandStill(mVideoPlayer, catchPostion, catchTime - System.currentTimeMillis())
      }
      VideoPlayer.STATE_PREPARING -> {
        seek.visibility = View.GONE
        loadingView.visibility = View.VISIBLE
      }
      VideoPlayer.STATE_ERROR -> {
        cancelUpdateProgressTimer()
      }
      VideoPlayer.STATE_COMPLETED -> {
        cancelUpdateProgressTimer()
      }
    }
    updatePausePlay()
  }

  override fun onPlayModeChanged(playMode: Int) {

  }

  override fun onLoopDone() {
    playChangeListener?.onRepeatPlay(mVideoPlayer)
    makeGifListener?.onPlayEnd()
  }

  override fun onVideoError(errorCode: Int) {
    playChangeListener?.onVideoLoadError(mVideoPlayer.getUrl(), errorCode)
  }

  override fun reset() {
    loadingView.visibility = View.GONE
    playButton.visibility = View.GONE
    fullShadow.visibility = View.GONE
    seek.visibility = View.GONE
    cancelUpdateProgressTimer()
    seek.progress = 0
  }

  override fun updateProgress() {
    seek.max = mVideoPlayer.getDuration().toInt()
    seek.progress = mVideoPlayer.getCurrentPosition().toInt()
  }

  private fun updatePausePlay() {
    if (mVideoPlayer.isPlaying()) {
      fullShadow.visibility = View.GONE
      playButton.visibility = View.GONE
    } else if (mVideoPlayer.isPaused()) {
      fullShadow.visibility = View.VISIBLE
      playButton.visibility = View.VISIBLE
    }
  }

    interface OnMakeGifListener {
      fun onPlayEnd()
    }
}
