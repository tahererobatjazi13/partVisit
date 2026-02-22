package com.partsystem.partvisitapp.core.utils

object ReleaseLogger {
    private const val TAG = "PartVisitRelease"
    
    // این متد حتی در ریلیز هم لاگ چاپ می‌کند
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        // System.out.println برای نمایش در Logcat حتی در بیلد ریلیز کار می‌کند
        System.out.println("[$TAG] $tag: $message")
        throwable?.printStackTrace()
    }
}