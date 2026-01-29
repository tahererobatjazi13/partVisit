package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Vat")
data class VatEntity(
    @PrimaryKey val id: Int,
    val code: Int,
    val createDate: String,
    val validDate: String,
    val serviceCalculateKind: Int,
    val productCalculateKind: Int,
    val vatPercent: Double,
    val tollPercent: Double,
    val description: String?,
    val sabt: Boolean,
    val kind: Int
)

/*{
    "id": 1,
    "code": 1,
    "createDate": "2023-03-21T00:00:00",
    "validDate": "2026-03-20T00:00:00",
    "serviceCalculateKind": 0,
    "productCalculateKind": 1,
    "vatPercent": 10.0,
    "tollPercent": 0.0,
    "description": "مالیات بر ارزش افزوده - واحد کارخانه",
    "sabt": true,
    "kind": 2,
    "vatDetails": [
    {
        "id": 531,
        "vatId": 1,
        "productId": 106,
        "vatPercent": 10.0,
        "tollPercent": 0.0,
        "taxPercent": 0.0,
        "vat": null
    }
    ]
}*/
