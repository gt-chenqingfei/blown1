package com.shuashuakan.android.data.api.model.explore

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  lijie
 * Date:   2018/11/30
 * Email:  2607401801@qq.com
 */
@JsonSerializable
data class ExploreCategoryModel(
        @Json(name = "data_list")
        val dataList: List<ExploreCategoryLinkModel>,
        val type: String?,
        val title: String?
) : ExploreModel

@JsonSerializable
data class ExploreCategoryLinkModel(
        val id: Int?,
        @Json(name = "image_url")
        val imageUrl: String?,
        val name: String?,
        @Json(name = "redirect_url")
        val redirectUrl: String?
)