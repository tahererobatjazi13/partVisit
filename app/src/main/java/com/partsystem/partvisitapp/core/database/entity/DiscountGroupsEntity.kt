package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "DiscountGroup",
    foreignKeys = [
        ForeignKey(
            entity = DiscountEntity::class,
            parentColumns = ["id"],
            childColumns = ["discountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("discountId")]
)
data class DiscountGroupsEntity(
    @PrimaryKey
    val id: Int,
    val discountId: Int,
    val kind: Int,
    val groupId: Int,
    val groupDetailId: Int,
    val rastehId: Int
)
