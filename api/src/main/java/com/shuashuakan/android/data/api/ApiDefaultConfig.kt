package com.shuashuakan.android.data.api

class ApiDefaultConfig {
    companion object {
        const val TEST_API = "https://opentest.seriousapps.cn"
        const val DEFAULT_URL = "https://open.seriousapps.cn"
        const val UPLOAD_URL = "https://upload.seriousapps.cn"
        const val TEST_UPLOAD_URL = "https://uploadtest.seriousapps.cn"
        const val CLIENT_KEY = "100031"
        const val CLIENT_SECRET = "0d2e7fde02a764eb87189f7e9531e1db"
        const val USER_SOURCE = "2"
        const val SERVER_URL = "https://log.ricebook.com/sa?project=ssk"
        const val CONFIGURE_URL = "https://log.ricebook.com/config/?project=ssk"
        const val TEST_SERVER_URL = "https://log.ricebook.com/sa?project=default"
        const val TEST_CONFIGURE_URL = "https://log.ricebook.com/config/?project=default"
        const val PID = "mm_127005907_43910603_383446703"
    }
}

enum class OpenPlatformType(val value: String) {
    Fish("1"),
    duck("4")
}
