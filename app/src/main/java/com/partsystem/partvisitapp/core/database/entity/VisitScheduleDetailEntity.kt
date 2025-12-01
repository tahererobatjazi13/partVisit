package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visit_schedule_detail_table")
data class VisitScheduleDetailEntity(
    @PrimaryKey val id: Int,
    val visitScheduleId: Int,
    val sortCode: Int,
    val directionId: Int,
    val directionDetailId: Int,
    val customerId: Int,
    val pathPriority: Int
)
