package com.shuashuakan.android.data.api.model.home

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class RouletteResponse(
    @Json(name = "config_id")
    val configId: Int,
    @Json(name = "lottery_rule_url")
    val ruleUrl: String,
    @Json(name = "roulette_list")
    val roulettes: List<Roulette>,
    val title: String,
    val cost: Int,
    val point: Int,
    @Json(name = "lottery_count_bonus")
    val lotteryCountBonus: CountBonus?
)

@JsonSerializable
data class Roulette(
    val id: Int,
    @Json(name = "goods_id")
    val productId: Long?,
    val image: String?,
    val title: String?,
    val description: String?,
    val amount: Int?,
    val type: LotteryType
) {

  override fun equals(other: Any?): Boolean {
    return this.id == (other as? Roulette)?.id
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }
}

@JsonSerializable
data class CountBonus(
    @Json(name = "bonus_description")
    val bonusDescription: List<String>?,
    @Json(name = "current_count")
    val currentCount: Int?,
    @Json(name = "max_count")
    val maxCount: Int?,
    @Json(name = "bonus_image")
    val bonusImage: String?
)
