package com.reedsloan.nihongolens.domain.use_case

import com.reedsloan.nihongolens.data.local.app_configuration.AppConfigurationEntity
import com.reedsloan.nihongolens.domain.model.AppConfiguration
import com.reedsloan.nihongolens.domain.repository.AppConfigurationRepository

class UpdateAppConfiguration(
    private val repository: AppConfigurationRepository
) {
    suspend operator fun invoke(appConfiguration: AppConfiguration) = repository.updateAppConfiguration(
        appConfiguration.toAppConfigurationEntity()
    )

    private fun AppConfiguration.toAppConfigurationEntity() = AppConfigurationEntity(
        previouslyRequestedPermissions = this.previouslyRequestedPermissions,
        hasSeenOnboarding = this.hasSeenOnboarding
    )
}