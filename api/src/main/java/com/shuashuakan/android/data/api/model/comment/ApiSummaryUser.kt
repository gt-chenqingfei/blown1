package com.shuashuakan.android.data.api.model.comment

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  treasure_ct
 * Date:    2018/11/20
 * Description:
 */
@JsonSerializable
data class ApiSummaryUser(
    @Json(name = "user_id")
    val userId: Long,

    @Json(name = "nick_name")
    val nickName: String,

    @Json(name = "avatar")
    val avatar: String,

    @Json(name = "redirect_url")
    val redirectUrl: String?
)