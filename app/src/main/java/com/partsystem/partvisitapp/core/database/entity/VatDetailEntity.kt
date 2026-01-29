package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "VatDetail")
data class VatDetailEntity(
    @PrimaryKey val id: Int,
    val vatId: Int,
    val productId: Int,
    val vatPercent: Double,
    val tollPercent: Double,
    val taxPercent: Double
)
