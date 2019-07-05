package com.shuashuakan.android.data.api.model.address

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class AddressProvince(
  @Json(name = "province_id")
  val provinceId: Int,
  @Json(name = "province_name")
  val provinceName: String
)
