package com.shuashuakan.android.data.api.services

import com.shuashuakan.android.data.api.ApiDefaultConfig.Companion.CLIENT_KEY
import com.shuashuakan.android.data.api.ApiDefaultConfig.Companion.CLIENT_SECRET
import com.shuashuakan.android.data.api.model.*
import com.shuashuakan.android.data.api.model.account.*
import com.shuashuakan.android.data.api.model.address.EnjoyAddress
import com.shuashuakan.android.data.api.model.address.TempAddress
import com.shuashuakan.android.data.api.model.chain.ChainSuccessModel
import com.shuashuakan.android.data.api.model.channel.*
import com.shuashuakan.android.data.api.model.comment.ApiComment
import com.shuashuakan.android.data.api.model.comment.CommentListResp
import com.shuashuakan.android.data.api.model.detail.ShareContent
import com.shuashuakan.android.data.api.model.detail.ShareResult
import com.shuashuakan.android.data.api.model.explore.ExploreResult
import com.shuashuakan.android.data.api.model.explore.RankingListModel
import com.shuashuakan.android.data.api.model.home.*
import com.shuashuakan.android.data.api.model.home.multitypetimeline.CardsType
import com.shuashuakan.android.data.api.model.home.multitypetimeline.MultiTypeTimeLineModel
import com.shuashuakan.android.data.api.model.home.multitypetimeline.MultiTypeTimeLineModel2
import com.shuashuakan.android.data.api.model.message.ActionUserInfoListItem
import com.shuashuakan.android.data.api.model.message.NewMessageRes
import com.shuashuakan.android.data.api.model.message.SystemNoticeItemModel
import com.shuashuakan.android.data.api.model.partition.PartitionData
import com.shuashuakan.android.data.api.model.partition.PartitionResult
import com.shuashuakan.android.data.api.model.ugc.TopicCategory
import com.shuashuakan.android.data.api.model.ugc.TopicNameListModel
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.*

interface ApiService {

    @POST("3/oauth2/send_taobao_totp_code.json")
    @FormUrlEncoded
    fun sendSMSCode(
            @Field("client_id") clientId: String = CLIENT_KEY, @Field(
                    "client_secret"
            ) clientSecret: String = CLIENT_SECRET, @Field(
                    "mobile_phone"
            ) mobilePhone: String
    ): Observable<CommonResult>

    @POST("3/oauth2/access_token.json")
    @FormUrlEncoded
    fun login(@FieldMap queryMap: Map<String, String>): Observable<Account>

    @POST("mhw/v1/user/taobao_sso.json")
    @FormUrlEncoded
    fun saveTaboToken(
            @Field("nick") nick: String?, @Field("avatar_url") avatarUrl: String?,
            @Field("open_id") openId: String?,
            @Field("open_sid") openSid: String?
    ): Observable<CommonResult>


    @POST("mhw/v1/favorite/fav.json")
    @FormUrlEncoded
    fun fav(@Field("product_id") productId: String): Observable<CommonResult>

    @POST("mhw/v1/favorite/un_fav.json")
    @FormUrlEncoded
    fun unFav(@Field("product_id") productId: String): Observable<CommonResult>

    @POST("mhw/v1/ssk/user/edit_user_info.json")
    @FormUrlEncoded
    fun editUserInfo(
            @Field("gender") gender: Int?,
            @Field("era_label_name") eraLabelName: String?,
            @Field("birthday") birth: String?,
            @Field("nick_name") nickName: String?,
            @Field("bio") bio: String?,
            @Field("avatar_id") avatarId: Long?
    ): Observable<CommonResult>


    @POST("mhw/v1/channel/batch_subscribe.json")
    @FormUrlEncoded
    fun batchSubscirbe(
            @Field("channel_ids") channelIds: String?
    ): Observable<CommonResult>

    @GET("redkeep/wx/pub/login.json")
    fun getAccessToken(@Query("code") code: String?,
                       @Query("state") state: String, @Query("app_key") appKey: String): Observable<Account>

