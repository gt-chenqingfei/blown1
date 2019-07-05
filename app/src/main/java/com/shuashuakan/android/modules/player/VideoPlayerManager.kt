package com.shuashuakan.android.modules.player

import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/12/29
 * Description:
 */
class VideoPlayerManager private constructor() {
  private val playerMap = ConcurrentHashMap<String, VideoPlayer>()

  fun getCurrentVideoPlayer(type: String): VideoPlayer? {
    return playerMap[type]
  }

  fun setCurrentVideoPlayer(type: String, videoPlayer: VideoPlayer) {
    if (getCurrentVideoPlayer(type) == null) {
      playerMap[type] = videoPlayer
      return
    }
    if (playerMap[type] != videoPlayer) {
      releaseVideoPlayer(type)
      playerMap[type] = videoPlayer
    }
  }

  fun suspendVideoPlayer(type: String) {
    if (getCurrentVideoPlayer(type) != null) {
      getCurrentVideoPlayer(type)!!.pause()
    }
  }

  fun resumeVideoPlayer(type: String) {
    Timber.d("dbtest  resume video player:  $type")
    if (getCurrentVideoPlayer(type) != null) {
      getCurrentVideoPlayer(type)!!.restart()
    }
  }


  fun releaseVideoPlayer(type: String) {
    if (getCurrentVideoPlayer(type) != null) {
//      getCurrentVideoPlayer(type)!!.release()
//      playerMap.remove(type)
      val iterator = playerMap.iterator()
      while (iterator.hasNext()) {
        val next = iterator.next()
        if (next.key == type) {
          getCurrentVideoPlayer(type)?.release()
          iterator.remove()
        }
      }
    }
  }

  fun releaseAllVideoPlayer() {
    playerMap.forEach {
      releaseVideoPlayer(it.key)
    }
  }

  companion object {

    private var sInstance: VideoPlayerManager? = null

    @Synchronized
    fun instance(): VideoPlayerManager {
      if (sInstance == null) {
        sInstance = VideoPlayerManager()
      }
      return sInstance!!
    }
  }
}