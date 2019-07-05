package com.shuashuakan.android.location

import android.content.Context
import arrow.core.getOrElse
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
import com.amap.api.location.AMapLocationListener
import com.shuashuakan.android.commons.cache.Storage
import timber.log.Timber

class LocationControllerImpl(
    context: Context, storage: Storage) : LocationController, AMapLocationListener {

  private val ampLocationClient: AMapLocationClient = AMapLocationClient(context)
  private val cache = storage.appCache.cacheOf<DuckLocation>()
  private val locationCacheKey: String = "location_cache_key"

  init {
    val option = AMapLocationClientOption()
    option.locationMode = Hight_Accuracy
    option.isOnceLocationLatest = true
    option.isLocationCacheEnable = false
    option.isOnceLocation = true
    ampLocationClient.setLocationOption(option)
    ampLocationClient.setLocationListener(this)
  }

  override fun onLocationChanged(location: AMapLocation?) {
    location?.let {
      if (it.errorCode == 0) {
        val fishLocation = DuckLocation(location.longitude, location.latitude, location.cityCode)
        cache.put(locationCacheKey, fishLocation)
        stopLocation()
      } else {
        Timber.e("Amp location errorCode: ${it.errorCode}, errorInfo: ${it.errorInfo}")
      }
    }
  }

  override fun startLocation() {
    ampLocationClient.startLocation()
  }

  private fun stopLocation() {
    ampLocationClient.stopLocation()
  }

  override fun getLocation(): DuckLocation {
    return cache.get(locationCacheKey).getOrElse { DuckLocation.invalid() }
  }
}