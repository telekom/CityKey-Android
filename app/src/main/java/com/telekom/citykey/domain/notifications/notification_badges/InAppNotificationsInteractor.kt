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
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

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
