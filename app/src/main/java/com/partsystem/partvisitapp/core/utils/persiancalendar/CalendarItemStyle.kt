package com.partsystem.partvisitapp.core.utils.persiancalendar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.*
import android.util.Log
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.util.Preconditions
import androidx.core.view.ViewCompat
import com.google.android.material.textview.MaterialTextView
import com.partsystem.partvisitapp.BuildConfig
import com.partsystem.partvisitapp.R
import com.partsystem.partvisitapp.core.utils.persiancalendar.utils.getColorStateList

class CalendarItemStyle private constructor(
    backgroundColor: ColorStateList?,
    textColor: ColorStateList?,
    strokeColor: ColorStateList?,
    strokeWidth: Int,
    itemShape: Int,
    cornerRadius: Float,
    insets: Rect
) {
    private val insets: Rect
    private val textColor: ColorStateList?
    private val backgroundColor: ColorStateList?
    private val strokeColor: ColorStateList?
    private val strokeWidth: Int
    private val itemShape: Int
    private val cornerRadius: Float

    fun styleItem(item: MaterialTextView) {
        val shape = GradientDrawable()
        shape.shape = if (itemShape == 0) GradientDrawable.OVAL else GradientDrawable.RECTANGLE

        val mask = GradientDrawable()
        mask.shape = if (itemShape == 0) GradientDrawable.OVAL else GradientDrawable.RECTANGLE

        shape.color = backgroundColor
        mask.color = ColorStateList.valueOf(
            ContextCompat.getColor(
                item.context,
                R.color.default_day_ripple_color
            )
        )

        shape.setStroke(strokeWidth, strokeColor)

        if (cornerRadius > 0F) {
            shape.cornerRadius = cornerRadius
            mask.cornerRadius = cornerRadius
        }

        if (textColor == null) {
            if (BuildConfig.DEBUG) {
                Log.w(
                    CalendarItemStyle::class::java.name, "Text color is null using white instead!" +
                            "\n looks like you haven't specified a color for attribute: textColor of this item"
                )
            }

            item.setTextColor(Color.WHITE)
        } else
            item.setTextColor(textColor)

        val d: Drawable
        d = RippleDrawable(
            textColor?.withAlpha(30) ?: ColorStateList.valueOf(Color.WHITE).withAlpha(30),
            shape, mask
        )

        ViewCompat.setBackground(
            item,
            InsetDrawable(d, insets.left, insets.top, insets.right, insets.bottom)
        )
    }

    val topInset: Int
        get() = insets.top

    val bottomInset: Int
        get() = insets.bottom

    companion object {
        fun create(
            context: Context, @StyleRes materialCalendarItemStyle: Int
        ): CalendarItemStyle {
            Preconditions.checkArgument(
                materialCalendarItemStyle != 0,
                "Cannot create a CalendarItemStyle with a styleResId of 0"
            )
            val styleableArray = context.obtainStyledAttributes(
                materialCalendarItemStyle,
                R.styleable.PersianMaterialCalendarItem
            )
            val insetLeft = styleableArray.getDimensionPixelOffset(
                R.styleable.PersianMaterialCalendarItem_android_insetLeft, 0
            )
            val insetTop = styleableArray.getDimensionPixelOffset(
                R.styleable.PersianMaterialCalendarItem_android_insetTop, 0
            )
            val insetRight = styleableArray.getDimensionPixelOffset(
                R.styleable.PersianMaterialCalendarItem_android_insetRight, 0
            )
            val insetBottom = styleableArray.getDimensionPixelOffset(
                R.styleable.PersianMaterialCalendarItem_android_insetBottom, 0
            )
            val insets =
                Rect(insetLeft, insetTop, insetRight, insetBottom)
            val backgroundColor =
                getColorStateList(
                    context, styleableArray, R.styleable.PersianMaterialCalendarItem_itemFillColor
                )
            val textColor =
                getColorStateList(
                    context, styleableArray, R.styleable.PersianMaterialCalendarItem_itemTextColor
                )
            val strokeColor =
                getColorStateList(
                    context, styleableArray, R.styleable.PersianMaterialCalendarItem_itemStrokeColor
                )
            val strokeWidth = styleableArray.getDimensionPixelSize(
                R.styleable.PersianMaterialCalendarItem_itemStrokeWidth,
                0
            )
            val itemShape =
                styleableArray.getInt(R.styleable.PersianMaterialCalendarItem_itemShape, 0)

            val cornerRadius = styleableArray.getFloat(
                R.styleable.PersianMaterialCalendarItem_itemShapeCornerRadius,
                0F
            )

            styleableArray.recycle()
            return CalendarItemStyle(
                backgroundColor,
                textColor,
                strokeColor,
                strokeWidth,
                itemShape,
                cornerRadius,
                insets
            )
        }
    }

    init {
        Preconditions.checkArgumentNonnegative(insets.left)
        Preconditions.checkArgumentNonnegative(insets.top)
        Preconditions.checkArgumentNonnegative(insets.right)
        Preconditions.checkArgumentNonnegative(insets.bottom)
        this.insets = insets
        this.textColor = textColor
        this.backgroundColor = backgroundColor
        this.strokeColor = strokeColor
        this.strokeWidth = strokeWidth
        this.itemShape = itemShape
        this.cornerRadius = cornerRadius
    }
}