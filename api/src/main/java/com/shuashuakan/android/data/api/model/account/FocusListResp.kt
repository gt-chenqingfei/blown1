package com.shuashuakan.android.data.api.model.account

import se.ansman.kotshi.JsonSerializable

/**
 * Created by lijie on ${Date} 上午11:51
 */
@JsonSerializable
data class FocusListResp(
    val message : List<FocusModel>
)