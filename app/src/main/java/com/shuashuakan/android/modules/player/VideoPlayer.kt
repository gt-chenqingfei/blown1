package com.shuashuakan.android.modules.player

import android.content.Context
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.graphics.drawable.BitmapDrawable
import android.media.AudioManager
import android.os.Build
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import com.pili.pldroid.player.*
import com.shuashuakan.android.DuckApplication
import com.shuashuakan.android.R
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.modules.widget.timeline.TextureVideoViewOutlineProvider
import com.shuashuakan.android.utils.*
import timber.log.Timber
import java.io.IOException


/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/12/29
 * Description:
 */
class VideoPlayer @kotlin.jvm.JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = -1) : FrameLayout(context, attrs, defStyleAttr), TextureView.SurfaceTextureListener {
  companion object {
    /**
     * 播放错误
     */
    const val STATE_ERROR = -1
    /**
     * 播放未开始
     */
    const val STATE_IDLE = 0
    /**
     * 播放准备中
     */
    const val STATE_PREPARING = 1
    /**
     * 播放准备就绪
     */
    const val STATE_PREPARED = 2
    /**
     * 正在播放
     */
    const val STATE_PLAYING = 3
    /**
     * 暂停播放
     */
    const val STATE_PAUSED = 4
    /**
     * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，缓冲区数据足够后恢复播放)
     */
    const val STATE_BUFFERING_PLAYING = 5
    /**
     * 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，此时暂停播放器，继续缓冲，缓冲区数据足够后恢复暂停
     */
    const val STATE_BUFFERING_PAUSED = 6
    /**
     * 播放完成
     */
    const val STATE_COMPLETED = 7
    /**
     * 普通模式
     */
    const val MODE_NORMAL = 10
    /**
     * 全屏模式
     */
    const val MODE_FULL_SCREEN = 11
  }

  private var mContainer: FrameLayout = FrameLayout(context)
  private var mUrl: String? = null
  private var mType: String? = null
  private var mAudioManager: AudioManager? = null
  private var mMediaPlayer: PLMediaPlayer? = null
  private var mTextureView: TextureView? = null
  private var mController: com.shuashuakan.android.modules.player.IVideoPlayerController? = null
  private var mSurfaceTexture: SurfaceTexture? = null
  private var mSurface: Surface? = null
  private var mCurrentState = STATE_IDLE
  private var mCurrentMode = MODE_NORMAL
  private var mBufferPercentage: Int = 0
  private var mVideoWidth = 0
  private var mVideoHeight = 0
  var viewRadiusDP: Float = 0f
  var initpost = 0L

