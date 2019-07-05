package com.shuashuakan.android.modules.widget.ranktop

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.shuashuakan.android.DuckApplication
import com.shuashuakan.android.R
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.explore.RankListModel
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.LoginSuccessEvent
import com.shuashuakan.android.event.SubscribeEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.ACCOUNT_PAGE
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.discovery.RankingListActivity
import com.shuashuakan.android.modules.profile.UserProfileActivity
import com.shuashuakan.android.modules.widget.FollowButton
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.utils.*
import com.shuashuakan.android.utils.extension.showCancelFollowDialog
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

/**
 * Author:  liJie
 * Date:   2019/1/14
 * Email:  2607401801@qq.com
 */
class RankingTopView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    private val oneAvatar: SimpleDraweeView
    private val twoAvatar: SimpleDraweeView
    private val threeAvatar: SimpleDraweeView

    private val oneFollow: FollowButton
    private val twoFollow: FollowButton
    private val threeFollow: FollowButton

    private val oneName: TextView
    private val twoName: TextView
    private val threeName: TextView

    private val oneUpNum: TextView
    private val twoUpNum: TextView
    private val threeUpNum: TextView

    private val oneFollowNum: TextView
    private val twoFollowNum: TextView
    private val threeFollowNum: TextView

    private lateinit var apiService: ApiService
    private lateinit var spider: Spider
    private lateinit var accountManager: AccountManager
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    init {
        View.inflate(context, R.layout.layout_ranking_top, this)
        oneAvatar = findViewById(R.id.one_avatar)
        twoAvatar = findViewById(R.id.two_avatar)
        threeAvatar = findViewById(R.id.three_avatar)

        oneFollow = findViewById(R.id.one_follow)
        twoFollow = findViewById(R.id.two_follow)
        threeFollow = findViewById(R.id.three_follow)

        oneName = findViewById(R.id.one_name)
        twoName = findViewById(R.id.two_name)
        threeName = findViewById(R.id.three_name)

        oneUpNum = findViewById(R.id.one_up_num)
        twoUpNum = findViewById(R.id.two_up_num)
        threeUpNum = findViewById(R.id.three_up_num)

        oneFollowNum = findViewById(R.id.one_follow_num)
        twoFollowNum = findViewById(R.id.two_follow_num)
        threeFollowNum = findViewById(R.id.three_follow_num)

        invalidate()
        initObservable()
    }

    @SuppressLint("CheckResult")
    private fun initObservable() {
        RxBus.get().toFlowable().subscribe {
            when (it) {
                is LoginSuccessEvent -> {
                    onLoginActionToFollow()
                }
            }
        }.addTo(compositeDisposable)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        compositeDisposable.clear()
    }

    fun setData(list: List<RankListModel>, apiService: ApiService, spider: Spider, accountManager: AccountManager) {
        this.apiService = apiService
        this.spider = spider
        this.accountManager = accountManager
        when {
            list.size == 1 -> setTwoView(list[0])
            list.size == 2 -> {
                setOneView(list[1])
                setTwoView(list[0])
            }
            list.size >= 3 -> {
                setOneView(list[1])
                setTwoView(list[0])
                setThreeView(list[2])
            }
        }
        //并且为关注按钮设置点击事件
        setFollowClick(list)

    }

    private fun setOneView(data: RankListModel) {
        oneAvatar.setImageURI(data.avatar)
        oneName.text = data.nickName
        oneUpNum.text = String.format(context.getString(com.shuashuakan.android.base.ui.R.string.string_up_value_format),
                numFormat(data.upCount))
        oneFollowNum.text = String.format(context.getString(com.shuashuakan.android.base.ui.R.string.string_follow_number_format),
                numFormat(data.fansCount))
        if (data.isFollow) {
            oneFollow.setFollowStatus(true, data.is_fans)
        } else {
            oneFollow.setFollowStatus(false, data.is_fans)
        }
        oneAvatar.noDoubleClick {
            context.startActivity(Intent(context, UserProfileActivity::class.java)
                    .putExtra("id", data.userId.toString())
                    .putExtra("source", SpiderAction.PersonSource.RANKING_LIST.source))
        }
    }

    private fun setTwoView(data: RankListModel) {
        twoAvatar.setImageURI(data.avatar)
        twoName.text = data.nickName
        twoUpNum.text = String.format(context.getString(com.shuashuakan.android.base.ui.R.string.string_up_value_format),
                numFormat(data.upCount))
        twoFollowNum.text = String.format(context.getString(com.shuashuakan.android.base.ui.R.string.string_follow_number_format),
                numFormat(data.fansCount))
        if (data.isFollow) {
            twoFollow.setFollowStatus(true, data.is_fans)
        } else {
            twoFollow.setFollowStatus(false, data.is_fans)
        }
        twoAvatar.noDoubleClick {
            context.startActivity(Intent(context, UserProfileActivity::class.java)
                    .putExtra("id", data.userId.toString())
                    .putExtra("source", SpiderAction.PersonSource.RANKING_LIST.source))
        }
    }

    private fun setThreeView(data: RankListModel) {
        threeAvatar.setImageURI(data.avatar)
        threeName.text = data.nickName
        threeUpNum.text = String.format(context.getString(com.shuashuakan.android.base.ui.R.string.string_up_value_format),
                numFormat(data.upCount))
        threeFollowNum.text = String.format(context.getString(com.shuashuakan.android.base.ui.R.string.string_follow_number_format),
                numFormat(data.fansCount))
        if (data.isFollow) {
            threeFollow.setFollowStatus(true, data.is_fans)
        } else {
            threeFollow.setFollowStatus(false, data.is_fans)
        }
        threeAvatar.noDoubleClick {
            context.startActivity(Intent(context, UserProfileActivity::class.java)
                    .putExtra("id", data.userId.toString())
                    .putExtra("source", SpiderAction.PersonSource.RANKING_LIST.source))
        }
    }

    private fun setFollowClick(list: List<RankListModel>) {
        if (list.size >= 2) {
            oneFollow.noDoubleClick {
                setFollowBtnClick(list[1].isFollow, list[1].userId.toString(), oneFollow, list[1], 1) {}
            }
        }
        if (list.isNotEmpty()) {
            twoFollow.noDoubleClick {
                setFollowBtnClick(list[0].isFollow, list[0].userId.toString(), twoFollow, list[0], 2) {}
            }
        }
        if (list.size >= 3) {
            threeFollow.noDoubleClick {
                setFollowBtnClick(list[2].isFollow, list[2].userId.toString(), threeFollow, list[2], 3) {}
            }
        }
    }

    private fun setFollowBtnClick(follow: Boolean, userId: String, followButton: FollowButton, item: RankListModel, num: Int,
                                  onFollowSuccess: () -> Unit) {
        if (!accountManager.hasAccount()) {
            waitFollow = follow
            waitUserId = userId
            waitFollowButton = followButton
            waitItem = item
            waitNum = num
            context.startActivity(ACCOUNT_PAGE)
            return
        }
        if (follow) {
            val cancelFollow = {
                apiService.cancelFollow(userId).applySchedulers().subscribeApi(onNext = {
                    onFollowSuccess.invoke()
                    if (it.result.isSuccess) {
                        FollowCacheManager.putFollowUserToCache(userId, false)
                        RankingListActivity.clickBtn = true
                        followButton.setFollowStatus(false, item.is_fans)
                        item.isFollow = false
                        item.fansCount = item.fansCount!! - 1
                        setFollowNum(item, num)
                        RxBus.get().post(SubscribeEvent())
                        spider.manuallyEvent(SpiderEventNames.USER_FOLLOW)
                                .put("userID", userId)
                                .put("TargetUserID", userId)
                                .put("method", "unfollow")
                                .track()
                    } else {
                        context.showLongToast(context.getString(R.string.string_un_follow_error))
                    }
                }, onApiError = {
                    onFollowSuccess.invoke()
                    if (it is ApiError.HttpError) {
                        context.showLongToast(it.displayMsg)
                    } else {
                        context.showLongToast(context.getString(R.string.string_un_follow_error))
                    }
                })
            }
            context.showCancelFollowDialog(item.nickName, cancelFollow)
        } else {
            apiService.createFollow(userId).applySchedulers().subscribeApi(onNext = {
                if (it.result.isSuccess) {
                    FollowCacheManager.putFollowUserToCache(userId, true)
                    RankingListActivity.clickBtn = true
                    followButton.setFollowStatus(true, item.is_fans)
                    item.isFollow = true
                    item.fansCount = item.fansCount!! + 1
                    setFollowNum(item, num)
                    RxBus.get().post(SubscribeEvent())
                    spider.manuallyEvent(SpiderEventNames.USER_FOLLOW)
                            .put("userID", userId)
                            .put("TargetUserID", userId)
                            .put("method", "follow")
                            .track()
                } else {
                    context.showLongToast(context.getString(R.string.string_attention_error))
                }
            }, onApiError = {
                if (it is ApiError.HttpError) {
                    context.showLongToast(it.displayMsg)
                } else {
                    context.showLongToast(context.getString(R.string.string_attention_error))
                }
            })
        }
    }

    private fun setFollowNum(item: RankListModel, num: Int) {
        when (num) {
            1 -> {
                oneFollowNum.text = String.format(context.getString(com.shuashuakan.android.base.ui.R.string.string_follow_number_format),
                        numFormat(item.fansCount))
            }
            2 -> {
                twoFollowNum.text = String.format(context.getString(com.shuashuakan.android.base.ui.R.string.string_follow_number_format),
                        numFormat(item.fansCount))
            }
            3 -> {
                threeFollowNum.text = String.format(context.getString(com.shuashuakan.android.base.ui.R.string.string_follow_number_format),
                        numFormat(item.fansCount))
            }
        }
    }

    private var waitFollow: Boolean? = null
    private var waitUserId: String? = null
    private var waitFollowButton: FollowButton? = null
    private var waitItem: RankListModel? = null
    private var waitNum: Int? = null
    private fun onLoginActionToFollow() {
        waitFollow ?: return
        waitUserId ?: return
        waitFollowButton ?: return
        waitItem ?: return
        waitNum ?: return
        setFollowBtnClick(waitFollow!!, waitUserId!!,
                waitFollowButton!!, waitItem!!, waitNum!!) {
            waitFollow = null
            waitUserId = null
            waitFollowButton = null
            waitItem = null
            waitNum = null
        }

    }
}