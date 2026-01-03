package com.partsystem.partvisitapp.feature.create_order.model// FactorWithDetails.kt
import androidx.room.Embedded
import androidx.room.Relation
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity

data class FactorWithDetails(
    @Embedded val header: FactorHeaderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "factorId"
    )
    val details: List<FactorDetailEntity>
) {
    val productIds: List<Int>
        get() = details.map { it.productId }
}