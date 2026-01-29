package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "InvoiceCategory")
data class InvoiceCategoryEntity(
    @PrimaryKey val id: Int,
    val code: Int,
    val name: String,
    val kind: Int,
    val fromSerial: Int?,
    val toSerial: Int?,
    val hasVatToll: Boolean,
    val isVatEditable: Boolean
)

/*{
    "id": 1,
    "code": 3,
    "name": "ثبت سفارش ",
    "kind": 17,
    "fromSerial": 200000,
    "toSerial": 250000,
    "hasVatToll": true,
    "isVatEditable": false,
    "tafsiliType": null,
    "invoiceCategoryDetails": [],
    "invoiceCategoryCenters": []
}*/
