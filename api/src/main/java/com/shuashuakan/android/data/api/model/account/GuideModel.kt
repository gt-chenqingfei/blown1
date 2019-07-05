package com.shuashuakan.android.data.api.model.account

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  liJie
 * Date:   2019/1/28
 * Email:  2607401801@qq.com
 * 引导model
 */
@Parcelize
@JsonSerializable
data class GuideModel(
        val data: GuideDataModel?,
        @Json(name = "page_types")
        val pageTypes: List<String>?,
        @Json(name = "require_guide")
        val requireGuide: Boolean
) : Parcelable

@Parcelize
@JsonSerializable
data class GuideDataModel(
        val avatar: String?,
        @Json(name = "user_name")
        val userName: String?,
        @Json(name = "api_interest")
        val apiInterest: ApiInterest?,

        @Json(name = "channel_list")
        val channelList: List<ChannelModel>?
) : Parcelable

@Parcelize
@JsonSerializable
data class ApiInterest(
        //val properties: GuideProperties
        val interests: List<InterestModel>
) : Parcelable

//@JsonSerializable
//data class GuideProperties()

@Parcelize
@JsonSerializable
data class InterestModel(
        val id: String,
        val name: String
) : Parcelable

@Parcelize
@JsonSerializable
data class ChannelModel(
        //val properties: GuideProperties
        @Json(name = "back_ground")
        val background: String?,
        val background_color: String?,
        val channel_icon: String,
        val channel_type: String,
        val cover_url: String,
        val description: String,
        val has_subscribe: Boolean,
        val id: Int,
        val name: String,
        val redirect_url: String?="",
        val score: Int,
        val new_feed_num: Int,
        val subscribed_count: Int,
        val total_feed_num: Int,
        val create_at: Long,
        val properties: ChannelProperties?,
        val isSelect: Boolean?,
        val category: List<ChannelCategory>
) : Parcelable

@Parcelize
@JsonSerializable
class ChannelProperties : Parcelable


@Parcelize
@JsonSerializable
data class ChannelCategory(
        val hot_sign: Boolean,
        val id: Int,
        val name: String,
        val properties: ChannelProperties?,
        val sort: Int,
        val status: Int

) : Parcelable
