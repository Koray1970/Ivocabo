package com.serko.ivocabo.data

import android.content.Context
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.serko.ivocabo.Helper
import com.serko.ivocabo.bluetooth.BleScanFilterItem
import com.serko.ivocabo.bluetooth.BleScanner
import com.serko.ivocabo.bluetooth.BleScannerResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.typeOf

@HiltViewModel
class BleScanViewModel @Inject constructor(
    @ApplicationContext val applicationContext: Context,
    private val repo: UserRepository
) : ViewModel() {
    private val helper = Helper()
    private val gson = Gson()
    private var _devices = mutableListOf<Device>()

    private var _scanDevices = mutableListOf<String>()
    fun scanDevices() = flow<List<String>> {
        while (true) {
            val dbDeviceList = repo.getDevices()
            //println(dbDeviceList)
            if (!dbDeviceList.isNullOrEmpty()) {
                _devices = gson.fromJson<List<Device>>(
                    dbDeviceList,
                    object : TypeToken<List<Device>>() {}.type
                ).toMutableStateList()

                if (_devices.isNotEmpty()) {
                    val getScanList =
                        _devices.filter { a -> (a.istracking != null && a.istracking == true) }
                    if (getScanList.isNotEmpty()) {
                        _scanDevices = mutableListOf<String>()
                        getScanList.onEach { a ->
                            _scanDevices.add(a.macaddress.uppercase())
                        }
                        emit(_scanDevices.toList())
                    } else {
                        emit(emptyList())
                    }
                } else
                    emit(emptyList())
            }
            delay(2000L)
        }
    }.distinctUntilChanged()

    private var _scanResultItems = mutableListOf<ScanResultItem>()
    val scanResultItems = flowOf(_scanResultItems).distinctUntilChanged()


    fun addAndUpdateScanResultList(scanResultItem: ScanResultItem) {
        if (_scanResultItems.isNotEmpty()) {
            if (_scanResultItems.none { a -> a.macaddress == scanResultItem.macaddress })
                _scanResultItems.add(scanResultItem)
            else {
                _scanResultItems.forEach { a ->
                    if (a.macaddress == scanResultItem.macaddress) {
                        a.rssi = scanResultItem.rssi
                        a.metricvalue = "${helper.CalculateRSSIToMeter(a.rssi)}mt"
                    } else {
                        a.disconnectedcounter?.plus(1)
                    }
                }
            }
        }
    }

    fun removeScanResultListItem(macAddress: String) {
        if (_scanResultItems.isNotEmpty()) {
            _scanResultItems.removeIf { a -> a.macaddress == macAddress }
        }
    }

    fun addItemToBleScannerFilter(device: Device, isStimulable: Boolean = false) {
        if (BleScanner.scanFilter.none { a -> a.macaddress.uppercase() == device.macaddress.uppercase() })
            BleScanner.scanFilter.add(
                BleScanFilterItem(
                    name = device.name,
                    macaddress = device.macaddress.uppercase(),
                    stimulable = isStimulable
                )
            )
    }

    fun removeItemToBleScannerFilter(macAddress: String) {
        BleScanner.scanFilter.removeIf { a -> a.macaddress.uppercase() == macAddress.uppercase() }
    }

    fun getCurrentDeviceResult(macAddress: String) = callbackFlow<BleScannerResult?> {
        while (true) {
            if (BleScanner.scanResults.any { a -> a.macaddress == macAddress.uppercase() }) {
                trySend(
                    BleScanner.scanResults.first { a -> a.macaddress == macAddress.uppercase() }
                )
            } else
                trySend(null)
            delay(2000)
        }
    }
}