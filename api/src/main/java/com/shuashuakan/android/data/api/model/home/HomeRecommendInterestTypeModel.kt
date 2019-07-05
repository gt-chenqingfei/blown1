package com.shuashuakan.android.data.api.model.home

import se.ansman.kotshi.JsonSerializable

/**
 * 首页-推荐的兴趣实体类
 *
 * Author: ZhaiDongyang
 * Date: 2019/1/2
 */
@JsonSerializable
data class HomeRecommendInterestTypeModel(
        val data : HomeRecommendInterestTypeModelDetail,
        val type:String): HomeRecommendModel

@JsonSerializable
data class HomeRecommendInterestTypeModelDetail(
        val interests: List<Interests>?,
        val title: String?,
        val subtitle: String?)

@JsonSerializable
data class Interests(
        val id: Int?,
        val name: String?,
        val is_selected: Boolean?)
