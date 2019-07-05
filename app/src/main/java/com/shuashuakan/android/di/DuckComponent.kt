package com.shuashuakan.android.di

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.text.TextUtils
import com.shuashuakan.android.ApplicationMonitor
import com.shuashuakan.android.DuckApplication
import com.shuashuakan.android.commons.cache.Storage
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.location.LocationController
import com.shuashuakan.android.modules.ActivityBindingModule
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.account.JMessageFunc
import com.shuashuakan.android.modules.message.badage.BadgeManager
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.utils.ApkChannel
import com.shuashuakan.android.utils.DeviceUtils
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import me.twocities.linker.LinkResolver
import javax.inject.Singleton

@Singleton
@Component(modules = [DuckModule::class,
    AndroidInjectionModule::class,
    ActivityBindingModule::class])
abstract class DuckComponent {
    abstract fun inject(app: DuckApplication)

    abstract fun linkResolver(): LinkResolver

    abstract fun apkChannel(): ApkChannel

    abstract fun accountManager(): AccountManager

    abstract fun deviceUtils(): DeviceUtils

    abstract fun apiService(): ApiService

    abstract fun appMonitor(): ApplicationMonitor

    abstract fun appConfig(): AppConfig

    abstract fun spider(): Spider

    abstract fun locationController(): LocationController

    abstract fun jMessageFunc(): JMessageFunc

    abstract fun unReadManager(): BadgeManager
    abstract fun storage(): Storage

    @Component.Builder
    interface Builder {
        fun build(): DuckComponent

        @BindsInstance
        fun application(app: Application): Builder
    }

    companion object {
        private const val COMPONENT_SERVICE_NAME = "duck.component"

        fun matches(name: String): Boolean = TextUtils.equals(COMPONENT_SERVICE_NAME, name)

        @SuppressLint("WrongConstant")
        fun get(context: Context): DuckComponent {
            val appContext = context.applicationContext
            return appContext.getSystemService(COMPONENT_SERVICE_NAME) as DuckComponent
        }
    }
}