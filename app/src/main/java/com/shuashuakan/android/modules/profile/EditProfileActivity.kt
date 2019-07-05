package com.shuashuakan.android.modules.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.os.toUri
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import com.facebook.drawee.view.SimpleDraweeView
import com.gyf.barlibrary.ImmersionBar
import com.jakewharton.rxbinding2.view.RxView
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.shuashuakan.android.R
import com.shuashuakan.android.commons.cache.Storage
import com.shuashuakan.android.constant.Constants
import com.shuashuakan.android.data.api.model.account.UserAccount
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.data.api.services.UploadImageService
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.ui.ProgressDialog
import com.shuashuakan.android.ui.base.FishActivity
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.modules.track.ClickAction
import com.shuashuakan.android.modules.track.trackProfileIncrementOnce
import com.shuashuakan.android.utils.*
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_edit_profile.*
import me.twocities.linker.annotations.Link
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Link("ssr://my/setting/user_info")
class EditProfileActivity : FishActivity() {

    companion object {
        private const val PHOTO_REQUEST_GALLERY = 1
        private const val PHOTO_REQUEST_CAMERA = 2
        private const val PHOTO_REQUEST_CROP = 3
        private const val TYPE_NICK_EDIT = 4
        private const val TYPE_BIO_EDIT = 5
        private const val TYPE_EXTRA = "type_extra"
        fun createIntent(context: Context, userAccount: UserAccount): Intent {
            return Intent(context, EditProfileActivity::class.java).putExtra(TYPE_EXTRA, userAccount)
        }
    }

    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var uploadService: UploadImageService
    @Inject
    lateinit var storage: Storage

    private val toolbar by bindView<Toolbar>(R.id.toolbar)
    private val avatarView by bindView<SimpleDraweeView>(R.id.avatar_view)
    private val nickNameView by bindView<TextView>(R.id.nick_name_view)
    private val birthdayView by bindView<TextView>(R.id.birthday_view)
    private val bioView by bindView<TextView>(R.id.bio_view)
    private val birthdayContainer by bindView<View>(R.id.birthday_container)
    private val genderContainer by bindView<View>(R.id.gender_container)
    private val genderView by bindView<TextView>(R.id.gender_view)


    private var userAccount: UserAccount? = null

    private var birthday: String? = null
    private var nickName: String? = null
    private var avatarId: Long? = null
    private var bio: String? = null
    private var gender: Int? = null

    private lateinit var pvTime: TimePickerView

    private val dialog by lazy {
        return@lazy ProgressDialog.progressDialog(this@EditProfileActivity, getString(R.string.string_data_saveing))
    }

    val uploadDialog by lazy {
        return@lazy ProgressDialog.progressDialog(this@EditProfileActivity, getString(R.string.string_avatar_upload_ing))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        ImmersionBar.with(this).navigationBarColor(R.color.black).init()
        ImmersionBar.setTitleBar(this, toolbar)
        spider.pageTracer().reportPageCreated(this)
        userAccount = intent.getParcelableExtra(TYPE_EXTRA)
        if (userAccount == null) {
            this.finish()
            return
        }
        initView()
        initDateView()
        setListener()
        setupToolbar()
    }

    override fun onDestroy() {
        super.onDestroy()
        ImmersionBar.with(this).destroy()
    }

    override fun onResume() {
        super.onResume()
        spider.pageTracer().reportPageShown(this, "ssr://my/setting/user_info", "")
    }

    private fun initView() {
        userAccount?.let {
            avatarView.setImageURI(it.avatar)
            nickNameView.text = it.nickName
            nickNameView.setHorizontallyScrolling(true)
            if (!TextUtils.isEmpty(it.birthday)) {
                birthdayView.text = it.birthday
                birthday = it.birthday
            }
            if (it.gender != null) {
                genderView.text = if (it.gender == 0) getString(com.shuashuakan.android.base.ui.R.string.string_male) else getString(com.shuashuakan.android.base.ui.R.string.string_female)
            }
            bioView.text = it.bio
        }
    }

    private fun initDateView() {
        val selectedDate = Calendar.getInstance()
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()

        startDate.set(1970, 0, 1)

        val currentTime = TimeUtil.getCurrentTime()
        if (currentTime?.length != null && currentTime.length > 9)
            endDate.set(currentTime.substring(0, 4).toInt(), currentTime.substring(5, 7).toInt() - 1, currentTime.substring(8, 10).toInt())
        else
            endDate.set(2018, 10, 11)

        pvTime = TimePickerBuilder(this@EditProfileActivity, OnTimeSelectListener { date: Date, view: View ->
            birthday = TimeUtil.format2YMD(date.time)
            birthdayView.text = birthday
            menuSave.isEnabled = !diff()
        }).setType(booleanArrayOf(true, true, true, false, false, false))
                .setLabel("年", "月", "日", "", "", "")
                .isCenterLabel(false)
                .setDividerColor(Color.DKGRAY)
                .setContentTextSize(21)
                .setDate(selectedDate)
                .setRangDate(startDate, endDate)
                .setBackgroundId(0x00FFFFFF) //设置外部遮罩颜色
                .setDecorView(null).build()
        if (birthday?.length == 10) {
            val sDate = Calendar.getInstance()
            sDate.set(birthday!!.substring(0, 4).toInt(), birthday!!.substring(5, 7).toInt() - 1, birthday!!.substring(8, 10).toInt())
            pvTime.setDate(sDate)
        }
    }

