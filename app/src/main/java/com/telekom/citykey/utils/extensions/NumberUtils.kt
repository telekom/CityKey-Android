package com.telekom.citykey.utils.extensions

import android.content.Context
import android.util.TypedValue

fun Int.dpToPixel(context: Context) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics).toInt()

fun Float.dpToPixel(context: Context) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics)
