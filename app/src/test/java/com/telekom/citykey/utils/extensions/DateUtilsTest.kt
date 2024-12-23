package com.telekom.citykey.utils.extensions

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.Calendar
import java.util.Date

class DateUtilsTest {
    @Test
    fun `isDateInFuture check for today's date`() {
        val expectedDate = Date()
        val isDateInFuture = expectedDate.isInFuture
        Assertions.assertEquals(false, isDateInFuture)
    }

    @Test
    fun `isDateInFuture1check for past date`() {
        val expectedDate = Date(1627776000000L) // 1st August 2021, 00:00:00 GMT
        val isDateInFuture = expectedDate.isInFuture
        Assertions.assertEquals(false, isDateInFuture)
    }
    @Test
    fun `isDateInFuture1check for tomorrow's date`() {
        val calendar = Calendar.getInstance()
        calendar.time = Date()  // Set current date
        calendar.add(Calendar.DAY_OF_YEAR, 1)  // Add one day
        val futureDate= calendar.time
        val isDateInFuture = futureDate.isInFuture
        Assertions.assertEquals(true, isDateInFuture)
    }
}