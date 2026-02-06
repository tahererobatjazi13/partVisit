package com.partsystem.partvisitapp.feature.create_order.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity


data class ProductWithPacking(
    @Embedded val product: ProductEntity,

    @ColumnInfo(name = "actRate")
    val rate: Double,

    @ColumnInfo(name = "actVatPercent")
    val vatPercent: Double,

    @ColumnInfo(name = "actTollPercent")
    val tollPercent: Double,

    @ColumnInfo(name = "finalRate")
    val finalRate: Double,// قیمت نهایی VAT + Toll + Rate

    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    var packings: List<ProductPackingEntity>
)
