package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "factor_discount_table",
    primaryKeys = ["id"],
    foreignKeys = [
        ForeignKey(
            entity = FactorDetailEntity::class,
            parentColumns = ["factorId", "productId"],
            childColumns = ["factorId", "productId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FactorDiscountEntity(
    val id: Int = 0,
    val factorId: Int,
    val productId: Int,
    val sortCode: Int,
    val discountId: Int,
    val price: Double,
    val arzPrice: Double,
    val discountPercent: Double
)