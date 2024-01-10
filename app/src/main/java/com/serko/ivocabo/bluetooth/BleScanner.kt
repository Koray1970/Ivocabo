package com.serko.ivocabo.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import com.serko.ivocabo.notification.NotificationService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface IBleScanner {
    fun StartScanning()
    fun StopScanning()
}

data class BleScanFilterItem(
    val name: String,
    val macaddress: String,
    var notifyid: Int = 0,
    var stimulable: Boolean = false,
    var onlytrackmydeviceevent: Boolean = false
)

enum class BluetoothScanStates {
    INIT, SCANNING
}

enum class BleScannerResultState { INIT, SCANNING, CONNECTED, DISCONNECTED }
data class BleScannerEventResult(
    val haserror: Boolean = false,
    val exception: String? = null
)

data class BleScannerResult(
    var macaddress: String,
    var rssi: Int? = null,
    var status: BleScannerResultState = BleScannerResultState.INIT,
    var disconnectedCounter: Int = 0,
    var resultpoint: String = ""
)

class BleScanner @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
) : IBleScanner {
    private val TAG = BleScanner::class.java.name
    private val gson = Gson()
    private val bluetoothManager =
        applicationContext.getSystemService(BluetoothManager::class.java) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private var bluetoothLeScanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private val REPORT_DELAY = 2600L
    private val DISCONNECTEDCOUNTER = 12
    private var notifService = NotificationService(applicationContext)


    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setReportDelay(REPORT_DELAY)
        .build()
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)

        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            //Log.v(TAG, "1")
            //Log.v(TAG, "scanFilter = ${gson.toJson(scanFilter)}")
            //Log.v(TAG, "scanResults = ${gson.toJson(scanResults)}")
            //if (scanResults.isNotEmpty()) {
            //scanfilter listesinden kaldirilan ivolar eger scanresult da var ise scanresult listesinden de kaldirilir
            if (scanResults.isNotEmpty())
                scanResults.removeIf { a -> scanFilter.none { g -> g.macaddress.uppercase() == a.macaddress } }
            if (!results.isNullOrEmpty()) {
                //scanfilter listesinde olan ama arama sonuclari listesinde olmayanlar filtrelenir
                //ve scan filterda olmayanlarin disconnectedcounter elemani bir arttirilir
                scanFilter.onEach { g ->
                    val getFromResults = results.filter { a -> a.device.address == g.macaddress }
                    if (getFromResults.isNotEmpty()) {
                        //eger bluetooth tarama sonuclari icinde scanfilter da ki mac adresi var ise
                        //Yani CONNECT OLMUSLAR
                        if (scanResults.any { a -> a.macaddress.uppercase() == g.macaddress.uppercase() }) {
                            //scanresult icinde scan filterdaki mac adress var ise CONNECTED olacak
                            scanResults.first { a -> a.macaddress.uppercase() == g.macaddress.uppercase() }
                                .let {
                                    it.disconnectedCounter = 0
                                    it.status = BleScannerResultState.CONNECTED
                                    it.rssi = getFromResults.first().rssi
                                    it.resultpoint = "G1"
                                }
                        } else {
                            //scanresult icinde scan filterdaki mac adress yok ise YENI ScanResult Eklenecek
                            scanResults.add(
                                BleScannerResult(
                                    macaddress = g.macaddress.uppercase(),
                                    rssi = getFromResults.first().rssi,
                                    status = BleScannerResultState.CONNECTED,
                                    disconnectedCounter = 0,
                                    resultpoint = "G2"
                                )
                            )
                        }
                    } else {
                        //eger bluetooth tarama sonuclari icinde scanfilter da ki mac adresi yok ise
                        //YANI LOST OLMUSLAR
                        if (scanResults.any { a -> a.macaddress.uppercase() == g.macaddress.uppercase() }) {
                            //scanresult icinde scan filterdaki mac adress var ise
                            scanResults.first { a -> a.macaddress.uppercase() == g.macaddress.uppercase() }
                                .let {
                                    it.disconnectedCounter += 1
                                    //it.status = BleScannerResultState.CONNECTED
                                    it.resultpoint = "G3"
                                    if (it.disconnectedCounter >= DISCONNECTEDCOUNTER) {
                                        it.status = BleScannerResultState.DISCONNECTED
                                        it.disconnectedCounter = 0
                                        it.rssi = null
                                        if (g.stimulable) {
                                            it.resultpoint = "G3A"
                                            notifService.showNotification(
                                                g.notifyid,
                                                g.name,
                                                g.macaddress.uppercase()
                                            )
                                        }
                                    }
                                }

                        } else {
                            //scanresult icinde scan filterdaki mac adress yok ise
                            scanResults.add(
                                BleScannerResult(
                                    macaddress = g.macaddress.uppercase(),
                                    rssi = null,
                                    status = BleScannerResultState.SCANNING,
                                    disconnectedCounter = 0,
                                    resultpoint = "G4"
                                )
                            )
                        }
                    }
                }

            }



                /*if (results.any { a -> scanFilter.any { g -> g.macaddress.uppercase() == a.device.address } }) {
                    scanFilter.filter { a -> results.none { g -> g.device.address == a.macaddress.uppercase() } }
                        .onEach { c ->
                            if (scanResults.any { a -> a.macaddress == c.macaddress.uppercase() }) {
                                scanResults.first { g -> g.macaddress == c.macaddress.uppercase() }
                                    .let { h ->
                                        val filt =
                                            results.first { v -> v.device.address == h.macaddress.uppercase() }

                                        h.disconnectedCounter = 0
                                        h.status = BleScannerResultState.CONNECTED
                                        h.rssi = filt.rssi
                                        h.resultpoint = "G1"
                                        if (filt == null) {
                                            if (h.disconnectedCounter >= DISCONNECTEDCOUNTER) {
                                                val filterItem =
                                                    scanFilter.first { g -> g.macaddress == h.macaddress }
                                                h.rssi = null
                                                h.status = BleScannerResultState.DISCONNECTED
                                                h.disconnectedCounter = 0
                                                h.resultpoint = "G2"
                                                if (filterItem.stimulable)
                                                    notifService.showNotification(
                                                        filterItem.notifyid,
                                                        filterItem.name,
                                                        h.macaddress
                                                    )
                                            }
                                        }
                                    }
                            } else {
                                scanResults.add(
                                    BleScannerResult(
                                        macaddress = c.macaddress.uppercase(),
                                        rssi = null,
                                        status = BleScannerResultState.SCANNING,
                                        disconnectedCounter = 0,
                                        resultpoint = "G3"
                                    )
                                )
                            }
                        }
                } else {
                    //scanfilter da ki devicelar result in icinde yok ise

                    scanFilter.onEach { a ->
                        if (scanResults.isNotEmpty()) {
                            if (scanResults.none { g -> g.macaddress.uppercase() == a.macaddress.uppercase() }) {
                                scanResults.add(
                                    BleScannerResult(
                                        macaddress = a.macaddress.uppercase(),
                                        rssi = null,
                                        status = BleScannerResultState.DISCONNECTED,
                                        disconnectedCounter = 0,
                                        resultpoint = "V1"
                                    )
                                )
                            } else {
                                scanResults.first { h -> h.macaddress.uppercase() == a.macaddress.uppercase() }
                                    .let {
                                        it.disconnectedCounter += 1
                                        it.status = BleScannerResultState.SCANNING
                                        it.resultpoint = "V2"
                                        if (it.disconnectedCounter >= DISCONNECTEDCOUNTER) {
                                            it.disconnectedCounter = 0
                                            it.status = BleScannerResultState.DISCONNECTED
                                            it.rssi = null
                                            it.resultpoint = "V3"
                                            if (a.stimulable) {
                                                notifService.showNotification(
                                                    a.notifyid,
                                                    a.name,
                                                    a.macaddress
                                                )
                                            }
                                        }
                                    }
                            }
                        } else {
                            scanResults.add(
                                BleScannerResult(
                                    macaddress = a.macaddress.uppercase(),
                                    rssi = null,
                                    status = BleScannerResultState.DISCONNECTED,
                                    disconnectedCounter = 0,
                                    resultpoint = "V4"
                                )
                            )
                        }
                    }
                }
            } else {
                //results bos ise
                scanResults.onEach { a ->
                    a.disconnectedCounter += 1
                    if (a.disconnectedCounter >= DISCONNECTEDCOUNTER) {
                        val filterItem = scanFilter.first { g -> g.macaddress == a.macaddress }
                        a.rssi = null
                        a.status = BleScannerResultState.DISCONNECTED
                        a.disconnectedCounter = 0
                        if (filterItem.stimulable)
                            notifService.showNotification(
                                filterItem.notifyid,
                                filterItem.name,
                                a.macaddress
                            )
                    }
                }
            }*/
            /*} else {
                scanFilter.onEach { a ->
                    scanResults.add(
                        BleScannerResult(
                            macaddress = a.macaddress.uppercase(),
                            rssi = null,
                            status = BleScannerResultState.DISCONNECTED,
                            disconnectedCounter = 0
                        )
                    )
                }
            }*/

            /*results?.filter { a -> scanFilter.any { g -> g.macaddress == a.device.address } }
                ?.onEach { r ->
                    if (scanResults.none { a -> a.macaddress == r.device.address }) {
                        scanResults.add(
                            BleScannerResult(
                                macaddress = r.device.address,
                                rssi = r.rssi,
                                status = BleScannerResultState.CONNECTED,
                                disconnectedCounter = 0
                            )
                        )
                    } else {
                        scanResults.first { a -> a.macaddress == r.device.address }.let { a ->
                            a.rssi = r.rssi
                            a.disconnectedCounter = 0
                            a.status = BleScannerResultState.CONNECTED
                        }
                    }
                }*/
            Log.v(TAG, "scanResults = ${gson.toJson(scanResults)}")
            /*if (scanResults.isNotEmpty())
                Log.v(TAG, "scanResults = ${gson.toJson(scanResults)}")*/

            //Log.v(TAG, "scanFilter = ${gson.toJson(scanFilter)}")
            /*Log.v(
                TAG,
                "ScanResult = ${gson.toJson(results?.filter { a -> scanFilter.any { g -> g == a.device.address } })}"
            )*/
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.v(TAG, gson.toJson(errorCode))
        }
    }

    @SuppressLint("MissingPermission")
    override fun StartScanning() {
        bluetoothLeScanner.startScan(null, scanSettings, scanCallback)
        Log.v(TAG, "Scan State = Start Scanning")
    }

    @SuppressLint("MissingPermission")
    override fun StopScanning() {
        bluetoothLeScanner.stopScan(scanCallback)
        bluetoothLeScanner.flushPendingScanResults(scanCallback)
        Log.v(TAG, "Scan State = Stop Scanning")
    }

    companion object {
        var scanStatus = mutableStateOf(BluetoothScanStates.INIT)
        var scanFilter = mutableListOf<BleScanFilterItem>()

        //var eventResult = mutableStateOf(BleScannerEventResult())
        var scanResults = mutableListOf<BleScannerResult>()
    }
}