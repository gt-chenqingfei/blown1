package com.shuashuakan.android.data.api.model.channel

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/09/30
 * Description:
 */
@JsonSerializable
data class CategoryBannerResp(
    @Json(name = "banner_list")
    val bannerList: List<ChannelBanner>,
    @Json(name = "category_list")
    val categoryList:List<ChannelCategory>

) {
  @JsonSerializable
  data class ChannelBanner(
      @Json(name = "banner_label")
      val bannerLabel: String?,
      @Json(name = "begin_at")
      val beginAt: Long?,
      @Json(name = "end_at")
      val endAt: Long?,
      val image: String?,
      val style: String?,
      val title: String?,
      val type: String,
      val url: String?
  )

  @JsonSerializable
  data class ChannelCategory(
      val id: Long,
      val name: String,
      val sort: Int,
      val status: Int
  )
}
