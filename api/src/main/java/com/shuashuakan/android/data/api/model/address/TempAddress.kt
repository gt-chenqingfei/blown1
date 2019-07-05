package com.shuashuakan.android.data.api.model.address

import com.shuashuakan.android.ui.address.AddressDistrict
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class TempAddress(
  val province: List<AddressProvince>,
  val city: Map<String, List<AddressCity>>?,
  val district: Map<String, List<AddressDistrict>>
)
