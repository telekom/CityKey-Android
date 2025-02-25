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
 * In accordance with Sections 4 and 6 of the License, the following exclusions apply:
 *
 *  1. Trademarks & Logos – The names, logos, and trademarks of the Licensor are not covered by this License and may not be used without separate permission.
 *  2. Design Rights – Visual identities, UI/UX designs, and other graphical elements remain the property of their respective owners and are not licensed under the Apache License 2.0.
 *  3: Non-Coded Copyrights – Documentation, images, videos, and other non-software materials require separate authorization for use, modification, or distribution.
 *
 * These elements are not considered part of the licensed Work or Derivative Works unless explicitly agreed otherwise. All elements must be altered, removed, or replaced before use or distribution. All rights to these materials are reserved, and Contributor accepts no liability for any infringing use. By using this repository, you agree to indemnify and hold harmless Contributor against any claims, costs, or damages arising from your use of the excluded elements.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0 AND LicenseRef-Deutsche-Telekom-Brand
 * License-Filename: LICENSES/Apache-2.0.txt LICENSES/LicenseRef-Deutsche-Telekom-Brand.txt
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
