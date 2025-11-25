package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity

@Entity(tableName = "sale_center_user_table", primaryKeys = ["saleCenterId", "userId"])
data class SaleCenterUserEntity(
    val saleCenterId: Int,
    val userId: Int
)
