package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class DiscountFull(
    @Embedded val discount: DiscountEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "discountId"
    )
    val products: List<DiscountProductEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "discountId"
    )
    val groups: List<DiscountGroupEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "discountId"
    )
    val stairs: List<DiscountStairEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "discountId"
    )
    val customers: List<DiscountCustomerEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "discountId"
    )
    val eshantyuns: List<DiscountEshantyunEntity>
)
