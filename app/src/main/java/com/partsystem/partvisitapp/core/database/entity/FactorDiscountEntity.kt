package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "FactorDiscount",
    foreignKeys = [
        ForeignKey(
            entity = FactorDetailEntity::class,
            parentColumns = ["id"],
            childColumns = ["factorDetailId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class FactorDiscountEntity(
    @PrimaryKey
    val id: Int = 0,
    val factorId: Int,
    val productId: Int,
    var sortCode: Int,
    val discountId: Int,
    var price: Double,
    val factorDetailId: Int,
    val discountPercent: Double
)
