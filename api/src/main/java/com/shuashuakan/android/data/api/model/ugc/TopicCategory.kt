package com.shuashuakan.android.data.api.model.ugc

import se.ansman.kotshi.JsonSerializable

/**
 * Author:  lijie
 * Date:   2018/12/8
 * Email:  2607401801@qq.com
 */
@JsonSerializable
data class TopicCategory (
    val id: Long,
    val name: String,
    val sort: Int,
    val status: Int
)