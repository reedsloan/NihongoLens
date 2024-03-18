package com.reedsloan.nihongolens.presentation.ocr_screen.components

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.reedsloan.nihongolens.presentation.permission.PermissionEvent
import com.reedsloan.nihongolens.presentation.permission.PermissionRequest

@Composable
fun OCRScreen(
    ocrState: OCRState,
    onOCREvent: (OCREvent) -> Unit,
    onPermissionEvent: (PermissionEvent) -> Unit
) {
    // LaunchedEffect to request camera permission when the screen is first shown
    val activity = LocalContext.current as Activity
    LaunchedEffect(Unit) {
        onPermissionEvent(
            PermissionEvent.RequestPermission(
                activity,
                PermissionRequest.CameraPermissionRequest
            )
        )
    }
}