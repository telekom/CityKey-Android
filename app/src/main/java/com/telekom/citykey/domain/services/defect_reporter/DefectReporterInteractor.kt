/**
 * Copyright (C) 2025 Deutsche Telekom AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

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
