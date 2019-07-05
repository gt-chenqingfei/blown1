package com.shuashuakan.android.analytics

import com.shuashuakan.android.spider.Spider.DeviceIdManager
import com.shuashuakan.android.utils.DeviceUtils

class DeviceIdManagerImpl(private val deviceUtils: DeviceUtils) : DeviceIdManager {

    override fun getDeviceId(): String {
        return deviceUtils.getDeviceId()
    }

    override fun setListener(l: DeviceIdManager.DeviceIdChangedListener?) {
    }

}