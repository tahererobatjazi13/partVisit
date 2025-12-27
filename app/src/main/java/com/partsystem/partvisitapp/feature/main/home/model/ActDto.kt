package com.partsystem.partvisitapp.feature.main.home.model

data class ActDto(
    val id: Int,
    val vatId: Int?,
    val code: Int,
    val createDate: String?,
    val fromDate: String?,
    val toDate: String?,
    val arzId: Int?,
    val description: String?,
    val sabt: Boolean,
    val kind: Int,
    val actDetails: List<ActDetailDto>?,
    val patternDetails: List<Any>?
)
