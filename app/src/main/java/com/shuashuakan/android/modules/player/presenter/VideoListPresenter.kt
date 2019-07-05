package com.shuashuakan.android.modules.player.presenter

import com.ishumei.smantifraud.SmAntiFraud
import com.shuashuakan.android.data.api.model.AcceptGift
import com.shuashuakan.android.data.api.model.ChestInfo
import com.shuashuakan.android.data.api.model.PublishActivity
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.model.home.HomeFeedTypeModel
import com.shuashuakan.android.data.api.services.ApiService
import com.shuashuakan.android.exts.applySchedulers
import com.shuashuakan.android.exts.mvp.ApiError
import com.shuashuakan.android.exts.mvp.BasePresenter
import com.shuashuakan.android.exts.subscribeApi
import com.shuashuakan.android.modules.FeedTransportManager
import com.shuashuakan.android.modules.publisher.chains.ChainsListIntentParam
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class VideoListPresenter @Inject constructor(
        private val apiService: ApiService
) : BasePresenter<RecommendVideoView>() {

    private var mIntentParam: ChainsListIntentParam? = null
    private var mUserIsMine: Boolean = false


    fun initIntentParam(intentParam: ChainsListIntentParam?, userIsMine: Boolean) {
        this.mIntentParam = intentParam
        this.mUserIsMine = userIsMine
    }

    fun fetchFloorData() {
        var responseObservable: Observable<List<Feed>>? = null

        when (mIntentParam?.fromMark) {
            FeedTransportManager.MARK_FROM_FOLLOW -> {
                responseObservable = fetchFollowFeeds(mIntentParam?.nextId)
            }
            FeedTransportManager.MARK_FROM_TOPIC_DETAIL_RECOMMEND -> {
                responseObservable = fetchTopicDetailRecommendFeeds(mIntentParam?.channelId, mIntentParam?.nextId)
            }
            FeedTransportManager.MARK_FROM_TOPIC_DETAIL_TIMELINE -> {
                responseObservable = fetchTopicDetailTimelineFees(mIntentParam?.channelId, mIntentParam?.nextId)
            }
            FeedTransportManager.MARK_FROM_PERSONAL_NEWEST -> {
                responseObservable = fetchPersonalNewestFeeds(mIntentParam?.feedList?.getOrNull(0)?.userId.toString(),
                        mIntentParam?.page ?: 0, mUserIsMine)
            }
            FeedTransportManager.MARK_FROM_PERSONAL_UP -> {
                responseObservable = fetchFaveoriteFeeds(mIntentParam?.feedList?.getOrNull(0)?.userId.toString(),
                        mIntentParam?.page ?: 0, mUserIsMine)
            }
            FeedTransportManager.MARK_FROM_EXPLORE_EXCELLENT_CHAINS -> {
                //排行榜点击进入视频接龙页的不需要加载数据
            }
            FeedTransportManager.MARK_FROM_VIP_HOME -> {
                //新人视频不需要加载更多
            }
            FeedTransportManager.MARK_FROM_RECOMMEND_USER_FEED -> {
                //  关注页面推荐用户的视频不需要加载更多
            }
            FeedTransportManager.MARK_FROM_CATEGORY_RECOMMEND_TOPIC -> {
                // 分区界面推荐话题的视频不需要加载更多
            }
            FeedTransportManager.MARK_FROM_CATEGORY_LEADERBOARD -> {
                // 分区界面热门视频的排行榜的视频不需要加载更多
            }
            else -> {
                responseObservable = fetchHomeData()
            }

        }

        responseObservable
                ?.applySchedulers()
                ?.subscribeApi(
                        onNext = {
                            if (isViewAttached) {
                                upDataPage()
                                view?.onFloorDataLoadSuccess(it)
                            }
                        },
                        onApiError = {
                            if (!defaultErrorProcessor(it)) {
                                view?.onFloorDataLoadError(it)
                            }
                        }
                )

    }

    private fun upDataPage() {
        mIntentParam?.page?.let {
            val page = mIntentParam?.page ?: 0
            mIntentParam?.page = page + 1
        }
    }

    fun fetchActivityPublish() {
        apiService.getPublishActivity()
                .applySchedulers()
                .subscribeApi(
                        onNext = {
                            view?.onPublishActivityLoadSuccess(it)
                        },
                        onApiError = {
                            if (!defaultErrorProcessor(it)) {
                                view?.onPublishActivityLoadError(it)
                            }
                        }
                )
    }

    /**
     * 获取宝箱信息和打开后宝箱信息
     */
    fun fetchChestInfo() {
        val chestInfoObservable = apiService.getChestInfo()
        val openChestObservable = apiService.openChest(SmAntiFraud.getDeviceId())
        Observable.zip(chestInfoObservable, openChestObservable, BiFunction<ChestInfo, ChestInfo, Array<ChestInfo>> { chestInfo, openChest ->
            arrayOf(chestInfo, openChest)
        })
                .applySchedulers()
                .subscribeApi(
                        onNext = {
                            view?.onChestLoadSuccess(it)
                        },
                        onApiError = {
                            if (!defaultErrorProcessor(it)) {
                                view.onChestLoadError(it)
                            }
                        }
                )
    }

    private fun fetchFollowFeeds(nextId: String?, pageCount: Int = 10): Observable<List<Feed>> {
        return apiService.getMultiTypeTimeLineData(next_id = nextId, count = pageCount)
                .doOnNext {
                    mIntentParam?.nextId = it.cursor?.next_id
                }
                .map {
                    it.timeline
                }
    }

    private fun fetchTopicDetailRecommendFeeds(channelId: Long?, nextId: String?, pageCount: Int = 10): Observable<List<Feed>> {
        return apiService.channelRecommendData(channelId, null, nextId, pageCount)
                .doOnNext {
                    mIntentParam?.nextId = it.cursor?.nextId
                }
                .map { channelDetailRecommendModel ->
                    channelDetailRecommendModel.feedList.map {
                        it.data.solitaireFeeds[0]
                    }
                }
    }

    private fun fetchTopicDetailTimelineFees(channelId: Long? = 0, channelNextId: String? = null): Observable<List<Feed>> {
        return apiService.getChannelTopicTimeLineInfo(channelId, channelNextId)
                .doOnNext {
                    mIntentParam?.nextId = it.cursor?.nextId
                }
                .map { topicTimeLineModel ->
                    topicTimeLineModel.feedList.map {
                        it.data
                    }
                }
    }

    private fun fetchPersonalNewestFeeds(userId: String, page: Int = 0, userIsMine: Boolean = false): Observable<List<Feed>> {
        return if (userIsMine) {
            apiService.userUploadVideo(page)
        } else {
            apiService.otherUserUploadFeeds(page, userId)
        }
    }

    private fun fetchFaveoriteFeeds(userId: String, page: Int = 0, userIsMine: Boolean = false): Observable<List<Feed>> {
        return if (!userIsMine) {
            apiService.otherUserFavFeeds(page, userId)
        } else {
            apiService.favFeeds(page)
        }
    }


    private fun fetchHomeData(): Observable<List<Feed>> {
        return apiService.getHomeFeed()
                .map { list ->
                    list.filter { it is HomeFeedTypeModel }
                            .map { (it as HomeFeedTypeModel).data }
                            .toList()
                }

    }


    fun acceptGift() {
        apiService.conversionChest(SmAntiFraud.getDeviceId())
                .applySchedulers()
                .subscribeApi(
                        onNext = {
                            view?.onAcceptGiftSuccess(it)
                        },
                        onApiError = {
                            if (!defaultErrorProcessor(it)) {
                                view?.onAcceptGiftError(it)
                            }
                        }
                )
    }

}


interface RecommendVideoView : com.shuashuakan.android.exts.mvp.ApiView {
    fun onFloorDataLoadSuccess(homeFeedData: List<Feed>)
    fun onFloorDataLoadError(error: ApiError)
    fun onPublishActivityLoadSuccess(publishActivity: PublishActivity)
    fun onPublishActivityLoadError(error: ApiError)
    fun onChestLoadSuccess(result: Array<ChestInfo>)
    fun onChestLoadError(error: ApiError)

    fun onAcceptGiftSuccess(acceptGift: AcceptGift)
    fun onAcceptGiftError(error: ApiError)

}