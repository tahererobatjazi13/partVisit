package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class DiscountFull(
    @Embedded val discount: DiscountEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "discountId"
    )
    val products: List<DiscountProductsEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "discountId"
    )
    val groups: List<DiscountGroupsEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "discountId"
    )
    val stairs: List<DiscountStairsEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "discountId"
    )
    val customers: List<DiscountCustomersEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "discountId"
    )
    val eshantyuns: List<DiscountEshantyunsEntity>
)
