package com.serko.ivocabo

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
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
    var result=false
    var openDialog by remember { mutableStateOf<Boolean>(false) }
    val dialogTitle=context.getString(R.string.alrtbluetoothtitle)
    var dailogText=""

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
                        result = true
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

            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_ADVERTISE,
            android.Manifest.permission.BLUETOOTH_CONNECT -> {
                when {
                    permis.status.isGranted -> {
                        result=true
                    }

                    permis.status.shouldShowRationale -> {
                        dailogText=context.getString(R.string.alrtbluetoothwarningtext)
                        openDialog=true
                    }

                    else -> {
                        dailogText=context.getString(R.string.alrtbluetoothdeniedtext)
                        openDialog=true
                    }
                }
            }
        }
    }

    if (openDialog) {
        AlertDialog(
            icon = {
                Icon(painter = painterResource(id = R.drawable.baseline_warning_amber_24), contentDescription = null)
            },
            title = {
                Text(
                    text = dialogTitle,
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                )
            },
            text = {
                Text(
                    text = dailogText,
                    style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp)
                )
            },
            onDismissRequest = { openDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                    }
                ) {
                    Text(text = context.getString(R.string.ireadandunderstand))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                    }
                ) {
                    Text(text = context.getString(R.string.cancel))
                }
            }
        )
    }

    return Pair(result, permissionRR)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermission(context: Context): Pair<Boolean, PermissionState> {
    var result = false
    var permissionRR =
        rememberPermissionState(permission = android.Manifest.permission.POST_NOTIFICATIONS)
    var openDialog by remember { mutableStateOf<Boolean>(false) }
    val dialogTitle=context.getString(R.string.alrtnotificationtitle)
    var dailogText=""
    if (Build.VERSION.SDK_INT >= 33) {

        when {
            permissionRR.status.isGranted -> {
                result = true
            }

            permissionRR.status.shouldShowRationale -> {
                dailogText=context.getString(R.string.alrtnotificationwarningtext)
                openDialog=true
            }

            else -> {
                dailogText=context.getString(R.string.alrtnotificationdeniedtext)
                openDialog=true
            }
        }
    } else
        result = true

    if (openDialog) {
        AlertDialog(
            icon = {
                Icon(painter = painterResource(id = R.drawable.baseline_warning_amber_24), contentDescription = null)
            },
            title = {
                Text(
                    text = dialogTitle,
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                )
            },
            text = {
                Text(
                    text = dailogText,
                    style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp)
                )
            },
            onDismissRequest = { openDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                    }
                ) {
                    Text(text = context.getString(R.string.ireadandunderstand))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                    }
                ) {
                    Text(text = context.getString(R.string.cancel))
                }
            }
        )
    }
    return Pair(result , permissionRR)
}