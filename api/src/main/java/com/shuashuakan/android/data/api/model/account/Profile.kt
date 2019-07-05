package com.shuashuakan.android.data.api.model.account

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.squareup.moshi.Json
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

@JsonSerializable
data class Profile(
  @Json(name = "user_info")
  val userInfo: UserInfo,
  val groups: List<List<Setting>>,
  @Json(name = "coin")
  val coinAmount: Int,
  @Json(name = "money")
  val moneyAmount: Int,
  @Json(name = "wallet_url")
  val walletUrl: String
)

@JsonSerializable
data class UserInfo @KotshiConstructor constructor(
  @Json(name = "nick_name")
  val nickName: String,
  @Json(name = "avatar")
  val avatarUrl: String,
  val birthday: Long?,
  val gender: String?,
  val mobile: String
): Parcelable {
  constructor(parcel: Parcel) : this(
      parcel.readString()?:"",
      parcel.readString()?:"",
      parcel.readValue(Long::class.java.classLoader) as? Long,
      parcel.readString(),
      parcel.readString()?:""
  )
  override fun writeToParcel(
    parcel: Parcel,
    flags: Int
  ) {
    parcel.writeString(nickName)
    parcel.writeString(avatarUrl)
    parcel.writeValue(birthday)
    parcel.writeString(gender)
    parcel.writeString(mobile)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Creator<UserInfo> {
    override fun createFromParcel(parcel: Parcel): UserInfo {
      return UserInfo(parcel)
    }

    override fun newArray(size: Int): Array<UserInfo?> {
      return arrayOfNulls(size)
    }
  }
}

@JsonSerializable
data class Setting(
  @Json(name = "left_title")
  val leftTitle: String,
  @Json(name = "right_title")
  val rightTitle: String,
  @Json(name = "right_title_color")
  val rightTitleColor: String,
  @Json(name = "redirect_url")
  val redirectUrl: String,
  @Json(name = "has_red_point")
  val hasRedPoint: Boolean
)
