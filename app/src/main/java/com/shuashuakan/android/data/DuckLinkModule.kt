package com.shuashuakan.android.data

import android.content.Context
import android.content.Intent
import com.shuashuakan.android.modules.account.AccountManager
import com.shuashuakan.android.modules.account.activity.LoginActivity
import com.shuashuakan.android.modules.web.H5Activity
import me.twocities.linker.LinkMetadata
import me.twocities.linker.LinkResolver
import me.twocities.linker.LinkResolver.Interceptor
import me.twocities.linker.annotations.LinkModule
import me.twocities.linker.annotations.LinkResolverBuilder
import okhttp3.HttpUrl
import timber.log.Timber

@LinkModule
interface FishLinkModule

@LinkResolverBuilder(modules = [(FishLinkModule::class)])
interface FishLinkResolverBuilder

private val userScopeUrl = listOf<String>()
const val REDIRECT_LINK = "com.dksheng.android.EXTRA_REDIRECT_LINK"

class LinkResolveLogger : LinkResolver.ResolvedListener {
  override fun onFailure(link: String, reason: String) {
    Timber.tag("Linker").d("Can't resolve $link, for $reason")
  }

  override fun onSuccess(link: String, target: Intent) {
    Timber.tag("Linker").d("Resolve $link into $target")
  }
}

class HttpUrlInterceptor(private val context: Context) : Interceptor {
  override fun intercept(link: String, metadata: LinkMetadata?): Intent? {
    return if (HttpUrl.parse(link) != null) {
      return H5Activity.intent(context, link)
} else null
}
}

/**
 * 处理需要用户登录的页面
 * 如果用户已登录则直接跳转到指定页面，如果没有登录则先调转到登录页面，
 * 登录成功后，LoginActivity 根据 `REDIRECT_LINK` 的值调转到指定页面
 *
 * 需要登录后访问的页面，需要在 `userScopeUrl` 里指定
 */
class UserScopeInterceptor(private val context: Context,
    private val accountManager: AccountManager) : Interceptor {
  override fun intercept(link: String, metadata: LinkMetadata?): Intent? {
    if (!accountManager.hasAccount() && userScopeUrl.any { link.startsWith(it) }) {
      val intent = Intent(context, LoginActivity::class.java)
      intent.putExtra(REDIRECT_LINK, link)
      return intent
    }
    return null
  }

}