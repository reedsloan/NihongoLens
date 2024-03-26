package com.reedsloan.nihongolens.data.repository

import com.reedsloan.nihongolens.data.data_source.AppConfigurationDao
import com.reedsloan.nihongolens.data.local.app_configuration.AppConfigurationEntity
import com.reedsloan.nihongolens.domain.repository.AppConfigurationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigurationRepositoryImpl @Inject constructor(
    private val dao: AppConfigurationDao
) : AppConfigurationRepository {
    override suspend fun getAppConfiguration(): AppConfigurationEntity {
        return dao.getAppData()
    }

    override suspend fun updateAppConfiguration(appConfiguration: AppConfigurationEntity) {
        dao.updateAppData(appConfiguration)
    }
}