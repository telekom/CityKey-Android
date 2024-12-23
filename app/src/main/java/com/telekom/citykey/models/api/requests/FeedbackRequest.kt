package com.telekom.citykey.models.api.requests

import com.telekom.citykey.BuildConfig

class FeedbackRequest(
    val aboutApp: String,
    val improvementOnApp: String,
    val currentCityName: String,
    val osInfo: String = "Android:${android.os.Build.VERSION.RELEASE}(${android.os.Build.VERSION.SDK_INT})-App-Version:${BuildConfig.VERSION_NAME}",
    val email: String
)
