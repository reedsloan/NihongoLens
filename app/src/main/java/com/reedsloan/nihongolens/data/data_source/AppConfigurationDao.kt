package com.reedsloan.nihongolens.data.data_source

import androidx.room.Dao
import androidx.room.Query
import com.reedsloan.nihongolens.data.local.app_configuration.AppConfigurationEntity

@Dao
interface AppConfigurationDao {
    // The ID is always 0, as there is only one instance of AppData
    @Query("SELECT * FROM app_configuration WHERE id = 0")
    suspend fun getAppData(): AppConfigurationEntity

    @Query("INSERT OR REPLACE INTO app_configuration (id, previouslyRequestedPermissions) VALUES (0, :appData)")
    suspend fun updateAppData(appData: AppConfigurationEntity)
}