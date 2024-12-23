package com.telekom.citykey.domain.services.fahrradparken

import android.annotation.SuppressLint
import android.graphics.Bitmap
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.ServicesRepository
import com.telekom.citykey.models.api.requests.FahrradparkenRequest
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
class FahrradparkenServiceInteractor(
    private val globalData: GlobalData, private val servicesRepository: ServicesRepository
) {

    private val fahrradparkenCategoriesSubject: BehaviorSubject<List<DefectCategory>> = BehaviorSubject.create()

    val fahrradparkenCategoriesObservable: Observable<List<DefectCategory>>
        get() = fahrradparkenCategoriesSubject.hide()

    init {
        globalData.city
            .distinctUntilChanged { c1, c2 -> c1.cityId == c2.cityId }
            .subscribe { fahrradparkenCategoriesSubject.onNext(mutableListOf()) }
    }

    fun loadCategories(): Completable =
        if (fahrradparkenCategoriesSubject.value.isNullOrEmpty()) {
            servicesRepository.getFahrradparkenCategories(globalData.currentCityId)
                .doOnSuccess(fahrradparkenCategoriesSubject::onNext)
                .flatMapCompletable {
                    if (it.isEmpty()) {
                        Completable.error(Exception("Could not retrieve categories for Fahrradparken"))
                    } else {
                        Completable.complete()
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
        } else {
            Completable.complete()
        }

    fun createFahrradparkenReport(fahrradparkenRequest: FahrradparkenRequest, image: Bitmap?): Maybe<DefectSuccess> {
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
                    servicesRepository.createFahrradparkenReport(
                        fahrradparkenRequest.apply { mediaUrl = responseMediaUrl },
                        globalData.currentCityId
                    )
                }
        } else {
            servicesRepository.createFahrradparkenReport(fahrradparkenRequest, globalData.currentCityId)
        }
    }

    fun getExistingReports(serviceCode: String, boundingBox: String, reportCountLimit: Int) =
        servicesRepository.getFahrradparkenExistingReports(
            globalData.currentCityId, serviceCode, boundingBox, reportCountLimit
        )

}
