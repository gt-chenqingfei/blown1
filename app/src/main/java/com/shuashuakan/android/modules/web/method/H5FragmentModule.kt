package com.shuashuakan.android.modules.web.method

import com.shuashuakan.android.commons.di.ActivityScope
import com.shuashuakan.android.js.MethodProcessor
import com.shuashuakan.android.modules.web.H5Fragment
import com.shuashuakan.android.modules.web.method.ViewMethodProcessor.ViewController
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet


@Module
abstract class H5ActivityModule {

  @Binds
  @ActivityScope
  abstract fun provideH5Fragment(activity: H5Fragment): ViewController

  @Binds
  @IntoSet
  abstract fun userMethodProcessor(processor: UserMethodProcessor): MethodProcessor

  @Binds
  @IntoSet
  abstract fun stateMethodProcessor(processor: StateMethodProcessor): MethodProcessor

  @Binds
  @IntoSet
  abstract fun deviceMethodProcessor(processor: DeviceMethodProcessor): MethodProcessor

  @Binds
  @IntoSet
  abstract fun viewMethodProcessor(processor: ViewMethodProcessor): MethodProcessor
}