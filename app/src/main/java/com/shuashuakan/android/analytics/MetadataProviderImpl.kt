package com.shuashuakan.android.analytics

import android.Manifest.permission.ACCESS_WIFI_STATE
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Context.WIFI_SERVICE
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Build.BRAND
import android.os.Build.MODEL
import android.os.Build.VERSION.SDK_INT
import com.ishumei.smantifraud.SmAntiFraud
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.location.LocationController
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.utils.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 打点基础信息
 * TODO 参考 Highgarden MetadataProviderImpl.java 实现
 */
@Singleton
class MetadataProviderImpl @Inject constructor(@AppContext private val context: Context,
                                               private val deviceUtils: DeviceUtils,
                                               private val accountManager: AccountManager,
                                               private val locationController: LocationController) : Spider.MetadataProvider {

    private fun hasPermission(context: Context, permission: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == context.checkCallingOrSelfPermission(permission)
    }

    override fun getUserId(): Long? {
        return accountManager.account()?.userId
    }

    override fun get(): MutableMap<String, Any> {
        val metadata: MutableMap<String, Any> = mutableMapOf()
        val deviceId = SmAntiFraud.getDeviceId()

        metadata["sm_id"] = deviceId
        metadata["sm_id_type"] = SmAntiFraud.checkDeviceIdType(deviceId)

        metadata["os"] = "Android"
        metadata["os_version"] = Build.VERSION.RELEASE

        putString(metadata, "device_model", MODEL)

        putString(metadata, "device_brand", BRAND)

        metadata["android_apk_market"] = context.channelName()

        metadata["android_api_level"] = SDK_INT

        metadata["imei"] = deviceUtils.getIMEI()

        metadata["mac"] = deviceUtils.getMac()

        metadata["deviceid"] = deviceUtils.getDeviceId()

        metadata["screen_size"] = "(${context.getScreenSize().x},${context.getScreenSize().y})"

        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val batteryManager = context.getSystemService(BATTERY_SERVICE) as BatteryManager
            val battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            metadata["battery_level"] = battery
        }

        metadata["network_type"] = NetworkUtil.GetNetworkType(context)

        accountManager.account()?.userId?.let {
            metadata["user_id"] = it
        }

        val location = locationController.getLocation()

        if (location.isValid()) {
            metadata["latitude"] = location.latitude
            metadata["longitude"] = location.longitude
            metadata["location_city_id"] = location.cityCode
        }

        try {
            appendWifiInfo(metadata)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return metadata
    }

    private fun appendWifiInfo(objectMap: MutableMap<String, Any>) {
        if (hasPermission(context, ACCESS_WIFI_STATE)) {
            val wifiManager = context.getSystemService(WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            if (wifiInfo != null) {
                putString(objectMap, "ssid", wifiInfo.ssid)
                putString(objectMap, "bssid", wifiInfo.bssid)
            }
        }
    }

    private fun putString(map: MutableMap<String, Any>, key: String, value: String) {
        if (!Strings.isBlank(value) && !Strings.isBlank(key)) {
            map[key] = value
        }
    }
}