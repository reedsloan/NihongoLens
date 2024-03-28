package com.reedsloan.nihongolens.domain.model

data class AppConfiguration(
    val previouslyRequestedPermissions: List<String> = emptyList(),
    val hasSeenOnboarding: Boolean = false,
)
