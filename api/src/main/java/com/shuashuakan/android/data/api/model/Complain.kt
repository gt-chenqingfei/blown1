package com.shuashuakan.android.data.api.model

import se.ansman.kotshi.JsonSerializable

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/09/17
 * Description:
 */
@JsonSerializable
data class Complain(val type: String, val desc: String,val url:String?)
