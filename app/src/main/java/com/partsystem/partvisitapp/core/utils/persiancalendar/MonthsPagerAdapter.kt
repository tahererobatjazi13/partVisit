package com.partsystem.partvisitapp.core.utils.persiancalendar

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.partsystem.partvisitapp.R

internal class MonthsPagerAdapter(
    context: Context,
    dateSelector: DateSelector<*>?,
    calendarConstraints: CalendarConstraints,
    onDayClickListener: MaterialCalendar.OnDayClickListener
) : RecyclerView.Adapter<MonthsPagerAdapter.ViewHolder>() {
    private val calendarConstraints: CalendarConstraints
    private val dateSelector: DateSelector<*>?
    private val onDayClickListener: MaterialCalendar.OnDayClickListener
    private val itemHeight: Int

    class ViewHolder internal constructor(container: LinearLayout) :
        RecyclerView.ViewHolder(container) {
        val monthGrid: MaterialCalendarGridView = container.findViewById(R.id.month_grid)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val container = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.calendar_month_labeled, viewGroup, false) as LinearLayout

        container.layoutParams =
            RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, itemHeight)
        return ViewHolder(container)
    }

    override fun onBindViewHolder(
        viewHolder: ViewHolder,
        position: Int
    ) {
        val month = calendarConstraints.start.monthsLater(position)
        val monthGrid: MaterialCalendarGridView = viewHolder.monthGrid.findViewById(R.id.month_grid)

        if (monthGrid.adapter != null && month == monthGrid.adapter?.month)
            monthGrid.adapter?.notifyDataSetChanged()
        else {
            val monthAdapter = MonthAdapter(month, dateSelector, calendarConstraints)
            monthGrid.numColumns = month.daysInWeek
            monthGrid.setAdapter(monthAdapter)
        }

        monthGrid.onItemClickListener = OnItemClickListener { _, _, p, _ ->
            if (monthGrid.adapter!!.withinMonth(p)) {
                onDayClickListener.onDayClick(monthGrid.adapter?.getItem(p)!!)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return calendarConstraints.start.monthsLater(position).stableId
    }

    override fun getItemCount(): Int {
        return calendarConstraints.monthSpan
    }

    fun getPageTitle(position: Int): CharSequence {
        return getPageMonth(position).longName
    }

    fun getPageMonth(position: Int): Month {
        return calendarConstraints.start.monthsLater(position)
    }

    fun getPosition(month: Month): Int {
        return calendarConstraints.start.monthsUntil(month)
    }

    init {
        val firstPage = calendarConstraints.start
        val lastPage = calendarConstraints.end
        val currentPage = calendarConstraints.openAt

        require(firstPage <= currentPage) { "firstPage cannot be after currentPage" }
        require(currentPage <= lastPage) { "currentPage cannot be after lastPage" }
        val daysHeight: Int = MonthAdapter.MAXIMUM_WEEKS * MaterialCalendar.getDayHeight(context)
        itemHeight = daysHeight
        this.calendarConstraints = calendarConstraints
        this.dateSelector = dateSelector
        this.onDayClickListener = onDayClickListener
        setHasStableIds(true)
    }
}