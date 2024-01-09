package com.serko.ivocabo.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.serko.ivocabo.Helper
import com.serko.ivocabo.bluetooth.BleScanner
import com.serko.ivocabo.data.AppDatabase
import com.serko.ivocabo.data.UserDao
import com.serko.ivocabo.data.UserRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


class NotificationReceiver @Inject constructor() : BroadcastReceiver() {
    private val helper = Helper()
    override fun onReceive(context: Context?, intent: Intent?) {
        val macaddress = intent?.getStringExtra("macaddress")
        Log.v("BleScanner 2", "macaddressDD = $macaddress")

        //remove from database
        Thread{
            val database = AppDatabase.getInstance(context!!).userDao()
            val getuser = database.fetchUser()
            val getDevices = helper.deviceStringToTypeClass(database.getDevices())
            if (getDevices != null)
                if (getDevices.isNotEmpty()) {
                    getDevices.first { a -> a.macaddress.uppercase() == macaddress?.uppercase() }.istracking =
                        null
                    getuser.devices = helper.deviceTypeClassToString(getDevices)
                    database.update2(getuser)
                }
        }.start()
        val bleScanner = BleScanner(context!!)
        bleScanner.StopScanning()
        BleScanner.scanFilter.removeIf { a -> a.macaddress == macaddress }
        if (BleScanner.scanResults.isNotEmpty()) {
            BleScanner.scanResults.removeIf { a -> a.macaddress.uppercase() == macaddress?.uppercase() }
        }

        if (BleScanner.scanFilter.isNotEmpty())
            bleScanner.StartScanning()

    }
}