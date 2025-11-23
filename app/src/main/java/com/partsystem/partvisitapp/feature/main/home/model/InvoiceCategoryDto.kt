package com.partsystem.partvisitapp.feature.main.home.model

data class InvoiceCategoryDto(
    val id: Int,
    val code: Int,
    val name: String,
    val kind: Int,
    val fromSerial: Int?,
    val toSerial: Int?,
    val hasVatToll: Boolean,
    val isVatEditable: Boolean
)
/*
"invoiceCategoryDetails": []
*/
