package com.shuashuakan.android.data.api.model.address

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class AddressCity(
  @Json(name = "province_id")
  val provinceId: Int,
  @Json(name = "province_name")
  val provinceName: String,
  @Json(name = "city_id")
  val cityId: Int,
  @Json(name = "city_name")
  val cityName: String,
  @Json(name = "district_id")
  val districtId: Int?,
  @Json(name = "district_name")
  val districtName: String?
)