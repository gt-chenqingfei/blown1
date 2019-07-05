package com.shuashuakan.android.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.content.edit
import javax.inject.Inject
import javax.inject.Singleton
import  com.shuashuakan.android.commons.cache.Storage
import com.shuashuakan.android.commons.di.AppContext
import timber.log.Timber
import android.support.v4.app.ActivityCompat
import java.lang.reflect.AccessibleObject.setAccessible
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.Manifest.permission.READ_PHONE_STATE
import android.os.Build
import android.support.annotation.RequiresPermission
import android.support.v4.content.ContextCompat
import java.net.NetworkInterface
import java.net.NetworkInterface.getNetworkInterfaces
import java.util.*


@Singleton
class DeviceUtils @Inject constructor(@AppContext val context: Context, val storage: Storage) {

  companion object {
    private const val DEVICE_ID = "device_id"
  }

  fun getDeviceId(): String {
    var deviceId = storage.userPreference.getString(DEVICE_ID, "")
    if (deviceId.isNotBlank()) {
      return deviceId
    } else {
      deviceId = buildDeviceId()
      storage.userPreference.edit(true) {
        putString(DEVICE_ID, deviceId)
      }
    }
    return deviceId
  }

  @SuppressLint("MissingPermission", "HardwareIds")
  @Suppress("DEPRECATION")
  private fun buildDeviceId(): String {
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    val androidId: String? =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    if (hasPermission(Manifest.permission.READ_PHONE_STATE)) {
      val deviceId: String? = telephonyManager.deviceId
      val serialId: String? = telephonyManager.simSerialNumber
      return with(StringBuilder()) {
        if (!deviceId.isNullOrEmpty()) {
          append(deviceId)
        }
        if (!serialId.isNullOrEmpty()) {
          append(serialId)
        }
        if (!androidId.isNullOrEmpty()) {
          append(androidId)
        }
        return@with hash(toString())
      }
    } else {
      return with(StringBuilder()) {
        if (!androidId.isNullOrEmpty()) {
          append(androidId)
        }
        return@with hash(toString())
      }
    }
  }

  /**
   * 判断是否有某项权限
   *
   * @param context
   * @param permission
   * @return
   */
  fun hasPermission(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
  }

  /**
   * Return the IMEI.
   *
   * @return the IMEI
   */
  @SuppressLint("HardwareIds")
  fun getIMEI(): String {
    try {
      val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        return tm.imei
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        try {
          val clazz = tm.javaClass

          val getImeiMethod = clazz.getDeclaredMethod("getImei")
          getImeiMethod.isAccessible = true
          val imei = getImeiMethod.invoke(tm) as String?
          if (imei != null) return imei
        } catch (e: Exception) {
          Timber.e("getIMEI:$e")
        }
      }
      val imei: String? = tm.deviceId
      return if (imei != null && imei.length == 15) {
        imei
      } else return ""
    }catch (e:java.lang.Exception){

    }
    return ""
  }

  /**
   * 获取MAC地址
   *
   * @return the Mac
   */
  fun getMac(): String {
    if (!hasPermission(Manifest.permission.READ_PHONE_STATE))
      return ""
    try {
      val all = Collections.list(NetworkInterface.getNetworkInterfaces())
      for (nif in all) {
        if (!nif.name.equals("wlan0", true)) continue
        val macBytes = nif.hardwareAddress ?: return ""
        val res1 = StringBuilder()
        for (b in macBytes) {
          res1.append(String.format("%02X:", b))
        }
        if (res1.isNotEmpty()) {
          res1.deleteCharAt(res1.length - 1)
        }
        return res1.toString()
      }
    } catch (ex: Exception) {
      Timber.e("getMac:$ex")
    }
    return ""
  }
}