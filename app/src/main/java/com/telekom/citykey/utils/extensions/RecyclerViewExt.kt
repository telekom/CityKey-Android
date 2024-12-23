package com.telekom.citykey.utils.extensions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun ViewGroup.inflateChild(@LayoutRes resource: Int): View =
    LayoutInflater.from(context)
        .inflate(resource, this, false)
