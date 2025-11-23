package com.partsystem.partvisitapp.core.utils.persiancalendar

import android.os.Parcel
import android.os.Parcelable
import com.partsystem.partvisitapp.core.utils.persiancalendar.utils.todayCalendar

/**
 * A [CalendarConstraints.DateValidator] that enables dates from a given point forward.
 * Defaults to the current moment in device time forward using
 * [DateValidatorPointForward.now], but can be set to any point, as UTC milliseconds, using
 * [DateValidatorPointForward.from].
 */
class DateValidatorPointForward private constructor(private val point: Long) :
    CalendarConstraints.DateValidator {

    override fun isValid(date: Long): Boolean {
        return date >= point
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(point)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is DateValidatorPointForward) {
            return false
        }
        return point == other.point
    }

    override fun hashCode(): Int {
        val hashedFields = arrayOf<Any>(point)
        return hashedFields.contentHashCode()
    }

    companion object {

        /**
         * Returns a [CalendarConstraints.DateValidator] which enables days from `point`, in
         * UTC milliseconds, forward.
         */
        fun from(point: Long): DateValidatorPointForward {
            return DateValidatorPointForward(point)
        }

        /**
         * Returns a [CalendarConstraints.DateValidator] enabled from the current moment in device
         * time forward.
         */
        fun now(): DateValidatorPointForward {
            return from(todayCalendar.timeInMillis)
        }

        /** Part of [Parcelable] requirements. Do not use.  */
        @JvmField
        val CREATOR: Parcelable.Creator<DateValidatorPointForward> =
            object :
                Parcelable.Creator<DateValidatorPointForward> {
                override fun createFromParcel(source: Parcel): DateValidatorPointForward {
                    return DateValidatorPointForward(source.readLong())
                }

                override fun newArray(size: Int): Array<DateValidatorPointForward?> {
                    return arrayOfNulls(size)
                }
            }
    }

}