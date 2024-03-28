package com.reedsloan.nihongolens.data.data_source

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.reedsloan.nihongolens.data.local.app_configuration.AppConfigurationEntity

class AppConfigurationTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromAppConfigurationEntity(appConfiguration: AppConfigurationEntity): String {
        return gson.toJson(appConfiguration)
    }

    @TypeConverter
    fun toAppConfigurationEntity(appConfiguration: String): AppConfigurationEntity {
        return gson.fromJson(appConfiguration, AppConfigurationEntity::class.java)
    }

    @TypeConverter
    fun fromListString(list: List<String>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toListString(json: String): List<String> {
        return gson.fromJson(json, Array<String>::class.java).toList()
    }

}