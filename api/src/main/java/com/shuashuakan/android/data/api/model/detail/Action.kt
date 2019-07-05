package com.shuashuakan.android.data.api.model.detail

enum class Action(val value: Int) {
  ActionViewDetail(1),
  ActionShare(2),
  ActionGetCoupon(4),
  ActionPay(5);

  override fun toString(): String {
    return value.toString()
  }
}
