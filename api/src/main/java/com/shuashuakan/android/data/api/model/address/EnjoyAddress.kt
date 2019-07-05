package com.shuashuakan.android.data.api.model.address

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

@JsonSerializable
class EnjoyAddress @KotshiConstructor constructor(
  @Json(name="province_id") var provinceId: Int?,
  @Json(name="province_name") var provinceName: String?,
  @Json(name="city_id") var cityId: Int?,
  @Json(name="city_name") var cityName: String?,
  @Json(name="district_id") var districtId: Int?,
  @Json(name="district_name") var districtName: String?,
  @Json(name="id") var addressId: Int?,
  @Json(name="phone") var mobilePhone: String?,
  @Json(name="zip_code") var zipCode: String?,
  @Json(name="detail_address") var detailAddress: String?,
  @Json(name="default") var isDefault: Boolean?,
  @Json(name="addressee") var userName: String?
): Parcelable {

  constructor(parcel: Parcel) : this(
      parcel.readValue(Int::class.java.classLoader) as Int?,
      parcel.readString(),
      parcel.readValue(Int::class.java.classLoader) as Int?,
      parcel.readString(),
      parcel.readValue(Int::class.java.classLoader) as Int?,
      parcel.readString(),
      parcel.readValue(Int::class.java.classLoader) as Int?,
      parcel.readString(),
      parcel.readString(),
      parcel.readString(),
      parcel.readValue(Boolean::class.java.classLoader) as Boolean?,
      parcel.readString()
  )

  fun parseAddress(): String {
    return if (provinceName == cityName) {
      provinceName + districtName + detailAddress
    } else {
      provinceName + cityName + districtName + detailAddress
    }
  }

  override fun writeToParcel(
    parcel: Parcel,
    flags: Int
  ) {
    parcel.writeValue(provinceId)
    parcel.writeString(provinceName)
    parcel.writeValue(cityId)
    parcel.writeString(cityName)
    parcel.writeValue(districtId)
    parcel.writeString(districtName)
    parcel.writeValue(addressId)
    parcel.writeString(mobilePhone)
    parcel.writeString(zipCode)
    parcel.writeString(detailAddress)
    parcel.writeValue(isDefault)
    parcel.writeString(userName)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Creator<EnjoyAddress> {
    override fun createFromParcel(parcel: Parcel): EnjoyAddress {
      return EnjoyAddress(parcel)
    }

    override fun newArray(size: Int): Array<EnjoyAddress?> {
      return arrayOfNulls(size)
    }
  }

}
