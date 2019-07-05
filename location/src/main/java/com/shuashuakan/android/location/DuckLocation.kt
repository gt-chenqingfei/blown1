package com.shuashuakan.android.location

data class DuckLocation(val latitude: Double = Double.NaN, val longitude: Double = Double.NaN, val cityCode: String = "") {

  fun isValid(): Boolean {
    return !latitude.isNaN() && !longitude.isNaN() && !cityCode.isNullOrEmpty()
  }

  companion object {
    fun invalid(): DuckLocation = DuckLocation()
  }
}