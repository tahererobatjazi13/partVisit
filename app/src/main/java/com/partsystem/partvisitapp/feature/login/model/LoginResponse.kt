package com.partsystem.partvisitapp.feature.login.model

data class LoginResponse(
    val isSuccess: Boolean,
    val message: String?,
    val listResult: List<User>?,
    val status: String?
)

data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val personnelId: Int
)
