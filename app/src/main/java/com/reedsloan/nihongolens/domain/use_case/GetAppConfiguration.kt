package com.reedsloan.nihongolens.domain.use_case

import com.reedsloan.nihongolens.data.local.app_configuration.AppConfigurationEntity
import com.reedsloan.nihongolens.domain.model.AppConfiguration
import com.reedsloan.nihongolens.domain.repository.AppConfigurationRepository

class GetAppConfiguration(
    private val repository: AppConfigurationRepository
) {
    suspend operator fun invoke() = repository.getAppConfiguration().toAppConfiguration()

    private fun AppConfigurationEntity.toAppConfiguration() = AppConfiguration(
        previouslyRequestedPermissions = this.previouslyRequestedPermissions,
        hasSeenOnboarding = this.hasSeenOnboarding
    )
}