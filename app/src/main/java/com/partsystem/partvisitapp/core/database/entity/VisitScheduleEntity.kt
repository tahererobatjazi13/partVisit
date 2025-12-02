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
/*
{
    "id": 1,
    "kind": 0,
    "mainCode": 1,
    "code": 1,
    "createDate": "2025-12-01T00:00:00",
    "persianDate": "1404/09/10",
    "visitorId": 2471,
    "sabt": 1,
    "fromHour": "07:00:00",
    "toHour": "15:00:00",
    "visitScheduleDetails": []
}*/