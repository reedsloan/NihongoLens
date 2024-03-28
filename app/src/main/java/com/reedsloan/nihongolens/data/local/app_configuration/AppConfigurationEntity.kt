package com.reedsloan.nihongolens.data.local.app_configuration

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_configuration")
data class AppConfigurationEntity(
    @PrimaryKey val id: Int = 0,
    val previouslyRequestedPermissions: List<String> = emptyList(),
    val hasSeenOnboarding: Boolean = false,
)
