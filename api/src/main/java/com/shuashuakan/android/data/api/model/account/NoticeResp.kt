package com.shuashuakan.android.data.api.model.account

import com.shuashuakan.android.data.api.model.NoticeListBean
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/09/27
 * Description:
 */
@JsonSerializable
data class NoticeResp(
    val message:List<NoticeListBean>
)
