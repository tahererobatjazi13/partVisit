package com.partsystem.partvisitapp.core.network.modelDto

import androidx.room.Embedded
import androidx.room.Relation
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity


data class ProductWithPacking(
    @Embedded val product: ProductEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val packings: MutableList<ProductPackingEntity>

)