package com.telekom.citykey.utils.extensions

import android.webkit.WebView
import androidx.annotation.ColorInt
import com.telekom.citykey.domain.city.CityInteractor

fun WebView.linkifyAndLoadNonHtmlTaggedData(text: String, @ColorInt color: Int = CityInteractor.cityColorInt) {
    val hex = String.format("#%06X", color and 0x00FFFFFF)

    val anchorTagStyle = "<style>\n" +
            "body, html { " +
            "width:100%; height: 100%;" +
            "margin: 0px; padding: 0px;" +
            "font-family: 'roboto-regular'; font-size:16px;" +
            "}\n" +
            "a {" +
            "color: $hex; " +  // Set the link color
            "text-decoration: underline; " +  // Add underline to links
            "}\n" +
            "</style>\n"

    val htmlContent = if (text.contains("</head>")) {
        val head = text.substringBefore("</head>")
        val body = text.substringAfter("</head>")
        head + "\n$anchorTagStyle</head>" + "\n${body.linkifyWithHtmlAnchor()}"
    } else {
        "<!DOCTYPE HTML>\n" +
                "<html>\n" +
                "<head>\n" +
                anchorTagStyle +
                "</head>\n" +
                "<body>\n" +
                text.linkifyWithHtmlAnchor() +
                "</body>\n" +
                "</html>"
    }

    settings.allowContentAccess = true
    loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
}

fun WebView.loadStyledHtml(text: String) {
    val generalStyle = "<style>\n" +
            "body, html { " +
            "margin: 0; padding: 0;" +
            "font-family: 'roboto-regular'; font-size:16px;" +
            "}\n" +
            "</style>\n"

    val htmlContent = if (text.contains("</head>")) {
        val head = text.substringBefore("</head>")
        val body = text.substringAfter("</head>")
        "$head\n$generalStyle</head>\n$body"
    } else {
        "<!DOCTYPE HTML>\n" +
                "<html>\n" +
                "<head>\n" +
                generalStyle +
                "</head>\n" +
                "<body>\n" +
                text +
                "</body>\n" +
                "</html>"
    }
    settings.allowContentAccess = true
    loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
}

fun WebView.loadBasicHtml(text: String) {
    loadDataWithBaseURL(null, text, "text/html", "utf-8", null)
}
