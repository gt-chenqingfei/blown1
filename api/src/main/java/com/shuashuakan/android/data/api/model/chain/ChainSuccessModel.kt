package com.shuashuakan.android.data.api.model.chain

import se.ansman.kotshi.JsonSerializable

/**
 * Author:  lijie
 * Date:   2019/1/4
 * Email:  2607401801@qq.com
 */
@JsonSerializable
data class ChainSuccessModel (
    val message:String?,
    val argument:String?,
    val color:String?
)