package com.partsystem.partvisitapp.feature.main.home.model

data class ProductPackingDto(
    val id: Int,
    val productId: Int,
    val packingId: Int,
    val unit1Id: Int,
    val unit1Value: Double,
    val unit2Id: Int,
    val unit2Value: Double,
    val length: Double,
    val width: Double,
    val height: Double,
    val volume: Double,
    val weight: Double,
    val isDefault: Boolean,
    val isDisable: Boolean,
    val packingCode: Int,
    val packingName: String?,
    val unit1Code: Int,
    val unit1Name: String?,
    val unit2Code: Int,
    val unit2Name: String?
)
