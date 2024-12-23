package com.telekom.citykey.view.services.citizen_surveys

import com.telekom.citykey.models.citizen_survey.Survey

sealed class SurveyListItem {
    class Header(val titleResId: Int) : SurveyListItem()
    object NoRunningSurveys : SurveyListItem()
    class Item(val survey: Survey) : SurveyListItem()
}
