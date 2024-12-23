package com.telekom.citykey.view.services.waste_calendar.address_change

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.toLiveData
import com.telekom.citykey.R
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.services.waste_calendar.WasteAddressState
import com.telekom.citykey.domain.services.waste_calendar.WasteCalendarInteractor
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.waste_calendar.Address
import com.telekom.citykey.models.waste_calendar.FtuWaste
import com.telekom.citykey.utils.SingleLiveEvent
import com.telekom.citykey.utils.extensions.retryOnError
import com.telekom.citykey.view.NetworkingViewModel
import io.reactivex.BackpressureStrategy

class WasteCalendarAddressViewModel(
    private val wasteCalendarInteractor: WasteCalendarInteractor,
    private val globalData: GlobalData
) : NetworkingViewModel() {

    val wasteCalendarAvailable: LiveData<Unit> get() = _wasteCalendarAvailable
    val ftuAddress: LiveData<List<String>> get() = _ftuAddress
    val inputValidation: LiveData<Pair<String, FieldValidation>> get() = _inputValidation
    val error: LiveData<FieldValidation> get() = _error
    val cityData: LiveData<String> get() = MutableLiveData(globalData.cityName)
    val availableHouseNumbers: LiveData<List<String>> get() = _availableHouseNumbers
    val currentAddress: LiveData<Address> get() = _currentAddress
    val userLoggedOut: LiveData<Unit>
        get() = globalData.user
            .filter { it is UserState.Absent }
            .map { Unit }
            .toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()

    private val _availableHouseNumbers: MutableLiveData<List<String>> = SingleLiveEvent()
    private val _error: MutableLiveData<FieldValidation> = SingleLiveEvent()
    private val _inputValidation: MutableLiveData<Pair<String, FieldValidation>> = MutableLiveData()
    private val _ftuAddress: MutableLiveData<List<String>> = MutableLiveData()
    private val _wasteCalendarAvailable: MutableLiveData<Unit> = SingleLiveEvent()
    private val _currentAddress: MutableLiveData<Address> = MutableLiveData()

    private val houseNumberSuggestions = mutableListOf<String>()
    private val streetSuggestions = mutableListOf<FtuWaste>()
    private var streetName = ""
    private var houseNumber = ""

    init {
        _currentAddress.postValue(wasteCalendarInteractor.address)

        launch {
            wasteCalendarInteractor.ftuAddressSubject
                .subscribe {
                    when (it) {
                        is WasteAddressState.Error -> onError(it.throwable)
                        is WasteAddressState.Success -> updateList(it.items)
                    }
                }
        }
    }

    fun onOpenWasteCalendarClicked(streetName: String, houseNumber: String) {
        launch {
            wasteCalendarInteractor.getData(streetName, houseNumber)
                .retryOnError(this::onError, retryDispatcher, pendingRetries, "wasteData")
                .subscribe(
                    {
                        _wasteCalendarAvailable.postValue(Unit)
                    },
                    this::onError
                )
        }
    }

    private fun updateList(list: List<FtuWaste>) {
        streetSuggestions.clear()
        streetSuggestions.addAll(list)
        _ftuAddress.postValue(list.map { it.streetName })
        validateStreetName(streetName)
    }

    fun onStreetTextChanged(selectedStreet: String, houseNumber: String) {
        this.houseNumber = houseNumber
        if (selectedStreet.isNotBlank()) {
            wasteCalendarInteractor.fetchAddressWaste(selectedStreet)
            streetName = selectedStreet
        }
    }

    private fun validateStreetName(streetName: String) {
        if (streetSuggestions.map { it.streetName.lowercase() }.contains(streetName.lowercase())) {
            _inputValidation.value = "streetName" to FieldValidation(FieldValidation.OK, null)
            houseNumberSuggestions.clear()
            streetSuggestions.find { it.streetName == streetName }?.let {
                houseNumberSuggestions.addAll(it.houseNumberList)
            }
            onHouseNumberChange(houseNumber)
            _availableHouseNumbers.postValue(houseNumberSuggestions)
        } else {
            _inputValidation.value = "streetName" to FieldValidation(
                FieldValidation.ERROR,
                null,
                R.string.wc_004_ftu_street_error
            )
            _availableHouseNumbers.postValue(emptyList())
        }
    }

    fun onHouseNumberChange(houseNumber: String) {
        _inputValidation.value = "houseNumber" to when {
            houseNumber.isBlank() -> FieldValidation(FieldValidation.IDLE, null)
            houseNumberSuggestions.contains(houseNumber) -> FieldValidation(FieldValidation.OK, null)
            houseNumberSuggestions.isEmpty() -> FieldValidation(FieldValidation.IDLE, null)
            else -> FieldValidation(FieldValidation.ERROR, null, R.string.wc_004_ftu_house_error)
        }
    }

    private fun onError(throwable: Throwable) {
        when (throwable) {
            is NetworkException -> {
                (throwable.error as OscaErrorResponse).errors.forEach {
                    when (it.errorCode) {
                        ErrorCodes.CALENDAR_NOT_EXIST -> _error.postValue(
                            FieldValidation(
                                FieldValidation.ERROR,
                                it.userMsg
                            )
                        )
                        else -> _technicalError.postValue(Unit)
                    }
                }
            }
            else -> _technicalError.postValue(Unit)
        }
    }
}
