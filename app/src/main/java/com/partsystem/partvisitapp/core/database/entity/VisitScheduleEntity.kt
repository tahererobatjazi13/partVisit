package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visit_schedule_table")
data class VisitScheduleEntity(
    @PrimaryKey val id: Int,
    val kind: Int,
    val mainCode: Int,
    val code: Int,
    val createDate: String,
    val persianDate: String,
    val visitorId: Int,
    val sabt: Int,
    val fromHour: String,
    val toHour: String
)
