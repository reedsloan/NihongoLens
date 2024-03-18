package com.reedsloan.nihongolens.presentation.permission

import android.app.Activity

sealed class PermissionEvent {
    data class RequestPermission(val activity: Activity, val permissionRequest: PermissionRequest) : PermissionEvent()
    data class OnPermissionResult(val permission: String, val granted: Boolean) : PermissionEvent()
    data object Initialize : PermissionEvent()
    data object OnDismissDialog : PermissionEvent()
}