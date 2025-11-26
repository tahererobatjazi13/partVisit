package com.partsystem.partvisitapp.core.utils.extensions

import android.view.View
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
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
