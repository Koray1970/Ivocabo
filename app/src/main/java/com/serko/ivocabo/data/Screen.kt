package com.serko.ivocabo.data


sealed class Screen(val route:String){
    object Signup:Screen("signup")
    object Signin:Screen("signin")
    object ForgetPassword:Screen("forgetpassword")
    object Dashboard:Screen("dashboard")
    object DeviceForm:Screen("deviceform")
    object DeviceDashboard:Screen("devicedashboard/{macaddress}")
}
