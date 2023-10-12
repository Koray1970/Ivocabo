package com.serko.ivocabo

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.serko.ivocabo.data.Screen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(composeProgressDialogStatus:MutableState<Boolean>){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Signup.route) {
        composable(Screen.Signup.route) {
            Signup(
                navController, composeProgressDialogStatus
            )
        }
        composable(Screen.Signin.route) {
            SignIn(
                navController, composeProgressDialogStatus
            )
        }
        composable(Screen.ForgetPassword.route) { ForgetPassword(navController) }
        composable(Screen.Dashboard.route) {
            Dashboard(
                navController,
                composeProgressDialogStatus
            )
        }
        composable(
            Screen.DeviceDashboard.route,
            arguments = listOf(navArgument("macaddress") {
                type = NavType.StringType
            })
        ) {
            DeviceDashboard(
                macaddress = it.arguments?.getString("macaddress"),
                navController,
                composeProgressDialogStatus
            )
        }
        composable(
            Screen.FindMyDevice.route,
            arguments = listOf(navArgument("macaddress") {
                type = NavType.StringType
            })
        ) {
            FindMyDevice(
                macaddress = it.arguments?.getString("macaddress"),
                navController,
                composeProgressDialogStatus
            )
        }
        composable(
            Screen.TrackMyDevice.route,
            arguments = listOf(navArgument("macaddress") {
                type = NavType.StringType
            })
        ) {
            TrackMyDevice(
                macaddress = it.arguments?.getString("macaddress"),
                navController,
                composeProgressDialogStatus
            )
        }
    }
}