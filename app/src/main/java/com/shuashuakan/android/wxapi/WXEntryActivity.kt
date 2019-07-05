package com.shuashuakan.android.wxapi

import com.shuashuakan.android.data.RxBus
import com.shuashuakan.android.event.WeChatBindEvent
import com.shuashuakan.android.utils.startActivity
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.modelmsg.ShowMessageFromWX
import com.tencent.mm.opensdk.modelmsg.WXAppExtendObject
import com.umeng.socialize.weixin.view.WXCallbackActivity
import org.json.JSONObject

/**
 * Created by dev4mobile on 3/22/18.
 */
class WXEntryActivity : WXCallbackActivity() {

    companion object {
        private const val LOGIN_TYPE = 1
    }

    override fun onReq(req: BaseReq) {
        when (req.type) {
            ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX -> {
                if (req is ShowMessageFromWX.Req) {
                    val wxMsg = req.message
                    val obj = wxMsg.mediaObject as WXAppExtendObject
                    if (isJSON(obj.extInfo)) {
                        val jsonObject = JSONObject(obj.extInfo)
                        val redirect = jsonObject.optString("redirect_url")
                        val autoRedirect = jsonObject.optString("auto_redirect")
                    } else {
                        if (obj.extInfo.startsWith("ssr://")) {
                            startActivity(obj.extInfo)
                        }
                    }

                    /* startActivity(redirect) {
                       putExtra(ProductDetailActivity.EXTRA_AUTO_REDIRECT, autoRedirect)
                     }*/
                }
            }
        }
        super.onReq(req)
    }

    override fun onResp(resp: BaseResp) {
        if (resp.type == LOGIN_TYPE) {
            when (resp.errCode) {
//                BaseResp.ErrCode.ERR_USER_CANCEL ->
//                    showLongToast("用户取消")
                BaseResp.ErrCode.ERR_OK -> {
                    if (resp is SendAuth.Resp && resp.state == "login") {
//                        LoginActivity.launchByWeChat(this, resp.state, resp.code)
                        RxBus.get().post(WeChatBindEvent(resp.state, resp.code))
                    } else if (resp is SendAuth.Resp && resp.state == "getWxInfo") {
                        RxBus.get().post(WeChatBindEvent(resp.state, resp.code))
                    }
                }
            }
            finish()
        } else {
            super.onResp(resp)
        }
    }

    fun isJSON(str: String): Boolean {
        return try {
            JSONObject(str)
            true
        } catch (e: Exception) {
            false
        }

    }
}