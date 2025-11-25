package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assign_direction_customer_table")
data class AssignDirectionCustomerEntity(
    @PrimaryKey val id: Int,
    val assignDirectionId: Int,
    val tafsiliId: Int,
    val mainCode: Int,
    val code: Int,
    val createDate: String?,
    val persianDate: String?,
    val isVisit: Boolean,
    val isDistribution: Boolean,
    val isDemands: Boolean,
    val isActive: Boolean,
    val customerId: Int,
    val saleCenterId: Int,
    val isVisitorDeactive: Boolean,
    val customerCode: Int,
    val customerName: String?,
    val tafsiliCode: Int,
    val tafsiliName: String?
)
/*

{
    "id": 25365,
    "assignDirectionId": 106,
    "tafsiliId": 2471,
    "mainCode": 2,
    "code": 2,
    "createDate": "2025-09-04T00:00:00",
    "persianDate": "1404/06/13",
    "isVisit": true,
    "isDistribution": false,
    "isDemands": false,
    "isActive": true,
    "customerId": 13737,
    "saleCenterId": 3,
    "isVisitorDeactive": false,
    "customerCode": 22493624,
    "customerName": "عطیه نوروز زاده قرغابی.....(کارشناس فروش:رضایی)",
    "tafsiliCode": 4561016,
    "tafsiliName": "رضایی - نرگس"
}*/
