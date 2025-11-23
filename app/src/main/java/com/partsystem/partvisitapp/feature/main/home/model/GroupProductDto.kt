package com.partsystem.partvisitapp.feature.main.home.model

data class GroupProductDto(
    val id: Int,
    val parentId: Int?,
    val code: Int,
    val name: String,
    val groupLevel: Int,
    val kind: Int
)

