package com.shuashuakan.android.modules.account

import com.shuashuakan.android.data.api.model.account.ChannelModel

/**
 * @author hushiguang
 * @since 2019-05-10.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */

enum class ProfileSource {
    NONE,
    MALE,
    FEMALE;

    interface IProfilePerfectCallback {
        fun onNext()
        fun getTopicListData(): List<ChannelModel>?
    }
}