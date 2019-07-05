package com.shuashuakan.android.data.api.model

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor
import java.io.Serializable

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/09/06
 * Description:
 */
@JsonSerializable
data class ActivityCard @KotshiConstructor constructor(
        val appear: Int,
        val button: String,
        val description: String?,
        @Json(name = "expire_at")
        val expireAt: Long,//过期时间
        val id: String,
        val image: String,
        val style: String,
        val title: String?,
        val url: String,
        val frequency:String?,//类型
        @Json(name = "create_at")
        val createAt:Long?,
        @Json(name = "start_at")
        val startAt:Long?,
        @Json(name = "button_font_color")
        val buttonFontColor:String?,
        @Json(name = "buttonColor")
        val buttonColor:String?,
        @Json(name = "floating_icon_url")
        val iconUrl:String?
) : Serializable
