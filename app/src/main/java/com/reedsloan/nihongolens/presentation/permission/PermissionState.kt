package com.reedsloan.nihongolens.presentation.permission

import com.reedsloan.nihongolens.domain.model.AppData

data class PermissionState(
    val permissionRequestQueue: List<PermissionRequest> = emptyList(),
    val previouslyRequestedPermissions: List<String> = emptyList(),
    val appData: AppData? = null
)