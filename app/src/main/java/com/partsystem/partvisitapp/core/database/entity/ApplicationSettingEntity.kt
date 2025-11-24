package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "application_setting_table")
data class ApplicationSettingEntity(
    @PrimaryKey val id: Int,
    val moduleId: Int,
    val code: Int,
    val name: String,
    val description: String?,
    val controlType: Int,
    val itemSource: String?,
    val defaultValue: String?,
    val value: String?
)
