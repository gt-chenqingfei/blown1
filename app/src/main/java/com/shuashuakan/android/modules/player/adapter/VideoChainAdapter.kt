package  com.shuashuakan.android.ui.player.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.GenericLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.shuashuakan.android.R
import com.shuashuakan.android.constant.Constants
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.home.ChainsFeedListModel
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.*
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.ACCOUNT_PAGE
import com.shuashuakan.android.modules.FeedTransportManager
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.modules.comment.VideoCommentDialogFragment
import com.shuashuakan.android.modules.player.fragment.VideoListFragment
import com.shuashuakan.android.modules.profile.UserProfileActivity
import com.shuashuakan.android.modules.share.ShareConfig
import com.shuashuakan.android.modules.share.ShareHelper
import com.shuashuakan.android.modules.share.SpeciaVideoLongClickDialog
import com.shuashuakan.android.modules.widget.DanmakuContainer
import com.shuashuakan.android.modules.widget.FollowTextView
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.modules.widget.customview.ViewPlayBottomlProgressBar
import com.shuashuakan.android.modules.widget.up.PeriscopeLayout
import com.shuashuakan.android.player.SSKVideoTextureView
import com.shuashuakan.android.player.SSKViewPagerLayoutManager
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.utils.*
import com.shuashuakan.android.utils.download.DownloadManager
import com.shuashuakan.android.utils.extension.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber

