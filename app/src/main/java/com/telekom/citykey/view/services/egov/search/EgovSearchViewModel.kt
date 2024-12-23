package com.telekom.citykey.view.services.egov.search

import androidx.lifecycle.toLiveData
import com.telekom.citykey.domain.services.egov.EgovInterractor
import com.telekom.citykey.models.egov.EgovService
import com.telekom.citykey.view.BaseViewModel
import io.reactivex.BackpressureStrategy

class EgovSearchViewModel(private val egovInterractor: EgovInterractor) : BaseViewModel() {

    val searchResults get() = egovInterractor.egovSearchResultsObservable
        .toFlowable(BackpressureStrategy.LATEST)
        .toLiveData()

    fun onSearchQueryChanged(text: String) = egovInterractor.searchForKeywords(text)

    fun onServiceSelected(service: EgovService) = egovInterractor.saveServiceInHistory(service)
}
