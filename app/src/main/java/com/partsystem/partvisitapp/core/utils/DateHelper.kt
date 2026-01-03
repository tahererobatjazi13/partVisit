package com.partsystem.partvisitapp.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateHelper {

    fun dateDiffDay(date1: String, date2: String): Long {
        return try {
            val diff =
                parseDateTime(date2).time - parseDateTime(date1).time
            TimeUnit.MILLISECONDS.toDays(diff)
        } catch (e: Exception) {
            0
        }
    }

    private fun parseDateTime(date: String): Date {
        val format = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return format.parse(date)!!
    }
}
