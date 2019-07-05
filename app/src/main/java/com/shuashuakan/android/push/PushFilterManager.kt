package com.shuashuakan.android.push

/**
 * @author hushiguang
 * @since 2019-06-12.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */

object PushFilterManager {
    private val filterUUIDList = arrayListOf<String>()
    fun filterUUID(UUID: String): Boolean {
        if (filterUUIDList.contains(UUID)) {
            return true
        }
        filterUUIDList.add(UUID)
        return false
    }
}