package com.shuashuakan.android.data.api.model

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/10/15
 * Description:
 */
@JsonSerializable
data class ConfigContext(@Json(name = "default_comments")
                         val defaultComments: List<String>,
                         @Json(name = "guide_feed_id")
                         val guideFeedId:String?)