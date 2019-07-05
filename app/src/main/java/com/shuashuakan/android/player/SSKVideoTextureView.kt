/*
package com.shuashuakan.android.player

import android.annotation.TargetApi
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import com.pili.pldroid.player.*
import com.pili.pldroid.player.widget.PLVideoTextureView
import com.shuashuakan.android.BuildConfig
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.utils.getVideoCacheDir
import timber.log.Timber

*/
/**
 * 使用七牛视频播放支持库做的IVideoView实现
 *//*

class SSKVideoTextureView : PLVideoTextureView
        , PLOnInfoListener
        , PLOnVideoSizeChangedListener
        , PLOnBufferingUpdateListener
        , PLOnCompletionListener
        , PLOnErrorListener
        , PLOnPreparedListener {

    private val upDateProgressDuration = 500L//更新进度时间间隔
    private val defaultProgressMaxValue = 1000L//默认进度条最大值
    private val mListeners: MutableList<SSKVideoPlayListener> = mutableListOf()

    private var clearUpDateSeekFlag: Boolean = false
    private val mHandler = Handler()//只负责刷新进度的handler
    private var mIsPreparedAllowPlay: Boolean = true
    private var mBindFeed: Feed? = null

    private var mPlayFeed: Feed? = null

    var fakeCoverView: View? = null//视频封面View

    private val upDateSeekTask = object : Runnable {
        override fun run() {
            if (duration > 0 && !clearUpDateSeekFlag) {
                val pos = defaultProgressMaxValue * currentPosition / duration
                mListeners.forEach {
                    it.onProgressUpdate(pos)
                }
            }
            if (!clearUpDateSeekFlag) {
                mHandler.postDelayed(this, upDateProgressDuration)
            }
        }
    }


    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    @TargetApi(21)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context)
    }


    private fun init(context: Context) {

        val options = AVOptions()
        // the unit of timeout is ms
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 20 * 1000)
        // 1 -> hw codec enable, 0 -> disable [recommended]
        options.setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_AUTO)
        var logLevel = 5
        if (BuildConfig.DEBUG) {
            logLevel = 1
        }
        options.setInteger(AVOptions.KEY_OPEN_RETRY_TIMES, 6)
        options.setInteger(AVOptions.KEY_LOG_LEVEL, logLevel)
        options.setString(AVOptions.KEY_CACHE_DIR, getVideoCacheDir(context))
        options.setInteger(AVOptions.KEY_PREFER_FORMAT, 2)

        setAVOptions(options)

        //listener
        setOnInfoListener(this)
        setOnVideoSizeChangedListener(this)
        setOnBufferingUpdateListener(this)
        setOnCompletionListener(this)
        setOnErrorListener(this)
        setOnPreparedListener(this)
    }

    override fun onPrepared(preparedTime: Int) {
        Timber.e("SSKVideoTextureView ==--  onPrepared ------- ")
        mListeners.forEach {
            it.onPrepared(preparedTime)
        }
    }

    override fun onInfo(what: Int, extra: Int) {
        mListeners.forEach {
            it.onInfo(what, extra)
        }
    }

    override fun onVideoSizeChanged(width: Int, height: Int) {
        mListeners.forEach {
            it.onVideoSizeChanged(width, height)
        }
    }

    override fun onBufferingUpdate(precent: Int) {
        mListeners.forEach {
            it.onBufferingUpdate(precent)
        }
    }

    override fun onCompletion() {
        Timber.e("SSKVideoTextureView ==--  onCompletion ------- ")
        mListeners.forEach {
            it.onCompletion()
        }
    }

    override fun onError(errorCode: Int): Boolean {
        Timber.e("SSKVideoTextureView ==--  onError ------- ")
        mListeners.forEach {
            it.onError(errorCode)
        }
        return false
    }


    override fun start() {

        Timber.e("SSKVideoTextureView ==--  start ------- ")
        if (mIsPreparedAllowPlay) {
            super.start()
            if (mListeners.isNotEmpty()) {
                clearUpDateSeekFlag = false
                mHandler.post(upDateSeekTask)
            }
            mListeners.forEach {
                it.onStart()
            }
        }
    }

    override fun stopPlayback() {
        Timber.e("SSKVideoTextureView ==--  stopPlayback ------- ")
//        mHandler.removeCallbacksAndMessages(null)
        super.stopPlayback()
        mListeners.forEach {
            it.onStopPlayback()
        }

    }

    fun addVideoPlayListener(listener: SSKVideoPlayListener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener)
        }
    }

    fun removeVideoPlayListener(listener: SSKVideoPlayListener) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener)
        }
    }


    fun clearUpDateSeekTask() {
        clearUpDateSeekFlag = true
    }


    fun startPlayFeed() {
        mBindFeed?.let { validFeed ->
            validFeed.videoDetails
                    ?.filter {
                        it.clarity.equals("ORIGINAL", true)
                    }
                    ?.getOrNull(0)?.let { videoDetail ->
                        mPlayFeed = mBindFeed
                        Timber.e("SSKVideoTextureView ==--  startPlayFeed ---mPlayFeed = ${mPlayFeed?.title}--listener = ${mListeners}-- ")
                        stopPlayback()
                        setVideoPath(videoDetail.url)
                    }
        }
        mListeners.forEach {
            it.startPlayFeed(mBindFeed)
        }
    }

    fun getPlayFeed(): Feed? {
        return mPlayFeed
    }


    fun removeAllVideoPlayListener() {
        mListeners.clear()
    }

    override fun pause() {
        super.pause()
        mListeners.forEach {
            it.onPause()
        }
        Timber.e("SSKVideoTextureView ==--  pause ------- ")
//        mHandler.removeCallbacksAndMessages(null)
    }

    fun bindFeed(target: Feed?) {
        mBindFeed = target
    }

    fun getBindFeed(): Feed? {
        return mBindFeed
    }

    */
/**
     * 宿主暂停了，拥有该播放控件的Fragment/Activity onPause请调用这个方法
     *//*

    fun hostPause() {
        mIsPreparedAllowPlay = false
        pause()
    }

    */
/**
     * 宿主恢复了，拥有该播放控件的Fragment/Activity onResume请调用这个方法
     *//*

    fun hostResume() {
        mIsPreparedAllowPlay = true
        start()
    }


}




*/
