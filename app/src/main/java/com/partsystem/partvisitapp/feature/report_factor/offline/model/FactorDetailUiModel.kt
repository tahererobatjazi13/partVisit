package com.partsystem.partvisitapp.feature.report_factor.offline.model

data class FactorDetailUiModel(
    val factorId: Int,
    val productId: Int,
    val productName: String?,
    val packingName: String?,
    val packingValue: Double,
    val unit1Name: String?,
    val unit1Value: Double,
    val unit2Value: Double,
    val unit1Rate: Double,
    val vat: Double
)
