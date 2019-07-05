package com.shuashuakan.android.danmaku

import android.support.v4.util.LruCache
import com.shuashuakan.android.event.DanmakaSendEvent
import org.json.JSONArray
import org.json.JSONObject

/**
 *@author: zhaoningqiang
 *@time: 2019/5/6
 *@Description:
 */
object SSKDanmakuCache {
    private val mCache: LruCache<String, JSONArray> = LruCache(32)

    fun putBarrageToMemory(feedId: String, barrageByteArray: JSONArray) {
        mCache.put(feedId, barrageByteArray)
    }

    fun getBarrageFromMemory(feedId: String): JSONArray? {
        return mCache.get(feedId)
    }

    fun addBarrage(feedId: String,danmaka: DanmakaSendEvent){
        val bytes = mCache.get(feedId)
        if (bytes == null){
            val items = JSONArray()
            val item = JSONObject()
            item.put("content",danmaka.content)
            item.put("group_count",danmaka.group_count)
            item.put("id",danmaka.id)
            item.put("position",danmaka.position)
            items.put(item)
            mCache.put(feedId, items)

        }else{
            val item = JSONObject()
            item.put("content",danmaka.content)
            item.put("group_count",danmaka.group_count)
            item.put("id",danmaka.id)
            item.put("position",danmaka.position)
            bytes.put(item)
            mCache.put(feedId, bytes)
        }
    }
}