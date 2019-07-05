package com.shuashuakan.android.enums

/**
 * Author:  liJie
 * Date:   2019/1/28
 * Email:  2607401801@qq.com
 */
enum class LoginSource(val source:Int) {
  /**
   * 手机登录且选过兴趣
   */
  SELECTED_INTEREST_MOBILE(1),//"手机登录且选过兴趣"
  /**
   * 微信登录且选过兴趣
   */
  SELECTED_INTEREST_WECHAT(2),//"微信登录且选过兴趣"
  /**
   * 手机登录
   */
  MOBILE(3),// "手机登录"
  /**
   * 微信登录
   */
  WECHAT(4)//"微信登录"
}