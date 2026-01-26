package com.partsystem.partvisitapp.core.network.modelDto

data class GroupProductDto(
    val id: Int,
    val parentId: Int?,
    val code: Int,
    val name: String,
    val groupLevel: Int,
    val kind: Int
)

