package com.shuashuakan.android.spider;

/**
 * @author dev4mobile
 */

public class SpiderEventNames {
    /////////////////////////////////PROGRAM EVENT NAMES//////////////////////////////////////


    /**
     * 当应用切到前台时打点
     */
    static final String FOREGROUND_EVENT = "Foreground";

    /**
     * 当用户点击 push 时打点
     */
    public static final String PUSH_OPENED = "PushOpened";

    /**
     * 当 device id 有变化时
     */
    public static final String Device_ID_Changed = "DeviceIDChanged";

    /**
     * 点赞视频（取消点赞)
     */
    public static final String LIKE = "Like";
    /**
     * 双击视频点赞
     */
    public static final String DOUBLE_LIKE_FEED = "DoubleTapUpFeed";

    /**
     * 评论查看
     */
    public static final String VIEW_COMMENTS = "ViewComments";

    /**
     * 评论
     */
    public static final String COMMENT = "Comment";

    /**
     * 评论点赞
     */
    public static final String COMMENT_SUPPORT = "CommentSupport";

    /**
     * 评论分享
     */
    public static final String COMMENT_SHARE = "CommentShare";
    /**
     * APP分享 推荐给朋友
     */
    public static final String APP_SHARE = "AppShare";

    /**
     * 点击头像查看大图
     */
    public static final String AVATAR_CLICK = "AvatarClick";

    /**
     * 评论弹出窗删除
     */
    public static final String COMMENT_DELETE_CLICK = "CommentDeleteClick";

    /**
     * 评论弹出窗关闭事件
     */
    public static final String COMMENT_POPOVER_CLOSE_CLICK = "CommentPopoverCloseClick";

    /**
     * 首页发现入口点击
     */
    public static final String EXPLORE_EXTRANCE_CLICK = "ExploreExtranceClick";
    /**
     * 话题标签点击
     */
    public static final String CHANNEL_LABEL_SELECTED = "ChannelLabelSelected";
    /**
     * 话题详情页 访问次数
     */
    public static final String CHANNEL_DETAIL_EXPOSURE = "ChannelDetailExposure";

    /**
     * 频道页banner点击
     */
    public static final String CHANNEL_BANNER = "FindBannerClick";

    /**
     * 发现页跳转频道页面
     */
    public static final String EXPLORE_CLICK_CHANNEL_PAGE = "ExploreClickChannelPage";

    /**
     * 视频播放跳转频道页
     */
    public static final String FEED_CLICK_CHANNEL_PAGE = "FeedClickChannelPage";

    /**
     * 个人页跳转
     */
    public static final String PERSON_PAGE = "PersonalPage";
    /**
     * 表情包创建
     */
    public static final String EXPRESSION_CREATE = "ExpressionCreate";
    /**
     * 表情包分享
     */
    public static final String EXPRESSION_SHARE = "ExpressionShare";

    /**
     * 用户关注/取关
     */
    public static final String USER_FOLLOW = "UserFollow";
    /**
     * 频道页分类切换
     */
    public static final String CHANNEL_CATEGORY_CLICK = "ChannelCategoryClick";
    /**
     * 用户绑定
     */
    public static final String BINDING_USER = "BindingUser";
    /**
     * 分享点击
     */
    public static final String SHARE_CLICK = "ShareClick";
    /**
     * 分享详情
     */
    public static final String SHARE_DETAILS = "ShareDetails";

    /**
     * 视频分享资源位曝光
     */
    public static final String FEED_SHARE_CARD_EXPOSURE = "FeedShareCardExposure";

    /**
     * 视频资源分享资源位点击
     */
    public static final String FEED_SHARE_CARD_CLICK = "FeedShareCardClick";
    /**
     * push点击
     */
    public static final String PUSH_CLICK = "PushClick";
    /**
     * 站内消息的展示次数
     */
    public static final String INAPP_MESSAGE_EXPOSURE = "InAppMessageExposure";
    /**
     * 站内消息的点击次数
     */
    public static final String INAPP_MESSAGE_CLICK = "InAppMessageClick";
    /**
     * 活动卡片
     */
    public static final String GAME_CARD_EXPOSURE = "GameCardExposure";
    /**
     * 卡片点击
     */
    public static final String GAME_CARD_CLICK = "GameCardClick";
    /**
     * 活动宝箱的曝光次数
     */
    public static final String ACTIVE_CHEST_EXPOSURE = "ActiveChestExposure";

