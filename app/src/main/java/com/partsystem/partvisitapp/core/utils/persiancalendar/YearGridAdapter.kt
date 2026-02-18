package com.partsystem.partvisitapp.core.utils.persiancalendar

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.persiancalendar.utils.todayCalendar

internal class YearGridAdapter(private val materialCalendar: MaterialCalendar<*>) :
    RecyclerView.Adapter<YearGridAdapter.ViewHolder>() {

    class ViewHolder internal constructor(val textView: MaterialTextView) :
        RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val yearTextView = LayoutInflater.from(viewGroup.context).inflate(
            R.layout.calendar_year,
            viewGroup,
            false
        ) as MaterialTextView
        return ViewHolder(yearTextView)
    }

    override fun onBindViewHolder(
        viewHolder: ViewHolder,
        position: Int
    ) {
        val year = getYearForPosition(position)
        viewHolder.textView.text = year.toString()
        val styles = materialCalendar.calendarStyle
        val calendar = todayCalendar
        var style = if (calendar.year == year) styles.todayYear else styles.year

        for (day in materialCalendar.dateSelector!!.selectedDays) {
            calendar.timeInMillis = day
            if (calendar.year == year) style = styles.selectedYear
        }

        style.styleItem(viewHolder.textView)

        setThemeTextColor(viewHolder.textView, viewHolder.textView.context)

        viewHolder.textView.setOnClickListener(createYearClickListener(year))
    }

    private fun createYearClickListener(year: Int): View.OnClickListener {
        return View.OnClickListener {
            val moveTo: Month = Month.create(year, materialCalendar.currentMonth.month)
            materialCalendar.currentMonth = moveTo
            materialCalendar.setSelector(MaterialCalendar.CalendarSelector.DAY)
        }
    }

    override fun getItemCount(): Int {
        return materialCalendar.calendarConstraints.yearSpan
    }

    fun getPositionForYear(year: Int): Int {
        return year - materialCalendar.calendarConstraints.start.year
    }

    private fun getYearForPosition(position: Int): Int {
        return materialCalendar.calendarConstraints.start.year + position
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
}