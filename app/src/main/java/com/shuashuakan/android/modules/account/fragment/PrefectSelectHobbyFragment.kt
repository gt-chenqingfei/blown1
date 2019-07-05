package com.shuashuakan.android.modules.account.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.chad.library.adapter.base.BaseViewHolder
import com.shuashuakan.android.R
import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.data.api.model.home.HomeRecommendInterestTypeModelDetail
import com.shuashuakan.android.data.api.model.home.Interests
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.event.RefreshProfileEvent
import com.shuashuakan.android.event.RefreshSettingInterestEvent
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.spider.Spider
import com.shuashuakan.android.spider.SpiderEventNames
import com.shuashuakan.android.ui.base.FishFragment
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.modules.account.activity.PerfectSelectHobbyActivity
import com.shuashuakan.android.modules.account.presenter.PrefectSelectHobbyPresenter
import com.shuashuakan.android.modules.account.adapter.SelectHobbyInterestAdapter
import com.shuashuakan.android.utils.*
import javax.inject.Inject

/**
 * 资料-兴趣页面
 *
 * Author: ZhaiDongyang
 * Date: 2019/1/26
 */
class PrefectSelectHobbyFragment : FishFragment(),
        SelectHobbyInterestAdapter.InterestAdapterItemClickListener,
        View.OnClickListener, PrefectSelectHobbyPresenter.PrefectSelectHobbyApiView<HomeRecommendInterestTypeModelDetail> {

    @Inject
    lateinit var spider: Spider
    @Inject
    lateinit var accountManager: AccountManager
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var prefectSelectHobbyPresenter: PrefectSelectHobbyPresenter

    private val rootView by bindView<ConstraintLayout>(R.id.fragment_select_hobby_cc_root)
    private val contentView by bindView<ConstraintLayout>(R.id.fragment_select_hobby_content)
    private val errorView by bindView<LinearLayout>(R.id.fragment_select_hobby_error_view);

    private val recyclerView by bindView<RecyclerView>(R.id.fragment_select_hobby_rv)
    private val sendButton by bindView<Button>(R.id.fragment_select_hobby_bt)
    private val titleTextView by bindView<TextView>(R.id.fragment_select_hobby_title)
    private val nextTextView by bindView<TextView>(R.id.fragment_select_hobby_next)
    private val backImageView by bindView<ImageView>(R.id.fragment_select_hobby_back)
    private val subtitleTextView by bindView<TextView>(R.id.fragment_select_hobby_tv_subtitle)
    private val nickNum by bindView<TextView>(R.id.perfect_nick_num)
    private lateinit var selectHobbyInterestAdapter: SelectHobbyInterestAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_select_hobby, container, false)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        prefectSelectHobbyPresenter.attachView(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        spider.pageTracer().reportPageCreated(this)
        spider.pageTracer().reportPageShown(this, "PrefectSelectHobbyFragment", "")
        sendButton.setOnClickListener(this)
        nextTextView.setOnClickListener(this)
        backImageView.setOnClickListener(this)
        contentView.visibility = View.GONE
        errorView.visibility = View.GONE
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.addItemDecoration(SpaceDecoration(requireContext().dip(20f)))
        selectHobbyInterestAdapter = SelectHobbyInterestAdapter(null)
        recyclerView.adapter = selectHobbyInterestAdapter
        selectHobbyInterestAdapter.setInterestAdapterItemClickListener(this)
        prefectSelectHobbyPresenter.requestApi()


        rootView.setBackgroundColor(Color.parseColor("#111217"))
        titleTextView.text = getString(R.string.string_want_watch)
        sendButton.text = getString(R.string.string_choose_finish)
        sendButton.setBackgroundResource(R.drawable.selector_hobby_yellow)
        subtitleTextView.setTextColor(Color.parseColor("#838791"))
        backImageView.visibility = View.VISIBLE
        nextTextView.visibility = View.GONE
        nickNum.visibility = View.GONE
        selectHobbyInterestAdapter.setGuidePage(0)

        errorView.setOnClickListener {
            prefectSelectHobbyPresenter.requestApi()
            errorView.visibility = View.GONE
        }
    }

    override fun onInterestAdapterItemClick(helper: BaseViewHolder) {
        // 点击条目时判断是否登录没有出现登录页面
        if (!accountManager.hasAccount()) {
            LoginActivity.launch(requireContext())
        } else {
            selectHobbyInterestAdapter.switchSelectedState(helper.adapterPosition)
            sendButton.isEnabled = !selectHobbyInterestAdapter.getSelectedItems().isEmpty()
        }
    }

    override fun showMessage(message: String) {
    }

    override fun showError() {
        errorView.visibility = View.VISIBLE
        contentView.visibility = View.GONE
    }

    override fun showData(data: HomeRecommendInterestTypeModelDetail) {
        dataInterests = data.interests!!
        selectHobbyInterestAdapter.setNewData(data.interests)

        var isSelected = false

        if (data.interests == null) return
        for (i in 0..(data.interests!!.size - 1)) {
            if (data.interests!![i].is_selected != null && data.interests!![i].is_selected!!) {
                selectHobbyInterestAdapter.switchSelectedState(i)
                selectHobbyInterestAdapter.notifyDataSetChanged()
                isSelected = true
            }
        }
        sendButton.isEnabled = isSelected
        contentView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
    }

    private lateinit var dataInterests: List<Interests>

    override fun onClick(v: View?) {
        if (v!!.id == R.id.fragment_select_hobby_bt) {

            val size = selectHobbyInterestAdapter.getSelectedItems()
            val iterator = size.iterator()
            val str = StringBuilder()

            if (size.isEmpty()) return

            do {
                str.append(dataInterests[iterator.next()].id.toString())
                if (iterator.hasNext()) {
                    str.append(",")
                }
            } while (iterator.hasNext())
            // 上传服务器，上传成功后改变页面为 GOT IT，不刷新页面
            apiService.selectInterestChoice(str.toString()).applySchedulers().subscribeApi(onNext = {
                if (it.result.isSuccess) {
                    selectHobbyInterestAdapter.clearSelectedState()


                    val strName = StringBuilder()
                    str.split(",").forEachIndexed { _, s ->
                        for (i in 0 until dataInterests.size) {
                            if (dataInterests[i].id.toString() == s) {
                                strName.append(dataInterests[i].name.toString())
                                strName.append(",")
                            }
                        }
                    }

                    RxBus.get().post(RefreshSettingInterestEvent(strName.toString()))
                    RxBus.get().post(RefreshProfileEvent())
                    (requireContext() as PerfectSelectHobbyActivity).finish()

                    // 打点兴趣数据
                    spider.manuallyEvent(SpiderEventNames.HOME_PAGE_INTEREST)
                            .put("userID", activity?.getUserId() ?: "")
                            .put("categoryIDs", str.toString())
                            .track()
                } else {
                    requireContext().showLongToast(getString(R.string.string_operating_error))
                }
            }, onApiError = {
                if (it is ApiError.HttpError) {
                    requireContext().showLongToast(it.displayMsg)
                } else {
                    requireContext().showLongToast(getString(R.string.string_operating_error))
                }
            })


        }

        if (v.id == R.id.fragment_select_hobby_next) {
            requireActivity().finish()
            RxBus.get().post(RefreshProfileEvent())

        }

        if (v.id == R.id.fragment_select_hobby_back) {
            showAlertDialog(requireContext())
        }
    }

    override fun onDetach() {
        super.onDetach()
        prefectSelectHobbyPresenter.detachView(false)
    }

}
