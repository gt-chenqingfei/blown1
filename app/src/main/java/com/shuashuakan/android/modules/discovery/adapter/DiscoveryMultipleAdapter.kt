package com.shuashuakan.android.modules.discovery.adapter

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.facebook.drawee.view.SimpleDraweeView
import com.luck.picture.lib.tools.ScreenUtils
import com.shuashuakan.android.R
import com.shuashuakan.android.constant.Constants
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.explore.*
import com.shuashuakan.android.data.api.model.home.Author
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.RefreshProfileEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.ACCOUNT_PAGE
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.modules.discovery.ItemDataPair
import com.shuashuakan.android.modules.discovery.RankingListActivity
import com.shuashuakan.android.modules.discovery.view.HorizontalLinePageIndicator
import com.shuashuakan.android.modules.discovery.view.PageGridRecyclerView
import com.shuashuakan.android.modules.profile.UserProfileActivity
import com.shuashuakan.android.modules.topic.TopicCategoryActivity
import com.shuashuakan.android.modules.widget.FollowTextView
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.ui.banner.BannerView
import com.shuashuakan.android.utils.*
import com.shuashuakan.android.utils.extension.showCancelFollowDialog


/**
 * Author:  lijie
 * Date:   2018/11/30
 * Email:  2607401801@qq.com
 */

