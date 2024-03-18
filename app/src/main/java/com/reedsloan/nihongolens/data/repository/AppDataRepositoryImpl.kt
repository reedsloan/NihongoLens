package com.reedsloan.nihongolens.data.repository

import com.reedsloan.nihongolens.data.data_source.AppDataDao
import com.reedsloan.nihongolens.data.data_source.AppDataDatabase
import com.reedsloan.nihongolens.domain.model.AppData
import com.reedsloan.nihongolens.domain.repository.AppDataRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDataRepositoryImpl @Inject constructor(
    private val dao: AppDataDao
) : AppDataRepository {
    override suspend fun getAppData(): AppData {
        return dao.getAppData()
    }

    override suspend fun updateAppData(appData: AppData) {
        dao.updateAppData(appData)
    }
}