package com.partsystem.partvisitapp.core.utils.extensions

import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import ir.huri.jcal.JalaliCalendar
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.active() {
    isEnabled = true
}

fun View.deactive() {
    isEnabled = false
}

fun View.setSafeOnClickListener(onClick: (View) -> Unit): Disposable {
    return this.clicks().observeOn(AndroidSchedulers.mainThread())
        .throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe {
            onClick(this)
        }
}

fun Double.clean(): String {
    return if (this % 1 == 0.0) {
        this.toInt().toString()
    } else {
        this.toString()
    }
}

fun Float.clean(): String {
    return if (this % 1 == 0f) {
        this.toInt().toString()
    } else {
        this.toString()
    }
}

fun String.toEnglishDigits(): String {
    val arabic = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val persian = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    var output = this
    for (i in 0..9) {
        output = output
            .replace(arabic[i], ('0' + i))
            .replace(persian[i], ('0' + i))
    }
    return output
}

@SuppressLint("DefaultLocale")
fun getTodayPersianDate(): String {
    val jalaliDate = JalaliCalendar()
    return String.format(
        "%d/%02d/%02d",
        jalaliDate.year,
        jalaliDate.month,
        jalaliDate.day
    )
}

fun getTodayGregorian(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date())
}

fun getCurrentTime(): String {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return formatter.format(Date())
}
/*
git add .
git commit -m "change item product"
git push -u origin master
git push
*/
