package com.reedsloan.nihongolens.domain.repository

import com.reedsloan.nihongolens.domain.model.AppData

interface AppDataRepository {
    suspend fun getAppData(): AppData
    suspend fun updateAppData(appData: AppData)
}