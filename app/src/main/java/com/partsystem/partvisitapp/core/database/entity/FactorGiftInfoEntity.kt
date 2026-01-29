package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "FactorGiftInfo",
    foreignKeys = [
        ForeignKey(
            entity = FactorHeaderEntity::class,
            parentColumns = ["id"],
            childColumns = ["factorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("factorId")
    ]
)
data class FactorGiftInfoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val factorId: Int,
    val productId: Int,
    val discountId: Int,
    var price: Double
)

