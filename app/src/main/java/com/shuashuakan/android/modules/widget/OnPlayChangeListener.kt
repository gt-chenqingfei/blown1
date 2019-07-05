package com.shuashuakan.android.modules.widget

import com.shuashuakan.android.modules.player.VideoPlayer

interface OnPlayChangeListener {
  fun onRepeatPlay(player: VideoPlayer)
  fun onSeekChange(player: VideoPlayer, startPos: Long, endPos: Long)
  fun onVideoStandStill(player: VideoPlayer, catchPosition: Long, catchTime: Long)
  fun onVideoPauseRecord(player: VideoPlayer, pointStart: Long, pointStop: Long, isActive: Boolean)
  fun onVideoLoadError(url:String?,errorCode:Int)
}