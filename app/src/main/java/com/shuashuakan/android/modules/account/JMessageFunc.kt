package com.shuashuakan.android.modules.account

import cn.jpush.im.android.ErrorCode
import cn.jpush.im.android.api.ChatRoomManager
import cn.jpush.im.android.api.JMessageClient
import cn.jpush.im.android.api.callback.RequestCallback
import cn.jpush.im.android.api.model.ChatRoomInfo
import cn.jpush.im.android.api.model.Conversation
import cn.jpush.im.api.BasicCallback
import com.shuashuakan.android.utils.StringUtils
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Author:  Chenglong.Lu
 * Email:   1053998178@qq.com | w490576578@gmail.com
 * Date:    2018/09/26
 * Description:
 */
@Singleton
class JMessageFunc @Inject constructor(private val accountManager: AccountManager) {


    fun logout() {
        JMessageClient.logout()
    }

    fun loginIM(loginSuccess: () -> Unit) {
        val account = accountManager.account()
        if (account != null) {
            if (JMessageClient.getMyInfo() != null) {
                login(account.userId.toString(), loginSuccess)
            } else {
                register(account.userId.toString(), loginSuccess)
            }
        }
    }

    fun enterChatRoom(chatRoomIds: List<Long>) {
        if (chatRoomIds.isNotEmpty()) {
            enterChatRoom(chatRoomIds[0])
        }
    }

    private fun enterChatRoom(chatRoomId: Long) {
        ChatRoomManager.enterChatRoom(chatRoomId, object : RequestCallback<Conversation>() {
            override fun gotResult(p0: Int, p1: String?, p2: Conversation?) {
                if (p0 == ErrorCode.NO_ERROR || p0 == 851003) {
                    Timber.d("IMJoinChatRoom id:$chatRoomId,加入成功")
                } else {
                    Timber.e("IMJoinChatRoom：id:$chatRoomId,ErrorCode:$p0,Message:$p1")
                }
                goOutChatRoom(chatRoomId)
            }
        })
    }

    private fun quitChatRoom(chatRoomId: Long, quitSuccess: () -> Unit) {
        ChatRoomManager.leaveChatRoom(chatRoomId, object : BasicCallback() {
            override fun gotResult(p0: Int, p1: String?) {
                if (p0 == ErrorCode.NO_ERROR) {
                    quitSuccess.invoke()
                }
            }
        })
    }

    private fun goOutChatRoom(chatRoomId: Long) {
        ChatRoomManager.getChatRoomListByUser(object : RequestCallback<List<ChatRoomInfo>>() {
            override fun gotResult(p0: Int, p1: String?, p2: List<ChatRoomInfo>?) {
                if (p2 != null) {
                    for (item in p2) {
                        if (chatRoomId != item.roomID) {
                            quitChatRoom(item.roomID) {
                                Timber.e("退出${item.roomID}聊天室成功")
                            }
                        }
                    }
                }
            }
        })

    }


    private fun login(uid: String, loginSuccess: () -> Unit) {
        JMessageClient.login(uid, StringUtils.md5(uid), object : BasicCallback() {
            override fun gotResult(p0: Int, p1: String?) {
                if (p0 == ErrorCode.NO_ERROR) {
                    Timber.d("IM登录成功")
                    loginSuccess.invoke()
                } else {
                    Timber.e("IM登录失败：ErrorCode:$p0,Message:$p1")
                }
            }
        })
    }

    private fun register(uid: String, loginSuccess: () -> Unit) {
        JMessageClient.register(uid, StringUtils.md5(uid), object : BasicCallback() {
            override fun gotResult(p0: Int, p1: String?) {
                if (p0 == ErrorCode.NO_ERROR || p0 == 898001) {
                    login(uid, loginSuccess)
                } else {
                    Timber.e("IM注册失败: ErrorCode:$p0,Message:$p1")
                }
            }
        })
    }

}
