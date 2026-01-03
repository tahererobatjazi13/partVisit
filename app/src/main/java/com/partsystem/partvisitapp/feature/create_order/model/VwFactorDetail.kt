package com.partsystem.partvisitapp.feature.create_order.model

data class VwFactorDetail(
    val FactorId: Int,
    val Id: Int,
    val SortCode: Int,
    val ProductId: Int,
    val Unit1Value: Double,
    val Unit2Value: Double,
    val Price: Double,
    val PackingId: Int?,
    val PackingValue: Double?,
    val IsGift: Boolean
)
