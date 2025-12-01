package com.partsystem.partvisitapp.core.network.modelDto

data class VisitScheduleDto(
    val id: Int,
    val kind: Int,
    val mainCode: Int,
    val code: Int,
    val createDate: String,
    val persianDate: String,
    val visitorId: Int,
    val sabt: Int,
    val fromHour: String,
    val toHour: String,
    val visitScheduleDetails: List<VisitScheduleDetailDto>
)
