package com.telekom.citykey.domain.services.defect_reporter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.ServicesRepository
import com.telekom.citykey.models.api.requests.DefectRequest
import com.telekom.citykey.models.defect_reporter.DefectCategory
import com.telekom.citykey.models.defect_reporter.DefectSuccess
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

@SuppressLint("CheckResult")
class DefectReporterInteractor(private val servicesRepository: ServicesRepository, private val globalData: GlobalData) {

    private val defectCategoriesSubject: BehaviorSubject<List<DefectCategory>> = BehaviorSubject.create()

    val defectCategoriesObservable: Observable<List<DefectCategory>> get() = defectCategoriesSubject.hide()

    init {
        globalData.city
            .distinctUntilChanged { c1, c2 -> c1.cityId == c2.cityId }
            .subscribe {
                defectCategoriesSubject.onNext(mutableListOf())
            }
    }

    fun loadCategories(): Completable =
        if (defectCategoriesSubject.value?.isEmpty() == false)
            Completable.complete()
        else
            servicesRepository.getDefectCategories(globalData.currentCityId)
                .doOnSuccess(defectCategoriesSubject::onNext)
                .flatMapCompletable {
                    if (it.isEmpty()) {
                        Completable.error(Exception("Retrieved no Defect Categories"))
                    } else Completable.complete()
                }
                .observeOn(AndroidSchedulers.mainThread())

    fun sendDefectRequest(defectRequest: DefectRequest, image: Bitmap?): Maybe<DefectSuccess> {
        return if (image != null) {
            Maybe.just(image)
                .subscribeOn(Schedulers.io())
                .map {
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                    byteArrayOutputStream.toByteArray().toRequestBody()
                }
                .flatMap { servicesRepository.uploadImage(globalData.currentCityId, it) }
                .flatMap { responseMediaUrl ->
                    servicesRepository.reportDefect(
                        defectRequest.apply { mediaUrl = responseMediaUrl },
                        globalData.currentCityId
                    )
                }
        } else {
            servicesRepository.reportDefect(defectRequest, globalData.currentCityId)
        }
    }
}
