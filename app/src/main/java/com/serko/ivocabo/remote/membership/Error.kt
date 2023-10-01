package com.serko.ivocabo.remote.membership

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Error(
    @SerializedName("code")
    val code: String,
    @SerializedName("exception")
    val exception: String,
    @SerializedName("func")
    val func: String
)