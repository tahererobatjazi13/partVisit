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
/*
{
    "id": 3,
    "visitScheduleId": 1,
    "sortCode": 3,
    "directionId": 2,
    "directionDetailId": 2527,
    "customerId": 6434,
    "pathPriority": 3
}*/
