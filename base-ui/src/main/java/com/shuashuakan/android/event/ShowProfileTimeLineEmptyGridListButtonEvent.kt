package com.shuashuakan.android.event

import io.reactivex.internal.operators.maybe.MaybeIsEmpty

/**
 * ProfileTimeLine 空页面禁止网格和列表切换事件
 *
 * Author: ZhaiDongyang
 * Date: 2019/1/18
 */
class ShowProfileTimeLineEmptyGridListButtonEvent(var isEmptyList: Boolean)