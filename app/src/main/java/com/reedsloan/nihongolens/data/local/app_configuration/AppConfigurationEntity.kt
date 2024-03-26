package com.reedsloan.nihongolens.data.local.app_configuration

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.reedsloan.nihongolens.presentation.permission.PermissionRequest

@Entity(tableName = "app_configuration")
data class AppConfigurationEntity(
    @PrimaryKey val id: Int = 0,
    val previouslyDeniedPermissions: List<PermissionRequest> = emptyList(),
    val hasSeenOnboarding: Boolean = false,
)
