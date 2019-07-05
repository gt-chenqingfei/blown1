package com.shuashuakan.android.modules.timeline

interface OnMakeGifTouchListener {
    fun OnStart()
    fun onEnd()
  }

  interface AdapterToPageListener {
    fun adapterItemClick(id: Int, position: Int)
  }

  interface AdapterToPlayerListener {
    fun showLabel()
    fun hiddenLabel()
  }