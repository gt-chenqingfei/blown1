package com.shuashuakan.android.location

interface LocationController {
  fun startLocation()
  fun getLocation(): DuckLocation
}