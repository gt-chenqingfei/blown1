package com.shuashuakan.android.data.api.model

import com.squareup.moshi.JsonAdapter
import se.ansman.kotshi.KotshiJsonAdapterFactory

@KotshiJsonAdapterFactory
abstract class ApiJsonAdapterFactory : JsonAdapter.Factory {
  companion object {
    val INSTANCE: ApiJsonAdapterFactory = KotshiApiJsonAdapterFactory()
  }
}