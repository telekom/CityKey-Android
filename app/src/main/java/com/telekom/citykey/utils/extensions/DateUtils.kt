package com.telekom.citykey.utils.extensions

import android.text.format.DateFormat
import com.telekom.citykey.utils.DateUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Date.toDateString(format: String = DateUtil.FORMAT_DD_MMMM_YYYY): String =
    SimpleDateFormat(format).format(this)

fun Date.toApiFormat(): String =
    DateFormat.format("yyyy-MM-dd", this).toString()

fun Date.year(): String = DateFormat.format("yyyy", this).toString()

fun Date.getHoursAndMins(): String =
    DateFormat.format("HH:mm", this).toString()

fun Calendar.getShortWeekDay(): String =
    getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) ?: ""

fun Calendar.getLongWeekDay(): String =
    getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) ?: ""

fun Calendar.getShortMonthName(): String =
    getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) ?: ""

val Calendar.longMonthName: String get() = getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: ""

fun Date?.isSameDayAs(anotherDate: Date?): Boolean {
    if (anotherDate == null) return false
    val firstDate = Calendar.getInstance().apply { time = this@isSameDayAs!! }
    val secondDate = Calendar.getInstance().apply { time = anotherDate }

    return firstDate.get(Calendar.YEAR) == secondDate.get(Calendar.YEAR) &&
            firstDate.get(Calendar.DAY_OF_YEAR) == secondDate.get(Calendar.DAY_OF_YEAR)
}

fun Calendar.isSameDay(other: Calendar?): Boolean {
    other?.let {
        return get(Calendar.YEAR) == it.get(Calendar.YEAR) &&
                get(Calendar.DAY_OF_YEAR) == it.get(Calendar.DAY_OF_YEAR)
    }
    return false
}

fun Calendar.isSameDay(other: Date?): Boolean {
    other?.let {
        val calendar = Calendar.getInstance().apply { time = it }
        return get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
    }
    return false
}

fun Calendar.isToday(): Boolean =
    Calendar.getInstance().get(Calendar.DAY_OF_YEAR) == get(Calendar.DAY_OF_YEAR) &&
            Calendar.getInstance().get(Calendar.YEAR) == get(Calendar.YEAR)

fun Date.between(rangeStart: Date?, rangeEnd: Date?): Boolean {
    rangeStart?.let { start ->
        rangeEnd?.let { end ->
            return before(end) && after(start)
        }
        return false
    }
    return false
}

fun Date?.toCalendar(): Calendar = Calendar.getInstance().apply { this@toCalendar?.let { time = it } }

val Date.isToday: Boolean
    get() =
        Calendar.getInstance().apply { clear(); time = this@isToday }.isToday()

val Date.isInPast: Boolean
    get() = this.before(Calendar.getInstance().time)

val Date.isInFuture: Boolean
    get() = this.after(Calendar.getInstance().time)

fun Calendar.isInCurrentMonth(): Boolean =
    Calendar.getInstance().get(Calendar.MONTH) == get(Calendar.MONTH)

fun Date.isTomorrow(): Boolean =
    Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.isSameDay(this@isTomorrow)

fun Date.isDayAfterTomorrow(): Boolean =
    Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 2) }.isSameDay(this@isDayAfterTomorrow)

fun Calendar.isFutureDay(): Boolean = when {
    (Calendar.getInstance().get(Calendar.YEAR) > get(Calendar.YEAR)) -> {
        true
    }

    (Calendar.getInstance().get(Calendar.YEAR) == get(Calendar.YEAR)) -> {
        Calendar.getInstance().get(Calendar.DAY_OF_YEAR) > get(Calendar.DAY_OF_YEAR)
    }

    else -> {
        false
    }
}
