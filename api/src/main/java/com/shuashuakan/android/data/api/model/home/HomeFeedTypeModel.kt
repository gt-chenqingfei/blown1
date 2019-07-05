package com.shuashuakan.android.data.api.model.home

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/11/29
 * Description:
 */
@JsonSerializable
data class HomeFeedTypeModel(
    val data: Feed,
    val type: String
) : HomeRecommendModel