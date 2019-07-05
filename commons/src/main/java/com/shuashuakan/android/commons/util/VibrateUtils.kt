@file:JvmName("VibrateUtils")
package com.shuashuakan.android.commons.util

import android.Manifest.permission.VIBRATE
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Process
import android.os.VibrationEffect
import android.os.Vibrator

@Suppress("DEPRECATION")
@SuppressLint("MissingPermission")
fun triggerVibrate(context: Context) {
  val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
  if (hasVibratePermission(context)) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      vibrator.vibrate(VibrationEffect.createOneShot(300, 10))
    } else {
      vibrator.vibrate(300)
    }
  }
}

private fun hasVibratePermission(context: Context): Boolean {
  return context.checkPermission(VIBRATE, Process.myPid(), Process.myUid()) == PERMISSION_GRANTED
}

