package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Visitor")
data class VisitorEntity(
    @PrimaryKey val id: Int,
    val tafsiliFullName: String?,
    val tafsiliLastName: String?,
    val tafsiliFirstName: String?,
    val saleCenterId: Int,
    val isVisitorDeactive: Boolean,
    val deactivePersianDate: String?,
    val deactiveDate: String?,
    val description: String?,
    val visitorSabt: Boolean,
    val saleCenterCode: String?,
    val saleCenterName: String?,
    val userId: Int?
)

/*{
    "id": 213,
    "tafsiliFullName": null,
    "tafsiliLastName": null,
    "tafsiliFirstName": null,
    "saleCenterId": 1,
    "isVisitorDeactive": false,
    "deactivePersianDate": null,
    "deactiveDate": null,
    "description": null,
    "visitorSabt": false,
    "saleCenterCode": null,
    "saleCenterName": null,
    "userId": null
}*/
