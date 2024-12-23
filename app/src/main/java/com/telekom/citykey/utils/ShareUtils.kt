package com.telekom.citykey.utils

import android.content.Intent

object ShareUtils {
    fun createShareIntent(title: String, url: String, shareHeader: String): Intent {
        return Intent.createChooser(
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, createText(title, url, shareHeader))
                putExtra(Intent.EXTRA_TITLE, title) // relevant for >= Android 10
                putExtra(Intent.EXTRA_SUBJECT, title)
                type = "text/plain"
            },
            null
        )
    }

    private fun createText(title: String, url: String, shareHeader: String) = StringBuilder()
        .appendLine(title)
        .appendLine(url)
        .appendLine()
        .appendLine(shareHeader)
        .toString()
}