    @POST("redkeep/wx/pub/mobile/bind.json")
    @FormUrlEncoded
    fun bindMobilePhone(@Field("mobile_phone") mobilePhone: String,
                        @Field("totp_code") totpCode: String): Observable<Account>


    @GET("mhw/v1/feed/index.json")
    fun getHomeFeeds(@Query("browse_count") browse_count: Int): Observable<List<Feed>>

    @GET("mhw/v1/solitaire/list/{id}")
    fun getChainsFeeds(@Path("id") id: String?,
                       @Query("cursor_feed_id") cursor_feed_id: String?,
                       @Query("direction") direction: String,
                       @Query("count") count: Int,
                       @Query("floor_feed_id") floor_feed_id: String?,
                       @Query("since_floor") since_floor: Int?,
                       @Query("snap_id") snap_id: Long?): Observable<ChainsFeedListModel>

    @GET("mhw/v1/feed/{id}")
    fun queryFeedById(@Path("id") id: String): Observable<Feed>


    @POST("mhw/v1/feed/report/watch.json")
    @FormUrlEncoded
    fun reportAwardBehavior(@FieldMap map: Map<String, String>): Observable<CommonResult>

    @POST("mhw/v1/feed/report/reward.json")
    @FormUrlEncoded
    fun reportAward(@FieldMap map: Map<String, String>): Observable<RewardResp>

    @GET("mhw/v1/feed/share_feed_client.json")
    fun getVideoShareInfo(@Query("id") id: String): Observable<ShareResult>

    @GET("mhw/v1/share/comment.json")
    fun getCommentShareInfo(@Query("comment_id") id: Long): Observable<ShareResult>

    @POST("mhw/v1/feed/vote.json")
    @FormUrlEncoded
    fun likeFeed(@Field("id") id: String): Observable<CommonResult>

    @POST("mhw/v1/feed/un_fav_feed.json")
    @FormUrlEncoded
    fun unLikeFeed(@Field("id") id: String): Observable<CommonResult>

    @POST("mhw/v1/feed/user_mount.json")
    @FormUrlEncoded
    fun feedProductLike(@Field("mount_id") id: Long): Observable<CommonResult>

    @POST("mhw/v1/feed/un_user_mount.json")
    @FormUrlEncoded
    fun feedProductUnLike(@Field("mount_id") id: Long): Observable<CommonResult>

    @GET("mhw/v1/lottery/roulette_page.json")
    fun roulette(@QueryMap map: Map<String, String>): Observable<RouletteResponse>

    @GET("mhw/v1/lottery/feed_roulette_page.json")
    fun feedRoulette(@QueryMap map: Map<String, String>): Observable<RouletteResponse>

    @POST("mhw/v1/lottery/start_roulette.json")
    @FormUrlEncoded
    fun startRoulette(@FieldMap map: Map<String, String>, @Field("config_id") configId: String): Observable<LotteryResult>

    @GET("mhw/v1/ssk/user/user_info.json")
    fun getUserInfo(): Observable<UserAccount>

    @GET("mhw/v1/ssk/user/other_user_info.json")
    fun getOtherUserInfo(@Query("target_user_id") userId: String): Observable<UserAccount>

    @GET("mhw/v1/ssk/user/fav/feed.json")
    fun favFeeds(@Query("page") page: Int): Observable<List<Feed>>

    @GET("mhw/v1/ssk/user/other/fav/feed.json")
    fun otherUserFavFeeds(@Query("page") page: Int, @Query("user_id") userId: String): Observable<List<Feed>>

    @GET("mhw/v1/ssk/user/other/upload/feed.json")
    fun otherUserUploadFeeds(
            @Query("page") page: Int,
            @Query("user_id") userId: String,
            @Query("count") count: Int = 10): Observable<List<Feed>>

    @GET("mhw/v1/feed/list_by_secret_key.json")
    fun getFeedListBySecretKey(@Query("page") page: Int, @Query("count") count: Int, @Query("keyword") keyword: String): Observable<VipHomeFeedListModel>

    @POST("mhw/v1/lottery/upload_winner_address.json")
    @FormUrlEncoded
    fun uploadRecord(@Field("record_id") recordId: Long, @Field("address_id") addressId: Int):
            Observable<CommonResult>


