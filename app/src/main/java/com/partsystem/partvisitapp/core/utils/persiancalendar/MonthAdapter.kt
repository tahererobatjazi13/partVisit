package com.partsystem.partvisitapp.core.utils.persiancalendar

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.google.android.material.textview.MaterialTextView
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.persiancalendar.utils.canonicalYearMonthDay
import com.partsystem.partvisitapp.core.utils.persiancalendar.utils.todayCalendar

internal class MonthAdapter(
    val month: Month,
    val dateSelector: DateSelector<*>?,
    private val calendarConstraints: CalendarConstraints
) : BaseAdapter() {
    var calendarStyle: CalendarStyle? = null
    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getItem(position: Int): Long? {
        return if (position < month.daysFromStartOfWeekToFirstOfMonth() || position > lastPositionInMonth()) {
            null
        } else month.getDay(positionToDay(position))
    }

    override fun getItemId(position: Int): Long {
        return (position / month.daysInWeek).toLong()
    }

    override fun getCount(): Int {
        return month.daysInMonth + firstPositionInMonth()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): MaterialTextView {
        initializeStyles(parent.context)
        var day = convertView

        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(parent.context)
            day = layoutInflater.inflate(R.layout.calendar_day, parent, false) as MaterialTextView
        }

        day as MaterialTextView

        val offsetPosition = position - firstPositionInMonth()
        if (offsetPosition < 0 || offsetPosition >= month.daysInMonth) {
            day.visibility = View.GONE
            day.isEnabled = false
            return day
        }

        val dayNumber = offsetPosition + 1
        day.text = dayNumber.toString()
        day.visibility = View.VISIBLE
        day.isEnabled = true

        val date = getItem(position) ?: return day

        if (!calendarConstraints.dateValidator.isValid(date)) {
            day.isEnabled = false
            calendarStyle!!.invalidDay.styleItem(day)
            setThemeTextColor(day, parent.context)
            return day
        }
        day.isEnabled = true

        for (selectedDay in dateSelector!!.selectedDays) {
            if (canonicalYearMonthDay(date) == canonicalYearMonthDay(selectedDay)) {
                calendarStyle!!.selectedDay.styleItem(day)
                setThemeTextColor(day, parent.context)
                return day
            }
        }

        if (canonicalYearMonthDay(todayCalendar.timeInMillis) == date) {
            calendarStyle!!.todayDay.styleItem(day)
            setThemeTextColor(day, parent.context)
            return day
        }

        calendarStyle!!.day.styleItem(day)
        setThemeTextColor(day, parent.context)

        return day
    }

    private fun setThemeTextColor(view: MaterialTextView, context: Context) {
        val typedArray = context.theme.obtainStyledAttributes(
            intArrayOf(android.R.attr.textColorPrimary)
        )
        try {
            val color = typedArray.getColor(0, Color.BLACK)
            view.setTextColor(color)
        } finally {
            typedArray.recycle()
        }
    }

    private fun initializeStyles(context: Context) {
        if (calendarStyle == null) {
            calendarStyle = CalendarStyle(context)
        }
    }

    fun firstPositionInMonth(): Int {
        return month.daysFromStartOfWeekToFirstOfMonth()
    }

    fun lastPositionInMonth(): Int {
        return month.daysFromStartOfWeekToFirstOfMonth() + month.daysInMonth - 1
    }

    private fun positionToDay(position: Int): Int {
        return position - month.daysFromStartOfWeekToFirstOfMonth() + 1
    }

    fun dayToPosition(day: Int): Int {
        val offsetFromFirst = day - 1
        return firstPositionInMonth() + offsetFromFirst
    }

    fun withinMonth(position: Int): Boolean {
        return position >= firstPositionInMonth() && position <= lastPositionInMonth()
    }

    fun isFirstInRow(position: Int): Boolean {
        return position % month.daysInWeek == 0
    }

    fun isLastInRow(position: Int): Boolean {
        return (position + 1) % month.daysInWeek == 0
    }

    companion object {
        const val MAXIMUM_WEEKS = 6
    }
}