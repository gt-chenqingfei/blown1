package com.shuashuakan.android.di

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.shuashuakan.android.commons.di.ActivityScope
import com.shuashuakan.android.commons.di.NetworkInterceptor
import com.shuashuakan.android.ui.DebugDrawerFragment
import com.shuashuakan.android.ui.PrometheusActivity
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
abstract class VariantDataModule {

  @Module
  companion object {
    @Provides
    @Singleton
    @JvmStatic
    @IntoMap
    @IntKey(Int.MAX_VALUE - 1) // order
    @NetworkInterceptor
    fun provideHttpLoggingInterceptor(): Interceptor {
      val logger = HttpLoggingInterceptor.Logger {
//        Timber.tag("HTTP").v(it)
      }
      return HttpLoggingInterceptor(logger)
          .apply { level = HttpLoggingInterceptor.Level.BODY}
    }

    @Provides
    @Singleton
    @JvmStatic
    @IntoMap
    @IntKey(Int.MAX_VALUE) // order
    @NetworkInterceptor
    fun provideStethoInterceptor(): Interceptor = StethoInterceptor()
  }

  @ActivityScope
  @ContributesAndroidInjector()
  abstract fun contributeDebugDrawerFragment(): DebugDrawerFragment

  @ActivityScope
  @ContributesAndroidInjector()
  abstract fun contributePrometheusActivity(): PrometheusActivity
}