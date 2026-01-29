package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SaleCenter")
data class SaleCenterEntity(
    @PrimaryKey val id: Int,
    val code: Int,
    val name: String?,
    val saleRateKind: Int
)

/*{
    "id": 1,
    "code": 1,
    "name": "واحد نیشابور - امیر کبیر",
    "saleRateKind": 0,
    "saleCenterAnbars": [
    {
        "saleCenterId": 1,
        "anbarId": 1,
        "isActive": true,
        "saleCenter": null
    },
    {
        "saleCenterId": 1,
        "anbarId": 2,
        "isActive": false,
        "saleCenter": null
    }
    ],
    "saleCenterUsers": [
    {
        "saleCenterId": 1,
        "userId": 1,
        "saleCenter": null
    },
    {
        "saleCenterId": 1,
        "userId": 8,
        "saleCenter": null
    }
    ],
    "visitors": []
}*/
