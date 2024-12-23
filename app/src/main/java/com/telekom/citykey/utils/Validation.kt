package com.telekom.citykey.utils

import android.util.Patterns
import java.util.regex.Pattern

object Validation {

    fun containsDigit(password: String) =
        Pattern.compile(".*[0-9].*").matcher(password).matches()

    fun containsSpecialSymbols(password: String): Boolean {
        password.forEach { if (!it.isLetter() && !it.isDigit()) return true }
        return false
    }

    fun isEmailFormat(email: String) = Patterns.EMAIL_ADDRESS.matcher(email).matches()
}
