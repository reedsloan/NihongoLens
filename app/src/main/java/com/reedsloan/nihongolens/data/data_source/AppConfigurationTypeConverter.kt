package com.reedsloan.nihongolens.data.data_source

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.reedsloan.nihongolens.data.local.app_configuration.AppConfigurationEntity
import com.reedsloan.nihongolens.presentation.permission.PermissionRequest

class AppConfigurationTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromPermissionRequestList(permissionRequestList: List<PermissionRequest>): String {
        return gson.toJson(permissionRequestList)
    }

    @TypeConverter
    fun toPermissionRequestList(permissionRequestList: String): List<PermissionRequest> {
        return gson.fromJson(permissionRequestList, Array<PermissionRequest>::class.java).toList()
    }

    @TypeConverter
    fun fromAppConfigurationEntity(appConfiguration: AppConfigurationEntity): String {
        return gson.toJson(appConfiguration)
    }

    @TypeConverter
    fun toAppConfigurationEntity(appConfiguration: String): AppConfigurationEntity {
        return gson.fromJson(appConfiguration, AppConfigurationEntity::class.java)
    }
}