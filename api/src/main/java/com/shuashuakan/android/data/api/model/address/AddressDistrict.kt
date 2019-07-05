package com.shuashuakan.android.ui.address

import com.shuashuakan.android.data.api.model.address.AddressCity
import com.shuashuakan.android.data.api.model.address.AddressProvince
import com.shuashuakan.android.data.api.model.address.EnjoyAddress
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
class AddressDistrict(
  @Json(name = "province_id")
  var provinceId: Int?,
  @Json(name = "province_name")
  var provinceName: String?,
  @Json(name = "city_id")
  var cityId: Int?,
  @Json(name = "city_name")
  var cityName: String?,
  @Json(name = "district_id")
  var districtId: Int?,
  @Json(name = "district_name")
  var districtName: String?
) {

  fun setData(addressCity: AddressCity) {
    provinceId = addressCity.provinceId
    provinceName = addressCity.provinceName
    cityId = addressCity.cityId
    cityName = addressCity.cityName
  }

  fun setData(addressProvince: AddressProvince) {
    provinceId = addressProvince.provinceId
    provinceName = addressProvince.provinceName
  }

  fun setData(enjoyAddress: EnjoyAddress) {
    provinceId = enjoyAddress.provinceId
    provinceName = enjoyAddress.provinceName
    cityId = enjoyAddress.cityId
    cityName = enjoyAddress.cityName
    districtId = enjoyAddress.districtId
    districtName = enjoyAddress.districtName
  }

}