package com.shuashuakan.android.modules.home

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.widget.Toast
import cn.jpush.android.api.JPluginPlatformInterface
import com.gyf.barlibrary.ImmersionBar
import com.luck.picture.lib.permissions.RxPermissions
import com.shuashuakan.android.ApplicationMonitor
import com.shuashuakan.android.DuckApplication
import com.shuashuakan.android.R
import com.shuashuakan.android.commons.cache.Storage
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.account.UserAccount
import com.shuashuakan.android.event.LoginOutEvent
import com.shuashuakan.android.event.LoginSuccessEvent
import com.shuashuakan.android.event.SpecialVideoPageToolbarAnimationEvent
import com.shuashuakan.android.modules.ACCOUNT_PAGE
import com.shuashuakan.android.modules.JOIN_PAGE
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.modules.discovery.DiscoveryActivity
import com.shuashuakan.android.modules.home.vm.HomeViewModel
import com.shuashuakan.android.modules.message.MessageActivity
import com.shuashuakan.android.modules.message.badage.BadgeClearNonSystemEvent
import com.shuashuakan.android.modules.message.badage.BadgeEvent
import com.shuashuakan.android.modules.message.badage.BadgeManager
import com.shuashuakan.android.modules.message.badage.BadgeViewChangeUtil
import com.shuashuakan.android.modules.player.VideoPlayerManager
import com.shuashuakan.android.modules.profile.UserProfileActivity
import com.shuashuakan.android.modules.profile.fragment.ProfileFragment
import com.shuashuakan.android.modules.publisher.PermissionRequestFragment
import com.shuashuakan.android.service.PullService
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.utils.*
import com.umeng.socialize.UMShareAPI
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_home_special.*
import me.twocities.linker.annotations.Link
import javax.inject.Inject

@Link("ssr://home")
class HomeActivity : FishActivity(), HomeNavigatorAdapter.OnHomePageListener, ScreenshotObserver.OnScreenshotListener {

    @Inject
    lateinit var mAccountManager: AccountManager
    @Inject
    lateinit var mStorage: Storage
    @Inject
    lateinit var mAppConfig: AppConfig
    @Inject
    lateinit var badgeManager: BadgeManager

