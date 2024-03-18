package com.reedsloan.nihongolens.data.data_source

import androidx.room.Dao
import androidx.room.Query
import com.reedsloan.nihongolens.domain.model.AppData

@Dao
interface AppDataDao {
    // The ID is always 0, as there is only one instance of AppData
    @Query("SELECT * FROM app_data WHERE id = 0")
    suspend fun getAppData(): AppData

    @Query("INSERT OR REPLACE INTO app_data (id, previouslyDeniedPermissions) VALUES (0, :appData)")
    suspend fun updateAppData(appData: AppData)
}