package com.shuashuakan.android.modules.activity

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.View
import com.bumptech.glide.Glide
import com.shuashuakan.android.data.api.model.Card
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.utils.TimeUtil
import com.shuashuakan.android.utils.extension.screenHeight
import com.shuashuakan.android.utils.extension.screenWidth
import com.shuashuakan.android.utils.extension.sharedPreferences
import com.shuashuakan.android.utils.getSpider
import com.shuashuakan.android.utils.getUserId
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * 活动卡片的帮助类
 */
class ActivityCardHelper {
    private val activitySP = "activity_sp"
    private val dayOnce = "DAILY_FIRST"
    private val activeFirst = "ACTIVITY_FIRST"


    private var card_list: List<Card>? = null

    fun initActivityCard(context: Context, cards: List<Card>) {
        this.card_list = cards

        val imageWidth = (context.screenWidth() * 0.5f).toInt()
        val imageHeight = (context.screenHeight() * 0.5f).toInt()

        Observable.fromIterable(cards)
                .doOnNext {
                    Glide.with(context)
                            .load(it.image_url)
                            .downloadOnly(imageWidth, imageHeight)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe()
//        card_list?.forEach {
//
//            Glide.with(context)
//                    .load(it.image_url)
//                    .downloadOnly(imageWidth, imageHeight)
//
//        }

    }


    fun showActivityCard(activity: Activity?,parent: View?, position: Int) {
        if (activity == null || activity.isFinishing){
            return
        }
        val cards = card_list
        if (cards == null || cards.isEmpty()) {
            return
        }

        for (item in cards) {
            if (item.position == position+1) {
                val currentTimeMillis = System.currentTimeMillis()
                if (item.expire_at > currentTimeMillis) {
                    if (item.frequency == dayOnce) {
                        val previewSaveTime = activity.sharedPreferences(activitySP).getLong(item.id.toString(), 0)
                        if (!TimeUtil.checkIfSameDay(previewSaveTime, currentTimeMillis)) {

                            val popupWindow = ActivityCardPopupWindow(activity, item)
                            popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0)
                            activity.sharedPreferences(activitySP).edit().putLong(item.id.toString(), currentTimeMillis).apply()

                            //活动卡片曝光
                            activity.getSpider().manuallyEvent(SpiderEventNames.GAME_CARD_EXPOSURE)
                                    .put("userID", activity.getUserId())
                                    .put("ssr", item.redirect_url)
                                    .put("target", "NORMAL")
                                    .track()

                            break
                        }
                    } else if (item.frequency == activeFirst) {
                        val previewSaveTime = activity.sharedPreferences(activitySP).getLong(item.id.toString(), 0)
                        if (previewSaveTime == 0L) {

                            val popupWindow = ActivityCardPopupWindow(activity, item)
                            popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0)
                            activity.sharedPreferences(activitySP).edit().putLong(item.id.toString(), currentTimeMillis).apply()

                            //活动卡片曝光
                            activity.getSpider().manuallyEvent(SpiderEventNames.GAME_CARD_EXPOSURE)
                                    .put("userID", activity.getUserId())
                                    .put("ssr", item.redirect_url)
                                    .put("target", "NORMAL")
                                    .track()

                            break
                        }

                    }

                } else {
                    activity.sharedPreferences(activitySP).edit().remove(item.id.toString()).apply()
                }
            }
        }
    }

}

