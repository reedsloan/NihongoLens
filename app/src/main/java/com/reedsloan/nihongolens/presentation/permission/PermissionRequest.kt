package com.reedsloan.nihongolens.presentation.permission

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi

sealed class PermissionRequest(
    val permission: String,
    val message: String,
    val isPermanentlyDeniedMessage: String = "",
    val allowCancelPermissionDialog: Boolean = true
) {
    data object CameraPermissionRequest : PermissionRequest(
        permission = "android.permission.CAMERA",
        message = "Camera permission is required for the text recognition feature.",
        isPermanentlyDeniedMessage = "Camera permission is required for the text recognition feature. Please enable it in your device settings.",
        allowCancelPermissionDialog = false
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    data object StoragePermissionRequestAPI33 : PermissionRequest(
        permission = Manifest.permission.READ_MEDIA_IMAGES,
        message = "Storage permission is required to select photos.",
        isPermanentlyDeniedMessage = "Storage permission is required to select photos. Please enable it in your device settings."
    )

    data object NotificationPermissionRequest : PermissionRequest(
        permission = Manifest.permission.ACCESS_NOTIFICATION_POLICY,
        message = "Notification permission is required to receive notifications.",
        isPermanentlyDeniedMessage = "Notification permission is required to receive notifications. Please enable it in your device settings."
    )
}
