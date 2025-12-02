package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pattern_details_table")
data class PatternDetailEntity(
    @PrimaryKey
    val id: Int,
    val patternId: Int,
    val kind: Int,
    val discountId: Int,
    val customerId: Int?,
    val customerKindId: Int?,
    val centerId: Int?,
    val invoiceCategoryId: Int?,
    val processId: Int?,
    val areaId: Int?,
    val actId: Int?,
    val isDefault: Boolean,
    val customerFilterKind: Int?,
    val customerDegreeId: Int?,
    val customerPishehId: Int?,
    val tafsiliGroupId: Int?,
    val tafsiliGroupDetailId: Int?
)
/*
{
    "id": 7272,
    "actId": 99,
    "productId": 8,
    "rate": 3144444.00,
    "unitKind": 0,
    "sabt": true,
    "useRate": 0.00,
    "arzRate": 0.00,
    "description": null,
    "saleRate": 0.00,
    "dataDictionaryId": null,
    "act": null,
    "product": null
}*/
