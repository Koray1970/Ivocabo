package com.serko.ivocabo

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermission(context: Context): Pair<Boolean, MultiplePermissionsState> {
    var permissionStatus by remember { mutableStateOf(false) }
    var permArry = listOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    var permissionRR = rememberMultiplePermissionsState(permissions = permArry)
    permissionRR.permissions.forEach { permis ->
        when (permis.permission) {
            android.Manifest.permission.ACCESS_COARSE_LOCATION -> {
                when {
                    permis.status.isGranted -> {
                        permissionStatus = true
                    }

                    permis.status.shouldShowRationale -> {
                        Toast.makeText(
                            context,
                            "Please granted Location permission!",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> {
                        Toast.makeText(
                            context,
                            "Location permission is denied, go to app settings for enabling",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            android.Manifest.permission.ACCESS_COARSE_LOCATION -> {
                when {
                    permis.status.isGranted -> {
                        permissionStatus = true
                    }

                    permis.status.shouldShowRationale -> {
                        Toast.makeText(
                            context,
                            "Please granted Location permission!",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> {
                        Toast.makeText(
                            context,
                            "Location permission is denied, go to app settings for enabling",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
    return Pair(permissionStatus, permissionRR)
}
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BluetoothPermission(context: Context): Pair<Boolean, MultiplePermissionsState> {
    var permissionStatus by remember { mutableStateOf(false) }
    var permArry =
        if (Build.VERSION.SDK_INT <= 27) {
            listOf(
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN
            )
        } else {
            listOf(
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT
            )
        }
    var permissionRR = rememberMultiplePermissionsState(permissions = permArry)
    permissionRR.permissions.forEach { permis ->
        when (permis.permission) {
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN
            -> {
                when {
                    permis.status.isGranted -> {
                        permissionStatus = true
                    }

                    permis.status.shouldShowRationale -> {
                        Toast.makeText(
                            context,
                            "Please granted bluetooth scan permission!",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> {
                        Toast.makeText(
                            context,
                            "Bluetooth scan permission is denied, go to app settings for enabling",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT -> {
                when {
                    permis.status.isGranted -> {
                        permissionStatus = true
                    }

                    permis.status.shouldShowRationale -> {
                        Toast.makeText(
                            context,
                            "Please granted bluetooth scan permission!",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> {
                        Toast.makeText(
                            context,
                            "Bluetooth scan permission is denied, go to app settings for enabling",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
    return Pair(permissionStatus, permissionRR)
}