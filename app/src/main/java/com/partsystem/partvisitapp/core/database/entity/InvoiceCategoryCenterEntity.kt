package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoice_category_center_table")
data class InvoiceCategoryCenterEntity(
    @PrimaryKey val InvoiceCategoryId: Int,
    val CenterId: Int,
)
