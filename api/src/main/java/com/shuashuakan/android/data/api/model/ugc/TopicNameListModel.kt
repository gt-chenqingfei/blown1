package com.shuashuakan.android.data.api.model.ugc

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

/**
 * Author:  lijie
 * Date:   2018/12/8
 * Email:  2607401801@qq.com
 */
@JsonSerializable
data class TopicNameListModel @KotshiConstructor constructor(
        @Json(name = "back_ground")
        val backGround: String?,
        @Json(name = "channel_icon")
        val channelIcon: String?,
        @Json(name = "create_at")
        val createAt: String,
        val description: String?,
        val id: Long,
        val name: String?,
        val score: Int,
        val status: Int,
        @Json(name = "subscribed_count")
        val subscribedCount: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString() ?: "",
            parcel.readString(),
            parcel.readLong(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(backGround)
        parcel.writeString(channelIcon)
        parcel.writeString(createAt)
        parcel.writeString(description)
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeInt(score)
        parcel.writeInt(status)
        parcel.writeInt(subscribedCount)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TopicNameListModel> {
        override fun createFromParcel(parcel: Parcel): TopicNameListModel {
            return TopicNameListModel(parcel)
        }

        override fun newArray(size: Int): Array<TopicNameListModel?> {
            return arrayOfNulls(size)
        }
    }

}