package com.telekom.citykey.utils.extensions

import com.adjust.sdk.AdjustEvent

fun AdjustEvent.addPartnerAndCallbackParameter(key: String, value: String) {
    addCallbackParameter(key, value)
    addPartnerParameter(key, value)
}
