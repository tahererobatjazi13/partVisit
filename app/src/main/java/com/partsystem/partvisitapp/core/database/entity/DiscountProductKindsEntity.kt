package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "DiscountProductKind",
    foreignKeys = [
        ForeignKey(
            entity = DiscountEntity::class,
            parentColumns = ["id"],
            childColumns = ["discountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("discountId")]
)
data class DiscountProductKindsEntity(
    @PrimaryKey
    val id: Int,
    val discountId: Int,
    val sortCode: Int,
    val fromProductKind: Int,
    val toProductKind: Int,
    val discountPercent: Double,
    val minPrice: Double,
)