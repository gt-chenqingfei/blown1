package com.shuashuakan.android.modules

import com.shuashuakan.android.SplashActivity
import com.shuashuakan.android.commons.di.ActivityScope
import com.shuashuakan.android.modules.account.activity.*
import com.shuashuakan.android.modules.account.fragment.PerfectGenderAgeFragment
import com.shuashuakan.android.modules.account.fragment.PerfectNickNameFragment
import com.shuashuakan.android.modules.account.fragment.PerfectTopicFragment
import com.shuashuakan.android.modules.account.fragment.PrefectSelectHobbyFragment
import com.shuashuakan.android.modules.comment.CommentImageShowActivity
import com.shuashuakan.android.modules.comment.VideoCommentDialogFragment
import com.shuashuakan.android.modules.discovery.DiscoveryActivity
import com.shuashuakan.android.modules.discovery.RankingListActivity
import com.shuashuakan.android.modules.discovery.UpStarRankingListActivity
import com.shuashuakan.android.modules.discovery.fragment.DiscoveryFragment
import com.shuashuakan.android.modules.discovery.fragment.SubRankingListFragment
import com.shuashuakan.android.modules.home.HomeActivity
import com.shuashuakan.android.modules.message.MessageActivity
import com.shuashuakan.android.modules.message.MessagePersonListActivity
import com.shuashuakan.android.modules.message.SystemNoticeActivity
import com.shuashuakan.android.modules.message.fragment.MessageFragment
import com.shuashuakan.android.modules.partition.CategoryIndexActivity
import com.shuashuakan.android.modules.partition.fragment.CategoryContentFragment
import com.shuashuakan.android.modules.player.activity.VideoPlayActivity
import com.shuashuakan.android.modules.player.fragment.VideoListFragment
import com.shuashuakan.android.modules.profile.EditProfileActivity
import com.shuashuakan.android.modules.profile.EditSignActivity
import com.shuashuakan.android.modules.profile.FocusListActivity
import com.shuashuakan.android.modules.profile.UserProfileActivity
import com.shuashuakan.android.modules.profile.fragment.ProfileFragment
import com.shuashuakan.android.modules.profile.fragment.SubFeedListFragment
import com.shuashuakan.android.modules.publisher.*
import com.shuashuakan.android.modules.publisher.chains.ChainsPublishActivity
import com.shuashuakan.android.modules.setting.SettingsActivity
import com.shuashuakan.android.modules.timeline.multitype.MultiTypeTimeLineFragment
import com.shuashuakan.android.modules.timeline.profile.ProfileTimeLineFragment
import com.shuashuakan.android.modules.topic.TopicCategoryActivity
import com.shuashuakan.android.modules.topic.TopicDetailActivity
import com.shuashuakan.android.modules.topic.TopicDetailRecommendFragment
import com.shuashuakan.android.modules.viphome.VideoHallActivity
import com.shuashuakan.android.modules.web.H5Activity
import com.shuashuakan.android.modules.web.H5Fragment
import com.shuashuakan.android.modules.web.method.H5ActivityModule
import com.shuashuakan.android.modules.widget.dialogs.BindPhoneDialog
import com.shuashuakan.android.push.receiver.PushReceiver
import com.shuashuakan.android.service.PullService
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Usage of Dagger's android support:
 *
 * 以 MyActivity 为例：
 * 1. 定义 MyActivityModule
 * 2. 在 [ActivityBindingModule] 添加 [ContributesAndroidInjector] 标注的方法
 * 3. AndroidInjection.inject(instance of MyActivity)
 *
 * https://google.github.io/dagger/android
 */

@Module
abstract class ActivityBindingModule {

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun contributeSecondHomeActivity(): HomeActivity

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun contributeMessageActivity(): MessageActivity

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun contributeLandingActivity(): SplashActivity


    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun contributeH5Activity(): H5Activity


    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun contributeLoginActivity(): LoginActivity

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun contributeMobileModifyActivity(): MobileModifyActivity

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun contributeWelcomeActivity(): WelcomeActivity

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun contributeUserSettingsActivity(): SettingsActivity


    @ActivityScope
    @ContributesAndroidInjector(modules = [H5ActivityModule::class])
    abstract fun contributeH5Fragment(): H5Fragment


    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun contributeProfileFragment(): ProfileFragment


    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun contributeEditProfileActivity(): EditProfileActivity

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun contributeSubFeedListFragment(): SubFeedListFragment

//    @ActivityScope
//    @ContributesAndroidInjector()
//    abstract fun contributeSubUploadFeedListFragment(): SubUploadFeedListFragment


    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun contributeChannelDetailTimeLineActivity(): TopicDetailActivity

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun videoCommentDialogFragment(): VideoCommentDialogFragment

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun pushReceiver(): PushReceiver

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun fansListActivity(): FocusListActivity

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun profileTimeLineFragment(): ProfileTimeLineFragment

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun MultiTypeTimeLineFragment(): MultiTypeTimeLineFragment

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun userProfileActivity(): UserProfileActivity

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun exploreActivity(): DiscoveryActivity

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun upRankingListActivity(): UpStarRankingListActivity

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun commentImageShowActivity(): CommentImageShowActivity

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun editSignActivity(): EditSignActivity

    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun pullService(): PullService


    @ActivityScope
    @ContributesAndroidInjector()
    abstract fun exploreFragment(): DiscoveryFragment


    @ActivityScope
    @ContributesAndroidInjector
    abstract fun chainPublishActivity(): ChainsPublishActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun selectTopicActivity(): SelectTopicActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun topicSublistFragment(): TopicSublistFragment

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun messageFragment(): MessageFragment

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun videoRecordActivity(): VideoRecordActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun videoEditActivity(): VideoEditActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun messagePersonListActivity(): MessagePersonListActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun systemNoticeActivity(): SystemNoticeActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun createGifActivity(): CreateGifActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindPhoneDialog(): BindPhoneDialog

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun rankingListActivity(): RankingListActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun subRankingListFragment(): SubRankingListFragment

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun channelDetailRecommendFragment(): TopicDetailRecommendFragment


    @ActivityScope
    @ContributesAndroidInjector
    abstract fun PerfectSelectHobbyActivity(): PerfectSelectHobbyActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun selectHobbyFragment(): PrefectSelectHobbyFragment

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun secondVideoPlayActivity(): VideoPlayActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun recommendVideoFragment(): VideoListFragment

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun profilePerctActivity(): PerfectProfileActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun perfectNickNameFragment(): PerfectNickNameFragment

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun perfectGenderAgeFragment(): PerfectGenderAgeFragment

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun perfectTopicFragment(): PerfectTopicFragment

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun topicCategoryActivity(): TopicCategoryActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun perfectVideoHallActivity(): VideoHallActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun partitionIndexActivity(): CategoryIndexActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun partitionContentFragment(): CategoryContentFragment

}