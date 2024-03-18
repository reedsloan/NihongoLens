package com.reedsloan.nihongolens.data.data_source

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.reedsloan.nihongolens.domain.model.AppData
import com.reedsloan.nihongolens.presentation.permission.PermissionRequest

class AppDataTypeConverter {
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
    fun fromAppData(appData: AppData): String {
        return gson.toJson(appData)
    }

    @TypeConverter
    fun toAppData(appData: String): AppData {
        return gson.fromJson(appData, AppData::class.java)
    }
}