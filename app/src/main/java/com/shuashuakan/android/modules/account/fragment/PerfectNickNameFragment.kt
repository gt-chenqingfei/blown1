package com.shuashuakan.android.modules.account.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.net.toUri
import androidx.os.toUri
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.shuashuakan.android.R
import com.shuashuakan.android.commons.cache.Storage
import com.shuashuakan.android.data.api.model.account.UserAccount
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.data.api.services.UploadImageService
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.account.activity.PerfectProfileActivity
import com.shuashuakan.android.modules.account.ProfileSource
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.ui.ProgressDialog
import com.shuashuakan.android.modules.profile.fragment.ProfileFragment
import com.shuashuakan.android.ui.base.FishFragment
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.modules.track.ClickAction
import com.shuashuakan.android.modules.track.trackProfileIncrementOnce
import com.shuashuakan.android.utils.*
import kotlinx.android.synthetic.main.fragment_profile_perfect_nick_avatar.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

/**
 * @author hushiguang
 * @since 2019-05-08.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
class PerfectNickNameFragment : FishFragment() {

    companion object {
        const val PERFECT_NICK_NAME = "perfect_nick_name"
        const val PERFECT_AVATAR = "perfect_avatar"
    }

    private var extraName: String? = null
    private var extraAvatar: String? = null
    private var avatarId: Long? = null

    @Inject
    lateinit var uploadService: UploadImageService
    @Inject
    lateinit var storage: Storage
    @Inject
    lateinit var apiService: ApiService
    private var profileCallback: ProfileSource.IProfilePerfectCallback? = null

    val mUploadDialog by lazy {
        return@lazy ProgressDialog.progressDialog(requireActivity(), getString(R.string.string_avatar_upload_ing))
    }

    private val mSaveDialog by lazy {
        return@lazy ProgressDialog.progressDialog(requireActivity(), getString(R.string.string_data_saveing))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LayoutInflater.from(container?.context).inflate(R.layout.fragment_profile_perfect_nick_avatar, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        profileCallback = (activity as PerfectProfileActivity)
        extraName = arguments?.getString(PERFECT_NICK_NAME)
        extraAvatar = arguments?.getString(PERFECT_AVATAR)

        initView()
        initListener()
        requireContext().getSpider().manuallyEvent(SpiderEventNames.GUIDE_NICK_NAME_PAGE_EXPOSURE)
                .put("userID", requireContext().getUserId())
                .track()
    }

    private fun initView() {
        sdv_avatar.setImageURI(extraAvatar?.toUri())
        profile_nick_avatar_next.isEnabled = profile_nickname_edit.text.toString().isEmpty() != true
    }

    private fun initListener() {
        profile_nickname_edit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(profile_nickname_error_tips != null) {
                    profile_nickname_error_tips.visibility = View.GONE
                    profile_nick_avatar_next.isEnabled = s.toString().isNotEmpty()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        sdv_avatar.noDoubleClick {
            showPickPhotoDialog()
        }

        //下一步
        profile_nick_avatar_next.noDoubleClick {
            goNextStep()
            requireContext().getSpider().manuallyEvent(SpiderEventNames.GUIDE_PAGE_NEXT_STEP)
                    .put("userID", requireContext().getUserId())
                    .put("page", "NickName")
                    .track()
        }
    }

    private fun goNextStep() {
        apiService.editUserInfo(null, null, null, profile_nickname_edit.text.toString(), null, avatarId)
                .applySchedulers()
                .subscribeApi(
                        onNext = {
                            if (it.result.isSuccess) {
                                mSaveDialog.dismiss()
                                trackProfileIncrementOnce(arrayListOf(ClickAction.SAVE_PROFILE_CLICK to 1, ClickAction.SUCCESS to 1))
                                profileCallback?.onNext()
                            } else {
                                mSaveDialog.dismiss()
                                requireActivity().showLongToast(getString(R.string.info_save_fail))
                            }
                        }, onApiError = {
                    val httpError = it as? ApiError.HttpError
                    if (httpError != null) {
                        profile_nickname_error_tips.visibility = View.VISIBLE
                        mSaveDialog.dismiss()
                    } else {
                        mSaveDialog.dismiss()
                        requireActivity().showLongToast(getString(R.string.info_save_fail))
                    }
                })
    }


    private fun showPickPhotoDialog() {
        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())
                .theme(R.style.customPictureStyle)
                .imageSpanCount(3)
                .selectionMode(PictureConfig.SINGLE)
                .previewImage(true)
                .isCamera(true)
                .enableCrop(true)
                .compress(true)
                .withAspectRatio(10, 10)
                .showCropFrame(false)
                .showCropGrid(false)
                .circleDimmedLayer(true)
                .freeStyleCropEnabled(true)
                .forResult(PictureMimeType.ofImage())

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PictureMimeType.ofImage()) {
            val result = PictureSelector.obtainMultipleResult(data)
            if (result.size != 0) {
                val localMedia = result[0]
                uploadServer(File(localMedia.compressPath))
            }
        }
    }

    private fun uploadServer(file: File) {
        mUploadDialog.show()
        sdv_avatar.setImageURI(file.toUri())
        val requestBody = RequestBody.create(MediaType.parse("image/*"), file)
        val imagePart = MultipartBody.Part.createFormData("pic", file.name, requestBody)
        val typePart = MultipartBody.Part.createFormData("type", "2")
        uploadService.updateProfilePic(imagePart, typePart)
                .applySchedulers()
                .subscribeApi(onNext = {
                    updaptAccountCache(it.result.url)
                    requireActivity().showLongToast(getString(R.string.upload_avatar_success))
                    avatarId = it.result.id

                    mUploadDialog.dismiss()
                }, onApiError = {
                    requireActivity().showLongToast(getString(R.string.upload_avatar_fail))
                    mUploadDialog.dismiss()
                })
    }


    private fun updaptAccountCache(avatarUrl: String) {
        val cacheOf = storage.userCache.cacheOf<UserAccount>()
        val account = cacheOf.get(ProfileFragment.ACCOUNT_CACHE_KEY).orNull()
        if (account != null) {
            account.avatar = avatarUrl
            cacheOf.put(ProfileFragment.ACCOUNT_CACHE_KEY, account)
        }
    }

}