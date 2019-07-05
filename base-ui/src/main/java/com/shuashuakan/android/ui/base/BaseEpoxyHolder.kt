package com.shuashuakan.android.ui.base

import android.view.View
import com.airbnb.epoxy.EpoxyHolder
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Created by twocity on 02/07/2017.
 */
abstract class BaseEpoxyHolder : EpoxyHolder() {
  lateinit var itemView: View

  final override fun bindView(view: View?) {
    this.itemView = view!!
    onBindView(itemView)
  }

  abstract fun onBindView(view: View)
}

fun <V : View> BaseEpoxyHolder.bindView(id: Int)
    : ReadOnlyProperty<BaseEpoxyHolder, V> = Lazy { _, _ ->
  @Suppress("UNCHECKED_CAST")
  this.itemView.findViewById<V>(id)
}

private class Lazy<T, R>(private val initializer: (T, KProperty<*>) -> R) : ReadOnlyProperty<T, R> {
  private object EMPTY

  private var value: Any? = EMPTY

  override fun getValue(thisRef: T, property: KProperty<*>): R {
    if (value == EMPTY) {
      value = initializer(thisRef, property)
    }
    @Suppress("UNCHECKED_CAST")
    return value as R
  }
}