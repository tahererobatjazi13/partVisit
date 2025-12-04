package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "factor_discount_table")
data class FactorDiscountEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var sortCode: Int? = null,
    var discountId: Int? = null,
    var price: Double? = 0.0,
    var arzPrice: Double? = 0.0,
    var factorDetailId: Int? = null,
    var discountPercent: Double? = 0.0
)

