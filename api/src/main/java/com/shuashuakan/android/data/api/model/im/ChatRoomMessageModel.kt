package com.shuashuakan.android.data.api.model.im

import se.ansman.kotshi.JsonSerializable

/**
 * Created by lijie on 2018/11/3 下午5:32
 */
@JsonSerializable
data class ChatRoomMessageModel(
    val scene:String,
    val scope:String,
    val source:String,
    val uuid:String,
    val data :Data

)
@JsonSerializable
data class Data(
    val content:String
)