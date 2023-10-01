package com.serko.ivocabo

class SignUpFormHelper {
    val helper=Helper()
    fun checkUsername(username:String):Boolean{
        var result=false
        try {
            result = username.isEmpty()
            result = username.length <= 3
        }
        catch (_:Exception){}
        return result
    }
    fun checkEmail(email:String):Boolean{
        var result=false
        try {
            result = email.isEmpty()
            result = !helper.isEmailValid(email)
        }
        catch (_:Exception){}
            return result
    }
    fun checkPassword(password:String):Boolean{
        var result=false
        try {
            result = password.isEmpty()
            result = !(6..16).contains(password.length)
        }
        catch (_:Exception){}
        return result
    }
}