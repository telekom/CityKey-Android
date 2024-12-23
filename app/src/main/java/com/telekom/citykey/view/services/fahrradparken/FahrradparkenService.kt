package com.telekom.citykey.view.services.fahrradparken

object FahrradparkenService {
    const val REPORT_STATUS_ICON_ERROR = "Error"
    const val REPORT_STATUS_ICON_DONE = "Done"
    const val REPORT_STATUS_ICON_IN_PROGRESS = "In progress"
    const val REPORT_STATUS_ICON_QUEUED = "Queued"

    const val SERVICE_PARAM_MORE_INFO_BASE_URL = "field_moreInformationBaseURL"

    object Input {
        const val TYPE_OPTIONAL = "OPTIONAL"
        const val TYPE_REQUIRED = "REQUIRED"
        const val TYPE_NOT_REQUIRED = "NOT REQUIRED"

        object Field {
            const val FIRST_NAME = "field_firstName"
            const val LAST_NAME = "field_lastName"
            const val EMAIL = "field_email"
            const val YOUR_CONCERN = "field_yourConcern"
            const val UPLOAD_IMAGE = "field_uploadImage"
            const val CHECK_BOX_TERMS_2 = "field_checkBoxTerms2"
        }
    }
}
