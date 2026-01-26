package com.partsystem.partvisitapp.core.network.modelDto

data class ApplicationSettingDto(
    val id: Int,
    val moduleId: Int,
    val code: Int,
    val name: String,
    val description: String?,
    val controlType: Int,
    val itemSource: String?,
    val defaultValue: String?,
    val value: String?
)
