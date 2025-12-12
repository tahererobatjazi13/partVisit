package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "factor_gift_info_table",
    foreignKeys = [ForeignKey(
        entity = FactorHeaderEntity::class,
        parentColumns = ["id"],
        childColumns = ["factorId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class FactorGiftInfoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val factorId: Int,
    val productId: Int,
    val discountId: Int,
    var price: Double = 0.0,
    var arzPrice: Double = 0.0
)

