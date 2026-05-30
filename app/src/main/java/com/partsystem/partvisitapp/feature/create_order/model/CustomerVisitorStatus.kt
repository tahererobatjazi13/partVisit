package com.partsystem.partvisitapp.feature.create_order.model

data class CustomerVisitorStatus(
    val hasErrorOrder: Boolean,
    val hasWarningOrder: Boolean
)