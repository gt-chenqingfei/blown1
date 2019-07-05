package com.shuashuakan.android.data.api.model.comment

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  treasure_ct
 * Date:    2018/11/20
 * Description:
 */
@JsonSerializable
data class ApiSummary(
    @Json(name = "type")
    val type: String,

    @Json(name = "title")
    val title: String?,

    @Json(name = "channel_name")
    val channelName: String?,

    @Json(name = "redirect_url")
    val redirectUrl: String?,

    @Json(name = "count")
    val count: Int?,

    @Json(name = "user_list")
    var userList: List<ApiSummaryUser>?
)