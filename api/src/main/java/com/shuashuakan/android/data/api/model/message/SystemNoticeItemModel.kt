package com.shuashuakan.android.data.api.model.message

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  lijie
 * Date:   2018/12/10
 * Email:  2607401801@qq.com
 */
@JsonSerializable
data class SystemNoticeItemModel (
    @Json(name = "api_medias")
    val apiMedias:List<ApiMediasItem>?,
    val avatar:String,
    val content:String?,
    @Json(name = "create_at")
    val createAt:Long,
    val id:Long,
    val status:String,
    val type:String,
    val url :String?,
    val title:String?
)
@JsonSerializable
data class ApiMediasItem(
    @Json(name = "media_type")
    val mediaType:String,
    @Json(name = "thumb_height")
    val thumbHeight:Int,
    @Json(name = "thumb_width")
    val thumbWidth:Int,
    @Json(name = "thumb_url")
    val thumbUrl:String,
    val media_info:List<MediaInfo>
)

