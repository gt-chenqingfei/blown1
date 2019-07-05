package com.shuashuakan.android.modules.activity

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.ChestInfo
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.utils.extension.load
import com.shuashuakan.android.utils.getSpider

/**
 * 新人宝箱帮助类
 */
class ChestHelper {

    private var mChestInfoResult: Array<ChestInfo>? = null
    private var mContent: View? = null
    private var isAutoShowChest = false
    private var mPopupWindow: ChestPopupWindow? = null
    fun initChestInfoView(context: Context, result: Array<ChestInfo>) {
        val chestInfo = result[0]
        val content = LayoutInflater.from(context).inflate(R.layout.popup_window_chest, null, false)
        this.mContent = content

        chestInfo.chest_award_list?.forEachIndexed { index, chestAwardList ->
            when (index) {
                0 -> {
                    val gift1 = content.findViewById<ImageView>(R.id.ic_gift_1)
                    gift1.load(chestAwardList.image, 120, 120)
                    val giftDetail1 = content.findViewById<TextView>(R.id.tv_gift_detail_1)
                    giftDetail1.text = chestAwardList.title


                }
                1 -> {
                    val gift2 = content.findViewById<ImageView>(R.id.ic_gift_2)
                    gift2.load(chestAwardList.image, 120, 120)
                    val giftDetail2 = content.findViewById<TextView>(R.id.tv_gift_detail_2)
                    giftDetail2.text = chestAwardList.title

                }
                2 -> {
                    val gift3 = content.findViewById<ImageView>(R.id.ic_gift_3)
                    gift3.load(chestAwardList.image, 120, 120)
                    val giftDetail3 = content.findViewById<TextView>(R.id.tv_gift_detail_3)
                    giftDetail3.text = chestAwardList.title

                }
                3 -> {
                    val gift4 = content.findViewById<ImageView>(R.id.ic_gift_4)
                    gift4.load(chestAwardList.image, 120, 120)
                    val giftDetail4 = content.findViewById<TextView>(R.id.tv_gift_detail_4)
                    giftDetail4.text = chestAwardList.title

                }
            }
        }

        this.mChestInfoResult = result
    }


    fun initOpenChestView(context: Context, result: Array<ChestInfo>) {
        val openChestDetail = result[1]
        val chestAward = openChestDetail.chest_award_list?.getOrNull(0) ?: return

        val content = LayoutInflater.from(context).inflate(R.layout.popup_window_open_chest, null, false)
        this.mContent = content
        this.mChestInfoResult = result

        val iv_gift = content.findViewById<ImageView>(R.id.iv_gift)
        iv_gift.load(chestAward.image, 120, 120)
    }

    fun dismissChestPop() {
        mPopupWindow?.dismiss()
    }

    fun showChestPopupWindow(accountManager: AccountManager, activity: Activity?, parent: View?, position: Int, acceptGiftListener: ChestPopupWindow.AcceptGiftListener) {
        if (isAutoShowChest || parent == null || activity == null || activity.isFinishing) {
            return
        }
        val chestResult = mChestInfoResult
        val contentView = mContent
        if (contentView != null && chestResult != null) {

            val chestInfo = chestResult[0]
            if (position == chestInfo.position) {
                mPopupWindow = ChestPopupWindow(accountManager, activity, contentView, chestResult, false).also {
                    it.mAcceptGiftListener = acceptGiftListener
                    it.showAtLocation(parent, Gravity.CENTER, 0, 0)
                }
                isAutoShowChest = true
            }

            //新人宝箱曝光
            logWithChestExposure(accountManager, activity,false)
        }
    }



    fun showOpenChestPopupWindow(accountManager: AccountManager, activity: Activity?, parent: View?, acceptGiftListener: ChestPopupWindow.AcceptGiftListener) {
        if (parent == null || activity == null || activity.isFinishing) {
            return
        }
        val contentView = LayoutInflater.from(activity).inflate(R.layout.popup_window_open_chest, null, false)
        val chestResult = mChestInfoResult
        if (chestResult != null) {
            val popupWindow = ChestPopupWindow(accountManager, activity, contentView, chestResult, true)
            popupWindow.mAcceptGiftListener = acceptGiftListener
            popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0)
            //开启新人宝箱.
            logWithChestOpen(accountManager, activity)
        }
    }

    fun showResultCallOpen(accountManager: AccountManager, activity: Activity?, parent: View?, acceptGiftListener: ChestPopupWindow.AcceptGiftListener) {
        if (parent == null || activity == null || activity.isFinishing) {
            return
        }

        // 这块是表示弹窗已经关闭了 然后需要重新show new pop
        mPopupWindow?.let {
            if (!mPopupWindow!!.isShowing) {
                showOpenChestPopupWindow(accountManager, activity, parent, acceptGiftListener)
                return
            }
        }

        val chestResult = mChestInfoResult
        if (chestResult != null) {
            mContent?.let {
                mPopupWindow?.showRotateEvent(mContent!!, chestResult)
            }
            //开启新人宝箱.
            logWithChestOpen(accountManager, activity)
        }
    }

     fun logWithChestExposure(accountManager: AccountManager, activity: Activity, isFloating:Boolean) {
        val target = if(isFloating)"FLOATING" else "NORMAL"
        if (accountManager.hasAccount()) {
            val userId = accountManager.account()?.userId ?: ""
            activity.getSpider().manuallyEvent(SpiderEventNames.ACTIVE_CHEST_EXPOSURE)
                    .put("userID", userId)
                    .put("ssr", "")
                    .put("target", target)
                    .track()

        } else {
            activity.getSpider().manuallyEvent(SpiderEventNames.ACTIVE_CHEST_EXPOSURE)
                    .put("ssr", "")
                    .put("target", target)
                    .track()
        }
    }

    private fun logWithChestOpen(accountManager: AccountManager, activity: Activity) {
        activity.getSpider().manuallyEvent(SpiderEventNames.ACTIVE_CHEST_OPEN_CLICK)
                .track()
    }


}
