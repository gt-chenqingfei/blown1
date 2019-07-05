package com.shuashuakan.android.event

import com.shuashuakan.android.data.api.model.im.AppCustomMessage

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/10/23
 * Description:
 */
class EditVideoSuccessEvent(val feedId: String, val content: String, val coverUrl: String?,
                            val canEdit: Boolean, val editCount: Int)
