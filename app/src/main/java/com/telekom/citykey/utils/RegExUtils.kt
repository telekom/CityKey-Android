package com.telekom.citykey.utils

import androidx.core.util.PatternsCompat
import java.util.regex.Pattern

object RegExUtils {
    private const val HTML_A_TAG_REG_EX = "(?i)<a([^>]+)>(.+?)</a>"
    private const val HTML_A_HREF_TAG_REG_EX = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))"

    private const val PHONE_NUMBER = "((\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4})" +
            "|((\\+\\d{1,3}( )?)?(\\d{3}[ ]?){2}\\d{3})" +
            "|((\\+\\d{1,3}( )?)?(\\d{3}[ ]?)(\\d{2}[ ]?){2}\\d{2})" +
            "|((0\\d{3,4}[- .]?)?\\d{3}[- .]?\\d{3})"
    // Valid Phone Numbers = {"2055550125","202 555 0125", "(202) 555-0125", "+111 (202) 555-0125", "636 856 789",
    // "+111 636 856 789", "636 85 67 89", "+111 636 85 67 89", "02055 550 125"}

    private const val HEX_COLOR = "#(([0-9a-fA-F]{2}){3,4}|([0-9a-fA-F]){3,4})\\b"

    const val PHONE_URI_PREFIX = "tel:"
    const val EMAIL_URI_PREFIX = "mailto:"

    val anchorTag: Pattern by lazy { Pattern.compile(HTML_A_TAG_REG_EX) }
    val anchorTagHrefValue: Pattern by lazy { Pattern.compile(HTML_A_HREF_TAG_REG_EX) }
    val emailAddress: Pattern by lazy { PatternsCompat.EMAIL_ADDRESS }
    val webUrl: Pattern by lazy { PatternsCompat.WEB_URL }
    val phoneNumber: Pattern by lazy { Pattern.compile(PHONE_NUMBER) }
    val hexColor: Pattern by lazy { Pattern.compile(HEX_COLOR) }
}
