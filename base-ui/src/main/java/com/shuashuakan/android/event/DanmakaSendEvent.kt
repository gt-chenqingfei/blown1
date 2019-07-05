package com.shuashuakan.android.event

/**
 *@author: zhaoningqiang
 *@time: 2019/5/10
 *@Description:弹幕发送事件
 */

class DanmakaSendEvent(
        val feedId:String,
        val content:String,
        val group_count:Int,
        val id:Long,
        val position:Long

)