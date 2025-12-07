package com.partsystem.partvisitapp.core.network.modelDto

data class ProductWithRateDb(
    val productId: Int,
    val code: String?,
    val name: String?,
    val description: String?,
    val rate: Double?,
    val unit2Id: Int?,
    val convertRatio: Double?,
    val calculateUnit2Type: Int?,
    val toll: Double?,
    val vat: Double?,
    val rateAfterVatAndToll: Double?,
    val tollPercent: Double?,
    val vatPercent: Double?,
    val fileName: String?
)
