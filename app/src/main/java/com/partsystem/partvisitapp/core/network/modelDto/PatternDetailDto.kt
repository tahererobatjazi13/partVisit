package com.partsystem.partvisitapp.core.network.modelDto

data class PatternDetailDto(
    val id: Int,
    val patternId: Int,
    val kind: Int,
    val discountId: Int,
    val customerId: Int?,
    val customerKindId: Int?,
    val centerId: Int?,
    val invoiceCategoryId: Int?,
    val processId: Int?,
    val areaId: Int?,
    val actId: Int?,
    val isDefault: Boolean,
    val customerFilterKind: Int?,
    val customerDegreeId: Int?,
    val customerPishehId: Int?,
    val tafsiliGroupId: Int?,
    val tafsiliGroupDetailId: Int?
)
