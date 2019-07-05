package com.shuashuakan.android.modules.activity

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.view.animation.*
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import com.shuashuakan.android.R
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.ChestInfo
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.event.PopWindowEvent
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.modules.ACCOUNT_PAGE
import com.shuashuakan.android.utils.extension.load
import com.shuashuakan.android.utils.extension.screenHeight
import com.shuashuakan.android.utils.extension.screenWidth
import com.shuashuakan.android.utils.getSpider
import com.shuashuakan.android.utils.setSafeClickListener
import com.shuashuakan.android.utils.startActivity

/**
 * 新人宝箱弹窗
 */
class ChestPopupWindow : PopupWindow, PopupWindow.OnDismissListener {


    private lateinit var mActivity: Activity
    private lateinit var mAccountManager: AccountManager

    constructor(accountManager: AccountManager, activity: Activity?, content: View, result: Array<ChestInfo>, showOpenDirect: Boolean) : super(content) {
        if (activity == null || activity.isFinishing) {
            dismiss()
            return
        }
        width = (activity.screenWidth() * 0.87f).toInt()
        height = (activity.screenHeight() * 0.82f).toInt()
        isFocusable = true
        mActivity = activity
        mAccountManager = accountManager

        setOnDismissListener(this)

        if (showOpenDirect) {
            initOpenChestView(content, result)
        } else {
            initChestInfoView(accountManager, content, result)
            animationStyle = R.style.ChestPopWindowStyle
        }
    }

    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        super.showAtLocation(parent, gravity, x, y)
        val window = mActivity.window
        window?.let {
            val attributes = it.attributes
            attributes?.alpha = 0.45f
            it.attributes = attributes
        }
        RxBus.get().post(PopWindowEvent())

    }

    override fun onDismiss() {

        val window = mActivity.window
        window?.let {
            val attributes = it.attributes
            attributes?.alpha = 1f
            it.attributes = attributes
        }
        RxBus.get().post(PopWindowEvent(false, isneedShowChestFloat = true))
    }

    private fun initChestInfoView(accountManager: AccountManager, content: View, result: Array<ChestInfo>) {
        val closeChest = content.findViewById<ImageView>(R.id.closeChest)
        val openChest = content.findViewById<ImageView>(R.id.openChest)


        closeChest.setOnClickListener {
            dismiss()
        }

        contentView.postOnAnimation {
            val openChest = contentView.findViewById<ImageView>(R.id.openChest)
            val animation = ScaleAnimation(0.9f, 1.2f, 0.9f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            animation.repeatMode = Animation.REVERSE
            animation.repeatCount = Animation.INFINITE
            animation.duration = 600
            animation.interpolator = LinearInterpolator()
            openChest.startAnimation(animation)
        }


        openChest.setSafeClickListener {
            if (accountManager.hasAccount()) {
                showRotateEvent(content, result)
            } else {
                mActivity.startActivity(ACCOUNT_PAGE)
            }


            //开启新人宝箱.
            if (accountManager.hasAccount()) {
                val userId = accountManager.account()?.userId ?: ""
                mActivity.getSpider().manuallyEvent(SpiderEventNames.ACTIVE_CHEST_OPEN_CLICK)
                        .put("userID", userId)
                        .put("ssr", "")
                        .put("target", "NORMAL")
                        .track()

            } else {
                mActivity.getSpider().manuallyEvent(SpiderEventNames.ACTIVE_CHEST_OPEN_CLICK)
                        .put("ssr", "")
                        .put("target", "NORMAL")
                        .track()
            }
        }

    }

    fun showRotateEvent(content: View, result: Array<ChestInfo>) {
        val imageView = content.findViewById<ImageView>(R.id.imageView)
        imageView.visibility = View.GONE
        val textView4 = content.findViewById<TextView>(R.id.textView4)
        textView4.visibility = View.GONE

        val constraintLayout = content.findViewById<View>(R.id.constraintLayout)
        constraintLayout.visibility = View.GONE

        val chest = content.findViewById<ImageView>(R.id.chest)
        val rotateAnimation = RotateAnimation(-3f, 3f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotateAnimation.duration = 40
        rotateAnimation.repeatMode = Animation.REVERSE
        rotateAnimation.repeatCount = 15
        rotateAnimation.interpolator = LinearInterpolator()
        chest.animation = rotateAnimation

        rotateAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                val container_chest_info = content.findViewById<View>(R.id.container_chest_info)
                container_chest_info.visibility = View.GONE


                val openChestDetail = result[1]
                val chestAward = openChestDetail.chest_award_list?.getOrNull(0)
                if (chestAward == null) {
                    dismiss()
                    return
                }

                val vs_open_chest = content.findViewById<ViewStub>(R.id.vs_open_chest)
                vs_open_chest.inflate()


                val openChestInfo = content.findViewById<ViewGroup>(R.id.openChestInfo)
                val iv_light = openChestInfo.findViewById<ImageView>(R.id.iv_light)
                val iv_gift = openChestInfo.findViewById<ImageView>(R.id.iv_gift)

                val iv_close = openChestInfo.findViewById<ImageView>(R.id.iv_close)
                val tv_accept_gift = openChestInfo.findViewById<TextView>(R.id.tv_accept_gift)

                val tv_see_detail = openChestInfo.findViewById<TextView>(R.id.tv_see_detail)
                val tv_gift_desc = openChestInfo.findViewById<TextView>(R.id.tv_gift_desc)


                val lightAnimation = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                lightAnimation.repeatMode = Animation.RESTART
                lightAnimation.duration = 3000
                lightAnimation.repeatCount = Animation.INFINITE
                lightAnimation.interpolator = LinearInterpolator()
                iv_light.startAnimation(lightAnimation)

                // 礼物的动画
                giftScaleAnim(iv_gift)
                // 底部的位移动画
                bottomTranslateAnim(iv_close, tv_accept_gift)

                iv_gift.load(chestAward.image, 120, 120)

                tv_gift_desc.text = openChestDetail.title
                tv_see_detail.text = openChestDetail.content


                tv_accept_gift.setSafeClickListener {
                    it.context.getSpider().manuallyEvent(SpiderEventNames.ACTIVE_CHEST_GET_CLICK).track()
                    mAcceptGiftListener?.onAcceptGift(it, this@ChestPopupWindow)
                }
                iv_close.setSafeClickListener {
                    dismiss()
                }
            }

            override fun onAnimationStart(animation: Animation?) {
            }

        })
    }

    private fun bottomTranslateAnim(iv_close: ImageView, tv_accept_gift: TextView) {
        val bottomTranslate = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 3f, Animation.RELATIVE_TO_SELF, 0f)
        bottomTranslate.duration = 150
        bottomTranslate.interpolator = LinearInterpolator()
        iv_close.startAnimation(bottomTranslate)
        tv_accept_gift.startAnimation(bottomTranslate)
    }

    private fun giftScaleAnim(iv_gift: ImageView) {
        val centerGiftAnim = ScaleAnimation(0f, 1f, 0f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        centerGiftAnim.duration = 200
        centerGiftAnim.interpolator = LinearInterpolator()
        iv_gift.startAnimation(centerGiftAnim)
    }

    private fun initOpenChestView(content: View, result: Array<ChestInfo>) {
        val openChestDetail = result[1]

        val chestAward = openChestDetail.chest_award_list?.getOrNull(0)
        if (chestAward == null) {
            dismiss()
            return
        }

        val iv_light = content.findViewById<ImageView>(R.id.iv_light)
        val iv_gift = content.findViewById<ImageView>(R.id.iv_gift)

        val iv_close = content.findViewById<ImageView>(R.id.iv_close)
        val tv_accept_gift = content.findViewById<TextView>(R.id.tv_accept_gift)
        val tv_see_detail = content.findViewById<TextView>(R.id.tv_see_detail)
        val tv_gift_desc = content.findViewById<TextView>(R.id.tv_gift_desc)

        iv_gift.load(chestAward.image, 120, 120)

        val lightAnimation = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        lightAnimation.repeatMode = Animation.RESTART
        lightAnimation.duration = 3000
        lightAnimation.repeatCount = Animation.INFINITE
        lightAnimation.interpolator = LinearInterpolator()
        iv_light.startAnimation(lightAnimation)

        // 礼物的动画
        giftScaleAnim(iv_gift)
        // 底部的位移动画
        bottomTranslateAnim(iv_close, tv_accept_gift)


        tv_gift_desc.text = openChestDetail.title
        tv_see_detail.text = openChestDetail.content


        tv_accept_gift.setSafeClickListener {
            it.context.getSpider().manuallyEvent(SpiderEventNames.ACTIVE_CHEST_GET_CLICK).track()
            mAcceptGiftListener?.onAcceptGift(it, this@ChestPopupWindow)
        }
        iv_close.setSafeClickListener {
            dismiss()
        }
    }

    var mAcceptGiftListener: AcceptGiftListener? = null

    interface AcceptGiftListener {
        fun onAcceptGift(v: View, popupWindow: ChestPopupWindow)
    }


}