package com.partsystem.partvisitapp.core.utils.persiancalendar

import androidx.fragment.app.Fragment
import java.util.*

abstract class PickerFragment<S> : Fragment() {
    protected val onSelectionChangedListeners =
        LinkedHashSet<OnSelectionChangedListener<S>>()

    abstract val dateSelector: DateSelector<S>?

    /** Adds a listener for selection changes. */
    fun addOnSelectionChangedListener(listener: OnSelectionChangedListener<S>): Boolean {
        return onSelectionChangedListeners.add(listener)
    }

    /** Removes a listener for selection changes. */
    fun removeOnSelectionChangedListener(listener: OnSelectionChangedListener<S>?): Boolean {
        return onSelectionChangedListeners.remove(listener)
    }

    /** Removes all listeners for selection changes. */
    fun clearOnSelectionChangedListeners() {
        onSelectionChangedListeners.clear()
    }
}