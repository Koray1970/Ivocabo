package com.serko.ivocabo.remote.membership

data class SignInRequest(
    val password: String,
    val username: String
)