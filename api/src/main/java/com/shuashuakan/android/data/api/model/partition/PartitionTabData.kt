package com.shuashuakan.android.data.api.model.partition

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import se.ansman.kotshi.JsonSerializable

/**
 * @author hushiguang
 * @since 2019-06-17.
 * Copyright © 2019 SSK Technology Co.,Ltd. All rights reserved.
 */

@Parcelize
@JsonSerializable
class PartitionData(val name: String?,
                    val id: Int?,
                    val sort: Int?,
                    val status: Int?,
                    val redirect_url: String?,
                    val image_url: String?) : Parcelable