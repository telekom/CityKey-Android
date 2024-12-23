package com.telekom.citykey.domain.notifications.notification_badges

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class InAppNotificationsInteractor {

    private val badgesMap: MutableMap<Int, MutableMap<String, Int>> = mutableMapOf()
    private val _badgesCounter: BehaviorSubject<Map<Int, Map<String, Int>>> = BehaviorSubject.create()

    val badgesCounter: Observable<Map<Int, Map<String, Int>>>
        get() = _badgesCounter.hide()

    fun setNotification(tabId: Int, function: String, count: Int) {
        setBadgesCount(NotificationItem(tabId, function, count))
        _badgesCounter.onNext(badgesMap)
    }

    private fun setBadgesCount(notificationItem: NotificationItem) {
        if (!badgesMap.containsKey(notificationItem.tabId)) {
            badgesMap[notificationItem.tabId] = mutableMapOf(notificationItem.function to notificationItem.count)
        } else {
            badgesMap[notificationItem.tabId]!![notificationItem.function] = notificationItem.count
        }
    }

    private class NotificationItem(val tabId: Int, val function: String, val count: Int)
}
