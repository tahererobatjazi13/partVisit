package com.partsystem.partvisitapp.core.utils

object Constant {

    const val BASE_URL = "http://hf2093f87sf.sn.mynetname.net:52438/api/Android/"
}

enum class SnackBarType(val value: String) {
    Error("error"),
    Success("success"),
    Warning("warning"),
}

enum class ReportFactorListType(val value: String) {
    Customer("customer"),
    Visitor("visitor")
}