package com.shuashuakan.android.data.api.model.account

data class PhoneContract(
  val phone: String,
  val name: String,
  val avatar: String?
) : Comparable<PhoneContract> {
  override fun compareTo(other: PhoneContract): Int {
    return name.compareTo(other.name)
  }
}
