package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "factor_gift_info_table")
data class FactorGiftInfoEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var factorId: Int? = null,
    var discountId: Int? = null,
    var productId: Int? = null,
    var price: Double? = 0.0,
    var arzPrice: Double? = 0.0
)

