package com.shuashuakan.android.data.api.model.home

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

@JsonSerializable
data class LotteryResult @KotshiConstructor constructor(
        @Json(name = "is_success")
        val success: Boolean,
        @Json(name = "result_message")
        val resultMessage: ResultMessage?,
        @Json(name = "record_id")
        val recordId: Long,
        val action: List<Action>?,
        @Json(name = "user_point")
        val userPoint: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readByte() != 0.toByte(),
            parcel.readParcelable(ResultMessage::class.java.classLoader),
            parcel.readLong(),
            parcel.createTypedArrayList(Action),
            parcel.readInt()
    )

    override fun writeToParcel(
            parcel: Parcel,
            flags: Int
    ) {
        parcel.writeByte(if (success) 1 else 0)
        parcel.writeParcelable(resultMessage, flags)
        parcel.writeLong(recordId)
        parcel.writeTypedList(action)
        parcel.writeInt(userPoint)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<LotteryResult> {
        override fun createFromParcel(parcel: Parcel): LotteryResult {
            return LotteryResult(parcel)
        }

        override fun newArray(size: Int): Array<LotteryResult?> {
            return arrayOfNulls(size)
        }
    }
}




