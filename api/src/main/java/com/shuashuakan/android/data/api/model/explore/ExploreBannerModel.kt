package com.shuashuakan.android.data.api.model.explore

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  lijie
 * Date:   2018/11/30
 * Email:  2607401801@qq.com
 */
@JsonSerializable
data class ExploreBannerModel(
        @Json(name = "data_list")
        val dataList: List<ExploreBannerItemModel>,
        val type: String?,
        val title: String?
) : ExploreModel

@JsonSerializable
data class ExploreBannerLinkModel(
        @Json(name = "cover_url")
        val coverUrl: String?,
        @Json(name = "redirect_url")
        val redirectUrl: String?
)

@JsonSerializable
data class ExploreBannerItemModel(
        @Json(name = "banner_label")
        val bannerLabel: String,
        @Json(name = "begin_at")
        val beginAt: String,
        @Json(name = "end_at")
        val endAt: String,
        val image: String?,
        @Json(name = "redirect_url")
        val redirectUrl: String?,
        @Json(name = "banner_image")
        val bannerImage: String?,
        val style: String?,
        val title: String?,
        val type: String?,
        val url: String?
)