package com.dkhs.android

import com.squareup.moshi.Types


data class User(val name: String, val id: String)

val userListType = Types.newParameterizedType(List::class.java, User::class.java)