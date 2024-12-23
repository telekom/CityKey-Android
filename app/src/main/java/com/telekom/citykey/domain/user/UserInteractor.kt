package com.telekom.citykey.domain.user

import androidx.lifecycle.LiveData
import com.telekom.citykey.domain.repository.OAuth2TokenManager
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.models.api.requests.LogInRequest
import com.telekom.citykey.models.api.requests.LogOutRequest
import com.telekom.citykey.models.content.UserProfile
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.view.user.login.LogoutReason
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

class UserInteractor(
    private val userRepository: UserRepository,
    private val preferencesHelper: PreferencesHelper,
    private val oAuth2TokenManager: OAuth2TokenManager
): KoinComponent {
    private var userLogoutDisposable: Disposable? = null
    private val _user: BehaviorSubject<UserState> = BehaviorSubject.create()
    private val _unexpectedLogout: SingleLiveEvent<LogoutReason> = SingleLiveEvent()
    val user: Observable<UserState> get() = _user.hide()
    val unexpectedLogout: LiveData<LogoutReason> get() = _unexpectedLogout

    val isUserLoggedIn get() = _user.value is UserState.Present
    val userCityName get() = (_user.value as? UserState.Present)?.profile?.cityName
    val userCityId get() = (_user.value as? UserState.Present)?.profile?.homeCityId
    val userId get() = (_user.value as? UserState.Present)?.profile?.accountId
    val hasAcceptedDpn get() = (_user.value as? UserState.Present)?.profile?.dpnAccepted ?: true

    val selectedCityId get() = preferencesHelper.getSelectedCityId()

    private val adjustManager: AdjustManager by inject()

    fun updatePersonalDataLocally(newPostalCode: String, newCityName: String, newCityId: Int) {
        (_user.value as? UserState.Present)?.profile?.apply {
            postalCode = newPostalCode
            cityName = newCityName
            homeCityId = newCityId
        }?.also {
            preferencesHelper.saveUserProfile(it)
            adjustManager.updateMoEngageUserAttributes()
            _user.onNext(UserState.Present(it))
        }
    }

    fun updateUser(): Maybe<UserProfile> = userRepository.getProfile()
        .doOnError(Timber::e)
        .doOnSuccess { oAuth2TokenManager.updateUserId(it.accountId) }
        .doOnSuccess {
            preferencesHelper.saveUserProfile(it)
            adjustManager.updateMoEngageUserAttributes()
            _user.onNext(UserState.Present(it))
        }

    fun logUserIn(request: LogInRequest, stayLoggedIn: Boolean) = userRepository.login(request, stayLoggedIn)
        .doOnSuccess { oAuth2TokenManager.updateCredentials(it, stayLoggedIn) }
        .flatMap { updateUser() }
        .doOnError { logOutUser(LogoutReason.NO_LOGOUT_REASON) }

    fun logOutUser(logoutReason: LogoutReason = LogoutReason.TECHNICAL_LOGOUT) {
        if (logoutReason == LogoutReason.TECHNICAL_LOGOUT || logoutReason == LogoutReason.TOKEN_EXPIRED_LOGOUT) {
            _unexpectedLogout.postValue(logoutReason)
        }
        if (isUserLoggedIn) _user.onNext(UserState.Absent)
        preferencesHelper.setLogoutReason(logoutReason)
        preferencesHelper.togglePreviewMode(false)
        preferencesHelper.setUserPostalCode("")
        preferencesHelper.clearUserProfile()
        adjustManager.updateMoEngageUserAttributes()
        notifyBackendAboutLogout()
    }

    private fun notifyBackendAboutLogout() {
        oAuth2TokenManager.refreshToken?.let {
            val token = it
            oAuth2TokenManager.logOut()
            if (token.isNotBlank()) {
                userLogoutDisposable?.dispose()
                userLogoutDisposable = userRepository.logout(LogOutRequest(token))
            }
        }
    }

    fun setUserAbsent() {
        _user.onNext(UserState.Absent)
    }

    fun isPreviewMode(): Boolean = preferencesHelper.isPreviewMode

    fun togglePreviewMode(shouldEnable: Boolean) = preferencesHelper.togglePreviewMode(shouldEnable)
}
