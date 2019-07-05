package com.shuashuakan.android.data.api.model.explore

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  liJie
 * Date:   2019/1/17
 * Email:  2607401801@qq.com
 */
@JsonSerializable
data class ExploreResult (
    @Json(name = "classification_list")
    val classificationList:List<ExploreModel>?
)