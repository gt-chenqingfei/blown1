package com.shuashuakan.android.modules.track

class ClickAction {

  companion object {
    //频道
    // BANNER
    const val CHANNEL_BANNEL_CLICK = "banner_click"
    // 分类点击
    const val CHANNEL_TAB_BAR_CLICK = "channel_tab_bar_click"

    //用户点
    const val HAS_BIND_MOBILE = "has_bind_mobile"
    const val HAS_BIND_WX = "has_bind_wx"
    const val INIT_HAS_BIND_MOBILE = "init_has_bind_mobile"
    const val INIT_HAS_BIND_WX = "init_has_bind_wx"

    const val SAVE_PROFILE_CLICK = "save_profile_click"

    const val SUCCESS = "success"

    //share
    //属性定义
    const val ACTION_SHARE = "share_feed"
    const val FEED_ID = "feed_id"
    const val SHARE_TYPE = "share_type"
    const val CHANNEL_NAME = "channel_name"
    const val LOGIN_TYPE = "login_type"
    const val DURATION = "duration"
    const val FEED_PAGE_TYPE = "feed_page_type"
    const val SHARE_CHANNEL = "share_channel"
    const val SHARE_SUCCESS = "share_success"
    const val PAGE_VIEW_TIME = "page_view_time"
    const val VIDEO_PLAY_DURATION = "video_play_duration"
    //分享渠道
    const val WX_FRIEND = "wx_friend"
    const val WX_MOMENT = "wx_moment"
    const val QQ_FRIEND = "qq_friend"
    const val QZONE = "qzone"
    const val COPY_URL = "copy_url"
    const val BROWSER = "browser"
    const val REPORT = "report"
    const val UN_LIKE = "un_like"
    //home tab click
    const val TAB_CLICK = "tab_click"
    //奖品领取提示
    const val AWARD_ALERT_SHOW = "award_alert_show"
    //抽奖
    const val ROULETTE_FINISH = "roulette_finish"

    //弹窗商品停留点
    const val FEED_PRESENT_VC_TRACK = "feed_present_vc_track"

    //点击抽奖次数
    const val START_ROULETTE_CLICK = "start_roulette_click"

    //点击视频商品图标的次数
    const val FEED_PRODUCT_VIEW = "feed_product_view"
    //开始播放视频打点
    const val FEED_START_PLAY_VIDEO = "start_play_video"
    //登陆成功打点
    const val LOGIN_SUCCESS = "login_success"
  }

  enum class FeedSource(val source: String) {
    HOMEPAGE("homepage"),
    CHANNEL_SCROLL_LIST("channel_scroll_list"),
    CHANNEL_PAGE("channel_page"),
    EXPLORE_LIST("explore"),
    FAV_LIST("fav_list"),
    LIKE_LIST("like_list"),
    PUBLISH_LIST("publish"),
    FEED_PROFILE("feed_profile"),
    FEED_GOODS("feed_goods"),
    FEED_LIST("feed_list"),
    CHAINS_FEED_LIST("chains_feed_list")
  }
}