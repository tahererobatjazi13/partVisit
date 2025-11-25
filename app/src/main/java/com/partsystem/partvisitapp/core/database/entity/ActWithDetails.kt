package com.partsystem.partvisitapp.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ActWithDetails(
    @Embedded val act: ActEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "actId"
    )
    val details: List<ActDetailEntity>
)
