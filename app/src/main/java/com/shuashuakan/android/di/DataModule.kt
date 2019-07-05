package com.shuashuakan.android.di

import android.content.Context
import com.shuashuakan.android.analytics.DeviceIdManagerImpl
import com.shuashuakan.android.analytics.MetadataProviderImpl
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.data.HttpUrlInterceptor
import com.shuashuakan.android.data.LinkResolveLogger
import com.shuashuakan.android.data.LinkerFishLinkResolverBuilder
import com.shuashuakan.android.data.UserScopeInterceptor
import com.shuashuakan.android.data.api.BuildConfig
import com.shuashuakan.android.data.api.COMMON
import com.shuashuakan.android.data.api.model.ApiJsonAdapterFactory
import com.shuashuakan.android.data.api.model.channel.CategoryTypeModel
import com.shuashuakan.android.data.api.model.explore.ExploreModel
import com.shuashuakan.android.data.api.model.home.HomeRecommendModel
import com.shuashuakan.android.data.api.model.home.ProfileTimeLineModel
import com.shuashuakan.android.data.api.model.home.TimeLineModel
import com.shuashuakan.android.data.api.model.home.TimeLineRecommendModel
import com.shuashuakan.android.data.api.model.home.multitypetimeline.CardsType
import com.shuashuakan.android.data.api.model.partition.PartitionModel
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.utils.DeviceUtils
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import me.twocities.linker.LinkResolver
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton

@Module
class DataModule {

  @Module
  companion object {
    @JvmStatic
    @Provides
    @Singleton
    fun provideLinkResolver(@AppContext context: Context,
                            accountManager: AccountManager): LinkResolver {
      return LinkerFishLinkResolverBuilder(context)
          .addInterceptor(HttpUrlInterceptor(context))
          .addInterceptor(UserScopeInterceptor(context, accountManager))
          .setListener(LinkResolveLogger())
          .build()
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
      return Moshi.Builder()
          .add(ApiJsonAdapterFactory.INSTANCE)
          .add(CategoryTypeModel.create())
          .add(TimeLineModel.create())
          .add(ProfileTimeLineModel.create())
          .add(HomeRecommendModel.create())
          .add(TimeLineRecommendModel.create())
          .add(ExploreModel.create())
          .add(CardsType.create())
          .add(PartitionModel.create())
          .build()
    }

    @JvmStatic
    @Provides
    @Singleton
    fun provideSpider(@AppContext context: Context,
                      @Named(COMMON) okHttpClient: OkHttpClient,
                      metadataProvider: MetadataProviderImpl,
                      deviceUtils: DeviceUtils): Spider {
      return Spider(context,
          metadataProvider,
          okHttpClient,
          BuildConfig.DEBUG,
          DeviceIdManagerImpl(deviceUtils))
    }
  }
}