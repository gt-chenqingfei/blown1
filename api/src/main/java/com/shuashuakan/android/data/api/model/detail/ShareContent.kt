package com.shuashuakan.android.data.api.model.detail

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import se.ansman.kotshi.JsonDefaultValueBoolean
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

@Parcelize
@JsonSerializable
data class ShareResult(val data: ShareContent,
                       @Json(name = "platform")
                       val platform: String?,
                       @Json(name = "target_id")
                       val targetId: String?,
                       @Json(name = "share_type")
                       val shareType: String?) : Parcelable


@JsonSerializable
@Parcelize
data class ShareContent @KotshiConstructor constructor(
        @Json(name = "trigger")
        @JsonDefaultValueBoolean(false)
        val trigger: Boolean,

        // 分享app使用
        val content: String?,
        val url: String?,
        val title: String?,
        val image: String?,

        @Json(name = "channel_name")
        val channelName: String?,
        @Json(name = "feed_author")
        val feedAuthor: String?,
        @Json(name = "message")
        val message: String?,

        @Json(name = "video_url")
        val videoUrl: String?,


        // 小程序的
        @Json(name = "image_url")
        val imageUrl: String?,
        @Json(name = "miniprogram_type")
        val miniprogramType: Int?,
        @Json(name = "path")
        val path: String?,
        @Json(name = "user_name")
        val userName: String?,
        @Json(name = "webpage_url")
        val webpageUrl: String?,


        @Json(name = "trace_id")
        val traceId: String?,
        val types: List<String>?


) : Parcelable


