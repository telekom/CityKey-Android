package com.telekom.citykey.view.services

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.toLiveData
import com.telekom.citykey.R
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.global_messager.GlobalMessages
import com.telekom.citykey.domain.notifications.notification_badges.InAppNotificationsInteractor
import com.telekom.citykey.domain.services.main.ServicesInteractor
import com.telekom.citykey.domain.services.main.ServicesStates
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.content.City
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.view.BaseViewModel
import io.reactivex.BackpressureStrategy
import timber.log.Timber

class ServicesViewModel(
    private val globalData: GlobalData,
    private val servicesInteractor: ServicesInteractor,
    private val globalMessages: GlobalMessages,
    private val inAppNotificationsInteractor: InAppNotificationsInteractor
) : BaseViewModel() {

    val isUserLoggedIn: LiveData<Boolean> get() = _isUserLoggedIn
    val shouldPromptLogin: LiveData<Unit> get() = _shouldPromptLogin
    val cancelLoading: LiveData<Unit> get() = _cancelLoading
    val servicesData: LiveData<ServicesStates> get() = servicesInteractor.state.toLiveData()
    val cityData: LiveData<City> get() = globalData.city.toFlowable(BackpressureStrategy.LATEST).toLiveData()
    val updates: LiveData<Map<String, Int>>
        get() = inAppNotificationsInteractor.badgesCounter
            .filter { it.containsKey(R.id.services_graph) }
            .map { it.getValue(R.id.services_graph) }
            .toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()

    private val _isUserLoggedIn: MutableLiveData<Boolean> = MutableLiveData()
    private val _shouldPromptLogin: SingleLiveEvent<Unit> = SingleLiveEvent()
    private val _cancelLoading: SingleLiveEvent<Unit> = SingleLiveEvent()

    init {
        launch { globalData.user.map { it is UserState.Present }.subscribe(_isUserLoggedIn::postValue) }
        launch { servicesInteractor.errors.doOnNext(this::evaluateActiveErrors).subscribe() }
    }

    fun onRefresh() {
        launch {
            globalData.refreshContent().subscribe {
                _cancelLoading.value = Unit
            }
        }
    }

    private fun evaluateActiveErrors(throwable: Throwable) {
        Timber.e(throwable)
        if (servicesData.hasActiveObservers()) {
            globalMessages.displayToast(R.string.s_001_services_error_snackbar_msg)
            _cancelLoading.value = Unit
        }
    }

    fun reloadServicesIfNeeded() {
        if (globalData.shouldRefreshServices) {
            servicesInteractor.observeCity()
        }
    }
}
