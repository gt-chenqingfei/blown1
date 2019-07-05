package com.shuashuakan.android.data.api.model.home.multitypetimeline

import android.os.Parcel
import android.os.Parcelable
import com.shuashuakan.android.data.api.model.home.Feed
import com.shuashuakan.android.data.api.runtimeAdapterFactory
import com.squareup.moshi.JsonAdapter
import se.ansman.kotshi.JsonSerializable
import se.ansman.kotshi.KotshiConstructor

/**
 * 多类型关注 TimeLine 数据 Model
 */
@JsonSerializable
data class MultiTypeTimeLineModel2 @KotshiConstructor constructor(
    val timeline: List<Feed>?,
    val cursor: Cursor?,
    val cards: List<CardsType>?
) : Parcelable {
  constructor(source: Parcel) : this(
      source.createTypedArrayList(Feed.CREATOR),
      source.readParcelable<Cursor>(Cursor::class.java.classLoader),
      ArrayList<CardsType>().apply { source.readList(this, CardsType::class.java.classLoader) }
  )

  override fun describeContents() = 0

  override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
    writeTypedList(timeline)
    writeParcelable(cursor, 0)
    writeList(cards)
  }

  companion object {
    @JvmField
    val CREATOR: Parcelable.Creator<MultiTypeTimeLineModel2> = object : Parcelable.Creator<MultiTypeTimeLineModel2> {
      override fun createFromParcel(source: Parcel): MultiTypeTimeLineModel2 = MultiTypeTimeLineModel2(source)
      override fun newArray(size: Int): Array<MultiTypeTimeLineModel2?> = arrayOfNulls(size)
    }
  }
}

@JsonSerializable
data class Cursor @KotshiConstructor constructor(
    val next_id: String?,
    val previous_id: String?
) : Parcelable {
  constructor(source: Parcel) : this(
      source.readString(),
      source.readString()
  )

  override fun describeContents() = 0

  override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
    writeString(next_id)
    writeString(previous_id)
  }

  companion object {
    @JvmField
    val CREATOR: Parcelable.Creator<Cursor> = object : Parcelable.Creator<Cursor> {
      override fun createFromParcel(source: Parcel): Cursor = Cursor(source)
      override fun newArray(size: Int): Array<Cursor?> = arrayOfNulls(size)
    }
  }
}