package com.shuashuakan.android.modules.widget.timeline

import android.animation.*
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.util.Util
import com.shuashuakan.android.R
import com.shuashuakan.android.modules.player.ExoPlayerHelper
import com.shuashuakan.android.modules.player.PlayState
import com.shuashuakan.android.modules.widget.ExoOnPlayListener
import com.shuashuakan.android.utils.dip
import com.shuashuakan.android.utils.isWebPSupportableHost
import timber.log.Timber

class TimeLinePlayer @kotlin.jvm.JvmOverloads constructor(context: Context,
                                                          attrs: AttributeSet? = null, defStyleAttr: Int = -1
) : FrameLayout(context, attrs, defStyleAttr),
    ActivityLifecycleCallbacks {

  private lateinit var exoPlayerHelper: com.shuashuakan.android.modules.player.ExoPlayerHelper
  private lateinit var simpleExoPlayer: SimpleExoPlayer
  private var mediaSource: MediaSource? = null
  private var textureView: TimeLinePlayerView
  private var backImgView: SimpleDraweeView
  private var progress: ProgressBar

  private var playerView: FrameLayout = LayoutInflater.from(context).inflate(R.layout.layout_timeline_video_view, null) as FrameLayout

  private val application = context.applicationContext as Application

  private var heartDrawable: Drawable

  private var feedId: String? = null

  private var videoUrl: String? = ""

  private val mTransition = LayoutTransition()

  var playListener: ExoOnPlayListener? = null

  private var localConfig = Configuration.ORIENTATION_PORTRAIT

  init {
    setupCustomAnimations()
    addView(playerView)

    textureView = findViewById(R.id.texture_view)
    progress = findViewById(R.id.progress)

    backImgView = findViewById(R.id.video_back_view)
    application.registerActivityLifecycleCallbacks(this)
    heartDrawable = ContextCompat.getDrawable(context, R.drawable.ic_heart)!!

    playerView.setOnClickListener {
      if (localConfig == Configuration.ORIENTATION_PORTRAIT) {
        if (simpleExoPlayer.playWhenReady) {
          scaleLayout(Configuration.ORIENTATION_LANDSCAPE)
        }
      } else if (localConfig == Configuration.ORIENTATION_LANDSCAPE) {
        scaleLayout(Configuration.ORIENTATION_PORTRAIT)
      }
    }

    setVideoOutlineProvider()
  }

  private fun setupCustomAnimations() {

    val pvhLeft = PropertyValuesHolder.ofInt("left", 0, 1)
    val pvhTop = PropertyValuesHolder.ofInt("top", 0, 1)
    val pvhRight = PropertyValuesHolder.ofInt("right", 0, 1)
    val pvhBottom = PropertyValuesHolder.ofInt("bottom", 0, 1)
    val pvhScaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, 0f, 1f)
    val pvhScaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 0f, 1f)

    val changeIn = ObjectAnimator.ofPropertyValuesHolder(this, pvhLeft, pvhTop, pvhRight, pvhBottom, pvhScaleX, pvhScaleY).setDuration(mTransition.getDuration(LayoutTransition.CHANGE_APPEARING))
    mTransition.setAnimator(LayoutTransition.CHANGE_APPEARING, changeIn)
    changeIn.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(animation: Animator?) {
        val view = (animation as ObjectAnimator).target as View
        // View也支持此种动画执行方式了
        view.scaleX = 1f
        view.scaleY = 1f
      }
    })

    // 动画：CHANGE_DISAPPEARING
    // Changing while Removing
    val kf0 = Keyframe.ofFloat(0f, 0f)
    val kf1 = Keyframe.ofFloat(.9999f, 360f)
    val kf2 = Keyframe.ofFloat(1f, 0f)
    val pvhRotation = PropertyValuesHolder.ofKeyframe("rotation", kf0, kf1, kf2)
    val changeOut = ObjectAnimator
        .ofPropertyValuesHolder(this, pvhLeft, pvhTop, pvhRight,
            pvhBottom, pvhRotation)
        .setDuration(
            mTransition
                .getDuration(LayoutTransition.CHANGE_DISAPPEARING))
      mTransition
        .setAnimator(LayoutTransition.CHANGE_DISAPPEARING, changeOut)
      changeOut.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(animation: Animator?) {
        super.onAnimationEnd(animation)
        val view = (animation as ObjectAnimator).target as View
        view.rotation = 0f
      }
    })
  }

  /***
   * 设置内容横竖屏内容
   *
   * @param newConfig 旋转对象
   */
  private fun scaleLayout(newConfig: Int, anim: Boolean = true) {
    localConfig = newConfig
    if (newConfig == Configuration.ORIENTATION_PORTRAIT) {
      Timber.d(context.getString(R.string.string_vertical_screen))
      val parent = playerView.parent as ViewGroup?
      parent?.removeView(playerView)
      val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
      layoutTransition = if (anim) mTransition else null
      playerView.layoutParams = params
      addView(playerView)
    } else {
      Timber.d(context.getString(R.string.string_horizontal_screen))
      val parent = playerView.parent as ViewGroup?
      parent?.removeView(playerView)
      val contentView: ViewGroup = (context as Activity).findViewById(android.R.id.content)
      contentView.layoutTransition = if (anim) mTransition else null
      val params = FrameLayout.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT
      )
      contentView.addView(playerView, params)
    }
    setVideoOutlineProvider()
  }

  private fun setVideoOutlineProvider() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      if (localConfig == Configuration.ORIENTATION_PORTRAIT) {
        textureView.outlineProvider = TextureVideoViewOutlineProvider(context.dip(4).toFloat())
        textureView.clipToOutline = true
      } else if (localConfig == Configuration.ORIENTATION_LANDSCAPE) {
        textureView.outlineProvider = TextureVideoViewOutlineProvider(context.dip(0).toFloat())
        textureView.clipToOutline = true
      }
    }
  }


  fun closeFullScreen(): Boolean {
    return if (localConfig == Configuration.ORIENTATION_LANDSCAPE) {
      scaleLayout(Configuration.ORIENTATION_PORTRAIT)
      true
    } else {
      false
    }
  }

  fun setImage(previewUrl: String, width: Int, height: Int) {
    backImgView.visibility = View.VISIBLE
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
    backImgView.setImageURI(buildUrl.toString())
  }

  fun init(exoPlayerHelper: com.shuashuakan.android.modules.player.ExoPlayerHelper) {
    this.exoPlayerHelper = exoPlayerHelper
    simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(
        context,
        exoPlayerHelper.renderersFactory(),
        exoPlayerHelper.trackSelector()
    )
    simpleExoPlayer.addListener(object : Player.EventListener {
      override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
          Player.STATE_BUFFERING -> if (playWhenReady) {
            progress.visibility = View.VISIBLE
          }
          Player.STATE_READY -> {
            progress.visibility = View.GONE
            backImgView.visibility = View.GONE
          }
        }

        if (playWhenReady && playbackState == Player.STATE_READY
            && currentPlayState != PlayState.START) {
          var playPosition: Long? = simpleExoPlayer.currentPosition
          if (currentPlayState == PlayState.NORMAL) {
            playPosition = 0L
          }
          if (startTime > 0) {
            if (playListener != null) {
              playListener!!.onPlayListener(simpleExoPlayer, PlayState.END, simpleExoPlayer.currentPosition, true)
            }
            simpleExoPlayer.seekTo(startTime)
            startTime = 0
          } else {
            currentPlayState = PlayState.START
            if (playListener != null) {
              playListener!!.onPlayListener(simpleExoPlayer, currentPlayState, playPosition!!, playWhenReady)
            }
          }
        } else
          if (currentPlayState == PlayState.START && playbackState == Player.STATE_ENDED) {
            currentPlayState = PlayState.END
            var playPosition: Long = simpleExoPlayer.currentPosition
            if (playPosition > simpleExoPlayer.duration) {
              playPosition = simpleExoPlayer.duration
            }
            if (playListener != null) {
              playListener!!.onPlayListener(simpleExoPlayer, currentPlayState, playPosition, playWhenReady)

            }
            simpleExoPlayer.seekTo(0)
          } else if (!playWhenReady && playbackState == Player.STATE_READY) {
            if (currentPlayState == PlayState.START) {
              currentPlayState = PlayState.END
              var playPosition: Long = simpleExoPlayer.currentPosition
              if (playPosition > simpleExoPlayer.duration) {
                playPosition = simpleExoPlayer.duration
              }
              if (playListener != null) {
                playListener!!.onPlayListener(simpleExoPlayer, currentPlayState, playPosition, playWhenReady)
              }
            }
          } else {
            currentPlayState = PlayState.NORMAL
            if (playListener != null) {
              playListener!!.onPlayListener(simpleExoPlayer, currentPlayState, simpleExoPlayer.currentPosition, playWhenReady)
            }
          }
      }

      override fun onSeekProcessed() {
        if (simpleExoPlayer.playWhenReady) {
          if (simpleExoPlayer.playWhenReady && currentPlayState == PlayState.END) {
            currentPlayState = PlayState.START
            playListener?.onPlayListener(simpleExoPlayer, currentPlayState, simpleExoPlayer.currentPosition, simpleExoPlayer.playWhenReady)
          } else {
            currentPlayState = PlayState.END
            playListener?.onPlayListener(simpleExoPlayer, currentPlayState, simpleExoPlayer.currentPosition, simpleExoPlayer.playWhenReady)
          }
        }
      }
    })
  }

  private var currentPlayState = PlayState.START

