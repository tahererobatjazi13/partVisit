package com.partsystem.partvisitapp.core.network.modelDto

data class AssignDirectionCustomerDto(
    val id: Int,
    val assignDirectionId: Int,
    val tafsiliId: Int,
    val mainCode: Int,
    val code: Int,
    val createDate: String?,
    val persianDate: String?,
    val isVisit: Boolean,
    val isDistribution: Boolean,
    val isDemands: Boolean,
    val isActive: Boolean,
    val customerId: Int,
    val saleCenterId: Int,
    val isVisitorDeactive: Boolean,
    val customerCode: Int,
    val customerName: String?,
    val tafsiliCode: Int,
    val tafsiliName: String?
)
