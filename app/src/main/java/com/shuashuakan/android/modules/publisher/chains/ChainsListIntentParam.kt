package com.shuashuakan.android.modules.publisher.chains

import android.os.Parcelable
import android.support.v7.widget.RecyclerView
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.enums.ChainFeedSource
import com.shuashuakan.android.modules.FeedTransportManager
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/12/11
 * Description:
 */
@Parcelize
class ChainsListIntentParam(
        val feedSource: ChainFeedSource,
        val id: String? = null,
        val position: Int? = 0,
        val feedList: List<Feed>,
        var page: Int = 0,
        val floorFeedId: String? = null,
        val childFeedList: List<Feed>? = null,
        val childEnterPosition: Int = RecyclerView.NO_POSITION,
        val channelId: Long? = null,
        var nextId: String? = null,
        var currentFloorFeed: Feed? = null,
        val fromMark: Int = FeedTransportManager.MARK_FROM_UNDEFINE
) : Parcelable