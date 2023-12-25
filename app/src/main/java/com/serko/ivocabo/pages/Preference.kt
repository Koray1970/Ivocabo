package com.serko.ivocabo.pages

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.serko.ivocabo.ComposeTitle
import com.serko.ivocabo.R
import com.serko.ivocabo.data.Screen
import com.serko.ivocabo.data.UserViewModel
import com.serko.ivocabo.preferanceSupportingText
import com.serko.ivocabo.profileFormLabel

@Composable
fun Preference(
    navController: NavController,
    composeProgressStatus: MutableState<Boolean> = mutableStateOf(false)
) {
    val context = LocalContext.current.applicationContext
    val userviewModel = hiltViewModel<UserViewModel>()

    val userDetail = userviewModel.fetchUser()

    var locationPermissionIcon by remember { mutableStateOf(R.drawable.baseline_indeterminate_check_box_24) }
    if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) locationPermissionIcon =
        R.drawable.baseline_check_box_24

    var bluetoothPermissionIcon by remember { mutableStateOf(R.drawable.baseline_indeterminate_check_box_24) }
    if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || context.checkSelfPermission(
            android.Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
    ) bluetoothPermissionIcon = R.drawable.baseline_check_box_24

    var notificationPermissionIcon by remember { mutableStateOf(R.drawable.baseline_indeterminate_check_box_24) }
    if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) notificationPermissionIcon =
        R.drawable.baseline_check_box_24
    Scaffold(bottomBar = {
        BottomAppBar(
            actions = {
                IconButton(onClick = { navController.navigate(Screen.Dashboard.route) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_home_24),
                        contentDescription = null
                    )
                }
                IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_person_24),
                        contentDescription = null
                    )
                }/*IconButton(onClick = { navController.navigate(Screen.Preference.route) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_settings_24),
                            contentDescription = null
                        )
                    }*/
            },
            floatingActionButton = {
                FloatingActionButton(containerColor = Color.Green,
                    shape = CircleShape,
                    onClick = {
                        navController.navigate(Screen.Dashboard.route)
                    },
                    content = {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                            contentDescription = ""
                        )
                    })
            },
        )
    }

    ) { innerpadding ->
        Column(
            modifier = Modifier
                .padding(innerpadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = context.getString(R.string.pre_preferencetitle),
                style = ComposeTitle,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(40.dp))

            ListItem(trailingContent = {
                Icon(
                    painter = painterResource(id = locationPermissionIcon),
                    contentDescription = null
                )
            }, headlineContent = {
                Text(
                    text = "${context.getString(R.string.pre_locationpermission)} : ",
                    style = profileFormLabel,
                    modifier = Modifier.fillMaxWidth()
                )
            }, leadingContent = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_location_on_24),
                    contentDescription = null
                )
            }, supportingContent = {
                Text(
                    text = "${context.getString(R.string.pre_permissionratio)} : ",
                    style = preferanceSupportingText,
                    modifier = Modifier.fillMaxWidth()
                )
            })
            HorizontalDivider()
            ListItem(trailingContent = {
                Icon(
                    painter = painterResource(id = bluetoothPermissionIcon),
                    contentDescription = null
                )
            }, headlineContent = {
                Text(
                    text = "${context.getString(R.string.pre_bluetoothpermission)} : ",
                    style = profileFormLabel,
                    modifier = Modifier.fillMaxWidth()
                )
            }, leadingContent = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_bluetooth_24),
                    contentDescription = null
                )
            }, supportingContent = {
                Text(
                    text = "${context.getString(R.string.pre_permissionratio)} : ",
                    style = preferanceSupportingText,
                    modifier = Modifier.fillMaxWidth()
                )
            })
            HorizontalDivider()
            ListItem(trailingContent = {
                Icon(
                    painter = painterResource(id = notificationPermissionIcon),
                    contentDescription = null
                )
            }, headlineContent = {
                Text(
                    text = "${context.getString(R.string.pre_notificationpermission)} : ",
                    style = profileFormLabel,
                    modifier = Modifier.fillMaxWidth()
                )
            }, leadingContent = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_notifications_24),
                    contentDescription = null
                )
            }, supportingContent = {
                Text(
                    text = "${context.getString(R.string.pre_permissionratio)} : ",
                    style = preferanceSupportingText,
                    modifier = Modifier.fillMaxWidth()
                )
            })
            HorizontalDivider()
            ListItem(trailingContent = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_info_24),
                        contentDescription = null
                    )
                }
            }, headlineContent = {
                Text(
                    text = "${context.getString(R.string.pre_privacypolicysubmitstamp)} : ",
                    style = profileFormLabel,
                    modifier = Modifier.fillMaxWidth()
                )
            }, leadingContent = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_privacy_tip_24),
                    contentDescription = null
                )
            }, supportingContent = {
                Text(
                    text = userDetail.registerdate.toString(),
                    style = preferanceSupportingText,
                    modifier = Modifier.fillMaxWidth()
                )
            })
            HorizontalDivider()
        }
    }
}