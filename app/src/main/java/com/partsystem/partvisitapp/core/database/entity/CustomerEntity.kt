package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Customer")
data class CustomerEntity(
    @PrimaryKey val id: Int,
    val code: Int,
    val name: String,
    val groupId: Int,
    val groupDetailId: Int,
    val tafsiliNationalId: String?,
    val saleCenterId: Int,
    val degreeId: Int?,
    val processKindId: Int?,
    val customerKindId: Int,
    val isCustomerDeactive: Boolean,
    val deactivePersianDate: String?,
    val deactiveDate: String?,
    val customerSabt: Boolean,
    val tafsiliPhone1: String?,
    val tafsiliPhone2: String?,
    val tafsiliMobile: String?,
)

/*{
    "id": 1547,
    "code": 4501032,
    "name": "کسائیان نائینی -  فرشاد",
    "groupId": 4,
    "groupDetailId": 10,
    "tafsiliFullName": null,
    "tafsiliLastName": "کسائیان نائینی ",
    "tafsiliFirstName": "فرشاد ",
    "tafsiliKind": 0,
    "tafsiliNationalId": null,
    "tafsiliRegisterationNumber": null,
    "tafsiliEconomicNumber": null,
    "tafsiliCity": null,
    "shenasemeli": null,
    "newEconomicNumber": null,
    "tafsiliSite": null,
    "tafsiliEmail": null,
    "tafsiliPhone1": null,
    "tafsiliPhone2": null,
    "tafsiliMobile": null,
    "tafsiliFax": null,
    "saleCenterId": 3,
    "degreeId": null,
    "processKindId": null,
    "customerKindId": 3007,
    "isCustomerDeactive": false,
    "description": null,
    "customerSabt": true
}*/
