package com.partsystem.partvisitapp.core.utils.contract

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


typealias ClickListener = () -> Unit
typealias TextChange = (text: CharSequence) -> Unit
typealias ClickItemListener = (item: Any) -> Unit
typealias ClickItemTypeListener = (item: Any,type:String) -> Unit
typealias clickItemPositionListener = (item: Any, position: Int) -> Unit
typealias ThrowableListener = (throwable: Throwable) -> Unit
typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T
