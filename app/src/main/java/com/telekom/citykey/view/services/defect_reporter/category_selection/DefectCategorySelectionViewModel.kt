package com.telekom.citykey.view.services.defect_reporter.category_selection

import androidx.lifecycle.LiveData
import androidx.lifecycle.toLiveData
import com.telekom.citykey.domain.services.defect_reporter.DefectReporterInteractor
import com.telekom.citykey.models.defect_reporter.DefectCategory
import com.telekom.citykey.view.NetworkingViewModel
import io.reactivex.BackpressureStrategy

class DefectCategorySelectionViewModel(private val defectReporterInteractor: DefectReporterInteractor) :
    NetworkingViewModel() {

    val defectReporterCategories: LiveData<List<DefectCategory>> get() =
        defectReporterInteractor.defectCategoriesObservable
            .toFlowable(BackpressureStrategy.LATEST)
            .toLiveData()
}
