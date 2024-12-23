@file:Suppress("MatchingDeclarationName")

package com.telekom.citykey.utils.extensions

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.core.text.HtmlCompat

inline fun EditText.textWatcher(init: KTextWatcher.() -> Unit) =
    addTextChangedListener(KTextWatcher().apply(init))

class KTextWatcher : TextWatcher {

    val EditText.isEmpty
        get() = text.isEmpty()

    val EditText.isNotEmpty
        get() = text.isNotEmpty()

    val EditText.isBlank
        get() = text.isBlank()

    val EditText.isNotBlank
        get() = text.isNotBlank()

    private var _beforeTextChanged: ((CharSequence?, Int, Int, Int) -> Unit)? = null
    private var _onTextChanged: ((CharSequence?, Int, Int, Int) -> Unit)? = null
    private var _afterTextChanged: ((Editable?) -> Unit)? = null

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        _beforeTextChanged?.invoke(s, start, count, after)
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        _onTextChanged?.invoke(s, start, before, count)
    }

    override fun afterTextChanged(s: Editable?) {
        _afterTextChanged?.invoke(s)
    }

    fun beforeTextChanged(listener: (CharSequence?, Int, Int, Int) -> Unit) {
        _beforeTextChanged = listener
    }

    fun onTextChanged(listener: (CharSequence?, Int, Int, Int) -> Unit) {
        _onTextChanged = listener
    }
}

fun TextView.setHtmlText(html: String) {
    text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
}
