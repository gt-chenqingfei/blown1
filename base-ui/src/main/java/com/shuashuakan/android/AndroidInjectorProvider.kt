package com.shuashuakan.android

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.support.v4.app.Fragment
import android.text.TextUtils
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidInjectorProvider @Inject constructor(
    private val activityInjector: DispatchingAndroidInjector<Activity>,
    private val fragmentInjector: DispatchingAndroidInjector<Fragment>,
    private val serviceInjector: DispatchingAndroidInjector<Service>,
    private val broadcastInjector: DispatchingAndroidInjector<BroadcastReceiver>
) {

  fun getService(name: String): Any? {
    return when {
      TextUtils.equals(FISH_ACTIVITY_INJECTOR, name) -> activityInjector
      TextUtils.equals(FISH_FRAGMENT_INJECTOR, name) -> fragmentInjector
      TextUtils.equals(FISH_BROADCAST_INJECTOR, name) -> broadcastInjector
      TextUtils.equals(FISH_SERVICE_INJECTOR, name) -> serviceInjector
      else -> null
    }
  }

  companion object {
    private const val FISH_ACTIVITY_INJECTOR = "fish_activity_injector_service"
    // support fragment
    private const val FISH_FRAGMENT_INJECTOR = "fish_fragment_injector_service"
    private const val FISH_BROADCAST_INJECTOR = "fish_broadcast_injector_service"
    private const val FISH_SERVICE_INJECTOR = "fish_service_injector_service"

    fun hasService(name: String): Boolean {
      return TextUtils.equals(FISH_ACTIVITY_INJECTOR, name) || TextUtils.equals(
          FISH_FRAGMENT_INJECTOR, name) || TextUtils.equals(
          FISH_BROADCAST_INJECTOR, name) || TextUtils.equals(FISH_SERVICE_INJECTOR, name)
    }


    @Suppress("UNCHECKED_CAST")
    @SuppressLint("WrongConstant")
    fun activityInjector(context: Context): AndroidInjector<Activity> {
      val service = context.getSystemService(FISH_ACTIVITY_INJECTOR)
      requireNotNull(service)
      return service as AndroidInjector<Activity>
    }

    @Suppress("UNCHECKED_CAST")
    @SuppressLint("WrongConstant")
    fun supportFragmentInjector(context: Context): AndroidInjector<Fragment> {
      val service = context.getSystemService(FISH_FRAGMENT_INJECTOR)
      requireNotNull(service)
      return service as AndroidInjector<Fragment>
    }

    @SuppressLint("WrongConstant")
    @Suppress("UNCHECKED_CAST")
    fun serviceInjector(context: Context): AndroidInjector<Service> {
      val service = context.getSystemService(FISH_SERVICE_INJECTOR)
      requireNotNull(service)
      return service as AndroidInjector<Service>
    }

    @SuppressLint("WrongConstant")
    @Suppress("UNCHECKED_CAST")
    fun broadcastInjector(context: Context): AndroidInjector<BroadcastReceiver> {
      val service = context.getSystemService(FISH_BROADCAST_INJECTOR)
      requireNotNull(service)
      return service as AndroidInjector<BroadcastReceiver>
    }
  }
}