//  private var listener: OnTimeLineAwardListener? = null
//
//  fun bind(feedId: String, videoUrl: String, previewUrl: String, width: Int, height: Int, listener: OnTimeLineAwardListener) {
//    scaleLayout(Configuration.ORIENTATION_PORTRAIT, false)
//    this.listener = listener
//    this.feedId = feedId
//    this.videoUrl = videoUrl
//    setImage(previewUrl, width, height)
//  }


  fun prepareVideo() {
    prepareMediaSource(videoUrl)
  }

  private fun setupTexture() {
    textureView.player = simpleExoPlayer
  }

  var startTime = 0L

  fun startPlayer(startTime: Long?) {
    this.exoPlayerHelper.timeLinePlayer = this@TimeLinePlayer
    if (startTime != null) {
      this.startTime = startTime
    } else {
      this.startTime = 0L
    }
    setupTexture()
    if (videoUrl != null)
      prepareVideo()
    progress.visibility = View.VISIBLE
    simpleExoPlayer.playWhenReady = true
  }

  fun stopPlayer() {
    Timber.d(context.getString(R.string.string_pause_label))
    simpleExoPlayer.playWhenReady = false
    backImgView.visibility = View.VISIBLE
  }


  fun continuePlay() {
    simpleExoPlayer.playWhenReady = true
  }


  fun getPlayer(): SimpleExoPlayer {
    return simpleExoPlayer
  }

  private fun prepareMediaSource(url: String?) {
    if (url != null) {
      simpleExoPlayer.prepare(buildMediaSource(Uri.parse(url)), true, true)
    }
  }

  @SuppressLint("SwitchIntDef")
  private fun buildMediaSource(uri: Uri): MediaSource {
    val type = Util.inferContentType(uri)
    mediaSource = when (type) {
      C.TYPE_HLS -> HlsMediaSource.Factory(exoPlayerHelper.dataSourceFactory())
          .setAllowChunklessPreparation(true)
          .setMinLoadableRetryCount(10)
          .createMediaSource(uri)
      else -> ExtractorMediaSource.Factory(exoPlayerHelper.dataSourceFactory())
          .createMediaSource(uri)
    }
    return mediaSource!!
  }

  fun releasePlayer() {
    feedId = null
    simpleExoPlayer.stop()
    textureView.release()
    simpleExoPlayer.release()
  }

  private fun isCurrentContext(activity: Activity): Boolean {
    return context === activity
  }

  override fun onActivityPaused(activity: Activity) {
  }

  override fun onActivityResumed(activity: Activity) {
  }

  override fun onActivityStarted(activity: Activity) {}

  override fun onActivityDestroyed(activity: Activity) {
    if (isCurrentContext(activity)) {
      releasePlayer()
      application.unregisterActivityLifecycleCallbacks(this)
    }
  }

  override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}

  override fun onActivityStopped(activity: Activity?) {}

  override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {}

}