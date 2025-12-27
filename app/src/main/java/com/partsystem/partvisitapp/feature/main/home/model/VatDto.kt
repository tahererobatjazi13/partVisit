package com.partsystem.partvisitapp.feature.main.home.model

data class VatDto(
    val id: Int,
    val code: Int,
    val createDate: String,
    val validDate: String,
    val serviceCalculateKind: Int,
    val productCalculateKind: Int,
    val vatPercent: Double,
    val tollPercent: Double,
    val description: String?,
    val sabt: Boolean,
    val kind: Int,
    val vatDetails: List<VatDetailDto>
)

