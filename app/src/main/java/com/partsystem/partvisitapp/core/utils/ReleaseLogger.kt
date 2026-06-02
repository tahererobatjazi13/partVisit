package com.partsystem.partvisitapp.core.utils

object ReleaseLogger {
    private const val TAG = "PartVisitRelease"
    
    // این متد حتی در ریلیز هم لاگ چاپ می‌کند
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        println("[$TAG] $tag: $message")
        throwable?.printStackTrace()
    }
}