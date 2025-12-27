package com.partsystem.partvisitapp.feature.create_order.model

data class ProductWithRate(
    val id: Int,
    val code: String?,
    val name: String?,
    val description: String?,
    val rate: Double?,
    val toll: Double?,
    val vat: Double?,
    val tollPercent: Double?,
    val vatPercent: Double?,
    val unit2Id: Int?,
    val convertRatio: Double?,
    val calculateUnit2Type: Int?,
    val rateAfterVatAndToll: Double?,
    val fileName: String?
)
