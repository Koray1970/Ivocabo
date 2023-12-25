package com.serko.ivocabo

import android.content.Context

data class FormDeviceItem(val id:Int,val name:Int,val image:Int)

class DeviceFormHelper{
    fun FormDeviceList():ArrayList<FormDeviceItem>{
        var devicelist=ArrayList<FormDeviceItem>();
        devicelist.add(FormDeviceItem(1,R.string.t3device,R.drawable.t3_icon_32))
        devicelist.add(FormDeviceItem(2,R.string.e9device,R.drawable.e9_icon_32))
        return devicelist
    }
}
