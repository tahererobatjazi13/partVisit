package com.partsystem.partvisitapp.core.network.modelDto

import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity

data class ProductModel(
    val id: Int,
    val code: String?,
    val name: String?,
    val description: String?,
    val rate: Double?,
    val toll: Double?,
    val vat: Double?,
    val rateAfterVatAndToll: Double?,
    val fileName: String?,
    val packings: List<ProductPackingEntity>
)
