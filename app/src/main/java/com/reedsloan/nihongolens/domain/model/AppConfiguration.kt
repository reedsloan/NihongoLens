package com.reedsloan.nihongolens.domain.model

import com.reedsloan.nihongolens.presentation.permission.PermissionRequest

class AppConfiguration(
    val previouslyDeniedPermissions: List<PermissionRequest> = emptyList(),
    val hasSeenOnboarding: Boolean = false,
)
