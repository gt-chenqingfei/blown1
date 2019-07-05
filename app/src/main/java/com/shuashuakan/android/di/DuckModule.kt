package com.shuashuakan.android.di

import android.app.Application
import android.content.Context
import com.shuashuakan.android.commons.cache.Storage
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.location.LocationController
import com.shuashuakan.android.location.LocationControllerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(
    includes = [NetworkModule::class, DataModule::class, VariantDataModule::class])
abstract class DuckModule {
  @Binds
  @Singleton
  @AppContext
  abstract fun provideAppContext(app: Application): Context

  @Module
  companion object {
    @JvmStatic
    @Provides
    @Singleton
    fun provideLocationController(@AppContext context: Context,
                                  storage: Storage): LocationController =
        LocationControllerImpl(context, storage)
  }
}