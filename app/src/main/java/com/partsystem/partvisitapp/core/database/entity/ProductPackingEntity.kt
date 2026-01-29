package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ProductPacking")
data class ProductPackingEntity(
    @PrimaryKey val id: Int,
    val productId: Int,
    val packingId: Int,
    val unit1Id: Int,
    val unit1Value: Double,
    val unit2Id: Int,
    val unit2Value: Double,
    val length: Double,
    val width: Double,
    val height: Double,
    val volume: Double,
    val weight: Double,
    val isDefault: Boolean,
    val isDisable: Boolean,
    val packingCode: Int,
    val packingName: String?,
    val unit1Code: Int,
    val unit1Name: String?,
    val unit2Code: Int,
    val unit2Name: String?
)

/*{
    "id": 1,
    "productId": 227,
    "packingId": 161,
    "unit1Id": 3111,
    "unit1Value": 7.0,
    "unit2Id": 3109,
    "unit2Value": 0.0,
    "length": 0.0,
    "width": 0.0,
    "height": 0.0,
    "volume": 0.0,
    "weight": 0.0,
    "isDefault": false,
    "isDisable": false,
    "packingCode": 161,
    "packingName": "سطل 7 کیلوگرمی",
    "unit1Code": 8,
    "unit1Name": "کیلوگرم",
    "unit2Code": 6,
    "unit2Name": "سطل "
}*/
