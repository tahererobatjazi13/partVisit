package com.partsystem.partvisitapp.core.network.modelDto

data class VisitScheduleDetailDto(
    val id: Int,
    val visitScheduleId: Int,
    val sortCode: Int,
    val directionId: Int,
    val directionDetailId: Int,
    val customerId: Int,
    val pathPriority: Int
)
