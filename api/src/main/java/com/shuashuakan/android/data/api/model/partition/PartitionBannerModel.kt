package com.shuashuakan.android.data.api.model.partition

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  lijie
 * Date:   2018/11/30
 * Email:  2607401801@qq.com
 */
@JsonSerializable
data class PartitionBannerModel(
        @Json(name = "data_list")
        val dataList: List<PartitionBannerItemModel>,
        val type: String?,
        val title: String?
) : PartitionModel

@JsonSerializable
data class PartitionBannerLinkModel(
        @Json(name = "cover_url")
        val coverUrl: String?,
        @Json(name = "redirect_url")
        val redirectUrl: String?
)

@JsonSerializable
data class PartitionBannerItemModel(
        val id: Int?,
        @Json(name = "image_id")
        val imageId: Long?,
        @Json(name = "target_id")
        val targetId: Int?,
        @Json(name = "banner_index")
        val bannerIndex: Int?,
        @Json(name = "begin_at")
        val beginAt: String?,
        @Json(name = "end_at")
        val endAt: String?,
        val image: String?,
        val url: String?
)