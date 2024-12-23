package com.telekom.citykey.domain.city.events

import androidx.annotation.StringRes
import com.telekom.citykey.models.content.Event

sealed class EventsListItem {
    class EventItem(val event: Event) : EventsListItem()
    class Header(@StringRes val title: Int) : EventsListItem()
}
