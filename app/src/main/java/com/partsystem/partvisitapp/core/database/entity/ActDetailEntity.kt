package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "act_detail_table"
)
data class ActDetailEntity(
    @PrimaryKey val id: Int,
    val actId: Int,
    val productId: Int,
    val rate: Double,
    val unitKind: Int,
    val sabt: Boolean,
    val useRate: Double,
    val arzRate: Double,
    val description: String?,
    val saleRate: Double,
    val dataDictionaryId: Int?,
    var rateAfterVatAndToll: Float? = null,
    var vatPercent: Float? = null,
    var tollPercent: Float? = null
)

/*
{
    "id": 4305,
    "actId": 67,
    "productId": 319,
    "rate": 1840000.00,
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
