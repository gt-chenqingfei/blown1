package com.shuashuakan.android.modules.discovery

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.gyf.barlibrary.ImmersionBar
import com.shuashuakan.android.R
import com.shuashuakan.android.modules.SSR_UP_STAR_RANK
import com.shuashuakan.android.modules.discovery.fragment.CallBackToActivity
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.modules.discovery.fragment.SubRankingListFragment
import com.shuashuakan.android.modules.topic.bindLinkParams
import com.shuashuakan.android.utils.*
import me.twocities.linker.annotations.Link
import me.twocities.linker.annotations.LinkQuery

/**
 * 发现页
 *
 * Author: guozhao
 * Date: 2019/6/23
 */
@Link(SSR_UP_STAR_RANK)
class UpStarRankingListActivity : FishActivity(), CallBackToActivity {


    @LinkQuery("categoryId")
    @JvmField
    var categoryId: String? = null

    @LinkQuery("channelId")
    @JvmField
    var channelId: String? = null

    private val backIv by bindView<ImageView>(R.id.back_iv)
    private val rule by bindView<TextView>(R.id.rule_tv)
    private val title by bindView<TextView>(R.id.title)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this).init()
        bindLinkParams()
        setContentView(R.layout.activity_upstar_ranking)
        supportFragmentManager.beginTransaction().replace(R.id.home_container, SubRankingListFragment.create(resources.getString(R.string.up_star_rank), channelId, categoryId)).commit()
        initListener()
        spider.pageTracer().reportPageCreated(this)
        this.getSpider().userChannelUpUserStarListExposureEvent(this, channelId, categoryId)
    }

    private fun initListener() {
        backIv.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ImmersionBar.with(this).destroy()
    }

    override fun sendDataToActivity(url: String, title: String) {
        rule.noDoubleClick {
            if (url.isNotEmpty())
                startActivity(url)
        }
        this.title.text = title
    }
}
