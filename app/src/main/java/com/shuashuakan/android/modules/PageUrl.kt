package com.shuashuakan.android.modules

const val ACCOUNT_PAGE = "ssr://oauth2/login"
const val HOME_PAGE = "ssr://home?index=homepage"
const val HOME_PAGE2 = "ssr://home2?index=homepage"
const val MY_PAGE = "ssr://home?index=my"
const val H5_PAGE = "ssr://home?index=web&url="
const val MY_FAVORITE = "ssr://my/favorite"
const val SETTING_PAGE = "ssr://my/setting"
const val SETTING_PROFILE= "ssr://my/setting/user_info"
const val SMS_INVITE = "ssr://sms/invite"
const val CHANNEL_PAGE = "ssr/home?index=channel"
const val FAV_LINK="https://app.shuashuakan.net/favgoods"
const val JOIN_PAGE="https://topic.shuashuakan.net/join.html"
// UP主明星榜
const val SSR_UP_STAR_RANK = "ssr://category/userleaderboard"

fun getPageSsr(position: Int): String {
  return when (position) {
    0 -> {
      return HOME_PAGE
    }
    1 -> {
      return CHANNEL_PAGE
    }
    2 -> {
      return MY_PAGE
    }
    else -> HOME_PAGE
  }
}