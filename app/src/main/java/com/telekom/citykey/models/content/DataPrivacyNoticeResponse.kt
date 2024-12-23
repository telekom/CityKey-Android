package com.telekom.citykey.models.content

import com.google.gson.annotations.SerializedName

data class DataPrivacyNoticeResponse(
    @SerializedName("dpn_text")
    var surveyDataPrivacyText: String
)