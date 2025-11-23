package com.partsystem.partvisitapp.feature.main.home.model


data class ActDetailDto(
    val id: Int,
    val actId: Int,
    val productId: Int,
    val rate: Double,
    val unitKind: Int,
    val sabt: Boolean,
    val useRate: Double,
    val arzRate: Double,
    val description: String?,
    val saleRate: Double,
    val dataDictionaryId: Int?
)