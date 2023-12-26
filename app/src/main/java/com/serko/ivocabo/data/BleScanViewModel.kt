package com.serko.ivocabo.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serko.ivocabo.Helper
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

class BleScanViewModel @Inject constructor(
    private val userViewModel: UserViewModel
) :
    ViewModel() {
    private val helper = Helper()
    private var _scanDevices = mutableListOf<String>()
    val scanDevices = flowOf(_scanDevices).distinctUntilChanged()

    private var _scanResultItems = mutableListOf<ScanResultItem>()
    val scanResultItems =
        flowOf(_scanResultItems).distinctUntilChanged()

    init {
        viewModelScope.launch {
            userViewModel.getScanDeviceList().collect {
                if (it.isNotEmpty()) {
                    it.forEach { r ->
                        _scanDevices.add(r)
                    }
                }
            }
        }
    }

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
}