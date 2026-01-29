package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.partsystem.partvisitapp.core.utils.ImageProductType

@Entity(tableName = "ProductImage")
data class ProductImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ownerId: Int,
    val tableName: String,  // Store.Product, Store.GroupProduct
    val fileName: String,
    val fileData: String, // اینجا می‌تواند Base64 یا URL اولیه باشد
    val localPath: String? = null // مسیر فایل ذخیره شده
) {
    val ownerType: ImageProductType
        get() = if (tableName == "Store.GroupProduct")
            ImageProductType.GROUP_PRODUCT
        else
            ImageProductType.PRODUCT
}

/*{
    "id": 8,
    "ownerId": 5,
    "tableName": "Store.Product",
    "fileName": "images (2)",
    "fileData": "/9j/4AAQSkZJRgABAQAAAQABAAD/...."
}*/
