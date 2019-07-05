package com.shuashuakan.android.event

import com.shuashuakan.android.data.api.model.home.Feed

/**
 * Author:  liJie
 * Date:   2019/2/28
 * Email:  2607401801@qq.com
 */
class NotifyChainListEvent (val feedList:ArrayList<Feed>,
                            val hasMore:Boolean)