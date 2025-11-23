package com.partsystem.partvisitapp.core.utils.persiancalendar

import android.content.Context
import android.os.Parcelable
import android.widget.AdapterView
import androidx.annotation.StringRes
import androidx.annotation.StyleRes


interface DateSelector<S> : Parcelable {

    /** Returns the current selection.  */
    fun getSelection(): S?

    /** Returns true if the current selection is acceptable.  */
    val isSelectionComplete: Boolean

    /**
     * Sets the current selection to `selection`.
     *
     * @throws IllegalArgumentException If `selection` creates an invalid state.
     */
    fun setSelection(selection: S)

    /**
     * Allows this selection handler to respond to clicks within the [AdapterView].
     *
     * @param selection The selected day represented as time in UTC milliseconds.
     */
    fun select(selection: Long)

    /**
     * Returns a list of longs whose time value represents days that should be marked selected.
     *
     *
     * Uses [R.styleable.PersianMaterialCalendar_daySelectedStyle] for styling.
     */
    val selectedDays: Collection<Long>

    /**
     * Returns a list of ranges whose time values represent ranges that should be filled.
     *
     *
     * Uses [R.styleable.PersianMaterialCalendar_rangeFillColor] for styling.
     */
    val selectedRanges: Collection<Pair<Long?, Long?>>

    fun getSelectionDisplayString(context: Context): String

    @get:StringRes
    val defaultTitleResId: Int

    @StyleRes
    fun getDefaultThemeResId(context: Context): Int
}