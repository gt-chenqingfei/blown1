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
data class ApiMediaInfo @KotshiConstructor constructor(
    @Json(name = "width")
    val width: Int,

    @Json(name = "height")
    val height: Int,

    @Json(name = "url")
    val url: String?,

    @Json(name = "clarity_type")
    val clarityType: String
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readInt(),
        source.readInt(),
        source.readString(),
        source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(width)
        writeInt(height)
        writeString(url)
        writeString(clarityType)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ApiMediaInfo> = object : Parcelable.Creator<ApiMediaInfo> {
            override fun createFromParcel(source: Parcel): ApiMediaInfo = ApiMediaInfo(source)
            override fun newArray(size: Int): Array<ApiMediaInfo?> = arrayOfNulls(size)
        }
    }
}