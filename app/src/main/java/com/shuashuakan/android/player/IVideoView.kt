package com.shuashuakan.android.player

import android.view.View
import com.shuashuakan.android.data.api.model.home.Feed

/**
 * 视频播放的接口，定义视频播放需要的方法，它的子类需要是View
 */
interface IVideoView {

    fun bindFeed(target:Feed?)

    fun startPlayFeed()

    fun hostResume()

    fun hostPause()

    fun getPlayFeed(): Feed?

    fun pausePlay()

    fun stopPlayback()

    fun clearUpDateSeekTask()

    fun removeDelayTask(callBack: Runnable?)

    fun postDelayedTask(r: Runnable?, delayMillis: Long = 0,saveTask:Boolean = false)

    fun removeALlDelayTask()


    fun setBufferingIndicator(indicator: View)

    fun addVideoPlayListener(listener: SSKVideoPlayListener)

    fun removeVideoPlayListener(listener: SSKVideoPlayListener)

    fun removeAllVideoPlayListener()






}

