package com.telekom.citykey.view.infobox

import com.telekom.citykey.models.user.InfoBoxContent

sealed class InfoboxItem {
    data class Mail(val item: InfoBoxContent) : InfoboxItem()
    object Empty : InfoboxItem()
}
