package com.reedsloan.nihongolens.data.data_source

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.reedsloan.nihongolens.domain.model.AppData

@Database(entities = [AppData::class], version = 1, exportSchema = false)
@TypeConverters(AppDataTypeConverter::class)
abstract class AppDataDatabase: RoomDatabase() {
    abstract val dao: AppDataDao
}