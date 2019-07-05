package com.shuashuakan.android.modules.account.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.gyf.barlibrary.ImmersionBar
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.model.account.ChannelModel
import com.shuashuakan.android.modules.account.fragment.PerfectGenderAgeFragment
import com.shuashuakan.android.modules.account.fragment.PerfectNickNameFragment
import com.shuashuakan.android.modules.account.fragment.PerfectTopicFragment
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.modules.HOME_PAGE
import com.shuashuakan.android.modules.account.ProfileSource
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.utils.getSpider
import com.shuashuakan.android.utils.getUserId
import com.shuashuakan.android.utils.startActivity
import kotlinx.android.synthetic.main.activity_profile_perfect.*

/**
 * @author:qingfei.chen
 * @Date:2019/5/6  下午6:19
 */
class PerfectProfileActivity : FishActivity(), ProfileSource.IProfilePerfectCallback {

    var avatar: String? = null
    var name: String? = null
    var pageList: ArrayList<String>? = null
    var channelModelListData: ArrayList<ChannelModel>? = null
    var mCurrentPageIndex = 0
    var mTotalPage = 0

    companion object {
        const val EXTRA_AVATAR = "extra_avatar"
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_PAGE_LIST = "extra_page_list"
        const val EXTRA_CHANNEL_LIST = "extra_chnanel_list"

        const val PAGE_NAME_AVATAR = "NAME_AVATAR"
        const val PAGE_GENDER_AGE = "GENDER_AGE"
        const val RECOMMEND_CHANNEL = "RECOMMEND_CHANNEL"

        fun launch(context: Context, avatar: String?, name: String?, pageList: ArrayList<String>, channelList: ArrayList<ChannelModel>) {
            context.startActivity(Intent(context, PerfectProfileActivity::class.java)
                    .putExtra(EXTRA_AVATAR, avatar ?: "")
                    .putExtra(EXTRA_NAME, name ?: "")
                    .putExtra(EXTRA_PAGE_LIST, pageList)
                    .putExtra(EXTRA_CHANNEL_LIST, channelList))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this).init()
        setContentView(R.layout.activity_profile_perfect)

        avatar = intent.getStringExtra(EXTRA_AVATAR)
        name = intent.getStringExtra(EXTRA_NAME)
        pageList = intent.getStringArrayListExtra(EXTRA_PAGE_LIST)
        channelModelListData = intent.getParcelableArrayListExtra(EXTRA_CHANNEL_LIST)

        pageList?.takeIf { it.isNotEmpty() }?.let {
            mTotalPage = it.size
            setPageIndex()
            showFragment()
        } ?: run {
            finish()
            return
        }

        tv_profile_title_skip.setOnClickListener {
            performSkip()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ImmersionBar.with(this).destroy()
    }

    private fun showFragment() {

        val page = pageList?.getOrNull(mCurrentPageIndex) ?: return
        var fragment: Fragment? = null
        val bundle = Bundle()
        when (page) {
            PAGE_NAME_AVATAR -> {
                fragment = PerfectNickNameFragment()
                bundle.putString(PerfectNickNameFragment.PERFECT_AVATAR, avatar)
                bundle.putString(PerfectNickNameFragment.PERFECT_NICK_NAME, name)
            }
            PAGE_GENDER_AGE -> {
                fragment = PerfectGenderAgeFragment()
            }
            RECOMMEND_CHANNEL -> {
                fragment = PerfectTopicFragment()
                fragment.arguments = bundle
            }
        }
        fragment?.let {
            fragment.arguments = bundle
            supportFragmentManager.beginTransaction()
                    .replace(R.id.profile_fragment_container, it).commit()
        }
    }

    private fun setPageIndex() {
        tv_profile_perfect_pageindex.text = "${mCurrentPageIndex + 1}"
        tv_profile_perfect_page_total.text = "/${mTotalPage}"
    }

    private fun performSkip() {
        logSkipWrite()
        onChangeShowContent()
    }


    override fun onNext() {
        onChangeShowContent()
    }

    override fun getTopicListData(): List<ChannelModel>? {
        return channelModelListData
    }


    private fun onChangeShowContent() {

        mCurrentPageIndex++
        if (mCurrentPageIndex == mTotalPage) {
            goHome()
            mCurrentPageIndex = mTotalPage - 1
            return
        }
        setPageIndex()
        showFragment()
    }


    private fun logSkipWrite() {
        when (mCurrentPageIndex) {
            0 -> {
                getSpider().manuallyEvent(SpiderEventNames.GUIDE_PAGE_LEAP)
                        .put("userID", getUserId()).put("page", "NickName").track()
            }

            1 -> {
                getSpider().manuallyEvent(SpiderEventNames.GUIDE_PAGE_LEAP)
                        .put("userID", getUserId())
                        .put("page", "Sex")
                        .track()
            }

            2 -> {
                getSpider().manuallyEvent(SpiderEventNames.GUIDE_PAGE_NEXT_STEP)
                        .put("userID", getUserId())
                        .put("page", "Interest")
                        .track()
            }
        }
    }

    private fun goHome() {
        startActivity(HOME_PAGE)
        finish()
    }
}