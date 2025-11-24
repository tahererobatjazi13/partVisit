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
/*
{
    "id": 420,
    "moduleId": 0,
    "code": 12,
    "name": "TransferWeighingBillInfo",
    "description": "اطلاعات قبض باسکول به اطلاعات تکمیلی رسید منتقل شود؟",
    "controlType": 0,
    "itemSource": null,
    "defaultValue": "False",
    "value": "False"
}*/