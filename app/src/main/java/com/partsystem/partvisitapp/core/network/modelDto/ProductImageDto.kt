package com.partsystem.partvisitapp.core.network.modelDto

data class ProductImageDto(
    val id: Int,
    val ownerId: Int,
    val tableName: String,
    val fileName: String,
    val fileData: String,
)
