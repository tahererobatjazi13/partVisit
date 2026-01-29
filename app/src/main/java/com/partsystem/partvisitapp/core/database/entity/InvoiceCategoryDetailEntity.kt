package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "InvoiceCategoryDetail")
data class InvoiceCategoryDetailEntity(
    @PrimaryKey val InvoiceCategoryId: Int,
    val UserId: Int,
)
