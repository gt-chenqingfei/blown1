package com.shuashuakan.android.data.api.model

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/10/14
 * Description:
 */
@JsonSerializable
data class CountBean(
    @Json(name = "comment_num")
    val commentNum: Int,
    @Json(name = "fav_num")
    val favNum: Int,
    @Json(name = "play_count")
    val playCount: Int,
    @Json(name = "share_num")
    val shareNum: Int,
    @Json(name = "solitaire_num")
    val solitaireNum:Int
)
