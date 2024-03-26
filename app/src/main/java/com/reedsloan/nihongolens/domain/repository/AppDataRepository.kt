package com.reedsloan.nihongolens.domain.repository

import com.reedsloan.nihongolens.data.local.app_configuration.AppConfigurationEntity

interface AppConfigurationRepository {
    suspend fun getAppConfiguration(): AppConfigurationEntity
    suspend fun updateAppConfiguration(appConfiguration: AppConfigurationEntity)
}