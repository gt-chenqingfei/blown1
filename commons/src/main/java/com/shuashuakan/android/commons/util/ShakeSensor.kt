package com.shuashuakan.android.commons.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class ShakeSensor(context: Context, speedShresHold: Int) : SensorEventListener {

  constructor(context: Context): this(context, DEFAULT_SHAKE_SPEED)

  companion object {
    private const val UPDATE_INTERVAL_TIME = 100
    private const val DEFAULT_SHAKE_SPEED = 2000
  }

  private var lastUpdateTime: Long = 0
  private var lastX: Float = 0f
  private var lastY: Float = 0f
  private var lastZ: Float = 0f

  var shakeListener: OnShakeListener? = null

  private val sensorManager: SensorManager by lazy {
    return@lazy context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
  }

  private val senor: Sensor by lazy {
    return@lazy sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
  }

  override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
  }

  override fun onSensorChanged(event: SensorEvent) {
    val currentUpdateTime =  System.currentTimeMillis()
    val timeInterval = currentUpdateTime - lastUpdateTime
    if (timeInterval < UPDATE_INTERVAL_TIME) {
      return
    }
    lastUpdateTime = currentUpdateTime
    val x = event.values[0]
    val y = event.values[1]
    val z = event.values[2]
    val deltaX = x - lastX
    val deltaY = y - lastY
    val deltaZ = z - lastZ
    lastX = x
    lastY = y
    lastZ = z
    val speed = Math.sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()) / timeInterval * 10000
    if (speed >= DEFAULT_SHAKE_SPEED && shakeListener != null) {
      shakeListener?.onShakeComplete(event)
    }
  }

  fun register() {
    sensorManager.registerListener(this, senor, SensorManager.SENSOR_DELAY_UI)
  }

  fun unregister() {
    sensorManager.unregisterListener(this)
  }

  interface OnShakeListener {
    fun onShakeComplete(event: SensorEvent)
  }
}