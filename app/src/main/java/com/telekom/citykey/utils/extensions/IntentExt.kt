package com.telekom.citykey.utils.extensions

import android.content.Intent
import android.os.Bundle
import java.util.*


fun Intent?.intentToString(): String? {
    if (this == null) {
        return null
    }

    var out = "\n" + this.toString()
    val extras = this.extras
    if (extras != null) {
        extras.size()
        out += "\n${bundleToString(extras)}"
    }

    if (this.action != null) out += "\nAction = ${this.action}"

    if (this.type != null) out += "\nType = ${this.type}"

    if (this.data != null) out += "\nData = ${this.data}"

    if (this.getPackage() != null) out += "\nPackage = ${this.getPackage()}"

    if (this.dataString != null) out += "\nDataString = ${this.dataString}"

    return out
}

fun bundleToString(bundle: Bundle?): String {
    val out = StringBuilder("Bundle[")
    if (bundle == null) {
        out.append("null")
    } else {
        var first = true
        for (key in bundle.keySet()) {
            if (!first) {
                out.append(", ")
            }
            out.append(key).append('=')
            val value = bundle[key]
            if (value is IntArray) {
                out.append(Arrays.toString(value as IntArray?))
            } else if (value is ByteArray) {
                out.append(Arrays.toString(value as ByteArray?))
            } else if (value is BooleanArray) {
                out.append(Arrays.toString(value as BooleanArray?))
            } else if (value is ShortArray) {
                out.append(Arrays.toString(value as ShortArray?))
            } else if (value is LongArray) {
                out.append(Arrays.toString(value as LongArray?))
            } else if (value is FloatArray) {
                out.append(Arrays.toString(value as FloatArray?))
            } else if (value is DoubleArray) {
                out.append(Arrays.toString(value as DoubleArray?))
            } else if (value is Array<*>) {
                out.append(Arrays.toString(value as Array<*>?))
            } else if (value is Bundle) {
                out.append(bundleToString(value as Bundle?))
            } else {
                out.append(value)
            }
            first = false
        }
    }
    out.append("]")
    return out.toString()
}