    @GET("mhw/v1/ssk/user/fav/goods.json")
    fun favGoods(@Query("page") page: Int): Observable<List<Good>>

    @GET("mhw/v1/user/address/list.json")
    fun getUserAddressList(): Observable<List<EnjoyAddress>>

    @FormUrlEncoded
    @POST("mhw/v1/user/address/create.json")
    fun createUserAddresse(
            @Field("province_id") provinceId: Int,
            @Field("city_id") cityId: Int, @Field("district_id") districtId: Int,
            @Field("detail_address") detailAddress: String, @Field("addressee") addressee: String,
            @Field("phone") phone: String, @Field("zip_code") zipCode: String,
            @Field("default") isDefault: Boolean
    ): Observable<EnjoyAddress>

    @FormUrlEncoded
    @POST("mhw/v1/user/address/update.json")
    fun updateUserAddresse(
            @Field("id") addressId: Int,
            @Field("province_id") provinceId: Int, @Field("city_id") cityId: Int,
            @Field("district_id") districtId: Int, @Field("detail_address") detailAddress: String,
            @Field("addressee") addressee: String, @Field("phone") phone: String,
            @Field("zip_code") zipCode: String, @Field("default") isDefault: Boolean
    ): Observable<EnjoyAddress>

    @FormUrlEncoded
    @POST("mhw/v1/user/address/delete.json")
    fun deleteUserAddresse(@Field("id") addressId: Int): Observable<CommonResult>

    @GET("mhw/v1/address/get_all_address.json")
    fun getAddressDatabase(): Observable<TempAddress>

    @GET("mhw/v1/feed/by_goods.json")
    fun getFeedsByGoods(@Query("goods_id") goodId: String, @Query("page") page: Int,
                        @Query("count") count: Int = 10): Observable<List<Feed>>


    //频道页
    @GET("mhw/v1/feed/channel/list_feed_channel.json")
    fun getChannelResp(): Observable<ChannelResp>

    @GET("mhw/v1/feed/channel/list_feed.json")
    fun getChannelFeeds(@Query("feed_id") feedId: String?, @Query("channel_id") channel: String?,
                        @Query("count") count: Int = 21): Observable<ChannelFeed>

    @POST("redkeep/sns/revoke.json")
    @FormUrlEncoded
    fun revokeWeChat(
            @Field("open_platform_type") open_platform_type: String?, @Field("client_id") clientId: String = CLIENT_KEY): Observable<CommonResult>


    @POST("redkeep/sns/authorize.json")
    @FormUrlEncoded
    fun authorizeWeChat(
            @Field("open_platform_type") open_platform_type: String?, @Field("wechat_open_id") wechat_open_id: String?,
            @Field("wechat_access_token") wechat_access_token: String?, @Field("client_id") clientId: String = CLIENT_KEY): Observable<CommonResult>


    @GET("https://api.weixin.qq.com/sns/oauth2/access_token")
    fun getWxAccessToken(@Query("appid") appid: String, @Query("secret") secret: String,
                         @Query("code") code: String, @Query("grant_type") grant_type: String): Observable<WeChatToken>


    @POST("3/oauth2/change_mobile_phone.json")
    @FormUrlEncoded
    fun changeMobilePhone(@Field("old_mobile_phone") old_mobile_phone: String?,
                          @Field("new_mobile_phone") new_mobile_phone: String,
                          @Field("totp_code") totp_code: String): Observable<CommonResult>

    //跳转淘宝客
    @GET("mhw/v1/product/redirect.json")
    fun getProductRedirect(
            @Query("product_id") productId: String,
            @Query("just_buy") justBuy: Boolean
    ): Observable<RedirectUrl>

    @GET("mhw/v1/feed/share_app.json")
    fun getAppShareInfo(): Observable<ShareResult>

    @GET("mhw/v1/share/info.json")
    fun getShareInfo(@Query("target_id") targetId: String?, @Query("platform") platform: String,
                     @Query("type") type: String): Observable<ShareResult>

