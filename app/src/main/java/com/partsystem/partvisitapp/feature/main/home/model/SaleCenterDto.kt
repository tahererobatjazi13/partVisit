package com.partsystem.partvisitapp.feature.main.home.model

data class SaleCenterDto(
    val id: Int,
    val code: Int,
    val name: String?,
    val saleRateKind: Int,
    val saleCenterAnbars: List<SaleCenterAnbarDto>,
    val saleCenterUsers: List<SaleCenterUserDto>
)


