package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "factor_header_table")
data class FactorHeaderEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var uniqueId: String? = null,
    var formKind: Int? = null,
    var centerId: Int? = null,
    var code: Int? = null,
    var createDate: String? = null,
    var persianDate: String? = null,
    var invoiceCategoryId: Int = 0,
    var patternId: Int = 0,
    var dueDate: String? = null,
    var processId: Int? = null,
    var deliveryDate: String? = null,
    var createTime: String? = null,
    var customerId: Int? = null,
    var directionDetailId: Int? = null,
    var visitorId: Int? = null,
    var distributorId: Int? = null,
    var description: String = "",
    var isCanceled: Int? = null,
    var sabt: Int = 0,
    var hasDetail: Boolean = false,
    var createUserId: Int? = null,
    var saleCenterId: Int? = null,
    var actId: Int = 0,
    var recipientId: Int? = null,
    var settlementKind: Int = -1,
    var createSource: Int = 0,
    var serverSyncDate: Long? = null,
    var finalPrice: Double = 0.0,
    var defaultAnbarId: Int? = null,
    var latitude: Double? = 0.0,
    var longitude: Double? = 0.0
)

/*[
{
    "uniqueId": "669DAF28-506C-4EAE-AAC3-F901B0B62A39",
    "id": 78,
    "formKind": 17,
    "centerId": 1,
    "mainCode": 90901,
    "code": 12999,
    "createDate": "2025-11-30",
    "invoiceCategoryId": 10,
    "patternId": 66,
    "dueDate": "2025-11-30",

    "SaleCenterId": 2526,
    "customerId": 2526,
    "visitorId": 2512,

    "description": "Test Android",
    "sabt": 1,
    "createUserId": 1,


    "saleCenterId": 3,
    "actId": 195,
    "settlementKind": 0,
    "deliveryDate": "2025-11-30",

    "createTime": "02:30:15",
    "directionDetailId": 22239,
    "latitude": 0,
    "longitude": 0,
    "factorDetails": [
    {
        "factorId": 78,
        "id": 140,
        "sortCode": 1,
        "anbarId": 6,
        "productId": 107,
        "actId": 195,
        "unit1Value": 40,
        "unit2Value": 0,
        "price": 80000,
        "description": "string  test",
        "packingId": 3,
        "packingValue": 2,
        "vat": 7320,
        "productSerial": 0,
        "isGift": 0,
        "returnCauseId": 0,
        "isCanceled": 0,
        "isModified": 0,
        "unit1Rate": 2000,
        "factorDiscounts": [
        {

            "id": 66,
            "sortCode": 1,
            "discountId": 130,
            "price": 6800,
            "arzPrice": 0,
            "factorDetailId": 140,
            "discountPercent": 0
        }
        ]
    }
    ],
    "factorGiftInfos": [
    {
        "id": 10,
        "factorId": 78,
        "discountId": 107,
        "productId": 107,
        "price": 4000,
        "arzPrice": 0
    }
    ]
}
]*/
