package com.shuashuakan.android.modules.account.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.shuashuakan.android.R
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.account.activity.PerfectProfileActivity
import com.shuashuakan.android.modules.account.ProfileSource
import com.shuashuakan.android.modules.account.adapter.PerfectAgeAdapter
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.ui.ProgressDialog
import com.shuashuakan.android.ui.base.FishFragment
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.modules.track.ClickAction
import com.shuashuakan.android.modules.track.trackProfileIncrementOnce
import com.shuashuakan.android.utils.*
import javax.inject.Inject

/**
 * @author hushiguang
 * @since 2019-05-08.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class PerfectGenderAgeFragment : FishFragment(), View.OnClickListener {

    private val dialog by lazy {
        return@lazy ProgressDialog.progressDialog(requireActivity(), getString(R.string.string_data_saveing))
    }

    var mRootView: View? = null
    var maleImage: AppCompatImageView? = null
    var femaleImage: AppCompatImageView? = null
    var mAgeRecyclerView: RecyclerView? = null
    var mNextBtn: TextView? = null
    var maleSource: ProfileSource = ProfileSource.NONE

    @Inject
    lateinit var apiService: ApiService

    private var mProfileAgeAdapter: PerfectAgeAdapter? = null
    private var profileCallback: ProfileSource.IProfilePerfectCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = LayoutInflater.from(container?.context).inflate(R.layout.fragment_profile_perfect_gender_age_info, container, false)
        initView()
        initListener()
        return mRootView
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        requireContext().getSpider().manuallyEvent(SpiderEventNames.GUIDE_SEX_PAGE_EXPOSURE)
                .put("userID", requireContext().getUserId())
                .track()

        profileCallback = (activity as PerfectProfileActivity)
        mProfileAgeAdapter = PerfectAgeAdapter(getAgeListValue()) { selectValue ->
            mProfileAgeAdapter?.selectTabValue = selectValue
            checkNextEnable()
        }
        mAgeRecyclerView?.layoutManager = GridLayoutManager(requireContext(), 3)
        mAgeRecyclerView?.adapter = mProfileAgeAdapter


    }


    private fun initView() {
        maleImage = mRootView?.findViewById(R.id.profile_male_image)
        femaleImage = mRootView?.findViewById(R.id.profile_female_image)
        mAgeRecyclerView = mRootView?.findViewById(R.id.profile_age_list)
        mNextBtn = mRootView?.findViewById(R.id.profile_next)
    }

    private fun initListener() {
        maleImage?.setOnClickListener(this)
        femaleImage?.setOnClickListener(this)
        mNextBtn?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.profile_male_image -> {
                if (maleSource != ProfileSource.MALE) {
                    animMale(maleImage, femaleImage)
                    maleSource = ProfileSource.MALE
                }
                checkNextEnable()
            }
            R.id.profile_female_image -> {
                if (maleSource != ProfileSource.FEMALE) {
                    animMale(femaleImage, maleImage)
                    maleSource = ProfileSource.FEMALE
                }
                checkNextEnable()
            }
            R.id.profile_next -> {
                requireContext().getSpider().manuallyEvent(SpiderEventNames.GUIDE_PAGE_NEXT_STEP)
                        .put("userID", requireContext().getUserId())
                        .put("page", "Sex")
                        .track()
                dialog.show()
                goNextStep()
            }
        }
    }

    private fun checkNextEnable() {
        mNextBtn?.isEnabled =
                maleSource != ProfileSource.NONE
                        && mProfileAgeAdapter?.selectTabValue != null
    }

    //0 nan 1 nv
    private fun goNextStep() {
        apiService.editUserInfo(if (maleSource == ProfileSource.MALE) 0 else 1,
                mProfileAgeAdapter?.selectTabValue, null, null, null, null)
                .applySchedulers()
                .subscribeApi(
                        onNext = {
                            if (it.result.isSuccess) {
                                //requireActivity().showLongToast(getString(R.string.info_save_success))
                                dialog.dismiss()
                                trackProfileIncrementOnce(arrayListOf(ClickAction.SAVE_PROFILE_CLICK to 1, ClickAction.SUCCESS to 1))
                                profileCallback?.onNext()
                            } else {
                                dialog.dismiss()
                                requireActivity().showLongToast(getString(R.string.info_save_fail))
                            }
                        }, onApiError = {
                    val httpError = it as? ApiError.HttpError
                    if (httpError != null) {
                        dialog.dismiss()
                        requireActivity().showLongToast(httpError.displayMsg)
                    } else {
                        dialog.dismiss()
                        requireActivity().showLongToast(getString(R.string.info_save_fail))
                    }
                })
    }

    private fun getAgeListValue(): ArrayList<String> {
        val ageValue = ArrayList<String>()
        ageValue.add(getString(R.string.string_00_after))
        ageValue.add(getString(R.string.string_95_after))
        ageValue.add(getString(R.string.string_90_after))
        ageValue.add(getString(R.string.string_80_after))
        ageValue.add(getString(R.string.string_70_after))
        ageValue.add(getString(R.string.string_other_after))
        return ageValue
    }

    private fun animMale(selectorView: View?, normalView: View?) {
        val scaleXWithSelector = ObjectAnimator.ofFloat(selectorView, "scaleX", 1.0f, 1.2f)
        val scaleYWithSelector = ObjectAnimator.ofFloat(selectorView, "scaleY", 1.0f, 1.2f)
        val alphaWithSelector = ObjectAnimator.ofFloat(selectorView, "alpha", 0.5f, 1.0f)
        val scaleXWithNormalView = ObjectAnimator.ofFloat(normalView, "scaleX", 1.2f, 1.0f)
        val scaleYWithNormalView = ObjectAnimator.ofFloat(normalView, "scaleY", 1.2f, 1.0f)
        val alphaWithNormalView = ObjectAnimator.ofFloat(normalView, "alpha", 1f, 0.5f)
        val animatorSet = AnimatorSet()
        if (maleSource == ProfileSource.NONE) {
            animatorSet.playTogether(scaleXWithSelector, scaleYWithSelector, alphaWithSelector)
        } else {
            animatorSet.playTogether(scaleXWithSelector, scaleYWithSelector, alphaWithSelector, scaleXWithNormalView,
                    scaleYWithNormalView, alphaWithNormalView)
        }
        animatorSet.duration = 200
        animatorSet.start()
    }


}