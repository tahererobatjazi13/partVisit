package com.partsystem.partvisitapp.core.network.modelDto

data class VatDetailDto(
    val id: Int,
    val vatId: Int,
    val productId: Int,
    val vatPercent: Double,
    val tollPercent: Double,
    val taxPercent: Double
)
