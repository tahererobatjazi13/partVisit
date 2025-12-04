package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey



@Entity(tableName = "factor_detail_table")
data class FactorDetailEntity(
    @PrimaryKey(autoGenerate = true)
    var factorId: Int? = null,
    var id: Int? = null,
    var sortCode: Int? = null,
    var anbarId: Int? = null,
    var productId: Int? = null,
    var actId: Int? = null,
    var unit1Value: Double? = 0.0,
    var unit2Value: Double? = 0.0,
    var price: Double? = 0.0,
    var description: String? = null,
    var packingId: Int? = null,
    var packingValue: Double? = 0.0,
    var vat: Double? = 0.0,
    var productSerial: Int? = null,
    var isGift: Int? = 0,
    var returnCauseId: Int? = 0,
    var isCanceled: Int? = 0,
    var isModified: Int? = 0,
    var unit1Rate: Double? = 0.0,

   // var factorDiscounts: MutableList<FactorDiscountEntity> = mutableListOf()
)