class DiscoveryMultipleAdapter constructor(
        private val context: Context,
        dataList: List<MultiItemEntity>?,
        val accountManager: AccountManager,
        val spider: Spider,
        val apiService: ApiService
) : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(dataList) {


    private val rankIconArray = intArrayOf(R.drawable.explore_ic_gold, R.drawable.explore_ic_silver, R.drawable.explore_ic_bronze)
    private lateinit var rankList: ArrayList<Feed>

    var listener: OnAdapterPlayerViewClickListener? = null

    interface OnAdapterPlayerViewClickListener {
        fun onAdapterPlayerViewClickListener(position: Int)
    }

    companion object {
        const val BANNER = 0  //banner
        const val CHAIN_USER_LIST = 1 //接龙达人榜
        const val HOT_CHANNEL = 2 //精彩话题
        // const val CHAIN_RANKING = 3  //接龙排行榜
        const val CHAIN_RAKING_TITLE = 3
        const val CHAIN_RANKING_ITEM = 4
        const val CATEGORY_ENTRANCE = 5
        const val CATEGORY_PAGE_COUNT = 10

        var array: ArrayList<String> = ArrayList()
        fun put(id: String, view: View) {
            if (array.contains(id)) {
                return
            }
            if (!array.isEmpty()) {
                array.clear()
            }
            array.add(id)
            view.context.getSpider().exploreFeedLeaderBoardExposureEvent(id)
        }
    }

    init {
        addItemType(BANNER, R.layout.item_multi_banner_explore)
        addItemType(CHAIN_USER_LIST, R.layout.item_muliti_explore_chain_user)
        addItemType(HOT_CHANNEL, R.layout.item_muliti_explore_hot_channel)
        addItemType(CHAIN_RAKING_TITLE, R.layout.layout_explore_chian_rank_title)
        addItemType(CHAIN_RANKING_ITEM, R.layout.item_explore_rank)
        addItemType(CATEGORY_ENTRANCE, R.layout.item_explore_category)
    }

    override fun convert(helper: BaseViewHolder, item: MultiItemEntity) {
        val itemData = item as com.shuashuakan.android.modules.discovery.ItemDataPair
        when (helper.itemViewType) {
            BANNER -> {
                setBannerMethod(helper, itemData)
            }
            CHAIN_USER_LIST -> {
                setChainUserMethod(helper, itemData)
            }
            HOT_CHANNEL -> {
                setHotChannelMethod(helper, itemData)
            }
            CHAIN_RAKING_TITLE -> {
                setChainRankTitle(helper, itemData)
            }
            CHAIN_RANKING_ITEM -> {
                setChainItem(helper, itemData)
            }
            CATEGORY_ENTRANCE -> {
                setCategoryMethod(helper, itemData)
            }
        }
    }

    private fun setChainRankTitle(helper: BaseViewHolder, itemData: com.shuashuakan.android.modules.discovery.ItemDataPair) {
        val model = itemData.data as ExploreRankingModel
        val rankTitle = helper.getView<TextView>(R.id.rank_title)
        val rankDesc = helper.getView<TextView>(R.id.rank_desc)
        rankDesc.visibility = if (model.desc.isNullOrEmpty()) View.GONE else View.VISIBLE
        rankTitle.text = model.title
        rankDesc.text = model.desc
    }

    private fun setChainItem(helper: BaseViewHolder, itemData: com.shuashuakan.android.modules.discovery.ItemDataPair) {
        val item = itemData.data as Feed

        put(item.id, helper.itemView)

        val upLayout = helper.getView<LinearLayout>(R.id.up_layout)
        val rankNum = helper.getView<TextView>(R.id.rank_num)
        val rankNumTop = helper.getView<ImageView>(R.id.rank_num_top)
        val itemUserIdentificationView = helper.getView<SimpleDraweeView>(R.id.mItemPartitionHotUserIdentificationView)
        val rankUpNum = helper.getView<TextView>(R.id.rank_up_num)
        val followText = helper.getView<FollowTextView>(R.id.hot_feed_follow)
        val lottieView = helper.getView<LottieAnimationView>(R.id.up_view)
        val cover = helper.getView<SimpleDraweeView>(R.id.explore_rank_image)
        val headImage = helper.getView<SimpleDraweeView>(R.id.explore_rank_head_image)
        val channelName = helper.getView<TextView>(R.id.rank_channel_name)



        cover.setImageUrl2Webp(item.cover!!, context.dip(140), context.dip(84))
        headImage.setImageUrl2Webp(item.avatar!!, context.dip(21), context.dip(21))
        helper.setText(R.id.ranking_title, item.title)
                .setText(R.id.rank_name, item.userName)
                .setText(R.id.rank_rules, userShownLabel(item.author))

        item.solitaireNum?.let {
            helper.setText(R.id.mItemPartitionHotSizeView, it.toString())
        }
        helper.setGone(R.id.mItemPartitionHotVideoImageView, item.solitaireNum != 0)
        helper.setGone(R.id.mItemPartitionHotSizeView, item.solitaireNum != 0)

        item.author?.tags?.let {
            itemUserIdentificationView.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
            if (!it.isEmpty()) {
                itemUserIdentificationView.setImageURI(it[0].icon)
            }
        }

//        rankNum.text = "TOP${(item.properties?.ranking)}"
//        if (item.properties?.ranking != null && item.properties?.ranking!! <= 3) {
//            when (item.properties?.ranking) {
//                1 -> {
//                    rankNum.setTextColor(getColor1(R.color.color_normal_ffa500, context))
//                    rankNumTop.setImageResource(R.drawable.bg_top1_explore)
//                }
//                2 -> {
//                    rankNum.setTextColor(getColor1(R.color.color_normal_9aaed4, context))
//                    rankNumTop.setImageResource(R.drawable.bg_top2_explore)
//                }
//                3 -> {
//                    rankNum.setTextColor(getColor1(R.color.color_normal_d18a11, context))
//                    rankNumTop.setImageResource(R.drawable.bg_top3_explore)
//                }
//            }
//        } else {
//            rankNumTop.setImageResource(R.drawable.bg_top4_30_explore)
//            rankNum.setTextColor(getColor1(R.color.enjoy_color_2, context))
//        }

        if (item.author?.is_fans == true) {
            followText.setText(R.string.string_follow_fans)
        } else {
            followText.setText(R.string.string_follow)
        }

        helper.setGone(R.id.hot_feed_follow, !item.author?.isFollow!!)
        helper.setGone(R.id.mFollowedLayout, item.author?.isFollow!!)


        helper.itemView.noDoubleClick {
            spider.manuallyEvent(SpiderEventNames.SOLITAIRE_LEADER_BOARD_FEED_CLICK)
                    .put("feedID", item.id)
                    .put("userID", mContext.getUserId())
                    .track()

            listener?.onAdapterPlayerViewClickListener(helper.adapterPosition)

//      context.startActivity(item.redirectUrl)

//      //接龙排行榜点击跳转视频
//      if (item.properties?.ranking != null&&rankList.isNotEmpty())
//        context.startActivity(AllChainsActivity.create(context, ChainsListIntentParam.recommendOpen(rankList,
//            item.properties?.ranking!!-1, ChainFeedSource.EXPLORE_RANKING_LIST)))

        }
        channelName.noDoubleClick { context.startActivity(item.channelUrl) }
        helper.getView<TextView>(R.id.rank_name).noDoubleClick {
            context.startActivity(Intent(context, UserProfileActivity::class.java)
                    .putExtra("id", item.userId.toString())
                    .putExtra("source", SpiderAction.PersonSource.RANKING_LIST.source))
        }
        if (item.fav) {
            lottieView.setAnimation("up_second_action.json")
        } else {
            lottieView.setAnimation("up_static.json")
        }
        lottieView.playAnimation()

        upLayout.setOnClickListener {
            favMethod(item, lottieView, rankUpNum)
        }

        followText.setOnClickListener {
            followUser(item, followText) {}
        }
    }

    private fun setBannerMethod(helper: BaseViewHolder, item: ItemDataPair) {
        val bannerModel = item.data as ExploreBannerModel
        val bannerList = helper.getView<BannerView<ExploreBannerItemModel>>(R.id.banner_list)
        val list = bannerModel.dataList
        bannerList.setViewFactory(BannerViewFactory())
        bannerList.setDataList(list)
        bannerList.start()
    }

    private fun setCategoryMethod(helper: BaseViewHolder, item: ItemDataPair) {
        val categoryModel = item.data as ExploreCategoryModel
        var recyclerView = helper.getView<PageGridRecyclerView>(R.id.mCategoryRecyclerView)
        var indicator = helper.getView<HorizontalLinePageIndicator>(R.id.mCategoryPageIndicator)
        val adapter = object : BaseQuickAdapter<ExploreCategoryLinkModel, BaseViewHolder>(R.layout.item_explore_category_list) {
            @SuppressLint("CheckResult")
            override fun convert(helper: BaseViewHolder, item: ExploreCategoryLinkModel) {
                helper.itemView.layoutParams.width = (context.resources.displayMetrics.widthPixels - context.dip(32)) / 5
                val categoryIconView = helper.getView<SimpleDraweeView>(R.id.mItemCategoryImageView)
                categoryIconView.setImageURI(item.imageUrl)
                helper.setText(R.id.mItemCategoryNameView, item.name)
                helper.itemView.setOnClickListener {
                    context.startActivity(item.redirectUrl)
                    context.getSpider().categoryDidSelectedEvent(item.id!!)
                }
            }
        }
        helper.setGone(R.id.mCategoryPageIndicator, categoryModel.dataList.size > CATEGORY_PAGE_COUNT)
        recyclerView.adapter = adapter
        adapter.setNewData(categoryModel.dataList)
        recyclerView.setPageIndicator(indicator)
        indicator.resetPosition()
    }

    private fun setChainUserMethod(helper: BaseViewHolder, item: ItemDataPair) {
        val userModel = item.data as ExploreUserModel
        val chainUserRec = helper.getView<com.shuashuakan.android.modules.widget.BetterRecyclerView>(R.id.recommend_rec)
        val rankListParent = helper.getView<LinearLayout>(R.id.rank_list_parent)
        val title = helper.getView<TextView>(R.id.title)
        rankListParent.setOnClickListener { context.startActivity(Intent(context, RankingListActivity::class.java)) }
        val list = userModel.dataList
        title.text = userModel.title
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.HORIZONTAL
        chainUserRec.layoutManager = linearLayoutManager
        val adapter = object : BaseQuickAdapter<ExploreChainUserItemModel, BaseViewHolder>(R.layout.item_sub_explore_chain_user) {
            @SuppressLint("CheckResult")
            override fun convert(helper: BaseViewHolder, item: ExploreChainUserItemModel) {
                val avatar = helper.getView<SimpleDraweeView>(R.id.chain_user_avatar)
                val rankIcon = helper.getView<ImageView>(R.id.chain_user_rank_icon)
                avatar.setImageUrl2Webp(item.avatar ?: "", context.dip(54), context.dip(54))
                helper.setText(R.id.chain_user_name, item.nickName)
                        .setText(R.id.chain_user_up_num,
                                String.format(context.getString(com.shuashuakan.android.base.ui.R.string.string_up_value_format),
                                        numFormat(item.upCount)))

                val follow = helper.getView<FollowTextView>(R.id.chain_user_follow)
                if (item.isFollow) {
                    follow.visibility = View.INVISIBLE
                } else {
                    follow.reset()
                    follow.visibility = View.VISIBLE
                }
                if (helper.adapterPosition < 3) {
                    rankIcon.visibility = View.VISIBLE
                    rankIcon.setImageResource(rankIconArray[helper.adapterPosition])
                } else {
                    rankIcon.visibility = View.GONE
                }

                val itemWidth: Double = (mContext.getScreenSize().x - mContext.dip(36f)) / 3.4

                helper.itemView.layoutParams.width = itemWidth.toInt()
                avatar.noDoubleClick {
                    context.startActivity(Intent(context, UserProfileActivity::class.java)
                            .putExtra("id", item.userId.toString())
                            .putExtra("source", SpiderAction.PersonSource.RANKING_LIST.source))
                }
                follow.noDoubleClick {
                    if (!accountManager.hasAccount()) {
                        waitUserId = item.userId.toString()
                        waitFollow = follow
                        waitItem = item
                        LoginActivity.launch(mContext)
                        return@noDoubleClick
                    }
                    setFollowClick(item.isFollow, item.userId.toString(), follow, item) {}
                }
            }
        }
        chainUserRec.adapter = adapter
        adapter.addData(list)

    }

    private fun setFollowClick(isFollow: Boolean, userId: String,
                               follow: FollowTextView,
                               item: ExploreChainUserItemModel, followSuccess: () -> Unit) {
        if (isFollow) {
            val cancelFollow = {
                apiService.cancelFollow(userId).applySchedulers().subscribeApi(onNext = {
                    followSuccess.invoke()
                    if (it.result.isSuccess) {
                        FollowCacheManager.putFollowUserToCache(userId, false)
                        follow.reset()
                        follow.visibility = View.VISIBLE
                        item.isFollow = false
                        RxBus.get().post(RefreshProfileEvent())
                        spider.userFollowEvent(mContext, userId, SpiderAction.VideoPlaySource.EXPLORE_PAGE.source, false)
                        //1550822478310002
                    } else {
                        context.showLongToast(context.getString(R.string.string_un_follow_error))
                    }
                }, onApiError = {
                    followSuccess.invoke()
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
                followSuccess.invoke()
                if (it.result.isSuccess) {
                    FollowCacheManager.putFollowUserToCache(userId, true)
                    follow.followSuccessInInvisible()
                    item.isFollow = true
                    RxBus.get().post(RefreshProfileEvent())
                    spider.userFollowEvent(mContext, userId, SpiderAction.VideoPlaySource.EXPLORE_PAGE.source, true)
                } else {
                    context.showLongToast(context.getString(R.string.string_follow_error))
                }
            }, onApiError = {
                followSuccess.invoke()
                if (it is ApiError.HttpError) {
                    context.showLongToast(it.displayMsg)
                } else {
                    context.showLongToast(context.getString(R.string.string_follow_error))
                }
            })
        }
    }

    private fun setHotChannelMethod(helper: BaseViewHolder, itemData: ItemDataPair) {
        val model = itemData.data as ExploreChannelModel
        val hotChannelRec = helper.getView<com.shuashuakan.android.modules.widget.BetterRecyclerView>(R.id.hot_channel_rec)
        val title = helper.getView<TextView>(R.id.title)
        val allChannel = helper.getView<TextView>(R.id.all_channel)
        allChannel.setOnClickListener {
            TopicCategoryActivity.launch(context, TopicCategoryActivity.showTypeChannel)
        }
        val list = model.dataList
        title.text = model.title
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = RecyclerView.HORIZONTAL
        hotChannelRec.layoutManager = linearLayoutManager

        val adapter = object : BaseQuickAdapter<ExploreChannelItemModel, BaseViewHolder>(R.layout.item_sub_explore_hot_channel) {
            @SuppressLint("CheckResult")
            override fun convert(helper: BaseViewHolder, item: ExploreChannelItemModel) {

                val itemWidth: Int = (mContext.getScreenSize().x - mContext.dip(36f)) / 3
                val itemHeight: Int = itemWidth / 3 * 4
                helper.itemView.layoutParams.height = itemHeight
                helper.itemView.layoutParams.width = itemWidth

                val backImage = helper.getView<SimpleDraweeView>(R.id.hot_channel_image_view)
                val name = helper.getView<TextView>(R.id.hot_channel_name)
                val subscribeBtn = helper.getView<TextView>(R.id.hot_channel_subscribe)
                val channelNum = helper.getView<TextView>(R.id.hot_channel_num)
                name.text = item.name
                backImage.setImageUrl2Webp(item.coverUrl ?: "", itemWidth, itemHeight)
                helper.setText(R.id.hot_channel_num, if (item.subscribedCount == 0) "" else
                    String.format(context.getString(R.string.string_video_subscription_people_format), numFormat(item.subscribedCount)))

                if (item.hasSubscribe) {
                    subscribeBtn.text = context.getString(R.string.string_has_subscription)
                    subscribeBtn.setBackgroundResource(R.drawable.bg_explore_followed)
                    subscribeBtn.setTextColor(subscribeBtn.context.getColor1(R.color.send_line_color))

                    subscribeBtn.visibility = View.INVISIBLE
                } else {
                    subscribeBtn.text = context.getString(R.string.string_subscription)
                    subscribeBtn.setBackgroundResource(R.drawable.bg_explore_un_follow)
                    subscribeBtn.setTextColor(subscribeBtn.context.getColor1(R.color.white))
                }
                subscribeBtn.noDoubleClick {
                    if (!accountManager.hasAccount()) {
                        waitSubscribeBtn = subscribeBtn
                        waitChannelId = item.id
                        waitItemChannel = item
                        waitChannelNum = channelNum
                        context.startActivity(ACCOUNT_PAGE)
                        return@noDoubleClick
                    }
                    setSubscribeClick(item.hasSubscribe, subscribeBtn, item.id, item, channelNum) {}
                }
                helper.itemView.noDoubleClick {
                    setSpiderChannelClick(item.id.toString())
                    context.startActivity(item.redirectUrl)
                }
            }
        }
        hotChannelRec.adapter = adapter
        adapter.addData(list)

    }

    fun setSubscribeClick(hasSubscribe: Boolean, subscribeBtn: TextView, channelId: Long,
                          item: ExploreChannelItemModel, channelNum: TextView, subscribeSuccess: () -> Unit) {
        if (hasSubscribe) {
            apiService.cancelSubscribe(channelId).applySchedulers().subscribeApi(onNext = {
                subscribeSuccess.invoke()
                if (it.result.isSuccess) {
                    subscribeBtn.setBackgroundResource(R.drawable.bg_explore_un_follow)
                    subscribeBtn.text = context.getString(R.string.string_subscription)
                    channelNum.text = String.format(context.getString(R.string.string_video_subscription_people_format),
                            numFormat(item.subscribedCount!! - 1).toString())

                    item.subscribedCount = item.subscribedCount!! - 1
                    item.hasSubscribe = false
                } else {
                    context.showLongToast(context.getString(R.string.string_un_subscription_error))
                }
            }, onApiError = {
                subscribeSuccess.invoke()
                if (it is ApiError.HttpError) {
                    context.showLongToast(it.displayMsg)
                } else {
                    context.showLongToast(context.getString(R.string.string_un_subscription_error))
                }
            })
        } else {
            apiService.subscribeMethod(channelId).applySchedulers().subscribeApi(onNext = {
                subscribeSuccess.invoke()
                if (it.result.isSuccess) {
                    context.getSpider().subscribeChinnalClickEvent(mContext, channelId.toString(),
                            SpiderAction.VideoPlaySource.EXPLORE_PAGE.source)
                    subscribeBtn.setBackgroundResource(R.drawable.bg_explore_followed)
                    subscribeBtn.text = context.getString(R.string.string_has_subscription)
                    channelNum.text = String.format(context.getString(R.string.string_video_subscription_people_format),
                            numFormat(item.subscribedCount!! + 1).toString())
                    item.subscribedCount = item.subscribedCount!! + 1
                    item.hasSubscribe = true
                    subscribeBtn.visibility = View.INVISIBLE
                } else {
                    context.showLongToast(context.getString(R.string.string_subscription_error))
                }
            }, onApiError = {
                subscribeSuccess.invoke()
                if (it is ApiError.HttpError) {
                    context.showLongToast(it.displayMsg)
                } else {
                    context.showLongToast(context.getString(R.string.string_subscription_error))
                }
            })
        }
    }

    private fun favMethod(feedData: Feed, lottieView: LottieAnimationView, rankUpNum: TextView) {

        if (accountManager.hasAccount()) {
            if (!feedData.fav) {
                upMethod(feedData, rankUpNum)
                setUpAnimation(lottieView)
            }
        } else {
            context.startActivityForResultByLink(ACCOUNT_PAGE, Constants.REQUEST_LOGIN_CODE)
        }
    }

    private fun upMethod(item: Feed, rankUpNum: TextView) {
        apiService.likeFeed(item.id)
                .applySchedulers()
                .subscribeApi(onNext = {
                    if (it.result.isSuccess) {
                        item.fav = true
                        item.favNum = item.favNum + 1
                        rankUpNum.text = numFormat(item.favNum)
                        mContext.getSpider().likeEvent(mContext, item.id, "like",
                                SpiderAction.VideoPlaySource.EXPLORE_PAGE.source)
                    } else {
                        item.fav = false
                        rankUpNum.text = numFormat(item.favNum)
                    }
                }, onApiError = {
                    item.fav = false
                    rankUpNum.text = numFormat(item.favNum)
                })
    }

    private fun setUpAnimation(upView: LottieAnimationView?) {
        if (upView != null) {
            upView.cancelAnimation()
            upView.setAnimation("up_action.json")
            upView.loop(false)
            upView.playAnimation()
            upView.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    upView.setAnimation("up_second_action.json")
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }

            })
        }
    }

    private fun setSpiderChannelClick(id: String) {
        spider.manuallyEvent(SpiderEventNames.EXPLORE_CLICK_CHANNEL_PAGE)
                .put("userID", accountManager.account()?.userId ?: "")
                .put("channelID", id)
                .track()
    }

    fun setRankingList(list: ArrayList<Feed>) {
        rankList = list
        notifyDataSetChanged()
    }


    internal inner class BannerViewFactory : BannerView.ViewFactory<ExploreBannerItemModel> {
        override fun create(item: ExploreBannerItemModel, position: Int, container: ViewGroup): View {
            var inflate = LayoutInflater.from(container.context!!).inflate(R.layout.item_channel_banner, null)
            var pictureImageView = inflate.findViewById<SimpleDraweeView>(R.id.banner_image)
            if (item.image != null) {
                pictureImageView.setImageUrl2Webp(item.bannerImage!!,
                        ScreenUtils.getScreenWidth(context) - ScreenUtils.dip2px(context, 30f), context.dip(140f))
            }
            pictureImageView.setOnClickListener {
                context.getSpider().manuallyEvent(SpiderEventNames.CHANNEL_BANNER)
                        .put("ssr", item.redirectUrl ?: "")
                        .put("userID", context.getUserId())
                        .track()
                context.startActivity(item.redirectUrl)
            }
            return inflate
        }
    }


    private fun followUser(feedData: Feed, followText: FollowTextView,
                           followSuccess: () -> Unit) {
        if (!accountManager.hasAccount()) {
            waitFeedData = feedData
            waitFollowText = followText
            LoginActivity.launch(followText.context)
        } else {
            if (!feedData.hasFollowUser!!) {
                apiService.createFollow(feedData.userId.toString())
                        .applySchedulers()
                        .subscribeApi(onNext = {
                            if (it.result.isSuccess) {
                                followSuccess.invoke()
                                FollowCacheManager.putFollowUserToCache(feedData.userId.toString(), true)
                                feedData.hasFollowUser = true
                                followText.followSuccessGone()
                                followText.context.getSpider().userFollowEvent(followText.context, feedData.getUserId(),
                                        SpiderAction.VideoPlaySource.EXPLORE_PAGE.source, true)
                            }
                        }
                        )
            }
        }
    }

    //粉丝 up 标签等展示 规则文档：https://www.teambition.com/project/5beb9f05d98d9700182887d0/tasks/scrum/5ca1c2f8c08c860018bb1581/task/5cac3847800c08001971bd31
    private fun userShownLabel(author: Author?): String? {
        if (author?.fans_count ?: 0 >= 5000) {
            return "粉丝 " + (numFormat(author?.fans_count ?: 0))
        }
        return "UP值 " + (numFormat(author?.upCount ?: 0))
    }


    private var waitUserId: String? = null
    private var waitFollow: FollowTextView? = null
    private var waitItem: ExploreChainUserItemModel? = null

    fun onLoginChainUserFollow() {
        waitUserId ?: return
        waitFollow ?: return
        waitItem ?: return
        setFollowClick(false, waitUserId!!, waitFollow!!, waitItem!!) {
            waitUserId = null
            waitFollow = null
            waitItem = null
        }
    }


    private var waitSubscribeBtn: TextView? = null
    private var waitChannelId: Long? = null
    private var waitItemChannel: ExploreChannelItemModel? = null
    private var waitChannelNum: TextView? = null

    fun onLoginHotChannelSubscribe() {
        waitSubscribeBtn ?: return
        waitChannelId ?: return
        waitItemChannel ?: return
        waitChannelNum ?: return
        setSubscribeClick(false, waitSubscribeBtn!!,
                waitChannelId!!, waitItemChannel!!, waitChannelNum!!) {
            waitSubscribeBtn = null
            waitChannelId = null
            waitItemChannel = null
            waitChannelNum = null
        }
    }


    private var waitFeedData: Feed? = null
    private var waitFollowText: FollowTextView? = null

    fun onLoginChainFollow() {
        waitFeedData ?: return
        waitFollowText ?: return
        followUser(waitFeedData!!, waitFollowText!!) {
            waitFeedData = null
            waitFollowText = null
        }
    }


}