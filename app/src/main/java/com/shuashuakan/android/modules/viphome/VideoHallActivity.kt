package com.shuashuakan.android.modules.viphome

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.gyf.barlibrary.ImmersionBar
import com.shuashuakan.android.R
import com.shuashuakan.android.enums.ChainFeedSource
import com.shuashuakan.android.modules.FeedTransportManager
import com.shuashuakan.android.modules.player.activity.VideoPlayActivity
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.ui.ProgressDialog
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.utils.getSpider
import kotlinx.android.synthetic.main.activity_video_hall.*
import me.twocities.linker.annotations.Link

/**
 * VIP 放映厅
 */
@Link("ssr://feed/rookie")
class VideoHallActivity : FishActivity(), View.OnClickListener, TextWatcher {


    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, VideoHallActivity::class.java))
        }
    }

    private val mProgressDialog by lazy {
        return@lazy ProgressDialog.progressDialog(this, getString(R.string.string_loading))
    }
    private lateinit var mVideoHallViewModel: VideoHallViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this).init()
        ImmersionBar.with(this).navigationBarColor(R.color.transparent).init()
        setContentView(R.layout.activity_video_hall)
        mVideoHallViewModel = VideoHallViewModel(application)
        initListener()
        initObserver()
    }

    override fun onDestroy() {
        super.onDestroy()
        ImmersionBar.with(this).destroy()
    }

    override fun onClick(v: View) {
        when (v) {
            tv_activity_video_hall_open -> {
                performOpen()
            }
            tv_activity_video_hall_open_help -> {
                getSpider().manuallyEvent(SpiderEventNames.VipRoom.ROOM_KEY_HELP_CLICK).track()
                VideoHallKeyHelpDialog.show(this)
            }

            iv_video_hall_back -> {
                this.finish()
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        tv_activity_video_hall_open.isEnabled = s?.isNotEmpty() == true
    }

    private fun performOpen() {
        getSpider().manuallyEvent(SpiderEventNames.VipRoom.ROOM_OPEN_CLICK).track()
        if (et_activity_video_hall_key.text.isNullOrEmpty()) {
            return
        }

        mProgressDialog.show()
        mVideoHallViewModel.getRookieVideoList(et_activity_video_hall_key.text.toString())
    }

    private fun initObserver() {
        mVideoHallViewModel.mChainsListParamLiveData.observe(this, Observer {
            mProgressDialog.dismiss()
            it?.let { chainsListParams ->
                this.finish()
                FeedTransportManager.message = mVideoHallViewModel.mMessage
                startActivity(VideoPlayActivity.create(this, chainsListParams, ChainFeedSource.VIP_HOME.source))
            }
                    ?: Toast.makeText(this, getString(R.string.strng_video_hall_key_error), Toast.LENGTH_SHORT).show()
        })
    }

    private fun initListener() {
        tv_activity_video_hall_open.setOnClickListener(this)
        tv_activity_video_hall_open_help.setOnClickListener(this)
        tv_activity_video_hall_sofo.setOnClickListener(this)
        iv_video_hall_back.setOnClickListener(this)
    }
}
