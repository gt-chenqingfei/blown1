package com.shuashuakan.android.data.api.model

import se.ansman.kotshi.JsonSerializable

/**
 * 新人宝箱数据结构
 */

@JsonSerializable
data class ChestInfo(val title: String?
                     , val status: String?
                     , val content: String?
                     , val expire_at: Long?
                     , val position: Int?
                     , val chest_award_list: List<ChestAwardList>?) {
    fun isEmpty(): Boolean {
        return chest_award_list == null || chest_award_list.isEmpty()
    }
}

@JsonSerializable
data class ChestAwardList(val description: String?
                          , val chest_award_type: String?
                          , val id: Int
                          , val image: String
                          , val title: String)


@JsonSerializable
data class AcceptGift(val result: AcceptGiftResult?
                      /*, val display_msg: String?
                      , val error_code: Int?
                      , val error_msg: String?
                      , val http_code: Int?
                      , val request_uri: String?*/)

@JsonSerializable
data class AcceptGiftResult(val is_success: Boolean
                            , val redirect_url: String)