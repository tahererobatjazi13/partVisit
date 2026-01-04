package com.partsystem.partvisitapp.feature.create_order.model

import androidx.room.Embedded
import androidx.room.Relation
import com.partsystem.partvisitapp.core.database.entity.DiscountEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountStairsEntity

data class DiscountWithStairs(
    @Embedded val discount: DiscountEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "discountId"
    )
    val discountStair: List<DiscountStairsEntity>
)