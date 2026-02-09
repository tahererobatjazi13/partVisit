package com.partsystem.partvisitapp.feature.report_factor.offline.model

data class FactorHeaderDbModel(
    val factorId: Int,
    val customerId: Int,
    val customerName: String?,
    val patternId: Int,
    val patternName: String?,
    val persianDate: String,
    val createTime: String,
    val finalPrice: Long,
    val hasDetail: Boolean,
    val sabt: Int
)
