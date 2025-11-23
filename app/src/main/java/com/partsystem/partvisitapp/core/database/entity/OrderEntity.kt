package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_table")
data class OrderEntity(
    @PrimaryKey val productId: Int,
    val productName: String,
    var quantity: Int,
    val price: Double
)
