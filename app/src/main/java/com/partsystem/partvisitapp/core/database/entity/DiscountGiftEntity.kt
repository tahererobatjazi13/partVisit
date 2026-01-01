package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "discount_gift",
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
data class DiscountGiftEntity(
    @PrimaryKey
    val id: Int,
    val discountId: Int,
    val sortCode: Int,
    val fromPrice: Double,
    val toPrice: Double,
    val anbarId: Int,
    val productId: Int,
    val unitKind: Int,
    val value: Double,
    val packingId: Int,
    val ratio: Int,
)

