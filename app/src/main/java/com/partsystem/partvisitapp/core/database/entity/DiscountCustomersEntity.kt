package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "discount_customers",
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
data class DiscountCustomersEntity(
    @PrimaryKey
    val id: Int,

    val discountId: Int,
    val customerId: Int,
    val customerKindId: Int,
    val customerDegreeId: Int,
    val customerPishehId: Int,
    val tafsiliGroupId: Int,
    val tafsiliGroupDetailId: Int,
    val customerPriceAmount: Double,
    val maxCustomerPriceAmount: Double
)
