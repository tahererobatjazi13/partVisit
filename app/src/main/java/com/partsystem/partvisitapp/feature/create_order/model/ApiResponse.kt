package com.partsystem.partvisitapp.feature.create_order.model

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @SerializedName("isSuccess")
    val isSuccess: Boolean,

    @SerializedName("message")
    val message: String?,

    @SerializedName("listResult")
    val listResult: Any?,

    @SerializedName("status")
    val status: Int?
)
