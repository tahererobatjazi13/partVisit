package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "factor_discount_table",
    foreignKeys = [
        ForeignKey(
            entity = FactorDetailEntity::class,
            parentColumns = ["id"],
            childColumns = ["factorDetailId"],
            onDelete = ForeignKey.CASCADE
        )
    ]/*,
    indices = [
        Index("factorDetailId"),
    ]*/
)

/*
@Entity(tableName = "factor_discount_table")
*/
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
