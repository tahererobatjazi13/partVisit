package com.partsystem.partvisitapp.feature.create_order.model

import androidx.room.ColumnInfo

data class DiscountEshantyunResult(

    @ColumnInfo(name = "AnbarId")
    val anbarId: Int,

    @ColumnInfo(name = "ProductId")
    val productId: Int,

    @ColumnInfo(name = "PackingId")
    val packingId: Int,

    @ColumnInfo(name = "UnitKind")
    val unitKind: Int,

    @ColumnInfo(name = "Value")
    val value: Double
)
