package com.shuashuakan.android.data.api.model

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/11/17
 * Description:
 */
@JsonSerializable
data class  UploadToken(val key: String,
                       val id: String,
                       val token: String)
