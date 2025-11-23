package com.partsystem.partvisitapp.core.utils.persiancalendar

import android.content.Context
import android.graphics.Paint
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.persiancalendar.utils.getColorStateList
import com.partsystem.partvisitapp.core.utils.persiancalendar.utils.resolve

class CalendarStyle(context: Context) {
    val day: CalendarItemStyle
    val selectedDay: CalendarItemStyle
    val todayDay: CalendarItemStyle
    val year: CalendarItemStyle
    val selectedYear: CalendarItemStyle

    val todayYear: CalendarItemStyle
    val invalidDay: CalendarItemStyle

    val rangeFill: Paint

    init {
        val calendarStyle: Int
        val style = resolve(context, R.attr.persianMaterialCalendarStyle)

        calendarStyle = style?.data ?: R.style.PersianMaterialCalendar_Default

        val calendarAttributes =
            context.obtainStyledAttributes(calendarStyle, R.styleable.PersianMaterialCalendar)

        day = CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(R.styleable.PersianMaterialCalendar_dayStyle, 0)
        )

        selectedDay = CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(
                R.styleable.PersianMaterialCalendar_daySelectedStyle,
                0
            )
        )

        invalidDay = CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(R.styleable.PersianMaterialCalendar_dayInvalidStyle, 0)
        )
        todayDay = CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(R.styleable.PersianMaterialCalendar_dayTodayStyle, 0)
        )
        val rangeFillColorList =
            getColorStateList(
                context, calendarAttributes, R.styleable.PersianMaterialCalendar_rangeFillColor)

        year = CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(R.styleable.PersianMaterialCalendar_yearStyle, 0)
        )

        selectedYear = CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(
                R.styleable.PersianMaterialCalendar_yearSelectedStyle,
                0
            )
        )

        todayYear = CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(R.styleable.PersianMaterialCalendar_yearTodayStyle, 0)
        )
        rangeFill = Paint()
        rangeFill.color = rangeFillColorList!!.defaultColor
        calendarAttributes.recycle()
    }
}