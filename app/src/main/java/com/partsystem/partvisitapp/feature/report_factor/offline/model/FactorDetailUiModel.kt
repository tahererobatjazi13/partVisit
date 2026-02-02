package com.partsystem.partvisitapp.feature.report_factor.offline.model

import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity

data class FactorDetailUiModel(
    val id: Int,
    val factorId: Int,
    val productId: Int,
    val productName: String?,
    val packingName: String?,
    val packingValue: Double,
    val unitPerPack: Double,
    val unit1Name: String?,
    val unit1Value: Double,
    val unit2Value: Double,
    val unit1Rate: Double,
    val vat: Double,
    val isGift: Int,
    val discountPrice: Int,
)
//
//data class FactorDetailUiModel(
//    val product: ProductEntity,
//    val packings: List<ProductPackingEntity>,
//    val factorId: Int,
//    val productName: String?,
//    val packingName: String?,
//    val packingValue: Double,
//    val unitPerPack: Double,
//    val unit1Name: String?,
//    val unit1Value: Double,
//    val unit2Value: Double,
//    val unit1Rate: Double,
//    val vat: Double,
//    val isGift: Int,
//    val discountPrice: Int,
//    val rate: Double,
//    val vatPercent: Float,
//    val tollPercent: Float,
//    val finalRate: Double
//)*/


fun FactorDetailUiModel.getPackingValueFormatted(): String {
    val unitPerPack = unitPerPack
    if (unitPerPack <= 0) return ""

    val fullPacks = kotlin.math.floor(unit1Value / unitPerPack).toInt()
    val remain = unit1Value % unitPerPack

    if (unit1Value <= 0) return ""

    val remainText =
        if (remain % 1 == 0.0) remain.toInt().toString() else remain.toString()

    return "$remainText:$fullPacks"
}