    /**
     * 开启宝箱点击次数
     */
    public static final String ACTIVE_CHEST_OPEN_CLICK = "AnewChestOpen";
    /**
     * 活动宝箱的领取结果
     */
    public static final String ACTIVE_CHEST_GET_RESULT = "ActiveChestGetResult";
    /**
     * 宝箱浮标的点击次数
     */
    public static final String ACTIVE_CHEST_FLOAT_CLICK = "ActiveChestFloatClick";

    /**
     * 收入囊中点击次数
     */
    public static final String ACTIVE_CHEST_GET_CLICK = "ActiveChestGetClick";

    /**
     * 接龙
     */
    public static final String SOLITAIRE_DETAILS = "solitaireDetails";

    /**
     * 首页兴趣选择
     */
    public static final String HOME_PAGE_INTEREST = "InterestSelection";
    /**
     * 用户发布主视频
     */
    public static final String MASTER_FEED_RELEASE = "MasterFeedRelease";

    /**
     * 榜单曝光
     */
    public static final String LEADER_BOARD_EXPOSURE = "LeaderBoardExposure";
    /**
     * 发现页接龙排行榜视频曝光
     */
    public static final String SOLITAIRE_LEADER_BOARD_FEED_EXPOSURE = "SolitaireLeaderboardFeedExposure";
    /**
     * 发现页接龙排行榜视频点击
     */
    public static final String SOLITAIRE_LEADER_BOARD_FEED_CLICK = "SolitaireLeaderboardFeedClick";
    /**
     * 话题页tab点击
     */
    public static final String CHANNEL_PAGE_TAB_CLICK = "ChannelPageTabClick";

    /**
     * 一条接龙内部视频滑动
     */
    public static final String GROUP_VIDEO_SLIDE = "GroupVideoSlide";


    /**
     * 引导页跳过点击
     */
    public static final String GUIDE_PAGE_LEAP = "GuidePageLeap";

    public static final String GUIDE_NICK_NAME_PAGE_EXPOSURE = "GuideNickNamePageExposure";

    public static final String GUIDE_SEX_PAGE_EXPOSURE = "GuideSexPageExposure";

    public static final String GUIDE_INTEREST_PAGE_EXPOSURE = "GuideInterestPageExposure";

    public static final String GUIDE_PAGE_NEXT_STEP = "GuidePageNextStep";

    public static final String EXPRESSION_COMMENT = "ExpressionComment";

    public static final String BIND_PHONE = "BindPhone";

    public static final String BIND_WECHAT = "BindWeChat";

    public static final String CHANNEL_SUBSCRIPTION = "ChannelSubscription";

    public static final String CHANNEL_SHARE = "ChannelShare";

    public static final String WEB_PAGE_SHARE = "WebPageShare";

    public static final String FEED_BACK_EXPOSURE = "AppFeedbackExposure";
    public static final String FEED_BACK_RESULT = "AppFeedbackResult";

    /**
     * 强插视频活动点击
     */
    public static final String FORCE_FEED_GAME_CLICK = "ForceFeedGameClick";

    /**
     * 更多按钮点击事件
     */
    public static final String MORE_PTION_CLICK = "MoreOptionClick";

    /**
     * 编辑修改button点击事件
     */
    public static final String FEED_EDIT_CLICK = "FeedEditClick";

    /////////////////////////////BUSINESS EVENT NAMES/////////////////////////////////////////////

    private SpiderEventNames() {
        throw new AssertionError("no instance");
    }

