package com.shuashuakan.android.data.api.model

import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable

/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/09/26
 * Description:
 */
@JsonSerializable
data class TestCaseResp(
    val test_case_comment: Boolean?,
    val test_case_channel_page: Boolean?,
    val test_case_roulette_page: Boolean?,
    val test_case_follow_index: Boolean?,
    val test_case_create_feed:Boolean?,
    val test_case_show_package:Boolean?,
    val test_case_new_display_page:Boolean?,
    val test_case_danmuku:Boolean?
)

