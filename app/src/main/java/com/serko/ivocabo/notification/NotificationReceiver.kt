package com.serko.ivocabo.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.serko.ivocabo.bluetooth.BleScanner
import javax.inject.Inject


class NotificationReceiver @Inject constructor() : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val macaddress = intent?.getStringExtra("macaddress")
        Log.v("BleScanner 2", "macaddress = $macaddress")
        BleScanner.scanFilter.removeIf { a -> a.macaddress == macaddress }
        //MainActivity().RemoveDeviceTracking(macaddress!!)
    }
}