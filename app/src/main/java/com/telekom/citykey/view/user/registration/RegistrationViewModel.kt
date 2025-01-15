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

package com.telekom.citykey.view.user.registration

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
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.api.requests.RegistrationRequest
import com.telekom.citykey.utils.DateUtil
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.Validation
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.utils.extensions.toApiFormat
import com.telekom.citykey.view.NetworkingViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class RegistrationViewModel(
    private val userRepository: UserRepository,
) : NetworkingViewModel() {

    val registration: LiveData<FieldValidation> get() = _registration
    val passwordStrength: LiveData<PasswordStrength> get() = _passwordStrength
    val inputValidation: LiveData<Pair<String, FieldValidation>> get() = _inputValidation
    val validatedOkFields: LiveData<Unit> get() = _validateOkFields
    val stopLoading: LiveData<Unit> get() = _stopLoading
    val resendTooSoon: LiveData<String> get() = _resendTooSoon

    private val _registration: MutableLiveData<FieldValidation> = MutableLiveData()
    private val _passwordStrength: MutableLiveData<PasswordStrength> = MutableLiveData()
    private val _inputValidation: MutableLiveData<Pair<String, FieldValidation>> = MutableLiveData()
    private val _validateOkFields: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _stopLoading: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _resendTooSoon: SingleLiveEvent<String> = SingleLiveEvent()

    private val passwordWatcher: BehaviorSubject<CharSequence> = BehaviorSubject.create()
    private val secondPasswordWatcher: BehaviorSubject<Pair<String, String>> =
        BehaviorSubject.create()
    private lateinit var formattedHints: String
    private lateinit var hints: Array<String>
    private var email: String = ""
    private var passwordAwaitsValidation = false

    init {
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
                return RegFields.SEC_PASSWORD to FieldValidation(FieldValidation.OK, null)
            }
            passwords.first.startsWith(passwords.second) -> {
                return RegFields.SEC_PASSWORD to FieldValidation(FieldValidation.IDLE, null)
            }
            else -> R.string.r_001_registration_error_password_no_match
        }
        return RegFields.SEC_PASSWORD to FieldValidation(FieldValidation.ERROR, null, errorMsg)
    }

    fun onPasswordTextChanged(chars: CharSequence) {
        passwordWatcher.onNext(chars)
    }

    fun onEmailUpdated(email: String, password: String) {
        this.email = email
        val fieldsNotBlank = email.isNotBlank() && password.isNotBlank()
        if (passwordAwaitsValidation || fieldsNotBlank && email == password) passwordWatcher.onNext(password)
    }

    fun onEmailReady(email: String) {
        if (email.isEmpty()) {
            _inputValidation.value = RegFields.EMAIL to FieldValidation(
                FieldValidation.ERROR,
                null,
                R.string.r_001_registration_error_empty_field
            )
        }
        if (email.isNotEmpty() && !Validation.isEmailFormat(email)) {
            _inputValidation.value = RegFields.EMAIL to FieldValidation(
                FieldValidation.ERROR,
                null,
                R.string.r_001_registration_error_incorrect_email
            )
        }
    }

    fun onPostCodeReady(postCode: String) {
        if (postCode.isEmpty()) {
            _inputValidation.value = RegFields.POSTAL_CODE to FieldValidation(
                FieldValidation.ERROR,
                null,
                R.string.r_001_registration_error_empty_field
            )
        }
        if (postCode.isNotEmpty() && postCode.length < 5) {
            _inputValidation.value = RegFields.POSTAL_CODE to FieldValidation(
                FieldValidation.ERROR,
                null,
                R.string.r_001_registration_error_incorrect_postcode
            )
        }
    }

    fun onSecondPasswordTextChanged(passwords: Pair<String, String>) {
        secondPasswordWatcher.onNext(passwords)
    }

    fun onRegisterButtonClicked(
        email: String,
        password: String,
        date: String,
        postalCode: String
    ) {
        var dayOfBirth: String? = null

        if (date.isNotEmpty()) {
            dayOfBirth = DateUtil.stringToDate(date, DateUtil.FORMAT_DD_MMMM_YYYY)
                .toApiFormat()
        }

        val registrationRequest = RegistrationRequest(
            password = password,
            email = email.lowercase(),
            dateOfBirth = dayOfBirth,
            postalCode = postalCode
        )
        registerUser(registrationRequest)
    }

    private fun registerUser(registrationRequest: RegistrationRequest) {
        launch {
            userRepository.register(registrationRequest)
                .retryOnError(this::onError, retryDispatcher, pendingRetries)
                .subscribe(
                    {
                        _registration.postValue(FieldValidation(FieldValidation.OK, it.content.postalCode))
                        _validateOkFields.value = Unit
                    },
                    this::onError
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

        return RegFields.PASSWORD to when {
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

    private fun onError(throwable: Throwable) {
        when (throwable) {
            is NetworkException -> {
                (throwable.error as OscaErrorResponse).errors.forEach {
                    when (it.errorCode) {
                        ErrorCodes.REGISTRATION_DATE_INVALID,
                        ErrorCodes.REGISTRATION_USER_TOO_YOUNG ->
                            _inputValidation.value =
                                RegFields.BIRTHDAY to FieldValidation(FieldValidation.ERROR, it.userMsg)
                        ErrorCodes.REGISTRATION_EMAIL_EXIST,
                        ErrorCodes.REGISTRATION_EMAIL_INVALID,
                        ErrorCodes.REGISTRATION_EMAIL_NOT_VERIFIED ->
                            _inputValidation.value =
                                RegFields.EMAIL to FieldValidation(FieldValidation.ERROR, it.userMsg)
                        ErrorCodes.REGISTRATION_PASSWORD_INVALID ->
                            _inputValidation.value =
                                RegFields.PASSWORD to FieldValidation(FieldValidation.ERROR, it.userMsg)
                        ErrorCodes.REGISTRATION_POSTALCODE_INVALID ->
                            _inputValidation.value =
                                RegFields.POSTAL_CODE to FieldValidation(FieldValidation.ERROR, it.userMsg)
                        ErrorCodes.EMAIL_RESEND_SOON -> _resendTooSoon.postValue(it.userMsg)
                        else -> _technicalError.value = Unit
                    }
                }
                _validateOkFields.value = Unit
                _stopLoading.value = Unit
            }
            is NoConnectionException -> _showRetryDialog.call()
            else -> {
                _technicalError.value = Unit
            }
        }
    }
}
