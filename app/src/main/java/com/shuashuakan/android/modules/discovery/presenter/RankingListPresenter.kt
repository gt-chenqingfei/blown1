package com.shuashuakan.android.modules.discovery.presenter

import android.content.Context
import com.shuashuakan.android.R
import com.shuashuakan.android.commons.di.AppContext
import com.shuashuakan.android.data.api.model.explore.RankingListModel
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.exts.mvp.ApiPresenter
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Author:  liJie
 * Date:   2019/1/14
 * Email:  2607401801@qq.com
 */
class RankingListPresenter @Inject constructor(
        @AppContext val context: Context, private val apiService: ApiService
) : ApiPresenter<RankingListApiView<RankingListModel>, RankingListModel>(context) {

    private var page: Int = 0
    private var tabName: String? = null
    private var channelId: String? = null
    private var categoryId: String? = null

    override fun onSuccess(data: RankingListModel) {
        view.showData(data)
    }

    fun requestApi(page: Int, tabName: String?, channelId: String?, categoryId: String?) {
        this.page = page
        this.tabName = tabName
        this.channelId = channelId
        this.categoryId = categoryId
        subscribe()
    }

    override fun getObservable(): Observable<RankingListModel> {
        //DAILY_USER_LEADER_BOARD
        //WEEK_USER_LEADER_BOARD
        var type = "DAILY_USER_LEADER_BOARD"
        if (tabName == context.getString(R.string.string_week_top))
            type = "WEEK_USER_LEADER_BOARD"
        else if (tabName == context.getString(R.string.string_today_top))
            type = "DAILY_USER_LEADER_BOARD"
        else if (tabName == context.getString(R.string.up_star_rank)) {
            type = "CATEGORY_USER_LEADER_BOARD"
//            val requestChannelId = channelId ?: ""
//            return if (requestChannelId.isNotEmpty()) {
//
//            } else {
//                apiService.getRankListData(type = type, categoryId = this.categoryId!!, page = this.page)
//            }
        }
        return apiService.getRankListData(type = type, channelId = this.channelId,categoryId = this.categoryId, page = this.page)
    }
}

interface RankingListApiView<in Data> : com.shuashuakan.android.exts.mvp.ApiView {
    fun showData(data: Data)
}