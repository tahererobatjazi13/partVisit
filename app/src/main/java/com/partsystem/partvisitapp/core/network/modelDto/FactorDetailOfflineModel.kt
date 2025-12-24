package com.partsystem.partvisitapp.core.network.modelDto

data class FactorDetailOfflineModel(
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
