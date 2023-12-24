package com.serko.ivocabo.data

import android.bluetooth.le.ScanFilter
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serko.ivocabo.pages.helper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

class BleScanViewModel @Inject constructor(
    private val userViewModel: userViewModel,
    @ApplicationContext context: Context
) :
    ViewModel() {

    private var _scanResultItems = mutableListOf<ScanResultItem>()
    val scanResultItems =
        flowOf(_scanResultItems).distinctUntilChanged()

    init {
        viewModelScope.launch {
            userViewModel.getScanDeviceList().collect {
                if (it.isNotEmpty()) {
                    it.forEach { r ->
                        _scanResultItems.add(ScanResultItem(macaddress = r))
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