package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/*@Entity(tableName = "factor_discount_table")
data class FactorDiscountEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var sortCode: Int? = null,
    var discountId: Int? = null,
    var price: Double? = 0.0,
    var arzPrice: Double? = 0.0,
    var factorDetailId: Int? = null,
    var discountPercent: Double? = 0.0
)*/

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
    val productId: Int, // برای راحتی join
    val discountId: Int,
    val price: Double, // مبلغ تخفیف یا افزودنی
    val factorDetailId: Int? = null,
    val discountKind: Byte // 0 = تخفیف، 1 = افزودنی
)