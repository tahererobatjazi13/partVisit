package com.partsystem.partvisitapp.feature.report_factor.offline.model

data class FactorHeaderUiModel(
    val factorId: Int,
    val customerName: String?,
    val patternName: String?,
    val persianDate: String,
    val createTime: String,
    val finalPrice: Long,
    val hasDetail: Boolean,
    val sabt: Int,
    val isSending: Boolean = false
)
