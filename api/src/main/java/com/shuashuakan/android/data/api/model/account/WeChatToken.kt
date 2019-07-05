package com.shuashuakan.android.data.api.model.account

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/07/27
 * Description:
 */
@JsonSerializable
data class WeChatToken(
  @Json(name="access_token")
  val accessToken: String,
  val openid: String
)