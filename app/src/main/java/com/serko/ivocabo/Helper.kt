package com.serko.ivocabo

import android.util.Patterns
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

class Helper {

    fun javaUtilDateToJavaSqlDate(date:java.util.Date):java.sql.Date{
        return java.sql.Date(date.time)
    }
    fun getNOWasDate(): java.util.Date {
        return java.util.Date()
    }

    fun getNOWasString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = java.util.Date()
        return formatter.format(date)
    }

    fun getNOWasTimeStamp(): Long {
        return java.util.Date().time
    }
    fun getNOWasSQLDate(): Date {
        val now = System.currentTimeMillis()
        return Date(now)
    }
    fun unformatedMacAddress(macaddress:String):String{
        if(macaddress.contains(":"))
            return macaddress.replace(":","").replace(" ","").uppercase(Locale.ENGLISH).toString()
        if(macaddress.contains("."))
            return macaddress.replace(".","").replace(" ","").uppercase(Locale.ENGLISH).toString()

        return macaddress.replace(" ","").uppercase(Locale.ENGLISH)
    }
    fun formatedMacAddress(maValue:String):String{
        val pp= maValue.chunked(2)
        return  pp.joinToString(":").uppercase()//macBuilder.toString().uppercase(Locale.ENGLISH)
    }

    fun isEmailValid(email:String):Boolean{
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}