    @GET("mhw/v1/share/info.json")
    fun getShareInfoWithApp(@Query("target_id") targetId: String, @Query("platform") platform: String,
                            @Query("type") type: String): Observable<ShareResult>

    @GET("mhw/v1/message/latest.json")
    fun getMessage(@Query("count") count: Int): Observable<List<Message>>

    //分享回掉
    @GET("mhw/v1/feed/share_feed_callback.json")
    fun shareFeedCallback(@Query("feed_id") feedId: String?): Observable<CommonResult>

    //获取评论列表
    @GET("mhw/v1/comment/v2/list.json")
    fun getCommentList(
            @Query("target_id") targetId: String,
            @Query("target_type") targetType: String,
            @Query("max_id") maxId: Long?,
            @Query("since_id") sinceId: Long?,
            @Query("count") count: Int?,
            @Query("with_hot_comments") withHotComments: Boolean?
    ): Observable<CommentListResp>

    //添加评论
    @POST("mhw/v1/comment/create.json")
    @FormUrlEncoded
    fun createComment(
            @Field("target_id") targetId: String,
            @Field("target_type") targetType: String,
            @Field("content") content: String?,
            @Field("media_ids") mediaIds: String? = null,
            @Field("comment_type") comment_type: String = "NORMAL",
            @Field("position") position: Long? = null
    ): Observable<ApiComment>

    //删除评论
    @POST("mhw/v1/comment/destroy.json")
    @FormUrlEncoded
    fun destoryComment(
            @Field("comment_id") targetId: String
    ): Observable<CommonResult>

    //点赞
    @POST("mhw/v1/like/create.json")
    @FormUrlEncoded
    fun praise(
            @Field("target_id") targetId: String,
            @Field("target_type") targetType: String
    ): Observable<CommonResult>


    //取消点赞
    @POST("mhw/v1/like/destroy.json")
    @FormUrlEncoded
    fun cancelPraise(
            @Field("target_id") targetId: String,
            @Field("target_type") targetType: String
    ): Observable<CommonResult>

    //邀请出手相助
    @GET("mhw/v1/openpack/share_pack.json")
    fun sharePack(@Query("record_id") recordId: Long): Observable<ShareContent>

//  @GET("mhw/v1/chest/info.json")
//  fun chestInfo(): Observable<ApiChest>
//
//  @POST("mhw/v1/chest/open_chest.json")
//  @FormUrlEncoded
//  fun openChest(@Field("sm_id") smId: String): Observable<ApiChest>
//
//  @POST("mhw/v1/chest/conversion_chest.json")
//  @FormUrlEncoded
//  fun conversionChest(@Field("sm_id") smId: String): Observable<CommonResult>

    @GET("mhw/v1/feed/channel/share_channel_client.json")
    fun shareChannel(@Query("channel_id") channelId: String): Observable<ShareContent>

    @GET("mhw/v1/activity_card/open.json")
    fun getActivityCard(): Observable<List<ActivityCard>>

    //举报
    @POST("mhw/v1/complain/create_complain.json")
    @FormUrlEncoded
    fun createComplain(@Field("feed_id") feedId: String?,
                       @Field("complain_type") complainType: String): Observable<CommonResult>

    //不感兴趣
    @POST("mhw/v1/complain/create_bored.json")
    @FormUrlEncoded
    fun createBored(@Field("feed_id") feedId: String?,
                    @Field("sm_id") smId: String): Observable<CommonResult>

    @GET("mhw/v1/complain/complain_list.json")
    fun getComplainList(): Observable<List<Complain>>

    @GET("mhw/v1/testcase/keys.json")
    fun testcase(): Observable<TestCaseResp>

    @GET("mhw/v1/feed/channel/category_banner.json")
    fun getCategoryAndBanner(): Observable<CategoryBannerResp>


    @GET("mhw/v1/feed/channel/channel_feed.json")
    fun getChannelAndFeed(
            @Query("is_renew") isReNew: Boolean,
            @Query("category_id") id: Long
    ): Observable<List<CategoryTypeModel>>

