package com.serko.ivocabo.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.text.toUpperCase
import com.serko.ivocabo.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import java.util.Locale
import javax.inject.Inject

interface IBluetoothScanService {
    fun bluetoothScanner(): Flow<MutableList<ScanResult>?>
}

class BluetoothScanService @Inject constructor(@ApplicationContext private val context: Context) :
    IBluetoothScanService {
    val flowListOfMacaddress = flowOf<MutableList<String>>()
    var listOfMacaddress = mutableListOf<String>()
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)
    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var scanSetting: ScanSettings
    private var scanList = mutableListOf<ScanFilter>()
    override fun bluetoothScanner(): Flow<MutableList<ScanResult>?> = callbackFlow {
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter?.isEnabled == false) {
            Toast.makeText(context, context.getString(R.string.enablebluetooth), Toast.LENGTH_LONG)
                .show()
        } else {
            bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner!!
            scanSetting = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setReportDelay(3000)
                //.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build()
            flowListOfMacaddress.flowOn(Dispatchers.Default).collect { mc ->
                if (mc.isNotEmpty()) {
                    mc.forEach { i ->
                        listOfMacaddress.add(i.uppercase(Locale.ROOT))
                    }
                    listOfMacaddress = listOfMacaddress.distinct().toMutableList()
                }
                else{

                }
            }
            scanList = mutableListOf<ScanFilter>()
            listOfMacaddress.forEach {a->
                scanList.add(
                    ScanFilter.Builder()
                        .setDeviceAddress(a.uppercase(Locale.ROOT))
                        .build()
                )
            }
            if (listOfMacaddress.isNotEmpty()) {
                listOfMacaddress = listOfMacaddress.distinct().toMutableList()
                setListofMacaddress()

                listOfMacaddress?.forEach {
                    scanList.add(
                        ScanFilter.Builder()
                            .setDeviceAddress(it.uppercase(Locale.ROOT))
                            .build()
                    )
                }
            }
            //scan islemleri baslar
            val scanCallback: ScanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    super.onScanResult(callbackType, result)
                    //currentRssi.postValue(result?.rssi)
                    //Log.v(TAG, "RSSI : ${result?.rssi}")
                }

                @SuppressLint("MissingPermission")
                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    super.onBatchScanResults(results)

                    //Log.v(BluetoothScanner.TAG, "ScanResults : ${BluetoothScanner.gson.toJson(results)}")

                }

                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                    //Log.v(BluetoothScanner.TAG, "ERROR CODE : $errorCode")
                }
            }
        }

    }
}