    /**
     * 关注动态需要埋点的事件
     */
    public static final String FOLLOW_TIMELINE_PAGETAB_CLICK = "FollowTimelinePageTabClick";
    public static final String FOLLOW_TIMELINE_PAGE_DURATION = "FollowTimelinePageDuration";
    public static final String FOLLOW_TIMELINE_SUBSCRIBED_CHANNEL_CLICK = "FollowTimelineSubscribedChannelClick";
    public static final String FOLLOW_TIMELINE_CHANNEL_NAME_CLICK = "FollowTimelineChannelNameClick";
    public static final String FOLLOW_TIMELINE_RECOMMENDED_CHANNEL_CLICK = "FollowTimelineRecommendedChannelClick";
    public static final String FOLLOW_TIME_LINE_RECOMMENDED_USER_CLICK = "FollowTimelineRecommendedUserClick";
    public static final String FOLLOW_TIMELINE_RECOMMENDED_USER_CLOSE_CLICK = "FollowTimelineRecommendedUserCloseClick";
    public static final String FOLLOW_TIMELINE_FEED_EXPOSURE = "FollowTimelineFeedExposure";
    public static final String FOLLOW_TIMELINE_FEED_PLAY = "FollowTimelineFeedPlay";
    public static final String RECOMMENDED_CHANNEL_EXPOSURE = "RecommendedChannelExposure";
    public static final String RECOMMENDED_USER_EXPOSURE = "RecommendedUserExposure";
    public static final String FOLLOW_TIMELINE_FEED_LOGIN_CLICK = "FollowTimelineFeedLoginClick";

    /**
     * 引导页的点击事件
     */
    public static final String GUIDE_HELP_BUTTON_CLICK = "HelpButtonClick";
    public static final String GUIDE_SKIP_BUTTON_CLICK = "SkipButtonClick";
    public static final String GUIDE_PHONE_LOGIN_BUTTON_CLICK = "PhoneLoginButtonClick";
    public static final String GUIDE_WECHAT_LOGIN_BUTTON_CLICK = "WechatLoginButtonClick";
    public static final String GUIDE_PRIVACY_BUTTON_CLICK = "PrivacyButtonClick";
    public static final String GUIDE_PRIVACY_INFORMATION_SUBSCRIBE_BUTTON_CLICK = "PerfectInformationSubscribeButtonClick";
    public static final String GUIDE_PERFECT_INFORMATION_FINISH_BUTTON_CLICK = "PerfectInformationFinishClick";

    /**
     * 4G 网络是否播放事件
     */
    public static final String TRAFFIC_AUTO_PLAY_ALERT_SHOW = "TrafficAutoPlayAlertShow";


    /**
     * 底部左侧-弹幕输入框—点击行为
     */
    public static final String BARRAGE_INPUT_CLICK = "BarrageInputClick";

    /**
     * 发送弹幕
     */
    public static final String BARRAGE_SENT = "BarrageSent";
    /**
     * 消息气泡曝光
     */
    public static final String UNREAD_MESSAGE_POP_EXPOSURE = "UnreadMessagePopExposure";
    /**
     * 消息气泡点击
     */
    public static final String UNREAD_MESSAGE_POP_CLICK = "UnreadMessagePopClick";

    /**
     * 垂类房间点击事件
     */
    public static class VipRoom {
        public static final String ROOM_OPEN_CLICK = "RoomOpenClick";
        public static final String ROOM_KEY_HELP_CLICK = "RoomKeyHelpClick";
        public static final String ROOM_EXPLORE_CLICK = "RoomExploreClick";
        public static final String ROOM_CLOSE_CLICK = "RoomCloseClick";
    }

    /**
     * 发布成功后，“继续接龙”按钮点击次数
     */
    public static final String CONTINUE_POST_SOLITAIRE_CLICK = "ContinuePostSolitaireClick";


    public static class Program {
        /**
         * 灰度上传事件
         */
        public static final String AB_INTERFACE = "ABInterface";
        /**
         * App启动
         */
        public static final String APP_START = "AppStart";
        /**
         * APP退出的点
         */
        public static final String APP_END = "AppEnd";

