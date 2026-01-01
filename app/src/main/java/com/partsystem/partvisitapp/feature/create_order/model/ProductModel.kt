package com.partsystem.partvisitapp.feature.create_order.model

import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity

data class ProductModel(
    val id: Int,
    val name: String,
    val code: String,
    val productPacking: List<ProductPackingEntity>
)