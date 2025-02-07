/**
 * Copyright (C) 2025 Deutsche Telekom AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * In accordance with Sections 4 and 6 of the License, the following exclusions apply:
 *
 *  1. Trademarks & Logos – The names, logos, and trademarks of the Licensor are not covered by this License and may not be used without separate permission.
 *  2. Design Rights – Visual identities, UI/UX designs, and other graphical elements remain the property of their respective owners and are not licensed under the Apache License 2.0.
 *  3: Non-Coded Copyrights – Documentation, images, videos, and other non-software materials require separate authorization for use, modification, or distribution.
 *
 * These elements are not considered part of the licensed Work or Derivative Works unless explicitly agreed otherwise. All elements must be altered, removed, or replaced before use or distribution. All rights to these materials are reserved, and Contributor accepts no liability for any infringing use. By using this repository, you agree to indemnify and hold harmless Contributor against any claims, costs, or damages arising from your use of the excluded elements.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

package com.telekom.citykey.utils.extensions

import androidx.core.text.HtmlCompat
import com.telekom.citykey.utils.RegExUtils
import java.util.regex.Matcher

private const val HREF_PREFIX = "href=\""
private const val HTTPS_PREFIX = "tps://"
private const val HTTP_PREFIX = "ttp://"

fun String.bold() = "<b>$this</b>"

fun String.decodeHTML(flag: Int = HtmlCompat.FROM_HTML_MODE_LEGACY) = HtmlCompat.fromHtml(this, flag)

fun String.linkifyWithHtmlAnchor(): String {

    // Find and replace all anchored link in received text with blank string
    var htmlTextWithoutAnchoredLinks = filterText(this, RegExUtils.anchorTag.matcher(this))

    //function for Updating Tags for Email in html text
    var str: String = updateTagsForEmails(this, htmlTextWithoutAnchoredLinks)

    // Find and replace all non-anchored emails in htmlTextWithoutEmailsAndWebUrls with blank string
    htmlTextWithoutAnchoredLinks =
        filterText(htmlTextWithoutAnchoredLinks, RegExUtils.emailAddress.matcher(htmlTextWithoutAnchoredLinks))

    //function for Updating Tags for WebUrls in html text
    str = updateTagsForLinks(str, htmlTextWithoutAnchoredLinks)

    // Find and replace all non-anchored web urls in htmlTextWithoutEmailsAndWebUrls  with blank string
    htmlTextWithoutAnchoredLinks =
        filterText(htmlTextWithoutAnchoredLinks, RegExUtils.webUrl.matcher(htmlTextWithoutAnchoredLinks))

    //function for Updating Tags for phone number in html text
    str = updateTagsForPhoneNumber(str, htmlTextWithoutAnchoredLinks)
    return str
}

private fun updateTagsForEmails(htmlText: String, htmlTextWithoutAnchoredEmails: String): String {
    var updatedHtmlText = htmlText

    //find remaining emails other than anchored web urls and update them with formatted tags.
    val availableLinks = mutableSetOf<String>()
    val emailAddressMatcher = RegExUtils.emailAddress.matcher(htmlTextWithoutAnchoredEmails)
    while (emailAddressMatcher.find()) {
        val link = emailAddressMatcher.group()
        if (link.isNotBlank()) {
            availableLinks.add(link)
        }
    }
    availableLinks.forEach {
        val newLink = "<a href=\"${RegExUtils.EMAIL_URI_PREFIX}$it\">$it</a>"
        updatedHtmlText = updatedHtmlText.replace(it, newLink)
    }

    return updatedHtmlText
}

private fun updateTagsForLinks(
    htmlText: String, filteredHtmlTextWithoutEmailsAndAnchoredWebUrls: String
): String {
    var updatedHtmlText = htmlText

    //find remaining web urls other than anchored web urls and update them with formatted tags.
    val availableWebUrls = mutableSetOf<String>()
    val webUrlMatcher = RegExUtils.webUrl.matcher(filteredHtmlTextWithoutEmailsAndAnchoredWebUrls)
    while (webUrlMatcher.find()) {
        val link = webUrlMatcher.group()
        if (link.isNotBlank()) {
            availableWebUrls.add(link)
        }
    }

    availableWebUrls.forEach {
        val linkStartIndex = updatedHtmlText.indexOf(it, ignoreCase = true)
        val newText: String
        if (linkStartIndex < 6) {
            val link = if (it.startsWith("http")) it else "https://$it"
            newText = "<a href=\"$link\">$it</a>"
            updatedHtmlText = updatedHtmlText.replace(link, newText)
        } else {
            val checkSubStr = updatedHtmlText.substring(linkStartIndex - 6, linkStartIndex)
            if (checkSubStr != HREF_PREFIX && checkSubStr != HTTPS_PREFIX && checkSubStr != HTTP_PREFIX) {
                val link = if (it.startsWith("http")) it else "https://$it"
                newText = "<a href=\"$link\">$it</a>"
                updatedHtmlText = updatedHtmlText.replace(it, newText)
            } else if (checkSubStr != HREF_PREFIX && (it.startsWith(HTTPS_PREFIX) or it.startsWith(HTTP_PREFIX))) {
                newText = "<a href=\"$it\">$it</a>"
                updatedHtmlText = updatedHtmlText.replace(it, newText)
            }
        }
    }
    return updatedHtmlText
}

private fun updateTagsForPhoneNumber(htmlText: String, filteredHtmlTextWithoutEmailsAndWebUrls: String): String {
    val phoneNumberMatcher = RegExUtils.phoneNumber.matcher(filteredHtmlTextWithoutEmailsAndWebUrls)
    var updatedHtmlText = htmlText
    val links = mutableSetOf<String>()
    while (phoneNumberMatcher.find()) {
        val link = phoneNumberMatcher.group()
        if (link.isNotBlank()) links.add(link)
    }
    links.forEach {
        val newLink = "<a href=\"${RegExUtils.PHONE_URI_PREFIX}$it\">$it</a>"
        updatedHtmlText = updatedHtmlText.replace(it, newLink)
    }
    return updatedHtmlText
}

fun filterText(data: String, matcher: Matcher): String {
    var htmlTextWithoutAnchoredLinks = data
    while (matcher.find()) {
        val link = matcher.group()
        if (link.isNotBlank()) {
            htmlTextWithoutAnchoredLinks = htmlTextWithoutAnchoredLinks.replace(link, "")
        }
    }
    return htmlTextWithoutAnchoredLinks
}