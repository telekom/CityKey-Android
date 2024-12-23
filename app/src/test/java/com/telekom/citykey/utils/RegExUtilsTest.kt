package com.telekom.citykey.utils

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RegExUtilsTest {

    @Test
    fun testAnchorTag() {
        val testString = "<a href=\"http://example.com\">Example</a>"
        val matcher = RegExUtils.anchorTag.matcher(testString)
        assertTrue(matcher.find())
    }

    @Test
    fun testAnchorTagHrefValue() {
        val testString = "<a href=\"http://example.com\">Example</a>"
        val matcher = RegExUtils.anchorTagHrefValue.matcher(testString)
        assertTrue(matcher.find())
    }

    @Test
    fun testEmailAddress() {
        val validEmails = listOf("test@example.com", "user.name+tag+sorting@example.com", "user@example.co.in")
        val invalidEmails = listOf("plainaddress", "@missingusername.com", "username@.com")

        validEmails.forEach { email ->
            val matcher = RegExUtils.emailAddress.matcher(email)
            assertTrue(matcher.matches(), "Expected valid email: $email")
        }

        invalidEmails.forEach { email ->
            val matcher = RegExUtils.emailAddress.matcher(email)
            assertFalse(matcher.matches(), "Expected invalid email: $email")
        }
    }

    @Test
    fun testWebUrl() {
        val validUrls = listOf("http://example.com", "https://example.com", "http://www.example.com")
        val invalidUrls = listOf("htt://example.com", "http:/example.com")

        validUrls.forEach { url ->
            val matcher = RegExUtils.webUrl.matcher(url)
            assertTrue(matcher.matches(), "Expected valid URL: $url")
        }

        invalidUrls.forEach { url ->
            val matcher = RegExUtils.webUrl.matcher(url)
            assertFalse(matcher.matches(), "Expected invalid URL: $url")
        }
    }

    @Test
    fun testPhoneNumber() {
        val validNumbers = listOf(
            "2055550125", "202 555 0125", "(202) 555-0125", "+111 (202) 555-0125",
            "636 856 789", "+111 636 856 789", "636 85 67 89", "+111 636 85 67 89",
            "02055 550 125"
        )
        val invalidNumbers = listOf(
            "12345", "555-0125", "202-555-012", "+1 (202) 555-0125x1234"
        )

        validNumbers.forEach { number ->
            val matcher = RegExUtils.phoneNumber.matcher(number)
            assertTrue(matcher.matches(), "Expected valid phone number: $number")
        }

        invalidNumbers.forEach { number ->
            val matcher = RegExUtils.phoneNumber.matcher(number)
            assertFalse(matcher.matches(), "Expected invalid phone number: $number")
        }
    }

    @Test
    fun testHexColor() {
        val validColors = listOf("#FFF", "#FFFFFF", "#123456", "#1234", "#AABBCCDD")
        val invalidColors = listOf("FFF", "#FFFFFG", "#12345", "#AABBCCDDE")

        validColors.forEach { color ->
            val matcher = RegExUtils.hexColor.matcher(color)
            assertTrue(matcher.matches(), "Expected valid hex color: $color")
        }

        invalidColors.forEach { color ->
            val matcher = RegExUtils.hexColor.matcher(color)
            assertFalse(matcher.matches(), "Expected invalid hex color: $color")
        }
    }
}
