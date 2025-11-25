package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class VatWithDetails(
    @Embedded val vat: VatEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "vatId"
    )
    val details: List<VatDetailEntity>
)
