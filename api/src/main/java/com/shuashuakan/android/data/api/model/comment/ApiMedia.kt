package com.shuashuakan.android.data.api.model.comment

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

/**
 * Author:  treasure_ct
 * Date:    2018/11/20
 * Description:
 */
@JsonSerializable
data class ApiMedia @KotshiConstructor constructor(
    @Json(name = "id")
    val id: Long,

    @Json(name = "thumb_height")
    val thumbHeight: Int,

    @Json(name = "thumb_width")
    val thumbWidth: Int,

    @Json(name = "thumb_url")
    val thumbUrl: String,

    @Json(name = "media_type")
    val mediaType: String,

    @Json(name = "media_info")
    var mediaInfo: List<ApiMediaInfo>?
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readLong(),
        source.readInt(),
        source.readInt(),
        source.readString(),
        source.readString(),
        source.createTypedArrayList(ApiMediaInfo.CREATOR)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeLong(id)
        writeInt(thumbHeight)
        writeInt(thumbWidth)
        writeString(thumbUrl)
        writeString(mediaType)
        writeTypedList(mediaInfo)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ApiMedia> = object : Parcelable.Creator<ApiMedia> {
            override fun createFromParcel(source: Parcel): ApiMedia = ApiMedia(source)
            override fun newArray(size: Int): Array<ApiMedia?> = arrayOfNulls(size)
        }
    }
}