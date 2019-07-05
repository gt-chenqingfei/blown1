package com.shuashuakan.android.push

import android.content.Context
import cn.jpush.im.android.api.JMessageClient
import cn.jpush.im.android.api.content.CustomContent
import cn.jpush.im.android.api.content.MessageContent
import cn.jpush.im.android.api.content.TextContent
import cn.jpush.im.android.api.event.ChatRoomMessageEvent
import cn.jpush.im.android.api.event.MessageBaseEvent
import cn.jpush.im.android.api.event.MessageEvent
import cn.jpush.im.android.api.event.NotificationClickEvent
import com.google.gson.jpush.Gson
import com.shuashuakan.android.BuildConfig
import com.shuashuakan.android.commons.cache.Storage
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.data.api.model.TestCaseResp
import com.shuashuakan.android.data.api.model.account.UserAccount
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.message.badage.BadgeManager
import com.shuashuakan.android.modules.profile.fragment.ProfileFragment
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.utils.daggerComponent
import com.shuashuakan.android.utils.getSpider
import org.json.JSONObject
import timber.log.Timber


/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/09/26
 * Description:
 */
class IMEventListener constructor(val context: Context) {

    private val appConfig: AppConfig = context.daggerComponent().appConfig()
    private val mStorage: Storage = context.daggerComponent().storage()
    private val badgeManager: BadgeManager = context.daggerComponent().unReadManager()

    fun onEvent(event: NotificationClickEvent) {
    }

    fun onEvent(event: MessageBaseEvent) {
        if (BuildConfig.DEBUG) {
            Timber.e("IMEventListener receiver event : $event")
        }

        //RECEIVE_NEW  LIKE_NEW  FOLLOW_NEW  SYSTEM_NEW
        var text: String? = null

        var content: MessageContent? = null
        var receiverTime: Long? = null
        when (event) {
            is MessageEvent -> {
                content = event.message.content
                receiverTime = event.message.createTime
            }
            is ChatRoomMessageEvent -> {
                if (event.messages.isNotEmpty()) {
                    val message = event.messages[0]
                    content = message.content
                    receiverTime = message.createTime
                }
            }
        }

        when (content) {
            is CustomContent -> {
                text = content.getStringValue("text")
            }
            is TextContent -> {
                if (BuildConfig.DEBUG) {
                    Timber.e("IMEventListener parser only debug mode")
                    text = content.text
                }
            }
        }
        if (filterReceiverMessage(receiverTime)) return
        parseMessageContent(text)
    }


    // 如果收到的消息时间比我注册的时间小 不处理消息
    private fun filterReceiverMessage(receiverTime: Long?): Boolean {
        receiverTime ?: return true
        // 从缓存用户取数据
        val cacheOf = mStorage.userCache.cacheOf<UserAccount>()
        val account = cacheOf.get(ProfileFragment.ACCOUNT_CACHE_KEY).orNull()
        // 用户缓存为空的情况 取极光的用户缓存
        val userRegisterTime = if (account == null) {
            val myInfo = JMessageClient.getMyInfo()
            myInfo ?: return true
            myInfo.getmTime() * 1000L
        } else {
            // 用户缓存数据没有被复制的情况下 取极光的用户数据
            if (account.createAt == null || account.createAt == 0L) {
                val myInfo = JMessageClient.getMyInfo()
                myInfo ?: return true
                myInfo.getmTime() * 1000L
            } else {
                account.createAt
            }
        }

        userRegisterTime ?: return true
        if (receiverTime < userRegisterTime) {
            return true
        }
        return false
    }


    private fun parseMessageContent(text: String?) {
        text ?: return
        if (!isJSON(text)) {
            return
        }
        val jsonObject = JSONObject(text)
        val scope: String = jsonObject.getString("scope")
        val uuid: String = jsonObject.optString("uuid")
        val scene: String = jsonObject.getString("scene")
        val data: JSONObject? = jsonObject.optJSONObject("data")
        if (PushFilterManager.filterUUID(uuid)) {
            return
        }
        when (scope) {
            "ALTER" ->
                when (scene) {
                    "IN_APP" -> {
                        if (data != null) {
                            PushManagerForInapp.show(context, data, 3000)
                        }
                    }
                }
            "NOTIFICATION" -> {
                when (scene) {
                    "SYSTEM_NEW" -> {
                        // 系统和刷刷酱消息 BROADCAST
                        data ?: return
                        var notificationType = data.optString("range")
                        notificationType ?: return
                        when (notificationType.toUpperCase()) {
                            "BROADCAST" -> {
                                // 系统消息
                                badgeManager.updateSystemBadge(true)
                            }
                            "PERSONAL" -> {
                                // 个人消息
                                badgeManager.updateSystemPersonalBadge(true)
                            }
                        }
                    }
                    else -> {
                        // 评论及其他互动消息
                        badgeManager.updateAppBadgeCount()
                    }
                }
                badgeManager.startMainBadgeBubbleInterval()
            }
            "TEST_CASE" -> {
                when (scene) {
                    "REFRESH_CASE" -> {
                        refreshABTestCase()
                    }
                }
            }
        }
    }

    private fun refreshABTestCase() {
        context.daggerComponent()
                .apiService()
                .testcase()
                .applySchedulers()
                .subscribeApi(
                        onNext = {
                            appConfig.setShowNewChannel(it.test_case_channel_page != null && it.test_case_channel_page!!)
                            appConfig.setCaseRoulettePage(it.test_case_roulette_page != null && it.test_case_roulette_page!!)
                            appConfig.setShowFollowPage(it.test_case_follow_index != null && it.test_case_follow_index!!)
                            appConfig.setShowCreateFeed(it.test_case_create_feed != null && it.test_case_create_feed!!)
                            appConfig.setShowPackage(it.test_case_show_package != null && it.test_case_show_package!!)
                            appConfig.setShowNewHomePage(it.test_case_new_display_page != null && it.test_case_new_display_page!!)
                            appConfig.setDanmakaOpen(it.test_case_danmuku != null && it.test_case_danmuku!!)
                            postSpiderEvent(it)
                        })
    }

    private fun postSpiderEvent(response: TestCaseResp) {
        val toJson = Gson().toJson(response)
        val toMap = Gson().fromJson(toJson, Map::class.java)
        toMap.filter { it.value == true }
        val toJsonMap = Gson().toJson(toMap)
        context.getSpider().programEvent(SpiderEventNames.Program.AB_INTERFACE).put("info", toJsonMap).track()
    }

    private fun isJSON(str: String): Boolean {
        return try {
            JSONObject(str)
            true
        } catch (e: Exception) {
            false
        }

    }
}
