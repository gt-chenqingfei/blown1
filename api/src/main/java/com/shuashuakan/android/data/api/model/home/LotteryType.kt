package com.shuashuakan.android.data.api.model.home

import com.squareup.moshi.Json

enum class LotteryType {
  @Json(name = "INTEGRAL")
  COIN,
  @Json(name = "GOODS")
  GOODS,
  @Json(name = "LUCKY_BAG")
  LUCKY_BAG,
  @Json(name = "NORMAL")
  NORMAL
}