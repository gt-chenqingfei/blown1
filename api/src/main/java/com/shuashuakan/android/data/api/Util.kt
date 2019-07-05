package com.shuashuakan.android.data.api

import kotlin.reflect.KClass

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/11/03
 * Description:
 */
fun runtimeAdapterFactory(
    label: String,
    baseType: KClass<*>,
    builder: () -> Map<String, KClass<*>>
): RuntimeJsonAdapterFactory {

  return RuntimeJsonAdapterFactory(baseType.java, label)
      .apply {
        val map = builder()
        map.forEach {
          registerSubtype(it.value.java, it.key)
        }
      }
}