    private val arrayOfPermissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE)

    private lateinit var pushInterface: JPluginPlatformInterface
    private val compositeDisposable = CompositeDisposable()
    private var mIsAnimationEvent = true
    private var mExitTimeMillis: Long = 0
    private lateinit var mAdapter: HomeNavigatorAdapter
    private lateinit var mHomeViewModel: HomeViewModel
    private var mCurrentPosition = PAGE_RECOMMEND
    private var mScreenShortObserver: ScreenshotObserver? = null

    companion object {
        private const val PAGE_RECOMMEND = 0
        private const val PAGE_TIMELINE = 1
        const val TAG = "HomeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        mHomeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        initView()
        requestPermission()
        mAdapter = HomeNavigatorAdapter(this, supportFragmentManager,
                mAppConfig.istHomePageFromH5(), this)
        mAdapter.attach(home_indicator, PAGE_RECOMMEND)
        initListener()
        initObservable()
    }

    override fun onResume() {
        super.onResume()
        refreshAvatarAndBadge()
    }

    private fun refreshTimelineBadge() {
        if (mCurrentPosition == PAGE_RECOMMEND) {
            mHomeViewModel.getTimeLineBadge()
        }
    }

    private fun refreshAvatarAndBadge() {
        if (mAccountManager.hasAccount()) {
            iv_home_avatar_badage_anim.alpha = if (badgeManager.isOpenProfile) 0f else 1f
            tv_home_avatar_badage.alpha = if (badgeManager.isShowBadge()) 1f else 0f

            val cacheOf = mStorage.userCache.cacheOf<UserAccount>()
            val account = cacheOf.get(ProfileFragment.ACCOUNT_CACHE_KEY).orNull()
            if (account != null && !account.avatar.isNullOrEmpty()) {
                val size = this.dip(24)
                loadImage(iv_home_user_avatar, account.avatar, size, size)
            }
        } else {
            BadgeViewChangeUtil.hideAll(tv_home_avatar_badage, iv_home_avatar_badage_anim)
            iv_home_user_avatar.setActualImageResource(R.drawable.ic_avatar_special_default)
        }
    }

    override fun onStart() {
        super.onStart()
        pushInterface.onStart(this)
    }

    override fun onStop() {
        super.onStop()
        pushInterface.onStop(this)
    }


    override fun onDestroy() {
        super.onDestroy()
        mScreenShortObserver?.unSubscript()
        compositeDisposable.clear()
        ImmersionBar.with(this).destroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                JPluginPlatformInterface.JPLUGIN_REQUEST_CODE ->
                    pushInterface.onActivityResult(this, requestCode, resultCode, data)
            }
        }
    }

    override fun onPageSelected(position: Int) {
        mCurrentPosition = position
        when (position) {
            PAGE_RECOMMEND -> {
            }
            PAGE_TIMELINE -> {
                spider.followTimeLineEvent(this, SpiderEventNames.FOLLOW_TIMELINE_PAGETAB_CLICK)
            }
        }
    }

    private fun initObservable() {
        mHomeViewModel.mTimelineBadgeLiveData.observe(this, Observer<Boolean> {
            it?.let { showTimelineBadge ->
                mAdapter.timeLineBadge = showTimelineBadge
                mAdapter.notifyDataSetChanged()
            }
        })

        mHomeViewModel.mApplicationStateLiveData.observe(this,
                Observer<ApplicationMonitor.ApplicationState> {
                    it?.let { applicationState ->
                        when (applicationState) {
                            ApplicationMonitor.ApplicationState.FOREGROUND -> {
                                refreshTimelineBadge()
                            }
                        }
                    }
                })
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onScreenshot(path: String?) {
        spider.programEvent(SpiderEventNames.Program.SCREEN_SHOT).track()
    }

    @SuppressLint("CheckResult")
    private fun requestPermission() {
        RxPermissions(this)
                .requestEach(*arrayOfPermissions)
                .subscribe { permission ->
                    when {
                        permission.granted -> {
                            //用户同意该权限
                            if (permission.name == "android.permission.ACCESS_FINE_LOCATION") {
                                val locationController = daggerComponent().locationController()
                                locationController.startLocation()
                            } else if (permission.name == Manifest.permission.READ_EXTERNAL_STORAGE) {
                                mScreenShortObserver = ScreenshotObserver(Handler(mainLooper), this)
                                mScreenShortObserver?.subscript(this)
                            }
                        }
                        permission.shouldShowRequestPermissionRationale -> {
                            //用户拒绝，没有选中不再询问
                            //Timber.e("==拒绝====${permission.name}")
                        }
                        else -> {
                            //用户拒绝，并选中不再询问
                        }
                    }
                }
    }

    private fun initView() {
        ImmersionBar.with(this).init()
        setViewMarginTop(this, home_navigation_rl, 0)
    }

    private fun initListener() {
        pushInterface = JPluginPlatformInterface(this.applicationContext)
        iv_home_publish.noDoubleClick {
            if (!mAccountManager.hasAccount()) {
                LoginActivity.launch(this)
            } else {
                if (mAppConfig.isShowCreateFeed()) {
                    goPublish()
                } else {
                    startActivity(JOIN_PAGE)
                }
            }
        }
        iv_home_discovery.noDoubleClick {
            spider.manuallyEvent(SpiderEventNames.EXPLORE_EXTRANCE_CLICK).track()
            startActivity(Intent(this, DiscoveryActivity::class.java))
        }
        iv_home_user_avatar.noDoubleClick {
            if (!mAccountManager.hasAccount()) {
                startActivity(ACCOUNT_PAGE)
            } else {
                if (badgeManager.isOpenProfile) {
                    startActivity(Intent(this, UserProfileActivity::class.java)
                            .putExtra("id", mAccountManager.account()!!.userId.toString()))
                } else {
                    spider.manuallyEvent(SpiderEventNames.UNREAD_MESSAGE_POP_CLICK).track()
                    startActivity(Intent(this, MessageActivity::class.java))
                }

            }
        }
        RxBus.get().toFlowable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when (it) {
                        is LoginOutEvent -> {
                            mHomeViewModel.getTimeLineBadge()
                            mAdapter.selectPage(PAGE_RECOMMEND)
                        }
                        is LoginSuccessEvent -> {
                            mHomeViewModel.getTimeLineBadge()
                            if (it.source == TAG) {
                                mAdapter.selectPage(PAGE_TIMELINE)
                            }
                        }
                        is SpecialVideoPageToolbarAnimationEvent -> {
                            mIsAnimationEvent = it.animationEvent
                        }

                        is BadgeEvent -> {
                            if (!badgeManager.isShowBadge()) {
                                BadgeViewChangeUtil.hideAll(tv_home_avatar_badage, iv_home_avatar_badage_anim)
                                return@subscribe
                            }
                            if (!it.enableAnim) {
                                BadgeViewChangeUtil.hidePointWithAnim(tv_home_avatar_badage, iv_home_avatar_badage_anim)
                            } else {
                                spider.manuallyEvent(SpiderEventNames.UNREAD_MESSAGE_POP_EXPOSURE).track()
                                BadgeViewChangeUtil.showPointWithAnim(tv_home_avatar_badage, iv_home_avatar_badage_anim)
                            }
                        }
                        is BadgeClearNonSystemEvent -> {
                            if (!badgeManager.isShowBadge()) {
                                BadgeViewChangeUtil.hideAll(tv_home_avatar_badage, iv_home_avatar_badage_anim)
                            }
                        }
                    }
                }.addTo(compositeDisposable)
    }


    private fun back() {
        if ((System.currentTimeMillis() - mExitTimeMillis) > 2000) {
            Toast.makeText(this, getString(R.string.string_back_home_comfire), Toast.LENGTH_SHORT).show()
            mExitTimeMillis = System.currentTimeMillis()
        } else {
            VideoPlayerManager.instance().releaseAllVideoPlayer()
            spider.programEvent(SpiderEventNames.Program.APP_END).track()

            iv_home_user_avatar.postDelayed(Runnable {
                val count = (application as DuckApplication).applicationMonitor.getActivityCount()
                if (count <= 0) {
                    System.exit(0)
                }
            }, 2000)
            finish()
        }
    }

    private fun goPublish() {
        if (PullService.canUpload()) {
            PermissionRequestFragment.create(PullService.UploadEntity.TYPE_ADD_HOME_VIDEO)
                    .show(this.supportFragmentManager, "home")
        } else {
            showShortToast(getString(R.string.string_publish_wait_edit))
        }
    }
}
