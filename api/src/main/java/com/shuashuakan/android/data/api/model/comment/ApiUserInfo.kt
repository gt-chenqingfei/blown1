package com.shuashuakan.android.data.api.model.comment

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/08/10
 * Description:
 */
@JsonSerializable
data class ApiUserInfo(
    @Json(name = "user_id")
    val userId: Long,

    val avatar: String?,
    @Json(name = "default_avatar")
    val defaultAvatar: String?,

    val birthday: String?,

    @Json(name = "nick_name")
    val nickName: String,

    val gender: Int?,

    val point: Long?,

    val bio: String?,

    @Json(name = "address_count")
    val addressCount: Int?,

    @Json(name = "mobile")
    val mobile: String?,

    @Json(name = "wechat_bind")
    val wechatBind: Boolean?,
    val labels:List<LabelsItem>?
    ){
@JsonSerializable
data class LabelsItem(
    val content:String
)}