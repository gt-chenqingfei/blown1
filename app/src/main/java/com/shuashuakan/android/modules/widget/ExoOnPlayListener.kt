package com.shuashuakan.android.modules.widget

import com.google.android.exoplayer2.ExoPlayer

interface ExoOnPlayListener {
  fun onPlayListener(player: ExoPlayer, playerState: Int, playPosition: Long, playWhenReady: Boolean)
}