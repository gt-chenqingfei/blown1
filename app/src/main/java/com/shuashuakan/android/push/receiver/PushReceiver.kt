package com.shuashuakan.android.push.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import cn.jpush.android.api.JPushInterface.*
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI
import com.shuashuakan.android.FishInjection
import com.shuashuakan.android.android.router.Router
import com.shuashuakan.android.commons.util.startActivity
import com.shuashuakan.android.modules.HOME_PAGE
import com.shuashuakan.android.modules.home.HomeActivity
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.utils.getSpider
import com.shuashuakan.android.utils.throwOrLog
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject


class PushReceiver : BroadcastReceiver() {

    @Inject
    lateinit var spider: Spider
    private var registerionId: String? = null

    override fun onReceive(context: Context, intent: Intent) {
        FishInjection.inject(this, context)
        val bundle = intent.extras
        bundle ?: return

        when (intent.action) {
            ACTION_REGISTRATION_ID -> {
                registerionId = bundle.getString(EXTRA_REGISTRATION_ID)
                log("ACTION_REGISTRATION_ID: $registerionId")
                val properties = JSONObject()
                properties.put("jpush_android", registerionId)
                SensorsDataAPI.sharedInstance(context).profileSet(properties)
                // 获取到registrationId 需要打点给spider
                spider.programEvent(SpiderEventNames.Program.PUSH_REGISTER_ID).put("registration_id", registerionId
                        ?: "").track()
            }
            ACTION_MESSAGE_RECEIVED -> {
                log("ACTION_MESSAGE_RECEIVED")
                val extras = bundle.getString(EXTRA_EXTRA)
                val obj = JSONObject(extras)
                val title = obj.optString("title")
                spider.programEvent(SpiderEventNames.Program.RECIVED_REMOTE_PUSH).track()
                SensorsDataAPI.sharedInstance()
                        .track("app_received_notification", JSONObject().put("title", title))
            }
            ACTION_NOTIFICATION_RECEIVED -> {
                log("ACTION_NOTIFICATION_RECEIVED")
            }
            ACTION_NOTIFICATION_OPENED -> {
                log("ACTION_NOTIFICATION_OPENED")
                openNotification(context, bundle)
            }
            ACTION_CONNECTION_CHANGE -> {
                log("ACTION_CONNECTION_CHANGE")
            }
        }
    }

    private fun openNotification(context: Context, bundle: Bundle) {

        val extras = bundle.getString(EXTRA_EXTRA)
        val url: String
        val obj = JSONObject(extras)
        try {
            if (extras != null && extras.contains("redirect_url")) {
                url = obj.getString("redirect_url")
                val result = Router.get(context).resolve(url)
                if (result.success) {
                    result.intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    val backIntent = Intent(context, HomeActivity::class.java)
                    backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    val pendingIntent = PendingIntent.getActivities(context, 0,
                            arrayOf(backIntent, result.intent!!), PendingIntent.FLAG_ONE_SHOT)
                    pendingIntent.send()
                } else {
                    context.startActivity(HomeActivity::class) {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }
                SensorsDataAPI.sharedInstance(context).track("app_clicked_notification",
                        JSONObject().put("title", obj.optString("title")))
            } else {
                url = HOME_PAGE
                context.startActivity(HomeActivity::class) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.getSpider().manuallyEvent(SpiderEventNames.PUSH_CLICK)
                    .put("pushID", registerionId ?: "")
                    .put("ssr", url)
                    .put("pushTitle", obj.optString("title"))
                    .track()
        } catch (e: Exception) {
            e.throwOrLog()
            context.startActivity(HomeActivity::class) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }

    private fun log(message: String) {
        Timber.tag("JPush").d(message)
    }
}