package com.serko.ivocabo.remote.membership

data class SignUpRequest(
    val email: String,
    val password: String,
    val username: String
)