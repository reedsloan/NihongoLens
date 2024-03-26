package com.reedsloan.nihongolens.data.data_source

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.reedsloan.nihongolens.data.local.app_configuration.AppConfigurationEntity

@Database(entities = [AppConfigurationEntity::class], version = 1, exportSchema = false)
@TypeConverters(AppConfigurationTypeConverter::class)
abstract class AppConfigurationDatabase: RoomDatabase() {
    abstract val dao: AppConfigurationDao
}