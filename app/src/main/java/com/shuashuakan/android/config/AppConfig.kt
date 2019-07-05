@file:JvmName("AppConfig")

package com.shuashuakan.android.config

import androidx.content.edit
import com.shuashuakan.android.commons.cache.Storage
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.event.RefreshUnReadStatusEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfig @Inject constructor(val storage: Storage) {
    companion object {
        /**
         * 是否流量播放
         */
        private const val IS_FLOW_PLAY_SWITCH = "is_flow_play_switch"
        const val IS_SHOWED_FEED_BEGINNER_GUIDE = "is_showed_feed_beginner_guide"
        const val IS_SHOWED_PROFILE_BEGINNER_GUIDE = "is_showed_profile_beginner_guide"
        const val IS_SHOW_NEW_CHANNEL = "is_show_new_channel"
        const val TEST_CASE_ROULETTE_PAGE = "test_case_roulette_page"
        const val TEST_CASE_FOLLOW_INDEX = "test_case_follow_index"
        const val IS_HAS_UNREAD_MESSAGE = "is_has_unread_message"
        const val COMMENT_UNREAD_COUNT = "comment_unread_count"
        const val FOLLOW_UNREAD_COUNT = "follow_unread_count"
        const val HAVE_APPLY_PERMISSION = "have_apply_permission"

        const val SHOW_GUIDE_DIALOG = "show_guide_dialog"
        const val CHAIN_BOTTOM_FLOAT = "chain_bottom_float"
        const val TEST_CASE_CREATE_FEED = "test_case_create_feed"
        const val TEST_CASE_SHOW_PACKAGE = "test_case_show_package"
        const val SHOW_NEW_HOME_PAGE = "show_new_home_page"
        const val HOME_PAGE_FROM_H5 = "home_page_from_H5"
        const val IS_OPEN_DANMAKA = "is_open_danmaka"
        const val IS_SHOW_DANMAKA = "is_show_danmaka"

        const val LOGIN_TYPE = "login_type" //登录方式
        const val PHONE_NUM = "phone_num" //手机号
        const val PHONE_NUM_FULL = "phone_num_full" //完整手机号
        const val SHOE_BIND_PHONE = "show_bind_phone"//是否显示绑定手机号弹窗
        const val BIND_DIALOG_SAVE_TIME = 7 * 24 * 3600//7*24*3600
        const val APP_END = "app_end"//app退出打点使用的常量
        const val GIF_GUIDE_SHOW = "gif_guide_show"
        const val ACTIVITY_CARD_SHOW = "activity_card_show"//活动卡片是否已经展示（活动期间只显示一次活动卡片的方式）
        const val ACTIVITY_CARD_SHOW_DATE = "activity_card_show_date"//活动卡片显示的日期
        const val DAILY_CARD_ID = "daily_current_activity_id"//当前活动ID
        const val ACTIVITY_CARD_ID = "current_activity_id"

        const val ACTIVITY_CARD_ICON_FLOAT = "activity_card_icon_float"
        const val ACTIVITY_CARD_URL_FLOAT = "activity_card_url_float"
        const val ACTIVITY_CARD_EXPIRE_FLOAT = "activity_card_expire_float"

        const val CACHE_SMID = "cache_smid"//缓存数美ID

        const val PERMISSION_PHONE_TIME = "permission_phone_time"

        // 数据的小红点
        const val KEY_UN_READ_MESSAGE = "key_un_read_message"
        const val HAVE_SYS_MSG = "have_sys_msg"
        const val HAVE_SYS_PERSONAL_MSG = "have_personal_sys_msg"
    }

    fun setHaveApplyPermission(haveApply: Boolean) {
        storage.appPreference.edit(commit = true) {
            putBoolean(HAVE_APPLY_PERMISSION, haveApply)
        }
    }

    fun haveApplyPermission(): Boolean = storage.appPreference.getBoolean(HAVE_APPLY_PERMISSION, false)

    fun setCommentUnreadCount(value: Int) {
        storage.appPreference.edit(commit = true) {
            putInt(COMMENT_UNREAD_COUNT, value)
        }
    }

    fun commentUnreadCount(): Int = storage.appPreference.getInt(COMMENT_UNREAD_COUNT, 0)

    fun setFollowUnreadCount(value: Int) {
        storage.appPreference.edit(commit = true) {
            putInt(FOLLOW_UNREAD_COUNT, value)
        }
    }

    fun followUnreadCount(): Int = storage.appPreference.getInt(FOLLOW_UNREAD_COUNT, 0)

    fun isHasUnreadMessage(): Boolean = storage.appPreference.getBoolean(IS_HAS_UNREAD_MESSAGE, false)

    fun setHasUnreadMessage(value: Boolean) {
        storage.appPreference.edit(commit = true) {
            putBoolean(IS_HAS_UNREAD_MESSAGE, value)
        }
        if (!value) {
            setCommentUnreadCount(0)
            setFollowUnreadCount(0)
        }
        RxBus.get().post(RefreshUnReadStatusEvent())
    }


    fun isShowNewChannel(): Boolean = storage.appPreference.getBoolean(IS_SHOW_NEW_CHANNEL, false)

    fun setShowNewChannel(value: Boolean) {
        storage.appPreference.edit(commit = true) {
            putBoolean(IS_SHOW_NEW_CHANNEL, value)
        }
    }

    fun caseRoulettePage(): Boolean = storage.appPreference.getBoolean(TEST_CASE_ROULETTE_PAGE, false)

    fun setCaseRoulettePage(value: Boolean) {
        storage.appPreference.edit(commit = true) {
            putBoolean(TEST_CASE_ROULETTE_PAGE, value)
        }
    }

    fun isShowFollowPage(): Boolean = storage.appPreference.getBoolean(TEST_CASE_FOLLOW_INDEX, false)

    fun setShowFollowPage(value: Boolean) {
        storage.appPreference.edit(commit = true) {
            putBoolean(TEST_CASE_FOLLOW_INDEX, value)
        }
    }

    fun isFlowPlaySwitch(): Boolean = storage.appPreference.getBoolean(IS_FLOW_PLAY_SWITCH, false)

    fun setFlowPlayBoolean(value: Boolean) {
        storage.appPreference.edit(commit = true) {
            putBoolean(IS_FLOW_PLAY_SWITCH, value)
        }
    }

    fun setShowCreateFeed(value: Boolean) {
        storage.appPreference.edit(commit = true) {
            putBoolean(TEST_CASE_CREATE_FEED, value)
        }
    }

    fun isShowCreateFeed(): Boolean = storage.appPreference.getBoolean(TEST_CASE_CREATE_FEED, false)

    fun setShowPackage(value: Boolean) {
        storage.appPreference.edit(commit = true) {
            putBoolean(TEST_CASE_SHOW_PACKAGE, value)
        }
    }

    fun isShowPackage(): Boolean = storage.appPreference.getBoolean(TEST_CASE_SHOW_PACKAGE, false)

    /**
     * 首页面的展示方式
     */
    fun setShowNewHomePage(value: Boolean) {
        storage.appPreference.edit(commit = true) {
            putBoolean(SHOW_NEW_HOME_PAGE, value)
        }
    }

    fun isShowNewHomePage(): Boolean = storage.appPreference.getBoolean(SHOW_NEW_HOME_PAGE, true)

    /**
     * 从H5页面跳转过来
     */
    fun setHomePageFromH5(value: Boolean) {
        storage.appPreference.edit(commit = true) {
            putBoolean(HOME_PAGE_FROM_H5, value)
        }
    }

    fun istHomePageFromH5(): Boolean = storage.appPreference.getBoolean(HOME_PAGE_FROM_H5, false)


    /**
     * 设置灰度测试是否开放弹幕功能
     */
    fun setDanmakaOpen(isOpen: Boolean) {
        storage.appPreference.edit(commit = true) {
            putBoolean(IS_OPEN_DANMAKA, isOpen)
        }
    }

    /**
     * 灰度测试是否开放弹幕功能
     */
    fun isDanmakaOpen(): Boolean = storage.appPreference.getBoolean(IS_OPEN_DANMAKA, false)

    /**
     * 设置是否展示弹幕
     */
    fun setDanmakaShow(isShow: Boolean) {
        storage.appPreference.edit(commit = true) {
            putBoolean(IS_SHOW_DANMAKA, isShow)
        }
    }

    /**
     * 是否展示弹幕
     */
    fun isDanmakaShow(): Boolean = storage.appPreference.getBoolean(IS_SHOW_DANMAKA, true)
}