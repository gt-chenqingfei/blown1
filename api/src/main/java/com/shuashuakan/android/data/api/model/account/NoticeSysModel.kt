package com.shuashuakan.android.data.api.model.account

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class NoticeSysModel(
    val content:String,
    val url :String?,
    val image:String?,
    val title:String)