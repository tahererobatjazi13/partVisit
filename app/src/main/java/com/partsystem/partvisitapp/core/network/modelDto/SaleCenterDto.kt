package com.partsystem.partvisitapp.core.network.modelDto

data class SaleCenterDto(
    val id: Int,
    val code: Int,
    val name: String?,
    val saleRateKind: Int,
    val saleCenterAnbars: List<SaleCenterAnbarDto>,
    val saleCenterUsers: List<SaleCenterUserDto>
)


