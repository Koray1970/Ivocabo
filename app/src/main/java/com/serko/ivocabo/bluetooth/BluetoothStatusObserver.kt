package com.serko.ivocabo.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow


interface IBluetoothStatusObserver {
    fun observeConnectivity(): Flow<BluetoothConnectivityStatus>
    enum class BluetoothConnectivityStatus {
        AVAILABLE, UNAVAILABLE
    }
}

class BluetoothStatusObserver(private val context: Context) : IBluetoothStatusObserver {
    private var bluetoothManager: BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)
    private var bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    override fun observeConnectivity(): Flow<IBluetoothStatusObserver.BluetoothConnectivityStatus> = flow<IBluetoothStatusObserver.BluetoothConnectivityStatus> {
            while (true) {
                when (bluetoothAdapter.isEnabled) {
                    true -> {
                        emit(IBluetoothStatusObserver.BluetoothConnectivityStatus.AVAILABLE)
                    }

                    false -> {
                        emit(IBluetoothStatusObserver.BluetoothConnectivityStatus.UNAVAILABLE)
                    }
                }
                delay(4000)
            }
    }.distinctUntilChanged()
}