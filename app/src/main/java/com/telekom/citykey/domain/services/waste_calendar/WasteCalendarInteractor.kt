package com.telekom.citykey.domain.services.waste_calendar

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.R
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.global_messager.GlobalMessages
import com.telekom.citykey.domain.repository.ServicesRepository
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.WasteItems
import com.telekom.citykey.models.api.requests.WasteCalendarRequest
import com.telekom.citykey.models.live_data.WasteCalendarData
import com.telekom.citykey.models.waste_calendar.*
import com.telekom.citykey.utils.extensions.isInPast
import com.telekom.citykey.utils.extensions.isToday
import com.telekom.citykey.utils.extensions.toCalendar
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit

class WasteCalendarInteractor(
    private val servicesRepository: ServicesRepository,
    private val globalData: GlobalData,
    private val globalMessages: GlobalMessages,
    private val wasteExportInteractor: WasteExportInteractor
) {
    private val filterOptions = mutableListOf<GetWasteTypeResponse>()
    private val _filters = mutableListOf<String>()
    private val _updateSelectedWasteCount: MutableLiveData<String> = MutableLiveData()
    private val _categories = mutableListOf<GetWasteTypeResponse>()
    private val _filterCategories = mutableListOf<GetWasteTypeResponse>()
    private val wasteCalendar = mutableListOf<WasteCalendarPickups>()
    private val wasteReminders = mutableListOf<WasteCalendarReminder>()
    private var streetName: String = ""
    private var houseNumber: String = ""
    private var lastShownCalendar: Calendar? = null
    private var forceRefresh = false
    var categoriesSize = 0
        private set

    private var filterDisposable: Disposable? = null

    private val _monthlyDataSubject: BehaviorSubject<WasteCalendarData> = BehaviorSubject.create()
    private val _ftuAddressSubject: PublishSubject<String> = PublishSubject.create()
    val ftuAddressSubject: Observable<WasteAddressState> = _ftuAddressSubject
        .debounce(400, TimeUnit.MILLISECONDS)
        .flatMap { address -> servicesRepository.getWasteAddressDetails(address, globalData.currentCityId) }
        .hide()
        .observeOn(AndroidSchedulers.mainThread())

    val monthlyDataSubject: Observable<WasteCalendarData> get() = _monthlyDataSubject.hide()
    val fullAddress: String get() = "$streetName $houseNumber"
    val address: Address get() = Address(streetName, houseNumber)
    val filters: List<String> get() = _filters
    val updateSelectedWasteCount: LiveData<String> get() = _updateSelectedWasteCount
    val categories: List<GetWasteTypeResponse> get() = _categories
    val filterCategories: List<GetWasteTypeResponse> get() = _filterCategories

    init {
        observeCity()
    }

    @SuppressLint("CheckResult")
    private fun observeCity() {
        globalData.city
            .distinctUntilChanged { t1, t2 -> t1.cityId == t2.cityId }
            .subscribe {
                clearData()
            }

        globalData.user
            .filter { it is UserState.Absent }
            .subscribe { clearData() }
    }

    private fun clearData() {
        filterOptions.clear()
        wasteCalendar.clear()
        wasteReminders.clear()
        _filters.clear()
        houseNumber = ""
        streetName = ""
        lastShownCalendar = null
    }

    fun setFilters(filters: List<String>) {
        this._filters.clear()
        this._filters.addAll(filters)
        getSelectedPickupsInFilterOption()
    }

    fun setCategoriesCount(categories: List<GetWasteTypeResponse>) {
        categoriesSize = categories.size
        _categories.clear()
        _categories.addAll(categories)

    }

    private fun setAddress(streetName: String, houseNumber: String) {
        this.streetName = streetName
        this.houseNumber = houseNumber
    }

    fun getData(streetName: String = this.streetName, houseNumber: String = this.houseNumber): Completable =
        if (wasteCalendar.isEmpty() || streetName != this.streetName || houseNumber != this.houseNumber || forceRefresh)
            servicesRepository
                .getWasteCalendar(WasteCalendarRequest(streetName, houseNumber), globalData.currentCityId)
                .doOnSuccess {
                    forceRefresh = false
                    wasteCalendar.clear()
                    wasteCalendar.addAll(it.calendar)
                    wasteReminders.clear()
                    wasteReminders.addAll(it.reminders)
                    setAddress(it.address.streetName, it.address.houseNumber)
                }
                .ignoreElement()
                .observeOn(AndroidSchedulers.mainThread())
        else Completable.complete()

    fun getFilterOptions(): Maybe<List<GetWasteTypeResponse>> = Maybe.just(filterOptions)
        .flatMap {
            if (it.isEmpty())
                servicesRepository.getWasteCalendarFilterOptions(globalData.currentCityId)
            else {
                Maybe.just(filterOptions)
            }

        }

    fun saveSelectedWastePickups(saveSelectedWastePickupRequest: SaveSelectedWastePickupRequest) =
        servicesRepository.saveSelectedWastePickups(saveSelectedWastePickupRequest, globalData.currentCityId)

    fun getSelectedWastePickups() =
        servicesRepository.getSelectedWastePickups(globalData.currentCityId)

    fun getDataForSelectedMonth(calendar: Calendar) {
        filterDisposable?.dispose()
        filterDisposable = Single.just(wasteCalendar)
            .subscribeOn(Schedulers.computation())
            .filter { it.isNotEmpty() }
            .map { createData(it, calendar) }
            .doOnSuccess { lastShownCalendar = calendar }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(_monthlyDataSubject::onNext)
    }

    fun exportCalendarEvents(account: CalendarAccount): Single<Int> =
        wasteExportInteractor.exportCalendarEvents(wasteCalendar, account, _filters)

    fun getCalendarsInfo() = wasteExportInteractor.getCalendarsInfo()

    private fun createData(wasteCalendarPickups: List<WasteCalendarPickups>, month: Calendar): WasteCalendarData {
        val monthData = wasteCalendarPickups.filter {
            it.date.toCalendar().get(Calendar.MONTH) == month.get(Calendar.MONTH) &&
                    it.date.toCalendar().get(Calendar.YEAR) == month.get(Calendar.YEAR)
        }.sortedBy { it.date }

        val waste = mutableListOf<WasteCalendarPickups>()

        monthData.forEach { data ->
            val filteredDate = data.wasteTypeList.filter {
                _filters.contains(it.wasteTypeId.toString())
            }
            if (filteredDate.isNotEmpty()) {
                waste.add(WasteCalendarPickups(data.date, filteredDate))
            }
        }

        val listItems = mutableListOf<WasteItems>()
        waste.filter { !it.date.isInPast || it.date.isToday }
            .forEach {
                listItems.add(WasteItems.DayItem(it.date))
                listItems.addAll(
                    it.wasteTypeList.map { pickUp ->
                        pickUp.hasReminder =
                            wasteReminders.any { reminder -> reminder.wasteTypeId == pickUp.wasteTypeId }
                        pickUp
                    }
                        .sortedBy { pickUp -> pickUp.wasteType }
                )
            }

        // (y2 - y1) * 12 + (m2 - m1) + 1
        val lastPickup = wasteCalendarPickups.sortedByDescending { it.date }[0].date.toCalendar()
        val currDate = Calendar.getInstance()
        val monthsBetween =
            (lastPickup.get(Calendar.YEAR) - currDate.get(Calendar.YEAR)) * 12 +
                    lastPickup.get(Calendar.MONTH) - currDate.get(Calendar.MONTH) + 1

        return WasteCalendarData(listItems, waste, globalData.cityColor, monthsBetween)
    }

    fun fetchAddressWaste(selectedStreet: String) {
        _ftuAddressSubject.onNext(selectedStreet)
    }

    fun findReminder(wasteTypeId: Int): Single<WasteCalendarReminder> = Single.just(wasteReminders)
        .subscribeOn(Schedulers.io())
        .map { reminders ->
            reminders.find { it.wasteTypeId == wasteTypeId }
                ?: WasteCalendarReminder(0, "07:00", sameDay = false, oneDayBefore = false, twoDaysBefore = false)
        }
        .observeOn(AndroidSchedulers.mainThread())

    @SuppressLint("CheckResult")
    fun saveReminder(reminder: WasteCalendarReminder) {
        servicesRepository.saveWasteCalendarReminder(reminder, globalData.currentCityId)
            .doOnSubscribe {
                wasteReminders.find { savedReminder -> savedReminder.wasteTypeId == reminder.wasteTypeId }
                    ?.let(wasteReminders::remove)
                if (reminder.sameDay || reminder.oneDayBefore || reminder.twoDaysBefore) wasteReminders.add(reminder)
                lastShownCalendar?.let(this::getDataForSelectedMonth)
            }
            .onErrorResumeNext {
                globalMessages.displayToast(R.string.wc_005_error_toast_message)
                forceRefresh = true
                getData()
            }
            .doOnComplete { lastShownCalendar?.let(this::getDataForSelectedMonth) }
            .onErrorComplete()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    fun appliyFilter(applyfilter: ArrayList<String>) {
        _filters.clear()
        _filters.addAll(applyfilter)
        getSelectedPickupsInFilterOption()
        _updateSelectedWasteCount.postValue(filters.size.toString())
    }

    fun getSelectedPickupsInFilterOption() {
        _filterCategories.clear()
        categories.forEach {
            if (filters.contains(it.id.toString())) {
                _filterCategories.add(it)
            }
        }
    }
}
