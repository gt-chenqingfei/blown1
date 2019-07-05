package com.shuashuakan.android.modules.share

import se.ansman.kotshi.JsonSerializable

/**
 * Author:  lijie
 * Date:   2018/12/25
 * Email:  2607401801@qq.com
 */
@JsonSerializable
data class ShareData(
        val image: Int,
        val name: String,
        val type: String
)