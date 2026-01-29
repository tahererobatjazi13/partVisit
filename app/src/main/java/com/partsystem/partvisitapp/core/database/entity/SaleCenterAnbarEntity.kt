package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity

@Entity(tableName = "SaleCenterAnbar", primaryKeys = ["saleCenterId", "anbarId"])
data class SaleCenterAnbarEntity(
    val saleCenterId: Int,
    val anbarId: Int,
    val isActive: Boolean
)
