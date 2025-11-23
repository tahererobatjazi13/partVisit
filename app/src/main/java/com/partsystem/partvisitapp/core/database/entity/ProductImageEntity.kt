package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_images_table")
data class ProductImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ownerId: Int,
    val tableName: String,
    val fileName: String,
    val fileData: String, // اینجا می‌تواند Base64 یا URL اولیه باشد
    val localPath: String? = null // مسیر فایل ذخیره شده
)

/*
{
    "id": 8,
    "ownerId": 5,
    "tableName": "Store.Product",
    "fileName": "images (2)",
    "fileData": "/9j/4AAQSkZJRgABAQAAAQABAAD/...."
}*/
