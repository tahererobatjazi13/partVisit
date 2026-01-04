package com.partsystem.partvisitapp.feature.create_order.model

data class VwFactorDetail(
    val factorId: Int,
    val id: Int,
    val sortCode: Int,
    val productId: Int,
    val unit1Value: Double,
    val unit2Value: Double,
    val price: Double,
    val packingId: Int?,
    val packingValue: Double?,
    val isGift: Boolean
)
