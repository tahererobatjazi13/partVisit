package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Mojoodi")
data class MojoodiEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val anbarId: Int,
    val productId: Int,
    val productSerial: Int,
    val remainValue: Double,
    val remainValue2: Double,
    val kasriValue: Double,
    val kasriValue2: Double,
    val mojoodi: Double,
    val mojoodi2: Double
)
/*
{
    "anbarId": 6,
    "productId": 89,
    "productSerial": 0,
    "remainValue": -1217.0,
    "remainValue2": 0.0,
    "kasriValue": 1225.0,
    "kasriValue2": 0.0,
    "mojoodi": 8.0,
    "mojoodi2": 0.0
}*/