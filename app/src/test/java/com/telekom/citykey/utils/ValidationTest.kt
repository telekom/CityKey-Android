package com.telekom.citykey.utils

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ValidationTest {
    @Test
    fun testContainsDigit() {
        // Test cases where password contains digits
        assertTrue(Validation.containsDigit("password1"))
        assertTrue(Validation.containsDigit("12345"))
        assertTrue(Validation.containsDigit("pass123word"))

        // Test cases where password does not contain digits
        assertFalse(Validation.containsDigit("password"))
        assertFalse(Validation.containsDigit("word"))
        assertFalse(Validation.containsDigit("!@#"))
    }

    @Test
    fun testContainsSpecialSymbols() {
        // Test cases where password contains special symbols
        assertTrue(Validation.containsSpecialSymbols("password!"))
        assertTrue(Validation.containsSpecialSymbols("p@ssw0rd"))
        assertTrue(Validation.containsSpecialSymbols("!@#$%^&*()"))

        // Test cases where password does not contain special symbols
        assertFalse(Validation.containsSpecialSymbols("password"))
        assertFalse(Validation.containsSpecialSymbols("123456"))
        assertFalse(Validation.containsSpecialSymbols("word123"))
    }

}