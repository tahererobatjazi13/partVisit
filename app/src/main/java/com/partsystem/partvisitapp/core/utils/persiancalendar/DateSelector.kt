package com.partsystem.partvisitapp.core.utils.persiancalendar

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.annotation.StyleRes

interface DateSelector<S> : Parcelable {

    fun getSelection(): S?

    val isSelectionComplete: Boolean

    fun setSelection(selection: S)

    fun select(selection: Long)

    val selectedDays: Collection<Long>

    val selectedRanges: Collection<Pair<Long?, Long?>>

    fun getSelectionDisplayString(context: Context): String

    @get:StringRes
    val defaultTitleResId: Int

    @StyleRes
    fun getDefaultThemeResId(context: Context): Int
}