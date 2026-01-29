package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "InvoiceCategoryCenter")
data class InvoiceCategoryCenterEntity(
    @PrimaryKey val InvoiceCategoryId: Int,
    val CenterId: Int,
)
