package com.shuashuakan.android.utils.extension

fun String?.getRealId(): String? {
    return if (this == null) {
        this
    } else {
        this.split('l').getOrNull(0) ?: this
    }
}
