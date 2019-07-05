package com.shuashuakan.android.modules.profile.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.AppBarLayout.OnOffsetChangedListener
import android.support.v4.view.ViewPager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.*
import com.facebook.drawee.view.SimpleDraweeView
import com.gyf.barlibrary.ImmersionBar
import com.jakewharton.rxbinding2.view.RxView
import com.shuashuakan.android.FishInjection
import com.shuashuakan.android.R
import com.shuashuakan.android.commons.cache.DiskCache
import com.shuashuakan.android.commons.cache.Storage
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.constant.Constants
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.account.CategoriesTag
import com.shuashuakan.android.data.api.model.account.Tags
import com.shuashuakan.android.data.api.model.account.UserAccount
import com.shuashuakan.android.data.api.model.account.UserTag
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.*
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.FAV_LINK
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.modules.message.MessageActivity
import com.shuashuakan.android.modules.message.badage.BadgeClearNonSystemEvent
import com.shuashuakan.android.modules.message.badage.BadgeEvent
import com.shuashuakan.android.modules.message.badage.BadgeManager
import com.shuashuakan.android.modules.profile.EditProfileActivity
import com.shuashuakan.android.modules.profile.FocusListActivity
import com.shuashuakan.android.modules.profile.FocusListActivity.Companion.EXTRA_FOCUS_INFO_ID
import com.shuashuakan.android.modules.profile.FocusListActivity.Companion.EXTRA_FOCUS_INFO_TYPE
import com.shuashuakan.android.modules.profile.FocusListActivity.Companion.FANS_TYPE
import com.shuashuakan.android.modules.profile.FocusListActivity.Companion.FOCUS_TYPE
import com.shuashuakan.android.modules.profile.ShowAvatarDialog
import com.shuashuakan.android.modules.profile.adapter.ProfileUserTagAdapter
import com.shuashuakan.android.modules.profile.adapter.ProfileViewPageAdapter
import com.shuashuakan.android.modules.profile.adapter.UserTagAdapter
import com.shuashuakan.android.modules.profile.presenter.ProfileApiView
import com.shuashuakan.android.modules.profile.presenter.ProfilePresenter
import com.shuashuakan.android.modules.setting.SettingsActivity
import com.shuashuakan.android.modules.topic.TopicCategoryActivity
import com.shuashuakan.android.modules.widget.ColorFlipPagerTitleView
import com.shuashuakan.android.modules.widget.FollowButton
import com.shuashuakan.android.modules.widget.SpiderAction
import com.shuashuakan.android.modules.widget.tagView.TagCloudLayout
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.ui.base.FishFragment
import com.shuashuakan.android.utils.*
import com.shuashuakan.android.utils.extension.showCancelFollowDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.include_fragment_profile_2f.*
import net.lucode.hackware.magicindicator.MagicIndicator
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ProfileFragment : FishFragment(), ProfileApiView<UserAccount>, OnOffsetChangedListener, ProfileUserTagAdapter.UserTagOnClickListener {

    private val backIv by bindView<ImageView>(R.id.back_iv)
    private val mineBag by bindView<ImageView>(R.id.mine_bag_iv)
    private val toolbar by bindView<Toolbar>(R.id.toolbar)
    private val toolbarMore by bindView<ImageView>(R.id.toolbar_more)
    private val toolbarShare by bindView<ImageView>(R.id.toolbar_share)
    private val toolbarMessage by bindView<ImageView>(R.id.toolbar_message)
    private val toolbarMessageCount by bindView<TextView>(R.id.tv_home_avatar_badage)
    private val toolbarSetting by bindView<ImageView>(R.id.toolbar_setting)
    private val toolbarTitle by bindView<TextView>(R.id.toolbar_title)

    private val appBar by bindView<AppBarLayout>(R.id.appbar_layout)
    private val tabLayout by bindView<MagicIndicator>(R.id.home_indicator)
    private val viewPager by bindView<ViewPager>(R.id.view_pager)

    private val profileBg by bindView<SimpleDraweeView>(R.id.profile_bg)

    private val headerLayout by bindView<View>(R.id.header_layout)
    private val avatarView by bindView<SimpleDraweeView>(R.id.avatar_view)
    private val userNameView by bindView<TextView>(R.id.user_name_view)

    private val userTagLl by bindView<TagCloudLayout>(R.id.user_tag_ll)

    private val signatureView by bindView<TextView>(R.id.signature_view)

    private val coinLayout by bindView<View>(R.id.coin_layout)

    private val coinView by bindView<TextView>(R.id.coin_view)
    private val attentionView by bindView<TextView>(R.id.attention_view)
    private val fansView by bindView<TextView>(R.id.fans_view)

    private val followBtn by bindView<FollowButton>(R.id.follow_btn)
    private val editBtn by bindView<Button>(R.id.follow_btn_edit)

    private val attention by bindView<LinearLayout>(R.id.attention_layout)
    private val fans by bindView<LinearLayout>(R.id.fans_layout)

    // 接龙、抢2F、UP值
    private val textViewJieLong by bindView<TextView>(R.id.profile_ll_2f_tv_jielong)
    private val textView2F by bindView<TextView>(R.id.profile_ll_2f_tv_2f)
    private val textViewUP by bindView<TextView>(R.id.profile_ll_2f_tv_up)
    private val question2F by bindView<ImageView>(R.id.profile_ll_2f_rl_iv_2f_question)
    private val listOrGridRL by bindView<RelativeLayout>(R.id.profile_ll_list_or_grid)
    private val listOrGrid by bindView<ImageView>(R.id.profile_list_or_grid)

    private val mUserTagTextView by bindView<TextView>(R.id.profile_ll_2f_tag_textView)
    private val userTagRecyclerView by bindView<RecyclerView>(R.id.profile_ll_2f_tag_recyclerview)

    @Inject
    lateinit var accountManager: AccountManager
    @Inject
    lateinit var appConfig: AppConfig
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var profilePresenter: ProfilePresenter
    @Inject
    lateinit var storage: Storage
    @Inject
    lateinit var spider: Spider
    @Inject
    lateinit var badgeManager: BadgeManager

    private lateinit var accountCache: DiskCache.Cache<UserAccount>

    private val compositeDisposable = CompositeDisposable()

    private lateinit var userTagAdapter: UserTagAdapter

    private lateinit var userId: String

    private var showBack: Boolean = true

    private var isMine: Boolean = false

    private var isList: Boolean = true

    private var isEmptyList: Boolean = false

    private val disposables = CompositeDisposable()
    private var isUnLoginActionFollow: Boolean? = null

    companion object {
        const val EXTRA_USER_ID = "EXTRA_USER_ID"
        const val EXTRA_SHOW_BACK = "EXTRA_SHOW_BACK"
        const val EXTRA_HAS_UNREAD = "EXTRA_HAS_UNREAD"

        const val ACCOUNT_CACHE_KEY = "account_cache_key"


        private const val REQUEST_EDIT_PROFILE = 0
        private const val REQUEST_LOGIN = 1
        private const val REQUEST_CODE_TOPIC_CATEGORY = 2

        fun create(userId: String?, showBack: Boolean, hasUnread: Boolean = false): ProfileFragment {
            val fragment = ProfileFragment()
            val argument = Bundle()
            argument.putString(EXTRA_USER_ID, userId)
            argument.putBoolean(EXTRA_SHOW_BACK, showBack)
            argument.putBoolean(EXTRA_HAS_UNREAD, hasUnread)
            fragment.arguments = argument
            return fragment
        }

        fun createIntentToFocus(context: Context, userId: String, type: String?): Intent {
            return Intent(context, FocusListActivity::class.java)
                    .putExtra(EXTRA_FOCUS_INFO_ID, userId)
                    .putExtra(EXTRA_FOCUS_INFO_TYPE, type)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {}

    @SuppressLint("CheckResult")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ImmersionBar.setTitleBar(activity, toolbar)
        accountCache = storage.userCache.cacheOf()
        userId = arguments?.getString(EXTRA_USER_ID) ?: ""
        showBack = arguments?.getBoolean(EXTRA_SHOW_BACK, true) ?: true

        if (accountManager.hasAccount()) {
            isMine = accountManager.account()!!.userId.toString() == userId
        }

        backIv.noDoubleClick { requireActivity().finish() }
        toolbarMore.noDoubleClick { moreMenu() }
        toolbarShare.noDoubleClick {
            requireContext().startActivity(userAccount?.shareCardUrl)
        }
        toolbarMessage.noDoubleClick {
            startActivity(Intent(requireContext(), MessageActivity::class.java))
        }
        toolbarSetting.noDoubleClick {
            if (userAccount != null) {
                startActivity(SettingsActivity.createIntent(requireContext(), userAccount))
            }
        }
        editBtn.noDoubleClick {
            userAccount?.let {
                startActivityForResult(EditProfileActivity.createIntent(requireContext(), it), REQUEST_EDIT_PROFILE)
            }
        }
        RxView.clicks(mineBag)
                .throttleFirst(Constants.DOUBLE_CLICK_INTERVAL, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe { requireActivity().startActivity("https://app.shuashuakan.net/giftrecord") }
        RxView.clicks(followBtn)
                .throttleFirst(Constants.DOUBLE_CLICK_INTERVAL, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (!accountManager.hasAccount()) {
                        isUnLoginActionFollow = true
                        LoginActivity.launchForResult(requireContext(), REQUEST_LOGIN)
                        return@subscribe
                    }
                    onFollow()
                }
        userTagLl.noDoubleClick {
            if (isMine) {
                userAccount?.let {
                    startActivityForResult(EditProfileActivity.createIntent(requireContext(), it), REQUEST_EDIT_PROFILE)
                }
            }
        }

        attention.noDoubleClick {
            if (!accountManager.hasAccount()) {
                LoginActivity.launch(requireContext())
                return@noDoubleClick
            }
            requireActivity().startActivity(createIntentToFocus(requireActivity(), userId, FOCUS_TYPE))
        }

        fans.noDoubleClick {
            if (!accountManager.hasAccount()) {
                LoginActivity.launch(requireContext())
                return@noDoubleClick
            }
            requireActivity().startActivity(createIntentToFocus(requireActivity(), userId, FANS_TYPE))
        }

        subscribe_layout.setOnClickListener {
            activity?.let {
                spider.manuallyEvent(SpiderEventNames.SUBSCRIBE_CLICK)
                        .put("userID", it.getUserId())
                        .track()
                spider.allTopicsExposureEvent(SpiderAction.TopicCategorySource.PersonelPage.source)
                TopicCategoryActivity.launchForFragment(activity = it, fragment = this@ProfileFragment, requestCode = REQUEST_CODE_TOPIC_CATEGORY)
            }
        }

        coinLayout.noDoubleClick { requireActivity().startActivity("https://app.shuashuakan.net/giftrecord") }

        listOrGridRL.noDoubleClick {
            if (isList) {
                isList = false
                listOrGrid.setImageResource(R.drawable.profile_2f_button_list)
            } else {
                isList = true
                listOrGrid.setImageResource(R.drawable.profile_2f_button_nine)
            }
            if (!isEmptyList)
                RxBus.get().post(ProfileTimeLineGridOrListEvent(isList))
        }

        question2F.noDoubleClick {
            requireActivity().startActivity("https://topic.shuashuakan.net/community/2floor-rule.html")
        }

        initView()
        initData()
    }

    private fun initView() {
        if (isMine) {
            subscribe_layout.visibility = View.VISIBLE
            followBtn.visibility = View.GONE
            editBtn.visibility = View.VISIBLE
            mineBag.visibility = if (appConfig.isShowPackage()) View.VISIBLE else View.GONE
            toolbarMore.visibility = View.GONE
            toolbarSetting.visibility = View.VISIBLE
            showProfileBadge()
            //主页绑定
            RxBus.get().toFlowable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        when (it) {
                            is RefreshProfileEvent -> profilePresenter.requestApi(userId, isMine)
                            is LoginSuccessEvent -> profilePresenter.requestApi(userId, isMine)
                            is UpdateLikeFeedListEvent -> profilePresenter.requestApi(userId, isMine) // 接龙、抢2F、UP值都要通知更新此列表
                            is BadgeEvent, is BadgeClearNonSystemEvent -> {
                                showProfileBadge()
                            }
                        }
                    }.addTo(compositeDisposable)
        } else {
            subscribe_layout.visibility = View.GONE
            followBtn.visibility = View.VISIBLE
            editBtn.visibility = View.GONE
            mineBag.visibility = View.GONE
            toolbarMore.visibility = View.GONE
            toolbarSetting.visibility = View.GONE
            toolbarMessage.visibility = View.GONE
            toolbarMessageCount.visibility = View.GONE
            //个人页
            compositeDisposable.clear()
        }
        RxBus.get().toFlowable().subscribe {
            when (it) {
                is ShowProfileTimeLineEmptyGridListButtonEvent -> {
                    isEmptyList = it.isEmptyList
                    if (it.isEmptyList) {
                        listOrGrid.setImageResource(R.drawable.profile_2f_button_list)
                    }
                }
                is LoginSuccessEvent -> {
                    isUnLoginActionFollow?.let {
                        onFollow()
                        isUnLoginActionFollow = null
                    }
                }
            }
        }.addTo(compositeDisposable)
        if (showBack) {
            backIv.visibility = View.VISIBLE
        } else {
            backIv.visibility = View.GONE
        }
        followBtn.setUserId(userId)

        userTagAdapter = UserTagAdapter(requireContext())

        userTagLl.setAdapter(userTagAdapter, false)
    }

    private fun moreMenu() {
        if (isMine && userAccount != null) {
            val items: Array<String> = if (appConfig.isShowPackage()) {
                arrayOf(getString(R.string.string_share_owner_carte), getString(R.string.string_grass_commodity), getString(R.string.string_change_profile_data), getString(R.string.string_setting))
            } else {
                arrayOf(getString(R.string.string_share_owner_carte), getString(R.string.string_change_profile_data), getString(R.string.string_setting))
            }
            userAccount?.let {
                AlertDialog.Builder(requireContext())
                        .setItems(items) { _, which ->
                            if (appConfig.isShowPackage()) {
                                when (which) {
                                    0 -> requireContext().startActivity(userAccount!!.shareCardUrl)
                                    1 -> requireActivity().startActivity(FAV_LINK)
                                    2 -> startActivityForResult(EditProfileActivity.createIntent(
                                            requireContext(), it), REQUEST_EDIT_PROFILE)
                                    3 -> startActivity(SettingsActivity.createIntent(requireContext(), userAccount))
                                }
                            } else {
                                when (which) {
                                    0 -> requireContext().startActivity(userAccount!!.shareCardUrl)
                                    1 -> startActivityForResult(EditProfileActivity.createIntent(
                                            requireContext(), it), REQUEST_EDIT_PROFILE)
                                    2 -> startActivity(SettingsActivity.createIntent(requireContext(), userAccount))
                                }
                            }
                        }

                        .show()
            }
        }
    }

    override fun showData(data: UserAccount) {
        initHeaderView(data)

        if (!isMine && (data.like_feed_count
                        ?: 0) > 0) {//看别人的个人页时，该用户没有发布过视频但是up过视频，进入时定位在“up的视频”tab
            val timeLineVideoCount = (data.uploadFeedCount ?: 0) + (data.solitaireNum ?: 0)
            if (timeLineVideoCount > 0) {
                viewPager.currentItem = 0
            } else {
                viewPager.currentItem = 1
            }
        }

        SpUtil.saveOrUpdate(AppConfig.PHONE_NUM, data.mobile ?: "")
        if (accountManager.hasAccount() && accountManager.account()?.userId == data.userId) {
            storage.userCache.cacheOf<UserAccount>().put(ACCOUNT_CACHE_KEY, data)
        }
    }

    private var firstLoad = true //只第一次加载indicator

    private var userAccount: UserAccount? = null

    private fun initHeaderView(userAccount: UserAccount) {
        this.userAccount = userAccount
        if (accountManager.hasAccount()) {
            if (accountManager.account()!!.userId.toString() == userId && !isMine) {
                isMine = true
                firstLoad = true
                initView()
            }
        }

        if (isMine) {
            accountCache.put(ACCOUNT_CACHE_KEY, userAccount)
            coinLayout.visibility = View.GONE
        } else {
            followBtn.setFollowStatus(userAccount.isFollow!!, userAccount.is_fans)
            coinLayout.visibility = View.GONE
        }

//        if (userAccount.labels != null && userAccount.labels!!.isNotEmpty()) {
//            userTagLl.visibility = View.VISIBLE
//            userTagAdapter.updateData(userAccount.labels)
//        } else {
//            userTagLl.visibility = View.GONE
//        }

        val size = requireContext().dip(76)
        if (userAccount.avatar == null) {
            loadImage(avatarView, userAccount.defaultAvatar, size, size)
            profileBg.setActualImageResource(R.drawable.profile_bg)
        } else {
            showUrlBlur(profileBg, userAccount.avatar!!, 12, 12)
            loadImage(avatarView, userAccount.avatar, size, size)
        }
        if (userAccount.avatar != null) {
            avatarView.setOnClickListener {
                spider.manuallyEvent(SpiderEventNames.AVATAR_CLICK).put("target_user_id", userAccount.userId.toString()).track()
                ShowAvatarDialog(requireActivity(), userAccount.avatar).show()
            }
        }

        toolbarTitle.text = userAccount.nickName
        toolbarTitle.setTextColor(Color.TRANSPARENT)

        userNameView.text = userAccount.nickName

        val length = userAccount.bio?.length ?: 0
        signatureView.text = if (length > 0) userAccount.bio else getString(R.string.string_empty_Introduction)

        coinView.text = numFormat(userAccount.point)
        attentionView.text = numFormat(userAccount.followCount)
        fansView.text = numFormat(userAccount.fansCount)
        subscribe_view.text = numFormat(userAccount.subscribed_channel_count)

        if (firstLoad) {
            firstLoad = false
            initMagicIndicator(userAccount)
        }

        textViewJieLong.text = numFormat(userAccount.solitaireNum)
        textView2F.text = numFormat(userAccount.optimalSolitaireCount)
        textViewUP.text = numFormat(userAccount.upCount)

        // 作者标识
//        if (userAccount.tags != null) {
//            val tagsList: List<Tags> = userAccount.tags!!
//            if (tagsList.isNotEmpty()) {
//                userTagRecyclerView.visibility = View.VISIBLE
//                val userTagLinearLayoutManager = LinearLayoutManager(requireContext())
//                userTagLinearLayoutManager.orientation = RecyclerView.HORIZONTAL
//                val userTagAdapter = ProfileUserTagAdapter(tagsList)
//                userTagRecyclerView.adapter = userTagAdapter
//                userTagRecyclerView.layoutManager = userTagLinearLayoutManager
//                userTagAdapter.setUserTagOnClickListener(this)
//            } else {
//                userTagRecyclerView.visibility = View.GONE
//            }
//        }

        val hasUnread = arguments?.getBoolean(EXTRA_HAS_UNREAD, false) ?: false
        if (hasUnread) {
            appBar.setExpanded(false)
        } else {
            appBar.setExpanded(true)
        }

        formatUserCategory(userAccount)
    }

    private fun formatUserCategory(userAccount: UserAccount) {
        // 用户的分类
        var categories = userAccount.categories
        if (categories != null && categories.isNotEmpty()) {
            formatCategoryContent(categories)
            mUserTagTextView.visibility = View.VISIBLE
        } else {
            mUserTagTextView.visibility = View.GONE
        }


        // 用户的标签
        val tags = arrayListOf<UserTag>()
        val tagsReal = userAccount.tags
        if (tagsReal != null && tagsReal.isNotEmpty()) {
            val tagsList: List<Tags> = userAccount.tags!!
            tags.add(UserTag(tagsList[0].title, tagsList[0].icon))
        }

        if (userAccount.labels != null && userAccount.labels!!.isNotEmpty()) {
            tags.addAll(userAccount.labels!!)
        }
        if (tags.isNotEmpty()) {
            userTagLl.visibility = View.VISIBLE
            userTagAdapter.updateData(tags)
        } else {
            userTagLl.visibility = View.GONE
        }
    }


    private fun formatCategoryContent(categories: List<CategoriesTag>) {
        val contentMap = HashMap<String, CategoriesTag>()
        val stringBuilderContent = SpannableStringBuilder("")
        val holderText = getString(R.string.string_user_category_holder)
        if (categories.size == 1) {
            val categoryTag = categories[0]
            categoryTag.startIndex = 0
            categoryTag.endIndex = categoryTag.name!!.length
            stringBuilderContent.append(categoryTag.name)
            contentMap[categories[0].name!!] = categoryTag
        } else {
            categories.forEachIndexed { position, categoryTag ->
                when (position) {
                    0 -> {
                        categoryTag.startIndex = 0
                        categoryTag.endIndex = categoryTag.name!!.length
                        stringBuilderContent.append(categoryTag.name + holderText)
                    }
                    categories.size - 1 -> {
                        categoryTag.startIndex = categories[position - 1].endIndex!! + holderText.length
                        categoryTag.endIndex = categoryTag.startIndex!! + categoryTag.name!!.length

                        stringBuilderContent.append(categoryTag.name)
                    }
                    else -> {
                        categoryTag.startIndex = categories[position - 1].endIndex!! + holderText.length
                        categoryTag.endIndex = categoryTag.startIndex!! + categoryTag.name!!.length

                        stringBuilderContent.append(categoryTag.name + holderText)
                    }
                }
                contentMap[categories[position].name!!] = categoryTag
            }
        }

        val categoryContentLength = stringBuilderContent.length
        for (content in contentMap) {
            stringBuilderContent.setSpan(CategoryClickSpan(contentMap, content.value.name!!), content.value.startIndex!!,
                    content.value.endIndex!!, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        stringBuilderContent.append(getString(R.string.string_category_label))
        stringBuilderContent.setSpan(ForegroundColorSpan(requireContext().resources.getColor(R.color.color_normal_838791)),
                categoryContentLength, stringBuilderContent.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        mUserTagTextView.text = stringBuilderContent
        mUserTagTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    internal inner class CategoryClickSpan(private val contentTagsMap: HashMap<String, CategoriesTag>,
                                           private val clickLinkText: String) : ClickableSpan() {

        override fun onClick(widget: View) {
            var categoriesTag = contentTagsMap[clickLinkText]
            categoriesTag?.let {
                requireContext().getSpider().userProfileCategoryClickEvent(it.id!!)
                requireContext().startActivity(it.redirect_url)
            }
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
            ds.color = requireContext().resources.getColor(R.color.color_normal_b6b6b6)
        }


    }

    override fun userTagOnClickListener(position: Int, url: String) {
        requireActivity().startActivity(url)
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val totalScrollRange = appBarLayout.totalScrollRange
        val percentage = Math.abs(verticalOffset).toFloat() / totalScrollRange
        toolbarTitle.setTextColor(ViewHelper.getColorWithAlpha(percentage, Color.WHITE))

        Math.abs(verticalOffset * 1.0f) / appBarLayout.totalScrollRange
        headerLayout.alpha = 1 - Math.abs(verticalOffset).toFloat() / totalScrollRange
    }

    override fun showMessage(message: String) {
        requireActivity().showLongToast(message)
    }

    private fun initData() {
        profilePresenter.requestApi(userId, isMine)
    }

    override fun onResume() {
        super.onResume()
        appBar.addOnOffsetChangedListener(this)
        showProfileBadge()
    }

    override fun onPause() {
        super.onPause()
        appBar.removeOnOffsetChangedListener(this)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        FishInjection.inject(this)
        profilePresenter.attachView(this)
    }

    override fun onDetach() {
        super.onDetach()
        profilePresenter.detachView(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        disposables.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EDIT_PROFILE && resultCode == Activity.RESULT_OK) {
            initData()
        } else if (requestCode == REQUEST_LOGIN && resultCode == Activity.RESULT_OK) {
            if (accountManager.account()?.userId.toString() != userId) {
                followBtn.callOnClick()
            } else {
                initData()
            }
        } else if (requestCode == REQUEST_CODE_TOPIC_CATEGORY) {
            initData()
        }
    }

    private fun initMagicIndicator(userAccount: UserAccount) {
        viewPager.adapter = ProfileViewPageAdapter(requireContext(), childFragmentManager, arrayListOf(getString(R.string.string_dynamic_label),
                getString(R.string.string_up_video_label)), userId, isMine)

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                // 也需要判断当前是否有值，没有就不显示
//                if (position == 0 && isEmptyList) {
                if (position == 0) {
                    listOrGridRL.visibility = View.VISIBLE
                } else {
                    listOrGridRL.visibility = View.GONE
                }
            }
        })

        val commonNavigator7 = CommonNavigator(requireContext())
        commonNavigator7.scrollPivotX = 0.65f
        commonNavigator7.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return viewPager.adapter!!.count
            }

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                val simplePagerTitleView = ColorFlipPagerTitleView(context)
                simplePagerTitleView.text = viewPager.adapter!!.getPageTitle(index)
                simplePagerTitleView.textSize = 14f
                simplePagerTitleView.normalColor = context.getColor1(R.color.color_normal_b6b6b6)
                simplePagerTitleView.selectedColor = context.getColor1(R.color.white)
                simplePagerTitleView.setOnClickListener { viewPager.currentItem = index }
                return simplePagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator {
                val indicator = com.shuashuakan.android.modules.widget.GradualLinePagerIndicator(context)
                indicator.mode = LinePagerIndicator.MODE_EXACTLY
                indicator.lineHeight = UIUtil.dip2px(context, 3.0).toFloat()
                indicator.lineWidth = UIUtil.dip2px(context, 28.0).toFloat()
                indicator.roundRadius = UIUtil.dip2px(context, 3.0).toFloat()
                indicator.yOffset = 12f
                indicator.startInterpolator = AccelerateInterpolator()
                indicator.endInterpolator = DecelerateInterpolator(2.0f)
                indicator.setColors(context.getColor1(R.color.color_ffef30), context.getColor1(R.color.color_normal_59ff5a))
                return indicator
            }
        }
        tabLayout.navigator = commonNavigator7
        ViewPagerHelper.bind(tabLayout, viewPager)
    }

    fun updateMine() {
        if (accountManager.hasAccount()) {
            userId = accountManager.account()?.userId.toString()
            isMine = true
            firstLoad = true
            initView()
            initData()
        }
    }

    private fun showProfileBadge() {
        if (appConfig.isShowNewHomePage() && isMine) {
            toolbarMessage.visibility = View.VISIBLE
            //是否展示小红点
            toolbarMessageCount.visibility = if (badgeManager.isShowBadge()) View.VISIBLE else View.GONE
        } else {
            toolbarMessage.visibility = View.GONE
            toolbarMessageCount.visibility = View.GONE
        }
    }

    private fun onFollow() {
        if (followBtn.isFollow) {
            val cancelFollow = {
                apiService.cancelFollow(userId).applySchedulers().subscribeApi(onNext = {
                    if (it.result.isSuccess) {
                        FollowCacheManager.putFollowUserToCache(userId, false)
                        followBtn.setFollowStatus(false, userAccount?.is_fans)
                        if (userAccount != null && userAccount!!.fansCount != null && userAccount!!.fansCount!! > 0) {
                            userAccount!!.fansCount = userAccount!!.fansCount!! - 1
                            fansView.text = userAccount!!.fansCount.toString()
                            Timber.e(getString(R.string.string_un_follow_success))
                            RxBus.get().post(FocusListRefreshEvent())
                            spider.userFollowEvent(requireContext(), userId,
                                    SpiderAction.VideoPlaySource.PERSONA_PAGE.source, false)
                        }
                        RxBus.get().post(FeedFollowChangeEvent(userId, false))
                    } else {
                        requireContext().showLongToast(getString(R.string.string_un_follow_error))
                    }
                }, onApiError = {
                    if (it is ApiError.HttpError) {
                        requireContext().showLongToast(it.displayMsg)
                    } else {
                        requireContext().showLongToast(getString(R.string.string_un_follow_error))
                    }
                }).addTo(disposables)
            }
            activity?.showCancelFollowDialog(userAccount?.nickName, cancelFollow)
        } else {


            val disposable = apiService.createFollow(userId).applySchedulers().subscribeApi(onNext = {
                if (it.result.isSuccess) {
                    FollowCacheManager.putFollowUserToCache(userId, true)
                    followBtn.setFollowStatus(true, userAccount?.is_fans)
                    if (userAccount != null && userAccount!!.fansCount != null) {
                        userAccount!!.fansCount = userAccount!!.fansCount!! + 1
                        fansView.text = userAccount!!.fansCount.toString()
                        RxBus.get().post(FocusListRefreshEvent())
                    }
                    RxBus.get().post(FeedFollowChangeEvent(userId, true))
                    spider.userFollowEvent(requireContext(), userId,
                            SpiderAction.VideoPlaySource.PERSONA_PAGE.source, true)
                } else {
                    requireContext().showLongToast(getString(R.string.string_attention_error))
                }
            }, onApiError = {
                if (it is ApiError.HttpError) {
                    requireContext().showLongToast(it.displayMsg)
                } else {
                    requireContext().showLongToast(getString(R.string.string_attention_error))
                }
            })
            disposables.add(disposable)
        }

    }

}
