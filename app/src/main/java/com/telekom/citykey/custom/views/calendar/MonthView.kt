/**
 * Copyright (C) 2025 Deutsche Telekom AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

package com.telekom.citykey.custom.views.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.*
import androidx.annotation.ColorInt
import com.telekom.citykey.R
import com.telekom.citykey.utils.ColorUtils
import com.telekom.citykey.utils.extensions.between
import com.telekom.citykey.utils.extensions.isSameDay
import com.telekom.citykey.utils.extensions.isToday
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

class MonthView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        private const val CORNER_RADIUS = 20f
    }

    private val textBounds = Rect()

    private var selectionStartDate: Date? = null
    private var selectionEndDate: Date? = null
    private var singleSelection: Calendar? = null
    private var dateClickListener: ((Date) -> Unit)? = null
    private var colors: CalendarColors = CalendarColors.fromPrimaryColor(0)

    private var mWidth = 0
    private var monthSize = 0f
    private var horizontalSpacing = 0f
    private var dateSize = right.toFloat()
    private var rowHeight = 0f
    private var year = 2019
    private var spaceBetweenRows = 0f
    private var dateTextSize = 0f
    private var month = 7
    private var thisMonth = Calendar.getInstance().apply {
        clear()
        minimalDaysInFirstWeek = 1
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
    }

    private val usualDayPaint: Paint by lazy {
        Paint().apply {
            color = context.getColor(R.color.onSurface)
            textSize = dateTextSize
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            isAntiAlias = true
        }
    }

    private val weekendDayPaint: Paint by lazy {
        Paint().apply {
            color = colors.weekends
            textSize = dateTextSize
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            isAntiAlias = true
        }
    }

    private val pastDayPaint: Paint by lazy {
        Paint().apply {
            color = colors.past
            textSize = dateTextSize
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            isAntiAlias = true
        }
    }

    private val monthPaint: Paint by lazy {
        Paint().apply {
            color = context.getColor(R.color.onSurface)
            textSize = monthSize
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            isAntiAlias = true
        }
    }

    private var currentWeek = 1

    init {
        val a = context.theme.obtainStyledAttributes(
            attrs, R.styleable.MonthView, 0, 0
        )
        try {
            monthSize = a.getDimension(R.styleable.MonthView_monthSize, 10f)
            horizontalSpacing = a.getDimension(R.styleable.MonthView_horizontalSpacing, 0f)
            rowHeight = a.getDimension(R.styleable.MonthView_rowHeight, 0f)
            dateTextSize = a.getDimension(R.styleable.MonthView_dateTextSize, 0f)
            spaceBetweenRows = a.getDimension(R.styleable.MonthView_spaceBetweenRows, 0f)
        } finally {
            a.recycle()
        }

        dateSize = (Resources.getSystem().displayMetrics.widthPixels - horizontalSpacing * 2) / 7
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val weeksInMonth = getNumberOfWeeks()

        val width: Int
        val height: Int
        val desiredWidth = mWidth
        val desiredHeight =
            (monthSize + rowHeight * weeksInMonth + spaceBetweenRows * weeksInMonth).toInt()

        width = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(desiredWidth, widthSize)
            else -> desiredWidth
        }

        height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        currentWeek = 1
        drawMonth(canvas)
        drawDays(canvas)
    }

    private fun drawMonth(canvas: Canvas) {
        val monthLabel = "${thisMonth.getDisplayName(
            Calendar.MONTH,
            Calendar.LONG,
            Locale.getDefault()
        )} $year"

        canvas.drawText(monthLabel, horizontalSpacing, monthSize, monthPaint)
    }

    private fun drawDays(canvas: Canvas) {
        for (day: Int in 1..thisMonth.getActualMaximum(Calendar.DATE)) {
            thisMonth.set(Calendar.DAY_OF_MONTH, day)

            when (thisMonth.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> drawMonday(canvas, day)
                Calendar.TUESDAY -> drawMidWeekDay(canvas, day, 1)
                Calendar.WEDNESDAY -> drawMidWeekDay(canvas, day, 2)
                Calendar.THURSDAY -> drawMidWeekDay(canvas, day, 3)
                Calendar.FRIDAY -> drawMidWeekDay(canvas, day, 4)
                Calendar.SATURDAY -> drawMidWeekDay(canvas, day, 5)
                Calendar.SUNDAY -> {
                    drawSunday(canvas, day)
                    currentWeek++
                }
            }
        }
    }

    private fun drawMonday(canvas: Canvas, day: Int) {
        val text = getStringDay(day)
        usualDayPaint.getTextBounds(text, 0, text.length, textBounds)

        val paint = when {
            thisMonth.time.between(selectionStartDate, selectionEndDate) -> {
                val rect = getRect(0)

                drawLeftRowFillerIfNecessary(canvas, rect)
                drawRightRowFillerIfNecessary(canvas, rect)
                canvas.drawRect(rect, colors.getPaint(colors.primary))

                Paint(usualDayPaint).apply { color = Color.WHITE }
            }
            thisMonth.isSameDay(singleSelection) -> {
                drawRoundedRect(canvas, getRect(0), colors.darkened)

                Paint(usualDayPaint).apply { color = Color.WHITE }
            }
            thisMonth.isSameDay(selectionStartDate) -> {
                val rectRound = getRect(0)
                val rectSquare = RectF(rectRound).apply { left += dateSize / 2 }

                drawRightRowFillerIfNecessary(canvas, rectRound)
                drawRoundedRect(canvas, rectRound, colors.darkened)
                canvas.drawRect(rectSquare, colors.getPaint(colors.darkened))

                Paint(usualDayPaint).apply { color = Color.WHITE }
            }
            thisMonth.isSameDay(selectionEndDate) -> {
                val rectRound = getRect(0)
                val rectSquare = RectF(rectRound).apply { right -= dateSize / 2 }

                drawLeftRowFillerIfNecessary(canvas, rectRound)
                drawRoundedRect(canvas, rectRound, colors.darkened)
                canvas.drawRect(rectSquare, colors.getPaint(colors.darkened))

                Paint(usualDayPaint).apply { color = Color.WHITE }
            }
            thisMonth.isToday() -> {
                drawRoundedRect(canvas, getRect(0), colors.lightened)

                usualDayPaint
            }
            Date().after(thisMonth.time) -> pastDayPaint
            else -> usualDayPaint
        }

        canvas.drawText(
            text,
            horizontalSpacing + dateSize / 2 - textBounds.exactCenterX(),
            rowHeight * (currentWeek - 1) + rowHeight / 2 + monthSize +
                textBounds.exactCenterY() + spaceBetweenRows * (2 + currentWeek),
            paint
        )
    }

    private fun drawSunday(canvas: Canvas, day: Int) {
        val text = getStringDay(day)
        usualDayPaint.getTextBounds(text, 0, text.length, textBounds)

        val paint = when {
            thisMonth.time.between(selectionStartDate, selectionEndDate) -> {
                val rect = getRect(6)

                drawLeftRowFillerIfNecessary(canvas, rect)
                drawRightRowFillerIfNecessary(canvas, rect)
                canvas.drawRect(rect, colors.getPaint(colors.primary))

                Paint(usualDayPaint).apply { color = Color.WHITE }
            }
            thisMonth.isSameDay(singleSelection) -> {
                drawRoundedRect(canvas, getRect(6), colors.darkened)
                Paint(usualDayPaint).apply { color = Color.WHITE }
            }
            thisMonth.isSameDay(selectionStartDate) -> {
                val rectRound = getRect(6)
                val rectSquare = RectF(rectRound).apply { left += dateSize / 2 }

                drawRightRowFillerIfNecessary(canvas, rectRound)
                drawRoundedRect(canvas, rectRound, colors.darkened)
                canvas.drawRect(rectSquare, colors.getPaint(colors.darkened))

                Paint(usualDayPaint).apply { color = Color.WHITE }
            }

            thisMonth.isSameDay(selectionEndDate) -> {
                val rectRound = getRect(6)
                val rectSquare = RectF(rectRound).apply { right -= dateSize / 2 }

                drawLeftRowFillerIfNecessary(canvas, rectRound)
                drawRoundedRect(canvas, rectRound, colors.darkened)
                canvas.drawRect(rectSquare, colors.getPaint(colors.darkened))

                Paint(usualDayPaint).apply { color = Color.WHITE }
            }
            thisMonth.isToday() -> {
                drawRoundedRect(canvas, getRect(6), colors.lightened)

                usualDayPaint
            }
            Date().after(thisMonth.time) -> pastDayPaint
            else -> weekendDayPaint
        }

        canvas.drawText(
            text,
            horizontalSpacing + dateSize * 6 + dateSize / 2 - textBounds.exactCenterX(),
            rowHeight * (currentWeek - 1) + rowHeight / 2 + monthSize +
                textBounds.exactCenterY() + spaceBetweenRows * (2 + currentWeek),
            paint
        )
    }

    private fun drawMidWeekDay(canvas: Canvas, day: Int, weekPosition: Int) {
        val text = getStringDay(day)
        usualDayPaint.getTextBounds(text, 0, text.length, textBounds)

        val paint = when {
            thisMonth.time.between(selectionStartDate, selectionEndDate) -> {
                val rect = getRect(weekPosition)

                drawLeftRowFillerIfNecessary(canvas, rect)
                drawRightRowFillerIfNecessary(canvas, rect)
                canvas.drawRect(rect, colors.getPaint(colors.primary))

                Paint(usualDayPaint).apply { color = Color.WHITE }
            }
            thisMonth.isSameDay(singleSelection) -> {
                drawRoundedRect(canvas, getRect(weekPosition), colors.darkened)

                Paint(usualDayPaint).apply { color = Color.WHITE }
            }
            thisMonth.isSameDay(selectionStartDate) -> {
                val rectRound = getRect(weekPosition)
                val rectSquare = RectF(rectRound).apply { left += dateSize / 2 }

                drawRightRowFillerIfNecessary(canvas, rectRound)

                drawRoundedRect(canvas, rectRound, colors.darkened)
                canvas.drawRect(rectSquare, colors.getPaint(colors.darkened))

                Paint(usualDayPaint).apply { color = Color.WHITE }
            }
            thisMonth.isSameDay(selectionEndDate) -> {
                val rectRound = getRect(weekPosition)
                val rectSquare = RectF(rectRound).apply { right -= dateSize / 2 }

                drawLeftRowFillerIfNecessary(canvas, rectRound)

                canvas.drawRoundRect(
                    rectRound,
                    CORNER_RADIUS,
                    CORNER_RADIUS,
                    colors.getPaint(colors.darkened)
                )
                canvas.drawRect(rectSquare, colors.getPaint(colors.darkened))

                Paint(usualDayPaint).apply { color = Color.WHITE }
            }
            thisMonth.isToday() -> {
                drawRoundedRect(canvas, getRect(weekPosition), colors.lightened)
                usualDayPaint
            }
            Date().after(thisMonth.time) -> pastDayPaint
            weekPosition == 5 -> weekendDayPaint
            else -> usualDayPaint
        }

        canvas.drawText(
            getStringDay(day),
            horizontalSpacing + dateSize * weekPosition + dateSize / 2 - (
                paint.measureText(
                    getStringDay(day)
                ) / 2
                ),
            rowHeight * (currentWeek - 1) + rowHeight / 2 + monthSize +
                textBounds.exactCenterY() + spaceBetweenRows * (currentWeek + 2),
            paint
        )
    }

    private fun drawRoundedRect(canvas: Canvas, rectF: RectF, @ColorInt color: Int) {
        canvas.drawRoundRect(
            rectF,
            CORNER_RADIUS,
            CORNER_RADIUS,
            Paint().apply { this.color = color }
        )
    }

    private fun drawLeftRowFillerIfNecessary(canvas: Canvas, rectF: RectF) {
        val calendar = thisMonth
        if (calendar.get(Calendar.DAY_OF_MONTH) == 1 ||
            calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
        ) {
            val rect = RectF(rectF).apply { left = 0f; right -= 15 }
            canvas.drawRect(rect, colors.getPaint(colors.primary))
        }
    }

    private fun drawRightRowFillerIfNecessary(canvas: Canvas, rectF: RectF) {
        val calendar = thisMonth
        if (calendar.get(Calendar.DAY_OF_MONTH) == calendar.getActualMaximum(Calendar.DATE) ||
            calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
        ) {
            val rect = RectF(rectF).apply { left += 15; right = this@MonthView.right.toFloat() }
            canvas.drawRect(rect, colors.getPaint(colors.primary))
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            getDayFromLocation(event.x, event.y)
        }
        return true
    }

    private fun getDayFromLocation(x: Float, y: Float) {

        if (y < monthSize - spaceBetweenRows) return
        if (x < horizontalSpacing || x > right - horizontalSpacing) return

        val weekRow = (y / (rowHeight + spaceBetweenRows)).roundToInt()
        val dayColumn = (x - horizontalSpacing).div(dateSize).toInt()
        val dayInWeek = getDayOfWeekFromColumn(dayColumn)

        val cal = Calendar.getInstance(Locale.GERMANY).apply {
            clear()
            minimalDaysInFirstWeek = 1
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.WEEK_OF_MONTH, weekRow)
            set(Calendar.DAY_OF_WEEK, dayInWeek)
        }

        if (cal.get(Calendar.MONTH) == thisMonth.get(Calendar.MONTH) &&
            !cal.before(Calendar.getInstance()) || cal.isToday()
        )
            dateClickListener?.invoke(cal.time)
    }

    private fun getDayOfWeekFromColumn(column: Int) = when (column) {
        0 -> Calendar.MONDAY
        1 -> Calendar.TUESDAY
        2 -> Calendar.WEDNESDAY
        3 -> Calendar.THURSDAY
        4 -> Calendar.FRIDAY
        5 -> Calendar.SATURDAY
        else -> Calendar.SUNDAY
    }

    private fun getStringDay(day: Int) = if (day < 10) "0$day" else day.toString()

    fun setDate(year: Int, month: Int) {
        this.year = year
        this.month = month
        thisMonth = Calendar.getInstance().apply {
            clear()
            minimalDaysInFirstWeek = 1
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
        }
        requestLayout()
    }

    fun onDateSelected(listener: ((Date) -> Unit)) {
        dateClickListener = listener
    }

    fun setSelection(selection: DateSelection) {
        selectionEndDate = null
        selectionStartDate = null
        singleSelection = null

        if (selection.single) {
            singleSelection = Calendar.getInstance().apply { time = selection.start!! }
        } else {
            selectionEndDate = selection.end
            selectionStartDate = selection.start
        }
    }

    fun setPrimaryColor(@ColorInt color: Int) {
        colors = CalendarColors.fromPrimaryColor(color)
    }

    private fun calcLeft(dayPosition: Int) = horizontalSpacing + dateSize * dayPosition
    private fun calcTop() = calcBottom() - rowHeight
    private fun calcRight(dayPosition: Int) = calcLeft(dayPosition) + dateSize
    private fun calcBottom() = rowHeight * currentWeek + monthSize + spaceBetweenRows * currentWeek
    private fun getRect(position: Int) =
        RectF(calcLeft(position), calcTop(), calcRight(position), calcBottom())

    private data class CalendarColors(
        @ColorInt val primary: Int,
        @ColorInt val darkened: Int,
        @ColorInt val lightened: Int,
        @ColorInt val weekends: Int,
        @ColorInt val past: Int
    ) {
        companion object {
            fun fromPrimaryColor(@ColorInt color: Int): CalendarColors {
                val darkened = ColorUtils.darken(color, 0.85f)
                val lightened = ColorUtils.setAlpha(color, 15)
                val past = 0xFF_7E_7E_7E.toInt()
                val weekends = 0xFF_C3_C3_C3.toInt()

                return CalendarColors(color, darkened, lightened, past, weekends)
            }
        }

        fun getPaint(@ColorInt color: Int) = Paint().apply { this.color = color }
    }

    private fun getNumberOfWeeks(): Int {
        val c = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 2)
        }
        var numOfWeeksInMonth = 1
        while (c.get(Calendar.MONTH) == month) {
            if (c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
                numOfWeeksInMonth++
            }
            c.add(Calendar.DAY_OF_MONTH, 1)
        }
        return numOfWeeksInMonth
    }
}
