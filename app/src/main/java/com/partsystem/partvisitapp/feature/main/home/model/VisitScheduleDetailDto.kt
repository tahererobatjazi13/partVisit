package com.partsystem.partvisitapp.feature.main.home.model

data class VisitScheduleDetailDto(
    val id: Int,
    val visitScheduleId: Int,
    val sortCode: Int,
    val directionId: Int,
    val directionDetailId: Int,
    val customerId: Int,
    val pathPriority: Int
)