    @GET("mhw/v1/feed/channel/play_feed.json")
    fun newChannelPlayFeed(@Query("feed_id") feedId: String?,
                           @Query("has_feed") hasFeed: Boolean?,
                           @Query("count") count: Int?
    ): Observable<List<Feed>>

    @GET("mhw/v1/feed/counter.json")
    fun counter(@Query("id") id: String?): Observable<CountBean>

    @GET("mhw/v1/config/context.json")
    fun configContext(): Observable<ConfigContext>

    @POST("mhw/v1/follow/create.json")
    @FormUrlEncoded
    fun createFollow(@Field("target_user_id") userId: String): Observable<CommonResult>

    @POST("mhw/v1/follow/cancel.json")
    @FormUrlEncoded
    fun cancelFollow(@Field("target_user_id") userId: String): Observable<CommonResult>

    /**
     * 关注列表
     */
    @GET("mhw/v1/follow/list.json")
    fun focusList(@Query("user_id") userId: String?,
                  @Query("page") page: Int,
                  @Query("count") count: Int): Observable<List<FocusModel>>

    /**
     * 粉丝列表
     */
    @GET("mhw/v1/follow/fans_list.json")
    fun fansList(@Query("user_id") userId: String?,
                 @Query("page") page: Int,
                 @Query("count") count: Int): Observable<List<FocusModel>>

    /**
     * 关注timeline
     */
    @GET("mhw/v1/follow/index.json")
    fun timeLineList(@Query("since_id") sinceId: Long?, @Query("count") count: Int = 20): Observable<List<TimeLineModel>>

    /**
     * 关注timeline
     */
    @GET("mhw/v1/follow/recommend.json")
    fun timeLineRecommendList(@Query("page") page: Int = 0, @Query("count") count: Int = 20): Observable<List<TimeLineRecommendModel>>

    /*
     * 聊天室列表
     */
    @GET("mhw/v1/notification/system_chatroom.json")
    fun getChatRoomList(): Observable<List<Long>>

    /**
     * 上传接龙视频
     */
    @POST("mhw/v1/solitaire/create.json")
    @FormUrlEncoded
    fun createSolitaire(@Field("video_id") videoId: String,
                        @Field("master_feed_id") feedId: String,
                        @Field("title") title: String?): Observable<Feed>

    @GET("/mhw/v1/home/index.json")
//  @GET("mhw/v1/feed/home.json")
    fun getHomeFeed(): Observable<List<HomeRecommendModel>>


//    @GET("mhw/v1/channel/subscribed_list.json")
//    fun getSubscribedList(@Query("page") page: Int,
//                          @Query("count") count: Int = 20): Observable<List<SubscribeModel>>

    @POST("mhw/v1/channel/subscribe.json")
    @FormUrlEncoded
    fun subscribeMethod(@Field("channel_id") channel_id: Long): Observable<CommonResult>

    @POST("mhw/v1/channel/unsubscribe.json")
    @FormUrlEncoded
    fun cancelSubscribe(@Field("channel_id") channel_id: Long): Observable<CommonResult>

    @GET("mhw/v1//ssk/user/upload_feed.json")
    fun userUploadVideo(
            @Query("page") page: Int,
            @Query("count") count: Int = 10): Observable<List<Feed>>

    @GET("mhw/v1/ssk/user/remove_feed.json")
    fun deleteFeed(@Query("feed_id") feed_id: String?): Observable<CommonResult>

    @GET("mhw/v1/channel/category/list.json")
    fun getChannelCategory(): Observable<List<TopicCategory>>

    @GET("mhw/v1/channel/list.json")
    fun geTopicList(@Query("category_id") categoryId: Long, @Query("page") page: Int, @Query("count") count: Int = 10): Observable<List<TopicNameListModel>>


    @GET("/mhw/v1/notification/hybrid.json")
    fun getNewMessageNotification(@Query("max_id") max_id: Long?,
                                  @Query("count") count: Int = 10,
                                  @Query("need_sys_notification_summaries") needSystem: Boolean = true): Observable<NewMessageRes>

