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

package com.telekom.citykey.domain.mailbox

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import com.telekom.citykey.R
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.global_messager.GlobalMessages
import com.telekom.citykey.domain.notifications.notification_badges.InAppNotificationsInteractor
import com.telekom.citykey.domain.repository.OscaRepository
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.user.InfoBoxContent
import com.telekom.citykey.view.infobox.InfoBoxStates
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class MailboxManager(
    private val globalData: GlobalData,
    private val oscaRepository: OscaRepository,
    private val globalMessages: GlobalMessages,
    private val inAppNotificationsInteractor: InAppNotificationsInteractor
) {

    private val _userInfoBox: BehaviorSubject<List<InfoBoxContent>> = BehaviorSubject.create()
    private val _errors: BehaviorSubject<Throwable> = BehaviorSubject.create()

    val userInfoBox: Observable<List<InfoBoxContent>>
        get() =
            _userInfoBox.hide().subscribeOn(Schedulers.io())

    val errorState: Observable<InfoBoxStates>
        get() =
            _errors.hide().subscribeOn(Schedulers.io())
                .map {
                    when (it) {
                        is InvalidRefreshTokenException -> {
                            globalData.logOutUser(it.reason)
                            InfoBoxStates.OK
                        }
                        is NoConnectionException -> if (_userInfoBox.hasValue())
                            InfoBoxStates.REFRESH_ERROR else InfoBoxStates.NO_INTERNET
                        else -> InfoBoxStates.ANYTHING_ELSE
                    }
                }

    private var getMessagesDisposable: Disposable? = null

    private var lastDeletedMsg: InfoBoxContent? = null

    init {
        observeUser()
    }

    @SuppressLint("CheckResult")
    private fun observeUser() {
        globalData.user
            .map { it is UserState.Present }
            .subscribe {
                if (it) refreshInfoBox()
                else {
                    _userInfoBox.onNext(emptyList())
                    setupUpdates()
                }
            }
    }

    fun refreshInfoBox() {
        getMessagesDisposable?.dispose()
        getMessagesDisposable = oscaRepository.getMailBox()
            .doOnSuccess { setupUpdates(it.filter { msg -> !msg.isRead }.size) }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError {
                if (_userInfoBox.hasValue() && it !is NoConnectionException && it !is InvalidRefreshTokenException)
                    globalMessages.displayToast(R.string.b_005_infobox_error_info_title)
            }
            .subscribe(_userInfoBox::onNext, _errors::onNext)
    }

    private fun setInformationReadLocal(msgId: Int, read: Boolean) {
        _userInfoBox.value?.apply { find { it.userInfoId == msgId }?.isRead = read }
            ?.also(_userInfoBox::onNext)
        setupUpdates()
    }

    fun toggleInfoRead(msgId: Int, read: Boolean): Completable =
        oscaRepository.setMailRead(msgId, !read)
            .doOnSubscribe { setInformationReadLocal(msgId, !read) }
            .doOnError {
                Handler(Looper.myLooper()!!).postDelayed({ setInformationReadLocal(msgId, read) }, 500)

                if (it !is NoConnectionException)
                    globalMessages.displayToast(
                        if (read) R.string.b_006_snackbar_mark_unread_failed
                        else R.string.b_006_snackbar_mark_read_failed
                    )
            }

    fun deleteMessage(msg: InfoBoxContent): Completable =
        oscaRepository.deleteMail(msg.userInfoId, true)
            .doOnSubscribe {
                lastDeletedMsg = msg

                _userInfoBox.value?.toMutableList()?.let {
                    it.remove(msg)
                    _userInfoBox.onNext(it)
                    setupUpdates()
                }
            }
            .doOnError { t ->
                _userInfoBox.value?.toMutableList()?.let {
                    it.add(lastDeletedMsg!!)
                    _userInfoBox.onNext(it.sortedByDescending { info -> info.creationDate })
                    setupUpdates()
                }

                if (t !is NoConnectionException)
                    globalMessages.displayToast(R.string.b_006_snackbar_delete_failed)
            }

    fun restoreDeletedMsg(): Completable =
        if (lastDeletedMsg == null) Completable.complete()
        else oscaRepository.deleteMail(lastDeletedMsg!!.userInfoId, false)
            .doOnSubscribe {
                _userInfoBox.value?.toMutableList()?.let {
                    it.add(lastDeletedMsg!!)
                    _userInfoBox.onNext(it.sortedByDescending { info -> info.creationDate })
                    setupUpdates()
                }
            }
            .observeOn(AndroidSchedulers.mainThread())

    private fun setupUpdates(count: Int = _userInfoBox.value?.filter { !it.isRead }?.size ?: 0) {
        inAppNotificationsInteractor.setNotification(
            R.id.infobox_graph,
            "updates",
            count
        )
    }
}
