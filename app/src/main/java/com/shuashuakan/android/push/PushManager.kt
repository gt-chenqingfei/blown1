package com.shuashuakan.android.push

import android.content.Context
import cn.jpush.android.api.JPushInterface
import com.shuashuakan.android.BuildConfig

object PushManager {

  fun initPush(context: Context) {
    JPushInterface.setDebugMode(BuildConfig.DEBUG)
    JPushInterface.init(context)
  }

  fun setAlias(context: Context, alias: String) {
    JPushInterface.setAlias(context, 0, alias)
  }

}