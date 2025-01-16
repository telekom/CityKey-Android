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

package com.telekom.citykey.view.user.forgot_password

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StrikethroughSpan
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.R
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.custom.views.passwordstrength.PasswordStrength
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.api.requests.NewPasswordRequest
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.Validation
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class ForgotPasswordViewModel(
    private val globalData: GlobalData,
    private val userRepository: UserRepository
) : NetworkingViewModel() {

    val userEmail: LiveData<String> get() = _userEmail
    val inputValidation: LiveData<Pair<String, FieldValidation>> get() = _inputValidation
    val passwordStrength: LiveData<PasswordStrength> get() = _passwordStrength
    val openVerifyEmail: LiveData<String?> get() = _openVerifyEmail
    val openPinVerification: LiveData<Unit> get() = _openPinVerification

    private val _userEmail: MutableLiveData<String> = MutableLiveData()
    private val _inputValidation: MutableLiveData<Pair<String, FieldValidation>> = MutableLiveData()
    private val _passwordStrength: MutableLiveData<PasswordStrength> = MutableLiveData()
    private val _openVerifyEmail: SingleLiveEvent<String?> = SingleLiveEvent()
    private val _openPinVerification: SingleLiveEvent<Unit> = SingleLiveEvent()

    private val passwordWatcher: BehaviorSubject<CharSequence> = BehaviorSubject.create()
    private val secondPasswordWatcher: BehaviorSubject<Pair<String, String>> =
        BehaviorSubject.create()
    private lateinit var formattedHints: String
    private lateinit var hints: Array<String>
    private var email: String = ""
    private var passwordAwaitsValidation = false

    init {
        launch {
            globalData.user
                .subscribeOn(Schedulers.io())
                .filter { it is UserState.Present }
                .map { (it as UserState.Present).profile.email }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_userEmail::postValue)
        }

        launch {
            passwordWatcher.subscribeOn(Schedulers.io())
                .map(CharSequence::toString)
                .map(this::validatePassword)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_inputValidation::setValue)
        }

        launch {
            secondPasswordWatcher.subscribeOn(Schedulers.io())
                .map(this::validateSecondPassword)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(_inputValidation::setValue)
        }
    }

    fun onPasswordHintsResourcesReceived(hints: Array<String>, formattedHints: String) {
        this.formattedHints = formattedHints
        this.hints = hints
    }

    private fun validateSecondPassword(
        passwords: Pair<String, String>
    ): Pair<String, FieldValidation> {
        val errorMsg: Int = when {
            passwords.second.isEmpty() -> R.string.r_001_registration_error_empty_field
            passwords.second.isNotEmpty() && passwords.second == passwords.first -> {
                return ForgotPasswordFields.SEC_PASSWORD to FieldValidation(FieldValidation.OK, null)
            }
            passwords.first.startsWith(passwords.second) -> {
                return ForgotPasswordFields.SEC_PASSWORD to FieldValidation(FieldValidation.IDLE, null)
            }
            else -> R.string.r_001_registration_error_password_no_match
        }
        return ForgotPasswordFields.SEC_PASSWORD to FieldValidation(FieldValidation.ERROR, null, errorMsg)
    }

    fun onSecondPasswordTextChanged(passwords: Pair<String, String>) {
        secondPasswordWatcher.onNext(passwords)
    }

    fun onPasswordTextChanged(chars: CharSequence) {
        passwordWatcher.onNext(chars)
    }

    fun onEmailReady(email: String) {
        if (!Validation.isEmailFormat(email)) {
            _inputValidation.value = ForgotPasswordFields.EMAIL to FieldValidation(
                FieldValidation.ERROR,
                null,
                R.string.r_001_registration_error_incorrect_email
            )
        }
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

        passwordAwaitsValidation = email.isNotBlank() && password == email

        val percentage: Float = hintsCompleted.toFloat() / (hints.size.toFloat() + 1) * 100

        _passwordStrength.postValue(
            PasswordStrength(
                spannableString,
                percentage.toInt()
            )
        )

        return ForgotPasswordFields.PASSWORD to when {
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

    fun onResetClicked(email: String, password: String) {
        launch {
            userRepository.resetPassword(NewPasswordRequest(email.lowercase(), password))
                .retryOnError(this::onError, retryDispatcher, pendingRetries, "RECOVERY")
                .subscribe({ _openPinVerification.value = Unit }, this::onError)
        }
    }

    fun onError(throwable: Throwable) {
        when (throwable) {
            is NetworkException -> {
                (throwable.error as OscaErrorResponse).errors.forEach {
                    when (it.errorCode) {
                        ErrorCodes.FORGOT_PASSWORD_EMAIL_INVALID,
                        ErrorCodes.FORGOT_PASSWORD_EMAIL_NOT_EXIST ->
                            _inputValidation.value =
                                ForgotPasswordFields.EMAIL to FieldValidation(FieldValidation.ERROR, it.userMsg)
                        ErrorCodes.FORGOT_PASSWORD_PASSWORD_INVALID ->
                            _inputValidation.value =
                                ForgotPasswordFields.PASSWORD to FieldValidation(FieldValidation.ERROR, it.userMsg)
                        ErrorCodes.FORGOT_PASSWORD_EMAIL_NOT_VERIFIED ->
                            _openVerifyEmail.value = null
                        ErrorCodes.EMAIL_RESEND_SOON ->
                            _openVerifyEmail.value = it.userMsg
                        else -> _technicalError.value = Unit
                    }
                }
            }
            is NoConnectionException -> _showRetryDialog.postValue(null)
            else -> _technicalError.value = Unit
        }
    }
}
