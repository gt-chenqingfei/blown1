package com.shuashuakan.android.modules.player.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.ViewDragHelper
import com.gyf.barlibrary.ImmersionBar
import com.shuashuakan.android.ApplicationMonitor
import com.shuashuakan.android.R
import com.shuashuakan.android.config.AppConfig
import com.shuashuakan.android.modules.FeedTransportManager
import com.shuashuakan.android.modules.player.fragment.VideoListFragment
import com.shuashuakan.android.modules.publisher.chains.ChainsListIntentParam
import com.shuashuakan.android.modules.viphome.Constants
import com.shuashuakan.android.modules.widget.swipexit.BaseSwipeLayout
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.ui.base.FishFragment
import com.umeng.socialize.UMShareAPI
import me.twocities.linker.annotations.Link
import me.twocities.linker.annotations.LinkQuery
import javax.inject.Inject

//横向结构的播放页Activity
@Link("ssr://feed/master")
class VideoPlayActivity : FishActivity() {
    @LinkQuery("id")
    @JvmField
    var feedId: String? = null

    @LinkQuery("feedSource")
    @JvmField
    var feedSource: String? = null

    @LinkQuery("floor_feed_id")
    @JvmField
    var floorFeedId: String? = null

    @Inject
    lateinit var appConfig: AppConfig

    private lateinit var chainsFeedFragment: FishFragment
    private lateinit var swipe_layout: BaseSwipeLayout
//    private var intentParam: ChainsListIntentParam? = null
    var isMine: Boolean = false

    companion object {
        const val FEED_ID = "id"
        const val FEED_SOURCE = "feedSource"
        const val INTENT_PARAM = "intentParam"
        const val IS_MINE = "isMine"

        fun create(context: Context, feedId: String, source: String): Intent {
            return Intent(context, VideoPlayActivity::class.java)
                    .putExtra(FEED_ID, feedId)
                    .putExtra(FEED_SOURCE, source)
        }

        fun create(context: Context, chainsListIntentParam: ChainsListIntentParam, isMine: Boolean = false): Intent {
            FeedTransportManager.intentParam = chainsListIntentParam
            return Intent(context, VideoPlayActivity::class.java)
                    // .putExtra(INTENT_PARAM, chainsListIntentParam)
                    .putExtra(IS_MINE, isMine)
        }

        fun create(context: Context, chainsListIntentParam: ChainsListIntentParam, source: String): Intent {
            FeedTransportManager.intentParam = chainsListIntentParam
            return Intent(context, VideoPlayActivity::class.java)
                    .putExtra(FEED_SOURCE, source)
                    .putExtra(IS_MINE, false)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindLinkParams()
        ImmersionBar.with(this).init()
        ImmersionBar.with(this).navigationBarColor(R.color.transparent).init()


        setContentView(R.layout.activity_second_video_play)
        setSwipExit()

//        intentParam = FeedTransportManager.intentParam//intent.getParcelableExtra(INTENT_PARAM)
//        FeedTransportManager.intentParam = null
        isMine = intent.getBooleanExtra(IS_MINE, false)
//        getPreviousPage()
        showFragment()
    }


    private fun showFragment() {
        chainsFeedFragment = VideoListFragment.create(isMine, feedId, feedSource, FeedTransportManager.intentParam, floorFeedId)
        supportFragmentManager.beginTransaction().replace(R.id.home_container, chainsFeedFragment).commit()
    }

//    private fun getPreviousPage() {
//        if (ApplicationMonitor.mActivityStack != null && ApplicationMonitor.mActivityStack!!.size >= 2) {
//            val activity = ApplicationMonitor.mActivityStack?.get(ApplicationMonitor.mActivityStack?.size!! - 2)
//            if (feedSource == null) {
//                feedSource = if (activity.toString().contains("H5Activity")) {
//                    "h5"
//                } else {
//                    feedSource ?: intentParam?.feedSource?.source
//                }
//            }
//        }
//    }

    private fun setSwipExit() {
        swipe_layout = findViewById(R.id.swipe_layout)
        swipe_layout.setSwipeEdge(ViewDragHelper.EDGE_LEFT)
        swipe_layout.setOnFinishScroll {
            finish()
            overridePendingTransition(0, 0)
        }

    }

//  private var dispatchScroll = false

    override fun onPause() {
        super.onPause()
        appConfig.setHomePageFromH5(false)
    }

    override fun onStop() {
        super.onStop()
        appConfig.setHomePageFromH5(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        appConfig.setHomePageFromH5(false)
        Constants.IS_OPEN_VIP_ROOM = false
        ImmersionBar.with(this).destroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data)
    }


}
