package com.partsystem.partvisitapp.feature.report_factor.online.model

import kotlin.math.floor

data class ReportFactorDto(
    val id: Int,
    val code: Int,
    val mainCode: Int,
    val persianDate: String,
    val createTime: String,
    val invoiceCategoryCode: Int,
    val invoiceCategoryName: String,
    val customerCode: Long,
    val customerName: String,
    val patternCode: Int,
    val patternName: String,
    val sumPrice: Double,
    val sumDiscountPrice: Double,
    val sumVat: Double,
    val finalPrice: Double,
    val anbarCode: Int?,
    val anbarName: String?,
    val productCode: Int?,
    val productName: String?,
    val unitCode: Int?,
    val unitName: String?,
    val unit1Value: Double?,
    val unit2Value: Double?,
    val packingValue: Double?,
    val packingCode: Int?,
    val packingName: String?,
    val discountPrice: Double?,
    val price: Double?,
    val priceAfterDiscount: Double?,
    val vat: Double?,
    val rate1: Double?,
    val priceAfterVat: Double?,
    val isGift: Boolean
)

fun ReportFactorDto.getPackingValueFormatted(): String {
    val unitPerPack = unit1Value!! / packingValue!!
    if (unitPerPack <= 0) return ""

    val fullPacks = floor(unit1Value / unitPerPack).toInt()
    val remain = unit1Value % unitPerPack

    if (unit1Value <= 0) return ""

    val remainText =
        if (remain % 1 == 0.0) remain.toInt().toString() else remain.toString()

    return "$fullPacks:$remainText"
}
