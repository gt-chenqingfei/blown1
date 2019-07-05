package com.shuashuakan.android.data.api.model.home

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

@JsonSerializable
data class AwardResponse(
  @Json(name="is_success")
  val isSuccess: Boolean,
  val resultMessage: ResultMessage,
  @Json(name="user_point")
  val userPoint: Int,
  val actions: List<Action>
)

@JsonSerializable
data class ResultMessage @KotshiConstructor constructor(
  val id: Int,
  @Json(name="goods_id")
  val productId: String?,
  val amount: Int?,
  val image: String?,
  val title: String?,
  @Json(name = "sub_title")
  val subTitle:String?,
  val description:String?,
  val type: LotteryType
): Parcelable {
  constructor(parcel: Parcel) : this(
      parcel.readInt(),
      parcel.readString(),
      parcel.readInt(),
      parcel.readString(),
      parcel.readString(),
      parcel.readString(),
      parcel.readString(),
      LotteryType.valueOf(parcel.readString())
  )


  override fun writeToParcel(
    parcel: Parcel,
    flags: Int
  ) {
    parcel.writeInt(id)
    parcel.writeString(productId)
    parcel.writeValue(amount)
    parcel.writeString(image)
    parcel.writeString(title)
    parcel.writeString(subTitle)
    parcel.writeString(description)
    parcel.writeString(type.name)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Creator<ResultMessage> {
    override fun createFromParcel(parcel: Parcel): ResultMessage {
      return ResultMessage(parcel)
    }

    override fun newArray(size: Int): Array<ResultMessage?> {
      return arrayOfNulls(size)
    }
  }

}

