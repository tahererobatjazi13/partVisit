package com.partsystem.partvisitapp.feature.main.home.model

data class CustomerDto(
    val id: Int,
    val code: Int,
    val name: String,
    val groupId: Int,
    val groupDetailId: Int,
    val tafsiliNationalId: String?,
    val saleCenterId: Int,
    val degreeId: Int?,
    val processKindId: Int?,
    val customerKindId: Int,
    val isCustomerDeactive: Boolean,
    val deactivePersianDate: String?,
    val deactiveDate: String?,
    val customerSabt: Boolean,
    val tafsiliPhone1: String?,
    val tafsiliPhone2: String?,
    val tafsiliMobile: String?,
)
