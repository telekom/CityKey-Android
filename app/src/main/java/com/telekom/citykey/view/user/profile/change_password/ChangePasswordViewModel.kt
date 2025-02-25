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
 * In accordance with Sections 4 and 6 of the License, the following exclusions apply:
 *
 *  1. Trademarks & Logos – The names, logos, and trademarks of the Licensor are not covered by this License and may not be used without separate permission.
 *  2. Design Rights – Visual identities, UI/UX designs, and other graphical elements remain the property of their respective owners and are not licensed under the Apache License 2.0.
 *  3: Non-Coded Copyrights – Documentation, images, videos, and other non-software materials require separate authorization for use, modification, or distribution.
 *
 * These elements are not considered part of the licensed Work or Derivative Works unless explicitly agreed otherwise. All elements must be altered, removed, or replaced before use or distribution. All rights to these materials are reserved, and Contributor accepts no liability for any infringing use. By using this repository, you agree to indemnify and hold harmless Contributor against any claims, costs, or damages arising from your use of the excluded elements.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0 AND LicenseRef-Deutsche-Telekom-Brand
 * License-Filename: LICENSES/Apache-2.0.txt LICENSES/LicenseRef-Deutsche-Telekom-Brand.txt
 */

package com.telekom.citykey.view.user.profile.change_password

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StrikethroughSpan
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.R
import com.telekom.citykey.common.ErrorCodes.CHANGE_PASSWORD_OLD_PASSWORD_WRONG
import com.telekom.citykey.common.ErrorCodes.CHANGE_PASSWORD_SAME_EMAIL
import com.telekom.citykey.common.ErrorCodes.PASSWORD_FORMAT_ERROR
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.custom.views.passwordstrength.PasswordStrength
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.api.requests.PasswordChangeRequest
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.Validation
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import com.telekom.citykey.view.user.login.LogoutReason
import com.telekom.citykey.view.user.registration.RegFields
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class ChangePasswordViewModel(
    private val globalData: GlobalData,
    private val userRepository: UserRepository
) : NetworkingViewModel() {

    val passwordStrength: LiveData<PasswordStrength> get() = _passwordStrength
    val saveSuccessful: LiveData<Boolean> get() = _saveSuccessful
    val logUserOut: LiveData<Unit> get() = _logUserOut
    val inputValidation: LiveData<Pair<String, FieldValidation>> get() = _inputValidation
    val stopLoading: LiveData<Unit> get() = _stopLoading

    private val _passwordStrength: MutableLiveData<PasswordStrength> = MutableLiveData()
    private val _saveSuccessful: MutableLiveData<Boolean> = MutableLiveData()
    private val _logUserOut: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _inputValidation: MutableLiveData<Pair<String, FieldValidation>> = MutableLiveData()
    private val _stopLoading: SingleLiveEvent<Unit> = SingleLiveEvent()

    private val newPasswordWatcher: BehaviorSubject<CharSequence> = BehaviorSubject.create()
    private val repeatPasswordWatcher: BehaviorSubject<Pair<String, String>> = BehaviorSubject.create()

    private lateinit var formattedHints: String
    private lateinit var hints: Array<String>

    companion object {
        private const val CHANGE_PASSWORD_API_TAG = "CHANGE_PASSWORD"
    }

    private var userEmail: String? = null
    private var passwordAwaitsValidation = false

    init {
        launch {
            newPasswordWatcher.subscribeOn(Schedulers.io())
                .map(CharSequence::toString)
                .map(this::validatePassword)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_inputValidation::setValue)
        }

        launch {
            repeatPasswordWatcher.subscribeOn(Schedulers.io())
                .map(this::validateSecondPassword)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_inputValidation::setValue)
        }

        launch {
            globalData.user
                .subscribe { userEmail = (it as? UserState.Present)?.profile?.email }
        }
    }

    fun onPasswordTextChanged(chars: CharSequence) {
        newPasswordWatcher.onNext(chars)
    }

    fun onSecondPasswordTextChanged(passwords: Pair<String, String>) {
        repeatPasswordWatcher.onNext(passwords)
    }

    private fun validatePassword(password: String): Pair<String, FieldValidation> {
        val spannableString = SpannableStringBuilder(formattedHints)
        var hintsCompleted = if (password.isEmpty()) 0 else 1

        if (password.length > 7) {
            val indexOfFirstHint = formattedHints.indexOf(hints[0])
            spannableString.setSpan(
                StrikethroughSpan(),
                indexOfFirstHint,
                indexOfFirstHint + hints[0].length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            hintsCompleted++
        }

        if (Validation.containsSpecialSymbols(password)) {
            val indexOfSecondHint = formattedHints.indexOf(hints[1])
            spannableString.setSpan(
                StrikethroughSpan(),
                indexOfSecondHint,
                indexOfSecondHint + hints[1].length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            hintsCompleted++
        }

        if (Validation.containsDigit(password)) {
            val indexOfThirdHint = formattedHints.indexOf(hints[2])
            spannableString.setSpan(
                StrikethroughSpan(),
                indexOfThirdHint,
                indexOfThirdHint + hints[2].length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            hintsCompleted++
        }

        passwordAwaitsValidation = userEmail?.isNotBlank() == true && password == userEmail

        val percentage: Float = hintsCompleted.toFloat() / (hints.size.toFloat() + 1) * 100

        _passwordStrength.postValue(
            PasswordStrength(
                spannableString,
                percentage.toInt()
            )
        )

        return ChangePassField.PASSWORD to when {
            percentage == 0f ->
                FieldValidation(FieldValidation.ERROR, null, R.string.r_001_registration_error_empty_field)
            passwordAwaitsValidation ->
                FieldValidation(FieldValidation.ERROR, null, R.string.r_001_registration_error_password_email_same)
            percentage < 99f ->
                FieldValidation(FieldValidation.ERROR, null, R.string.r_001_registration_error_password_too_weak)
            else ->
                FieldValidation(FieldValidation.SUCCESS, "")
        }
    }

    private fun validateSecondPassword(
        passwords: Pair<String, String>
    ): Pair<String, FieldValidation> {
        val errorMsg: Int = when {
            passwords.second.isEmpty() -> R.string.r_001_registration_error_empty_field
            passwords.second.isNotEmpty() && passwords.second == passwords.first -> {
                return RegFields.SEC_PASSWORD to FieldValidation(FieldValidation.OK, null)
            }
            passwords.first.startsWith(passwords.second) -> {
                return RegFields.SEC_PASSWORD to FieldValidation(FieldValidation.IDLE, null)
            }
            else -> R.string.r_001_registration_error_password_no_match
        }
        return ChangePassField.SEC_PASSWORD to FieldValidation(FieldValidation.ERROR, null, errorMsg)
    }

    fun onPasswordHintsResourcesReceived(hints: Array<String>, formattedHints: String) {
        this.formattedHints = formattedHints
        this.hints = hints
    }

    fun onSaveButtonClicked(actualPwd: String, newPwd: String) {
        launch {
            userRepository.changePassword(PasswordChangeRequest(actualPwd, newPwd))
                .retryOnError(this::onError, retryDispatcher, pendingRetries, CHANGE_PASSWORD_API_TAG)
                .subscribe(
                    {
                        _saveSuccessful.postValue(true)
                        globalData.logOutUser(LogoutReason.NO_LOGOUT_REASON)
                    },
                    this::onError
                )
        }
    }

    fun onError(throwable: Throwable) {
        when (throwable) {
            is InvalidRefreshTokenException -> handleInvalidRefreshTokenException(throwable.reason)
            is NetworkException -> handleNetworkException(throwable)
            is NoConnectionException -> _showRetryDialog.postValue(CHANGE_PASSWORD_API_TAG)
            else -> _technicalError.postValue(Unit)
        }
        _stopLoading.value = Unit
    }

    private fun handleInvalidRefreshTokenException(reason: LogoutReason) {
        globalData.logOutUser(reason)
        _logUserOut.postValue(Unit)
    }

    private fun handleNetworkException(throwable: NetworkException) {
        (throwable.error as OscaErrorResponse).errors.forEach {
            when (it.errorCode) {
                CHANGE_PASSWORD_OLD_PASSWORD_WRONG ->
                    _inputValidation.value =
                        ChangePassField.CURRENT_PASSWORD to FieldValidation(FieldValidation.ERROR, it.userMsg)
                CHANGE_PASSWORD_SAME_EMAIL, PASSWORD_FORMAT_ERROR ->
                    _inputValidation.value =
                        ChangePassField.PASSWORD to FieldValidation(FieldValidation.ERROR, it.userMsg)
                else -> _technicalError.postValue(Unit)
            }
        }
    }
}
