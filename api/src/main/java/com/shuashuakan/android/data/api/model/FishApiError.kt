package com.shuashuakan.android.data.api.model

data class FishApiError(val errorCode: Long, val errorMsg: String) {

  enum class ApiErrorType(val errorCode: Long, val errorMsg: String) {
      ERROR_PARAMETER (ERROR_PARAMETER_CODE, "参数错误"),
      LOSE_PARAMETER (LOSE_PARAMETER_CODE,"缺少参数"),
      TOKEN_EXPIRE (TOKEN_EXPIRE_CODE,"token 已过期，请重新登录"),
      TOKEN_INVALID (TOKEN_INVALID_CODE,"登录信息有误，请退出后重新登录"),
      VERIFY_CODE_INVALID (VERIFY_CODE_INVALID_CODE,"手机验证码错误"),
      APP_VERSION_TOO_LOW (APP_VERSION_TOO_LOW_CODE,"App 版本过低，请升级最新版本"),
      USER_NOT_EXIST (USER_NOT_EXIST_CODE,"用户不存在"),
      NICK_NAME_HAS_BEEN_USED (NICK_NAME_HAS_BEEN_USED_CODE,"该昵称已被使用"),
      PHONE_NUMBER_HAS_BEEN_REGISTERED (PHONE_NUMBER_HAS_BEEN_REGISTERED_CODE,"手机号已注册"),
      MOBIL_PHONE_IS_EXIST (MOBIL_PHONE_IS_EXIST_CODE,"手机号已存在"),
      SEND_TOO_MANY_VERIFY (SEND_TOO_MANY_VERIFY_CODE,"发送验证码次数过多，请稍后再试"),
      HTTP_METHOD_NOT_ALLOW (HTTP_METHOD_NOT_ALLOW_CODE,"API 请求方式错误"),
      VERIFY_SIGNATURE_FAILED (VERIFY_SIGNATURE_FAILED_CODE,"请求参数签名错误"),
      ANTI_CHEAT_ERROR (ANTI_CHEAT_ERROR_CODE,"设备异常"),
      NOT_BIND_MOBILE_PHONE (NOT_BIND_MOBILE_PHONE_CODE,"没有绑定手机号"),
      DAILY_REWARD_LIMIT (DAILY_REWARD_LIMIT_CODE,"观看时长金币奖励已达每日上限"),
      ILLEGAL_WORD_ERROR (ILLEGAL_WORD_ERROR_CODE,"因包含敏感词提交失败");
  }

  companion object {
    const val ERROR_PARAMETER_CODE = 4000001L  // 参数错误
    const val LOSE_PARAMETER_CODE=4000002L  // 缺少参数
    const val TOKEN_EXPIRE_CODE=4030003L   // token已过期
    const val TOKEN_INVALID_CODE=4030004L    // token错误
    const val VERIFY_CODE_INVALID_CODE=4030009L // 手机验证码错误
    const val APP_VERSION_TOO_LOW_CODE=4030015L   // 当前版本号过低
    const val USER_NOT_EXIST_CODE=4040004L   // 用户不存在
    const val NICK_NAME_HAS_BEEN_USED_CODE=4040006L  // 昵称已被使用
    const val PHONE_NUMBER_HAS_BEEN_REGISTERED_CODE=4040010L // 手机号码已经注册
    const val MOBIL_PHONE_IS_EXIST_CODE=4040011L   // 手机号已存在
    const val SEND_TOO_MANY_VERIFY_CODE=4040413L  // 发送验证码次数过多
    const val HTTP_METHOD_NOT_ALLOW_CODE=4050001L  // 接口请求方式错误
    const val VERIFY_SIGNATURE_FAILED_CODE=4030012L   // 签名错误
    const val ANTI_CHEAT_ERROR_CODE=4030016L  // 数美校验错误
    const val NOT_BIND_MOBILE_PHONE_CODE=40456001L  // 没有绑定手机号
    const val DAILY_REWARD_LIMIT_CODE=40456065L  // 每日金币奖励已达上限
    const val ILLEGAL_WORD_ERROR_CODE=40456073L  // 触发违禁词错误

    fun messageLookup(errorCode: Long): String? {
      return ApiErrorType.values()
          .filter { it.errorCode == errorCode }.takeIf { it.isNotEmpty() }?.get(0)?.errorMsg
    }
  }
}