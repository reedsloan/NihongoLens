package com.reedsloan.nihongolens.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.reedsloan.nihongolens.presentation.permission.PermissionRequest

@Entity(tableName = "app_data")
data class AppData(
    @PrimaryKey val id: Int = 0,
    val previouslyDeniedPermissions: List<PermissionRequest> = emptyList(),
)
