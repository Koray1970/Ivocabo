package com.serko.ivocabo.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeProgress(dialogshow: MutableState<Boolean>) {
    if (dialogshow.value) {
        BasicAlertDialog(
            onDismissRequest = { dialogshow.value = false },
            properties = DialogProperties(
            usePlatformDefaultWidth = false
        ), content = {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = .7f)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(100.dp),
                        strokeWidth = 16.dp,
                        strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap
                    )
                }
            }
        })
    }
}