package com.telekom.citykey.custom.views.wastecalendar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import com.telekom.citykey.R
import com.telekom.citykey.models.waste_calendar.WasteCalendarPickups
import com.telekom.citykey.utils.ColorUtils
import com.telekom.citykey.utils.extensions.isSameDay
import com.telekom.citykey.utils.extensions.isToday
import com.telekom.citykey.utils.extensions.toCalendar
import com.telekom.citykey.utils.isDarkMode
import timber.log.Timber
import java.util.*
import kotlin.math.min
import kotlin.math.roundToInt

class WCMonthView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    companion object {
        private const val CORNER_RADIUS = 20f
    }

    private val textBounds = Rect()

    private var dateClickListener: ((Date) -> Unit)? = null
    private var colors: CalendarColors = CalendarColors.fromPrimaryColor(0xFF000000.toInt())
    private val pickUps = mutableListOf<WasteCalendarPickups>()
    private var selectedDate: Calendar? = null

    private var mWidth = 0
    private var horizontalSpacing = 0f
    private var dateSize = right.toFloat()
    private var rowHeight = 0f
    private var year = 2021
    private var spaceBetweenRows = 0f
    private var dateTextSize = 0f
    private var month = Calendar.DECEMBER
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

    private val prevNextMonthsPaint: Paint by lazy {
        Paint().apply {
            color = Color.WHITE
            textSize = dateTextSize
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            isAntiAlias = true
        }
    }

    private val unavailableDayPaint: Paint by lazy {
        Paint().apply {
            color = colors.past
            textSize = dateTextSize
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            isAntiAlias = true
        }
    }

    private var currentWeek = 1

    init {
        val a = context.theme.obtainStyledAttributes(
            attrs, R.styleable.MonthView, 0, 0
        )
        try {
            horizontalSpacing = a.getDimension(R.styleable.MonthView_horizontalSpacing, 0f)
            rowHeight = a.getDimension(R.styleable.MonthView_rowHeight, 0f)
            dateTextSize = a.getDimension(R.styleable.MonthView_dateTextSize, 0f)
            spaceBetweenRows = a.getDimension(R.styleable.MonthView_spaceBetweenRows, 0f)
        } finally {
            a.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val weeksInMonth = getNumberOfWeeks()

        val width: Int
        val height: Int
        val desiredWidth = mWidth
        val desiredHeight =
            (rowHeight * weeksInMonth + spaceBetweenRows * weeksInMonth).toInt()

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

        dateSize = (width - horizontalSpacing * 2) / 7
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        currentWeek = 1
        drawDays(canvas)
    }

    private fun drawDays(canvas: Canvas) {
        thisMonth.set(Calendar.DAY_OF_MONTH, 1)
        if (thisMonth.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            thisMonth.add(Calendar.MONTH, -1)
            thisMonth.set(Calendar.DAY_OF_MONTH, thisMonth.getActualMaximum(Calendar.DATE))

            loop@ while (true) {
                val day = thisMonth.get(Calendar.DAY_OF_MONTH)
                when (thisMonth.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> {
                        drawDifferentMonthDay(canvas, day, 0); break@loop
                    }

                    Calendar.TUESDAY -> drawDifferentMonthDay(canvas, day, 1)
                    Calendar.WEDNESDAY -> drawDifferentMonthDay(canvas, day, 2)
                    Calendar.THURSDAY -> drawDifferentMonthDay(canvas, day, 3)
                    Calendar.FRIDAY -> drawDifferentMonthDay(canvas, day, 4)
                    Calendar.SATURDAY -> drawDifferentMonthDay(canvas, day, 5)
                    Calendar.SUNDAY -> drawDifferentMonthDay(canvas, day, 6)
                }
                thisMonth.add(Calendar.DAY_OF_MONTH, -1)
            }
            thisMonth.add(Calendar.MONTH, 1)
        }

        for (day: Int in 1..thisMonth.getActualMaximum(Calendar.DATE)) {
            thisMonth.set(Calendar.DAY_OF_MONTH, day)

            when (thisMonth.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> drawWeekDay(canvas, day, 0)
                Calendar.TUESDAY -> drawWeekDay(canvas, day, 1)
                Calendar.WEDNESDAY -> drawWeekDay(canvas, day, 2)
                Calendar.THURSDAY -> drawWeekDay(canvas, day, 3)
                Calendar.FRIDAY -> drawWeekDay(canvas, day, 4)
                Calendar.SATURDAY -> drawWeekDay(canvas, day, 5)
                Calendar.SUNDAY -> {
                    drawWeekDay(canvas, day, 6); currentWeek++
                }
            }
        }

        if (thisMonth.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            thisMonth.add(Calendar.DAY_OF_YEAR, 1)

            loop@ while (true) {
                val day = thisMonth.get(Calendar.DAY_OF_MONTH)
                when (thisMonth.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> drawDifferentMonthDay(canvas, day, 0)
                    Calendar.TUESDAY -> drawDifferentMonthDay(canvas, day, 1)
                    Calendar.WEDNESDAY -> drawDifferentMonthDay(canvas, day, 2)
                    Calendar.THURSDAY -> drawDifferentMonthDay(canvas, day, 3)
                    Calendar.FRIDAY -> drawDifferentMonthDay(canvas, day, 4)
                    Calendar.SATURDAY -> drawDifferentMonthDay(canvas, day, 5)
                    Calendar.SUNDAY -> {
                        drawDifferentMonthDay(canvas, day, 6); break@loop
                    }
                }
                thisMonth.add(Calendar.DAY_OF_MONTH, 1)
            }
            thisMonth.add(Calendar.MONTH, -1)
        }
    }

    private fun drawWeekDay(canvas: Canvas, day: Int, weekPosition: Int) {
        val text = getStringDay(day)
        usualDayPaint.getTextBounds(text, 0, text.length, textBounds)

        val paint = when {
            thisMonth.isSameDay(selectedDate) -> {
                drawRoundedRect(canvas, getRect(weekPosition), colors.primary)
                drawCircles(canvas, weekPosition)
                Paint(usualDayPaint).apply { color = Color.WHITE }
            }

            thisMonth.isToday() -> {
                drawRoundedRect(canvas, getRect(weekPosition), colors.lightened)
                drawCircles(canvas, weekPosition)
                usualDayPaint
            }

            Date().after(thisMonth.time) -> {
                drawCircles(canvas, weekPosition)
                unavailableDayPaint
            }

            weekPosition == 5 || weekPosition == 6 -> {
                drawCircles(canvas, weekPosition)
                weekendDayPaint
            }

            else -> {
                drawCircles(canvas, weekPosition)
                usualDayPaint
            }
        }

        canvas.drawText(
            getStringDay(day),
            horizontalSpacing + dateSize * weekPosition + dateSize / 2 - (paint.measureText(getStringDay(day)) / 2),
            rowHeight * (currentWeek - 1) + rowHeight / 2 + textBounds.exactCenterY() + spaceBetweenRows * (currentWeek + 1),
            paint
        )
    }

    private fun drawDifferentMonthDay(canvas: Canvas, day: Int, weekPosition: Int) {
        val text = getStringDay(day)
        usualDayPaint.getTextBounds(text, 0, text.length, textBounds)

        canvas.drawText(
            "",
            horizontalSpacing + dateSize * weekPosition + dateSize / 2 - (unavailableDayPaint.measureText(
                getStringDay(
                    day
                )
            ) / 2),
            rowHeight * (currentWeek - 1) + rowHeight / 2 + textBounds.exactCenterY() + spaceBetweenRows * (currentWeek + 1),
            prevNextMonthsPaint
        )
    }

    private fun drawRoundedRect(canvas: Canvas, rectF: RectF, @ColorInt color: Int) {
        canvas.drawRoundRect(
            rectF,
            CORNER_RADIUS,
            CORNER_RADIUS,
            Paint().apply {
                this.color = color
                isAntiAlias = true
            }
        )
    }

    private fun drawCircles(canvas: Canvas, weekPosition: Int) {
        val wasteList =
            pickUps.find { it.date.toCalendar().get(Calendar.DAY_OF_MONTH) == thisMonth.get(Calendar.DAY_OF_MONTH) }
                ?.wasteTypeList?.distinctBy { it.wasteIconColorInt } ?: return

        val dots = wasteList.size

        val spaceNeeded = dots * 16 + dots * 5 - 21
        val middle = horizontalSpacing + dateSize * weekPosition + dateSize / 2

        wasteList.forEachIndexed { i, wasteItem ->
            val dotPosition: Float = i * 19 + i * 5.toFloat()
            canvas.drawCircle(
                middle - spaceNeeded / 2 + dotPosition,
                rowHeight * (currentWeek - 1) + rowHeight * 0.55F + spaceBetweenRows * (currentWeek + 1),
                10F,
                Paint().apply {
                    color = if (resources.isDarkMode)
                        ColorUtils.invertIfDark(wasteItem.wasteIconColorInt)
                    else
                        wasteItem.wasteIconColorInt
                    isAntiAlias = true
                }
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        Timber.i(event.action.toString())
        when (event.action) {
            MotionEvent.ACTION_UP -> findDateAndInformListener(event.x, event.y)
            MotionEvent.ACTION_DOWN -> giveTouchFeedBack(event.x, event.y)
            MotionEvent.ACTION_CANCEL -> removeTouchFeedBack()
            else -> return false
        }
        return true
    }

    private fun giveTouchFeedBack(x: Float, y: Float) {
        if (x < horizontalSpacing || x > right - horizontalSpacing) return

        val weekRow = (y / (rowHeight)).roundToInt()
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

        if (cal.get(Calendar.MONTH) == thisMonth.get(Calendar.MONTH)) {
            selectedDate = cal
            invalidate()
        }
    }

    private fun removeTouchFeedBack() {
        selectedDate = null
        invalidate()
    }

    private fun findDateAndInformListener(x: Float, y: Float) {
        if (x < horizontalSpacing || x > right - horizontalSpacing) return

        val weekRow = (y / (rowHeight)).roundToInt()
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

        if (cal.get(Calendar.MONTH) == thisMonth.get(Calendar.MONTH)) {
            dateClickListener?.invoke(cal.time)
            selectedDate = null
            invalidate()
        } else if (selectedDate != null) removeTouchFeedBack()
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

    fun setPickups(items: List<WasteCalendarPickups>) {
        pickUps.clear()
        pickUps.addAll(items)

        invalidate()
    }

    fun onDateSelected(listener: ((Date) -> Unit)) {
        dateClickListener = listener
    }

    fun setPrimaryColor(@ColorInt color: Int) {
        colors = CalendarColors.fromPrimaryColor(color)
    }

    private fun calcLeft(dayPosition: Int) = horizontalSpacing + dateSize * dayPosition
    private fun calcTop() = calcBottom() - rowHeight
    private fun calcRight(dayPosition: Int) = calcLeft(dayPosition) + dateSize
    private fun calcBottom() = rowHeight * currentWeek + spaceBetweenRows * (currentWeek - 1)
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