  init {
    mContainer.setBackgroundColor(Color.TRANSPARENT)
    val params = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT)
    this.addView(mContainer, params)
  }

  fun setUp(url: String, type: String) {
    mUrl = url
    mType = type
  }

  fun setController(controller: com.shuashuakan.android.modules.player.IVideoPlayerController) {
    mContainer.removeView(mController)
    mController = controller
    mController?.reset()
    mController?.setVideoPlayer(this)
    val params = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT)
    mContainer.addView(mController, params)
  }


  fun getDuration(): Long {
    return mMediaPlayer?.duration ?: 0L
  }

  fun getUrl(): String? {
    return mUrl
  }

  fun getCurrentPosition(): Long {
    return if (mMediaPlayer != null) mMediaPlayer!!.currentPosition else 0L
  }

  fun isIdle(): Boolean {
    return mCurrentState == STATE_IDLE
  }

  fun isPreparing(): Boolean {
    return mCurrentState == STATE_PREPARING
  }

  fun isPrepared(): Boolean {
    return mCurrentState == STATE_PREPARED
  }

  // 设置音量
  fun setVolume(left: Float, right: Float) {
    mMediaPlayer?.setVolume(left, right)
  }

  fun isBufferingPlaying(): Boolean {
    return mCurrentState == STATE_BUFFERING_PLAYING
  }

  fun isBufferingPaused(): Boolean {
    return mCurrentState == STATE_BUFFERING_PAUSED
  }

  fun isPlaying(): Boolean {
    return mCurrentState == STATE_PLAYING
  }

  fun isPaused(): Boolean {
    return mCurrentState == STATE_PAUSED
  }

  fun isError(): Boolean {
    return mCurrentState == STATE_ERROR
  }

  fun isCompleted(): Boolean {
    return mCurrentState == STATE_COMPLETED
  }

  fun isFullScreen(): Boolean {
    return mCurrentMode == MODE_FULL_SCREEN
  }

  fun isNormal(): Boolean {
    return mCurrentMode == MODE_NORMAL
  }

  fun start() {
    start(0)
  }
  fun start(pos :  Long) {
    if (mCurrentState == STATE_IDLE && mType != null) {
      initpost = pos
      VideoPlayerManager.instance().setCurrentVideoPlayer(mType!!, this)
      initAudioManager()
      initMediaPlayer()
      initTextureView()
      addTextureView()
    } else {
      Timber.d("VideoPlayer只有在mCurrentState == STATE_IDLE时才能调用start方法.")
    }
  }

  fun pause() {
    Timber.d("pause: mCurrentState:$mCurrentState")
    if (mCurrentState == STATE_IDLE) return

    if (mCurrentMode == STATE_PAUSED && mMediaPlayer?.isPlaying == false) {
      return
    }
    mMediaPlayer?.pause()
    mCurrentState = STATE_PAUSED
    mController?.onPlayStateChanged(mCurrentState)
    Timber.d("STATE_PAUSED")
  }

  fun restart() {
    Timber.d("dbtest  restart 0000   $mCurrentState")
    if (mCurrentState == STATE_PAUSED) {
      mMediaPlayer!!.start()
      mCurrentState = STATE_PLAYING
      mController?.onPlayStateChanged(mCurrentState)
      Timber.d("STATE_PLAYING")
    } else if (mCurrentState == STATE_BUFFERING_PAUSED) {
      mMediaPlayer!!.start()
      mCurrentState = STATE_BUFFERING_PLAYING
      mController?.onPlayStateChanged(mCurrentState)
      Timber.d("STATE_BUFFERING_PLAYING")
    } else if (mCurrentState == STATE_COMPLETED) {
      mCurrentState = STATE_PREPARED
      mController?.onPlayStateChanged(mCurrentState)
      mMediaPlayer!!.seekTo(0)
      mMediaPlayer!!.start()
      mCurrentState = STATE_PLAYING
      mController?.onPlayStateChanged(mCurrentState)
      Timber.d("STATE_PLAYING")
    } else if (mCurrentState == STATE_ERROR) {
      networkStatus()
    } else {
      Timber.d("NiceVideoPlayer在mCurrentState == $mCurrentState 时不能调用restart()方法.")
    }
  }

  private fun initAudioManager() {
    if (mAudioManager == null) {
      mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
      mAudioManager!!.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
    }
  }

  private fun initMediaPlayer() {
    if (mMediaPlayer == null) {
      val options = AVOptions()
      options.setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_AUTO)
      options.setInteger(AVOptions.KEY_FAST_OPEN, 1)
      options.setInteger(AVOptions.KEY_PREFER_FORMAT, 2)
      options.setInteger(AVOptions.KEY_LOG_LEVEL, 2)
      options.setString(AVOptions.KEY_CACHE_DIR, getVideoCacheDir(context))
      mMediaPlayer = PLMediaPlayer(context, options)
//      mMediaPlayer!!.isLooping = true
    }
  }

  private fun initTextureView() {
    if (mTextureView == null) {
      mTextureView = TextureView(context)
      mTextureView!!.surfaceTextureListener = this
      setTextureViewRadius()
    }
  }

  private fun addTextureView() {
    mContainer.removeView(mTextureView)
    val params = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT,
        Gravity.CENTER)
    mContainer.addView(mTextureView, 0, params)
  }

  override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
  }

  override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
  }

  override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
    return mSurfaceTexture == null
  }

  override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
    if (mSurfaceTexture == null) {
      mSurfaceTexture = surface
      networkStatus()
    } else {
      mTextureView!!.surfaceTexture = mSurfaceTexture
    }
  }

  private fun openMediaPlayer() {
    if(mSurfaceTexture == null){
      Log.e("VideoPlayer","openMediaPlayer error SurfaceTexture not init")
      return
    }
    // 屏幕常亮
    mContainer.keepScreenOn = true
    // 设置监听
    mMediaPlayer?.setOnPreparedListener(mOnPreparedListener)
    mMediaPlayer?.setOnCompletionListener(mOnCompletionListener)
    mMediaPlayer?.setOnErrorListener(mOnErrorListener)
    mMediaPlayer?.setOnVideoSizeChangedListener(mOnVideoSizeListener)
    mMediaPlayer?.setOnInfoListener(mOnInfoListener)
    mMediaPlayer?.setOnBufferingUpdateListener(mOnBufferingUpdateListener)
    // 设置dataSource
    try {
      mMediaPlayer?.dataSource = mUrl
      if (mSurface == null) {
        mSurface = Surface(mSurfaceTexture)
      }
      mMediaPlayer?.setSurface(mSurface)
      mMediaPlayer?.prepareAsync()

      if(mCurrentState != STATE_PAUSED){
        mCurrentState = STATE_PREPARING
        Timber.d("STATE_PREPARING")
        mController?.onPlayStateChanged(mCurrentState)
      }
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  private fun networkStatus() {
    if (isNetWorkConntectd(context)) {
      if (!isWifiConnected(context)
          && !context.daggerComponent().appConfig().isFlowPlaySwitch() && !DuckApplication.HAS_SHOW_WIFI_DIALOG) {
        showWifiTipDialog()
      } else {
        openMediaPlayer()
      }
    } else {
      mOnErrorListener.onError(0)
    }
  }

  private fun showWifiTipDialog() {
    val view = LayoutInflater.from(context).inflate(R.layout.layout_show_wifi_tip, null)
    val dialog = AlertDialog.Builder(context).setView(view).show()
    val window = dialog.window
    window?.let {
      it.attributes.width = (context.getScreenSize().x * 0.7f).toInt()
      it.setBackgroundDrawable(BitmapDrawable())
      it.setDimAmount(0f)
    }

    val stopPlayView = view.findViewById<View>(R.id.stop_play_view)
    stopPlayView.setOnClickListener {
      mOnErrorListener.onError(1)
      dialog.dismiss()
      DuckApplication.HAS_SHOW_WIFI_DIALOG = false
      // TODO 事件点
      context?.getSpider()?.manuallyEvent(SpiderEventNames.TRAFFIC_AUTO_PLAY_ALERT_SHOW)
          ?.put("state", "close")
          ?.track()
    }
    val continuePlayView = view.findViewById<View>(R.id.continue_play_view)
    continuePlayView.setOnClickListener {
      dialog.dismiss()
      DuckApplication.HAS_SHOW_WIFI_DIALOG = true
      openMediaPlayer()
      context?.getSpider()?.manuallyEvent(SpiderEventNames.TRAFFIC_AUTO_PLAY_ALERT_SHOW)
          ?.put("state", "open")
          ?.track()
    }
    dialog.show()
  }

  fun releasePlayer() {
    if (mCurrentState != STATE_PAUSED) {
      pause()
    }
    if (mAudioManager != null) {
      mAudioManager!!.abandonAudioFocus(null)
      mAudioManager = null
    }
    if (mMediaPlayer != null) {
      mMediaPlayer!!.release()
      mMediaPlayer = null
    }
    mContainer.removeView(mTextureView)
    if (mSurface != null) {
      mSurface!!.release()
      mSurface = null
    }
    if (mSurfaceTexture != null) {
      mSurfaceTexture!!.release()
      mSurfaceTexture = null
    }
    mCurrentState = STATE_IDLE
    mController?.onPlayStateChanged(mCurrentState)
  }

  fun release() {
    // 退出全屏或小窗口
//    if (isFullScreen()) {
//      exitFullScreen()
//    }
    mCurrentMode = MODE_NORMAL

    // 释放播放器
    releasePlayer()

    // 恢复控制器
    mController?.reset()
  }

  private val mOnPreparedListener = PLOnPreparedListener {
    if (mCurrentState == STATE_PREPARING) {
      mCurrentState = STATE_PREPARED
      Timber.d("onPrepared ——> STATE_PREPARED")
      mController?.onPlayStateChanged(mCurrentState)


      mMediaPlayer?.start()
      mMediaPlayer?.seekTo(initpost)
      initpost = 0
    }
  }

  private val mOnCompletionListener = PLOnCompletionListener {
    mCurrentState = STATE_COMPLETED
    Timber.d("onCompletion ——> STATE_COMPLETED")
    mController?.onPlayStateChanged(mCurrentState)
    // 清除屏幕常亮
    mContainer.keepScreenOn = false
    //restart()
    this.postDelayed({ mController?.onLoopDone() }, 200)
  }

  private val mOnErrorListener = PLOnErrorListener {
    mCurrentState = STATE_ERROR
    Timber.d("onError ——> STATE_ERROR ———— what：$it")
    mController?.onPlayStateChanged(mCurrentState)
    mController?.onVideoError(it)
    true
  }

  private val mOnInfoListener = PLOnInfoListener { what, extra ->
    if (what == PLOnInfoListener.MEDIA_INFO_VIDEO_RENDERING_START) {
      // 播放器开始渲染
      mCurrentState = STATE_PLAYING
      Timber.d("onInfo ——> MEDIA_INFO_VIDEO_RENDERING_START：STATE_PLAYING")
      mController?.onPlayStateChanged(mCurrentState)
    } else if (what == PLOnInfoListener.MEDIA_INFO_BUFFERING_START) {
      // MediaPlayer暂时不播放，以缓冲更多的数据
      if (mCurrentState == STATE_PAUSED || mCurrentState == STATE_BUFFERING_PAUSED) {
        mCurrentState = STATE_BUFFERING_PAUSED
        Timber.d("onInfo ——> MEDIA_INFO_BUFFERING_START：STATE_BUFFERING_PAUSED")
      } else {
        mCurrentState = STATE_BUFFERING_PLAYING
        Timber.d("onInfo ——> MEDIA_INFO_BUFFERING_START：STATE_BUFFERING_PLAYING")
      }
      mController?.onPlayStateChanged(mCurrentState)
    } else if (what == PLOnInfoListener.MEDIA_INFO_BUFFERING_END) {
      // 填充缓冲区后，MediaPlayer恢复播放/暂停
      if (mCurrentState == STATE_BUFFERING_PLAYING) {
        mCurrentState = STATE_PLAYING
        Timber.d("onInfo ——> MEDIA_INFO_BUFFERING_END： STATE_PLAYING")
        mController?.onPlayStateChanged(mCurrentState)
      }
      if (mCurrentState == STATE_BUFFERING_PAUSED) {
        mCurrentState = STATE_PAUSED
        Timber.d("onInfo ——> MEDIA_INFO_BUFFERING_END： STATE_PAUSED")
        mController?.onPlayStateChanged(mCurrentState)
      }
    } else if (what == PLOnInfoListener.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
      // 视频旋转了extra度，需要恢复
      if (mTextureView != null) {
        mTextureView!!.rotation = extra.toFloat()
        mTextureView!!.layoutParams.width = measuredHeight
        mTextureView!!.layoutParams.height = measuredWidth
        mTextureView!!.requestLayout()
        adjustAspectRatio(mVideoHeight, mVideoWidth)
        Timber.d("视频旋转角度：$extra")
      }
    } else if (what == PLOnInfoListener.MEDIA_INFO_LOOP_DONE) {
    } else {
//      Timber.d("onInfo ——> what：$what")
    }
    true
  }

  private val mOnBufferingUpdateListener = PLOnBufferingUpdateListener { p0 ->
    mBufferPercentage = p0
  }

  private val mOnVideoSizeListener = PLOnVideoSizeChangedListener { width, height ->
    mTextureView!!.layoutParams.width = measuredWidth
    mTextureView!!.layoutParams.height = measuredHeight
    mTextureView!!.requestLayout()
    mVideoWidth = width
    mVideoHeight = height
    adjustAspectRatio(width, height)
  }

  private fun adjustAspectRatio(videoWidth: Int, videoHeight: Int) {
    val viewWidth = measuredWidth
    val viewHeight = measuredHeight
    val aspectRatio = videoHeight.toDouble() / videoWidth
    var newWidth: Int
    var newHeight: Int

    newWidth = viewWidth
    newHeight = (newWidth * aspectRatio).toInt()
    val viewInScreenRatio = newHeight.toFloat() / viewHeight
    if (viewInScreenRatio > 0.82 && viewInScreenRatio <= 1) {
      newHeight = viewHeight
      newWidth = (viewHeight / aspectRatio).toInt()
    }
    val xoff = (viewWidth - newWidth) / 2
    val yoff = (viewHeight - newHeight) / 2
    val txform = Matrix()
    mTextureView!!.getTransform(txform)
    txform.setScale(newWidth.toFloat() / viewWidth, newHeight.toFloat() / viewHeight)
    txform.postTranslate(xoff.toFloat(), yoff.toFloat())
    mTextureView!!.setTransform(txform)
  }

  private fun setTextureViewRadius() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      mTextureView?.outlineProvider = TextureVideoViewOutlineProvider(context.dip(viewRadiusDP).toFloat())
      mTextureView?.clipToOutline = true
    }
  }
}

