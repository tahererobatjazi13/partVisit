package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "discount_eshantyuns_table",
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
data class DiscountEshantyunsEntity(
    @PrimaryKey
    val id: Int,

    val discountId: Int,
    val sortCode: Int,
    val saleUnitKind: Int,
    val fromValue: Double,
    val toValue: Double,
    val anbarId: Int,
    val productId: Int,
    val unitKind: Int,
    val value: Double,
    val packingId: Int,
    val ratio: Double
)
