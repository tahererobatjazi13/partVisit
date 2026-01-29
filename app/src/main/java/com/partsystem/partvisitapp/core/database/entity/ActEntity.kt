package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Act")
data class ActEntity(
    @PrimaryKey val id: Int,
    val vatId: Int?,
    val code: Int,
    val createDate: String?,
    val fromDate: String?,
    val toDate: String?,
    val arzId: Int?,
    val description: String?,
    val sabt: Boolean,
    val kind: Int
)

/*{
    "id": 67,
    "vatId": 2,
    "code": 12,
    "createDate": "0001-01-01T00:00:00",
    "fromDate": "2024-03-20T00:00:00",
    "toDate": "2025-09-22T00:00:00",
    "arzId": null,
    "description": "نجفی تبریز",
    "sabt": true,
    "kind": 2,
    "actDetails": [],
    "patternDetails": []
}*/
