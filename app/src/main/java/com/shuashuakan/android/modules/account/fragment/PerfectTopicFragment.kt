package com.shuashuakan.android.modules.account.fragment

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.shuashuakan.android.R
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.event.FinishWelcome
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.account.activity.PerfectProfileActivity
import com.shuashuakan.android.modules.account.ProfileSource
import com.shuashuakan.android.modules.account.adapter.PerfectTopicSubAdapter
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.modules.HOME_PAGE
import com.shuashuakan.android.ui.ProgressDialog
import com.shuashuakan.android.ui.base.FishFragment
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.utils.*
import javax.inject.Inject

/**
 * @author hushiguang
 * @since 2019-05-08.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class PerfectTopicFragment : FishFragment(), View.OnClickListener {
    private val dialog by lazy {
        return@lazy ProgressDialog.progressDialog(requireActivity(), getString(R.string.string_data_saveing))
    }

    var mRootView: View? = null
    var mNextBtn: TextView? = null
    var mTopicRecyclerView: RecyclerView? = null
    private var profileCallback: ProfileSource.IProfilePerfectCallback? = null
    private var mProfileTopicAdapter: PerfectTopicSubAdapter? = null

    @Inject
    lateinit var spider: Spider

    @Inject
    lateinit var apiService: ApiService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = LayoutInflater.from(container?.context).inflate(R.layout.fragment_profile_perfect_topic, container, false)
        initView()
        initListener()
        return mRootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        spider.manuallyEvent(SpiderEventNames.GUIDE_INTEREST_PAGE_EXPOSURE)
                .put("userID", requireContext().getUserId())
                .track()
        profileCallback = (activity as PerfectProfileActivity)
        mProfileTopicAdapter = PerfectTopicSubAdapter(profileCallback?.getTopicListData()!!) { selectSize ->
            mNextBtn?.text = if (selectSize > 0) getString(R.string.string_finish_label) else getString(R.string.string_sub_topic_with_key)
        }
        mTopicRecyclerView?.layoutManager = GridLayoutManager(requireContext(), 3)
        mTopicRecyclerView?.adapter = mProfileTopicAdapter
    }

    private fun initView() {
        mTopicRecyclerView = mRootView?.findViewById(R.id.profile_topic_list)
        mNextBtn = mRootView?.findViewById(R.id.profile_next)
    }

    private fun initListener() {
        mNextBtn?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.profile_next -> {
                dialog.show()
                onNext()
            }
        }
    }

    private fun onNext() {
        if (mProfileTopicAdapter?.selectIds!!.size <= 0) {
            for (c in profileCallback?.getTopicListData()!!) {
                mProfileTopicAdapter?.selectIds!!.add(c.id.toString())
            }
            mProfileTopicAdapter?.notifyDataSetChanged()
            requireContext().getSpider()
                    .manuallyEvent(SpiderEventNames.GUIDE_PRIVACY_INFORMATION_SUBSCRIBE_BUTTON_CLICK)
                    .put("count", mProfileTopicAdapter?.selectIds?.size.toString())
                    .track()
        } else {
            requireContext().getSpider()
                    .manuallyEvent(SpiderEventNames.GUIDE_PERFECT_INFORMATION_FINISH_BUTTON_CLICK)
                    .put("count", mProfileTopicAdapter?.selectIds?.size.toString())
                    .track()
        }



        mProfileTopicAdapter?.let { adapter ->
            val selectValues = adapter.selectIds
            apiService.batchSubscirbe(selectValues.toString()).applySchedulers().subscribeApi(onNext = {
                if (it.result.isSuccess) {
                    dialog.dismiss()
                    // 打点兴趣数据
                    spider.manuallyEvent(SpiderEventNames.HOME_PAGE_INTEREST)
                            .put("userID", activity?.getUserId() ?: "")
                            .put("categoryIDs", selectValues.toString())
                            .track()
                    goHome()


                } else {
                    dialog.dismiss()
                    requireContext().showLongToast(getString(R.string.string_operating_error))
                }
            }, onApiError = {
                dialog.dismiss()
                if (it is ApiError.HttpError) {
                    requireContext().showLongToast(it.displayMsg)
                } else {
                    requireContext().showLongToast(getString(R.string.string_operating_error))
                }
            })
        }
    }


    private fun goHome() {
        RxBus.get().post(FinishWelcome())
        requireContext().startActivity(HOME_PAGE)
        requireActivity().finish()
    }
}