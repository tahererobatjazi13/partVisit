package com.partsystem.partvisitapp.core.utils.persiancalendar.utils

import com.partsystem.partvisitapp.core.utils.persiancalendar.calendar.PersianCalendar


/**
 * Persian month names
 */
internal val MONTH_NAMES = arrayOf(
    "فروردین",
    "اردیبهشت",
    "خرداد",
    "تیر",
    "مرداد",
    "شهریور",
    "مهر",
    "آبان",
    "آذر",
    "دی",
    "بهمن",
    "اسفند"
)

/**
 * Persian days of week abbreviated
 */
private val ABBR_WEEK_DAY_NAMES = arrayOf(
    "ج",
    "پ",
    "چ",
    "س",
    "د",
    "ی",
    "ش"
)

fun getAbbrDayName(day: Int): String {
    return ABBR_WEEK_DAY_NAMES[day]
}

fun getYearMonthDay(timeInMillis: Long): String {
    val calendar = PersianCalendar()
    calendar.timeInMillis = timeInMillis
    return "${calendar.day} ${MONTH_NAMES[calendar.month]}، ${calendar.year}"
}


fun formatYearMonth(calendar: PersianCalendar): String {
    return "${calendar.getMonthName()}، ${calendar.year}"
}

