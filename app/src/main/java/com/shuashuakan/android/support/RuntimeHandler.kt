package com.shuashuakan.android.support

object RuntimeHandler {

    private fun getDevice(): DeviceInterceptor {

        when (android.os.Build.MANUFACTURER) {
            "OPPO" -> {
                return DeviceOppo()
            }
            else -> {
                return DeviceDefault()
            }
        }
    }

    fun intecpt() {
        var interceptor: DeviceInterceptor = getDevice()
        interceptor.intercept()
    }

}