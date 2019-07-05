package com.shuashuakan.android.data.api.model

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor
import java.io.Serializable

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/08/29
 * Description:
 */
@JsonSerializable
data class ApiChest @KotshiConstructor constructor(
    val title: String,
    val content: String?,
    @Json(name = "chest_award_list")
    val chestAwardList: List<ChestAward>
) : Serializable {
  @JsonSerializable
  data class ChestAward @KotshiConstructor constructor(
      val id: String,
      val title: String,
      val status: String?,
      val style: String?,
      val description: String?,
      val image: String) : Serializable
}

