package com.partsystem.partvisitapp.core.utils.persiancalendar.utils

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.StyleableRes
import androidx.appcompat.content.res.AppCompatResources

fun getColorStateList(
    context: Context, attributes: TypedArray, @StyleableRes index: Int
): ColorStateList? {
    if (attributes.hasValue(index)) {
        val resourceId = attributes.getResourceId(index, 0)
        if (resourceId != 0) {
            val value =
                AppCompatResources.getColorStateList(context, resourceId)
            if (value != null) {
                return value
            }
        }
    }

    return attributes.getColorStateList(index)
}

fun resolve(context: Context, @AttrRes attributeResId: Int): TypedValue? {
    val typedValue = TypedValue()
    return if (context.theme.resolveAttribute(attributeResId, typedValue, true)) {
        typedValue
    } else null
}