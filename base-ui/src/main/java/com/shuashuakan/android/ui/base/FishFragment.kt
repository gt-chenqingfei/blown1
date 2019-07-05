package com.shuashuakan.android.ui.base

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.Fragment
import com.shuashuakan.android.FishInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

abstract class FishFragment : Fragment(), HasSupportFragmentInjector {
  @Inject lateinit var childFragmentInjector: DispatchingAndroidInjector<Fragment>

  override fun onAttach(context: Context?) {
    FishInjection.inject(this)
    super.onAttach(context)
  }

  override fun supportFragmentInjector(): AndroidInjector<Fragment> = childFragmentInjector

  fun goActivityWithAnim(intent: Intent) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity).toBundle())
    } else {
      startActivity(intent)
    }
  }
}