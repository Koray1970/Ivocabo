package com.serko.ivocabo.remote.membership

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class EventResult(
    @SerializedName("error")
    var error: Error?,
    @SerializedName("eventresultflag")
    val eventresultflag: Int
)
enum class EventResultFlags(val flag:Int){
    SUCCESS(0),FAILED(1)
}