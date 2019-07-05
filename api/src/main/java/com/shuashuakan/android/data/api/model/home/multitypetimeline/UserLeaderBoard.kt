package com.shuashuakan.android.data.api.model.home.multitypetimeline

import com.shuashuakan.android.data.api.model.partition.PartitionChainUserItemModel
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

/**
 * @author hushiguang
 * @since 2019-06-25.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */


@JsonSerializable
data class UserLeaderBoard @KotshiConstructor constructor(
        val data: UserLeaderBoardCard?,
        val index: Int?,
        val redirect_url: String?,
        val title: String?,
        val type: String?
) : CardsType

@JsonSerializable
data class UserLeaderBoardCard @KotshiConstructor constructor(
        val list: List<PartitionChainUserItemModel>?
)