    @GET("/mhw/v1/notification/system_broadcast.json")
    fun getSystemMessageList(@Query("max_id") max_id: Long?,
                             @Query("count") count: Int = 10): Observable<NewMessageRes>

    @GET("/mhw/v1/notification/system_personal.json")
    fun getSystemPersonalList(@Query("max_id") max_id: Long?,
                              @Query("count") count: Int = 10): Observable<NewMessageRes>

    @GET("mhw/v1/notice/proclamation_list.json")
    fun sysNoticeData(@Query("cursor_id") cursor_id: Long?, @Query("count") count: Int = 10): Observable<List<SystemNoticeItemModel>>

    @GET("/mhw/v1/notification/message_detail.json")
    fun noticeSubListData(@Query("id") id: Long): Observable<List<ActionUserInfoListItem>>

    @POST("/mhw/v1/feed/create.json")
    @FormUrlEncoded
    fun createMainFeed(@Field("channel_id") channel_id: Long, @Field("video") video: Long,
                       @Field("title") title: String, @Field("text") text: String): Observable<Feed>


    @POST("/mhw/v1/interest/choice.json")
    @FormUrlEncoded
    fun selectInterestChoice(@Field("ids") interest_ids: String): Observable<CommonResult>

    /**
     * 提供信息通用接口 比如接龙成功之后据二楼的楼层信息
     */
    @GET("/mhw/v1/tips/get.json")
    fun getNormalTip(@Query("id") id: String, @Query("type") type: String): Observable<ChainSuccessModel>

    /**
     * 发现
     */
    @GET("/mhw/v1/discovery/list.json")
    fun getExplore(@Query("page") page: Int, @Query("count") count: Int = 10): Observable<ExploreResult>

    /**
     * 话题页频道详细信息
     */
    @GET("/mhw/v1/channel/get_channel_info.json")
    fun getChannelInfo(@Query("channel_id") channel: String?): Observable<ChannelTopicInfo>

    /**
     * 话题页动态
     */
    @GET("/mhw/v1/channel/trend.json")
    fun getChannelTopicTimeLineInfo(
            @Query("channel_id") channel: Long?,
            @Query("next_id") maxId: String? = null,
            @Query("count") count: Int = 10): Observable<TopicTimeLineModel>

    /**
     * 个人页动态
     */
    @GET("/mhw/v1/ssk/user/upload_feed.json")
    fun getProfileTimeLineInfo(@Query("page") page: Int, @Query("count") count: Int = 10): Observable<List<TopicFeedData>>

    /**
     * 榜单
     */
    @GET("/mhw/v1/leaderboard/user.json")
    fun getRankListData(@Query("type") type: String,
                        @Query("channel_id") channelId:String?,
                        @Query("category_id") categoryId: String?,
                        @Query("page") page: Int,
                        @Query("count") count: Int = 10): Observable<RankingListModel>

//    /**
//     * 全部话题
//     */
//    @GET("/mhw/v1/feed/channel/all_channel.json")
//    fun getAllChannel(@Query("page") page: Int,
//                      @Query("count") count: Int = 10): Observable<List<SubscribeModel>>

    /**
     * 话题页推荐
     */
    @GET("/mhw/v1/channel/feed_recommend.json")
    fun channelRecommendData(@Query("channel_id") channel_id: Long?,
                             @Query("previous_id") previousId: String?,
                             @Query("next_id") nextId: String?,
                             @Query("count") count: Int = 5): Observable<ChannelDetailRecommendModel>

    /**
     * 话题页up明星榜&up明星榜页面
     */
//    @GET("mhw/v1/discovery/user/leaderboard.json")
//    fun channelRankData(@Query("channel_id") channelId:Long,
//                        @Query("type") type: String = "CATEGORY_USER_LEADER_BOARD",
//                        @Query("page") page: Int = 0,
//                        @Query("count") count: Int = 10): Observable<RankingListModel>

    /**
     * 获取个人的兴趣
     */
    @GET("mhw/v1//ssk/user/get_interest.json")
    fun getMyInterest(): Observable<HomeRecommendInterestTypeModelDetail>

