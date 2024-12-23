package com.telekom.citykey.domain.repository

import com.telekom.citykey.models.api.requests.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class UserRepository(
    private val api: SmartCityApi,
    private val authApi: SmartCityAuthApi
) {

    fun login(request: LogInRequest, keepLoggedIn: Boolean) = api.login(request, keepLoggedIn = keepLoggedIn)
        .subscribeOn(Schedulers.io())
        .map { it.content }

    fun logout(request: LogOutRequest): Disposable = api.logout(request)
        .subscribeOn(Schedulers.io())
        .onErrorComplete()
        .subscribe()

    fun register(request: RegistrationRequest) = api.register(request)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun resendPIN(request: ResendPinRequest, actionName: String) = api.resendPINEmail(request, actionName = actionName)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun deleteUser(request: DeleteAccountRequest) = authApi.deleteUser(request)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun getProfile() = authApi.getUserProfile()
        .subscribeOn(Schedulers.io())
        .map { it.content }
        .observeOn(AndroidSchedulers.mainThread())

    fun confirmRegistration(request: PinConfirmationRequest, actionName: String) =
        api.setRegistrationConfirmation(request, actionName = actionName)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun resetPassword(request: NewPasswordRequest) = api.requestNewPassword(request)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun changePassword(request: PasswordChangeRequest) = authApi.changePassword(request)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun validatePostalCode(postalCode: String) = authApi.validatePostalCode(postalCode)
        .subscribeOn(Schedulers.io())
        .map { it.content }
        .observeOn(AndroidSchedulers.mainThread())

    fun changePersonalData(request: PersonalDetailChangeRequest, update: String) =
        authApi.changePersonalData(request, update)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    fun changeEmail(request: EmailChangeRequest) = authApi.changeEmail(request)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}
