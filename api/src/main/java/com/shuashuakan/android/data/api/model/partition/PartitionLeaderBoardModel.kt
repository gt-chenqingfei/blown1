package com.shuashuakan.android.data.api.model.partition

import com.shuashuakan.android.data.api.model.home.Feed
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * @author hushiguang
 * @since 2019-06-19.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */

@JsonSerializable
class PartitionLeaderBoardModel(
        @Json(name = "data_list")
        val dataList: List<Feed>,
        val type: String?,
        val title: String?
) : PartitionModel
