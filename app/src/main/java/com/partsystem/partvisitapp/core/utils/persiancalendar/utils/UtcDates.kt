package com.partsystem.partvisitapp.core.utils.persiancalendar.utils

import com.partsystem.partvisitapp.core.utils.persiancalendar.calendar.PersianCalendar
import java.util.*
import java.util.Calendar.*


private const val IRST = "IRST"

/**
 * Iran's timezone
 */
private val timeZone: TimeZone
    get() = TimeZone.getTimeZone(IRST)

/**
 * A [PersianCalendar] instance representing the day
 */
val todayCalendar: PersianCalendar
    get() {
        val calendar = getInstance(timeZone)
        return PersianCalendar(calendar)
    }

/**
 * A [PersianCalendar] with no data
 */
val iranCalendar: PersianCalendar
    get() = getIranCalendar(null)

/**
 * Converts a [Calendar] to a [PersianCalendar] with [IRST] timezone
 * @return [PersianCalendar] with [IRST] timezone
 */
private fun getIranCalendar(rawCalendar: Calendar?): PersianCalendar {
    val utc = getInstance(timeZone)

    if (rawCalendar == null) utc.clear()
    else utc.timeInMillis = rawCalendar.timeInMillis

    return PersianCalendar(utc)
}

/**
 * Clears all data from rawCalendar except date and
 * returns a [PersianCalendar] with same date as rawCalendar
 */
fun getDayCopy(rawCalendar: Calendar): PersianCalendar {
    val raw = getIranCalendar(rawCalendar)
    val irCalendar = iranCalendar

    irCalendar.set(raw.get(YEAR), raw.get(MONTH), raw.get(DAY_OF_MONTH))
    return irCalendar
}


fun canonicalYearMonthDay(rawDate: Long): Long {
    val calendar = PersianCalendar()
    calendar.timeInMillis = rawDate
    calendar.timeZone = TimeZone.getTimeZone(IRST)
    calendar.set(HOUR_OF_DAY, 0)
    calendar.set(MINUTE, 0)
    calendar.set(SECOND, 0)
    calendar.set(MILLISECOND, 0)
    return calendar.timeInMillis
}