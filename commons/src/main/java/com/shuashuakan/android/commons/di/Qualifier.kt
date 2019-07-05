package com.shuashuakan.android.commons.di

import javax.inject.Qualifier
import javax.inject.Scope

@Scope
annotation class ActivityScope

@Qualifier
annotation class AppContext

@Qualifier
annotation class ActivityContext

@Qualifier
annotation class NetworkInterceptor

@Qualifier
annotation class AppInterceptor