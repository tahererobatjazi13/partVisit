package com.partsystem.partvisitapp.feature.main.home.model

data class ProductImageDto(
    val id: Int,
    val ownerId: Int,
    val tableName: String,
    val fileName: String,
    val fileData: String,
)
