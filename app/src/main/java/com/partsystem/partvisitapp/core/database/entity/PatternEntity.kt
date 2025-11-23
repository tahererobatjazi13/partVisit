package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pattern_table")
data class PatternEntity(
    @PrimaryKey val id: Int,
    val code: Int,
    val name: String,
    val createDate: String?,
    val persianDate: String?,
    val fromDate: String?,
    val fromPersianDate: String?,
    val toDate: String?,
    val toPersianDate: String?,
    val arzId: Int?,
    val description: String?,
    val settelmentKind: Int?,
    val creditDuration: Int?,
    val discountInclusionKind: Int?,
    val groupInclusionKind: Int?,
    val centerInclusionKind: Int?,
    val customerInclusionKind: Int?,
    val processInclusionKind: Int?,
    val regionInclusionKind: Int?,
    val sabt: Boolean,
    val fromSaleAmount: Double?,
    val toSaleAmount: Double?,
    val hasCash: Boolean,
    val hasMaturityCash: Boolean,
    val hasSanad: Boolean,
    val hasSanadAndCash: Boolean,
    val hasCredit: Boolean,
    val dayCount: Int?,
    val hasAndroid: Boolean,
    val patternDetails: String? // لیست رو به JSON تبدیل می‌کنیم
)


/*
{
    "id": 26,
    "code": 19,
    "name": "مشهد - هتل رستوران",
    "createDate": "2024-03-20T00:00:00",
    "persianDate": "1403/01/01",
    "fromDate": "2024-03-20T00:00:00",
    "fromPersianDate": "1403/01/01",
    "toDate": "2026-03-20T00:00:00",
    "toPersianDate": "1404/12/29",
    "arzId": null,
    "description": null,
    "settelmentKind": 2,
    "creditDuration": 0,
    "discountInclusionKind": 1,
    "groupInclusionKind": 1,
    "centerInclusionKind": 1,
    "customerInclusionKind": 2,
    "processInclusionKind": 0,
    "regionInclusionKind": 0,
    "sabt": true,
    "fromSaleAmount": 0.00,
    "toSaleAmount": 0.00,
    "hasCash": true,
    "hasMaturityCash": true,
    "hasSanad": true,
    "hasSanadAndCash": true,
    "hasCredit": true,
    "dayCount": 0,
    "hasAndroid": true,
    "patternDetails": []
}*/
