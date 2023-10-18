package com.serko.ivocabo

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

//data class PermissionResults(val state:Boolean,val multiStatus:MultiplePermissionsState,val alrtTitle:String,)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermission(context: Context): Pair<Boolean, MultiplePermissionsState> {
    var alertState by remember { mutableStateOf(false) }
    var alerttext by remember { mutableStateOf("") }
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
                        alerttext = context.getString(R.string.pms_locationrationale)
                        alertState = true
                    }

                    else -> {
                       alerttext = context.getString(R.string.pms_locationdenied)
                       alertState = true
                    }
                }
            }

            android.Manifest.permission.ACCESS_COARSE_LOCATION -> {
                when {
                    permis.status.isGranted -> {
                        permissionStatus = true
                    }

                    permis.status.shouldShowRationale -> {
                       alerttext = context.getString(R.string.pms_locationrationale)
                        alertState = true
                    }

                    else -> {
                        alerttext = context.getString(R.string.pms_locationdenied)
                       alertState = true
                    }
                }
            }
        }
    }
    if (alertState)
        AlertDialog(
            onDismissRequest = { alertState = false },
            confirmButton = {
                TextButton(onClick = { alertState = false }) {
                    Text(text = context.getString(R.string.ok))
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_not_listed_location_24),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = { Text(text = context.getString(R.string.pms_locationtitle)) },
            text = { Text(text = alerttext) }
        )
    return Pair(permissionStatus, permissionRR)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BluetoothPermission(context: Context): Pair<Boolean, MultiplePermissionsState> {
    var alertState by remember { mutableStateOf(false) }
    var alerttext by remember { mutableStateOf("") }
    var statusResult by remember { mutableStateOf(false) }
    var permArry =
        if (Build.VERSION.SDK_INT <= 30) {
            listOf(
                android.Manifest.permission.BLUETOOTH,
            )
        } else {
            listOf(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_ADVERTISE,
                android.Manifest.permission.BLUETOOTH_CONNECT
            )
        }
    var permissionRR = rememberMultiplePermissionsState(permissions = permArry)
    permissionRR.permissions.forEach { permis ->
        when (permis.permission) {
            android.Manifest.permission.BLUETOOTH,
            -> {
                when {
                    permis.status.isGranted -> {
                        statusResult = true
                    }

                    permis.status.shouldShowRationale -> {
                        alerttext = context.getString(R.string.pms_bluetoothrationale)
                        alertState = true
                    }

                    else -> {
                        alerttext = context.getString(R.string.pms_bluetoothdenied)
                        alertState = true
                    }
                }
            }

            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_ADVERTISE,
            android.Manifest.permission.BLUETOOTH_CONNECT -> {
                when {
                    permis.status.isGranted -> {
                        statusResult = true
                    }

                    permis.status.shouldShowRationale -> {
                        alerttext = context.getString(R.string.pms_bluetoothrationale)
                        alertState = true
                    }

                    else -> {
                        alerttext = context.getString(R.string.pms_bluetoothdenied)
                        alertState = true
                    }
                }
            }
        }
    }

    if (alertState)
        AlertDialog(
            onDismissRequest = { alertState = false },
            confirmButton = {
                TextButton(onClick = { alertState = false }) {
                    Text(text = context.getString(R.string.ok))
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_bluetooth_24),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = { Text(text = context.getString(R.string.pms_locationtitle)) },
            text = { Text(text = alerttext) }
        )
    return Pair(statusResult, permissionRR)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermission(context: Context): Pair<Boolean, PermissionState> {
    var alertState by remember { mutableStateOf(false) }
    var alerttext by remember { mutableStateOf("") }
    var statusResult by remember { mutableStateOf(false) }
    var permissionRR = rememberPermissionState(permission = android.Manifest.permission.POST_NOTIFICATIONS)

    if (Build.VERSION.SDK_INT >= 33) {
        when {
            permissionRR.status.isGranted -> {
                statusResult = true
            }

            permissionRR.status.shouldShowRationale -> {
                alerttext = context.getString(R.string.pms_notificationrationale)
                alertState = true
            }

            else -> {
                alerttext = context.getString(R.string.pms_notificationdenied)
                alertState = true
            }
        }
    } else
        statusResult = true

    if (alertState)
        AlertDialog(
            onDismissRequest = { alertState = false },
            confirmButton = {
                TextButton(onClick = { alertState = false }) {
                    Text(text = context.getString(R.string.ok))
                }
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_notifications_24),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = { Text(text = context.getString(R.string.pms_locationtitle)) },
            text = { Text(text = alerttext) }
        )

    return Pair(statusResult, permissionRR)
}