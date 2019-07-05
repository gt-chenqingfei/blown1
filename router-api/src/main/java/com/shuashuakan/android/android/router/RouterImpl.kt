package com.shuashuakan.android.android.router

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.shuashuakan.android.android.router.Router.Companion.REFERRER
import me.twocities.linker.LinkResolver
import me.twocities.linker.Result
import me.twocities.linker.annotations.LINK
import timber.log.Timber

internal
class RouterImpl constructor(private val linkResolver: LinkResolver) : Router {

  override fun resolve(link: String): Result = linkResolver.resolve(link)

  private fun resolveInto(link: String, success: (Intent) -> Unit) {
    val result = resolve(link)
    if (result.success) success(result.intent!!)
    else Timber.e("Can't resolve $link")
  }

  override fun startActivity(context: Context, link: String?, block: Intent.() -> Unit) {
    link?.let {
      resolveInto(it) { intent ->
        startSafely {
          block(intent)
          if (context !is Activity && (intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) == 0) {
            if(link != "ssr://oauth2/login") {
              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            Timber.tag("Router").w(
                "WARNING: You're starting activity outside of an activity, FLAG_ACTIVITY_NEW_TASK was added automatically")
          }
          if(context is Activity) {
            context.intent.getStringExtra(LINK)?.let {
              intent.putExtra(REFERRER, it)
            }
          }
          context.startActivity(intent)
        }
      }
    }
  }

  override fun startActivityForResult(context: Activity, link: String?, requestCode: Int,
      block: Intent.() -> Unit) {
    link?.let {
      resolveInto(it) { intent ->
        startSafely {
          block(intent)
          context.startActivityForResult(intent, requestCode)
        }
      }
    }
  }

  private fun startSafely(starter: () -> Unit) {
    try {
      starter()
    } catch (e: Exception) {
      Timber.e(e)
    }
  }

}