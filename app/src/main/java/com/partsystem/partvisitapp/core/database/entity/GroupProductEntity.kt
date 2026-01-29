package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "GroupProduct")
data class GroupProductEntity(
    @PrimaryKey val id: Int,
    val parentId: Int?,
    val code: Int,
    val name: String,
    val groupLevel: Int,
    val kind: Int
)
/*
 {
        "id": 251,
        "parentId": 163,
        "code": 1,
        "name": "شوینده ها و مواد شیمیایی",
        "groupLevel": 3,
        "kind": 2,
        "parent": null,
        "inverseParent": []
    }*/