class VideoChainAdapter(
        private val videoPlayContainer: ViewGroup,
        private val apiService: ApiService,
        private val accountManager: AccountManager,
        private val shareHelper: ShareHelper,
        private val fromMark: Int? = null,
        private val feedSource: String? = null,
        private val fragment: VideoListFragment

) : BaseQuickAdapter<Feed, BaseViewHolder>(R.layout.view_play_cell), GenericLifecycleObserver {

    private lateinit var masterFeed: Feed
    private val compositeDisposable = CompositeDisposable()
    private lateinit var progress: ViewPlayBottomlProgressBar

    private var videoTexture = videoPlayContainer.findViewById<SSKVideoTextureView>(R.id.video_texture)

    private var currentChainsFeedListModel: ChainsFeedListModel? = null
    private val defaultLoadItemCount = 5

    private var currentPosition = 0

    private var floorFeedId: String? = null

    private var mSelectChainPosition: Int = 0
    private var mActivityEvent: ActivityEvent? = null
    private var mDanmakaEvent: DanmakaControlEvent? = null

    fun reset(feed: Feed, progress: ViewPlayBottomlProgressBar, selectChainPosition: Int, activityEvent: ActivityEvent?, danmakaEvent: DanmakaControlEvent?) {
        this.masterFeed = feed
        this.progress = progress
        this.currentPosition = 0
        this.floorFeedId = masterFeed.id
        this.mSelectChainPosition = selectChainPosition
        this.mActivityEvent = activityEvent
        this.mDanmakaEvent = danmakaEvent
        initChainCount(masterFeed.masterFeedId)
    }

    private val shareAnimation = ScaleAnimation(0.9f, 1.1f, 0.9f, 1.1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)

    init {
        shareAnimation.repeatMode = Animation.REVERSE
        shareAnimation.repeatCount = Animation.INFINITE
        shareAnimation.duration = 500
        shareAnimation.interpolator = LinearInterpolator()

        fragment.lifecycle.addObserver(this@VideoChainAdapter)
    }

    override fun onStateChanged(source: LifecycleOwner?, event: Lifecycle.Event?) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            fragment.lifecycle.removeObserver(this)
            compositeDisposable.clear()
            shareAnimation.cancel()

            for (i in 0 until recyclerView.childCount) {
                val child = recyclerView.getChildAt(i)
                val lottieAnimationView = child?.findViewById<LottieAnimationView>(R.id.up_view)
                lottieAnimationView?.cancelAnimation()
            }
        }
    }


    private val mClickListener = object : View.OnClickListener, VideoCommentDialogFragment.OnDismissListener {


        private fun commentClickMethod(v: View, onCommentDialogShown: (Feed) -> Unit) {
            val position = v.tag as Int
            val feedData = data[position]

            if (fromMark == FeedTransportManager.MARK_FROM_PERSONAL_NEWEST && feedData.has_audit == true) {
                v.context.showLongToast(R.string.string_video_in_audit)
            } else {
                val activity = v.context as? FragmentActivity
                activity?.let {
                    val dialog = VideoCommentDialogFragment.create(feedData.id, feedData.commentNum, feedData.getUserId(), position)
                    dialog.show(activity.supportFragmentManager, "VideoChainAdapter")
                    dialog.setOnDismissListener(this)
                    onCommentDialogShown(feedData)
                }
            }
        }

        private fun shareMethod(v: View) {
            val position = v.tag as Int
            val feedData = data[position]
            if (fromMark == FeedTransportManager.MARK_FROM_PERSONAL_NEWEST && feedData.has_audit == true) {
                v.context.showLongToast(R.string.string_video_in_audit)
            } else {
                val activity = v.context as? Activity
                shareHelper.shareType = ShareConfig.SHARE_TYPE_VIDEO
                if (!feedData.getUserId().isNullOrEmpty() && feedData.getUserId() == accountManager.account()?.userId.toString()) {
                    shareHelper.doShare(activity!!, null, feedData.id,
                            false, true, null,
                            videoUrl = feedData.videoDetails?.let { details -> details[0].url }
                                    ?: "", // data.videoDetails!![0].url,
                            videoCoverUrl = feedData.cover,
                            channelName = feedData.channelName,
                            title = feedData.title,
                            tag = DownloadManager.DOWNLOAD_TAG_FRAGMENT_SPECIAL_VIDEO,
                            allowDownload = feedData.properties?.allow_download
                                    ?: false,
                            canEdit = feedData.properties?.editInfo?.canEdit
                                    ?: false,
                            message = feedData.properties?.editInfo?.message
                                    ?: "",
                            editableCount = feedData.properties?.editInfo?.editableCount
                                    ?: 0)

                    activity.getSpider().shareClickEvent(activity, feedData.id, SpiderAction.VideoPlaySource.FEED_PLAY.source)
                } else {
                    shareHelper.doShare(activity!!, null, feedData.id,
                            false, false, null,
                            tag = DownloadManager.DOWNLOAD_TAG_FRAGMENT_SPECIAL_VIDEO,
                            allowDownload = feedData.properties?.allow_download
                                    ?: false)

                    activity.getSpider().shareClickEvent(activity, feedData.id, SpiderAction.VideoPlaySource.FEED_PLAY.source)

                }
            }
        }

        private fun avatarClickMethod(v: View) {
            val position = v.tag as Int
            val feedData = data[position]
            val activity = v.context as? Activity
            activity?.let {

                val source = if (com.shuashuakan.android.modules.viphome.Constants.IS_OPEN_VIP_ROOM)
                    SpiderAction.PersonSource.VIP_HOME_PAGE.source
                else SpiderAction.PersonSource.FEED_PLAY.source
                it.startActivity(Intent(activity, UserProfileActivity::class.java)
                        .putExtra("id", feedData.userId.toString())
                        .putExtra("source", source)
                        .putExtra("feedId", feedData.id))
            }

        }

        private fun followUser(v: FollowTextView) {
            val position = v.tag as Int
            val feedData = data[position]
            if (!accountManager.hasAccount()) {
                followUserFeed = feedData
                followTextView = v
                RxBus.get().post(VideoListFollowUserLogin())
            } else {
                val hasFollowUser = hasFollowWithFeed(feedData)
                if (!hasFollowUser) {
                    apiService.createFollow(feedData.userId.toString())
                            .applySchedulers()
                            .subscribeApi(
                                    onNext = {
                                        FollowCacheManager.putFollowUserToCacheWithEvent(feedData.userId.toString(), true, false)
                                        feedData.hasFollowUser = true
                                        if (masterFeed.userId == feedData.userId) {
                                            masterFeed.hasFollowUser = true
                                        }
                                        v.followSuccessVisible()
                                        v.context.getSpider().userFollowEvent(v.context, feedData.getUserId(),
                                                SpiderAction.VideoPlaySource.FEED_PLAY.source, true)
                                    }
                            )
                }
            }
        }


        /**
         * 评论对话框被隐藏
         */
        override fun onDismiss() {
            videoTexture.hostResume()
        }

        private val once = "ONCE"
        private val always = "ALWAYS"
        private val activitySP = "activity_sp"

        private fun jumpToTarget(v: View) {
            val homePageIcon = mActivityEvent?.homePageIcon ?: return
            when (homePageIcon.frequency) {
                once -> {
                    homePageIcon.redirect_url.let {
                        v.context.startActivity(it)
                        //活动卡片点击
                        v.context.getSpider().manuallyEvent(SpiderEventNames.GAME_CARD_CLICK)
                                .put("userID", v.context.getUserId())
                                .put("ssr", it)
                                .put("target", "FLOATING")
                                .track()
                    }
                    v.context.sharedPreferences(activitySP).edit().putBoolean(homePageIcon.id.toString(), true).apply()
                    RxBus.get().post(ActivityEvent(false, homePageIcon))
                }
                always -> {
                    homePageIcon.redirect_url.let {
                        v.context.startActivity(it)
                        //活动卡片点击
                        v.context.getSpider().manuallyEvent(SpiderEventNames.GAME_CARD_CLICK)
                                .put("userID", v.context.getUserId())
                                .put("ssr", it)
                                .put("target", "FLOATING")
                                .track()
                    }
                }
            }
        }

        private fun danmakaToggle(v: View) {
            val feedData = v.tag as Feed
            if (fromMark == FeedTransportManager.MARK_FROM_PERSONAL_NEWEST && feedData.has_audit == true) {
                v.context.showLongToast(R.string.string_video_in_audit)
            } else {
                val danmakaEvent = mDanmakaEvent ?: return
                danmakaEvent.isShow = !danmakaEvent.isShow
                if (danmakaEvent.isShow) {
                    v.context.showTopToast(R.string.string_show_barrage)
                } else {
                    v.context.showTopToast(R.string.string_hide_barrage)
                }
                RxBus.get().post(danmakaEvent)
            }
        }

        override fun onClick(v: View) = when (v.id) {
            R.id.ll_up_view -> {
                upViewClick(v, false, null) {}
            }
            R.id.ll_comment -> {
                commentClickMethod(v) {
                    videoTexture.hostPause()
                    v.context.getSpider().viewCommentsEvent(v.context, it.id, SpiderAction.VideoPlaySource.FEED_PLAY.source)
                }
            }
            R.id.share_icon_view -> {
                shareMethod(v)
            }
            R.id.operation_ll -> {
                avatarClickMethod(v)
            }
            R.id.activity_float -> {
                jumpToTarget(v)
            }
            R.id.barrageToggle -> {
                danmakaToggle(v)
            }
            R.id.sendBarrage -> {
                createBarrage(v)
            }
            R.id.followUser -> {
                followUser(v as FollowTextView)
            }
            else -> {
                //do nothing
            }
        }

    }

    var likeViewClickTime = 0L
    private fun upViewClick(v: View, isDoubleClick: Boolean, event: MotionEvent?, onLikeSucceed: () -> Unit) {
        val position = v.tag as Int
        val feedData = data[position]
        if (fromMark == FeedTransportManager.MARK_FROM_PERSONAL_NEWEST && feedData.has_audit == true) {
            v.context.showLongToast(R.string.string_video_in_audit)
        } else {
            if (!feedData.fav) {
                likeViewClickTime = System.currentTimeMillis()
                likeFeed(v, isDoubleClick, event) {
                    onLikeSucceed.invoke()
                    if (isDoubleClick) {
                        v.context.getSpider().doubleTabUpFeedEvent(v.context, feedData.id, true,
                                SpiderAction.VideoPlaySource.FEED_PLAY.source)
                    } else {
                        v.context.getSpider().likeEvent(v.context, it.id, "like",
                                SpiderAction.VideoPlaySource.FEED_PLAY.source)
                    }
                }
            } else {
                if (System.currentTimeMillis() - likeViewClickTime < likeIntervalTime) {
                    likeViewClickTime = System.currentTimeMillis()
                    likeFeed(v, isDoubleClick, event) {
                        onLikeSucceed.invoke()
                        if (isDoubleClick) {
                            v.context.getSpider().doubleTabUpFeedEvent(v.context, feedData.id, true,
                                    SpiderAction.VideoPlaySource.FEED_PLAY.source)
                        } else {
                            v.context.getSpider().likeEvent(v.context, it.id, "like",
                                    SpiderAction.VideoPlaySource.FEED_PLAY.source)
                        }
                    }
                } else {
                    unlikeFeed(v) {
                        onLikeSucceed.invoke()
                        v.context.getSpider().likeEvent(v.context, it.id, "unlike",
                                SpiderAction.VideoPlaySource.FEED_PLAY.source)
                    }
                }
            }
        }
    }

    private fun createBarrage(v: View) {
        val position = v.tag as? Int ?: return
        val feedData = data[position]

        if (fromMark == FeedTransportManager.MARK_FROM_PERSONAL_NEWEST && feedData.has_audit == true) {
            v.context.showLongToast(R.string.string_video_in_audit)
        } else {
            val inputBarrage = getViewByPosition(position, R.id.inputBarrage) as? EditText ?: return
            val ctx = inputBarrage.context
            val content = inputBarrage.text?.trim()
            if (content == null || content.isEmpty()) {
                return
            }


            val danmakaSendEvent = DanmakaSendEvent(feedData.id, content.toString(), 0, 0, videoTexture.currentPosition)
            RxBus.get().post(danmakaSendEvent)
            KeyBoardUtil.hideInputSoftFromWindowMethod(mContext, inputBarrage)
            inputBarrage.setText("")
            //发送弹幕打点
            ctx.getSpider().manuallyEvent(SpiderEventNames.BARRAGE_SENT)
                    .put("feedID", feedData.id)
                    .put("currentPlayTime", videoTexture.currentPosition)
                    .put("data", content)
                    .put("dataType", "text")
                    .track()

            apiService.createBarrage(feedData.id, "FEED", content.toString(), null, "BARRAGE", videoTexture.currentPosition)
                    .applySchedulers()
                    .subscribeApi(onNext = {}, onApiError = {})
        }

    }

    private val mInputTouchListener = object : View.OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    val position = v.tag as Int
                    val feedData = data[position]
                    if (fromMark == FeedTransportManager.MARK_FROM_PERSONAL_NEWEST && feedData.has_audit == true) {
                        v.context.showLongToast(R.string.string_video_in_audit)
                        return true
                    } else {
                        if (accountManager.hasAccount()) {
                            trackInputClick(v)
                            return false
                        } else {
                            v.context.startActivityForResultByLink(ACCOUNT_PAGE, Constants.REQUEST_LOGIN_CODE)
                            return true
                        }
                    }
                }
                else -> {
                    return false
                }
            }
        }
    }

    /**
     * 弹幕输入框点击打点
     */
    private fun trackInputClick(v: View) {
        val feed = videoTexture.playFeed ?: return
        v.context.getSpider().manuallyEvent(SpiderEventNames.BARRAGE_INPUT_CLICK)
                .put("feedID", feed.id)
                .put("currentPlayTime", videoTexture.currentPosition)
                .track()
    }


    override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder {
        //Timber.e("RecommendChainAdapter onCreateDefViewHolder2")
        val holder = super.onCreateDefViewHolder(parent, viewType)

        holder.getView<View>(R.id.ll_up_view).setSafeClickListener(null, mClickListener)
        val playLayoutView = holder.getView<View>(R.id.play_layout)
        playLayoutView.setOnTouchListener(DoubleTouchEvent { view, event ->
            onViewDoubleClick(view, event)
        })

        holder.getView<View>(R.id.ll_comment).setSafeClickListener(mClickListener)
        holder.getView<View>(R.id.share_icon_view).setSafeClickListener(mClickListener)
        holder.getView<View>(R.id.operation_ll).setSafeClickListener(mClickListener)
        holder.getView<View>(R.id.activity_float).setSafeClickListener(mClickListener)
        val barrageContainer = holder.getView<DanmakuContainer>(R.id.barrageContainer)
        holder.getView<View>(R.id.followUser).setOnClickListener(mClickListener)
        barrageContainer.setDanmakaEvent(mDanmakaEvent)

        holder.getView<ImageView>(R.id.barrageToggle).setOnClickListener(mClickListener)
        val sendBarrage = holder.getView<TextView>(R.id.sendBarrage)
        sendBarrage.setOnClickListener(mClickListener)

        val inputBarrage = holder.getView<EditText>(R.id.inputBarrage)
        inputBarrage.setOnTouchListener(mInputTouchListener)
        inputBarrage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    sendBarrage.isEnabled = !TextUtils.isEmpty(s.trim())
                    if (s.length > 20) {
                        val ctx = holder.itemView.context
                        ctx.showShortToast(ctx.getString(R.string.string_barrage_text_count_limit))
                        val text = s.toString().substring(0, 20)
                        inputBarrage.setText(text)
                        inputBarrage.setSelection(20)
                    }
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })


        inputBarrage.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                createBarrage(v)
                SoftKeyBoardHelper.hideSoftKeyboard(v, v.context)
                true
            } else {
                false
            }

        }

        return holder
    }

    private val mItemViewLongClickListener = object : View.OnLongClickListener {
        override fun onLongClick(v: View?): Boolean {
            val activity = v?.context as? FragmentActivity
            activity?.let {
                val feed = v.tag as? Feed
                val dialog = SpeciaVideoLongClickDialog(it, accountManager = accountManager, feed = feed)
                dialog.show()
                return true
            }
            return false
        }

    }

    override fun convert(holder: BaseViewHolder, item: Feed) {
        spiderFeedExposure(item.id, feedSource, masterFeed.masterFeedId, item.channelId, holder.itemView)
        holder.itemView.tag = item
        holder.itemView.setTag(R.id.chainPosition, holder.adapterPosition)
        holder.itemView.setTag(R.id.video_progress, progress)
        holder.itemView.setTag(R.id.feed, item)
        holder.itemView.setOnLongClickListener(mItemViewLongClickListener)
        val position = holder.adapterPosition
        val ll_up_view = holder.getView<View>(R.id.ll_up_view)
        ll_up_view.tag = position
        val ll_comment = holder.getView<View>(R.id.ll_comment)
        ll_comment.tag = position

        val shareIconView = holder.getView<ImageView>(R.id.share_icon_view)
        shareIconView.tag = position

        val inputBarrage = holder.getView<TextView>(R.id.inputBarrage)
        inputBarrage.tag = position
        val followUser = holder.getView<FollowTextView>(R.id.followUser)
        followUser?.tag = position

        holder.getView<View>(R.id.operation_ll).tag = position
        val mBottomFollowView = holder.getView<View>(R.id.bottom_follow)

        val blurView = holder.getView<SimpleDraweeView>(R.id.blur_view)
        blurView.loadBlur(item.firstFrame ?: item.cover, item.width, item.height)

        val fakeVideoCover = holder.getView<SimpleDraweeView>(R.id.fake_video_cover)
        fakeVideoCover.loadMatchScreenWidth(item.firstFrame
                ?: item.cover, item.width, item.height, 0.4f)


        if (item == videoTexture.playFeed) {
            holder.getView<View>(R.id.frame_fake_cover).visibility = View.INVISIBLE
        } else {
            holder.getView<View>(R.id.frame_fake_cover).visibility = View.VISIBLE
        }

        formatContentView(item, holder, position)

        holder.getView<SimpleDraweeView>(R.id.user_avatar)
                .loadExpectSize(item.avatar, 36, 36)
        val userNameView = holder.getView<TextView>(R.id.user_name_view)
        userNameView?.text = item.userName


        val upView = holder.getView<LottieAnimationView>(R.id.up_view)
        val upCount = holder.getView<TextView>(R.id.tv_up_count)

        val commentCount = holder.getView<TextView>(R.id.tv_comment_count)

        if (item.commentNum != 0) {
            commentCount.visibility = View.VISIBLE
            commentCount.text = numFormat(item.commentNum)
        } else {
            commentCount.visibility = View.INVISIBLE
        }

        if (item.favNum != 0) {
            upCount.visibility = View.VISIBLE
            upCount.text = numFormat(item.favNum)
        } else {
            upCount.visibility = View.INVISIBLE
        }

        if (item.fav) {
            upView.setAnimation("up_second_action.json")
        } else {
            upView.setAnimation("up_static.json")
            upView.repeatCount = ValueAnimator.INFINITE
        }
        upView.playAnimation()

        val activityFloat = holder.getView<SimpleDraweeView>(R.id.activity_float)
        val activityEvent = mActivityEvent

        if (activityEvent?.isShow == true) {
            activityFloat.visibility = View.VISIBLE
            activityFloat.loadExpectSize(activityEvent.homePageIcon.image_url, 40, 40)
            put(item.id, activityEvent.homePageIcon.redirect_url, activityFloat)
        } else {
            activityFloat.visibility = View.GONE
        }

        val hasFollowUser = hasFollowWithFeed(item)
        if (accountManager.hasAccount()) {
            if (accountManager.account()!!.userId.toString() == item.userId.toString()) {
                followUser.layoutParams.width = 0
                mBottomFollowView.visibility = View.GONE
                (userNameView.layoutParams as RelativeLayout.LayoutParams).leftMargin = mContext.dip(10f)
            } else {
                relayoutUserParams(userNameView!!, hasFollowUser, mBottomFollowView!!, followUser!!)
            }
        } else {
            relayoutUserParams(userNameView!!, hasFollowUser, mBottomFollowView!!, followUser!!)
        }
        if (item == suitableShareAnimationFeed && isShowShareAnimation) {
            shareIconView.startAnimation(shareAnimation)
            shareIconView.setImageResource(R.drawable.ic_wechat_share)
        } else {
            shareIconView.clearAnimation()
            shareIconView.setImageResource(R.drawable.ic_timeline_share)
        }

        val barrageContainer = holder.getView<ViewGroup>(R.id.barrageContainer)

        val danmakaEvent = mDanmakaEvent
        if (danmakaEvent?.isOpen == true) {
            barrageContainer.visibility = View.VISIBLE
            barrageContainer.translationY = 0f

            val barrageToggle = holder.getView<ImageView>(R.id.barrageToggle)
            barrageToggle.tag = item
            barrageToggle.isSelected = danmakaEvent.isShow

            val inputBarrage = holder.getView<EditText>(R.id.inputBarrage)
            inputBarrage.setText("")

            val sendBarrage = holder.getView<TextView>(R.id.sendBarrage)
            sendBarrage.tag = position

            if (fromMark == FeedTransportManager.MARK_FROM_PERSONAL_NEWEST && item.has_audit == true) {
                barrageContainer.alpha = 0.5f
                shareIconView.alpha = 0.5f
                ll_comment.alpha = 0.5f
                ll_up_view.alpha = 0.5f
                inputBarrage.isFocusable = false

            } else {
                barrageContainer.alpha = 1f
                shareIconView.alpha = 1f
                ll_comment.alpha = 1f
                ll_up_view.alpha = 1f
                inputBarrage.isFocusable = true
            }


        } else {
            barrageContainer.visibility = View.GONE
        }
    }

    private fun hasFollowWithFeed(item: Feed): Boolean {
        return if (item.userId == masterFeed.userId) {
            FollowCacheManager.isUserFollowInCache(masterFeed.userId.toString())
                    ?: (masterFeed.hasFollowUser ?: false)
        } else {
            FollowCacheManager.isUserFollowInCache(masterFeed.userId.toString())
                    ?: (item.hasFollowUser ?: false)
        }
    }

    private fun relayoutUserParams(userNameView: TextView, hasFollowUser: Boolean, mBottomFollowView: View, followUser: FollowTextView) {
        val layoutParams = (userNameView.layoutParams as RelativeLayout.LayoutParams)
        if (hasFollowUser) {
            mBottomFollowView.visibility = View.VISIBLE
            followUser.visibility = View.GONE
            layoutParams.leftMargin = mContext.dip(46f)
            layoutParams.removeRule(RelativeLayout.CENTER_VERTICAL)
        } else {
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
            mBottomFollowView.visibility = View.GONE
            followUser.visibility = View.VISIBLE
            followUser.reset()
            (userNameView.layoutParams as RelativeLayout.LayoutParams).leftMargin = mContext.dip(10f)
        }
        userNameView.layoutParams = layoutParams
    }

    private fun formatContentView(item: Feed, holder: BaseViewHolder, position: Int) {
        val channelTitle = item.channelName ?: ""
        val realContent = StringUtils.replaceBlank(item.title)
        val contentText = SpannableString(StringUtils.replaceBlank(item.title) + " #" + channelTitle)
        contentText.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                labelClickMethod(widget) { context, feed ->
                    feed.channelUrl?.let {
                        //打点之频道页面跳转
                        val channel = it.split("id=")
                        val channelId = channel[channel.size - 1]
                        context.getSpider().manuallyEvent(SpiderEventNames.FEED_CLICK_CHANNEL_PAGE)
                                .put("feedID", feed.id)
                                .put("userID", accountManager.account()?.userId ?: "")
                                .put("channelID", channelId)
                                .track()
                    }
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.typeface = Typeface.DEFAULT_BOLD
                ds.isUnderlineText = false
                ds.color = mContext.resources.getColor(R.color.white)
            }

            private fun labelClickMethod(v: View, doBeforeToChannel: (Context, Feed) -> Unit) {
                val position = v.tag as Int
                val feedData = data[position]
                val activity = v.context as? Activity
                activity?.let {
                    doBeforeToChannel(it.application, feedData)
                    it.startActivity(feedData.channelUrl)
                }
            }
        }, realContent.length, contentText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val videoTitleView = holder.getView<TextView>(R.id.video_title_view)
        videoTitleView.tag = position
        videoTitleView.text = contentText
        videoTitleView.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onViewDetachedFromWindow(holder: BaseViewHolder) {
        super.onViewDetachedFromWindow(holder)

        holder.getView<LottieAnimationView>(R.id.up_view)?.cancelAnimation()
    }


    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onViewAttachedToWindow(holder: BaseViewHolder) {
        super.onViewAttachedToWindow(holder)
        Timber.e("RecommendChainAdapter onViewAttachedToWindow")
        videoTexture.clearUpDateSeekTask()
        holder.getView<LottieAnimationView>(R.id.up_view)?.playAnimation()

        suitableShareAnimationFeed = null
        isShowShareAnimation = false

        val shareIconView = holder.getView<ImageView>(R.id.share_icon_view)
        shareIconView?.let {
            it.clearAnimation()
            it.setImageResource(R.drawable.ic_timeline_share)
        }
    }

    @SuppressLint("CheckResult")
    private fun initObservable() {
        RxBus.get().toFlowable().subscribe { event ->
            when (event) {
                is VideoChainFollowEvent -> {
                    for (singleData in data.indices) {
                        if (data[singleData].userId.toString() == event.userId) {
                            notifyDataSetChanged()
                        }
                    }
                }
                is VideoChainClearFollowEvent -> {
                    notifyDataSetChanged()
                }
            }
        }.addTo(compositeDisposable)
    }


    private fun initChainCount(masterFeedId: String?) {
        if (masterFeedId != null && masterFeedId.getRealId() == masterFeed.masterFeedId.getRealId()) {
            apiService.counter(masterFeedId).applySchedulers().subscribeApi(
                    onNext = {
                        val solitaireNum = it.solitaireNum + 1
                        masterFeed.solitaireNum = solitaireNum
                        progress.setProgressNum(solitaireNum)
                    })
        }
    }


    private fun loadMoreData() {
        apiService.getChainsFeeds(masterFeed.masterFeedId,
                data.last().id,
                "DOWN",
                defaultLoadItemCount,
                floorFeedId,
                data.last().properties?.floor,
                currentChainsFeedListModel?.snapId)
                .doOnNext {
                    floorFeedId = null
                }
                .applySchedulers()
                .subscribeApi(
                        onNext = {
                            val list = it.feedList
                            currentChainsFeedListModel = it
                            val listIsEmpty = list?.isEmpty()
                            if (listIsEmpty == true) {
                                loadMoreEnd()
                            } else {
                                if (listIsEmpty == false) {
                                    val firstFeed = list[0]

                                    if (firstFeed.userId == masterFeed.userId) {
                                        masterFeed.hasFollowUser = firstFeed.hasFollowUser
                                    }

                                    if (firstFeed.masterFeedId.getRealId() == masterFeed.masterFeedId.getRealId()) {
                                        addData(list)
                                    }
                                }
                                loadMoreComplete()
                            }
                        },
                        onApiError = {
                            loadMoreComplete()
                        })
    }

    override fun addData(newData: Collection<Feed>) {
        val okData = newData.filter {
            !data.contains(it)
        }.toList()
        super.addData(okData)
    }

    override fun addData(feed: Feed) {
        if (!data.contains(feed)) {
            super.addData(feed)
        }
    }


    override fun createBaseViewHolder(view: View?): BaseViewHolder {
        return BaseViewHolder(view)
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        initObservable()
        //Timber.e("RecommendChainAdapter onAttachedToRecyclerView --")
        if (mSelectChainPosition > 0) {
            bindToRecyclerView(recyclerView)
        } else {
            setOnLoadMoreListener({
                loadMoreData()
            }, recyclerView)
        }

    }

    /**
     * 取消点赞 点过赞的肯定都是已登录的
     */
    private fun unlikeFeed(v: View, unLikeSucceed: (feed: Feed) -> Unit) {
        val position = v.tag as Int
        val feedData = data[position]
        val upView = v.findViewById<LottieAnimationView>(R.id.up_view)
        val upTextView = v.findViewById<TextView>(R.id.tv_up_count)
        if (feedData.fav) {
            feedData.fav = false
            upView.cancelAnimation()
            upView.setAnimation("up_action.json")
            upView.repeatCount = 0
            upView.playAnimation()
            upView.addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    upView.removeAnimatorListener(this)
                    apiService.unLikeFeed(feedData.id)
                            .applySchedulers()
                            .subscribeApi(
                                    onNext = {
                                        if (it.result.isSuccess) {
                                            feedData.fav = false
                                            feedData.favNum--
                                            if (feedData.favNum <= 0) {
                                                feedData.favNum = 0
                                            }

                                            upTextView.visibility = if (feedData.favNum == 0) View.INVISIBLE else View.VISIBLE
                                            upTextView.text = numFormat(feedData.favNum)

                                            unLikeSucceed(feedData)
                                            upView.setAnimation("up_static.json")
                                            upView.playAnimation()
                                            if (feedData.favNum == 1) {
                                                //修复点赞数为零的情况，点赞后不刷新的问题，没找到根本原因，这只是临时方案
                                                notifyItemChanged(position)
                                            }
                                        } else {
                                            feedData.fav = true
                                            upView.setAnimation("up_action.json")
                                            upView.playAnimation()
                                        }
                                    },
                                    onApiError = {
                                        feedData.fav = true
                                        upView.setAnimation("up_action.json")
                                        upView.playAnimation()
                                    })
                }
            })
        }
    }


    private fun likeFeed(v: View, isDoubleClick: Boolean, event: MotionEvent?, onLikeSucceed: (feed: Feed) -> Unit) {
        val position = v.tag as Int
        val feedData = data[position]
        if (accountManager.hasAccount()) {
            val periscopeLayout = videoPlayContainer.findViewById(R.id.up_layout) as PeriscopeLayout
            if (isDoubleClick) {
                periscopeLayout.addHeartWithDelay(event)
            } else {
                periscopeLayout.addHeart()
            }


            val upView = v.findViewById<LottieAnimationView>(R.id.up_view)
            val upTextView = v.findViewById<TextView>(R.id.tv_up_count)
            if (!feedData.fav) {
                feedData.fav = true
                upView.cancelAnimation()
                upView.setAnimation("up_action.json")
                upView.repeatCount = 0
                upView.playAnimation()
                upView.addAnimatorListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        upView.removeAnimatorListener(this)
                        apiService.likeFeed(feedData.id)
                                .applySchedulers()
                                .subscribeApi(
                                        onNext = {
                                            if (it.result.isSuccess) {
                                                feedData.fav = true
                                                feedData.favNum++
                                                upTextView.text = numFormat(feedData.favNum)

                                                onLikeSucceed(feedData)
                                                upView.setAnimation("up_second_action.json")
                                                upView.playAnimation()
                                                if (feedData.favNum == 1) {
                                                    //修复点赞数为零的情况，点赞后不刷新的问题，没找到根本原因，这只是临时方案
                                                    notifyItemChanged(position)
                                                }
                                            } else {
                                                feedData.fav = false
                                                upView.setAnimation("up_static.json")
                                                upView.playAnimation()
                                            }
                                        },
                                        onApiError = {
                                            feedData.fav = false
                                            upView.setAnimation("up_static.json")
                                            upView.playAnimation()
                                        })
                    }
                })
            }

        } else {
            likeView = v
            clickEvent = event
            isLikeDoubleClick = isDoubleClick
            LoginActivity.launchForResult(v.context, Constants.REQUEST_LOGIN_CODE)
//            v.context.startActivityForResultByLink(ACCOUNT_PAGE, Constants.REQUEST_LOGIN_CODE)
        }
    }

    private fun onViewDoubleClick(v: View, event: MotionEvent) {
        var clickView = v.findViewById<View>(R.id.ll_up_view)
        val position = clickView.tag as Int
        val feedData = data[position]
        // 双击不能取消点赞
        if (feedData.fav) {
            val periscopeLayout = videoPlayContainer.findViewById(R.id.up_layout) as PeriscopeLayout
            periscopeLayout.addHeartWithDelay(event)
            v.context.getSpider().doubleTabUpFeedEvent(v.context, feedData.id, false,
                    SpiderAction.VideoPlaySource.FEED_PLAY.source)
        } else {
            upViewClick(clickView, true, event) {}
        }
    }

    companion object {
        var array: ArrayList<String> = ArrayList()
        var arrayWithFeedExposure: ArrayList<String> = ArrayList()
        var likeIntervalTime = 3000

        fun put(feedId: String, ssr: String, view: View) {

            if (array.contains(feedId)) {
                return
            }
            if (!array.isEmpty()) {
                array.clear()
            }
            array.add(feedId)

            view.context.getSpider().manuallyEvent(SpiderEventNames.GAME_CARD_EXPOSURE)
                    .put("userID", view.context.getUserId())
                    .put("ssr", ssr)
                    .put("target", "FLOATING")
                    .track()

        }

        fun spiderFeedExposure(feedId: String, feedSource: String?, masterID: String?, channelId: Int?, view: View) {
            if (arrayWithFeedExposure.contains(feedId)) {
                return
            }
            if (!arrayWithFeedExposure.isEmpty()) {
                arrayWithFeedExposure.clear()
            }
            arrayWithFeedExposure.add(feedId)

            val source = view.context.getChangeSource(feedSource)
            view.context.getSpider().manuallyEvent(SpiderEventNames.Player.VIDEO_EXPOSURE)
                    .put("source", source)
                    .put("masterID", masterID ?: "")
                    .put("feedID", feedId)
                    .put("channelID", channelId ?: "")
                    .track()
        }
    }

    private var suitableShareAnimationFeed: Feed? = null
    private var isShowShareAnimation = false

    fun showShareAnimation(playFeed: Feed) {
        if (playFeed != suitableShareAnimationFeed) {
            suitableShareAnimationFeed = playFeed
            isShowShareAnimation = true

            val lm = recyclerView?.layoutManager as? SSKViewPagerLayoutManager ?: return
            val position = lm.getPosition(lm.findCenterView())
            notifyItemChanged(position)
        }
    }


    private var followUserFeed: Feed? = null
    private var followTextView: FollowTextView? = null
    fun setFollowViewState() {
        if (accountManager.hasAccount()) {
            val feedData = followUserFeed ?: return
            val followView = followTextView ?: return

            val hasFollowUser = hasFollowWithFeed(feedData)

            if (!hasFollowUser) {
                apiService.createFollow(feedData.userId.toString())
                        .applySchedulers()
                        .subscribeApi(
                                onNext = {
                                    feedData.hasFollowUser = true
                                    if (masterFeed.userId == feedData.userId) {
                                        masterFeed.hasFollowUser = true
                                    }
                                    followView.followSuccessVisible()
                                    followUserFeed = null
                                    followTextView = null
                                }
                        )
            }
        }
    }


    private var likeView: View? = null
    private var isLikeDoubleClick: Boolean? = null
    private var clickEvent: MotionEvent? = null
    fun setUpViewStatus() {
        if (accountManager.hasAccount()) {
            likeView ?: return
            isLikeDoubleClick ?: return
            upViewClick(likeView!!, isLikeDoubleClick!!, if (isLikeDoubleClick!!) clickEvent else null) {
                likeView = null
                isLikeDoubleClick = null
                clickEvent = null
            }
        }
    }


}

