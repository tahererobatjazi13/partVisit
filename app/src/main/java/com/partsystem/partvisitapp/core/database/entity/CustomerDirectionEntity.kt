package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "customer_direction_table")
data class CustomerDirectionEntity(
    @PrimaryKey val id: Int,
    val customerId: Int,
    val sortCode: Int,
    val cityId: Int,
    val directionId: Int,
    val areaId: Int,
    val directionDetailId: Int,
    val fullAddress: String,
    val cityCode: String,
    val cityName: String,
    val latitude: Double?,
    val longitude: Double?,
    val mainStreet: String,
    val subStreet: String,
    val mahalehCode: String,
    val mahalehName: String,
    val phone1: String?,
    val phone2: String?,
    val mobile: String?,
    val fax: String?,
    val webSite: String?,
    val email: String?,
    val isVisit: Boolean,
    val isDistribution: Boolean,
    val isPayment: Boolean,
    val isActive: Boolean,
    val isMainAddress: Boolean
)

/*
{
    "id": 424,
    "customerId": 3585,
    "sortCode": 0,
    "cityId": 14,
    "directionId": 0,
    "areaId": 0,
    "directionDetailId": 17088,
    "fullAddress": "ن م -فارغ التحصیلان بین 4و6-218186-مشهد-مسیر توزیع قاسم آباد- آزادشهر-.",
    "cityCode": "14",
    "cityName": "مشهد",
    "latitude": null,
    "longitude": null,
    "mainStreet": "فارغ التحصیلان بین 4و6",
    "subStreet": "218186",
    "mahalehCode": "1016",
    "mahalehName": "ن م ",
    "phone1": null,
    "phone2": null,
    "mobile": null,
    "fax": null,
    "webSite": null,
    "email": null,
    "isVisit": false,
    "isDistribution": false,
    "isPayment": false,
    "isActive": false,
    "isMainAddress": false
}*/
