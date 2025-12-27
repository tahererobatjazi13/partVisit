package com.partsystem.partvisitapp.feature.login.model

data class VisitorDto(
    val id: Int,
    val tafsiliFullName: String?,
    val tafsiliLastName: String?,
    val tafsiliFirstName: String?,
    val saleCenterId: Int,
    val isVisitorDeactive: Boolean,
    val deactivePersianDate: String?,
    val deactiveDate: String?,
    val description: String?,
    val visitorSabt: Boolean,
    val saleCenterCode: String?,
    val saleCenterName: String?,
    val userId: Int?
)