    @GET("/mhw/v1/user_guide/info.json")
    fun userGuide(@Query("user_source") user_source: Int): Observable<GuideModel>

    /**
     * 获取水印视频
     */
    @GET("/mhw/v1/feed/wm/download.json")
    fun getWatermarkUrl(@Query("id") id: String): Observable<DownloadResult>

    /**
     * 修改视频信息
     */
    @POST("/mhw/v1/feed/update_feed_info.json")
    @FormUrlEncoded
    fun updateVideoInfo(
            @Field("id") id: String,
            @Field("title") title: String? = null,
            @Field("split_time") split_time: Double? = null,
            @Field("type") type: Boolean? = null
    ): Observable<EditVideoResult>

    /**
     * 多类型关注 TimeLine 获取数据  （目前弃用）
     */
    @GET("/mhw/v1/timeline/list.json")
    fun getMultiTypeTimeLineData2(@Query("next_id") next_id: String? = null,
                                  @Query("previous_id") previous_id: String? = null,
                                  @Query("count") count: Int): Observable<MultiTypeTimeLineModel2>

    /**
     * 关注页面 ：TimeLine
     */
    @GET("/mhw/v1/timeline/homeline.json")
    fun getMultiTypeTimeLineData(@Query("next_id") next_id: String? = null,
                                 @Query("previous_id") previous_id: String? = null,
                                 @Query("count") count: Int): Observable<MultiTypeTimeLineModel>

    /**
     * 关注页面：Cards
     */
    @GET("/mhw/v1/timeline/cards.json")
    fun getMultiTypeCardsData(): Observable<List<CardsType>>

    /**
     * 活动卡片/浮标/新人宝箱
     */
    @GET("mhw/v1/publish/available.json")
    fun getPublishActivity(): Observable<PublishActivity>

    /**
     * 获取宝箱信息
     */
    @GET("mhw/v1/chest/info.json")
    fun getChestInfo(): Observable<ChestInfo>

    /**
     * 打开宝箱
     */
    @POST("mhw/v1/chest/open_chest.json")
    @FormUrlEncoded
    fun openChest(@Field("sm_id") smId: String): Observable<ChestInfo>


    @POST("mhw/v1/chest/conversion_chest.json")
    @FormUrlEncoded
    fun conversionChest(@Field("sm_id") smId: String): Observable<AcceptGift>


    /**
     * 获取弹幕
     */
    @GET("mhw/v1/barrage/list/{feed_id}")
    fun getListBarrage(@Path("feed_id") feedId: String?): Observable<ResponseBody>

    /**
     * 添加弹幕
     */
    @POST("mhw/v1/barrage/create.json")
    @FormUrlEncoded
    fun createBarrage(
            @Field("target_id") targetId: String,
            @Field("target_type") targetType: String,
            @Field("content") content: String?,
            @Field("media_ids") mediaIds: String? = null,
            @Field("comment_type") comment_type: String = "BARRAGE",//表情弹幕commentType 枚举类型为 STICKER_BARRAGE 弹幕类型 BARRAGE
            @Field("position") position: Long? = null,
            //暂时不要 @Field("color") color: String = "WHITE",//代表颜色 默认WHITE
            @Field("direction") direction: String = "LEFT"//代表方向，默认LEFT 想左漂浮，支持HEADLINE 置顶
    ): Observable<ApiComment>

    /**
     * 获取话题分类信息
     */
    @GET("mhw/v1/feed/channel/list_category.json")
    fun getTopicCategory(): Observable<List<com.shuashuakan.android.data.api.model.TopicCategory>>

    /**
     * 获取话题分类信息
     */
    @GET("mhw/v1/timeline/has_unread.json")
    fun getFollowUnReadPoint(): Observable<CommonResult>

    /**
     * 分区列表
     */
    @GET("/mhw/v1/feed_category/all.json")
    fun getPartitionTab(): Observable<List<PartitionData>>

    /**
     * 分区列表详情数据
     */
    @GET("/mhw/v1/feed_category/info.json")
    fun getPartition(@Query("id") id: Int): Observable<PartitionResult>


}
