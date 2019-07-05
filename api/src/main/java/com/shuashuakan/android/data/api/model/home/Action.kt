package com.shuashuakan.android.data.api.model.home

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

@JsonSerializable
data class Action @KotshiConstructor constructor(
  //1 商品 2 外链 3 ssr url
  val type: Int,
  val title: String,
  @Json(name="sub_title")
  val subTitle: String?,
  val url: String
): Parcelable {
  constructor(parcel: Parcel) : this(
      parcel.readInt(),
      parcel.readString(),
      parcel.readString(),
      parcel.readString()
  )

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeInt(type)
    parcel.writeString(title)
    parcel.writeString(subTitle)
    parcel.writeString(url)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Creator<Action> {
    override fun createFromParcel(parcel: Parcel): Action {
      return Action(parcel)
    }

    override fun newArray(size: Int): Array<Action?> {
      return arrayOfNulls(size)
    }
  }
}