        /**
         * 当应用切到后台时打点
         */
        static final String BACKGROUND_EVENT = "Background";

        /**
         * 获取手机上安装了哪些app
         */
        public static final String INSTALLED_APP = "InstalledApplications";
        /**
         * 当用户登录登出时打点
         */
        public static final String LOGIN_STATUS_CHANGED = "LoginStatusChange";

        public static final String RECIVED_REMOTE_PUSH = "RecivedRemotePush";

        public static final String PUSH_REGISTER_ID = "RegisterDeviceToken";

        public static final String SCREEN_SHOT = "ScreenShot";

        public static final String SM_ID_CHANGE = "SmIdChanged";

        /**
         * 当应用切到前台时打点
         */
        static final String FOREGROUND_EVENT = "Foreground";
    }

    public static class Player {
        /**
         * 视频结束播放
         */
        public static final String END_PLAY = "EndPlay";
        /**
         * 视频暂停
         */
        public static final String PAUSE_PLAY = "PausePlay";
        /**
         * 视频暂停
         */
        public static final String RESUME_PLAY = "ResumePlay";
        /**
         * 播放视频切换（视频开始播放）
         */
        public static final String START_PLAY = "StartPlay";

        /**
         * 视频加载失败
         */
        public static final String VIDEO_LOAD_FAILED = "videoLoadFailed";

        /**
         * 视频卡顿检测
         */
        public static final String VIDEO_STAND_STILL = "VideoStandStill";
        /**
         * 视频曝光
         */
        public static final String VIDEO_EXPOSURE = "VideoExposure";
    }

    public static final String ALL_CHANNEL_CATEGORY_CLICK = "AllChannelCategoryClick";
    //我的订阅 入口点击
    public static final String SUBSCRIBE_CLICK = "SubscribeClick";
    // 刷刷酱消息 消息点击
    public static final String SHUASHUA_AUTHOR = "ShuaShuaMsgClick";

    // 发现页面优秀视频集曝光
    public static final String EXPLORE_FEED_LEADER_BOARD_EXPOSURE = "ExploreFeedLeaderboardExposure";
    // 发现页面的分区点击
    public static final String CATEGORY_DID_SELECTED = "CategoryDidSelected";
    // 分区访问次数
    public static final String CATEGORY_DETAIL_EXPOSURE = "CategoryDetailExposure";
    // 分区banner的曝光次数
    public static final String CATEGORY_BANNER_EXPOSURE = "CategoryBannerExposure";
    // 近期热门视频曝光
    public static final String CATEGORY_FEED_LEADER_BOARD_EXPOSURE = "CategoryFeedLeaderboardExposure";
    // 分区banner点击
    public static final String CATEGORY_BANNER_DID_SELECTED = "CategoryBannerDidSelected";
    // 近期热门视频点击
    public static final String CATEGORY_FEED_LEADER_BOARD_DID_SELECTED = "CategoryFeedLeaderboardDidSelected";
    // 新动态话题点击事件
    public static final String CATEGORY_TOPICS_TAG_DID_SELECTED = "CategoryTopicsTagDidSelected";
    // 新动态视频点击事件
    public static final String CATEGORY_TOPICS_FEED_DID_SELECTED = "CategoryTopicsFeedDidSelected";
    // 全部话题列表来源
    public static final String ALL_TOPICS_EXPOSURE = "AllTopicsExposure";
    // 个人页面 分区标签点击次数
    public static final String USER_PROFILE_CATEGORY_CLICK = "UserProfileCategoryClick";
    // 分区界面的明星榜的点击事件
    public static final String CHANNEL_UP_USER_STAR_LIST_ENTRANCE_CLICK = "ChannelUpUserStarListEntranceClick";
    // 分区up主明星榜 访问次数
    public static final String CHANNEL_UP_STAR_EXPOSURE = "ChannelUpUserStarListExposure";
    // 分区up主明星榜 入口点击次数
    public static final String CHANNEL_UP_STAR_CLICK = "ChannelUpUserStarListEntranceClick";
}
