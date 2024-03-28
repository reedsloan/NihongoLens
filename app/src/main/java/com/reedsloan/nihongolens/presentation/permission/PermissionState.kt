package com.reedsloan.nihongolens.presentation.permission

import com.reedsloan.nihongolens.domain.model.AppConfiguration

data class PermissionState(
    val permissionRequestQueue: List<PermissionRequest> = emptyList(),
    val appConfiguration: AppConfiguration = AppConfiguration()
)