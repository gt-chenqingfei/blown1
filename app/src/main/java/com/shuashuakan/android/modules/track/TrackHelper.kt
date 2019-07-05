package com.shuashuakan.android.modules.track

import com.sensorsdata.analytics.android.sdk.SensorsDataAPI
import org.json.JSONObject

//点击事件
fun trackClick(action: String, pairs: List<Pair<String, Any?>>) {
  val properties = JSONObject().apply {
    pairs.forEach {
      put(it.first, it.second)
    }
  }
  SensorsDataAPI.sharedInstance().track(action, properties)
}

//与用户相关的
fun trackProfileSet(pairs: List<Pair<String, String>>) {
  val properties = JSONObject().apply {
    pairs.forEach {
      put(it.first, it.second)
    }
  }
  SensorsDataAPI.sharedInstance().profileSet(properties)
}

//计数
fun trackProfileIncrementOnce(pairs: List<Pair<String, Number>>) {
  val properties = HashMap<String, Number>().apply {
    pairs.forEach {
      put(it.first, it.second)
    }
  }
  SensorsDataAPI.sharedInstance().profileIncrement(properties)
}

//页面浏览时间
fun trackPageStart(action: String) {
  SensorsDataAPI.sharedInstance().trackTimerStart(action)
}

//页面浏览结束
fun trackPageEnd(action: String, pairs: List<Pair<String, Any?>>) {
  val properties = JSONObject().apply {
    pairs.forEach {
      put(it.first, it.second)
    }
  }
  SensorsDataAPI.sharedInstance().trackTimerEnd(action, properties)
}



