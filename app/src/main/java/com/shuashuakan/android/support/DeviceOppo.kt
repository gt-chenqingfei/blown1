package com.shuashuakan.android.support

import com.shuashuakan.android.utils.DaemonUtils

class DeviceOppo : DeviceInterceptor {

    override fun intercept() {
        fixOppoConcurrentTimeoutException()
    }

    private fun fixOppoConcurrentTimeoutException() {
        DaemonUtils.fixFinalizerWatchdogDemon()
    }
}