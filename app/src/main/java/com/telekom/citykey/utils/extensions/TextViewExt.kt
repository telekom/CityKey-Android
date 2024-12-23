package com.telekom.citykey.utils.extensions

import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.text.util.Linkify
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.util.LinkifyCompat
import com.telekom.citykey.domain.city.CityInteractor

fun AppCompatTextView.autoLinkAll(@ColorInt color: Int = CityInteractor.cityColorInt) {
    setLinkTextColor(color)
    LinkifyCompat.addLinks(this, Linkify.EMAIL_ADDRESSES and Linkify.WEB_URLS)

//    LinkifyCompat.addLinks(
//        this,
//        Patterns.PHONE,
//        "tel:",
//        { chars, _, _ -> chars.map { it.isDigit() }.size >= 6 },
//        Linkify.sPhoneNumberTransformFilter
//    )

    movementMethod = LinkMovementMethod.getInstance()
}

fun AppCompatTextView.linkify(text: CharSequence, @ColorInt color: Int = CityInteractor.cityColorInt) {
    setLinkTextColor(color)

    val currentSpans = (text as? Spanned)?.getSpans(0, text.length, URLSpan::class.java)

    val buffer = SpannableString(text)
    Linkify.addLinks(buffer, Linkify.EMAIL_ADDRESSES or Linkify.WEB_URLS)

    currentSpans?.forEach { span ->
        val end = text.getSpanEnd(span)
        val start = text.getSpanStart(span)
        buffer.setSpan(span, start, end, 0)
    }

    this.text = buffer

    movementMethod = LinkMovementMethod.getInstance()
}
