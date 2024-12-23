package com.telekom.citykey.view.services.fahrradparken.category_selection

import androidx.lifecycle.LiveData
import androidx.lifecycle.toLiveData
import com.telekom.citykey.domain.services.fahrradparken.FahrradparkenServiceInteractor
import com.telekom.citykey.models.defect_reporter.DefectCategory
import com.telekom.citykey.view.NetworkingViewModel
import io.reactivex.BackpressureStrategy

class FahrradparkenCategorySelectionViewModel(
    private val fahrradparkenServiceInteractor: FahrradparkenServiceInteractor
) : NetworkingViewModel() {

    val fahrradparkenCategories: LiveData<List<DefectCategory>>
        get() = fahrradparkenServiceInteractor.fahrradparkenCategoriesObservable
            .toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()

}
