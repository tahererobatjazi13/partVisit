package com.partsystem.partvisitapp.core.utils.persiancalendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.google.android.material.textview.MaterialTextView
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.persiancalendar.calendar.PersianCalendar
import com.partsystem.partvisitapp.core.utils.persiancalendar.calendar.PersianCalendar.Companion.DAYS_IN_WEEK
import com.partsystem.partvisitapp.core.utils.persiancalendar.utils.getAbbrDayName

/**
 * A single row adapter representing the days of the week for [PersianCalendar].
 * This [android.widget.Adapter] respects the [PersianCalendar.getFirstDayOfWeek]
 */
internal class DaysOfWeekAdapter : BaseAdapter() {

    override fun getItem(position: Int): Int? {
        return if (position > DAYS_IN_WEEK) {
            null
        } else position
    }

    override fun getItemId(position: Int): Long = 0

    override fun getCount(): Int = DAYS_IN_WEEK + 1

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View? {
        var dayOfWeek = convertView
        if (dayOfWeek == null) {
            val layoutInflater = LayoutInflater.from(parent.context)
            dayOfWeek = layoutInflater.inflate(R.layout.calendar_day_of_week, parent, false)
        }

        (dayOfWeek as MaterialTextView).text = getAbbrDayName(position)
        return dayOfWeek
    }
}