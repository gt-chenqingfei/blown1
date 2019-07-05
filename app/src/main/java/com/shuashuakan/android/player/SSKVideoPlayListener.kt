package com.shuashuakan.android.player

import com.pili.pldroid.player.*
import com.shuashuakan.android.data.api.model.home.Feed

/**
 * 监听视频播放各种状态的回调
 */
abstract class SSKVideoPlayListener : PLOnInfoListener,
        PLOnSeekCompleteListener,
        PLOnBufferingUpdateListener,
        PLOnVideoSizeChangedListener,
        PLOnCompletionListener,
        PLOnPreparedListener,
        PLOnErrorListener,
        PLOnVideoFrameListener {

    /**
     * 默认进度最大1000
     */
    open fun onProgressUpdate(currentProgress: Long) {

    }

    override fun onSeekComplete() {
    }

    open fun startPlayFeed(feed: Feed?) {}

    open fun onPause() {}

    open fun onStopPlayback() {}

    open fun onStart() {}

    override fun onPrepared(preparedTime: Int) {
    }

    override fun onInfo(what: Int, extra: Int) {
    }

    override fun onVideoSizeChanged(width: Int, height: Int) {
    }

    override fun onBufferingUpdate(precent: Int) {
    }

    override fun onCompletion() {
    }

    override fun onError(errorCode: Int): Boolean {
        return false
    }

    override fun onVideoFrameAvailable(data: ByteArray?, size: Int, width: Int, height: Int, format: Int, ts: Long) {
    }


}