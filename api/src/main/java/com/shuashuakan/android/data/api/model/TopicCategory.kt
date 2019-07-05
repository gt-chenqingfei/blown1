package com.shuashuakan.android.data.api.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import se.ansman.kotshi.JsonSerializable

/**
 *@author: zhaoningqiang
 *@time: 2019/5/23
 *@Description:
 */

@JsonSerializable
class TopicCategory(val id: Long,
                    val name: String,
                    val status: Int?,
                    val sort: Int?,
                    val hot_sign: Boolean?,
                    val feed_channels: List<FeedChannel>)

@Parcelize
@JsonSerializable
class FeedChannel(val cover_url: String?,
                  val name: String,
                  val new_feed_num: Int,
                  var subscribed_count: Int,
                  val total_feed_num: Int,
                  val redirect_url: String?,
                  val channel_icon: String?,
                  val id: Long,
                  var has_subscribe: Boolean,
                  var categroyId: Long? = null) : Parcelable

class EmptyChannel(val title: String, var categroyId: Long? = null)