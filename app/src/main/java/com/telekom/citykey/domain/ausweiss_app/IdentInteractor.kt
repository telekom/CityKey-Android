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

package com.telekom.citykey.domain.ausweiss_app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.nfc.Tag
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import com.governikus.ausweisapp2.IAusweisApp2Sdk
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class IdentInteractor(private val context: Context) {

    private companion object {
        const val SERVICE_NAME = "com.governikus.ausweisapp2.START_SERVICE"
    }

    val enableNfc = MutableLiveData<Boolean>()
    val state: Observable<IdentState>
        get() = _state
            .debounce(300L, TimeUnit.MILLISECONDS)
            .hide()

    private val _state: PublishSubject<IdentState> = PublishSubject.create()

    private var sdk: IAusweisApp2Sdk? = null
    private var sdkConnection: ServiceConnection? = null
    private var messageHandler = IdentMessageReceiver(this::handleResponse)
    private val serviceIntent: Intent get() = Intent(SERVICE_NAME).apply { setPackage(context.packageName) }

    private val accessRights = mutableListOf<String>()
    private var pendingCmd = ""
    private var isCardAvailable = false
    private var isCardBlocked = false

    fun startIdentification(tokenURL: String) {
        if (sdkConnection != null) return
        isCardBlocked = false
        isCardAvailable = false

        Timber.i("Binding auth service... ")

        sdkConnection = object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                Timber.i("Service Connected")
                try {
                    sdk = IAusweisApp2Sdk.Stub.asInterface(service)
                    if (!sdk!!.connectSdk(messageHandler)) {
                        // already connected? Handle error...
                        Timber.i("Connection Issue")
                    }
                    startIdent(tokenURL)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }

            override fun onServiceDisconnected(className: ComponentName) {
                Timber.i("Service Disconnected")
                sdk = null
            }
        }

        context.bindService(serviceIntent, sdkConnection!!, Context.BIND_AUTO_CREATE)
    }

    fun unBind() {
        enableNfc.postValue(false)
        sdkConnection?.let(context::unbindService)
        sdkConnection = null
    }

    private fun handleResponse(msg: IdentMsg) {
        when (msg) {
            is IdentMsg.AccessRights -> {
                accessRights.clear()
                accessRights.addAll(msg.rights)
                sendCMD(IdentConst.CMD_GET_CERTIFICATE)
            }

            is IdentMsg.Certificate -> {
                _state.onNext(IdentState.ShowInfo(accessRights, msg.certificateInfo, msg.certificateValidity))
            }

            is IdentMsg.Error -> {
                Timber.e(msg.msg)
                _state.onNext(IdentState.Error(null))
            }

            is IdentMsg.ErrorResult -> {
                _state.onNext(IdentState.Error(msg.result))
            }

            is IdentMsg.INSERT_CARD -> {
                requestNfcFeature()
                _state.onNext(IdentState.ATTACH_CARD)
            }

            is IdentMsg.CardRecognized -> {
                if (isCardBlocked) return

                if (msg.card == null) {
                    isCardAvailable = false
                } else {
                    isCardAvailable = true
                    if (pendingCmd.isNotEmpty()) {
                        send(pendingCmd)
                        pendingCmd = ""
                        _state.onNext(IdentState.LOADING)
                    }
                }
            }

            is IdentMsg.RequestPin -> _state.onNext(IdentState.InsertPin(msg.retries))
            is IdentMsg.REQUEST_CAN -> _state.onNext(IdentState.INSERT_CAN)
            is IdentMsg.REQUEST_PUK -> _state.onNext(IdentState.INSERT_PUK)
            is IdentMsg.Completed -> _state.onNext(IdentState.Success(msg.url))
            is IdentMsg.CARD_BLOCKED -> {
                isCardBlocked = true
                _state.onNext(IdentState.CARD_BLOCKED)
            }
        }
    }

    fun startIdent(tokenURL: String) {
        var finalUrl = tokenURL
        if (tokenURL.contains("servicekonto", ignoreCase = true)) {
            while (finalUrl.contains('%')) {
                val index = finalUrl.indexOf('%')
                val hex = finalUrl.substring(index + 1, index + 3)
                finalUrl = finalUrl.replace(
                    "%$hex", hex.toInt(16).toChar().toString()
                )
            }
        }
        enableNfc.postValue(true)
        send(
            buildCmdString(
                IdentConst.CMD_RUN_AUTH, IdentConst.PARAM_TCTOKEN to finalUrl
            )
        )
    }

    fun setPin(pin: String) {
        val cmd = buildCmdString(
            IdentConst.CMD_SET_PIN, IdentConst.PARAM_VALUE to pin
        )

        if (isCardAvailable) {
            send(cmd)
            pendingCmd = ""
        } else {
            pendingCmd = cmd
            requestNfcFeature()
            _state.onNext(IdentState.ATTACH_CARD)
        }
    }

    fun setPuk(puk: String) {
        val cmd = buildCmdString(
            IdentConst.CMD_SET_PUK, IdentConst.PARAM_VALUE to puk
        )

        if (isCardAvailable) {
            send(cmd)
            pendingCmd = ""
        } else {
            pendingCmd = cmd
            requestNfcFeature()
            _state.onNext(IdentState.ATTACH_CARD)
        }
    }

    fun setCan(can: String) {
        val cmd = buildCmdString(
            IdentConst.CMD_SET_CAN, IdentConst.PARAM_VALUE to can
        )

        if (isCardAvailable) {
            send(cmd)
            pendingCmd = ""
        } else {
            pendingCmd = cmd
            requestNfcFeature()
            _state.onNext(IdentState.ATTACH_CARD)
        }
    }

    fun sendCMD(cmd: String) {
        send(buildCmdString(cmd))
    }

    private fun send(cmd: String) {
        Timber.i("Sending command: $cmd")
        try {
            if (!sdk!!.send(messageHandler.mSessionID, cmd)) {
                Timber.e("Could not sendRaw command to SDK")
            }
        } catch (e: Exception) {
            Timber.e("Could not sendRaw command to SDK")
            Timber.e(e)
        }
    }

    fun dispatchNfcTag(tag: Tag) {
        try {
            sdk!!.updateNfcTag(messageHandler.mSessionID, tag)
        } catch (e: Exception) {
            Timber.i("An error occured updating/dispating a NFC Tag")
        }
    }

    private fun requestNfcFeature() {
        enableNfc.postValue(true)
    }

    private fun buildCmdString(cmd: String, payload: Pair<String, String>? = null): String {
        return "{\"${IdentConst.PARAM_CMD}\": \"${cmd}\" " + (if (payload != null) ", \"${payload.first}\": \"${payload.second}\"" else "") + "}"
    }
}
