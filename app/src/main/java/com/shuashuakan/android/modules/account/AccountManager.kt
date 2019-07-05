package com.shuashuakan.android.modules.account

import androidx.content.edit
import com.shuashuakan.android.commons.cache.DiskCache.Cache
import com.shuashuakan.android.commons.cache.MemorizedCache
import com.shuashuakan.android.commons.cache.Storage
import com.shuashuakan.android.data.api.model.account.Account
import com.shuashuakan.android.network.AccessTokenInterceptor.AccessTokenProvider
import timber.log.Timber
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("unused", "MemberVisibilityCanPrivate")
@Singleton
class AccountManager @Inject constructor(val storage: Storage) : AccessTokenProvider {
    companion object {
        private const val USER_CACHED_USERID = "user_account_userid"
        private const val USER_CACHED_TOKEN = "user_account_token"

        private const val USER_CACHED_KEY = "user_account_key"
    }

    private val logging = object : AccountChangedListener {
        override fun onAccountChanged(before: Account?, after: Account?) {
            Timber.tag("AccountManager").i("account changed: $before to $after")
        }
    }

    private val accountChangedListener: MutableList<AccountChangedListener> = CopyOnWriteArrayList()

    private val accountCache by lazy {
        val cache: Cache<Account> = MemorizedCache.wrap(storage.userCacheOf())
        cache
    }

    private val accountPreference by lazy {
        val cache = storage.userPreference
        cache
    }

    init {
        addAccountChangedListener(logging)
    }

    @Synchronized
    fun updateAccount(account: Account) {
        val cached = account()

        val changed = if (cached == null) true else cached.userId != account.userId

        if (changed) {
            accountPreference.edit(commit = true) {
                putLong(USER_CACHED_USERID, account.userId ?: 0)
                putString(USER_CACHED_TOKEN, account.accessToken)
            }
            // notify
            accountChangedListener.forEach { it.onAccountChanged(cached, account) }
        }
    }

    fun account(): Account? {
        return if (accountPreference.getString(USER_CACHED_TOKEN, "")?.isNotEmpty() == true && accountPreference.getLong(USER_CACHED_USERID, 0L) > 0L) {
            Account(accountPreference.getLong(USER_CACHED_USERID, 0L), accountPreference.getString(USER_CACHED_TOKEN, ""))
        } else {
            if (accountCache.get(USER_CACHED_KEY).orNull() != null) {
                accountPreference.edit(commit = true) {
                    putLong(USER_CACHED_USERID, accountCache.get(USER_CACHED_KEY).orNull()?.userId
                            ?: 0)
                    putString(USER_CACHED_TOKEN, accountCache.get(USER_CACHED_KEY).orNull()!!.accessToken)
                }
                accountCache.get(USER_CACHED_KEY).orNull()
            } else {
                null
            }
        }
    }

    fun hasAccount(): Boolean = account() != null

    @Synchronized
    fun logout() {
        val cached = account()
        accountPreference.edit(commit = true) {
            putLong(USER_CACHED_USERID, 0L)
            putString(USER_CACHED_TOKEN, "")
        }
        accountCache.remove(USER_CACHED_KEY)
        // notify user
        accountChangedListener.forEach { it.onAccountChanged(cached, null) }
    }

    fun addAccountChangedListener(l: AccountChangedListener) {
        accountChangedListener.add(l)
    }

    fun removeAccountChangedListener(l: AccountChangedListener) {
        accountChangedListener.remove(l)
    }

    override fun accessToken(): String? {
        val acc = account()
        return if (acc != null) {
            acc.accessToken
        } else {
            null
        }
    }

    interface AccountChangedListener {
        /**
         * before 为 null 表示之前没有用户登录, after 为 null 表示用户登出
         */
        fun onAccountChanged(before: Account?, after: Account?)
    }
}