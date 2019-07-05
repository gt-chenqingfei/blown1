package com.shuashuakan.android.event

import com.shuashuakan.android.data.api.model.HomePageIcon

/**
 * 弹幕控制时间
 */
class DanmakaControlEvent(
        val isOpen:Boolean,//灰度测试是否开启弹幕功能
        var isShow:Boolean//是否显示弹幕
 )