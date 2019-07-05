package com.shuashuakan.android.data.api.model.partition

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * @author hushiguang
 * @since 2019-06-17.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */
@JsonSerializable
data class PartitionResult(
        @Json(name = "modules")
        val classificationList: List<PartitionModel>?
)