    @SuppressLint("CheckResult")
    private fun setListener() {
        avatarView.setOnClickListener {
            showPickPhotoDialog()
        }

        RxView.clicks(birthdayContainer)
                .throttleFirst(Constants.DOUBLE_CLICK_INTERVAL, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    pvTime.show(birthdayContainer)
                }

        genderContainer.setOnClickListener {
            val items = arrayOf(getString(R.string.string_male), getString(R.string.string_female))
            AlertDialog.Builder(this)
                    .setItems(items) { _, which ->
                        if (which == 0) {
                            genderView.text = getString(R.string.string_male)
                            gender = 0
                        } else {
                            genderView.text = getString(R.string.string_female)
                            gender = 1
                        }
                        menuSave.isEnabled = !diff()
                    }
                    .show()
        }
        RxView.clicks(nickNameView)
                .throttleFirst(Constants.DOUBLE_CLICK_INTERVAL, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    startActivityForResult(EditSignActivity.create(this, false, nickNameView.text.toString()), TYPE_NICK_EDIT)
                }
        RxView.clicks(bioView)
                .throttleFirst(Constants.DOUBLE_CLICK_INTERVAL, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    startActivityForResult(EditSignActivity.create(this, true, bioView.text.toString()), TYPE_BIO_EDIT)
                }
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


    private fun doSave() {
        dialog.show()

        userAccount?.let {
            if (gender == it.gender) {
                gender = null
            }
            if (birthday == it.birthday) {
                birthday = null
            }

            if (bio == it.bio) {
                bio = null
            }
            if (nickName == it.nickName) {
                nickName = null
            }
        }


    apiService.editUserInfo(gender,null, birthday, nickName, bio, avatarId)
        .applySchedulers()
        .autoDisposable(AndroidLifecycleScopeProvider.from(this))
        .subscribeApi(
            onNext = {
              if (it.result.isSuccess) {
                Handler().postDelayed({
                  showLongToast(getString(R.string.info_save_success))
                  if (!this.isFinishing)
                    dialog.dismiss()
                  setResult(Activity.RESULT_OK)
                  finish()
                    trackProfileIncrementOnce(arrayListOf(ClickAction.SAVE_PROFILE_CLICK to 1, ClickAction.SUCCESS to 1))
                }, 1500)
              } else {
                dialog.dismiss()
                showLongToast(getString(R.string.info_save_fail))
              }
            }, onApiError = {
          val httpError = it as? ApiError.HttpError
          if (httpError != null) {
            dialog.dismiss()
            showLongToast(httpError.displayMsg)
          } else {
            dialog.dismiss()
            showLongToast(getString(R.string.info_save_fail))
          }
        })
  }

    private fun diff(): Boolean {
        return (avatarId == null && (nickName == null ||
                nickNameView.text.toString() == userAccount?.nickName)
                && (gender == null || gender == userAccount?.gender) && (birthday == null ||
                birthdayView.text.toString() == userAccount?.birthday) && (bio == null || bioView.text.toString() == userAccount?.bio))
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            intent: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == TYPE_NICK_EDIT && intent != null) {
                val s = intent.getStringExtra(EditSignActivity.SIGN_TEXT)
                nickName = s
                if (!TextUtils.isEmpty(s))
                    nickNameView.text = s
            } else if (requestCode == TYPE_BIO_EDIT && intent != null) {
                val s = intent.getStringExtra(EditSignActivity.SIGN_TEXT)
                bio = s
                if (!TextUtils.isEmpty(s))
                    bioView.text = s
            }
            menuSave.isEnabled = !diff()
        }

        if (resultCode == Activity.RESULT_OK && requestCode == PictureMimeType.ofImage()) {
            val result = PictureSelector.obtainMultipleResult(intent)
            if (result.size != 0) {
                val localMedia = result[0]
                uploadServer(File(localMedia.compressPath))
            }
        }
    }

    private fun uploadServer(file: File) {
        uploadDialog.show()
        avatarView.setImageURI(file.toUri())
        val requestBody = RequestBody.create(MediaType.parse("image/*"), file)
        val imagePart = MultipartBody.Part.createFormData("pic", file.name, requestBody)
        val typePart = MultipartBody.Part.createFormData("type", "2")
        uploadService.updateProfilePic(imagePart, typePart)
                .applySchedulers()
                .subscribeApi(onNext = {
                    showLongToast(getString(R.string.upload_avatar_success))
                    avatarId = it.result.id
                    uploadDialog.dismiss()
                    menuSave.isEnabled = !diff()
                }, onApiError = {
                    showLongToast(getString(R.string.upload_avatar_fail))
                    uploadDialog.dismiss()
                }
                )
    }

    private fun setupToolbar() {
        toolbar.title = getString(R.string.string_change_profile_data)
        menuSave.setOnClickListener {
            doSave()
        }
        toolbar.setNavigationOnClickListener {
            if (diff()) {
                finish()
            } else {
                AlertDialog.Builder(this)
                        .setMessage(getString(R.string.is_save_change))
                        .setPositiveButton(getString(com.shuashuakan.android.base.ui.R.string.string_save)) { dialog, _ ->
                            dialog.dismiss()
                            doSave()
                        }
                        .setNegativeButton(getString(R.string.string_give_up)) { dialog, _ ->
                            dialog.dismiss()
                            finish()
                        }
                        .show()
            }
        }
    }

}