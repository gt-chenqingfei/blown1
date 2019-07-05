package com.shuashuakan.android.data.api.model

import se.ansman.kotshi.JsonSerializable

/**
 * 获取活动卡片，活动浮标bean
 */
@JsonSerializable
data class PublishActivity(val homepage_card: HomePageCard?
                           , val homepage_icon: HomePageIcon?
                           , val feed_share: FeedShare?)

@JsonSerializable
data class HomePageCard(val card_list: List<Card>?)

@JsonSerializable
data class Card(val button_background_color: String
                , val button_text: String
                , val button_text_color: String
                , val expire_at: Long
                , val frequency: String
                , val id: Long
                , val image_url: String
                , val position: Int
                , val redirect_url: String
                , val card_style: String?
                , val title: String?
                , val description: String?)

@JsonSerializable
data class HomePageIcon(val business_type: String
                        , val expire_at: Long
                        , val frequency: String
                        , val id: Long
                        , val image_url: String
                        , val redirect_url: String)

@JsonSerializable
data class FeedShare(
        val expire_at: Long
        , val frequency: String
        , val id: Long
        , val image_id: Long
        , val image_url: String
        , val position: String
        , val redirect_url: String)

enum class FeedSharePosition(val position: String) {
    left("LEFT"),
    right("RIGHT"),
    center("MID"),
    full("TILE");
}

