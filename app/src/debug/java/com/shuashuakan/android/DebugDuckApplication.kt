package com.shuashuakan.android

import com.facebook.stetho.Stetho

class DebugDuckApplication: DuckApplication() {

  override fun initAtMainProcess() {
    super.initAtMainProcess()
    Stetho.initializeWithDefaults(this)
  }
}