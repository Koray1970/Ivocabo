package com.serko.ivocabo.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.serko.ivocabo.ComposeTitle
import com.serko.ivocabo.R
import com.serko.ivocabo.data.Screen
import com.serko.ivocabo.data.userViewModel
import com.serko.ivocabo.profileFormLabel
import com.serko.ivocabo.profileFormValue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(
    navController: NavController,
    composeProgressStatus: MutableState<Boolean> = mutableStateOf(false),
    userviewModel: userViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current.applicationContext
    val userDetail = userviewModel.fetchUser()

    var privacyBottomSheet by remember { mutableStateOf(false) }
    var privacysheetState = rememberModalBottomSheetState()
    Scaffold(bottomBar = {
        BottomAppBar(
            actions = {
                IconButton(onClick = { navController.navigate(Screen.Dashboard.route) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_home_24),
                        contentDescription = null
                    )
                }/*IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_person_24),
                            contentDescription = null
                        )
                    }*/
                IconButton(onClick = { navController.navigate(Screen.Preference.route) }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_settings_24),
                        contentDescription = null
                    )
                }
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
                .padding(20.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = context.getString(R.string.prf_title),
                style = ComposeTitle,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "${context.getString(R.string.username)} : ",
                style = profileFormLabel,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = userDetail.username,
                style = profileFormValue,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "${context.getString(R.string.email)} : ",
                style = profileFormLabel,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = userDetail.email,
                style = profileFormValue,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { privacyBottomSheet = true },
                    shape = RoundedCornerShape(corner = CornerSize(6.dp))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_privacy_tip_24),
                        contentDescription = null
                    )
                    Text(text = context.getString(R.string.prf_privacypolicy))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row() {
                Button(
                    onClick = { /*TODO*/ }, shape = RoundedCornerShape(corner = CornerSize(6.dp))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_delete_24),
                        contentDescription = null
                    )
                    Text(text = context.getString(R.string.prf_removeuser))
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        context.deleteDatabase(context.getString(R.string.dbname))
                        navController.navigate(Screen.Signin.route)
                    }, shape = RoundedCornerShape(corner = CornerSize(6.dp))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_logout_24),
                        contentDescription = null
                    )
                    Text(text = context.getString(R.string.prf_signout))
                }
            }
        }

        if (privacyBottomSheet) {
            ModalBottomSheet(
                shape = RectangleShape,
                onDismissRequest = {
                    privacyBottomSheet = false
                },
                containerColor = Color.White,
                sheetState = privacysheetState,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(modifier = Modifier.fillMaxWidth(), shape = RectangleShape, onClick = {
                        scope.launch { privacysheetState.hide() }.invokeOnCompletion {
                            if (!privacysheetState.isVisible) {
                                privacyBottomSheet = false
                            }
                        }
                    }) {
                        Text(text = context.getString(R.string.ireadandunderstand))
                    }
                    PrivacyViewer(context.getString(R.string.privacypolicyurl))
                }
            }
        }
    }
}